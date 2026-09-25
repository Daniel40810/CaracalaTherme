package com.dan.caracalla.render;

import com.dan.caracalla.geo.Mesh;

/** Hüllkörperbaum über alle Dreiecke für Strahltests (Himmelssicht). */
public final class Bvh {
    private final float[] tri;      // 9 je Dreieck (in Baumreihenfolge)
    private int[] nodeA, nodeN;     // Blatt: erstes Dreieck, Anzahl; innen: linker Kindknoten, 0
    private float[] box;
    private int nNodes;
    private final int[] order;
    private final float[] cen;

    public Bvh(Mesh m) {
        int n = m.nt;
        order = new int[n];
        cen = new float[3 * n];
        for (int t = 0; t < n; t++) {
            order[t] = t;
            for (int k = 0; k < 3; k++) {
                int o = 3 * m.idx[3 * t + k];
                cen[3 * t] += m.pos[o] / 3f; cen[3 * t + 1] += m.pos[o + 1] / 3f; cen[3 * t + 2] += m.pos[o + 2] / 3f;
            }
        }
        int cap = 2 * n + 1;
        nodeA = new int[cap]; nodeN = new int[cap]; box = new float[6 * cap];
        nNodes = 1;
        build(m, 0, 0, n, 0);
        tri = new float[9 * n];
        for (int i = 0; i < n; i++) {
            int t = order[i];
            for (int k = 0; k < 3; k++) {
                int o = 3 * m.idx[3 * t + k];
                tri[9 * i + 3 * k] = m.pos[o]; tri[9 * i + 3 * k + 1] = m.pos[o + 1]; tri[9 * i + 3 * k + 2] = m.pos[o + 2];
            }
        }
    }

    private void build(Mesh m, int node, int s, int e, int depth) {
        float mnx = Float.MAX_VALUE, mny = mnx, mnz = mnx, mxx = -mnx, mxy = -mnx, mxz = -mnx;
        float cnx = mnx, cny = mnx, cnz = mnx, cxx = -mnx, cxy = -mnx, cxz = -mnx;
        for (int i = s; i < e; i++) {
            int t = order[i];
            for (int k = 0; k < 3; k++) {
                int o = 3 * m.idx[3 * t + k];
                float x = m.pos[o], y = m.pos[o + 1], z = m.pos[o + 2];
                if (x < mnx) mnx = x; if (y < mny) mny = y; if (z < mnz) mnz = z;
                if (x > mxx) mxx = x; if (y > mxy) mxy = y; if (z > mxz) mxz = z;
            }
            float x = cen[3 * t], y = cen[3 * t + 1], z = cen[3 * t + 2];
            if (x < cnx) cnx = x; if (y < cny) cny = y; if (z < cnz) cnz = z;
            if (x > cxx) cxx = x; if (y > cxy) cxy = y; if (z > cxz) cxz = z;
        }
        int b = 6 * node;
        box[b] = mnx; box[b + 1] = mny; box[b + 2] = mnz; box[b + 3] = mxx; box[b + 4] = mxy; box[b + 5] = mxz;
        int count = e - s;
        if (count <= 4 || depth > 60) { nodeA[node] = s; nodeN[node] = count; return; }
        float ex = cxx - cnx, ey = cxy - cny, ez = cxz - cnz;
        int axis = ex > ey ? (ex > ez ? 0 : 2) : (ey > ez ? 1 : 2);
        float split = axis == 0 ? (cnx + cxx) / 2 : axis == 1 ? (cny + cxy) / 2 : (cnz + cxz) / 2;
        int i = s, j = e - 1;
        while (i <= j) {
            if (cen[3 * order[i] + axis] < split) i++;
            else { int tmp = order[i]; order[i] = order[j]; order[j] = tmp; j--; }
        }
        int mid = i;
        if (mid == s || mid == e) mid = (s + e) / 2;
        int left = nNodes;
        nNodes += 2;
        nodeA[node] = left; nodeN[node] = 0;
        build(m, left, s, mid, depth + 1);
        build(m, left + 1, mid, e, depth + 1);
    }

