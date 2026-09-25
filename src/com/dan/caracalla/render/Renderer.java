package com.dan.caracalla.render;

import com.dan.caracalla.geo.Mat;
import com.dan.caracalla.geo.Mesh;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.stream.IntStream;

/**
 * Software-Renderer mit verzögerter Schattierung: Rasterung in einen G-Puffer
 * (Tiefe, Normale, Material, Himmelssicht, Lichtrückwurf), dann Beleuchtung je Pixel mit
 * Sonne, Schattenkarte, Himmelslicht, zurückgeworfenem Licht, Glanz und Wasser.
 * Danach Lichtstrahlen in Dampf und Luft (Viertelauflösung), Luftperspektive, Bloom,
 * Belichtungsautomatik und Filmkurve.
 */
public final class Renderer {
    public final Mesh mesh;
    public boolean showRoof = true;
    public boolean volumetric = true;
    public boolean bloom = true;
    /** Wellen und Tropfen; null = ruhiges Wasser. */
    public WaterSim water;
    /** Dampfteilchen und Dichtegitter; null = einfaches Rauschfeld. */
    public SteamSim steam;
    public boolean steamSprites = true;

    // ------------------------------------------------------------ Zugaben
    /** Schnitt: alles mit x > cutX im Bereich des Zentralbaus ist fort, die Schnittflächen werden gefüllt. */
    public boolean cut;
    public float cutX;
    /** Zeitregler: 0 = 216 n. Chr., 1 = heute. */
    public float ruin;
    /** Wärmebild statt Farbbild. */
    public boolean thermo;
    /** Wasserweg und Heißluft als leuchtende Teilchen; null = aus. */
    public Streams waterFlow, heatFlow;
    /** Schürlöcher der Praefurnien (x, y, z, Richtung x, z). */
    public java.util.List<double[]> furnaces = java.util.Collections.emptyList();
    /** Sonnenzeit und Tag für das Wärmebild. */
    public double hour = 15, dayOfYear = 172;
    private static final double[] CB = com.dan.caracalla.model.ThermenModel.CUT_BOX;
    private final float cbX1 = (float) CB[2] + 0.05f, cbZ0 = (float) CB[1] - 0.05f, cbZ1 = (float) CB[3] + 0.05f;
    private float[] colA = new float[0];
    private float tout, heatOn, ie = 1;

    private static final int VA = 10; // Werte je Ecke im Aufbau: x y z nx ny nz sky br bg bb

    private int W, H;
    private float[] gz, gnx, gny, gnz, gsk, gbr, gbg, gbb;
    private byte[] gm;
    private float[] hr, hg, hb;
    private final BufferedImage[] images = new BufferedImage[2];
    private int cur;

    private float[] vx = new float[0], vy = new float[0], vz = new float[0];
    private float[] st = new float[3 * VA * 4096];
    private byte[] sm = new byte[4096];
    private int nst;

    // Lichtstrahlen und Bloom in Viertelauflösung
    private int QW, QH;
    private float[] qr, qg, qb, qt, qd, qh, tr, tg, tb;
    private float[] cr, cg, cb;
    private int frame;

    private double exposure = 1;
    private boolean exposureValid;
    private final int strips;
    private static final float[] GAMMA = new float[4097];

    static {
        for (int i = 0; i <= 4096; i++) {
            double c = i / 4096.0;
            GAMMA[i] = (float) (c <= 0.0031308 ? 12.92 * c : 1.055 * Math.pow(c, 1 / 2.4) - 0.055);
        }
    }

    // Beleuchtung: aktuell, wartend, frei
    private Lighting light, pending, spare;
    private Lighting L; // für den laufenden Frame

    // Kamera für den laufenden Frame
    private double ex, ey, ez, cfx, cfy, cfz, crx, cry, crz, cux, cuy, cuz, pfx, pfy;
    private float time;

    public Renderer(Mesh mesh, int shadowSize) {
        this.mesh = mesh;
        this.light = new Lighting(shadowSize, mesh.nv);
        this.spare = new Lighting(shadowSize, mesh.nv);
        this.strips = Math.max(8, Runtime.getRuntime().availableProcessors() * 3);
    }

    public int width() { return W; }
    public int height() { return H; }

    public void setSize(int w, int h) {
        if (w == W && h == H) return;
        W = w; H = h;
        int n = w * h;
        gz = new float[n]; gnx = new float[n]; gny = new float[n]; gnz = new float[n]; gsk = new float[n];
        gbr = new float[n]; gbg = new float[n]; gbb = new float[n];
        gm = new byte[n]; hr = new float[n]; hg = new float[n]; hb = new float[n];
        images[0] = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        images[1] = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        QW = (w + 3) / 4; QH = (h + 3) / 4;
        int q = QW * QH;
        qr = new float[q]; qg = new float[q]; qb = new float[q]; qt = new float[q]; qd = new float[q]; qh = new float[q];
        cr = new float[n]; cg = new float[n]; cb = new float[n];
        tr = new float[q]; tg = new float[q]; tb = new float[q];
    }

    /** Sonnenstand sofort übernehmen (für Standbilder). */
    public synchronized void setSun(double[] dir, double haze) {
        light.ruin = ruin;
        light.compute(mesh, dir, haze);
    }

    public void setSun(double[] dir) { setSun(dir, 0); }

    /** Für den Hintergrund-Thread: freies Beleuchtungsobjekt holen (oder null, wenn gerade keins frei ist). */
    public synchronized Lighting takeSpare() {
        Lighting s = spare;
        spare = null;
        return s;
    }

    /** Fertig berechnete Beleuchtung übergeben; sie gilt ab dem nächsten Bild. */
    public synchronized void offer(Lighting l) { pending = l; }

    private synchronized Lighting acquire() {
        if (pending != null) {
            spare = light;
            light = pending;
            pending = null;
        }
        return light;
    }

    public void resetExposure() { exposureValid = false; }

    /** Rendert ein Bild und gibt es zurück (abwechselnd einer von zwei Puffern). */
    public BufferedImage render(Camera cam, double t, double dt) {
        L = acquire();
        time = (float) t;
        frame++;
        cam.update();
        tout = Thermal.outside(hour, (int) dayOfYear);
        if (thermo) Thermal.setRange(tout);
        heatOn = Ruin.working(ruin);
        ie = thermo ? 1f : (float) (1 / exposure);
        ex = cam.ex; ey = cam.ey; ez = cam.ez;
        cfx = cam.fx; cfy = cam.fy; cfz = cam.fz;
        crx = cam.rx; cry = cam.ry; crz = cam.rz;
        cux = cam.ux; cuy = cam.uy; cuz = cam.uz;
        double tanY = Math.tan(cam.fovY / 2), tanX = tanY * W / (double) H;
        pfx = (W / 2.0) / tanX;
        pfy = (H / 2.0) / tanY;
        if (colA.length != W) colA = new float[W];
        for (int px = 0; px < W; px++) colA[px] = (float) ((px + 0.5 - W / 2.0) / pfx);
        transform();
        setup(cam.near, tanX, tanY);
        java.util.Arrays.fill(gz, Float.MAX_VALUE);
        java.util.Arrays.fill(gm, (byte) 0);
        int rowsPer = (H + strips - 1) / strips;
        IntStream.range(0, strips).parallel().forEach(s -> rasterStrip(s * rowsPer, Math.min(H, (s + 1) * rowsPer)));
        if (water != null && dt > 0) water.step(dt);
        if (steam != null && dt > 0) steam.step(dt);
        if (waterFlow != null && dt > 0) waterFlow.step(dt);
        if (heatFlow != null && dt > 0) heatFlow.step(dt);
        IntStream.range(0, strips).parallel().forEach(s -> shadeStrip(s * rowsPer, Math.min(H, (s + 1) * rowsPer)));
        IntStream.range(0, strips).parallel().forEach(s -> shadeWaterStrip(s * rowsPer, Math.min(H, (s + 1) * rowsPer)));
        boolean live = !thermo;
        if (water != null && live && ruin < 0.3f) drawDrops();
        if (steam != null && steamSprites && live) drawSteam();
        int qPer = (QH + strips - 1) / strips;
        if (volumetric && live) {
            IntStream.range(0, strips).parallel().forEach(s -> marchStrip(s * qPer, Math.min(QH, (s + 1) * qPer)));
            blurQuarter();
            IntStream.range(0, strips).parallel().forEach(s -> applyScatter(s * rowsPer, Math.min(H, (s + 1) * rowsPer)));
            if (steam != null) shimmer(rowsPer);
        }
        if (heatFlow != null) drawStreams(heatFlow, 0.13f, true);
        if (waterFlow != null) drawStreams(waterFlow, 0.2f, false);
        if (live) adaptExposure(dt);
        if (bloom && live) doBloom(rowsPer, qPer);
        cur ^= 1;
        BufferedImage img = images[cur];
        int[] out = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        IntStream.range(0, strips).parallel().forEach(s -> tonemap(out, s * rowsPer, Math.min(H, (s + 1) * rowsPer)));
        return img;
    }

    // ------------------------------------------------------------ Geometrie

    private void transform() {
        int nv = mesh.nv;
        if (vx.length < nv) { vx = new float[nv]; vy = new float[nv]; vz = new float[nv]; }
        float[] p = mesh.pos;
        for (int v = 0; v < nv; v++) {
            double dx = p[3 * v] - ex, dy = p[3 * v + 1] - ey, dz = p[3 * v + 2] - ez;
            vx[v] = (float) (dx * crx + dy * cry + dz * crz);
            vy[v] = (float) (dx * cux + dy * cuy + dz * cuz);
            vz[v] = (float) (dx * cfx + dy * cfy + dz * cfz);
        }
    }

