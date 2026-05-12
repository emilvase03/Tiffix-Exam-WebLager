package dk.easv.tiffixexamweblager.BE;

import java.awt.image.BufferedImage;

public class ScannedFile {

    // ── Persisted (match new DB columns) ──────────────────────────────────────
    private int    id;
    private int    documentId;
    private byte[] tiffFile;      // VARBINARY(MAX) – the raw TIFF bytes
    private int    scanOrder;
    private int    sortOrder;

    // ── Transient / in-session only (never written to DB) ─────────────────────
    /** Absolute path of the temp file written during scanning.
     *  Kept solely so BarcodeDetector.hasBarcode(File) can run.
     *  Null for files loaded from the database. */
    private String        filePath;
    private BufferedImage processedImage;
    private int           userRotation;
    private int           userBrightness;

    // ── Constructor (used when loading from DB) ────────────────────────────────
    public ScannedFile(int id, int documentId, byte[] tiffFile, int scanOrder, int sortOrder) {
        this.id         = id;
        this.documentId = documentId;
        this.tiffFile   = tiffFile;
        this.scanOrder  = scanOrder;
        this.sortOrder  = sortOrder;
    }

    // ── Factory methods ────────────────────────────────────────────────────────

    /** Freshly scanned file not yet saved to the database. */
    public static ScannedFile unsaved(int order, String filePath, byte[] tiffFile) {
        ScannedFile f = new ScannedFile(0, 0, tiffFile, order, order);
        f.filePath = filePath;
        return f;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int           getId()             { return id; }
    public int           getDocumentId()     { return documentId; }
    public byte[]        getTiffFile()       { return tiffFile; }
    public int           getScanOrder()      { return scanOrder; }
    public int           getSortOrder()      { return sortOrder; }
    public String        getFilePath()       { return filePath; }
    public BufferedImage getProcessedImage() { return processedImage; }
    public int           getUserRotation()   { return userRotation; }
    public int           getUserBrightness() { return userBrightness; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setId(int id)                        { this.id = id; }
    public void setSortOrder(int sortOrder)          { this.sortOrder = sortOrder; }
    public void setProcessedImage(BufferedImage img) { this.processedImage = img; }
    public void setUserRotation(int degrees)         { this.userRotation = degrees; }
    public void setUserBrightness(int brightness)    { this.userBrightness = brightness; }
}