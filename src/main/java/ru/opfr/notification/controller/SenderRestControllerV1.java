package ru.opfr.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.opfr.notification.constraint.impl.FilesSizeValidator;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;
import ru.opfr.notification.model.dto.ResponseWrapper;
import ru.opfr.notification.service.SenderServiceFacadeSafeWrapper;

import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class SenderRestControllerV1 {
    private final SenderServiceFacadeSafeWrapper senderService;
    private final FilesSizeValidator filesSizeValidator;

    @PostMapping()
    public ResponseEntity<ResponseWrapper> sendNotification(@RequestPart(required = false) Request request,
                                                            @RequestParam(required = false) MultipartFile[] files,
                                                            BindingResult bindingResult) {
        filesSizeValidator.validate(files, bindingResult);
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getCode).collect(Collectors.joining(", "));
            ResponseWrapper wrapper = ResponseWrapper.builder().version("v1").message(message).build();
            return new ResponseEntity<>(wrapper, BAD_REQUEST);
        }
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
