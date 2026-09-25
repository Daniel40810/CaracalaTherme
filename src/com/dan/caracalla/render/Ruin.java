package com.dan.caracalla.render;

import com.dan.caracalla.geo.Mat;

/**
 * Der Zeitregler zwischen 216 n. Chr. (0) und heute (1): Gewölbe stürzen ein, die Mauern brechen
 * in unregelmäßiger Höhe ab, Marmor und Putz sind fort und geben das Ziegelmauerwerk frei,
 * Statuen und Säulen verschwinden, die Becken liegen trocken, Gras wächst auf den Böden.
 */
public final class Ruin {
    private Ruin() { }

    // Abbruchhöhe auf einem Gitter von 0,5 m über der Anlage (außerhalb direkt gerechnet)
    private static final float G = 0.5f, GX0 = -185, GZ0 = -170;
    private static final int GW = 740, GH = 820;
    private static volatile float[] grid;

    private static float rawHeight(float x, float z) {
        float n = Noise.fbm(x * 0.026f, 0.37f, z * 0.026f, 3);
        float big = Math.max(0, Math.min(1, (n - 0.34f) / 0.3f));
        // Der Zentralbau steht höher an als Umfassung und Aquädukt
        boolean core = Math.abs(x) < 108 && z > -56 && z < 71;
        float h = (core ? 7 : 3) + (core ? 34 : 14) * big * big;
        h += 4.5f * Noise.value(x * 0.17f, 1.3f, z * 0.17f);
        // abgetreppte Bruchkanten: Lagen von 1,2 m, dazu kleine Ausbrüche
        float step = 1.2f;
        h = (float) Math.floor(h / step) * step + 0.35f * step * Noise.value(x * 0.6f, 2.9f, z * 0.6f);
        h += 0.5f * Noise.value(x * 2.1f, 4.1f, z * 2.1f);
        return h;
    }

