package com.dan.caracalla.tools;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 * Bestandsaufnahme im Schema DEMO, nur lesend: Präfixe der vorhandenen Tabellen, Objekte mit CAR_,
 * Spatial-Metadaten, Platz und Rechte. Schreibt nach db/bestand.txt im Projektordner.
 * <p>Aufruf in NetBeans: Rechtsklick auf die Datei, „Run File“. Andere Verbindung:
 * Argumente url benutzer passwort.</p>
 */
public final class DbBestand {
    public static void main(String[] a) throws Exception {
        String url = a.length > 0 ? a[0] : "jdbc:oracle:thin:@//localhost:1521/PDBORCL";
        String user = a.length > 1 ? a[1] : "DEMO";
        String pwd = a.length > 2 ? a[2] : "de";
        File out = new File("db/bestand.txt");
        out.getParentFile().mkdirs();
        try (Connection c = DriverManager.getConnection(url, user, pwd);
             PrintWriter w = new PrintWriter(out, StandardCharsets.UTF_8)) {
            c.setReadOnly(true);
            q(c, w, "Datenbank", "select banner_full from v$version");
            q(c, w, "Sitzung", "select user, sys_context('USERENV','CON_NAME') pdb, sys_context('USERENV','DB_NAME') db, to_char(sysdate,'YYYY-MM-DD HH24:MI') jetzt from dual");
            q(c, w, "Tablespace und Quote", "select u.default_tablespace, q.tablespace_name, round(q.bytes/1048576) mb_belegt, "
                    + "case q.max_bytes when -1 then 'unbegrenzt' else to_char(round(q.max_bytes/1048576)) end mb_max "
                    + "from user_users u left join user_ts_quotas q on 1=1");
            q(c, w, "Tabellen nach Präfix", "select nvl(regexp_substr(table_name,'^[A-Z0-9]+_'),'(ohne)') praefix, count(*) tabellen, "
                    + "sum(nvl(num_rows,0)) zeilen_statistik from user_tables group by nvl(regexp_substr(table_name,'^[A-Z0-9]+_'),'(ohne)') order by 1");
            q(c, w, "Objekte nach Typ", "select object_type, count(*) anzahl, sum(case when status<>'VALID' then 1 else 0 end) ungueltig "
                    + "from user_objects group by object_type order by 1");
            q(c, w, "Alles mit CAR_ (sollte leer sein)", "select object_type, object_name, status from user_objects where object_name like 'CAR\\_%' escape '\\' order by 1,2");
            q(c, w, "Spatial-Metadaten nach Präfix", "select nvl(regexp_substr(table_name,'^[A-Z0-9]+_'),'(ohne)') praefix, count(*) eintraege, "
                    + "min(srid) srid_min, max(srid) srid_max from user_sdo_geom_metadata group by nvl(regexp_substr(table_name,'^[A-Z0-9]+_'),'(ohne)') order by 1");
            q(c, w, "Spatial vorhanden", "select count(*) sdo_geometry_typ from all_types where owner='MDSYS' and type_name='SDO_GEOMETRY'");
            q(c, w, "Rechte der Sitzung", "select privilege from session_privs order by 1");
            q(c, w, "Rollen", "select granted_role from user_role_privs order by 1");
            q(c, w, "Vorbild VUL_: Tabellen und Spalten", "select table_name, count(*) spalten from user_tab_columns where table_name like 'VUL\\_%' escape '\\' group by table_name order by 1");
        }
        System.out.println("Geschrieben: " + out.getAbsolutePath());
    }

    private static void q(Connection c, PrintWriter w, String title, String sql) {
        w.println("== " + title);
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            ResultSetMetaData md = r.getMetaData();
            int n = md.getColumnCount();
            StringBuilder h = new StringBuilder();
            for (int i = 1; i <= n; i++) h.append(i > 1 ? " | " : "").append(md.getColumnLabel(i));
            w.println(h);
            int rows = 0;
            while (r.next() && rows < 400) {
                StringBuilder b = new StringBuilder();
                for (int i = 1; i <= n; i++) b.append(i > 1 ? " | " : "").append(r.getString(i));
                w.println(b);
                rows++;
            }
            if (rows == 0) w.println("(keine Zeilen)");
        } catch (Exception e) {
            w.println("(nicht lesbar: " + e.getMessage().trim() + ")");
        }
        w.println();
    }
}
