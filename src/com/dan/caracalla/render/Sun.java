package com.dan.caracalla.render;

/**
 * Sonnenstand über den Caracalla-Thermen (41,88° N) zu Tag im Jahr und wahrer Ortszeit
 * (Sonnenzeit, wie sie eine römische Sonnenuhr zeigt). Liefert die Richtung zur Sonne im
 * lokalen Koordinatensystem des Modells.
 */
public final class Sun {
    public static final double LAT = 41.879;
    /** Geografisches Azimut der lokalen +z-Achse (Richtung Caldarium), etwa Südwest. */
    public static final double AXIS_AZ = 225;

    public double elevationDeg, azimuthDeg;
    public final double[] dir = new double[3];

    public void set(int dayOfYear, double solarHour) {
        double decl = Math.toRadians(23.44) * Math.sin(2 * Math.PI * (284 + dayOfYear) / 365.0);
        double H = Math.toRadians(15 * (solarHour - 12));
        double phi = Math.toRadians(LAT);
        double e = -Math.cos(decl) * Math.sin(H);
        double n = Math.sin(decl) * Math.cos(phi) - Math.cos(decl) * Math.cos(H) * Math.sin(phi);
        double u = Math.sin(decl) * Math.sin(phi) + Math.cos(decl) * Math.cos(H) * Math.cos(phi);
        elevationDeg = Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, u))));
        double az = Math.toDegrees(Math.atan2(e, n));
        azimuthDeg = (az + 360) % 360;
        double zA = Math.toRadians(AXIS_AZ), xA = Math.toRadians(AXIS_AZ - 90);
        dir[0] = e * Math.sin(xA) + n * Math.cos(xA);
        dir[1] = u;
        dir[2] = e * Math.sin(zA) + n * Math.cos(zA);
        double l = Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1] + dir[2] * dir[2]);
        dir[0] /= l; dir[1] /= l; dir[2] /= l;
    }

    public static String dateLabel(int day) {
        int[] len = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        String[] mon = {"Jan.", "Feb.", "März", "Apr.", "Mai", "Juni", "Juli", "Aug.", "Sep.", "Okt.", "Nov.", "Dez."};
        int d = day, m = 0;
        while (m < 11 && d > len[m]) { d -= len[m]; m++; }
        return d + ". " + mon[m];
    }

    public static String timeLabel(double h) {
        int hh = (int) Math.floor(h), mm = (int) Math.round((h - hh) * 60);
        if (mm == 60) { hh++; mm = 0; }
        return String.format("%02d:%02d", hh, mm);
    }
}