    private boolean chunkVisible(int c, double[][] planes) {
        float[] b = mesh.chunkBox;
        int o = 6 * c;
        for (double[] n : planes) {
            double px = n[0] >= 0 ? b[o + 3] : b[o], py = n[1] >= 0 ? b[o + 4] : b[o + 1], pz = n[2] >= 0 ? b[o + 5] : b[o + 2];
            if ((px - ex) * n[0] + (py - ey) * n[1] + (pz - ez) * n[2] < 0) return false;
        }
        return true;
    }

    private void setup(double near, double tanX, double tanY) {
        nst = 0;
        float nr = (float) near;
        float[] cv = new float[VA * 3];
        float[] poly = new float[VA * 5];
        double[][] planes = {
                {cfx, cfy, cfz},
                {cfx * tanX - crx, cfy * tanX - cry, cfz * tanX - crz},
                {cfx * tanX + crx, cfy * tanX + cry, cfz * tanX + crz},
                {cfx * tanY - cux, cfy * tanY - cuy, cfz * tanY - cuz},
                {cfx * tanY + cux, cfy * tanY + cuy, cfz * tanY + cuz}};
        float[] bo = L.bounce;
        float[] cb = mesh.chunkBox;
        float minH = ruin > 0.01f ? 3.5f + (1 - Ruin.smooth((ruin - 0.12f) / 0.82f)) * 70 : Float.MAX_VALUE;
        for (int c = 0; c < mesh.nChunks; c++) {
            int grp = mesh.chunkGrp[c];
            if (!showRoof && grp == Mat.G_ROOF) continue;
            if (grp == Mat.G_ROOF && ruin > 0.63f) continue;
            if (grp == Mat.G_UNDER && !cut) continue;
            int o = 6 * c;
            if (cut && grp != Mat.G_UNDER && cb[o] > cutX && cb[o + 3] < cbX1 && cb[o + 2] > cbZ0 && cb[o + 5] < cbZ1) continue;
            if (!chunkVisible(c, planes)) continue;
            boolean caps = (cut && cb[o + 3] > cutX && cb[o] < cbX1 && cb[o + 5] > cbZ0 && cb[o + 2] < cbZ1) || cb[o + 4] > minH;
            int t0 = mesh.chunkStart[c], t1 = t0 + mesh.chunkCount[c];
            for (int t = t0; t < t1; t++) {
                int a = mesh.idx[3 * t], b = mesh.idx[3 * t + 1], cc = mesh.idx[3 * t + 2];
                int pa = 3 * a;
                double dot = (ex - mesh.pos[pa]) * mesh.fn[3 * t] + (ey - mesh.pos[pa + 1]) * mesh.fn[3 * t + 1]
                        + (ez - mesh.pos[pa + 2]) * mesh.fn[3 * t + 2];
                boolean back = false;
                if (dot <= 0) {
                    if (!caps) continue;
                    back = true;
                }
                float za = vz[a], zb = vz[b], zc = vz[cc];
                if (za < nr && zb < nr && zc < nr) continue;
                load(cv, 0, a, bo); load(cv, VA, b, bo); load(cv, 2 * VA, cc, bo);
                int m = mesh.mat[t] | (mesh.grp[t] == Mat.G_ROOF ? 32 : 0) | (back ? 64 : 0);
                if (za >= nr && zb >= nr && zc >= nr) {
                    emit(cv, 0, VA, 2 * VA, m);
                } else {
                    int n = clipNear(cv, poly, nr);
                    for (int k = 1; k + 1 < n; k++) emit(poly, 0, VA * k, VA * (k + 1), m);
                }
            }
        }
    }

    private void load(float[] d, int o, int v, float[] bo) {
        d[o] = vx[v]; d[o + 1] = vy[v]; d[o + 2] = vz[v];
        d[o + 3] = mesh.nrm[3 * v]; d[o + 4] = mesh.nrm[3 * v + 1]; d[o + 5] = mesh.nrm[3 * v + 2];
        d[o + 6] = mesh.sky[v];
        d[o + 7] = bo[3 * v]; d[o + 8] = bo[3 * v + 1]; d[o + 9] = bo[3 * v + 2];
    }

    /** Schneidet das Dreieck an der Nahebene; liefert Eckenzahl in poly. */
    private static int clipNear(float[] tri, float[] poly, float nr) {
        int n = 0;
        for (int i = 0; i < 3; i++) {
            int a = VA * i, b = VA * ((i + 1) % 3);
            boolean ia = tri[a + 2] >= nr, ib = tri[b + 2] >= nr;
            if (ia) { System.arraycopy(tri, a, poly, VA * n, VA); n++; }
            if (ia != ib) {
                float t = (nr - tri[a + 2]) / (tri[b + 2] - tri[a + 2]);
                for (int k = 0; k < VA; k++) poly[VA * n + k] = tri[a + k] + (tri[b + k] - tri[a + k]) * t;
                n++;
            }
        }
        return n;
    }

