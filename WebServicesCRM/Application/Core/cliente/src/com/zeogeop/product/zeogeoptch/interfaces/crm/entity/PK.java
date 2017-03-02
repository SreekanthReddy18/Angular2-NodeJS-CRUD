package com.consisint.acsele.interseguro.interfaces.crm.entity;

import java.io.Serializable;

/**
 * Created by Julio on 03/02/2017.
 */
public class PK implements Serializable {
    private static final long serialVersionUID = -1848694082747088883L;
    private CRMCase crmCase;
    private Long opmId;

    public PK() {
    }

    public PK(CRMCase crmCase, Long opmId) {
        this.crmCase = crmCase;
        this.opmId = opmId;
    }

    public CRMCase getCrmCase() {
        return crmCase;
    }

    public void setCrmCase(CRMCase crmCase) {
        this.crmCase = crmCase;
    }

    public Long getOpmId() {
        return opmId;
    }

    public void setOpmId(Long opmId) {
        this.opmId = opmId;
    }

    @Override
    public int hashCode() {
        int result = 13;
        long longField = opmId.longValue();
        result = 31 * result + crmCase.hashCode();
        result = 31 * result + (int) (longField ^ (longField >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PK pk = (PK) o;
        if (!crmCase.equals(pk.crmCase)) return false;
        return opmId.equals(pk.opmId);
    }
}
