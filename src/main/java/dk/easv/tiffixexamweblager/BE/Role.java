package dk.easv.tiffixexamweblager.BE;

public enum Role {
    ADMIN(1),
    EMPLOYEE(2);

    private int id;

    Role(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public static Role fromId(int id) {
        for (Role role : values()) {
            if (role.id == id)
                return role;
        }
        throw new IllegalArgumentException("Unknown role id: " + id);
    }
}
