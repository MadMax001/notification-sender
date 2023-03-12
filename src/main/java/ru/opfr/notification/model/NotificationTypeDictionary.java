package ru.opfr.notification.model;

import java.util.HashMap;
import java.util.Map;

public enum NotificationTypeDictionary {
    MESSAGE, EMAIL, FILE;

    private static final Map<String, NotificationTypeDictionary> map = new HashMap<>();
    static {
        for (NotificationTypeDictionary type : NotificationTypeDictionary.values())
            map.put(type.toString(), type);
    }

    public static NotificationTypeDictionary of (String typeString) {
        return map.get(typeString);
    }
}
