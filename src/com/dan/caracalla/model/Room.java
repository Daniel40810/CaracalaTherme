package com.dan.caracalla.model;

/** Ein Saal oder Hof mit Ankunftsblick für die Innenkamera. */
public final class Room {
    public final String name;
    public final double[] eye, look;
    /** Rauminhalt: Quader {x0,y0,z0,x1,y1,z1} oder, wenn radius > 0, stehender Zylinder um (x0,z0). */
    final double[] box;
    final double radius;
    /** true für überdachte Säle (dort schaltet die Kamera beim Hineinzoomen um). */
    public final boolean roofed;

    Room(String name, double[] eye, double[] look, double[] box, double radius, boolean roofed) {
        this.name = name; this.eye = eye; this.look = look; this.box = box; this.radius = radius; this.roofed = roofed;
    }

    public boolean contains(double x, double y, double z) {
        if (box == null) return false;
        if (radius > 0) {
            double dx = x - box[0], dz = z - box[2];
            return dx * dx + dz * dz < radius * radius && y > box[1] && y < box[4];
        }
        return x > box[0] && x < box[3] && y > box[1] && y < box[4] && z > box[2] && z < box[5];
    }

    @Override public String toString() { return name; }
}
