package com.dan.caracalla.model;

import com.dan.caracalla.geo.Mat;
import com.dan.caracalla.geo.MeshBuilder;
import com.dan.caracalla.geo.Shapes;

import java.util.List;
import java.util.Random;

/**
 * Die Fußbodenheizung: Praefurnien außen am Caldarium, darunter das Hypokaustum mit
 * Ziegelpfeilern (pilae), der schwebende Boden (suspensura) und die Wege der heißen Luft
 * durch die Wandröhren (tubuli). Der unterirdische Teil gehört zur Gruppe G_UNDER und
 * erscheint nur im Schnitt; dazu ein geschlossener Erdkörper unter dem Zentralbau,
 * damit die Schnittfläche gefüllt wird.
 */
final class Hypocaust {
    static final double PIT_Y0 = -1.1, PIT_Y1 = -0.3;
    /** Winkel der drei Praefurnien (dort liegen die Wannen). */
    static final double[] FURNACE_DEG = {45, 90, 135};
    static final double R_WALL_OUT = ThermenModel.CAL_R + ThermenModel.CAL_T, F_DEPTH = 3.2;

    private Hypocaust() { }

    /** Grenzen des Erdkörpers und des Schnittbereichs: x0, z0, x1, z1, Unterkante. */
    static final double[] EARTH = {-ThermenModel.BX - 2, -ThermenModel.BZ - 2, ThermenModel.BX + 2, 76, -2.0};

