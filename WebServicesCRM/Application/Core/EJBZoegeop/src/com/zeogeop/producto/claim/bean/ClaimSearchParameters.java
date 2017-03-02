package com.consisint.acsele.claim.bean;

import com.consisint.acsele.claim.category.ClaimCollectionOrderByType;
import com.consisint.acsele.claim.category.EnumTypeClaimSearch;

import java.util.Date;
import java.util.Map;

/**
 * Title: ClaimSearchParameters <br>
 * Copyright: (c) 2015 Consis International<br>
 * Company: Consis International<br>
 *
 * @author Consis International (CON)
 * @version Acsel-e v13.7
 *
 * Builder of Query for Claim Advanced Search
 */

public class ClaimSearchParameters {

    private EnumTypeClaimSearch enumTypeClaimSearch;
    private ClaimCollectionOrderByType orderBy;

    /* For simple search */
    private String claimNumber;
    private String policyNumber;
    private Date occurrenceDate;

    /* For advanced search */
    private String productName;
    private String crmNumber;

    private Map<String, String> mapPolicyValues;
    private Map<String, String> mapThirdPartyValues;
    private Map<String, String> mapClaimEventValues;
    private Map<String, String> mapRoleValues;
    private Map<String, String> mapRUValues;
    private Map<String, String> mapIOValues;

    private String templateNameThirdParty;
    private String templateNameClaimEvent;
    private String templateNameRole;
    private String templateNameRU;
    private String templateNameIO;

    public ClaimSearchParameters(EnumTypeClaimSearch enumTypeClaimSearch) {
        this.enumTypeClaimSearch = enumTypeClaimSearch;
    }

    public EnumTypeClaimSearch getEnumTypeClaimSearch() {
        return enumTypeClaimSearch;
    }

    public void setEnumTypeClaimSearch(EnumTypeClaimSearch enumTypeClaimSearch) {
        this.enumTypeClaimSearch = enumTypeClaimSearch;
    }

    public ClaimCollectionOrderByType getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(ClaimCollectionOrderByType orderBy) {
        this.orderBy = orderBy;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public Date getOccurrenceDate() {
        return occurrenceDate;
    }

    public void setOccurrenceDate(Date occurrenceDate) {
        this.occurrenceDate = occurrenceDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Map<String, String> getMapPolicyValues() {
        return mapPolicyValues;
    }

    public void setMapPolicyValues(Map<String, String> mapPolicyValues) {
        this.mapPolicyValues = mapPolicyValues;
    }

    public Map<String, String> getMapThirdPartyValues() {
        return mapThirdPartyValues;
    }

    public void setMapThirdPartyValues(Map<String, String> mapThirdPartyValues) {
        this.mapThirdPartyValues = mapThirdPartyValues;
    }

    public Map<String, String> getMapClaimEventValues() {
        return mapClaimEventValues;
    }

    public void setMapClaimEventValues(Map<String, String> mapClaimEventValues) {
        this.mapClaimEventValues = mapClaimEventValues;
    }

    public Map<String, String> getMapRoleValues() {
        return mapRoleValues;
    }

    public void setMapRoleValues(Map<String, String> mapRoleValues) {
        this.mapRoleValues = mapRoleValues;
    }

    public Map<String, String> getMapRUValues() {
        return mapRUValues;
    }

    public void setMapRUValues(Map<String, String> mapRUValues) {
        this.mapRUValues = mapRUValues;
    }

    public Map<String, String> getMapIOValues() {
        return mapIOValues;
    }

    public void setMapIOValues(Map<String, String> mapIOValues) {
        this.mapIOValues = mapIOValues;
    }

    public String getTemplateNameThirdParty() {
        return templateNameThirdParty;
    }

    public void setTemplateNameThirdParty(String templateNameThirdParty) {
        this.templateNameThirdParty = templateNameThirdParty;
    }

    public String getTemplateNameClaimEvent() {
        return templateNameClaimEvent;
    }

    public void setTemplateNameClaimEvent(String templateNameClaimEvent) {
        this.templateNameClaimEvent = templateNameClaimEvent;
    }

    public String getTemplateNameRole() {
        return templateNameRole;
    }

    public void setTemplateNameRole(String templateNameRole) {
        this.templateNameRole = templateNameRole;
    }

    public String getTemplateNameRU() {
        return templateNameRU;
    }

    public void setTemplateNameRU(String templateNameRU) {
        this.templateNameRU = templateNameRU;
    }

    public String getTemplateNameIO() {
        return templateNameIO;
    }

    public void setTemplateNameIO(String templateNameIO) {
        this.templateNameIO = templateNameIO;
    }

    public String getCrmNumber() {
        return crmNumber;
    }

    public void setCrmNumber(String crmNumber) {
        this.crmNumber = crmNumber;
    }
}