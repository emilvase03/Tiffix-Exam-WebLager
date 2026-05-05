package dk.easv.tiffixexamweblager.BE;

import java.time.LocalDateTime;

public class Box {
    private int id;
    private int number;
    private String title;
    private LocalDateTime createdAt;
    private int createdByUserId;
    private int documentsAmount;
    private int pagesAmount;

    public Box(int id, int number, String title, LocalDateTime createdAt, int createdByUserId, int documentsAmount, int pagesAmount) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.createdAt = createdAt;
        this.createdByUserId = createdByUserId;
        this.documentsAmount = documentsAmount;
        this.pagesAmount = pagesAmount;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getCreatedByUserId() {
        return createdByUserId;
    }
    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public int getDocumentsAmount() {
        return documentsAmount;
    }
    public void setDocumentsAmount(int documentsAmount) {
        this.documentsAmount = documentsAmount;
    }

    public int getPagesAmount() {
        return pagesAmount;
    }
    public void setPagesAmount(int pagesAmount) {
        this.pagesAmount = pagesAmount;
    }

    @Override
    public String toString() {
        return "Box{" +
                "id=" + id +
                ", number=" + number +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", createdByUserId=" + createdByUserId +
                ", documentsAmount=" + documentsAmount +
                ", pagesAmount=" + pagesAmount +
                '}';
    }
}