    static void build(MeshBuilder mb, List<Route> heat, List<double[]> furnaces) {
        double keep = mb.maxEdge;
        int keepG = mb.group;
        // --------------------------------------------------------- Erdkörper
        mb.group = Mat.G_UNDER;
        mb.maxEdge = 8;
        double x0 = EARTH[0], z0 = EARTH[1], x1 = EARTH[2], z1 = EARTH[3], yb = EARTH[4];
        mb.quad(new double[]{x0, yb, z0}, new double[]{x1, yb, z0}, new double[]{x1, 0, z0}, new double[]{x0, 0, z0}, 0, 0, -1, Mat.EARTH);
        mb.quad(new double[]{x0, yb, z1}, new double[]{x1, yb, z1}, new double[]{x1, 0, z1}, new double[]{x0, 0, z1}, 0, 0, 1, Mat.EARTH);
        mb.quad(new double[]{x0, yb, z0}, new double[]{x0, yb, z1}, new double[]{x0, 0, z1}, new double[]{x0, 0, z0}, -1, 0, 0, Mat.EARTH);
        mb.quad(new double[]{x1, yb, z0}, new double[]{x1, yb, z1}, new double[]{x1, 0, z1}, new double[]{x1, 0, z0}, 1, 0, 0, Mat.EARTH);
        mb.rectH(x0, z0, x1, z1, yb, true, Mat.EARTH);

        // --------------------------------------------------- Caldarium-Hypokaustum
        double cz = ThermenModel.CAL_Z, R = ThermenModel.CAL_R;
        mb.maxEdge = 2.5;
        Shapes.ring(mb, 0, cz, 0, R, 0, 360, PIT_Y0, true, Mat.BRICK, 48);
        Shapes.ring(mb, 0, cz, 0, R, 0, 360, PIT_Y1, false, Mat.BRICK, 48);
        Shapes.drum(mb, 0, cz, R, PIT_Y0, PIT_Y1, true, Mat.BRICK, 72);
        double step = 0.9;
        for (double x = -R + 0.45; x < R; x += step) {
            for (double z = cz - R + 0.45; z < cz + R; z += step) {
                double d = Math.hypot(x, z - cz);
                if (d > R - 0.55) continue;
                // Unter den Praefurnien bleiben Gassen frei, durch die das Feuer einzieht
                boolean lane = false;
                for (double deg : FURNACE_DEG) {
                    double a = Math.toRadians(deg), ux = Math.cos(a), uz = Math.sin(a);
                    double along = x * ux + (z - cz) * uz, across = Math.abs(-x * uz + (z - cz) * ux);
                    if (along > R - 5.5 && across < 0.8) lane = true;
                }
                if (!lane) pila(mb, x, z);
            }
        }
        // ---------------------------------------------------- Tepidarium-Hypokaustum
        double tx0 = -10, tx1 = 10, tz0 = 20, tz1 = 32.5;
        mb.rectH(tx0, tz0, tx1, tz1, PIT_Y0, true, Mat.BRICK);
        mb.rectH(tx0, tz0, tx1, tz1, PIT_Y1, false, Mat.BRICK);
        mb.quad(new double[]{tx0, PIT_Y0, tz0}, new double[]{tx1, PIT_Y0, tz0}, new double[]{tx1, PIT_Y1, tz0}, new double[]{tx0, PIT_Y1, tz0}, 0, 0, 1, Mat.BRICK);
        mb.quad(new double[]{tx0, PIT_Y0, tz1}, new double[]{tx1, PIT_Y0, tz1}, new double[]{tx1, PIT_Y1, tz1}, new double[]{tx0, PIT_Y1, tz1}, 0, 0, -1, Mat.BRICK);
        mb.quad(new double[]{tx0, PIT_Y0, tz0}, new double[]{tx0, PIT_Y0, tz1}, new double[]{tx0, PIT_Y1, tz1}, new double[]{tx0, PIT_Y1, tz0}, 1, 0, 0, Mat.BRICK);
        mb.quad(new double[]{tx1, PIT_Y0, tz0}, new double[]{tx1, PIT_Y0, tz1}, new double[]{tx1, PIT_Y1, tz1}, new double[]{tx1, PIT_Y1, tz0}, -1, 0, 0, Mat.BRICK);
        for (double x = tx0 + 0.55; x < tx1 - 0.3; x += step) {
            for (double z = tz0 + 0.55; z < tz1 - 0.3; z += step) pila(mb, x, z);
        }

        // -------------------------------------------------------- Praefurnien
        mb.group = Mat.G_BASE;
        mb.maxEdge = 1.2;
        for (double deg : FURNACE_DEG) {
            double a = Math.toRadians(deg), c = Math.cos(a), s = Math.sin(a);
            int v0 = mb.vertexCount();
            mb.box(-1.7, 0, -0.4, 1.7, 2.4, F_DEPTH, Mat.BRICK, false);
            mb.box(-1.9, 2.4, -0.4, 1.9, 2.62, F_DEPTH + 0.2, Mat.CORNICE, true);
            // Schürloch: Rahmen und Glut
            mb.box(-0.8, 1.05, F_DEPTH, 0.8, 1.35, F_DEPTH + 0.12, Mat.BRICK, true);
            mb.quad(new double[]{-0.58, 0.12, F_DEPTH + 0.015}, new double[]{0.58, 0.12, F_DEPTH + 0.015},
                    new double[]{0.58, 1.02, F_DEPTH + 0.015}, new double[]{-0.58, 1.02, F_DEPTH + 0.015}, 0, 0, 1, Mat.EMBER);
            // Kessel (testudo) und Rohr in die Mauer
            mb.cylinder(0, 1.5, 2.62, 3.95, 0.95, 0.9, 20, Mat.BRONZE, true);
            mb.box(-0.1, 3.25, -0.5, 0.1, 3.45, 0.7, Mat.BRONZE, true);
            // Brennholz neben dem Ofen
            for (int i = 0; i < 4; i++) {
                double y = 0.22 * i, off = (i & 1) * 0.1;
                mb.box(2.1 + off, y, 0.6, 2.35 + off, y + 0.2, 2.6, Mat.BARK, false);
                mb.box(2.45 - off, y, 0.6, 2.7 - off, y + 0.2, 2.6, Mat.BARK, false);
            }
            mb.transform(v0, Math.atan2(c, s), c * R_WALL_OUT, 0, cz + s * R_WALL_OUT);
            double rm = R_WALL_OUT + F_DEPTH + 0.02;
            furnaces.add(new double[]{c * rm, 0.6, cz + s * rm, c, s});
        }
        mb.maxEdge = keep;
        mb.group = keepG;

        routes(heat);
    }

