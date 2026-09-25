package com.dan.caracalla.regie;

import com.dan.caracalla.render.Camera;

/** Spielt eine Zeitleiste ab: Kamera, Sonne, Dunst, Tafeln und Abblenden an Schnitten. */
public final class Director {
    private volatile Timeline tl;
    private double t;
    private int clipIndex;
    private double clipStart;

    public synchronized void play(Timeline timeline) { tl = timeline; t = 0; locate(); }

    public synchronized void stop() { tl = null; }

    public boolean active() { return tl != null; }

    public synchronized Timeline timeline() { return tl; }

    public synchronized double time() { return t; }

    public synchronized void seek(double fraction) {
        if (tl == null) return;
        t = Math.max(0, Math.min(0.999, fraction)) * tl.duration();
        locate();
    }

    private void locate() {
        clipIndex = 0; clipStart = 0;
        if (tl == null) return;
        while (clipIndex < tl.clips.size() - 1 && clipStart + tl.clips.get(clipIndex).dur <= t) {
            clipStart += tl.clips.get(clipIndex).dur;
            clipIndex++;
        }
    }

    /** Einen Schritt weiter; liefert false, wenn die Zeitleiste zu Ende ist (Kamera bleibt am letzten Bild). */
    public synchronized boolean update(double dt, Camera cam) {
        if (tl == null) return false;
        t += dt;
        double total = tl.duration();
        if (t >= total) {
            Clip last = tl.clips.get(tl.clips.size() - 1);
            last.path.eval(last.dur, cam);
            tl = null;
            return false;
        }
        locate();
        Clip c = tl.clips.get(clipIndex);
        c.path.eval(t - clipStart, cam);
        return true;
    }

    public synchronized Clip clip() { return tl == null ? null : tl.clips.get(clipIndex); }

    public synchronized double clipTime() { return t - clipStart; }

    /** Sonnenstand der laufenden Einstellung oder NaN. */
    public synchronized double hour() {
        Clip c = clip();
        if (c == null || Double.isNaN(c.hour0)) return Double.NaN;
        double u = Math.max(0, Math.min(1, (t - clipStart) / c.dur));
        return c.hour0 + (c.hour1 - c.hour0) * u;
    }

    public synchronized double haze() {
        Clip c = clip();
        return c == null ? Double.NaN : c.haze;
    }

    /** Abblende an harten Schnitten (0..1). */
    public synchronized double fade() {
        if (tl == null) return 0;
        Clip c = tl.clips.get(clipIndex);
        double lt = t - clipStart;
        double f = 0;
        if (c.cut && clipIndex > 0 && lt < 0.45) f = 1 - lt / 0.45;
        if (clipIndex + 1 < tl.clips.size() && tl.clips.get(clipIndex + 1).cut && c.dur - lt < 0.45) f = Math.max(f, 1 - (c.dur - lt) / 0.45);
        if (clipIndex == 0 && lt < 0.6) f = Math.max(f, 1 - lt / 0.6);
        return f;
    }

    /** Sichtbarkeit der Tafel (0..1). */
    public synchronized double captionAlpha() {
        Clip c = clip();
        if (c == null || c.title == null) return 0;
        double lt = t - clipStart;
        return Math.max(0, Math.min(1, Math.min((lt - 0.6) / 0.9, (c.dur - lt - 0.3) / 0.9)));
    }
}
