package dk.easv.tiffixexamweblager.BE;

public class Customer {
    private int id;
    private String name;
    private boolean isDeleted;

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

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    @Override
    public String toString() {
        return getName();
    }
}
