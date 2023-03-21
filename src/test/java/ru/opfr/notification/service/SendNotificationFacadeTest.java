package ru.opfr.notification.service;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.opfr.notification.ApplicationConstants;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.exception.SendNotificationException;
import ru.opfr.notification.model.*;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;
import ru.opfr.notification.transformers.RequestNotificationTransformerImpl;

import java.time.LocalDateTime;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static ru.opfr.notification.model.NotificationProcessStageDictionary.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.*;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SendNotificationFacadeTestConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class SendNotificationFacadeTest {

    @MockBean
    private final NotificationService notificationService;
    @SpyBean
    private final RequestNotificationTransformerImpl requestNotificationTransformer;
    private final Map<NotificationTypeDictionary, SenderService> senderServiceMap;

    private SendNotificationFacade facade;

    private final String modelRequestId = "remote-999";
    private final long modelNotificationId = 999L;

    private final long receivedStageId = 1010L;
    private final long processedStageId = 1011L;

    private ThreadLocal<Notification> innerNotificationInProcess;
    final static String PROCESSED_STAGE_MESSAGE = "processed successfully";
    final static String FAILED_STAGE_MESSAGE = "processed unsuccessfully";

    @BeforeEach
    void setUp() {
        innerNotificationInProcess = new ThreadLocal<>();
    }

    @Test
    void sendNotificationWorkflow_WithSuccessfulSendOperation_AndGetSuccessfulResponse_AndCheckTwoStagesInInnerNotificationObject() throws CreationNotificationException, SendNotificationException {
        Map<NotificationTypeDictionary, SenderService> spiedSendersMap = new HashMap<>();
        for (SenderService service : senderServiceMap.values()) {
            SenderService mockSenderService = Mockito.spy(service);
            doReturn(true).when(mockSenderService).send(any(Notification.class));
            doReturn(PROCESSED_STAGE_MESSAGE).when(mockSenderService).getSendingResultMessage();
            spiedSendersMap.put(mockSenderService.getType(), mockSenderService);
        }

        facade = new SendNotificationFacadeImpl(notificationService, spiedSendersMap, requestNotificationTransformer);


        NotificationTypeDictionary type = MESSAGE;

        Request request = getRequestByType(type);

        mockAddStageAndSaveMethodInNotificationService();

        Response response = facade.sendNotificationWorkflow(request);

        verify(requestNotificationTransformer).transform(request);
        verify(notificationService, never()).save(any(Notification.class));
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(PROCESSED), eq(PROCESSED_STAGE_MESSAGE), any(Notification.class));
        verifyForInvokingOnlyOneSenderServiceInServiceMapByType(spiedSendersMap, type);

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals(modelRequestId, response.remoteId);
        assertNotNull(response.operationId);
        assertEquals(PROCESSED_STAGE_MESSAGE, response.message);

        Notification notification2 = innerNotificationInProcess.get();
        assertNotNull(notification2);
        assertEquals(modelNotificationId, notification2.getId());
        assertEquals(2, notification2.getStages().size());
        NotificationStage stage21 = notification2.getStages().get(0);
        assertEquals(RECEIVED, stage21.getStage());
        assertEquals(receivedStageId, stage21.getId());
        assertNull(stage21.getMessage());
        NotificationStage stage22 = notification2.getStages().get(1);
        assertEquals(PROCESSED, stage22.getStage());
        assertEquals(processedStageId, stage22.getId());
        assertEquals(PROCESSED_STAGE_MESSAGE, stage22.getMessage());

    }

    @Test
    void sendNotificationWorkflow_WithSuccessfulSendOperationWithNullOperationMessage_AndGetSuccessfulResponse_AndCheckTwoStagesInInnerNotificationObject() throws CreationNotificationException, SendNotificationException {
        Map<NotificationTypeDictionary, SenderService> spiedSendersMap = new HashMap<>();
        for (SenderService service : senderServiceMap.values()) {
            SenderService mockSenderService = Mockito.spy(service);
            doReturn(true).when(mockSenderService).send(any(Notification.class));
            doReturn(null).when(mockSenderService).getSendingResultMessage();
            spiedSendersMap.put(mockSenderService.getType(), mockSenderService);
        }

        facade = new SendNotificationFacadeImpl(notificationService, spiedSendersMap, requestNotificationTransformer);


        NotificationTypeDictionary type = MESSAGE;

        Request request = getRequestByType(type);

        mockAddStageAndSaveMethodInNotificationService();

        Response response = facade.sendNotificationWorkflow(request);

        verify(requestNotificationTransformer).transform(request);
        verify(notificationService, never()).save(any(Notification.class));
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(PROCESSED), isNull(), any(Notification.class));
        verifyForInvokingOnlyOneSenderServiceInServiceMapByType(spiedSendersMap, type);

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals(modelRequestId, response.remoteId);
        assertNotNull(response.operationId);
        assertNull(response.message);

        Notification notification2 = innerNotificationInProcess.get();
        assertNotNull(notification2);
        assertEquals(modelNotificationId, notification2.getId());
        assertEquals(2, notification2.getStages().size());
        NotificationStage stage21 = notification2.getStages().get(0);
        assertEquals(RECEIVED, stage21.getStage());
        assertEquals(receivedStageId, stage21.getId());
        assertNull(stage21.getMessage());
        NotificationStage stage22 = notification2.getStages().get(1);
        assertEquals(PROCESSED, stage22.getStage());
        assertEquals(processedStageId, stage22.getId());
        assertNull(stage22.getMessage());

    }


    @Test
    void sendNotificationWorkflow_WithUnsuccessfulSendOperation_AndGetUnsuccessfulResponse_AndCheckTwoStagesInInnerNotificationObject() throws CreationNotificationException, SendNotificationException {
        Map<NotificationTypeDictionary, SenderService> spiedSendersMap = new HashMap<>();
        for (SenderService service : senderServiceMap.values()) {
            SenderService mockSenderService = Mockito.spy(service);
            doReturn(false).when(mockSenderService).send(any(Notification.class));
            doReturn(FAILED_STAGE_MESSAGE).when(mockSenderService).getSendingResultMessage();
            spiedSendersMap.put(mockSenderService.getType(), mockSenderService);
        }

        facade = new SendNotificationFacadeImpl(notificationService, spiedSendersMap, requestNotificationTransformer);


        NotificationTypeDictionary type = MESSAGE;

        Request request = getRequestByType(type);

        mockAddStageAndSaveMethodInNotificationService();
        Response response = facade.sendNotificationWorkflow(request);

        verify(requestNotificationTransformer).transform(request);

        verify(notificationService, never()).save(any(Notification.class));
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(FAILED), eq(FAILED_STAGE_MESSAGE), any(Notification.class));
        verifyForInvokingOnlyOneSenderServiceInServiceMapByType(spiedSendersMap, type);

        assertNotNull(response);
        assertFalse(response.success);
        assertEquals(request.id, response.remoteId);
        assertNotNull(response.operationId);
        assertEquals(FAILED_STAGE_MESSAGE, response.message);

        Notification notification2 = innerNotificationInProcess.get();
        assertNotNull(notification2);
        assertEquals(modelNotificationId, notification2.getId());
        assertEquals(2, notification2.getStages().size());
        NotificationStage stage21 = notification2.getStages().get(0);
        assertEquals(RECEIVED, stage21.getStage());
        assertEquals(receivedStageId, stage21.getId());
        assertNull(stage21.getMessage());
        NotificationStage stage22 = notification2.getStages().get(1);
        assertEquals(FAILED, stage22.getStage());
        assertEquals(processedStageId, stage22.getId());
        assertEquals(FAILED_STAGE_MESSAGE, stage22.getMessage());
    }

    @Test
    void sendNotificationWorkflow_WithUnsuccessfulSendOperationWithNullOperationMessage_AndGetUnsuccessfulResponse_AndCheckTwoStagesInInnerNotificationObject() throws CreationNotificationException, SendNotificationException {
        Map<NotificationTypeDictionary, SenderService> spiedSendersMap = new HashMap<>();
        for (SenderService service : senderServiceMap.values()) {
            SenderService mockSenderService = Mockito.spy(service);
            doReturn(false).when(mockSenderService).send(any(Notification.class));
            doReturn(null).when(mockSenderService).getSendingResultMessage();
            spiedSendersMap.put(mockSenderService.getType(), mockSenderService);
        }

        facade = new SendNotificationFacadeImpl(notificationService, spiedSendersMap, requestNotificationTransformer);


        NotificationTypeDictionary type = MESSAGE;

        Request request = getRequestByType(type);

        mockAddStageAndSaveMethodInNotificationService();
        Response response = facade.sendNotificationWorkflow(request);

        verify(requestNotificationTransformer).transform(request);

        verify(notificationService, never()).save(any(Notification.class));
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(FAILED), isNull(), any(Notification.class));
        verifyForInvokingOnlyOneSenderServiceInServiceMapByType(spiedSendersMap, type);

        assertNotNull(response);
        assertFalse(response.success);
        assertEquals(request.id, response.remoteId);
        assertNotNull(response.operationId);
        assertNull(response.message);

        Notification notification2 = innerNotificationInProcess.get();
        assertNotNull(notification2);
        assertEquals(modelNotificationId, notification2.getId());
        assertEquals(2, notification2.getStages().size());
        NotificationStage stage21 = notification2.getStages().get(0);
        assertEquals(RECEIVED, stage21.getStage());
        assertEquals(receivedStageId, stage21.getId());
        assertNull(stage21.getMessage());
        NotificationStage stage22 = notification2.getStages().get(1);
        assertEquals(FAILED, stage22.getStage());
        assertEquals(processedStageId, stage22.getId());
        assertNull(stage22.getMessage());
    }


    @Test
    void sendNotificationWorkflow_WithNullRequest_AndGetUnsuccessfulResponse() throws SendNotificationException, CreationNotificationException {
        Map<NotificationTypeDictionary, SenderService> spiedSendersMap = new HashMap<>();
        for (SenderService service : senderServiceMap.values()) {
            SenderService mockSenderService = Mockito.spy(service);
            doReturn(false).when(mockSenderService).send(any(Notification.class));
            doReturn(null).when(mockSenderService).getSendingResultMessage();
            spiedSendersMap.put(mockSenderService.getType(), mockSenderService);
        }

        facade = new SendNotificationFacadeImpl(notificationService, spiedSendersMap, requestNotificationTransformer);
        when(notificationService.save(any(Notification.class))).thenReturn(null);

        Response response = facade.sendNotificationWorkflow(null);

        verify(requestNotificationTransformer).transform(null);
        verify(notificationService, never()).save(any(Notification.class));
        verify(notificationService, never()).addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), isNull(), any(Notification.class));
        for (Map.Entry<NotificationTypeDictionary, SenderService> serviceEntry : spiedSendersMap.entrySet()) {
            verify(spiedSendersMap.get(serviceEntry.getKey()), never()).send(any(Notification.class));
        }

        assertNotNull(response);
        assertFalse(response.success);
        assertNull(response.remoteId);
        assertNull(response.operationId);
        assertTrue(response.message.contains(ApplicationConstants.NULL_REQUEST));
        assertTrue(response.message.contains("CreationNotificationException"));
        assertNull(innerNotificationInProcess.get());
    }

    @Test
    void sendNotificationWorkflow_WithWrongRequest_AndGetUnsuccessfulResponse() throws SendNotificationException, CreationNotificationException {
        Map<NotificationTypeDictionary, SenderService> spiedSendersMap = new HashMap<>();
        for (SenderService service : senderServiceMap.values()) {
            SenderService mockSenderService = Mockito.spy(service);
            doReturn(false).when(mockSenderService).send(any(Notification.class));
            doReturn(null).when(mockSenderService).getSendingResultMessage();
            spiedSendersMap.put(mockSenderService.getType(), mockSenderService);
        }

        facade = new SendNotificationFacadeImpl(notificationService, spiedSendersMap, requestNotificationTransformer);
        String messageException = "message!!!";
        Throwable persistException = new CreationNotificationException(messageException);
        when(notificationService.addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class))).thenThrow(persistException);

        Request request = getRequestByType(FILE);
        Response response = facade.sendNotificationWorkflow(request);

        verify(requestNotificationTransformer).transform(request);
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
        verify(notificationService, never()).save(any(Notification.class));

        for (Map.Entry<NotificationTypeDictionary, SenderService> serviceEntry : spiedSendersMap.entrySet()) {
            verify(spiedSendersMap.get(serviceEntry.getKey()), never()).send(any(Notification.class));
        }

        assertNotNull(response);
        assertFalse(response.success);
        assertEquals(request.id, response.remoteId);
        assertNull(response.operationId);
        assertTrue(response.message.contains(messageException));
        assertTrue(response.message.contains("CreationNotificationException"));
        assertNull(innerNotificationInProcess.get());
    }

    @Test
    void sendNotificationWorkflow_AndThrowExceptionInSendProcess_AndGetUnsuccessfulResponse() throws SendNotificationException, CreationNotificationException {
        Map<NotificationTypeDictionary, SenderService> spiedSendersMap = new HashMap<>();

        String messageException = "message!!!";
        Throwable sendException = new SendNotificationException(messageException);

        for (SenderService service : senderServiceMap.values()) {
            SenderService mockSenderService = Mockito.spy(service);
            doThrow(sendException).when(mockSenderService).send(any(Notification.class));
            doReturn(sendException.getClass().getSimpleName() + ": " + sendException.getMessage()).when(mockSenderService).getSendingResultMessage();
            spiedSendersMap.put(mockSenderService.getType(), mockSenderService);
        }

        facade = new SendNotificationFacadeImpl(notificationService, spiedSendersMap, requestNotificationTransformer);
        mockAddStageAndSaveMethodInNotificationService();

        Request request = getRequestByType(EMAIL);
        Response response = facade.sendNotificationWorkflow(request);

        verify(requestNotificationTransformer).transform(request);
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
        verify(notificationService, never()).save(any(Notification.class));

        verifyForInvokingOnlyOneSenderServiceOnlyOneSendMethodInServiceMapByType(spiedSendersMap, EMAIL);

        assertNotNull(response);
        assertFalse(response.success);
        assertEquals(request.id, response.remoteId);
        assertEquals(modelNotificationId, response.operationId);
        assertTrue(response.message.contains(messageException));
        assertTrue(response.message.contains("SendNotificationException"));

        Notification notification2 = innerNotificationInProcess.get();
        assertNotNull(notification2);
        assertEquals(modelNotificationId, notification2.getId());
        assertEquals(2, notification2.getStages().size());
        NotificationStage stage21 = notification2.getStages().get(0);
        assertEquals(RECEIVED, stage21.getStage());
        assertEquals(receivedStageId, stage21.getId());
        assertNull(stage21.getMessage());
        NotificationStage stage22 = notification2.getStages().get(1);
        assertEquals(FAILED, stage22.getStage());
        assertEquals(processedStageId, stage22.getId());
        assertTrue(stage22.getMessage().contains(messageException));
        assertTrue(stage22.getMessage().contains(sendException.getClass().getSimpleName()));

    }


    @Test
    void sendRequest_AndThrowExceptionInAfterSendingProcess_AndGetUnsuccessfulResponse() throws SendNotificationException, CreationNotificationException {
        Map<NotificationTypeDictionary, SenderService> spiedSendersMap = new HashMap<>();

        String messageException = "message!!!";
        Throwable sendException = new SendNotificationException(messageException);

        String errorMessage = "Error";
        for (SenderService service : senderServiceMap.values()) {
            SenderService mockSenderService = Mockito.spy(service);
            doReturn(true).when(mockSenderService).send(any(Notification.class));
            doThrow(sendException).when(mockSenderService).afterSending(any(Notification.class), any(Boolean.class));
            doReturn(errorMessage).when(mockSenderService).getSendingResultMessage();
            spiedSendersMap.put(mockSenderService.getType(), mockSenderService);
        }

        facade = new SendNotificationFacadeImpl(notificationService, spiedSendersMap, requestNotificationTransformer);
        mockAddStageAndSaveMethodInNotificationService();

        Request request = getRequestByType(EMAIL);
        Response response = facade.sendNotificationWorkflow(request);

        verify(requestNotificationTransformer).transform(request);
        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(RECEIVED), isNull(), any(Notification.class));