    /** true, wenn der Strahl innerhalb tMax auf ein Dreieck trifft. */
    public boolean occluded(float ox, float oy, float oz, float dx, float dy, float dz, float tMax, int[] stack) {
        float ix = 1f / (Math.abs(dx) < 1e-9f ? 1e-9f : dx), iy = 1f / (Math.abs(dy) < 1e-9f ? 1e-9f : dy),
                iz = 1f / (Math.abs(dz) < 1e-9f ? 1e-9f : dz);
        int sp = 0;
        stack[sp++] = 0;
        while (sp > 0) {
            int node = stack[--sp];
            int b = 6 * node;
            float t0 = (box[b] - ox) * ix, t1 = (box[b + 3] - ox) * ix;
            float tmin = Math.min(t0, t1), tmax = Math.max(t0, t1);
            t0 = (box[b + 1] - oy) * iy; t1 = (box[b + 4] - oy) * iy;
            tmin = Math.max(tmin, Math.min(t0, t1)); tmax = Math.min(tmax, Math.max(t0, t1));
            t0 = (box[b + 2] - oz) * iz; t1 = (box[b + 5] - oz) * iz;
            tmin = Math.max(tmin, Math.min(t0, t1)); tmax = Math.min(tmax, Math.max(t0, t1));
            if (tmax < Math.max(tmin, 0) || tmin > tMax) continue;
            int n = nodeN[node];
            if (n == 0) {
                stack[sp++] = nodeA[node];
                stack[sp++] = nodeA[node] + 1;
                continue;
            }
            for (int i = nodeA[node], e = i + n; i < e; i++) {
                int o = 9 * i;
                float e1x = tri[o + 3] - tri[o], e1y = tri[o + 4] - tri[o + 1], e1z = tri[o + 5] - tri[o + 2];
                float e2x = tri[o + 6] - tri[o], e2y = tri[o + 7] - tri[o + 1], e2z = tri[o + 8] - tri[o + 2];
                float px = dy * e2z - dz * e2y, py = dz * e2x - dx * e2z, pz = dx * e2y - dy * e2x;
                float det = e1x * px + e1y * py + e1z * pz;
                if (Math.abs(det) < 1e-10f) continue;
                float inv = 1f / det;
                float sx = ox - tri[o], sy = oy - tri[o + 1], sz = oz - tri[o + 2];
                float u = (sx * px + sy * py + sz * pz) * inv;
                if (u < 0 || u > 1) continue;
                float qx = sy * e1z - sz * e1y, qy = sz * e1x - sx * e1z, qz = sx * e1y - sy * e1x;
                float v = (dx * qx + dy * qy + dz * qz) * inv;
                if (v < 0 || u + v > 1) continue;
                float t = (e2x * qx + e2y * qy + e2z * qz) * inv;
                if (t > 1e-4f && t < tMax) return true;
            }
        }
        return false;
    }

    /** Nächster Treffer; liefert das Dreieck (Netzindex) oder -1, Abstand in tOut[0]. */
    public int closest(float ox, float oy, float oz, float dx, float dy, float dz, float tMax, int[] stack, float[] tOut) {
        float ix = 1f / (Math.abs(dx) < 1e-9f ? 1e-9f : dx), iy = 1f / (Math.abs(dy) < 1e-9f ? 1e-9f : dy),
                iz = 1f / (Math.abs(dz) < 1e-9f ? 1e-9f : dz);
        int sp = 0, best = -1;
        float bestT = tMax;
        stack[sp++] = 0;
        while (sp > 0) {
            int node = stack[--sp];
            int b = 6 * node;
            float t0 = (box[b] - ox) * ix, t1 = (box[b + 3] - ox) * ix;
            float tmin = Math.min(t0, t1), tmax = Math.max(t0, t1);
            t0 = (box[b + 1] - oy) * iy; t1 = (box[b + 4] - oy) * iy;
            tmin = Math.max(tmin, Math.min(t0, t1)); tmax = Math.min(tmax, Math.max(t0, t1));
            t0 = (box[b + 2] - oz) * iz; t1 = (box[b + 5] - oz) * iz;
            tmin = Math.max(tmin, Math.min(t0, t1)); tmax = Math.min(tmax, Math.max(t0, t1));
            if (tmax < Math.max(tmin, 0) || tmin > bestT) continue;
            int n = nodeN[node];
            if (n == 0) {
                stack[sp++] = nodeA[node];
                stack[sp++] = nodeA[node] + 1;
                continue;
            }
            for (int i = nodeA[node], e = i + n; i < e; i++) {
                int o = 9 * i;
                float e1x = tri[o + 3] - tri[o], e1y = tri[o + 4] - tri[o + 1], e1z = tri[o + 5] - tri[o + 2];
                float e2x = tri[o + 6] - tri[o], e2y = tri[o + 7] - tri[o + 1], e2z = tri[o + 8] - tri[o + 2];
                float px = dy * e2z - dz * e2y, py = dz * e2x - dx * e2z, pz = dx * e2y - dy * e2x;
                float det = e1x * px + e1y * py + e1z * pz;
                if (Math.abs(det) < 1e-10f) continue;
                float inv = 1f / det;
                float sx = ox - tri[o], sy = oy - tri[o + 1], sz = oz - tri[o + 2];
                float u = (sx * px + sy * py + sz * pz) * inv;
                if (u < 0 || u > 1) continue;
                float qx = sy * e1z - sz * e1y, qy = sz * e1x - sx * e1z, qz = sx * e1y - sy * e1x;
                float v = (dx * qx + dy * qy + dz * qz) * inv;
                if (v < 0 || u + v > 1) continue;
                float t = (e2x * qx + e2y * qy + e2z * qz) * inv;
                if (t > 1e-4f && t < bestT) { bestT = t; best = order[i]; }
            }
        }
        tOut[0] = bestT;
        return best;
    }

