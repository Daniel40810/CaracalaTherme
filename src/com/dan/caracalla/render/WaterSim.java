package com.dan.caracalla.render;

import com.dan.caracalla.model.Fountain;
import com.dan.caracalla.model.WaterBody;

import java.util.List;
import java.util.Random;

/**
 * Wasser in Bewegung: je Becken ein Höhenfeld nach der Wellengleichung (Wellen laufen, werden an den
 * Rändern zurückgeworfen und klingen ab) und die Tropfen der Wasserspeier als Teilchen. Wo ein Tropfen
 * aufschlägt, entsteht eine Welle und manchmal ein Spritzer.
 */
public final class WaterSim {
    static final float DX = 0.2f;
    private static final float C = 1.15f, DAMP = 0.996f, STEP = 1f / 60f;

    public final List<WaterBody> bodies;
    private final List<Fountain> fountains;
    final Grid[] grids;
    private float acc;
    private final Random rnd = new Random(79);
    private float simTime;

    // Tropfen
    public static final int MAXP = 6000;
    public final float[] px = new float[MAXP], py = new float[MAXP], pz = new float[MAXP];
    final float[] pvx = new float[MAXP], pvy = new float[MAXP], pvz = new float[MAXP];
    public final float[] psize = new float[MAXP];
    final boolean[] spray = new boolean[MAXP];
    public int np;
    private final float[] emitAcc;

    static final class Grid {
        final WaterBody b;
        final int nx, nz;
        final float x0, z0;
        float[] h, hp, hn;
        final boolean[] wet;

        Grid(WaterBody b) {
            this.b = b;
            double[] bb = b.bounds();
            x0 = (float) bb[0]; z0 = (float) bb[1];
            nx = Math.max(3, (int) Math.ceil((bb[2] - bb[0]) / DX) + 1);
            nz = Math.max(3, (int) Math.ceil((bb[3] - bb[1]) / DX) + 1);
            h = new float[nx * nz]; hp = new float[nx * nz]; hn = new float[nx * nz];
            wet = new boolean[nx * nz];
            for (int j = 0; j < nz; j++) for (int i = 0; i < nx; i++) wet[j * nx + i] = b.contains(x0 + i * DX, z0 + j * DX);
        }
    }

    public WaterSim(List<WaterBody> bodies, List<Fountain> fountains) {
        this.bodies = bodies;
        this.fountains = fountains;
        grids = new Grid[bodies.size()];
        for (int i = 0; i < grids.length; i++) grids[i] = new Grid(bodies.get(i));
        emitAcc = new float[fountains.size()];
    }

    /** Index des Beckens unter (x,z) auf Höhe y, sonst -1. */
    public int bodyAt(double x, double y, double z) {
        for (int i = 0; i < grids.length; i++) {
            WaterBody b = grids[i].b;
            if (Math.abs(y - b.waterY) < 0.15 && b.contains(x, z)) return i;
        }
        return -1;
    }

    public WaterBody body(int i) { return grids[i].b; }

    public void warmup(double seconds) {
        for (double t = 0; t < seconds; t += 0.05) step(0.05);
    }

    public void step(double dt) {
        acc += (float) Math.min(dt, 0.1);
        while (acc >= STEP) {
            acc -= STEP;
            simTime += STEP;
            particles(STEP);
            for (Grid g : grids) wave(g);
            // leichte Brise und fallende Tropfen von oben halten die Natatio lebendig
            if (rnd.nextFloat() < 0.08f) {
                Grid g = grids[0];
                drop(0, g.x0 + rnd.nextFloat() * (g.nx - 1) * DX, g.z0 + rnd.nextFloat() * (g.nz - 1) * DX, -0.004f);
            }
        }
    }

    private void wave(Grid g) {
        int nx = g.nx, nz = g.nz;
        float k = (C * STEP / DX) * (C * STEP / DX);
        float[] h = g.h, hp = g.hp, hn = g.hn;
        boolean[] wet = g.wet;
        for (int j = 0; j < nz; j++) {
            for (int i = 0; i < nx; i++) {
                int o = j * nx + i;
                if (!wet[o]) { hn[o] = 0; continue; }
                float c = h[o];
                float l = (i > 0 && wet[o - 1]) ? h[o - 1] : c;
                float r = (i < nx - 1 && wet[o + 1]) ? h[o + 1] : c;
                float d = (j > 0 && wet[o - nx]) ? h[o - nx] : c;
                float u = (j < nz - 1 && wet[o + nx]) ? h[o + nx] : c;
                hn[o] = (2 * c - hp[o] + k * (l + r + d + u - 4 * c)) * DAMP;
            }
        }
        g.hp = h; g.h = hn; g.hn = hp;
    }

    /** Stößt eine Welle an (amp in Metern, negativ = Mulde). */
    public void drop(int bi, float x, float z, float amp) {
        Grid g = grids[bi];
        int ci = Math.round((x - g.x0) / DX), cj = Math.round((z - g.z0) / DX);
        for (int j = cj - 2; j <= cj + 2; j++) {
            for (int i = ci - 2; i <= ci + 2; i++) {
                if (i < 0 || j < 0 || i >= g.nx || j >= g.nz) continue;
                int o = j * g.nx + i;
                if (!g.wet[o]) continue;
                float d2 = (i - ci) * (i - ci) + (j - cj) * (j - cj);
                g.h[o] += amp * (float) Math.exp(-d2 / 1.5);
            }
        }
    }

