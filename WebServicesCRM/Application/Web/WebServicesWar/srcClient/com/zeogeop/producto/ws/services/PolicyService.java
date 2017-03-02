package com.interseguro.acsele.ws.services;

import com.consisint.acsele.agreements.server.NewAgreement;
import com.consisint.acsele.interseguro.interfaces.enums.ws.Constants;
import com.consisint.acsele.openapi.policy.PolicyOperation;
import com.consisint.acsele.openapi.policy.agreement.Agreement;
import com.consisint.acsele.openapi.policy.search.CriteriaToOrderBy;
import com.consisint.acsele.openapi.product.Product;
import com.consisint.acsele.persistent.managers.NewAgreementManager;
import com.consisint.acsele.policy.api.*;
import com.consisint.acsele.thirdparty.api.ThirdParty;
import com.consisint.acsele.uaa.api.Role;
import com.consisint.acsele.uaa.api.RoleList;
import com.consisint.acsele.util.HibernateUtil;
import com.consisint.acsele.util.StringUtil;
import com.consisint.acsele.util.error.ApplicationException;
import com.consisint.acsele.util.error.TechnicalException;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.interseguro.acsele.ws.bean.policy.*;
import com.interseguro.acsele.ws.errors.ServiceException;
import com.interseguro.acsele.ws.errors.ServiceExceptionsType;
import com.interseguro.acsele.ws.utils.GenericJsonBuilder;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import java.sql.SQLException;
import java.util.*;

/**
 * PolicyService.java
 * Copyright: (c) 2013 Consis International<br>
 * Company: Consis International<br>
 *
 * @author Gilberto J Requena (GJR)
 *         <br>
 *         Changes:<br>
 *         <ul>
 *         <li> 2013-07-24 (GJR) Class creation. </li>
 *         <li> 2013-08-20 (JCM) ACSELE-5299. </li>
 *         <li> 2013-09-19 (GJR) Added the loadAgreement method. </li>
 *         </ul>
 */
public class PolicyService extends Service {
    private static AcseleLogger log = AcseleLogger.getLogger(PolicyService.class);
    private static List<String> roleList;

    public static String loadPolicy(PolicyData policyData) throws Exception {
        GenericJsonBuilder jsonBuilder = new GenericJsonBuilder();
        PolicyData loadedPolicyData = new PolicyData();
        Policy policy = null;

        try {
            if (policyData.getPolicyId() != null) {
                policy = Policy.Impl.loadById(policyData.getPolicyId());
            } else if (!StringUtil.isEmptyOrNullValue(policyData.getPolicyNumber())) {
                policy = Policy.Impl.loadByPolicyNumber(policyData.getPolicyNumber());
            } else if (policyData.getOperationId() != null) {
                policy = Policy.Impl.loadByOperationId(policyData.getOperationId());
            }
        } catch (TechnicalException e) {
            return jsonBuilder.buildJson(Constants.POLICY.getValue());
        } catch (NoSuchElementException e) {
            return jsonBuilder.buildJson(Constants.POLICY.getValue());
        }
        if (policy != null) {
            loadedPolicyData = new PolicyData();
            loadedPolicyData.loadPolicyDataFromPolicy(policy);
        }

        jsonBuilder.setData(loadedPolicyData);
        return jsonBuilder.buildJson(Constants.POLICY_JSON.getValue());
    }

