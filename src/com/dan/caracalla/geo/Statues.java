package com.dan.caracalla.geo;

import java.util.Random;

/**
 * Prozedurale Statuen: Gewandfigur, Athlet und Held mit Keule, jeweils auf einer Plinthe.
 * Keine Nachbildung eines bestimmten Werks, nur die Grundformen römischer Standbilder.
 */
public final class Statues {
    public static final int DRAPED = 0, ATHLETE = 1, HERO = 2;

    private Statues() { }

    /**
     * @param x,z     Standpunkt (Mitte der Plinthe)
     * @param y0      Höhe der Standfläche
     * @param H       Gesamthöhe mit Plinthe
     * @param yaw     Blickrichtung: (sin yaw, cos yaw)
     */
    public static void statue(MeshBuilder mb, double x, double z, double y0, double H, double yaw, int kind, int m, long seed) {
        int v0 = mb.vertexCount();
        double keep = mb.maxEdge;
        mb.maxEdge = 10;
        Random rnd = new Random(seed);
        double P = H * 0.14, F = H - P;
        mb.box(-0.21 * F, 0, -0.17 * F, 0.21 * F, P, 0.17 * F, Mat.CORNICE, false);
        mb.box(-0.23 * F, P - 0.05 * F, -0.19 * F, 0.23 * F, P, 0.19 * F, Mat.CORNICE, false);
        double lean = (rnd.nextDouble() - 0.5) * 0.04;
        figure(mb, F, P, kind, lean, rnd, m);
        mb.transform(v0, yaw, x, y0, z);
        mb.maxEdge = keep;
    }

    private static void figure(MeshBuilder mb, double F, double b, int kind, double lean, Random rnd, int m) {
        double sway = kind == DRAPED ? 0 : 0.03 * F;
        // Kopf, Hals
        double hx = lean * F + sway * 0.3;
        mb.ellipsoid(hx, b + 0.925 * F, 0.012 * F, 0.064 * F, 0.078 * F, 0.07 * F, 10, 6, m);
        Shapes.limb(mb, hx * 0.8, b + 0.83 * F, 0, hx, b + 0.9 * F, 0.005 * F, 0.036 * F, 0.032 * F, 8, m);
        // Rumpf
        mb.ellipsoid(sway * 0.4, b + 0.70 * F, 0, 0.125 * F, 0.16 * F, 0.078 * F, 12, 7, m);
        mb.ellipsoid(sway, b + 0.54 * F, 0, 0.11 * F, 0.085 * F, 0.074 * F, 12, 6, m);
        double shY = b + 0.795 * F;
        mb.ellipsoid(0.15 * F + sway * 0.3, shY, 0, 0.05 * F, 0.045 * F, 0.05 * F, 8, 5, m);
        mb.ellipsoid(-0.15 * F + sway * 0.3, shY, 0, 0.05 * F, 0.045 * F, 0.05 * F, 8, 5, m);
        if (kind == DRAPED) {
            // Gewand: weiter Rock mit Falten bis zum Boden
            mb.patch((u, v, p, n) -> {
                double r = (0.155 - 0.05 * v) * F * (1 + 0.07 * Math.sin(11 * u + 3 * v));
                double rz = r * 0.78;
                p[0] = r * Math.cos(u); p[1] = b + 0.02 * F + 0.56 * F * v; p[2] = rz * Math.sin(u);
                n[0] = Math.cos(u); n[1] = 0.12; n[2] = Math.sin(u) / 0.78;
            }, 0, 2 * Math.PI, 22, 0, 1, 3, m);
            // Mantelbausch über die Schulter
            Shapes.limb(mb, 0.14 * F, b + 0.78 * F, 0.03 * F, -0.1 * F, b + 0.5 * F, 0.07 * F, 0.06 * F, 0.08 * F, 10, m);
            // linker Arm am Körper, rechter angewinkelt nach vorn
            arm(mb, -1, F, b, sway, 0.62, 0.02, 0.45, 0.05, m);
            arm(mb, 1, F, b, sway, 0.6, 0.05, 0.64, 0.16, m);
            return;
        }
        // Beine im Kontrapost: Standbein rechts, Spielbein links
        leg(mb, 1, F, b, sway, 0.0, m);
        leg(mb, -1, F, b, sway, 0.05, m);
        if (kind == ATHLETE) {
            arm(mb, -1, F, b, sway, 0.6, 0.0, 0.44, 0.02, m);
            boolean raise = rnd.nextBoolean();
            if (raise) arm(mb, 1, F, b, sway, 0.95, 0.02, 1.07, 0.02, m);
            else arm(mb, 1, F, b, sway, 0.6, 0.08, 0.66, 0.18, m);
        } else {
            // Held auf die Keule gestützt, Löwenfell über dem Arm angedeutet
            arm(mb, 1, F, b, sway, 0.6, -0.02, 0.43, -0.04, m);
            arm(mb, -1, F, b, sway, 0.62, 0.03, 0.47, 0.07, m);
            Shapes.limb(mb, -0.2 * F, b + 0.47 * F, 0.07 * F, -0.28 * F, b + 0.01 * F, 0.12 * F, 0.024 * F, 0.042 * F, 8, m);
            mb.ellipsoid(-0.2 * F, b + 0.62 * F, 0.02 * F, 0.06 * F, 0.12 * F, 0.06 * F, 8, 5, m);
        }
    }

    /** Arm: side ±1, Ellbogenhöhe/-tiefe und Handhöhe/-tiefe in Anteilen der Figurhöhe. */
    private static void arm(MeshBuilder mb, int side, double F, double b, double sway, double elY, double elZ,
                            double hY, double hZ, int m) {
        double sx = side * 0.155 * F + sway * 0.3, sy = b + 0.785 * F;
        double ex = side * 0.19 * F + sway * 0.2, ey = b + elY * F, ez = elZ * F;
        double wx = side * (hZ > 0.1 ? 0.12 : 0.2) * F + sway * 0.1, wy = b + hY * F, wz = hZ * F;
        Shapes.limb(mb, sx, sy, 0, ex, ey, ez, 0.042 * F, 0.034 * F, 8, m);
        Shapes.limb(mb, ex, ey, ez, wx, wy, wz, 0.032 * F, 0.025 * F, 8, m);
        mb.ellipsoid(wx, wy - 0.02 * F, wz, 0.025 * F, 0.04 * F, 0.02 * F, 6, 4, m);
    }

    private static void leg(MeshBuilder mb, int side, double F, double b, double sway, double relax, int m) {
        double hx = side * 0.058 * F + sway, hy = b + 0.5 * F;
        double kx = side * 0.065 * F + sway * 0.5, ky = b + 0.27 * F, kz = relax * F * 0.8;
        double ax = side * (0.07 + relax * 0.5) * F, ay = b + 0.035 * F, az = -relax * F * 0.6;
        Shapes.limb(mb, hx, hy, 0, kx, ky, kz, 0.062 * F, 0.042 * F, 9, m);
        Shapes.limb(mb, kx, ky, kz, ax, ay, az, 0.042 * F, 0.027 * F, 9, m);
        mb.ellipsoid(ax, b + 0.018 * F, az + 0.035 * F, 0.03 * F, 0.018 * F, 0.06 * F, 6, 4, m);
    }
}
