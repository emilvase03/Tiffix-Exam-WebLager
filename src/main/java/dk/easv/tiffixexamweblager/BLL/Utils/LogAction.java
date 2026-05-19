package dk.easv.tiffixexamweblager.BLL.Utils;

public enum LogAction {
    CREATE(1),
    READ(2),
    UPDATE(3),
    DELETE(4),
    LOGIN(5),
    LOGOUT(6);

    private final int id;

    LogAction(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}