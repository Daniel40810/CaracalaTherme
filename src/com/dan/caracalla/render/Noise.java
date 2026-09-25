package com.dan.caracalla.render;

/** Schnelles Wert-Rauschen für prozedurale Materialien. */
public final class Noise {
    private Noise() { }

    public static int hash(int x, int y, int z) {
        int h = x * 374761393 + y * 668265263 + z * 1274126177;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }

    /** Zufallswert 0..1 je Gitterzelle. */
    public static float cell(int x, int y, int z) {
        return (hash(x, y, z) & 0xFFFFFF) / 16777215f;
    }

    private static int fl(float v) { int i = (int) v; return v < i ? i - 1 : i; }

    /** Glattes Rauschen 0..1. */
    public static float value(float x, float y, float z) {
        int xi = fl(x), yi = fl(y), zi = fl(z);
        float fx = x - xi, fy = y - yi, fz = z - zi;
        fx = fx * fx * (3 - 2 * fx); fy = fy * fy * (3 - 2 * fy); fz = fz * fz * (3 - 2 * fz);
        float a = cell(xi, yi, zi), b = cell(xi + 1, yi, zi), c = cell(xi, yi + 1, zi), d = cell(xi + 1, yi + 1, zi);
        float e = cell(xi, yi, zi + 1), f = cell(xi + 1, yi, zi + 1), g = cell(xi, yi + 1, zi + 1), h = cell(xi + 1, yi + 1, zi + 1);
        float ab = a + (b - a) * fx, cd = c + (d - c) * fx, ef = e + (f - e) * fx, gh = g + (h - g) * fx;
        float abcd = ab + (cd - ab) * fy, efgh = ef + (gh - ef) * fy;
        return abcd + (efgh - abcd) * fz;
    }

    /** Fraktales Rauschen 0..1. */
    public static float fbm(float x, float y, float z, int oct) {
        float s = 0, a = 0.5f, n = 0;
        for (int i = 0; i < oct; i++) {
            s += a * value(x, y, z);
            n += a;
            x = x * 2.03f + 17.1f; y = y * 2.03f + 3.7f; z = z * 2.03f + 9.2f;
            a *= 0.5f;
        }
        return s / n;
    }
}
