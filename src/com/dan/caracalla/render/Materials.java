package com.dan.caracalla.render;

import com.dan.caracalla.geo.Mat;

/**
 * Prozedurale Materialien: Grundfarbe (linear) aus Weltposition und Normale, dazu
 * Glanzstärke und Glanzschärfe. Gewölbe und Decken bekommen ein Kassettenrelief über
 * eine verformte Normale. Keine Bilddateien.
 */
public final class Materials {
    public static final float[] SPEC = new float[Mat.COUNT];
    public static final float[] SHIN = new float[Mat.COUNT];

    static {
        set(Mat.MARBLE, 0.07f, 90); set(Mat.FLOOR, 0.09f, 140); set(Mat.GIALLO, 0.07f, 90);
        set(Mat.PORPHYRY, 0.06f, 90); set(Mat.BRONZE, 0.35f, 30); set(Mat.GRANITE, 0.08f, 90);
        set(Mat.POOL, 0.05f, 60); set(Mat.CORNICE, 0.04f, 60); set(Mat.PAVING, 0.015f, 20);
        set(Mat.TILE, 0.02f, 16); set(Mat.STATUE, 0.05f, 40); set(Mat.GOLD, 0.55f, 45);
        set(Mat.COFFER, 0.01f, 10);
    }

    private static void set(int m, float s, float sh) { SPEC[m] = s; SHIN[m] = sh; }

    /** Mittlere Grundfarbe je Material (für den Lichtrückwurf), aus Stichproben gemittelt. */
    public static final float[][] AVG = new float[Mat.COUNT][3];

    static {
        java.util.Random r = new java.util.Random(7);
        float[] o = new float[3], n = new float[3];
        for (int m = 0; m < Mat.COUNT; m++) {
            float ar = 0, ag = 0, ab = 0;
            int N = 400;
            for (int i = 0; i < N; i++) {
                float x = (float) (r.nextDouble() * 200 - 100), y = (float) (r.nextDouble() * 20), z = (float) (r.nextDouble() * 100 - 50);
                boolean up = (i & 1) == 0;
                n[0] = up ? 0 : 1; n[1] = up ? 1 : 0; n[2] = 0;
                if (m == Mat.VAULT) { n[1] = -1; n[0] = 0; y = 25; }
                surface(m, x, y, z, n, o);
                ar += o[0]; ag += o[1]; ab += o[2];
            }
            AVG[m][0] = ar / N; AVG[m][1] = ag / N; AVG[m][2] = ab / N;
        }
        AVG[Mat.WATER][0] = 0.03f; AVG[Mat.WATER][1] = 0.06f; AVG[Mat.WATER][2] = 0.07f;
    }

    private Materials() { }

    /** Steincodes wie in CAR_STONE; die Materialfunktionen vermerken sie in o[3], wenn o länger als 3 ist. */
    public static final String[] STONE_CODES = {"PAVONAZZETTO", "GIALLO_ANTICO", "VERDE_ANTICO", "PORFIDO_ROSSO", "GRANITO_GRIGIO",
            "MARMO_LUNENSE", "MARMO_PROCONNESIO", "SERPENTINO", "TRAVERTINO"};

    private static void tag(float[] o, int s) { if (o.length > 3) o[3] = s; }

    /** Welcher Stein liegt an diesem Punkt? null, wenn die Materialfunktion keinen vermerkt. */
    public static String stoneAt(int m, float x, float y, float z, float nx, float ny, float nz) {
        float[] o = {0, 0, 0, -1};
        float[] n = {nx, ny, nz};
        surface(m, x, y, z, n, o);
        return o[3] < 0 ? null : STONE_CODES[(int) o[3]];
    }

    private static float frac(float v) { return v - (float) Math.floor(v); }

    private static void rgb(float[] o, float r, float g, float b) { o[0] = r; o[1] = g; o[2] = b; }

    private static void mul(float[] o, float k) { o[0] *= k; o[1] *= k; o[2] *= k; }

