package dk.easv.tiffixexamweblager.BE;

import java.time.LocalDateTime;

public class Box {
    private int id;
    private int number;
    private String title;
    private LocalDateTime createdAt;
    private int createdByUserId;
    private String createdByUsername;
    private int documentsAmount;
    private int pagesAmount;
    private Integer profileId;
    private int customerId;

    public Box(int id, int number, String title, LocalDateTime createdAt,
               String createdByUsername, int documentsAmount, int pagesAmount, int customerId) {
        this.id                = id;
        this.number            = number;
        this.title             = title;
        this.createdAt         = createdAt;
        this.createdByUsername = createdByUsername;
        this.documentsAmount   = documentsAmount;
        this.pagesAmount       = pagesAmount;
        this.customerId = customerId;
    }

    public Box(int id, int number, String title, LocalDateTime createdAt,
               int createdByUserId, int documentsAmount, int pagesAmount, int customerId) {
        this.id              = id;
        this.number          = number;
        this.title           = title;
        this.createdAt       = createdAt;
        this.createdByUserId = createdByUserId;
        this.documentsAmount = documentsAmount;
        this.pagesAmount     = pagesAmount;
        this.customerId = customerId;
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

    public String getCreatedByUsername() {
        return createdByUsername;
    }
    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
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

    public Integer getProfileId() { return profileId; }
    public void setProfileId(Integer profileId) { this.profileId = profileId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    @Override
    public String toString() {
        return "Box " + getNumber();
    }
}