package music.user.entity;

public enum Role {
    ADMIN, USER;

    String getRoleName() {
        if (this == ADMIN) {
            return "admin";
        } else {
            return "user";
        }
    }
}
