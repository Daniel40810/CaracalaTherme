package com.dan.caracalla.render;

import com.dan.caracalla.model.WaterBody;

import java.util.List;
import java.util.Random;

/**
 * Dampf und Nebel als Teilchen. Warmes Wasser gibt Dampf ab (Caldarium-Wannen stark, Labrum mittel,
 * Becken am Tepidarium schwach, Frigidarium gar nicht); über der Natatio liegt morgens und abends Nebel.
 * Die Teilchen steigen mit abnehmendem Auftrieb, werden von einem wirbelfreien Strömungsfeld verweht,
 * gleiten unter der Kuppel entlang, sammeln sich oben und ziehen durch die oberen Fenster ab.
 * Aus den Teilchen entsteht ein Dichtegitter, das die Lichtstrahlen speist.
 */
public final class SteamSim {
    public static final int MAXP = 26000;
    public final float[] x = new float[MAXP], y = new float[MAXP], z = new float[MAXP];
    final float[] vx = new float[MAXP], vy = new float[MAXP], vz = new float[MAXP];
    public final float[] age = new float[MAXP], life = new float[MAXP], heat = new float[MAXP];
    final boolean[] outside = new boolean[MAXP];
    public int n;

    private final List<WaterBody> bodies;
    private final float[] bodyHeat, emitAcc;
    private final Random rnd = new Random(5);
    private float acc, t;
    private volatile double hour = 15.5, haze = 0;
    private static final float STEP = 1f / 20f;

    // Kuppel des Caldariums
    private static final float CX = 0, CZ = 50, CR = 17.5f, CYS = 27;

    /** Dichtegitter: Hitzeräume und Natatio. */
    final Field hot = new Field(-32, -0.5f, 18, 32, 46, 69, 0.8f);
    final Field pool = new Field(-30, -0.5f, -53, 30, 6, -26, 0.8f);

    static final class Field {
        final float x0, y0, z0, c;
        final int nx, ny, nz;
        float[] d, tmp;

        Field(float x0, float y0, float z0, float x1, float y1, float z1, float c) {
            this.x0 = x0; this.y0 = y0; this.z0 = z0; this.c = c;
            nx = (int) Math.ceil((x1 - x0) / c) + 1; ny = (int) Math.ceil((y1 - y0) / c) + 1; nz = (int) Math.ceil((z1 - z0) / c) + 1;
            d = new float[nx * ny * nz]; tmp = new float[nx * ny * nz];
        }

        void splat(float px, float py, float pz, float m) {
            float fx = (px - x0) / c, fy = (py - y0) / c, fz = (pz - z0) / c;
            int i = (int) Math.floor(fx), j = (int) Math.floor(fy), k = (int) Math.floor(fz);
            if (i < 0 || j < 0 || k < 0 || i >= nx - 1 || j >= ny - 1 || k >= nz - 1) return;
            float tx = fx - i, ty = fy - j, tz = fz - k;
            for (int q = 0; q < 8; q++) {
                int dx = q & 1, dy = (q >> 1) & 1, dz = (q >> 2) & 1;
                float w = (dx == 1 ? tx : 1 - tx) * (dy == 1 ? ty : 1 - ty) * (dz == 1 ? tz : 1 - tz);
                d[((k + dz) * ny + (j + dy)) * nx + (i + dx)] += m * w;
            }
        }

        float at(float px, float py, float pz) {
            float fx = (px - x0) / c, fy = (py - y0) / c, fz = (pz - z0) / c;
            int i = (int) Math.floor(fx), j = (int) Math.floor(fy), k = (int) Math.floor(fz);
            if (i < 0 || j < 0 || k < 0 || i >= nx - 1 || j >= ny - 1 || k >= nz - 1) return 0;
            float tx = fx - i, ty = fy - j, tz = fz - k;
            int o = (k * ny + j) * nx + i, sy = nx, sz = nx * ny;
            float a = d[o] + (d[o + 1] - d[o]) * tx, b = d[o + sy] + (d[o + sy + 1] - d[o + sy]) * tx;
            float c2 = d[o + sz] + (d[o + sz + 1] - d[o + sz]) * tx, e = d[o + sz + sy] + (d[o + sz + sy + 1] - d[o + sz + sy]) * tx;
            float ab = a + (b - a) * ty, ce = c2 + (e - c2) * ty;
            return ab + (ce - ab) * tz;
        }

        /** Weichzeichnen: dreimal 1-2-1 entlang jeder Achse. */
        void blur() {
            int[] str = {1, nx, nx * ny};
            int[] len = {nx, ny, nz};
            for (int ax = 0; ax < 3; ax++) {
                int s = str[ax];
                float[] a = d, b = tmp;
                for (int o = 0; o < a.length; o++) {
                    int coord = (o / s) % len[ax];
                    float l = coord > 0 ? a[o - s] : a[o], r = coord < len[ax] - 1 ? a[o + s] : a[o];
                    b[o] = 0.25f * l + 0.5f * a[o] + 0.25f * r;
                }
                d = b; tmp = a;
            }
        }
    }

