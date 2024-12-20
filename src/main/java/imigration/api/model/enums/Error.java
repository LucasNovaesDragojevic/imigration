package imigration.api.model.enums;

public enum Error {

    E1000("Invalid request fields."),
    E1001("User already exists."),
    E1002("User is locked."),
    E1003("User credentials are expired."),
    E1004("User account is expired."),
    E1005("User is disabled."),
    E1006("User or password is invalid."), 
    E1007("Token not found."),
    E1008("Token validated."),
    E1009("Token expired."), 
    E1010("Token not validated."), 
    E1011("Token invalid for user"), 
    E1012("User not found."),
    E1013("Bearer token expired.");

    private final String title;

    private Error(final String title) {
        this.title = title;
    }

    public final String getTitle() {
        return this.title;
    }
}
