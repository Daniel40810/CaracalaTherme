package com.dan.caracalla.render;

/** Lochkamera: Augpunkt, Blickrichtung (Gier/Nick) und Öffnungswinkel. */
public final class Camera {
    public double ex, ey, ez;
    public double yaw, pitch;          // Bogenmaß; yaw 0 blickt nach -z
    public double fovY = Math.toRadians(58);
    public double near = 0.15;

    // Basis (nach update())
    public double fx, fy, fz, rx, ry, rz, ux, uy, uz;

    public void update() {
        double cp = Math.cos(pitch);
        fx = -Math.sin(yaw) * cp; fy = Math.sin(pitch); fz = -Math.cos(yaw) * cp;
        rx = -fz; ry = 0; rz = fx;
        double l = Math.sqrt(rx * rx + rz * rz);
        if (l < 1e-9) { rx = 1; rz = 0; l = 1; }
        rx /= l; rz /= l;
        ux = ry * fz - rz * fy; uy = rz * fx - rx * fz; uz = rx * fy - ry * fx;
    }

    /** Richtet die Kamera von (ex,ey,ez) auf den Punkt (tx,ty,tz) aus. */
    public void lookAt(double tx, double ty, double tz) {
        double dx = tx - ex, dy = ty - ey, dz = tz - ez;
        double h = Math.sqrt(dx * dx + dz * dz);
        yaw = Math.atan2(-dx, -dz);
        pitch = Math.atan2(dy, h);
        update();
    }

    public void copyFrom(Camera c) {
        ex = c.ex; ey = c.ey; ez = c.ez; yaw = c.yaw; pitch = c.pitch; fovY = c.fovY; near = c.near;
        update();
    }
}