    public static String listPolicies(PolicyData policyData) throws Exception {
        GenericJsonBuilder jsonBuilder = new GenericJsonBuilder();

        PolicyList policies = null;
        Policy policy = null;
        Product product = null;

        List<ThirdPartyData> thirdPartyDataList = null;
        Set<Policy> policySet = null;

        String productName = "";
        String policyNumber = "";
        Session session;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = HibernateUtil.beginTransaction(session);

            if (policyData.getPolicyId() == null && StringUtil.isEmptyOrNullValue(policyData.getPolicyNumber()) && policyData.getProductId() == null && StringUtil.isEmptyOrNullValue(policyData.getProductName()) && policyData.getThirdPartyDataList() == null) {
                throw new ServiceException(ServiceExceptionsType.INVALID_PARAMS_FOR_POLICY_SEARCH);
            }

            if (policyData.getPolicyId() != null) {
                policy = Policy.Impl.loadById(policyData.getPolicyId());
            }
            if (policyData.getProductId() != null) {
                product = Product.Impl.getProduct(policyData.getProductId());
            }

            if (StringUtil.isEmptyOrNullValue(policyData.getProductName()) && product != null) {
                productName = product.getName();
            } else if (!StringUtil.isEmptyOrNullValue(policyData.getProductName())) {
                productName = policyData.getProductName();
            } else if (policy != null) {
                productName = policy.getProduct().getName();
            }

            if (StringUtil.isEmptyOrNullValue(policyData.getPolicyNumber()) && policy != null) {
                policyNumber = policy.getPolicyNumber();
            } else if (!StringUtil.isEmptyOrNullValue(policyData.getPolicyNumber())) {
                policyNumber = policyData.getPolicyNumber();
            }

            if (policyData.getThirdPartyDataList() != null && StringUtil.isEmptyOrNullValue(productName) && StringUtil.isEmptyOrNullValue(policyNumber)) {
                thirdPartyDataList = policyData.getThirdPartyDataList();
                policySet = new HashSet<Policy>();

                for (ThirdPartyData thirdPartyData : thirdPartyDataList) {
                    ThirdParty thirdParty = ThirdParty.Impl.load(thirdPartyData.getThirdPartyId());
                    if (thirdParty != null) {
                        PolicyList thirdPartyPolicyList = PolicyList.Impl.getByThirdPartyParticipations(thirdParty, policyData.getInitialDate(), policyData.getFinalDate(), CriteriaToOrderBy.NONE);
                        for (Policy policy1 : thirdPartyPolicyList) {
                            policySet.add(policy1);
                        }
                    }
                }
            } else {
                policies = PolicyList.Impl.getAvancedSearch(productName, policyNumber, policyData.getInitialDate(), policyData.getFinalDate(), policy.getDynamicData().getInputs(), "", null, "", null, null, CriteriaToOrderBy.NONE);
            }

            if (policySet != null && policySet.size() > 0) {
                List<Policy> policyList = new ArrayList(policySet);
                List<Policy> policyListSlice = policyList.subList(0, policyList.size());
                for (Policy pol : policyListSlice) {
                    jsonBuilder.addDataToDataList(new PolicyData().loadPolicyDataFromPolicySearch(pol));
                }
            } else if (policies != null) {
                List<Policy> policyDataListSlice = policies.slice(0, policies.size());
                for (Policy pol : policyDataListSlice) {
                    jsonBuilder.addDataToDataList(new PolicyData().loadPolicyDataFromPolicySearch(pol));
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if(e instanceof ServiceException) jsonBuilder.addError((ServiceException) e);
            else jsonBuilder.addError(new ServiceException(ServiceExceptionsType.UNEXPECTED_ERROR, e));
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return jsonBuilder.buildJson(Constants.POLICY_LIST_JSON.getValue());
    }

    public static List<String> getRoleList() {
        if (roleList == null) {
            RoleList roles = RoleList.Impl.loadAll();
            roleList = new ArrayList<String>();
            for (Role role : roles) {
                roleList.add(role.getDescription());
            }
        }
        return roleList;
    }

    public static String loadRiskUnit(RiskUnitData riskUnitData) throws ServiceException {

        GenericJsonBuilder jsonBuilder = new GenericJsonBuilder();

        if ((riskUnitData.getPolicyId() == null && riskUnitData.getOperationId() == null) || riskUnitData.getRiskUnitId() == null) {
            throw new ServiceException(ServiceExceptionsType.INVALID_PARAMS_FOR_RISK_UNIT_LOAD);
        }

        Policy policy;
        RiskUnit riskUnit;
        RiskUnitData loadedRiskUnitData = new RiskUnitData();
        try {
            if (riskUnitData.getPolicyId() != null) {
                policy = Policy.Impl.loadById(riskUnitData.getPolicyId());
            } else {
                policy = Policy.Impl.loadByOperationId(riskUnitData.getOperationId());
            }
        } catch (TechnicalException e) {
            return jsonBuilder.buildJson(Constants.RISK_UNIT.getValue());
        } catch (NoSuchElementException e) {
            return jsonBuilder.buildJson(Constants.RISK_UNIT.getValue());
        }

        if (policy != null) {
            try {
                riskUnit = policy.getRiskUnitById(riskUnitData.getRiskUnitId());
                loadedRiskUnitData.loadFromRU(riskUnit);
            } catch (ApplicationException e) {
                throw new ServiceException(ServiceExceptionsType.RISK_UNIT_NOT_ASOCIATED_TO_POLICY);
            }
        }

        jsonBuilder.setData(loadedRiskUnitData);
        return jsonBuilder.buildJson(Constants.RISK_UNIT_JSON.getValue());
    }

    public static String loadInsuredObject(InsuredObjectData insuredObjectData) throws ServiceException {
        GenericJsonBuilder jsonBuilder = new GenericJsonBuilder();

        Policy policy;
        RiskUnit riskUnit;
        InsuranceObject insuranceObject;
        InsuredObjectData loadInsuredObjectData = new InsuredObjectData();

        if ((insuredObjectData.getPolicyId() == null && insuredObjectData.getOperationId() == null) || insuredObjectData.getRiskUnitId() == null || insuredObjectData.getInsuredObjectId() == null) {
            throw new ServiceException(ServiceExceptionsType.INVALID_PARAMS_FOR_INSURED_OBJECT_LOAD);
        }

        try {
            if (insuredObjectData.getPolicyId() != null) {
                policy = Policy.Impl.loadById(insuredObjectData.getPolicyId());
            } else {
                policy = Policy.Impl.loadByOperationId(insuredObjectData.getOperationId());
            }
            riskUnit = policy.getRiskUnitById(insuredObjectData.getRiskUnitId());
            insuranceObject = riskUnit.getInsuranceObjectById(insuredObjectData.getInsuredObjectId());
            loadInsuredObjectData.loadInsuredObjectDataFromIO(insuranceObject);
        } catch (Exception e) {
            throw new ServiceException(ServiceExceptionsType.ERROR_DURING_LOADING_INSURED_OBJECT);
        }
        jsonBuilder.setData(loadInsuredObjectData);
        return jsonBuilder.buildJson(Constants.INSURED_OBJECT_JSON.getValue());
    }

    public static String loadCoverage(CoverageData coverageData) throws ServiceException {
        GenericJsonBuilder jsonBuilder = new GenericJsonBuilder();

        Policy policy;
        RiskUnit riskUnit;
        InsuranceObject insuranceObject;
        Coverage coverage;
        CoverageData loadedCoverageData = new CoverageData();

        if ((coverageData.getPolicyId() == null && coverageData.getOperationId() == null) || coverageData.getRiskUnitId() == null || coverageData.getInsuredObjectId() == null || coverageData.getCoverageId() == null) {
            throw new ServiceException(ServiceExceptionsType.INVALID_PARAMS_FOR_COVERAGE_LOAD);
        }
        try {
            if (coverageData.getPolicyId() != null) {
                policy = Policy.Impl.loadById(coverageData.getPolicyId());
            } else {
                policy = Policy.Impl.loadByOperationId(coverageData.getOperationId());
            }
            riskUnit = policy.getRiskUnitById(coverageData.getRiskUnitId());
            insuranceObject = riskUnit.getInsuranceObjectById(coverageData.getInsuredObjectId());
            coverage = insuranceObject.getCoverageById(coverageData.getCoverageId());
            loadedCoverageData.loadFromCoverage(coverage);
        } catch (Exception e) {
            throw new ServiceException(ServiceExceptionsType.ERROR_DURING_LOADING_INSURED_OBJECT);
        }
        jsonBuilder.setData(loadedCoverageData);
        return jsonBuilder.buildJson(Constants.COVERAGE_JSON.getValue());
    }

    public static String loadParticipation(ParticipationData participationData) throws ServiceException {
        GenericJsonBuilder jsonBuilder = new GenericJsonBuilder();

        Policy policy;
        RiskUnit riskUnit;
        InsuranceObject insuranceObject;
        Coverage coverage;
        ParticipationData loadedParticipationData = new ParticipationData();

        try {

            if (participationData.getObjectType().equals(Constants.COVERAGE_PARTICIPATION_LOAD)) {
                if (((participationData.getPolicyId() == null && participationData.getOperationId() == null) || participationData.getRiskUnitId() == null || participationData.getInsuredObjectId() == null || participationData.getCoverageId() == null)) {
                    throw new ServiceException(ServiceExceptionsType.INVALID_PARAMS_FOR_PARTICIPATION_LOAD);
                }
                if (participationData.getParticipationId() != null) {
                    policy = Policy.Impl.loadById(participationData.getPolicyId());
                } else {
                    policy = Policy.Impl.loadByOperationId(participationData.getOperationId());
                }
                riskUnit = policy.getRiskUnitById(participationData.getRiskUnitId());
                insuranceObject = riskUnit.getInsuranceObjectById(participationData.getInsuredObjectId());
                coverage = insuranceObject.getCoverageById(participationData.getCoverageId());
                ParticipationList participations = coverage.getParticipationList();
                for (Participation participation : participations) {
                    if (participation.getId() == participationData.getParticipationId()) {
                        loadedParticipationData.loadFromParticipation(participation);
                        break;
                    }
                }
            } else if (participationData.getObjectType().equals(Constants.INSURED_OBJECT_PARTICIPATION_LOAD.getValue())) {
                if ((participationData.getPolicyId() == null && participationData.getOperationId() == null) || participationData.getRiskUnitId() == null || participationData.getInsuredObjectId() == null) {
                    throw new ServiceException(ServiceExceptionsType.INVALID_PARAMS_FOR_PARTICIPATION_LOAD);
                }
                if (participationData.getPolicyId() != null) {
                    policy = Policy.Impl.loadById(participationData.getPolicyId());
                } else {
                    policy = Policy.Impl.loadByOperationId(participationData.getOperationId());
                }
                riskUnit = policy.getRiskUnitById(participationData.getRiskUnitId());
                insuranceObject = riskUnit.getInsuranceObjectById(participationData.getInsuredObjectId());
                ParticipationList participations = insuranceObject.getParticipationList();
                for (Participation participation : participations) {
                    if (participation.getId() == participationData.getParticipationId()) {
                        loadedParticipationData.loadFromParticipation(participation);
                        break;
                    }
                }
            } else if (participationData.getObjectType().equals(Constants.RISK_UNIT_PARTICIPATION_LOAD.getValue())) {
                if ((participationData.getPolicyId() == null && participationData.getOperationId() == null) || participationData.getRiskUnitId() == null) {
                    throw new ServiceException(ServiceExceptionsType.INVALID_PARAMS_FOR_PARTICIPATION_LOAD);
                }
                if (participationData.getPolicyId() != null) {
                    policy = Policy.Impl.loadById(participationData.getPolicyId());
                } else {
                    policy = Policy.Impl.loadByOperationId(participationData.getOperationId());
                }
                riskUnit = policy.getRiskUnitById(participationData.getRiskUnitId());
                ParticipationList participations = riskUnit.getParticipationList();
                for (Participation participation : participations) {
                    if (participation.getId() == participationData.getParticipationId()) {
                        loadedParticipationData.loadFromParticipation(participation);
                        break;
                    }
                }
            } else if (participationData.getObjectType().equals(Constants.POLICY_PARTICIPATION_LOAD.getValue())) {
                if (participationData.getPolicyId() == null && participationData.getOperationId() == null) {
                    throw new ServiceException(ServiceExceptionsType.INVALID_PARAMS_FOR_PARTICIPATION_LOAD);
                }
                if (participationData.getPolicyId() != null) {
                    policy = Policy.Impl.loadById(participationData.getPolicyId());
                } else {
                    policy = Policy.Impl.loadByOperationId(participationData.getOperationId());
                }
                Participation participation = policy.getParticipation(participationData.getParticipationId());
                loadedParticipationData.loadFromParticipation(participation);
            } else {
                throw new ServiceException(ServiceExceptionsType.INVALID_PARAMS_FOR_PARTICIPATION_LOAD);
            }
        } catch (Exception e) {
            throw new ServiceException(ServiceExceptionsType.ERROR_DURING_LOADING_PARTICIPATION_OBJECT);
        }
        jsonBuilder.setData(loadedParticipationData);
        return jsonBuilder.buildJson(Constants.PARTICIPATION_JSON.getValue());
    }

    public static String listEvents(PolicyData policyData) throws Exception {

        GenericJsonBuilder jsonBuilder = new GenericJsonBuilder();

        Policy policy = null;
        List<PolicyOperation> eventList = new ArrayList<PolicyOperation>();
        PolicyData loadedPolicyData = new PolicyData();
        try {
            if (policyData.getPolicyId() != null) {
                policy = Policy.Impl.loadById(policyData.getPolicyId());
                eventList = policy.listPolicyEvents();
            } else if (!StringUtil.isEmptyOrNullValue(policyData.getPolicyNumber())) {
                policy = Policy.Impl.loadByPolicyNumber(policyData.getPolicyNumber());
                eventList = policy.listPolicyEvents();
            } else if (policyData.getOperationId() != null) {
                policy = Policy.Impl.loadByOperationId(policyData.getOperationId());
                eventList = policy.listPolicyEvents();
            }
        } catch (TechnicalException e) {
            return jsonBuilder.buildJson(Constants.POLICY.getValue());
        } catch (NoSuchElementException e) {
            return jsonBuilder.buildJson(Constants.POLICY.getValue());
        }

        if (policy != null) {
            loadedPolicyData = new PolicyData();
            loadedPolicyData.setPolicyId(policy.getId());
            loadedPolicyData.setPolicyNumber(policy.getPolicyNumber());
            loadedPolicyData.setOperationId(policy.getOperationId());
            loadedPolicyData.setEventDataList(eventList);
        }

        jsonBuilder.setData(loadedPolicyData);
        return jsonBuilder.buildJson(Constants.POLICY_EVENT_LIST_JSON.getValue());
    }

    public static String loadAgreement(AgreementData agreementData) throws ServiceException {
        GenericJsonBuilder jsonBuilder = new GenericJsonBuilder();

        NewAgreement agreement = null;
        Policy policy = null;
        AgreementData loadedAgreementData = null;
        try {
            if (agreementData.getAgreementNumber() != null) {
                agreement = NewAgreementManager.getInstance().loadByAgreementNumber(agreementData.getAgreementNumber());
            } else if (agreementData.getAgreementId() != null) {
                agreement = NewAgreementManager.getInstance().load(agreementData.getAgreementId());
            } else if (agreementData.getPolicyId() != null) {
                policy = Policy.Impl.loadById(agreementData.getPolicyId());
            } else if (!StringUtil.isEmptyOrNullValue(agreementData.getPolicyNumber())) {
                policy = Policy.Impl.loadByPolicyNumber(agreementData.getPolicyNumber());
            } else {
                throw new ServiceException(ServiceExceptionsType.INVALID_PARAMS);
            }
        } catch (TechnicalException e) {
            return jsonBuilder.buildJson(Constants.MASTER_POLICY_JSON.getValue());
        } catch (NoSuchElementException e) {
            return jsonBuilder.buildJson(Constants.MASTER_POLICY_JSON.getValue());
        } catch (SQLException e) {
            return jsonBuilder.buildJson(Constants.MASTER_POLICY_JSON.getValue());
        } catch (ArrayIndexOutOfBoundsException e) {
            return jsonBuilder.buildJson(Constants.MASTER_POLICY_JSON.getValue());
        }


        if (agreement != null) {
            loadedAgreementData = new AgreementData(agreement);
        } else if (policy != null) {
            Collection<Agreement> agreementAssociated = policy.getAgreementAssociated();
            if (agreementAssociated.size() > 0) {
                Iterator<Agreement> iterator = agreementAssociated.iterator();
                Agreement agreementFromList = iterator.next();
                try {
                    loadedAgreementData = new AgreementData(NewAgreementManager.getInstance().load((long) agreementFromList.getId()));
                } catch (SQLException e) {

                }
            }
        }

        if (loadedAgreementData != null) {
            jsonBuilder.setData(loadedAgreementData);
        }
        return jsonBuilder.buildJson(Constants.MASTER_POLICY_JSON.getValue());
    }

}
