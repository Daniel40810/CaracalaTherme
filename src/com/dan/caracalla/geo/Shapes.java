package com.dan.caracalla.geo;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/** Gewölbe, Kuppeln, Böden, Becken, Säulen und Bäume. */
public final class Shapes {

    private Shapes() { }

    // ----------------------------------------------------------------- Gewölbe

    /** Höhe und Gefälle einer Gewölbefläche über dem Grundriss. */
    public interface HeightFn {
        /** Liefert die Höhe; grad[0] = dh/dx, grad[1] = dh/dz. */
        double h(double x, double z, double[] grad);
    }

    private static double ell(double d, double a, double r, double[] dd) {
        double q = d / a, s = 1 - q * q;
        if (s <= 0) { dd[0] = 0; return 0; }
        double sq = Math.sqrt(Math.max(s, 1e-5));
        double der = -r * (d / (a * a)) / sq;
        dd[0] = Math.max(-40, Math.min(40, der));
        return r * Math.sqrt(s);
    }

    /**
     * Kreuzgratgewölbe: Tonne entlang x (Halbbreite az, Stich R) gekreuzt von
     * {@code bays} Quertonnen (Halbbreite ax je Joch, Stich R). bays = 0 ergibt eine reine Tonne.
     */
    public static HeightFn groinX(double x0, double x1, double zc, double az, int bays, double ax, double ys, double R) {
        double bw = bays > 0 ? (x1 - x0) / bays : 0;
        double[] dd = new double[1];
        return (x, z, g) -> {
            double hA = ell(z - zc, az, R, dd);
            double gA = dd[0];
            double best = hA;
            g[0] = 0; g[1] = gA;
            for (int i = 0; i < bays; i++) {
                double cx = x0 + bw * (i + 0.5);
                double hB = ell(x - cx, ax, R, dd);
                if (hB > best) { best = hB; g[0] = dd[0]; g[1] = 0; }
            }
            return ys + best;
        };
    }

    /** Tonne entlang z über x-Halbbreite ax. */
    public static HeightFn barrelZ(double xc, double ax, double ys, double R) {
        double[] dd = new double[1];
        return (x, z, g) -> {
            double h = ell(x - xc, ax, R, dd);
            g[0] = dd[0]; g[1] = 0;
            return ys + h;
        };
    }

    /** Stützstellen: Sinus-verteilt um Mittelpunkte plus gleichmäßiges Raster, sortiert. */
    public static double[] samples(double lo, double hi, double step, double[] centers, double[] halfWidths, int n) {
        TreeSet<Double> s = new TreeSet<>();
        s.add(lo); s.add(hi);
        for (double x = lo; x < hi; x += step) s.add(x);
        for (int c = 0; c < centers.length; c++) {
            for (int k = 0; k <= n; k++) {
                double th = -Math.PI / 2 + Math.PI * k / n;
                double x = centers[c] + halfWidths[c] * Math.sin(th);
                if (x >= lo && x <= hi) s.add(x);
            }
        }
        double[] out = new double[s.size()];
        int i = 0;
        double prev = -1e9;
        for (double x : s) {
            if (x - prev > 0.03 || x == hi) { out[i++] = x; prev = x; }
        }
        return Arrays.copyOf(out, i);
    }

    /** Gewölbe als Höhenfeld über xs × zs; interior = Unterseite (Normalen nach unten). */
    public static void heightVault(MeshBuilder mb, double[] xs, double[] zs, HeightFn hf, boolean interior, int m) {
        int nx = xs.length, nz = zs.length;
        int[] g = new int[nx * nz];
        double[] gr = new double[2];
        for (int j = 0; j < nz; j++) {
            for (int i = 0; i < nx; i++) {
                double y = hf.h(xs[i], zs[j], gr);
                double s = interior ? 1 : -1;
                g[j * nx + i] = mb.v(xs[i], y, zs[j], s * gr[0], -s, s * gr[1]);
            }
        }
        for (int j = 0; j + 1 < nz; j++) {
            for (int i = 0; i + 1 < nx; i++) {
                int a = g[j * nx + i], b = g[j * nx + i + 1], c = g[(j + 1) * nx + i + 1], d = g[(j + 1) * nx + i];
                mb.tri(a, b, c, m);
                mb.tri(a, c, d, m);
            }
        }
    }

