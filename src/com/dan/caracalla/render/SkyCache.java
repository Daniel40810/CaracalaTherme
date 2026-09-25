package com.dan.caracalla.render;

import com.dan.caracalla.geo.Mesh;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.zip.CRC32;

/**
 * Legt die vorberechnete Himmelssicht im Benutzerordner ab (~/.caracalla), damit der
 * zweite Start ohne Rechnen auskommt. Der Schlüssel ändert sich mit jeder Geometrieänderung.
 */
public final class SkyCache {
    private SkyCache() { }

    static File file(Mesh m, int rays) {
        CRC32 crc = new CRC32();
        byte[] b = new byte[4];
        for (int i = 0; i < m.pos.length; i += 7) {
            int v = Float.floatToIntBits(m.pos[i]);
            b[0] = (byte) v; b[1] = (byte) (v >> 8); b[2] = (byte) (v >> 16); b[3] = (byte) (v >> 24);
            crc.update(b);
        }
        String key = String.format("%08x-%d-%d-%d", crc.getValue(), m.nv, m.nt, rays);
        return new File(new File(System.getProperty("user.home"), ".caracalla"), "himmel2-" + key + ".bin");
    }

    /** Lädt die Himmelssicht; false, wenn nichts Passendes vorliegt. */
    public static boolean load(Mesh m, int rays) {
        File f = file(m, rays);
        if (!f.isFile()) return false;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)))) {
            int n = in.readInt();
            if (n != m.nv) return false;
            for (int i = 0; i < n; i++) m.sky[i] = in.readFloat();
            int nb = in.readInt();
            int[] ht = new int[n * nb];
            float[] hd = new float[n * nb];
            for (int i = 0; i < ht.length; i++) { ht[i] = in.readInt(); hd[i] = in.readFloat(); }
            m.bounceRays = nb; m.hitTri = ht; m.hitT = hd;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void save(Mesh m, int rays) {
        File f = file(m, rays);
        try {
            File dir = f.getParentFile();
            if (!dir.isDirectory() && !dir.mkdirs()) return;
            File[] old = dir.listFiles((d, name) -> name.startsWith("himmel") && name.endsWith(".bin"));
            if (old != null) for (File o : old) o.delete();
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) {
                out.writeInt(m.nv);
                for (int i = 0; i < m.nv; i++) out.writeFloat(m.sky[i]);
                out.writeInt(m.bounceRays);
                for (int i = 0; i < m.hitTri.length; i++) { out.writeInt(m.hitTri[i]); out.writeFloat(m.hitT[i]); }
            }
        } catch (Exception e) {
            // Zwischenspeicher ist nur eine Beschleunigung
        }
    }
}
