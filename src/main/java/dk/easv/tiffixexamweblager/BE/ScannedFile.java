package dk.easv.tiffixexamweblager.BE;

public class ScannedFile {

    private int id;
    private int documentId;
    private int scanOrder;
    private int sortOrder;
    private String filePath;
    private double rotationAngle;

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


    public static ScannedFile unsaved(int order, String filePath) {
        return new ScannedFile(
                0,
                0,
                order,
                order,
                filePath,
                0.0
        );
    }

    public int getScanOrder() {
        return scanOrder; }
    public int getSortOrder() {
        return sortOrder; }
    public String getFilePath() {
        return filePath; }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }
}