package com.consisint.acsele.interseguro.interfaces.crm.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ext.dpalma on 24/01/2017.
 */
public class CRMCase implements Serializable {
    private static final long serialVersionUID = 4498683215363441283L;

    private String crmNumber;
    private Long policyId;
    private String policyNumber;
    private String mediumAnswer;
    private Integer status;
    private Integer associationType;
    private Long claimId;
    private Long ccvId;
    private Long hltId;
    private Set<CRMOpenItem> crmOpenItems = new HashSet<CRMOpenItem>();
    transient private String claimNumber;
    transient private String ccvName;
    transient private String letterName;

    public CRMCase() {
    }

    public CRMCase(String crmNumber, Long policyId, String policyNumber, String mediumAnswer, Integer status) {
        this.crmNumber = crmNumber;
        this.policyId = policyId;
        this.policyNumber = policyNumber;
        this.mediumAnswer = mediumAnswer;
        this.status = status;
    }

    public String getCrmNumber() {
        return crmNumber;
    }

    public void setCrmNumber(String crmNumber) {
        this.crmNumber = crmNumber;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getMediumAnswer() {
        return mediumAnswer;
    }

    public void setMediumAnswer(String mediumAnswer) {
        this.mediumAnswer = mediumAnswer;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getAssociationType() {
        return associationType;
    }

    public void setAssociationType(Integer associationType) {
        this.associationType = associationType;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public Long getCcvId() {
        return ccvId;
    }

    public void setCcvId(Long ccvId) {
        this.ccvId = ccvId;
    }

    public Long getHltId() {
        return hltId;
    }

    public void setHltId(Long hltId) {
        this.hltId = hltId;
    }

    public Set<CRMOpenItem> getCrmOpenItems() {
        return crmOpenItems;
    }

    public void setCrmOpenItems(Set<CRMOpenItem> crmOpenItems) {
        this.crmOpenItems = crmOpenItems;
    }

    public String getCcvName() {
        return ccvName;
    }

    public void setCcvName(String ccvName) {
        this.ccvName = ccvName;
    }

    public String getLetterName() {
        return letterName;
    }

    public void setLetterName(String letterName) {
        this.letterName = letterName;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CRMCase crmCase = (CRMCase) o;
        if (!crmNumber.equals(crmCase.crmNumber)) return false;
        if (!policyId.equals(crmCase.policyId)) return false;        
        return true/*status.equals(crmCase.status)*/;

    }

    @Override
    public int hashCode() {
        int result = crmNumber.hashCode();
        result = 31 * result + policyId.hashCode();
        return result;
    }
}
