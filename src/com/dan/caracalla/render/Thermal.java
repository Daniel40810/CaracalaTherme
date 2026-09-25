package com.dan.caracalla.render;

import com.dan.caracalla.geo.Mat;

/**
 * Wärmebild: eine einfache Temperaturverteilung der Anlage (Außenluft nach Jahres- und
 * Tageszeit, beheizte Säle, Becken, Hypokaustum, Feuer) und die Farbskala „Eisenglut“.
 */
public final class Thermal {
    private Thermal() { }

    /** Skalenbereich; folgt der Außentemperatur wie bei einer Wärmebildkamera mit Automatik. */
    public static volatile float T_MIN = 0, T_MAX = 60;

    public static void setRange(float tout) {
        float lo = Math.round(tout - 12);
        T_MIN = lo;
        T_MAX = Math.max(lo + 56, 58);
    }

    /** Lufttemperatur in Rom (°C) nach Tag im Jahr und wahrer Ortszeit. */
    public static float outside(double hour, int day) {
        double mean = 15.8 + 8.4 * Math.cos(2 * Math.PI * (day - 205) / 365.0);
        double amp = 4.2 + 1.6 * Math.cos(2 * Math.PI * (day - 190) / 365.0);
        return (float) (mean + amp * Math.cos(2 * Math.PI * (hour - 15) / 24.0));
    }

    private static boolean inCaldarium(float x, float z) { float dz = z - 50; return x * x + dz * dz < 17.6f * 17.6f; }
    private static boolean inTepidarium(float x, float z) { return x > -10.5f && x < 10.5f && z > 19.5f && z < 33; }
    private static boolean inSudatorium(float x, float z) { float ax = Math.abs(x); return ax > 11.5f && ax < 32.4f && z > 19 && z < 53.75f; }
    private static boolean inFrigidarium(float x, float z) { return Math.abs(x) < 31.4f && z > -26 && z < 19; }
    private static boolean inBuilding(float x, float z) { return Math.abs(x) < 107 && Math.abs(z) < 55; }

    /** Luft an einem Ort; heat = 1 im Betrieb, 0 kalt (Ruine). */
    public static float air(float x, float y, float z, float tout, float heat) {
        float t;
        if (y > 48) return tout;
        if (inCaldarium(x, z)) t = 42 + y * 0.12f;
        else if (inTepidarium(x, z)) t = 30 + y * 0.08f;
        else if (inSudatorium(x, z)) t = 38;
        else if (inFrigidarium(x, z) && y < 36) return tout + (19 - tout) * 0.55f;
        else if (inBuilding(x, z) && y < 21) return tout + (18 - tout) * 0.35f;
        else return tout;
        return tout + (t - tout) * heat;
    }

    /** Hypokaustum: der Raum unter den Böden der warmen Säle. */
    public static boolean inPit(float x, float y, float z) {
        return y < -0.28f && y > -1.15f && (inCaldarium(x, z) || (x > -10 && x < 10 && z > 20 && z < 32.5f));
    }

    /** Wandröhren (tubuli) im Mauerkern der beheizten Säle. */
    public static boolean inTubuli(float x, float y, float z) {
        if (y < -0.3f) return false;
        float dz = z - 50, r = (float) Math.sqrt(x * x + dz * dz);
        if (r > 17.55f && r < 18.15f && y < 26.6f) return true;
        float ax = Math.abs(x);
        return ax > 10.05f && ax < 10.6f && z > 20 && z < 32.5f && y < 12;
    }

    /** Nähe zum Feuer: 1 im Schürloch, fällt über einige Meter ab. */
    public static float fire(float x, float y, float z, java.util.List<double[]> furnaces) {
        float best = 0;
        for (double[] f : furnaces) {
            // Einlass unter der Mauer (3,5 m hinter dem Schürloch Richtung Mitte)
            float fx = (float) (f[0] - f[3] * 6.5), fz = (float) (f[2] - f[4] * 6.5);
            float dx = x - fx, dz = z - fz, dy = y + 0.7f;
            float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            best = Math.max(best, (float) Math.exp(-d / 5.5));
        }
        return best;
    }

