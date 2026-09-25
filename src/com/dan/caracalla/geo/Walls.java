package com.dan.caracalla.geo;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Mauern mit Öffnungen: Fenster, Türen, Bögen und Nischen, gerade oder gekrümmt.
 * Eine Mauer liegt auf einem Pfad in der Grundrissebene. u läuft entlang des Pfads,
 * v ist die Höhe, w der Abstand von der Mittellinie (positiv = Vorderseite).
 */
public final class Walls {

    private Walls() { }

    // ------------------------------------------------------------------ Pfade

    public interface Path {
        double length();
        void base(double u, double[] o);
        void normal(double u, double[] o);
        void tangent(double u, double[] o);
        boolean curved();
    }

    /** Gerade von A nach B. Die Vorderseite liegt links der Laufrichtung: n = (-tz, 0, tx). */
    public static final class Line implements Path {
        final double ax, az, tx, tz, len;

        public Line(double ax, double az, double bx, double bz) {
            this.ax = ax; this.az = az;
            double dx = bx - ax, dz = bz - az;
            len = Math.hypot(dx, dz);
            tx = dx / len; tz = dz / len;
        }

        /** u-Position des Punkts (px,pz), projiziert auf die Gerade. */
        public double at(double px, double pz) { return (px - ax) * tx + (pz - az) * tz; }

        @Override public double length() { return len; }
        @Override public void base(double u, double[] o) { o[0] = ax + tx * u; o[1] = 0; o[2] = az + tz * u; }
        @Override public void normal(double u, double[] o) { o[0] = -tz; o[1] = 0; o[2] = tx; }
        @Override public void tangent(double u, double[] o) { o[0] = tx; o[1] = 0; o[2] = tz; }
        @Override public boolean curved() { return false; }
    }

    /** Kreisbogen um (cx,cz), Winkel in Grad; Punkt = (cx + r cos a, cz + r sin a). */
    public static final class Arc implements Path {
        final double cx, cz, r, a0, a1;
        final boolean inward;

        public Arc(double cx, double cz, double r, double a0Deg, double a1Deg, boolean inward) {
            this.cx = cx; this.cz = cz; this.r = r;
            this.a0 = Math.toRadians(a0Deg); this.a1 = Math.toRadians(a1Deg);
            this.inward = inward;
        }

        /** u-Position zum Winkel in Grad (wird in den Bogenbereich gefaltet). */
        public double atDeg(double deg) {
            double a = Math.toRadians(deg);
            double span = 2 * Math.PI;
            while (a < a0) a += span;
            while (a > a0 + span) a -= span;
            return (a - a0) * r;
        }

        @Override public double length() { return r * (a1 - a0); }
        @Override public void base(double u, double[] o) {
            double a = a0 + u / r;
            o[0] = cx + r * Math.cos(a); o[1] = 0; o[2] = cz + r * Math.sin(a);
        }
        @Override public void normal(double u, double[] o) {
            double a = a0 + u / r, s = inward ? -1 : 1;
            o[0] = s * Math.cos(a); o[1] = 0; o[2] = s * Math.sin(a);
        }
        @Override public void tangent(double u, double[] o) {
            double a = a0 + u / r;
            o[0] = -Math.sin(a); o[1] = 0; o[2] = Math.cos(a);
        }
        @Override public boolean curved() { return true; }
    }

    // --------------------------------------------------------------- Öffnungen

    public static final class Opening {
        public double u0, u1, v0, v1;
        public boolean arch;
        /** Tiefe einer Nische; negativ = durchgehende Öffnung. */
        public double depth = -1;
        /** Material für Laibung und Nischenrückwand, -1 = wie Mauer. */
        public int mat = -1;

        public static Opening rect(double uc, double w, double v0, double v1) {
            Opening o = new Opening();
            o.u0 = uc - w / 2; o.u1 = uc + w / 2; o.v0 = v0; o.v1 = v1;
            return o;
        }

        public static Opening arch(double uc, double w, double v0, double v1) {
            Opening o = rect(uc, w, v0, v1);
            o.arch = true;
            return o;
        }

        public static Opening niche(double uc, double w, double v0, double v1, double depth, int mat) {
            Opening o = arch(uc, w, v0, v1);
            o.depth = depth; o.mat = mat;
            return o;
        }

        boolean through() { return depth < 0; }
    }

    public static final class Wall {
        public final Path path;
        public final double t, vb, vt;
        public int mFront, mBack, mReveal, mTop;
        public boolean capTop = true;
        public final List<Opening> ops = new ArrayList<>();

