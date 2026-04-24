package dk.easv.tiffixexamweblager.BE;

public class Profile {
    private int id;
    private String title;

    public Profile(String title) {
        setTitle(title);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id != -1)
            this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (!title.isBlank())
            this.title = title;
    }
}
