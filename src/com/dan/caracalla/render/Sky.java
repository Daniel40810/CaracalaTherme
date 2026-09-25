package com.dan.caracalla.render;

/** Einfaches analytisches Himmelsmodell mit Sonnenlicht und Umgebungslicht. */
public final class Sky {
    public final double[] sun = {0, 1, 0};
    /** Beleuchtungsstärke der Sonne (linear, RGB). */
    public float sunR, sunG, sunB;
    float zenR, zenG, zenB, horR, horG, horB;
    /** Umgebungslicht für nach oben, seitlich und nach unten gerichtete Flächen. */
    public float upR, upG, upB, sideR, sideG, sideB, downR, downG, downB;
    float day, sunset;

    /** Dunst 0 (klar) bis 1 (dunstig). */
    public float haze;

    public void update(double[] s) { update(s, 0); }

    public void update(double[] s, double hz) {
        haze = (float) hz;
        sun[0] = s[0]; sun[1] = s[1]; sun[2] = s[2];
        double sy = s[1];
        day = (float) smooth(-0.12, 0.12, sy);
        double el = Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, sy))));
        double m = sy > -0.02 ? 1.0 / (Math.max(sy, 0) + 0.15 * Math.pow(Math.max(el, 0) + 3.885, -1.253)) : 40;
        m = Math.min(m, 40);
        double vis = smooth(-0.03, 0.04, sy);
        double hk = 1 + 1.6 * hz;
        double dim = 1 - 0.3 * hz;
        sunR = (float) (3.1 * dim * Math.exp(-m * 0.050 * hk) * vis);
        sunG = (float) (3.1 * dim * Math.exp(-m * 0.115 * hk) * vis);
        sunB = (float) (3.1 * dim * Math.exp(-m * 0.260 * hk) * vis);
        sunset = (float) (Math.exp(-Math.max(el, 0) / 9.0) * day);
        float lowZ = (float) smooth(0.0, 0.5, sy);
        zenR = lerp(0.004f, lerp(0.10f, 0.19f, lowZ), day);
        zenG = lerp(0.006f, lerp(0.20f, 0.36f, lowZ), day);
        zenB = lerp(0.016f, lerp(0.46f, 0.78f, lowZ), day);
        horR = lerp(0.010f, lerp(0.72f, 1.05f, sunset), day);
        horG = lerp(0.012f, lerp(0.80f, 0.62f, sunset), day);
        horB = lerp(0.022f, lerp(0.92f, 0.42f, sunset), day);
        // Dunst: Himmel blasser, Horizont heller und grauer
        float hzf = (float) hz;
        zenR += (horR * 0.8f - zenR) * 0.45f * hzf; zenG += (horG * 0.8f - zenG) * 0.45f * hzf; zenB += (horB * 0.8f - zenB) * 0.45f * hzf;
        float grey = (horR + horG + horB) / 3f * 1.08f;
        horR += (grey - horR) * 0.5f * hzf; horG += (grey - horG) * 0.5f * hzf; horB += (grey - horB) * 0.5f * hzf;
        upR = (zenR * 0.6f + horR * 0.4f) * 1.5f + 0.004f;
        upG = (zenG * 0.6f + horG * 0.4f) * 1.5f + 0.005f;
        upB = (zenB * 0.6f + horB * 0.4f) * 1.5f + 0.008f;
        sideR = horR * 0.75f + zenR * 0.35f + 0.003f;
        sideG = horG * 0.75f + zenG * 0.35f + 0.004f;
        sideB = horB * 0.75f + zenB * 0.35f + 0.006f;
        float gs = (float) Math.max(0, sy) * 0.22f;
        downR = sunR * gs * 0.55f + upR * 0.18f;
        downG = sunG * gs * 0.50f + upG * 0.18f;
        downB = sunB * gs * 0.40f + upB * 0.18f;
    }

    /** Himmelsleuchtdichte in Richtung d (normiert). */
    public void radiance(float dx, float dy, float dz, float[] o) {
        float r, g, b;
        if (dy >= 0) {
            float t = (float) Math.pow(dy, 0.42);
            r = horR + (zenR - horR) * t; g = horG + (zenG - horG) * t; b = horB + (zenB - horB) * t;
        } else {
            float t = Math.min(1, -dy * 4);
            float gr = 0.10f * day + 0.004f, gg = 0.095f * day + 0.004f, gb = 0.085f * day + 0.005f;
            r = horR * 0.8f + (gr - horR * 0.8f) * t; g = horG * 0.8f + (gg - horG * 0.8f) * t; b = horB * 0.8f + (gb - horB * 0.8f) * t;
        }
        float mu = (float) (dx * sun[0] + dy * sun[1] + dz * sun[2]);
        if (mu > 0) {
            float m2 = mu * mu, m4 = m2 * m2, m8 = m4 * m4;
            float m32 = m8 * m8; m32 *= m32;
            float glow = 0.05f * m4 + 0.16f * m8 + 0.45f * m32 * m32;
            float k = 0.25f;
            r += sunR * glow * k; g += sunG * glow * k; b += sunB * glow * k;
            if (mu > 0.99996f) { r += sunR * 60; g += sunG * 60; b += sunB * 60; }
        }
        o[0] = r; o[1] = g; o[2] = b;
    }

    /** Dunstfarbe für die Luftperspektive in Blickrichtung. */
    public void haze(float dx, float dy, float dz, float[] o) {
        radiance(dx, Math.max(0.03f, Math.abs(dy) * 0.3f), dz, o);
        float mu = (float) Math.max(0, dx * sun[0] + dz * sun[2]);
        o[0] = o[0] * 0.9f + sunR * mu * mu * 0.05f;
        o[1] = o[1] * 0.9f + sunG * mu * mu * 0.05f;
        o[2] = o[2] * 0.9f + sunB * mu * mu * 0.05f;
    }

    private static double smooth(double a, double b, double x) {
        double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
}
