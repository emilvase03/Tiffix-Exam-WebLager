package dk.easv.tiffixexamweblager.BE;

import java.time.LocalDateTime;

public class ScannedFile {

    private final int id;
    private final int documentId;
    private final int scanOrder;
    private int sortOrder;
    private final String filePath;
    private int rotationAngle;
    private final LocalDateTime createdAt;

    public ScannedFile(
            int id,
            int documentId,
            int scanOrder,
            int sortOrder,
            String filePath,
            int rotationAngle,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.documentId = documentId;
        this.scanOrder = scanOrder;
        this.sortOrder = sortOrder;
        this.filePath = filePath;
        this.rotationAngle = rotationAngle;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getDocumentId() {
        return documentId;
    }

    public int getScanOrder() {
        return scanOrder;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}