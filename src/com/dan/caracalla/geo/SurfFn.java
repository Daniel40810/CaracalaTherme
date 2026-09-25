package com.dan.caracalla.geo;

/** Parametrische Fläche: liefert zu (u,v) Punkt und Normale. */
@FunctionalInterface
public interface SurfFn {
    void eval(double u, double v, double[] p, double[] n);
}
