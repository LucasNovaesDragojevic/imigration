package imigration.api.model.enums;

public enum Error {

    E1000("Invalid request fields."),
    E1001("User already exists.");

    private final String title;

    Error(final String title) {
        this.title = title;
    }

    public final String getTitle() {
        return this.title;
    }
}