    /** Marmor-Maserung: 0 = Grund, 1 = Ader. */
    private static float vein(float x, float y, float z, float scale, float sharp) {
        float t = Noise.fbm(x * scale, y * scale, z * scale, 4);
        float s = (float) Math.abs(Math.sin((x * 0.6f + y * 0.35f + z * 0.5f) * scale * 2.2f + t * 9f));
        return (float) Math.pow(1 - s, sharp);
    }

    // ------------------------------------------------------------- Marmorsorten

    static void pavonazzetto(float x, float y, float z, float[] o) {
        tag(o, 0);
        float v = vein(x, y, z, 1.0f, 6), w = vein(z + 5, x, y - 3, 2.6f, 10);
        rgb(o, 0.83f, 0.81f, 0.78f);
        o[0] -= 0.30f * v + 0.08f * w; o[1] -= 0.40f * v + 0.1f * w; o[2] -= 0.26f * v + 0.07f * w;
    }

    static void giallo(float x, float y, float z, float[] o) {
        tag(o, 1);
        float v = vein(x + 3, y, z, 1.2f, 5), n = Noise.value(x * 2, y * 2, z * 2);
        rgb(o, 0.72f, 0.51f, 0.22f);
        mul(o, 0.9f + 0.18f * n);
        o[0] -= 0.22f * v; o[1] -= 0.18f * v; o[2] -= 0.08f * v;
    }

    static void verde(float x, float y, float z, float[] o) {
        tag(o, 2);
        float v = vein(x, y * 2, z, 1.6f, 4);
        float f = Noise.cell((int) Math.floor(x * 14), (int) Math.floor(y * 14), (int) Math.floor(z * 14));
        rgb(o, 0.06f, 0.13f, 0.085f);
        if (f > 0.82f) rgb(o, 0.15f, 0.25f, 0.17f);
        o[0] += 0.22f * v; o[1] += 0.25f * v; o[2] += 0.21f * v;
    }

    static void porphyry(float x, float y, float z, float[] o) {
        tag(o, 3);
        float f = Noise.cell((int) Math.floor(x * 70), (int) Math.floor(y * 70), (int) Math.floor(z * 70));
        rgb(o, 0.28f, 0.065f, 0.08f);
        mul(o, 0.9f + 0.2f * Noise.value(x * 9, y * 9, z * 9));
        if (f > 0.9f) rgb(o, 0.52f, 0.38f, 0.36f);
    }

    static void whiteMarble(float x, float y, float z, float[] o) {
        tag(o, 5);
        float v = vein(x, y, z, 0.55f, 9), w = vein(z + 13, y * 0.8f, x - 7, 1.4f, 14);
        rgb(o, 0.80f, 0.78f, 0.74f);
        o[0] -= 0.16f * v + 0.07f * w; o[1] -= 0.16f * v + 0.07f * w; o[2] -= 0.11f * v + 0.04f * w;
    }

    // ------------------------------------------------------------- Einstieg

    /**
     * Füllt die Grundfarbe und darf die Normale n (Welt, normiert) für Reliefs verändern.
     * Rückgabe: Verdeckungsfaktor 0..1 für das Umgebungslicht.
     */
    public static float surface(int m, float x, float y, float z, float[] n, float[] o) {
        if (m == Mat.VAULT) return vault(x, y, z, n, o);
        albedo(m, x, y, z, n[0], n[1], n[2], o);
        return 1;
    }

