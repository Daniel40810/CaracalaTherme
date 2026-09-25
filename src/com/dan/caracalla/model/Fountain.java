package com.dan.caracalla.model;

/** Ein Wasserspeier oder eine Fontäne: Düse, Austrittsgeschwindigkeit, Streuung, Tropfen je Sekunde. */
public final class Fountain {
    public final double x, y, z, vx, vy, vz, spread, rate;

    public Fountain(double x, double y, double z, double vx, double vy, double vz, double spread, double rate) {
        this.x = x; this.y = y; this.z = z; this.vx = vx; this.vy = vy; this.vz = vz; this.spread = spread; this.rate = rate;
    }
}
