package com.dan.caracalla.regie;

import java.util.ArrayList;
import java.util.List;

/** Folge von Einstellungen: eine Kamerafahrt, der Rundgang oder das Drehbuch. */
public final class Timeline {
    public final String name;
    public final List<Clip> clips = new ArrayList<>();
    /** Tag im Jahr, den das Drehbuch setzt (0 = unverändert). */
    public int day;
    /** Beim Abspielen den Wasserweg einblenden. */
    public boolean waterPath;

    public Timeline(String name) { this.name = name; }

    public Timeline add(Clip c) { clips.add(c); return this; }

    public double duration() {
        double d = 0;
        for (Clip c : clips) d += c.dur;
        return d;
    }

    @Override public String toString() { return name; }
}
