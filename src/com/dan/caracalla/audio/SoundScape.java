package com.dan.caracalla.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Klang ohne Tondateien: alles wird laufend berechnet. Wasser besteht aus vielen kleinen
 * Blasen-Resonanzen (kurze, nach oben gleitende Sinustöne) über gefiltertem Rauschen, dazu
 * einzelne Tropfen, Feuerknistern an den Praefurnien, Wind im Freien und heute Vogelrufe.
 * Ein Hall nach Freeverb-Art bekommt seine Nachhallzeit vom Saal, in dem man steht:
 * unter der Kuppel des Caldariums klingt ein Tropfen viele Sekunden nach.
 */
public final class SoundScape implements Runnable {
    public static final int SR = 44100;

    // Zustand, den die Bildschleife setzt (gleitet im Klangthread weich nach)
    private volatile float tWater, tFountain, tFountainPan, tDrips, tFire, tFirePan, tWind, tBirds, tRt = 0.6f, tWet = 0.1f;
    private volatile float master = 0.7f;
    private volatile boolean enabled = true, running = true;
    private volatile int splashes;
    private volatile float splashGain, splashPan;

    private float gWater, gFountain, pFountain, gDrips, gFire, pFire, gWind, gBirds, rt = 0.6f, wet = 0.1f;

    private long seed = 0x9E3779B97F4A7C15L;
    private float lp1, lp2, lp3, wl1, wl2, fl1, bp1, bp2, windLfo;
    private double time;

    // Stimmen: Blasen, Tropfen, Vogelrufe
    private static final int NV = 96;
    private final float[] vFreq = new float[NV], vRise = new float[NV], vDecay = new float[NV], vAmp = new float[NV],
            vPhase = new float[NV], vAge = new float[NV], vPanL = new float[NV], vPanR = new float[NV], vWet = new float[NV];
    private final int[] vKind = new int[NV];
    private final boolean[] vOn = new boolean[NV];

    private final Reverb reverb = new Reverb();

    public void setEnabled(boolean on) { enabled = on; }
    public boolean enabled() { return enabled; }
    public void setMaster(float m) { master = m; }

    /**
     * Neue Lage des Hörers. Gains 0..1, pan -1 (links) .. 1 (rechts), rt = Nachhallzeit in s,
     * wetMix = Anteil Hall.
     */
    public void set(float water, float fountain, float fountainPan, float drips, float fire, float firePan,
                    float wind, float birds, float rt60, float wetMix) {
        tWater = water; tFountain = fountain; tFountainPan = fountainPan; tDrips = drips; tFire = fire; tFirePan = firePan;
        tWind = wind; tBirds = birds; tRt = rt60; tWet = wetMix;
    }

    /** Ein Stein fällt ins Becken. */
    public void splash(float gain, float pan) {
        splashGain = gain; splashPan = pan;
        splashes++;
    }

    public void stop() { running = false; }

    /** Startet die Ausgabe in einem eigenen Thread; ohne Tonausgabe bleibt es still. */
    public static SoundScape start() {
        SoundScape s = new SoundScape();
        Thread t = new Thread(s, "Caracalla-Klang");
        t.setDaemon(true);
        t.start();
        return s;
    }

