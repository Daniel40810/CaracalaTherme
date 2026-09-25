package com.dan.caracalla.tools;

import com.dan.caracalla.audio.SoundScape;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

/** Rechnet einige Sekunden Klang in eine WAV-Datei (zum Prüfen ohne Lautsprecher). */
public final class SoundTest {
    public static void main(String[] a) throws Exception {
        SoundScape s = new SoundScape();
        int sr = SoundScape.SR, secs = 16, n = 1024;
        float[] l = new float[n], r = new float[n];
        short[] pcm = new short[secs * sr * 2];
        int w = 0;
        double peak = 0, sum = 0;
        for (int b = 0; b * n < secs * sr; b++) {
            double t = b * n / (double) sr;
            if (t < 6) s.set(0.3f, 0.9f, -0.3f, 0.6f, 0, 0, 0, 0, 6.5f, 0.55f);        // Caldarium am Labrum
            else if (t < 11) s.set(0.2f, 0.1f, 0.4f, 0.1f, 0.8f, 0.5f, 0.5f, 0, 0.6f, 0.12f); // außen am Praefurnium
            else s.set(0, 0, 0, 0, 0, 0, 0.6f, 1, 0.5f, 0.08f);                          // heute: Wind und Vögel
            if (Math.abs(t - 3) < 0.01) s.splash(1, 0.2f);
            s.render(l, r, n);
            for (int i = 0; i < n && w < pcm.length; i++) {
                peak = Math.max(peak, Math.max(Math.abs(l[i]), Math.abs(r[i])));
                sum += l[i] * l[i] + r[i] * r[i];
                pcm[w++] = (short) (Math.max(-1, Math.min(1, l[i])) * 32767);
                pcm[w++] = (short) (Math.max(-1, Math.min(1, r[i])) * 32767);
            }
        }
        System.out.printf("Spitze %.3f, Effektivwert %.4f%n", peak, Math.sqrt(sum / w));
        try (DataOutputStream o = new DataOutputStream(new FileOutputStream(a[0]))) {
            int bytes = pcm.length * 2;
            o.writeBytes("RIFF"); o.writeInt(Integer.reverseBytes(36 + bytes)); o.writeBytes("WAVEfmt ");
            o.writeInt(Integer.reverseBytes(16)); o.writeShort(Short.reverseBytes((short) 1)); o.writeShort(Short.reverseBytes((short) 2));
            o.writeInt(Integer.reverseBytes(sr)); o.writeInt(Integer.reverseBytes(sr * 4)); o.writeShort(Short.reverseBytes((short) 4));
            o.writeShort(Short.reverseBytes((short) 16)); o.writeBytes("data"); o.writeInt(Integer.reverseBytes(bytes));
            for (short v : pcm) o.writeShort(Short.reverseBytes(v));
        }
    }
}
