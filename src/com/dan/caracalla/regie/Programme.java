package com.dan.caracalla.regie;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Inhalte der Regie: drei Kamerafahrten, der Rundgang in der römischen Badefolge und das
 * Drehbuch „Ein Tag in den Thermen“. Alle Wege führen durch echte Öffnungen des Modells.
 */
public final class Programme {
    private Programme() { }

    // ------------------------------------------------------------ Kamerafahrten

    public static List<Timeline> flights() {
        List<Timeline> l = new ArrayList<>();
        l.add(new Timeline("Rundumblick").add(new Clip(orbit(0, 12, 6, 270, 190, 300, 18, 34, 35, 395, 72))));
        l.add(new Timeline("Flug durch die Achse").add(new Clip(axisFlight())));
        l.add(new Timeline("Aufstieg im Licht").add(new Clip(spiral(32))));
        l.add(waterFlight());
        return l;
    }

    /** Dem Wasser folgen: vom Aquädukt über die Zisternen und den Kessel bis ins Labrum. */
    public static Timeline waterFlight() {
        Timeline tl = new Timeline("Dem Wasser folgen");
        tl.waterPath = true;
        tl.add(new Clip(new CameraPath()
                .key(0, 470, 46, 262, 330, 10, 214)
                .key(8, 330, 30, 246, 190, 10, 214)
                .key(16, 180, 22, 238, 60, 9, 208)
                .key(22, 96, 24, 232, 44, 6, 186))
                .caption("Aqua Antoniniana", "Ein eigener Zweig der Aqua Marcia bringt das Wasser auf Bögen heran, oben in einem gemauerten Kanal."));
        tl.add(new Clip(new CameraPath()
                .key(0, 96, 24, 232, 44, 6, 186)
                .key(7, 70, 30, 205, 24, 0, 160)
                .key(14, 40, 26, 138, 4, 0, 96)
                .key(20, 26, 18, 104, 2, 2, 74))
                .noCut().caption("Zisternen und Bleirohre", "Unter den Stufen des Stadions liegen die Zisternen mit 64 Kammern. Von dort laufen Bleirohre unter dem Garten zum Zentralbau."));
        tl.add(new Clip(new CameraPath()
                .key(0, 26, 18, 104, 2, 2, 74)
                .key(6, 14, 7.5, 84, 0, 3, 71)
                .key(11, 6, 6.2, 79.5, 0, 3.4, 71.5)
                .key(16, 0.5, 8.5, 76, 0, 5, 60)
                .key(21, 0, 8, 64, 0, 3, 52)
                .key(27, -5.5, 4.4, 57.5, 0, 1.3, 50))
                .noCut().caption("Über dem Feuer", "Das Wasser für die heißen Wannen läuft durch Kessel über den Praefurnien. Die Heißluft zieht unter den Boden und in die Wände."));
        return tl;
    }

    /** Orbit mit Abstand und Neigung, die zur Mitte der Fahrt hin wechseln. */
    static CameraPath orbit(double cx, double cy, double cz, double d0, double dMid, double d1,
                            double pitch0, double pitchMid, double yaw0, double yaw1, double dur) {
        CameraPath p = new CameraPath();
        int n = 24;
        for (int i = 0; i <= n; i++) {
            double u = i / (double) n;
            double w = Math.sin(Math.PI * u);
            double d = (1 - u) * d0 + u * d1 + (dMid - (d0 + d1) / 2) * w;
            double pitch = Math.toRadians(pitch0 + (pitchMid - pitch0) * w);
            double yaw = Math.toRadians(yaw0 + (yaw1 - yaw0) * u);
            double ex = cx + d * Math.cos(pitch) * Math.sin(yaw), ey = cy + d * Math.sin(pitch), ez = cz + d * Math.cos(pitch) * Math.cos(yaw);
            p.key(dur * u, ex, ey, ez, cx, cy, cz);
        }
        return p;
    }

    /** Von außen über die Schauwand in die Natatio, dann durch alle Säle bis unter die Kuppel. */
    static CameraPath axisFlight() {
        return new CameraPath()
                .key(0, 0, 27, -112, 0, 6, -40)
                .key(5, 0, 25, -60, 0, 4, -36)
                .key(9, 0, 8, -42, 0, 5, -22)
                .key(12.5, 0, 5, -31, 0, 5, -12)
                .key(15.5, 0, 5, -20, 0, 6, 0)
                .key(19.5, 0, 6.5, -8, 0, 8, 10)
                .key(23.5, 0, 6.5, 5, 0, 6, 20)
                .key(26.5, 0, 5, 15.5, 0, 5, 26)
                .key(29.5, 0, 5, 22.5, 0, 5.5, 36)
                .key(33, 0, 5, 30.5, 0, 7, 46)
                .key(37, 0, 7.5, 42, 0, 20, 60)
                .key(42, 0, 15, 49, 5, 38, 56)
                .key(47, 0, 29, 50, 0.4, 45, 50.6);
    }