    /** Kuppel (oder Halbkuppel über einen Azimutbereich) über dem Kämpfer ys. */
    public static void dome(MeshBuilder mb, double cx, double ys, double cz, double R,
                            double a0Deg, double a1Deg, boolean interior, int m, int nAz, int nEl) {
        double s = interior ? -1 : 1;
        mb.patch((u, v, p, n) -> {
            double cv = Math.cos(v), sv = Math.sin(v), cu = Math.cos(u), su = Math.sin(u);
            p[0] = cx + R * cv * cu; p[1] = ys + R * sv; p[2] = cz + R * cv * su;
            n[0] = s * cv * cu; n[1] = s * sv; n[2] = s * cv * su;
        }, Math.toRadians(a0Deg), Math.toRadians(a1Deg), nAz, 0, Math.PI / 2, nEl, m);
    }

    /** Waagrechter Ring (Kreisringausschnitt) in Höhe y. */
    public static void ring(MeshBuilder mb, double cx, double cz, double r0, double r1, double a0Deg, double a1Deg,
                            double y, boolean up, int m, int nAz) {
        double s = up ? 1 : -1;
        mb.patch((u, v, p, n) -> {
            p[0] = cx + v * Math.cos(u); p[1] = y; p[2] = cz + v * Math.sin(u);
            n[0] = 0; n[1] = s; n[2] = 0;
        }, Math.toRadians(a0Deg), Math.toRadians(a1Deg), nAz, r0, r1, Math.max(1, mb.segs(r1 - r0)), m);
    }

    /** Senkrechte Zylinderwand (innen oder außen sichtbar). */
    public static void drum(MeshBuilder mb, double cx, double cz, double r, double y0, double y1, boolean inward, int m, int nAz) {
        double s = inward ? -1 : 1;
        mb.patch((u, v, p, n) -> {
            double c = Math.cos(u), sn = Math.sin(u);
            p[0] = cx + r * c; p[1] = v; p[2] = cz + r * sn;
            n[0] = s * c; n[1] = 0; n[2] = s * sn;
        }, 0, 2 * Math.PI, nAz, y0, y1, Math.max(1, mb.segs(y1 - y0)), m);
    }

    // ------------------------------------------------------------ Böden, Becken

    /** Rechteck in Höhe y ohne die Löcher (je {x0,z0,x1,z1}). */
    public static void floorRect(MeshBuilder mb, double x0, double z0, double x1, double z1, double y,
                                 List<double[]> holes, boolean up, int m) {
        TreeSet<Double> xs = new TreeSet<>(), zs = new TreeSet<>();
        xs.add(x0); xs.add(x1); zs.add(z0); zs.add(z1);
        for (double[] hl : holes) {
            xs.add(Math.max(x0, Math.min(x1, hl[0]))); xs.add(Math.max(x0, Math.min(x1, hl[2])));
            zs.add(Math.max(z0, Math.min(z1, hl[1]))); zs.add(Math.max(z0, Math.min(z1, hl[3])));
        }
        Double[] X = xs.toArray(new Double[0]), Z = zs.toArray(new Double[0]);
        for (int i = 0; i + 1 < X.length; i++) {
            for (int j = 0; j + 1 < Z.length; j++) {
                double cx = (X[i] + X[i + 1]) / 2, cz = (Z[j] + Z[j + 1]) / 2;
                if (X[i + 1] - X[i] < 1e-6 || Z[j + 1] - Z[j] < 1e-6) continue;
                boolean inHole = false;
                for (double[] hl : holes) {
                    if (cx > hl[0] && cx < hl[2] && cz > hl[1] && cz < hl[3]) { inHole = true; break; }
                }
                if (!inHole) mb.rectH(X[i], Z[j], X[i + 1], Z[j + 1], y, up, m);
            }
        }
    }

