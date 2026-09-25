package com.dan.caracalla.db;

import com.dan.caracalla.regie.CameraPath;
import com.dan.caracalla.regie.Clip;
import com.dan.caracalla.regie.Timeline;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Zugriff auf die Tabellen <code>CAR_</code>. Alles wird beim Start einmal gelesen; geschrieben
 * werden nur gemerkte Blicke und selbst angelegte Fahrten. Jede Verbindung ist kurz und wird
 * gleich wieder geschlossen, damit die App nie an einer hängenden Sitzung klebt.
 */
public final class CaracallaDb {
    private final DbConfig cfg;

    public CaracallaDb(DbConfig cfg) { this.cfg = cfg; }

    public DbConfig config() { return cfg; }

    private Connection connect() throws SQLException {
        DriverManager.setLoginTimeout(4);
        Connection c = DriverManager.getConnection(cfg.url, cfg.user, cfg.password);
        c.setAutoCommit(false);
        return c;
    }

    // ------------------------------------------------------------ Datentypen

    public static final class Stone {
        public String code, name, nameAntik, art, farbe, beschreibung, inDenThermen, quelle;
        public String quarryAntik, quarryHeute, land, wegText;
        public int wegKm;
        public double[] lon = new double[0], lat = new double[0];
        public double quarryLon, quarryLat;
    }

    public static final class Event {
        public int jahr;
        public Integer jahrBis;
        public String datierung, titel, text, quelle, raum;
        public double zeit;
    }

    public static final class RoomInfo {
        public String code, name, funktion;
        public Double tempLuft, tempWasser, nachhall;
    }

    public static final class View {
        public long id;
        public String name, modus = "ORBIT", notiz;
        public double ex, ey, ez, lx, ly, lz;
        public int day;
        public double hour, haze;
        public boolean roof = true, cut, thermo, water;
        public double cutX, zeit;

        @Override public String toString() { return name; }
    }

    /** Alles, was die App beim Start liest. */
    public static final class Snapshot {
        public final List<Timeline> flights = new ArrayList<>();
        public Timeline tour, dayScript;
        public final Map<String, Stone> stones = new HashMap<>();
        /** Materialnummer → Steincodes in der Reihenfolge der Zuordnung. */
        public final Map<Integer, List<String>> matStones = new HashMap<>();
        public final List<Event> events = new ArrayList<>();
        public final Map<String, RoomInfo> rooms = new HashMap<>();
        public final List<View> views = new ArrayList<>();
    }

    // ------------------------------------------------------------ Lesen

    public Snapshot load() throws SQLException {
        Snapshot s = new Snapshot();
        try (Connection c = connect()) {
            readTimelines(c, s);
            readStones(c, s);
            readEvents(c, s);
            readRooms(c, s);
            s.views.addAll(readViews(c));
        }
        return s;
    }

    private static Double dbl(ResultSet r, String col) throws SQLException {
        double v = r.getDouble(col);
        return r.wasNull() ? null : v;
    }