    /** Spiralförmiger Aufstieg im Caldarium, zuletzt der Blick in die Kuppel. */
    static CameraPath spiral(double dur) {
        CameraPath p = new CameraPath();
        int n = 20;
        for (int i = 0; i <= n; i++) {
            double u = i / (double) n;
            double a = Math.toRadians(200 + 330 * u);
            double r = 10.5 - 6.5 * u;
            double y = 2 + 31 * u * u * (3 - 2 * u);
            double ex = r * Math.cos(a), ez = 50 + r * Math.sin(a);
            double lookUp = 5 + 10 * u;
            double lx = -0.35 * r * Math.cos(a), lz = 50 - 0.35 * r * Math.sin(a);
            p.key(dur * u, ex, y, ez, lx, Math.min(44.5, y + lookUp), lz);
        }
        return p;
    }

    // ------------------------------------------------------------ Rundgang

    /** Baut Gehwege mit Blick voraus und Stationen mit langsamem Schwenk. */
    static final class Walk {
        final Timeline tl;
        double[] eye, look;

        Walk(Timeline tl, double[] eye, double[] look) { this.tl = tl; this.eye = eye; this.look = look; }

        Walk go(double speed, double[]... pts) {
            CameraPath p = new CameraPath().key(0, eye[0], eye[1], eye[2], look[0], look[1], look[2]);
            double t = 0;
            double[] prev = eye;
            for (int i = 0; i < pts.length; i++) {
                double[] q = pts[i];
                double d = Math.sqrt(sq(q[0] - prev[0]) + sq(q[1] - prev[1]) + sq(q[2] - prev[2]));
                t += Math.max(0.8, d / speed);
                double[] ahead = i + 1 < pts.length ? pts[i + 1] : new double[]{2 * q[0] - prev[0], q[1], 2 * q[2] - prev[2]};
                double lx = ahead[0], ly = ahead[1] + 0.3, lz = ahead[2];
                p.key(t, q[0], q[1], q[2], lx, ly, lz);
                prev = q;
                look = new double[]{lx, ly, lz};
            }
            eye = prev;
            tl.add(new Clip(p).noCut());
            return this;
        }

        Walk station(double dur, double[] at, double[] lookA, double[] lookB, String title, String text) {
            CameraPath p = new CameraPath()
                    .key(0, eye[0], eye[1], eye[2], look[0], look[1], look[2])
                    .key(2.2, at[0], at[1], at[2], lookA[0], lookA[1], lookA[2])
                    .key(dur, at[0], at[1], at[2], lookB[0], lookB[1], lookB[2]);
            tl.add(new Clip(p).noCut().caption(title, text));
            eye = at; look = lookB;
            return this;
        }

        private static double sq(double v) { return v * v; }
    }

    private static double[] v(double x, double y, double z) { return new double[]{x, y, z}; }

    public static Timeline tour() {
        Timeline tl = new Timeline("Rundgang");
        // Ankunft: über den Park und die Mauer in die westliche Palästra
        CameraPath arrive = new CameraPath()
                .key(0, -40, 32, -150, -60, 10, -40)
                .key(6, -76, 30, -64, -78, 8, -20)
                .key(9, -78, 26, -42, -80, 4, 0)
                .key(12.5, -77, 9, -14, -72, 3, 20)
                .key(16, -68.5, 1.8, 26, -90, 6, -12);
        tl.add(new Clip(arrive).caption("Ankunft", "Wer die Thermen betrat, legte zuerst in den Umkleiden ab und ging dann hinaus in eine der beiden Palästren."));
        Walk w = new Walk(tl, v(-68.5, 1.8, 26), v(-90, 6, -12));
        w.station(12, v(-68.5, 1.8, 26), v(-90, 6, -12), v(-80, 6, 30),
                "Die Palästra", "Ein offener Hof mit Säulenhallen an drei Seiten. Hier wurde gelaufen, gerungen und Ball gespielt, bevor es in die heißen Säle ging.");
        w.go(2.4, v(-67.6, 1.7, 27.2), v(-64.5, 1.7, 25.2), v(-63, 1.7, 24), v(-58.5, 1.7, 24), v(-40, 1.7, 25), v(-35, 1.7, 25), v(-30.6, 1.7, 25.2),
                v(-30.2, 1.7, 30.4), v(-20, 1.7, 30.6), v(-14, 1.7, 28.3), v(-11, 1.7, 25.8), v(-6.5, 1.7, 25.8),
                v(-2, 1.7, 28.5), v(0, 1.7, 31), v(-0.5, 1.7, 34.5));
        w.station(15, v(-2, 1.8, 37), v(6, 13, 66), v(-14, 11, 60),
                "Das Caldarium", "Der heiße Rundsaal, 35 Meter weit, mit einer Kuppel fast so groß wie die des Pantheons. Die Wannen liegen unter den großen Fenstern nach Südwesten.");
        w.go(2.2, v(0, 1.7, 33), v(0, 1.7, 29.5));
        w.station(11, v(0, 1.8, 29), v(0, 9, 19), v(-9, 4, 25.5),
                "Das Tepidarium", "Der lauwarme Saal zwischen heiß und kalt. Seitlich liegen zwei Becken mit lauwarmem Wasser.");
        w.go(2.4, v(0, 1.7, 24), v(0, 1.7, 19), v(0, 1.7, 15.5), v(0, 1.7, 11.5), v(2.5, 1.7, 7.5));
        w.station(15, v(4, 1.8, 6), v(-25, 12, -3), v(25, 13, -3),
                "Das Frigidarium", "Der kalte Saal, 59 mal 24 Meter, unter drei Kreuzgratgewölben auf acht Granitsäulen. In den Seitenräumen liegen die Kaltwasserbecken.");
        w.go(2.4, v(1.5, 1.7, -2), v(0, 1.7, -9), v(0, 1.7, -15), v(0, 1.7, -21), v(0, 1.7, -25.5), v(-1.5, 1.7, -27.6),
                v(-9, 1.7, -27.65), v(-20, 1.7, -27.65));
        w.station(13, v(-24, 2.0, -27.8), v(16, 8, -52), v(-6, 13, -52),
                "Die Natatio", "Das große Freibad, 54 mal 23 Meter, vor einer Schauwand mit drei Reihen von Statuennischen. Hier endete der Weg durch die Bäder.");
        CameraPath out = new CameraPath()
                .key(0, -24, 2.0, -27.8, -6, 13, -52)
                .key(4.5, -20, 13, -37, -5, 10, -20)
                .key(9, -8, 48, -95, 0, 10, 0)
                .key(15, 45, 95, -185, 0, 12, 10);
        tl.add(new Clip(out).noCut().caption("Thermae Antoninianae", "Eröffnet 216 n. Chr. unter Kaiser Caracalla. Bis zu 1600 Menschen badeten hier gleichzeitig."));
        return tl;
    }