    public static void albedo(int m, float x, float y, float z, float nx, float ny, float nz, float[] o) {
        switch (m) {
            case Mat.GROUND: ground(x, z, o); break;
            case Mat.LAND: {
                float n = Noise.fbm(x * 0.004f, 0, z * 0.004f, 4), f = Noise.value(x * 0.08f, 0.5f, z * 0.08f);
                rgb(o, 0.12f + 0.08f * n, 0.14f + 0.05f * n, 0.07f + 0.02f * n);
                mul(o, 0.82f + 0.3f * f);
                break;
            }
            case Mat.PAVING: travertine(x, y, z, nx, ny, nz, o); break;
            case Mat.STUCCO: stucco(x, y, z, nx, nz, o); break;
            case Mat.PLASTER: {
                float n = Noise.fbm(x * 0.1f, y * 0.1f, z * 0.1f, 3);
                rgb(o, 0.58f, 0.54f, 0.48f);
                mul(o, 0.88f + 0.16f * n);
                break;
            }
            case Mat.MARBLE: marbleWall(x, y, z, nx, ny, nz, o); break;
            case Mat.CORNICE: {
                whiteMarble(x, y, z, o);
                mul(o, 1.03f);
                break;
            }
            case Mat.FLOOR: floor(x, y, z, ny, o); break;
            case Mat.GRANITE: {
                tag(o, 4);
                float f = Noise.cell((int) Math.floor(x * 24), (int) Math.floor(y * 24), (int) Math.floor(z * 24));
                float g = Noise.cell((int) Math.floor(x * 9), (int) Math.floor(y * 9), (int) Math.floor(z * 9) + 5);
                float n = Noise.value(x * 1.5f, y * 1.5f, z * 1.5f);
                float k = f < 0.2f ? 0.3f : (f > 0.9f ? 2.1f : 1f);
                rgb(o, 0.25f, 0.25f, 0.27f);
                if (g > 0.9f) rgb(o, 0.33f, 0.27f, 0.27f);
                mul(o, k * (0.85f + 0.3f * n));
                break;
            }
            case Mat.TILE: {
                float a = Math.abs(nx) > Math.abs(nz) ? z : x, b = y * 1.3f + (Math.abs(nx) > Math.abs(nz) ? x : z);
                float row = (float) Math.floor(b / 0.42f);
                float t = Noise.cell((int) Math.floor(a / 0.5f + row * 0.5f), (int) row, 3);
                float weather = Noise.fbm(x * 0.2f, y * 0.2f, z * 0.2f, 3);
                rgb(o, 0.44f, 0.20f, 0.10f);
                mul(o, (0.72f + 0.45f * t) * (0.85f + 0.3f * weather));
                if (frac(b / 0.42f) < 0.14f) mul(o, 0.55f);
                if (frac(a / 0.5f + row * 0.5f) < 0.06f) mul(o, 0.7f);
                break;
            }
            case Mat.ROOFFLAT: {
                float f = Noise.cell((int) Math.floor(x * 15), 0, (int) Math.floor(z * 15));
                float n = Noise.fbm(x * 0.2f, 0, z * 0.2f, 3);
                rgb(o, 0.42f, 0.29f, 0.23f);
                mul(o, (0.82f + 0.3f * n) * (f > 0.85f ? 1.3f : 1f));
                break;
            }
            case Mat.POOL: {
                tag(o, 6);
                float v = vein(x, y, z, 0.8f, 5);
                rgb(o, 0.62f, 0.70f, 0.66f);
                o[0] -= 0.12f * v; o[1] -= 0.1f * v; o[2] -= 0.1f * v;
                break;
            }
            case Mat.BRONZE: {
                float n = Noise.fbm(x * 3, y * 3, z * 3, 3);
                rgb(o, 0.30f, 0.21f, 0.10f);
                // grünliche Patina in Mulden
                float p = Math.max(0, n - 0.55f) * 2.2f;
                o[0] = o[0] * (1 - p) + 0.12f * p; o[1] = o[1] * (1 - p) + 0.26f * p; o[2] = o[2] * (1 - p) + 0.18f * p;
                break;
            }
            case Mat.GOLD: rgb(o, 0.78f, 0.56f, 0.20f); break;
            case Mat.GIALLO: giallo(x, y, z, o); break;
            case Mat.PORPHYRY: porphyry(x, y, z, o); break;
            case Mat.STATUE: {
                float v = vein(x, y, z, 2.0f, 12), n = Noise.value(x * 6, y * 6, z * 6);
                rgb(o, 0.86f, 0.83f, 0.77f);
                mul(o, 0.95f + 0.06f * n);
                o[0] -= 0.08f * v; o[1] -= 0.08f * v; o[2] -= 0.06f * v;
                break;
            }
            case Mat.COFFER: {
                rgb(o, 0.30f, 0.40f, 0.55f);
                mul(o, 0.92f + 0.12f * Noise.value(x * 2, y * 2, z * 2));
                break;
            }
            case Mat.PINE: {
                float n = Noise.fbm(x * 0.9f, y * 0.9f, z * 0.9f, 3);
                rgb(o, 0.045f, 0.085f, 0.03f);
                mul(o, 0.55f + 0.9f * n);
                break;
            }
            case Mat.BARK: rgb(o, 0.17f, 0.11f, 0.08f); mul(o, 0.8f + 0.4f * Noise.value(x * 3, y * 8, z * 3)); break;
            case Mat.CYPRESS: {
                float n = Noise.fbm(x * 1.4f, y * 0.8f, z * 1.4f, 3);
                rgb(o, 0.028f, 0.065f, 0.032f);
                mul(o, 0.6f + 0.8f * n);
                break;
            }
            case Mat.BRICK: brick(x, y, z, nx, ny, nz, o); break;
            case Mat.EARTH: earth(x, y, z, o); break;
            case Mat.EMBER: rgb(o, 0.05f, 0.035f, 0.03f); break;
            default: rgb(o, 0.5f, 0.5f, 0.5f);
        }
    }