    private static float[][] samples(int rays) {
        float[] sx = new float[rays], sy = new float[rays], sz = new float[rays];
        for (int i = 0; i < rays; i++) {
            double u1 = (i + 0.5) / rays, u2 = radInv(i);
            double r = Math.sqrt(u1), phi = 2 * Math.PI * u2;
            sx[i] = (float) (r * Math.cos(phi)); sy[i] = (float) (r * Math.sin(phi)); sz[i] = (float) Math.sqrt(1 - u1);
        }
        return new float[][]{sx, sy, sz};
    }

    /** Richtung des i-ten Strahls der Ecke v (kosinusgewichtet, je Ecke gedreht) – beim Backen und beim Rückwurf gleich. */
    static void rayDir(Mesh m, int v, int i, float[][] smp, float[] out) {
        int o = 3 * v;
        float nx = m.nrm[o], ny = m.nrm[o + 1], nz = m.nrm[o + 2];
        float ax = Math.abs(nx) < 0.9f ? 1 : 0, ay = ax == 1 ? 0 : 1;
        float tx = ay * nz, ty = -ax * nz, tz = ax * ny - ay * nx;
        float tl = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
        tx /= tl; ty /= tl; tz /= tl;
        float bx = ny * tz - nz * ty, by = nz * tx - nx * tz, bz = nx * ty - ny * tx;
        double rot = (Noise.hash(v, 7, 3) & 0xFFFF) / 65536.0 * 2 * Math.PI;
        float cr = (float) Math.cos(rot), sr = (float) Math.sin(rot);
        float a = smp[0][i] * cr - smp[1][i] * sr, b2 = smp[0][i] * sr + smp[1][i] * cr, c = smp[2][i];
        out[0] = tx * a + bx * b2 + nx * c; out[1] = ty * a + by * b2 + ny * c; out[2] = tz * a + bz * b2 + nz * c;
    }

    /**
     * Berechnet für jede Ecke den Anteil des sichtbaren Himmels (kosinusgewichtet) und merkt sich
     * für jeden zweiten Strahl das getroffene Dreieck und den Abstand (für den Lichtrückwurf).
     * progress wird mit Werten 0..1 aufgerufen (darf null sein).
     */
    public static void bakeSky(Mesh m, int rays, java.util.function.DoubleConsumer progress) {
        Bvh bvh = new Bvh(m);
        float[][] smp = samples(rays);
        int nb = rays / 2;
        m.bounceRays = nb;
        m.hitTri = new int[m.nv * nb];
        m.hitT = new float[m.nv * nb];
        java.util.concurrent.atomic.AtomicInteger done = new java.util.concurrent.atomic.AtomicInteger();
        int nv = m.nv, block = 2048;
        int blocks = (nv + block - 1) / block;
        java.util.stream.IntStream.range(0, blocks).parallel().forEach(bi -> {
            int[] stack = new int[128];
            float[] d = new float[3], tt = new float[1];
            for (int v = bi * block; v < Math.min(nv, (bi + 1) * block); v++) {
                int o = 3 * v;
                float px = m.pos[o] + m.nrm[o] * 0.06f, py = m.pos[o + 1] + m.nrm[o + 1] * 0.06f, pz = m.pos[o + 2] + m.nrm[o + 2] * 0.06f;
                int free = 0;
                for (int i = 0; i < rays; i++) {
                    rayDir(m, v, i, smp, d);
                    if ((i & 1) == 0) {
                        int hit = bvh.closest(px, py, pz, d[0], d[1], d[2], 400f, stack, tt);
                        m.hitTri[v * nb + i / 2] = hit;
                        m.hitT[v * nb + i / 2] = tt[0];
                        if (hit < 0 || tt[0] > 150f) free++;
                    } else if (!bvh.occluded(px, py, pz, d[0], d[1], d[2], 150f, stack)) {
                        free++;
                    }
                }
                m.sky[v] = free / (float) rays;
            }
            int dn = done.incrementAndGet();
            if (progress != null) progress.accept(dn / (double) blocks);
        });
    }

