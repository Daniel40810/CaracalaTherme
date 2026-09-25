package com.dan.caracalla.tools;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/** Startet die Anwendung und bedient sie mit Tasten und Mausklicks; macht Bildschirmfotos. Nur zum Testen. */
public final class GuiShot {
    public static void main(String[] a) throws Exception {
        File dir = new File(a[0]);
        dir.mkdirs();
        com.dan.caracalla.CaracallaApp.main(new String[0]);
        Robot r = new Robot();
        r.setAutoDelay(40);
        for (int i = 1; i < a.length; i++) {
            String[] q = a[i].split(":");
            switch (q[0]) {
                case "wait": Thread.sleep(Long.parseLong(q[1])); break;
                case "key": {
                    int k = KeyEvent.getExtendedKeyCodeForChar(q[1].charAt(0));
                    if (q[1].length() > 1) k = (int) KeyEvent.class.getField("VK_" + q[1]).get(null);
                    r.keyPress(k); r.keyRelease(k); break;
                }
                case "click": {
                    r.mouseMove(Integer.parseInt(q[1]), Integer.parseInt(q[2]));
                    r.mousePress(InputEvent.BUTTON1_DOWN_MASK); r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); break;
                }
                case "scroll": {
                    r.mouseMove(Integer.parseInt(q[1]), Integer.parseInt(q[2]));
                    r.mouseWheel(Integer.parseInt(q[3])); break;
                }
                case "shot": {
                    Rectangle s = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    ImageIO.write(r.createScreenCapture(s), "png", new File(dir, q[1] + ".png"));
                    System.out.println("Bild " + q[1]);
                    break;
                }
                default: System.out.println("?" + a[i]);
            }
        }
        System.exit(0);
    }
}
