package com.dan.caracalla.tools;

import com.dan.caracalla.model.Room;
import com.dan.caracalla.model.ThermenModel;
import com.dan.caracalla.render.Bvh;
import com.dan.caracalla.render.Camera;
import com.dan.caracalla.render.Renderer;
import com.dan.caracalla.render.Sun;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Rechnet Standbilder ohne Fenster. Aufruf:
 * StillRender ausgabeOrdner breite höhe tag uhrzeit [dach=0|1] ansicht...
 * Ansicht: Raumname, "orbit:yawGrad:nickGrad:abstand:zx:zy:zz" oder "eye:x:y:z:lx:ly:lz".
 */
public final class StillRender {
    public static void main(String[] a) throws Exception {
        File dir = new File(a[0]);
        dir.mkdirs();
        int w = Integer.parseInt(a[1]), h = Integer.parseInt(a[2]);
        int day = Integer.parseInt(a[3]);
        double hour = Double.parseDouble(a[4]);
        boolean roof = !a[5].equals("0");
        long t0 = System.nanoTime();
        ThermenModel model = ThermenModel.build();
        long t1 = System.nanoTime();
        System.out.printf("Modell: %d Ecken, %d Dreiecke, %d Blöcke, %.1f s%n", model.mesh.nv, model.mesh.nt,
                model.mesh.nChunks, (t1 - t0) / 1e9);
        if (!com.dan.caracalla.render.SkyCache.load(model.mesh, 48)) {
            Bvh.bakeSky(model.mesh, 48, null);
            com.dan.caracalla.render.SkyCache.save(model.mesh, 48);
        }
        long t2 = System.nanoTime();
        System.out.printf("Himmelssicht: %.1f s%n", (t2 - t1) / 1e9);
        Renderer r = new Renderer(model.mesh, 4096);
        r.showRoof = roof;
        if (System.getProperty("cut") != null) { r.cut = true; r.cutX = Float.parseFloat(System.getProperty("cut")); }
        r.ruin = Float.parseFloat(System.getProperty("ruin", "0"));
        com.dan.caracalla.render.Ruin.prepare();
        r.thermo = Boolean.getBoolean("thermo");
        r.furnaces = model.furnaces;
        r.hour = hour; r.dayOfYear = day;
        String flow = System.getProperty("flow", "");
        if (flow.contains("water")) r.waterFlow = new com.dan.caracalla.render.Streams(model.waterRoutes, 9000, 14, 0.22f);
        if (flow.contains("heat")) r.heatFlow = new com.dan.caracalla.render.Streams(model.heatRoutes, 2600, 2.4f, 0.12f);
        Sun sun = new Sun();
        sun.set(day, hour);
        r.setSun(sun.dir, Double.parseDouble(System.getProperty("haze", "0")));
        System.out.printf("Sonne: Höhe %.1f°, Azimut %.1f°, lokal (%.2f %.2f %.2f); Schatten %.1f s%n",
                sun.elevationDeg, sun.azimuthDeg, sun.dir[0], sun.dir[1], sun.dir[2], (System.nanoTime() - t2) / 1e9);
        r.setSize(w, h);
        com.dan.caracalla.render.WaterSim ws = new com.dan.caracalla.render.WaterSim(model.waters, model.fountains);
        ws.warmup(4);
        r.water = ws;
        com.dan.caracalla.render.SteamSim ss = new com.dan.caracalla.render.SteamSim(model.waters);
        ss.setHour(hour);
        ss.setHaze(Double.parseDouble(System.getProperty("haze", "0")));
        ss.warmup(Double.parseDouble(System.getProperty("steamWarm", "80")));
        r.steam = r.ruin > 0.3f ? null : ss;
        for (int i = 6; i < a.length; i++) {
            Camera c = new Camera();
            String v = a[i];
            String name = v.replace(':', '_').replace(' ', '_');
            if (v.startsWith("orbit:")) {
                String[] q = v.split(":");
                double yaw = Math.toRadians(Double.parseDouble(q[1])), pitch = Math.toRadians(Double.parseDouble(q[2]));
                double dist = Double.parseDouble(q[3]);
                double tx = Double.parseDouble(q[4]), ty = Double.parseDouble(q[5]), tz = Double.parseDouble(q[6]);
                c.ex = tx + dist * Math.cos(pitch) * Math.sin(yaw);
                c.ey = ty + dist * Math.sin(pitch);
                c.ez = tz + dist * Math.cos(pitch) * Math.cos(yaw);
                c.lookAt(tx, ty, tz);
            } else if (v.startsWith("eye:")) {
                String[] q = v.split(":");
                c.ex = Double.parseDouble(q[1]); c.ey = Double.parseDouble(q[2]); c.ez = Double.parseDouble(q[3]);
                c.lookAt(Double.parseDouble(q[4]), Double.parseDouble(q[5]), Double.parseDouble(q[6]));
            } else {
                Room room = null;
                for (Room rm : model.rooms) if (rm.name.startsWith(v)) room = rm;
                if (room == null || room.eye == null) { System.out.println("Unbekannt: " + v); continue; }
                c.ex = room.eye[0]; c.ey = room.eye[1]; c.ez = room.eye[2];
                c.lookAt(room.look[0], room.look[1], room.look[2]);
                name = room.name.replace(' ', '_');
            }
            r.resetExposure();
            long s = System.nanoTime();
            BufferedImage img = r.render(c, 3.0, 0);
            img = r.render(c, 3.0, 0);
            System.out.printf("%s: %.0f ms, Belichtung %.2f%n", name, (System.nanoTime() - s) / 2e6, r.exposure());
            ImageIO.write(img, "png", new File(dir, name + ".png"));
        }
    }
}
