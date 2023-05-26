package ru.opfr.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@SpringBootTest
class WinCredentialsManagerStoreCheckerTest {
    @SpyBean
    private WinCredentialsManagerStoreChecker checker;

    @Test
    void checkForAppListenerRefreshedEventRun() {
        verify(checker, times(1)).checkByWinCommandOnRefreshEvent(any(ContextRefreshedEvent.class));
    }
}