    private void readTimelines(Connection c, Snapshot s) throws SQLException {
        Map<Long, Timeline> tls = new LinkedHashMap<>();
        Map<Long, String> art = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT timeline_id, name, art, tag_im_jahr, wasserweg FROM car_timeline ORDER BY reihenfolge NULLS LAST, timeline_id")) {
            while (r.next()) {
                Timeline tl = new Timeline(r.getString("name"));
                int d = r.getInt("tag_im_jahr");
                if (!r.wasNull()) tl.day = d;
                tl.waterPath = "J".equals(r.getString("wasserweg"));
                tls.put(r.getLong("timeline_id"), tl);
                art.put(r.getLong("timeline_id"), r.getString("art"));
            }
        }
        Map<Long, CameraPath> paths = new LinkedHashMap<>();
        Map<Long, Object[]> clipInfo = new LinkedHashMap<>();
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT clip_id, timeline_id, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text FROM car_clip ORDER BY timeline_id, pos")) {
            while (r.next()) {
                long id = r.getLong("clip_id");
                paths.put(id, new CameraPath());
                clipInfo.put(id, new Object[]{r.getLong("timeline_id"), r.getString("harter_schnitt"), dbl(r, "stunde_von"), dbl(r, "stunde_bis"),
                        dbl(r, "dunst"), r.getString("titel"), r.getString("text")});
            }
        }
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z FROM car_key ORDER BY clip_id, t_s")) {
            while (r.next()) {
                CameraPath p = paths.get(r.getLong(1));
                if (p != null) p.key(r.getDouble(2), r.getDouble(3), r.getDouble(4), r.getDouble(5), r.getDouble(6), r.getDouble(7), r.getDouble(8));
            }
        }
        for (Map.Entry<Long, Object[]> e : clipInfo.entrySet()) {
            CameraPath p = paths.get(e.getKey());
            if (p.size() == 0) continue;
            Object[] o = e.getValue();
            Timeline tl = tls.get((Long) o[0]);
            if (tl == null) continue;
            Clip cl = new Clip(p);
            cl.cut = "J".equals(o[1]);
            if (o[2] != null && o[3] != null) cl.sun((Double) o[2], (Double) o[3]);
            if (o[4] != null) cl.haze((Double) o[4]);
            if (o[5] != null) cl.caption((String) o[5], (String) o[6]);
            tl.add(cl);
        }
        for (Map.Entry<Long, Timeline> e : tls.entrySet()) {
            Timeline tl = e.getValue();
            if (tl.clips.isEmpty()) continue;
            switch (art.get(e.getKey())) {
                case "RUNDGANG": if (s.tour == null) s.tour = tl; break;
                case "DREHBUCH": if (s.dayScript == null) s.dayScript = tl; break;
                default: s.flights.add(tl);
            }
        }
    }

    private void readStones(Connection c, Snapshot s) throws SQLException {
        Map<String, Long> quarryOf = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT s.code, s.name, s.name_antik, s.art, s.farbe_hex, s.beschreibung, s.in_den_thermen, s.quelle, "
                     + "q.quarry_id, q.name_antik q_antik, q.name_heute, q.land_heute, q.weg_km, q.weg_text, q.lage.sdo_point.x qx, q.lage.sdo_point.y qy "
                     + "FROM car_stone s LEFT JOIN car_quarry q ON q.quarry_id = s.quarry_id")) {
            while (r.next()) {
                Stone t = new Stone();
                t.code = r.getString("code"); t.name = r.getString("name"); t.nameAntik = r.getString("name_antik");
                t.art = r.getString("art"); t.farbe = r.getString("farbe_hex"); t.beschreibung = r.getString("beschreibung");
                t.inDenThermen = r.getString("in_den_thermen"); t.quelle = r.getString("quelle");
                t.quarryAntik = r.getString("q_antik"); t.quarryHeute = r.getString("name_heute"); t.land = r.getString("land_heute");
                t.wegKm = r.getInt("weg_km"); t.wegText = r.getString("weg_text");
                t.quarryLon = r.getDouble("qx"); t.quarryLat = r.getDouble("qy");
                long qid = r.getLong("quarry_id");
                if (!r.wasNull()) quarryOf.put(t.code, qid);
                s.stones.put(t.code, t);
            }
        }
        // Wegpunkte als Zahlenreihe (ohne Objekt-Mapping des Treibers)
        Map<Long, List<double[]>> pts = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT q.quarry_id, v.id, v.x, v.y FROM car_quarry q, TABLE(MDSYS.SDO_UTIL.GETVERTICES(q.weg)) v ORDER BY q.quarry_id, v.id")) {
            while (r.next()) pts.computeIfAbsent(r.getLong(1), k -> new ArrayList<>()).add(new double[]{r.getDouble(3), r.getDouble(4)});
        }
        for (Stone t : s.stones.values()) {
            Long q = quarryOf.get(t.code);
            List<double[]> l = q == null ? null : pts.get(q);
            if (l == null) continue;
            t.lon = new double[l.size()]; t.lat = new double[l.size()];
            for (int i = 0; i < l.size(); i++) { t.lon[i] = l.get(i)[0]; t.lat[i] = l.get(i)[1]; }
        }
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT m.mat_no, s.code FROM car_mat_stone m JOIN car_stone s ON s.stone_id = m.stone_id ORDER BY m.mat_no, m.ROWID")) {
            while (r.next()) s.matStones.computeIfAbsent(r.getInt(1), k -> new ArrayList<>()).add(r.getString(2));
        }
    }

    private void readEvents(Connection c, Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT e.jahr, e.jahr_bis, e.datierung, e.titel, e.text, e.zeit, e.quelle, r.name raum "
                     + "FROM car_event e LEFT JOIN car_room r ON r.room_id = e.room_id ORDER BY e.zeit NULLS LAST, e.jahr")) {
            while (r.next()) {
                Event e = new Event();
                e.jahr = r.getInt("jahr");
                int b = r.getInt("jahr_bis");
                e.jahrBis = r.wasNull() ? null : b;
                e.datierung = r.getString("datierung"); e.titel = r.getString("titel"); e.text = r.getString("text");
                double z = r.getDouble("zeit");
                e.zeit = r.wasNull() ? -1 : z;
                e.quelle = r.getString("quelle"); e.raum = r.getString("raum");
                s.events.add(e);
            }
        }
    }

    private void readRooms(Connection c, Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT code, name, funktion, temp_luft_c, temp_wasser_c, nachhall_s FROM car_room")) {
            while (r.next()) {
                RoomInfo i = new RoomInfo();
                i.code = r.getString("code"); i.name = r.getString("name"); i.funktion = r.getString("funktion");
                i.tempLuft = dbl(r, "temp_luft_c"); i.tempWasser = dbl(r, "temp_wasser_c"); i.nachhall = dbl(r, "nachhall_s");
                s.rooms.put(i.name, i);
            }
        }
    }

    public List<View> readViews() throws SQLException {
        try (Connection c = connect()) { return readViews(c); }
    }

    private List<View> readViews(Connection c) throws SQLException {
        List<View> l = new ArrayList<>();
        try (Statement st = c.createStatement();
             ResultSet r = st.executeQuery("SELECT * FROM car_view ORDER BY view_id")) {
            while (r.next()) {
                View v = new View();
                v.id = r.getLong("view_id"); v.name = r.getString("name"); v.modus = r.getString("modus"); v.notiz = r.getString("notiz");
                v.ex = r.getDouble("eye_x"); v.ey = r.getDouble("eye_y"); v.ez = r.getDouble("eye_z");
                v.lx = r.getDouble("look_x"); v.ly = r.getDouble("look_y"); v.lz = r.getDouble("look_z");
                v.day = r.getInt("tag_im_jahr"); v.hour = r.getDouble("stunde"); v.haze = r.getDouble("dunst");
                v.roof = "J".equals(r.getString("dach")); v.cut = "J".equals(r.getString("schnitt"));
                v.cutX = r.getDouble("schnitt_x"); v.zeit = r.getDouble("zeit");
                v.thermo = "J".equals(r.getString("waermebild")); v.water = "J".equals(r.getString("wasserweg"));
                l.add(v);
            }
        }
        return l;
    }

    // ------------------------------------------------------------ Schreiben

    private static String jn(boolean b) { return b ? "J" : "N"; }

    /** Legt einen Blick an oder ersetzt den gleichnamigen. */
    public void saveView(View v) throws SQLException {
        try (Connection c = connect()) {
            try (PreparedStatement d = c.prepareStatement("DELETE FROM car_view WHERE name = ?")) {
                d.setString(1, v.name);
                d.executeUpdate();
            }
            try (PreparedStatement p = c.prepareStatement("INSERT INTO car_view (name, modus, eye_x, eye_y, eye_z, look_x, look_y, look_z, "
                    + "tag_im_jahr, stunde, dunst, dach, schnitt, schnitt_x, zeit, waermebild, wasserweg, notiz) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 1;
                p.setString(i++, v.name); p.setString(i++, v.modus);
                p.setDouble(i++, v.ex); p.setDouble(i++, v.ey); p.setDouble(i++, v.ez);
                p.setDouble(i++, v.lx); p.setDouble(i++, v.ly); p.setDouble(i++, v.lz);
                p.setInt(i++, v.day); p.setDouble(i++, Math.round(v.hour * 100) / 100.0); p.setDouble(i++, Math.round(v.haze * 100) / 100.0);
                p.setString(i++, jn(v.roof)); p.setString(i++, jn(v.cut)); p.setDouble(i++, Math.round(v.cutX * 10) / 10.0);
                p.setDouble(i++, Math.round(v.zeit * 100) / 100.0);
                p.setString(i++, jn(v.thermo)); p.setString(i++, jn(v.water));
                if (v.notiz == null) p.setNull(i, Types.VARCHAR); else p.setString(i, v.notiz);
                p.executeUpdate();
            }
            c.commit();
        }
    }

    public void deleteView(String name) throws SQLException {
        try (Connection c = connect(); PreparedStatement d = c.prepareStatement("DELETE FROM car_view WHERE name = ?")) {
            d.setString(1, name);
            d.executeUpdate();
            c.commit();
        }
    }

    /** Speichert eine eigene Fahrt (EINGEBAUT = N) mit einer Einstellung; liefert den Code. */
    public String saveTimeline(Timeline tl) throws SQLException {
        try (Connection c = connect()) {
            String code;
            try (Statement st = c.createStatement();
                 ResultSet r = st.executeQuery("SELECT COUNT(*) FROM car_timeline WHERE eingebaut = 'N'")) {
                r.next();
                code = "EIGEN_" + (r.getInt(1) + 1) + "_" + (System.currentTimeMillis() % 100000);
            }
            try (PreparedStatement p = c.prepareStatement("INSERT INTO car_timeline (code, name, art, tag_im_jahr, wasserweg, reihenfolge, eingebaut) "
                    + "VALUES (?, ?, 'FAHRT', ?, ?, (SELECT NVL(MAX(reihenfolge), 0) + 1 FROM car_timeline), 'N')")) {
                p.setString(1, code); p.setString(2, tl.name);
                if (tl.day > 0) p.setInt(3, tl.day); else p.setNull(3, Types.NUMERIC);
                p.setString(4, jn(tl.waterPath));
                p.executeUpdate();
            }
            int pos = 1;
            for (Clip cl : tl.clips) {
                try (PreparedStatement p = c.prepareStatement("INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text) "
                        + "SELECT timeline_id, ?, ?, ?, ?, ?, ?, ? FROM car_timeline WHERE code = ?")) {
                    p.setInt(1, pos); p.setString(2, jn(cl.cut));
                    setD(p, 3, cl.hour0); setD(p, 4, cl.hour1); setD(p, 5, cl.haze);
                    p.setString(6, cl.title); p.setString(7, cl.text); p.setString(8, code);
                    p.executeUpdate();
                }
                for (double[] k : cl.path.keys()) {
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z) "
                            + "SELECT c.clip_id, ?, ?, ?, ?, ?, ?, ? FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = ? AND c.pos = ?")) {
                        for (int i = 0; i < 7; i++) p.setDouble(i + 1, Math.round(k[i] * 1000) / 1000.0);
                        p.setString(8, code); p.setInt(9, pos);
                        p.executeUpdate();
                    }
                }
                pos++;
            }
            c.commit();
            return code;
        }
    }

    private static void setD(PreparedStatement p, int i, double v) throws SQLException {
        if (Double.isNaN(v)) p.setNull(i, Types.NUMERIC); else p.setDouble(i, Math.round(v * 100) / 100.0);
    }
}