    @Override
    public void run() {
        SourceDataLine line;
        try {
            AudioFormat fmt = new AudioFormat(SR, 16, 2, true, false);
            line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, 4096 * 4);
            line.start();
        } catch (Throwable e) {
            System.err.println("Kein Tonausgang: " + e.getMessage());
            return;
        }
        int n = 1024;
        float[] l = new float[n], r = new float[n];
        byte[] buf = new byte[n * 4];
        while (running) {
            render(l, r, n);
            for (int i = 0; i < n; i++) {
                int a = (int) (Math.max(-1, Math.min(1, l[i])) * 32767), b = (int) (Math.max(-1, Math.min(1, r[i])) * 32767);
                buf[4 * i] = (byte) a; buf[4 * i + 1] = (byte) (a >> 8);
                buf[4 * i + 2] = (byte) b; buf[4 * i + 3] = (byte) (b >> 8);
            }
            line.write(buf, 0, buf.length);
        }
        line.drain();
        line.close();
    }

    // ------------------------------------------------------------ Klangerzeugung

    private float rnd() {
        seed ^= seed << 13; seed ^= seed >>> 7; seed ^= seed << 17;
        return (seed >>> 40) / (float) (1L << 24);
    }

    private float noise() { return rnd() * 2 - 1; }

    private int lastSplash;

    /** Füllt n Proben links und rechts. */
    public void render(float[] outL, float[] outR, int n) {
        float dt = 1f / SR;
        // Parameter gleiten über den Block
        float k = 1 - (float) Math.exp(-n * dt / 0.25f);
        float on = enabled ? 1 : 0;
        gWater += (tWater * on - gWater) * k; gFountain += (tFountain * on - gFountain) * k; pFountain += (tFountainPan - pFountain) * k;
        gDrips += (tDrips * on - gDrips) * k; gFire += (tFire * on - gFire) * k; pFire += (tFirePan - pFire) * k;
        gWind += (tWind * on - gWind) * k; gBirds += (tBirds * on - gBirds) * k;
        rt += (tRt - rt) * k; wet += (tWet - wet) * k;
        reverb.setRt(rt);
        int sp = splashes;
        if (sp != lastSplash) {
            lastSplash = sp;
            if (enabled) splashBurst(splashGain, splashPan);
        }
        float bubbleRate = (gFountain * 520 + gWater * 90) * dt;
        float dripRate = gDrips * 1.3f * dt;
        float crackRate = gFire * 14 * dt;
        float birdRate = gBirds * 0.5f * dt;
        float fpL = (float) Math.cos((pFountain + 1) * Math.PI / 4), fpR = (float) Math.sin((pFountain + 1) * Math.PI / 4);
        float xpL = (float) Math.cos((pFire + 1) * Math.PI / 4), xpR = (float) Math.sin((pFire + 1) * Math.PI / 4);
        for (int i = 0; i < n; i++) {
            time += dt;
            // Ereignisse
            if (rnd() < bubbleRate) bubble(pFountain + (rnd() - 0.5f) * 0.9f, 0.12f + 0.25f * rnd() * rnd(), 0.35f);
            if (rnd() < dripRate) drip();
            if (rnd() < birdRate) bird();
            float l = 0, r = 0, send = 0;
            // Rauschbett des Wassers: rosa-ähnlich, bandbegrenzt
            float w = noise();
            lp1 += (w - lp1) * 0.22f;
            lp2 += (lp1 - lp2) * 0.06f;
            float bed = (lp1 - lp2) * (gWater * 0.22f + gFountain * 0.55f);
            // plätschernde Modulation
            float mod = 0.7f + 0.3f * (float) Math.sin(time * 7.3 + Math.sin(time * 2.1) * 2);
            bed *= mod;
            l += bed * (0.6f + 0.4f * fpL); r += bed * (0.6f + 0.4f * fpR);
            send += bed * 0.5f;
            // Wind: tiefes Rauschen mit langsam wanderndem Filter
            windLfo += dt * 0.23f;
            float cut = 0.004f + 0.012f * (0.5f + 0.5f * (float) Math.sin(windLfo * 6.283f + Math.sin(windLfo * 2.7f) * 1.7f));
            wl1 += (noise() - wl1) * cut;
            wl2 += (wl1 - wl2) * cut;
            float wind = wl2 * gWind * 2.2f;
            l += wind * 1.05f; r += wind * 0.95f;
            // Feuer: Grollen und Knistern
            if (gFire > 0.001f) {
                fl1 += (noise() - fl1) * 0.012f;
                float roar = fl1 * gFire * 1.4f;
                if (rnd() < crackRate) crackle(pFire);
                l += roar * xpL; r += roar * xpR;
                send += roar * 0.3f;
            }
            // Stimmen
            for (int v = 0; v < NV; v++) {
                if (!vOn[v]) continue;
                float a = vAge[v];
                if (a < 0) { vAge[v] = a + dt; continue; }
                float env = (float) Math.exp(-a / vDecay[v]);
                float y;
                if (vKind[v] == 2) {
                    // Knacken: gefiltertes Rauschen
                    bp1 += (noise() - bp1) * 0.5f;
                    y = bp1 * env * vAmp[v];
                } else {
                    float f = vFreq[v] * (1 + vRise[v] * a);
                    vPhase[v] += f * dt;
                    if (vPhase[v] > 1) vPhase[v] -= 1;
                    float att = Math.min(1, a / 0.0015f);
                    y = (float) Math.sin(vPhase[v] * 6.2831853f) * env * att * vAmp[v];
                }
                l += y * vPanL[v]; r += y * vPanR[v];
                send += y * vWet[v];
                vAge[v] = a + dt;
                if (env < 0.001f) vOn[v] = false;
            }
            // Hall
            float[] rv = reverb.process(send);
            l += rv[0] * wet; r += rv[1] * wet;
            outL[i] = soft(l * master);
            outR[i] = soft(r * master);
        }
    }

    private static float soft(float x) {
        if (x > 1.5f) return 1; if (x < -1.5f) return -1;
        return x - x * x * x / 6.75f;
    }

    private int free() {
        for (int v = 0; v < NV; v++) if (!vOn[v]) return v;
        int best = 0;
        for (int v = 1; v < NV; v++) if (vAge[v] > vAge[best]) best = v;
        return best;
    }

    private void voice(int kind, float freq, float rise, float decay, float amp, float pan, float wetSend) {
        int v = free();
        vOn[v] = true; vKind[v] = kind; vFreq[v] = freq; vRise[v] = rise; vDecay[v] = decay; vAmp[v] = amp;
        vPhase[v] = 0; vAge[v] = 0;
        pan = Math.max(-1, Math.min(1, pan));
        vPanL[v] = (float) Math.cos((pan + 1) * Math.PI / 4); vPanR[v] = (float) Math.sin((pan + 1) * Math.PI / 4);
        vWet[v] = wetSend;
    }

    /** Blasenresonanz: je kleiner die Blase, desto höher und kürzer; der Ton steigt beim Aufsteigen. */
    private void bubble(float pan, float amp, float wetSend) {
        float rad = 0.6f + 3.4f * rnd() * rnd();          // mm
        float f = 3260f / rad;                             // Minnaert
        voice(0, f, 3.5f + 6 * rnd(), 0.0045f + 0.0012f * rad, amp * (0.3f + 0.3f * rad / 4), pan, wetSend);
    }

    private void drip() {
        float f = 850 + 900 * rnd();
        voice(0, f, 7 + 6 * rnd(), 0.02f + 0.03f * rnd(), 0.22f + 0.2f * rnd(), (rnd() - 0.5f) * 1.6f, 1.0f);
    }

    private void crackle(float pan) {
        voice(2, 0, 0, 0.0015f + 0.004f * rnd(), 0.35f + 0.6f * rnd() * rnd(), pan + (rnd() - 0.5f) * 0.4f, 0.4f);
    }

    private void bird() {
        float pan = (rnd() - 0.5f) * 1.8f;
        int notes = 2 + (int) (rnd() * 4);
        float base = 2600 + 1800 * rnd();
        for (int i = 0; i < notes; i++) {
            // Töne nacheinander: Alter negativ = späterer Einsatz
            int v = free();
            voice(1, base * (1 + 0.15f * (rnd() - 0.5f)), -1.5f, 0.045f, 0.05f, pan, 0.2f);
            vAge[v] = -i * 0.11f;
        }
    }

    private void splashBurst(float gain, float pan) {
        for (int i = 0; i < 40; i++) {
            int v = free();
            float rad = 0.8f + 4 * rnd();
            voice(0, 3260f / rad, 4 + 5 * rnd(), 0.006f + 0.002f * rad, gain * (0.25f + 0.35f * rnd()), pan + (rnd() - 0.5f) * 0.5f, 0.6f);
            vAge[v] = -0.35f * rnd() * rnd();
        }
        for (int i = 0; i < 6; i++) {
            int v = free();
            voice(2, 0, 0, 0.03f + 0.05f * rnd(), gain * 0.5f, pan, 0.6f);
            vAge[v] = -0.02f * i;
        }
    }

    // ------------------------------------------------------------ Hall

    /** Hall nach dem Freeverb-Schema: acht gedämpfte Kammfilter und vier Allpässe je Seite. */
    static final class Reverb {
        private static final int[] COMB = {1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617};
        private static final int[] AP = {556, 441, 341, 225};
        private static final int SPREAD = 23;
        private final float[][] cb = new float[16][], ab = new float[8][];
        private final int[] ci = new int[16], ai = new int[8];
        private final float[] cs = new float[16], fb = new float[16];
        private final float[] pre = new float[SR / 5];
        private int pi;
        private int preLen = SR / 50;
        private final float[] out = new float[2];
        private float damp = 0.3f;

        Reverb() {
            for (int c = 0; c < 8; c++) {
                cb[c] = new float[(int) (COMB[c] * 1.6)];
                cb[c + 8] = new float[(int) ((COMB[c] + SPREAD) * 1.6)];
            }
            for (int a = 0; a < 4; a++) {
                ab[a] = new float[AP[a]];
                ab[a + 4] = new float[AP[a] + SPREAD];
            }
            setRt(1);
        }

        /** Nachhallzeit (Abfall um 60 dB) in Sekunden; große Säle auch mit längeren Wegen. */
        void setRt(float rt) {
            float size = Math.min(1.6f, 1 + Math.max(0, rt - 1) * 0.12f);
            for (int c = 0; c < 16; c++) {
                int d = (int) ((c < 8 ? COMB[c] : COMB[c - 8] + SPREAD) * size);
                d = Math.min(d, cb[c].length);
                lens[c] = d;
                fb[c] = (float) Math.pow(10, -3.0 * d / (SR * Math.max(0.2, rt)));
            }
            damp = rt > 3 ? 0.18f : 0.32f;
            preLen = (int) (SR * Math.min(0.08, 0.012 + rt * 0.01));
        }

        private final int[] lens = new int[16];

        float[] process(float x) {
            pre[pi] = x;
            int ri = pi - preLen;
            if (ri < 0) ri += pre.length;
            float in = pre[ri] * 0.03f;
            pi = (pi + 1) % pre.length;
            float l = 0, r = 0;
            for (int c = 0; c < 16; c++) {
                float[] b = cb[c];
                int i = ci[c];
                float y = b[i];
                cs[c] = y * (1 - damp) + cs[c] * damp;
                b[i] = in + cs[c] * fb[c];
                ci[c] = (i + 1) % lens[c];
                if (c < 8) l += y; else r += y;
            }
            for (int a = 0; a < 8; a++) {
                float[] b = ab[a];
                int i = ai[a];
                float bo = b[i];
                float v = a < 4 ? l : r;
                float y = -v + bo;
                b[i] = v + bo * 0.5f;
                ai[a] = (i + 1) % b.length;
                if (a < 4) l = y; else r = y;
            }
            out[0] = l; out[1] = r;
            return out;
        }
    }
}
