package ru.opfr.notification;

public class ApplicationConstants {
    public static final String NULL_REQUEST = "null-request";
    public static final String NULL_TYPE = "null-type";
    public static final String NO_STAGES = "no stages";
    public static final String NULL_CONTENT = "null-content";
    public static final String NULL_USER = "User can't be null with MESSAGE type";
    public static final String NULL_IP = "IP can't be null with MESSAGE type";
    public static final String NULL_EMAIL = "Email can't be null with EMAIL or FILE types";

    private ApplicationConstants() {
        throw new IllegalStateException("Constant class");
    }
}
