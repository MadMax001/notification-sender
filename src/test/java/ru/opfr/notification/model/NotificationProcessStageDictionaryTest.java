package ru.opfr.notification.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationProcessStageDictionaryTest {
    @Test
    void testNotificationTypeElements() {
        assertEquals(3, NotificationProcessStageDictionary.values().length);
    }

    @Test
    void testOrdinal() {
        assertEquals(0, NotificationProcessStageDictionary.RECEIVED.ordinal());
        assertEquals(1, NotificationProcessStageDictionary.PROCESSED.ordinal());
        assertEquals(2, NotificationProcessStageDictionary.FAILED.ordinal());
    }
}