package ru.opfr.notification.constraint.impl;

import ru.opfr.notification.constraint.NotNullByType;
import ru.opfr.notification.exception.ApplicationRuntimeException;
import ru.opfr.notification.model.Notification;
import ru.opfr.notification.model.NotificationTypeDictionary;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class NotEmptyByTypeValidator implements ConstraintValidator<NotNullByType, Notification> {
    private String fieldName;
    private String typeFieldName;
    private NotificationTypeDictionary type;
    private NotificationTypeDictionary[] types;

    @Override
    public void initialize(NotNullByType constraintAnnotation) {
        fieldName = constraintAnnotation.field();
        typeFieldName = constraintAnnotation.typeField();
        type = constraintAnnotation.type();
        types = constraintAnnotation.types();
    }

    @Override
    public boolean isValid(Notification notification, ConstraintValidatorContext constraintValidatorContext) {
        try {
            Object fieldValue = getValueByFieldNameInObjectByType(fieldName, notification);
            NotificationTypeDictionary notificationTypeDictionary = (NotificationTypeDictionary)getValueByFieldNameInObjectByType(typeFieldName, notification);
            return checkCondition(fieldValue, notificationTypeDictionary);
        } catch (NoSuchFieldException|IllegalAccessException e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    private boolean checkCondition(Object fieldValue, NotificationTypeDictionary notificationTypeDictionary) {
        return !checkForType(notificationTypeDictionary) || checkForType(notificationTypeDictionary) && Objects.nonNull(fieldValue);
    }

    private boolean checkForType(NotificationTypeDictionary notificationTypeDictionary) {
        if (types.length > 0) {
            return Arrays.asList(types).contains(notificationTypeDictionary);
        } else {
            return notificationTypeDictionary == type;
        }
    }

    private Object getValueByFieldNameInObjectByType(String fieldName, Object source) throws NoSuchFieldException, IllegalAccessException {
        if(fieldName.indexOf('.') > -1) {
            return getValueByFieldInNestedObjectByType(fieldName, source);
        }
        Field filed = source.getClass().getDeclaredField(fieldName);
        filed.setAccessible(true);
        return filed.get(source);
    }

    private Object getValueByFieldInNestedObjectByType(String fieldName, Object source) throws NoSuchFieldException, IllegalAccessException {
        String nestedObjectFieldName = fieldName.substring(0, fieldName.indexOf('.'));
        Object nestedObject = getNestedObjectByFieldName(nestedObjectFieldName, source);
        return getValueByFieldNameInObjectByType(fieldName.substring(fieldName.indexOf('.') + 1), nestedObject);
    }

    private Object getNestedObjectByFieldName(String nestedObjectFieldName, Object source) throws NoSuchFieldException, IllegalAccessException {
        Field filed = source.getClass().getDeclaredField(nestedObjectFieldName);
        filed.setAccessible(true);
        return filed.get(source);
    }

}
