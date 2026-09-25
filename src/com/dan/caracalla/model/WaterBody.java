package com.dan.caracalla.model;

/** Ein Becken: Umriss (Rechteck, Kreisringausschnitt oder Scheibe), Wasserspiegel und Boden. */
public final class WaterBody {
    public enum Shape { RECT, SECTOR, DISC }

    public final String name;
    public final Shape shape;
    /** RECT: x0,z0,x1,z1. SECTOR: cx,cz,r0,r1,a0Deg,a1Deg. DISC: cx,cz,r. */
    public final double[] p;
    public final double waterY, floorY;

    public WaterBody(String name, Shape shape, double waterY, double floorY, double... p) {
        this.name = name; this.shape = shape; this.waterY = waterY; this.floorY = floorY; this.p = p;
    }

    public boolean contains(double x, double z) {
        switch (shape) {
            case RECT: return x >= p[0] && x <= p[2] && z >= p[1] && z <= p[3];
            case DISC: { double dx = x - p[0], dz = z - p[1]; return dx * dx + dz * dz <= p[2] * p[2]; }
            default: {
                double dx = x - p[0], dz = z - p[1], r = Math.hypot(dx, dz);
                if (r < p[2] || r > p[3]) return false;
                double a = Math.toDegrees(Math.atan2(dz, dx));
                double a0 = p[4], a1 = p[5];
                while (a < a0) a += 360;
                while (a > a0 + 360) a -= 360;
                return a <= a1;
            }
        }
    }

    /** Umschließendes Rechteck {x0,z0,x1,z1}. */
    public double[] bounds() {
        switch (shape) {
            case RECT: return new double[]{p[0], p[1], p[2], p[3]};
            case DISC: return new double[]{p[0] - p[2], p[1] - p[2], p[0] + p[2], p[1] + p[2]};
            default: {
                double x0 = 1e9, z0 = 1e9, x1 = -1e9, z1 = -1e9;
                for (int i = 0; i <= 16; i++) {
                    double a = Math.toRadians(p[4] + (p[5] - p[4]) * i / 16);
                    for (double r : new double[]{p[2], p[3]}) {
                        double x = p[0] + r * Math.cos(a), z = p[1] + r * Math.sin(a);
                        x0 = Math.min(x0, x); z0 = Math.min(z0, z); x1 = Math.max(x1, x); z1 = Math.max(z1, z);
                    }
                }
                return new double[]{x0, z0, x1, z1};
            }
        }
    }

    /** Nächster Punkt im Becken (für den Blick durchs Wasser an die Wände). */
    public void clamp(double[] xz) {
        if (contains(xz[0], xz[1])) return;
        switch (shape) {
            case RECT:
                xz[0] = Math.max(p[0], Math.min(p[2], xz[0]));
                xz[1] = Math.max(p[1], Math.min(p[3], xz[1]));
                return;
            case DISC: {
                double dx = xz[0] - p[0], dz = xz[1] - p[1], r = Math.hypot(dx, dz);
                xz[0] = p[0] + dx / r * p[2] * 0.999; xz[1] = p[1] + dz / r * p[2] * 0.999;
                return;
            }
            default: {
                double dx = xz[0] - p[0], dz = xz[1] - p[1];
                double r = Math.max(p[2], Math.min(p[3], Math.hypot(dx, dz)));
                double a = Math.toDegrees(Math.atan2(dz, dx));
                while (a < p[4] - 180) a += 360;
                while (a > p[4] + 180) a -= 360;
                a = Math.toRadians(Math.max(p[4], Math.min(p[5], a)));
                xz[0] = p[0] + r * Math.cos(a); xz[1] = p[1] + r * Math.sin(a);
            }
        }
    }
}
