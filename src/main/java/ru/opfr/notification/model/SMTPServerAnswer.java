package ru.opfr.notification.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SMTPServerAnswer {
    public final int code;
    public final String message;

}
