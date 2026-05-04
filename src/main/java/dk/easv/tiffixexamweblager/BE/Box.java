package dk.easv.tiffixexamweblager.BE;

public class Box {
    private int id;
    private int number;

    public Box(int id, int number) {
        this.id = id;
        this.number = number;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNumber() { return number; }
    public void setLabel(String label) {
        this.number = number; }

    @Override
    public String toString() {
        return "Box " + number;
    }
}