    /** Ziegelmauerwerk (opus testaceum) mit Mörtelfugen und Bändern aus großen Ziegeln (bipedales). */
    public static void brick(float x, float y, float z, float nx, float ny, float nz, float[] o) {
        float a, b;
        if (Math.abs(ny) > 0.7f) { a = x; b = z; }
        else { a = Math.abs(nx) > Math.abs(nz) ? z : x; b = y; }
        // feine Lagen nur schwach (sonst flimmert es in der Ferne), kräftiger die Bänder und Flecken
        float course = (float) Math.floor(b / 0.066f);
        float t = Noise.cell((int) Math.floor(a / 0.31f + course * 0.5f), (int) course, 11);
        float w = Noise.fbm(x * 0.35f, y * 0.35f, z * 0.35f, 3);
        float patch = Noise.value(a * 1.1f, b * 2.2f, 3.3f);
        rgb(o, 0.41f, 0.245f, 0.165f);
        mul(o, (0.93f + 0.1f * t) * (0.7f + 0.45f * w) * (0.88f + 0.22f * patch));
        float mo = (float) Math.cos(b / 0.066f * 6.2831853f) * 0.5f + 0.5f;
        mo *= mo * mo;
        o[0] += (0.30f - o[0]) * 0.18f * mo; o[1] += (0.27f - o[1]) * 0.18f * mo; o[2] += (0.23f - o[2]) * 0.18f * mo;
        float band = frac(b / 1.25f);
        if (band < 0.05f) mul(o, 0.8f);
    }

    /** Römischer Beton (opus caementicium): Mörtel mit Tuff- und Ziegelbrocken. */
    public static void concrete(float x, float y, float z, float[] o) {
        float f = Noise.cell((int) Math.floor(x * 6), (int) Math.floor(y * 6), (int) Math.floor(z * 6));
        float n = Noise.fbm(x * 0.8f, y * 0.8f, z * 0.8f, 3);
        rgb(o, 0.46f, 0.41f, 0.35f);
        if (f > 0.78f) rgb(o, 0.42f, 0.24f, 0.16f);
        else if (f < 0.18f) rgb(o, 0.34f, 0.31f, 0.27f);
        mul(o, 0.82f + 0.3f * n);
    }

    /** Erdreich mit Steinchen. */
    public static void earth(float x, float y, float z, float[] o) {
        float n = Noise.fbm(x * 0.5f, y * 0.9f, z * 0.5f, 3);
        float f = Noise.cell((int) Math.floor(x * 9), (int) Math.floor(y * 9), (int) Math.floor(z * 9));
        rgb(o, 0.24f, 0.165f, 0.105f);
        mul(o, 0.75f + 0.45f * n);
        if (f > 0.93f) rgb(o, 0.42f, 0.38f, 0.33f);
    }

