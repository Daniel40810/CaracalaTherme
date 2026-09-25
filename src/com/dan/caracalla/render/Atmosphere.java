package com.dan.caracalla.render;

/**
 * Dichte der streuenden Luft je Meter: etwas Dunst überall, dazu Dampf in den warmen Sälen.
 * In Phase 5 kommt der Dampf aus einem Partikelfeld; bis dahin ein ruhig wogendes Rauschfeld.
 */
public final class Atmosphere {
    private Atmosphere() { }

    /** Dunst der freien Luft je Meter. */
    public static float air(float haze) { return 0.00045f + 0.0008f * haze; }

    /** Ersatz ohne Dampfteilchen: ruhig wogendes Rauschfeld. */
    public static float density(float x, float y, float z, float t, float haze) {
        float d = air(haze);
        float ax = Math.abs(x);
        // Caldarium: dichter Dampf, unter der Kuppel gesammelt
        float dz = z - 50;
        float r2 = x * x + dz * dz;
        if (r2 < 17.5f * 17.5f && y < 45 && y > 0) {
            float n = Noise.value(x * 0.13f + t * 0.05f, y * 0.1f - t * 0.11f, z * 0.13f - t * 0.03f);
            float m = Noise.value(x * 0.37f - t * 0.09f, y * 0.3f - t * 0.2f, z * 0.37f);
            float rise = 0.6f + 0.4f * Math.min(1, y / 30f);
            d += 0.013f * rise * (0.35f + 0.65f * n) * (0.7f + 0.3f * m);
        } else if (ax < 10 && z > 20 && z < 32.5f && y < 22) {
            float n = Noise.value(x * 0.15f + t * 0.04f, y * 0.12f - t * 0.1f, z * 0.15f);
            d += 0.008f * (0.4f + 0.6f * n);
        } else if (ax < 29 && Math.abs(z) < 12 && y < 33) {
            d += 0.0016f;
        } else if (ax < 32 && z > -52 && z < -27 && y < 4) {
            // leichter Dunst über der Natatio
            d += 0.004f * (1 - y / 4f) * Noise.value(x * 0.1f + t * 0.03f, y, z * 0.1f);
        }
        return d;
    }
}
