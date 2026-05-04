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

    public int getId() {
        return id; }

    public void setId(int id) {
        this.id = id; }

    public int getBoxId() {
        return boxId; }

    public int getSortOrder() {
        return sortOrder; }


}
