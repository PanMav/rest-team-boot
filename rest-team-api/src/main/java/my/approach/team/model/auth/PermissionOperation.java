package my.approach.team.model.auth;

import java.util.HashMap;
import java.util.Map;

public enum PermissionOperation {
    READ("read"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");

    private final String name;
    private static final Map<String, PermissionOperation> BY_NAME = new HashMap<>();

    static {
        for (PermissionOperation value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    PermissionOperation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PermissionOperation valueOfName(String name) {
        return BY_NAME.get(name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
