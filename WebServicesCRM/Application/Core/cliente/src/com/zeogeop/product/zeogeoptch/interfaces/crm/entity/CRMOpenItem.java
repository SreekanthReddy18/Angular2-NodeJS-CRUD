package com.consisint.acsele.interseguro.interfaces.crm.entity;

import java.io.Serializable;

/**
 * Created by ext.dpalma on 24/01/2017.
 */
public class CRMOpenItem implements Serializable {
    private static final long serialVersionUID = -2014573273976931300L;
    PK pk;

    public CRMOpenItem() { }

    public CRMOpenItem(PK pk) { this.pk = pk; }

    public PK getPk() {
        return pk;
    }

    public void setPk(PK pk) {
        this.pk = pk;
    }

    @Override
    public int hashCode() {
        return pk.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return pk.equals(((CRMOpenItem)o).pk);
    }
}
