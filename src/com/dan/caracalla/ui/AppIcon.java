package com.dan.caracalla.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Das Programmsymbol, gemalt statt geladen: die Kuppel des Caldariums mit Kassetten auf der
 * Trommel mit drei Bogenfenstern, darüber Dampf, auf dunklem Grund mit warmem Abendlicht.
 * Liefert alle üblichen Größen, damit Windows für Taskleiste, Titel und Alt+Tab jeweils ein
 * scharfes Bild findet.
 */
public final class AppIcon {
    public static final int[] SIZES = {16, 20, 24, 32, 40, 48, 64, 128, 256};

    private AppIcon() { }

    /** Alle Größen für {@code frame.setIconImages(...)}. */
    public static List<Image> images() {
        List<Image> l = new ArrayList<>();
        for (int s : SIZES) l.add(paint(s));
        return l;
    }

    /** Setzt das Symbol am Fenster und, wo unterstützt, in der Taskleiste. */
    public static void install(java.awt.Window w) {
        List<Image> l = images();
        w.setIconImages(l);
        try {
            if (java.awt.Taskbar.isTaskbarSupported()) {
                java.awt.Taskbar tb = java.awt.Taskbar.getTaskbar();
                if (tb.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) tb.setIconImage(l.get(l.size() - 1));
            }
        } catch (Exception | Error ignored) {
            // Windows nimmt ohnehin die Fenstersymbole; das hier ist für macOS
        }
    }

    /** Malt das Symbol in der Kantenlänge s (quadratisch, mit Alpha). */
    public static BufferedImage paint(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.scale(s / 256.0, s / 256.0);
        boolean small = s <= 24;

        // Grund: abgerundetes Quadrat, Nachthimmel von Weinrot nach Petrol
        RoundRectangle2D bg = new RoundRectangle2D.Double(6, 6, 244, 244, 56, 56);
        g.setPaint(new GradientPaint(0, 6, new Color(92, 22, 48), 0, 250, new Color(10, 58, 66)));
        g.fill(bg);
        // Abendsonne hinter der Kuppel
        g.setClip(bg);
        g.setPaint(new RadialGradientPaint(128, 118, 118, new float[]{0f, 0.55f, 1f},
                new Color[]{new Color(255, 196, 110, 170), new Color(214, 120, 70, 60), new Color(0, 0, 0, 0)}));
        g.fill(new Rectangle2D.Double(0, 0, 256, 256));

        // Dampf über der Kuppel
        if (!small) {
            g.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Color steam = new Color(170, 235, 235, 150);
            g.setColor(steam);
            for (int i = 0; i < 3; i++) {
                double x = 100 + i * 28;
                Path2D p = new Path2D.Double();
                p.moveTo(x, 58);
                p.curveTo(x - 12, 46, x + 12, 36, x, 22);
                g.draw(p);
            }
        }

        // Trommel
        double dx0 = 50, dx1 = 206, dy0 = 132, dy1 = 206;
        g.setPaint(new GradientPaint((float) dx0, 0, new Color(236, 214, 170), (float) dx1, 0, new Color(170, 128, 86)));
        g.fill(new Rectangle2D.Double(dx0, dy0, dx1 - dx0, dy1 - dy0));
        // drei Bogenfenster mit warmem Licht
        for (int i = 0; i < 3; i++) {
            double cx = 84 + i * 44, w = 26, top = 150, bot = 202;
            Path2D win = new Path2D.Double();
            win.moveTo(cx - w / 2, bot);
            win.lineTo(cx - w / 2, top + w / 2);
            win.append(new Arc2D.Double(cx - w / 2, top, w, w, 180, -180, Arc2D.OPEN), true);
            win.lineTo(cx + w / 2, bot);
            win.closePath();
            g.setPaint(new GradientPaint(0, (float) top, new Color(255, 214, 120), 0, (float) bot, new Color(214, 110, 40)));
            g.fill(win);
        }
        // Gesims
        g.setColor(new Color(250, 236, 206));
        g.fill(new Rectangle2D.Double(dx0 - 8, dy0 - 10, dx1 - dx0 + 16, 12));

        // Kuppel: Halbkreis mit Ziegelrot, Kassetten als Bänder
        double r = 86, cx = 128, cy = 122;
        Arc2D dome = new Arc2D.Double(cx - r, cy - r, 2 * r, 2 * r, 0, 180, Arc2D.CHORD);
        g.setPaint(new GradientPaint((float) (cx - r), (float) (cy - r), new Color(226, 132, 78), (float) (cx + r), (float) cy, new Color(128, 52, 34)));
        g.fill(dome);
        if (!small) {
            g.setClip(dome);
            g.setColor(new Color(96, 34, 24, 150));
            g.setStroke(new BasicStroke(5));
            for (int k = 1; k <= 3; k++) {
                double rr = r * k / 4.0;
                g.draw(new Arc2D.Double(cx - r, cy - rr, 2 * r, 2 * rr, 0, 180, Arc2D.OPEN));
            }
            for (int k = -3; k <= 3; k++) {
                double a = Math.toRadians(90 + k * 22);
                g.draw(new java.awt.geom.Line2D.Double(cx, cy, cx + Math.cos(a) * r, cy - Math.sin(a) * r));
            }
            g.setClip(bg);
        }
        // Opaion und Lichtkante
        g.setColor(new Color(255, 230, 180));
        g.fill(new Ellipse2D.Double(cx - 9, cy - r - 5, 18, 10));
        g.setStroke(new BasicStroke(small ? 10 : 5));
        g.setColor(new Color(255, 214, 150, 200));
        g.draw(new Arc2D.Double(cx - r, cy - r, 2 * r, 2 * r, 95, 80, Arc2D.OPEN));

        // Wasser vorne
        g.setPaint(new GradientPaint(0, 206, new Color(40, 190, 196), 0, 250, new Color(8, 90, 104)));
        g.fill(new Rectangle2D.Double(0, 206, 256, 50));
        if (!small) {
            g.setColor(new Color(200, 250, 250, 170));
            g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new java.awt.geom.Line2D.Double(70, 222, 118, 222));
            g.draw(new java.awt.geom.Line2D.Double(140, 234, 186, 234));
        }

        // Glaskante wie im F-Stil
        g.setClip(null);
        g.setStroke(new BasicStroke(small ? 10 : 6));
        g.setPaint(new GradientPaint(0, 6, new Color(255, 255, 255, 110), 0, 250, new Color(255, 255, 255, 20)));
        g.draw(bg);
        g.dispose();
        return img;
    }
}
