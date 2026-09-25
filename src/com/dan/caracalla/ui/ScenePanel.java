package com.dan.caracalla.ui;

import com.dan.caracalla.model.ThermenModel;
import com.dan.caracalla.render.Bvh;
import com.dan.caracalla.render.Camera;
import com.dan.caracalla.regie.Clip;
import com.dan.caracalla.regie.Director;
import com.dan.caracalla.regie.Programme;
import com.dan.caracalla.regie.Timeline;
import com.dan.caracalla.render.Lighting;
import com.dan.caracalla.render.Renderer;
import com.dan.caracalla.render.SkyCache;
import com.dan.caracalla.render.WaterSim;
import com.dan.caracalla.render.SteamSim;
import com.dan.caracalla.render.Sun;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/** Zeigt die Szene, nimmt Maus und Tastatur entgegen und treibt die Bildschleife. */
public final class ScenePanel extends JPanel {
    /** Strahlen je Ecke für die Himmelssicht. */
    static final int SKY_RAYS = 48;
    private volatile ThermenModel model;
    private volatile Renderer renderer;
    private volatile CameraController ctl;
    private final Camera cam = new Camera();
    private final Sun sun = new Sun();

    private volatile BufferedImage shown;
    private volatile String loading = "Die Thermen werden errichtet …";
    private volatile double scale = 0.75;
    private volatile boolean roof = true;
    private volatile int day = 172;
    private volatile double hour = 15.5;
    private volatile boolean sunDirty = true;
    private volatile double haze = 0, timelapse = 0;
    private volatile boolean volumetric = true, bloom = true, steamOn = true;
    private volatile SteamSim steamSim;
    private Consumer<Double> timeListener = h -> { };
    private volatile int[] pickRequest, stoneRequest;
    private volatile double fps;
    private volatile boolean running = true;

    private Consumer<String> status = s -> { };
    private final Director director = new Director();
    private Consumer<Integer> dayListener = d -> { };
    private Runnable onReady = () -> { };

    private int lastX, lastY;

    // Zugaben
    private volatile boolean cutOn, thermo, waterPath, soundOn = true;
    private volatile float cutX = 0, ruinTarget = 0, ruin = 0, lightRuin = 0;
    private volatile com.dan.caracalla.render.Streams waterFlow, heatFlow;
    private volatile com.dan.caracalla.audio.SoundScape sound;
    private Runnable extrasListener = () -> { };

    // Datenbank (Phase 8): Inhalte, Steckbrief, Baugeschichte, gemerkte Blicke
    private volatile com.dan.caracalla.db.CaracallaDb db;
    private volatile com.dan.caracalla.db.CaracallaDb.Snapshot snap;
    private volatile String dbState = "Datenbank wird gesucht …";
    private Runnable dbListener = () -> { };
    private volatile boolean stoneCards = true;
    private volatile StoneCard card;
    private volatile String lastPlace = "";
    private volatile long placeSince;

    /** Steckbrief eines angeklickten Steins. */
    private static final class StoneCard {
        final String code;
        final com.dan.caracalla.db.CaracallaDb.Stone stone;
        final long since = System.currentTimeMillis();
        StoneCard(String code, com.dan.caracalla.db.CaracallaDb.Stone stone) { this.code = code; this.stone = stone; }
    }

    public ScenePanel() {
        setBackground(new Color(14, 13, 12));
        setFocusable(true);
        setDoubleBuffered(true);
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                card = null;
                lastX = e.getX(); lastY = e.getY();
                if (director.active()) {
                    double f = timelineHit(e.getX(), e.getY());
                    if (f >= 0) { director.seek(f); seeking = true; return; }
                    stopDirector();
                }
            }

            @Override public void mouseReleased(MouseEvent e) { seeking = false; }

