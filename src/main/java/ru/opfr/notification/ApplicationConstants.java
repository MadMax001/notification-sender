package ru.opfr.notification;

public class ApplicationConstants {
    public static final String NULL_REQUEST = "null-request";
    public static final String NULL_TYPE = "null-type";
    public static final String NO_STAGES = "no stages";
    public static final String NULL_CONTENT = "null-content";
    public static final String NULL_USER = "User can't be null with MESSAGE type";                                  //todo Null-user for MESSAGE type
    public static final String NULL_IP = "IP can't be null with MESSAGE type";                                      //todo
    public static final String NULL_EMAIL = "Email can't be null with EMAIL or FILE types";                         //todo
    public static final String MAX_LENGTH_USER = "User has more than 255 characters";
    public static final String WRONG_IP = "Wrong ip address";
    public static final String MAX_LENGTH_EMAIL = "Email has more than 255 characters";
    public static final String WRONG_EMAIL = "Wrong email according to OWASP";
    public static final String MAX_LENGTH_THEME = "Theme has more than 255 characters";
    public static final String MAX_LENGTH_REMOTE_ID = "Id has more than 255 characters";
    public static final String MAX_LENGTH_STAGE_MESSAGE = "Message in stage has more than 255 characters";
    public static final String MAX_LENGTH_ATTACHMENT_NAME = "Filename has more than 255 characters";
    public static final String NULL_ATTACHMENT_NAME = "Null-filename";
    public static final String MAX_COUNT_ATTACHMENTS = "Attachments files more than 5";
    public static final String NULL_ATTACHMENTS = "Null-attachments";


    private ApplicationConstants() {
        throw new IllegalStateException("Constant class");
    }
}
