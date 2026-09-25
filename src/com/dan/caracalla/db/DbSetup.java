package com.dan.caracalla.db;

import com.dan.caracalla.regie.Clip;
import com.dan.caracalla.regie.Programme;
import com.dan.caracalla.regie.Timeline;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Einrichter für die Tabellen CAR_ im Schema aus db/db.properties.
 * <ul>
 *   <li>ohne Argument: 01_tabellen, 02_inhalte, 03_regie ausführen, dann prüfen</li>
 *   <li><code>neu</code>: vorher 99_abbau (alles mit CAR_ weg), dann wie oben</li>
 *   <li><code>pruefen</code>: nur den Durchstich</li>
 *   <li><code>abbau</code>: nur 99_abbau</li>
 * </ul>
 * Das Protokoll steht auf der Konsole und in db/einrichtung.txt.
 * In NetBeans: Rechtsklick auf die Datei, „Run File“ (ohne Argument).
 */
public final class DbSetup {
    private static PrintWriter log;
    private static int problems;

    public static void main(String[] a) throws Exception {
        String mode = a.length > 0 ? a[0] : "";
        DbConfig cfg = DbConfig.load();
        File out = new File("db/einrichtung.txt");
        out.getParentFile().mkdirs();
        try (PrintWriter w = new PrintWriter(out, StandardCharsets.UTF_8)) {
            log = w;
            say("Caracalla · Einrichtung der Datenbank · " + java.time.LocalDateTime.now().withNano(0));
            if (cfg == null) {
                say("FEHLER: db/db.properties fehlt oder ist unvollständig (url, user, password).");
                return;
            }
            say("Verbindung: " + cfg.url + " als " + cfg.user);
            try (Connection c = DriverManager.getConnection(cfg.url, cfg.user, cfg.password)) {
                c.setAutoCommit(false);
                if (mode.equals("neu") || mode.equals("abbau")) run(c, "db/99_abbau.sql");
                if (mode.isEmpty() || mode.equals("neu")) {
                    if (!run(c, "db/01_tabellen.sql")) { say("Abbruch nach 01_tabellen.sql."); return; }
                    if (!run(c, "db/02_inhalte.sql")) { say("Abbruch nach 02_inhalte.sql."); return; }
                    if (!run(c, "db/03_regie.sql")) { say("Abbruch nach 03_regie.sql."); return; }
                }
                if (!mode.equals("abbau")) check(c, cfg);
            }
            say(problems == 0 ? "ERGEBNIS: alles in Ordnung." : "ERGEBNIS: " + problems + " Problem(e), siehe oben.");
        } finally {
            System.out.println("Protokoll: " + out.getAbsolutePath());
        }
    }

    private static void say(String s) {
        System.out.println(s);
        log.println(s);
        log.flush();
    }

    /** Führt ein Skript aus; Anweisungen enden mit einer Zeile "/". */
    static boolean run(Connection c, String file) throws Exception {
        List<String> stmts = split(new String(Files.readAllBytes(new File(file).toPath()), StandardCharsets.UTF_8));
        say("");
        say("== " + file + " (" + stmts.size() + " Anweisungen)");
        int ok = 0;
        try (Statement st = c.createStatement()) {
            for (String s : stmts) {
                try {
                    st.execute(s);
                    ok++;
                } catch (SQLException e) {
                    problems++;
                    say("  FEHLER " + e.getErrorCode() + ": " + e.getMessage().trim());
                    say("  in: " + s.substring(0, Math.min(160, s.length())).replace('\n', ' '));
                    c.rollback();
                    return false;
                }
            }
        }
        c.commit();
        say("  " + ok + " ausgeführt");
        return true;
    }

