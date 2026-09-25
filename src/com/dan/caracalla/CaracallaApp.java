package com.dan.caracalla;

import com.dan.caracalla.ui.ControlPanel;
import com.dan.caracalla.ui.ScenePanel;
import com.dan.fframe.FFrame;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Caracalla-Thermen — 3D-Rekonstruktion der Thermae Antoninianae (216 n. Chr.).
 * Einstieg der Anwendung.
 */
public final class CaracallaApp {

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        SwingUtilities.invokeLater(CaracallaApp::open);
    }

    private static void open() {
        FFrame f = new FFrame("Caracalla-Thermen · Thermae Antoninianae");
        ScenePanel scene = new ScenePanel();
        ControlPanel controls = new ControlPanel(scene);
        JLabel status = new JLabel(" ");
        status.setForeground(new Color(170, 162, 150));
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        status.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        scene.setStatusListener(status::setText);
        scene.setOnReady(controls::modelReady);

        JPanel root = f.getComponentPane();
        root.setLayout(new BorderLayout());
        root.setBackground(new Color(22, 23, 27));
        root.add(scene, BorderLayout.CENTER);
        javax.swing.JScrollPane side = new javax.swing.JScrollPane(controls,
                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        side.setBorder(BorderFactory.createEmptyBorder());
        side.getViewport().setBackground(new Color(22, 23, 27));
        com.dan.fscrollbar.FScrollBar bar = new com.dan.fscrollbar.FScrollBar(javax.swing.JScrollBar.VERTICAL);
        bar.setUnitIncrement(16);
        bar.setBlockIncrement(120);
        side.setVerticalScrollBar(bar);
        side.setBackground(new Color(22, 23, 27));
        side.setPreferredSize(new Dimension(290, 600));
        root.add(side, BorderLayout.EAST);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(new Color(16, 17, 20));
        south.add(status, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);

        scene.setCinemaListener(on -> {
            side.setVisible(!on);
            south.setVisible(!on);
            root.revalidate();
            scene.requestFocusInWindow();
        });

        f.setPreferredFrameSize(new Dimension(1480, 900));
        f.setSize(1480, 900);
        f.setLocationRelativeTo(null);
        f.setResizable(true);
        f.setVisible(true);
        startMaximized(f);
        scene.start();
        scene.requestFocusInWindow();
    }

    /**
     * Startet maximiert (ExtendedState MAXIMIZED_BOTH), ohne die Taskleiste zu verdecken. Die normale
     * Größe von 1480 × 900 bleibt als Rückfall erhalten; die Schaltfläche „Wiederherstellen“ des
     * FFrame kennt den Zustand und stellt die normale Größe wieder her, danach ist das Fenster frei
     * veränderbar.
     */
    private static void startMaximized(FFrame f) {
        java.awt.Rectangle normal = f.getBounds();
        f.setMaximizedBounds(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        f.setExtendedState(f.getExtendedState() | java.awt.Frame.MAXIMIZED_BOTH);
        try {
            // Die Titelleiste des FFrame führt einen eigenen Maximiert-Zustand; ihn angleichen
            for (java.lang.reflect.Field fd : FFrame.class.getDeclaredFields()) {
                if (!fd.getType().getSimpleName().equals("FTaskbar")) continue;
                fd.setAccessible(true);
                Object bar = fd.get(f);
                Class<?> bc = bar.getClass();
                java.lang.reflect.Field mx = bc.getDeclaredField("maximized"), rb = bc.getDeclaredField("restoreBounds"),
                        btn = bc.getDeclaredField("btnMaximize");
                mx.setAccessible(true); rb.setAccessible(true); btn.setAccessible(true);
                mx.setBoolean(bar, true);
                rb.set(bar, normal);
                Object icon = btn.get(bar);
                icon.getClass().getMethod("setType", com.dan.ficons.FIconType.class).invoke(icon, com.dan.ficons.FIconType.RESTORE);
                ((javax.swing.JComponent) icon).setToolTipText("Wiederherstellen");
                // Beim Wiederherstellen auch den ExtendedState zurücksetzen
                ((java.awt.Component) icon).addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                        SwingUtilities.invokeLater(() -> {
                            if ((f.getExtendedState() & java.awt.Frame.MAXIMIZED_BOTH) != 0) {
                                f.setExtendedState(f.getExtendedState() & ~java.awt.Frame.MAXIMIZED_BOTH);
                                f.setBounds(normal);
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("FFrame-Titelleiste nicht angeglichen: " + e);
        }
    }

    private CaracallaApp() { }
}