    /**
     * Oberflächentemperatur. sun = direkte Sonne 0..1 (schon mit Schatten), sky = Himmelssicht,
     * night = 1 bei Nacht, heat = Betrieb.
     */
    public static float surface(int m, float x, float y, float z, float ny, float sun, float sky,
                                float tout, float heat, java.util.List<double[]> furnaces) {
        if (m == Mat.EMBER) return tout + (720 - tout) * heat;
        if (inPit(x, y, z) || (y < -0.25f && inTubuli(x, 0, z))) {
            float f = furnaces == null ? 0.3f : fire(x, y, z, furnaces);
            return tout + (95 + 280 * f - tout) * heat;
        }
        if (m == Mat.BRONZE && y > 2.3f && y < 4.2f && !inBuilding(x, z) && z > 55) return tout + (88 - tout) * heat;
        if (m == Mat.TILE || m == Mat.ROOFFLAT) {
            // Dachhaut außen: Sonne und etwas Wärme, die durch Kuppel und Gewölbe dringt
            boolean overHot = inCaldarium(x, z) || Math.hypot(x, z - 50) < 21 || inTepidarium(x, z) || inSudatorium(x, z);
            return tout + sun * 17 - 2.5f * sky + (overHot ? 6 * heat : 1.5f * heat);
        }
        float t = air(x, y, z, tout, heat);
        boolean warmRoom = inCaldarium(x, z) || inTepidarium(x, z) || inSudatorium(x, z);
        if (warmRoom && y < 40) {
            if (ny > 0.7f && y < 1.0f) t += (inCaldarium(x, z) ? 9 : 5) * heat;   // Fußbodenheizung
            else if (ny < 0.5f && ny > -0.5f) t += 2.5f * heat;                   // Wandheizung
            return t;
        }
        // außen: Sonne erwärmt Stein, der offene Himmel kühlt nachts
        boolean plant = m == Mat.PINE || m == Mat.CYPRESS || m == Mat.LAND || m == Mat.BARK;
        t += sun * (plant ? 4 : 17);
        t -= 2.5f * sky;
        return t;
    }

    /** Wassertemperatur der Becken nach Ort. */
    public static float water(float x, float y, float z, float tout, float heat) {
        if (inCaldarium(x, z)) return tout + (40 - tout) * heat;
        if (Math.abs(x) > 14 && Math.abs(x) < 29 && z > 21 && z < 30) return tout + (29 - tout) * heat;
        if (y > 5) return 14;                       // Aquädukt
        if (z < -27) return Math.max(9, tout - 1.5f); // Natatio unter freiem Himmel
        return 16;                                  // Frigidarium: Quellwasser
    }

    private static final float[][] STOPS = {
            {0.00f, 0.00f, 0.00f, 0.05f}, {0.14f, 0.12f, 0.03f, 0.36f}, {0.30f, 0.38f, 0.04f, 0.56f},
            {0.46f, 0.66f, 0.10f, 0.48f}, {0.60f, 0.87f, 0.25f, 0.18f}, {0.73f, 0.96f, 0.50f, 0.06f},
            {0.86f, 0.99f, 0.79f, 0.20f}, {1.00f, 1.00f, 0.99f, 0.88f}};

    /** Farbe der Skala (sRGB 0..1) für eine Temperatur. */
    public static void palette(float t, float[] o) {
        float lo = T_MIN, hi = T_MAX;
        float u = (t - lo) / (hi - lo);
        if (u <= 0) { o[0] = STOPS[0][1]; o[1] = STOPS[0][2]; o[2] = STOPS[0][3]; return; }
        if (u >= 1) {
            // heißer als die Skala: weiß, bei sehr heiß leicht ins Bläuliche
            float k = Math.min(1, (t - hi) / 300);
            o[0] = 1 - 0.1f * k; o[1] = 0.99f - 0.04f * k; o[2] = 0.88f + 0.12f * k;
            return;
        }
        int i = 1;
        while (STOPS[i][0] < u) i++;
        float[] a = STOPS[i - 1], b = STOPS[i];
        float k = (u - a[0]) / (b[0] - a[0]);
        o[0] = a[1] + (b[1] - a[1]) * k; o[1] = a[2] + (b[2] - a[2]) * k; o[2] = a[3] + (b[3] - a[3]) * k;
    }
}