    /** Stein ins Wasser: an welchem Becken auch immer der Punkt liegt. */
    public boolean splash(double x, double y, double z) {
        int b = bodyAt(x, y, z);
        if (b < 0) return false;
        drop(b, (float) x, (float) z, -0.09f);
        for (int i = 0; i < 14; i++) {
            double a = rnd.nextDouble() * Math.PI * 2, s = 0.6 + rnd.nextDouble() * 1.2;
            add((float) x, (float) grids[b].b.waterY + 0.02f, (float) z, (float) (Math.cos(a) * s), 1.5f + rnd.nextFloat() * 2f,
                    (float) (Math.sin(a) * s), true);
        }
        return true;
    }

    /** Neigung des Wasserspiegels (dh/dx, dh/dz) und Krümmung (für die Lichtnetze). */
    public void slope(int bi, float x, float z, float[] out) {
        Grid g = grids[bi];
        float fx = (x - g.x0) / DX, fz = (z - g.z0) / DX;
        int i = (int) Math.floor(fx), j = (int) Math.floor(fz);
        if (i < 1 || j < 1 || i >= g.nx - 2 || j >= g.nz - 2) { out[0] = 0; out[1] = 0; out[2] = 0; return; }
        float tx = fx - i, tz = fz - j;
        float[] h = g.h;
        int nx = g.nx;
        float gx0 = (h[j * nx + i + 1] - h[j * nx + i - 1]), gx1 = (h[j * nx + i + 2] - h[j * nx + i]);
        float gx2 = (h[(j + 1) * nx + i + 1] - h[(j + 1) * nx + i - 1]), gx3 = (h[(j + 1) * nx + i + 2] - h[(j + 1) * nx + i]);
        float gz0 = (h[(j + 1) * nx + i] - h[(j - 1) * nx + i]), gz1 = (h[(j + 1) * nx + i + 1] - h[(j - 1) * nx + i + 1]);
        float gz2 = (h[(j + 2) * nx + i] - h[j * nx + i]), gz3 = (h[(j + 2) * nx + i + 1] - h[j * nx + i + 1]);
        out[0] = ((gx0 * (1 - tx) + gx1 * tx) * (1 - tz) + (gx2 * (1 - tx) + gx3 * tx) * tz) / (2 * DX);
        out[1] = ((gz0 * (1 - tx) + gz1 * tx) * (1 - tz) + (gz2 * (1 - tx) + gz3 * tx) * tz) / (2 * DX);
        out[2] = (lap(h, nx, i, j) * (1 - tx) + lap(h, nx, i + 1, j) * tx) * (1 - tz)
                + (lap(h, nx, i, j + 1) * (1 - tx) + lap(h, nx, i + 1, j + 1) * tx) * tz;
    }

    private static float lap(float[] h, int nx, int i, int j) {
        int o = j * nx + i;
        return (h[o - 1] + h[o + 1] + h[o - nx] + h[o + nx] - 4 * h[o]) / (DX * DX);
    }

    // ------------------------------------------------------------ Tropfen

    private void add(float x, float y, float z, float vx, float vy, float vz, boolean isSpray) {
        if (np >= MAXP) return;
        px[np] = x; py[np] = y; pz[np] = z; pvx[np] = vx; pvy[np] = vy; pvz[np] = vz;
        psize[np] = isSpray ? 0.012f + rnd.nextFloat() * 0.01f : 0.022f + rnd.nextFloat() * 0.012f;
        spray[np] = isSpray;
        np++;
    }

    private void particles(float dt) {
        for (int f = 0; f < fountains.size(); f++) {
            Fountain fo = fountains.get(f);
            emitAcc[f] += (float) fo.rate * dt;
            float pulse = 1 + 0.04f * (float) Math.sin(simTime * 7 + f);
            while (emitAcc[f] >= 1) {
                emitAcc[f] -= 1;
                float s = (float) fo.spread;
                add((float) fo.x, (float) fo.y, (float) fo.z,
                        (float) fo.vx * pulse + (float) rnd.nextGaussian() * s,
                        (float) fo.vy * pulse + (float) rnd.nextGaussian() * s,
                        (float) fo.vz * pulse + (float) rnd.nextGaussian() * s, false);
            }
        }
        int w = 0;
        for (int i = 0; i < np; i++) {
            float ox = px[i], oy = py[i], oz = pz[i];
            pvy[i] -= 9.81f * dt;
            px[i] += pvx[i] * dt; py[i] += pvy[i] * dt; pz[i] += pvz[i] * dt;
            boolean dead = py[i] < -2;
            if (pvy[i] < 0) {
                int b = -1;
                for (int k = 0; k < grids.length; k++) {
                    WaterBody wb = grids[k].b;
                    if (oy >= wb.waterY && py[i] < wb.waterY && wb.contains(px[i], pz[i])) { b = k; break; }
                }
                if (b >= 0) {
                    dead = true;
                    if (!spray[i]) {
                        drop(b, px[i], pz[i], -0.006f);
                        if (rnd.nextFloat() < 0.25f && np + 2 < MAXP) {
                            float wy = (float) grids[b].b.waterY + 0.01f;
                            add(px[i], wy, pz[i], (float) rnd.nextGaussian() * 0.35f, 0.5f + rnd.nextFloat() * 0.7f,
                                    (float) rnd.nextGaussian() * 0.35f, true);
                        }
                    } else {
                        drop(b, px[i], pz[i], -0.0015f);
                    }
                } else if (py[i] < 0.02f && oy >= 0.02f && !inAnyBody(px[i], pz[i])) {
                    dead = true;
                }
            }
            if (!dead) {
                px[w] = px[i]; py[w] = py[i]; pz[w] = pz[i]; pvx[w] = pvx[i]; pvy[w] = pvy[i]; pvz[w] = pvz[i];
                psize[w] = psize[i]; spray[w] = spray[i];
                w++;
            }
        }
        np = w;
    }

    private boolean inAnyBody(float x, float z) {
        for (Grid g : grids) if (g.b.contains(x, z)) return true;
        return false;
    }
}
