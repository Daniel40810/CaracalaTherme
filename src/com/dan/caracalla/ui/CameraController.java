package com.dan.caracalla.ui;

import com.dan.caracalla.model.Room;
import com.dan.caracalla.render.Camera;

import java.util.List;

/**
 * Kamerasteuerung: Außen kreist die Kamera um einen Zielpunkt (Orbit mit Zoom),
 * innen bewegt sie sich frei wie ein Besucher. Wer in eine überdachte Halle
 * hineinzoomt, wechselt automatisch in die Innenkamera.
 */
public final class CameraController {
    public enum Mode { ORBIT, WALK }

    private Mode mode = Mode.ORBIT;
    private final List<Room> rooms;

    // Orbit: Ziel- und aktuelle Werte (weich nachgeführt)
    private double gtx = 0, gty = 12, gtz = 6, gyaw = Math.toRadians(35), gpitch = Math.toRadians(21), gdist = 270;
    private double tx = gtx, ty = gty, tz = gtz, yaw = gyaw, pitch = gpitch, dist = 520;
    private boolean armed = true;

    // Innen
    private double wx, wy, wz, wyaw, wpitch, gwyaw, gwpitch;
    private final boolean[] keys = new boolean[8]; // W A S D Q E Shift, frei
    public static final int K_W = 0, K_A = 1, K_S = 2, K_D = 3, K_Q = 4, K_E = 5, K_SHIFT = 6;

    public volatile boolean autoOrbit;
    public volatile boolean roofVisible = true;

    // Überblendung beim Raumwechsel
    private double fade, fadeDir;
    private Runnable pending;
    private String place = "Außen";

    public CameraController(List<Room> rooms) {
        this.rooms = rooms;
    }

    public synchronized Mode mode() { return mode; }
    public synchronized String place() { return place; }
    public synchronized double fade() { return fade; }

    public synchronized void setKey(int k, boolean down) { if (k >= 0 && k < keys.length) keys[k] = down; }

    public synchronized void releaseKeys() { java.util.Arrays.fill(keys, false); }

    // ------------------------------------------------------------ Eingaben

    public synchronized void drag(double dx, double dy, boolean pan) {
        if (mode == Mode.ORBIT) {
            if (pan) {
                double k = gdist * 0.0016;
                double sx = Math.cos(gyaw), sz = -Math.sin(gyaw);
                double fx = -Math.sin(gyaw), fz = -Math.cos(gyaw);
                gtx += (-dx * sx + dy * fx) * k;
                gtz += (-dx * sz + dy * fz) * k;
            } else {
                gyaw -= dx * 0.006;
                gpitch = clamp(gpitch + dy * 0.0045, Math.toRadians(2), Math.toRadians(89));
            }
        } else {
            gwyaw -= dx * 0.0042;
            gwpitch = clamp(gwpitch - dy * 0.0042, Math.toRadians(-80), Math.toRadians(85));
        }
    }

    public synchronized void wheel(double notches) {
        if (mode == Mode.ORBIT) {
            gdist = clamp(gdist * Math.pow(1.13, notches), 3, 1500);
            if (notches < 0) armed = true;
        } else {
            double f = -notches * 2.2;
            double cp = Math.cos(wpitch);
            wx += -Math.sin(wyaw) * cp * f; wy += Math.sin(wpitch) * f; wz += -Math.cos(wyaw) * cp * f;
            wy = Math.max(0.6, wy);
        }
    }

    /** Doppelklick außen: neuer Drehpunkt. */
    public synchronized void focus(double[] p) {
        if (p == null || mode != Mode.ORBIT) return;
        gtx = p[0]; gty = p[1]; gtz = p[2];
        gdist = Math.min(gdist, Math.max(25, gdist * 0.6));
    }

    // ------------------------------------------------------------ Orte

    public synchronized void goRoom(Room r) {
        startFade(() -> {
            if (r.eye == null) {
                overview();
                return;
            }
            mode = Mode.WALK;
            wx = r.eye[0]; wy = r.eye[1]; wz = r.eye[2];
            double dx = r.look[0] - wx, dy = r.look[1] - wy, dz = r.look[2] - wz;
            wyaw = gwyaw = Math.atan2(-dx, -dz);
            wpitch = gwpitch = Math.atan2(dy, Math.hypot(dx, dz));
            place = r.name;
        });
    }

    public synchronized void goOverview() { startFade(this::overview); }

    private void overview() {
        mode = Mode.ORBIT;
        gtx = 0; gty = 12; gtz = 6; gyaw = Math.toRadians(35); gpitch = Math.toRadians(21); gdist = 270;
        tx = gtx; ty = gty; tz = gtz; yaw = gyaw; pitch = gpitch; dist = gdist;
        armed = false;
        place = "Außen";
    }

    /** Von innen nach außen, Blickrichtung bleibt. */
    public synchronized void goOutside() {
        if (mode == Mode.ORBIT) return;
        double ox = wx, oz = wz, oy = wy, ay = wyaw;
        startFade(() -> {
            mode = Mode.ORBIT;
            gtx = ox; gtz = oz; gty = Math.max(8, oy);
            gyaw = ay; gpitch = Math.toRadians(28); gdist = 110;
            tx = gtx; ty = gty; tz = gtz; yaw = gyaw; pitch = gpitch; dist = gdist;
            armed = false;
            place = "Außen";
        });
    }

