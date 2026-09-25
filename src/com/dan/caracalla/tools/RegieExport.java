package com.dan.caracalla.tools;

import com.dan.caracalla.regie.Clip;
import com.dan.caracalla.regie.Programme;
import com.dan.caracalla.regie.Timeline;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** Schreibt die eingebauten Fahrten, den Rundgang und das Drehbuch als SQL (db/03_regie.sql). */
public final class RegieExport {
    public static void main(String[] a) throws Exception {
        String file = a.length > 0 ? a[0] : "db/03_regie.sql";
        try (PrintWriter w = new PrintWriter(file, StandardCharsets.UTF_8)) {
            w.println("-- =====================================================================");
            w.println("-- Caracalla-Thermen · Phase 8 · 03 Regie");
            w.println("-- Die eingebauten Fahrten, der Rundgang und das Drehbuch, erzeugt von");
            w.println("-- com.dan.caracalla.tools.RegieExport aus Programme.java (EINGEBAUT = 'J').");
            w.println("-- =====================================================================");
            w.println();
            List<Timeline> fl = Programme.flights();
            String[] codes = {"RUNDUMBLICK", "ACHSE", "AUFSTIEG", "WASSERWEG"};
            int order = 1;
            for (int i = 0; i < fl.size(); i++) write(w, fl.get(i), i < codes.length ? codes[i] : "FAHRT_" + (i + 1), "FAHRT", order++);
            write(w, Programme.tour(), "RUNDGANG", "RUNDGANG", order++);
            write(w, Programme.dayScript(), "DREHBUCH_TAG", "DREHBUCH", order++);
            w.println("COMMIT");
            w.println("/");
        }
        System.out.println("Geschrieben: " + file);
    }

    private static String q(String s) { return s == null ? "NULL" : "'" + s.replace("'", "''") + "'"; }

    private static String n(double v) { return Double.isNaN(v) ? "NULL" : String.format(Locale.ROOT, "%.3f", v); }

    private static void write(PrintWriter w, Timeline tl, String code, String art, int order) {
        w.printf("-- %s%n", tl.name);
        w.printf("INSERT INTO car_timeline (code, name, art, tag_im_jahr, wasserweg, reihenfolge, eingebaut)%nVALUES (%s, %s, %s, %s, %s, %d, 'J')%n/%n",
                q(code), q(tl.name), q(art), tl.day > 0 ? String.valueOf(tl.day) : "NULL", tl.waterPath ? "'J'" : "'N'", order);
        int pos = 1;
        for (Clip c : tl.clips) {
            w.printf("INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)%n"
                            + "SELECT timeline_id, %d, %s, %s, %s, %s, %s, %s FROM car_timeline WHERE code = %s%n/%n",
                    pos, c.cut ? "'J'" : "'N'", n(c.hour0), n(c.hour1), n(c.haze), q(c.title), q(c.text), q(code));
            for (double[] k : c.path.keys()) {
                w.printf(Locale.ROOT, "INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)%n"
                                + "SELECT c.clip_id, %.3f, %.3f, %.3f, %.3f, %.3f, %.3f, %.3f FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id "
                                + "WHERE t.code = %s AND c.pos = %d%n/%n",
                        k[0], k[1], k[2], k[3], k[4], k[5], k[6], q(code), pos);
            }
            pos++;
        }
        w.println();
    }
}