        public Wall(Path path, double t, double vb, double vt, int mFront, int mBack) {
            this.path = path; this.t = t; this.vb = vb; this.vt = vt;
            this.mFront = mFront; this.mBack = mBack; this.mReveal = mFront; this.mTop = mBack;
        }

        public Wall add(Opening o) { ops.add(o); return this; }
    }

    // ------------------------------------------------------------------ Aufbau

    private static int segsU(MeshBuilder mb, Path p, double len) {
        if (p.curved()) {
            double keep = mb.maxEdge;
            int n = Math.max(mb.segs(len), (int) Math.ceil(len / 1.2));
            mb.maxEdge = keep;
            return n;
        }
        return mb.segs(len);
    }

    private static Opening find(Wall w, double u, double v) {
        for (Opening o : w.ops) {
            if (u > o.u0 && u < o.u1 && v > o.v0 && v < o.v1) return o;
        }
        return null;
    }

    private static void map(Path p, double u, double v, double w, double[] out, double[] tmp) {
        p.base(u, out);
        p.normal(u, tmp);
        out[0] += tmp[0] * w; out[2] += tmp[2] * w; out[1] = v;
    }

    /** Senkrechte Fläche im Abstand wOff von der Mittellinie. */
    static void face(MeshBuilder mb, Path p, double ua, double ub, double va, double vb, double wOff, double sign, int m) {
        if (ub - ua < 1e-6 || vb - va < 1e-6) return;
        mb.patch((u, v, o, n) -> {
            p.base(u, o); p.normal(u, n);
            o[0] += n[0] * wOff; o[2] += n[2] * wOff; o[1] = v;
            n[0] *= sign; n[1] = 0; n[2] *= sign;
        }, ua, ub, segsU(mb, p, ub - ua), va, vb, mb.segs(vb - va), m);
    }

    /** Laibung quer zur Mauer bei u = U; Normale = Tangente·sign. */
    static void jamb(MeshBuilder mb, Path p, double U, double wA, double wB, double va, double vb, double sign, int m) {
        if (vb - va < 1e-6 || wB - wA < 1e-6) return;
        double[] b = new double[3], nn = new double[3], tt = new double[3];
        p.base(U, b); p.normal(U, nn); p.tangent(U, tt);
        mb.patch((w, v, o, n) -> {
            o[0] = b[0] + nn[0] * w; o[1] = v; o[2] = b[2] + nn[2] * w;
            n[0] = tt[0] * sign; n[1] = 0; n[2] = tt[2] * sign;
        }, wA, wB, mb.segs(wB - wA), va, vb, mb.segs(vb - va), m);
    }

    /** Waagrechte Fläche (Sohlbank, Sturz, Mauerkrone) bei Höhe V. */
    static void horiz(MeshBuilder mb, Path p, double ua, double ub, double wA, double wB, double V, double sign, int m) {
        if (ub - ua < 1e-6 || wB - wA < 1e-6) return;
        mb.patch((u, w, o, n) -> {
            p.base(u, o); p.normal(u, n);
            o[0] += n[0] * w; o[2] += n[2] * w; o[1] = V;
            n[0] = 0; n[1] = sign; n[2] = 0;
        }, ua, ub, segsU(mb, p, ub - ua), wA, wB, mb.segs(wB - wA), m);
    }

    /** Bogenleibung eines Rundbogens (Halbkreis). */
    static void soffit(MeshBuilder mb, Path p, double uc, double r, double vs, double wA, double wB, int m) {
        double[] tt = new double[3];
        mb.patch((th, w, o, n) -> {
            double uu = uc + r * Math.cos(th), vv = vs + r * Math.sin(th);
            p.base(uu, o); p.normal(uu, n);
            o[0] += n[0] * w; o[2] += n[2] * w; o[1] = vv;
            p.tangent(uu, tt);
            double c = Math.cos(th), s = Math.sin(th);
            n[0] = -tt[0] * c; n[1] = -s; n[2] = -tt[2] * c;
        }, 0, Math.PI, 16, wA, wB, mb.segs(wB - wA), m);
    }

