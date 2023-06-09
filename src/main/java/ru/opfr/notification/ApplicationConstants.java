package ru.opfr.notification;

public class ApplicationConstants {
    public static final String NULL_REQUEST = "Null-request";
    public static final String NULL_TYPE = "Null-type";
    public static final String NO_STAGES = "No stages";
    public static final String NULL_CONTENT = "Null-content";
    public static final String NULL_USER = "Null-user for MESSAGE type";
    public static final String NULL_IP = "Null-ip for MESSAGE type";
    public static final String NULL_EMAIL = "Null-email for EMAIL type";
    public static final String MAX_LENGTH_USER = "User has more than 255 characters";
    public static final String WRONG_IP = "Wrong ip address";
    public static final String MAX_LENGTH_EMAIL = "Email has more than 255 characters";
    public static final String WRONG_EMAIL = "Wrong email according to OWASP";
    public static final String MAX_LENGTH_THEME = "Theme has more than 255 characters";
    public static final String MAX_LENGTH_REMOTE_ID = "Id has more than 255 characters";
    public static final String MAX_LENGTH_STAGE_MESSAGE = "Message in stage has more than 255 characters";
    public static final String MAX_LENGTH_ATTACHMENT_NAME = "Filename has more than 255 characters";
    public static final String NULL_ATTACHMENT_NAME = "Null-filename";
    public static final String MAX_COUNT_ATTACHMENTS = "Attachment files more than 5";
    public static final String NULL_ATTACHMENTS = "Null-attachments";
    public static final String NULL_STAGE_TYPE = "Null-stage type";
    public static final String FILES_SIZE_TOO_LARGE = "Large size of attachment";


    private ApplicationConstants() {
        throw new IllegalStateException("Constant class");
    }
}
