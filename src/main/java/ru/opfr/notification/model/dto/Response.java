package ru.opfr.notification.model.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@ToString
@EqualsAndHashCode
public class Response {
    public final Boolean success;
    public final String remoteId;
    public final Long operationId;
    public final String message;


}
