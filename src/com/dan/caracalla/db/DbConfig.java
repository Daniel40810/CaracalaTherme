package com.dan.caracalla.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Verbindungsdaten aus <code>db/db.properties</code> im Projektordner (oder der Datei aus der
 * Systemeigenschaft <code>caracalla.db</code>). Fehlt die Datei, läuft die App ohne Datenbank.
 * <pre>
 * url=jdbc:oracle:thin:@//localhost:1521/PDBORCL
 * user=DEMO
 * password=...
 * </pre>
 */
public final class DbConfig {
    public final String url, user, password;
    public final File file;

    private DbConfig(File file, String url, String user, String password) {
        this.file = file; this.url = url; this.user = user; this.password = password;
    }

    /** Liest die Datei; null, wenn sie fehlt oder unvollständig ist. */
    public static DbConfig load() {
        File f = new File(System.getProperty("caracalla.db", "db/db.properties"));
        if (!f.isFile()) return null;
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(f)) {
            p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
        String url = p.getProperty("url"), user = p.getProperty("user"), pwd = p.getProperty("password");
        if (url == null || user == null || pwd == null) return null;
        return new DbConfig(f, url.trim(), user.trim(), pwd);
    }

    /** Kurzform für die Statuszeile, ohne Passwort. */
    public String label() {
        String svc = url.contains("/") ? url.substring(url.lastIndexOf('/') + 1) : url;
        return user + "@" + svc;
    }
}
