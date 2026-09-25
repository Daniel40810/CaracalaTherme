package com.dan.caracalla.regie;

/** Eine Einstellung: Kamerafahrt, Sonnenstand von-bis, Dunst, Tafel. */
public final class Clip {
    public final CameraPath path;
    public final double dur;
    public double hour0 = Double.NaN, hour1 = Double.NaN;
    public double haze = Double.NaN;
    public String title, text;
    /** true: harter Schnitt mit kurzer Abblende vor dem Clip. */
    public boolean cut = true;

    public Clip(CameraPath path) {
        this.path = path;
        this.dur = path.duration();
    }

    public Clip sun(double h0, double h1) { hour0 = h0; hour1 = h1; return this; }
    public Clip haze(double h) { haze = h; return this; }
    public Clip caption(String title, String text) { this.title = title; this.text = text; return this; }
    public Clip noCut() { cut = false; return this; }
}
