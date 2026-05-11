package dk.easv.tiffixexamweblager.BE;

public class Document {
    private int id;
    private int boxId;
    private int sortOrder;

    public Document(int id, int boxId, int sortOrder) {
        this.id = id;
        this.boxId = boxId;
        this.sortOrder = sortOrder;
    }

    public static Document unsaved(int boxId, int sortOrder) {
        return new Document(-1, boxId, sortOrder);
    }

    public boolean isUnsaved() {
        return id == -1;
    }

    public int getId() {
        return id; }

    public void setId(int id) {
        this.id = id; }

    public int getBoxId() {
        return boxId; }

    public int getSortOrder() {
        return sortOrder; }


    public void setSortOrder(int sortOrder) {
        this.sortOrder=sortOrder;
    }
}