    /** Ein Ziegelpfeiler: vier Seiten, oben stößt er an die Bodenplatte. */
    private static void pila(MeshBuilder mb, double x, double z) {
        double h = 0.21, ya = PIT_Y0, yb = PIT_Y1;
        mb.quad(new double[]{x - h, ya, z - h}, new double[]{x + h, ya, z - h}, new double[]{x + h, yb, z - h}, new double[]{x - h, yb, z - h}, 0, 0, -1, Mat.BRICK);
        mb.quad(new double[]{x - h, ya, z + h}, new double[]{x + h, ya, z + h}, new double[]{x + h, yb, z + h}, new double[]{x - h, yb, z + h}, 0, 0, 1, Mat.BRICK);
        mb.quad(new double[]{x - h, ya, z - h}, new double[]{x - h, ya, z + h}, new double[]{x - h, yb, z + h}, new double[]{x - h, yb, z - h}, -1, 0, 0, Mat.BRICK);
        mb.quad(new double[]{x + h, ya, z - h}, new double[]{x + h, ya, z + h}, new double[]{x + h, yb, z + h}, new double[]{x + h, yb, z - h}, 1, 0, 0, Mat.BRICK);
    }

    /** Wege der heißen Luft: vom Schürloch unter den Boden, zwischen den Pfeilern hindurch, in den Wänden hinauf. */
    private static void routes(List<Route> heat) {
        Random rnd = new Random(2160);
        double cz = ThermenModel.CAL_Z, R = ThermenModel.CAL_R;
        for (double deg : FURNACE_DEG) {
            double a = Math.toRadians(deg);
            for (int v = 0; v < 18; v++) {
                Route.Builder b = new Route.Builder("Heißluft " + (int) deg);
                polar(b, a, R_WALL_OUT + F_DEPTH - 0.4, 0.6);
                polar(b, a, R_WALL_OUT + 1.2, 0.35);
                polar(b, a, R_WALL_OUT - 1.5, -0.7);
                polar(b, a, R - 0.4, -0.7);
                // Aufstieg in einem Mauerpfeiler zwischen den Fenstern (Pfeilermitten bei -67,5° + 45° k)
                double pier = -67.5 + 45 * Math.round((deg + (rnd.nextDouble() * 2 - 1) * 110 + 67.5) / 45.0);
                double at = Math.toRadians(pier + (rnd.nextDouble() * 2 - 1) * 9);
                // zwischen den Pfeilern hindurch zu einer Stelle an der Wand
                double r0 = R - 0.4, r1 = R - 0.6;
                int k = 4 + rnd.nextInt(3);
                for (int i = 1; i <= k; i++) {
                    double u = i / (double) (k + 1);
                    double aa = a + (at - a) * u + (rnd.nextDouble() - 0.5) * 0.12;
                    double rr = r0 + (r1 - r0) * u - Math.sin(Math.PI * u) * (4 + rnd.nextDouble() * 9);
                    polar(b, aa, rr, -0.95 + rnd.nextDouble() * 0.5);
                }
                polar(b, at, R - 0.3, -0.6);
                polar(b, at, R + 0.35, -0.2);
                polar(b, at, R + 0.35, 9 + rnd.nextDouble() * 4);
                polar(b, at + 0.01, R + 0.35, 24);
                polar(b, at, R + 1.0, 27.3);
                polar(b, at, R + 1.6, 30.5);
                heat.add(b.build(1, Route.AIR));
            }
        }
        // Weiter zum Tepidarium: unter dem Caldarium hindurch nach Norden
        double a = Math.toRadians(90);
        for (int v = 0; v < 12; v++) {
            Route.Builder b = new Route.Builder("Heißluft Tepidarium");
            polar(b, a, R_WALL_OUT + F_DEPTH - 0.4, 0.6);
            polar(b, a, R_WALL_OUT + 1.2, 0.35);
            polar(b, a, R_WALL_OUT - 1.5, -0.7);
            polar(b, a, R - 0.4, -0.7);
            b.to((rnd.nextDouble() - 0.5) * 6, -0.8, cz + 6);
            b.to((rnd.nextDouble() - 0.5) * 4, -0.7, cz - 8);
            b.to((rnd.nextDouble() - 0.5) * 3, -0.7, 33.5);
            double side = v % 2 == 0 ? -1 : 1;
            double zt = 21 + rnd.nextDouble() * 10.5;
            b.to(side * (2 + rnd.nextDouble() * 4), -0.8, 27 + rnd.nextDouble() * 4);
            b.to(side * 9.6, -0.6, zt);
            b.to(side * 10.45, -0.2, zt);
            b.to(side * 10.45, 11.5, zt);
            b.to(side * 10.8, 13.4, zt);
            heat.add(b.build(0.7, Route.AIR));
        }
    }

    private static void polar(Route.Builder b, double a, double r, double y) {
        b.to(Math.cos(a) * r, y, ThermenModel.CAL_Z + Math.sin(a) * r);
    }
}