    /** Rechteck in Höhe y ohne den Kreis (cx,cz,R); Zeilen von 1 m. */
    public static void slabMinusCircle(MeshBuilder mb, double x0, double z0, double x1, double z1, double y,
                                       double cx, double cz, double R, boolean up, int m) {
        int rows = (int) Math.ceil(z1 - z0);
        for (int r = 0; r < rows; r++) {
            double za = z0 + (z1 - z0) * r / rows, zb = z0 + (z1 - z0) * (r + 1) / rows;
            double zc = (za + zb) / 2, dz = zc - cz;
            if (Math.abs(dz) < R) {
                double hw = Math.sqrt(R * R - dz * dz);
                double ea = cx - hw, eb = cx + hw;
                if (ea > x0) mb.rectH(x0, za, Math.min(ea, x1), zb, y, up, m);
                if (eb < x1) mb.rectH(Math.max(eb, x0), za, x1, zb, y, up, m);
            } else {
                mb.rectH(x0, za, x1, zb, y, up, m);
            }
        }
    }

    /** Eingelassenes Becken: Wände, Boden und Wasserspiegel. */
    public static void pool(MeshBuilder mb, double x0, double z0, double x1, double z1, double depth, double waterY,
                            int mWall, int mWater) {
        double yb = -depth;
        mb.quad(new double[]{x0, yb, z0}, new double[]{x1, yb, z0}, new double[]{x1, 0, z0}, new double[]{x0, 0, z0}, 0, 0, 1, mWall);
        mb.quad(new double[]{x0, yb, z1}, new double[]{x1, yb, z1}, new double[]{x1, 0, z1}, new double[]{x0, 0, z1}, 0, 0, -1, mWall);
        mb.quad(new double[]{x0, yb, z0}, new double[]{x0, yb, z1}, new double[]{x0, 0, z1}, new double[]{x0, 0, z0}, 1, 0, 0, mWall);
        mb.quad(new double[]{x1, yb, z0}, new double[]{x1, yb, z1}, new double[]{x1, 0, z1}, new double[]{x1, 0, z0}, -1, 0, 0, mWall);
        mb.rectH(x0, z0, x1, z1, yb, true, mWall);
        mb.rectH(x0, z0, x1, z1, waterY, true, mWater);
    }

    // ----------------------------------------------------------- Säulen, Bäume

    /** Säule mit Plinthe, Basis, leicht verjüngtem Schaft und Kapitell. */
    public static void column(MeshBuilder mb, double x, double z, double y0, double h, double r, int mShaft, int mTrim) {
        double pl = 0.45 * r * 2;
        double keep = mb.maxEdge;
        mb.maxEdge = Math.max(0.6, r * 1.5);
        mb.box(x - 1.3 * r, y0, z - 1.3 * r, x + 1.3 * r, y0 + pl, z + 1.3 * r, mTrim, false);
        mb.cylinder(x, z, y0 + pl, y0 + pl + 0.35 * r, 1.18 * r, 1.08 * r, 16, mTrim, false);
        double sA = y0 + pl + 0.35 * r, sB = y0 + h - 1.3 * r;
        mb.maxEdge = Math.max(1.5, (sB - sA) / 4);
        mb.cylinder(x, z, sA, sB, r, 0.86 * r, 18, mShaft, false);
        mb.maxEdge = Math.max(0.6, r * 1.5);
        mb.cylinder(x, z, sB, sB + 0.18 * r, 0.95 * r, 0.95 * r, 16, mTrim, false);
        mb.cylinder(x, z, sB + 0.18 * r, y0 + h - 0.35 * r, 0.9 * r, 1.3 * r, 16, mTrim, false);
        mb.box(x - 1.42 * r, y0 + h - 0.35 * r, z - 1.42 * r, x + 1.42 * r, y0 + h, z + 1.42 * r, mTrim, true);
        mb.maxEdge = keep;
    }

