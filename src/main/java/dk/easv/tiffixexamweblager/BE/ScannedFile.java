package dk.easv.tiffixexamweblager.BE;

import java.awt.image.BufferedImage;

public class ScannedFile {

    private int id;
    private int documentId;
    private int scanOrder;
    private int sortOrder;
    private String filePath;
    private double rotationAngle;

    private byte[] rawBytes;
    private BufferedImage processedImage;

    private int userRotation;

    private int userBrightness;

    public ScannedFile(int id, int documentId,
                       int scanOrder, int sortOrder,
                       String filePath, double rotationAngle) {
        this.id = id;
        this.documentId = documentId;
        this.scanOrder = scanOrder;
        this.sortOrder = sortOrder;
        this.filePath = filePath;
        this.rotationAngle = rotationAngle;
    }

    //freshly scanned file that has not been saved to the database yet.
    public static ScannedFile unsaved(int order, String filePath, byte[] rawBytes) {
        ScannedFile f = new ScannedFile(0, 0, order, order, filePath, 0.0);
        f.rawBytes = rawBytes;
        return f;
    }

   // files loaded from the database in a previous session).
    public static ScannedFile unsaved(int order, String filePath) {
        return new ScannedFile(0, 0, order, order, filePath, 0.0);
    }

    public int    getScanOrder()       {
        return scanOrder; }
    public int    getSortOrder()       {
        return sortOrder; }
    public String getFilePath()        {
        return filePath; }
    public double getRotationAngle()   {
        return rotationAngle; }

    public byte[]        getRawBytes()       {
        return rawBytes; }

    public BufferedImage getProcessedImage() {
        return processedImage; }

    public int           getUserRotation()   {
        return userRotation; }

    public int           getUserBrightness() {
        return userBrightness; }

    public void setSortOrder(int sortOrder)            {
        this.sortOrder = sortOrder; }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle; }

    public void setProcessedImage(BufferedImage img)   {
        this.processedImage = img; }

    public void setUserRotation(int degrees)           {
        this.userRotation = degrees; }

    public void setUserBrightness(int brightness)      {
        this.userBrightness = brightness; }
}