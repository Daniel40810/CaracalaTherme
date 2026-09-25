package com.dan.caracalla.tools;

import com.dan.caracalla.model.ThermenModel;
import com.dan.caracalla.regie.Director;
import com.dan.caracalla.regie.Programme;
import com.dan.caracalla.regie.Timeline;
import com.dan.caracalla.render.Bvh;
import com.dan.caracalla.render.Camera;
import com.dan.caracalla.render.Renderer;
import com.dan.caracalla.render.SkyCache;
import com.dan.caracalla.render.SteamSim;
import com.dan.caracalla.render.Sun;
import com.dan.caracalla.render.WaterSim;

import javax.imageio.ImageIO;
import java.io.File;

/**
 * Rechnet Einzelbilder aus einer Fahrt, dem Rundgang oder dem Drehbuch ohne Fenster.
 * Aufruf: ScriptFrames ordner breite höhe (drehbuch|rundgang|fahrt0|fahrt1|fahrt2) zeit1 zeit2 …
 */
public final class ScriptFrames {
    public static void main(String[] a) throws Exception {
        File dir = new File(a[0]);
        dir.mkdirs();
        int w = Integer.parseInt(a[1]), h = Integer.parseInt(a[2]);
        Timeline tl = a[3].equals("drehbuch") ? Programme.dayScript() : a[3].equals("rundgang") ? Programme.tour()
                : Programme.flights().get(Integer.parseInt(a[3].substring(5)));
        ThermenModel m = ThermenModel.build();
        if (!SkyCache.load(m.mesh, 48)) { Bvh.bakeSky(m.mesh, 48, null); SkyCache.save(m.mesh, 48); }
        Renderer r = new Renderer(m.mesh, 4096);
        r.setSize(w, h);
        WaterSim ws = new WaterSim(m.waters, m.fountains);
        ws.warmup(3);
        r.water = ws;
        SteamSim ss = new SteamSim(m.waters);
        ss.warmup(70);
        r.steam = ss;
        int day = tl.day > 0 ? tl.day : 172;
        Director d = new Director();
        Camera cam = new Camera();
        Sun sun = new Sun();
        for (int i = 4; i < a.length; i++) {
            double t = Double.parseDouble(a[i]);
            d.play(tl);
            d.seek(t / tl.duration());
            d.update(0.001, cam);
            double hh = Double.isNaN(d.hour()) ? 15.5 : d.hour();
            double hz = Double.isNaN(d.haze()) ? 0 : d.haze();
            sun.set(day, hh);
            ss.setHour(hh);
            ss.setHaze(hz);
            r.setSun(sun.dir, hz);
            r.resetExposure();
            r.render(cam, t, 0);
            java.awt.image.BufferedImage img = r.render(cam, t, 0);
            String name = String.format("%s_%05.1f.png", a[3], t);
            ImageIO.write(img, "png", new File(dir, name));
            System.out.printf("%s  %s  Sonne %s%n", name, d.clip() == null ? "-" : d.clip().title, Sun.timeLabel(hh));
        }
    }
}
