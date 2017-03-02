package com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.enums;

import com.consisint.acsele.UserInfo;

import java.util.ResourceBundle;

/**
 * Created by ext.dpalma on 27/01/2017.
 */
public enum StatusCRM {
    // Applied Operation: After applying an crm
    RECD_CREATE(2, "crm.created"),
    RECD_UPDATE(3, "crm.updated"),
    SENT_OK(4, "crm.sent"),
    SENT_ERROR(6, "crm.senterror"), // agregar a bundled
    RECD_ERROR(7, "crm.error");

    private int value;
    private String bundled;

    StatusCRM(int value, String bundled) {
        this.value = value;
        this.bundled = bundled;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return getResourceBundle().getString(bundled);
    }

    private static ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle("PolicyToolMessagesBundle", UserInfo.getLocale());
    }

    public static StatusCRM getIntanceByValue(int value) {
        for (StatusCRM o : StatusCRM.values()) if (o.value == value) return o;
        return null;
    }
}