    private void emit(float[] v, int a, int b, int c, int m) {
        int S = 3 * VA;
        if (S * nst + S > st.length) {
            st = java.util.Arrays.copyOf(st, st.length * 2);
            sm = java.util.Arrays.copyOf(sm, sm.length * 2);
        }
        int o = S * nst;
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int k = 0; k < 3; k++) {
            int src = k == 0 ? a : (k == 1 ? b : c);
            float iz = 1f / v[src + 2];
            float sx = (float) (W / 2.0 + v[src] * iz * pfx);
            float sy = (float) (H / 2.0 - v[src + 1] * iz * pfy);
            st[o] = sx; st[o + 1] = sy; st[o + 2] = iz;
            for (int q = 3; q < VA; q++) st[o + q] = v[src + q];
            minX = Math.min(minX, sx); maxX = Math.max(maxX, sx); minY = Math.min(minY, sy); maxY = Math.max(maxY, sy);
            o += VA;
        }
        if (maxX < 0 || minX > W || maxY < 0 || minY > H) return;
        sm[nst] = (byte) m;
        nst++;
    }

    // ------------------------------------------------------------ Rasterung

    private void rasterStrip(int y0, int y1) {
        final float[] st = this.st;
        final boolean clip = cut || ruin > 0.01f;
        final int S = 3 * VA;
        for (int t = 0; t < nst; t++) {
            int o = S * t;
            int o1 = o + VA, o2 = o + 2 * VA;
            float x0 = st[o], yy0 = st[o + 1], x1 = st[o1], yy1 = st[o1 + 1], x2 = st[o2], yy2 = st[o2 + 1];
            float minY = Math.min(yy0, Math.min(yy1, yy2)), maxY = Math.max(yy0, Math.max(yy1, yy2));
            if (maxY < y0 - 0.5f || minY > y1 + 0.5f) continue;
            float area = (x1 - x0) * (yy2 - yy0) - (x2 - x0) * (yy1 - yy0);
            if (Math.abs(area) < 1e-9f) continue;
            float inv = 1f / area;
            int ya = Math.max(y0, (int) Math.ceil(minY - 0.5f)), yb = Math.min(y1 - 1, (int) Math.floor(maxY - 0.5f));
            int xa0 = Math.max(0, (int) Math.ceil(Math.min(x0, Math.min(x1, x2)) - 0.5f));
            int xb0 = Math.min(W - 1, (int) Math.floor(Math.max(x0, Math.max(x1, x2)) - 0.5f));
            if (xa0 > xb0 || ya > yb) continue;
            float iz0 = st[o + 2], iz1 = st[o1 + 2], iz2 = st[o2 + 2];
            int code = sm[t];
            byte m = (byte) (code + 1);
            int mm = code & 31;
            boolean roofT = (code & 32) != 0, backT = (code & 64) != 0;
            float B0 = -(yy2 - yy1) * inv, B1 = -(yy0 - yy2) * inv, B2 = -(yy1 - yy0) * inv;
            for (int py = ya; py <= yb; py++) {
                float cy = py + 0.5f;
                float rb = (float) (-(cy - H / 2.0) / pfy);
                float rbx = (float) (cfx + cux * rb), rby = (float) (cfy + cuy * rb), rbz = (float) (cfz + cuz * rb);
                float A0 = ((x2 - x1) * (cy - yy1) + (yy2 - yy1) * x1) * inv;
                float A1 = ((x0 - x2) * (cy - yy2) + (yy0 - yy2) * x2) * inv;
                float A2 = ((x1 - x0) * (cy - yy0) + (yy1 - yy0) * x0) * inv;
                long span = Span.of(A0, B0, A1, B1, A2, B2, xa0 + 0.5f, xb0 + 0.5f);
                if (span == Span.EMPTY) continue;
                int xa = Math.max(xa0, Span.lo(span)), xb = Math.min(xb0, Span.hi(span));
                int row = py * W;
                for (int px = xa; px <= xb; px++) {
                    float cx = px + 0.5f;
                    float w0 = A0 + B0 * cx, w1 = A1 + B1 * cx, w2 = A2 + B2 * cx;
                    float iz = w0 * iz0 + w1 * iz1 + w2 * iz2;
                    if (iz <= 0) continue;
                    float z = 1f / iz;
                    int p = row + px;
                    if (z >= gz[p]) continue;
                    if (clip) {
                        float ca = colA[px];
                        float ddx = rbx + (float) crx * ca, ddy = rby + (float) cry * ca, ddz = rbz + (float) crz * ca;
                        float wx = (float) ex + ddx * z, wy = (float) ey + ddy * z, wz = (float) ez + ddz * z;
                        if (gone(mm, roofT, wx, wy, wz)) continue;
                        if (backT && !validCap(roofT, wx, wy, wz, ddx, ddy, ddz, z)) continue;
                    }
                    gz[p] = z;
                    float q0 = w0 * iz0 * z, q1 = w1 * iz1 * z, q2 = w2 * iz2 * z;
                    gnx[p] = q0 * st[o + 3] + q1 * st[o1 + 3] + q2 * st[o2 + 3];
                    gny[p] = q0 * st[o + 4] + q1 * st[o1 + 4] + q2 * st[o2 + 4];
                    gnz[p] = q0 * st[o + 5] + q1 * st[o1 + 5] + q2 * st[o2 + 5];
                    gsk[p] = q0 * st[o + 6] + q1 * st[o1 + 6] + q2 * st[o2 + 6];
                    gbr[p] = q0 * st[o + 7] + q1 * st[o1 + 7] + q2 * st[o2 + 7];
                    gbg[p] = q0 * st[o + 8] + q1 * st[o1 + 8] + q2 * st[o2 + 8];
                    gbb[p] = q0 * st[o + 9] + q1 * st[o1 + 9] + q2 * st[o2 + 9];
                    gm[p] = m;
                }
            }
        }
    }

    // ------------------------------------------------------------ Schnitt und Ruine

    /** Liegt der Punkt im weggeschnittenen Teil? */
    public boolean cutAway(float x, float z) {
        return cut && x > cutX && x < cbX1 && z > cbZ0 && z < cbZ1;
    }

    /** Ist diese Fläche an diesem Punkt fort (Schnitt, eingestürzt, abgebrochen, fortgeschafft)? */
    private boolean gone(int m, boolean roofT, float x, float y, float z) {
        if (cutAway(x, z) && !(m == Mat.EARTH && y < (float) CB[4] + 0.05f)) return true;
        float r = ruin;
        if (r > 0.01f && !Mat.natural(m) && y > -0.35f) {
            if (m == Mat.WATER) return r > 0.3f;
            if (roofT && Ruin.roofGone(x, y, z, r)) return true;
            if (Ruin.isObject(m) && Ruin.objectGone(x, z, r)) return true;
            if (y > Ruin.clipHeight(x, z, r)) return true;
        }
        return false;
    }

    /**
     * Eine Rückseite zählt als Schnittfläche nur, wenn der Blick kurz davor durch weggenommenes
     * Volumen ging; sonst bleibt sie unsichtbar wie bisher.
     */
    private boolean validCap(boolean roofT, float x, float y, float z, float dx, float dy, float dz, float t) {
        if (cut && Math.abs(dx) > 1e-6f) {
            float tc = (float) ((cutX - ex) / dx);
            if (tc > 0 && tc < t) {
                float zc = (float) ez + dz * tc;
                if (zc > cbZ0 && zc < cbZ1) return true;
            }
        }
        if (ruin > 0.01f) {
            float dl = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            for (int k = 1; k <= 4; k++) {
                float sp = k * 1.5f / dl;
                if (sp >= t) break;
                float qx = x - dx * sp, qy = y - dy * sp, qz = z - dz * sp;
                if (qy > Ruin.clipHeight(qx, qz, ruin)) return true;
                if (roofT && Ruin.roofGone(qx, qy, qz, ruin)) return true;
            }
        }
        return false;
    }

    private static float flick(float x, float z, float t) {
        return Noise.value(x * 2.1f + t * 5.3f, z * 2.1f, t * 1.7f) * 0.6f + Noise.value(t * 11.3f, x, z) * 0.4f;
    }

    private static float lin(float c) { return c * c * (0.3f + 0.7f * c); }

    /** Schnittfläche: Mauerkern im Schnitt, Abbruchkante oben, Bruchstelle im Gewölbe. */
    private void shadeCap(int p, int m, float dx, float dy, float dz, float dl, float z, float[] sk, float[] alb) {
        final Lighting li = L;
        final Sky s = li.sky;
        float best = -1;
        int kind = 2;
        if (cut && Math.abs(dx) > 1e-6f) {
            float tc = (float) ((cutX - ex) / dx);
            if (tc > 0 && tc < z) {
                float zc = (float) (ez + dz * tc);
                if (zc > cbZ0 && zc < cbZ1) { best = tc; kind = 0; }
            }
        }
        if (ruin > 0.01f && dy < -1e-4f) {
            float th = z;
            for (int k = 0; k < 4 && th > 0; k++) {
                float hx = (float) (ex + dx * th), hz = (float) (ez + dz * th);
                th = (float) ((Ruin.clipHeight(hx, hz, ruin) - ey) / dy);
                if (th > z) th = z;
            }
            if (th > 0 && th > best) {
                float hx = (float) (ex + dx * th), hz = (float) (ez + dz * th);
                float yy = (float) (ey + dy * th);
                if (Math.abs(yy - Ruin.clipHeight(hx, hz, ruin)) < 0.4f) { best = th; kind = 1; }
            }
        }
        if (best < 0) best = z;
        float wx = (float) (ex + dx * best), wy = (float) (ey + dy * best), wz = (float) (ez + dz * best);
        gz[p] = best;
        float nx, ny, nz;
        if (kind == 0) { nx = ex > cutX ? 1 : -1; ny = 0; nz = 0; }
        else if (kind == 1) { nx = 0; ny = 1; nz = 0; }
        else { nx = -dx / dl; ny = -dy / dl; nz = -dz / dl; }
        boolean earthy = m == Mat.EARTH || m == Mat.GROUND || m == Mat.LAND;
        float r, g, b;
        boolean tub = kind == 0 && Thermal.inTubuli(wx, wy, wz);
        if (thermo) {
            float T = Thermal.surface(m, wx, wy, wz, ny, 0, 0.5f, tout, heatOn, furnaces);
            if (tub) T = tout + (75 - wy * 1.1f - tout) * heatOn;
            Thermal.palette(T, sk);
            float sh = kind == 0 ? 0.8f : 0.7f;
            hr[p] = lin(sk[0]) * sh; hg[p] = lin(sk[1]) * sh; hb[p] = lin(sk[2]) * sh;
            return;
        }
        if (kind == 0) {
            if (earthy) Materials.earth(wx, wy, wz, alb);
            else { Materials.concrete(wx, wy, wz, alb); alb[0] *= 1.08f; alb[1] *= 0.86f; alb[2] *= 0.76f; }
            float hatch = (wz + wy) * 1.25f;
            if (hatch - (float) Math.floor(hatch) < 0.08f) { alb[0] *= 0.72f; alb[1] *= 0.72f; alb[2] *= 0.72f; }
            float lr = (s.sideR * 0.9f + s.upR * 0.35f + s.sunR * 0.16f) * 0.62f + 0.01f;
            float lg = (s.sideG * 0.9f + s.upG * 0.35f + s.sunG * 0.16f) * 0.62f + 0.01f;
            float lb = (s.sideB * 0.9f + s.upB * 0.35f + s.sunB * 0.16f) * 0.62f + 0.012f;
            r = alb[0] * lr; g = alb[1] * lg; b = alb[2] * lb;
            if (tub) {
                // Hohlziegel: dunkler Ruß, darin die heiße Luft
                float joint = wy / 0.36f;
                float d = joint - (float) Math.floor(joint) < 0.12f ? 0.35f : 1f;
                float e = heatOn * ie * (0.35f + 1.1f * Math.max(0, 1 - wy / 28f)) * (0.8f + 0.2f * flick(wx, wz, time)) * d;
                r = 0.03f * lr + e * 0.9f; g = 0.025f * lg + e * 0.34f; b = 0.02f * lb + e * 0.08f;
            }
        } else {
            if (kind == 1) {
                if (Noise.value(wx * 0.7f, wy * 0.7f, wz * 0.7f) > 0.55f) Materials.brick(wx, wy, wz, 0, 1, 0, alb);
                else Materials.concrete(wx, wy, wz, alb);
                float gg = Noise.fbm(wx * 0.6f, 3.1f, wz * 0.6f, 3);
                if (gg > 0.5f) {
                    float k = Math.min(1, (gg - 0.5f) * 7);
                    Ruin.grass(wx, wz, sk);
                    alb[0] += (sk[0] - alb[0]) * k; alb[1] += (sk[1] - alb[1]) * k; alb[2] += (sk[2] - alb[2]) * k;
                }
            } else {
                Materials.concrete(wx, wy, wz, alb);
            }
            float lx = (float) s.sun[0], ly = (float) s.sun[1], lz = (float) s.sun[2];
            float ndl = nx * lx + ny * ly + nz * lz;
            float lit = 0;
            if (ndl > 0 && s.sunR + s.sunG + s.sunB > 0.001f) {
                float sinT = (float) Math.sqrt(Math.max(0, 1 - ndl * ndl));
                lit = li.lit(wx + nx * 0.06, wy + ny * 0.06, wz + nz * 0.06, sinT / Math.max(ndl, 0.05f));
            }
            float dk = Math.max(0, ndl) * lit, occ = kind == 1 ? 0.95f : 0.5f;
            r = alb[0] * (s.sunR * dk + (s.upR * ny + s.sideR * (1 - ny)) * occ + 0.002f);
            g = alb[1] * (s.sunG * dk + (s.upG * ny + s.sideG * (1 - ny)) * occ + 0.002f);
            b = alb[2] * (s.sunB * dk + (s.upB * ny + s.sideB * (1 - ny)) * occ + 0.0025f);
        }
        float dist = best * dl;
        float f = 1 - (float) Math.exp(-dist * (0.0008f + 0.0009f * s.haze));
        if (f > 0.002f) {
            s.haze(dx / dl, dy / dl, dz / dl, sk);
            r += (sk[0] - r) * f; g += (sk[1] - g) * f; b += (sk[2] - b) * f;
        }
        hr[p] = r; hg[p] = g; hb[p] = b;
    }

    // ------------------------------------------------------------ Beleuchtung

    private void shadeStrip(int y0, int y1) {
        float[] alb = new float[3], sk = new float[3], nb = new float[3];
        final Lighting li = L;
        final Sky s = li.sky;
        float lx = (float) s.sun[0], ly = (float) s.sun[1], lz = (float) s.sun[2];
        boolean sunUp = s.sunR + s.sunG + s.sunB > 0.001f;
        float fogK = 0.0008f + 0.0009f * s.haze;
        for (int py = y0; py < y1; py++) {
            double b = -(py + 0.5 - H / 2.0) / pfy;
            for (int px = 0; px < W; px++) {
                int p = py * W + px;
                double a = (px + 0.5 - W / 2.0) / pfx;
                float dx = (float) (cfx + crx * a + cux * b), dy = (float) (cfy + cry * a + cuy * b), dz = (float) (cfz + crz * a + cuz * b);
                float dl = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                float ndx = dx / dl, ndy = dy / dl, ndz = dz / dl;
                int code = gm[p] - 1;
                if (code < 0) {
                    if (thermo) {
                        Thermal.palette(tout - 30 + 10 * Math.max(0, 1 - ndy * 3), sk);
                        hr[p] = lin(sk[0]) * 0.5f; hg[p] = lin(sk[1]) * 0.5f; hb[p] = lin(sk[2]) * 0.5f;
                        continue;
                    }
                    s.radiance(ndx, ndy, ndz, sk);
                    hr[p] = sk[0]; hg[p] = sk[1]; hb[p] = sk[2];
                    continue;
                }
                int m = code & 31;
                float z = gz[p];
                if ((code & 64) != 0) {
                    shadeCap(p, m, dx, dy, dz, dl, z, sk, alb);
                    continue;
                }
                float wx = (float) (ex + dx * z), wy = (float) (ey + dy * z), wz = (float) (ez + dz * z);
                float nx = gnx[p], ny = gny[p], nz = gnz[p];
                float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                nx /= nl; ny /= nl; nz /= nl;
                float vxv = -ndx, vyv = -ndy, vzv = -ndz;
                if (nx * vxv + ny * vyv + nz * vzv < 0 && m != Mat.WATER) { nx = -nx; ny = -ny; nz = -nz; }
                float skyv = Math.max(0, Math.min(1, gsk[p]));
                float r, g, bl;
                if (m == Mat.WATER) {
                    hr[p] = 0; hg[p] = 0; hb[p] = 0;
                    continue;
                } else {
                    nb[0] = nx; nb[1] = ny; nb[2] = nz;
                    if (m == Mat.EARTH && wy < (float) CB[4] + 0.05f && ny > 0.9f) {
                        // Grundfläche unter dem weggeschnittenen Teil: heller Modellsockel mit Raster
                        float gx = wx / 5f, gzz = wz / 5f, fx1 = wx, fz1 = wz;
                        float line5 = Math.min(Math.abs(gx - Math.round(gx)), Math.abs(gzz - Math.round(gzz))) * 5f;
                        float line1 = Math.min(Math.abs(fx1 - Math.round(fx1)), Math.abs(fz1 - Math.round(fz1)));
                        float k = 0.93f + 0.07f * Noise.value(wx * 0.3f, 0, wz * 0.3f);
                        if (line5 < 0.06f) k *= 0.8f; else if (line1 < 0.025f) k *= 0.93f;
                        float br = 0.27f * k, bg = 0.255f * k, bb = 0.235f * k;
                        if (thermo) {
                            Thermal.palette(tout + 2, sk);
                            hr[p] = lin(sk[0]) * 0.8f; hg[p] = lin(sk[1]) * 0.8f; hb[p] = lin(sk[2]) * 0.8f;
                            continue;
                        }
                        float ndl0 = Math.max(0, ly);
                        r = br * (s.sunR * ndl0 * 0.9f + s.upR); g = bg * (s.sunG * ndl0 * 0.9f + s.upG); bl = bb * (s.sunB * ndl0 * 0.9f + s.upB);
                        float dist0 = z * dl, f0 = 1 - (float) Math.exp(-dist0 * fogK);
                        s.haze(ndx, ndy, ndz, sk);
                        r += (sk[0] - r) * f0; g += (sk[1] - g) * f0; bl += (sk[2] - bl) * f0;
                        hr[p] = r; hg[p] = g; hb[p] = bl;
                        continue;
                    }
                    float ao = Materials.surface(m, wx, wy, wz, nb, alb);
                    if (ruin > 0.01f) {
                        Ruin.decay(m, wx, wy, wz, nb, alb, ruin);
                        skyv += (1 - skyv) * ruin * 0.7f;
                    }
                    nx = nb[0]; ny = nb[1]; nz = nb[2];
                    float ndl = nx * lx + ny * ly + nz * lz;
                    float lit = 0;
                    if (sunUp && ndl > 0) {
                        float sinT = (float) Math.sqrt(Math.max(0, 1 - ndl * ndl));
                        lit = li.lit(wx + nx * 0.04, wy + ny * 0.04, wz + nz * 0.04, sinT / Math.max(ndl, 0.05f));
                    }
                    float dirK = Math.max(0, ndl) * lit;
                    float ar, ag, ab;
                    if (ny >= 0) { ar = s.sideR + (s.upR - s.sideR) * ny; ag = s.sideG + (s.upG - s.sideG) * ny; ab = s.sideB + (s.upB - s.sideB) * ny; }
                    else { ar = s.sideR + (s.downR - s.sideR) * -ny; ag = s.sideG + (s.downG - s.sideG) * -ny; ab = s.sideB + (s.downB - s.sideB) * -ny; }
                    float occ = skyv * skyv * (3 - 2 * skyv);
                    ar = (ar * occ + gbr[p] + 0.002f) * ao; ag = (ag * occ + gbg[p] + 0.002f) * ao; ab = (ab * occ + gbb[p] + 0.0025f) * ao;
                    r = alb[0] * (s.sunR * dirK + ar);
                    g = alb[1] * (s.sunG * dirK + ag);
                    bl = alb[2] * (s.sunB * dirK + ab);
                    float spec = Materials.SPEC[m];
                    if (spec > 0) {
                        float hx = lx + vxv, hy = ly + vyv, hz = lz + vzv;
                        float hl = (float) Math.sqrt(hx * hx + hy * hy + hz * hz);
                        float ndh = (nx * hx + ny * hy + nz * hz) / hl;
                        float shin = Materials.SHIN[m];
                        if (dirK > 0 && ndh > 0) {
                            float sp = (float) Math.pow(ndh, shin) * (shin + 8) / 25.13f * spec * dirK;
                            r += s.sunR * sp; g += s.sunG * sp; bl += s.sunB * sp;
                        }
                        // Spiegelung von Himmel und Raum in polierten Flächen
                        float nv = nx * vxv + ny * vyv + nz * vzv;
                        float cosV = Math.max(0, nv);
                        float fr = spec * 0.5f + (1 - spec) * (float) Math.pow(1 - cosV, 5) * 0.25f * spec * 10;
                        float rdx = 2 * nv * nx - vxv, rdy = 2 * nv * ny - vyv, rdz = 2 * nv * nz - vzv;
                        s.radiance(rdx, Math.max(rdy, 0.02f), rdz, sk);
                        float env = fr * 0.45f;
                        float br = (gbr[p] + gbg[p] + gbb[p]) * 0.6f;
                        r += (sk[0] * occ + br * (1 - occ)) * env; g += (sk[1] * occ + br * (1 - occ)) * env; bl += (sk[2] * occ + br * (1 - occ)) * env;
                    }
                    if (thermo) {
                        float sunK = dirK * Math.min(1, (s.sunR + s.sunG + s.sunB) / 6f);
                        float T = Thermal.surface(m, wx, wy, wz, ny, sunK, skyv, tout, heatOn, furnaces);
                        Thermal.palette(T, sk);
                        float shd = 0.5f + 0.5f * Math.min(1, dirK * 0.6f + occ * 0.7f);
                        hr[p] = lin(sk[0]) * shd; hg[p] = lin(sk[1]) * shd; hb[p] = lin(sk[2]) * shd;
                        continue;
                    }
                    if (heatOn > 0.01f) {
                        // Glut im Schürloch, Feuerschein davor, glühendes Hypokaustum
                        if (m == Mat.EMBER) {
                            float e = heatOn * (1.5f + 1.0f * flick(wx, wz, time)) * ie;
                            r += e; g += e * 0.42f; bl += e * 0.1f;
                        } else if (wy < -0.25f && Thermal.inPit(wx, wy, wz)) {
                            float gi = heatOn * ie * (0.22f + 2.4f * Thermal.fire(wx, wy, wz, furnaces)) * (0.85f + 0.15f * flick(wx, wz, time * 0.5f));
                            r += alb[0] * gi; g += alb[1] * gi * 0.4f; bl += alb[2] * gi * 0.12f;
                        } else {
                            for (double[] fu : furnaces) {
                                float fx = wx - (float) fu[0], fy = wy - 0.6f, fz = wz - (float) fu[2];
                                float d2 = fx * fx + fy * fy + fz * fz;
                                if (d2 > 90 || d2 < 1e-4f) continue;
                                float d = (float) Math.sqrt(d2);
                                float front = (fx * (float) fu[3] + fz * (float) fu[4]) / d;
                                float lam = -(nx * fx + ny * fy + nz * fz) / d;
                                if (front < -0.2f || lam <= 0) continue;
                                float w = lam * (front + 0.2f) * 3.2f / (1 + d2) * heatOn * (0.75f + 0.5f * flick((float) fu[0], (float) fu[2], time)) * (1 + 0.6f * ie);
                                r += alb[0] * w; g += alb[1] * w * 0.45f; bl += alb[2] * w * 0.14f;
                            }
                        }
                    }
                }
                // Luftperspektive
                float dist = z * dl;
                float f = 1 - (float) Math.exp(-dist * fogK);
                if (f > 0.002f) {
                    s.haze(ndx, ndy, ndz, sk);
                    r += (sk[0] - r) * f; g += (sk[1] - g) * f; bl += (sk[2] - bl) * f;
                }
                hr[p] = r; hg[p] = g; hb[p] = bl;
            }
        }
    }

    // ------------------------------------------------------------ Wasser

    private static final float[] ABS = {0.38f, 0.075f, 0.055f};

    /** Wasserflächen: Wellen aus der Simulation, Spiegelung im Bild, Brechung auf den Beckenboden, Lichtnetze. */
    private void shadeWaterStrip(int y0, int y1) {
        float[] sk = new float[3], sl = new float[3], alb = new float[3], ref = new float[3];
        double[] cl = new double[2];
        final Lighting li = L;
        final Sky s = li.sky;
        float lx = (float) s.sun[0], ly = (float) s.sun[1], lz = (float) s.sun[2];
        boolean sunUp = s.sunR + s.sunG + s.sunB > 0.001f;
        float fogK = 0.0008f + 0.0009f * s.haze;
        float t = time;
        for (int py = y0; py < y1; py++) {
            double b = -(py + 0.5 - H / 2.0) / pfy;
            for (int px = 0; px < W; px++) {
                int p = py * W + px;
                if (gm[p] - 1 != Mat.WATER) continue;
                double a = (px + 0.5 - W / 2.0) / pfx;
                float dx = (float) (cfx + crx * a + cux * b), dy = (float) (cfy + cry * a + cuy * b), dz = (float) (cfz + crz * a + cuz * b);
                float dl = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                float vx = -dx / dl, vy = -dy / dl, vz = -dz / dl;
                float z = gz[p];
                float wx = (float) (ex + dx * z), wy = (float) (ey + dy * z), wz = (float) (ez + dz * z);
                if (thermo) {
                    Thermal.palette(Thermal.water(wx, wy, wz, tout, heatOn), sk);
                    hr[p] = lin(sk[0]) * 0.92f; hg[p] = lin(sk[1]) * 0.92f; hb[p] = lin(sk[2]) * 0.92f;
                    continue;
                }
                int bi = water == null ? -1 : water.bodyAt(wx, wy, wz);
                float gx = 0, gzz = 0, lap = 0;
                if (bi >= 0) { water.slope(bi, wx, wz, sl); gx = sl[0]; gzz = sl[1]; lap = sl[2]; }
                for (float[] q : WAVES) {
                    float c = (float) Math.cos(wx * q[0] + wz * q[1] + t * q[2]) * q[3] * 0.55f;
                    gx += c * q[0]; gzz += c * q[1];
                }
                float nx = -gx, ny = 1, nz = -gzz;
                float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                nx /= nl; ny /= nl; nz /= nl;
                float cosV = Math.max(0.02f, nx * vx + ny * vy + nz * vz);
                float F = 0.02f + 0.98f * (float) Math.pow(1 - cosV, 5);
                float rx = 2 * cosV * nx - vx, ry = 2 * cosV * ny - vy, rz = 2 * cosV * nz - vz;
                float skyv = Math.max(0, Math.min(1, gsk[p]));
                // Spiegelung: erst im Bild suchen, sonst schätzen
                float wgt = ssr(wx, wy, wz, rx, ry, rz, ref);
                s.radiance(rx, Math.max(ry, 0.01f), rz, sk);
                float up = Math.max(0, Math.min(1, (ry - 0.03f) / 0.35f));
                float open = Math.min(1, skyv * 1.3f);
                float wallR = 0.6f * (s.sunR * 0.3f + s.sideR), wallG = 0.57f * (s.sunG * 0.3f + s.sideG), wallB = 0.5f * (s.sunB * 0.3f + s.sideB);
                float er = (sk[0] * up + wallR * (1 - up)) * open + (1 - open) * gbr[p] * 0.7f;
                float eg = (sk[1] * up + wallG * (1 - up)) * open + (1 - open) * gbg[p] * 0.7f;
                float eb = (sk[2] * up + wallB * (1 - up)) * open + (1 - open) * gbb[p] * 0.7f;
                float rr = ref[0] * wgt + er * (1 - wgt), rg = ref[1] * wgt + eg * (1 - wgt), rb = ref[2] * wgt + eb * (1 - wgt);
                // Brechung: Blick durchs Wasser auf Boden und Wände des Beckens
                float br, bgc, bb;
                float ambR = s.upR * open * 0.8f + gbr[p], ambG = s.upG * open * 0.8f + gbg[p], ambB = s.upB * open * 0.8f + gbb[p];
                if (bi >= 0) {
                    com.dan.caracalla.model.WaterBody body = water.body(bi);
                    float eta = 1 / 1.33f;
                    float ci = cosV, k = 1 - eta * eta * (1 - ci * ci);
                    float f2 = eta * ci - (float) Math.sqrt(Math.max(0, k));
                    float tx = -eta * vx + f2 * nx, ty = -eta * vy + f2 * ny, tz = -eta * vz + f2 * nz;
                    if (ty > -0.05f) ty = -0.05f;
                    float depth = (float) (body.waterY - body.floorY);
                    float tt = depth / -ty;
                    cl[0] = wx + tx * tt; cl[1] = wz + tz * tt;
                    body.clamp(cl);
                    float fx = (float) cl[0], fz = (float) cl[1], fy = (float) body.floorY;
                    float path = (float) Math.sqrt((fx - wx) * (fx - wx) + (fy - wy) * (fy - wy) + (fz - wz) * (fz - wz));
                    Materials.albedo(Mat.POOL, fx, fy, fz, 0, 1, 0, alb);
                    float sr = 0, sg = 0, sb = 0;
                    if (sunUp) {
                        // Sonnenstrahl im Wasser (gebrochen), Eintrittspunkt an der Oberfläche
                        float sly = -(float) Math.sqrt(Math.max(0.01, 1 - eta * eta * (1 - ly * ly)));
                        float sxh = -eta * lx, szh = -eta * lz;
                        float back = depth / -sly;
                        float sx = fx - sxh * back, sz = fz - szh * back;
                        float litS = li.litHard(sx, body.waterY + 0.03, sz);
                        if (litS > 0) {
                            float caus = caustic(bi, sx, sz, t, sl);
                            float sunPath = back;
                            sr = s.sunR * ly * litS * caus * (float) Math.exp(-ABS[0] * sunPath);
                            sg = s.sunG * ly * litS * caus * (float) Math.exp(-ABS[1] * sunPath);
                            sb = s.sunB * ly * litS * caus * (float) Math.exp(-ABS[2] * sunPath);
                        }
                    }
                    br = alb[0] * (sr + ambR * 0.8f) * (float) Math.exp(-ABS[0] * path);
                    bgc = alb[1] * (sg + ambG * 0.8f) * (float) Math.exp(-ABS[1] * path);
                    bb = alb[2] * (sb + ambB * 0.8f) * (float) Math.exp(-ABS[2] * path);
                    float scat = 1 - (float) Math.exp(-0.35f * path);
                    br += 0.010f * (ambR + s.sunR * ly * 0.2f) * scat;
                    bgc += 0.045f * (ambG + s.sunG * ly * 0.2f) * scat;
                    bb += 0.050f * (ambB + s.sunB * ly * 0.2f) * scat;
                } else {
                    br = 0.012f * ambR; bgc = 0.05f * ambG; bb = 0.055f * ambB;
                }
                float r = br * (1 - F) + rr * F, g = bgc * (1 - F) + rg * F, bl = bb * (1 - F) + rb * F;
                if (sunUp) {
                    float lit = li.litHard(wx, wy + 0.02, wz);
                    if (lit > 0) {
                        float rs = Math.max(0, rx * lx + ry * ly + rz * lz);
                        float sp = (float) Math.pow(rs, 1400) * 110 + (float) Math.pow(rs, 140) * 1.4f;
                        sp *= lit * (0.3f + F);
                        r += s.sunR * sp; g += s.sunG * sp; bl += s.sunB * sp;
                    }
                }
                float dist = z * dl;
                float f = 1 - (float) Math.exp(-dist * fogK);
                if (f > 0.002f) {
                    s.haze(-vx, -vy, -vz, sk);
                    r += (sk[0] - r) * f; g += (sk[1] - g) * f; bl += (sk[2] - bl) * f;
                }
                hr[p] = r; hg[p] = g; hb[p] = bl;
            }
        }
    }

    /** Lichtnetz am Beckenboden: aus der Krümmung der Wellen und einem wandernden Muster. */
    private float caustic(int bi, float x, float z, float t, float[] sl) {
        water.slope(bi, x, z, sl);
        float c1 = 1 + Math.max(-0.6f, Math.min(2.5f, -sl[2] * 5f));
        float v1 = Noise.value(x * 1.5f, t * 0.55f, z * 1.5f), v2 = Noise.value(x * 1.5f + 3.1f, t * 0.55f + 1.7f, z * 1.5f + 5.3f);
        float ridge = 1 - Math.min(1, Math.abs(v1 - v2) * 5);
        ridge *= ridge; ridge *= ridge;
        return (0.5f + 1.9f * ridge) * c1;
    }

    /**
     * Spiegelung im Bildraum: der gespiegelte Strahl wird schrittweise verfolgt, bis er hinter einer
     * sichtbaren Fläche verschwindet. Liefert das Gewicht (0 = nichts gefunden) und die Farbe in out.
     */
    private float ssr(float ox, float oy, float oz, float rx, float ry, float rz, float[] out) {
        float tPrev = 0, t = 0.12f;
        for (int i = 0; i < 34; i++) {
            float qx = ox + rx * t, qy = oy + ry * t, qz = oz + rz * t;
            double ddx = qx - ex, ddy = qy - ey, ddz = qz - ez;
            double vz = ddx * cfx + ddy * cfy + ddz * cfz;
            if (vz < 0.2) return 0;
            double sx = W / 2.0 + (ddx * crx + ddy * cry + ddz * crz) / vz * pfx;
            double sy = H / 2.0 - (ddx * cux + ddy * cuy + ddz * cuz) / vz * pfy;
            if (sx < 0 || sy < 0 || sx >= W || sy >= H) return 0;
            int pix = (int) sy * W + (int) sx;
            float d = gz[pix];
            if (vz > d + 0.03f && gm[pix] != 0) {
                if (vz - d < 0.8f + 0.08f * t) {
                    // verfeinern
                    float a = tPrev, bnd = t;
                    for (int k = 0; k < 5; k++) {
                        float m = (a + bnd) / 2;
                        float mx = ox + rx * m, my = oy + ry * m, mz = oz + rz * m;
                        double mdx = mx - ex, mdy = my - ey, mdz = mz - ez;
                        double mvz = mdx * cfx + mdy * cfy + mdz * cfz;
                        double msx = W / 2.0 + (mdx * crx + mdy * cry + mdz * crz) / mvz * pfx;
                        double msy = H / 2.0 - (mdx * cux + mdy * cuy + mdz * cuz) / mvz * pfy;
                        int mp = Math.max(0, Math.min(H - 1, (int) msy)) * W + Math.max(0, Math.min(W - 1, (int) msx));
                        if (mvz > gz[mp] + 0.03f) { bnd = m; pix = mp; sx = msx; sy = msy; } else a = m;
                    }
                    if (gm[pix] == 0 || gm[pix] - 1 == Mat.WATER) return 0;
                    out[0] = hr[pix]; out[1] = hg[pix]; out[2] = hb[pix];
                    float edge = (float) Math.min(Math.min(sx, W - sx) / (0.08 * W), Math.min(sy, H - sy) / (0.08 * H));
                    return Math.max(0, Math.min(1, edge)) * Math.max(0, 1 - t / 90f);
                }
            }
            tPrev = t;
            t = t * 1.14f + 0.05f;
        }
        return 0;
    }

    /** Tropfen der Wasserspeier: kleine, weiche Scheiben mit Himmels- und Sonnenlicht. */
    private void drawDrops() {
        WaterSim w = water;
        Sky s = L.sky;
        for (int i = 0; i < w.np; i++) {
            if (cutAway(w.px[i], w.pz[i])) continue;
            double ddx = w.px[i] - ex, ddy = w.py[i] - ey, ddz = w.pz[i] - ez;
            double vz = ddx * cfx + ddy * cfy + ddz * cfz;
            if (vz < 0.3) continue;
            double sx = W / 2.0 + (ddx * crx + ddy * cry + ddz * crz) / vz * pfx;
            double sy = H / 2.0 - (ddx * cux + ddy * cuy + ddz * cuz) / vz * pfy;
            double rad = w.psize[i] * pfy / vz;
            if (sx < -3 || sy < -3 || sx > W + 3 || sy > H + 3) continue;
            float lit = L.litHard(w.px[i], w.py[i], w.pz[i]);
            float cr = s.upR * 0.25f + s.sideR * 0.2f + s.sunR * lit * 0.55f + 0.02f;
            float cg = s.upG * 0.25f + s.sideG * 0.2f + s.sunG * lit * 0.55f + 0.02f;
            float cb = s.upB * 0.25f + s.sideB * 0.2f + s.sunB * lit * 0.55f + 0.025f;
            float alpha = 0.55f;
            double r = Math.max(0.7, rad);
            if (rad < 0.7) alpha *= (float) (rad / 0.7) * 0.8f + 0.2f;
            int x0 = (int) Math.floor(sx - r), x1 = (int) Math.ceil(sx + r), y0 = (int) Math.floor(sy - r), y1 = (int) Math.ceil(sy + r);
            for (int yy = Math.max(0, y0); yy <= Math.min(H - 1, y1); yy++) {
                for (int xx = Math.max(0, x0); xx <= Math.min(W - 1, x1); xx++) {
                    double ex2 = xx + 0.5 - sx, ey2 = yy + 0.5 - sy;
                    double q = (ex2 * ex2 + ey2 * ey2) / (r * r);
                    if (q >= 1) continue;
                    int p = yy * W + xx;
                    if (vz > gz[p]) continue;
                    float a = alpha * (float) (1 - q);
                    hr[p] += (cr - hr[p]) * a; hg[p] += (cg - hg[p]) * a; hb[p] += (cb - hb[p]) * a;
                }
            }
        }
    }

    private static final float[][] WAVES = {
            {1.1f, 0.35f, 1.9f, 0.010f}, {-0.5f, 1.3f, 2.3f, 0.008f}, {2.2f, -1.6f, 3.4f, 0.0045f},
            {-2.9f, -1.0f, 4.1f, 0.0035f}, {0.3f, 4.1f, 5.6f, 0.002f}, {5.3f, 2.2f, 7.1f, 0.0012f}};

    // ------------------------------------------------------------ Lichtstrahlen

    private static final int STEPS = 30;
    private static final float MAX_DIST = 140;

    /** Strahlengang in Viertelauflösung: Streulicht der Sonne (Schattenkarte) und des Raums in Dampf und Luft. */
    private void marchStrip(int y0, int y1) {
        final Lighting li = L;
        final Sky s = li.sky;
        float lx = (float) s.sun[0], ly = (float) s.sun[1], lz = (float) s.sun[2];
        boolean sunUp = s.sunR + s.sunG + s.sunB > 0.001f && li.fine.valid;
        float g = 0.6f, g2 = g * g;
        float haze = s.haze;
        for (int qy = y0; qy < y1; qy++) {
            int py = Math.min(H - 1, qy * 4 + 2);
            double b = -(py + 0.5 - H / 2.0) / pfy;
            for (int qx = 0; qx < QW; qx++) {
                int px = Math.min(W - 1, qx * 4 + 2);
                int p = py * W + px, q = qy * QW + qx;
                double a = (px + 0.5 - W / 2.0) / pfx;
                float dx = (float) (cfx + crx * a + cux * b), dy = (float) (cfy + cry * a + cuy * b), dz = (float) (cfz + crz * a + cuz * b);
                float dl = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                float ndx = dx / dl, ndy = dy / dl, ndz = dz / dl;
                float depth = gm[p] == 0 ? MAX_DIST : Math.min(MAX_DIST, gz[p] * dl);
                qd[q] = gm[p] == 0 ? Float.MAX_VALUE : gz[p];
                float mu = ndx * lx + ndy * ly + ndz * lz;
                float phase = (1 - g2) / (float) Math.pow(1 + g2 - 2 * g * mu, 1.5) * 0.5f + 0.5f;
                float ds = depth / STEPS;
                float jit = ((Noise.hash(qx, qy, frame & 7) & 1023) / 1024f);
                float T = 1, sr = 0, sg = 0, sb = 0, heat = 0;
                SteamSim st = steam;
                float airD = Atmosphere.air(haze);
                for (int i = 0; i < STEPS; i++) {
                    float tpos = (i + jit) * ds;
                    float wx = (float) (ex + ndx * tpos), wy = (float) (ey + ndy * tpos), wz = (float) (ez + ndz * tpos);
                    float d;
                    if (st != null) {
                        float sd = cut && cutAway(wx, wz) ? 0 : st.density(wx, wy, wz);
                        if (sd > 1e-5f) {
                            float det = Noise.value(wx * 0.45f + time * 0.06f, wy * 0.45f - time * 0.15f, wz * 0.45f);
                            sd *= 0.45f + 1.1f * det;
                        }
                        d = airD + sd;
                        heat += st.heatAt(wx, wy, wz) * ds;
                    } else if (ruin > 0.25f) {
                        d = airD;
                    } else {
                        d = Atmosphere.density(wx, wy, wz, time, haze);
                    }
                    float lit = sunUp ? li.litHard(wx, wy, wz) : 0;
                    float k = T * d * ds;
                    float amb = 0.018f;
                    sr += k * (s.sunR * lit * phase + (s.upR + s.sideR) * amb);
                    sg += k * (s.sunG * lit * phase + (s.upG + s.sideG) * amb);
                    sb += k * (s.sunB * lit * phase + (s.upB + s.sideB) * amb);
                    T *= (float) Math.exp(-d * ds * 0.6f);
                }
                qr[q] = sr; qg[q] = sg; qb[q] = sb; qt[q] = T; qh[q] = Math.min(1, heat * 0.35f);
            }
        }
    }

    /** Glättet das Streulicht (3×3), um das Rauschen der versetzten Abtastung zu verbergen. */
    private void blurQuarter() {
        for (int pass = 0; pass < 2; pass++) {
            float[] ir = pass == 0 ? qr : tr, ig = pass == 0 ? qg : tg, ib = pass == 0 ? qb : tb;
            float[] or = pass == 0 ? tr : qr, og = pass == 0 ? tg : qg, ob = pass == 0 ? tb : qb;
            for (int y = 0; y < QH; y++) {
                for (int x = 0; x < QW; x++) {
                    float r = 0, g = 0, b = 0, w = 0;
                    float d0 = qd[y * QW + x];
                    for (int k = -1; k <= 1; k++) {
                        int xx = pass == 0 ? x + k : x, yy = pass == 0 ? y : y + k;
                        if (xx < 0 || yy < 0 || xx >= QW || yy >= QH) continue;
                        int i = yy * QW + xx;
                        float dd = qd[i];
                        float wk = (k == 0 ? 2 : 1) * (Math.abs(dd - d0) < 0.1f * Math.min(dd, d0) + 0.5f || (dd > 1e30f && d0 > 1e30f) ? 1f : 0.05f);
                        r += ir[i] * wk; g += ig[i] * wk; b += ib[i] * wk; w += wk;
                    }
                    int o = y * QW + x;
                    or[o] = r / w; og[o] = g / w; ob[o] = b / w;
                }
            }
        }
    }

    /** Streulicht tiefenbewusst hochskalieren und zum Bild geben. */
    private void applyScatter(int y0, int y1) {
        for (int py = y0; py < y1; py++) {
            float fy = (py + 0.5f) / 4f - 0.5f;
            int qy0 = Math.max(0, Math.min(QH - 1, (int) Math.floor(fy))), qy1 = Math.min(QH - 1, qy0 + 1);
            float ty = Math.max(0, Math.min(1, fy - qy0));
            for (int px = 0; px < W; px++) {
                int p = py * W + px;
                float fx = (px + 0.5f) / 4f - 0.5f;
                int qx0 = Math.max(0, Math.min(QW - 1, (int) Math.floor(fx))), qx1 = Math.min(QW - 1, qx0 + 1);
                float tx = Math.max(0, Math.min(1, fx - qx0));
                float z = gm[p] == 0 ? Float.MAX_VALUE : gz[p];
                float r = 0, g = 0, b = 0, T = 0, w = 0;
                for (int k = 0; k < 4; k++) {
                    int qx = (k & 1) == 0 ? qx0 : qx1, qy = (k & 2) == 0 ? qy0 : qy1;
                    float bw = ((k & 1) == 0 ? 1 - tx : tx) * ((k & 2) == 0 ? 1 - ty : ty);
                    int i = qy * QW + qx;
                    float dd = qd[i];
                    float sim = (dd > 1e30f && z > 1e30f) ? 1 : 1f / (1 + 20 * Math.abs(dd - z) / Math.min(dd, z));
                    float wk = bw * sim + 1e-4f;
                    r += qr[i] * wk; g += qg[i] * wk; b += qb[i] * wk; T += qt[i] * wk; w += wk;
                }
                r /= w; g /= w; b /= w; T /= w;
                hr[p] = hr[p] * T + r; hg[p] = hg[p] * T + g; hb[p] = hb[p] * T + b;
            }
        }
    }

    /** Flimmern über heißem Wasser: Bild leicht versetzt auslesen, wo der Blick durch heiße Luft geht. */
    private void shimmer(int rowsPer) {
        System.arraycopy(hr, 0, cr, 0, W * H);
        System.arraycopy(hg, 0, cg, 0, W * H);
        System.arraycopy(hb, 0, cb, 0, W * H);
        float amp = 2.2f * W / 960f;
        float tt = time;
        IntStream.range(0, strips).parallel().forEach(s -> {
            for (int py = s * rowsPer; py < Math.min(H, (s + 1) * rowsPer); py++) {
                int qy = Math.min(QH - 1, py / 4);
                for (int px = 0; px < W; px++) {
                    float h = qh[qy * QW + Math.min(QW - 1, px / 4)];
                    if (h < 0.01f) continue;
                    float ox = (Noise.value(px * 0.09f, py * 0.05f + tt * 2.2f, tt * 0.5f) - 0.5f) * 2 * amp * h;
                    float oy = (Noise.value(px * 0.07f + 11, py * 0.06f + tt * 2.6f, 3) - 0.5f) * 2 * amp * h;
                    float sx = Math.max(0, Math.min(W - 1.001f, px + ox)), sy = Math.max(0, Math.min(H - 1.001f, py + oy));
                    int ix = (int) sx, iy = (int) sy;
                    float fx = sx - ix, fy = sy - iy;
                    int a = iy * W + ix, b = a + (ix < W - 1 ? 1 : 0), c = a + (iy < H - 1 ? W : 0), d = c + (ix < W - 1 ? 1 : 0);
                    int p = py * W + px;
                    hr[p] = (cr[a] * (1 - fx) + cr[b] * fx) * (1 - fy) + (cr[c] * (1 - fx) + cr[d] * fx) * fy;
                    hg[p] = (cg[a] * (1 - fx) + cg[b] * fx) * (1 - fy) + (cg[c] * (1 - fx) + cg[d] * fx) * fy;
                    hb[p] = (cb[a] * (1 - fx) + cb[b] * fx) * (1 - fy) + (cb[c] * (1 - fx) + cb[d] * fx) * fy;
                }
            }
        });
    }

    /** Junge Dampfschwaden in der Nähe als weiche, beleuchtete Wolken (mit weichem Übergang an Flächen). */
    private void drawSteam() {
        SteamSim st = steam;
        Sky s = L.sky;
        float lx = (float) s.sun[0], ly = (float) s.sun[1], lz = (float) s.sun[2];
        float g = 0.6f, g2 = g * g;
        for (int i = 0; i < st.n; i++) {
            float a0 = st.age[i];
            if (a0 > 5f) continue;
            if (cutAway(st.x[i], st.z[i])) continue;
            double ddx = st.x[i] - ex, ddy = st.y[i] - ey, ddz = st.z[i] - ez;
            double vz = ddx * cfx + ddy * cfy + ddz * cfz;
            if (vz < 0.5 || vz > 35) continue;
            float size = 0.35f + a0 * 0.22f;
            double rad = size * pfy / vz;
            if (rad < 1.5) continue;
            if (rad > 70) rad = 70;
            double sx = W / 2.0 + (ddx * crx + ddy * cry + ddz * crz) / vz * pfx;
            double sy = H / 2.0 - (ddx * cux + ddy * cuy + ddz * cuz) / vz * pfy;
            if (sx < -rad || sy < -rad || sx > W + rad || sy > H + rad) continue;
            float dl = (float) Math.sqrt(ddx * ddx + ddy * ddy + ddz * ddz);
            float mu = (float) ((ddx * lx + ddy * ly + ddz * lz) / dl);
            float phase = (1 - g2) / (float) Math.pow(1 + g2 - 2 * g * mu, 1.5) * 0.5f + 0.5f;
            float lit = L.litHard(st.x[i], st.y[i], st.z[i]);
            float amb = 0.12f;
            float colR = s.sunR * lit * phase * 0.5f + (s.upR + s.sideR) * amb + 0.02f;
            float colG = s.sunG * lit * phase * 0.5f + (s.upG + s.sideG) * amb + 0.02f;
            float colB = s.sunB * lit * phase * 0.5f + (s.upB + s.sideB) * amb + 0.025f;
            float fade = Math.min(1, a0 * 1.2f) * (1 - a0 / 5f);
            float alpha = 0.05f * fade * Math.max(0.3f, st.heat[i]);
            if (alpha < 0.002f) continue;
            int x0 = (int) Math.floor(sx - rad), x1 = (int) Math.ceil(sx + rad), y0 = (int) Math.floor(sy - rad), y1 = (int) Math.ceil(sy + rad);
            double r2 = rad * rad;
            for (int yy = Math.max(0, y0); yy <= Math.min(H - 1, y1); yy++) {
                for (int xx = Math.max(0, x0); xx <= Math.min(W - 1, x1); xx++) {
                    double exx = xx + 0.5 - sx, eyy = yy + 0.5 - sy;
                    double q = (exx * exx + eyy * eyy) / r2;
                    if (q >= 1) continue;
                    int p = yy * W + xx;
                    float soft = (float) Math.min(1, (gz[p] - vz) / 0.8);
                    if (soft <= 0) continue;
                    float a = alpha * (float) ((1 - q) * (1 - q)) * soft;
                    hr[p] += (colR - hr[p]) * a; hg[p] += (colG - hg[p]) * a; hb[p] += (colB - hb[p]) * a;
                }
            }
        }
    }

    // ------------------------------------------------------------ Wege

    /**
     * Leuchtende Teilchen entlang der Wege. Was hinter Wänden oder unter der Erde liegt, scheint
     * gedämpft durch (Röntgenblick), damit der ganze Weg lesbar bleibt.
     */
    private void drawStreams(Streams st, float size, boolean air) {
        float[] q = new float[3];
        float fogK = 0.0008f + 0.0009f * L.sky.haze;
        float gain = thermo ? 0.9f : ie;
        float xray = air ? 0.1f : 0.26f;
        for (int i = 0; i < st.n; i++) {
            float u = st.position(i, q);
            if (cutAway(q[0], q[2])) continue;
            double ddx = q[0] - ex, ddy = q[1] - ey, ddz = q[2] - ez;
            double vz = ddx * cfx + ddy * cfy + ddz * cfz;
            if (vz < 0.4) continue;
            double sx = W / 2.0 + (ddx * crx + ddy * cry + ddz * crz) / vz * pfx;
            double sy = H / 2.0 - (ddx * cux + ddy * cuy + ddz * cuz) / vz * pfy;
            double rad = size * pfy / vz;
            if (rad > 30) rad = 30;
            float alpha = 1;
            if (rad < 0.9) { alpha = (float) (rad / 0.9); rad = 0.9; }
            if (sx < -rad || sy < -rad || sx > W + rad || sy > H + rad) continue;
            com.dan.caracalla.model.Route ro = st.routes.get(st.route[i]);
            float cr, cg, cb, k;
            if (air) {
                float tmp = (float) Math.pow(Math.max(0, 1 - u), 0.7);
                cr = 1f; cg = 0.18f + 0.62f * tmp * tmp; cb = 0.04f + 0.4f * tmp * tmp * tmp;
                k = (0.5f + 1.6f * tmp) * heatOn;
            } else {
                cr = 0.22f; cg = 0.72f; cb = 1.0f;
                if (ro.heatAt >= 0 && st.s[i] > ro.heatAt) {
                    float w = Math.min(1, (st.s[i] - ro.heatAt) / 6f);
                    float hrr = ro.kind == com.dan.caracalla.model.Route.HOT ? 1f : 1f;
                    float hgg = ro.kind == com.dan.caracalla.model.Route.HOT ? 0.42f : 0.78f;
                    float hbb = ro.kind == com.dan.caracalla.model.Route.HOT ? 0.12f : 0.42f;
                    cr += (hrr - cr) * w; cg += (hgg - cg) * w; cb += (hbb - cb) * w;
                }
                k = 1.1f;
            }
            boolean under = q[1] < -0.2f;
            float dist = (float) Math.sqrt(ddx * ddx + ddy * ddy + ddz * ddz);
            k *= gain * alpha * (float) Math.exp(-dist * fogK * 1.5f);
            if (k < 1e-4f) continue;
            int x0 = (int) Math.floor(sx - rad), x1 = (int) Math.ceil(sx + rad), y0 = (int) Math.floor(sy - rad), y1 = (int) Math.ceil(sy + rad);
            double r2 = rad * rad;
            float zv = (float) vz;
            for (int yy = Math.max(0, y0); yy <= Math.min(H - 1, y1); yy++) {
                for (int xx = Math.max(0, x0); xx <= Math.min(W - 1, x1); xx++) {
                    double e1 = xx + 0.5 - sx, e2 = yy + 0.5 - sy;
                    double qq = (e1 * e1 + e2 * e2) / r2;
                    if (qq >= 1) continue;
                    int p = yy * W + xx;
                    float fall = (float) ((1 - qq) * (1 - qq));
                    float behind = zv - gz[p];
                    float a = k * fall * (behind < 0.05f ? 1f : (air ? (under && behind < 1.6f ? 0.4f : 0f) : xray));
                    hr[p] += cr * a; hg[p] += cg * a; hb[p] += cb * a;
                }
            }
        }
    }

    // ------------------------------------------------------------ Bloom

    private void doBloom(int rowsPer, int qPer) {
        float e = (float) exposure;
        float thr = 1.1f;
        // Helle Anteile in Viertelauflösung sammeln
        IntStream.range(0, strips).parallel().forEach(s -> {
            for (int qy = s * qPer; qy < Math.min(QH, (s + 1) * qPer); qy++) {
                for (int qx = 0; qx < QW; qx++) {
                    float r = 0, g = 0, b = 0;
                    int n = 0;
                    for (int yy = qy * 4; yy < Math.min(H, qy * 4 + 4); yy++) {
                        for (int xx = qx * 4; xx < Math.min(W, qx * 4 + 4); xx++) {
                            int p = yy * W + xx;
                            float l = (0.2126f * hr[p] + 0.7152f * hg[p] + 0.0722f * hb[p]) * e;
                            if (l > thr) {
                                float k = Math.min(l - thr, 30) / l;
                                r += hr[p] * k; g += hg[p] * k; b += hb[p] * k;
                            }
                            n++;
                        }
                    }
                    int q = qy * QW + qx;
                    tr[q] = r / n; tg[q] = g / n; tb[q] = b / n;
                }
            }
        });
        // Zweimal breit weichzeichnen (Gauß, Radius 6)
        float[] kern = new float[13];
        float ks = 0;
        for (int i = -6; i <= 6; i++) { kern[i + 6] = (float) Math.exp(-i * i / 12.0); ks += kern[i + 6]; }
        for (int i = 0; i < 13; i++) kern[i] /= ks;
        for (int it = 0; it < 2; it++) {
            gauss(tr, tg, tb, qr, qg, qb, kern, true);
            gauss(qr, qg, qb, tr, tg, tb, kern, false);
        }
        float strength = 0.18f;
        IntStream.range(0, strips).parallel().forEach(s -> {
            for (int py = s * rowsPer; py < Math.min(H, (s + 1) * rowsPer); py++) {
                float fy = (py + 0.5f) / 4f - 0.5f;
                int y0 = Math.max(0, Math.min(QH - 1, (int) Math.floor(fy))), y1 = Math.min(QH - 1, y0 + 1);
                float ty = Math.max(0, Math.min(1, fy - y0));
                for (int px = 0; px < W; px++) {
                    float fx = (px + 0.5f) / 4f - 0.5f;
                    int x0 = Math.max(0, Math.min(QW - 1, (int) Math.floor(fx))), x1 = Math.min(QW - 1, x0 + 1);
                    float tx = Math.max(0, Math.min(1, fx - x0));
                    int a = y0 * QW + x0, b = y0 * QW + x1, c = y1 * QW + x0, d = y1 * QW + x1;
                    float w0 = (1 - tx) * (1 - ty), w1 = tx * (1 - ty), w2 = (1 - tx) * ty, w3 = tx * ty;
                    int p = py * W + px;
                    hr[p] += (tr[a] * w0 + tr[b] * w1 + tr[c] * w2 + tr[d] * w3) * strength;
                    hg[p] += (tg[a] * w0 + tg[b] * w1 + tg[c] * w2 + tg[d] * w3) * strength;
                    hb[p] += (tb[a] * w0 + tb[b] * w1 + tb[c] * w2 + tb[d] * w3) * strength;
                }
            }
        });
    }

    private void gauss(float[] ir, float[] ig, float[] ib, float[] or, float[] og, float[] ob, float[] k, boolean horiz) {
        IntStream.range(0, QH).parallel().forEach(y -> {
            for (int x = 0; x < QW; x++) {
                float r = 0, g = 0, b = 0;
                for (int i = -6; i <= 6; i++) {
                    int xx = horiz ? Math.max(0, Math.min(QW - 1, x + i)) : x;
                    int yy = horiz ? y : Math.max(0, Math.min(QH - 1, y + i));
                    int s = yy * QW + xx;
                    float w = k[i + 6];
                    r += ir[s] * w; g += ig[s] * w; b += ib[s] * w;
                }
                int o = y * QW + x;
                or[o] = r; og[o] = g; ob[o] = b;
            }
        });
    }

    // ------------------------------------------------------------ Belichtung

    private void adaptExposure(double dt) {
        double sum = 0;
        int n = 0;
        int step = 7;
        for (int p = 3; p < W * H; p += step) {
            double l = 0.2126 * hr[p] + 0.7152 * hg[p] + 0.0722 * hb[p];
            sum += Math.log(1e-4 + Math.min(l, 50));
            n++;
        }
        double avg = Math.exp(sum / Math.max(1, n));
        double target = 0.20 / Math.pow(avg, 0.82) * Math.pow(0.35, 0.18);
        target = Math.max(0.15, Math.min(40, target));
        if (!exposureValid || dt <= 0) { exposure = target; exposureValid = true; }
        else {
            double k = 1 - Math.exp(-dt * 1.8);
            exposure = Math.exp(Math.log(exposure) + (Math.log(target) - Math.log(exposure)) * k);
        }
    }

    private void tonemap(int[] out, int y0, int y1) {
        float e = (float) exposure;
        if (thermo) {
            for (int py = y0; py < y1; py++) {
                for (int px = 0; px < W; px++) {
                    int p = py * W + px;
                    out[p] = (lg(hr[p]) << 16) | (lg(hg[p]) << 8) | lg(hb[p]);
                }
            }
            return;
        }
        for (int py = y0; py < y1; py++) {
            for (int px = 0; px < W; px++) {
                int p = py * W + px;
                float d = ((Noise.hash(px, py, 1) & 255) / 255f - 0.5f) / 255f;
                int r = tm(hr[p] * e, d), g = tm(hg[p] * e, d), b = tm(hb[p] * e, d);
                out[p] = (r << 16) | (g << 8) | b;
            }
        }
    }

    private static int lg(float x) {
        float y = x < 0 ? 0 : (x > 1 ? 1 : x);
        int v = (int) (GAMMA[(int) (y * 4096)] * 255 + 0.5f);
        return v > 255 ? 255 : v;
    }

    private static int tm(float x, float dither) {
        float y = (x * (2.51f * x + 0.03f)) / (x * (2.43f * x + 0.59f) + 0.14f);
        if (y < 0) y = 0; else if (y > 1) y = 1;
        float g = GAMMA[(int) (y * 4096)] + dither;
        int v = (int) (g * 255 + 0.5f);
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    /** Material unter dem Pixel des letzten Bildes (-1 = Himmel). */
    public int pickMaterial(int px, int py) {
        if (px < 0 || py < 0 || px >= W || py >= H) return -1;
        int code = gm[py * W + px] - 1;
        return code < 0 ? -1 : code & 31;
    }

    /** Weltpunkt unter dem Pixel (px,py) des letzten Bildes oder null bei Himmel. */
    public double[] pick(int px, int py) {
        if (px < 0 || py < 0 || px >= W || py >= H) return null;
        int p = py * W + px;
        if (gm[p] == 0) return null;
        double a = (px + 0.5 - W / 2.0) / pfx, b = -(py + 0.5 - H / 2.0) / pfy;
        double dx = cfx + crx * a + cux * b, dy = cfy + cry * a + cuy * b, dz = cfz + crz * a + cuz * b;
        double z = gz[p];
        return new double[]{ex + dx * z, ey + dy * z, ez + dz * z};
    }

    public double exposure() { return exposure; }

    /** Normale (Welt) unter dem Pixel des letzten Bildes oder null. */
    public float[] pickNormal(int px, int py) {
        if (px < 0 || py < 0 || px >= W || py >= H) return null;
        int p = py * W + px;
        if (gm[p] == 0) return null;
        float x = gnx[p], y = gny[p], z = gnz[p], l = (float) Math.sqrt(x * x + y * y + z * z);
        if (l < 1e-6f) return null;
        return new float[]{x / l, y / l, z / l};
    }
}
