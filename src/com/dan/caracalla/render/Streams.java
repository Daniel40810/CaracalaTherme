package com.dan.caracalla.render;

import com.dan.caracalla.model.Route;

import java.util.List;
import java.util.Random;

/**
 * Teilchen, die festen Wegen folgen: das Wasser vom Aquädukt bis in die Becken oder die
 * heiße Luft vom Feuer bis unter die Kuppel. Jedes Teilchen wählt beim Start einen Weg
 * (nach Gewicht) und läuft ihn mit leicht eigener Geschwindigkeit ab.
 */
public final class Streams {
    public final List<Route> routes;
    public final int n;
    public final int[] route;
    public final float[] s, speed, jx, jy, jz;
    private final double[] cumW;
    private final Random rnd = new Random(77);
    private final float baseSpeed, jitter;
    private float time;

    public Streams(List<Route> routes, int count, float baseSpeed, float jitter) {
        this.routes = routes;
        this.n = routes.isEmpty() ? 0 : count;
        this.baseSpeed = baseSpeed;
        this.jitter = jitter;
        route = new int[n];
        s = new float[n]; speed = new float[n]; jx = new float[n]; jy = new float[n]; jz = new float[n];
        cumW = new double[routes.size()];
        double acc = 0;
        for (int i = 0; i < routes.size(); i++) { acc += routes.get(i).weight * routes.get(i).len; cumW[i] = acc; }
        for (int i = 0; i < n; i++) {
            spawn(i);
            s[i] = rnd.nextFloat() * routes.get(route[i]).len;
        }
    }

    private void spawn(int i) {
        double u = rnd.nextDouble() * cumW[cumW.length - 1];
        int k = 0;
        while (k < cumW.length - 1 && cumW[k] < u) k++;
        route[i] = k;
        s[i] = 0;
        speed[i] = baseSpeed * (0.8f + 0.4f * rnd.nextFloat());
        jx[i] = (float) rnd.nextGaussian() * jitter;
        jy[i] = (float) rnd.nextGaussian() * jitter * 0.6f;
        jz[i] = (float) rnd.nextGaussian() * jitter;
    }

    public void step(double dt) {
        time += (float) dt;
        for (int i = 0; i < n; i++) {
            s[i] += speed[i] * (float) dt;
            if (s[i] >= routes.get(route[i]).len) spawn(i);
        }
    }

    public float time() { return time; }

    /** Ort des Teilchens i mit seiner kleinen Abweichung vom Weg; Rückgabe: Anteil des Wegs 0..1. */
    public float position(int i, float[] out) {
        Route r = routes.get(route[i]);
        r.at(s[i], out);
        float wob = (float) Math.sin(time * 2.1f + i * 1.7f);
        out[0] += jx[i] * (1 + 0.3f * wob);
        out[1] += jy[i];
        out[2] += jz[i] * (1 - 0.3f * wob);
        return s[i] / r.len;
    }
}
