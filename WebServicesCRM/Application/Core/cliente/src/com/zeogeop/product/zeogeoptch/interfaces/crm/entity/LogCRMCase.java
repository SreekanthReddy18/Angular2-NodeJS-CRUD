package com.consisint.acsele.interseguro.interfaces.crm.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by ext.dpalma on 24/01/2017.
 */
public class LogCRMCase implements Serializable {
    private static final long serialVersionUID = -6227649286347071435L;
    private Long elCRMId;
    private String crmNumber;
    private Date movementDate;
    private String movementStatus;
    private String jsonIn;
    private String jsonOut;

    public LogCRMCase() {
    }

    public LogCRMCase(String crmNumber, Date movementDate, String movementStatus, String jsonIn, String jsonOut) {
        this.crmNumber = crmNumber;
        this.movementDate = movementDate;
        this.movementStatus = movementStatus;
        this.jsonIn = jsonIn;
        this.jsonOut = jsonOut;
    }

    public Long getElCRMId() {
        return elCRMId;
    }

    public void setElCRMId(Long elCRMId) {
        this.elCRMId = elCRMId;
    }

    public String getCrmNumber() {
        return crmNumber;
    }

    public void setCrmNumber(String crmNumber) {
        this.crmNumber = crmNumber;
    }

    public Date getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
    }

    public String getMovementStatus() {
        return movementStatus;
    }

    public void setMovementStatus(String movementStatus) {
        this.movementStatus = movementStatus;
    }

    public String getJsonIn() {
        return jsonIn;
    }

    public void setJsonIn(String jsonIn) {
        this.jsonIn = jsonIn;
    }

    public String getJsonOut() {
        return jsonOut;
    }

    public void setJsonOut(String jsonOut) {
        this.jsonOut = jsonOut;
    }
}
