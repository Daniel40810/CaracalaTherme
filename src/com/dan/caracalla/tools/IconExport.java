package com.dan.caracalla.tools;

import com.dan.caracalla.ui.AppIcon;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Schreibt das Programmsymbol als Windows-Symboldatei (.ico) mit allen Größen und als PNG.
 * Die Einträge sind PNG-komprimiert, das verstehen Windows ab Vista, NetBeans und Launch4j.
 * Aufruf: IconExport [ziel.ico]  (Vorgabe: caracalla.ico im Projektordner)
 */
public final class IconExport {
    public static void main(String[] a) throws Exception {
        String file = a.length > 0 ? a[0] : "caracalla.ico";
        List<byte[]> data = new ArrayList<>();
        for (int s : AppIcon.SIZES) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ImageIO.write(AppIcon.paint(s), "png", b);
            data.add(b.toByteArray());
        }
        try (DataOutputStream o = new DataOutputStream(new FileOutputStream(file))) {
            int n = AppIcon.SIZES.length;
            le16(o, 0); le16(o, 1); le16(o, n);                   // ICONDIR: reserviert, Typ 1 = Symbol, Anzahl
            int offset = 6 + 16 * n;
            for (int i = 0; i < n; i++) {
                int s = AppIcon.SIZES[i];
                o.writeByte(s >= 256 ? 0 : s);                     // Breite (0 = 256)
                o.writeByte(s >= 256 ? 0 : s);                     // Höhe
                o.writeByte(0);                                    // Palette
                o.writeByte(0);                                    // reserviert
                le16(o, 1);                                        // Farbebenen
                le16(o, 32);                                       // Bit je Pixel
                le32(o, data.get(i).length);
                le32(o, offset);
                offset += data.get(i).length;
            }
            for (byte[] d : data) o.write(d);
        }
        ImageIO.write(AppIcon.paint(256), "png", new java.io.File(file.replaceAll("\\.ico$", "") + "_256.png"));
        System.out.println("Geschrieben: " + file);
    }

    private static void le16(DataOutputStream o, int v) throws java.io.IOException { o.writeByte(v & 255); o.writeByte((v >> 8) & 255); }

    private static void le32(DataOutputStream o, int v) throws java.io.IOException { le16(o, v & 0xFFFF); le16(o, (v >>> 16) & 0xFFFF); }
}