    /** Zwickel oberhalb des Bogens bis zur Oberkante vTop, als Streifen zwischen Bogen und Oberkante. */
    static void spandrels(MeshBuilder mb, Path p, double u0, double u1, double uc, double r, double vs, double vTop,
                          double wOff, double sign, int m) {
        if (vTop <= vs + 1e-6) return;
        int N = 8;
        double[] q = new double[3], tmp = new double[3], nn = new double[3];
        for (int side = 0; side < 2; side++) {
            int pa = -1, pt = -1;
            for (int k = 0; k <= N; k++) {
                double th = side == 0 ? Math.PI - (Math.PI / 2) * k / N : (Math.PI / 2) - (Math.PI / 2) * k / N;
                double uu = uc + r * Math.cos(th), vv = Math.min(vTop, vs + r * Math.sin(th));
                int ia = vtx(mb, p, uu, vv, wOff, sign, q, tmp, nn);
                int it = vtx(mb, p, uu, vTop, wOff, sign, q, tmp, nn);
                if (pa >= 0) { mb.tri(pa, ia, it, m); mb.tri(pa, it, pt, m); }
                pa = ia; pt = it;
            }
        }
    }

    /** Halbkreisscheibe (Rückwand einer Bogennische) über dem Kämpfer. */
    static void halfDisc(MeshBuilder mb, Path p, double uc, double r, double vs, double wOff, double sign, int m) {
        int N = 12;
        double[] q = new double[3], tmp = new double[3], nn = new double[3];
        map(p, uc, vs, wOff, q, tmp);
        p.normal(uc, nn);
        int ic = mb.v(q[0], q[1], q[2], nn[0] * sign, 0, nn[2] * sign);
        int prev = -1;
        for (int k = 0; k <= N; k++) {
            double th = Math.PI * k / N;
            double uu = uc + r * Math.cos(th), vv = vs + r * Math.sin(th);
            map(p, uu, vv, wOff, q, tmp);
            p.normal(uu, nn);
            int iv = mb.v(q[0], q[1], q[2], nn[0] * sign, 0, nn[2] * sign);
            if (prev >= 0) mb.tri(ic, prev, iv, m);
            prev = iv;
        }
    }

    /** Baut eine Mauer samt Öffnungen. */
    public static void build(MeshBuilder mb, Wall w) {
        Path p = w.path;
        double L = p.length(), h = w.t / 2;
        TreeSet<Double> us = new TreeSet<>(), vs = new TreeSet<>();
        us.add(0.0); us.add(L); vs.add(w.vb); vs.add(w.vt);
        for (Opening o : w.ops) {
            us.add(clamp(o.u0, 0, L)); us.add(clamp(o.u1, 0, L));
            vs.add(clamp(o.v0, w.vb, w.vt)); vs.add(clamp(o.v1, w.vb, w.vt));
        }
        Double[] U = us.toArray(new Double[0]), V = vs.toArray(new Double[0]);
        for (int i = 0; i + 1 < U.length; i++) {
            for (int j = 0; j + 1 < V.length; j++) {
                double ua = U[i], ub = U[i + 1], va = V[j], vb = V[j + 1];
                if (ub - ua < 1e-6 || vb - va < 1e-6) continue;
                Opening o = find(w, (ua + ub) / 2, (va + vb) / 2);
                if (o == null) {
                    face(mb, p, ua, ub, va, vb, +h, +1, w.mFront);
                    face(mb, p, ua, ub, va, vb, -h, -1, w.mBack);
                } else if (!o.through()) {
                    face(mb, p, ua, ub, va, vb, -h, -1, w.mBack);
                }
            }
        }
        for (Opening o : w.ops) opening(mb, w, o);
        if (w.capTop) {
            for (int i = 0; i + 1 < U.length; i++) {
                double ua = U[i], ub = U[i + 1];
                if (find(w, (ua + ub) / 2, w.vt - 1e-4) == null) horiz(mb, p, ua, ub, -h, h, w.vt, 1, w.mTop);
            }
        }
    }

    private static void opening(MeshBuilder mb, Wall w, Opening o) {
        Path p = w.path;
        double h = w.t / 2;
        int mr = o.mat >= 0 ? o.mat : w.mReveal;
        double wA = o.through() ? -h : h - o.depth, wB = h;
        double u0 = Math.max(0, o.u0), u1 = Math.min(p.length(), o.u1);
        double v0 = Math.max(o.v0, w.vb), v1 = Math.min(o.v1, w.vt);
        double r = (u1 - u0) / 2, uc = (u0 + u1) / 2;
        double vs = o.arch ? Math.min(v1, o.v1 - r) : v1;
        if (o.u0 >= 0) jamb(mb, p, u0, wA, wB, v0, vs, +1, mr);
        if (o.u1 <= p.length()) jamb(mb, p, u1, wA, wB, v0, vs, -1, mr);
        if (v0 > w.vb + 1e-6) horiz(mb, p, u0, u1, wA, wB, v0, +1, mr);
        if (!o.arch && v1 < w.vt - 1e-6) horiz(mb, p, u0, u1, wA, wB, v1, -1, mr);
        if (o.arch) {
            soffit(mb, p, uc, r, vs, wA, wB, mr);
            spandrels(mb, p, u0, u1, uc, r, vs, v1, +h, +1, w.mFront);
            if (o.through()) spandrels(mb, p, u0, u1, uc, r, vs, v1, -h, -1, w.mBack);
        }
        if (!o.through()) {
            face(mb, p, u0, u1, v0, vs, wA, +1, mr);
            if (o.arch) halfDisc(mb, p, uc, r, vs, wA, +1, mr);
        }
    }