    // ------------------------------------------------------------ Drehbuch

    public static Timeline dayScript() {
        Timeline tl = new Timeline("Ein Tag in den Thermen");
        tl.day = 80;
        tl.add(new Clip(orbit(0, 12, 6, 360, 300, 330, 22, 26, 20, 95, 20)).sun(5.85, 6.6).haze(0.8)
                .caption("Rom, im Jahr 216", "Kurz nach Sonnenaufgang über den Thermae Antoninianae, den Thermen des Caracalla."));
        tl.add(new Clip(new CameraPath()
                .key(0, 0, 17, -152, 0, 10, -60)
                .key(9, 0, 20, -105, 0, 8, -50)
                .key(18, 0, 26, -64, 0, 2, -38)).sun(6.6, 7.6).haze(0.8)
                .caption("Morgennebel", "Über der Natatio, dem großen Freibad, liegt noch der Nebel der Nacht."));
        tl.add(new Clip(axisFlight()).sun(8, 10.5).haze(0.3)
                .caption("Durch die Achse", "Natatio, Frigidarium, Tepidarium und Caldarium liegen hintereinander: vom kalten zum heißen Wasser."));
        tl.add(new Clip(spiral(22)).sun(10.5, 12.5).haze(0)
                .caption("Das Caldarium", "Unter der Kuppel sammelt sich der Dampf der heißen Wannen."));
        tl.add(new Clip(new CameraPath()
                .key(0, 24, 2.2, -5, -10, 10, 2)
                .key(11, 4, 3, 0, -24, 13, -2)
                .key(22, -18, 4, 5, -28, 15, -5)).sun(12.5, 14).haze(0)
                .caption("Das Frigidarium", "Drei Kreuzgratgewölbe auf acht Granitsäulen. Die Thermenfenster in den Lünetten tragen das Licht in den kalten Saal."));
        tl.add(new Clip(new CameraPath()
                .key(0, -71.5, 1.8, -27, -88, 5, -8)
                .key(10, -72.5, 2.0, -3, -90, 6, -2)
                .key(20, -71.5, 2.2, 21, -88, 7, 4)).sun(14, 15.3).haze(0)
                .caption("Die Palästra", "In den Säulenhallen und im offenen Hof trieb man Sport, bevor es in die heißen Säle ging."));
        tl.add(new Clip(new CameraPath()
                .key(0, -10, 2, 36, 6, 14, 66)
                .key(13, -8, 2.6, 37, 7, 17, 65)
                .key(26, -6, 3.2, 38, 8, 20, 64)).sun(15.3, 16.3).haze(0)
                .caption("Nachmittag im Caldarium", "Die großen Fenster öffnen sich nach Südwesten. Jetzt fällt die Sonne in den Dampf."));
        tl.add(new Clip(orbit(0, 20, 40, 125, 115, 125, 14, 17, 150, 240, 26)).sun(17, 18.4).haze(0.3)
                .caption("Abendlicht", "Die Sonne sinkt hinter die Kuppel des Caldariums."));
        tl.add(new Clip(new CameraPath()
                .key(0, 60, 40, 160, 0, 15, 20)
                .key(11, 100, 70, 240, 0, 14, 20)
                .key(22, 150, 110, 330, 0, 12, 20)).sun(18.4, 19.2).haze(0.6)
                .caption("Um 537 n. Chr.", "Im Krieg gegen die Goten werden die Wasserleitungen gekappt. Die Thermen fallen trocken und verfallen."));
        return tl;
    }
}
