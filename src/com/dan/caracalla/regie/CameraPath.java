package com.dan.caracalla.regie;

import com.dan.caracalla.render.Camera;

import java.util.ArrayList;
import java.util.List;

/** Kamerafahrt aus Schlüsselbildern: Augpunkt und Blickziel, weich verbunden (Catmull-Rom). */
public final class CameraPath {
    private final List<double[]> keys = new ArrayList<>(); // t, ex, ey, ez, lx, ly, lz

    public CameraPath key(double t, double ex, double ey, double ez, double lx, double ly, double lz) {
        keys.add(new double[]{t, ex, ey, ez, lx, ly, lz});
        return this;
    }

    public double duration() { return keys.isEmpty() ? 0 : keys.get(keys.size() - 1)[0]; }

    public int size() { return keys.size(); }

    public double[] last() { return keys.get(keys.size() - 1); }

    /** Kopie der Schlüsselbilder: je Eintrag t, ex, ey, ez, lx, ly, lz. */
    public List<double[]> keys() {
        List<double[]> l = new ArrayList<>();
        for (double[] k : keys) l.add(k.clone());
        return l;
    }

    /** Setzt die Kamera auf den Stand zur Zeit t. */
    public void eval(double t, Camera cam) {
        double[] o = new double[6];
        eval(t, o);
        cam.ex = o[0]; cam.ey = o[1]; cam.ez = o[2];
        double dx = o[3] - o[0], dz = o[5] - o[2];
        if (dx * dx + dz * dz < 1e-6) { o[3] += 0.01; }
        cam.lookAt(o[3], o[4], o[5]);
    }

    public void eval(double t, double[] out) {
        int n = keys.size();
        if (n == 1 || t <= keys.get(0)[0]) { copy(keys.get(0), out); return; }
        if (t >= keys.get(n - 1)[0]) { copy(keys.get(n - 1), out); return; }
        int i = 0;
        while (i < n - 2 && keys.get(i + 1)[0] <= t) i++;
        double[] p1 = keys.get(i), p2 = keys.get(i + 1);
        double[] p0 = i > 0 ? keys.get(i - 1) : p1, p3 = i + 2 < n ? keys.get(i + 2) : p2;
        double u = (t - p1[0]) / (p2[0] - p1[0]);
        for (int k = 1; k <= 6; k++) {
            // Catmull-Rom mit Tangenten nach Zeitabständen (keine Überschwinger bei ungleichen Abständen)
            double dt = p2[0] - p1[0];
            double m1 = p0 == p1 ? (p2[k] - p1[k]) : (p2[k] - p0[k]) / (p2[0] - p0[0]) * dt;
            double m2 = p3 == p2 ? (p2[k] - p1[k]) : (p3[k] - p1[k]) / (p3[0] - p1[0]) * dt;
            double u2 = u * u, u3 = u2 * u;
            out[k - 1] = (2 * u3 - 3 * u2 + 1) * p1[k] + (u3 - 2 * u2 + u) * m1 + (-2 * u3 + 3 * u2) * p2[k] + (u3 - u2) * m2;
        }
    }

    private static void copy(double[] k, double[] out) { System.arraycopy(k, 1, out, 0, 6); }
}