    /**
     * Lünette: halbelliptisches Mauerfeld über der Kämpferhöhe vb, außen (ru, rv),
     * optional mit Thermenfenster (wu, wv) und Pfosten.
     */
    public static void lunette(MeshBuilder mb, Path p, double uc, double vb, double ru, double rv, double t,
                               double wu, double wv, int mullions, int mFront, int mBack, int mReveal, int mMull) {
        double h = t / 2;
        int N = 28;
        double[] q = new double[3], tmp = new double[3], nn = new double[3];
        for (int side = 0; side < 2; side++) {
            double wOff = side == 0 ? h : -h, sign = side == 0 ? 1 : -1;
            int m = side == 0 ? mFront : mBack;
            for (int k = 0; k < N; k++) {
                double ta = Math.PI * k / N, tb = Math.PI * (k + 1) / N;
                int oa = vtx(mb, p, uc + ru * Math.cos(ta), vb + rv * Math.sin(ta), wOff, sign, q, tmp, nn);
                int ob = vtx(mb, p, uc + ru * Math.cos(tb), vb + rv * Math.sin(tb), wOff, sign, q, tmp, nn);
                if (wu > 0) {
                    int ia = vtx(mb, p, uc + wu * Math.cos(ta), vb + wv * Math.sin(ta), wOff, sign, q, tmp, nn);
                    int ib = vtx(mb, p, uc + wu * Math.cos(tb), vb + wv * Math.sin(tb), wOff, sign, q, tmp, nn);
                    mb.tri(oa, ob, ib, m);
                    mb.tri(oa, ib, ia, m);
                } else {
                    int ic = vtx(mb, p, uc, vb, wOff, sign, q, tmp, nn);
                    mb.tri(oa, ob, ic, m);
                }
            }
        }
        if (wu > 0) {
            double[] tt = new double[3];
            mb.patch((th, w, o, n) -> {
                double c = Math.cos(th), s = Math.sin(th);
                double uu = uc + wu * c, vv = vb + wv * s;
                p.base(uu, o); p.normal(uu, n);
                o[0] += n[0] * w; o[2] += n[2] * w; o[1] = vv;
                p.tangent(uu, tt);
                double gu = c / wu, gv = s / wv, l = Math.hypot(gu, gv);
                gu /= l; gv /= l;
                n[0] = -tt[0] * gu; n[1] = -gv; n[2] = -tt[2] * gu;
            }, 0, Math.PI, N, -h, h, mb.segs(t), mReveal);
            horiz(mb, p, uc - wu, uc + wu, -h, h, vb, 1, mReveal);
            for (int m = 1; m <= mullions; m++) {
                double um = uc - wu + 2 * wu * m / (mullions + 1);
                double du = (um - uc) / wu;
                double top = vb + wv * Math.sqrt(Math.max(0, 1 - du * du));
                boxLocal(mb, p, um - 0.28, um + 0.28, vb, top, -h * 0.5, h * 0.5, mMull);
            }
        }
    }

    private static int vtx(MeshBuilder mb, Path p, double u, double v, double w, double sign,
                           double[] q, double[] tmp, double[] nn) {
        map(p, u, v, w, q, tmp);
        p.normal(u, nn);
        return mb.v(q[0], q[1], q[2], nn[0] * sign, 0, nn[2] * sign);
    }

    /** Quader in Mauerkoordinaten (für Pfosten und Gesimse auf Geraden). */
    public static void boxLocal(MeshBuilder mb, Path p, double uA, double uB, double vA, double vB,
                                double wA, double wB, int m) {
        face(mb, p, uA, uB, vA, vB, wB, +1, m);
        face(mb, p, uA, uB, vA, vB, wA, -1, m);
        jamb(mb, p, uA, wA, wB, vA, vB, -1, m);
        jamb(mb, p, uB, wA, wB, vA, vB, +1, m);
        horiz(mb, p, uA, uB, wA, wB, vB, +1, m);
    }

    private static double clamp(double x, double a, double b) { return Math.max(a, Math.min(b, x)); }
}