    static List<String> split(String text) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String line : text.split("\r?\n")) {
            String t = line.trim();
            if (t.equals("/")) {
                String s = cur.toString().trim();
                if (!s.isEmpty()) out.add(s);
                cur.setLength(0);
                continue;
            }
            if (cur.length() == 0 && (t.isEmpty() || t.startsWith("--"))) continue;
            cur.append(line).append('\n');
        }
        return out;
    }

    // ------------------------------------------------------------ Durchstich

    private static void expect(Connection c, String what, String sql, long want) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(sql)) {
            r.next();
            long got = r.getLong(1);
            boolean good = want < 0 ? got > 0 : got == want;
            if (!good) problems++;
            say(String.format("  %-44s %6d %s", what, got, good ? "ok" : "ERWARTET " + (want < 0 ? "> 0" : String.valueOf(want))));
        }
    }

    private static void check(Connection c, DbConfig cfg) throws Exception {
        say("");
        say("== Durchstich");
        expect(c, "Säle", "SELECT COUNT(*) FROM car_room", 6);
        expect(c, "Steinbrüche", "SELECT COUNT(*) FROM car_quarry", 9);
        expect(c, "Steinsorten", "SELECT COUNT(*) FROM car_stone", 9);
        expect(c, "Material-Zuordnungen", "SELECT COUNT(*) FROM car_mat_stone", 17);
        expect(c, "Ereignisse", "SELECT COUNT(*) FROM car_event", 22);
        expect(c, "eingebaute Fahrten", "SELECT COUNT(*) FROM car_timeline WHERE eingebaut = 'J'", 6);
        expect(c, "Einstellungen der eingebauten Fahrten", "SELECT COUNT(*) FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.eingebaut = 'J'", 26);
        expect(c, "Schlüsselbilder der eingebauten Fahrten", "SELECT COUNT(*) FROM car_key k JOIN car_clip c ON c.clip_id = k.clip_id JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.eingebaut = 'J'", 230);
        expect(c, "ungültige Geometrien", "SELECT COUNT(*) FROM car_quarry WHERE MDSYS.SDO_GEOM.VALIDATE_GEOMETRY_WITH_CONTEXT(lage, 0.05) <> 'TRUE' "
                + "OR MDSYS.SDO_GEOM.VALIDATE_GEOMETRY_WITH_CONTEXT(weg, 0.05) <> 'TRUE'", 0);
        expect(c, "Spatial-Metadaten CAR_", "SELECT COUNT(*) FROM user_sdo_geom_metadata WHERE table_name LIKE 'CAR\\_%' ESCAPE '\\'", 2);
        expect(c, "Brüche im Umkreis von 450 km um Rom (Index)", "SELECT COUNT(*) FROM car_quarry q WHERE MDSYS.SDO_WITHIN_DISTANCE(q.lage, "
                + "MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(12.4925, 41.879, NULL), NULL, NULL), 'distance=450 unit=KM') = 'TRUE'", 2);
        expect(c, "Ereignisse mit Quelle", "SELECT COUNT(*) FROM car_event WHERE quelle_url IS NOT NULL", 22);
        say("  Wege nach Rom:");
        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT code, weg_km FROM car_quarry ORDER BY weg_km")) {
            while (r.next()) say(String.format("    %-18s %6d km", r.getString(1), r.getInt(2)));
        }

        // Laden wie die App und mit dem Code vergleichen
        CaracallaDb db = new CaracallaDb(cfg);
        CaracallaDb.Snapshot s = db.load();
        List<Timeline> code = new ArrayList<>(Programme.flights());
        int builtIn = 0;
        for (Timeline t : s.flights) if (code.stream().anyMatch(x -> x.name.equals(t.name))) builtIn++;
        sayCheck("Fahrten aus der DB wie im Code", builtIn == code.size(), builtIn + " von " + code.size());
        for (Timeline t : code) {
            Timeline d = s.flights.stream().filter(x -> x.name.equals(t.name)).findFirst().orElse(null);
            sayCheck("  " + t.name + " gleich", d != null && same(t, d), d == null ? "fehlt" : String.format("%.1f s", d.duration()));
        }
        sayCheck("Rundgang gleich", s.tour != null && same(Programme.tour(), s.tour), s.tour == null ? "fehlt" : String.format("%.1f s", s.tour.duration()));
        sayCheck("Drehbuch gleich", s.dayScript != null && same(Programme.dayScript(), s.dayScript), s.dayScript == null ? "fehlt" : String.format("%.1f s", s.dayScript.duration()));
        CaracallaDb.Stone pav = s.stones.get("PAVONAZZETTO");
        sayCheck("Steckbrief Pavonazzetto mit Weg", pav != null && pav.lon.length > 5, pav == null ? "fehlt" : pav.lon.length + " Wegpunkte, " + pav.wegKm + " km");
        sayCheck("Material MARBLE hat Steine", s.matStones.getOrDefault(6, List.of()).size() == 5, String.valueOf(s.matStones.get(6)));

        // Schreiben, lesen, löschen
        CaracallaDb.View v = new CaracallaDb.View();
        v.name = "Durchstich-Test";
        v.ex = 1; v.ey = 2; v.ez = 3; v.lx = 4; v.ly = 5; v.lz = 6; v.day = 172; v.hour = 15.5;
        db.saveView(v);
        boolean found = db.readViews().stream().anyMatch(x -> x.name.equals(v.name) && Math.abs(x.ez - 3) < 1e-6);
        db.deleteView(v.name);
        boolean gone = db.readViews().stream().noneMatch(x -> x.name.equals(v.name));
        sayCheck("Blick schreiben, lesen, löschen", found && gone, found ? (gone ? "ok" : "nicht gelöscht") : "nicht gefunden");
    }

    private static void sayCheck(String what, boolean good, String info) {
        if (!good) problems++;
        say(String.format("  %-44s %s %s", what, good ? "ok" : "FEHLER", info));
    }

    private static boolean same(Timeline a, Timeline b) {
        if (a.clips.size() != b.clips.size() || a.day != b.day || a.waterPath != b.waterPath) return false;
        for (int i = 0; i < a.clips.size(); i++) {
            Clip x = a.clips.get(i), y = b.clips.get(i);
            List<double[]> kx = x.path.keys(), ky = y.path.keys();
            if (kx.size() != ky.size() || x.cut != y.cut) return false;
            if (!eq(x.hour0, y.hour0) || !eq(x.hour1, y.hour1) || !eq(x.haze, y.haze)) return false;
            if (!java.util.Objects.equals(x.title, y.title) || !java.util.Objects.equals(x.text, y.text)) return false;
            for (int k = 0; k < kx.size(); k++)
                for (int j = 0; j < 7; j++) if (Math.abs(kx.get(k)[j] - ky.get(k)[j]) > 0.0006) return false;
        }
        return true;
    }

    private static boolean eq(double a, double b) {
        return Double.isNaN(a) ? Double.isNaN(b) : Math.abs(a - b) < 0.006;
    }
}