//        verify(notificationService, times(1)).addStageWithMessageAndSave(eq(FAILED), any(String.class), any(Notification.class));
        verify(notificationService, never()).save(any(Notification.class));

        verifyForInvokingOnlyOneSenderServiceInServiceMapByType(spiedSendersMap, EMAIL);

        assertNotNull(response);
        assertFalse(response.success);
        assertEquals(request.id, response.remoteId);
        assertEquals(modelNotificationId, response.operationId);
        assertEquals(errorMessage, response.message);

        Notification notification2 = innerNotificationInProcess.get();
        assertNotNull(notification2);
        assertEquals(modelNotificationId, notification2.getId());
        assertEquals(2, notification2.getStages().size());
        NotificationStage stage21 = notification2.getStages().get(0);
        assertEquals(RECEIVED, stage21.getStage());
        assertEquals(receivedStageId, stage21.getId());
        assertNull(stage21.getMessage());
        NotificationStage stage22 = notification2.getStages().get(1);
        assertEquals(FAILED, stage22.getStage());
        assertEquals(processedStageId, stage22.getId());
        assertEquals(errorMessage, stage22.getMessage());

    }

    private void verifyForInvokingOnlyOneSenderServiceInServiceMapByType(Map<NotificationTypeDictionary, SenderService> spiedSendersMap, NotificationTypeDictionary type) throws SendNotificationException {
        for (Map.Entry<NotificationTypeDictionary, SenderService> serviceEntry : spiedSendersMap.entrySet()) {
            if (serviceEntry.getKey() == type) {
                verify(spiedSendersMap.get(serviceEntry.getKey())).send(any(Notification.class));
                verify(spiedSendersMap.get(serviceEntry.getKey())).afterSending(any(Notification.class), any(Boolean.class));
            } else {
                verify(spiedSendersMap.get(serviceEntry.getKey()), never()).send(any(Notification.class));
                verify(spiedSendersMap.get(serviceEntry.getKey()), never()).afterSending(any(Notification.class), any(Boolean.class));
            }
        }
    }


    private void verifyForInvokingOnlyOneSenderServiceOnlyOneSendMethodInServiceMapByType(Map<NotificationTypeDictionary, SenderService> spiedSendersMap, NotificationTypeDictionary type) throws SendNotificationException {
        for (Map.Entry<NotificationTypeDictionary, SenderService> serviceEntry : spiedSendersMap.entrySet()) {
            if (serviceEntry.getKey() == type) {
                verify(spiedSendersMap.get(serviceEntry.getKey())).send(any(Notification.class));
            } else {
                verify(spiedSendersMap.get(serviceEntry.getKey()), never()).send(any(Notification.class));
            }
            verify(spiedSendersMap.get(serviceEntry.getKey()), never()).afterSending(any(Notification.class), any(Boolean.class));
        }
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

    private void mockAddStageAndSaveMethodInNotificationService() throws CreationNotificationException {
        when(notificationService.addStageWithMessageAndSave(any(NotificationProcessStageDictionary.class), any(), any(Notification.class))).then((invocation) -> {

            Notification notification = invocation.getArgument(2);
            notification.setId(modelNotificationId);
            if (Objects.isNull(notification.getCreated())) {
                notification.setCreated(LocalDateTime.now());
            } else {
                notification.setUpdated(LocalDateTime.now());
            }

            NotificationStage stage = new NotificationStage();
            stage.setStage(invocation.getArgument(0));
            notification.addStage(stage);

            if (notification.getStages().size() == 1) {
                notification.getStages().get(0).setCreated(LocalDateTime.now());
                notification.getStages().get(0).setId(receivedStageId);
                notification.getStages().get(0).setMessage(invocation.getArgument(1));
            } else if (notification.getStages().size() == 2) {
                notification.getStages().get(1).setCreated(LocalDateTime.now());
                notification.getStages().get(1).setId(processedStageId);
                notification.getStages().get(1).setMessage(invocation.getArgument(1));
            }
            innerNotificationInProcess.set(notification);
            return notification;
        });
    }
}