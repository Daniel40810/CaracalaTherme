package com.dan.caracalla.geo;

/**
 * Fertiges Dreiecksnetz der Szene. Dreiecke sind nach räumlichen Blöcken (Chunks)
 * sortiert, damit der Renderer ganze Blöcke gegen das Sichtfeld prüfen kann.
 */
public final class Mesh {
    public final int nv, nt;
    public final float[] pos, nrm;   // 3 je Ecke
    public final float[] sky;        // Himmelssicht je Ecke, 0..1
    /** Treffer der Rückwurfstrahlen je Ecke (Dreieck, -1 = Himmel) und Abstand; bounceRays je Ecke. */
    public int bounceRays;
    public int[] hitTri;
    public float[] hitT;
    public final int[] idx;          // 3 je Dreieck
    public final byte[] mat, grp;    // je Dreieck
    public final float[] fn;         // Flächennormale, 3 je Dreieck
    public final int nChunks;
    public final int[] chunkStart, chunkCount, chunkGrp;
    public final float[] chunkBox;   // minX,minY,minZ,maxX,maxY,maxZ

    Mesh(int nv, int nt, float[] pos, float[] nrm, int[] idx, byte[] mat, byte[] grp, float[] fn,
         int nChunks, int[] cs, int[] cc, int[] cg, float[] cb) {
        this.nv = nv; this.nt = nt; this.pos = pos; this.nrm = nrm; this.idx = idx;
        this.mat = mat; this.grp = grp; this.fn = fn; this.nChunks = nChunks;
        this.chunkStart = cs; this.chunkCount = cc; this.chunkGrp = cg; this.chunkBox = cb;
        this.sky = new float[nv];
        java.util.Arrays.fill(sky, 1f);
    }
}
