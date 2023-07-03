package ru.opfr.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.constraint.FileSize;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;
import ru.opfr.notification.model.dto.ResponseWrapper;
import ru.opfr.notification.service.SenderServiceFacadeSafeWrapper;

import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static ru.opfr.notification.ValidationMessages.FILES_SIZE_TOO_LARGE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Validated
public class SenderRestControllerV1 {
    private final SenderServiceFacadeSafeWrapper senderService;


    @PostMapping()
    public ResponseEntity<ResponseWrapper> sendNotification(
                                                      @RequestPart(required = false) Request request,
            @FileSize(message = FILES_SIZE_TOO_LARGE) @RequestPart(required = false) MultipartFile[] files
                                                            ) {
        if (Objects.nonNull(request)) {
            request.files = files;
            return sendNonNullNotification(request);
        }
        ResponseWrapper wrapper = ResponseWrapper.builder().version("v1").build();
        return new ResponseEntity<>(wrapper, BAD_REQUEST);
    }

    private ResponseEntity<ResponseWrapper> sendNonNullNotification(Request request) {
        Response response = senderService.safeSend(request);
        ResponseWrapper wrapper = ResponseWrapper.builder().response(response).version("v1").build();
        HttpStatus status = Boolean.TRUE.equals(response.success) ? CREATED : BAD_REQUEST;
        return new ResponseEntity<>(wrapper, status);
    }
}