    // ------------------------------------------------------------- Außen

    private static void ground(float x, float z, float[] o) {
        float ax = Math.abs(x), az = z;
        boolean gravel = (ax < 116 && Math.abs(az) < 64)
                || (ax < 34 && az > 0 && az < 80)
                || (ax < 5.5f && az < -60)
                || (Math.abs(az + 106) < 3.5f && ax < 150)
                || (az > 128 && az < 140 && ax < 118);
        if (gravel) {
            float f = Noise.value(x * 6, 0, z * 6), n = Noise.fbm(x * 0.1f, 0, z * 0.1f, 2);
            rgb(o, 0.46f, 0.41f, 0.33f);
            mul(o, 0.82f + 0.2f * f + 0.1f * n);
            return;
        }
        float n = Noise.fbm(x * 0.03f, 0, z * 0.03f, 4), f = Noise.value(x * 1.3f, 0, z * 1.3f);
        float r = 0.075f + 0.07f * n, g = 0.13f + 0.05f * n, b = 0.035f + 0.015f * n;
        float k = 0.8f + 0.35f * f;
        rgb(o, r * k, g * k, b * k);
    }

    /** Travertin: Platten im Verband, typische Poren und warme Tönung je Platte. */
    private static void travertine(float x, float y, float z, float nx, float ny, float nz, float[] o) {
        tag(o, 8);
        float u, v;
        if (Math.abs(ny) > 0.7f) { u = x; v = z; } else if (Math.abs(nx) > Math.abs(nz)) { u = z; v = y; } else { u = x; v = y; }
        float row = (float) Math.floor(v / 0.9f);
        float ju = frac(u / 1.5f + row * 0.37f), jv = frac(v / 0.9f);
        float t = Noise.cell((int) Math.floor(u / 1.5f + row * 0.37f), (int) row, 7);
        rgb(o, 0.62f, 0.56f, 0.44f);
        mul(o, 0.86f + 0.2f * t + 0.08f * Noise.value(x * 3, y * 3, z * 3));
        // Schichtung und Poren
        float band = Noise.value(u * 0.8f, v * 9f, t * 10);
        mul(o, 0.93f + 0.1f * band);
        float pore = Noise.cell((int) Math.floor(u * 28), (int) Math.floor(v * 60), 13);
        if (pore > 0.985f) mul(o, 0.78f);
        if (ju < 0.012f || jv < 0.02f) mul(o, 0.62f);
    }

    /** Außenputz, in Quader geritzt (Quaderimitation). */
    private static void stucco(float x, float y, float z, float nx, float nz, float[] o) {
        float n = Noise.fbm(x * 0.08f, y * 0.08f, z * 0.08f, 3), f = Noise.value(x * 4, y * 4, z * 4);
        rgb(o, 0.66f, 0.55f, 0.40f);
        mul(o, 0.84f + 0.22f * n + 0.05f * f);
        float h = Math.abs(nx) > Math.abs(nz) ? z : x;
        float row = (float) Math.floor(y / 0.62f);
        if (frac(y / 0.62f) < 0.03f || frac(h / 1.3f + row * 0.5f) < 0.012f) mul(o, 0.86f);
        // Regenspuren unter der Traufe
        float streak = Noise.value(h * 1.7f, y * 0.08f, 3) * Math.max(0, (y - 12) / 8f);
        mul(o, 1 - 0.12f * streak);
    }

    // ------------------------------------------------------------- Innenwände

