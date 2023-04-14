package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import ru.opfr.notification.converters.RequestNotificationConverterImpl;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.*;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.*;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class SenderServiceFacadeImplTest {
    @Mock
    private NotificationService notificationService;
    @Mock
    private RequestNotificationConverterImpl requestNotificationConverter;
    @Mock
    private SenderServiceSafeWrapper senderServiceSafeWrapper;
    private SenderServiceFacade facade;

    private final String modelRequestId = "remote-999";
    private final long modelNotificationId = 999L;
    final static String PROCESSED_STAGE_MESSAGE = "processed successfully";
    final static String FAILED_STAGE_MESSAGE = "processed unsuccessfully";


    @Test
    void sendWithSuccessfulSendOperation_AndGetSuccessfulResponse_AndCheckTwoStagesInInnerNotificationObject() throws CreationNotificationException {
        NotificationTypeDictionary type = MESSAGE;
        Request request = getRequestByType(type);

        Map<NotificationTypeDictionary, SenderService> sendersMap = new HashMap<>();
        boolean sendOperation = true;
        String resultMessage = PROCESSED_STAGE_MESSAGE;
        SenderService service = createSenderService(type, sendOperation, resultMessage);
        sendersMap.put(service.getType(), spy(service));

        when(senderServiceSafeWrapper.safeSend(any(Notification.class))).thenReturn(true);
        when(notificationService.addStageWithMessageAndSave(any(), any(), any())).then((invocation) -> {
            Notification notification = invocation.getArgument(2);
            notification.setId(modelNotificationId);
            return notification;
        });
        mockConvertByRequest(request);
        facade = new SenderServiceFacadeImpl(notificationService, senderServiceSafeWrapper, sendersMap, requestNotificationConverter);

        Response response = facade.send(request);

        verify(requestNotificationConverter).convert(request);
        verify(notificationService, never()).save(any(Notification.class));
        verify(notificationService).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
        verify(notificationService).addStageWithMessageAndSave(eq(PROCESSED), eq(PROCESSED_STAGE_MESSAGE), any(Notification.class));
        verify(senderServiceSafeWrapper).safeSend(any());
        verify(sendersMap.get(MESSAGE), times(2)).getSendingResultMessage();

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals(modelRequestId, response.remoteId);
        assertEquals(modelNotificationId, response.operationId);
        assertEquals(PROCESSED_STAGE_MESSAGE, response.message);
    }

    @Test
    void sendNullRequest_AndThrowException() throws CreationNotificationException {
        NotificationTypeDictionary type = MESSAGE;

        Map<NotificationTypeDictionary, SenderService> sendersMap = new HashMap<>();
        boolean sendOperation = true;
        String resultMessage = PROCESSED_STAGE_MESSAGE;
        SenderService service = createSenderService(type, sendOperation, resultMessage);
        sendersMap.put(service.getType(), spy(service));

        mockConvertByRequest(null);
        facade = new SenderServiceFacadeImpl(notificationService, senderServiceSafeWrapper, sendersMap, requestNotificationConverter);

        assertThrows(CreationNotificationException.class, () -> facade.send(null));

        verify(requestNotificationConverter).convert(any());
        verify(notificationService, never()).addStageWithMessageAndSave(any(), isNull(), any());
        verify(senderServiceSafeWrapper, never()).safeSend(any());
        verify(sendersMap.get(MESSAGE), never()).getSendingResultMessage();
    }

    @Test
    void sendWithThrowingExceptionInNotificationService_InFirstStageAdding_AndThrowException() throws CreationNotificationException {
        NotificationTypeDictionary type = MESSAGE;
        Request request = getRequestByType(type);

        Map<NotificationTypeDictionary, SenderService> sendersMap = new HashMap<>();
        boolean sendOperation = true;
        String resultMessage = PROCESSED_STAGE_MESSAGE;
        SenderService service = createSenderService(type, sendOperation, resultMessage);
        sendersMap.put(service.getType(), spy(service));

        Throwable error = new CreationNotificationException("Creation error");
        doThrow(error).when(notificationService).addStageWithMessageAndSave(eq(RECEIVED), any(), any());
        mockConvertByRequest(request);
        facade = new SenderServiceFacadeImpl(notificationService, senderServiceSafeWrapper, sendersMap, requestNotificationConverter);

        assertThrows(CreationNotificationException.class, () -> facade.send(request));

        verify(requestNotificationConverter).convert(request);
        verify(notificationService, never()).save(any(Notification.class));
        verify(notificationService).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
        verify(notificationService, never()).addStageWithMessageAndSave(eq(PROCESSED), any(), any());
        verify(senderServiceSafeWrapper, never()).safeSend(any());
        verify(sendersMap.get(MESSAGE), never()).getSendingResultMessage();
    }

    @Test
    void sendWithUnsuccessfulSendOperation_AndGetUnsuccessfulResponse_AndCheckTwoStagesInInnerNotificationObject() throws CreationNotificationException, SendNotificationException {
        NotificationTypeDictionary type = MESSAGE;
        Request request = getRequestByType(type);

        Map<NotificationTypeDictionary, SenderService> sendersMap = new HashMap<>();
        boolean sendOperation = false;
        String resultMessage = FAILED_STAGE_MESSAGE;
        SenderService service = createSenderService(type, sendOperation, resultMessage);
        sendersMap.put(service.getType(), spy(service));

        when(senderServiceSafeWrapper.safeSend(any(Notification.class))).thenReturn(false);
        when(notificationService.addStageWithMessageAndSave(any(), any(), any())).then((invocation) -> {
            Notification notification = invocation.getArgument(2);
            notification.setId(modelNotificationId);
            return notification;
        });

        mockConvertByRequest(request);
        facade = new SenderServiceFacadeImpl(notificationService, senderServiceSafeWrapper, sendersMap, requestNotificationConverter);

        Response response = facade.send(request);

        verify(requestNotificationConverter).convert(request);

        verify(notificationService, never()).save(any(Notification.class));
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(FAILED), eq(FAILED_STAGE_MESSAGE), any(Notification.class));
        verify(sendersMap.get(MESSAGE), times(2)).getSendingResultMessage();

        assertNotNull(response);
        assertFalse(response.success);
        assertEquals(request.id, response.remoteId);
        assertEquals(modelNotificationId, response.operationId);
        assertEquals(FAILED_STAGE_MESSAGE, response.message);

    }

    @Test
    void sendWithThrowingExceptionInNotificationService_InSecondStageAdding_AndThrowException() throws CreationNotificationException {
        NotificationTypeDictionary type = MESSAGE;
        Request request = getRequestByType(type);

        Map<NotificationTypeDictionary, SenderService> sendersMap = new HashMap<>();
        boolean sendOperation = true;
        String resultMessage = PROCESSED_STAGE_MESSAGE;
        SenderService service = createSenderService(type, sendOperation, resultMessage);
        sendersMap.put(service.getType(), spy(service));

        when(senderServiceSafeWrapper.safeSend(any(Notification.class))).thenReturn(true);
        when(notificationService.addStageWithMessageAndSave(eq(RECEIVED), any(), any())).then((invocation) -> invocation.getArgument(2));
        Throwable error = new CreationNotificationException("Creation error");
        doThrow(error).when(notificationService).addStageWithMessageAndSave(eq(PROCESSED), any(), any());
        mockConvertByRequest(request);
        facade = new SenderServiceFacadeImpl(notificationService, senderServiceSafeWrapper, sendersMap, requestNotificationConverter);

        assertThrows(CreationNotificationException.class, () -> facade.send(request));

        verify(requestNotificationConverter).convert(request);
        verify(notificationService, never()).save(any(Notification.class));
        verify(notificationService).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
        verify(notificationService).addStageWithMessageAndSave(eq(PROCESSED), any(), any());
        verify(senderServiceSafeWrapper).safeSend(any());
        verify(sendersMap.get(MESSAGE)).getSendingResultMessage();
    }

    private Request getRequestByType(NotificationTypeDictionary type) {
        Request request = new Request();
        request.type = type.toString();
        request.id = modelRequestId;
        request.user = "073User";
        request.ip = "10.73.13.14";
        request.email = "user@server.ru";
        request.content = "Content";
        request.theme = "Theme";
        return request;
    }

    private void mockConvertByRequest(Request request) throws CreationNotificationException {
        if (Optional.ofNullable(request).isPresent()) {
            Notification notification = new Notification();
            Person person = new Person();
            person.setUser(request.user);
            person.setIp(request.ip);
            person.setEmail(request.email);
            notification.setPerson(person);
            notification.setType(NotificationTypeDictionary.of(request.type));
            notification.setRemoteId(request.id);
            notification.setContent(request.content);
            notification.setTheme(request.theme);
            when(requestNotificationConverter.convert(request)).thenReturn(notification);
        } else {
            Throwable error = new CreationNotificationException("Creation error");
            doThrow(error).when(requestNotificationConverter).convert(any());
        }

    }

    private SenderService createSenderService(
            NotificationTypeDictionary type,
            boolean sendOperation,
            String resultMessage
    ) {
        return new SenderService() {
            @Override
            public NotificationTypeDictionary getType() {
                return type;
            }

            @Override
            public boolean send(Notification notification) throws SendNotificationException {
                return sendOperation;
            }

            @Override
            public void afterSending(Notification notification, boolean result) throws SendNotificationException {}


            @Override
            public String getSendingResultMessage() {
                return resultMessage;
            }

            @Override
            public void setErrorMessage(Throwable e) {}
        };
    }



}