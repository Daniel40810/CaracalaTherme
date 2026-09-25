package com.dan.caracalla.model;

import com.dan.caracalla.geo.Mat;
import com.dan.caracalla.geo.Mesh;
import com.dan.caracalla.geo.MeshBuilder;
import com.dan.caracalla.geo.Shapes;
import com.dan.caracalla.geo.Statues;
import com.dan.caracalla.geo.Walls;
import com.dan.caracalla.geo.Walls.Arc;
import com.dan.caracalla.geo.Walls.Line;
import com.dan.caracalla.geo.Walls.Opening;
import com.dan.caracalla.geo.Walls.Wall;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Parametrisches Modell der Caracalla-Thermen (Zustand 216 n. Chr.).
 *
 * <p>Lokales Koordinatensystem in Metern: y nach oben, Ursprung in der Mitte des Zentralbaus,
 * +z zeigt zum Caldarium (geografisch etwa Südwest), -z zur Natatio (Nordost),
 * +x etwa nach Südost. Der Zentralbau misst 214 × 110 m.</p>
 */
public final class ThermenModel {

    // Zentralbau
    public static final double BX = 107, BZ = 55;
    static final double T_PER = 2.5, H_PER = 20.4, Y_TERRACE = 19.6;
    // Frigidarium
    static final double FX = 29, FZ = 12, F_YS = 21, F_R = 12, F_TW = 2.4;
    // Caldarium
    public static final double CAL_Z = 50, CAL_R = 17.5, CAL_T = 3, CAL_YS = 27;
    // Umfassung
    public static final double PX = 168, PZ0 = -150, PZ1 = 178;

    public final Mesh mesh;
    public final List<Room> rooms;
    public final List<WaterBody> waters;
    public final List<Fountain> fountains;
    /** Wege der heißen Luft (Hypokaustum) und des Wassers (Aquädukt bis Becken). */
    public final List<Route> heatRoutes = new ArrayList<>(), waterRoutes = new ArrayList<>();
    /** Schürlöcher der Praefurnien: x, y, z, Richtung nach außen (x, z). */
    public final List<double[]> furnaces = new ArrayList<>();
    /** Bereich, den der Schnitt öffnet (x0, z0, x1, z1, Unterkante des Erdkörpers). */
    public static final double[] CUT_BOX = Hypocaust.EARTH;

    private ThermenModel(Mesh mesh, List<Room> rooms, List<WaterBody> waters, List<Fountain> fountains) {
        this.mesh = mesh;
        this.rooms = rooms;
        this.waters = waters;
        this.fountains = fountains;
    }

    public static ThermenModel build() {
        MeshBuilder mb = new MeshBuilder();
        ground(mb);
        floorsAndPools(mb);
        perimeter(mb);
        natatio(mb);
        centralWalls(mb);
        palaestra(mb);
        frigidarium(mb);
        tepidarium(mb);
        caldarium(mb);
        roofs(mb);
        precinct(mb);
        trees(mb);
        List<Fountain> fo = new ArrayList<>();
        waterworks(mb, fo);
        List<Route> heat = new ArrayList<>(), water = new ArrayList<>();
        List<double[]> furn = new ArrayList<>();
        Hypocaust.build(mb, heat, furn);
        WaterSupply.build(mb, water);
        ThermenModel tm = new ThermenModel(mb.build(24), rooms(), waters(), fo);
        tm.heatRoutes.addAll(heat);
        tm.waterRoutes.addAll(water);
        tm.furnaces.addAll(furn);
        return tm;
    }

    // ================================================================= Gelände

    private static void ground(MeshBuilder mb) {
        mb.maxEdge = 4;
        List<double[]> holes = new ArrayList<>();
        holes.add(new double[]{-BX, -BZ, BX, BZ});
        Shapes.floorRect(mb, -PX, PZ0, PX, PZ1, 0, holes, true, Mat.GROUND);
        // Exedren-Gärten außerhalb der Umfassungslinie
        mb.maxEdge = 60;
        double F = 2000;
        mb.rectH(-F, -F, F, PZ0, 0, true, Mat.LAND);
        mb.rectH(-F, PZ1, F, F, 0, true, Mat.LAND);
        mb.rectH(-F, PZ0, -PX, PZ1, 0, true, Mat.LAND);
        mb.rectH(PX, PZ0, F, PZ1, 0, true, Mat.LAND);
        hills(mb);
        mb.maxEdge = 2.5;
    }

    /**
     * Horizont: flache Hügelkette ringsum, im Südosten die Albaner Berge, im Nordosten die
     * Tiburtiner Berge (Höhen so gewählt, dass die Blickwinkel von Rom aus ungefähr stimmen).
     */
    private static void hills(MeshBuilder mb) {
        double r0 = 1850, r1 = 2700, r2 = 3600;
        mb.patch((u, v, p, n) -> {
            double r = v < 0.5 ? r0 + (r1 - r0) * (v / 0.5) : r1 + (r2 - r1) * ((v - 0.5) / 0.5);
            double h = hillHeight(u) * smooth(r0, r1, r);
            p[0] = r * Math.cos(u); p[1] = h; p[2] = r * Math.sin(u);
            double e = 0.002;
            double dh = (hillHeight(u + e) - hillHeight(u - e)) / (2 * e * r) * smooth(r0, r1, r);
            double dr = hillHeight(u) * (smooth(r0, r1, r + 5) - smooth(r0, r1, r - 5)) / 10;
            double c = Math.cos(u), sn = Math.sin(u);
            n[0] = -(dr * c - dh * sn); n[1] = 1; n[2] = -(dr * sn + dh * c);
        }, -Math.PI, Math.PI, 180, 0, 1, 6, Mat.LAND);
    }

    private static double hillHeight(double a) {
        double h = 22 + 10 * Math.sin(a * 7 + 1) + 6 * Math.sin(a * 13 + 2) + 4 * Math.sin(a * 29);
        h += 115 * gauss(angDiff(a, 0), 0.32) + 40 * gauss(angDiff(a, 0.25), 0.1);
        h += 85 * gauss(angDiff(a, Math.toRadians(-75)), 0.28);
        h *= 1 - 0.7 * gauss(angDiff(a, Math.PI * 0.75), 0.6);
        return Math.max(4, h);
    }