    /**
     * Wandverkleidung: Sockel aus Verde antico, darüber Orthostaten aus Pavonazzetto in
     * Rahmen aus Giallo antico, ein Gesimsband, oben weiße Marmorplatten.
     */
    private static void marbleWall(float x, float y, float z, float nx, float ny, float nz, float[] o) {
        boolean vertical = Math.abs(ny) < 0.5f;
        if (!vertical) { whiteMarble(x, y, z, o); return; }
        float h = Math.abs(nx) > Math.abs(nz) ? z : x;
        if (y < 1.05f && y > -0.3f) {
            if (y > 0.93f) { rgb(o, 0.84f, 0.82f, 0.77f); return; }
            verde(x, y, z, o);
            return;
        }
        if (y < 4.3f) {
            float pw = 2.4f, fu = frac(h / pw), du = Math.min(fu, 1 - fu) * pw;
            float dv = Math.min(y - 1.05f, 4.3f - y);
            float d = Math.min(du, dv);
            int panel = (int) Math.floor(h / pw);
            if (d < 0.05f) { rgb(o, 0.84f, 0.82f, 0.77f); return; }
            if (d < 0.2f) { giallo(x, y, z, o); return; }
            if (d < 0.26f) { verde(x, y, z, o); return; }
            if ((panel & 3) == 1) {
                // jede vierte Platte: Porphyr-Scheibe auf Pavonazzetto
                float cu = (fu - 0.5f) * pw, cv = y - 2.675f;
                if (cu * cu + cv * cv < 0.36f) {
                    if (cu * cu + cv * cv > 0.3f) { giallo(x, y, z, o); return; }
                    porphyry(x, y, z, o);
                    return;
                }
            }
            pavonazzetto(x + panel * 3.1f, y, z, o);
            return;
        }
        if (y < 4.7f) {
            rgb(o, 0.84f, 0.82f, 0.77f);
            if (y < 4.36f || y > 4.64f) mul(o, 0.8f);
            return;
        }
        whiteMarble(x, y, z, o);
        float jr = frac(h / 2.4f), jv = frac((y - 4.7f) / 1.2f);
        float tone = Noise.cell((int) Math.floor(h / 2.4f), (int) Math.floor((y - 4.7f) / 1.2f), 11);
        mul(o, 0.93f + 0.12f * tone);
        if (jr < 0.006f || jv < 0.012f) mul(o, 0.8f);
    }

    // ------------------------------------------------------------- Böden

    private static void floor(float x, float y, float z, float ny, float[] o) {
        if (Math.abs(ny) < 0.7f) { marbleWall(x, y, z, 0, 0, 1, o); return; }
        float dcx = x, dcz = z - 50;
        float rc = (float) Math.sqrt(dcx * dcx + dcz * dcz);
        if (rc < 17.8f && y > 0.03f) { caldariumFloor(x, z, rc, (float) Math.atan2(dcz, dcx), o); return; }
        float ax = Math.abs(x);
        if (ax > 60 && ax < 95 && Math.abs(z) < 40) { mosaic(x, z, o); return; }
        if (ax < 31.5f && z > -26 && z < 33) { sectile(x, z, o); return; }
        simpleMosaic(x, z, o);
    }

    /** Opus sectile: Felder aus Giallo antico und Pavonazzetto, Porphyrkreise, Rahmen aus Verde antico. */
    private static void sectile(float x, float z, float[] o) {
        float s = 1.6f;
        float fx = frac(x / s), fz = frac(z / s);
        int ix = (int) Math.floor(x / s), iz = (int) Math.floor(z / s);
        float b = Math.min(Math.min(fx, 1 - fx), Math.min(fz, 1 - fz));
        if (b < 0.05f) { verde(x, 0, z, o); return; }
        boolean odd = ((ix + iz) & 1) == 0;
        float dx = fx - 0.5f, dz = fz - 0.5f, r = (float) Math.sqrt(dx * dx + dz * dz);
        if (odd && r < 0.3f) {
            if (r > 0.26f) { giallo(x, 0, z, o); return; }
            porphyry(x, 0, z, o);
            return;
        }
        if (odd) pavonazzetto(x, 0, z, o); else giallo(x, 0, z, o);
        mul(o, 0.94f + 0.1f * Noise.cell(ix, 0, iz));
    }