    /** Lichtrückwurf je Ecke: Licht, das die Strahlen an ihren Trefferpunkten vorfinden, einmal zurückgeworfen. */
    public static void bounce(Mesh m, Lighting L, float[] out) {
        int nb = m.bounceRays;
        if (m.hitTri == null || nb == 0) { java.util.Arrays.fill(out, 0f); return; }
        float[][] smp = samples(nb * 2);
        Sky s = L.sky;
        float lx = (float) s.sun[0], ly = (float) s.sun[1], lz = (float) s.sun[2];
        boolean sunUp = s.sunR + s.sunG + s.sunB > 0.001f;
        int nv = m.nv, block = 4096;
        java.util.stream.IntStream.range(0, (nv + block - 1) / block).parallel().forEach(bi -> {
            float[] d = new float[3];
            for (int v = bi * block; v < Math.min(nv, (bi + 1) * block); v++) {
                int o = 3 * v;
                float px = m.pos[o] + m.nrm[o] * 0.06f, py = m.pos[o + 1] + m.nrm[o + 1] * 0.06f, pz = m.pos[o + 2] + m.nrm[o + 2] * 0.06f;
                float sr = 0, sg = 0, sb = 0;
                for (int k = 0; k < nb; k++) {
                    int t = m.hitTri[v * nb + k];
                    if (t < 0) continue;
                    float dist = m.hitT[v * nb + k];
                    if (dist > 150f) continue;
                    rayDir(m, v, 2 * k, smp, d);
                    float qx = px + d[0] * dist, qy = py + d[1] * dist, qz = pz + d[2] * dist;
                    float nx = m.fn[3 * t], ny = m.fn[3 * t + 1], nz = m.fn[3 * t + 2];
                    if (nx * d[0] + ny * d[1] + nz * d[2] > 0) { nx = -nx; ny = -ny; nz = -nz; }
                    float er = 0, eg = 0, eb = 0;
                    float ndl = nx * lx + ny * ly + nz * lz;
                    if (sunUp && ndl > 0) {
                        float lit = L.litHard(qx + nx * 0.1f, qy + ny * 0.1f, qz + nz * 0.1f);
                        er = s.sunR * ndl * lit; eg = s.sunG * ndl * lit; eb = s.sunB * ndl * lit;
                    }
                    int i0 = m.idx[3 * t], i1 = m.idx[3 * t + 1], i2 = m.idx[3 * t + 2];
                    float sq = (m.sky[i0] + m.sky[i1] + m.sky[i2]) / 3f;
                    float ar, ag, ab;
                    if (ny >= 0) { ar = s.sideR + (s.upR - s.sideR) * ny; ag = s.sideG + (s.upG - s.sideG) * ny; ab = s.sideB + (s.upB - s.sideB) * ny; }
                    else { ar = s.sideR + (s.downR - s.sideR) * -ny; ag = s.sideG + (s.downG - s.sideG) * -ny; ab = s.sideB + (s.downB - s.sideB) * -ny; }
                    er += ar * sq; eg += ag * sq; eb += ab * sq;
                    float[] a = Materials.AVG[m.mat[t]];
                    sr += a[0] * er; sg += a[1] * eg; sb += a[2] * eb;
                }
                float k = 1.3f / nb;
                out[3 * v] = sr * k; out[3 * v + 1] = sg * k; out[3 * v + 2] = sb * k;
            }
        });
        smooth(m, out);
    }

