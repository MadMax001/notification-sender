package ru.opfr.notification.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.opfr.notification.exception.CreationNotificationException;
import ru.opfr.notification.model.dto.Request;
import ru.opfr.notification.model.dto.Response;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.*;


@ExtendWith(MockitoExtension.class)
class SenderServiceFacadeSafeWrapperImplTest {
    @Mock
    private SenderServiceFacade senderServiceFacade;
    private SenderServiceFacadeSafeWrapper senderServiceFacadeSafeWrapper;

    @BeforeEach
    void tearDown() {
        senderServiceFacadeSafeWrapper = new SenderServiceFacadeSafeWrapperImpl(senderServiceFacade);
    }

    @Test
    void successfulSend_AndCheckResponse() {
        Request request = new Request();
        request.type = MESSAGE.toString();
        request.id = "100";
        request.user = "073User";
        request.ip = "10.73.13.14";
        request.email = "user@server.ru";
        request.content = "Content";
        request.theme = "Theme";

        Response expectedResponse = Response.builder().build();
        when(senderServiceFacadeSafeWrapper.safeSend(request)).thenReturn(expectedResponse);

        Response response = senderServiceFacadeSafeWrapper.safeSend(request);
        assertNotNull(response);
        assertEquals(expectedResponse, response);
    }

    @Test
    void throwCreationNotificationException_AndCheckFailedResponse() throws CreationNotificationException {
        Request request = new Request();
        request.type = MESSAGE.toString();
        request.id = "100";
        request.user = "073User";
        request.ip = "10.73.13.14";
        request.email = "user@server.ru";
        request.content = "Content";
        request.theme = "Theme";

        Throwable error = new CreationNotificationException("Error in creation process");
        doThrow(error).when(senderServiceFacade).send(request);
        Response response = senderServiceFacadeSafeWrapper.safeSend(request);
        assertNotNull(response);
        assertEquals("100", response.remoteId);
        assertNull(response.operationId);
        assertFalse(response.success);
        assertTrue(response.message.contains("Error in creation process"));
        assertTrue(response.message.contains(error.getClass().getSimpleName()));
    }

    @Test
    void throwCreationNotificationException_WithNullRequest_AndCheckFailedResponse() throws CreationNotificationException {

        Throwable error = new CreationNotificationException("Error in creation process");
        doThrow(error).when(senderServiceFacade).send(any());
        Response response = senderServiceFacadeSafeWrapper.safeSend(null);
        assertNotNull(response);
        assertNull(response.remoteId);
        assertNull(response.operationId);
        assertFalse(response.success);
        assertTrue(response.message.contains("Error in creation process"));
        assertTrue(response.message.contains(error.getClass().getSimpleName()));
    }
}