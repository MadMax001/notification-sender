package ru.opfr.notification.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.opfr.notification.constraint.impl.NotEmptyByTypeValidator;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;
import ru.opfr.notification.model.Person;

import javax.validation.ConstraintValidator;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static ru.opfr.notification.model.NotificationTypeDictionary.MESSAGE;

class NotEmptyByTypeValidatorTest {
    private ConstraintValidator<NotNullByType, Notification> validator;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        validator = new NotEmptyByTypeValidator();
        Field typeFieldName = validator.getClass().getDeclaredField("typeFieldName");
        typeFieldName.setAccessible(true);
        typeFieldName.set(validator, "type");
    }

    @Test
    void checkForSuccessValidationForMessageTypeAndNotNullUser() throws NoSuchFieldException, IllegalAccessException {
        setParamsForMessageTypeInValidator();
        Field fieldName = validator.getClass().getDeclaredField("fieldName");
        fieldName.setAccessible(true);
        fieldName.set(validator, "person.user");
        setParamsForMessageTypeInValidator();
        Notification notification = createNotificationWithAllNonNullFiledForValidator();
        assertTrue(validator.isValid(notification, null));
    }


    @Test
    void checkForFailValidationForMessageTypeAndNullUser() throws NoSuchFieldException, IllegalAccessException {
        setParamsForMessageTypeInValidator();
        Field fieldName = validator.getClass().getDeclaredField("fieldName");
        fieldName.setAccessible(true);
        fieldName.set(validator, "person.user");
        setParamsForMessageTypeInValidator();
        Notification notification = createNotificationWithAllNonNullFiledForValidator();
        notification.getPerson().setUser(null);
        assertFalse(validator.isValid(notification, null));
    }

    @Test
    void checkForSuccessValidationForMessageTypeInArrayAndNotNullUser() throws NoSuchFieldException, IllegalAccessException {
        setParamsForMessageTypeInTypesArrayInValidator();
        Field fieldName = validator.getClass().getDeclaredField("fieldName");
        fieldName.setAccessible(true);
        fieldName.set(validator, "person.user");
        Notification notification = createNotificationWithAllNonNullFiledForValidator();
        assertTrue(validator.isValid(notification, null));
    }


    @Test
    void checkForFailValidationForMessageTypeInArrayAndNullUser() throws NoSuchFieldException, IllegalAccessException {
        setParamsForMessageTypeInTypesArrayInValidator();
        Field fieldName = validator.getClass().getDeclaredField("fieldName");
        fieldName.setAccessible(true);
        fieldName.set(validator, "person.user");
        Notification notification = createNotificationWithAllNonNullFiledForValidator();
        notification.getPerson().setUser(null);
        assertFalse(validator.isValid(notification, null));
    }

    @Test
    void validationForWrongFieldName_andThrowException() throws NoSuchFieldException, IllegalAccessException{
        setParamsForMessageTypeInValidator();
        Field fieldName = validator.getClass().getDeclaredField("fieldName");
        fieldName.setAccessible(true);
        fieldName.set(validator, "aaaaa");
        Notification notification = createNotificationWithAllNonNullFiledForValidator();
        assertThrows(ApplicationRuntimeException.class, () -> validator.isValid(notification, null));
    }

    @Test
    void validationForWrongNestedFieldName_andThrowException() throws NoSuchFieldException, IllegalAccessException{
        setParamsForMessageTypeInValidator();
        Field fieldName = validator.getClass().getDeclaredField("fieldName");
        fieldName.setAccessible(true);
        fieldName.set(validator, "person.aaaaa");
        Notification notification = createNotificationWithAllNonNullFiledForValidator();
        setParamsForMessageTypeInTypesArrayInValidator();
        assertThrows(ApplicationRuntimeException.class, () -> validator.isValid(notification, null));
    }

    @Test
    void validationForWrongTypeFieldName_andThrowException() throws NoSuchFieldException, IllegalAccessException{
        setParamsForMessageTypeInValidator();
        Field typeFieldName = validator.getClass().getDeclaredField("typeFieldName");
        typeFieldName.setAccessible(true);
        typeFieldName.set(validator, "aaaaa");
        Field fieldName = validator.getClass().getDeclaredField("fieldName");
        fieldName.setAccessible(true);
        fieldName.set(validator, "person.user");
        Notification notification = createNotificationWithAllNonNullFiledForValidator();
        setParamsForMessageTypeInTypesArrayInValidator();
        assertThrows(ApplicationRuntimeException.class, () -> validator.isValid(notification, null));
    }

    private Notification createNotificationWithAllNonNullFiledForValidator() {
        Notification notification = new Notification();
        notification.setType(MESSAGE);
        Person person = new Person();
        person.setIp("10.12.14.16");
        person.setUser("073User");
        person.setEmail("user@server.com");
        notification.setPerson(person);
        return notification;
    }

    private void setParamsForMessageTypeInValidator() throws NoSuchFieldException, IllegalAccessException {
        Field type = validator.getClass().getDeclaredField("type");
        type.setAccessible(true);
        type.set(validator, MESSAGE);
        Field types = validator.getClass().getDeclaredField("types");
        types.setAccessible(true);
        types.set(validator, new NotificationTypeDictionary[0]);
    }

    private void setParamsForMessageTypeInTypesArrayInValidator() throws NoSuchFieldException, IllegalAccessException {
        Field types = validator.getClass().getDeclaredField("types");
        types.setAccessible(true);
        types.set(validator, new NotificationTypeDictionary[]{MESSAGE});
    }

}