    /** Pinie: schlanker, leicht geneigter Stamm und flache Schirmkrone. */
    public static void pine(MeshBuilder mb, double x, double z, double h, double crown, double lean, double dir, long seed) {
        double tx = x + Math.cos(dir) * lean, tz = z + Math.sin(dir) * lean;
        double keep = mb.maxEdge;
        mb.maxEdge = h;
        mb.patch((u, v, p, n) -> {
            double r = 0.38 - 0.16 * v, c = Math.cos(u), s = Math.sin(u);
            p[0] = x + (tx - x) * v + r * c; p[1] = h * v; p[2] = z + (tz - z) * v + r * s;
            n[0] = c; n[1] = 0.05; n[2] = s;
        }, 0, 2 * Math.PI, 7, 0, 1, 3, Mat.BARK);
        java.util.Random rnd = new java.util.Random(seed);
        int blobs = 3;
        for (int b = 0; b < blobs; b++) {
            double ang = rnd.nextDouble() * Math.PI * 2, off = b == 0 ? 0 : crown * 0.35;
            double rx = crown * (b == 0 ? 1.0 : 0.62), ry = crown * (b == 0 ? 0.36 : 0.3);
            mb.ellipsoid(tx + Math.cos(ang) * off, h + ry * 0.45 + (b == 0 ? 0 : rnd.nextDouble() * 0.8),
                    tz + Math.sin(ang) * off, rx, ry, rx * (0.85 + rnd.nextDouble() * 0.3), 14, 6, Mat.PINE);
        }
        mb.maxEdge = keep;
    }

    /** Zypresse: schmale, hohe Spindel. */
    public static void cypress(MeshBuilder mb, double x, double z, double h) {
        double keep = mb.maxEdge;
        mb.maxEdge = h;
        mb.cylinder(x, z, 0, 1.2, 0.22, 0.2, 6, Mat.BARK, false);
        mb.patch((u, v, p, n) -> {
            double t = v; // 0 unten .. 1 Spitze
            double r = 1.35 * Math.sin(Math.PI * Math.pow(t, 0.7)) * (1 - 0.25 * t) + 0.02;
            double c = Math.cos(u), s = Math.sin(u);
            p[0] = x + r * c; p[1] = 0.8 + (h - 0.8) * t; p[2] = z + r * s;
            n[0] = c; n[1] = 0.25 - 0.5 * (1 - t); n[2] = s;
        }, 0, 2 * Math.PI, 10, 0, 1, 8, Mat.CYPRESS);
        mb.maxEdge = keep;
    }

    // ------------------------------------------------------------ Kassetten

    private static void sph(double cx, double ys, double cz, double r, double a, double e, double[] o) {
        o[0] = cx + r * Math.cos(e) * Math.cos(a); o[1] = ys + r * Math.sin(e); o[2] = cz + r * Math.cos(e) * Math.sin(a);
    }

    /** Kugelstück in festem Radius zwischen (a0..a1, e0..e1), Normale zur Mitte. */
    private static void sphPatch(MeshBuilder mb, double cx, double ys, double cz, double r,
                                 double a0, double a1, double e0, double e1, int m) {
        int nu = Math.max(1, (int) Math.ceil(Math.abs(a1 - a0) / Math.toRadians(5.6)));
        int nv = Math.max(1, (int) Math.ceil(Math.abs(e1 - e0) / Math.toRadians(5.6)));
        mb.patch((u, v, p, n) -> {
            double ce = Math.cos(v);
            p[0] = cx + r * ce * Math.cos(u); p[1] = ys + r * Math.sin(v); p[2] = cz + r * ce * Math.sin(u);
            n[0] = -ce * Math.cos(u); n[1] = -Math.sin(v); n[2] = -ce * Math.sin(u);
        }, a0, a1, nu, e0, e1, nv, m);
    }

    /** Ebenes Viereck, Normale zur Kuppelmitte hin ausgerichtet. */
    private static void quadToward(MeshBuilder mb, double[] a, double[] b, double[] c, double[] d, double tx, double ty, double tz, int m) {
        double ux = c[0] - a[0], uy = c[1] - a[1], uz = c[2] - a[2], vx = d[0] - b[0], vy = d[1] - b[1], vz = d[2] - b[2];
        double nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
        double mx = (a[0] + c[0]) / 2, my = (a[1] + c[1]) / 2, mz = (a[2] + c[2]) / 2;
        if (nx * (tx - mx) + ny * (ty - my) + nz * (tz - mz) < 0) { nx = -nx; ny = -ny; nz = -nz; }
        mb.quad(a, b, c, d, nx, ny, nz, m);
    }

