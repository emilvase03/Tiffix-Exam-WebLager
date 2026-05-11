package dk.easv.tiffixexamweblager.BE;

public enum RuleType {
    ROTATE,
    BRIGHTNESS;

    public static RuleType fromDatabase(String value) {
        String typeOnly = value.trim().split("\\s+")[0];
        return RuleType.valueOf(typeOnly);
    }

}

