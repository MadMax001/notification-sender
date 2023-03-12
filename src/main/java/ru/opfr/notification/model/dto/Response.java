package ru.opfr.notification.model.dto;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class Response {
    public final Boolean success;
    public final String remoteId;
    public final Long operationId;
    public final String message;


}