    /** Rechnet das Höhengitter vor (einmal beim Laden, nicht aus einem Rechen-Thread heraus). */
    public static synchronized void prepare() {
        if (grid != null) return;
        float[] ng = new float[GW * GH];
        Thread[] ts = new Thread[Math.max(1, Runtime.getRuntime().availableProcessors())];
        for (int k = 0; k < ts.length; k++) {
            final int kk = k;
            ts[k] = new Thread(() -> {
                for (int j = kk; j < GH; j += ts.length)
                    for (int i = 0; i < GW; i++) ng[j * GW + i] = rawHeight(GX0 + i * G, GZ0 + j * G);
            });
            ts[k].start();
        }
        for (Thread t : ts) {
            try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
        grid = ng;
    }

    /** Höhe, in der die Mauern heute abbrechen. */
    public static float height(float x, float z) {
        float fx = (x - GX0) / G, fz = (z - GZ0) / G;
        float[] g = grid;
        if (g == null || fx < 0 || fz < 0 || fx >= GW - 1 || fz >= GH - 1) return rawHeight(x, z);
        int ix = (int) fx, iz = (int) fz;
        float tx = fx - ix, tz = fz - iz;
        int o = iz * GW + ix;
        float a = g[o] + (g[o + 1] - g[o]) * tx, b = g[o + GW] + (g[o + GW + 1] - g[o + GW]) * tx;
        return a + (b - a) * tz;
    }

    /** Abbruchhöhe beim Reglerstand r: bei 216 weit über allem, heute die Ruinenhöhe. */
    public static float clipHeight(float x, float z, float r) {
        float k = smooth((r - 0.12f) / 0.82f);
        if (k <= 0) return 1e9f;
        return height(x, z) + (1 - k) * 70;
    }

    /** Gewölbe und Dächer: stürzen stückweise ein. */
    public static boolean roofGone(float x, float y, float z, float r) {
        if (r < 0.08f) return false;
        float n = Noise.value(x * 0.09f, y * 0.09f, z * 0.09f) * 0.7f + Noise.value(x * 0.4f, y * 0.4f, z * 0.4f) * 0.3f;
        return n < (r - 0.08f) * 2.2f;
    }

    /** Statuen, Säulen, Bronze: werden fortgeschafft (grob nach Ort, so bleiben auch Bruchstücke). */
    public static boolean objectGone(float x, float z, float r) {
        if (r < 0.2f) return false;
        return Noise.value(x * 0.33f, 5.5f, z * 0.33f) < (r - 0.2f) * 2.4f;
    }

    public static boolean isObject(int m) {
        return m == Mat.STATUE || m == Mat.BRONZE || m == Mat.GRANITE || m == Mat.GIALLO || m == Mat.GOLD || m == Mat.PORPHYRY;
    }

    /** Heizung und Wasser laufen nur, solange die Thermen in Betrieb sind. */
    public static float working(float r) { return 1 - smooth((r - 0.08f) / 0.3f); }

    static float smooth(float t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }

    /** Verwitterung der Grundfarbe; n ist die (gegebenenfalls verformte) Normale. */
    public static void decay(int m, float x, float y, float z, float[] n, float[] o, float r) {
        if (r < 0.01f) return;
        float[] T3 = TMP.get();
        if (m == Mat.GROUND) {
            // der Garten ist heute Rasen und Weg
            float g = Noise.fbm(x * 0.05f, 3.3f, z * 0.05f, 3);
            float k = r * Math.max(0, Math.min(1, (g - 0.35f) * 3));
            grass(x, z, T3);
            mix(o, T3, k);
            return;
        }
        if (Mat.natural(m) || m == Mat.EMBER) return;
        float ny = n[1];
        boolean wallish = ny < 0.6f && ny > -0.6f;
        float lost = Noise.fbm(x * 0.21f, y * 0.21f, z * 0.21f, 2) * 0.8f + Noise.value(x * 1.7f, y * 1.7f, z * 1.7f) * 0.2f;
        boolean bare = lost < r * 0.82f - 0.02f;
        switch (m) {
            case Mat.MARBLE: case Mat.STUCCO: case Mat.PLASTER: case Mat.CORNICE: case Mat.TILE: case Mat.ROOFFLAT:
                if (bare) Materials.brick(x, y, z, n[0], n[1], n[2], o);
                break;
            case Mat.VAULT: case Mat.COFFER:
                if (bare) Materials.concrete(x, y, z, o);
                break;
            default:
        }
        if (ny > 0.6f && y < 1.5f) {
            // Böden: Mosaik bleibt in Resten, dazwischen Erde und Gras
            float g = Noise.fbm(x * 0.13f, 7.1f, z * 0.13f, 3) * 0.75f + Noise.value(x * 0.8f, 1.1f, z * 0.8f) * 0.25f;
            float cover = Math.max(0, Math.min(1, (r * 0.95f - g) * 6));
            if (cover > 0) {
                grass(x, z, T3);
                mix(o, T3, cover);
            }
        }
        // Schmutz, Regenspuren an den Wänden
        float grime = 1 - 0.28f * r * Noise.fbm(x * 0.4f, y * 0.4f, z * 0.4f, 2);
        if (wallish) {
            float a = Math.abs(n[0]) > Math.abs(n[2]) ? z : x;
            float streak = Noise.value(a * 2.3f, y * 0.06f, 0.5f);
            grime *= 1 - 0.3f * r * Math.max(0, streak - 0.45f) * 2;
        }
        o[0] *= grime; o[1] *= grime; o[2] *= grime;
    }

    private static final ThreadLocal<float[]> TMP = ThreadLocal.withInitial(() -> new float[3]);

    /** Gras und Erde: Farbe nach Ort. */
    public static void grass(float x, float z, float[] o) {
        float n = Noise.fbm(x * 0.35f, 0.9f, z * 0.35f, 3), f = Noise.value(x * 3.1f, 0.2f, z * 3.1f);
        if (n < 0.42f) { o[0] = 0.20f; o[1] = 0.15f; o[2] = 0.10f; }
        else { o[0] = 0.10f + 0.06f * f; o[1] = 0.15f + 0.08f * f; o[2] = 0.05f + 0.02f * f; }
        float k = 0.8f + 0.4f * Noise.value(x * 0.7f, 4.4f, z * 0.7f);
        o[0] *= k; o[1] *= k; o[2] *= k;
    }

    private static void mix(float[] o, float[] t, float k) {
        o[0] += (t[0] - o[0]) * k; o[1] += (t[1] - o[1]) * k; o[2] += (t[2] - o[2]) * k;
    }
}