    // Glättung über ein Gitter mit Umgebungswürfeln (sechs Richtungen je Zelle)
    private static final float G0X = -112, G0Y = -3, G0Z = -62, GC = 2.0f;
    private static final int GX = 112, GY = 28, GZ = 72;

    /**
     * Die rohen Werte je Ecke (24 Strahlen) rauschen. Sie werden in ein grobes Raumgitter mit je sechs
     * Richtungsfächern gestreut und wieder eingesammelt; getrennte Fächer für Innen- und Außenseiten
     * halten das Licht davon ab, durch Mauern zu sickern.
     */
    private static void smooth(Mesh m, float[] val) {
        int cells = GX * GY * GZ;
        float[] acc = new float[cells * 18];
        float[] wsum = new float[cells * 6];
        float[] w6 = new float[6];
        int[] ci = new int[8];
        float[] cw = new float[8];
        for (int v = 0; v < m.nv; v++) {
            if (!corners(m, v, ci, cw)) continue;
            dirWeights(m, v, w6);
            for (int d = 0; d < 6; d++) {
                if (w6[d] <= 0) continue;
                for (int k = 0; k < 8; k++) {
                    float w = w6[d] * cw[k];
                    int c = ci[k];
                    acc[(c * 6 + d) * 3] += val[3 * v] * w;
                    acc[(c * 6 + d) * 3 + 1] += val[3 * v + 1] * w;
                    acc[(c * 6 + d) * 3 + 2] += val[3 * v + 2] * w;
                    wsum[c * 6 + d] += w;
                }
            }
        }
        for (int i = 0; i < cells * 6; i++) {
            float w = wsum[i];
            if (w > 1e-5f) { acc[3 * i] /= w; acc[3 * i + 1] /= w; acc[3 * i + 2] /= w; }
        }
        for (int v = 0; v < m.nv; v++) {
            if (!corners(m, v, ci, cw)) continue;
            dirWeights(m, v, w6);
            float r = 0, g = 0, b = 0, ws = 0;
            for (int d = 0; d < 6; d++) {
                if (w6[d] <= 0) continue;
                for (int k = 0; k < 8; k++) {
                    int i = ci[k] * 6 + d;
                    if (wsum[i] < 0.02f) continue;
                    float w = w6[d] * cw[k];
                    r += acc[3 * i] * w; g += acc[3 * i + 1] * w; b += acc[3 * i + 2] * w; ws += w;
                }
            }
            if (ws > 1e-5f) { val[3 * v] = r / ws; val[3 * v + 1] = g / ws; val[3 * v + 2] = b / ws; }
        }
    }

    private static boolean corners(Mesh m, int v, int[] ci, float[] cw) {
        float fx = (m.pos[3 * v] - G0X) / GC - 0.5f, fy = (m.pos[3 * v + 1] - G0Y) / GC - 0.5f, fz = (m.pos[3 * v + 2] - G0Z) / GC - 0.5f;
        int ix = (int) Math.floor(fx), iy = (int) Math.floor(fy), iz = (int) Math.floor(fz);
        if (ix < 0 || iy < 0 || iz < 0 || ix >= GX - 1 || iy >= GY - 1 || iz >= GZ - 1) return false;
        float tx = fx - ix, ty = fy - iy, tz = fz - iz;
        for (int k = 0; k < 8; k++) {
            int dx = k & 1, dy = (k >> 1) & 1, dz = (k >> 2) & 1;
            ci[k] = ((iz + dz) * GY + (iy + dy)) * GX + (ix + dx);
            cw[k] = (dx == 1 ? tx : 1 - tx) * (dy == 1 ? ty : 1 - ty) * (dz == 1 ? tz : 1 - tz);
        }
        return true;
    }

    private static void dirWeights(Mesh m, int v, float[] w) {
        float nx = m.nrm[3 * v], ny = m.nrm[3 * v + 1], nz = m.nrm[3 * v + 2];
        w[0] = nx > 0 ? nx * nx : 0; w[1] = nx < 0 ? nx * nx : 0;
        w[2] = ny > 0 ? ny * ny : 0; w[3] = ny < 0 ? ny * ny : 0;
        w[4] = nz > 0 ? nz * nz : 0; w[5] = nz < 0 ? nz * nz : 0;
    }

    private static double radInv(int i) {
        long b = Integer.reverse(i) & 0xFFFFFFFFL;
        return b / 4294967296.0;
    }
}
