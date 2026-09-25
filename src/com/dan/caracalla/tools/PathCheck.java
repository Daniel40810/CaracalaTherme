package com.dan.caracalla.tools;

import com.dan.caracalla.model.ThermenModel;
import com.dan.caracalla.regie.Clip;
import com.dan.caracalla.regie.Programme;
import com.dan.caracalla.regie.Timeline;
import com.dan.caracalla.render.Bvh;

import java.util.ArrayList;
import java.util.List;

/** Prüft alle Kamerafahrten: Die Kamera darf keine Fläche durchqueren und keiner zu nahe kommen. */
public final class PathCheck {
    public static void main(String[] a) {
        ThermenModel m = ThermenModel.build();
        Bvh bvh = new Bvh(m.mesh);
        List<Timeline> all = new ArrayList<>(Programme.flights());
        all.add(Programme.tour());
        all.add(Programme.dayScript());
        int[] stack = new int[128];
        float[] tt = new float[1];
        float[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        int problems = 0;
        for (Timeline tl : all) {
            System.out.printf("%s: %.0f s%n", tl.name, tl.duration());
            int ci = 0;
            for (Clip c : tl.clips) {
                double[] prev = null, o = new double[6];
                for (double t = 0; t <= c.dur + 1e-9; t += 0.04) {
                    c.path.eval(t, o);
                    if (prev != null) {
                        float dx = (float) (o[0] - prev[0]), dy = (float) (o[1] - prev[1]), dz = (float) (o[2] - prev[2]);
                        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (len > 1e-4) {
                            int hit = bvh.closest((float) prev[0], (float) prev[1], (float) prev[2], dx / len, dy / len, dz / len, len, stack, tt);
                            if (hit >= 0) {
                                System.out.printf("  Clip %d t=%.2f DURCHQUERT bei (%.1f %.1f %.1f)%n", ci, t, o[0], o[1], o[2]);
                                problems++;
                            }
                        }
                    }
                    for (float[] d : dirs) {
                        int hit = bvh.closest((float) o[0], (float) o[1], (float) o[2], d[0], d[1], d[2], 0.25f, stack, tt);
                        if (hit >= 0 && t > 0.3 && t < c.dur - 0.3) {
                            System.out.printf("  Clip %d t=%.2f zu nah (%.2f m) bei (%.1f %.1f %.1f)%n", ci, t, tt[0], o[0], o[1], o[2]);
                            problems++;
                            break;
                        }
                    }
                    prev = o.clone();
                }
                ci++;
            }
        }
        System.out.println("Probleme: " + problems);
    }
}