    private static double gauss(double d, double w) { return Math.exp(-(d * d) / (2 * w * w)); }

    private static double angDiff(double a, double b) {
        double d = a - b;
        while (d > Math.PI) d -= 2 * Math.PI;
        while (d < -Math.PI) d += 2 * Math.PI;
        return d;
    }

    private static double smooth(double a, double b, double x) {
        double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    // ===================================================== Böden und Becken

    private static void floorsAndPools(MeshBuilder mb) {
        mb.maxEdge = 2.5;
        List<double[]> holes = new ArrayList<>();
        holes.add(new double[]{-31.4, -52.5, 31.4, -27});        // Natatio-Hof
        holes.add(new double[]{-88, -33.5, -67.5, 33.5});        // Palästra West, offener Hof
        holes.add(new double[]{67.5, -33.5, 88, 33.5});          // Palästra Ost
        holes.add(new double[]{-27, -23.5, -13, -16});           // Frigidarium-Becken
        holes.add(new double[]{13, -23.5, 27, -16});
        holes.add(new double[]{-28, 22, -15, 29});               // Becken neben dem Tepidarium
        holes.add(new double[]{15, 22, 28, 29});
        Shapes.floorRect(mb, -BX, -BZ, BX, BZ, 0, holes, true, Mat.FLOOR);

        List<double[]> nat = new ArrayList<>();
        nat.add(new double[]{-27, -51.2, 27, -28.3});
        Shapes.floorRect(mb, -31.4, -52.5, 31.4, -27, 0, nat, true, Mat.PAVING);
        Shapes.pool(mb, -27, -51.2, 27, -28.3, 1.6, -0.25, Mat.POOL, Mat.WATER);

        Shapes.floorRect(mb, -88, -33.5, -67.5, 33.5, 0, new ArrayList<>(), true, Mat.PAVING);
        Shapes.floorRect(mb, 67.5, -33.5, 88, 33.5, 0, new ArrayList<>(), true, Mat.PAVING);

        Shapes.pool(mb, -27, -23.5, -13, -16, 1.2, -0.2, Mat.POOL, Mat.WATER);
        Shapes.pool(mb, 13, -23.5, 27, -16, 1.2, -0.2, Mat.POOL, Mat.WATER);
        Shapes.pool(mb, -28, 22, -15, 29, 1.2, -0.2, Mat.POOL, Mat.WATER);
        Shapes.pool(mb, 15, 22, 28, 29, 1.2, -0.2, Mat.POOL, Mat.WATER);

        // Boden des Caldariums, eine Stufe höher
        Shapes.ring(mb, 0, CAL_Z, 0, CAL_R + 0.2, 0, 360, 0.06, true, Mat.FLOOR, 64);
    }

    // =========================================================== Außenmauern

    private static void windows(Wall w, Line l, double from, double to, double step, double[] skip) {
        for (double u = from; u <= to + 1e-6; u += step) {
            boolean bad = false;
            for (double s : skip) if (Math.abs(u - s) < 3.2) bad = true;
            if (bad) continue;
            w.add(Opening.arch(u, 2.6, 3.5, 9.2));
            w.add(Opening.arch(u, 2.6, 12.2, 17.6));
        }
    }

    private static void perimeter(MeshBuilder mb) {
        double c = BZ - T_PER / 2; // 53.75
        double cx = BX - T_PER / 2; // 105.75
        // Nord (außen = -z): zwei Abschnitte links und rechts der Natatio-Fassade
        Line nw = new Line(-32.4, -c, -BX, -c);
        Wall w = new Wall(nw, T_PER, 0, H_PER, Mat.STUCCO, Mat.PLASTER);
        windows(w, nw, 6, nw.length() - 5, 7, new double[]{nw.at(-61, -c), nw.at(-94, -c)});
        Walls.build(mb, w);
        Line ne = new Line(BX, -c, 32.4, -c);
        w = new Wall(ne, T_PER, 0, H_PER, Mat.STUCCO, Mat.PLASTER);
        windows(w, ne, 5, ne.length() - 6, 7, new double[]{ne.at(61, -c), ne.at(94, -c)});
        Walls.build(mb, w);
        // Süd (außen = +z), Lücke für das Caldarium
        Line sw = new Line(-BX, c, -19.6, c);
        w = new Wall(sw, T_PER, 0, H_PER, Mat.STUCCO, Mat.PLASTER);
        windows(w, sw, 5, sw.length() - 5, 7, new double[]{sw.at(-94, c), sw.at(-61, c), sw.at(-32.4, c)});
        Walls.build(mb, w);
        Line se = new Line(19.6, c, BX, c);
        w = new Wall(se, T_PER, 0, H_PER, Mat.STUCCO, Mat.PLASTER);
        windows(w, se, 5, se.length() - 5, 7, new double[]{se.at(94, c), se.at(61, c), se.at(32.4, c)});
        Walls.build(mb, w);
        // West (außen = -x) und Ost (außen = +x), schließt an die Ecken der Nord- und Südmauer an
        Line we = new Line(-cx, -BZ, -cx, BZ);
        w = new Wall(we, T_PER, 0, H_PER, Mat.STUCCO, Mat.PLASTER);
        windows(w, we, 7.5, we.length() - 7.5, 7.2, new double[]{we.at(-cx, -40), we.at(-cx, 40)});
        Walls.build(mb, w);
        Line ea = new Line(cx, BZ, cx, -BZ);
        w = new Wall(ea, T_PER, 0, H_PER, Mat.STUCCO, Mat.PLASTER);
        windows(w, ea, 7.5, ea.length() - 7.5, 7.2, new double[]{ea.at(cx, -40), ea.at(cx, 40)});
        Walls.build(mb, w);
        // Kranzgesims außen
        mb.box(-BX - 0.5, H_PER - 0.9, -BZ - 0.5, BX + 0.5, H_PER - 0.3, -BZ, Mat.CORNICE, true);
        mb.box(-BX - 0.5, H_PER - 0.9, BZ, -19.9, H_PER - 0.3, BZ + 0.5, Mat.CORNICE, true);
        mb.box(19.9, H_PER - 0.9, BZ, BX + 0.5, H_PER - 0.3, BZ + 0.5, Mat.CORNICE, true);
        mb.box(-BX - 0.5, H_PER - 0.9, -BZ, -BX, H_PER - 0.3, BZ, Mat.CORNICE, true);
        mb.box(BX, H_PER - 0.9, -BZ, BX + 0.5, H_PER - 0.3, BZ, Mat.CORNICE, true);
    }

    // ================================================================ Natatio

    private static void natatio(MeshBuilder mb) {
        double c = BZ - T_PER / 2;
        // Schauwand im Norden: Vorderseite zum Hof, drei Reihen Statuennischen
        Line f = new Line(-32.4, -c, 32.4, -c);
        Wall w = new Wall(f, T_PER, 0, H_PER, Mat.MARBLE, Mat.STUCCO);
        for (int i = 0; i < 8; i++) {
            double x = -26.25 + 7.5 * i, u = f.at(x, -c);
            w.add(Opening.niche(u, 3.2, 1.2, 7.0, 1.3, Mat.MARBLE));
            w.add(Opening.niche(u, 2.8, 8.8, 13.8, 1.3, Mat.MARBLE));
            w.add(Opening.niche(u, 2.4, 15.2, 19.4, 1.3, Mat.MARBLE));
        }
        Walls.build(mb, w);
        // Statuen in den Nischen der Schauwand
        double[] base = {1.2, 8.8, 15.2}, hs = {4.0, 3.4, 2.9};
        for (int i = 0; i < 8; i++) {
            double x = -26.25 + 7.5 * i;
            for (int t = 0; t < 3; t++) {
                int kind = (i + t * 2) % 3;
                int mat = (t == 1 && (i & 1) == 0) ? Mat.BRONZE : Mat.STATUE;
                Statues.statue(mb, x, -53.15, base[t], hs[t], 0, kind, mat, 1000 + i * 10 + t);
            }
        }
        // Gesimse der drei Geschosse
        for (double y : new double[]{7.7, 14.5}) {
            mb.box(-31.4, y, -52.5, 31.4, y + 0.45, -51.9, Mat.CORNICE, true);
        }
    }

    // ========================================== Mauern der Mittelachse (x = ±32,4)

    private static void centralWalls(MeshBuilder mb) {
        int v0 = mb.vertexCount(), t0 = mb.triCount();
        // Natatio-Hof: Vorderseite zum Hof (+x)
        Line a = new Line(-32.4, -26, -32.4, -52.5);
        Wall w = new Wall(a, 2, 0, H_PER, Mat.MARBLE, Mat.PLASTER);
        for (double z : new double[]{-46, -39, -32}) {
            w.add(Opening.niche(a.at(-32.4, z), 2.8, 1.2, 7.2, 1.1, Mat.MARBLE));
            w.add(Opening.niche(a.at(-32.4, z), 2.4, 9.2, 14.6, 1.1, Mat.MARBLE));
        }
        Walls.build(mb, w);
        mb.box(-31.4, 8.2, -52.5, -30.8, 8.65, -27, Mat.CORNICE, true);
        int k = 0;
        for (double z : new double[]{-46, -39, -32}) {
            Statues.statue(mb, -31.95, z, 1.2, 4.0, Math.PI / 2, k % 3, Mat.STATUE, 2000 + k);
            Statues.statue(mb, -31.95, z, 9.2, 3.5, Math.PI / 2, (k + 1) % 3, Mat.STATUE, 2100 + k);
            k++;
        }
        // übriger Abschnitt bis zur Südmauer
        Line b = new Line(-32.4, 53.75, -32.4, -26);
        w = new Wall(b, 2, 0, H_PER, Mat.PLASTER, Mat.PLASTER);
        w.add(Opening.arch(b.at(-32.4, 0), 10, 0, 14));
        w.add(Opening.rect(b.at(-32.4, -19), 3, 0, 5.5));
        w.add(Opening.rect(b.at(-32.4, 25), 3, 0, 5.5));
        w.add(Opening.rect(b.at(-32.4, 42), 3, 0, 5.5));
        Walls.build(mb, w);
        mb.mirrorX(v0, t0);
    }

    // =============================================================== Palästren

    private static void palaestra(MeshBuilder mb) {
        int v0 = mb.vertexCount(), t0 = mb.triCount();
        // Innenmauer x = -61, Vorderseite zum Hof (-x)
        Line in = new Line(-61, -40, -61, 40);
        Wall w = new Wall(in, 2, 0, H_PER, Mat.STUCCO, Mat.PLASTER);
        for (double z : new double[]{-24, -8, 8, 24}) {
            w.add(Opening.rect(in.at(-61, z), 3.2, 0, 5.5));
            w.add(Opening.arch(in.at(-61, z), 2.6, 11, 16.5));
        }
        Walls.build(mb, w);
        // Außenmauer x = -94 mit großem Bogen zur Exedra
        Line out = new Line(-94, 40, -94, -40);
        w = new Wall(out, 2, 0, H_PER, Mat.STUCCO, Mat.PLASTER);
        w.add(Opening.arch(out.at(-94, 0), 11.5, 0, 16.75));
        for (double z : new double[]{-24, 24}) w.add(Opening.rect(out.at(-94, z), 3.2, 0, 5.5));
        for (double z : new double[]{-30, -16, 16, 30}) w.add(Opening.arch(out.at(-94, z), 2.6, 11, 16.5));
        Walls.build(mb, w);
        // Quermauern z = ±40
        Line n = new Line(-94, -40, -61, -40);
        w = new Wall(n, 2, 0, H_PER, Mat.STUCCO, Mat.PLASTER);
        for (double x : new double[]{-84, -71}) {
            w.add(Opening.rect(n.at(x, -40), 3.2, 0, 5.5));
            w.add(Opening.arch(n.at(x, -40), 2.6, 11, 16.5));
        }
        Walls.build(mb, w);
        Line s = new Line(-61, 40, -94, 40);
        w = new Wall(s, 2, 0, H_PER, Mat.STUCCO, Mat.PLASTER);
        for (double x : new double[]{-84, -71}) {
            w.add(Opening.rect(s.at(x, 40), 3.2, 0, 5.5));
            w.add(Opening.arch(s.at(x, 40), 2.6, 11, 16.5));
        }
        Walls.build(mb, w);
        // Exedra mit Halbkuppel
        Arc ap = new Arc(-94.75, 0, 6.5, 90, 270, true);
        w = new Wall(ap, 1.5, 0, 11, Mat.MARBLE, Mat.PLASTER);
        for (double deg : new double[]{140, 180, 220}) w.add(Opening.niche(ap.atDeg(deg), 2.0, 1.5, 6.5, 0.7, Mat.MARBLE));
        Walls.build(mb, w);
        int sk = 0;
        for (double deg : new double[]{140, 180, 220}) {
            double a = Math.toRadians(deg);
            Statues.statue(mb, -94.75 + 6.1 * Math.cos(a), 6.1 * Math.sin(a), 1.5, 3.8,
                    Math.atan2(-Math.cos(a), -Math.sin(a)), sk == 1 ? Statues.HERO : Statues.ATHLETE,
                    sk == 1 ? Mat.BRONZE : Mat.STATUE, 3000 + sk);
            sk++;
        }
        mb.group = Mat.G_ROOF;
        Shapes.cofferedDome(mb, -94.75, 11, 0, 5.75, 90, 270, 8, new double[]{8, 31, 52}, 0.3, Mat.VAULT, Mat.COFFER);
        Shapes.dome(mb, -94.75, 11, 0, 7.25, 90, 270, false, Mat.TILE, 16, 8);
        mb.group = Mat.G_BASE;
        // Säulenhallen an drei Seiten
        List<double[]> cols = new ArrayList<>();
        for (int i = 0; i <= 5; i++) cols.add(new double[]{-88 + 20.5 * i / 5, -33.5});
        for (int i = 0; i <= 5; i++) cols.add(new double[]{-88 + 20.5 * i / 5, 33.5});
        for (int i = 1; i < 16; i++) cols.add(new double[]{-67.5, -33.5 + 67.0 * i / 16});
        for (double[] cc : cols) Shapes.column(mb, cc[0], cc[1], 0, 6.4, 0.33, Mat.GIALLO, Mat.CORNICE);
        mb.box(-88.4, 6.4, -33.85, -67.1, 7.2, -33.15, Mat.CORNICE, true);
        mb.box(-88.4, 6.4, 33.15, -67.1, 7.2, 33.85, Mat.CORNICE, true);
        mb.box(-67.85, 6.4, -33.85, -67.15, 7.2, 33.85, Mat.CORNICE, true);
        mb.group = Mat.G_ROOF;
        // Pultdächer: Nord, Süd, Ost
        leanTo(mb, new double[]{-93, 9.6, -39}, new double[]{-62, 9.6, -39}, new double[]{-62, 7.2, -32.9}, new double[]{-93, 7.2, -32.9});
        leanTo(mb, new double[]{-93, 7.2, 32.9}, new double[]{-62, 7.2, 32.9}, new double[]{-62, 9.6, 39}, new double[]{-93, 9.6, 39});
        leanTo(mb, new double[]{-68.1, 7.2, -39}, new double[]{-62, 9.6, -39}, new double[]{-62, 9.6, 39}, new double[]{-68.1, 7.2, 39});
        mb.group = Mat.G_BASE;
        mb.mirrorX(v0, t0);
    }

    /** Pultdach aus vier Ecken: Ziegel oben, verputzte Untersicht. */
    private static void leanTo(MeshBuilder mb, double[] a, double[] b, double[] c, double[] d) {
        double[] e1 = {b[0] - a[0], b[1] - a[1], b[2] - a[2]}, e2 = {d[0] - a[0], d[1] - a[1], d[2] - a[2]};
        double nx = e1[1] * e2[2] - e1[2] * e2[1], ny = e1[2] * e2[0] - e1[0] * e2[2], nz = e1[0] * e2[1] - e1[1] * e2[0];
        if (ny < 0) { nx = -nx; ny = -ny; nz = -nz; }
        mb.quad(a, b, c, d, nx, ny, nz, Mat.TILE);
        double o = 0.3;
        mb.quad(new double[]{a[0], a[1] - o, a[2]}, new double[]{b[0], b[1] - o, b[2]},
                new double[]{c[0], c[1] - o, c[2]}, new double[]{d[0], d[1] - o, d[2]}, -nx, -ny, -nz, Mat.PLASTER);
    }

    // ============================================================= Frigidarium

    private static void frigidarium(MeshBuilder mb) {
        double zc = FZ + F_TW / 2; // 13.2
        double xe = FX + F_TW / 2; // 30.2
        double bay = 2 * FX / 3;   // 19.333
        double[] bays = {-bay, 0, bay};
        // Längsmauern mit großen Bögen zu den Seitenräumen
        Line n = new Line(-31.4, -zc, 31.4, -zc);
        Wall w = new Wall(n, F_TW, 0, F_YS, Mat.MARBLE, Mat.MARBLE);
        for (double x : bays) w.add(Opening.arch(n.at(x, -zc), 12, 0, 17));
        Walls.build(mb, w);
        Line s = new Line(31.4, zc, -31.4, zc);
        w = new Wall(s, F_TW, 0, F_YS, Mat.MARBLE, Mat.MARBLE);
        for (double x : bays) w.add(Opening.arch(s.at(x, zc), 12, 0, 17));
        Walls.build(mb, w);
        // Stirnmauern mit Durchgang
        Line we = new Line(-xe, FZ, -xe, -FZ);
        w = new Wall(we, F_TW, 0, F_YS, Mat.MARBLE, Mat.PLASTER);
        w.add(Opening.arch(we.at(-xe, 0), 10, 0, 14));
        Walls.build(mb, w);
        Line ea = new Line(xe, -FZ, xe, FZ);
        w = new Wall(ea, F_TW, 0, F_YS, Mat.MARBLE, Mat.PLASTER);
        w.add(Opening.arch(ea.at(xe, 0), 10, 0, 14));
        Walls.build(mb, w);
        // Lünetten mit Thermenfenstern
        double rOut = F_R + F_TW;
        for (double x : bays) {
            Walls.lunette(mb, n, n.at(x, -zc), F_YS, bay / 2, rOut, F_TW, 7.2, 9.3, 2, Mat.MARBLE, Mat.STUCCO, Mat.MARBLE, Mat.BRONZE);
            Walls.lunette(mb, s, s.at(x, zc), F_YS, bay / 2, rOut, F_TW, 7.2, 9.3, 2, Mat.MARBLE, Mat.STUCCO, Mat.MARBLE, Mat.BRONZE);
        }
        Walls.lunette(mb, we, we.at(-xe, 0), F_YS, rOut, rOut, F_TW, 9, 9, 2, Mat.MARBLE, Mat.STUCCO, Mat.MARBLE, Mat.BRONZE);
        Walls.lunette(mb, ea, ea.at(xe, 0), F_YS, rOut, rOut, F_TW, 9, 9, 2, Mat.MARBLE, Mat.STUCCO, Mat.MARBLE, Mat.BRONZE);
        // Gesims am Kämpfer
        mb.box(-FX, F_YS - 0.7, -FZ, FX, F_YS, -FZ + 0.45, Mat.CORNICE, true);
        mb.box(-FX, F_YS - 0.7, FZ - 0.45, FX, F_YS, FZ, Mat.CORNICE, true);
        mb.box(-FX, F_YS - 0.7, -FZ + 0.45, -FX + 0.45, F_YS, FZ - 0.45, Mat.CORNICE, true);
        mb.box(FX - 0.45, F_YS - 0.7, -FZ + 0.45, FX, F_YS, FZ - 0.45, Mat.CORNICE, true);
        // Die acht Granitsäulen mit Gebälkstücken
        for (double x : new double[]{-27.6, -bay / 2, bay / 2, 27.6}) {
            for (double sz : new double[]{-1, 1}) {
                double z = sz * (FZ - 1.05);
                Shapes.column(mb, x, z, 0, 12.6, 0.8, Mat.GRANITE, Mat.CORNICE);
                double za = sz * FZ, zb = sz * (FZ - 2.2);
                mb.box(x - 1.25, 12.6, Math.min(za, zb), x + 1.25, 14.4, Math.max(za, zb), Mat.CORNICE, true);
            }
        }
        // Kreuzgratgewölbe, innen und außen
        mb.group = Mat.G_ROOF;
        double[] cen = {-bay, 0, bay}, hw = {bay / 2, bay / 2, bay / 2};
        double[] xs = Shapes.samples(-FX, FX, 1.5, cen, hw, 20);
        double[] zs = Shapes.samples(-FZ, FZ, 1.5, new double[]{0}, new double[]{FZ}, 28);
        Shapes.heightVault(mb, xs, zs, Shapes.groinX(-FX, FX, 0, FZ, 3, bay / 2, F_YS, F_R), true, Mat.VAULT);
        double[] xo = Shapes.samples(-31.4, 31.4, 1.5, cen, hw, 20);
        double[] zo = Shapes.samples(-FZ - F_TW, FZ + F_TW, 1.5, new double[]{0}, new double[]{rOut}, 28);
        Shapes.heightVault(mb, xo, zo, Shapes.groinX(-FX, FX, 0, rOut, 3, bay / 2, F_YS, rOut), false, Mat.TILE);
        mb.group = Mat.G_BASE;
        // Mauer zum Natatio-Hof (Vorderseite zum Hof, Nischen dort)
        Line cw = new Line(32.4, -26, -32.4, -26);
        w = new Wall(cw, 2, 0, 18.6, Mat.MARBLE, Mat.MARBLE);
        for (double x : bays) w.add(Opening.arch(cw.at(x, -26), 8, 0, 12));
        for (double x : new double[]{-26, 26}) w.add(Opening.niche(cw.at(x, -26), 2.4, 9.2, 14.6, 1.0, Mat.MARBLE));
        Walls.build(mb, w);
        Statues.statue(mb, -26, -26.5, 9.2, 3.5, Math.PI, Statues.DRAPED, Mat.STATUE, 4001);
        Statues.statue(mb, 26, -26.5, 9.2, 3.5, Math.PI, Statues.DRAPED, Mat.STATUE, 4002);
        // Kolossalstatuen an den Stirnseiten
        Statues.statue(mb, -27.2, -7.8, 0, 7.2, Math.PI / 2, Statues.HERO, Mat.BRONZE, 4101);
        Statues.statue(mb, -27.2, 7.8, 0, 7.2, Math.PI / 2, Statues.ATHLETE, Mat.STATUE, 4102);
        Statues.statue(mb, 27.2, -7.8, 0, 7.2, -Math.PI / 2, Statues.ATHLETE, Mat.STATUE, 4103);
        Statues.statue(mb, 27.2, 7.8, 0, 7.2, -Math.PI / 2, Statues.HERO, Mat.STATUE, 4104);
    }

    // ============================================================= Tepidarium

    private static void tepidarium(MeshBuilder mb) {
        // Mauer z = 19 zwischen den südlichen Seitenräumen und dem Tepidarium
        Line a = new Line(-11, 19, -32.4, 19);
        Walls.build(mb, new Wall(a, 2, 0, 16.6, Mat.MARBLE, Mat.PLASTER));
        Line b = new Line(32.4, 19, 11, 19);
        Walls.build(mb, new Wall(b, 2, 0, 16.6, Mat.MARBLE, Mat.PLASTER));
        Line m = new Line(11, 19, -11, 19);
        Wall w = new Wall(m, 2, 0, 12, Mat.MARBLE, Mat.MARBLE);
        w.add(Opening.arch(m.at(0, 19), 8, 0, 11));
        w.capTop = false;
        Walls.build(mb, w);
        Walls.lunette(mb, m, m.at(0, 19), 12, 11, 11, 2, 6.5, 6.5, 2, Mat.STUCCO, Mat.MARBLE, Mat.MARBLE, Mat.BRONZE);
        // Seitenmauern x = ±11 mit Durchgängen zu den Becken
        int v0 = mb.vertexCount(), t0 = mb.triCount();
        Line sw = new Line(-11, 32.5, -11, 19);
        w = new Wall(sw, 2, 0, 12, Mat.MARBLE, Mat.PLASTER);
        w.add(Opening.arch(sw.at(-11, 25.5), 6, 0, 9.5));
        Walls.build(mb, w);
        mb.mirrorX(v0, t0);
        // Tonnengewölbe entlang z
        mb.group = Mat.G_ROOF;
        double[] xs = Shapes.samples(-10, 10, 1.2, new double[]{0}, new double[]{10}, 24);
        double[] zs = Shapes.samples(20, 32.5, 1.5, new double[0], new double[0], 0);
        Shapes.heightVault(mb, xs, zs, Shapes.barrelZ(0, 10, 12, 10), true, Mat.VAULT);
        double[] xo = Shapes.samples(-11, 11, 1.2, new double[]{0}, new double[]{11}, 24);
        double[] zo = Shapes.samples(19, 32.5, 1.5, new double[0], new double[0], 0);
        Shapes.heightVault(mb, xo, zo, Shapes.barrelZ(0, 11, 12, 11), false, Mat.TILE);
        mb.group = Mat.G_BASE;
    }

    // ============================================================== Caldarium

    private static void caldarium(MeshBuilder mb) {
        double rc = CAL_R + CAL_T / 2; // Mittellinie 19
        Arc drum = new Arc(0, CAL_Z, rc, -67.5, 292.5, true);
        Wall w = new Wall(drum, CAL_T, 0, CAL_YS, Mat.MARBLE, Mat.STUCCO);
        for (int k = 0; k < 8; k++) {
            double a = -90 + 45 * k, u = drum.atDeg(a);
            if (k == 0) {
                w.add(Opening.arch(u, 9, 0, 14));
            } else if (k == 3 || k == 4 || k == 5) {
                w.add(Opening.arch(u, 7.6, 4, 17));
            } else {
                w.add(Opening.niche(u, 7, 1, 11, 1.4, Mat.MARBLE));
            }
            w.add(Opening.arch(u, 6.2, 18.6, 25.6));
        }
        Walls.build(mb, w);
        for (int k : new int[]{1, 2, 6, 7}) {
            double a = Math.toRadians(-90 + 45 * k);
            Statues.statue(mb, 18.2 * Math.cos(a), CAL_Z + 18.2 * Math.sin(a), 1, 6.0,
                    Math.atan2(-Math.cos(a), -Math.sin(a)), k % 3, k == 2 || k == 6 ? Mat.BRONZE : Mat.STATUE, 5000 + k);
        }
        // Gesims unter der Kuppel
        Shapes.drum(mb, 0, CAL_Z, CAL_R - 0.45, CAL_YS - 0.8, CAL_YS, true, Mat.CORNICE, 64);
        Shapes.ring(mb, 0, CAL_Z, CAL_R - 0.45, CAL_R, 0, 360, CAL_YS - 0.8, false, Mat.CORNICE, 64);
        Shapes.ring(mb, 0, CAL_Z, CAL_R - 0.45, CAL_R, 0, 360, CAL_YS, true, Mat.CORNICE, 64);
        // Gesims zwischen den Fensterreihen
        Shapes.drum(mb, 0, CAL_Z, CAL_R - 0.3, 17.4, 17.9, true, Mat.CORNICE, 64);
        Shapes.ring(mb, 0, CAL_Z, CAL_R - 0.3, CAL_R, 0, 360, 17.9, true, Mat.CORNICE, 64);
        Shapes.ring(mb, 0, CAL_Z, CAL_R - 0.3, CAL_R, 0, 360, 17.4, false, Mat.CORNICE, 64);
        // Kuppel
        mb.group = Mat.G_ROOF;
        Shapes.cofferedDome(mb, 0, CAL_YS, CAL_Z, CAL_R, 0, 360, 32, new double[]{6, 18, 29, 39, 48, 56}, 0.5, Mat.VAULT, Mat.COFFER);
        Shapes.dome(mb, 0, CAL_YS, CAL_Z, CAL_R + CAL_T, 0, 360, false, Mat.TILE, 64, 18);
        mb.group = Mat.G_BASE;
        // Wannen (alvei) in den drei Fensterbuchten zum Garten
        for (int k = 3; k <= 5; k++) {
            double a = -90 + 45 * k;
            double r0 = 11.8;
            Arc rim = new Arc(0, CAL_Z, r0 + 0.25, a - 16, a + 16, false);
            Wall pw = new Wall(rim, 0.5, 0, 0.9, Mat.MARBLE, Mat.MARBLE);
            pw.mTop = Mat.GIALLO;
            Walls.build(mb, pw);
            for (double da : new double[]{-16, 16}) {
                double ar = Math.toRadians(a + da), c = Math.cos(ar), s = Math.sin(ar);
                Line side = new Line(c * r0, CAL_Z + s * r0, c * CAL_R, CAL_Z + s * CAL_R);
                Wall sw = new Wall(side, 0.5, 0, 0.9, Mat.MARBLE, Mat.MARBLE);
                sw.mTop = Mat.GIALLO;
                Walls.build(mb, sw);
            }
            Shapes.ring(mb, 0, CAL_Z, r0 + 0.5, CAL_R, a - 15.6, a + 15.6, 0.7, true, Mat.WATER, 12);
        }
    }

    // ================================================================= Dächer

    private static void roofs(MeshBuilder mb) {
        mb.group = Mat.G_ROOF;
        mb.maxEdge = 3;
        List<double[]> holes = new ArrayList<>();
        holes.add(new double[]{-32.4, -BZ, 32.4, BZ});
        holes.add(new double[]{-94, -40, -61, 40});
        holes.add(new double[]{61, -40, 94, 40});
        double e = BX - T_PER / 2, f = BZ - T_PER / 2;
        Shapes.floorRect(mb, -e, -f, e, f, Y_TERRACE, holes, true, Mat.ROOFFLAT);
        Shapes.floorRect(mb, -e, -f, e, f, Y_TERRACE - 0.6, holes, false, Mat.PLASTER);
        // Seitenräume des Frigidariums
        mb.rectH(-32.4, -26, 32.4, -13.2, 18, true, Mat.ROOFFLAT);
        mb.rectH(-32.4, -26, 32.4, -13.2, 17.4, false, Mat.VAULT);
        mb.rectH(-32.4, 13.2, 32.4, 19, 16, true, Mat.ROOFFLAT);
        mb.rectH(-32.4, 13.2, 32.4, 19, 15.4, false, Mat.VAULT);
        // Räume beiderseits von Tepidarium und Caldarium
        double rc = CAL_R + CAL_T / 2;
        for (double sgn : new double[]{-1, 1}) {
            double xa = sgn < 0 ? -32.4 : 11, xb = sgn < 0 ? -11 : 32.4;
            Shapes.slabMinusCircle(mb, xa, 19, xb, 53.75, 16, 0, CAL_Z, rc, true, Mat.ROOFFLAT);
            Shapes.slabMinusCircle(mb, xa, 19, xb, 53.75, 15.4, 0, CAL_Z, rc, false, Mat.PLASTER);
        }
        mb.group = Mat.G_BASE;
        mb.maxEdge = 2.5;
    }

    // ========================================================== Umfassung, Park

    private static void precinct(MeshBuilder mb) {
        mb.maxEdge = 4;
        Line n = new Line(-PX - 1.5, PZ0, PX + 1.5, PZ0);
        Wall w = new Wall(n, 3, 0, 10, Mat.STUCCO, Mat.STUCCO);
        for (double x = -160; x <= 160; x += 7) {
            if (Math.abs(x) < 9) continue;
            w.add(Opening.niche(n.at(x, PZ0), 4, 0, 6, 2.2, Mat.PLASTER));
        }
        w.add(Opening.arch(n.at(0, PZ0), 8, 0, 8.5));
        Walls.build(mb, w);
        Walls.build(mb, new Wall(new Line(PX + 1.5, PZ1, -PX - 1.5, PZ1), 3, 0, 10, Mat.STUCCO, Mat.STUCCO));
        int v0 = mb.vertexCount(), t0 = mb.triCount();
        Walls.build(mb, new Wall(new Line(-PX, PZ1 + 1.5, -PX, 46), 3, 0, 10, Mat.STUCCO, Mat.STUCCO));
        Walls.build(mb, new Wall(new Line(-PX, -18, -PX, PZ0 - 1.5), 3, 0, 10, Mat.STUCCO, Mat.STUCCO));
        Arc ex = new Arc(-PX, 14, 32, 90, 270, true);
        w = new Wall(ex, 3, 0, 12, Mat.STUCCO, Mat.STUCCO);
        for (double deg = 105; deg <= 255; deg += 15) w.add(Opening.niche(ex.atDeg(deg), 3.6, 1, 7.5, 1.6, Mat.PLASTER));
        Walls.build(mb, w);
        int ek = 0;
        for (double deg = 105; deg <= 255; deg += 15) {
            double a = Math.toRadians(deg);
            Statues.statue(mb, -PX + 31.3 * Math.cos(a), 14 + 31.3 * Math.sin(a), 1, 4.8,
                    Math.atan2(-Math.cos(a), -Math.sin(a)), ek % 3, ek % 4 == 2 ? Mat.BRONZE : Mat.STATUE, 6000 + ek);
            ek++;
        }
        // Bibliothek
        mb.box(-152, 0, 136, -116, 15, 176.5, Mat.STUCCO, false);
        mb.group = Mat.G_ROOF;
        mb.box(-153, 15, 135, -115, 15.8, 177, Mat.TILE, true);
        mb.group = Mat.G_BASE;
        mb.mirrorX(v0, t0);
        // Stufen über den Zisternen
        for (int i = 0; i < 10; i++) {
            mb.box(-110, 0.7 * i, 140 + 3.6 * i, 110, 0.7 * (i + 1), 176.5, Mat.PAVING, false);
        }
        mb.maxEdge = 2.5;
    }

    private static void trees(MeshBuilder mb) {
        Random rnd = new Random(216);
        List<double[]> spots = new ArrayList<>();
        for (double x = -150; x <= 150; x += 24) spots.add(new double[]{x + rnd.nextGaussian() * 2, -126 + rnd.nextGaussian() * 2});
        for (double x = -138; x <= 138; x += 23) {
            if (Math.abs(x) < 14) continue;
            spots.add(new double[]{x + rnd.nextGaussian() * 2, -86 + rnd.nextGaussian() * 2});
        }
        for (double z = -60; z <= 118; z += 22) {
            spots.add(new double[]{-142 + rnd.nextGaussian() * 2, z});
            spots.add(new double[]{142 + rnd.nextGaussian() * 2, z});
        }
        for (int i = 0; i < 26; i++) {
            double x = -150 + 300 * rnd.nextDouble(), z = 82 + 44 * rnd.nextDouble();
            if (Math.abs(x) < 40 && z < 95) continue;
            spots.add(new double[]{x, z});
        }
        for (double[] s : spots) {
            double h = 9 + rnd.nextDouble() * 4, cr = 4.5 + rnd.nextDouble() * 2.2;
            Shapes.pine(mb, s[0], s[1], h, cr, 0.6 + rnd.nextDouble() * 1.2, rnd.nextDouble() * Math.PI * 2, rnd.nextLong());
        }
        // Zypressen am Hauptweg und an den Ecken des Zentralbaus
        for (double z = -140; z <= -64; z += 9) {
            Shapes.cypress(mb, -7, z, 13 + rnd.nextDouble() * 3);
            Shapes.cypress(mb, 7, z, 13 + rnd.nextDouble() * 3);
        }
        for (double[] c : new double[][]{{-118, -66}, {118, -66}, {-118, 66}, {118, 66}}) {
            Shapes.cypress(mb, c[0], c[1], 15);
            Shapes.cypress(mb, c[0] + Math.signum(c[0]) * 4, c[1], 13);
        }
    }

    // ============================================================= Wasser

    private static List<WaterBody> waters() {
        List<WaterBody> w = new ArrayList<>();
        w.add(new WaterBody("Natatio", WaterBody.Shape.RECT, -0.25, -1.6, -27, -51.2, 27, -28.3));
        w.add(new WaterBody("Frigidarium West", WaterBody.Shape.RECT, -0.2, -1.2, -27, -23.5, -13, -16));
        w.add(new WaterBody("Frigidarium Ost", WaterBody.Shape.RECT, -0.2, -1.2, 13, -23.5, 27, -16));
        w.add(new WaterBody("Tepidarium West", WaterBody.Shape.RECT, -0.2, -1.2, -28, 22, -15, 29));
        w.add(new WaterBody("Tepidarium Ost", WaterBody.Shape.RECT, -0.2, -1.2, 15, 22, 28, 29));
        for (int k = 3; k <= 5; k++) {
            double a = -90 + 45 * k;
            w.add(new WaterBody("Caldarium-Wanne " + (k - 2), WaterBody.Shape.SECTOR, 0.7, 0.06, 0, CAL_Z, 12.3, CAL_R, a - 15.6, a + 15.6));
        }
        w.add(new WaterBody("Labrum", WaterBody.Shape.DISC, 1.12, 0.9, 0, CAL_Z, 1.98));
        return w;
    }

    /** Labrum im Caldarium und bronzene Wasserspeier an allen Becken. */
    private static void waterworks(MeshBuilder mb, List<Fountain> fo) {
        double keep = mb.maxEdge;
        mb.maxEdge = 1.0;
        // Labrum: Fuß, Schale aus Porphyr, Wasser, Düse
        mb.cylinder(0, CAL_Z, 0.06, 0.85, 0.55, 0.42, 20, Mat.PORPHYRY, false);
        mb.cylinder(0, CAL_Z, 0.8, 1.25, 1.1, 2.25, 40, Mat.PORPHYRY, false);
        Shapes.ring(mb, 0, CAL_Z, 2.0, 2.25, 0, 360, 1.25, true, Mat.GIALLO, 40);
        Shapes.drum(mb, 0, CAL_Z, 2.0, 0.9, 1.25, true, Mat.PORPHYRY, 40);
        Shapes.ring(mb, 0, CAL_Z, 0, 2.0, 0, 360, 0.9, true, Mat.PORPHYRY, 40);
        Shapes.ring(mb, 0, CAL_Z, 0, 1.98, 0, 360, 1.12, true, Mat.WATER, 40);
        mb.cylinder(0, CAL_Z, 1.12, 1.42, 0.12, 0.07, 10, Mat.BRONZE, true);
        fo.add(new Fountain(0, 1.44, CAL_Z, 0, 3.1, 0, 0.12, 320));
        // Natatio: vier Speier auf dem Nordrand
        for (double x : new double[]{-18, -6, 6, 18}) {
            spout(mb, x, -51.95, 0, 0, 0.8);
            fo.add(new Fountain(x, 0.7, -51.3, 0, 1.3, 2.6, 0.06, 240));
        }
        // Frigidarium-Becken
        for (double x : new double[]{-20, 20}) {
            spout(mb, x, -24.4, 0, 0, 0.8);
            fo.add(new Fountain(x, 0.7, -23.75, 0, 1.0, 2.1, 0.06, 200));
        }
        // Becken neben dem Tepidarium
        spout(mb, -28.8, 25.5, 0, Math.PI / 2, 0.8);
        fo.add(new Fountain(-28.15, 0.7, 25.5, 2.1, 1.0, 0, 0.06, 200));
        spout(mb, 28.8, 25.5, 0, -Math.PI / 2, 0.8);
        fo.add(new Fountain(28.15, 0.7, 25.5, -2.1, 1.0, 0, 0.06, 200));
        // Caldarium-Wannen: Speier an der Wand
        for (int k = 3; k <= 5; k++) {
            double a = Math.toRadians(-90 + 45 * k), c = Math.cos(a), sn = Math.sin(a);
            int v0 = mb.vertexCount();
            mb.box(-0.22, 1.2, -0.3, 0.22, 1.6, 0.05, Mat.BRONZE, true);
            mb.cylinder(0, 0.12, 1.36, 1.44, 0.07, 0.06, 8, Mat.BRONZE, false);
            mb.box(-0.06, 1.34, 0.0, 0.06, 1.44, 0.2, Mat.BRONZE, true);
            mb.transform(v0, Math.atan2(-c, -sn), c * (CAL_R - 0.05), 0, CAL_Z + sn * (CAL_R - 0.05));
            double r = CAL_R - 0.3;
            fo.add(new Fountain(c * r, 1.4, CAL_Z + sn * r, -c * 1.8, 0.5, -sn * 1.8, 0.06, 200));
        }
        mb.maxEdge = keep;
    }

    /** Sockel aus Marmor mit bronzenem Speier; Ausfluss in Richtung (sin yaw, cos yaw). */
    private static void spout(MeshBuilder mb, double x, double z, double y0, double yaw, double h) {
        int v0 = mb.vertexCount();
        mb.box(-0.28, 0, -0.4, 0.28, h, 0.4, Mat.CORNICE, false);
        mb.box(-0.16, h - 0.25, 0.3, 0.16, h - 0.02, 0.55, Mat.BRONZE, true);
        mb.box(-0.06, h - 0.17, 0.55, 0.06, h - 0.07, 0.68, Mat.BRONZE, true);
        mb.transform(v0, yaw, x, y0, z);
    }

    // ================================================================= Räume

    private static List<Room> rooms() {
        List<Room> r = new ArrayList<>();
        r.add(new Room("Übersicht", null, null, null, 0, false));
        r.add(new Room("Natatio", new double[]{-24, 2.2, -29.5}, new double[]{16, 8, -52},
                new double[]{-31.4, -2, -52.5, 31.4, 24, -27}, 0, false));
        r.add(new Room("Frigidarium", new double[]{-25.5, 1.9, 7}, new double[]{12, 13, -4},
                new double[]{-FX, 0.2, -FZ, FX, 33, FZ}, 0, true));
        r.add(new Room("Tepidarium", new double[]{0, 1.8, 21}, new double[]{0, 9, 56},
                new double[]{-10, 0.2, 20, 10, 22, 32.5}, 0, true));
        r.add(new Room("Caldarium", new double[]{-2, 2.2, 36.5}, new double[]{6, 13, 66},
                new double[]{0, 0.3, CAL_Z, 0, 44, 0}, CAL_R - 0.3, true));
        r.add(new Room("Palästra West", new double[]{-68.5, 1.8, 31}, new double[]{-90, 6, -12},
                new double[]{-93, 0, -39, -62, 20, 39}, 0, false));
        return r;
    }
}
