package com.dan.caracalla.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Weg, dem Teilchen folgen: Wasser vom Aquädukt zu einem Becken oder heiße Luft vom
 * Praefurnium durch das Hypokaustum und die Wandröhren nach oben.
 */
public final class Route {
    public static final int COLD = 0, WARM = 1, HOT = 2, AIR = 3;

    public final String name;
    public final float[] p;      // x y z je Punkt
    public final float[] cum;    // Weglänge bis zu jedem Punkt
    public final float len;
    public final double weight;
    public final int kind;
    /** Weglänge, ab der das Wasser im Kessel erwärmt ist (-1 = nie). */
    public final float heatAt;

    private Route(String name, List<double[]> pts, double weight, int kind, double heatAt) {
        this.name = name;
        int n = pts.size();
        p = new float[3 * n];
        cum = new float[n];
        float acc = 0;
        for (int i = 0; i < n; i++) {
            double[] q = pts.get(i);
            p[3 * i] = (float) q[0]; p[3 * i + 1] = (float) q[1]; p[3 * i + 2] = (float) q[2];
            if (i > 0) {
                double[] r = pts.get(i - 1);
                acc += (float) Math.sqrt((q[0] - r[0]) * (q[0] - r[0]) + (q[1] - r[1]) * (q[1] - r[1]) + (q[2] - r[2]) * (q[2] - r[2]));
            }
            cum[i] = acc;
        }
        len = acc;
        this.weight = weight;
        this.kind = kind;
        this.heatAt = (float) heatAt;
    }

    /** Punkt bei Weglänge s (auf den Weg begrenzt). */
    public void at(float s, float[] out) {
        int n = cum.length;
        if (s <= 0) { out[0] = p[0]; out[1] = p[1]; out[2] = p[2]; return; }
        if (s >= len) { out[0] = p[3 * n - 3]; out[1] = p[3 * n - 2]; out[2] = p[3 * n - 1]; return; }
        int lo = 0, hi = n - 1;
        while (hi - lo > 1) {
            int mid = (lo + hi) >>> 1;
            if (cum[mid] <= s) lo = mid; else hi = mid;
        }
        float u = (s - cum[lo]) / Math.max(1e-6f, cum[hi] - cum[lo]);
        for (int k = 0; k < 3; k++) out[k] = p[3 * lo + k] + (p[3 * hi + k] - p[3 * lo + k]) * u;
    }

    /** Baut einen Weg Punkt für Punkt. */
    public static final class Builder {
        private final String name;
        private final List<double[]> pts = new ArrayList<>();
        private double heatAt = -1;

        public Builder(String name) { this.name = name; }

        public Builder to(double x, double y, double z) { pts.add(new double[]{x, y, z}); return this; }

        /** Ab dem zuletzt gesetzten Punkt ist das Wasser warm. */
        public Builder heatHere() {
            double acc = 0;
            for (int i = 1; i < pts.size(); i++) {
                double[] a = pts.get(i - 1), b = pts.get(i);
                acc += Math.sqrt((a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]) + (a[2] - b[2]) * (a[2] - b[2]));
            }
            heatAt = acc;
            return this;
        }

        public Builder copy(String newName) {
            Builder b = new Builder(newName);
            for (double[] q : pts) b.pts.add(q.clone());
            b.heatAt = heatAt;
            return b;
        }

        public Route build(double weight, int kind) { return new Route(name, pts, weight, kind, heatAt); }
    }
}