            @Override public void mouseDragged(MouseEvent e) {
                CameraController c = ctl;
                if (c == null) return;
                if (seeking) { double f = timelineHit(e.getX(), getHeight() - 60); if (f >= 0) director.seek(f); return; }
                if (director.active()) stopDirector();
                boolean pan = SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e) || e.isShiftDown();
                c.drag(e.getX() - lastX, e.getY() - lastY, pan);
                lastX = e.getX(); lastY = e.getY();
            }

            @Override public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                if (e.getClickCount() == 2) pickRequest = new int[]{e.getX(), e.getY()};
                else if (e.getClickCount() == 1) stoneRequest = new int[]{e.getX(), e.getY()};
            }

            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                if (director.active()) stopDirector();
                CameraController c = ctl;
                if (c != null) c.wheel(e.getPreciseWheelRotation());
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { key(e, true); }
            @Override public void keyReleased(KeyEvent e) { key(e, false); }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                CameraController c = ctl;
                if (c != null) c.releaseKeys();
            }
        });
    }

    private void key(KeyEvent e, boolean down) {
        if (down) card = null;
        CameraController c = ctl;
        if (c == null) return;
        int k = -1;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP: k = CameraController.K_W; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN: k = CameraController.K_S; break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT: k = CameraController.K_A; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: k = CameraController.K_D; break;
            case KeyEvent.VK_Q: case KeyEvent.VK_PAGE_DOWN: k = CameraController.K_Q; break;
            case KeyEvent.VK_E: case KeyEvent.VK_PAGE_UP: k = CameraController.K_E; break;
            case KeyEvent.VK_SHIFT: k = CameraController.K_SHIFT; break;
            default:
        }
        if (k >= 0) {
            if (down && director.active() && k != CameraController.K_SHIFT) stopDirector();
            c.setKey(k, down);
            return;
        }
        if (!down) return;
        ThermenModel m = model;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_F5: play(dayScript()); return;
            case KeyEvent.VK_T: play(tour()); return;
            case KeyEvent.VK_ESCAPE:
                if (help) { help = false; return; }
                if (!director.active() && cinema) { setCinema(false); return; }
                stopDirector();
                return;
            case KeyEvent.VK_X: setCut(!cutOn); return;
            case KeyEvent.VK_I: setThermo(!thermo); return;
            case KeyEvent.VK_L: setWaterPath(!waterPath); return;
            case KeyEvent.VK_M: setSound(!soundOn); return;
            case KeyEvent.VK_Z: setRuin(ruinTarget > 0.5f ? 0 : 1); return;
            case KeyEvent.VK_P: requestStill(); return;
            case KeyEvent.VK_K: setCinema(!cinema); return;
            case KeyEvent.VK_F1: case KeyEvent.VK_H: help = !help; return;
            case KeyEvent.VK_F11: setCinema(!cinema); return;
            default:
        }
        if (director.active()) stopDirector();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_O: c.goOutside(); break;
            case KeyEvent.VK_R: case KeyEvent.VK_0: c.goOverview(); break;
            case KeyEvent.VK_SPACE: c.autoOrbit = !c.autoOrbit; break;
            default:
                int n = e.getKeyCode() - KeyEvent.VK_1 + 1;
                if (m != null && n >= 1 && n < m.rooms.size()) c.goRoom(m.rooms.get(n));
        }
    }

    public void setStatusListener(Consumer<String> s) { status = s; }

    // ------------------------------------------------------------ Zugaben

    public void setCut(boolean on) { cutOn = on; extrasChanged(); }
    public boolean cut() { return cutOn; }
    public void setCutX(float x) { cutX = x; }
    public float cutX() { return cutX; }
    /** Zeitregler: 0 = 216 n. Chr., 1 = heute (gleitet weich dorthin). */
    public void setRuin(float r) { ruinTarget = Math.max(0, Math.min(1, r)); extrasChanged(); }
    public float ruinTarget() { return ruinTarget; }
    public void setThermo(boolean on) { thermo = on; Renderer r = renderer; if (r != null && !on) r.resetExposure(); extrasChanged(); }
    public boolean thermo() { return thermo; }
    public void setWaterPath(boolean on) { waterPath = on; extrasChanged(); }
    public boolean waterPath() { return waterPath; }
    public void setSound(boolean on) {
        soundOn = on;
        com.dan.caracalla.audio.SoundScape sc = sound;
        if (sc != null) sc.setEnabled(on);
        extrasChanged();
    }
    public boolean sound() { return soundOn; }
    public void setExtrasListener(Runnable r) { extrasListener = r; }

    private void extrasChanged() { SwingUtilities.invokeLater(extrasListener); }

    // ------------------------------------------------------------ Datenbank

    public void setDbListener(Runnable r) { dbListener = r; }
    public String dbState() { return dbState; }
    public boolean dbReady() { return db != null && snap != null; }
    public void setStoneCards(boolean on) { stoneCards = on; if (!on) card = null; }
    public boolean roof() { return roof; }
    public double haze() { return haze; }

    /** Kamerafahrten: aus der Datenbank, sonst die eingebauten. */
    public java.util.List<Timeline> flights() {
        com.dan.caracalla.db.CaracallaDb.Snapshot s = snap;
        return s != null && !s.flights.isEmpty() ? new java.util.ArrayList<>(s.flights) : Programme.flights();
    }
    public Timeline tour() { com.dan.caracalla.db.CaracallaDb.Snapshot s = snap; return s != null && s.tour != null ? s.tour : Programme.tour(); }
    public Timeline dayScript() { com.dan.caracalla.db.CaracallaDb.Snapshot s = snap; return s != null && s.dayScript != null ? s.dayScript : Programme.dayScript(); }
    public java.util.List<com.dan.caracalla.db.CaracallaDb.View> views() {
        com.dan.caracalla.db.CaracallaDb.Snapshot s = snap;
        return s == null ? java.util.List.of() : new java.util.ArrayList<>(s.views);
    }

    private void loadDb() {
        Thread t = new Thread(() -> {
            com.dan.caracalla.db.DbConfig cfg = com.dan.caracalla.db.DbConfig.load();
            if (cfg == null) {
                dbState = "ohne Datenbank (db/db.properties fehlt)";
            } else {
                try {
                    com.dan.caracalla.db.CaracallaDb d = new com.dan.caracalla.db.CaracallaDb(cfg);
                    snap = d.load();
                    db = d;
                    dbState = "Datenbank " + cfg.label();
                } catch (Throwable e) {
                    String m = String.valueOf(e.getMessage());
                    int nl = m.indexOf('\n');
                    String first = (nl > 0 ? m.substring(0, nl) : m).trim();
                    if (first.length() > 70) first = first.substring(0, 67) + " …";
                    dbState = "ohne Datenbank (" + first + ")";
                    System.err.println("Datenbank: " + m);
                }
            }
            SwingUtilities.invokeLater(dbListener);
        }, "Caracalla-DB");
        t.setDaemon(true);
        t.start();
    }

    /** Hält den jetzigen Blick fest (Kamera, Sonne, Schalter). */
    public com.dan.caracalla.db.CaracallaDb.View captureView(String name) {
        com.dan.caracalla.db.CaracallaDb.View v = new com.dan.caracalla.db.CaracallaDb.View();
        cam.update();
        v.name = name;
        CameraController c = ctl;
        v.modus = c != null && c.mode() == CameraController.Mode.WALK ? "INNEN" : "ORBIT";
        v.ex = cam.ex; v.ey = cam.ey; v.ez = cam.ez;
        v.lx = cam.ex + cam.fx * 20; v.ly = cam.ey + cam.fy * 20; v.lz = cam.ez + cam.fz * 20;
        v.day = day; v.hour = hour; v.haze = haze;
        v.roof = roof; v.cut = cutOn; v.cutX = cutX; v.zeit = ruinTarget; v.thermo = thermo; v.water = waterPath;
        return v;
    }

    /** Stellt einen gemerkten Blick wieder her. */
    public void applyView(com.dan.caracalla.db.CaracallaDb.View v) {
        if (director.active()) stopDirector();
        day = v.day > 0 ? v.day : day;
        hour = v.hour > 0 ? v.hour : hour;
        haze = v.haze;
        sunDirty = true;
        roof = v.roof;
        cutOn = v.cut; cutX = (float) v.cutX; ruinTarget = (float) v.zeit; thermo = v.thermo; waterPath = v.water;
        CameraController c = ctl;
        if (c != null) {
            c.roofVisible = roof;
            cam.ex = v.ex; cam.ey = v.ey; cam.ez = v.ez;
            cam.lookAt(v.lx, v.ly, v.lz);
            c.adopt(cam);
        }
        int d = day; double h = hour;
        SwingUtilities.invokeLater(() -> { dayListener.accept(d); timeListener.accept(h); });
        extrasChanged();
    }

    /** Merkt den Blick in der Datenbank; ruft danach fertig (auf dem EDT) mit einer Meldung auf. */
    public void saveView(String name, Consumer<String> done) {
        com.dan.caracalla.db.CaracallaDb d = db;
        if (d == null) { done.accept("Ohne Datenbank lässt sich nichts merken."); return; }
        com.dan.caracalla.db.CaracallaDb.View v = captureView(name);
        new Thread(() -> {
            String msg;
            try {
                d.saveView(v);
                java.util.List<com.dan.caracalla.db.CaracallaDb.View> l = d.readViews();
                com.dan.caracalla.db.CaracallaDb.Snapshot s = snap;
                if (s != null) { s.views.clear(); s.views.addAll(l); }
                msg = "Gemerkt: " + name;
            } catch (Exception e) {
                msg = "Nicht gespeichert: " + e.getMessage();
            }
            String m = msg;
            SwingUtilities.invokeLater(() -> done.accept(m));
        }, "Caracalla-DB-Blick").start();
    }

    /** Baut aus allen gemerkten Blicken eine Fahrt (7 s je Blick), speichert sie und spielt sie ab. */
    public void flightFromViews(String name, Consumer<String> done) {
        com.dan.caracalla.db.CaracallaDb d = db;
        java.util.List<com.dan.caracalla.db.CaracallaDb.View> vs = views();
        if (d == null || vs.size() < 2) { done.accept("Dafür braucht es mindestens zwei gemerkte Blicke."); return; }
        com.dan.caracalla.regie.CameraPath p = new com.dan.caracalla.regie.CameraPath();
        for (int i = 0; i < vs.size(); i++) {
            com.dan.caracalla.db.CaracallaDb.View v = vs.get(i);
            p.key(i * 7.0, v.ex, v.ey, v.ez, v.lx, v.ly, v.lz);
        }
        Timeline tl = new Timeline(name);
        tl.day = vs.get(0).day;
        com.dan.caracalla.regie.Clip cl = new com.dan.caracalla.regie.Clip(p);
        if (vs.get(0).hour > 0 && vs.get(vs.size() - 1).hour > 0) cl.sun(vs.get(0).hour, vs.get(vs.size() - 1).hour);
        cl.caption(name, vs.size() + " gemerkte Blicke, nacheinander angeflogen.");
        tl.add(cl);
        new Thread(() -> {
            String msg;
            try {
                d.saveTimeline(tl);
                com.dan.caracalla.db.CaracallaDb.Snapshot s = snap;
                if (s != null) s.flights.add(tl);
                msg = "Fahrt gespeichert: " + name;
            } catch (Exception e) {
                msg = "Nicht gespeichert: " + e.getMessage();
            }
            String m = msg;
            SwingUtilities.invokeLater(() -> { done.accept(m); play(tl); });
        }, "Caracalla-DB-Fahrt").start();
    }

    /** Das Ereignis der Baugeschichte, das zur Stellung des Zeitreglers passt. */
    private com.dan.caracalla.db.CaracallaDb.Event eventAt(float ru) {
        com.dan.caracalla.db.CaracallaDb.Snapshot s = snap;
        if (s == null) return null;
        com.dan.caracalla.db.CaracallaDb.Event best = null;
        for (com.dan.caracalla.db.CaracallaDb.Event e : s.events) if (e.zeit >= 0 && e.zeit <= ru + 0.004) best = e;
        return best;
    }

    private static final java.util.Set<Integer> STONE_MATS = java.util.Set.of(com.dan.caracalla.geo.Mat.MARBLE, com.dan.caracalla.geo.Mat.FLOOR,
            com.dan.caracalla.geo.Mat.GRANITE, com.dan.caracalla.geo.Mat.GIALLO, com.dan.caracalla.geo.Mat.PORPHYRY, com.dan.caracalla.geo.Mat.CORNICE,
            com.dan.caracalla.geo.Mat.STATUE, com.dan.caracalla.geo.Mat.POOL, com.dan.caracalla.geo.Mat.PAVING);

    /** Steckbrief für den Stein unter dem Klick. */
    private void stoneClick(Renderer r, int sx, int sy, int m) {
        if (!stoneCards || !STONE_MATS.contains(m) || thermo) return;
        double[] q = r.pick(sx, sy);
        float[] n = r.pickNormal(sx, sy);
        if (q == null || n == null) return;
        if (ruin > 0.5f) return;
        String code = com.dan.caracalla.render.Materials.stoneAt(m, (float) q[0], (float) q[1], (float) q[2], n[0], n[1], n[2]);
        com.dan.caracalla.db.CaracallaDb.Snapshot s = snap;
        if (code == null && s != null) {
            java.util.List<String> l = s.matStones.get(m);
            if (l != null && !l.isEmpty()) code = l.get(0);
        }
        if (code == null) return;
        card = new StoneCard(code, s == null ? null : s.stones.get(code));
    }

    /** Kamera vor die Schnittfläche. */
    public void viewSection() {
        if (director.active()) stopDirector();
        if (!cutOn) setCut(true);
        CameraController c = ctl;
        if (c != null) c.goSection(cutX);
    }
    public void setDayListener(Consumer<Integer> l) { dayListener = l; }

    /** Spielt eine Kamerafahrt, den Rundgang oder das Drehbuch ab. */
    public void play(Timeline tl) {
        if (ctl == null) return;
        if (tl.day > 0) {
            day = tl.day;
            sunDirty = true;
            int d = tl.day;
            SwingUtilities.invokeLater(() -> dayListener.accept(d));
        }
        timelapse = 0;
        ctl.releaseKeys();
        if (tl.waterPath) setWaterPath(true);
        director.play(tl);
    }

    /** Fahrt anhalten, die Kamera bleibt, wo sie ist, und gehört wieder dem Benutzer. */
    public void stopDirector() {
        if (!director.active()) return;
        director.stop();
        CameraController c = ctl;
        if (c != null) c.adopt(cam);
    }

    public boolean directorActive() { return director.active(); }

    private volatile boolean seeking;

    /** Position auf der Zeitleiste (0..1) oder -1, wenn nicht getroffen. */
    private double timelineHit(int x, int y) {
        int W = getWidth(), H = getHeight();
        int x0 = 24, x1 = W - 24, yb = H - 60;
        if (y < yb - 10 || y > yb + 14 || x < x0 || x > x1) return -1;
        return (x - x0) / (double) (x1 - x0);
    }
    public void setOnReady(Runnable r) { onReady = r; }
    public ThermenModel model() { return model; }
    public CameraController controller() { return ctl; }

    public void setScale(double s) { scale = s; autoQuality = false; }
    public void setRoof(boolean r) { roof = r; CameraController c = ctl; if (c != null) c.roofVisible = r; }
    public void setSunTime(int day, double hour) { this.day = day; this.hour = hour; sunDirty = true; }
    public void setHaze(double h) { haze = h; sunDirty = true; }
    public void setVolumetric(boolean v) { volumetric = v; }
    public void setBloom(boolean b) { bloom = b; }
    public void setSteam(boolean on) { steamOn = on; }
    /** Zeitraffer: Stunden Sonnenzeit je Sekunde, 0 = aus. */
    public void setTimelapse(double hoursPerSecond) { timelapse = hoursPerSecond; }
    public void setTimeListener(Consumer<Double> l) { timeListener = l; }
    public Sun sun() { return sun; }

    /** Lädt das Modell im Hintergrund und startet dann die Bildschleife. */
    public void start() {
        Thread t = new Thread(this::loop, "Caracalla-Render");
        t.setDaemon(true);
        t.start();
    }

    public void stop() { running = false; }

    private void loop() {
        try {
            ThermenModel m = ThermenModel.build();
            loading = "Himmelslicht wird berechnet …";
            repaint();
            if (!SkyCache.load(m.mesh, SKY_RAYS)) {
                Bvh.bakeSky(m.mesh, SKY_RAYS, p -> { loading = String.format("Himmelslicht wird berechnet … %d %%", (int) (p * 100)); repaint(); });
                SkyCache.save(m.mesh, SKY_RAYS);
            }
            Renderer r = new Renderer(m.mesh, 4096);
            loading = "Licht wird gesetzt …";
            repaint();
            sun.set(day, hour);
            sunDirty = false;
            r.setSun(sun.dir, haze);
            WaterSim ws = new WaterSim(m.waters, m.fountains);
            ws.warmup(2);
            r.water = ws;
            SteamSim ss = new SteamSim(m.waters);
            ss.setHour(hour);
            loading = "Dampf steigt auf …";
            repaint();
            ss.warmup(70);
            r.steam = ss;
            steamSim = ss;
            CameraController c = new CameraController(m.rooms);
            c.roofVisible = roof;
            r.furnaces = m.furnaces;
            com.dan.caracalla.render.Ruin.prepare();
            waterFlow = new com.dan.caracalla.render.Streams(m.waterRoutes, 9000, 14, 0.22f);
            heatFlow = new com.dan.caracalla.render.Streams(m.heatRoutes, 2600, 2.4f, 0.12f);
            if (!Boolean.getBoolean("caracalla.silent")) {
                com.dan.caracalla.audio.SoundScape sc = com.dan.caracalla.audio.SoundScape.start();
                sc.setEnabled(soundOn);
                sound = sc;
            }
            model = m; renderer = r; ctl = c;
            loadDb();
            loading = null;
            SwingUtilities.invokeLater(onReady);
            String auto = System.getProperty("caracalla.autoplay");
            if (auto != null) {
                Timeline tl = auto.equals("rundgang") ? Programme.tour() : Programme.dayScript();
                SwingUtilities.invokeLater(() -> play(tl));
            }
        } catch (Throwable ex) {
            loading = "Fehler beim Aufbau: " + ex;
            repaint();
            ex.printStackTrace();
            return;
        }
        Thread lw = new Thread(this::lightLoop, "Caracalla-Licht");
        lw.setDaemon(true);
        lw.setPriority(Thread.NORM_PRIORITY - 1);
        lw.start();
        long last = System.nanoTime();
        double t = 0, fpsAcc = 0;
        int frames = 0;
        long lastStatus = 0;
        while (running) {
            long now = System.nanoTime();
            double realDt = Math.min(0.5, (now - last) / 1e9);
            double dt = Math.min(0.1, realDt);
            last = now;
            t += dt;
            Renderer r = renderer;
            CameraController c = ctl;
            double tl = timelapse;
            if (tl > 0) {
                double hh = hour + dt * tl;
                if (hh > 20.5) hh = 5.5;
                hour = hh;
                sunDirty = true;
                double shown = hh;
                SwingUtilities.invokeLater(() -> timeListener.accept(shown));
            }
            r.volumetric = volumetric;
            // Zeitregler gleitet, Schatten werden nachgeführt, sobald er sich merklich bewegt hat
            float rt = ruinTarget, ru = ruin;
            if (Math.abs(rt - ru) > 1e-4f) {
                ru += (rt - ru) * (float) Math.min(1, realDt * 1.6);
                if (Math.abs(rt - ru) < 0.003f) ru = rt;
                ruin = ru;
            }
            if (Math.abs(ru - lightRuin) > 0.025f || (ru == rt && ru != lightRuin)) { lightRuin = ru; sunDirty = true; }
            r.ruin = ru;
            r.cut = cutOn;
            r.cutX = cutX;
            r.thermo = thermo;
            r.hour = hour;
            r.dayOfYear = day;
            r.waterFlow = waterPath && ru < 0.35f ? waterFlow : null;
            r.heatFlow = (cutOn || thermo) && ru < 0.35f ? heatFlow : null;
            SteamSim ssim = steamSim;
            if (ssim != null) { ssim.setHour(hour); ssim.setHaze(haze); }
            r.steam = steamOn && ru < 0.3f ? ssim : null;
            r.steamSprites = steamOn;
            r.bloom = bloom;
            int pw = Math.max(1, getWidth()), ph = Math.max(1, getHeight());
            // Klicks beziehen sich auf das zuletzt gezeigte Bild
            int rw = Math.max(1, r.width()), rh = Math.max(1, r.height());
            r.showRoof = roof;
            int[] sr = stoneRequest;
            if (sr != null && r.width() > 0) {
                stoneRequest = null;
                int sx = (int) (sr[0] * rw / (double) pw), sy = (int) (sr[1] * rh / (double) ph);
                int pm = r.pickMaterial(sx, sy);
                if (pm != com.dan.caracalla.geo.Mat.WATER && pm >= 0) stoneClick(r, sx, sy, pm);
                if (pm == com.dan.caracalla.geo.Mat.WATER) {
                    double[] q = r.pick(sx, sy);
                    if (q != null && r.water != null) {
                        r.water.splash(q[0], q[1], q[2]);
                        com.dan.caracalla.audio.SoundScape sc = sound;
                        if (sc != null) {
                            double d = Math.sqrt((q[0] - cam.ex) * (q[0] - cam.ex) + (q[1] - cam.ey) * (q[1] - cam.ey) + (q[2] - cam.ez) * (q[2] - cam.ez));
                            sc.splash((float) (1 / (1 + d / 12)), pan(q[0], q[2]));
                        }
                    }
                }
            }
            int[] pr = pickRequest;
            if (pr != null) {
                pickRequest = null;
                c.focus(r.pick((int) (pr[0] * rw / (double) pw), (int) (pr[1] * rh / (double) ph)));
            }
            if (director.active()) {
                boolean still = director.update(realDt, cam);
                double dh = director.hour();
                if (!Double.isNaN(dh)) {
                    hour = dh;
                    sunDirty = true;
                    SwingUtilities.invokeLater(() -> timeListener.accept(dh));
                }
                double dz = director.haze();
                if (!Double.isNaN(dz) && Math.abs(dz - haze) > 1e-6) {
                    boolean flip = (dz > 0.5) != (haze > 0.5);
                    haze = dz; sunDirty = true;
                    if (flip) extrasChanged();
                }
                if (!still) c.adopt(cam);
            } else {
                c.update(dt, cam);
            }
            // Qualität: fest oder automatisch (bewegt kleiner, im Stillstand volle Auflösung)
            cam.update();
            boolean moved = Math.abs(cam.ex - lastCam[0]) + Math.abs(cam.ey - lastCam[1]) + Math.abs(cam.ez - lastCam[2])
                    + Math.abs(cam.fx - lastCam[3]) + Math.abs(cam.fy - lastCam[4]) + Math.abs(cam.fz - lastCam[5]) > 1e-4;
            lastCam[0] = cam.ex; lastCam[1] = cam.ey; lastCam[2] = cam.ez; lastCam[3] = cam.fx; lastCam[4] = cam.fy; lastCam[5] = cam.fz;
            stillFor = moved || director.active() ? 0 : stillFor + realDt;
            double sc = scale;
            if (autoQuality) {
                if (lastWasAuto && !lastWasSharp) {
                    if (realDt > 1 / 13.0) autoScale = Math.max(0.42, autoScale * 0.96);
                    else if (realDt < 1 / 22.0) autoScale = Math.min(1.0, autoScale * 1.03);
                }
                boolean sharp = stillFor > 0.7;
                sc = sharp ? 1.0 : autoScale;
                lastWasSharp = sharp;
            }
            lastWasAuto = autoQuality;
            shownScale = sc;
            int w = Math.max(64, (int) (pw * sc)), h = Math.max(48, (int) (ph * sc));
            r.setSize(w, h);
            shown = r.render(cam, t, dt);
            if (stillRequest) { stillRequest = false; saveStill(r); r.setSize(w, h); }
            if (sound != null && (frames & 3) == 0) updateSound(m0(), ru);
            repaint();
            frames++;
            fpsAcc += realDt;
            if (fpsAcc > 0.5) { fps = frames / fpsAcc; frames = 0; fpsAcc = 0; }
            if (now - lastStatus > 250_000_000L) {
                lastStatus = now;
                Timeline tlx = director.timeline();
                String mode = tlx != null ? "Regie: " + tlx.name : (c.mode() == CameraController.Mode.ORBIT ? "Außen, Orbit" : "Innen");
                String s = String.format("%s  ·  %s  ·  Sonne %s Uhr, Höhe %.0f°, Azimut %.0f°  ·  %d × %d%s  ·  %.0f Bilder/s  ·  %s",
                        mode, c.place(),
                        Sun.timeLabel(hour), sun.elevationDeg, sun.azimuthDeg, w, h, autoQuality ? " (Auto)" : "", fps, dbState);
                SwingUtilities.invokeLater(() -> status.accept(s));
            }
            try { Thread.sleep(1); } catch (InterruptedException e) { return; }
        }
    }

    private ThermenModel m0() { return model; }

    // ------------------------------------------------------------ Schliff

    private final double[] lastCam = new double[6];
    private double stillFor, autoScale = 0.75;
    private boolean lastWasAuto, lastWasSharp;
    private volatile boolean autoQuality = true, stillRequest, cinema, help;
    private volatile double shownScale = 0.75;
    private volatile String toast;
    private volatile long toastUntil;
    private Consumer<Boolean> cinemaListener = b -> { };

    public void setAutoQuality() { autoQuality = true; }
    public boolean autoQuality() { return autoQuality; }
    public void setCinemaListener(Consumer<Boolean> l) { cinemaListener = l; }
    public boolean cinema() { return cinema; }

    /** Kinomodus: Bedienfeld und Statuszeile verschwinden, Breitbild-Balken, keine Hinweise. */
    public void setCinema(boolean on) {
        cinema = on;
        SwingUtilities.invokeLater(() -> cinemaListener.accept(on));
        extrasChanged();
    }

    /** Standbild in hoher Auflösung beim nächsten Bild. */
    public void requestStill() { stillRequest = true; showToast("Standbild wird gerechnet …", 30000); }

    private void showToast(String s, long ms) { toast = s; toastUntil = System.currentTimeMillis() + ms; }

    /** Rechnet das Bild doppelt so groß (höchstens 3840 Pixel breit) und legt es als PNG ab. */
    private void saveStill(Renderer r) {
        try {
            int pw = Math.max(1, getWidth()), ph = Math.max(1, getHeight());
            double k = Math.min(2.0, 3840.0 / pw);
            int w = (int) (pw * k), h = (int) (ph * k);
            r.setSize(w, h);
            r.resetExposure();
            r.render(cam, 0, 0);
            BufferedImage img = r.render(cam, 0, 0);
            java.io.File dir = new java.io.File(System.getProperty("user.home"), "Pictures");
            dir = dir.isDirectory() ? new java.io.File(dir, "Caracalla") : new java.io.File("standbilder");
            dir.mkdirs();
            String name = "Caracalla_" + java.time.LocalDateTime.now().withNano(0).toString().replace(':', '-').replace('T', '_') + ".png";
            java.io.File f = new java.io.File(dir, name);
            javax.imageio.ImageIO.write(img, "png", f);
            r.resetExposure();
            showToast("Standbild gespeichert: " + f.getAbsolutePath() + "  (" + w + " × " + h + ")", 6000);
        } catch (Exception e) {
            showToast("Standbild nicht gespeichert: " + e.getMessage(), 6000);
        }
    }

    /** Links (-1) bis rechts (1) relativ zur Blickrichtung. */
    private float pan(double x, double z) {
        double dx = x - cam.ex, dz = z - cam.ez, l = Math.hypot(dx, dz);
        if (l < 1e-6) return 0;
        cam.update();
        double rl = Math.hypot(cam.rx, cam.rz);
        if (rl < 1e-6) return 0;
        return (float) ((dx * cam.rx + dz * cam.rz) / (l * rl));
    }

    /** Hörerlage: nächster Speier, Becken, Feuer, Saal mit seinem Nachhall. */
    private void updateSound(ThermenModel m, float ru) {
        com.dan.caracalla.audio.SoundScape sc = sound;
        if (sc == null || m == null) return;
        double ex = cam.ex, ey = cam.ey, ez = cam.ez;
        float wet = Math.max(0, 1 - ru / 0.3f);
        double bestF = 1e9; com.dan.caracalla.model.Fountain nf = null;
        for (com.dan.caracalla.model.Fountain f : m.fountains) {
            double d = Math.sqrt((f.x - ex) * (f.x - ex) + (f.y - ey) * (f.y - ey) + (f.z - ez) * (f.z - ez));
            if (d < bestF) { bestF = d; nf = f; }
        }
        double bestW = 1e9;
        for (com.dan.caracalla.model.WaterBody w : m.waters) {
            double[] b = w.bounds();
            double dx = Math.max(0, Math.max(b[0] - ex, ex - b[2])), dz = Math.max(0, Math.max(b[1] - ez, ez - b[3]));
            bestW = Math.min(bestW, Math.sqrt(dx * dx + dz * dz + (ey - w.waterY) * (ey - w.waterY)));
        }
        double bestFire = 1e9; double[] ff = null;
        for (double[] f : m.furnaces) {
            double d = Math.sqrt((f[0] - ex) * (f[0] - ex) + (f[1] - ey) * (f[1] - ey) + (f[2] - ez) * (f[2] - ez));
            if (d < bestFire) { bestFire = d; ff = f; }
        }
        String room = null;
        for (com.dan.caracalla.model.Room r : m.rooms) if (r.contains(ex, ey, ez)) { room = r.name; break; }
        float rt, mix;
        boolean indoor = true;
        if ("Caldarium".equals(room)) { rt = 6.5f; mix = 0.55f; }
        else if ("Frigidarium".equals(room)) { rt = 4.8f; mix = 0.48f; }
        else if ("Tepidarium".equals(room)) { rt = 3.2f; mix = 0.4f; }
        else if ("Natatio".equals(room)) { rt = 1.3f; mix = 0.18f; indoor = false; }
        else if (room != null) { rt = 1.0f; mix = 0.14f; indoor = false; }
        else if (Math.abs(ex) < 107 && Math.abs(ez) < 55 && ey < 19) { rt = 2.2f; mix = 0.3f; }
        else { rt = 0.55f; mix = 0.07f; indoor = false; }
        float open = Math.min(1, ru * 1.2f);
        rt = rt * (1 - 0.85f * open) + 0.5f * 0.85f * open;
        mix = mix * (1 - 0.7f * open) + 0.07f * 0.7f * open;
        float fountain = (float) (1 / (1 + Math.pow(bestF / 7, 2))) * wet;
        float water = (float) (1 / (1 + Math.pow(bestW / 14, 2))) * wet;
        float drips = indoor ? 0.7f * (1 - open) : 0.05f * wet;
        float heat = com.dan.caracalla.render.Ruin.working(ru);
        float fire = (float) (1 / (1 + Math.pow(bestFire / 5, 2))) * heat;
        float wind = indoor ? 0.03f + 0.2f * open : (float) Math.min(0.55, 0.18 + ey / 250);
        float birds = sun.elevationDeg > 2 ? Math.max(0, (ru - 0.5f) * 2) * (indoor ? 0.4f : 0.8f) : 0;
        sc.set(water, fountain, nf == null ? 0 : pan(nf.x, nf.z), drips, fire, ff == null ? 0 : pan(ff[0], ff[2]),
                wind, birds, rt, mix);
    }

    /** Hintergrund: berechnet Schatten, Himmel und Lichtrückwurf für den neuen Sonnenstand. */
    private void lightLoop() {
        Sun s2 = new Sun();
        while (running) {
            Renderer r = renderer;
            if (r == null || !sunDirty) { sleep(8); continue; }
            Lighting spare = r.takeSpare();
            if (spare == null) { sleep(4); continue; }
            sunDirty = false;
            s2.set(day, hour);
            spare.ruin = lightRuin;
            spare.compute(model.mesh, s2.dir, haze);
            sun.elevationDeg = s2.elevationDeg;
            sun.azimuthDeg = s2.azimuthDeg;
            r.offer(spare);
        }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        int W = getWidth(), H = getHeight();
        BufferedImage img = shown;
        String l = loading;
        if (img != null && l == null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, W, H, null);
            CameraController c = ctl;
            double f = Math.max(c == null ? 0 : c.fade(), director.fade());
            if (f > 0) {
                g.setColor(new Color(0, 0, 0, (int) (255 * Math.min(1, f))));
                g.fillRect(0, 0, W, H);
            }
            if (cinema) {
                int bar = (int) Math.max(0, (H - W / 2.39) / 2);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, W, bar);
                g.fillRect(0, H - bar, W, bar);
                if (director.active()) cinemaCaption(g, bar);
            } else if (c != null) {
                if (director.active()) regieHud(g); else hud(g, c);
                extrasHud(g);
                if (card != null) stoneCardHud(g);
            }
            if (help) helpHud(g);
            if (toast != null && System.currentTimeMillis() < toastUntil) {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setFont(new Font("SansSerif", Font.PLAIN, 13));
                int tw = g.getFontMetrics().stringWidth(toast);
                g.setColor(new Color(0, 0, 0, 170));
                g.fillRoundRect((W - tw) / 2 - 16, H / 2 - 20, tw + 32, 36, 10, 10);
                g.setColor(new Color(236, 224, 200));
                g.drawString(toast, (W - tw) / 2, H / 2 + 3);
            }
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(24, 21, 18), 0, H, new Color(8, 8, 9)));
            g.fillRect(0, 0, W, H);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(214, 196, 160));
            g.setFont(new Font("Serif", Font.PLAIN, 34));
            String title = "THERMAE ANTONINIANAE";
            g.drawString(title, (W - g.getFontMetrics().stringWidth(title)) / 2, H / 2 - 10);
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(new Color(170, 160, 145));
            String s = l == null ? "" : l;
            g.drawString(s, (W - g.getFontMetrics().stringWidth(s)) / 2, H / 2 + 24);
        }
        g.dispose();
    }

    /** Im Kinomodus: nur die Tafel, schlicht im unteren Balken. */
    private void cinemaCaption(Graphics2D g, int bar) {
        Clip clip = director.clip();
        double a = director.captionAlpha();
        if (clip == null || clip.title == null || a < 0.01) return;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int W = getWidth(), H = getHeight();
        int base = bar > 50 ? H - bar + 34 : H - 60;
        g.setFont(new Font("Serif", Font.PLAIN, 24));
        int tw = g.getFontMetrics().stringWidth(clip.title);
        g.setColor(new Color(240, 230, 210, (int) (255 * a)));
        g.drawString(clip.title, (W - tw) / 2, base);
        if (clip.text != null) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            java.util.List<String> lines = wrap(g, clip.text, g.getFont(), Math.min(760, W - 120));
            int ly = base + 24;
            g.setColor(new Color(205, 195, 178, (int) (230 * a)));
            for (String l : lines) {
                int lw = g.getFontMetrics().stringWidth(l);
                g.drawString(l, (W - lw) / 2, ly);
                ly += 19;
            }
        }
    }

    /** Übersicht aller Tasten (F1 oder H). */
    private void helpHud(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String[][] rows = {
                {"KAMERA", null},
                {"Maus ziehen", "drehen, innen umsehen"}, {"rechts ziehen", "verschieben"}, {"Mausrad", "Zoom, innen vor und zurück"},
                {"Doppelklick", "neuer Drehpunkt"}, {"W A S D, Pfeile", "gehen"}, {"Q E", "tiefer, höher"}, {"Umschalt", "schneller"},
                {"1 bis 5", "Säle"}, {"0 oder R", "Übersicht"}, {"O", "nach außen"}, {"Leertaste", "Rundflug"},
                {"REGIE", null},
                {"F5", "Drehbuch"}, {"T", "Rundgang"}, {"Esc", "anhalten"},
                {"ZUGABEN", null},
                {"X", "Schnitt"}, {"Z", "216 oder heute"}, {"I", "Wärmebild"}, {"L", "Wasserweg"}, {"M", "Klang"},
                {"Klick auf Stein", "Steckbrief"}, {"Klick ins Wasser", "Stein werfen"},
                {"BILD", null},
                {"P", "Standbild speichern"}, {"K oder F11", "Kinomodus"}, {"F1 oder H", "diese Übersicht"}};
        int W = getWidth(), H = getHeight();
        int col = 2, perCol = (rows.length + 1) / 2;
        int bw = 620, bh = 56 + perCol * 20;
        int x = (W - bw) / 2, y = Math.max(20, (H - bh) / 2);
        g.setColor(new Color(12, 11, 10, 215));
        g.fillRoundRect(x, y, bw, bh, 14, 14);
        g.setFont(new Font("Serif", Font.PLAIN, 22));
        g.setColor(new Color(240, 230, 210));
        g.drawString("Tasten", x + 24, y + 34);
        for (int i = 0; i < rows.length; i++) {
            int cx = x + 24 + (i / perCol) * (bw / col), cy = y + 62 + (i % perCol) * 20;
            if (rows[i][1] == null) {
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                g.setColor(new Color(214, 170, 92));
                g.drawString(rows[i][0], cx, cy);
            } else {
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                g.setColor(new Color(236, 224, 200));
                g.drawString(rows[i][0], cx, cy);
                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g.setColor(new Color(200, 190, 172));
                g.drawString(rows[i][1], cx + 118, cy);
            }
        }
    }

    /** Tafel und Zeitleiste während einer Fahrt. */
    private void regieHud(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Timeline tl = director.timeline();
        if (tl == null) return;
        int W = getWidth(), H = getHeight();
        // Name der Fahrt oben links
        g.setFont(new Font("Serif", Font.PLAIN, 20));
        String nm = tl.name.toUpperCase();
        int nw = g.getFontMetrics().stringWidth(nm);
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(14, 14, nw + 28, 36, 10, 10);
        g.setColor(new Color(236, 224, 200));
        g.drawString(nm, 28, 39);
        // Tafel
        Clip clip = director.clip();
        double a = director.captionAlpha();
        if (clip != null && clip.title != null && a > 0.01) {
            int bw = Math.min(560, W - 80), x = 40, y = H - 205;
            java.util.List<String> lines = wrap(g, clip.text, new Font("SansSerif", Font.PLAIN, 15), bw - 40);
            int bh = 58 + lines.size() * 21;
            y = H - 90 - bh;
            g.setColor(new Color(12, 11, 10, (int) (170 * a)));
            g.fillRoundRect(x, y, bw, bh, 12, 12);
            g.setColor(new Color(214, 170, 92, (int) (255 * a)));
            g.fillRect(x + 20, y + 16, 3, bh - 32);
            g.setFont(new Font("Serif", Font.PLAIN, 24));
            g.setColor(new Color(240, 230, 210, (int) (255 * a)));
            g.drawString(clip.title, x + 36, y + 38);
            g.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g.setColor(new Color(215, 205, 188, (int) (255 * a)));
            int ly = y + 64;
            for (String l : lines) { g.drawString(l, x + 36, ly); ly += 21; }
        }
        // Zeitleiste
        int x0 = 24, x1 = W - 24, yb = H - 60;
        double total = tl.duration(), acc = 0;
        g.setColor(new Color(0, 0, 0, 110));
        g.fillRoundRect(x0 - 8, yb - 12, x1 - x0 + 16, 34, 10, 10);
        int ci = 0;
        for (Clip cc : tl.clips) {
            int sx = x0 + (int) ((x1 - x0) * acc / total), ex = x0 + (int) ((x1 - x0) * (acc + cc.dur) / total);
            g.setColor(cc.title != null ? new Color(214, 170, 92, 150) : new Color(170, 160, 145, 90));
            g.fillRect(sx + 1, yb, Math.max(1, ex - sx - 2), 6);
            acc += cc.dur;
            ci++;
        }
        int px = x0 + (int) ((x1 - x0) * director.time() / total);
        g.setColor(new Color(245, 238, 225));
        g.fillOval(px - 5, yb - 2, 10, 10);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(220, 210, 195));
        String tm = String.format("%d:%02d / %d:%02d   ·   Klick auf die Leiste springt · Maus oder Taste übernimmt · Esc hält an",
                (int) director.time() / 60, (int) director.time() % 60, (int) total / 60, (int) total % 60);
        g.drawString(tm, x0, yb + 20);
    }

    private static java.util.List<String> wrap(Graphics2D g, String text, Font f, int width) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (text == null) return out;
        java.awt.FontMetrics fm = g.getFontMetrics(f);
        StringBuilder line = new StringBuilder();
        for (String w : text.split(" ")) {
            String t = line.length() == 0 ? w : line + " " + w;
            if (fm.stringWidth(t) > width && line.length() > 0) { out.add(line.toString()); line = new StringBuilder(w); }
            else { line = new StringBuilder(t); }
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }

    /** Legenden der Zugaben: Wärmebild, Zeitregler, Schnitt, Wasserweg. */
    private void extrasHud(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int W = getWidth();
        int y = 14;
        // Jahreszahl des Zeitreglers
        float ru = ruin;
        if (ru > 0.005f || ruinTarget > 0) {
            String yr = ru < 0.5f ? "um 216 n. Chr." : "heute";
            String sub = ru < 0.5f ? "Die Thermen in Betrieb" : "Die Ruine, 1800 Jahre später";
            if (ru > 0.05f && ru < 0.95f) sub = "Die Zeit vergeht …";
            com.dan.caracalla.db.CaracallaDb.Event ev = eventAt(ru);
            if (ev != null) { yr = ev.datierung; sub = ev.titel; }
            g.setFont(new Font("Serif", Font.PLAIN, 22));
            int w1 = g.getFontMetrics().stringWidth(yr);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            int w2 = g.getFontMetrics().stringWidth(sub);
            int bw = Math.max(w1, w2) + 28;
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRoundRect(W - bw - 14, y, bw, 56, 10, 10);
            g.setColor(new Color(236, 224, 200));
            g.setFont(new Font("Serif", Font.PLAIN, 22));
            g.drawString(yr, W - bw, y + 26);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(new Color(200, 190, 172));
            g.drawString(sub, W - bw, y + 44);
            // kleiner Balken 216 … heute
            g.setColor(new Color(214, 170, 92, 160));
            g.fillRect(W - bw, y + 49, (int) ((bw - 28) * ru), 2);
            y += 66;
            if (ev != null && ev.text != null) {
                java.util.List<String> lines = wrap(g, ev.text, new Font("SansSerif", Font.PLAIN, 12), 300);
                int th = 16 + lines.size() * 16 + (ev.quelle != null ? 16 : 0);
                g.setColor(new Color(0, 0, 0, 90));
                g.fillRoundRect(W - 328 - 14, y - 4, 328, th, 10, 10);
                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g.setColor(new Color(220, 210, 192));
                int ly = y + 12;
                for (String l : lines) { g.drawString(l, W - 328, ly); ly += 16; }
                if (ev.quelle != null) {
                    g.setColor(new Color(160, 150, 135));
                    g.setFont(new Font("SansSerif", Font.ITALIC, 11));
                    g.drawString("Quelle: " + ev.quelle, W - 328, ly);
                }
                y += th + 6;
            }
        }
        if (thermo) {
            int bw = 220, bh = 64, x = W - bw - 14;
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRoundRect(x, y, bw, bh, 10, 10);
            float[] c = new float[3];
            int gx = x + 14, gw = bw - 28;
            for (int i = 0; i < gw; i++) {
                float t = com.dan.caracalla.render.Thermal.T_MIN + (com.dan.caracalla.render.Thermal.T_MAX - com.dan.caracalla.render.Thermal.T_MIN) * i / (float) (gw - 1);
                com.dan.caracalla.render.Thermal.palette(t, c);
                g.setColor(new Color(c[0], c[1], c[2]));
                g.fillRect(gx + i, y + 26, 1, 12);
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.setColor(new Color(214, 170, 92));
            g.drawString("WÄRMEBILD", gx, y + 18);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(220, 210, 195));
            float to = com.dan.caracalla.render.Thermal.outside(hour, day);
            String o = String.format("außen %.0f °C", to);
            g.drawString(o, x + bw - 14 - g.getFontMetrics().stringWidth(o), y + 18);
            float lo = com.dan.caracalla.render.Thermal.T_MIN, hi0 = com.dan.caracalla.render.Thermal.T_MAX;
            g.drawString(String.format("%.0f °C", lo), gx, y + 53);
            String mid = String.format("%.0f", (lo + hi0) / 2);
            g.drawString(mid, gx + gw / 2 - g.getFontMetrics().stringWidth(mid) / 2, y + 53);
            String hi = String.format("%.0f °C +", hi0);
            g.drawString(hi, gx + gw - g.getFontMetrics().stringWidth(hi), y + 53);
            y += bh + 10;
        }
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        String title = null;
        if (waterPath && ru < 0.35f) {
            title = "WASSERWEG";
            rows.add(new String[]{"c", "Aqua Antoniniana auf Bögen, Zisternen unter dem Stadion"});
            rows.add(new String[]{"c", "Bleirohre unter dem Garten, kalt zur Natatio und zum Frigidarium"});
            rows.add(new String[]{"h", "über die Kessel der Praefurnien heiß ins Caldarium"});
            rows.add(new String[]{"w", "lauwarm zu den Becken neben dem Tepidarium"});
        }
        if (cutOn) {
            if (title != null) rows.add(new String[]{"", ""});
            rows.add(new String[]{"t", cutOn && title == null ? "SCHNITT" : "SCHNITT"});
            rows.add(new String[]{"f", "Praefurnium: das Feuer außen am Caldarium"});
            rows.add(new String[]{"f", "Hypokaustum: Heißluft zwischen Ziegelpfeilern"});
            rows.add(new String[]{"s", "Suspensura: der schwebende Boden darüber"});
            rows.add(new String[]{"s", "Tubuli: Hohlziegel leiten die Wärme in die Wände"});
        }
        if (!rows.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            int bw = 0;
            for (String[] r0 : rows) bw = Math.max(bw, g.getFontMetrics().stringWidth(r0[1]));
            bw += 50;
            int bh = 30 + rows.size() * 18 + (title != null ? 0 : -12);
            int x = W - bw - 14;
            g.setColor(new Color(0, 0, 0, 110));
            g.fillRoundRect(x, y, bw, bh, 10, 10);
            int ly = y + 20;
            if (title != null) {
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                g.setColor(new Color(214, 170, 92));
                g.drawString(title, x + 14, ly);
                ly += 18;
            }
            for (String[] r0 : rows) {
                if (r0[0].equals("t")) {
                    g.setFont(new Font("SansSerif", Font.BOLD, 11));
                    g.setColor(new Color(214, 170, 92));
                    g.drawString(r0[1], x + 14, ly);
                    ly += 18;
                    continue;
                }
                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                Color dot = switch (r0[0]) {
                    case "c" -> new Color(70, 185, 255);
                    case "h" -> new Color(255, 120, 40);
                    case "w" -> new Color(255, 200, 110);
                    case "f" -> new Color(255, 150, 60);
                    case "s" -> new Color(190, 150, 130);
                    default -> null;
                };
                if (dot != null) { g.setColor(dot); g.fillOval(x + 16, ly - 9, 9, 9); }
                g.setColor(new Color(225, 215, 195));
                g.drawString(r0[1], x + 34, ly);
                ly += 18;
            }
        }
    }

    /** Steckbrief: Stein, Herkunft, Karte des Wegs nach Rom. */
    private void stoneCardHud(Graphics2D g) {
        StoneCard sc = card;
        if (sc == null) return;
        long age = System.currentTimeMillis() - sc.since;
        if (age > 25000) { card = null; return; }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        com.dan.caracalla.db.CaracallaDb.Stone st = sc.stone;
        int bw = 380, x = 14, y = 64;
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        String title;
        Color swatch = new Color(200, 190, 175);
        if (st == null) {
            String t = sc.code.replace('_', ' ').toLowerCase();
            title = Character.toUpperCase(t.charAt(0)) + t.substring(1);
            rows.add(new String[]{"", "Herkunft, Weg nach Rom und Beschreibung kommen aus der Datenbank. " + dbState + "."});
        } else {
            title = st.name;
            try { swatch = Color.decode(st.farbe); } catch (Exception ignored) { }
            if (st.nameAntik != null) rows.add(new String[]{"i", st.nameAntik});
            String from = (st.quarryAntik != null ? st.quarryAntik + ", heute " : "") + st.quarryHeute + (st.land != null ? " (" + st.land + ")" : "");
            rows.add(new String[]{"b", "Aus " + from + (st.wegKm > 0 ? String.format(" · rund %,d km bis Rom", st.wegKm).replace(',', '.') : "")});
            if (st.beschreibung != null) rows.add(new String[]{"", st.beschreibung});
            if (st.wegText != null) rows.add(new String[]{"", st.wegText});
            if (st.inDenThermen != null) rows.add(new String[]{"g", st.inDenThermen});
        }
        Font fBody = new Font("SansSerif", Font.PLAIN, 12);
        java.util.List<Object[]> lines = new java.util.ArrayList<>();
        for (String[] r0 : rows) {
            Font f = r0[0].equals("i") ? new Font("Serif", Font.ITALIC, 14) : r0[0].equals("b") ? new Font("SansSerif", Font.BOLD, 12) : fBody;
            for (String l : wrap(g, r0[1], f, bw - 36)) lines.add(new Object[]{f, l, r0[0]});
            lines.add(new Object[]{null, "", ""});
        }
        boolean map = st != null && st.lon.length > 1;
        int mapH = map ? 150 : 0;
        int bh = 52 + lines.size() * 16 + mapH + 26;
        g.setColor(new Color(12, 11, 10, 200));
        g.fillRoundRect(x, y, bw, bh, 12, 12);
        g.setColor(swatch);
        g.fillRoundRect(x + 16, y + 16, 22, 22, 6, 6);
        g.setColor(new Color(214, 170, 92));
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.drawString("STECKBRIEF", x + 48, y + 20);
        g.setFont(new Font("Serif", Font.PLAIN, 21));
        g.setColor(new Color(240, 230, 210));
        g.drawString(title, x + 48, y + 40);
        int ly = y + 62;
        for (Object[] l : lines) {
            if (l[0] == null) { ly += 4; continue; }
            g.setFont((Font) l[0]);
            g.setColor("g".equals(l[2]) ? new Color(214, 170, 92) : "i".equals(l[2]) ? new Color(200, 190, 172) : new Color(222, 212, 194));
            g.drawString((String) l[1], x + 18, ly);
            ly += 16;
        }
        if (map) routeMap(g, st, x + 16, ly, bw - 32, mapH - 10, swatch);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(new Color(150, 140, 126));
        g.drawString("Klick schließt · Quelle: " + (st != null && st.quelle != null ? st.quelle.replaceFirst("^https?://", "").replaceFirst("/.*", "") : "–"), x + 18, y + bh - 10);
    }

    /** Kleine Karte: Längen- und Breitengrade, der Weg, Bruch und Rom. */
    private void routeMap(Graphics2D g, com.dan.caracalla.db.CaracallaDb.Stone st, int x, int y, int w, int h, Color col) {
        double lo0 = 12.49, lo1 = 12.49, la0 = 41.88, la1 = 41.88;
        for (int i = 0; i < st.lon.length; i++) {
            lo0 = Math.min(lo0, st.lon[i]); lo1 = Math.max(lo1, st.lon[i]);
            la0 = Math.min(la0, st.lat[i]); la1 = Math.max(la1, st.lat[i]);
        }
        double k = Math.cos(Math.toRadians((la0 + la1) / 2));
        double pad = 1.0;
        lo0 -= pad; lo1 += pad; la0 -= pad; la1 += pad;
        double sx = w / ((lo1 - lo0) * k), sy = h / (la1 - la0), s = Math.min(sx, sy);
        double ox = x + (w - (lo1 - lo0) * k * s) / 2, oy = y + (h - (la1 - la0) * s) / 2;
        final double fLo0 = lo0, fLa1 = la1;
        java.util.function.DoubleUnaryOperator px = lon -> ox + (lon - fLo0) * k * s;
        java.util.function.DoubleUnaryOperator py = lat -> oy + (fLa1 - lat) * s;
        g.setColor(new Color(24, 30, 36, 220));
        g.fillRoundRect(x, y, w, h, 8, 8);
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));
        double step = (lo1 - lo0) > 14 ? 5 : 2;
        for (double lo = Math.ceil(lo0 / step) * step; lo < lo1; lo += step) {
            int X = (int) px.applyAsDouble(lo);
            g.setColor(new Color(90, 110, 125, 90)); g.drawLine(X, y + 2, X, y + h - 2);
            g.setColor(new Color(120, 140, 150)); g.drawString((int) lo + "° O", X + 2, y + h - 4);
        }
        for (double la = Math.ceil(la0 / step) * step; la < la1; la += step) {
            int Y = (int) py.applyAsDouble(la);
            g.setColor(new Color(90, 110, 125, 90)); g.drawLine(x + 2, Y, x + w - 2, Y);
            g.setColor(new Color(120, 140, 150)); g.drawString((int) la + "° N", x + 3, Y - 2);
        }
        java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
        for (int i = 0; i < st.lon.length; i++) {
            double X = px.applyAsDouble(st.lon[i]), Y = py.applyAsDouble(st.lat[i]);
            if (i == 0) path.moveTo(X, Y); else path.lineTo(X, Y);
        }
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 230));
        g.draw(path);
        g.setStroke(new BasicStroke(1f));
        int qx = (int) px.applyAsDouble(st.lon[0]), qy = (int) py.applyAsDouble(st.lat[0]);
        int rx = (int) px.applyAsDouble(12.4925), ry = (int) py.applyAsDouble(41.879);
        g.setColor(new Color(240, 230, 210));
        g.fillOval(qx - 4, qy - 4, 8, 8);
        g.fillRect(rx - 4, ry - 4, 8, 8);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.drawString(st.quarryHeute, Math.min(qx + 7, x + w - g.getFontMetrics().stringWidth(st.quarryHeute) - 4), qy + 4);
        g.drawString("Rom", rx + 7, ry + 4);
    }

    private void hud(Graphics2D g, CameraController c) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String place = c.place().toUpperCase();
        g.setFont(new Font("Serif", Font.PLAIN, 22));
        int w = g.getFontMetrics().stringWidth(place);
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(14, 14, w + 28, 38, 10, 10);
        g.setColor(new Color(236, 224, 200));
        g.drawString(place, 28, 40);
        String pl = c.place();
        if (!pl.equals(lastPlace)) { lastPlace = pl; placeSince = System.currentTimeMillis(); }
        com.dan.caracalla.db.CaracallaDb.Snapshot sn = snap;
        long age = System.currentTimeMillis() - placeSince;
        if (sn != null && card == null && age < 12000 && sn.rooms.containsKey(pl) && sn.rooms.get(pl).funktion != null) {
            com.dan.caracalla.db.CaracallaDb.RoomInfo ri = sn.rooms.get(pl);
            float a = age > 10000 ? (12000 - age) / 2000f : 1;
            java.util.List<String> lines = wrap(g, ri.funktion, new Font("SansSerif", Font.PLAIN, 12), 330);
            String facts = (ri.tempLuft != null ? String.format("Luft %.0f °C", ri.tempLuft) : "")
                    + (ri.tempWasser != null ? String.format("%sWasser %.0f °C", ri.tempLuft != null ? " · " : "", ri.tempWasser) : "")
                    + (ri.nachhall != null ? String.format(" · Nachhall %.1f s", ri.nachhall) : "");
            int bh = 14 + lines.size() * 16 + (facts.isEmpty() ? 0 : 18);
            g.setColor(new Color(0, 0, 0, (int) (80 * a)));
            g.fillRoundRect(14, 58, 358, bh, 10, 10);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(new Color(225, 215, 195, (int) (255 * a)));
            int ly = 76;
            for (String l : lines) { g.drawString(l, 28, ly); ly += 16; }
            if (!facts.isEmpty()) { g.setColor(new Color(214, 170, 92, (int) (255 * a))); g.drawString(facts.replaceFirst("^ · ", ""), 28, ly + 2); }
        }
        g.setStroke(new BasicStroke(1f));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String hint = c.mode() == CameraController.Mode.ORBIT
                ? "Ziehen: drehen · Rechts ziehen: verschieben · Rad: Zoom · Doppelklick: Drehpunkt · Leertaste: Rundflug"
                : "Ziehen: umsehen · W A S D: gehen · Q E: tiefer, höher · Umschalt: schneller · O: nach außen";
        int hw = g.getFontMetrics().stringWidth(hint);
        g.setColor(new Color(0, 0, 0, 80));
        g.fillRoundRect(14, getHeight() - 36, hw + 20, 24, 8, 8);
        g.setColor(new Color(225, 215, 195));
        g.drawString(hint, 24, getHeight() - 19);
    }
}
