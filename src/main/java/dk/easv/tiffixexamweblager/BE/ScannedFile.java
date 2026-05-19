package dk.easv.tiffixexamweblager.BE;

import java.awt.image.BufferedImage;

public class ScannedFile {

    private int    id;
    private int    documentId;
    private byte[] tiffFile;
    private int    scanOrder;
    private int    sortOrder;

    private String fileName;

    private String        filePath;
    private BufferedImage processedImage;
    private int           userRotation;
    private int           userBrightness;

    public ScannedFile(int id, int documentId, byte[] tiffFile, int scanOrder, int sortOrder) {
        this.id         = id;
        this.documentId = documentId;
        this.tiffFile   = tiffFile;
        this.scanOrder  = scanOrder;
        this.sortOrder  = sortOrder;
        assignOrderName(sortOrder);
    }

    public static ScannedFile unsaved(int order, String filePath, byte[] tiffFile) {
        ScannedFile f = new ScannedFile(0, 0, tiffFile, order, order);
        f.filePath = filePath;
        return f;
    }

    public void assignOrderName(int order) {
        this.fileName = "File " + order;
    }

    public int           getId()             {
        return id; }

    public int           getDocumentId()     {
        return documentId; }

    public byte[]        getTiffFile()       {
        return tiffFile; }

    public int           getScanOrder()      {
        return scanOrder; }

    public int           getSortOrder()      {
        return sortOrder; }

    public String        getFilePath()       {
        return filePath; }
    public BufferedImage getProcessedImage() {
        return processedImage; }

    public int           getUserRotation()   {
        return userRotation; }

    public int           getUserBrightness() {
        return userBrightness; }

    public String        getFileName()       {
        return fileName; }


    public void setId(int id)                        {
        this.id = id; }

    public void setSortOrder(int sortOrder)          {
        this.sortOrder = sortOrder; }

    public void setProcessedImage(BufferedImage img) {
        this.processedImage = img; }

    public void setUserRotation(int degrees)         {
        this.userRotation = degrees; }

    public void setUserBrightness(int brightness)    {
        this.userBrightness = brightness; }

    public void setFileName(String fileName)         {
        this.fileName = fileName; }
}