    /**
     * Kassettenkuppel (innen): Kämpferband, Reihen gestufter Kassetten mit Goldrosetten, glatte Kappe.
     * rowsDeg: Reihengrenzen als Höhenwinkel; Maße in Metern.
     */
    public static void cofferedDome(MeshBuilder mb, double cx, double ys, double cz, double R, double a0Deg, double a1Deg,
                                    int cols, double[] rowsDeg, double rib, int mSurf, int mPanel) {
        double A0 = Math.toRadians(a0Deg), A1 = Math.toRadians(a1Deg), da = (A1 - A0) / cols;
        double eLow = Math.toRadians(rowsDeg[0]), eTop = Math.toRadians(rowsDeg[rowsDeg.length - 1]);
        sphPatch(mb, cx, ys, cz, R, A0, A1, 0, eLow, mSurf);
        sphPatch(mb, cx, ys, cz, R, A0, A1, eTop, Math.PI / 2, mSurf);
        double d1 = 0.24, d2 = 0.22, w1 = 0.16, w2 = 0.15, w3 = 0.14;
        double[] p = new double[3];
        for (int i = 0; i + 1 < rowsDeg.length; i++) {
            double e0 = Math.toRadians(rowsDeg[i]), e1 = Math.toRadians(rowsDeg[i + 1]), em = (e0 + e1) / 2;
            for (int j = 0; j < cols; j++) {
                double a0 = A0 + j * da, a1 = a0 + da;
                double rE = (rib / 2) / R, rA = (rib / 2) / (R * Math.cos(em));
                // Rahmen (Rippen) in Radius R
                sphPatch(mb, cx, ys, cz, R, a0, a1, e0, e0 + rE, mSurf);
                sphPatch(mb, cx, ys, cz, R, a0, a1, e1 - rE, e1, mSurf);
                sphPatch(mb, cx, ys, cz, R, a0, a0 + rA, e0 + rE, e1 - rE, mSurf);
                sphPatch(mb, cx, ys, cz, R, a1 - rA, a1, e0 + rE, e1 - rE, mSurf);
                // Stufen: I (R) -> S1 (R+d1) -> Ring -> S2 (R+d1) -> S3 (R+d1+d2)
                double[][] rects = new double[4][];
                double[] rad = {R, R + d1, R + d1, R + d1 + d2};
                double[] inset = {0, w1, w1 + w2, w1 + w2 + w3};
                for (int k = 0; k < 4; k++) {
                    double r = rad[k];
                    double ie = (rib / 2 + inset[k]) / r, ia = (rib / 2 + inset[k]) / (r * Math.cos(em));
                    rects[k] = new double[]{a0 + ia, a1 - ia, e0 + ie, e1 - ie};
                }
                // Schrägen
                bevel(mb, cx, ys, cz, rects[0], rad[0], rects[1], rad[1], mSurf);
                sphRing(mb, cx, ys, cz, rad[1], rects[1], rects[2], mSurf);
                bevel(mb, cx, ys, cz, rects[2], rad[2], rects[3], rad[3], mSurf);
                double[] r3 = rects[3];
                sphPatch(mb, cx, ys, cz, rad[3], r3[0], r3[1], r3[2], r3[3], mPanel);
                // Rosette
                double ac = (r3[0] + r3[1]) / 2, ec = (r3[2] + r3[3]) / 2;
                double size = Math.min((r3[1] - r3[0]) * rad[3] * Math.cos(ec), (r3[3] - r3[2]) * rad[3]) * 0.24;
                rosette(mb, cx, ys, cz, rad[3] - 0.02, ac, ec, size);
            }
        }
    }

    private static void sphRing(MeshBuilder mb, double cx, double ys, double cz, double r, double[] o, double[] in, int m) {
        sphPatch(mb, cx, ys, cz, r, o[0], o[1], o[2], in[2], m);
        sphPatch(mb, cx, ys, cz, r, o[0], o[1], in[3], o[3], m);
        sphPatch(mb, cx, ys, cz, r, o[0], in[0], in[2], in[3], m);
        sphPatch(mb, cx, ys, cz, r, in[1], o[1], in[2], in[3], m);
    }