    /** Blick von der Seite auf die Schnittfläche bei x = cutX (Kamera auf der weggeschnittenen Seite). */
    public synchronized void goSection(double cutX) {
        startFade(() -> {
            mode = Mode.ORBIT;
            gtx = cutX; gty = 4; gtz = 22;
            gyaw = Math.toRadians(82); gpitch = Math.toRadians(13); gdist = 105;
            tx = gtx; ty = gty; tz = gtz; yaw = gyaw; pitch = gpitch; dist = gdist;
            armed = false;
            place = "Schnitt";
        });
    }

    private void startFade(Runnable r) {
        pending = r;
        fadeDir = 1;
    }

    /** Übernimmt die Kamera nach einer Fahrt an genau dieser Stelle (außen hoch: Orbit, sonst frei). */
    public synchronized void adopt(Camera cam) {
        fadeDir = 0; fade = 0; pending = null;
        boolean inside = false;
        for (Room r : rooms) if (r.roofed && r.contains(cam.ex, cam.ey, cam.ez)) inside = true;
        cam.update();
        if (!inside && cam.ey > 18) {
            double fx = cam.fx, fy = cam.fy, fz = cam.fz;
            double t = fy < -0.05 ? (10 - cam.ey) / fy : 150;
            t = Math.max(40, Math.min(900, t));
            gtx = cam.ex + fx * t; gty = cam.ey + fy * t; gtz = cam.ez + fz * t;
            gdist = t;
            gyaw = cam.yaw;
            gpitch = clamp(-cam.pitch, Math.toRadians(2), Math.toRadians(89));
            tx = gtx; ty = gty; tz = gtz; yaw = gyaw; pitch = gpitch; dist = gdist;
            mode = Mode.ORBIT;
            armed = false;
            place = "Außen";
        } else {
            mode = Mode.WALK;
            wx = cam.ex; wy = cam.ey; wz = cam.ez;
            wyaw = gwyaw = cam.yaw; wpitch = gwpitch = cam.pitch;
        }
        releaseKeys();
    }

    // ------------------------------------------------------------ Takt

    public synchronized void update(double dt, Camera cam) {
        if (fadeDir != 0) {
            fade += fadeDir * dt / 0.3;
            if (fade >= 1) {
                fade = 1; fadeDir = -1;
                if (pending != null) { pending.run(); pending = null; }
            } else if (fade <= 0) {
                fade = 0; fadeDir = 0;
            }
        }
        double k = 1 - Math.exp(-dt * 7);
        if (mode == Mode.ORBIT) {
            if (autoOrbit) gyaw += dt * 0.09;
            tx += (gtx - tx) * k; ty += (gty - ty) * k; tz += (gtz - tz) * k;
            yaw += (gyaw - yaw) * k; pitch += (gpitch - pitch) * k;
            dist = Math.exp(Math.log(dist) + (Math.log(gdist) - Math.log(dist)) * k);
            cam.ex = tx + dist * Math.cos(pitch) * Math.sin(yaw);
            cam.ey = ty + dist * Math.sin(pitch);
            cam.ez = tz + dist * Math.cos(pitch) * Math.cos(yaw);
            if (cam.ey < 0.8) cam.ey = 0.8;
            cam.lookAt(tx, ty, tz);
            if (armed && roofVisible && fadeDir == 0) {
                for (Room r : rooms) {
                    if (r.roofed && r.contains(cam.ex, cam.ey, cam.ez)) {
                        mode = Mode.WALK;
                        wx = cam.ex; wy = cam.ey; wz = cam.ez;
                        wyaw = gwyaw = cam.yaw; wpitch = gwpitch = cam.pitch;
                        place = r.name;
                        break;
                    }
                }
            }
        } else {
            wyaw += (gwyaw - wyaw) * k * 1.4; wpitch += (gwpitch - wpitch) * k * 1.4;
            double sp = (keys[K_SHIFT] ? 12 : 4) * dt;
            double fx = -Math.sin(wyaw), fz = -Math.cos(wyaw), rx = -fz, rz = fx;
            double mf = (keys[K_W] ? 1 : 0) - (keys[K_S] ? 1 : 0), ms = (keys[K_D] ? 1 : 0) - (keys[K_A] ? 1 : 0);
            double mu = (keys[K_E] ? 1 : 0) - (keys[K_Q] ? 1 : 0);
            wx += (fx * mf + rx * ms) * sp; wz += (fz * mf + rz * ms) * sp; wy = Math.max(0.6, wy + mu * sp);
            cam.ex = wx; cam.ey = wy; cam.ez = wz; cam.yaw = wyaw; cam.pitch = wpitch;
            cam.update();
            String now = "Innen";
            for (Room r : rooms) if (r.contains(wx, wy, wz)) { now = r.name; break; }
            place = now;
        }
    }

    private static double clamp(double v, double a, double b) { return Math.max(a, Math.min(b, v)); }
}
