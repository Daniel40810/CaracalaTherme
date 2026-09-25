package com.dan.caracalla.model;

import com.dan.caracalla.geo.Mat;
import com.dan.caracalla.geo.MeshBuilder;
import com.dan.caracalla.geo.Walls;
import com.dan.caracalla.geo.Walls.Line;
import com.dan.caracalla.geo.Walls.Opening;
import com.dan.caracalla.geo.Walls.Wall;

import java.util.List;

/**
 * Der Weg des Wassers: die Aqua Antoniniana kommt auf Bögen aus Südosten, fällt in die
 * Zisternen unter den Stufen des Stadions, läuft in Bleirohren unter dem Garten zum Zentralbau
 * und teilt sich dort. Das Wasser für das Caldarium geht durch die Kessel über den Praefurnien.
 */
final class WaterSupply {
    /** Mittellinie des Aquädukts: entlang z = ZA von weit draußen bis x = XB, dann nach Norden bis zur Umfassung. */
    static final double ZA = 214, XB = 46, T = 2.6, Y_TOP = 10.2, Y_WATER = 10.95;

    private WaterSupply() { }

    static void build(MeshBuilder mb, List<Route> water) {
        double keep = mb.maxEdge;
        mb.maxEdge = 4;
        double h = T / 2;
        // Bogenreihe A (ost–west)
        Line a = new Line(1150, ZA, XB + h, ZA);
        Wall w = new Wall(a, T, 0, Y_TOP, Mat.BRICK, Mat.BRICK);
        for (double u = 4.4; u < a.length() - 3; u += 6.4) w.add(Opening.arch(u, 4.4, 0, 7.4));
        Walls.build(mb, w);
        // Bogenreihe B (süd–nord bis zur Umfassungsmauer)
        Line b = new Line(XB, ZA + h, XB, ThermenModel.PZ1 + 1.5);
        w = new Wall(b, T, 0, Y_TOP, Mat.BRICK, Mat.BRICK);
        for (double u = 4.4; u < b.length() - 3; u += 6.4) w.add(Opening.arch(u, 4.4, 0, 7.4));
        Walls.build(mb, w);
        // Kanal (specus) oben: zwei Wangen und der Wasserspiegel
        double c = 0.55, top = Y_TOP + 1.3;
        mb.box(XB + h, Y_TOP, ZA - h, 1150, top, ZA - h + c, Mat.BRICK, false);
        mb.box(XB + h, Y_TOP, ZA + h - c, 1150, top, ZA + h, Mat.BRICK, false);
        mb.rectH(XB + h, ZA - h + c, 1150, ZA + h - c, Y_WATER, true, Mat.WATER);
        mb.box(XB - h, Y_TOP, ThermenModel.PZ1 + 1.5, XB - h + c, top, ZA + h, Mat.BRICK, false);
        mb.box(XB + h - c, Y_TOP, ThermenModel.PZ1 + 1.5, XB + h, top, ZA - h + c, Mat.BRICK, false);
        mb.box(XB - h + c, Y_TOP, ZA + h - c, XB + h, top, ZA + h, Mat.BRICK, false);
        mb.rectH(XB - h + c, ThermenModel.PZ1 + 1.5, XB + h - c, ZA + h - c, Y_WATER, true, Mat.WATER);
        mb.maxEdge = keep;
        routes(water);
    }

    private static void routes(List<Route> water) {
        double y = Y_WATER + 0.12, cz = ThermenModel.CAL_Z;
        Route.Builder trunk = new Route.Builder("Aqua Antoniniana")
                .to(1150, y, ZA).to(XB, y, ZA).to(XB, y, ThermenModel.PZ1 + 1.6)
                .to(XB, 7.4, 176.5).to(40, 2.2, 168).to(14, 1.2, 160).to(0, 0.6, 156)
                .to(0, -1.2, 150).to(0, -1.2, 90);
        // Caldarium: die drei Wannen, jede über den Kessel ihres Praefuriums
        for (double deg : Hypocaust.FURNACE_DEG) {
            double a = Math.toRadians(deg), c = Math.cos(a), s = Math.sin(a);
            Route.Builder r = trunk.copy("Caldarium-Wanne");
            if (deg != 90) r.to(c * 30, -1.2, cz + s * 30 + 8);
            r.to(c * 26, 0.2, cz + s * 26).to(c * 22.0, 3.35, cz + s * 22.0).heatHere()
                    .to(c * 20.2, 3.2, cz + s * 20.2).to(c * 18.4, 1.6, cz + s * 18.4).to(c * 17.2, 1.42, cz + s * 17.2);
            water.add(r.build(1.0, Route.HOT));
        }
        // Labrum in der Mitte: vom mittleren Kessel unter dem Boden hindurch
        water.add(trunk.copy("Labrum").to(0, 0.2, cz + 26).to(0, 3.35, cz + 22).heatHere()
                .to(0, 1.0, cz + 19.5).to(0, -0.9, cz + 17).to(0, -0.9, cz + 0.4).to(0, 1.44, cz).build(0.8, Route.HOT));
        // Becken neben dem Tepidarium: lauwarm, über die seitlichen Kessel
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            double a = Math.toRadians(sgn < 0 ? 135 : 45), c = Math.cos(a), s = Math.sin(a);
            water.add(trunk.copy("Tepidarium-Becken").to(c * 30, -1.2, cz + s * 30 + 8).to(c * 26, 0.2, cz + s * 26)
                    .to(c * 22.0, 3.35, cz + s * 22.0).heatHere().to(c * 24, -1.2, cz + s * 24)
                    .to(sgn * 30, -1.2, 44).to(sgn * 30, -1.2, 25.5).to(sgn * 29.4, 0.2, 25.5).to(sgn * 28.15, 0.72, 25.5)
                    .build(0.8, Route.WARM));
        }
        // Kaltwasser: Frigidarium-Becken und Natatio, in zwei Strängen außen um die warmen Säle
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            Route.Builder cold = trunk.copy("Kaltwasser").to(sgn * 36, -1.2, 82).to(sgn * 36, -1.2, -24.9);
            water.add(cold.copy("Frigidarium-Becken").to(sgn * 20, -1.2, -24.9).to(sgn * 20, 0.2, -24.9)
                    .to(sgn * 20, 0.72, -23.75).build(1.0, Route.COLD));
            Route.Builder nat = cold.copy("Natatio").to(sgn * 36, -1.2, -53.3);
            for (double x : new double[]{6, 18}) {
                water.add(nat.copy("Natatio").to(sgn * x, -1.2, -53.3).to(sgn * x, 0.2, -52.2)
                        .to(sgn * x, 0.72, -51.3).build(1.5, Route.COLD));
            }
        }
    }
}
