package imigration.api.model.enums;

public enum Error {

    E1000("Invalid request fields."),
    E1001("User already exists."),
    E1002("User is locked."),
    E1003("User credentials are expired."),
    E1004("User account is expired."),
    E1005("User is disabled."),
    E1006("User or password is invalid.");

    private final String title;

    Error(final String title) {
        this.title = title;
    }

    public final String getTitle() {
        return this.title;
    }
}
