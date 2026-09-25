package com.dan.caracalla.render;

import com.dan.caracalla.geo.Mesh;

/**
 * Alles, was vom Sonnenstand abhängt: Himmel, zwei Schattenkarten und der Lichtrückwurf je Ecke.
 * Zwei solcher Objekte wechseln sich ab, damit ein Hintergrund-Thread das nächste berechnen kann,
 * während das aktuelle gezeichnet wird.
 */
public final class Lighting {
    public final Sky sky = new Sky();
    public final ShadowMap fine, wide;
    public final float[] bounce;
    public final double[] sun = new double[3];
    public double haze;
    public boolean ready;
    /** Zeitregler (0 = 216, 1 = heute), mit dem die Schatten berechnet werden. */
    public float ruin;

    public Lighting(int shadowSize, int nv) {
        fine = new ShadowMap(shadowSize);
        wide = new ShadowMap(Math.max(1024, shadowSize / 2));
        bounce = new float[3 * nv];
    }

    public void compute(Mesh m, double[] dir, double hz) {
        sun[0] = dir[0]; sun[1] = dir[1]; sun[2] = dir[2];
        haze = hz;
        sky.update(dir, hz);
        if (dir[1] > -0.02) {
            fine.render(m, dir, 0, 14, 8, 122, ruin);
            wide.render(m, dir, 0, 10, 14, 270, ruin);
        } else {
            fine.valid = false;
            wide.valid = false;
        }
        Bvh.bounce(m, this, bounce);
        ready = true;
    }

    public float lit(double x, double y, double z, double slope) {
        if (fine.covers(x, y, z)) return fine.lit(x, y, z, slope);
        return wide.lit(x, y, z, slope);
    }

    public float litHard(double x, double y, double z) {
        if (fine.covers(x, y, z)) return fine.litHard(x, y, z);
        return wide.litHard(x, y, z);
    }
}
