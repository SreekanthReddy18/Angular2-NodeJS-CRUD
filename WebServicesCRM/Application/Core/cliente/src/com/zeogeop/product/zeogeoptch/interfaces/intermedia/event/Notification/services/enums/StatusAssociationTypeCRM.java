package com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.enums;

import com.consisint.acsele.UserInfo;

import java.util.ResourceBundle;

/**
 * Created by ext.dpalma on 27/01/2017.
 */
public enum StatusAssociationTypeCRM {
    // Applied Operation: After applying an AssociationTypeCRM
    AP(1, "crm.AssociationType.aproved"),
    RS(2, "crm.AssociationType.rejectClaim"),
    RC(3, "crm.AssociationType.rejectCoverage"),
    RR(4, "crm.AssociationType.rejectRequire"); // agregar a bundled

    private int value;
    private String bundled;

    StatusAssociationTypeCRM(int value, String bundled) {
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
        return ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
    }

    public static StatusAssociationTypeCRM getIntanceByValue(int value) {
        for (StatusAssociationTypeCRM o : StatusAssociationTypeCRM.values()) if (o.value == value) return o;
        return null;
    }
}
