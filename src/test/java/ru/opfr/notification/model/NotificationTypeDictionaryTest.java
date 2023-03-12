package ru.opfr.notification.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.*;

class NotificationTypeDictionaryTest {
    @Test
    void testNotificationTypeElements() {
        assertEquals(3, NotificationTypeDictionary.values().length);
    }

    @Test
    void testOrdinal() {
        assertEquals(0, MESSAGE.ordinal());
        assertEquals(1, EMAIL.ordinal());
        assertEquals(2, FILE.ordinal());
    }

    @Test
    void tryToParseWrongTypeString_AndGetNull() {
        NotificationTypeDictionary type = NotificationTypeDictionary.of("aaa");
        assertNull(type);
    }

    @Test
    void tryToParseTypeToLowerCase_AndGetNull() {
        NotificationTypeDictionary type = NotificationTypeDictionary.of(MESSAGE.toString().toLowerCase());
        assertNull(type);
    }

}