    /** Caldarium: konzentrische Ringe aus Platten, im Wechsel Giallo und Pavonazzetto, Porphyrfugen. */
    private static void caldariumFloor(float x, float z, float r, float a, float[] o) {
        if (r < 2.2f) {
            if (r > 1.9f) { giallo(x, 0, z, o); return; }
            porphyry(x, 0, z, o);
            return;
        }
        float ring = (r - 2.2f) / 1.55f;
        int ri = (int) Math.floor(ring);
        float fr = frac(ring);
        int nSeg = Math.max(8, (int) (2 * Math.PI * r / 1.4f) / 4 * 4);
        float seg = (float) ((a + Math.PI) / (2 * Math.PI) * nSeg + ri * 0.5f);
        float fs = frac(seg);
        int si = (int) Math.floor(seg);
        if (fr < 0.05f || fr > 0.95f) { porphyry(x, 0, z, o); return; }
        if (fs < 0.03f || fs > 0.97f) { rgb(o, 0.84f, 0.82f, 0.77f); return; }
        if (((si + ri) & 1) == 0) giallo(x, 0, z, o); else pavonazzetto(x, 0, z, o);
    }

    /** Schwarz-weißes Mosaik in den Säulenhallen der Palästren: Kreuzmuster und Mäanderband. */
    private static void mosaic(float x, float z, float[] o) {
        float t = 0.045f;
        int tx = (int) Math.floor(x / t), tz = (int) Math.floor(z / t);
        float jit = Noise.cell(tx, 3, tz);
        float cx = (tx + 0.5f) * t, cz = (tz + 0.5f) * t;
        boolean black;
        float ax = Math.abs(cx);
        float edge = Math.min(Math.min(ax - 62.2f, 92.8f - ax), 39.2f - Math.abs(cz));
        if (edge < 1.2f && edge > 0.35f) {
            // Band: einfacher Mäander (Schlüssel) mit 0,85 m Periode
            float p = (Math.abs(cz) < 33 ? cz : cx) / 0.85f, q = (edge - 0.35f) / 0.85f;
            float fp = frac(p);
            black = (q < 0.14f || q > 0.86f) || (fp < 0.14f && q < 0.72f) || (fp > 0.14f && fp < 0.72f && q > 0.58f && q < 0.72f)
                    || (fp > 0.58f && fp < 0.72f && q > 0.28f && q < 0.72f);
        } else {
            float fx = frac(cx / 0.9f) - 0.5f, fz = frac(cz / 0.9f) - 0.5f;
            black = (Math.abs(fx) < 0.06f && Math.abs(fz) < 0.3f) || (Math.abs(fz) < 0.06f && Math.abs(fx) < 0.3f);
        }
        if (black) rgb(o, 0.05f, 0.05f, 0.055f); else rgb(o, 0.80f, 0.78f, 0.72f);
        mul(o, 0.92f + 0.12f * jit);
    }

    private static void simpleMosaic(float x, float z, float[] o) {
        float t = 0.05f;
        int tx = (int) Math.floor(x / t), tz = (int) Math.floor(z / t);
        float cx = (tx + 0.5f) * t, cz = (tz + 0.5f) * t;
        float fx = frac(cx / 1.2f), fz = frac(cz / 1.2f);
        boolean black = Math.min(Math.min(fx, 1 - fx), Math.min(fz, 1 - fz)) < 0.06f;
        if (black) rgb(o, 0.06f, 0.06f, 0.065f); else rgb(o, 0.76f, 0.74f, 0.68f);
        mul(o, 0.9f + 0.16f * Noise.cell(tx, 5, tz));
    }

    // ------------------------------------------------------------- Kassetten