    public SteamSim(List<WaterBody> bodies) {
        this.bodies = bodies;
        bodyHeat = new float[bodies.size()];
        emitAcc = new float[bodies.size()];
        for (int i = 0; i < bodies.size(); i++) {
            String nm = bodies.get(i).name;
            bodyHeat[i] = nm.startsWith("Caldarium") ? 1.0f : nm.startsWith("Labrum") ? 0.75f
                    : nm.startsWith("Tepidarium") ? 0.4f : nm.startsWith("Natatio") ? -1f : 0f;
        }
    }

    public void setHour(double h) { hour = h; }
    public void setHaze(double h) { haze = h; }

    /** Nebelstärke über der Natatio: morgens und abends, bei Dunst mehr. */
    float mist() {
        double h = hour;
        double m = Math.max(0, Math.min(1, (9.5 - h) / 3)) + Math.max(0, Math.min(1, (h - 18) / 2.5));
        return (float) Math.min(1, m + 0.35 * haze);
    }

    public void warmup(double seconds) {
        for (double s = 0; s < seconds; s += STEP) {
            t += STEP;
            emit(STEP);
            move(STEP);
        }
        rebuild();
    }

    public void step(double dt) {
        acc += (float) Math.min(dt, 0.2);
        boolean any = false;
        while (acc >= STEP) {
            acc -= STEP;
            t += STEP;
            emit(STEP);
            move(STEP);
            any = true;
        }
        if (any) rebuild();
    }

    private void emit(float dt) {
        float mist = mist();
        for (int b = 0; b < bodies.size(); b++) {
            float hh = bodyHeat[b];
            if (hh < 0) hh = 0.22f * mist;
            if (hh <= 0) continue;
            WaterBody wb = bodies.get(b);
            double[] bb = wb.bounds();
            double area = (bb[2] - bb[0]) * (bb[3] - bb[1]);
            emitAcc[b] += (float) (area * 1.1 * hh * dt);
            int guard = 0;
            while (emitAcc[b] >= 1 && n < MAXP && guard++ < 400) {
                double px = bb[0] + rnd.nextDouble() * (bb[2] - bb[0]), pz = bb[1] + rnd.nextDouble() * (bb[3] - bb[1]);
                if (!wb.contains(px, pz)) continue;
                emitAcc[b] -= 1;
                int i = n++;
                x[i] = (float) px; y[i] = (float) wb.waterY + 0.05f; z[i] = (float) pz;
                vx[i] = 0; vy[i] = 0.2f * hh; vz[i] = 0;
                age[i] = 0;
                boolean natatio = bodyHeat[b] < 0;
                life[i] = natatio ? 9 + rnd.nextFloat() * 6 : 55 + rnd.nextFloat() * 25;
                heat[i] = natatio ? -0.3f : hh;
                outside[i] = natatio;
            }
            if (emitAcc[b] > 50) emitAcc[b] = 0;
        }
    }

    private void move(float dt) {
        int w = 0;
        for (int i = 0; i < n; i++) {
            age[i] += dt * (outside[i] && heat[i] > 0 ? 4 : 1);
            if (age[i] > life[i]) continue;
            float px = x[i], py = y[i], pz = z[i];
            float h = heat[i];
            // Auftrieb nimmt mit der Abkühlung ab; Nebel schwebt
            float buoy = h > 0 ? h * (0.7f + 0.5f * (float) Math.exp(-age[i] / 10f)) + 0.05f : 0.02f * (1.6f - py);
            // wirbelfreies Strömungsfeld aus Sinuswellen (zwei Stufen)
            float tt = t;
            float ux = (float) (Math.sin(py * 0.9 + tt * 0.35) * Math.cos(pz * 0.7 - tt * 0.2) + 0.5 * Math.sin(py * 2.3 - tt * 0.8 + 1.3) * Math.cos(pz * 1.9 + 0.7));
            float uy = (float) (Math.sin(pz * 0.8 + tt * 0.3 + 2.1) * Math.cos(px * 0.75 + tt * 0.25) + 0.5 * Math.sin(pz * 2.1 + tt * 0.7) * Math.cos(px * 2.2 - 1.1));
            float uz = (float) (Math.sin(px * 0.85 - tt * 0.28 + 0.4) * Math.cos(py * 0.8 + tt * 0.33) + 0.5 * Math.sin(px * 2.4 + tt * 0.9) * Math.cos(py * 2.0 + 2.2));
            float turb = h > 0 ? 0.55f : 0.2f;
            float tvx = ux * turb, tvy = uy * turb * 0.6f + buoy, tvz = uz * turb;
            // Nebel draußen: leichter Wind
            if (h < 0) { tvx += 0.25f; tvz += 0.08f; }
            // Abzug durch die oberen Fenster des Caldariums
            float dx = px - CX, dz = pz - CZ;
            float rr = (float) Math.sqrt(dx * dx + dz * dz);
            // Umwälzung im Caldarium: über den Wannen steigt es an der Wand hoch, oben strömt es zur Mitte
            if (!outside[i] && rr < CR + 1 && pz > 30 && rr > 0.5f) {
                float inward = py > 12 ? Math.min(0.35f, (py - 12) * 0.03f) : -0.04f;
                tvx -= dx / rr * inward; tvz -= dz / rr * inward;
            }
            vx[i] += (tvx - vx[i]) * 0.3f; vy[i] += (tvy - vy[i]) * 0.3f; vz[i] += (tvz - vz[i]) * 0.3f;
            px += vx[i] * dt; py += vy[i] * dt; pz += vz[i] * dt;
            if (!outside[i]) {
                if (!confine(i, px, py, pz)) continue;
                px = cx; py = cy; pz = cz;
            }
            if (outside[i] && h > 0 && py > 60) continue;
            x[w] = px; y[w] = py; z[w] = pz; vx[w] = vx[i]; vy[w] = vy[i]; vz[w] = vz[i];
            age[w] = age[i]; life[w] = life[i]; heat[w] = heat[i]; outside[w] = outside[i];
            w++;
        }
        n = w;
    }