    private static void bevel(MeshBuilder mb, double cx, double ys, double cz, double[] o, double ro, double[] in, double ri, int m) {
        double[][] co = new double[4][3], ci = new double[4][3];
        double[][] ang = {{o[0], o[2]}, {o[1], o[2]}, {o[1], o[3]}, {o[0], o[3]}};
        double[][] angI = {{in[0], in[2]}, {in[1], in[2]}, {in[1], in[3]}, {in[0], in[3]}};
        for (int k = 0; k < 4; k++) {
            sph(cx, ys, cz, ro, ang[k][0], ang[k][1], co[k]);
            sph(cx, ys, cz, ri, angI[k][0], angI[k][1], ci[k]);
        }
        for (int k = 0; k < 4; k++) {
            int k1 = (k + 1) % 4;
            quadToward(mb, co[k], co[k1], ci[k1], ci[k], cx, ys, cz, m);
        }
    }

    /** Vergoldete Rosette mit acht Blättern auf der Kugelfläche. */
    private static void rosette(MeshBuilder mb, double cx, double ys, double cz, double r, double a, double e, double size) {
        double[] c = new double[3];
        sph(cx, ys, cz, r, a, e, c);
        double nx = -Math.cos(e) * Math.cos(a), ny = -Math.sin(e), nz = -Math.cos(e) * Math.sin(a);
        double t1x = -Math.sin(a), t1y = 0, t1z = Math.cos(a);
        double t2x = -Math.sin(e) * Math.cos(a), t2y = Math.cos(e), t2z = -Math.sin(e) * Math.sin(a);
        double h = size * 0.45;
        mb.patch((u, v, p, n) -> {
            double rr = size * v * (0.78 + 0.22 * Math.cos(8 * u));
            double hh = h * (1 - v * v);
            double cu = Math.cos(u), su = Math.sin(u);
            p[0] = c[0] + (t1x * cu + t2x * su) * rr + nx * hh;
            p[1] = c[1] + (t1y * cu + t2y * su) * rr + ny * hh;
            p[2] = c[2] + (t1z * cu + t2z * su) * rr + nz * hh;
            double k = 2 * v * h / size;
            n[0] = nx + (t1x * cu + t2x * su) * k; n[1] = ny + (t1y * cu + t2y * su) * k; n[2] = nz + (t1z * cu + t2z * su) * k;
        }, 0, 2 * Math.PI, 16, 0, 1, 2, Mat.GOLD);
    }

    /** Gliedmaße: Kegelstumpf zwischen zwei Punkten. */
    public static void limb(MeshBuilder mb, double ax, double ay, double az, double bx, double by, double bz,
                            double ra, double rb, int seg, int m) {
        double dx = bx - ax, dy = by - ay, dz = bz - az, len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= len; dy /= len; dz /= len;
        double hx = Math.abs(dy) < 0.9 ? 0 : 1, hy = Math.abs(dy) < 0.9 ? 1 : 0;
        double e1x = hy * dz, e1y = -hx * dz, e1z = hx * dy - hy * dx;
        double l1 = Math.sqrt(e1x * e1x + e1y * e1y + e1z * e1z);
        e1x /= l1; e1y /= l1; e1z /= l1;
        double e2x = dy * e1z - dz * e1y, e2y = dz * e1x - dx * e1z, e2z = dx * e1y - dy * e1x;
        double slope = (ra - rb) / len;
        final double fdx = dx, fdy = dy, fdz = dz, f1x = e1x, f1y = e1y, f1z = e1z;
        mb.patch((u, v, p, n) -> {
            double r = ra + (rb - ra) * v, c = Math.cos(u), s = Math.sin(u);
            double ox = f1x * c + e2x * s, oy = f1y * c + e2y * s, oz = f1z * c + e2z * s;
            p[0] = ax + fdx * len * v + ox * r; p[1] = ay + fdy * len * v + oy * r; p[2] = az + fdz * len * v + oz * r;
            n[0] = ox + fdx * slope; n[1] = oy + fdy * slope; n[2] = oz + fdz * slope;
        }, 0, 2 * Math.PI, seg, 0, 1, 1, m);
    }
}
