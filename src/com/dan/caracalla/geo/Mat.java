package com.dan.caracalla.geo;

/** Materialnummern und Geometriegruppen der Szene. */
public final class Mat {
    public static final int SKY = 0, GROUND = 1, LAND = 2, PAVING = 3, STUCCO = 4, PLASTER = 5,
            MARBLE = 6, FLOOR = 7, GRANITE = 8, VAULT = 9, TILE = 10, ROOFFLAT = 11, WATER = 12,
            POOL = 13, BRONZE = 14, GIALLO = 15, PORPHYRY = 16, PINE = 17, BARK = 18, CYPRESS = 19,
            CORNICE = 20, COFFER = 21, STATUE = 22, GOLD = 23,
            EMBER = 24, BRICK = 25, EARTH = 26;
    public static final int COUNT = 27;

    /** Gruppe 0: Bau und Gelände. Gruppe 1: Dächer, Gewölbe, Kuppeln (ausblendbar). Gruppe 2: unter der Erde (nur im Schnitt). */
    public static final int G_BASE = 0, G_ROOF = 1, G_UNDER = 2;

    /** Gewachsenes und Erde: bleibt im Ruinenzustand stehen. */
    public static boolean natural(int m) {
        return m == GROUND || m == LAND || m == PINE || m == BARK || m == CYPRESS || m == EARTH || m == SKY;
    }

    private Mat() { }
}