    private float cx, cy, cz;

    /** Hält Teilchen in ihrem Raum; Fensteröffnungen lassen sie hinaus. */
    private boolean confine(int i, float px, float py, float pz) {
        cx = px; cy = py; cz = pz;
        float dx = px - CX, dz = pz - CZ;
        float rr = (float) Math.sqrt(dx * dx + dz * dz);
        boolean inCal = rr < CR + 0.5f && pz > 30;
        if (inCal) {
            if (py > CYS) {
                // unter der Kuppel entlanggleiten
                float dy = py - CYS;
                float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (d > CR - 0.6f) {
                    float k = (CR - 0.6f) / d;
                    cx = CX + dx * k; cy = CYS + dy * k; cz = CZ + dz * k;
                    vx[i] *= 0.5f; vz[i] *= 0.5f;
                    vy[i] = Math.max(vy[i], 0.05f);
                }
            } else if (rr > CR - 0.4f) {
                if (window(dx, dz, rr, py)) {
                    outside[i] = true;
                } else {
                    float k = (CR - 0.4f) / rr;
                    cx = CX + dx * k; cz = CZ + dz * k;
                }
            }
            if (cy < 0.2f) cy = 0.2f;
            return true;
        }
        // Tepidarium und Nebenräume mit Becken
        float ax = Math.abs(px);
        if (ax < 10.5f && pz > 19.5f && pz < 33) {
            float top = 12 + (float) Math.sqrt(Math.max(0, 100 - px * px)) - 0.5f;
            if (cy > top) cy = top;
            return true;
        }
        if (ax > 11.5f && ax < 31.5f && pz > 19.5f && pz < 34) {
            if (cy > 15.0f) cy = 15.0f;
            if (Math.abs(cx) > 31.2f) cx = Math.signum(cx) * 31.2f;
            if (Math.abs(cx) < 12.2f) cx = Math.signum(cx) * 12.2f;
            if (cz < 20.2f) cz = 20.2f;
            return true;
        }
        // durch den Durchgang ins Caldarium oder in den Gang: weiter, aber schneller auflösen
        outside[i] = true;
        return true;
    }

    /** Liegt der Punkt am Rand des Caldariums in einer Fensteröffnung? */
    private static boolean window(float dx, float dz, float rr, float py) {
        double a = Math.toDegrees(Math.atan2(dz, dx));
        for (int k = 0; k < 8; k++) {
            double c = -90 + 45 * k;
            double da = Math.abs(((a - c) % 360 + 540) % 360 - 180);
            if (py > 18.6 && py < 25.6 && da < 8.5) return true;
            if (k >= 3 && k <= 5 && py > 4 && py < 17 && da < 10.5) return true;
        }
        return false;
    }

    /** Dichtegitter aus den Teilchen neu aufbauen. */
    private void rebuild() {
        java.util.Arrays.fill(hot.d, 0);
        java.util.Arrays.fill(pool.d, 0);
        for (int i = 0; i < n; i++) {
            float f = age[i] / life[i];
            float fade = Math.min(1, age[i] / 3.5f) * (1 - f * f);
            if (heat[i] < 0) {
                pool.splat(x[i], y[i], z[i], 0.07f * Math.min(1, age[i] * 1.5f) * (1 - f * f));
            } else {
                float m = 0.045f * fade * (outside[i] ? 0.5f : 1f);
                hot.splat(x[i], y[i], z[i], m);
            }
        }
        hot.blur();
        pool.blur();
    }

    /** Dampfdichte je Meter am Punkt (ohne Luftdunst). */
    public float density(float px, float py, float pz) {
        if (pz > 17) return hot.at(px, py, pz);
        if (pz < -25 && py < 6) return pool.at(px, py, pz);
        return 0;
    }

    /** Wärme für das Flimmern: dicht über heißem Wasser am stärksten. */
    public float heatAt(float px, float py, float pz) {
        for (int b = 0; b < bodies.size(); b++) {
            float hh = bodyHeat[b];
            if (hh <= 0.3f) continue;
            WaterBody wb = bodies.get(b);
            float dy = (float) (py - wb.waterY);
            if (dy < 0 || dy > 2.5f) continue;
            if (!wb.contains(px, pz)) continue;
            return hh * (1 - dy / 2.5f);
        }
        return 0;
    }
}
