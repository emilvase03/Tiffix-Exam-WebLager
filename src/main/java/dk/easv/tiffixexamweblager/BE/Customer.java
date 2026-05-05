package dk.easv.tiffixexamweblager.BE;

public class Customer {
    private int id;
    private String name;

    public Customer(String name) {
        setName(name);
    }

    public void setName(String name) {
        if (!name.isBlank())
            this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        if (id != -1)
            this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return getName();
    }
}