    /**
     * Stuckgewölbe mit Kassetten: Rahmen, Schräge und vertiefter Spiegel mit Goldrosette.
     * Das Relief entsteht über die verformte Normale.
     */
    private static float vault(float x, float y, float z, float[] n, float[] o) {
        float base = 0.8f + 0.1f * Noise.fbm(x * 0.15f, y * 0.15f, z * 0.15f, 3);
        rgb(o, 0.82f * base / 0.85f, 0.79f * base / 0.85f, 0.72f * base / 0.85f);
        float u, s, tux, tuy, tuz, tsx, tsy, tsz, cell;
        float ax = Math.abs(x);
        // Caldarium-Kuppel und Apsiden haben echte Kassetten aus Geometrie
        if (x * x + (z - 50) * (z - 50) < 400 && y > 26) return 1;
        if (ax > 87 && Math.abs(z) < 8 && y > 10.8f) return 1;
        if (ax < 29.3f && Math.abs(z) < 12.3f && y > 20.8f) {
            // Frigidarium: Kreuzgratgewölbe, Zweig nach Normalenrichtung
            if (Math.abs(n[2]) >= Math.abs(n[0])) {
                float th = (float) Math.atan2(z, y - 21);
                u = x; s = 12 * th;
                tux = 1; tuy = 0; tuz = 0;
                tsx = 0; tsy = -(float) Math.sin(th); tsz = (float) Math.cos(th);
            } else {
                float cx = x < -9.667f ? -19.333f : (x > 9.667f ? 19.333f : 0);
                float th = (float) Math.atan2(x - cx, y - 21);
                u = z; s = 12 * th;
                tux = 0; tuy = 0; tuz = 1;
                tsx = (float) Math.cos(th); tsy = -(float) Math.sin(th); tsz = 0;
            }
            cell = 2.0f;
        } else if (ax < 10.3f && z > 19.5f && z < 33 && y > 11.8f && y < 23.5f) {
            float th = (float) Math.atan2(x, y - 12);
            u = z - 20; s = 10 * th;
            tux = 0; tuy = 0; tuz = 1;
            tsx = (float) Math.cos(th); tsy = -(float) Math.sin(th); tsz = 0;
            cell = 1.8f;
        } else if (n[1] < -0.95f) {
            u = x; s = z;
            tux = 1; tuy = 0; tuz = 0; tsx = 0; tsy = 0; tsz = 1;
            cell = 2.2f;
        } else {
            return 1;
        }
        float fu = frac(u / cell), fs = frac(s / cell);
        float du = Math.min(fu, 1 - fu) * cell, ds = Math.min(fs, 1 - fs) * cell;
        float d = Math.min(du, ds);
        float frame = 0.22f, bevel = 0.2f;
        if (d < frame) {
            if (d < 0.06f) mul(o, 0.93f);
            return 1;
        }
        if (d < frame + bevel) {
            float k = 0.9f;
            if (du < ds) {
                float sg = fu < 0.5f ? 1 : -1;
                tilt(n, tux * sg * k, tuy * sg * k, tuz * sg * k);
            } else {
                float sg = fs < 0.5f ? 1 : -1;
                tilt(n, tsx * sg * k, tsy * sg * k, tsz * sg * k);
            }
            mul(o, 0.9f);
            return 0.82f;
        }
        // Spiegel: helles Blau mit Goldrosette in der Mitte
        float cu = (fu - 0.5f) * cell, cs = (fs - 0.5f) * cell;
        float r = (float) Math.sqrt(cu * cu + cs * cs);
        float rr = 0.17f * cell;
        float petals = (float) Math.cos(8 * Math.atan2(cs, cu));
        if (r < rr * (0.75f + 0.25f * petals)) {
            rgb(o, 0.80f, 0.58f, 0.22f);
            return 0.9f;
        }
        float edge = Math.min(du, ds) - frame - bevel;
        rgb(o, 0.40f, 0.52f, 0.66f);
        mul(o, base);
        return 0.72f + 0.2f * Math.min(1, edge / 0.25f);
    }

    private static void tilt(float[] n, float tx, float ty, float tz) {
        float x = n[0] + tx, y = n[1] + ty, z = n[2] + tz;
        float l = (float) Math.sqrt(x * x + y * y + z * z);
        n[0] = x / l; n[1] = y / l; n[2] = z / l;
    }
}
