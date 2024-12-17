package imigration.api.constant;

public abstract class Url {

    public static final String ID = "id";
    public static final String ID_PATH_VARIABLE = "/{" + ID + "}";
    public static final String SIGNIN = "/signin";
    public static final String SIGNUP = "/signup";
    public static final String EMAIL_VERIFICATIONS = "/email-verifications" + ID_PATH_VARIABLE;
    public static final String PASSWORDS = "/passwords";
    public static final String PASSWORDS_RECOVERY = PASSWORDS + "/recovery";
    public static final String PASSWORDS_RESET = PASSWORDS + "/reset" + ID_PATH_VARIABLE;
    public static final String USERS = "/users";
    public static final String USER = USERS + ID_PATH_VARIABLE;
    public static final String PROCESSES = "/processes";
    public static final String PROCESS = PROCESSES + ID_PATH_VARIABLE;
    public static final String COMMENTS = "/comments";
    public static final String COMMENTS_BY_PROCESS = PROCESS + COMMENTS;
    public static final String ATTACHMENTS = "/attachments";
    public static final String ATTACHMENTS_BY_COMMENT = COMMENTS + ID_PATH_VARIABLE + ATTACHMENTS;
}
