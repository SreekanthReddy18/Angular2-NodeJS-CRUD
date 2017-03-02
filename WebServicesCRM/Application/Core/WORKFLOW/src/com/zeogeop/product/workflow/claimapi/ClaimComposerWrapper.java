package com.consisint.acsele.workflow.claimapi;

import com.consisint.acsele.AggregateObject;
import com.consisint.acsele.DefaultConfigurableObject;
import com.consisint.acsele.UserInfo;
import com.consisint.acsele.Versionable;
import com.consisint.acsele.agreements.bean.AgreementClaimRejectMotive;
import com.consisint.acsele.agreements.persister.AgreementClaimRejectPersister;
import com.consisint.acsele.agreements.persister.hibernatepersister.AgreementClaimHibPersister;
import com.consisint.acsele.agreements.server.NewAgreement;
import com.consisint.acsele.audit.api.AuditTrailManager;
import com.consisint.acsele.audit.api.Context;
import com.consisint.acsele.audit.engine.ClaimContext;
import com.consisint.acsele.audit.engine.CustomAuditItem;
import com.consisint.acsele.audit.engine.CustomAuditItemManager;
import com.consisint.acsele.cashierstand.bean.OpenItemBean;
import com.consisint.acsele.cashierstand.report.CurrencyRate;
import com.consisint.acsele.cashierstand.type.PaymentTransactionType;
import com.consisint.acsele.claim.api.*;
import com.consisint.acsele.claim.api.payment.PaymentStatus;
import com.consisint.acsele.claim.api.paymentorder.PaymentOrderStatus;
import com.consisint.acsele.claim.api.paymentorder.PaymentOrderType;
import com.consisint.acsele.claim.engine.ClaimBeneficiaryPK;
import com.consisint.acsele.claim.paymentorder.service.PaymentOrderService;
import com.consisint.acsele.claim.service.ClaimValidationService;
import com.consisint.acsele.claim.service.impl.ClaimValidationServiceImpl;
import com.consisint.acsele.coinsurance.persistent.CoinsuranceContract;
import com.consisint.acsele.entry.service.EntryService;
import com.consisint.acsele.entry.service.impl.EntryServiceImpl;
import com.consisint.acsele.entry.servlet.CreateEntrys;
import com.consisint.acsele.entry.symbols.SymbolsCashier;
import com.consisint.acsele.entry.symbols.SymbolsClaim;
import com.consisint.acsele.entry.types.GroupOperationType;
import com.consisint.acsele.entry.types.OperationType;
import com.consisint.acsele.genericcontracts.genericinterface.MaintainGenericContractsImpl;
import com.consisint.acsele.interfaces.axa.oim.bean.OIMTPClaimEnum;
import com.consisint.acsele.interseguro.interfaces.intermedia.openitems.services.OpenItemService;
import com.consisint.acsele.letters.correspondence.CorrespondenceHistory;
import com.consisint.acsele.letters.strategy.LetterGenerator;
import com.consisint.acsele.life.engine.InsuranceObjectInfo;
import com.consisint.acsele.maintenancegeneric.util.Tools;
import com.consisint.acsele.management.api.*;
import com.consisint.acsele.management.maintainer.User;
import com.consisint.acsele.openapi.claim.ClaimHistory;
import com.consisint.acsele.openapi.claim.CoverageReserve;
import com.consisint.acsele.openapi.currency.Currency;
import com.consisint.acsele.openapi.currency.CurrencyList;
import com.consisint.acsele.openapi.policy.DynamicData;
import com.consisint.acsele.openapi.policy.agreement.Agreement;
import com.consisint.acsele.openapi.product.ClaimRejectionMotive;
import com.consisint.acsele.openapi.product.ClaimRejectionMotiveList;
import com.consisint.acsele.persistence.hibernate.AcseleHibernateSessionProvider;
import com.consisint.acsele.persistent.IDDBFactory;
import com.consisint.acsele.persistent.managers.*;
import com.consisint.acsele.persistent.persisters.ValidationDamageClaimPersister;
import com.consisint.acsele.policy.api.Policy;
import com.consisint.acsele.policy.ejb.ValidationHandler;
import com.consisint.acsele.policy.server.*;
import com.consisint.acsele.policy.session.OperationPK;
import com.consisint.acsele.policy.session.PolicySession;
import com.consisint.acsele.product.ProductLanguageHandler;
import com.consisint.acsele.product.applet.EnumValidationCovClaim;
import com.consisint.acsele.product.applet.ValidationCoverageClaim;
import com.consisint.acsele.product.applet.ValidationDamageClaim;
import com.consisint.acsele.product.applet.ValidationUtil;
import com.consisint.acsele.product.engine.bean.ProductBehaviour;
import com.consisint.acsele.product.engine.life.ReserveMovement;
import com.consisint.acsele.product.engine.life.ReserveOperationType;
import com.consisint.acsele.product.server.*;
import com.consisint.acsele.product.server.persistent.ClaimCovRelationshipPersister;
import com.consisint.acsele.product.server.persistent.hibernate_persister.ClaimCovRelationshipHibPersister;
import com.consisint.acsele.reinsurance.distribution.ReinsuranceDistributionService;
import com.consisint.acsele.reinsurance.history.beans.HistoryType;
import com.consisint.acsele.reinsurance.history.beans.OperationHistory;
import com.consisint.acsele.reinsurance.history.beans.OperationHistoryDetail;
import com.consisint.acsele.reinsurance.reinsuranceconfiguration.Constants;
import com.consisint.acsele.reinsurance.reinsuranceconfiguration.ReinsuranceContract;
import com.consisint.acsele.reinsurance.reinsuranceoperations.ClaimOperation;
import com.consisint.acsele.reinsurance.testinterface.AccessContractsImpl;
import com.consisint.acsele.template.api.*;
import com.consisint.acsele.template.lifecycle.beans.api.EnumCategoryEventType;
import com.consisint.acsele.template.server.*;
import com.consisint.acsele.thirdparty.api.ThirdPartyFinancialDetail;
import com.consisint.acsele.thirdparty.api.ThirdPartyRole;
import com.consisint.acsele.thirdparty.beans.impls.ThirdPartyImpl;
import com.consisint.acsele.thirdparty.persistent.AddressBook;
import com.consisint.acsele.thirdparty.persistent.AddressBookDynamic;
import com.consisint.acsele.thirdparty.persistent.Role;
import com.consisint.acsele.thirdparty.persistent.ThirdParty;
import com.consisint.acsele.title.api.CoverageTitle;
import com.consisint.acsele.uaa.*;
import com.consisint.acsele.uaa.api.*;
import com.consisint.acsele.uaa.bean.UaaDetail;
import com.consisint.acsele.uaa.ejb.openitemreference.OpenItemReference;
import com.consisint.acsele.uaa.ejb.openitemreference.OpenItemReferenceDTO;
import com.consisint.acsele.uaa.ejb.openitemreference.OpenItemReferenceImpl;
import com.consisint.acsele.uaa.financial.FinancialSession;
import com.consisint.acsele.uaa.financial.FinancialSessionImpl;
import com.consisint.acsele.uaa.openitem.OpenItemReferenceType;
import com.consisint.acsele.uaa.search.SearchThirdPartyResult;
import com.consisint.acsele.util.*;
import com.consisint.acsele.util.context.CRMInternalServices;
import com.consisint.acsele.util.error.*;
import com.consisint.acsele.util.evaluator.*;
import com.consisint.acsele.util.forms.tools.Util;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.workflow.beans.*;
import com.consisint.acsele.workflow.claimInterfaces.forms.BeneficiariesAndPercentageForm;
import com.consisint.acsele.workflow.claimInterfaces.forms.ClaimRequisitesForm;
import com.consisint.acsele.workflow.claimInterfaces.forms.CoverageForm;
import com.consisint.acsele.workflow.claimInterfaces.forms.SearchBean;
import com.consisint.acsele.workflow.claimapi.historical.ClaimHistoricalMovementType;
import com.consisint.acsele.workflow.claimapi.historical.ClaimHistoricalOperationType;
import com.consisint.acsele.workflow.claimapi.persistent.ClaimNormalReservePersister;
import com.consisint.acsele.workflow.claimapi.persistent.ClaimPersister;
import com.consisint.acsele.workflow.claimapi.persistent.ClaimStateBeanPersister;
import com.consisint.acsele.workflow.claimapi.persistent.PaymentOrderPersister;
import com.consisint.acsele.workflow.claimapi.recovery.ClaimDetailsPaymentBean;
import com.consisint.acsele.workflow.claimapi.recovery.ClaimMovementBean;
import com.consisint.acsele.workflow.claimapi.recovery.ClaimStateBean;
import com.consisint.acsele.workflow.claimapi.recovery.ClaimUtil;
import com.consisint.acsele.workflow.claimapi.service.ClaimProcessService;
import com.consisint.acsele.workflow.claimapi.validator.ClaimValidator;
import com.consisint.acsele.workflow.claimtool.bean.*;
import com.consisint.acsele.workflow.tools.WController;
import com.consisint.acsele.workflow.workflowcontroller.ClaimNote;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import org.apache.commons.lang.math.NumberUtils;

import javax.ejb.FinderException;
import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;


/**
 * Title: ClaimComposerWrapper.java <br>
 * Copyright: (c) 2008 Consis International<br>
 * Company: Consis International<br>
 *
 * @author Consis International (CON)
 * @version Acsel-e v5.2
 */

public class ClaimComposerWrapper implements ClaimComposer, Serializable {

    private static final AcseleLogger log = AcseleLogger.getLogger(ClaimComposerWrapper.class);
    private static EntryServiceImpl entryService = (EntryServiceImpl) BeanFactory.getBean(EntryService.class);
    private transient AgregatedPolicy policy = null;
    protected transient Transaction tx = null;
    private transient Claim claim;
    private OpenItemHome openItemHome;
    private boolean beginTransactions = false;
    private boolean startTx = true;
    private long claimId;
    private ClaimExclusions claimExclusions = null;
    private Collection beneficiaries = null;
    private OperationPK operationPK;
    private static final long serialVersionUID = -3757639021598363777L;
    private boolean asl;
    private RoleList roles;
    private Date ocurrenceDate;
    private boolean isValidated = false;


    //Constants...
    public static final String OCURRENCE_DATE = "ocurrenceDate";
    public static final String TEMPLATE_CLAIM_CLOSED = "templateclaimclosed";
    public static final String TEMPLATE_CLAIM_DENIED = "templateclaimdenied";
    public static final String TEMPLATE_CLAIM_REOPEN = "templateclaimreopen";
    public static final String CLAIM_REINSURANCE_CESSIONS_GENERATED = "totalize";

    private Policy policyOA;
    public Boolean isRetroactive;
    private Long dcoClaimDenied = null;

    /**
     * Constructor
     */
    public ClaimComposerWrapper() {
        //UserInfo.setCountry((String) JNDIUtil.lookup("java:comp/env/country"));
        openItemHome = new OpenItemHomeWrapper();
    }

    public Policy getPolicyOA() {
        return policyOA;
    }

    public void setPolicyOA(Policy policyOA) {
        this.policyOA = policyOA;
    }

    public Date getOcurrenceDate() {
        return getOcurrenceDate(claim);
    }

    public Date getOcurrenceDate(Claim claimPolicy) {
        this.claim = claimPolicy;
        if (ocurrenceDate == null) {
            ocurrenceDate = claim.getOcurrenceDate();
        }
        return ocurrenceDate;
    }

    /**
     * Constructor
     */
    public ClaimComposerWrapper(boolean asl) {
        super();
        this.asl = asl;

    }

    private void closeHibernateSession(Session session) {
    }

    /**
     * Find a claim
     *
     * @param claimId
     * @return Claim
     */
    public Claim findClaim(String claimId) {
        boolean isSessionNull = HibernateUtil.isSessionNull();
        log.info("findClaim(String claimId) - isSessionNull = " + isSessionNull);
        Session session = null;
        try {
            session = getHibernateSession();
            this.claimId = Long.parseLong(claimId);
            claim = Claim.getInstance(Long.valueOf(claimId));
            log.debug("## claimId=" + claimId + ", claim = " + claim);
            return this.claim;
        } catch (Exception e) {
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.FATAL, e);
        } finally {
            if (isSessionNull) {
                closeHibernateSession(session);
            }
        }
    }

    public Claim findClaimByClaimNumber(String claimNumber) {
        log.info("findClaimByClaimNumber(String claimNumber)");
        Session session = null;
        try {
            session = getHibernateSession();
            claim = Claim.loadByClaimNumber(claimNumber);
            claimId = Long.parseLong(claim.getPk());
            policy = claim.getAgregatedPolicy();
            return claim;
        } catch (Exception e) {
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.FATAL, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * @param request
     * @return boolean
     * @throws StackException
     */
    public boolean checkPolicyConditions(ClientRequest request) throws StackException, ApplicationException {

        log.info("checkPolicyConditions(ClientRequest request)");
        Session session = null;
        boolean result = false;
        try {
            session = getHibernateSession();
            ClaimCreationBean claimCreationBean = (ClaimCreationBean) request.getAttribute("data");

            //Verifica si la fecha de ocurrencia es retroactiva
            Date dateClaim;
            Date retroactiveDate = com.consisint.acsele.openapi.claim.ClaimValidator.getRetroactivityDateClaim(claimCreationBean.getOcurrenceDate(), String.valueOf(policy.getId()));
            if (retroactiveDate != null) {
                dateClaim = new java.sql.Date(retroactiveDate.getTime());
            } else
                dateClaim = claimCreationBean.getOcurrenceDate();

            result = checkPolicyConditions(policy, claimCreationBean.getSelectedRiskUnits(), (java.sql.Date) dateClaim,
                    claimCreationBean.getNotification(), claimCreationBean.getEventTypeId());
        } catch (StackException e) {
            log.error("Error: ", e);
            throw e;
        } catch (ApplicationException e) {

            throw e;
        } catch (Exception e) {
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.FATAL, e);
        } finally {
            closeHibernateSession(session);
        }
        return result;
    }

    /**
     * @param policy
     * @param riskUnits
     * @param ocurrenceDate
     * @param claimNotification
     * @param eventTypeId
     * @return boolean
     * @throws StackException
     */
    private boolean checkPolicyConditions(AgregatedPolicy policy, Collection riskUnits, java.sql.Date ocurrenceDate,
                                          ClaimNotification claimNotification, String eventTypeId) throws StackException, ApplicationException {
        log.info("checkPolicyConditions(.....)");
        log.debug("policy.getPolicyNumber() = " + policy.getPolicyNumber());
        log.debug("ocurrenceDate = " + ocurrenceDate);
        log.debug("eventTypeId = " + eventTypeId);
        boolean valid = true;
        StackException e = new StackException(Exceptions.CCCouldNotCreate, Severity.INFO);

        if(this.claim!=null){
            if(claim.getOperationPK()!=policy.getOperationId()){
                policy = (AgregatedPolicy) Policy.Impl.loadByOperationId(claim.getOperationPK());
            }else {
                policy= (AgregatedPolicy) Policy.Impl.loadByIdAndEffectiveDate(policy.getPolicyPk(),ocurrenceDate,UserInfo.getGlobalUser());
            }
        }
        if(policy!=null) {
            if (!isPolicyActive(policy, ocurrenceDate)) {
                e.add(Exceptions.CCEValidationNonActivePolicy);
                log.debug(Exceptions.CCEValidationNonActivePolicy);
                throw new ApplicationException(
                        ResourceBundle.getBundle("exceptions", UserInfo.getLocale()).getString(Exceptions.CCEValidationNonActivePolicy));
            }

            if (!checkClaimNotificationDate(policy, ocurrenceDate, claimNotification)) {
                valid = false;
                e.add(Exceptions.CCInvalidNotificationDate);
                log.debug(Exceptions.CCInvalidNotificationDate);
            }

            if (!checkPolicyPayments(policy, ocurrenceDate)) {
                valid = false;
                e.add(Exceptions.CCPolicyUnPaided);

                log.debug(Exceptions.CCPolicyUnPaided);
            }

            if (!hasCoverage(policy, ocurrenceDate, riskUnits)) {
                log.debug(Exceptions.CCPolicyWithoutCoverage);
            }

            if (!eventTypeIDValid(policy, eventTypeId)) {
                valid = false;
                e.add(Exceptions.CCEventClaimInvalid);
                e.setPropertyString(eventTypeId);
                log.debug(Exceptions.CCEventClaimInvalid);
            }
        }else{
            throw new ApplicationException(Exceptions.PPErrorPolicyNotExist);
        }
        log.debug(" resultado de la validacion: " + valid + " # de condiciones fallidas: " + e.size());
        if (!valid) {
            throw e;
        }
        return true;
    }

    public boolean isPolicyActive(AgregatedPolicy policy, java.sql.Date occurenceDate) {
        return !isCancelState(policy.getStateOA().getDesc()) && !isPolicySuspended(policy) && checkPolicyForce(policy, occurenceDate);
    }

    private boolean isPolicySuspended(AgregatedPolicy policy) {
        return isSuspendedState(policy.getState().getDesc());
    }


    /**
     * Verify is the policy was active in the ocurrence date
     *
     * @return boolean
     */
    private boolean isPolicyCancelled(AgregatedPolicy policy, java.sql.Date occurenceDate) {
        log.debug("Policy pk del operation" + policy.getOperationPK().getPK());
        log.debug("Policy OperationID del operation" + policy.getOperationPK().getOperationID());
        log.debug("Policy....name" + policy.getPolicyNumber());
        log.debug("Policy....substatus" + policy.getSubStatus());

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        StringBuffer queryLoad = new StringBuffer(190);
        String state = null;
        Date feOcu = null;
        try {
            conn = JDBCUtil.openUserDbConnection();
            queryLoad.append("select B.OPERATIONPK, B.FINISHDATE, C.DESCRIPTION  from CONTEXTOPERATION a ");
            queryLoad.append("join  POLICYDCO B on (a.ITEM = B.AGREGATEDOBJECTID) join STATE C on C.STATEID = B.STATEID ");
            queryLoad.append("where a.ITEM = ? and  a.id = (select max(id) from CONTEXTOPERATION F where F.ITEM = a.ITEM and  F.TIME_STAMP < ? AND F.STATUS =");
            queryLoad.append(Versionable.STATUS_APPLIED);
            queryLoad.append(" ) and a.id = B.OPERATIONPK  ");
            log.debug(policy.getId() + " " + occurenceDate);
            statement = conn.prepareStatement(queryLoad.toString());
            statement.setString(1, policy.getPk());
            ;
            statement.setDate(2, occurenceDate);
            rs = statement.executeQuery();
            if (rs.next()) {
                state = rs.getString("DESCRIPTION");
                feOcu = rs.getDate("FINISHDATE");
            }
            log.debug(state + " " + feOcu);
        } catch (Exception SQLEx) {
            log.error("Query = " + queryLoad, SQLEx);
            throw new TechnicalException(Exceptions.PPErrorLoadingPolicy, Severity.FATAL, SQLEx);
        } finally {
            JDBCUtil.closeQuietly(rs, statement, conn);
        }
       /* if (policy.getState() == null) {
            return false;
        }*/

        return isCancelState(state);
    }

    /**
     * @param policy
     * @param eventTypeId
     * @return boolean
     */
    public boolean eventTypeIDValid(AgregatedPolicy policy, String eventTypeId) {
        log.info("eventTypeIDValid(AgregatedPolicy policy, String eventTypeId)");
        log.info("eventTypeId CCW = " + eventTypeId);
        Inheritable inheritable = policy.getProduct().getEventClaimByPK(Long.parseLong(eventTypeId));

        return inheritable != null;
    }

    /**
     * Check if the notification date was in a apropiated date, i.e, a maximun of n days after the
     * ocurrence date number of days specified. The number of days is specified in the product.
     *
     * @param claimNotification
     * @return true is the date of notification is valid, false otherwise
     */
    private boolean checkClaimNotificationDate(AgregatedPolicy policy, java.sql.Date ocurrenceDate, ClaimNotification claimNotification) {
        log.info("checkClaimNotificationDate(AgregatedPolicy policy, java.sql.Date ocurrenceDate, ClaimNotification claimNotification)");
        if (claimNotification != null) {
            String notificationDaysProperty = AcseleConf.getProperty("notificationDaysProperty");
            String days = policy.getProduct().getDCO().getCriterioInput(notificationDaysProperty);

            Date notificationDate = claimNotification.getNotificationDate();
            Date maxNotificationDate = Funciones.sumDaysToDate(ocurrenceDate, (days != null) ? days : "30");


            log.debug(" dias de plazo para notificar: " + days + " notificationDate: " + notificationDate + " ocurrenceDate: " + ocurrenceDate
                    + " maxNotificationDate: " + maxNotificationDate);

            return (notificationDate.compareTo(maxNotificationDate) <= 0);
        }
        return true;
    }


    /**
     * todo asumo que la poliza no cambia de estado durante el periodo de gracia
     * Check if the claim ocurrence was in the  policy force
     *
     * @return true is the ocurrence date was between the policy force or extra days to renew, false
     * otherwise
     */
    private boolean checkPolicyForce(AgregatedPolicy policy, java.sql.Date ocurrenceDate) { //vigencia
        log.info("checkPolicyForce(AgregatedPolicy policy, java.sql.Date ocurrenceDate)");
        log.debug("Va chequear la vigencia");
        if ((policy.getInitialDate() == null && policy.getFirstInitialDate()==null) || ((policy.getValidity() != ValidityType.OPEN) && (policy.getFinishDate() == null))) {

            log.debug("policy.getInitialDate()" + policy.getInitialDate() + " policy.getFinishDate()" + policy.getFinishDate());

            return false;
        }
        boolean allowRetroactive = policy.getProduct().getProductBehaviour().isAllowRetroactive();
        boolean effectiveDateVersion = policy.getProduct().getProductBehavior().isEffectiveDateVersion();
        Date emissionDate = getEmissionDate(policy);
        if (policy.getValidity() == ValidityType.OPEN) {
            if (allowRetroactive || effectiveDateVersion) {
                return (emissionDate.compareTo(ocurrenceDate) <= 0);
            }
            return (policy.getInitialDate().compareTo(ocurrenceDate) <= 0);
        } else {
            String extraDaysProperty = AcseleConf.getProperty("extraForceDaysProperty");

            log.debug("policy.getProduct().getPk() = " + policy.getProduct().getId());
            DefaultConfigurableObject dcoImpl = policy.getProduct().getDCO();
            log.debug("policy.getProduct().getDCOImpl().getPk() = " + dcoImpl.getId());

            String days = dcoImpl.getCriterioInput(extraDaysProperty); //cambio solicitado por DJB el 30-09-2003
            log.debug("days product = " + days);
            if (StringUtil.isEmptyOrNullValue(days)) {
                days = policy.getDCO().getCriterioInput(extraDaysProperty);
            }

            Date maxFinalDate = Funciones.sumDaysToDate(policy.getFinishDate(), ((StringUtil.isEmptyOrNullValue(days)) ? "30" : days));

            log.debug(" dias de gracia: " + days + " ocurrenceDate: " + DateUtil.getFormatLongShow().format(ocurrenceDate) + " inicio de vigencia:"
                    + DateUtil.getFormatLongShow().format(policy.getInitialDate()) + " final de vigencia:"
                    + DateUtil.getFormatLongShow().format(policy.getFinishDate()) + " vigencia con prorroga:" + DateUtil.getFormatLongShow()
                    .format(maxFinalDate));
            if (allowRetroactive || effectiveDateVersion) {
                return (emissionDate.compareTo(ocurrenceDate) <= 0) && ((maxFinalDate.compareTo(ocurrenceDate) >= 0));
            }
            return (policy.getInitialDate().compareTo(ocurrenceDate) <= 0) && ((maxFinalDate.compareTo(ocurrenceDate) >= 0));
        }
    }

    private Date getEmissionDate(AgregatedPolicy agregatedPolicy) {
        String emissionDateInput = agregatedPolicy.getDCO().getCriterioInput(AcseleConf.getProperty("FecEmision"));
        java.util.Date emissionDate;
        try {
            emissionDate = DateUtil.getFormatToShow().parse(emissionDateInput);
        } catch (Exception e) {
            String message = " Error getting the value of Emision Date.  " + " Check the value of the system property ( FecEmision ) ";
            throw new ApplicationException(Exceptions.PPPropertyNotExistentInConfigurationFile, Severity.ERROR, message);
        }
        return emissionDate;
    }

    private boolean checkPolicyPayments(AgregatedPolicy policy, java.sql.Date ocurrenceDate) {
        log.info("checkPolicyPayments(AgregatedPolicy policy, java.sql.Date ocurrenceDate)");
        String reservePendingPremiumsProperty = AcseleConf.getProperty("reservePendingPremiumsProperty");
        log.debug("reservePendingPremiumsProperty = " + reservePendingPremiumsProperty);
        Product product = policy.getProduct();
        log.debug("product.getDesc() = " + product.getDesc());
        DefaultConfigurableObject dco = product.getDCO();
        log.debug("dco.getCOT().getDesc() = " + dco.getCOT().getDesc());
        String reservePendingPremiumsInd = dco.getCriterioInput(reservePendingPremiumsProperty);

        log.debug("ClaimComposerEJB.checkPolicyPayments -> reservePendingPremiumsInd  '" + reservePendingPremiumsInd + "'");
        boolean canReservePendingPremiumsInd = (reservePendingPremiumsInd != null) && (!"no".equals(reservePendingPremiumsInd.trim().toLowerCase()));
        SimpleDateFormat sdf = DateUtil.getFormatToShow();
        String ocurrenceDateStr = sdf.format(ocurrenceDate);

        if (!canReservePendingPremiumsInd) {
            ClaimValidationServiceImpl claimValidationService = new ClaimValidationServiceImpl();
            claimValidationService.setOcurrenceDate(ocurrenceDate);
            claimValidationService.setPolicy(policy);
            int gracePeriod = claimValidationService.getGracePeriod();
            if (gracePeriod != 0) {
                log.debug("this policy have grace Period configured" + "graceDay :" + gracePeriod);
                if (!claimValidationService.validateGracePolicy(gracePeriod)) {
                    boolean isOnlyDocDacte = true;
                    double pendingPremium = Funciones.getAllPendingNetPremiumsForPolicy(ocurrenceDateStr, policy.getPK(), isOnlyDocDacte); // TODO cambiar por solo docdate
                    return pendingPremium == 0;
                }
                return true;
            } else {
                double pendingPremium = Funciones.getAllPendingNetPremiumsForPolicy(ocurrenceDateStr, policy.getPK());
                log.debug("ClaimComposerEJB.checkPolicyPayments -> pendingPremium " + pendingPremium);
                return pendingPremium == 0;
            }
        } else {
            return true;
        }
    }

    /**
     * Check if exits a valid coverage at least in the ocurrence date
     * ( for the claim cause - not yet).
     *
     * @param riskUnitsSelected
     * @return true if exist one or more valid coverages, false otherwise
     */
    private boolean hasCoverage(AgregatedPolicy policy, Date ocurrenceDate, Collection riskUnitsSelected) {
        log.info("hasCoverage(AgregatedPolicy policy, Date ocurrenceDate, Collection riskUnitsSelected)");
        log.debug("policy.getInitialDate()" + policy.getInitialDate() + " policy.getFinishDate()" + policy.getFinishDate());
        if ((policy.getInitialDate() == null) || ((policy.getValidity() != ValidityType.OPEN) && (policy.getFinishDate() == null))) {

            log.debug("policy.getInitialDate()" + policy.getInitialDate() + " policy.getFinishDate()" + policy.getFinishDate());
            return false;
        }

        if (riskUnitsSelected == null) {
            return false;
        }
        boolean has = false;

        if (policy.getValidity() == ValidityType.OPEN) {
            Enumeration riskUnits = getSelectedRiskUnitEnumeration(riskUnitsSelected, ocurrenceDate);
            while (riskUnits.hasMoreElements()) {
                AgregatedRiskUnit aru = (AgregatedRiskUnit) riskUnits.nextElement();
                if (!(aru.getInitialDate().compareTo(ocurrenceDate) <= 0)) { // no esta vigente
                    continue;
                }
                List<AgregatedInsuranceObject> insuranceObjects = getAgregatedInsuranceObjectList(aru);
                for (AgregatedInsuranceObject aio : insuranceObjects) {
                    if (!(aio.getInitialDate().compareTo(ocurrenceDate) <= 0)) { // no esta vigente
                        continue;
                    }
                    List<EvaluatedCoverage> coverages = getEvaluatedCoverageList(aio);
                    for (EvaluatedCoverage coverage : coverages) {

                        if (coverage.getInitialDate().compareTo(ocurrenceDate) <= 0) { // no esta vigente
                            has = true;
                        }
                        log.debug(" esta vigente la cobertura?: " + coverage.getDesc() + " " + has + " init date: " + coverage.getInitialDate()
                                + " final date: " + coverage.getFinishDate());
                    }
                }
            }
        } else {

            String extraDaysProperty = AcseleConf.getProperty("extraForceDaysProperty");
            String days = policy.getProduct().getDCO().getCriterioInput(extraDaysProperty);   //cambio solicitado por DJB el 30-09-2003
            if (days == null) {
                policy.getDCO().getCriterioInput(extraDaysProperty);
            }

            Date maxFinalDate = Funciones.sumDaysToDate(policy.getFinishDate(), ((days != null) ? days : "30"));

            Enumeration riskUnits = getSelectedRiskUnitEnumeration(riskUnitsSelected, ocurrenceDate);
            while (riskUnits.hasMoreElements()) {
                AgregatedRiskUnit aru = (AgregatedRiskUnit) riskUnits.nextElement();
                if (!((aru.getInitialDate().compareTo(ocurrenceDate) <= 0) && (ocurrenceDate.compareTo(maxFinalDate) <= 0))) { // no esta vigente
                    continue;
                }
                List<AgregatedInsuranceObject> insuranceObjects = getAgregatedInsuranceObjectList(aru);
                for (AgregatedInsuranceObject aio : insuranceObjects) {
                    if (!((aio.getInitialDate().compareTo(ocurrenceDate) <= 0) && (ocurrenceDate.compareTo(maxFinalDate) <= 0))) { // no esta vigente
                        continue;
                    }
                    List<EvaluatedCoverage> coverages = getEvaluatedCoverageList(aio);
                    for (EvaluatedCoverage coverage : coverages) {

                        if ((coverage.getInitialDate().compareTo(ocurrenceDate) <= 0) && (ocurrenceDate.compareTo(maxFinalDate)
                                <= 0)) { // no esta vigente
                            has = true;
                        }
                        log.debug(" esta vigente la cobertura?: " + coverage.getDesc() + " " + has + " max date " + maxFinalDate + " init date: "
                                + coverage.getInitialDate() + " final date: " + coverage.getFinishDate());
                    }
                }
            }
        }
        return has;
    }

    /**
     * Build the tree of a claim
     *
     * @return the response of the actionç
     */
    public ClientResponse buildTree(String claimId) {
        log.info("buildTree(String claimId)");
        findClaim(claimId);
        ClientResponse response = new ClientResponse();
        try {
            log.debug("[buildTree] claim " + claim);
            if (this.claim != null) {
                response.putAttribute("Claim", this.claim);
                response.putAttribute("policyId", this.claim.getPolicyId());
            }
            response.setResult(true);
            return response;
        } catch (Exception ec) {
            log.error("Error", ec);
            throw new TechnicalException(" Claim Composer could not invoke Claim Entity ", Severity.FATAL, ec);
        }
    }

    /**
     * Gets the types of damages asociated with de claim event
     *
     * @return the response of the action
     */
    public ClientResponse getEventClaimTypes(String requestCommand, String ioID) throws ApplicationExceptionChecked {
        log.info(" *** METHOD NAME:  getEventClaimTypes(..) - Entrando");
        log.debug("requestCommand -> " + requestCommand);
        log.debug("ioID " + ioID);
        Session session = null;
        try {
            session = getHibernateSession();
            ClientResponse response = new ClientResponse();
            response.setCommand(requestCommand);
            Collection<ClaimRiskUnit> claimRU = claim.getClaimRiskUnitsList();
            //  Chequeamos que NO esté ya asociado
            for (ClaimRiskUnit cru : claimRU) {
                log.debug("cru.getDesc() = " + cru.getDesc());
                Collection<ClaimInsuranceObject> claimInsuranceObjects = cru.getClaimInsuranceObjectsList();
                ClaimInsuranceObject cio = null;
                for (ClaimInsuranceObject claimInsuranceObject : claimInsuranceObjects) {
                    if (claimInsuranceObject.getDesc().equals(ioID)) {
                        cio = claimInsuranceObject;
                        break;
                    }
                }
                if (cio != null) {
                    response.setResult(true);
                    response.putAttribute("error", "claim.associateError");
                    return response;
                }
            }

            // Cargamos el EventClaim del Producto y sus ClaimTypes(Damage Templates)
            // Para Obtener las etiqueta de cada Damage Template y de cada Event Claim Coverage.
            Map<String, String> covs = new HashMap<String, String>();
            Map<String, Map<String, String>> claimtypes = new HashMap<String, Map<String, String>>();
            EventClaim eventClaim = claim.getEventClaim();
            log.debug("eventClaim = " + eventClaim);
            Enumeration claimTypes = eventClaim.elements();
            while (claimTypes.hasMoreElements()) { //Cargamos los Claim Types...
                ClaimType claimType = (ClaimType) claimTypes.nextElement();
                log.debug("***** claimType = " + claimType);
                Map<String, EventClaimCoverage> eventClaimCoverages = claimType.getEventClaimCoverages();
                Collection<EventClaimCoverage> enumeration = eventClaimCoverages.values();
                Map<String, String> eventCoverages = new HashMap<String, String>();
                for (EventClaimCoverage eventClaimCoverage : enumeration) { //Cargamos las coberturas asociadas a cada Claim Type.
                    String descCov = eventClaimCoverage.getDesc();
                    log.debug("***** eventClaimCoverage = " + descCov);
                    covs.put(descCov, descCov);
                    eventCoverages.put(descCov, claimType.getLabel());  //Guardo el label deo claimType de la cobertura.
                }
                log.debug("claimType.getDesc() = " + claimType.getDesc());
                claimtypes.put(claimType.getDesc(), eventCoverages);
            }

            //Cargamos las ClaimRiskUnits del Claim para obtener las coberturas asociadas al IO que el usuario selecciono.
            Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
            Enumeration<EvaluatedCoverage> ecovs = null;

            List<String> covDescs = new ArrayList<String>();
            AgregatedInsuranceObject aio = null;
            if (claimRiskUnits != null && !claimRiskUnits.isEmpty()) {
                log.debug("claimRiskUnits != null && !claimRiskUnits.isEmpty() es true");
                for (ClaimRiskUnit riskUnit : claimRiskUnits) {
                    log.debug("****** riskUnit = " + riskUnit);
                    AgregatedRiskUnit aru = riskUnit.getAgregatedRiskUnit();
                    log.debug("***** AgregatedRiskUnit aru = " + aru.getDesc());
                    aio = getAgregatedInsuranceObject(aru, ioID); //Obtengo el IO que seleccione..
                    log.debug("***** aio = " + aio);
                    if (aio != null) {//Si hay mas de una RU, el aio puede ser null
                        List<EvaluatedCoverage> coverages = getEvaluatedCoverageList(aio);
                        for (EvaluatedCoverage ecov : coverages) {
                            log.debug("****** EvaluatedCoverage ecov = " + ecov.getDesc());
                            String covDesc = ecov.getDesc();
                            covDescs.add(covDesc);  //Obtengo todas las coberturas asociadas al IO.
                        }
                    }
                }
            } else {
                boolean found = false;
                log.debug("claimRiskUnits != null && !claimRiskUnits.isEmpty() es false");
                Enumeration rus = policy.getRiskUnits();
                while (rus.hasMoreElements() && !found) {
                    AgregatedRiskUnit aru = (AgregatedRiskUnit) rus.nextElement();
                    log.debug("***** AgregatedRiskUnit aru = " + aru.getDesc());
                    aio = aru.getInsuranceObject(ioID); //Obtengo el IO que seleccione...
                    log.debug("***** aio = " + aio);
                    if (aio != null) {
                        found = true;
                        ecovs = aio.getEvaluatedCoverages();
                        while (ecovs.hasMoreElements()) {
                            EvaluatedCoverage ecov = ecovs.nextElement();
                            String covDesc = ecov.getDesc();
                            log.debug("***** EvaluatedCoverage ecov = " + covDesc);
                            covDescs.add(covDesc);  //Obtengo todas las coberturas asociadas al IO.
                        }
                    }
                }
            }

            if (aio == null) {
                log.error("The ioID (" + ioID + ") has not an InsuranceObject associated.");
            } else {
                log.debug("aio != null " + aio.getDesc());
            }


            //Filtrando los eventType para que permita crear el reclamo solo...
            // para las coberturas que tiene asociado el plan que el usuario selecciono.
            Collection<String> eventClaimsCoverages = covs.values();
            Iterator<String> iterator = eventClaimsCoverages.iterator();
            HashMap<String, String> dataFinal = new HashMap<String, String>();
            Set<String> cotIds = claimtypes.keySet();
            while (iterator.hasNext()) {
                String eventClaimDesc = iterator.next();
                log.debug("***** eventClaimDesc = " + eventClaimDesc);
                for (int j = 0; j < covDescs.size(); j++) {
                    String comparation = covDescs.get(j).trim();
                    //Valido que la cobertura este asociada al IO.
                    if (comparation.equalsIgnoreCase(eventClaimDesc)) {
                        String cotLabel = covs.get(eventClaimDesc);
                        log.debug("cotLabel = " + cotLabel);
                        for (String cotDesc : cotIds) {
                            HashMap eventCoverages = (HashMap) claimtypes.get(cotDesc);
                            String cot = (String) eventCoverages.get(cotLabel);
                            log.debug("cot = " + cot + " cotLabel = " + cotLabel);
                            if (!StringUtil.isEmptyOrNullValue(cot) && !dataFinal.containsValue(cot)) {
                                dataFinal.put(cotDesc, cot);
                            } //Almacenando los eventTypes validos.

                        }
                    }
                }
            }
            log.debug("dataFinal.size() = " + dataFinal.size());
            log.debug("dataFinal = " + dataFinal);
            //E-YAM

            response.putAttribute("responsedata", dataFinal);
            response.setResult(true);
            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            return response;
        } finally {
            closeHibernateSession(session);
            log.info(" *** METHOD NAME:  getEventClaimTypes(..) - Saliendo");
        }
    }

    /**
     * Desassociate an object to a claim
     *
     * @return the response of the action
     */
    public ClientResponse desassociate(ClientRequest request) throws ApplicationExceptionChecked {
        log.info("desassociate(ClientRequest request)");
        Session session = null;
        Transaction transaction = null;
        ClientResponse response = new ClientResponse();
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);
            log.debug("Desasociando Objects");
            response.setResult(true);
            String classSource = (String) request.getAttribute("classSource");
            log.debug("classSource = " + classSource);
            if (classSource.equals(PolicySystem.CLAIM_INSURANCE_OBJECT)) {
                String aruId = (String) request.getAttribute("ruID");
                String ioID = (String) request.getAttribute("ioID");
                desassociateInsuranceObject(aruId, ioID, response);
            }
            if (classSource.equals(PolicySystem.CLAIM_NORMAL_RESERVE)) {
                String aruId = (String) request.getAttribute("ruID");
                String idSource = (String) request.getAttribute("claimReserveId");
                String ioID = (String) request.getAttribute("ioID");
                desassociateClaimNormalReserve(aruId, idSource, ioID);
            }
            if (classSource.equals(PolicySystem.CLAIM_RESERVE_BY_CONCEPT)) {
                String aruId = (String) request.getAttribute("ruID");
                String idSource = (String) request.getAttribute("claimReserveId");
                String ioID = (String) request.getAttribute("ioID");
                desassociateClaimReservedByConcept(aruId, idSource, ioID, response);
                log.debug("Desasociando Reserva por Concepto");
            }

            log.debug("Objects Desasociados");
            commitTransaction(transaction, session);
            return response;
        } catch (Exception e) {
            rollbackTransaction(transaction, session);
            log.error("Error", e);
            throw new ApplicationExceptionChecked(Exceptions.CCSystemError, Severity.FATAL, e.getMessage());
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Edit an object of a claim
     *
     * @return the response of the action
     */
    public ClientResponse edit(String action, String sourceNode, String classSource, String cruId, String cioId, String cnrId, String parentSource,
                               String damageDcoPk, String damageDcoName) throws ApplicationExceptionChecked {
        log.info(" edit(String action, String sourceNode, String classSource, String cruId, String cioId, String cnrId, String parentSource,\n"
                + "  String damageDcoPk, String damageDcoName");
        ClientResponse response = null;
        Session session = null;
        try {
            session = getHibernateSession();
//            Object userObject = null;
            log.debug("action = " + action);
            log.debug("sourceNode " + sourceNode + " classSource " + classSource);
            log.debug("cruId = " + cruId);
            log.debug("cioId = " + cioId);
            log.debug("parentSource = " + parentSource);
            if (classSource.equals(PolicySystem.CLAIM_CLASS)) {
                response = editClaim(action);
            } else if (classSource.equals(PolicySystem.CLAIM_INSURANCE_OBJECT)) {
                log.debug("parentSource = " + parentSource);
                log.debug("sourceNode = " + sourceNode);
                ClaimInsuranceObject cio = searchClaimInsuranceObject(parentSource, sourceNode);
                if (cio == null) {
                    cio = searchClaimInsuranceObject(parentSource, cioId);
                }
                log.debug("cio = " + cio);
                if ((action != null) && (action.equalsIgnoreCase("validateRequired"))) {
                    response = validateRequired(cio);
                } else {
                    response = editAffectedObject(cio, damageDcoPk, damageDcoName);
                }
            } else if (classSource.equals(PolicySystem.CLAIM_NORMAL_RESERVE)) {
                log.debug("cruId " + cruId + "cioId " + cioId + "cnrId " + cnrId);

                response = editNormalReserve((ClaimNormalReserve) searchClaimReserve(cruId, cioId, cnrId, ReserveType.NORMAL_RESERVE.getValue()));
                if (cruId != null && cioId != null && cnrId != null) {
                    ClaimNormalReserve cnr = (ClaimNormalReserve) searchClaimReserve
                            (cruId, cioId, cnrId, ReserveType.NORMAL_RESERVE.getValue());
                    cnr.load();
                    EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                    response.putAttribute("validationCheck", ec.getConfiguratedCoverageOA().getClaimsCoverageConfiguration().isClaimReOpening());
                    response.putAttribute("claimNormalReserve", cnr);
                    response.putAttribute("validationCheck", ec.getConfiguratedCoverageOA().getClaimsCoverageConfiguration().isClaimReOpening());
                }
            } else if (classSource.equals(PolicySystem.CLAIM_RESERVE_BY_CONCEPT)) {
                log.debug("cruId " + cruId + "cioId " + cioId + "crcId " + cnrId);
                log.debug("editReserveByConcept ");
                response = editReserveByConcept(searchClaimReserve(cruId, cioId, cnrId, ReserveType.CONCEPT_RESERVE.getValue()));
            }

            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());


            response.setCommand(action);
            return response;
        } catch (Exception ex) {
            log.error("Error", ex);
            response = new ClientResponse();
            response.setResult(false);
            return response;
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Update a claim
     *
     * @return the response of the action
     */
    public ClientResponse update(ClientRequest request) throws ApplicationExceptionChecked {
        log.info("update(ClientRequest request)");
        ClientResponse response = new ClientResponse();
        ClientResponse insuranceObjectResponse = new ClientResponse();
        Session session = null;
        Transaction transaction = null;
        List validationError = Arrays.asList(EnumValidationCovClaim.ERROR.getValue(), EnumValidationCovClaim.WARNING.getValue());

        long claimId1 = Long.parseLong((String) request.getAttribute("claimId"));

        String validations = getValidations(claimId1, request, validationError);

        try {
            log.debug("[DFR] update -- try begin");

            session = getHibernateSession();
            transaction = beginTransaction(false, session);
            response.setResult(false);
            String aruId = (String) request.getAttribute("aruId");
            if (StringUtil.isEmptyOrNullValue(aruId)) {
                log.debug("[DFR] update -- aruID esta vacio o es nulo");
                aruId = (String) request.getAttribute("ruID");
            }
            String idSource = (String) request.getAttribute("sourceNode");
            SearchBean searchBean = null;
            List<SearchBean> searchBeanList = (List<SearchBean>) request.getAttribute("searchBeanList");
            if (searchBeanList != null) {
                for (SearchBean sb : searchBeanList) {
                    if (sb.getDesc().equals(idSource)) {
                        searchBean = sb;
                    }
                }
            }
            String classSource = (String) request.getAttribute("classSource");
            Object receivedData = request.getAttribute("requestdata");
            String action = (String) request.getAttribute("action");
            if (action == null) {
                log.debug("[DFR] update -- action es null");
                action = (String) request.getAttribute("command");
            }
            if (classSource == null) {
                log.debug("[DFR] update -- classSource es null");

                classSource = "";
            }

            if (receivedData != null) {
                log.debug("[DFR] update -- receivedData es distinto de null");

                if (classSource.equals(PolicySystem.CLAIM_INSURANCE_OBJECT)) {
                    log.debug("[DFR] update -- classSource es equivalente a Claim_Insurance_Object");
                    String damageTemplatename = (String) request.getAttribute("damageDcoName");
                    String dcoPk = (String) request.getAttribute("damageDcoPk");
                    ClaimInsuranceObject cio = searchClaimInsuranceObject(aruId, idSource);
                    DefaultConfigurableObject damage;
                    if (cio == null) {
                        log.debug("[DFR] update -- cio es null");

                        damage = updateAffectedObject(damageTemplatename, dcoPk, receivedData);
                        insuranceObjectResponse = summitAssociateInsuranceObject(aruId, idSource, damage, damageTemplatename, (String) request.getAttribute("login"), searchBean);
                        AuditTrailManager.generateClaimAuditTrail(new Date(), claim, CustomAuditItem.ADD_AFFECTED_OBJECT, null);
                        try {
                            String recoveryDateOperation = damage.getCriterioInput(AcseleConf.getProperty("recoveryDateOperation"));
                            Date noticeDate = DateUtil.getDateToParse(DateUtil.getISOParseFormated(recoveryDateOperation));

                            AuditTrailManager.generateClaimAuditTrail(noticeDate, claim, CustomAuditItem.CLAIM_CREATION_NOTICE_DATE, null);

                        } catch (ParseException e) {
                            log.warn(
                                    "It couldn't parse NoticeDate when it was creating AuditTrail.  Please check your configuration or enter a valid date to Damage Template.  It'll use sysDate as NoticeDate",
                                    e);
                        }
                    } else {
                        log.debug("[DFR] update -- cio no es null");
                        damage = updateAffectedObject(cio, receivedData);
                        AuditTrailManager.generateClaimAuditTrail(new Date(), claim, CustomAuditItem.EDIT_AFFECTED_OBJECT, null);
                    }

                } else if (classSource.equals(PolicySystem.CLAIM_NORMAL_RESERVE)) {
                    log.debug("[DFR] update -- classSource es claim_normal_reserve");
                    String aioId = (String) request.getAttribute("ioID");

                    ClaimNormalReserve cnr = (ClaimNormalReserve) searchClaimReserve(aruId, aioId, idSource, ReserveType.NORMAL_RESERVE.getValue());
                    cnr.load();
                    EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                    Plan plan = ec.getPlan();
                    String planId = plan.getPk();
                    long configuratedCoverage = ec.getConfiguratedCoverageOA().getId();
                    updateReserve(cnr, receivedData, planId, configuratedCoverage, ec, validations);

                } else if (classSource.equals(PolicySystem.CLAIM_RESERVE_BY_CONCEPT)) {
                    log.debug("[DFR] update -- classSource claim_reserve_by_concept");
                    String aioId = (String) request.getAttribute("ioID");
                    ClaimReserveByConcept crc = (ClaimReserveByConcept) searchClaimReserve(aruId, aioId, idSource, ReserveType.CONCEPT_RESERVE.getValue());
                    List<ClaimNormalReserve> claimNormalReserves;
                    EvaluatedCoverage ec;
                    if (crc != null) {
                        crc.load();
                        claimNormalReserves = crc.getContainer().getClaimNormalReserveList();
                        ec = claimNormalReserves.get(0).getEvaluatedCoverage();
                        updateReserve(crc, receivedData, null, 0, claimNormalReserves.size() < 0 ? null : ec, validations);
                    }

                } else if (classSource.equals(PolicySystem.CLAIM_CLASS)) {
                    log.debug("[DFR] update -- classSource es claim_class");
                    updateClaim(action, receivedData);
                }
                response.setResult(true);
                response.setCommand(action);
                response.putAttribute("claimNumber", this.claim.getClaimNumber());
                findClaim(String.valueOf(claimId));
                response.putAttribute("claimstate", this.claim.getClaimStatus().getValue());

                log.debug("[DFR] update -- Estado del Claim: " + claim.getClaimStatus().toString());
            }
            if (insuranceObjectResponse.getAttribute("DamageErrorValidations") != null && (Boolean) insuranceObjectResponse.getAttribute("DamageErrorValidations")) {
                rollbackTransaction(transaction, session);
                response.setResult(false);
                response.putAttribute("ClaimDamageValidations", insuranceObjectResponse.getAttribute("ClaimDamageValidations"));
                response.putAttribute("PropertyValidationToClean", insuranceObjectResponse.getAttribute("PropertyValidationToClean"));
                log.error("Existen validaciones de tipo error que se activaron");
                return response;
            } else {
                commitTransaction(transaction, session);
                log.debug("[DFR] update -- try begin");
            }
            try {
                HibernateUtil.flush();
            } catch (Exception e) {
                log.warn("Already flushed!");
            }
            return response;
        } catch (Exception e) {
            rollbackTransaction(transaction, session);
            response.setResult(false);
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * @param sourceNode
     * @param policyId
     * @param ruID
     * @param ioID
     * @param typeReserve
     * @param claimReserveId
     * @return ClientResponse
     * @throws ApplicationExceptionChecked
     */
    public ClientResponse getPaymentsOrders(String sourceNode, String policyId, String ruID, String ioID, String typeReserve,
                                            String claimReserveId, boolean allParticipation) throws ApplicationExceptionChecked {
        log.info("getPaymentsOrders(String sourceNode, String policyId, String ruID, String ioID, String typeReserve,\n"
                + "                                            String claimReserveId)");
        Session session = null;
        AgregatedInsuranceObject aio;
        AgregatedRiskUnit aru;
        EvaluatedCoverage evaluatedCoverage;
        try {
            session = getHibernateSession(); session.clear();
            ClientResponse response = new ClientResponse();
            ClaimReserve reserve = searchClaimReserve(ruID, ioID, claimReserveId, Integer.parseInt(typeReserve));
            if (reserve instanceof ClaimReserveByConcept || (mandatoryRequisitesReceived() && ((ClaimNormalReserve) reserve)
                    .mandatoryRequisitesReceived())) {
                Collection paymentOrders = new Vector();
                ArrayList collection = new ArrayList(reserve.getPaymentOrderList().values());
                Collections.sort(collection);
                Iterator payments = collection.iterator();

                PaymentOrderCollection paymentOrderCollection = new PaymentOrderCollection(reserve, paymentOrders);
                log.debug("[983] paymentOrderCollection.getPaymentOrders().size() -> " + paymentOrderCollection.getPaymentOrders().size());
                paymentOrderCollection.setPolicyId(policy.getPK());
                paymentOrderCollection.setProduct(policy.getProduct().getDesc());
                if (reserve instanceof ClaimNormalReserve) {
                    log.debug("Es una Reserva Normal");
                    ClaimNormalReserve cnr = (ClaimNormalReserve) reserve;
                    collection.clear();
                    paymentOrderCollection.setBenefitPayments(0);
                    List<ClaimNormalReserve> listCNR = cnr.getListClaimNormalReserveByCoverage(cnr.getEvaluatedCoverage().getPk(), cnr.getContainer().getPk());
                    for (ClaimNormalReserve cnrFor : listCNR) {
                        paymentOrderCollection.setReserveAmount(paymentOrderCollection.getReserveAmount() + cnrFor.getAmountWithDeductible());
                        paymentOrderCollection.setPenaltyPercentage(paymentOrderCollection.getPenaltyPercentage() + cnrFor.getPenaltyPercentage());
                        paymentOrderCollection.setBenefitsAmount(paymentOrderCollection.getBenefitsAmount() + cnrFor.getBenefitsAmount());
                        paymentOrderCollection.setBenefitPayments(paymentOrderCollection.getBenefitPayments() + cnrFor.getBenefitPayments());
                        paymentOrderCollection.setBenefitPeriod(paymentOrderCollection.getBenefitPeriod() + cnrFor.getBenefitPeriod());
                        paymentOrderCollection.setMaxBenefitAmount(paymentOrderCollection.getMaxBenefitAmount() + cnrFor.getMaxBenefitAmount());
                        collection.addAll(new ArrayList(cnrFor.getPaymentOrderList().values()));
                    }
                    paymentOrderCollection.setDeductibleAmount(0);
                    paymentOrderCollection.setRefundPercentage(cnr.getRefundPercentage());
                    paymentOrderCollection.setDeductiblePercentage(0);
                    paymentOrderCollection.setScheduledPayment(cnr.getBenefitPayments() > 0);
                    ClaimsCoverageConfiguration conf = getClaimCoverageConfiguration(ruID, ioID, cnr.getEvaluatedCoverage());
                    Collections.sort(collection);
                    payments = collection.iterator();
                    if (conf == null) {
                        paymentOrderCollection.setMaxTotalAmount(0);
                    } else {
                        int maxNumberBenefit;
                        double maxAmount;
                        if (StringUtil.isEmptyOrNullValue(conf.getMaxBenefitPayment()) || StringUtil.isEmptyOrNullValue(conf.getMaxAmount())) {
                            paymentOrderCollection.setMaxTotalAmount(0);
                        } else {

                            TablaSimbolos symbolsTable = new TablaSimbolos();
                            maxAmount = this.evaluate(conf.getMaxAmount(), symbolsTable, null);

                            maxNumberBenefit = (int) this.evaluate(conf.getMaxBenefitPayment(), symbolsTable, null);

                            double maxTotal = maxAmount * maxNumberBenefit;
                            paymentOrderCollection.setMaxTotalAmount(maxTotal);
                        }

                    }
                    //    ClaimInsuranceObject cio = cnr.getContainer();
                    evaluatedCoverage = cnr.getEvaluatedCoverage();
                    aio = evaluatedCoverage.getAgregatedInsuranceObject();
                    aru = aio.getAgregatedRiskUnit();


                    try {
                        String insuranceAmountStr = aio.getDCO().getCriterioValue(AcseleConf.getProperty("insuranceAmount"));
                        if (insuranceAmountStr == null) {
                            // TODO: 2006-04-14 (GS) Correct this to search the Coverage by its Pk.
                            //  if(evaluatedCoverage==null){
                            //    evaluatedCoverage = aio.getEvaluatedCoverageByDesc(cnr.getDesc());
                            // }
                            insuranceAmountStr = evaluatedCoverage.getDCO().getCriterioValue(AcseleConf.getProperty("insuranceAmount"));
                        }
                        paymentOrderCollection.setInsuranceAmount(Double.parseDouble(insuranceAmountStr));
                    } catch (Exception e) {
                        paymentOrderCollection.setInsuranceAmount(0);
                        log.error("No se encontro el valor de la suma asegurada");
                    }
                    paymentOrderCollection.setProduct(policy.getProduct().getDesc());
                    paymentOrderCollection.setDescCoverage(reserve.getDesc());  //LL
                    try {
                        List clients = checkINDBeneficiaryPreferential(aru);
                        if (!clients.isEmpty()) {
                            response.putAttribute("clientsPolicy", clients);
                        }
                    } catch (Exception ex) {
                        log.error("Error", ex);
                    }
                    Collection participationBeneficarys = new ArrayList();
                    try {
                        //    participationBeneficarys = getBeneficarys(ruID, ioID);
                        participationBeneficarys = evaluatedCoverage.getParticipationByRole(RoleGroup.BENEFICIARY_ROLES.getRoleList());
                    } catch (Exception e) {
                        log.debug(e);
                    }
                    Collection policyThirdParties = new Vector(1);
                    try {
                        if (allParticipation) {
                            policyThirdParties = this.getPolicyThirdParties(ioID, cnr.getDesc());

                        }
                    } catch (Exception e) {
                        log.error("Error in getPolicyThirdParties", e);
                    }
                    Collection coverageThirdParties = new HashSet(1);
                    try {
                        coverageThirdParties = this.getCoverageThirdParties(evaluatedCoverage);
                    } catch (Exception e) {
                        log.error("Error in getCoverageThirdParties", e);
                    }

                    Collection coverageThirdPartiesWithRole = new HashSet(1);
                    try {
                        if (allParticipation) {


                            //   evaluatedCoverage.getParticipationCollection() //alternativa a la linea de abajo
                            coverageThirdPartiesWithRole = this.getCoverageThirdPartiesWithRole(evaluatedCoverage.getPk());
                        }
                        //coverageThirdPartiesWithRole = this.getCoverageThirdPartiesWithRole(evaluatedCoverage.getPk());
                    } catch (Exception e) {
                        log.error("Error in getCoverageThirdPartiesWithRole", e);
                    }
                    coverageThirdParties.addAll(coverageThirdPartiesWithRole);
                    Collection preferentialThirdParties = new HashSet(1);
                    try {
                        preferentialThirdParties = this.getPreferentialThirdParties(evaluatedCoverage);
                    } catch (Exception e) {
                        log.error("Error in getPreferentialThirdParties", e);
                    }

                    response.putAttribute("policyThirdParties", policyThirdParties);
                    response.putAttribute("coverageThirdParties", coverageThirdParties);
                    response.putAttribute("preferentialThirdParties", preferentialThirdParties);

                    if (!participationBeneficarys.isEmpty()) {
                        try {
                            response.putAttribute("beneficiarys", participationBeneficarys);
                        } catch (Exception ex) {
                            log.error("Error", ex);
                        }
                    }
                } else {
                    Collection preferentialThirdParties = new HashSet(1);
                    Collection policyThirdParties = new Vector(1);
                    Collection coverageThirdParties = new HashSet(1);
                    response.putAttribute("policyThirdParties", policyThirdParties);
                    response.putAttribute("coverageThirdParties", coverageThirdParties);
                    response.putAttribute("preferentialThirdParties", preferentialThirdParties);
                    paymentOrderCollection.setReserveAmount(reserve.getAmount());
                    paymentOrderCollection.setBenefitsAmount(reserve.getAmount());
                }
                log.debug("Antes de entrar en while payments.hasMoreElements()");
                log.debug("[1129] paymentOrderCollection.getPaymentOrders().size() -> " + paymentOrderCollection.getPaymentOrders().size());

                int i = 0;
                while (payments.hasNext()) {
                    log.debug("i -> " + i++);
                    PaymentOrder paymentOrder = (PaymentOrder) payments.next();
                    paymentOrders.add(paymentOrder);

                    if (paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.CANCELED_STATE) {
                        paymentOrderCollection.addToMaxObligation(calculateMaxObligationForPaymentOrder(paymentOrder));
                    }
                }
                List<PaymentOrder> data = new ArrayList<PaymentOrder>(paymentOrderCollection.getPaymentOrders());
                Collections.sort(data, new ComparatorDate());
                paymentOrderCollection.setPaymentOrders(data);
                log.debug("paymentOrderCollection.getPaymentOrders().size() -> " + paymentOrderCollection.getPaymentOrders().size());
                response.putAttribute("paymentOrders", paymentOrderCollection);
                response.setCommand("getReservePayments");
                response.setResult(true);
            } else {
                log.debug(" Requisitos incompletos ...");
                response.setResult(false);
                throw new ApplicationExceptionChecked(Exceptions.CCErrorRequisitesNotReceived, Severity.FATAL);
            }
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            return response;
        } catch (ApplicationExceptionChecked aec) {
            log.error("Error", aec);
            throw aec;
        } catch (Exception e) {
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Update the payments contained in the request parameter.
     *
     * @param request             Object with the client data
     * @param automaticCloseValue It specifies the reason of closing a Claim when a total payment
     *                            must be performed
     * @return A response object with the data required by the client
     * @throws ApplicationExceptionChecked
     */
    public ClientResponse updatePaymentsOrders(ClientRequest request, String automaticCloseValue) throws ApplicationExceptionChecked {
        log.info("updatePaymentsOrders(ClientRequest request, String automaticCloseValue)");
        ClientResponse response = new ClientResponse();
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);

            boolean totalPayment;
            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) request.getAttribute("paymentOrderCollection");
            boolean fromPaymentEdition = request.getAttribute("fromEditPayments") != null ?
                    Boolean.valueOf((String) request.getAttribute("fromEditPayments")) : false;
            String ruID = (String) request.getAttribute("ruID");
            String ioID = (String) request.getAttribute("ioID");
            String typeReserve = (String) request.getAttribute("typeReserve");
            String claimReserveId = (String) request.getAttribute("claimReserveId");
            String doneby = (String) request.getAttribute("login");
            ClaimReserve reserve = searchClaimReserve(ruID, ioID, claimReserveId, Integer.parseInt(typeReserve));
            if (Integer.parseInt(typeReserve) == ReserveType.NORMAL_RESERVE.getValue() && ((ClaimNormalReserve) reserve).isDistributed()) {
                reserve = searchClaimReserve(ruID, ioID, paymentOrderCollection.getClaimReserve().getPK(), Integer.parseInt(typeReserve));
            }


            if (paymentOrderCollection != null) {
                Collection paymentOrders = paymentOrderCollection.getPaymentOrders();
                Collection validsPayments = new ArrayList();
                Collection invalidPayments = new ArrayList();
                for (Iterator iterator = paymentOrders.iterator(); iterator.hasNext(); ) {
                    PaymentOrder paymentOrder = (PaymentOrder) iterator.next();
                    if ((paymentOrder.getAmount() != null) && (paymentOrder.getAmount() != 0.0f)) {
                        if (reserve instanceof ClaimNormalReserve) {
                            ClaimNormalReserve claimReserve = (ClaimNormalReserve) paymentOrder.getClaimReserve();
                            double deducedAmount = deducedRelatedLifeCoverage(claimReserve, paymentOrder.getAmount(), response);
                            log.debug("Deduced amount by coverage message: " + response.getMessage() + ", deducedAmount: " + deducedAmount + ". Amount with deductible: " + paymentOrder.getAmount() + " payment amount: " + paymentOrder.getAmount());
                            if (response.getMessage() == null) {
                                if (!paymentOrder.isEditedDeductible()) {
                                    double paymentAmount = paymentOrder.getAmount() - deducedAmount;
                                    paymentOrder.setAmount(paymentAmount);
                                }
                                if (paymentOrder.getAmount() != 0.0f) {

                                    validsPayments.add(paymentOrder);
                                }
                            } else {
                                return response;
                            }
                        } else if (reserve instanceof ClaimReserveByConcept) {
                            ClaimReserveByConcept claimReserve = (ClaimReserveByConcept) reserve;
                            validsPayments.add(paymentOrder);
                        }
                    } else if (fromPaymentEdition) {
                        invalidPayments.add(paymentOrder);
                    }
                }

                log.debug("+++++ Keep Going...");
                log.debug("paymentsValids = " + validsPayments);
                log.debug("paymentsValids.size() = " + validsPayments.size());
                log.debug("invalidPayments = " + invalidPayments.size());

                if (fromPaymentEdition) {
                    response.putAttribute("invalidPayments", invalidPayments);
                }
                paymentOrders = validsPayments;
                paymentOrderCollection.setPaymentOrders(paymentOrders);

                int reserveType = Integer.parseInt(typeReserve);
                totalPayment = totalPaymentCurrent(paymentOrderCollection, reserve, doneby);

                Iterator claimRUList = claim.getClaimRiskUnitsList().iterator();
                //TODO we have to check this diffAmount when there are payment orders canceled
                double diffAmount = fillPaymentList(reserve.getPaymentOrderList(), paymentOrders, reserve, Long.valueOf(claim.getPk()));
                if (reserve instanceof ClaimNormalReserve) {
                    ClaimNormalReserve cnr = (ClaimNormalReserve) reserve;
                    cnr.loadAdjust();
                    automaticRestitution(cnr);
                    if (!ClaimComposerWrapper.CLAIM_REINSURANCE_CESSIONS_GENERATED.equals(AcseleConf.getProperty("claim.reinsurance.cessions.generated"))) {
                        generateReserveReinsuranceDistribution(cnr, String.valueOf(diffAmount), Constants.PAYMENT_CLAIM, new java.sql.Date(cnr.getDate().getTime()), null);
                    }

                } else if (reserve instanceof ClaimReserveByConcept) {
                    boolean distribution = false;
                    reserve.update();
                    ClaimInsuranceObject claimInsuranceObject = ((ClaimReserveByConcept) reserve).getContainer();
                    java.sql.Date today = new java.sql.Date(reserve.getDate().getTime());
                    AgregatedInsuranceObject aio = claimInsuranceObject.getAgregatedInsuranceObject();
                    Long productId = aio.getAgregatedPolicy().getProduct().getId();
                    Long normalReserveId = ((ClaimReserveByConcept) reserve).getNormalReserveId();
                    ClaimNormalReserve cnr = ClaimNormalReserve.load(String.valueOf(normalReserveId.longValue()));
                    if (cnr != null) {
                        EvaluatedCoverage evaluatedCoverage = cnr.getEvaluatedCoverage();
                        String ramoColumn = evaluatedCoverage.getDCO().getCriterioInput(AcseleConf.getProperty("ramoColumn"));
                        AccessContractsImpl acessContractImpl = new AccessContractsImpl();
                        String reinsuranceGroupId = acessContractImpl.getReinsuranceGroupByCode(ramoColumn);
                        String contractId = acessContractImpl.getPublishedContractByGroupAndDate(reinsuranceGroupId, today, productId);
                        ReinsuranceContract reCon = ReinsuranceContract.load(contractId);
                        if (reCon != null) {
                            distribution = reCon.isExpensesDistribution();
                        }
                    }
                    if (distribution) {
                        if (!ClaimComposerWrapper.CLAIM_REINSURANCE_CESSIONS_GENERATED.equals(AcseleConf.getProperty("claim.reinsurance.cessions.generated"))) {
                            generateReserveReinsuranceDistribution(reserve, String.valueOf(diffAmount), Constants.PAYMENT_CLAIM, today, null);
                        }
                    }
                    //generateConceptReserveReinsuranceDistribution((ClaimReserveByConcept) reserve, diffAmount, Constants.PAYMENT_CLAIM);
                }
                if (paymentOrderCollection.isScheduledPayment() && paymentOrderCollection.getPaymentOrders().size() == paymentOrderCollection.getBenefitPayments()) {
                    try {
                        if (reserve instanceof ClaimNormalReserve) {
                            ClaimNormalReserve cnr = (ClaimNormalReserve) reserve;
                            double amountEntry = cnr.getAmountWithDeductible() - cnr.getAmountPaymentsforReserve();
                            EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                            String planId = ec.getPlan().getPk();
                            long configuratedCoverage = ec.getConfiguratedCoverageOA().getId();
                            entryReserve(amountEntry, planId, configuratedCoverage, ReserveAdjustType.DECREASE.getValue(), ec, cnr.getDoneBy(), cnr.getCurrency().getIsoCode(), cnr);
                            ClaimReserveAdjust claimReserveAdjust = reserve.freeReserve(ClaimReserveAdjust.AUTOMATIC_ADJUST);
                            generateReserveReinsuranceDistribution(reserve, String.valueOf(claimReserveAdjust.getAmount() * -1), Constants.RESERVE_CLAIM, new java.sql.Date(reserve.getDate().getTime()), ReserveAdjustType.DECREASE);
                        }
                    } catch (Exception ex) {
                        log.debug("The reserve amount is already 0");
                    }
                }
                log.debug("ClaimComposerEJB.updatePaymentOrders: totalPayment = " + totalPayment);
                log.debug("ClaimComposerEJB.updatePaymentOrders: paymentOrderCollection.isScheduledPayment() = " + paymentOrderCollection.isScheduledPayment());

                response.setResult(true);
            }

            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            response.setCommand("updateClaimPayment");

            commitTransaction(transaction, session);

            return response;
        } catch (Exception e) {
            log.error("Error", e);
            rollbackTransaction(transaction, session);
            response.setResult(false);
            response.putAttribute("error", e.getMessage() == null ? "" : e.getMessage());
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Reverse update PaymentsOrder Reverse Coverage
     *
     * @param paymentsNormal
     * @param request
     * @return ClientResponse
     * @throws ApplicationExceptionChecked
     */
    public ClientResponse updatePaymentsOrdersReverseCoverage(Map paymentsNormal) throws ApplicationExceptionChecked {
        log.debug("updatePaymentsOrdersReverseCoverage(Hashtable paymentsNormal,HttpServletRequest request)");
        ClientResponse response = new ClientResponse();
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);
            if (paymentsNormal != null) {
                log.debug("UserInfo.getCurrentInstance() : " + UserInfo.getCurrentInstance());
                Collection payments = paymentsNormal.values();

                for (Iterator iterator = payments.iterator(); iterator.hasNext(); ) {
                    Payment payment = (Payment) iterator.next();
                    PaymentOrder paymentOrder = payment.getPaymentOrder();

                 /*Acsele-27878 - OpenItem is generated by reinsurance claims payments and hedging concept*/
                    ClaimReserve cnr = (ClaimReserve) paymentOrder.getClaimReserve();

                    generateReserveReinsuranceDistributionReverse(cnr, String.valueOf(paymentOrder.getAmountWithDeductible()),
                            Constants.PAYMENT_CLAIM, ReserveAdjustType.INCREASE);
                }
                response.setResult(true);
            }
            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            commitTransaction(transaction, session);
            return response;
        } catch (Exception e) {
            log.error("Error: ", e);
            rollbackTransaction(transaction, session);
            response.setResult(false);
            response.putAttribute("error", e.getMessage() != null ? e.getMessage() : "");
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * This methods performs the tasks required for a TotalPayment-checked Payment Order
     *
     * @param claimRUList
     * @param reserve
     * @param doneby
     * @param request
     * @param reserveType
     * @param automaticCloseValue
     * @throws Exception
     */
    private void processTotalPayment(Iterator claimRUList, ClaimReserve reserve, String doneby, ClientRequest request, int reserveType,
                                     String automaticCloseValue, String ruID, String ioID) throws Exception {
        log.info("processTotalPayment(Iterator claimRUList, ClaimReserve reserve, String doneby, ClientRequest request, int reserveType,\n"
                + "                                     String automaticCloseValue, String ruID, String ioID)");
        ClaimInsuranceObject cio;
        Iterator paymentsReserve;
        while (claimRUList.hasNext()) {
            ClaimRiskUnit ru = (ClaimRiskUnit) claimRUList.next();
            Iterator io = ru.getClaimInsuranceObjects().values().iterator();
            while (io.hasNext()) { //the insurance object list
                cio = (ClaimInsuranceObject) io.next();
                cio.load();
                //Only the payment orders approved can be paid
                cio.approveAllPaymentOrders();
                paymentsReserve = cio.getNormalReserves().values().iterator();
                while (paymentsReserve.hasNext()) {  //the reserve payments list
                    ClaimNormalReserve cnr = (ClaimNormalReserve) paymentsReserve.next();
                    if (!cnr.getPK().equals(reserve.getPK())) {
                        totalPaymentOthers(cnr, doneby);
                    }
                }
            }
        }
        ClientResponse resp = this.accountPayments(this.claim.getPK(), doneby, false);

        if (resp.getAttribute("claimTotalize") instanceof ClaimTotalizeOneByOne) {
            ClaimTotalizeOneByOne claimTotalize = (ClaimTotalizeOneByOne) resp.getAttribute("claimTotalize");

            Vector conceptPendingPaymnts = claimTotalize.getConceptReservePendingPayments();
            Vector normalReservePendingPayments = claimTotalize.getNormalReservePendingPayments();
            this.setPaidAmountToPaymentsInVector(conceptPendingPaymnts, reserve.getCurrency());
            this.setPaidAmountToPaymentsInVector(normalReservePendingPayments, reserve.getCurrency());
            claimTotalize.setPendingPaymentOrders(reserve.hasPendingPaymentOrders());
            this.applyPayments(claimTotalize, false, ruID, ioID);
        } else {
            ClaimTotalize claimTotalize = (ClaimTotalize) resp.getAttribute("claimTotalize");

            Hashtable conceptPendingPaymnts = claimTotalize.getConceptReservePendingPayments();
            Hashtable normalReservePendingPayments = claimTotalize.getNormalReservePendingPayments();
            this.setPaidAmountToPaymentsInHashtable(conceptPendingPaymnts, reserve.getCurrency());
            this.setPaidAmountToPaymentsInHashtable(normalReservePendingPayments, reserve.getCurrency());
            claimTotalize.setPendingPaymentOrders(reserve.hasPendingPaymentOrders());
            this.applyPayments(claimTotalize, false, ruID, ioID);
        }


        String templateName = AcseleConf.getProperty(TEMPLATE_CLAIM_CLOSED);
        if (reserveType == ReserveType.NORMAL_RESERVE.getValue()) {
            setClosedState(templateName, automaticCloseValue);
        }
    }

    /**
     * sets the Claim State to Closed automatically
     *
     * @param templateName
     * @param automaticCloseValue
     * @throws Exception
     */
    private void setClosedState(String templateName, String automaticCloseValue) throws Exception {
        log.info("setClosedState(String templateName, String automaticCloseValue)");
        ConfigurableObjectType cot = this.getConfigurableObjectType(CotType.OTHER, templateName);
        DefaultConfigurableObject dco = DefaultConfigurableObject.create(cot);
        dco.setCriterioInput(AcseleConf.getProperty("propertyautomaticclose"), automaticCloseValue);
        dco.setCriterioInput(AcseleConf.getProperty("propertyautomaticclosedate"), DateUtil.getDateToShow(Calendar.getInstance().getTime()));
        dco.save();
        ClaimStateBean bean = new ClaimStateBean();
        bean.setClaimId(this.claim.getPK());
        bean.setClaimStatus(ClaimStatus.CLOSED);
        bean.setDco(dco);
        bean.setOperationdate(new java.sql.Date(new Date().getTime()));
        this.setStateWithDCOInformation(bean, false);
    }

    /**
     * Adds requeriments to a claim
     *
     * @return the response of the action
     */
    public ClientResponse addRequeriments(String reqDesc) throws ApplicationExceptionChecked {
        log.info("addRequeriments(String reqDesc)");
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);
            ClientResponse response = new ClientResponse();
            Collection IoIDs = getClaimsInsuranceObjectDesc(); //Se Cargan los DESC de los IOs
            Hashtable reqAdds = new Hashtable();
            for (Iterator iterator = IoIDs.iterator(); iterator.hasNext(); ) {
                String ioID = (String) iterator.next();
                Plan plan = (Plan) policy.getProduct().get(Tools.getStringInId(ioID, "(", ")"));
                Enumeration requeriments = plan.getRequisiteTypes();
                while (requeriments.hasMoreElements()) {
                    RequisiteType requeriment = (RequisiteType) requeriments.nextElement();
                    if (requeriment != null) {
                        if (!reqAdds.containsKey(requeriment.getDesc())) {
                            reqAdds.put(requeriment.getDesc(), requeriment);
                        }
                    }
                }
            }

            Object userObject1;
            RequisiteType myRequisite;
            userObject1 = reqAdds.get(reqDesc);
            if (userObject1 instanceof RequisiteType) {
                myRequisite = (RequisiteType) userObject1;
                processAddRequisite(myRequisite);
            }

            response.setCommand("addRequeriments");
            String res = "added";

            response.putAttribute("responsedata", res);
            response.setResult(true);

            commitTransaction(transaction, session);
            return response;
        } catch (Exception e) {
            log.error("Error", e);
            rollbackTransaction(transaction, session);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Checks the requisites of a claim.
     *
     * @return the response of the action
     */
    public ClientResponse checkRequisites(String claimID) throws ApplicationExceptionChecked {
        log.info("checkRequisites(String claimID");
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);
            ClientResponse response = new ClientResponse();
            log.debug("(Claim Entity EJB) Entering check Requisites!!!");
            log.debug("ClaimRequisite claimID " + claimID);
            Vector<ClaimRequisitesForm> data = new Vector<ClaimRequisitesForm>();
            Date date = new Date();
            for (ClaimRequisite requisite : claim.getRequisiteCheckList()) {
                String symbol = requisite.getDescription();
                PropiedadImpl property = PropiedadImpl.getInstanceTemplate(symbol);
                ClaimRequisitesForm bean = new ClaimRequisitesForm(requisite.isMandatory(),
                        requisite.getDescription(),
                        requisite.isChecked());
                if (property == null) {
                    bean.setLabel(requisite.getDescription());
                } else {
                    bean.setLabel(property.getEtiqueta());
                }
                bean.setIdRequisite(String.valueOf(requisite.getPk()));
                String doneBy = (requisite.getDoneBy() != null) ? requisite.getDoneBy() : "";
                if (requisite.getDate() != null) {
                    date = requisite.getDate();
                }
                bean.setDoneBy(doneBy);
                bean.setDate(DateUtil.getDateToShow(date));
                data.addElement(bean);
            }
            response.putAttribute("responsedata", data);
            response.setCommand("checkRequisites");
            response.setResult(true);
            commitTransaction(transaction, session);
            return response;
        } catch (Exception e) {
            rollbackTransaction(transaction, session);
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Returns all the requisites as ClaimRequisiteBean objects
     *
     * @return a vector
     */
    public Vector getRequisites() {
        Session session = null;
        Vector claimRequisiteBeans = new Vector();
        try {
            session = getHibernateSession();
            Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
            for (ClaimRiskUnit claimRiskUnit : claimRiskUnits) {
                Iterator insuranceObjects = claimRiskUnit.getClaimInsuranceObjects().values().iterator();
                while (insuranceObjects.hasNext()) {
                    ClaimInsuranceObject claimInsuranceObject = (ClaimInsuranceObject) insuranceObjects.next();
                    Iterator normalReserves = claimInsuranceObject.getNormalReserves().
                            values().iterator();
                    while (normalReserves.hasNext()) {
                        ClaimNormalReserve normalReserve = (ClaimNormalReserve) normalReserves.next();
                        claimRequisiteBeans.addAll(normalReserve.getClaimRequisitesAsBeans());

                    }
                }
            }
        } catch (Exception e) {
            log.error("Error", e);
        } finally {
            closeHibernateSession(session);
        }
        return claimRequisiteBeans;
    }

    /**
     * Returns the requisites by IdSelected as ClaimRequisiteBean objects
     *
     * @return a vector
     */
    public Vector getRequisitesByIdReserve(String coverageID) {
        Session session = null;
        Vector claimRequisiteBeans = new Vector();
        try {
            session = getHibernateSession();
            Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
            for (ClaimRiskUnit claimRiskUnit : claimRiskUnits) {
                Iterator insuranceObjects = claimRiskUnit.getClaimInsuranceObjects().values().iterator();
                while (insuranceObjects.hasNext()) {
                    ClaimInsuranceObject claimInsuranceObject = (ClaimInsuranceObject) insuranceObjects.next();
                    Iterator normalReserves = claimInsuranceObject.getNormalReserves().
                            values().iterator();
                    while (normalReserves.hasNext()) {
                        ClaimNormalReserve normalReserve = (ClaimNormalReserve) normalReserves.next();
                        if (normalReserve.getPK().equals(coverageID)) {
                            claimRequisiteBeans.addAll(normalReserve.getClaimRequisitesAsBeans());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error", e);
        } finally {
            closeHibernateSession(session);
        }
        return claimRequisiteBeans;
    }

    /**
     * Update the requeriments of a claim
     *
     * @return the response of the action
     */
    public ClientResponse updateRequeriments(Object receivedData) throws ApplicationExceptionChecked {
        Session session = null;
        Transaction transaction = null;
        log.debug("[updateRequeriments] " + receivedData);
        ClientResponse response = new ClientResponse();
        response.setCommand("updateRequeriments");
        response.setResult(false);
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);
            if (receivedData != null) {
                response.setResult(true);
            }
            commitTransaction(transaction, session);
        } catch (Exception e) {
            rollbackTransaction(transaction, session);
            log.error("Error", e);
        } finally {
            closeHibernateSession(session);
        }
        return response;
    }

    /**
     * @param requisiteBeans
     */
    public void updateRequeriments(Vector requisiteBeans, boolean fromParser) {
        Session session = null;
        Transaction transaction = null;
        Iterator it;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(false, session);
            ClaimRequisitesForm bean;
            ClaimRequisite claimRequisite;
            for (it = requisiteBeans.iterator(); it.hasNext(); ) {
                bean = (ClaimRequisitesForm) it.next();
                log.debug("bean = " + bean);
                claimRequisite = (ClaimRequisite) HibernateUtil.load(ClaimRequisite.class, Long.valueOf(bean.getIdRequisite()));
                claimRequisite.setMandatory(bean.isMandatory());
                try {
                    claimRequisite.setDate(DateUtil.getFormatToShow().parse(bean.getDate()));
                } catch (ParseException ex) {
                    log.debug("Something wrong parsing requisite date. Setting system date...");
                    claimRequisite.setDate(new Date());
                }
                if (bean.isChecked() == claimRequisite.isChecked()) {
                    continue;
                }
                claimRequisite.setChecked(bean.isChecked());
                claimRequisite.setDoneBy(bean.getDoneBy());

                if (fromParser) {
                    claimRequisite.setClaim(claim);
                    //  claimRequisite.update();
                }

            }
            commitTransaction(transaction, session);
        } catch (Exception e) {
            rollbackTransaction(transaction, session);
            log.error("Error", e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Gets the payments by beneficiary
     *
     * @param claimID the object identifier
     * @return the response with the payments
     */
    public ClientResponse accountPayments(String claimID, String doneby, boolean openTx) throws ApplicationExceptionChecked {
        ClientResponse response = new ClientResponse();
        Session session = null;
        Transaction transaction = null;
        try {
            if (openTx) {
                session = getHibernateSession();
                transaction = beginTransaction(true, session);
            }
            ClaimTotalize claimTotalize = new ClaimTotalize(claimID);
            Iterator paymentsReserveConcepts;
            Iterator paymentsReserve;
            ClaimInsuranceObject cio;
            double totalOrders = 0.0f;
            Map<String, Currency> currenciesList = new HashMap<String, Currency>();
            CurrencyList currencies = CurrencyList.Impl.loadAll();
            for (Currency c : currencies) {
                currenciesList.put(String.valueOf(c.getId()), c);
            }

            try {
                loadPaymentInMapGroupByBeneficiary(ReserveType.NORMAL_RESERVE.getValue(), claim.getPaymentsNormalReserveList(), claimTotalize);
                loadPaymentInMapGroupByBeneficiary(ReserveType.CONCEPT_RESERVE.getValue(), claim.getPaymentsConceptReserveList(), claimTotalize);

            } catch (Exception e) {
                log.error("There was an error associating the payments to the beneficiaries: ", e);
                throw new TechnicalException("Exception loading currencies ", Severity.FATAL, e);
            }
            Map<String, Map<String, String>> currenciesExchangeRates = new HashMap<String, Map<String, String>>();
            claimTotalize.setCurrencyList(currenciesList);
            claimTotalize.setCurrenciesRates(currenciesExchangeRates);
            log.debug("\nCurrency List ->" + currenciesList);
            Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
            for (ClaimRiskUnit ru : claimRiskUnits) {
                Iterator io = ru.getClaimInsuranceObjects().values().iterator();
                while (io.hasNext()) {
                    cio = (ClaimInsuranceObject) io.next();
                    cio = (ClaimInsuranceObject) HibernateUtil.load(ClaimInsuranceObject.class, cio.getPk());
                    cio.load();
                    paymentsReserve = cio.getNormalReserves().values().iterator();
                    if (claimTotalize.getNormalReservePendingPayments() == null) {
                        claimTotalize.setNormalReservePendingPayments(new Hashtable());
                    }
                    while (paymentsReserve.hasNext()) {
                        ClaimNormalReserve cnr = (ClaimNormalReserve) paymentsReserve.next();
                        cnr = (ClaimNormalReserve) HibernateUtil.load(ClaimNormalReserve.class, cnr.getPk());
                        cnr.load();
                        if (!claimTotalize.areTherePendingPaymentOrders()) {
                            claimTotalize.setPendingPaymentOrders(cnr.hasPendingPaymentOrders());
                        }

                        totalOrders += accountReservePaymentsOrders(cnr, claimTotalize.getNormalReservePendingPayments(), ru, doneby);

                        if (cnr.getCurrency() != null) {
                            log.debug("Currency Id: " + cnr.getCurrency().getId());
                            log.debug("Currency Desc:" + cnr.getCurrency().getDescription());
                        }
                        loadCurrencyExchangeRates(cnr.getCurrency(), currenciesList, currenciesExchangeRates);
                    }
                    paymentsReserveConcepts = cio.getReservesByConcept().values().iterator();
                    if (claimTotalize.getConceptReservePendingPayments() == null) {
                        claimTotalize.setConceptReservePendingPayments(new Hashtable());
                    }
                    while (paymentsReserveConcepts.hasNext()) {

                        ClaimReserveByConcept claimconcept = (ClaimReserveByConcept) paymentsReserveConcepts.next();
                        claimconcept = (ClaimReserveByConcept) HibernateUtil.load(ClaimReserveByConcept.class, claimconcept.getPk());

                        claimconcept.load();
                        if (!claimTotalize.areTherePendingPaymentOrders()) {
                            claimTotalize.setPendingPaymentOrders(claimconcept.hasPendingPaymentOrders());
                        }
                        totalOrders += accountReservePaymentsOrders(claimconcept, claimTotalize.getConceptReservePendingPayments(), ru, doneby);
                        loadCurrencyExchangeRates(claimconcept.getCurrency(), currenciesList, currenciesExchangeRates);

                    } // ClaimReserveByConcept
                }  // InsuranceObjects
            } //if instance Claim
            log.debug("\nCurrency Exchange Rate List ->" + currenciesExchangeRates + "\n");
            double total = totalOrders;
            claim.setAmountToPay(total);
            claim.update();
            response.putAttribute("claimTotalize", claimTotalize);
            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            response.setResult(true);
            if (openTx) {
                commitTransaction(transaction, session);
            }
            return response;
        } catch (Exception ex) {
            log.error("There was an error accounting the payments: ", ex);
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            throw new TechnicalException(Exceptions.CCErrorClaim, Severity.FATAL, ex);
        } finally {
            if (openTx) {
                closeHibernateSession(session);
            }
        }
    }

    /**
     * Gets the payments by beneficiary
     *
     * @param claimID      the object identifier
     * @param coverageDesc
     * @return the response with the payments
     */
    public ClientResponse accountPaymentsOneByOne(String claimID, String doneby, boolean openTx, String coverageDesc, String ruID,
                                                  String ioDesc) throws ApplicationExceptionChecked {
        log.info("accountPaymentsOneByOne(String claimID, String doneby, boolean openTx, String coverageDesc, String ruID,\n" + " String ioDesc)");
        log.debug("claimID " + claimID);
        log.debug("doneby " + doneby);
        log.debug("openTx " + openTx);
        log.debug("coverageDesc " + coverageDesc);
        log.debug("ruID " + ruID);
        log.debug("ioDesc " + ioDesc);
        ClientResponse response = new ClientResponse();
        Session session = null;
        Transaction transaction = null;
        try {
            if (openTx) {
                session = getHibernateSession();
                transaction = beginTransaction(true, session);
            }
            log.debug("claimID = " + claimID);
            ClaimTotalizeOneByOne claimTotalize = new ClaimTotalizeOneByOne(claimID);
            Iterator paymentsReserveConcepts;
            Iterator paymentsReserve;
            ClaimInsuranceObject cio;
            double totalOrders = 0.0f;
            Map<String, Currency> currenciesList = new HashMap<String, Currency>();

            log.debug("ruID: " + ruID);
            log.debug("policy.getAgregatedRiskUnit(ruID): " + policy.getAgregatedRiskUnit(ruID));
            log.debug("ioDesc:" + ioDesc);
            log.debug(
                    "policy.getAgregatedRiskUnit(ruID).getDCO(ioDesc): " + policy.getAgregatedRiskUnit(ruID).getInsuranceObject(ioDesc));
            log.debug("coverageDesc: " + coverageDesc);

            //EvaluatedCoverage ec = searchEvaluatedCoverage(ruID, ioDesc, coverageDesc);
            List<ClaimNormalReserve> normalReserves = claim.getNormalReserves();
            EvaluatedCoverage ec=getEvaluatedCoverageByDesc(normalReserves,coverageDesc);
            log.debug("ec = " + ec);

            com.consisint.acsele.product.api.ClaimsCoverageConfiguration cconf = ec.getConfiguratedCoverageOA().getClaimsCoverageConfiguration();

            try {

                log.debug("claimTotalize = " + claimTotalize);
                log.debug("ec = " + ec);
                setPaymentsInClaimTotalizer(ReserveType.NORMAL_RESERVE.getValue(), this.claim.getPaymentsNormalReserveList(), claimTotalize, cconf);
                log.debug("claimTotalize = " + claimTotalize);
                log.debug("ec = " + ec);
                setPaymentsInClaimTotalizer(ReserveType.CONCEPT_RESERVE.getValue(), this.claim.getPaymentsConceptReserveList(), claimTotalize, cconf);

                CurrencyList currencies = CurrencyList.Impl.loadAll();
                for (Currency c : currencies) {
                    currenciesList.put(String.valueOf(c.getId()), c);
                }

            } catch (Exception e) {
                log.error("There was an error loading the payments to claimtotalizer and setting " + "the list of currencies: ", e);
                throw new TechnicalException("Exception loading currencies ", Severity.FATAL, e);
            }
            Map<String, Map<String, String>> currenciesExchangeRates = new HashMap<String, Map<String, String>>();
            claimTotalize.setCurrencyList(currenciesList);
            claimTotalize.setCurrenciesRates(currenciesExchangeRates);
            log.debug("\nCurrency List ->" + currenciesList);
            Collection<ClaimRiskUnit> claimRiskUnits = this.claim.getClaimRiskUnitsList();
            Vector normalReservePendingPayments = claimTotalize.getNormalReservePendingPayments();
            for (ClaimRiskUnit ru : claimRiskUnits) {
                Iterator io = ru.getClaimInsuranceObjectsList().iterator();
                while (io.hasNext()) {
                    cio = (ClaimInsuranceObject) io.next();

                    paymentsReserve = cio.getNormalReserves().values().iterator();
                    if (normalReservePendingPayments == null) {
                        claimTotalize.setNormalReservePendingPayments(new Vector());
                    }
                    while (paymentsReserve.hasNext()) {
                        ClaimNormalReserve cnr = (ClaimNormalReserve) paymentsReserve.next();
                        //cnr = (ClaimNormalReserve) HibernateUtil.load(ClaimNormalReserve.class, cnr.getPk());
                        //cnr.load();
                        if (!claimTotalize.areTherePendingPaymentOrders()) {
                            claimTotalize.setPendingPaymentOrders(cnr.hasPendingPaymentOrders());
                        }

                        totalOrders += accountReservePaymentsOrdersOneByOne(cnr, normalReservePendingPayments, ru, doneby, cconf, claimTotalize);

                        if (cnr.getCurrency() != null) {
                            log.debug("Currency Id: " + cnr.getCurrency().getId());
                            log.debug("Currency Desc:" + cnr.getCurrency().getDescription());
                        }
                        loadCurrencyExchangeRates(cnr.getCurrency(), currenciesList, currenciesExchangeRates);
                    }
                    paymentsReserveConcepts = cio.getReservesByConcept().values().iterator();
                    if (claimTotalize.getConceptReservePendingPayments() == null) {
                        claimTotalize.setConceptReservePendingPayments(new Vector());
                    }
                    while (paymentsReserveConcepts.hasNext()) {

                        ClaimReserveByConcept claimconcept = (ClaimReserveByConcept) paymentsReserveConcepts.next();
                        claimconcept = (ClaimReserveByConcept) HibernateUtil.load(ClaimReserveByConcept.class, claimconcept.getPk());

                        claimconcept.load();
                        if (!claimTotalize.areTherePendingPaymentOrders()) {
                            claimTotalize.setPendingPaymentOrders(claimconcept.hasPendingPaymentOrders());
                        }
                        totalOrders += accountReservePaymentsOrdersOneByOne(claimconcept, claimTotalize.getConceptReservePendingPayments(), ru,
                                doneby, cconf, claimTotalize);
                        loadCurrencyExchangeRates(claimconcept.getCurrency(), currenciesList, currenciesExchangeRates);

                    } // ClaimReserveByConcept
                }  // InsuranceObjects
            } //if instance Claim
            log.debug("\nCurrency Exchange Rate List ->" + currenciesExchangeRates + "\n");
            double total = totalOrders;
            this.claim.setAmountToPay(total);
            response.putAttribute("claimTotalize", claimTotalize);
            response.putAttribute("claimNumber", this.claim.getClaimNumber());
            response.putAttribute("claimstate", this.claim.getClaimStatus().getValue());
            response.setResult(true);

            if (openTx) {
                commitTransaction(transaction, session);
            }
            return response;
        } catch (Exception ex) {
            log.error("There was an error accounting the payments: ", ex);
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            response = new ClientResponse();
            response.setResult(false);
            return response;
        } finally {
            if (openTx) {
                closeHibernateSession(session);
            }
        }
    }

    private EvaluatedCoverage getEvaluatedCoverageByDesc(List<ClaimNormalReserve> normalReserves, String coverageDesc) {
        EvaluatedCoverage evaluatedCoverage = null;
        if(!ListUtil.isEmptyOrNull(normalReserves)){
            for(ClaimNormalReserve claimNormalReserve: normalReserves){
                evaluatedCoverage = claimNormalReserve.getEvaluatedCoverage();
                if(evaluatedCoverage.getDesc().equals(coverageDesc)){
                    return evaluatedCoverage;
                }
            }
        }
        return  evaluatedCoverage;
    }

    /**
     * Loads payments to reverse.
     *
     * @return
     * @throws ApplicationExceptionChecked
     */

    public ClientResponse loadPaymentToReverse() throws ApplicationExceptionChecked {

        Map paymentsNormal = new HashMap();
        Map paymentsConcept = new HashMap();
        ClientResponse response = new ClientResponse();
        Session session = null;
        try {
            session = getHibernateSession();
            Iterator openitemActiveIter = openItemHome.findByActiveOpenItemForThirdPartyAndClaimReference(null, claim.getPK(), null, null, null)
                    .iterator();
            while (openitemActiveIter.hasNext()) {
                log.debug("Tiene referencias");
                OpenItem openitem = (OpenItem) openitemActiveIter.next();
                Object reference = openitem.getOpenItemReference();

                int paymentId = (int) ((OpenItemReference) reference).getAdditionalReference();
                Payment payment = Payment.load(Long.valueOf(paymentId));

                if ((payment.getPaymentStatus() != PaymentStatus.CANCELED_STATE)
                        || (payment.getPaymentStatus() != PaymentStatus.PENDING_STATE)) {

                    if (payment.getReserveType() == ReserveType.NORMAL_RESERVE.getValue() && !payment.hasPaymentOrderOnHold()) {

                        paymentsNormal.put(openitem.getPrimaryKey(), payment);
                    } else if (payment.getReserveType() == ReserveType.CONCEPT_RESERVE.getValue()) {
                        paymentsConcept.put(openitem.getPrimaryKey(), payment);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error", e);
        } finally {
            closeHibernateSession(session);
        }
        response.putAttribute("paymentsNormal", paymentsNormal);
        response.putAttribute("paymentsConcept", paymentsConcept);
        return response;
    }


    public ClientResponse loadPaymentToReverse(OpenItemHome openItemHome) throws ApplicationExceptionChecked {

        Map paymentsNormal = new HashMap();
        Map paymentsConcept = new HashMap();
        ClientResponse response = new ClientResponse();
        Session session = null;
        try {
            session = getHibernateSession();
            Iterator openitemActiveIter = openItemHome.findByActiveOpenItemForThirdPartyAndClaimReference(null, claim.getPK(), null, null, null)
                    .iterator();
            while (openitemActiveIter.hasNext()) {
                log.debug("Tiene referencias");
                OpenItem openitem = (OpenItem) openitemActiveIter.next();
                Object reference = openitem.getOpenItemReference();

                int paymentId = (int) ((OpenItemReference) reference).getAdditionalReference();
                Payment payment = Payment.load(Long.valueOf(paymentId));

                if ((payment.getPaymentStatus() != PaymentStatus.CANCELED_STATE)
                        || (payment.getPaymentStatus() != PaymentStatus.PENDING_STATE)) {

                    if (payment.getReserveType() == ReserveType.NORMAL_RESERVE.getValue() && !payment.hasPaymentOrderOnHold()) {

                        paymentsNormal.put(openitem.getPrimaryKey(), payment);
                    } else if (payment.getReserveType() == ReserveType.CONCEPT_RESERVE.getValue()) {
                        paymentsConcept.put(openitem.getPrimaryKey(), payment);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error", e);
        } finally {
            closeHibernateSession(session);
        }
        response.putAttribute("paymentsNormal", paymentsNormal);
        response.putAttribute("paymentsConcept", paymentsConcept);
        return response;
    }

    /**
     * Stores payments in UAA
     *
     * @param claimTotalize
     * @return ClientResponse
     */
    public ClientResponse applyPayments(ClaimTotalizeOneByOne claimTotalize, String policyNumber, boolean openTx) throws ApplicationExceptionChecked {
        log.debug("ANGEL.... PASANDO LOS VALORES NULL...........");
        return applyPayments(claimTotalize, policyNumber, openTx, null, null, null);
    }

    /**
     * Stores payments in UAA. Use this method with ClaimTotalizeOneByOne
     *
     * @param claimTotalize
     * @return ClientResponse
     */
    public ClientResponse applyPayments(ClaimTotalizeOneByOne claimTotalize, String policyNumber, boolean openTx, String ruID, String ioID,
                                        HttpServletRequest request) throws ApplicationExceptionChecked {

        Session session = null;
        Transaction transaction = null;
        Long operationPk = null;
        try {
            if (openTx) {
                session = getHibernateSession();
                transaction = beginTransaction(false, session);
                reloadClaim();
            }
            ClientResponse response = new ClientResponse();
            response.setResult(true);

            log.debug("(ClaimComposerEJB) Payments data received.  claimTotalize: " + claimTotalize);
            if (claimTotalize == null) {
                return response;
            }

            int policySubStatus;
            if (asl) {
                policySubStatus = getAgregatedPolicy().getSubStatus();
            } else {
                PolicySession policySession = PolicySession.Impl.getInstance();
                policySession.findByLastApplied(this.getAgregatedPolicy().getPk());
                policySubStatus = policySession.getAgregatedPolicy().getSubStatus();
                operationPk = Long.parseLong(this.getAgregatedPolicy().getOperationPK().getPK());
            }
            log.debug("SubStatus 1 " + this.getAgregatedPolicy().getSubStatus() + "(" + this.getAgregatedPolicy().getTimeStamp() + ")");
            log.debug("SubStatus 2 " + getAgregatedPolicy().getSubStatus() + "(" + getAgregatedPolicy().getTimeStamp() + ")");

            int paymentsProxyVectorSize = claimTotalize.getNormalReservePaidPaymentsSize()+
                                            claimTotalize.getNormalReservePendingPaymentsSize()+
                                            claimTotalize.getNormalReserveCancelledPaymentsSize();
            if (!claimTotalize.getNormalReservePendingPayments().isEmpty()) {
                if (policySubStatus == PolicySubStatus.POLICY_SUBSTATUS_APPROVED || isCurrentDateValidity(this.claimId, operationPk)) {
                    applyPaymentsWithType(claimTotalize.getNormalReservePendingPayments(), paymentsProxyVectorSize, ruID, ioID);
                } else {
                    throw new ApplicationException(Exceptions.CCPolicySubStatusPending, Severity.ERROR, " (" + policySubStatus + ")");
                }
            }

            paymentsProxyVectorSize = claimTotalize.getConceptReservePaidPaymentsSize()+
                    claimTotalize.getConceptReservePendingPaymentsSize()+
                    claimTotalize.getConceptReserveCancelledPaymentsSize();
            applyPaymentsWithType(claimTotalize.getConceptReservePendingPayments(), paymentsProxyVectorSize, ruID, ioID);

            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            if (openTx) {
                commitTransaction(transaction, session);
            }
            return response;

        } catch (ApplicationExceptionChecked aec) {
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            log.error("Error applying payments - " + aec.getMessage());
            throw aec;
        } catch (AcseleException ae) {
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            log.error("Error applying payments - " + ae.getMessage());
            throw ae;
        } catch (Exception e) {
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            log.error("Error applying payments - " + e.getMessage());
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            if (openTx) {
                closeHibernateSession(session);
            }
        }
    }

    /**
     * Check validity for dates of claims and openitems
     *
     * @param claimId
     * @param operationPkOpenItem
     * @return boolean
     */

    public Boolean isCurrentDateValidity(Long claimId, Long operationPkOpenItem) throws FinderException {
        List openItemsList = null;
        try {
            openItemsList = (List)openItemHome.findByOperationPK(operationPkOpenItem);
        } catch (FinderException e) {
            log.debug(e);
            e.printStackTrace();
        }
        OpenItemImpl openItem  = (OpenItemImpl) Collections.min(openItemsList);
        Date openItemDate = openItem.getDueDate();
        Date claimDate = com.consisint.acsele.openapi.claim.Claim.findByPk(String.valueOf(claimId)).getClaimDate();
        if(claimDate.before(openItemDate) || claimDate.equals(openItemDate)){
            return true;
        }
        return false;
    }

    /**
     * Stores payments in UAA. Use this method with ClaimTotalizeOneByOne
     *
     * @param claimTotalize
     * @return ClientResponse
     */
    public ClientResponse applyPayments(ClaimTotalizeOneByOne claimTotalize, String policyNumber, boolean openTx, String ruID, String ioID, Long currencyPayment, String amountPayment, Date paymentDate,
                                        HttpServletRequest request) throws ApplicationExceptionChecked {

        Session session = null;
        Transaction transaction = null;
        try {
            if (openTx) {
                session = getHibernateSession();
                transaction = beginTransaction(false, session);
                reloadClaim();
            }
            ClientResponse response = new ClientResponse();
            response.setResult(true);

            log.debug("(ClaimComposerEJB) Payments data received.  claimTotalize: " + claimTotalize);
            if (claimTotalize == null) {
                return response;
            }

            int policySubStatus;
            if (asl) {
                policySubStatus = getAgregatedPolicy().getSubStatus();
            } else {
                PolicySession policySession = PolicySession.Impl.getInstance();
                policySession.findByLastApplied(this.getAgregatedPolicy().getPk());
                policySubStatus = policySession.getAgregatedPolicy().getSubStatus();
            }
//

            log.debug("SubStatus 1 " + this.getAgregatedPolicy().getSubStatus() + "(" + this.getAgregatedPolicy().getTimeStamp() + ")");
            log.debug("SubStatus 2 " + getAgregatedPolicy().getSubStatus() + "(" + getAgregatedPolicy().getTimeStamp() + ")");

            if (!claimTotalize.getNormalReservePendingPayments().isEmpty()) {
                if (policySubStatus == PolicySubStatus.POLICY_SUBSTATUS_APPROVED) {
                    applyPaymentsWithType(claimTotalize.getNormalReservePendingPayments(), claim.getPaymentsNormalReserveList().size(), ruID, ioID, currencyPayment, amountPayment, paymentDate);
                } else {
                    throw new ApplicationException(Exceptions.CCPolicySubStatusPending, Severity.ERROR, " (" + policySubStatus + ")");
                }
            }
            applyPaymentsWithType(claimTotalize.getConceptReservePendingPayments(), claim.getPaymentsConceptReserveList().size(), ruID, ioID, currencyPayment, amountPayment, paymentDate);

            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
//            log.debug("!!!!!!Estado del Claim: " + new Integer(claim.getState()));
            if (openTx) {
                commitTransaction(transaction, session);
            }
            return response;

        } catch (ApplicationExceptionChecked aec) {
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            log.error("Error applying payments - " + aec.getMessage());
            throw aec;
        } catch (AcseleException ae) {
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            log.error("Error applying payments - " + ae.getMessage());
            throw ae;
        } catch (Exception e) {
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            log.error("Error applying payments - " + e.getMessage());
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            if (openTx) {
                closeHibernateSession(session);
            }
        }
    }

    /**
     * Stores payments in UAA. Use this method with ClaimTotalizeOneByOne
     *
     * @param claimTotalize
     * @return ClientResponse
     */
    public ClientResponse applyPayments(ClaimTotalize claimTotalize, String policyNumber, boolean openTx, String ruID, String ioID,
                                        HttpServletRequest request) throws ApplicationExceptionChecked {

        Session session = null;
        Transaction transaction = null;
        try {
            if (openTx) {
                session = getHibernateSession();
                transaction = beginTransaction(false, session);
                reloadClaim();
            }
            ClientResponse response = new ClientResponse();
            response.setResult(true);

            log.debug("(ClaimComposerEJB) Payments data received.  claimTotalize: " + claimTotalize);
            if (claimTotalize == null) {
                return response;
            }

            int policySubStatus;
            if (asl) {
                policySubStatus = getAgregatedPolicy().getSubStatus();
            } else {
                PolicySession policySession = PolicySession.Impl.getInstance();
                policySession.findByLastApplied(this.getAgregatedPolicy().getPk());
                policySubStatus = policySession.getAgregatedPolicy().getSubStatus();
            }
//

            log.debug("SubStatus 1 " + this.getAgregatedPolicy().getSubStatus() + "(" + this.getAgregatedPolicy().getTimeStamp() + ")");
            log.debug("SubStatus 2 " + getAgregatedPolicy().getSubStatus() + "(" + getAgregatedPolicy().getTimeStamp() + ")");

            if (!claimTotalize.getNormalReservePendingPayments().isEmpty()) {
                if (policySubStatus == PolicySubStatus.POLICY_SUBSTATUS_APPROVED) {
                    applyPaymentsWithType(claimTotalize.getNormalReservePendingPayments(), claim.getPaymentsNormalReserveList().size(), ruID, ioID);
                } else {
                    throw new ApplicationException(Exceptions.CCPolicySubStatusPending, Severity.ERROR, " (" + policySubStatus + ")");
                }
            }
            applyPaymentsWithType(claimTotalize.getConceptReservePendingPayments(), claim.getPaymentsConceptReserveList().size(), ruID, ioID);

            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
//            log.debug("!!!!!!Estado del Claim: " + new Integer(claim.getState()));
            if (openTx) {
                commitTransaction(transaction, session);
            }
            return response;

        } catch (ApplicationExceptionChecked aec) {
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            log.error("Error applying payments - " + aec.getMessage());
            throw aec;
        } catch (AcseleException ae) {
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            log.error("Error applying payments - " + ae.getMessage());
            throw ae;
        } catch (Exception e) {
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            log.error("Error applying payments - " + e.getMessage());
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            if (openTx) {
                closeHibernateSession(session);
            }
        }
    }

    /**
     * @param paymentsNormal
     * @param paymentsConcept
     * @param policyNumber
     * @return ClientResponse
     * @throws ApplicationExceptionChecked
     */
    public ClientResponse applyReversePayment(Map paymentsNormal, Map paymentsConcept,
                                              String policyNumber) throws ApplicationExceptionChecked {
        log.debug("method:applyReversePayment()");
        ClientResponse response = new ClientResponse();
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);

            boolean result = applyReverseWithTypeOneByOne(paymentsNormal, policyNumber, null, null) | applyReverseWithTypeOneByOne(paymentsConcept,
                    policyNumber, null, null);

            response.setResult(result);
            commitTransaction(transaction, session);

        } catch (ApplicationExceptionChecked ae) {
            log.error("Error", ae);
            rollbackTransaction(transaction, session);
            response.putAttribute("error", ae.getKeyCode());
        } catch (Exception e) {
            log.error("Error", e);
            response.putAttribute("error", "claim.errorReversePayments");
            rollbackTransaction(transaction, session);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
        return response;
    }

    /**
     * @param paymentsNormal
     * @param paymentsConcept
     * @param policyNumber
     * @return ClientResponse
     * @throws ApplicationExceptionChecked
     */
    public ClientResponse applyReversePayment(Map paymentsNormal, Map paymentsConcept, String policyNumber, String ruID,
                                              String ioID) throws ApplicationExceptionChecked {

        ClientResponse response = new ClientResponse();
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);

            boolean result = applyReverseWithTypeOneByOne(paymentsNormal, policyNumber, ruID, ioID) | applyReverseWithTypeOneByOne(paymentsConcept,
                    policyNumber, ruID, ioID);

            response.setResult(result);
            commitTransaction(transaction, session);

        } catch (ApplicationExceptionChecked ae) {
            log.error("Error", ae);
            rollbackTransaction(transaction, session);
            response.putAttribute("error", ae.getKeyCode());
        } catch (Exception e) {
            log.error("Error", e);
            response.putAttribute("error", "claim.errorReversePayments");
            rollbackTransaction(transaction, session);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
        return response;
    }

    /*
     * Reverse apply Payment Coverage
     *
     * @param paymentsNormal
     * @param request
     * @return
     * @throws ApplicationExceptionChecked
     */
    public ClientResponse applyReversePaymentCoverage(Map paymentsNormal) throws ApplicationExceptionChecked {
        ClientResponse response = new ClientResponse();
        Session session = null;
        Transaction transaction = null;
        log.debug("   applyReversePaymentCoverage    ");
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);
            updatePaymentsOrdersReverseCoverage(paymentsNormal);

            response.setResult(true);
            commitTransaction(transaction, session);

        } catch (ApplicationExceptionChecked ae) {
            log.error("Error: ", ae);
            rollbackTransaction(transaction, session);
            response.putAttribute("error", ae.getKeyCode());
        } catch (Exception ex) {
            log.error("Error: ", ex);
            response.putAttribute("error", "claim.errorReversePayments");
            rollbackTransaction(transaction, session);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, ex);
        } finally {
            closeHibernateSession(session);
        }
        return response;
    }


    /**
     * Evaluates all the claim  defaults configurable objects
     *
     * @param symbolsTable the simbol table
     */
    public TablaSimbolos evaluateClaim(String claimId, TablaSimbolos symbolsTable) throws ApplicationExceptionChecked {

        Session session = null;
        try {
            session = getHibernateSession();

            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            evaluator.setTablaSimbolos(symbolsTable);
            evaluator.addSymbol("claimStatus", String.valueOf(claim.getClaimStatus().getValue()), new Double(0), true);
            evaluator.addSymbol("claimNumber", claim.getClaimNumber(), new Double(0), true);
            try {
                String ocurrenceDateSymbol = AcseleConf.getProperty("ocurrenceDate");
                String ocurrenceDateSymbolInput = DateUtil.getDateFormated(getOcurrenceDate());
                double ocurrenceDateSymbolValue = evaluator.evaluate(ocurrenceDateSymbolInput);

                evaluator.addSymbol(ocurrenceDateSymbol, ocurrenceDateSymbolInput, ocurrenceDateSymbolValue, false);
            } catch (Exception e) {
                log.error(" evaluateClaim -> colocando la fecha de ocurrencia", e);
            }

            if (claim.getNotification() != null) {
                evaluator.evaluateConfigurableObject(claim.getNotification().getDCO());
            }
            ClaimUtil.fillEvaluator(policy.getDCO(), evaluator);
            ClaimUtil.fillEvaluator(policy.getProduct().getDCO(), evaluator);

            Iterator<ClaimRiskUnit> claimRUList = claim.getClaimRiskUnitsList().iterator();
            if (claimRUList.hasNext()) {
                ClaimRiskUnit cru = claimRUList.next();
                AgregatedRiskUnit aru = cru.getAgregatedRiskUnit();
                ClaimUtil.fillEvaluator(aru.getDCO(), evaluator);

                Collection<ClaimInsuranceObject> cioList = cru.getClaimInsuranceObjects().values();
                Iterator<ClaimInsuranceObject> itCio = cioList.iterator();
                if (itCio.hasNext()) {
                    ClaimInsuranceObject cio = itCio.next();
                    cio.load();
                    evaluator.evaluateConfigurableObject(cio.getDamage());

                    AgregatedInsuranceObject aio = aru.getInsuranceObject(cio.getDesc());
                    ClaimUtil.fillEvaluator(aio.getDCO(), evaluator);

                    //todo si es una sola cobertura coloco el coverageReserve
                    Iterator normalReserveList = cio.getNormalReserves().values().iterator();
                    if (normalReserveList.hasNext()) {
                        ClaimNormalReserve cnr = (ClaimNormalReserve) normalReserveList.next();
                        cnr.load();
                        Double amountReserve = cnr.getAmount();
                        log.debug(" evaluateClaim -> coverageReserve (input/value): " + amountReserve);
                        evaluator.addSymbol("coverageReserve", amountReserve.toString(), amountReserve, false);
                        double deductibleAmount = cnr.getDeductibleAmount();
                        log.debug(" evaluateClaim -> deductibleAmount (input/value): " + deductibleAmount);
                        evaluator.addSymbol(EvaluationConstants.COVERAGE_RESERVE_DEDUCTIBLE, String.valueOf(deductibleAmount), deductibleAmount, false);

                    }
                }
            }

            return evaluator.getTablaSimbolos();
        } catch (Exception e) {
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    public TablaSimbolos fillSymbolTableOnlyPayment(PaymentOrder paymentOrder, TablaSimbolos symbolsTable) {

        symbolsTable = fillSymbolTableClaim(paymentOrder.getClaimReserve().getContainer().getContainer().getContainer().getPk(), symbolsTable);

        try {

            String billDetail = "";
            String billNumbers = "";
            double paymentOrderAmount = 0.0d;
            String beneficiaryName = "";
            double beneficiaryPk = 0.0d;

            paymentOrderAmount = paymentOrder.getAmount();
            beneficiaryName = paymentOrder.getBeneficiaryOpc().getName();
            beneficiaryPk = paymentOrder.getBeneficiaryOpc().getPk().doubleValue();

            String paidReason = paymentOrder.getReason();

            ClaimInvoiceSummary claimInvoiceSummary = ClaimInvoiceSummary.Impl.loadByPaymentOrder(paymentOrder.getPk());
            if (claimInvoiceSummary != null) {
                billDetail = claimInvoiceSummary.getDetail();
            }
            List<CoverageInvoice> invoiceList = CoverageInvoice.Impl.loadInvoiceByPaymentOrder(paymentOrder.getPk());
            for (CoverageInvoice invoice : invoiceList) {
                billNumbers += invoice.getId() + " ";
            }

            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            evaluator.setTablaSimbolos(symbolsTable);

            evaluator.addSymbol(EvaluationConstants.RESERVE_PAY_REASON, String.valueOf(paidReason), new Double(0), true);
            evaluator.addSymbol(EvaluationConstants.PAYMENT_ORDERS_BILL_DETAIL, billDetail, new Double(0), true);
            evaluator.addSymbol(EvaluationConstants.PAYMENT_ORDERS_BILL_NUMBERS, billNumbers, new Double(0), true);
            evaluator.addSymbol(EvaluationConstants.PAYMENT_ORDER_AMOUNT, String.valueOf(paymentOrderAmount), paymentOrderAmount, false);
            evaluator.getTablaSimbolos().put(EvaluationConstants.CLAIM_BENEFICIARY, beneficiaryName, beneficiaryPk, true);

            evaluator.addSymbols(paymentOrder.getBeneficiaryOpc().getDynamic().getDCO(), EvaluationConstants.BENEFICIARY_PREFIX);
            Role beneficiaryRole = Role.getInstance(paymentOrder.getThirdPartyRoleID(), paymentOrder.getBeneficiaryOpc().getId());
            if (beneficiaryRole != null)
                evaluator.addSymbols(beneficiaryRole.getDynamic().getDCO(), EvaluationConstants.BENEFICIARY_PREFIX);

            evaluator.addSymbols(paymentOrder.getThirdParty().getDynamic().getDCO(), EvaluationConstants.RECIPIENT_PREFIX);

            return evaluator.getTablaSimbolos();

        } catch (Exception e) {
            log.error("Error # " + Exceptions.CCErrorFillingSymbolTableForClaim);
            log.error("The available environment snapshot is: \n" + "claimId: " + claimId + "symbolsTable: " + symbolsTable);
            ExceptionUtil.handleException(Exceptions.CCErrorFillingSymbolTableForClaim, e);
            return symbolsTable;
        }
    }

    public TablaSimbolos fillSymbolTablePayment(PaymentOrder paymentOrder, TablaSimbolos symbolsTable) {

        symbolsTable = fillSymbolTableClaim(paymentOrder.getClaimReserve().getContainer().getContainer().getContainer().getPk(), symbolsTable);

        return fillSymbolTableOnlyPayment(paymentOrder, symbolsTable);
    }

    public TablaSimbolos fillSymbolTableClaim(String claimId, TablaSimbolos symbolsTable) {
        return fillSymbolTableClaim(claimId, null, symbolsTable, 0);
    }

    public TablaSimbolos fillSymbolTableClaim(String claimId, TablaSimbolos symbolsTable, long idRejectionMotive) {
        return fillSymbolTableClaim(claimId, null, symbolsTable, idRejectionMotive);
    }

    public TablaSimbolos fillSymbolTableClaim(String claimId, ClaimNormalReserve claimNormalReserve, TablaSimbolos symbolsTable) {
        return fillSymbolTableClaim(claimId, claimNormalReserve, symbolsTable, 0);
    }

    public TablaSimbolos fillSymbolTableClaim(String claimId, TablaSimbolos symbolsTable, Long dcoClaimDenied) {
        return fillSymbolTableClaim(claimId, null, symbolsTable, 0, dcoClaimDenied);
    }

    /**
     * Evaluates all the claim  defaults configurable objects
     *
     * @param symbolsTable the simbol table
     */
    //TODO: revisar el uso de ClaimID... esta usando el claim privado.
    public TablaSimbolos fillSymbolTableClaim(String claimId, ClaimNormalReserve claimNormalReserve, TablaSimbolos symbolsTable, long idRejectionMotive) {
        return fillSymbolTableClaim(claimId, claimNormalReserve, symbolsTable, idRejectionMotive, null);
    }


    public TablaSimbolos fillSymbolTableClaim(String claimId, ClaimNormalReserve claimNormalReserve, TablaSimbolos symbolsTable, long idRejectionMotive, Long dcoClaimDenied) {

        Session session = null;
        try {
            session = getHibernateSession();
            ClaimRiskUnit cru;
            ClaimInsuranceObject cio;
            AgregatedRiskUnit aru;
            AgregatedInsuranceObject aio;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new java.util.Date());
            Claim claim = Claim.getInstance(Long.valueOf(claimId));
            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            evaluator.setTablaSimbolos(symbolsTable);

            String year = String.valueOf(calendar.get(Calendar.YEAR));
            String monthName = DateUtil.getMonthNames()[calendar.get(Calendar.MONTH)];
            String monthNumber = String.valueOf(calendar.get(Calendar.MONTH) + 1);
            String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

            evaluator.addSymbol(EvaluationConstants.SYSDATE_DAY, day, new Double(0), false);
            evaluator.addSymbol(EvaluationConstants.SYSDATE_MONTH_NAME, monthName, new Double(0), false);
            evaluator.addSymbol(EvaluationConstants.SYSDATE_MONTH_NUMBER, monthNumber, new Double(0), false);
            evaluator.addSymbol(EvaluationConstants.SYSDATE_YEAR, year, new Double(0), false);

            evaluator.addSymbol(EvaluationConstants.CLAIM_STATUS, String.valueOf(claim.getClaimStatus().getValue()), new Double(0), true);
            evaluator.addSymbol(EvaluationConstants.CLAIM_NUMBER, claim.getClaimNumber(), new Double(0), true);

            evaluator.addSymbol(EvaluationConstants.POLICY_PK, claim.getPolicyId(), new Double(0), true);
            evaluator.addSymbol(EvaluationConstants.POLICY_NUMBER, claim.getPolicy().getPolicyNumber(), new Double(0), true);
            if (claim.getGlobalAgency() != null) {
                evaluator.addSymbol(EvaluationConstants.CLAIM_BRANCH, claim.getGlobalAgency().getDescription(), new Double(0), true);
            }
            evaluator.addSymbol(EvaluationConstants.PRODUCT_DESC, claim.getPolicy().getProduct().getName(), new Double(0), true);
            evaluator.addSymbol(EvaluationConstants.CLAIM_PK, claim.getPk(), new Double(0), true);
            evaluator.addSymbol(EvaluationConstants.EVENTCLAIMDESC, claim.getEventClaim().getDesc(), new Double(0), true);

            Collection movementCollection = OpenItemReferenceImpl.findByClaimNumber(claim.getClaimNumber());
            double recoveryAmount = 0.0;
            if (!movementCollection.isEmpty()) {
                Vector vector = new Vector(movementCollection);
                Enumeration recoveryElements = vector.elements();
                while (recoveryElements.hasMoreElements()) {
                    OpenItemReferenceImpl recoveries = (OpenItemReferenceImpl) recoveryElements.nextElement();
                    if (recoveries.getRecoveryNumber() != null) {
                        recoveryAmount += recoveries.getOpenItem().getAmount();
                    }
                }
            }
            evaluator.addSymbol(EvaluationConstants.RECOVERY_AMOUNT, String.valueOf(recoveryAmount), new Double(recoveryAmount), false);

            try {
                String ocurrenceDateSymbol = AcseleConf.getProperty("ocurrenceDate");
                String ocurrenceDateSymbolInput = DateUtil.getDateFormated(getOcurrenceDate());
                evaluator.addSymbol(ocurrenceDateSymbol, ocurrenceDateSymbolInput, new Double(0), false);
            } catch (Exception e) {
                log.error(" evaluateClaim -> colocando la fecha de ocurrencia", e);
            }

            if (claim.getNotification() != null) {
                evaluator.addSymbols(claim.getNotification().getDCO());
            }
            if (policy == null) {
                policy = claim.getAgregatedPolicy();
            }

            double paidAmount = 0.0f;

            Iterator normalReservesI = claim.getNormalReserves().iterator();
            if (normalReservesI.hasNext()) {
                ClaimNormalReserve nr = (ClaimNormalReserve) normalReservesI.next();
                Iterator payments = nr.getPaymentOrderList().values().iterator();
                while (payments.hasNext()) {
                    PaymentOrder paymentOrder = (PaymentOrder) payments.next();
                    paymentOrder = PaymentOrder.load(paymentOrder.getPk());

                    if (paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.CANCELED_STATE &&
                            paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.REJECTED_STATE) {
                        paidAmount += paymentOrder.getAmount();

                    }
                }
            }

            evaluator.addSymbol(EvaluationConstants.RESERVE_PAY_AMOUNT, String.valueOf(paidAmount), new Double(0), true);

            NewAgreement policyAgreement = policy.getAgreement();
            if (policyAgreement != null) {
                evaluator.addSymbol(EvaluationConstants.AGREEMENT_DESC, String.valueOf(policyAgreement.getDescription()), new Double(0), true);
                evaluator.addSymbol(EvaluationConstants.AGREEMENT_ID, String.valueOf(policyAgreement.getId()), new Double(0), true);
                evaluator.addSymbols(policyAgreement.getDco());
                claim.getAgregatedPolicy().publishAgreement(evaluator);
            }
            ClaimUtil.fillEvaluator(policy.getProduct().getDCO(), evaluator);

            //ClaimRejectionMotiveImpl claimRejectionMotive =  ClaimRejectionMotiveImpl.

            //Product pi =    Product.Impl.getProduct(policy.getProduct().getId());
            //List<ClaimRejectionMotive> claimRejectionMotiveList = new ArrayList<ClaimRejectionMotive>();
            Policy polApi = claim.getPolicy();

            Collection<Agreement> agreements = polApi.getAgreementAssociated();
            for (Agreement agreement : agreements) {
                com.consisint.acsele.agreements.server.Agreement agreementMax = AgreementManager.findMaxVersionById(new Long(agreement.getId()));
                AgreementClaimRejectPersister agreementClaimRejectPersister = AgreementClaimHibPersister.Impl.getInstance();
                List<AgreementClaimRejectMotive> agreementClaimRejectMotives = agreementClaimRejectPersister.getByVersionId(new Long(agreementMax.getVersionPk()));
                for (AgreementClaimRejectMotive agreementClaimRejectMotive : agreementClaimRejectMotives) {
                    //claimRejectionMotiveList.add(agreementClaimRejectMotive.getClaimRejectionMotive());
                    evaluator.addSymbol(EvaluationConstants.CLAIM_REJECT_MOTIVE, String.valueOf(agreementClaimRejectMotive.getClaimRejectionMotiveImpl().getRejectionmotive()), new Double(0), true);
                }
            }

            log.debug("fillSymbolTableClaim ------- idRejectionMotive : " + idRejectionMotive);
            log.debug("fillSymbolTableClaim ------- claim.getId() : " + claim.getId());
            if (idRejectionMotive != 0) {
                ClaimStateBeanPersister claimStateBeanPersister = ClaimStateBeanPersister.Impl.getInstance();
                List<ClaimStateBean> claimStateBeanList = claimStateBeanPersister.getAllByClaimStateBeans(claim.getId());
                /*for (ClaimStateBean claimStateBean : claimStateBeanList) {
                        if(claimStateBean.getClaimRejectMotiveId()==Long.getLong(rejectMotive)) {*/
                Policy policyApi = claim.getPolicy();
                ClaimRejectionMotiveList claimRejectionMotiveList = policyApi.getProduct().getClaimRejectionMotiveList();
                log.debug("fillSymbolTableClaim ------- claimRejectionMotiveList: " + claimRejectionMotiveList);
                Iterator iter = claimRejectionMotiveList.iterator();
                while (iter.hasNext()) {
                    ClaimRejectionMotive claimRejectionMotive = (ClaimRejectionMotive) iter.next();
                    log.debug("fillSymbolTableClaim ------- Iterando - claimRejectionMotive: " + claimRejectionMotive);
                    log.debug("fillSymbolTableClaim ------- Iterando - claimRejectionMotive.getPk(): " + claimRejectionMotive.getPk());
                    if (claimRejectionMotive.getPk() == idRejectionMotive) {
                        String descrMotive = claimRejectionMotive.getRejectionmotive();
                        log.debug("fillSymbolTableClaim ------- Iterando - descrMotive: " + descrMotive);
                        evaluator.addSymbol(EvaluationConstants.CLAIM_REJECT_MOTIVE, String.valueOf(descrMotive), new Double(0), true);
                        log.debug("fillSymbolTableClaim ------- Iterando - EvaluationConstants.CLAIM_REJECT_MOTIVE: " + EvaluationConstants.CLAIM_REJECT_MOTIVE);
                    }
                }
                // }
                //}
            }

            ClaimStateBean claimStateBean = claim.getLastClaimState();
            String motive = "";
            if (claimStateBean.getClaimStatus().getValue() == ClaimStatus.CLOSED.getValue()) {  //1
                DefaultConfigurableObject dco = claimStateBean.getDco();
                motive = dco.getCriterioInput("MotivoCierre") != null ? dco.getCriterioInput("MotivoCierre") : " ";
            }
            evaluator.addSymbol(EvaluationConstants.CLAIM_REJECT_MOTIVE, motive, new Double(0), true);

            //Getting client role at policy level and its
            RoleList rolesClient = RoleGroup.CLIENT_ROLES.getRoleList();
            Collection<Participation> participationCollection = policy.getParticipationCollection();
            String clientCompleteAddress = getThirdPartyAddressRole(rolesClient, participationCollection);
            evaluator.addSymbol(EvaluationConstants.CLIENT_COMPLETE_ADDRESS, clientCompleteAddress.toUpperCase(), new Double(0), false);


            if (claimNormalReserve != null) {
                try {
                    CoverageTitle coverageTitle = claimNormalReserve.getEvaluatedCoverage().getConfiguratedCoverageOA().getCoverageTitle();
                    evaluator.addSymbol(EvaluationConstants.COVERAGE_SELECTED, coverageTitle.getDesc(), new Double(coverageTitle.getId()), false);
                    evaluator.addSymbol(EvaluationConstants.CLAIM_NORMAL_RESERVE_DESC, claimNormalReserve.getDesc(), new Double(0), true);
                } catch (Exception e) {
                }

                //Getting insurance role at policy level and its address
                evaluator.addSymbol(EvaluationConstants.TYPE_RESERVE, claimNormalReserve.getLastReserveAdjust().getTypeString().substring(0, 3),
                        new Double(claimNormalReserve.getLastReserveAdjust().getType()), true);
                claimNormalReserve.getLastReserveAdjust().getTypeString();
                RoleList rolesInsurance = RoleGroup.INSURANCE_ROLES.getRoleList();

                ClaimInsuranceObject claimInsuranceObject = claimNormalReserve.getContainer();
                participationCollection = claimInsuranceObject.getAgregatedInsuranceObject().getParticipationCollection();

                String insuranceCompleteAddress = getThirdPartyAddressRole(rolesInsurance, participationCollection);
                evaluator.addSymbol(EvaluationConstants.INSURANCE_COMPLETE_ADDRESS, insuranceCompleteAddress.toUpperCase(), new Double(0), false);
            }

            ClaimUtil.fillEvaluator(policy.getProduct().getDCO(), evaluator);

            Collection reserveConcepts = ClaimUtil.getReservesConcepts();

            evaluator = this.getAgregatedPolicy().publishAllSymbols(evaluator);

            Iterator<Participation> participations = participationCollection.iterator();
            Participation par;
            if (participations.hasNext()) {
                par = participations.next();
                par.publishAllSymbols(evaluator);
            }

            //     evaluator = this.getAgregatedPolicy().publishAllSymbols(evaluator);

            Iterator<ClaimRiskUnit> claimRUList = claim.getClaimRiskUnitsList().iterator();
            if (claimRUList.hasNext()) {
                cru = claimRUList.next();
                evaluator.addSymbol(EvaluationConstants.RISK_UNIT_INITIAL_DATE, cru.getAgregatedRiskUnit().getInitialDate().toString(),
                        Funciones.dateTransformer.toNumber(cru.getAgregatedRiskUnit().getInitialDate()), false);
                Collection<ClaimInsuranceObject> cioList = cru.getClaimInsuranceObjects().values();
                Iterator<ClaimInsuranceObject> itCio = cioList.iterator();
                if (itCio.hasNext()) {
                    cio = itCio.next();
                    if (!asl) {
                        cio.load();
                    }
                    evaluator.addSymbol(EvaluationConstants.RISK_UNIT_PK, cru.getPK(), new Double(cru.getPK()), false);
                    evaluator.addSymbol(EvaluationConstants.PLAN_NAME, cio.getAgregatedInsuranceObject().getPlan().getDescription(), new Double(0), false);
                    evaluator.addSymbols(cio.getDamage());


                    //todo si es una sola cobertura coloco el coverageReserve
                    if (claimNormalReserve == null) {
                        Iterator normalReserveList = cio.getNormalReserves().values().iterator();
                        if (normalReserveList.hasNext()) {
                            claimNormalReserve = (ClaimNormalReserve) normalReserveList.next();
                            claimNormalReserve.load();
                        }
                    }

                    if (claimNormalReserve != null) {
                        Double amountReserve = claimNormalReserve.getAmount();
                        log.debug(" evaluateClaim -> coverageReserve (input/value): " + amountReserve);
                        evaluator.addSymbol("coverageReserve", amountReserve.toString(), amountReserve, false);
                        double deductibleAmount = claimNormalReserve.getDeductibleAmount();
                        log.debug(" evaluateClaim -> deductibleAmount (input/value): " + deductibleAmount);
                        EvaluatedCoverage evaluatedCoverage = claimNormalReserve.getEvaluatedCoverage();
                        evaluator.addSymbol(EvaluationConstants.TYPE_RESERVE, claimNormalReserve.getLastReserveAdjust().getTypeString().substring(0, 3),
                                new Double(claimNormalReserve.getLastReserveAdjust().getType()), true);
                        evaluator.addSymbol(EvaluationConstants.COVERAGE_RESERVE_DEDUCTIBLE, String.valueOf(deductibleAmount), deductibleAmount, false);
                        evaluator.addSymbol("coverageReserveDeductibleevaluatedCoverage.publishAllSymbolsToUp(evaluator);", String.valueOf(deductibleAmount), deductibleAmount, false);
                        evaluator.addSymbol(EvaluationConstants.CURRENCY, claimNormalReserve.getCurrency().getDescription(), new Double(0), true);
                        evaluator.addSymbol(EvaluationConstants.CLAIM_NORMAL_RESERVE_DATE, DateUtil.getDateToShow(claimNormalReserve.getDate()), new Double(0), true);
                        evaluator.addSymbol(EvaluationConstants.CLAIM_NORMAL_RESERVE_DESC, claimNormalReserve.getDesc(), new Double(0), true);

                        if (claimNormalReserve.getCoverageReserveStatus() == CoverageReserveStatus.REFUSED) {
                            String temp = AcseleConf.getProperty(this.TEMPLATE_CLAIM_DENIED);
                            Categorias templates = Categorias.getBean(Categorias.ALL_TEMPLATES_STATE);
                            ConfigurableObjectType cot = (ConfigurableObjectType) templates.get(CotType.OTHER, temp);
                            DefaultConfigurableObject dco =
                                    DefaultConfigurableObject.load(cot, Long.valueOf(claimNormalReserve.getRefusalDcoId()));
                            evaluator.addSymbols(dco);
                        }
                        int daysAccumlated = calculateDaysAccumated(claimNormalReserve.getPaymentOrderList());
                        log.debug("## daysAccumlated=" + daysAccumlated);
                        evaluator.addSymbol(EvaluationConstants.DAYS_ACCUMULATED, String.valueOf(daysAccumlated), Double.valueOf(daysAccumlated), true);

                        if (dcoClaimDenied != null) {
                            String temp = AcseleConf.getProperty(this.TEMPLATE_CLAIM_DENIED);
                            Categorias templates = Categorias.getBean(Categorias.ALL_TEMPLATES_STATE);
                            ConfigurableObjectType cot = (ConfigurableObjectType) templates.get(CotType.OTHER, temp);
                            DefaultConfigurableObject dco = DefaultConfigurableObject.load(cot, dcoClaimDenied);
                            evaluator.addSymbols(dco);
                        }


                        evaluatedCoverage.publishAllSymbolsToUp(evaluator);
                    }
                }
            }
            return evaluator.getTablaSimbolos();
        } catch (Exception e) {
            log.error("Error # " + Exceptions.CCErrorFillingSymbolTableForClaim);
            log.error("The available environment snapshot is: \n" + "claimId: " + claimId + "symbolsTable: " + symbolsTable);
            ExceptionUtil.handleException(Exceptions.CCErrorFillingSymbolTableForClaim, e);
            return null;
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Se contabiliza los dias acumulados de los pagos del siniestro
     *
     * @param paymentOrderCollection
     * @return
     */
    private int calculateDaysAccumated(Map<Long, PaymentOrder> paymentOrderCollection) {
        long daysAccumulatedPaid = 0;
        if (paymentOrderCollection != null && !paymentOrderCollection.isEmpty()) {
            for (PaymentOrder paymentOrder : paymentOrderCollection.values()) {
                if (PaymentOrderStatus.PAID_STATE.getValue() == paymentOrder.getState() || PaymentOrderStatus.APPROVED_STATE.getValue() == paymentOrder.getState()) {
                    long dif = ((paymentOrder.getEndDate().getTime() - paymentOrder.getStartDate().getTime()) / (1000 * 60 * 60 * 24)) + 1;
                    log.debug("####### " + paymentOrder.getEndDate() + "-" + paymentOrder.getStartDate() + " = " + dif);
                    daysAccumulatedPaid = daysAccumulatedPaid + dif;
                }
            }
        }
        return (int) daysAccumulatedPaid;
    }

    public ExpresionEvaluator fillSymbolClaim(String claimId, TablaSimbolos symbolsTable) {
        return fillSymbolClaim(claimId, symbolsTable, claim, policy);
    }

    public ExpresionEvaluator fillSymbolClaim(String claimId, TablaSimbolos symbolsTable, Claim claimPolicy, AgregatedPolicy agregatedPolicy) {
        this.policy = agregatedPolicy;
        this.claim = claimPolicy;
        Session session = null;
        try {
            session = getHibernateSession();
            ClaimRiskUnit cru;
            ClaimInsuranceObject cio;
            ClaimNormalReserve cnr = null;
            AgregatedRiskUnit aru;
            AgregatedInsuranceObject aio;

            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            evaluator.setTablaSimbolos(symbolsTable);
            evaluator.addSymbol("claimStatus", String.valueOf(claim.getClaimStatus().getValue()), (double) claim.getClaimStatus().getValue(), true);
            evaluator.addSymbol("claimNumber", claim.getClaimNumber(), 0.0, true);

            try {
                String ocurrenceDateSymbol = AcseleConf.getProperty("ocurrenceDate");
                String ocurrenceDateSymbolInput = DateUtil.getDateFormated(getOcurrenceDate());
                evaluator.addSymbol(ocurrenceDateSymbol, ocurrenceDateSymbolInput, (double) DateUtil.getValueFromDate(ocurrenceDateSymbolInput), false);
            } catch (Exception e) {
                log.error(" evaluateClaim -> colocando la fecha de ocurrencia", e);
            }

            if (claim.getNotification() != null) {
                evaluator.addSymbols(claim.getNotification().getDCO());
            }
            if (policy == null) {
                policy = claim.getAgregatedPolicy();
            }

            NewAgreement policyAgreement = policy.getAgreement();
            if (policyAgreement != null) {
                evaluator.addSymbols(policyAgreement.getDco());
                claim.getAgregatedPolicy().publishAgreement(evaluator);
            }

            ClaimUtil.fillEvaluator(policy.getProduct().getDCO(), evaluator);

            evaluator = this.getAgregatedPolicy().publishAllSymbols(evaluator);

            Iterator<ClaimRiskUnit> claimRUList = claim.getClaimRiskUnitsList().iterator();
            if (claimRUList.hasNext()) {
                cru = claimRUList.next();
                Collection<ClaimInsuranceObject> cioList = cru.getClaimInsuranceObjects().values();
                Iterator<ClaimInsuranceObject> itCio = cioList.iterator();
                if (itCio.hasNext()) {
                    cio = itCio.next();
                    if (!asl) {
                        cio.load();
                    }
                    evaluator.addSymbols(cio.getDamage(), EvaluationConstants.DAMAGE_CLAIM_PREFIX);

                    Iterator normalReserveList = cio.getNormalReserves().values().iterator();
                    if (normalReserveList.hasNext()) {
                        cnr = (ClaimNormalReserve) normalReserveList.next();
                        cnr.load();
                        Double amountReserve = cnr.getAmount();
                        log.debug(" evaluateClaim -> coverageReserve (input/value): " + amountReserve);
                        evaluator.addSymbol("coverageReserve", amountReserve.toString(), amountReserve, false);
                        double deductibleAmount = cnr.getDeductibleAmount();
                        log.debug(" evaluateClaim -> deductibleAmount (input/value): " + deductibleAmount);
                        EvaluatedCoverage evaluatedCoverage = cnr.getEvaluatedCoverage();
                        evaluator.addSymbol(EvaluationConstants.IOPK, String.valueOf(evaluatedCoverage.getInsuranceObject().getId()), (double) evaluatedCoverage.getInsuranceObject().getId(), false);
                        evaluator.addSymbol(EvaluationConstants.COVERAGE_RESERVE_DEDUCTIBLE, String.valueOf(deductibleAmount), deductibleAmount, false);

                        if (cnr.getCoverageReserveStatus() == CoverageReserveStatus.REFUSED) {
                            String temp = AcseleConf.getProperty(TEMPLATE_CLAIM_DENIED);
                            Categorias templates = Categorias.getBean(Categorias.ALL_TEMPLATES_STATE);
                            ConfigurableObjectType cot = (ConfigurableObjectType) templates.get(CotType.OTHER, temp);
                            DefaultConfigurableObject dco =
                                    DefaultConfigurableObject.load(cot, Long.valueOf(cnr.getRefusalDcoId()));
                            evaluator.addSymbols(dco);
                        }
                        evaluatedCoverage.publishAllSymbolsToUp(evaluator);
                        HibernateUtil.cleanCache();
                    }
                }
                GlobalUser globalUser = UserInfo.getGlobalUser();
                GlobalGroupList groupList = globalUser.getGlobalGroupList();

                if(groupList.size() == 1){
                    GlobalGroup group = groupList.getAll().get(0);
                    evaluator.addSymbol(EvaluationConstants.GROUP, group.getName() , new Double(0) , false);
                }

            }
            return evaluator;
        } catch (Exception e) {
            log.error("Error # " + Exceptions.CCErrorFillingSymbolTableForClaim);
            log.error("The available environment snapshot is: \n" + "claimId: " + claimId + "symbolsTable: " + symbolsTable);
            ExceptionUtil.handleException(Exceptions.CCErrorFillingSymbolTableForClaim, e);
            return null;
        } finally {
            HibernateUtil.cleanCache();
            closeHibernateSession(session);
        }
    }

    /**
     * Generate the reinsurance distributions
     *
     * @return a ClientResponse object
     */
    public ClientResponse generateReinsuranceDistributions() throws ApplicationExceptionChecked {

        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);
            ClientResponse response = new ClientResponse();
            ReinsuranceDistributionService gcs = ReinsuranceDistributionService.Impl.getInstance();
            Collection<ClaimOperation> reinsuranceDistributions = gcs.getReinsuranceClaimOperations(this.claim.getPK());
            response.putAttribute("reinsuranceDistributions", reinsuranceDistributions);
            response.setResult(true);
            commitTransaction(transaction, session);
            return response;
        } catch (Exception e) {
            rollbackTransaction(transaction, session);
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Sets the claim inputs for the inputs in the list if any property match
     *
     * @param claimId
     * @param inputsTable
     */
    public void setPropertiesInputs(String claimId, Map<String, String> inputsTable) throws
            ApplicationExceptionChecked {
        Session session = null;
        try {
            session = getHibernateSession();
            if (inputsTable == null) {
                return;
            }
            if (claim.getNotification() != null) {
                setDCOPropertiesInputs(claim.getNotification().getDCO(), inputsTable);
            }
            Iterator claimRUList = claim.getClaimRiskUnitsList().iterator();
            if (claimRUList.hasNext()) {
                ClaimRiskUnit cru = (ClaimRiskUnit) claimRUList.next();
                Iterator insuranceObjects = cru.getClaimInsuranceObjects().values().iterator();
                if (insuranceObjects.hasNext()) {
                    ClaimInsuranceObject cio = (ClaimInsuranceObject) insuranceObjects.next();
                    if (cio.getDamage() == null) {
                        cio.load();
                    }
                    setDCOPropertiesInputs(cio.getDamage(), inputsTable);
                }
            }
        } catch (Exception e) {
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    public Hashtable calculateTaxesForRole(int roleId) throws ApplicationExceptionChecked {
        return calculateTaxesForRole(policy, roleId);
    }

    /**
     * Calculates the taxes for the specified payment order, according to the specified role
     *
     * @param roleID
     * @return Hashtable
     */
    public Hashtable calculateTaxesForRole(AgregatedPolicy policy, int roleID) throws ApplicationExceptionChecked {
        Session session = null;
        try {
            session = getHibernateSession();

            com.consisint.acsele.uaa.api.Role roleImpl = com.consisint.acsele.uaa.api.Role.Impl.load(roleID);
            log.debug("roleDTO" + roleImpl.getDescription());
            AgregatedRole role = (AgregatedRole) policy.getProduct().getAgregatedRolesLevelPolicy()
                    .get(roleImpl.getDescription());
            Hashtable taxes = new Hashtable();
            if (role != null) {
                log.debug("ClaimComposerEJB.calculateTaxesForRole -> roleId " + roleID + " pk:" + role.getId());
//                role.load();
                ConfiguratedOperation confTax = role.getClaimTaxDistribution();

                log.debug("ClaimComposerEJB.calculateTaxesForRole -> distribution " + confTax + " optype: " + confTax.getConfigurableObject());
                if ((confTax == null) || (confTax.getConfigurableObject() == null)) {
                    return null;
                }

                com.consisint.acsele.template.server.OperationType operationType = confTax.getOperationType();
                Enumeration datos = operationType.elements();
                ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
                //todo asumo que son porcentajes asi que no necesito evaluar el reclamo evaluateClaim(evaluator);
                while (datos.hasMoreElements()) {
                    PropiedadImpl propiedad = (PropiedadImpl) datos.nextElement();
                    UAADetailType uaaDetailType = propiedad.getUAADetailType();
                    double percentage = 0;
                    if (uaaDetailType != null) {
                        Enumeration details = uaaDetailType.elements();
                        int sequenceIndex = 0;

                        while (details.hasMoreElements()) {
                            Propiedad property = (Propiedad) details.nextElement();

                            if ((property.getDesc() != null) && (!property.getDesc().equalsIgnoreCase(AcseleConf.getProperty("descPagoClaim")))) {
                                try {
                                    percentage = evaluator.evaluate(property.getFormula());
                                    log.debug("[calculateTaxesForRole] percentage = " + percentage);

                                    PaymentOrderTax paymentOrderTax = new PaymentOrderTax(sequenceIndex++, property.getEtiqueta(), percentage, -1,
                                            PaymentOrderTax.APPLIED);
                                    taxes.put(property.getEtiqueta(), paymentOrderTax);
                                } catch (Exception e) {
                                    taxes.put(property.getEtiqueta(), new PaymentOrderTax(sequenceIndex++, property.getEtiqueta(), percentage, -1,
                                            PaymentOrderTax.APPLIED));
                                    log.error("Error", e);
                                }

                                log.debug("ClaimComposerEJB.calculateTaxesForRole -> etiqueta: " + property.getEtiqueta() + " value " + percentage);
                            }
                        }
                    } else {
                        return null;
                    }
                }
            }
            return taxes;
        } catch (Exception e) {
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Change the claim status
     *
     * @param state_ the new valid state
     */
    public ClientResponse setState(String claimId, ClaimStatus claimStatus) throws ApplicationExceptionChecked {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);
            findClaim(claimId);
            ClientResponse response = new ClientResponse();
            response.setResult(true);
            log.debug("Paso Claim Entity EJB");
            setState(claimStatus);
            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            log.debug(" Estado del Claim: " + claim.getClaimStatus().toString());
            commitTransaction(transaction, session);
            return response;
        } catch (Exception e) {
            rollbackTransaction(transaction, session);
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    public ClientResponse setState(String claimId, ClaimStatus claimStatus, com.consisint.acsele.openapi.claim.ClaimHistory history) throws ApplicationExceptionChecked {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(true, session);
            findClaim(claimId);
            ClientResponse response = new ClientResponse();
            response.setResult(true);
            log.debug("Paso Claim Entity EJB");
            if (history == null) {
                setState(claimStatus);
            } else {
                setState(claimStatus, history);
            }

            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            log.debug(" Estado del Claim: " + claim.getClaimStatus().toString());
            commitTransaction(transaction, session);
            return response;
        } catch (Exception e) {
            rollbackTransaction(transaction, session);
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    public ClientResponse setStateWithDCOInformation(ClaimStateBean bean, boolean openTx) throws ApplicationExceptionChecked {
        return setStateWithDCOInformation(bean, openTx, null);
    }

    /**
     * @param bean
     * @param openTx
     * @return
     * @throws ApplicationExceptionChecked
     */
    public ClientResponse setStateWithDCOInformation(ClaimStateBean bean, boolean openTx, Long dcoClaimDenied) throws ApplicationExceptionChecked {
        Session session = null;
        Transaction transaction = null;
        try {
            if (openTx) {
                session = getHibernateSession();
                transaction = beginTransaction(true, session);
            }
            ClientResponse response = new ClientResponse();
            response.setResult(true);
            Claim claim = this.getClaim();
            Product product = policy.getProduct();
            HashMap coveragesInfo = new HashMap();
            coveragesInfo.put("claim", claim);
            List cnrsPk = new ArrayList();
            Iterator<ClaimNormalReserve> itCnrs = claim.getNormalReserves().iterator();
            while (itCnrs.hasNext()) {
                ClaimNormalReserve cnr = itCnrs.next();
                loadCoverageConfInfo(product, coveragesInfo, cnr);
                cnrsPk.add(cnr.getEvaluatedCoverage().getPk());
            }

            //Cargamos todos los ARU y AIO asociados a cada una de las coberturas(CNR) del reclamo.
            HashMap ecovsList = new HashMap();

            Enumeration arus = policy.getRiskUnits();
            while (arus.hasMoreElements()) {
                AgregatedRiskUnit aru = (AgregatedRiskUnit) arus.nextElement();
                Enumeration ios = aru.getInsuranceObjects();

                while (ios.hasMoreElements()) {
                    AgregatedInsuranceObject aio = (AgregatedInsuranceObject) ios.nextElement();
                    Enumeration ecovs = aio.getEvaluatedCoverages();
                    while (ecovs.hasMoreElements()) {
                        EvaluatedCoverage ecov = (EvaluatedCoverage) ecovs.nextElement();
                        if (cnrsPk.contains(ecov.getPk())) {
                            HashMap map = new HashMap();
                            map.put("idRU", aru);
                            map.put("idIO", aio);
                            map.put("idCOV", ecov);
                            ecovsList.put(ecov.getPk(), map);
                        }
                    }
                }
            }

            //Buscamos todas las participaciones que tengan el rol insured en cada nivel de la poliza,
            //donde estas correspondan a los CNR del reclamo...
            Map listPartsCov = new HashMap();
            Map listPartsAll = new HashMap();
            boolean insuredInAnyLevels = false;
            if (ecovsList.size() != cnrsPk.size() || ecovsList.size() == 0 || cnrsPk.size() == 0) {
                log.error("The coverages number for the policy is inconsistent with the claim coverages. Please, check it!");
                log.error("Number Policy Coverages = " + ecovsList.size());
                log.error("Number Claim Coverages = " + cnrsPk.size());
            } else {
                RoleList roles = RoleGroup.INSURANCE_ROLES.getRoleList();

                Iterator iterator = cnrsPk.iterator();

                while (iterator.hasNext()) {
                    Collection<Participation> parstPOL = policy.getParticipationByRole(roles);
                    String covId = (String) iterator.next();
                    HashMap map = (HashMap) ecovsList.get(covId);
                    AgregatedRiskUnit aru = (AgregatedRiskUnit) map.get("idRU");
                    AgregatedInsuranceObject aio = (AgregatedInsuranceObject) map.get("idIO");
                    EvaluatedCoverage ec = (EvaluatedCoverage) map.get("idCOV");
                    Collection partsCov = ec.getParticipationByRole(roles);
                    Collection<Participation> partsIO = aio.getParticipationByRole(roles);
                    Collection parstRU = aru.getParticipationByRole(roles);
                    if (partsCov.size() > 0) {
                        listPartsCov.put(ec.getId(), partsCov);
                    }

                    //Verifica si existe algun asegurado en la poliza...
                    // En caso de que no haya no ebe generar la carta.
                    if (parstPOL.size() > 0 || parstRU.size() > 0 || partsIO.size() > 0 || listPartsCov.size() > 0) {
                        insuredInAnyLevels = true;
                    }

                    parstPOL.addAll(parstRU);
                    parstPOL.addAll(partsIO);
                    listPartsAll.put(ec.getId(), (parstPOL));
                }
            }
            setState(bean);
            response.putAttribute("claimNumber", claim.getClaimNumber());
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            response.putAttribute("coverageInfo", coveragesInfo);
            if (openTx) {
                commitTransaction(transaction, session);
            }
            //Se necesita haber realizado commit para generar cartas, en las mismas se necesitan valores actualizados en BD,
            //se separa la generación de Letters a un metodo fuera del commitTransaction
            log.debug("***[PMM]*** Generando Letters");
            processReserves(bean, insuredInAnyLevels, listPartsCov, cnrsPk, listPartsAll, ecovsList, product, dcoClaimDenied);
            return response;
        } catch (Exception e) {
            if (openTx) {
                rollbackTransaction(transaction, session);
            }
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            if (openTx) {
                log.debug("Closing the connection...");
                closeHibernateSession(session);
            }
        }
    }


    /**
     * @param bean
     * @param insuredInAnyLevels
     * @param listPartsCov
     * @param cnrsPk
     * @param listPartsAll
     * @param ecovsList
     */
    public void processReserves(ClaimStateBean bean, boolean insuredInAnyLevels, Map listPartsCov, List cnrsPk, Map listPartsAll, HashMap ecovsList,
                                Product product, Long dcoClaimDenied) {
        try {
            String timestamp = DateUtil.getDateToShow(claim.getClaimDate()) + " 00:00:00";
            Session session = getHibernateSession();
            if (bean.getClaimStatus() == ClaimStatus.CLOSED) {
                freeReserves(ClaimReserveAdjust.CLAIM_CLOSE_ADJUST);
                log.debug("***[PMM]*** session status before generateLetters = " + session.isOpen());
                generateLetters(timestamp, insuredInAnyLevels, listPartsCov, cnrsPk, listPartsAll, ecovsList, product, dcoClaimDenied);
                log.debug("***[PMM]*** session status after generateLetters = " + session.isOpen());
            } else if (bean.getClaimStatus() == ClaimStatus.RE_OPEN) {
                rollbackReserves();
            } else if (bean.getClaimStatus() == ClaimStatus.DENIED) {
                freeReserves(ClaimReserveAdjust.CLAIM_CANCEL_DUE_TO_DENIAL);
                log.debug("Generation of automatic letter for Claim status Denied");
                generateLetters(timestamp, insuredInAnyLevels, listPartsCov, cnrsPk, listPartsAll, ecovsList, product, dcoClaimDenied);
                //Generation of Automatic Letters
                /*try {
                    Iterator iter3 = listPartsAll.values().iterator();
                    Iterator iter2 = listPartsCov.values().iterator();
                    //Generando carta para cada TP con el rol asegurado de cada nivel de la poliza asociada al reclamo.
                    processGenerateLetterClaimDenied(claim, timestamp, iter2);
                    processGenerateLetterClaimDenied(claim, timestamp, iter3);
                } catch (Exception e) {
                    log.error("Can not generate automatic letter for Claim status Denied", e);
                }*/
            }
            session.flush();
        } catch (Exception e) {
            log.error("Can not generate automatic letter for Claim", e);
        }

    }

    private void loadCoverageConfInfo(Product product, HashMap coveragesInfo, ClaimNormalReserve cnr) {
        try {

            ClaimsCoverageConfiguration claimConf = ClaimsCoverageConfiguration
                    .load(Long.parseLong(product.getPk()), cnr.getEvaluatedCoverage().getConfiguratedCoverageOA().getId());
            List coverages = new ArrayList();
            coverages.add(cnr);
            coverages.add(claimConf);

            coveragesInfo.put(cnr.getDesc(), coverages);
        } catch (NumberFormatException e) {
            log.error(" Error loading claim coverage configuration for coverage " + cnr.getDesc(), e);
            throw new TechnicalException(" Error loading claim coverage configuration for coverage " + cnr.getDesc(), e);
        }
    }

    /**
     * @param
     * @param insuredInAnyLevels
     * @param listPartsCov
     * @param cnrsPk
     * @param listPartsAll
     * @param ecovsList
     */
    public void generateLetters(String timestamp, boolean insuredInAnyLevels, Map listPartsCov, List cnrsPk, Map listPartsAll, HashMap ecovsList,
                                Product product, Long dcoClaimDenied) {
        try {
//          if (AcseleConf.getProperty(TEMPLATE_CLAIM_DENIED).equals(bean.getCotname())) {
            //Generation of Automatic Letters for Claim Deny...
            try {
                TablaSimbolos symbolsTable = this.fillSymbolTableClaim(this.getClaim().getPk(), new TablaSimbolos(), dcoClaimDenied);

                LetterGenerator letterGenerator = (LetterGenerator) BeanFactory.getBean(LetterGenerator.LETTER_GENERATOR);

                log.debug("Generation of automatic letter for Claim status Denied for each coverage");
                if (insuredInAnyLevels) {
                    Iterator covs = listPartsCov.values().iterator();
                    while (covs.hasNext()) {
                        Collection list = (Collection) covs.next();
                        Iterator listParts = list.iterator();
                        while (listParts.hasNext()) {
                            Participation part = (Participation) listParts;
                            long coverageId = part.getAggregateParent().getId();
                            ThirdParty insurancedThirdParty = part.getThirdParty();

                            //Generando la carta para cada asegurado de la cobertura correspondiente...
                            letterGenerator
                                    .generateAutomaticLetterClaimByProductAndCoverage(LetterGenerator.DENY, symbolsTable, claim.getPk(), timestamp,
                                            true, coverageId, product.getPk(), insurancedThirdParty.getConfiguratedLanguageByCustomer(),
                                            insurancedThirdParty.getConfiguratedLanguageByDefault());
                        }
                    }
                    //Generando la carta para los demas asegurados de la poliza asociada al reclamo.
                    //Como podemos tener mas de una pàrticipacion con un rol asegurado a la misma
                    //cobertura, debemos validar que solo genere la carta para los demas niveles una
                    Iterator listIt = cnrsPk.iterator();
                    //sola vez.
                    while (listIt.hasNext()) {
                        String coverageId = (String) listIt.next();
                        log.debug("loading coverage " + coverageId);
                        ArrayList map = (ArrayList) listPartsAll.get(Long.valueOf(coverageId));

                        Iterator iter1 = map.iterator();
                        while (iter1.hasNext()) {
                            Participation part_ = (Participation) iter1.next();
                            ThirdParty insurancedThirdParty_ = part_.getThirdParty();
                            HashMap parents = (HashMap) ecovsList.get(coverageId);
                            log.debug("***[PMM]*** parents: " + parents);
                            EvaluatedCoverage ec = (EvaluatedCoverage) parents.get("idCOV");
                            log.debug("Generating letter for ConfiguratedCoverage " + ec.getConfiguratedCoverageOA().getId());
                            letterGenerator
                                    .generateAutomaticLetterClaimByProductAndCoverage(LetterGenerator.DENY, symbolsTable, claim.getPk(), timestamp,
                                            true, ec.getConfiguratedCoverageOA().getId(), product.getPk(), insurancedThirdParty_.getConfiguratedLanguageByCustomer(),
                                            insurancedThirdParty_.getConfiguratedLanguageByDefault());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Can not generate automatic letters for Claim status Closed for each coverage.", e);
            }
        } catch (Exception e) {
            log.error("Can not generate automatic letter for Claim", e);
        }
    }

    /**
     * Generation of letter to policy insuranced thirdparties  when claim is denied
     *
     * @param claim
     * @param timestamp
     * @param iter2
     * @throws ApplicationExceptionChecked
     */
    private void processGenerateLetterClaimDenied(Claim claim, String timestamp, Iterator iter2) throws ApplicationExceptionChecked {
        while (iter2.hasNext()) {
            ArrayList parts1 = (ArrayList) iter2.next();
            for (int i = 0; i < parts1.size(); i++) {
                Participation part = (Participation) parts1.get(i);
                ThirdParty insurancedThirdParty = part.getThirdParty();
                TablaSimbolos symbolsTable = this.fillSymbolTableClaim(this.getClaim().getPk(), new TablaSimbolos());
                LetterGenerator letterGenerator = (LetterGenerator) BeanFactory.getBean(LetterGenerator.LETTER_GENERATOR);
                letterGenerator.generateAutomaticLetterClaim(LetterGenerator.DENY, symbolsTable, claim.getPk(), timestamp, true,
                        insurancedThirdParty.getConfiguratedLanguageByCustomer(), insurancedThirdParty.getConfiguratedLanguageByDefault());
            }

        }
    }

    public ArrayList getConfiguratedCoverages(Claim claim, AgregatedPolicy policy) {
        ClaimInsuranceObject cio;
        Iterator paymentsReserve;
        ArrayList coverages = new ArrayList();
        log.debug("*** Claim:" + claim);
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit cru : claimRiskUnits) {
            Iterator io = cru.getClaimInsuranceObjects().values().iterator();
            while (io.hasNext()) { //the insurance object list
                cio = (ClaimInsuranceObject) io.next();
                cio = (ClaimInsuranceObject) HibernateUtil.load(ClaimInsuranceObject.class, cio.getPk());
                paymentsReserve = cio.getNormalReserves().values().iterator();
                while (paymentsReserve.hasNext()) {  //the reserve payments list
                    ClaimNormalReserve cnr = (ClaimNormalReserve) paymentsReserve.next();
                    EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                    ConfiguratedCoverage conf = ec.getConfiguratedCoverage();
                    coverages.add(conf);
                }
            }
        }
        return coverages;
    }

    public void sendToApproval(Collection paymentOrders) {
        String workflowModelName;
        String workflowOpName;
        WController controller = WController.Impl.getInstance();

        workflowModelName = AcseleConf.getProperty("claimApprovalModel");
        workflowOpName = AcseleConf.getProperty("claimApprovalOperation");

        log.debug("workflow Model = " + workflowModelName);
        log.debug("workflow Operation = " + workflowOpName);

        WorkFlowOperationType wfot = getWorkFlowOperationType(workflowModelName, workflowOpName);

        try {
            if (wfot != null) {
                for (Iterator iterator = paymentOrders.iterator(); iterator.hasNext(); ) {
                    PaymentOrder paymentOrder = (PaymentOrder) iterator.next();
                    controller.createOperation(wfot, EnumDocumentType.CLAIM_PAYMENT_ORDER,
                            paymentOrder.getPK(), null, null);
                }
                log.debug("Se creó la operacion ");
            } else {
                log.debug("No creó la operacion ");
                throw new ApplicationException("Error al intentar crear la operacion de workflow ", Severity.ERROR);
            }
        } catch (Exception e) {
            throw new TechnicalException("Error al intentar crear la operacion de workflow ", e);
        }
    }

    public static WorkFlowOperationType getWorkFlowOperationType(String workflowModelName, String workflowOpName) {

        WorkFlowModel models = WorkFlowModel.Impl.load(workflowModelName);
        WorkFlowOperationTypeList workFlowOperations = models.getWorkFlowOperationTypeListLastVersion();
        for (WorkFlowOperationType workFlowOperationType : workFlowOperations) {
            if (workFlowOperationType.getName().equals(workflowOpName) && workFlowOperationType
                    .getEnumWorkFlowOperationPublished() == EnumWorkFlowOperationPublished.OPERATION_PUBLISHED) {
                return workFlowOperationType;
            }
        }
        return null;
    }


    /**
     * Change the claim status
     *
     * @param claimId the new valid state (TO_BE_PAYED, PAYED, DENIED,CANCELED)
     */
    public String getPolicyId(String claimId) throws ApplicationExceptionChecked {
        Session session = null;
        try {
            session = getHibernateSession();
            return claim.getPolicyId();
        } catch (Exception e) {
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Get the Claim Operation Details Report
     *
     * @param claimOperationID a Client Request object
     * @return a ClientResponse object
     * @throws ApplicationExceptionChecked
     */
    public ClientResponse getReinsuranceClaimOperationDetails(String claimOperationID) throws ApplicationExceptionChecked {
        Session session = null;
        try {
            session = getHibernateSession();
            ClientResponse response = new ClientResponse();
            ReinsuranceDistributionService gcs = ReinsuranceDistributionService.Impl.getInstance();
            Map reinsuranceDetails = new HashMap();

            ClaimOperation claimOperation = null;
            try {
                reinsuranceDetails = gcs.getReinsuranceClaimOperationDetailsContract_PK_Key(claimOperationID);
                claimOperation = gcs.getReinsuranceClaimOperationInformation(claimOperationID);
            } catch (Exception e) {
                log.error("Error consulting the Claim Operations for Claim: " + claim.getPK(), e);
            }

            if (reinsuranceDetails != null && claimOperation != null) {
                response.putAttribute("reinsuranceClaimOperationDetails", reinsuranceDetails);
                response.putAttribute("reinsuranceClaimOperation", claimOperation);
            }

            response.setResult(true);
            return response;
        } catch (Exception e) {
            log.error("Error", e);
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * @param riskUnitId
     * @param insuranceObjectId
     * @param coverageId
     * @return String
     * @throws ApplicationExceptionChecked
     */
    public String getCoverageReserveLimit(String riskUnitId, String insuranceObjectId, String coverageId) throws ApplicationExceptionChecked {
        log.info("getCoverageReserveLimit() - Entrando");
        log.debug("riskUnitId = " + riskUnitId);
        log.debug("insuranceObjectId = " + insuranceObjectId);
        log.debug("coverageId = " + coverageId);
        String reserveLimit = null;
        Session session = null;
        try {
            session = getHibernateSession();
            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            ClaimUtil.fillEvaluator(policy.getProduct().getDCO(), evaluator);

            AgregatedRiskUnit agregatedRiskUnit = getAgregatedRiskUnitById(riskUnitId);
            EvaluatedCoverage evaluatedCoverage = null;
            if (agregatedRiskUnit != null) {
                Enumeration<AgregatedInsuranceObject> insuranceObjects = agregatedRiskUnit.getInsuranceObjects();
                uno:
                while (insuranceObjects.hasMoreElements()) {
                    AgregatedInsuranceObject agregatedInsuranceObject = insuranceObjects.nextElement();
                    if (agregatedInsuranceObject.getPk().equals(insuranceObjectId)) {
                        Enumeration<EvaluatedCoverage> evaluatedCoverages = agregatedInsuranceObject.getEvaluatedCoverages();
                        while (evaluatedCoverages.hasMoreElements()) {
                            EvaluatedCoverage coverage = evaluatedCoverages.nextElement();
                            if (String.valueOf(coverage.getId()).equalsIgnoreCase(coverageId)) {
                                evaluatedCoverage = coverage;
                                Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
                                for (ClaimRiskUnit claimRiskUnit : claimRiskUnits) {
                                    ClaimInsuranceObject claimInsuranceObject = claimRiskUnit.getClaimInsuranceObjects().get(agregatedInsuranceObject.getDesc());
                                    if (claimInsuranceObject != null) {
                                        ClaimUtil.fillEvaluator(claimInsuranceObject.getDamage(), evaluator);
                                    }
                                }
                                break uno;
                            }
                        }

                    }
                }

            }

            if (evaluatedCoverage == null) {
                evaluatedCoverage = getEvaluatedCoverageById(riskUnitId, insuranceObjectId, coverageId);
            }
            log.debug("evaluatedCoverage = " + evaluatedCoverage);
            evaluatedCoverage.publishAllSymbolsToUp(evaluator);

            evaluator.addSymbol(EvaluationConstants.CURRENT_ID_COVERAGE, evaluatedCoverage.getPk(), new Double(0), true);
            String ocurrenceDateStr = DateUtil.getDateToShow(getOcurrenceDate());
            evaluator.addSymbol(AcseleConf.getProperty(OCURRENCE_DATE), ocurrenceDateStr,
                    new Double(Funciones.dateTransformer.toNumber(ocurrenceDateStr)), true);
            ClaimUtil.fillEvaluator(evaluatedCoverage.getDCO(), evaluator);

            Currency polCurrency = policy.getCurrency();
//                polCurrency.load();
            String currencyName = polCurrency.getDescription();
            String currencyValue = String.valueOf(polCurrency.getId());
            log.debug("Policy's Financial Plan's Currency: '" + currencyName + "' (" + currencyValue + ")");

            String currencyKey = AcseleConf.getProperty("currency");

            // Coverage's DCO's Currency
            if (StringUtil.isEmptyOrNullValue(currencyName)) {
                DefaultConfigurableObject covDCO = evaluatedCoverage.getDCO();
                currencyName = covDCO.getCriterioInput(currencyKey);
                currencyValue = covDCO.getCriterioValue(currencyKey);
                log.debug("Coverage's DCO's Currency: '" + currencyName + "' (" + currencyValue + ")");
            }

            // Policy's DCO's Currency
            if (StringUtil.isEmptyOrNullValue(currencyName)) {
                DefaultConfigurableObject polDCO = policy.getDCO();
                currencyName = polDCO.getCriterioInput(currencyKey);
                currencyValue = polDCO.getCriterioValue(currencyKey);
            }

            try {
                log.debug("Publishing Policy's Currency '" + currencyName + "' (" + currencyValue + ") with symbol '" + currencyKey + "'.");
                evaluator.addSymbol(currencyKey, currencyName, new Double(currencyValue), true);
            } catch (NumberFormatException e) {
                String oiCurrencyKey = AcseleConf.getProperty("OpenItemCurrency");
                log.warn("Error in Policy's Currency.  Publishing Open Item's Currency (" + oiCurrencyKey + ") '" + currencyName + "' ("
                        + currencyValue + ") with symbol '" + currencyKey + "'.");
                Currency currency = Currency.Impl.load(oiCurrencyKey);
                evaluator.addSymbol(currencyKey, currency.getDescription(), Double.valueOf(currency.getId()), true);
            }

            ClaimsCoverageConfiguration ccc = ClaimsCoverageConfiguration.load(policy.getProduct().getId(), evaluatedCoverage.getCcId());
            ClaimUtil.fillEvaluator(evaluatedCoverage.getDCO(), evaluator);
            reserveLimit = String.valueOf(evaluator.evaluate(ccc.getMaxReserveAmount()));
            log.debug("reserveLimit = " + reserveLimit);
        } catch (Exception e) {
            log.error("ClaimComposerEJB.agregateCoverages -> calculando la reserva limite", e);
            throw new TechnicalException(Exceptions.CCAccessReserveLimit, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
            log.info("getCoverageReserveLimit() - Saliendo");
        }
        return reserveLimit;
    }

    public String getCoverageReserveLimit(EvaluatedCoverage evaluatedCoverage, ClaimInsuranceObject claimInsuranceObject) throws ApplicationExceptionChecked {
        String reserveLimit = null;
        Session session = null;
        try {
            session = getHibernateSession();
            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            ClaimUtil.fillEvaluator(policy.getProduct().getDCO(), evaluator);

            if (claimInsuranceObject != null) {
                ClaimUtil.fillEvaluator(claimInsuranceObject.getDamage(), evaluator);
            }


            log.debug("evaluatedCoverage = " + evaluatedCoverage);

            evaluatedCoverage.setAggregateParent(claimInsuranceObject.getAgregatedInsuranceObject());
            evaluatedCoverage.publishAllSymbolsToUp(evaluator);

            evaluator.addSymbol(EvaluationConstants.CURRENT_ID_COVERAGE, evaluatedCoverage.getPk(), new Double(0), true);
            String ocurrenceDateStr = DateUtil.getDateToShow(getOcurrenceDate());
            evaluator.addSymbol(AcseleConf.getProperty(OCURRENCE_DATE), ocurrenceDateStr,
                    new Double(Funciones.dateTransformer.toNumber(ocurrenceDateStr)), true);
            ClaimUtil.fillEvaluator(evaluatedCoverage.getDCO(), evaluator);

            Currency polCurrency = policy.getCurrency();
//                polCurrency.load();
            String currencyName = polCurrency.getDescription();
            String currencyValue = String.valueOf(polCurrency.getId());
            log.debug("Policy's Financial Plan's Currency: '" + currencyName + "' (" + currencyValue + ")");

            String currencyKey = AcseleConf.getProperty("currency");

            // Coverage's DCO's Currency
            if (StringUtil.isEmptyOrNullValue(currencyName)) {
                DefaultConfigurableObject covDCO = evaluatedCoverage.getDCO();
                currencyName = covDCO.getCriterioInput(currencyKey);
                currencyValue = covDCO.getCriterioValue(currencyKey);
                log.debug("Coverage's DCO's Currency: '" + currencyName + "' (" + currencyValue + ")");
            }

            // Policy's DCO's Currency
            if (StringUtil.isEmptyOrNullValue(currencyName)) {
                DefaultConfigurableObject polDCO = policy.getDCO();
                currencyName = polDCO.getCriterioInput(currencyKey);
                currencyValue = polDCO.getCriterioValue(currencyKey);
            }

            try {
                log.debug("Publishing Policy's Currency '" + currencyName + "' (" + currencyValue + ") with symbol '" + currencyKey + "'.");
                evaluator.addSymbol(currencyKey, currencyName, new Double(currencyValue), true);
            } catch (NumberFormatException e) {
                String oiCurrencyKey = AcseleConf.getProperty("OpenItemCurrency");
                log.warn("Error in Policy's Currency.  Publishing Open Item's Currency (" + oiCurrencyKey + ") '" + currencyName + "' ("
                        + currencyValue + ") with symbol '" + currencyKey + "'.");
                Currency currency = Currency.Impl.load(oiCurrencyKey);
                evaluator.addSymbol(currencyKey, currency.getDescription(), Double.valueOf(currency.getId()), true);
            }

            ClaimsCoverageConfiguration ccc = ClaimsCoverageConfiguration.load(policy.getProduct().getId(), evaluatedCoverage.getCcId());
            ClaimUtil.fillEvaluator(evaluatedCoverage.getDCO(), evaluator);
            reserveLimit = String.valueOf(evaluator.evaluate(ccc.getMaxReserveAmount()));
            log.debug("reserveLimit = " + reserveLimit);
        } catch (Exception e) {
            log.error("ClaimComposerEJB.agregateCoverages -> calculando la reserva limite", e);
            throw new TechnicalException(Exceptions.CCAccessReserveLimit, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
            log.info("getCoverageReserveLimit() - Saliendo");
        }
        return reserveLimit;
    }

    @Override
    public String getCoverageReserveLimit(EvaluatedCoverage evaluatedCoverage) throws ApplicationExceptionChecked, RemoteException {
        return getCoverageReserveLimit(evaluatedCoverage.getAgregatedInsuranceObject().getAgregatedRiskUnit().getPk(), evaluatedCoverage.getAgregatedInsuranceObject().getPk(), evaluatedCoverage.getPk());
    }

    /**
     * Updates the details of the payment orders associated to a claim.
     *
     * @param paymentOrderCollection
     * @param paymentOrder
     * @param paymentOrderMaxObligation
     * @param isSubmit
     * @throws ApplicationExceptionChecked
     */
    public void updateClaimPaymentsDetailOrders(PaymentOrderCollection paymentOrderCollection, PaymentOrder paymentOrder,
                                                double paymentOrderMaxObligation, boolean isSubmit) throws ApplicationExceptionChecked {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(false, session);
            reloadClaim();
            double totalAmount = paymentOrderCollection.getPaidAmount();
            if ((paymentOrder.getPK() != null)) {
                //if (paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.CANCELED_STATE)
                {
                    updateAmountPayment(paymentOrder);
                    // save State in the history payments order

                    paymentOrder.setHistoryStatePaymentsFile();
                    if ((paymentOrder.getClaimReserve() instanceof ClaimNormalReserve) && (paymentOrder.isTotalPayment())) {
                        canceledPaymentOrders(paymentOrder);
                    } else {
                        paymentOrder.update();
                    }
                }

            }

            /**
             * To avoid pay amounts greater than reserve amount
             */
            if (totalAmount > paymentOrderCollection.getReserveAmount()) {

                throw new IllegalArgumentException(
                        "The total amount to pay = " + totalAmount + " is greater than the reserve amount = " + paymentOrderCollection
                                .getReserveAmount());

            }
            //paymentOrderCollection.addToMaxObligation(paymentOrderMaxObligation);
            if (paymentOrder.getPk() > 0) {
                commitTransaction(transaction, session);
            }
        } catch (Exception ex) {
            log.error("Error generando Detalle de Orden de Pago....", ex);
            rollbackTransaction(transaction, session);
            throw new TechnicalException(Exceptions.CCAccessReserveLimit, Severity.ERROR, ex);
        } finally {
            closeHibernateSession(session);
        }
    }


    /**
     * Associate a object to an Aggregated insurance object
     *
     * @param damageTemplateName
     * @param aruId              The aggregated risk unit id
     * @param idSource           The aggregated insurance object id
     * @return the response with the new node of the claim tree
     * @throws ApplicationExceptionChecked
     */
    public ClientResponse associateToAgregatedInsuranceObject(String damageTemplateName, String aruId, String idSource,
                                                              String login) throws ApplicationExceptionChecked {
        ClientResponse response = new ClientResponse();
        Session session = null;
        try {
            session = getHibernateSession();
            response.setResult(false);
            AgregatedRiskUnit aru = getAgregatedRiskUnitById(aruId);
            AgregatedInsuranceObject aio = getAgregatedInsuranceObject(aru, idSource);
            log.debug("damageTemplateName: " + damageTemplateName);
            Categorias categorias = Categorias.getBean(Categorias.EXTENDED_TEMPLATES_STATE);
            ConfigurableObjectType cot =
                    (ConfigurableObjectType) categorias.get(CotType.CLAIM, damageTemplateName);
            log.debug("[DEBUG] Claim Entity EJB  Claim Insurance Object Created");
            log.debug("cot = " + cot);

            ExpresionEvaluator expresionEvaluator = createExpresion(aio);

            DefaultConfigurableObject damage = DefaultConfigurableObject.create(cot);
            damage = evaluateDCO(expresionEvaluator, damage, cot);
            damage.save();

            response.putAttribute("DescObjectNew", aio.getDesc());
            response.putAttribute("damageDcoPk", damage.getPk());
            response.setResult(true);
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            response.putAttribute("damageTemplate", damage);

            try {
                HibernateUtil.getSession().flush();
            } catch (HibernateException e) {
                log.warn("Already flushed!");
            }
            return response;
        } catch (Exception e) {
            response.setResult(false);
            log.error(" Internal error :  ", e);
            response.putAttribute("msg", e.getMessage() == null ? "" : e.getMessage());
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * @param damageTemplateName Template Name of damage
     * @return ClientResponse
     * @throws ApplicationExceptionChecked if an exception is throw
     * @throws RemoteException             if an exception is throw
     */
    public ClientResponse saveDamageTemplate(String damageTemplateName) throws ApplicationExceptionChecked {
        log.info("associateToAgregatedInsuranceObject(....) - Entrando");
        log.debug("damageTemplateName = " + damageTemplateName);
        ClientResponse response = new ClientResponse();
        Session session = null;
        try {
            session = getHibernateSession();
            response.setResult(false);
            Categorias categorias = Categorias.getBean(Categorias.EXTENDED_TEMPLATES_STATE);
            ConfigurableObjectType cot =
                    (ConfigurableObjectType) categorias.get(CotType.CLAIM, damageTemplateName);
            log.debug("cot -> " + cot);
            DefaultConfigurableObject damage = DefaultConfigurableObject.create(cot);

            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            evaluator.addSymbol(EvaluationConstants.POLICY_PK, this.getAgregatedPolicy().getPk(), new Double(this.getAgregatedPolicy().getPk()), false);
            evaluator.addSymbol(EvaluationConstants.CLAIM_PK, this.getClaim().getPk(), new Double(this.getClaim().getPk()), false);
            damage.evaluate(evaluator);

            damage.save();
            response.putAttribute("damageDcoPk", damage.getPk());
            response.setResult(true);
            log.debug("damageDcoPk-> " + damage.getPk());
            return response;
        } catch (Exception e) {
            response.setResult(false);
            log.error(" Internal error :  ", e);
            response.putAttribute("msg", e.getMessage() == null ? "" : e.getMessage());
            throw new TechnicalException(Exceptions.CCSystemError, Severity.ERROR, e);
        } finally {
            closeHibernateSession(session);
            log.debug("associateToAgregatedInsuranceObject(....) - Saliendo");
        }
    }

    /**
     * Associate a object to an Evaluated coverage
     *
     * @param aioId          The aggregated insurance object id
     * @param aruId          The aggregated risk unit id
     * @param login          The client login
     * @param idSource       The evaluated coverage id
     * @param idTarget       The claim insurance object id
     * @param setting
     * @param typeOfPayments
     * @param periodicity
     * @return the response with the new node of the claim tree
     * @throws ApplicationExceptionChecked
     * @throws RemoteException
     */
    public ClientResponse associateToEvaluatedCoverage(ClaimInsuranceObject cio, ClaimRiskUnit cru, String login, EvaluatedCoverage coverage, String idTarget, Object setting,
                                                       int typeOfPayments, int periodicity) throws ApplicationExceptionChecked, RemoteException {
        Vector datos = (Vector) setting;
        String maxBenefitAmountStr = (String) datos.elementAt(0);
        String currencyIdStr = (String) datos.elementAt(1);
        String limitReserveStr = (String) datos.elementAt(2);
        String benefitPaymentsStr = (String) datos.elementAt(3);

        CoverageForm coverageForm = new CoverageForm();
        coverageForm.setIoID(cio.getPk());
        coverageForm.setRuID(cru.getPk());
        coverageForm.setCoverageID(coverage.getPk());
        coverageForm.setEvaluatedCoverage(coverage);
        coverageForm.setClaimRiskUnit(cru);
        coverageForm.setClaimInsuranceObject(cio);

        coverageForm.setMaxBenefitAmount(maxBenefitAmountStr);
        coverageForm.setCurrency(currencyIdStr);
        coverageForm.setReserveLimit(limitReserveStr);


        return associateToEvaluatedCoverage(coverageForm);
    }

    /**
     * Associate a object to an Evaluated coverage
     *
     * @param form The ActionForm with all the data
     * @return the response with the new node of the claim tree
     * @throws ApplicationExceptionChecked
     */
    public ClientResponse associateToEvaluatedCoverage(CoverageForm form) throws ApplicationExceptionChecked {
        log.debug("---------- associateToEvaluatedCoverage ----------");
        ClientResponse response = new ClientResponse();

        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(false, session);
            String login = UserInfo.getUser();
            response.setResult(false);
            String riskUnitId = form.getRuID();
            String insuranceObjectId = form.getIoID();
            String coverageId = form.getCoverageID();
            log.debug("[Acsel-e] riskUnitId = " + riskUnitId);
            log.debug("[Acsel-e] insuranceObjectId = " + insuranceObjectId);
            log.debug("[Acsel-e] coverageId = " + coverageId);
            EvaluatedCoverage ec = searchEvaluatedCoverage(riskUnitId, insuranceObjectId, coverageId);
            log.debug("ec.getDesc() = " + ec.getDesc());
            ClaimInsuranceObject cio = searchClaimInsuranceObject(riskUnitId, insuranceObjectId);

            String[] amounts = form.getItemsAmountDistribution().split(",");
            for (int i = 0; i < amounts.length; i++) {
                //******************************************************************************************************************
                ClaimNormalReserve cnr = new ClaimNormalReserve(cio, ec, this);

                if (form.getIsDistributionAjust()) {
                    cnr.setMaxBenefitAmount(Double.valueOf(amounts[i]));
                    if (form.getItemAdjust() != null) {
                        cnr.setItemAjust(form.getItemAdjust());
                    } else {
                        cnr.setItemAjust(i + 1);
                    }
                } else {
                    cnr.setItemAjust(null);
                    cnr.setMaxBenefitAmount(Double.valueOf(form.getMaxBenefitAmount()));
                }
                cnr.setDoneBy(login);
                String registerClaimNotActivePolicy = AcseleConf.getProperty("registerClaim.with.NotActivePolicy");
                log.debug("registerClaimNotActivePolicy = " + registerClaimNotActivePolicy);
                //Default validation, don't allow register coverage if policy is cancelled or ocurrenceDate of Claim is out
                int productType = policy.getProduct().getProductBehaviour().getProductType();
                boolean isLifeOrWarranty = productType == ProductBehaviour.LIFE_PRODUCT || productType == ProductBehaviour.WARRANTY_PRODUCT;
                log.debug("isLifeOrWarranty = " + isLifeOrWarranty);
                if (registerClaimNotActivePolicy.equals("0")) {
                    boolean cancelledState = isCancelState(ec.getState().getDesc());
                    boolean suspendedState = isSuspendedState(ec.getState().getDesc());
                    Date initialDate = ec.getInitialDate();
                    boolean allowRetroactive = allowRetroactive(ec);
                    if (allowRetroactive) {
                        initialDate = getEmissionDate(getAgregatedPolicy(ec));
                    }
                    //TODO: Que demonios es que un productos no sea esto? ( !isLifeOrWarranty ) Commented

                    if ((cancelledState || suspendedState)) {
                        log.debug(Exceptions.CCErrorPolicyCancelToClaim);
                        response = addClaimNotActivePolicy(false, response, null);
                    } else {
                        log.debug("Associate EvaluatedCoverage" + cnr);
                        log.debug("Associate EvaluatedCoverage" + cnr.getPK() + " Desc" + cnr.getDesc());
                        log.debug("[cio] pk " + cio.getPk());
                        log.debug("ClaimComposerEJB.associateToEvaluatedCoverage : Cobertura ya siniestrada");
                        log.debug("[DEBUG] Claim Entity EJB Associate Coverage ");
                        associateEvaluatedCoverage(cio, cnr, login, ec, form, HibernateUtil.getConnection());
                        cancelPolicy(cio, ec, response);

                        response = addClaimNotActivePolicy(true, response, cnr);
                        commitTransaction(transaction, session);
                    }
                } else {

                    log.debug("Associate EvaluatedCoverage" + cnr);
                    log.debug("Associate EvaluatedCoverage" + cnr.getPK() + " Desc" + cnr.getDesc());
                    log.debug("[cio] pk " + cio.getPk());
                    log.debug("ClaimComposerEJB.associateToEvaluatedCoverage : Cobertura ya siniestrada");
                    log.debug("[DEBUG] Claim Entity EJB Associate Coverage ");
                    associateEvaluatedCoverage(cio, cnr, login, ec, form, HibernateUtil.getConnection());
                    cancelPolicy(cio, ec, response);
                    response = addClaimNotActivePolicy(true, response, cnr);
                    commitTransaction(transaction, session);

                }
            }

            return response;

        } catch (AcseleException e) {
            log.error("Error", e);
            if (!Exceptions.PPErrorApplyingEventToActiveOperation.equals(e.getKeyCode())) {
                response.putAttribute("msg", e.getMessage());
                response.putAttribute("keyCode", e.getKeyCode());
            }
            response.setResult(false);
            rollbackTransaction(transaction, session);
            return response;
            //throw e;
        } catch (Exception e) {
            log.error("Error", e);
            response.putAttribute("msg", e.getMessage() == null ? "Error" : e.getMessage());
            response.putAttribute("keyCode", Exceptions.UNKNOWN_EXCEPTION);
            response.setResult(false);
            rollbackTransaction(transaction, session);
            return response;

        } finally {
            closeHibernateSession(session);
        }
    }

    private void cancelPolicy(ClaimInsuranceObject cio, EvaluatedCoverage ec, ClientResponse response) throws Exception {
        Claim claim = cio.getContainer().getContainer();
        Policy policyApi = claim.getPolicy();
        Policy lastVersionPolicy = Policy.Impl.loadById(policy.getId());
        if(lastVersionPolicy.getStateOA()!=null) {
            if (!isCancelState(lastVersionPolicy.getStateOA().getDesc())) {
                ClaimsCoverageConfiguration ccc = this.getClaimCoverageConfiguration(cio.getContainer().getPk(), cio.getPk(), ec);
                GNUEvaluator traductor = GNUEvaluator.getInstance();
                try {
                    if (ccc.getIsCancelled()) {
                        String formula = ccc.getCancelledFormula();
                        if (!StringUtil.isEmptyOrNullValue(formula)) {
                            TablaSimbolos tablaSimbolos = this.publishSymbolsTable(claim, ec);
                            ExpresionEvaluator evaluator = this.fillSymbolClaim(String.valueOf(claim.getId()), new TablaSimbolos());

                            if (cio != null) {
                                addSymbols(evaluator, cio, getAgregatedPolicy());
                            }
                            boolean condition = traductor.evaluateLogical(formula, evaluator.getTablaSimbolos());

                            log.debug("##Condition to apply cancel: " + formula + ", result: " + condition);
                            if (condition) {
                                //cancelar la póliza
                                String eventDate = "";
                                long lastVersionPolicyOperationId = lastVersionPolicy.getOperationId();
                                String notificationProperty = AcseleConf.getProperty("ocurrenceDate");
                                String ocurrenceDate = ((String) cio.getDamage().getCriterioInput(notificationProperty));

                            double notificationDateDouble = Double.parseDouble((String) cio.getDamage().getValues().get(notificationProperty));
                            Date ocurranceDateF = DateUtil.getDateToParse(ocurrenceDate);
                            if(lastVersionPolicyOperationId>0){
                                OperationPK operationPK = OperationPK.getByPk(lastVersionPolicyOperationId);
                                java.sql.Date operationPKTimeStamp = operationPK.getTimeStamp();
                                log.debug("ClaimComposerWrapper: operation date" +operationPKTimeStamp.getTime());
                                SimpleDateFormat formatLongShow = DateUtil.getFormatLongShow();
                                String dateOperationPkFormat = formatLongShow.format(operationPKTimeStamp.getTime());
                                log.debug("ClaimComposerWrapper: ocurrance date" +ocurrenceDate);
                                Date operationDate = formatLongShow.parse(dateOperationPkFormat);
                                Date ocurrenceD = ocurranceDateF;
                                log.debug("ClaimComposerWrapper: operation date" +operationDate);
                                log.debug("ClaimComposerWrapper: ocurrenceD date" +ocurrenceD);
                                boolean greatherDate = DateUtil.DateGreaterThanDate(ocurrenceD, operationDate);
                                if(greatherDate){
                                    ocurrenceD= DateUtil.sumDaysToDate(ocurrenceD,1);
                                    eventDate=DateUtil.getDateToShow(ocurrenceD);
                                }else{

                                        eventDate = DateUtil.getDateToShow(operationDate);
                                    }
                                    log.debug("ClaimComposerWrapper: Event date" + ocurrenceD);

                                }


                /*Cancelacion de openitems*/

                                String premiumDocument = AcseleConf.getProperty("premiumConcept");
                                Date notificationDate = DateUtil.getDateToParse(Funciones.dateToString(notificationDateDouble));
                                long premiumDocumentId = DocType.Impl.load(premiumDocument).getId();
                                List openItemList = OpenItemImpl.findByAnyCriteria(null, null,
                                        StatusMovement.ACTIVE.getValue(), notificationDate, null,
                                        policyApi.getId(),
                                        null, premiumDocumentId, -1, null, -1, -1, null);

                                for (Iterator iterator = openItemList.iterator(); iterator.hasNext(); ) {
                                    OpenItem oiPremium = (OpenItem) iterator.next();
                                    try {
                                        if (DateUtil.DateLessThanDate(notificationDate, oiPremium.getDocDate())) {
                                            oiPremium.setStatus(StatusMovement.CANCELLED.getValue());
                                            OpenItemImpl.scheduleOpenitem((OpenItemImpl) oiPremium, false);
                                        }
                                    } catch (Exception ex) {
                                        OpenItemImpl.cleanOpenItemsToProcess(OpenItemReferenceType.POLICY, policy.getId());
                                        log.error("Error updating openitem state: " + ex.getMessage());
                                        throw new TechnicalException(Exceptions.PPErrorProcessingUAAMovement);
                                    }
                                }

                                com.consisint.acsele.template.lifecycle.beans.api.EventType eventCancel = null;
                                Map<String, com.consisint.acsele.template.lifecycle.beans.api.EventType> eventTypeMap = lastVersionPolicy.nextEventsMap();
                                Iterator iterator = eventTypeMap.values().iterator();
                                //Utilizo el primer evento de cancelacion de los configurados
                                String claimCancellation = AcseleConf.getProperty("claimCancellation");
                                log.debug("ClaimCancelation: " + claimCancellation);
                                boolean emptyClaimCancellation = StringUtil.isEmptyOrNullValue(claimCancellation);
                                log.debug("EmptyClaimCancellation : " + emptyClaimCancellation);
                                while (iterator.hasNext()) {
                                    com.consisint.acsele.template.lifecycle.beans.api.EventType event = (com.consisint.acsele.template.lifecycle.beans.api.EventType) iterator.next();
                                    if (eventCancel == null) {
                                        if (event.getEnumCategoryEventType() == EnumCategoryEventType.CANCELLATION) {
                                            eventCancel = event;
                                            if (emptyClaimCancellation) {
                                                break;
                                            }


                                        }
                                    }
                                    if (event.getDesc().equals(claimCancellation) && event.getEnumCategoryEventType() == EnumCategoryEventType.CANCELLATION) {
                                        eventCancel = event;
                                        break;
                                    }
                                }

                                if (eventCancel != null) {
                                    log.debug("EventCancel : " + eventCancel.getDesc());
                                    String operationDateKey = AcseleConf.getProperty(EventType.OPERATION_DATE);
                                    String effectiveDateKey = AcseleConf.getProperty(EventType.EFFECTIVE_DATE);
                                    String cancelDate = AcseleConf.getProperty(EventType.CANCEL_DATE);


                                    HashMap eventProperties = new HashMap();
                                    String cancelReasonInput = StringUtil.EMPTY_STRING;
                                    String cancelReasonProp = StringUtil.EMPTY_STRING;
                                    String cancellationMotiveSP = StringUtil.EMPTY_STRING;
                                    String cancellationMotive = AcseleConf.getProperty("cancellationMotive");
                                    if (cancellationMotive != null) {
                                        cancellationMotiveSP = cancellationMotive.trim();
                                        log.debug("#### cancellationMotiveSP = " + cancellationMotiveSP);
                                        //It returns a list of properties. Tokenize it.
                                        StringTokenizer tok = new StringTokenizer(cancellationMotiveSP, StringUtil.COMMA);
                                        boolean found = false;
                                        Property prop = null;
                                        PropertyList eventProperty = eventCancel.getTemplateEvent().getPropertyList();
                                        //Search for the cancelMotive valid for the policyApi
                                        while (tok.hasMoreTokens() && !found) {
                                            String cancelM = tok.nextToken();
                                            for (Property property : eventProperty.getAll()) {
                                                if (property.getDescription().equals(cancelM)) {
                                                    prop = property;
                                                    found = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (prop != null) {
                                            cancelReasonProp = prop.getDescription();
                                            TransformerList transformer = prop.getTransformerList();
                                            for (Transformer transform : transformer.getAll()) {

                                                if ((int) transform.getValue() == EventType.CANCELLATION_BY_CLAIM) {
                                                    cancelReasonInput = transform.getInput();
                                                    break;
                                                }
                                            }
                                        } else {
                                            log.error("Property not found for: " + cancellationMotiveSP);
                                            throw new TechnicalException(Exceptions.PPErrorGettingEvaluatedPropertyValue);
                                        }
                                    } else {
                                        OpenItemImpl.cleanOpenItemsToProcess(OpenItemReferenceType.POLICY, policy.getId());
                                        log.error("Property not found for: " + cancellationMotiveSP);
                                        throw new TechnicalException(Exceptions.PPErrorGettingEvaluatedPropertyValue);
                                    }

                                eventProperties.put(operationDateKey, eventDate);
                                eventProperties.put(cancelReasonProp, cancelReasonInput);
                                boolean greatherDateEffective = DateUtil.DateGreaterThanDate(ocurranceDateF, lastVersionPolicy.getInitialDate());
                                if(greatherDateEffective){
                                    eventProperties.put(effectiveDateKey, ocurrenceDate);
                                }else{
                                    eventProperties.put(effectiveDateKey, DateUtil.getDateToShow(lastVersionPolicy.getInitialDate()));
                                }
                                if(!StringUtil.isEmptyOrNullValue(cancelDate)){
                                    eventProperties.put(cancelDate, eventDate);
                                }
                                eventCancel.updateAutomaticDate(eventDate);

                                    log.debug("##set event to lastVersionPolicy");
                                    lastVersionPolicy.setEvent(eventCancel.getDesc());
                                    lastVersionPolicy.updateEventData(eventProperties);
                                    log.debug("##set event to operation: " + lastVersionPolicy.getOperationId());
                                    lastVersionPolicy.calculate();
                                    Map errors = lastVersionPolicy.apply();
                                    Iterator it = errors.entrySet().iterator();
                                    List cont = new ArrayList<String>();
                                    while (it.hasNext()) {
                                        Map.Entry ent = (Map.Entry) it.next();
                                        cont = (ArrayList) ent.getValue();
                                    }
                                    if (cont.size() > 0) {
                                        if (cont.iterator().hasNext()) {
                                            response.setMessage((String) cont.iterator().next());
                                            throw new TechnicalException(Exceptions.PPErrorApplyingEventToActiveOperation);
                                        }
                                    }
                                } else {
                                    log.error("Event cancellation not found for: " + policyApi.getPolicyNumber());
                                    throw new TechnicalException(Exceptions.PPErrorEventNotFoundInLifeCycle);
                                }

                            } else {
                                OpenItemImpl.cleanOpenItemsToProcess(OpenItemReferenceType.POLICY, policy.getId());
                                log.error("unapproved formula: ".concat(formula));
                            }

                        }
                    }
                } catch (AcseleException ae) {
                    OpenItemImpl.cleanOpenItemsToProcess(OpenItemReferenceType.POLICY, claim != null && claim.getAgregatedPolicy() != null ? claim.getAgregatedPolicy().getId() : 0);

                    log.error("Acsele Exception: " + ae.getMessage());
                    throw ae;
                } catch (Exception ex) {
                    OpenItemImpl.cleanOpenItemsToProcess(OpenItemReferenceType.POLICY, claim != null && claim.getAgregatedPolicy() != null ? claim.getAgregatedPolicy().getId() : 0);
                    log.error("Exception: " + ex.getMessage());
                    throw ex;
                }
            }
        }
    }


    private AgregatedPolicy getAgregatedPolicy(EvaluatedCoverage evaluatedCoverage) {
        AgregatedInsuranceObject aio = evaluatedCoverage.getAgregatedInsuranceObject();
        AgregatedRiskUnit aru = aio.getAgregatedRiskUnit();
        return aru.getAgregatedPolicy();
    }

    private boolean allowRetroactive(EvaluatedCoverage evaluatedCoverage) {
        AgregatedPolicy ap = getAgregatedPolicy(evaluatedCoverage);
        ProductBehaviour productBehaviour = ap.getProduct().getProductBehaviour();
        return productBehaviour.isAllowRetroactive();
    }

    private ClientResponse addClaimNotActivePolicy(boolean allow, ClientResponse response, ClaimNormalReserve cnr) {
        if (allow) {
            response.putAttribute("DescObjectNew", cnr.getDesc());
            response.putAttribute("cnrpk", cnr.getPK());
            response.putAttribute("claimNormalReserve", cnr);
            response.setResult(true);
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
        } else {
            response.putAttribute("DateClaimgreater", getOcurrenceDate());
            response.setResult(false);
            response.putAttribute("errorCancel", Exceptions.CCErrorPolicyCancelToClaim);
        }
        return response;
    }

    /**
     * Associate a object to a reserve concept
     *
     * @param idSource The reserve concept id
     * @param idTarget The claim insurance object id
     * @param setting
     * @param login
     * @param cruId    The claim risk unit id
     * @return the response with the new node of the claim tree
     * @throws ApplicationExceptionChecked
     */

    public ClientResponse associateToReserveConcept(String idSource, String idTarget, Object setting, String login,
                                                    String cruId) throws ApplicationExceptionChecked, RemoteException {
        return associateToReserveConcept(idSource, idTarget, setting, login, cruId, null);
    }

    public ClientResponse associateToReserveConcept(String idSource, String idTarget, Object setting, String login, String cruId,
                                                    String normalReserveId) throws ApplicationExceptionChecked, RemoteException {
        ClientResponse response = new ClientResponse();
        try {
            ReserveConcept rc = ReserveConcept.load(idSource);
            Currency currency = Currency.Impl.load(Long.valueOf((String) ((Vector) setting).elementAt(1)));

            String amountStr = (String) ((Vector) setting).elementAt(0);
            response = associateToReserveConcept(rc, currency, Double.parseDouble(amountStr), cruId, idTarget, login, normalReserveId);
        } catch (Exception e) {
            log.error("Error", e);
            response.putAttribute("msg", (e.getMessage() != null ? e.getMessage() : "NullPointerException"));
            response.setResult(false);
        }
        return response;
    }


    public ClientResponse associateToReserveConcept(ReserveConcept rc, Currency currency, double amount, String claimRiskUnitid,
                                                    String insuranceObjectId, String login,
                                                    String normalReserveId) throws ApplicationExceptionChecked, RemoteException {
        ClientResponse response = new ClientResponse();
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(false, session);
            response.setResult(false);
            log.debug("Reserve concept: " + rc.getPK() + rc.getDesc());
            ClaimInsuranceObject cio = searchClaimInsuranceObject(claimRiskUnitid, insuranceObjectId);
            log.debug("ClaimInsuranceObject: " + cio);
            ClaimReserveByConcept crc = new ClaimReserveByConcept(rc);
            crc.setContainer(cio);
            crc.setCurrency(currency);
            crc.setDoneBy(login);
            if (!StringUtil.isEmptyOrNullValue(normalReserveId))
                crc.setNormalReserveId(Long.parseLong(normalReserveId));

            crc.save();
            ClaimReserveAdjust cra = new ClaimReserveAdjust(crc.getDesc(), new java.util.Date(), amount, login,
                    ClaimReserveAdjust.initialAdjustReasonKey, ReserveAdjustType.INITIAL.getValue());
            cra.setClaimReserveId(crc.getPK());
            crc.setInitialAdjust(cra);
            generateConceptReserveReinsuranceDistribution(crc, amount, Constants.RESERVE_CLAIM, ReserveAdjustType.INITIAL);

            ClaimHistorical claimHistorical = new ClaimHistorical();
            String legacyType = OIMTPClaimEnum.ADMINISTRATIVA_BY_CONCEPT.getLabel();
            claimHistorical.generateHistoricalWithMovement(this.claim, ClaimHistoricalOperationType.CREATE_CLAIM, ClaimHistoricalMovementType.CLAIM_CREATE, Long.parseLong(cra.getPK()), legacyType);

            legacyType = legacyType == null ? OIMTPClaimEnum.ADMINISTRATIVA_BY_CONCEPT.getLabel() : legacyType + OIMTPClaimEnum.BY_CONCEPT;

            claimHistorical.generateHistoricalWithMovement(this.claim, ClaimHistoricalOperationType.ADD_RESERVE, ClaimHistoricalMovementType.CHANGE_RESERVE, Long.parseLong(cra.getPK()), legacyType);

            cio.add(crc);
            ClaimNormalReserve normalReserve = null;
            EvaluatedCoverage ec = null;
            try {
                normalReserve = ClaimNormalReserve.load(normalReserveId);
                if (normalReserve != null) {
                    ec = normalReserve.getEvaluatedCoverage();
                }
            } catch (Exception e) {
                log.debug("There is no Claim Normal Reserve: " + e);
            }
            AuditTrailManager.generateClaimAuditTrail(new Date(), CustomAuditItem.ADD_CONCEPTUS_RESERVES, claim, Long.valueOf(cra.getPK()), Long.valueOf(crc.getPk()));
            entryReserve(crc.getAmount().doubleValue(), ReserveAdjustType.INITIAL.getValue(), crc.getDoneBy(), currency.getIsoCode(), ec, crc);
            log.debug("[DEBUG] Claim Entity EJB Associate Reserve Concept " + crc.getReserveConcept().getConcept());
            response.putAttribute("DescObjectNew", rc.getDesc());
            response.setResult(true);
            response.putAttribute("claimstate", claim.getClaimStatus().getValue());
            response.putAttribute("claimReserveByConcept", crc);
            log.debug("Estado del Claim: " + claim.getClaimStatus().toString());
            commitTransaction(transaction, session);
        } catch (Exception e) {
            log.error("Error", e);
            response.putAttribute("msg", (e.getMessage() != null ? e.getMessage() : "NullPointerException"));
            response.setResult(false);
            rollbackTransaction(transaction, session);
        } finally {
            closeHibernateSession(session);
        }
        return response;
    }

    /**
     *
     */
    public void testLoad() {
    }

    /**
     * It gets the asocciated clim object
     *
     * @return the asocciated clim object
     */
    public Claim getClaim() throws ApplicationExceptionChecked {
        boolean isSessionNull = HibernateUtil.isSessionNull();
        log.debug("[getClaim] isSessionNull " + isSessionNull);
        Session session = null;
        if (claim != null && asl) {
            return claim;
        }
        try {
            session = getHibernateSession();
            claim = Claim.getInstance(Long.valueOf(claim.getPk()));
            return claim;
        } catch (Exception e) {
            log.error("Error # " + Exceptions.CCErrorGettingClaim);
            log.error("The available environment snapshot is: \n" + "claim: " + this.claim);
            if (claim != null) {
                log.error("The available environment snapshot is: \n" + "claimId: " + this.claim.getPk());
            }
            ExceptionUtil.handleException(Exceptions.CCErrorGettingClaim, e);
            return null;
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Wrapper method to invoke HibernateUtil.beginTransaction(sessionHibernate)
     */
    private Transaction beginTransaction(boolean reload, Session session) {
        if (isStartTx()) {
            beginTransactions = true;
            if (reload && !asl) {
                reloadClaim();
            }
            return ((!asl) ? HibernateUtil.beginTransaction(session) : null);
        } else {
            return null;
        }
    }

    /**
     * Wrapper method to invoke HibernateUtil.commit(tx, sessionHibernate)
     */
    private void commitTransaction(Transaction tx, Session session) {
        if (!asl && (beginTransactions && isStartTx())) {
            HibernateUtil.commit(tx, session);
        }
    }

    /**
     * Wrapper method to invoke HibernateUtil.rollBack(tx)
     */
    private void rollbackTransaction(Transaction tx, Session session) {
        if (!asl && beginTransactions && isStartTx()) {
            HibernateUtil.rollBack(tx);
        }
    }

    /**
     * Resets the session object stored in the HibernateUtil class with the ClaimComposerEJB stored
     * session if the latter is null.
     * Previously named resetNullHibernateSessionIfNecessary
     */
    private Session getHibernateSession() {
        return HibernateUtil.getSession();
    }

    /**
     * @param aruId
     * @param ioID
     * @param response
     */

    private void desassociateInsuranceObject(String aruId, String ioID, ClientResponse response) throws ApplicationExceptionChecked {

        log.debug("aruId = " + aruId);
        ClaimRiskUnit cru = null;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit claimRiskUnit : claimRiskUnits) {
            if (claimRiskUnit.getAgregatedRiskUnit().getPk().equals(aruId)) {
                cru = claimRiskUnit;
            }
        }
        if (cru != null) {
            log.debug("Desasociando InsuranceObject: ioID = " + ioID);
            ClaimInsuranceObject cio = cru.getClaimInsuranceObjects().get(ioID);
            cio.load();
            log.debug("cio.getNormalReserves().entrySet() = " + cio.getNormalReserves().values());
            Iterator normalReserves = cio.getNormalReserves().values().iterator();
            boolean todoOk = true;
            while ((normalReserves.hasNext()) && (todoOk)) {
                ClaimNormalReserve reserve = (ClaimNormalReserve) normalReserves.next();
                todoOk = deleteReserve(reserve);
                if (todoOk) {
                    log.debug("reserve.getPK() = " + reserve.getPK());
                    cio.getReservesByConcept().remove(reserve.getPK());
                }
            }
            if (todoOk) {
                Iterator reserves = cio.getReservesByConcept().values().iterator();
                while ((reserves.hasNext()) && (todoOk)) {
                    ClaimReserveByConcept reserve = (ClaimReserveByConcept) reserves.next();
                    todoOk = deleteReserve(reserve);
                    if (todoOk) {
                        log.debug("reserve.getPK() = " + reserve.getPK());
                        cio.getReservesByConcept().remove(reserve.getPK());
                    }
                }
                if (todoOk) {
                    processDeleteRequisite();
                    log.debug("Set Null Requisite Check List");
                    cio.delete();
                    cru.getClaimInsuranceObjects().remove(ioID);
                } else {
                    response.putAttribute("err", "claim.errorDessasociateIO");
                }
            } else {
                response.putAttribute("err", "Número de reclamo Inválido");
            }
        } else {
            response.putAttribute("err", "claim.errorInvalidaClaimNumber");
        }
    }

    /**
     * @param data
     * @return true if the reserve could be deleted
     */
    private boolean deleteReserve(Object data) throws ApplicationExceptionChecked {
        if (data instanceof ClaimNormalReserve) {
            ClaimNormalReserve cnr = (ClaimNormalReserve) data;
            if (isPaymentsOrderPendingInReserve(cnr)) {
                throw new ApplicationExceptionChecked("claim.errorDessasociateReserve", Severity.ERROR);
            }

            throw new ApplicationExceptionChecked("claim.errorDessasociateCoverage", Severity.ERROR);
        } else {
            if (data instanceof ClaimReserveByConcept) {
                ClaimReserveByConcept crc = (ClaimReserveByConcept) data;
                if (isPaymentsOrderPendingInReserve(crc)) {
                    throw new ApplicationExceptionChecked("claim.errorDessasociateReserve", Severity.ERROR);
                }

                throw new ApplicationExceptionChecked("claim.errorDessasociateConcept", Severity.ERROR);
            }
        }
        return false;
    }

    /**
     * @param reserve
     * @return boolean
     */
    public boolean isPaymentsOrderPendingInReserve(ClaimReserve reserve) {
        if ((reserve.getPaymentOrderList() != null) && (reserve.getPaymentOrderList().size() > 0)) {
            Iterator payments = reserve.getPaymentOrderList().values().iterator();
            while (payments.hasNext()) {
                PaymentOrder paymentOrder = (PaymentOrder) payments.next();
                paymentOrder = PaymentOrder.load(paymentOrder.getPk());
                if (paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.CANCELED_STATE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Allows to desassociar the requirements associated to the claim that is being processed.
     *
     * @return boolean
     */
    private boolean processDeleteRequisite() {
        log.debug("(Claim Entity EJB) Delete requisite");
        Collection<ClaimRequisite> list = claim.getRequisiteCheckList();
        boolean resp = false;
        if (list != null && !list.isEmpty()) {
            for (ClaimRequisite requisite : list) {
                log.debug("Delete Requisite: " + requisite.getDescription());
                requisite.delete();
            }
            resp = true;
        }
        return resp;
    }

    private boolean desassociateClaimNormalReserve(String aruId, String idSource, String ioID) throws ApplicationExceptionChecked {
        ClaimRiskUnit cru = null;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit claimRiskUnit : claimRiskUnits) {
            if (claimRiskUnit.getAgregatedRiskUnit().getPk().equals(aruId)) {
                cru = claimRiskUnit;
            }
        }

        if (cru != null) {
            log.debug("Desasociando Claim Normal Reserve: ioID = " + ioID);
            ClaimInsuranceObject cio = cru.getClaimInsuranceObjects().get(ioID);
            cio.load();
            ClaimNormalReserve reserve = (ClaimNormalReserve) cio.getNormalReserves().get(idSource);
            if (isPaymentsOrderPendingInReserve(reserve)) {
                throw new ApplicationExceptionChecked("claim.errorDessasociateReserve", Severity.ERROR);
            }

            throw new ApplicationExceptionChecked("claim.errorDessasociateCoverage", Severity.ERROR);
        } else {
            throw new ApplicationExceptionChecked("claim.errorDessasociateReserve", Severity.ERROR);
        }
    }

    /**
     * Create Entry Reserve
     *
     * @param amount
     * @param planId
     * @param configuratedCoverage
     * @param typeAdjust
     */
    public void entryReserve(double amount, String planId, long configuratedCoverage, int typeAdjust, EvaluatedCoverage ec, String login,
                             String currencyCode, ClaimReserve claimReserve) {
        SymbolsClaim entrys = new SymbolsClaim();
        TablaSimbolos table = new TablaSimbolos();

        publishSymbols(entrys, table, this.policy, this.claim, ec);
        entrys.addSymbol(table, EvaluationConstants.USER_LOGIN, login, 0.0);
        entrys.addSymbol(table, EvaluationConstants.CURRENCY_CODE, currencyCode, (double) claimReserve.getCurrency().getId());
        entrys.addSymbol(table, EvaluationConstants.CLAIM_AMOUNT, String.valueOf(amount), amount);
        String typeReserve = "INI";
        switch (typeAdjust) {
            case 1:
                typeReserve = "INC";
                break;
            case 2:
                typeReserve = "DEC";
                break;
            case 3:
                typeReserve = "ANU";
                break;
            case 4:
                typeReserve = "LIB";
                break;

        }
        entrys.addSymbol(table, EvaluationConstants.CLAIM_TYPE_RESERVE, typeReserve, (double) typeAdjust);
        if (ec != null) {
            String policyPlan = AcseleConf.getProperty("policy.plan");
            try {
                DefaultConfigurableObject ecDCO = ec.getPlan().getDCO();
                entrys.addSymbol(table, policyPlan, ecDCO.getCriterioInput(policyPlan), new Double(ecDCO.getCriterioValue(policyPlan)));
                log.debug(policyPlan + " =  " + ecDCO.getCriterioInput(policyPlan));
            } catch (NumberFormatException e) {
                log.warn(" Error publishing symbol '" + policyPlan + "'.  check your configuration (systemproperty 'policy.plan')", e);
            }

            entrys.addSymbols(ec.getDCO(), table);
        }
        entrys.addSymbols(policy.getProduct().getDCO(), table);
        String ocurrenceDateStr = DateUtil.getDateToShow(getOcurrenceDate());
        entrys.addSymbol(table, EvaluationConstants.OCURRENCE_DATE, ocurrenceDateStr,
                Funciones.dateTransformer.toNumber(ocurrenceDateStr));
        ClaimReserveAdjust lastReserveAdjust = claimReserve.getLastReserveAdjust();

        if (lastReserveAdjust != null) {
            entrys.setOpenitemID(Long.parseLong(lastReserveAdjust.getPK()));
        } else {
            entrys.setOpenitemID(Long.parseLong(claimReserve.getPK()));
        }

        String claimReserveDate = DateUtil.getDateToShow(claimReserve.getDate());
        entrys.addSymbol(table, EvaluationConstants.CLAIM_RESERVE_DATE, claimReserveDate, Funciones.dateTransformer.toNumber(claimReserveDate));

        if (claimReserve instanceof ClaimNormalReserve) {
            if (typeAdjust == 4) {
                entrys.createEntrys(OperationType.SINI_CLOSING, Long.valueOf(policy.getIDProduct()), Long.valueOf(planId), configuratedCoverage, table);
            } else {
                entrys.createEntrys(OperationType.SINI_OPENING, Long.valueOf(policy.getIDProduct()), Long.valueOf(planId), configuratedCoverage, table);
            }
        } else if (claimReserve instanceof ClaimReserveByConcept) {
            entrys.createEntrys(OperationType.SINI_RESERVE_EXPENSES, Long.valueOf(policy.getIDProduct()), Long.valueOf(planId), configuratedCoverage, table);
        }
        log.debug("End Code Entry Tool");
    }

    /**
     * @param entrys
     * @param table
     * @param ec
     */
    public void publishSymbols(SymbolsClaim entrys, TablaSimbolos table, AgregatedPolicy policy, Claim claim, EvaluatedCoverage ec) {

        log.debug("Publishing symbols for claim " + claim);
        String claimNumber;
        // Claim
        String dateSystem = DateUtil.getDateToShow(new java.util.Date());
        entrys.addSymbol(table, EvaluationConstants.SYSTEM_DATE, dateSystem, new Double(Funciones.dateTransformer.toNumber(dateSystem)));

        entrys.addSymbol(table, EvaluationConstants.CLAIM_PK, claim.getPK(), new Double(claim.getPK()));
        log.debug(EvaluationConstants.CLAIM_PK + " =  " + claim.getPK());

        claimNumber = claim.getClaimNumber();
        if (claimNumber == null) {
            claimNumber = "0";
        }

        //publicacion de fecha en la que se creo o se abrio el siniestro
        String claimOpenDate = DateUtil.getDateToShow(claim.getClaimDate());
        entrys.addSymbol(table, EvaluationConstants.CLAIM_OPEN_DATE, claimOpenDate,
                new Double(Funciones.dateTransformer.toNumber(claimOpenDate)));

        //publicacion de Causa del Siniestro
        entrys.addSymbol(table, EvaluationConstants.EVENTCLAIMDESC, claim.getClaimEvent().getDesc(), 0.0);

        entrys.addSymbol(table, EvaluationConstants.CLAIM_NUMBER, claimNumber, new Double(0));

        String policyNumberProperty = AcseleConf.getProperty("policyIdentification");
        String policyNumber = (String) policy.getInput().get(policyNumberProperty);
        entrys.addSymbol(table, policyNumberProperty, policyNumber, new Double(0));
        entrys.addSymbol(table, EvaluationConstants.POLICY_NUMBER, policyNumber, new Double(0));
        log.debug(policyNumberProperty + " =  " + policyNumber);

        entrys.addSymbol(table, EvaluationConstants.POLICY_PK, policy.getPk(), new Double(policy.getPk()));
        entrys.addSymbol(table, EvaluationConstants.PRODUCT, policy.getProduct().getDesc(), new Double(policy.getProduct().getPk()));
        log.debug(EvaluationConstants.PRODUCT + " =  " + policy.getProduct().getDesc());

        // Policy
        try {
            entrys.addSymbols(policy.getDCO(), table);
        } catch (Exception e) {
            log.error("Error publishing policy's symbols: ", e);
        }

        // Product
        entrys.addSymbol(table, EvaluationConstants.PRODUCT, policy.getProduct().getDesc(), new Double(policy.getProduct().getPk()));
        log.debug(EvaluationConstants.PRODUCT + " =  " + policy.getProduct().getDesc());
        entrys.addSymbol(table, AcseleConf.getProperty("policy.product"),
                policy.getProduct().getDCO().getCriterioInput(AcseleConf.getProperty("policy.product")),
                new Double(policy.getProduct().getDCO().getCriterioInput(AcseleConf.getProperty("policy.product"))));
        log.debug(AcseleConf.getProperty("policy.product") + " =  " + policy.getProduct().getDCO()
                .getCriterioInput(AcseleConf.getProperty("policy.product")));

        try {
            entrys.addSymbols(policy.getProduct().getDCO(), table);
        } catch (Exception e) {
            log.error("Error publishing product's symbols: ", e);
        }

        // PolicyMovements
        if (policy.movements == null || !asl) {
            policy.movements = new ArrayList();
            try {
                DBMovementsPolicyManager dbManager = (DBMovementsPolicyManager) BeanFactory.getBean(DBMovementsPolicyManager.class.getName());
                Collection movements = dbManager.getCollectionIDs(policy, policy.getOperationPK().getPK(), 0);
                Iterator iterator = movements.iterator();

                while (iterator.hasNext()) {

                    String idMovement = (String) iterator.next();
                    try {
                        ThirdPartyMovement movement = dbManager.getMovement(idMovement);
                        policy.movements.add(movement);
                        entrys.addSymbol(table, movement.getConcept(), movement.getDesc(), movement.getAmount());
                    } catch (Exception e) {
                        log.error("Error publishing movement " + idMovement + " symbol.");
                    }
                }
            } catch (Exception e) {
                log.error("Error publishing movement's symbols: ", e);
            }

        } else {
            for (Iterator iterator = policy.movements.iterator(); iterator.hasNext(); ) {
                ThirdPartyMovement thirdPartyMovement = (ThirdPartyMovement) iterator.next();
                entrys.addSymbol(table, thirdPartyMovement.getConcept(), thirdPartyMovement.getDesc(), thirdPartyMovement.getAmount());

            }

        }
        // Coverage
        try {
            if (ec != null && ec.getDCO() != null) {
                entrys.addSymbol(table, EvaluationConstants.CURRENT_ID_COVERAGE, ec.getPk(), new Double(0));
                entrys.addSymbols(ec.getDCO(), ec.getDesc(), table);
            }
        } catch (Exception e) {
            log.error("Error publishing coverage's symbols: ", e);
        }

        // Third Parties
        try {
            Enumeration en = policy.getParticipations(null);
            while (en.hasMoreElements()) {
                Participation p = (Participation) en.nextElement();
                String role = p.getRole().getDynamicTemplateName();
                DefaultConfigurableObject dco = p.getThirdParty().getDynamic().getDCO();
                entrys.addSymbol(table, EvaluationConstants.THIRDPARTY_NAME + role, p.getThirdParty().getName(), new Double(0));
                entrys.addSymbol(table, role + "_" + EvaluationConstants.THIRDPARTY_NAME, p.getThirdParty().getName(), new Double(0));
                entrys.addSymbols(dco.getInput(), dco.getValues(), role, table);

                AddressBook addressBook = AddressBook.getInstance(p.getAddressBookID());
                if (addressBook != null) {
                    AddressBookDynamic abd = addressBook.getDynamic();
                    if (abd != null) {
                        dco = abd.getDCO();
                        entrys.addSymbols(dco.getInput(), dco.getValues(), role + AddressBook.PREFIX, table);
                    }

                }
                // Aquí se usa rol_prop en vez de proprol, como en la 2.4.4.  Documentar.

                Iterator claimRUList = claim.getClaimRiskUnitsList().iterator();
                ClaimRiskUnit cru = null;
                if (claimRUList.hasNext()) {
                    cru = (ClaimRiskUnit) claimRUList.next();

                    ClaimInsuranceObject cio = null;
                    Collection<ClaimInsuranceObject> cioList = cru.getClaimInsuranceObjects().values();
                    Iterator<ClaimInsuranceObject> itCio = cioList.iterator();
                    if (itCio.hasNext()) {
                        cio = itCio.next();
                        if (!asl) {
                            cio.load();
                        }
                        DefaultConfigurableObject dco2 = cio.getDamage();

                        entrys.addSymbols(dco2.getInput(), dco2.getValues(),
                                dco2.getCOT().getDesc(), table);

                        DefaultConfigurableObject dcoAIO = cio.getAgregatedInsuranceObject().getDCO();

                        entrys.addSymbols(dcoAIO.getInput(), dcoAIO.getValues(),
                                dcoAIO.getCOT().getDesc(), table);

                    }
                }

            }
        } catch (Exception e) {
            log.error("Error publishing third parties' symbols: ", e);
        }

        // Varios
        entrys.addSymbol(table, "TaxThirdPartyName", AcseleConf.getProperty("retention.Agent"), new Double(0));
    }

    public void entryReserve(double amount, int typeAdjust, String login, String currencyCode, ClaimReserve claimReserve) {
        entryReserve(amount, typeAdjust, login, currencyCode, null, claimReserve);
    }

    /**
     * Create Entry Reserve
     *
     * @param amount
     * @param typeAdjust
     */
    public void entryReserve(double amount, int typeAdjust, String login, String currencyCode, EvaluatedCoverage ec, ClaimReserve claimReserve) {
        SymbolsClaim entrys = new SymbolsClaim();
        TablaSimbolos table = new TablaSimbolos();

        publishSymbols(entrys, table, this.policy, this.claim, ec);
        entrys.addSymbol(table, EvaluationConstants.USER_LOGIN, login, 0.0);
        entrys.addSymbol(table, EvaluationConstants.CURRENCY_CODE, currencyCode, (double) claimReserve.getCurrencyId());
        entrys.addSymbol(table, EvaluationConstants.CLAIM_AMOUNT, String.valueOf(amount), amount);
        String typeReserve = "INI";
        switch (typeAdjust) {
            case 1:
                typeReserve = "INC";
                break;
            case 2:
                typeReserve = "DEC";
                break;
            case 3:
                typeReserve = "ANU";
                break;
            case 4:
                typeReserve = "LIB";
                break;
        }
        log.debug("entryReserve --- typeReserve: " + typeReserve);
        entrys.addSymbol(table, EvaluationConstants.CLAIM_TYPE_RESERVE, typeReserve, (double) typeAdjust);
        Long planId = 0L;
        if (ec != null) {
            entrys.addSymbol(table, AcseleConf.getProperty("policy.plan"),
                    ec.getPlan().getDCO().getCriterioInput(AcseleConf.getProperty("policy.plan")),
                    new Double(StringUtil.defaultIfEmptyOrNull(ec.getPlan().getDCO().getCriterioInput(AcseleConf.getProperty("policy.plan")), "0")));
            planId = Long.valueOf(ec.getPlan().getPk());
            log.debug(AcseleConf.getProperty("policy.plan") + " =  " + ec.getPlan().getDCO().getCriterioInput(AcseleConf.getProperty("policy.plan")));

            entrys.addSymbols(ec.getDCO(), table);

        }
        entrys.addSymbols(policy.getProduct().getDCO(), table);
        String ocurrenceDateStr = DateUtil.getDateToShow(getOcurrenceDate());
        entrys.addSymbol(table, EvaluationConstants.OCURRENCE_DATE, ocurrenceDateStr,
                Funciones.dateTransformer.toNumber(ocurrenceDateStr));
        log.debug(EvaluationConstants.OCURRENCE_DATE + " =  " + ocurrenceDateStr);
        log.debug("entrys.createEntrys --- claimReserve: " + claimReserve);
        Long ccId = ec != null ? Long.valueOf(ec.getConfiguratedCoverage().getPk()) : 0;

        String claimReserveDate = DateUtil.getDateToShow(claimReserve.getDate());
        entrys.addSymbol(table, EvaluationConstants.CLAIM_RESERVE_DATE, claimReserveDate, Funciones.dateTransformer.toNumber(claimReserveDate));

        if (claimReserve instanceof ClaimNormalReserve) {
            entrys.createEntrys(OperationType.SINI_OPENING, Long.valueOf(policy.getIDProduct()), planId, ccId, table);
        } else if (claimReserve instanceof ClaimReserveByConcept) {
            log.debug("/////// Entrada a entrys.createEntrys --- SINI: 6");

            ClaimReserveAdjust lastReserveAdjust = claimReserve.getLastReserveAdjust();

            if (lastReserveAdjust != null){
                entrys.setOpenitemID(Long.parseLong(lastReserveAdjust.getPK()));
            }else {
                entrys.setOpenitemID(Long.parseLong(claimReserve.getPK()));
            }

            entrys.createEntrys(6, Long.valueOf(policy.getIDProduct()), planId, ccId, table, false);
            log.debug("/////// Salida de entrys.createEntrys --- SINI: 6");
        }
        log.debug("End Code Entry Tool");
    }

    /**
     * @param agregatedRiskUnitid
     * @param insuranceObjectId
     * @param evaluatedCoverageId
     * @return EvaluatedCoverage
     */
    public EvaluatedCoverage searchEvaluatedCoverage(String agregatedRiskUnitid, String insuranceObjectId, String evaluatedCoverageId) throws Exception {
        return searchEvaluatedCoverage(agregatedRiskUnitid, insuranceObjectId, evaluatedCoverageId, policy, claim);
    }

    public EvaluatedCoverage searchEvaluatedCoverage(String agregatedRiskUnitid, String insuranceObjectId, String evaluatedCoverageId, AgregatedPolicy policy, Claim claim) throws Exception {
        EvaluatedCoverage ec;
        if (isRetroactive(claim)) {
            ec = policy.getAgregatedRiskUnit(agregatedRiskUnitid).getInsuranceObject(insuranceObjectId)
                    .getEvaluatedCoverage(evaluatedCoverageId);
            if (ec == null) {
                ec = searchEvaluatedCoverageByDesc(agregatedRiskUnitid, insuranceObjectId, evaluatedCoverageId.contains("(R) ") ? evaluatedCoverageId.replaceFirst("\\(R\\) ", "") : evaluatedCoverageId);
            }
        } else {
            AgregatedInsuranceObject aio = claim.getClaimInsuranceObject(insuranceObjectId).getAgregatedInsuranceObject();
            ec = getEvaluatedCoverage(aio, evaluatedCoverageId);
            if (ec == null) {
                ec = searchEvaluatedCoverageWithDesc(aio, evaluatedCoverageId.contains("(R) ") ? evaluatedCoverageId.replaceFirst("\\(R\\) ", "") : evaluatedCoverageId);
            }
            if (ec == null) {
                if (!StringUtil.isEmptyOrNullValue(claim.getDoneBy())) {
                    if (claim.getDoneBy().equals("migrationlbc")) {
                        ec = policy.getAgregatedRiskUnit(agregatedRiskUnitid).getInsuranceObject(insuranceObjectId).getEvaluatedCoverage(evaluatedCoverageId);
                    }
                }
            }
            if (ec == null) {
                if (!StringUtil.isEmptyOrNullValue(claim.getDoneBy())) {
                    if (claim.getDoneBy().equals("migrationlbc")) {
                        ec = searchEvaluatedCoverageByDesc(agregatedRiskUnitid, insuranceObjectId, evaluatedCoverageId.contains("(R) ") ? evaluatedCoverageId.replaceFirst("\\(R\\) ", "") : evaluatedCoverageId);

                    }
                }
            }
        }
        return ec;
    }

    /**
     * @param agregatedRiskUnitid
     * @param insuranceObjectId
     * @param evaluatedCoverageId
     * @return EvaluatedCoverage
     */
    private EvaluatedCoverage searchEvaluatedCoverageByDesc(String agregatedRiskUnitid, String insuranceObjectId, String evaluatedCoverageId) throws Exception {
        if (isRetroactive()) {
            return policy.getAgregatedRiskUnit(agregatedRiskUnitid).getInsuranceObject(insuranceObjectId).getEvaluatedCoverageByDesc(evaluatedCoverageId);
        } else {
            AgregatedInsuranceObject aio = claim.getClaimInsuranceObject(insuranceObjectId).getAgregatedInsuranceObject();
            EvaluatedCoverage ec = getEvaluatedCoverage(aio, evaluatedCoverageId);
            if (ec == null) {
                ec = searchEvaluatedCoverageWithDesc(aio, evaluatedCoverageId);
            }
            if (ec == null) {
                if (!StringUtil.isEmptyOrNullValue(claim.getDoneBy())) {
                    if (claim.getDoneBy().equals("migrationlbc")) {
                        return policy.getAgregatedRiskUnit(agregatedRiskUnitid).getInsuranceObject(insuranceObjectId).getEvaluatedCoverageByDesc(evaluatedCoverageId);
                    }
                }
            }

            return ec;
        }

    }

    /**
     * @param aruId
     * @param idSource
     * @param ioID
     * @param response
     * @return true if the reserve could be desassociated
     */
    private boolean desassociateClaimReservedByConcept(String aruId, String idSource, String ioID,
                                                       ClientResponse response) throws ApplicationExceptionChecked {

        ClaimRiskUnit cru = null;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit claimRiskUnit : claimRiskUnits) {
            if (claimRiskUnit.getAgregatedRiskUnit().getPk().equals(aruId)) {
                cru = claimRiskUnit;
            }
        }
        if (cru != null) {
            ClaimInsuranceObject cio = cru.getClaimInsuranceObjects().get(ioID);
            ClaimReserveByConcept crbc = (ClaimReserveByConcept) cio.getReservesByConcept().get(idSource);
            if (crbc != null) {
                log.debug("Elimiando Reserva");
                if (isPaymentsOrderPendingInReserve(crbc)) {
                    throw new ApplicationExceptionChecked("claim.errorDessasociateReserve", Severity.ERROR);
                }

                throw new ApplicationExceptionChecked("claim.errorDessasociateConcept", Severity.ERROR);
            } else {
                response.putAttribute("err", "Reserva por concepto Inválida");
            }
        } else {
            response.putAttribute("err", "Número de reclamo Inválido");
        }
        return false;
    }

    /**
     * Edit the claim
     *
     * @param action
     * @return the response with the editable data
     */
    private ClientResponse editClaim(String action) {
        Object data = null;
        if (action.equals("editStatisticTitles")) {
            data = claim.editStatisticsTitles();
        } else {
            if (claim.getNotification() != null) {
                String ocurrenceDateStr = DateUtil.getDateToShow(getOcurrenceDate());
                claim.getNotification().getDCO().setCriterioInput(AcseleConf.getProperty(OCURRENCE_DATE), ocurrenceDateStr);

                data = claim.getNotification().getDCO().toHashtable();
            }
        }
        ClientResponse response = new ClientResponse();
        response.setCommand(action);
        log.debug("data " + data);
        if (data != null) {
            response.putAttribute("responsedata", data);
            response.putAttribute("multidcoid", claim.getNotification().getDCO().getPk()); //multiregister
            response.setResult(true);
        } else {
            response.setResult(false);
        }
        return response;
    }

    public String evaluatorPublishPolicyCurrency(ExpresionEvaluator evaluator, EvaluatedCoverage ec) {
        Currency polCurrency = policy.getCurrency();
        String currencyName = polCurrency.getDescription();
        String currencyValue = String.valueOf(polCurrency.getId());

        String currencyKey = AcseleConf.getProperty("currency");
        // Coverage's DCO's Currency
        if (StringUtil.isEmptyOrNullValue(currencyName)) {
            DefaultConfigurableObject covDCO = ec.getDCO();
            currencyName = covDCO.getCriterioInput(currencyKey);
            currencyValue = covDCO.getCriterioValue(currencyKey);
            log.debug("Coverage's DCO's Currency: '" + currencyName + "' (" + currencyValue + ")");
        }

        // Policy's DCO's Currency
        if (StringUtil.isEmptyOrNullValue(currencyName)) {
            DefaultConfigurableObject polDCO = policy.getDCO();
            currencyName = polDCO.getCriterioInput(currencyKey);
            currencyValue = polDCO.getCriterioValue(currencyKey);
        }

        try {
            log.debug("Publishing Policy's Currency '" + currencyName + "' (" + currencyValue + ") with symbol '" + currencyKey + "'.");
            evaluator.addSymbol(currencyKey, currencyName, new Double(currencyValue), true);
        } catch (NumberFormatException e) {
            String oiCurrencyKey = AcseleConf.getProperty("OpenItemCurrency");
            log.warn("Error in Policy's Currency.  Publishing Open Item's Currency (" + oiCurrencyKey + ") '" + currencyName + "' ("
                    + currencyValue + ") with symbol '" + currencyKey + "'.");
            Currency currency = Currency.Impl.load(oiCurrencyKey);
            evaluator.addSymbol(currencyKey, currency.getDescription(), Double.valueOf(currency.getId()), true);
        }
        return currencyName;
    }

    /**
     * @param claimRiskUnitid
     * @param insuranceObjectId
     * @return ClaimInsuranceObject
     */
    private ClaimInsuranceObject searchClaimInsuranceObject(String claimRiskUnitid, String insuranceObjectId) {
        return searchClaimInsuranceObject(claimRiskUnitid, insuranceObjectId, claim);
    }

    public ClaimInsuranceObject searchClaimInsuranceObject(String claimRiskUnitid, String insuranceObjectId, Claim claim) {
        log.debug("[LAD] claimRiskUnitid: " + claimRiskUnitid);
        ClaimInsuranceObject claimInsuranceObjectResult = null;
        ClaimInsuranceObject claimInsuranceObject = null;

        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        ClaimRiskUnit claimRiskUnit = null;
        for (ClaimRiskUnit cru : claimRiskUnits) {
            if (cru.getAgregatedRiskUnit().getPk().equals(claimRiskUnitid)) {
                claimRiskUnit = cru;
                break;
            }
        }
        if (claimRiskUnit == null) {
            return null;
        }
        log.debug("[LAD] insuranceObjectId: " + insuranceObjectId);

        claimInsuranceObjectResult = claimRiskUnit.getClaimInsuranceObjects().get(insuranceObjectId);

        if (claimInsuranceObjectResult == null) {
            Iterator<ClaimInsuranceObject> claimInsuranceObjectIterator = claimRiskUnit.getClaimInsuranceObjects().values().iterator();
            while (claimInsuranceObjectIterator.hasNext()) {
                claimInsuranceObject = claimInsuranceObjectIterator.next();
                if (claimInsuranceObject.getAioId().equals(insuranceObjectId)) {
                    return claimInsuranceObject;
                }
            }
        } else {
            return claimInsuranceObject = claimInsuranceObjectResult;
        }

        return null;
    }

    /**
     * @param affected
     * @return ClientResponse
     */
    private ClientResponse validateRequired(ClaimInsuranceObject affected) {
        ClientResponse response = new ClientResponse();
        try {
            ValidationHandler.validateRequired(affected.getDamage(), affected.getDesc());
            response.setResult(false);
        } catch (Exception e) {
            response.putAttribute("error", e.getMessage());
            response.setResult(true);
            log.error("Error", e);
        }
        return response;
    }

    /**
     * Edit an affected object
     *
     * @param affected thaat contains the object identifier
     * @return the response with the editable data
     */
    private ClientResponse editAffectedObject(ClaimInsuranceObject affected, String damageDcoPk, String damageDcoName) {
        DefaultConfigurableObject dco;
        if (affected == null) {
            Categorias categorias = Categorias.getBean(Categorias.EXTENDED_TEMPLATES_STATE);
            ConfigurableObjectType cot =
                    (ConfigurableObjectType) categorias.get(CotType.CLAIM, damageDcoName);
            dco = DefaultConfigurableObject.load(cot, Long.valueOf(damageDcoPk));
        } else {
            affected.load();
            dco = affected.getDamage();
        }

        Object data = null;

        if (dco != null) {
            String ocurrenceDateStr = DateUtil.getDateToShow(getOcurrenceDate());
            dco.setCriterioInput(AcseleConf.getProperty(OCURRENCE_DATE), ocurrenceDateStr);
            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            String dateSystem = DateUtil.getDateToShow(new java.util.Date());
            evaluator.addSymbol("claimNumber", claim.getClaimNumber(), new Double(0), true);
            try {
                Collection<Participation> it = affected.getAgregatedInsuranceObject().getParticipationCollection();
                for (Participation participation : it) {
                    evaluator = participation.publishAllSymbols(evaluator);
                }
                AgregatedPolicy ap = (AgregatedPolicy) affected.getContainer().getAgregatedRiskUnit().getAggregateParent();
                ap.publishSymbols(evaluator);
            } catch (Exception e) {
                log.debug("Exception ClaimComposerWrapper.editAffectedObject = " + e);
            }
            evaluator.addSymbol("sysDate", dateSystem, new Double(Funciones.dateTransformer.toNumber(dateSystem)), true);

            evaluator.evaluateConfigurableObject(dco);
            data = dco.toHashtable();
            log.debug("data = " + data);
        }

        ClientResponse response = new ClientResponse();

        if (data == null) {
            response.setResult(false);
        } else {
            response.putAttribute("responsedata", data);
            response.putAttribute("multidcoid", dco.getPk()); //multiregister
            response.setResult(true);
        }
        return response;
    }

    /**
     * Edit a normal reserve
     *
     * @param reserve a reseve
     * @return the response with the editable data
     */
    private ClientResponse editNormalReserve(ClaimNormalReserve reserve) {
        reserve.load();
        AdjustCollection adjustCollection;
        ClientResponse response = new ClientResponse();

        adjustCollection = new AdjustCollection(reserve.getAdjustList().values());
        double paidAmount = 0.0f;
        Iterator payments = reserve.getPaymentOrderList().values().iterator();
        while (payments.hasNext()) {
            PaymentOrder paymentOrder = (PaymentOrder) payments.next();
            if (paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.CANCELED_STATE &&
                    paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.REJECTED_STATE) {
                paidAmount += paymentOrder.getAmountWithDeductible();
            }
        }

        AgregatedInsuranceObject aio = reserve.getEvaluatedCoverage().getAgregatedInsuranceObject();
        EvaluatedCoverage ec = reserve.getEvaluatedCoverage();
        AgregatedRiskUnit ru = reserve.getEvaluatedCoverage().getAgregatedInsuranceObject().getAgregatedRiskUnit();
        ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
        ClaimUtil.fillEvaluator(policy.getProduct().getDCO(), evaluator);
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit claimRiskUnit : claimRiskUnits) {
            ClaimInsuranceObject claimInsuranceObject = claimRiskUnit.getClaimInsuranceObjects().get(aio.getDesc());
            if (claimInsuranceObject != null) {
                ClaimUtil.fillEvaluator(claimInsuranceObject.getDamage(), evaluator);
            }
        }
        ec.publishAllSymbolsToUp(evaluator);
        evaluator.addSymbol(EvaluationConstants.CURRENT_ID_COVERAGE, ec.getPk(), new Double(0), true);
        String ocurrenceDateStr = DateUtil.getDateToShow(claim.getOcurrenceDate());
        evaluator.addSymbol(AcseleConf.getProperty(OCURRENCE_DATE), ocurrenceDateStr,
                new Double(Funciones.dateTransformer.toNumber(ocurrenceDateStr)), true);
        evaluator.addSymbol(EvaluationConstants.CLAIM_NUMBER, claim.getClaimNumber(), new Double(0), true);
        ClaimUtil.fillEvaluator(ec.getDCO(), evaluator);

        evaluator.addSymbol(EvaluationConstants.RISK_UNIT_PK, String.valueOf(ru.getId()), new Double(ru.getId()), true);

        if (aio != null) {
            evaluator.getTablaSimbolos().put("INSURANCEOBJECTPK", aio.getPk(), Double.valueOf(aio.getPk()));
        }

        evaluatorPublishPolicyCurrency(evaluator, ec);

        ClaimsCoverageConfiguration ccc = this.getClaimCoverageConfiguration(ru.getPk(), aio.getPrimaryKey(), ec);

        String initialResExp =  ccc.getInitialReserveAmount();
        double initialReserve = evaluator.evaluate(initialResExp);

        evaluator.getTablaSimbolos().put("InitialReserve", String.valueOf(initialReserve), initialReserve);

        String resLimitExp = ccc.getMaxReserveAmount();
        double reservLimit = evaluator.evaluate(resLimitExp);

        adjustCollection.setCurrency(reserve.getCurrency().getDescription());
        adjustCollection.setPaidAmount(paidAmount);
        adjustCollection.setReserveDesc(reserve.getDesc());
        adjustCollection.setReserveLimitAmount(reservLimit);
        adjustCollection.setReserveType(ReserveType.NORMAL_RESERVE.getValue());
        adjustCollection.setLegacyType(reserve.getLegacyType());

        response.putAttribute("responsedata", adjustCollection);
        response.setResult(true);

        return response;
    }

    /**
     * @param claimRiskUnitid
     * @param insuranceObjectId
     * @param claimReserveId
     * @param reserveType
     * @return ClaimReserve
     */

    public ClaimReserve searchClaimReserve(String claimRiskUnitid, String insuranceObjectId, String claimReserveId, int reserveType) {
        log.debug("---------------------- searchClaimReserve ----------------------");
        log.debug("**claimRiskUnitid = " + claimRiskUnitid);
        log.debug("**insuranceObjectId = " + insuranceObjectId);
        log.debug("**claimReserveId = " + claimReserveId);
        log.debug("**reserveType = " + reserveType);

        ClaimRiskUnit cru = null;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        log.debug("**claimRiskUnits=" + claimRiskUnits);
        for (ClaimRiskUnit claimRiskUnit : claimRiskUnits) {
            String pk = claimRiskUnit.getAgregatedRiskUnit().getPk();
            log.debug("**pk=" + pk);
            if (pk.equals(claimRiskUnitid)) {
                cru = claimRiskUnit;
            }
        }
        log.debug("**cru=" + cru);
        if (cru == null) {
            return null;
        }
        ClaimInsuranceObject io = cru.getClaimInsuranceObjects().get(insuranceObjectId);
        if (!asl) {
            io.load();
        }
        log.debug("**io=" + io);
        ClaimReserve claimReserve = new ClaimReserve();
        log.debug("**claimReserveId = " + claimReserveId);
        log.debug("**reserveType = " + reserveType);
        log.debug("**[io.getNormalReserves()] " + io.getNormalReserves());
        log.debug("**io.getReservesByConcept() " + io.getReservesByConcept());
        if (reserveType == ReserveType.NORMAL_RESERVE.getValue()) {
            log.debug("**INTO io.getNormalReserves() ");
            //ClaimNormalReserve claimReserveAux = ClaimNormalReserve.load(claimReserveId);
            claimReserve = (ClaimReserve) io.getNormalReservesById().get(Long.valueOf(claimReserveId));
            if (claimReserve == null) {
                claimReserve = ClaimNormalReserve.load(claimReserveId);
            }
        } else if (reserveType == ReserveType.CONCEPT_RESERVE.getValue()) {
            log.debug("**INTO io.getReservesByConcept() ");
            Iterator itClaimReservesByConcept = io.getReservesByConcept().values().iterator();
            while (itClaimReservesByConcept.hasNext()) {
                claimReserve = (ClaimReserve) itClaimReservesByConcept.next();
                if (claimReserve.getPk().equals(claimReserveId)) {
                    break;
                }
            }
            ((ClaimReserveByConcept) claimReserve).setContainer(io);
        }
        return claimReserve;
    }

    /**
     * Edit a reseve by concept
     *
     * @param reserve teh reserve by concept
     * @return the response with the editable data
     */
    private ClientResponse editReserveByConcept(ClaimReserve reserve) {
        reserve.load();
        AdjustCollection adjustCollection = null;
        ClientResponse response = new ClientResponse();
        boolean isValid = true;
        ClaimUtil claimUtil = new ClaimUtil();
        if (reserve.getAdjustList() != null && !reserve.getAdjustList().isEmpty()) {
            String productPk = policy.getIDProduct();
            ClaimReserveByConcept reserveByConcept = (ClaimReserveByConcept) reserve;
            response.putAttribute("reserveByConcept", reserveByConcept);
            ClaimNormalReserve normalReserve = ClaimNormalReserve.load(reserveByConcept.getNormalReserveId().toString());
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();
            TablaSimbolos symbolsTable = new TablaSimbolos();
            ClaimsCoverageConfiguration claimConf = ClaimsCoverageConfiguration.load(Long.parseLong(productPk), ec.getConfiguratedCoverageOA().getId());   //Configuracion del Reclamo
            double maxAmount = 0;
            long currencyPol = policy.getCurrency().getId();
            String normalReserveId = reserveByConcept.getNormalReserveId().toString();
            Long normalReserveIdbyConcept = Long.valueOf(reserveByConcept.getPK());
            maxAmount = evaluate(claimConf.getExpenseLimit(), symbolsTable, ec);
            double amountAcum = claimUtil.getClaimReserveAdjustment(normalReserveId, currencyPol, normalReserveIdbyConcept);
            maxAmount = maxAmount - amountAcum;
            adjustCollection = new AdjustCollection(reserve.getAdjustList().values());
            adjustCollection.setReserveLimitAmount(maxAmount);
            double paidAmount = 0.0f;
            Iterator payments = reserve.getPaymentOrderList().values().iterator();
            while (payments.hasNext()) {
                PaymentOrder paymentOrder = (PaymentOrder) payments.next();
                paymentOrder = PaymentOrder.load(paymentOrder.getPk());
                if (paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.CANCELED_STATE &&
                        paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.REJECTED_STATE) {
                    paidAmount += paymentOrder.getAmountWithDeductible();
                }
            }
            adjustCollection.setCurrency(reserve.getCurrency().toString());
            adjustCollection.setPaidAmount(paidAmount);
            adjustCollection.setReserveDesc(reserve.getDesc());
            adjustCollection.setReserveType(ReserveType.CONCEPT_RESERVE.getValue());
            adjustCollection.setLegacyType(((ClaimReserveByConcept) reserve).getLegacyType());
        }
        if (adjustCollection != null) {
            response.putAttribute("responsedata", adjustCollection);
            response.setResult(true);
        } else {
            response.setResult(false);
        }

        return response;
    }

    /**
     * Updates the affected object
     *
     * @param receivedData
     */
    private DefaultConfigurableObject updateAffectedObject(ClaimInsuranceObject cio, Object receivedData) {
        DefaultConfigurableObject dco = null;
        if (cio != null) {
            cio.load();
            dco = cio.getDamage();
            updateDCO(dco, receivedData);
        }
        return dco;
    }

    /**
     * Edit a normal reserve's benefit
     *
     * @param benefit a reseve
     * @return the response with the editable data
     */
    private ClientResponse editBenefit(ClaimBenefit benefit) {

        ClientResponse response = new ClientResponse();

        AdjustCollection adjustCollection = new AdjustCollection(benefit.getAdjustList().values());

        adjustCollection.setCurrency(benefit.getCurrency().toString());
        adjustCollection.setReserveDesc(benefit.getDesc());
        adjustCollection.setReserveLimitAmount(benefit.getReserveLimit());
        adjustCollection.setReserveType(ReserveType.BENEFIT_RESERVE.getValue());


        response.putAttribute("responsedata", adjustCollection);
        response.setResult(true);


        return response;
    }

    public ClientResponse aslAssociateInsuranceObject(String aruId, String idSource, DefaultConfigurableObject damage, String damageTemlateName,
                                                      String login) {
        ClientResponse response = new ClientResponse();
        AgregatedInsuranceObject aio = searchAgregatedInsuranceObject(aruId, idSource);
        try {
            ClaimInsuranceObject cio = claim.add(aio);
            cio.setDamage(damage);
            cio.setDamageDcoId(damage.getId());
            cio.setDamageDcotemplate(damage.getCOT().getId());
            cio.update();
            associateTemplateIO(damage, cio);
            response.putAttribute("DescObjectIO", cio.getDesc());

            response.setResult(true);
            response.putAttribute("claimstate", this.claim.getClaimStatus().getValue());
        } catch (Exception e) {
            log.error("Error", e);
            response.putAttribute("msg", e.getMessage() == null ? "" : e.getMessage());
            response.setResult(false);
        }
        return response;

    }

    /**
     * @param aruId
     * @param idSource
     * @param damage
     * @param damageTemlateName
     * @param login
     * @return ClientResponse
     */
    public ClientResponse summitAssociateInsuranceObject(String aruId, String idSource,
                                                         DefaultConfigurableObject damage,
                                                         String damageTemplateName,
                                                         String login,
                                                         SearchBean searchBean) {

        ClientResponse response = new ClientResponse();
        log.debug("searchBean=" + searchBean);
//        log.debug("searchBean.getCruObj()=" + searchBean.getCruObj());
        try {
            AgregatedRiskUnit aggregatedRiskUnit = searchBean.getCruObj().getAgregatedRiskUnit();
            AgregatedInsuranceObject aio = DBAgregatedInsuranceObject.loadIOByIdAndOperation(Long.parseLong(searchBean.getPk()), searchBean.getOperationPk(), aggregatedRiskUnit);
            Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
            ClaimRiskUnit claimRiskUnit = null;
            for (ClaimRiskUnit cru : claimRiskUnits) {
                if (cru.equals(searchBean.getCruObj())) {
                    claimRiskUnit = cru;
                    break;
                }
            }
            if (claimRiskUnit == null) {
                claimRiskUnit = new ClaimRiskUnit(aggregatedRiskUnit);
                claimRiskUnit.setContainer(claim);
                claimRiskUnit.save();
            }
            ClaimInsuranceObject cio = new ClaimInsuranceObject(aio);
            cio.setContainer(claimRiskUnit);
            cio.setDamage(damage);
            cio.setOperationPK(aio.getOperationPK().getId());
            cio.save();

            List<ValidationDamageClaim> validationDamageClaims = ValidationDamageClaimPersister.Impl.getInstance()
                    .listValidationsByTypes(claim.getClaimEvent().getId(), Arrays.asList(EnumValidationCovClaim.ERROR.getValue()));
            this.evaluateDamageValidations(claim, validationDamageClaims, Long.parseLong(cio.getPk()));
            for (ValidationDamageClaim validation : validationDamageClaims) {
                if (!validation.isValidFormula() && (validation.getType() == 0 || validation.getType() == 2)) {
                    response.putAttribute("ClaimDamageValidations", validationDamageClaims);
                    response.putAttribute("PropertyValidationToClean", validation.getProperty());
                    response.setResult(false);
                    response.putAttribute("DamageErrorValidations", new Boolean(true));
                    return response;
                }
            }

            claimRiskUnit.getClaimInsuranceObjects().put(cio.getDesc(), cio);
            ExpresionEvaluator evaluator1 = ExpresionEvaluator.createEvaluator();
            evaluator1.addSymbol("claimNumber", claim.getClaimNumber(), new Double(0), true);
            evaluator1.evaluateConfigurableObject(damage);
            damage.updateEditable();
            EventClaim eventClaim = claim.getEventClaim();
            if (eventClaim != null) {
                ClaimType claimType = eventClaim.getClaimTypeByDesc(damageTemplateName);
                if (claimType != null) {
                    log.debug(claimType.getPK());
                    log.debug(claimType.getPk());
                }

                Collection<EventClaimCoverage> ct = claimType.getAllEventClaimCoverages().values();
                for (EventClaimCoverage eventClaimCoverage : ct) {
                    try {
                        EvaluatedCoverage evaluatedCoverage = searchEvaluatedCoverageWithDesc(aio, eventClaimCoverage.getDesc());
                        if (evaluatedCoverage != null) {
                            ExpresionEvaluator evaluator = updateCoverage(evaluatedCoverage, claimRiskUnit, cio);
                            String operation = eventClaimCoverage.getOperation();
                            evaluator.addSymbol("amountReserve", operation, new Double(0), true);
                            double amountReserve = (operation == null) ? 0 : evaluator.evaluate(operation);
                            int currency = (int) evaluator.get(AcseleConf.getProperty("monedaProperty"));
                            double reserveLimit = evaluator.get("reserveLimit");

                            Vector datos = new Vector();
                            datos.add(String.valueOf(amountReserve));
                            datos.add(String.valueOf(currency));
                            datos.add(String.valueOf(reserveLimit));

                            if (amountReserve > 0) {

                                response = associateToEvaluatedCoverage(cio, claimRiskUnit, login, evaluatedCoverage, idSource, datos,
                                        ClaimNormalReserve.PAYMENT_TYPE_UNIQUE, ClaimNormalReserve.PERIODICITY_NONE);


                            } else {
                                if (reserveLimit <= 0) {
                                    response.putAttribute("error", "appletClaimTool.notRestReserve");
                                } else {
                                    response.putAttribute("error", "claim.batConfigurationForCoverage");
                                }
                            }
                        } else {
                            log.debug("Cobertura No Encontrada... " + eventClaimCoverage.getDesc());
                        }
                    } catch (ApplicationExceptionChecked ae) {
                        log.error("Error", ae);
                        response.putAttribute("error", ae.getKeyCode());
                    }
                }
            }
            response.putAttribute("DescObjectIO", cio.getDesc());

            response.setResult(true);
            response.putAttribute("claimstate", this.claim.getClaimStatus().getValue());
        } catch (Exception e) {
            log.error("Error", e);
            response.putAttribute("msg", e.getMessage() == null ? "" : e.getMessage());
            response.setResult(false);
        }
        return response;
    }

    /**
     * @param reserve
     * @param receivedData
     */
    private void updateReserve(ClaimReserve reserve, Object receivedData, String planId, long configuratedCoverage, EvaluatedCoverage ec, String validation) {

        if (validation != null && validation.equals(ProductLanguageHandler.getValidationTypeClaimString(ProductLanguageHandler.EVENT_ERROR_VALIDATION_TYPE))) {
            throw new TechnicalException(ProductLanguageHandler.getValidationTypeClaimString(ProductLanguageHandler.EVENT_ERROR_VALIDATION_TYPE));
        }

        AdjustCollection adjustCollection = (AdjustCollection) receivedData;

        int oldAdjustListSize = reserve.getAdjustList().size();
        double diffAmount = 0;

        if (adjustCollection.getAdjustList().size() > oldAdjustListSize) {
            //nuevo ajuste , calculo el deducible

            String legacyType = null;
            Vector<ClaimReserveAdjust> adjustList = adjustCollection.getAdjustList();
            for (int i = adjustList.size() - 1; i >= oldAdjustListSize - 1; i--) {

                ClaimReserveAdjust adjust = adjustList.elementAt(i);
                if (adjust.getPK() != null) {
                    continue;
                }
                adjust.setReserveDesc(adjustCollection.getReserveDesc());
                adjust.save();
                if (reserve instanceof ClaimNormalReserve) {
                    ClaimValidationService claimValidationService = ((ClaimValidationService) BeanFactory.getBean(ClaimValidationService.class));
                    claimValidationService.claimreOpening(claim.getId(), ec);
                }
                try {
                    ClaimHistorical claimHistorical = new ClaimHistorical();
                    //String legacyType;
                    legacyType = adjustCollection.getLegacyType();
                    if (reserve instanceof ClaimReserveByConcept) {
                        legacyType = legacyType.equalsIgnoreCase(OIMTPClaimEnum.ADMINISTRATIVA.getLabel()) ?
                                OIMTPClaimEnum.ADMINISTRATIVA_BY_CONCEPT.getLabel() : OIMTPClaimEnum.JUDICIAL_BY_CONCEPT.getLabel();
                    }
                    claimHistorical.generateHistoricalWithMovement(this.claim, ClaimHistoricalOperationType.ADD_RESERVE, ClaimHistoricalMovementType.CHANGE_RESERVE, Long.parseLong(adjust.getPK()), legacyType);
                } catch (Exception e) {

                }
                reserve.getAdjustList().put(adjust.getPk(), adjust);

                if (adjust.getType() == ReserveAdjustType.INCREASE.getValue()) {
                    diffAmount += adjust.getAmount().doubleValue();
                } else {
                    diffAmount -= adjust.getAmount().doubleValue();
                }
                log.debug("Amount Adjust: " + adjust.getAmount().doubleValue());
                if (reserve instanceof ClaimNormalReserve)
                    generateAuditTrailChangeState(this.claim, adjust.getType() == ReserveAdjustType.INCREASE.getValue() ? CustomAuditItem.INCREASE_OF_RESERVES : CustomAuditItem.DECREASE_OF_RESERVES, reserve.getPk(), null, adjust.getPk());

                if (reserve instanceof ClaimReserveByConcept)
                    generateAuditTrailChangeState(this.claim, CustomAuditItem.ADJUSTMENT_RESERVE, null, reserve.getPk(), adjust.getPk());

            }

            if (reserve instanceof ClaimNormalReserve) {
                ((ClaimNormalReserve) reserve).setLegacyType(adjustCollection.getLegacyType());
                ReserveAdjustType typeAdjust = (diffAmount > 0) ? ReserveAdjustType.INCREASE : ReserveAdjustType.DECREASE;
                entryReserve(Math.abs(diffAmount), planId, configuratedCoverage, typeAdjust.getValue(), ec, reserve.getDoneBy(),
                        reserve.getCurrency().getIsoCode(), reserve);
                generateReserveReinsuranceDistribution(reserve, String.valueOf(diffAmount), Constants.RESERVE_CLAIM, new java.sql.Date(new Date().getTime()), typeAdjust);

            } else if (reserve instanceof ClaimReserveByConcept) {
                ((ClaimReserveByConcept) reserve).setLegacyType(legacyType);
                reserve.update();
                ReserveAdjustType typeAdjust = (diffAmount > 0) ? ReserveAdjustType.INCREASE : ReserveAdjustType.DECREASE;
                generateConceptReserveReinsuranceDistribution((ClaimReserveByConcept) reserve, diffAmount, Constants.RESERVE_CLAIM, typeAdjust);
                entryReserve(Math.abs(diffAmount), typeAdjust.getValue(), reserve.getDoneBy(),
                        reserve.getCurrency() == null ? "" : reserve.getCurrency().getIsoCode(), ec, reserve);
            }
        }
    }

    public void updateReserveAuto(ClaimReserve reserve, Object receivedData, String planId, long configuratedCoverage, EvaluatedCoverage ec, String validation) {

        if (validation != null && validation.equals("Error")) {
            throw new TechnicalException("Error");
        }

        AdjustCollection adjustCollection = (AdjustCollection) receivedData;

        int oldAdjustListSize = 0;
        double diffAmount = 0;

        if (adjustCollection.getAdjustList().size() > oldAdjustListSize) {
            ClaimInsuranceObject cio = ((ClaimNormalReserve) reserve).getContainer();
            ClaimRiskUnit cru = cio.getContainer();
            ClaimsCoverageConfiguration ccc = getClaimCoverageConfiguration(cru.getRiskUnitId(), cio.getPk(), ec);
            ReserveAdjustType typeAdj;
            Vector adjustList = adjustCollection.getAdjustList();
            for (int i = 0; i < adjustCollection.getAdjustList().size(); i++) {

                ClaimReserveAdjust adjust = (ClaimReserveAdjust) adjustList.elementAt(i);
                if (adjust.getPK() != null) {
                    continue;
                }
                long id = generateNextId("CLAIMRESERVEADJUST");

                adjust.setPK(String.valueOf(id));
                adjust.setReserveDesc(adjustCollection.getReserveDesc());
                if(ccc.isAllowReserveDistribution()){
                    Map<String, ClaimReserveAdjust> listAdjust  =  reserve.getAdjustList();
                    typeAdj = ReserveAdjustType.INITIAL;
                    adjust.setType(typeAdj.getValue());
                    long ecid = ((ClaimNormalReserve) reserve).getEvaluatedCoverage().getId();
                    long ioid = ((ClaimNormalReserve) reserve).getClaimInsuranceObjectId();
                    int itemid =0;
                    if(ioid != 0){
                        itemid = ((ClaimNormalReserve) reserve).getMaxItemidForEvaluatedCoverage(ecid , ioid);
                    }
                    ClaimNormalReserve cnr =((ClaimNormalReserve) reserve);
                    cnr.setItemAjust(itemid+1);
                    cnr.setAdjustList(null);
                    cnr.setPaymentOrderList(null);
                    cnr.save();
                    adjust.setClaimReserveId(cnr.getPK());
                    reserve.setAdjustList(listAdjust);
                }
                adjust.save();
                if (reserve instanceof ClaimNormalReserve) {
                    ClaimValidationService claimValidationService = ((ClaimValidationService) BeanFactory.getBean(ClaimValidationService.class));
                    claimValidationService.claimreOpening(claim.getId(), ec);
                }
                reserve.getAdjustList().put(adjust.getPk(), adjust);

                if (adjust.getType() == ReserveAdjustType.INCREASE.getValue()) {
                    diffAmount += adjust.getAmount().doubleValue();
                } else {
                    diffAmount -= adjust.getAmount().doubleValue();
                }
                log.debug("Amount Adjust: " + adjust.getAmount().doubleValue());
            }
            double amountToPaid = 0.0;

            ((ClaimNormalReserve) reserve).setLegacyType(adjustCollection.getLegacyType());

            ReserveAdjustType adjustType = (diffAmount > 0) ? ReserveAdjustType.INCREASE : ReserveAdjustType.DECREASE;
            entryReserve(Math.abs(diffAmount), planId, configuratedCoverage, adjustType.getValue(), ec, reserve.getDoneBy(),
                    reserve.getCurrency().getIsoCode(), reserve);
            //amountToPaid = ((ClaimNormalReserve) reserve).getAmountWithDeductible();
            generateReserveReinsuranceDistribution(reserve, String.valueOf(diffAmount), Constants.RESERVE_CLAIM, new java.sql.Date(reserve.getDate().getTime()), adjustType);
        }
    }

    /**
     * @param action
     * @param receivedData
     */
    private void updateClaim(String action, Object receivedData) {
        if (action.equals("updateStatisticTitles")) {
            log.debug("[updateStatisticTitles]");
            claim.updateStatisticsTitles(receivedData);
        } else {
            if (claim.getNotification() != null) {
                Hashtable inputs = (Hashtable) receivedData;
                claim.getNotification().updateDCO(inputs);
            }
        }
    }

    /**
     * Updates the DCO object
     *
     * @param dco
     * @param receivedData
     */
    private void updateDCO(DefaultConfigurableObject dco, Object receivedData) {
        if (dco != null) {
            Hashtable inputs = (Hashtable) receivedData;
            Enumeration keys = inputs.keys();
            while (keys.hasMoreElements()) {
                String symbol = (String) keys.nextElement();
                String input = (String) inputs.get(symbol);

                Propiedad property = dco.getCOT().get(symbol);
                if (property != null) {
                    dco.setCriterioInput(property, input);

                }
                log.debug("symbol = " + symbol);
                log.debug("input = " + input);
            }
            ExpresionEvaluator evaluator = new ExpresionEvaluator();
            dco.evaluate(evaluator);
            dco.updateEditable();
        }
    }

    /**
     * @param agregatedRiskUnitid
     * @param insuranceObjectId
     * @return AgregatedInsuranceObject
     */
    private AgregatedInsuranceObject searchAgregatedInsuranceObject(String agregatedRiskUnitid, String insuranceObjectId) {
        if (isRetroactive()) {
            return policy.getAgregatedRiskUnit(agregatedRiskUnitid).getInsuranceObject(insuranceObjectId);
        } else {
            return claim.getClaimInsuranceObject(insuranceObjectId).getAgregatedInsuranceObject();
        }
    }

    /**
     * Used to associate the object assured indicated to the given reclamation
     *
     * @param damage
     * @param cio
     */
    private void associateTemplateIO(DefaultConfigurableObject damage, ClaimInsuranceObject cio) {
        if (cio != null) {
            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            evaluator.addSymbol("claimNumber", claim.getClaimNumber(), new Double(0), true);
            evaluator.evaluateConfigurableObject(damage);
            damage.updateEditable();
            cio.setDamage(damage);
            cio.setDamageDcoId(damage.getId());
            cio.setDamageDcotemplate(damage.getCOT().getId());
            cio.update();

        }
    }

    private EvaluatedCoverage searchEvaluatedCoverageWithDesc(AgregatedInsuranceObject aio, String desc) {
        if (aio != null) {
            if (isRetroactive()) {
                Collection idsCov = aio.getCoverages().getHashtable().keySet();
                EvaluatedCoverage cov = null;
                if (idsCov != null) {
                    for (Iterator iterator = idsCov.iterator(); iterator.hasNext(); ) {
                        String key = (String) iterator.next();
                        cov = aio.getEvaluatedCoverage(key);
                        log.debug("[searchEvaluatedCoverageWithDesc] cov.getDesc()" + cov.getDesc());
                        if (cov.getDesc().equalsIgnoreCase(desc)) {
                            break;
                        }
                    }
                    return cov;
                }
            } else {
                EvaluatedCoverage cov = null;
                List<EvaluatedCoverage> coverages = getEvaluatedCoverageList(aio);
                for (EvaluatedCoverage ec : coverages) {
                    if (ec.getDesc().equalsIgnoreCase(desc)) {
                        cov = ec;
                        break;
                    }
                }
                return cov;
            }
        }
        return null;
    }

    /**
     * Used to evaluate the coverages of the policy
     *
     * @param evaluatedCoverage
     * @return
     * @throws ApplicationExceptionChecked
     */
    private ExpresionEvaluator updateCoverage(EvaluatedCoverage evaluatedCoverage,
                                              ClaimRiskUnit cru,
                                              ClaimInsuranceObject cio) throws ApplicationExceptionChecked, RemoteException {
        ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
        String extraDays = policy.getProduct().getDCO().getCriterioInput(AcseleConf.getProperty("extraForceDaysProperty"));

        String currencyPolicy = policy.getDCO().getCriterioInput(AcseleConf.getProperty("monedaProperty"));

        if (evaluatedCoverage != null) {
            java.util.Date maxFinalDate = Funciones.sumDaysToDate(evaluatedCoverage.getFinishDate(), (extraDays != null) ? extraDays : "30");
            if (!((evaluatedCoverage.getInitialDate().compareTo(getClaim().getOcurrenceDate()) <= 0) && (
                    getClaim().getOcurrenceDate().compareTo(maxFinalDate) <= 0))) {
                log.debug(" No esta vigente la cobertura: " + evaluatedCoverage.getDesc() + " init date: " + evaluatedCoverage.getInitialDate()
                        + " final date: " + evaluatedCoverage.getFinishDate());

                throw new ApplicationExceptionChecked("claim.coverageExpires", Severity.ERROR);
            }
            evaluator.addSymbol(EvaluationConstants.CURRENT_ID_COVERAGE, evaluatedCoverage.getPK(), new Double(0), true);
            String ocurrenceDateStr = DateUtil.getDateToShow(getClaim().getOcurrenceDate());
            log.debug("ocurrenceDateStr = " + ocurrenceDateStr);

            evaluator.addSymbol(AcseleConf.getProperty(OCURRENCE_DATE), ocurrenceDateStr,
                    new Double(Funciones.dateTransformer.toNumber(ocurrenceDateStr)), true);
            ClaimUtil.fillEvaluator(evaluatedCoverage.getDCO(), evaluator); //agotamiento de coberturas
            String currencyCoverage = evaluatedCoverage.getDCO().getCriterioInput(AcseleConf.getProperty("monedaProperty"));

            currencyCoverage = (currencyCoverage == null) ? currencyPolicy : currencyCoverage;

            String currencyCoverageValue = evaluatedCoverage.getDCO().getCriterioValue(AcseleConf.getProperty("monedaProperty"));
            currencyCoverageValue = (currencyCoverageValue == null) ? policy.getDCO().getCriterioValue(AcseleConf.getProperty("monedaProperty"))
                    : currencyCoverageValue;

            try {
                evaluator.addSymbol(AcseleConf.getProperty("monedaProperty"), currencyCoverage, new Double(currencyCoverageValue), true);
            } catch (Exception e) {
                Currency currency =
                        Currency.Impl.load(AcseleConf.getProperty("OpenItemCurrency"));
                evaluator.addSymbol(AcseleConf.getProperty("monedaProperty"), currency.getDescription(),
                        Double.valueOf(currency.getId()), true);
            }
            String reseveLimit = this.getCoverageReserveLimit(evaluatedCoverage, cio);
            double amountReserveLimit = Double.parseDouble(reseveLimit);
            evaluator.addSymbol("reserveLimit", reseveLimit, new Double(amountReserveLimit), false);
        } else {
            throw new ApplicationExceptionChecked("claim.batConfigurationForCoverage", Severity.ERROR);
        }
        return evaluator;
    }

    /**
     * Returns the next claimPaymentID
     *
     * @return int
     */
    private long generateNextId(String table) {
        long id = -1;
        try {
            id = IDDBFactory.getNextIDL(table);
        } catch (Exception e) {
            log.error("Error", e);
        }
        return id;
    }

    /**
     * @param reserve
     * @param amount
     * @param type
     */
    public void generateReserveReinsuranceDistribution(ClaimReserve reserve, String amount, int type, java.sql.Date operationDate, ReserveAdjustType typeAdjust) {

            ReinsuranceDistributionService gcs = ReinsuranceDistributionService.Impl.getInstance();
            MaintainGenericContractsImpl mgc = new MaintainGenericContractsImpl();
            java.sql.Date today = null;
        String policyId = policy.getPK();
        String riskUnitId = null;
        String insuranceObjectId = null;
        EvaluatedCoverage evaluatedCoverage = null;
        //String evaluatedCoverageId = null;
        AgregatedInsuranceObject aio = null;
        AgregatedRiskUnit agregatedRiskUnit = null;
        if (reserve instanceof ClaimNormalReserve) {
            ClaimNormalReserve normalReserve = (ClaimNormalReserve) reserve;
            agregatedRiskUnit = normalReserve.getContainer().getContainer().getAgregatedRiskUnit();
            riskUnitId = agregatedRiskUnit.getPk();
            insuranceObjectId = normalReserve.getContainer().getAgregatedInsuranceObject().getPk();
            evaluatedCoverage = normalReserve.getEvaluatedCoverage();
            //evaluatedCoverageId = evaluatedCoverage.getPk();
            aio = normalReserve.getContainer().getAgregatedInsuranceObject();
        } else if (reserve instanceof ClaimReserveByConcept) {
            ClaimReserveByConcept conceptReserve = (ClaimReserveByConcept) reserve;
            agregatedRiskUnit = conceptReserve.getContainer().getContainer().getAgregatedRiskUnit();
            riskUnitId = agregatedRiskUnit.getPk();
            insuranceObjectId = conceptReserve.getContainer().getAgregatedInsuranceObject().getPk();
            aio = agregatedRiskUnit.getInsuranceObject(conceptReserve.getContainer().getDesc());
            Long normalReserveId = conceptReserve.getNormalReserveId();
            ClaimNormalReserve cnr = ClaimNormalReserve.load(String.valueOf(normalReserveId.longValue()));
            evaluatedCoverage = cnr.getEvaluatedCoverage();
            //evaluatedCoverageId = evaluatedCoverage.getPk();
        }

        try {
            String ocurrenceDateStr = aio.getDCO().getCriterioInput(AcseleConf.getProperty(OCURRENCE_DATE));
            today = new java.sql.Date(DateUtil.getFormatToShow().parse(ocurrenceDateStr).getTime());
        } catch (Exception e) {
            log.error(" Error looking for value of property " + AcseleConf.getProperty(OCURRENCE_DATE)
                    + " on Insurance Object. Check 'ocurrenceDate' system property");
            today = policy.getOperationPK().getTimeStamp();

            //YJA - 2009-10-08 La fecha a utilizar es la del evento de la poliza que es la misma del
            // timeStamp de la operacion.
        }
        BigDecimal monto = new BigDecimal(amount);
        String reinsuranceGroup = evaluatedCoverage.getDCO().getCriterioInput(AcseleConf.getProperty("coverageBough"));

        try {

            OperationHistory operationHistory = new OperationHistory();
            operationHistory.setAgregatedPolicyId(Long.valueOf(policyId));
            operationHistory.setHistoryDetails(new HashSet<OperationHistoryDetail>());
            if (reserve instanceof ClaimNormalReserve) {
                operationHistory.setNormalReserveId(reserve.getPK());
            } else if (reserve instanceof ClaimReserveByConcept) {
                Long normalReserveId = ((ClaimReserveByConcept) reserve).getNormalReserveId();
                operationHistory.setNormalReserveId(String.valueOf(normalReserveId));

            }
            operationHistory.setDate(new Date());
            operationHistory.setAmount(monto.doubleValue());
            operationHistory.setHistoryType(HistoryType.CLAIM);

            Long claimReserveAdjustId = null;
            Long paymentOrderId = null;
            int claimStatus = claim.getClaimStatus().getValue();
            if (type == Constants.RESERVE_CLAIM && reserve.getLastReserveAdjust() != null) {
                claimReserveAdjustId = Long.parseLong(reserve.getLastReserveAdjust().getPK());
                String reason = reserve.getLastReserveAdjust().getReason();
                if(!StringUtil.isEmptyOrNullValue(reason) && reason.equals(ClaimReserveAdjust.CLAIM_FINAL_PAYMENT)){
                    claimStatus = ClaimStatus.CLOSED.getValue();
                }
            } else if (type == Constants.PAYMENT_CLAIM && reserve.getPaymentOrderList() != null) {
                paymentOrderId = 0l;
                for (Map.Entry<Long, PaymentOrder> entry : reserve.getPaymentOrderList().entrySet()) {
                    if (entry.getKey() > paymentOrderId)
                        paymentOrderId = entry.getKey();
                }
                paymentOrderId = paymentOrderId == 0l ? null : paymentOrderId;
            }
            //  operationHistory.save();
            gcs.generateSinisterDistributionForUAAByType(reinsuranceGroup, evaluatedCoverage, operationDate, monto, type, (int) reserve.getCurrencyId(), this.claim.getPK(), operationHistory, typeAdjust, claimReserveAdjustId, paymentOrderId, null, claimStatus, reserve);

            log.debug("generateReserveReinsuranceDistribution Coaseguro-> policyId: " + policyId + " - riskUnitId: " + riskUnitId
                    + " - insuranceObjectId: " + insuranceObjectId + " - date: " + operationDate + " - amount: " + monto + " - type: " + type);
            /*try {
                mgc.generateSinisterDistributionForUAAByType(policy, riskUnitId, insuranceObjectId, cov, operationDate, monto, type, (int) reserve.getCurrencyId());
            } catch (Exception e) {
                log.error("Error coinsuring.", e);
            }*/

            if (type == Constants.PAYMENT_CLAIM) {
                try {
                    gcs.generateExcessLossDistribution(reinsuranceGroup, policy, riskUnitId, insuranceObjectId, claim.getPK(),
                            claim.getExcessClaimEventID(), operationDate, monto, (int) reserve.getCurrencyId());
                } catch (Exception e) {
                    log.error("Error generating excess distribution .", e);
                }
            }
            log.debug("Ending generateReserveReinsuranceDistribution...");
        } catch (Exception e) {
            log.error("Error reinsuring.", e);
        }
    }


    public void generateReserveReinsuranceDistributionReverse(ClaimReserve reserve, String amount, int type, ReserveAdjustType typeAdjust) {
        String policyId = policy.getPK();
        String riskUnitId = null;
        java.sql.Date today = null;
        String insuranceObjectId = null;
        ReinsuranceDistributionService gcs = new ReinsuranceDistributionService.Impl().getInstance();
        EvaluatedCoverage evaluatedCoverage = null;
        log.debug("amount - type - typeAdjust " + amount + " " + type + " " + typeAdjust);
        AgregatedInsuranceObject aio = null;
        AgregatedRiskUnit agregatedRiskUnit = null;
        if (reserve instanceof ClaimNormalReserve) {

            ClaimNormalReserve normalReserve = (ClaimNormalReserve) reserve;
            agregatedRiskUnit = normalReserve.getContainer().getContainer().getAgregatedRiskUnit();
            riskUnitId = agregatedRiskUnit.getPk();
            normalReserve.loadAdjust();
            insuranceObjectId = normalReserve.getContainer().getAgregatedInsuranceObject().getPk();
            evaluatedCoverage = normalReserve.getEvaluatedCoverage();

            aio = normalReserve.getContainer().getAgregatedInsuranceObject();
        } else if (reserve instanceof ClaimReserveByConcept) {
            ClaimReserveByConcept conceptReserve = (ClaimReserveByConcept) reserve;
            agregatedRiskUnit = conceptReserve.getContainer().getContainer().getAgregatedRiskUnit();
            riskUnitId = agregatedRiskUnit.getPk();

            insuranceObjectId = conceptReserve.getContainer().getAgregatedInsuranceObject().getPk();
            aio = agregatedRiskUnit.getInsuranceObject(conceptReserve.getContainer().getDesc());
            Long normalReserveId = conceptReserve.getNormalReserveId();
            ClaimNormalReserve cnr = ClaimNormalReserve.load(String.valueOf(normalReserveId.longValue()));
            evaluatedCoverage = cnr.getEvaluatedCoverage();
        }

        try {
            String ocurrenceDateStr = aio.getDCO().getCriterioInput(AcseleConf.getProperty(OCURRENCE_DATE));
            today = new java.sql.Date(DateUtil.getFormatToShow().parse(ocurrenceDateStr).getTime());
        } catch (Exception e) {
            log.error(" Error looking for value of property " + AcseleConf.getProperty(OCURRENCE_DATE)
                    + " on Insurance Object. Check 'ocurrenceDate' system property");
            today = policy.getOperationPK().getTimeStamp();
//ojo - revisa esta fecha, coloca la de la poliza y es menor a la de hoy
        }
        BigDecimal monto = new BigDecimal(amount);
        String reinsuranceGroup = evaluatedCoverage.getDCO().getCriterioInput(AcseleConf.getProperty("coverageBough"));
        try {

            OperationHistory operationHistory = new OperationHistory();
            operationHistory.setAgregatedPolicyId(Long.valueOf(policyId));
            operationHistory.setHistoryDetails(new HashSet<OperationHistoryDetail>());
            if (reserve instanceof ClaimNormalReserve) {
                operationHistory.setNormalReserveId(reserve.getPK());
            } else if (reserve instanceof ClaimReserveByConcept) {
                Long normalReserveId = ((ClaimReserveByConcept) reserve).getNormalReserveId();
                operationHistory.setNormalReserveId(String.valueOf(normalReserveId));

            }
            operationHistory.setDate(new Date());
            operationHistory.setAmount(monto.doubleValue());
            operationHistory.setHistoryType(HistoryType.CLAIM);

            Long claimReserveAdjustId = null;
            Long paymentOrderId = null;
            if (type == Constants.RESERVE_CLAIM && reserve.getLastReserveAdjust() != null) {
                claimReserveAdjustId = Long.parseLong(reserve.getLastReserveAdjust().getPK());
            } else if (type == Constants.PAYMENT_CLAIM && reserve.getPaymentOrderList() != null) {
                paymentOrderId = 0l;
                for (Map.Entry<Long, PaymentOrder> entry : reserve.getPaymentOrderList().entrySet()) {
                    if (entry.getKey() > paymentOrderId)
                        paymentOrderId = entry.getKey();
                }
                paymentOrderId = paymentOrderId == 0l ? null : paymentOrderId;
            }


            gcs.generateSinisterDistributionReverseForUAAByType(reinsuranceGroup, evaluatedCoverage, today, monto, type, (int) reserve.getCurrencyId(), this.claim.getPK(), operationHistory, typeAdjust, claimReserveAdjustId, paymentOrderId, null, claim.getClaimStatus().getValue(), reserve);

            log.debug("generateReserveReinsuranceDistributionReverse -> policyId: " + policyId + " - riskUnitId: " + riskUnitId
                    + " - insuranceObjectId: " + insuranceObjectId + " - date: " + today + " - amount: " + monto + " - type: " + type);

            if (type == Constants.PAYMENT_CLAIM) {
                try {
                    gcs.generateExcessLossDistribution(reinsuranceGroup, policy, riskUnitId, insuranceObjectId, claim.getPK(),
                            claim.getExcessClaimEventID(), today, monto, (int) reserve.getCurrencyId());
                } catch (Exception e) {
                    log.error("Error generating excess distribution .", e);
                }
            }
            log.debug("Ending generateReserveReinsuranceDistributionReverse...");
        } catch (Exception e) {
            log.error("Error reinsuring reverse.", e);
        }
    }

//    /**
//     * Reverse Generate Reserve Reinsurance Distribution Reverse
//     *
//     * @param reserve
//     * @param amount
//     * @param type
//     */
//    @Deprecated
//    private void generateReserveReinsuranceDistributionReverse(ClaimNormalReserve reserve, String amount, int type) {
//        try {
//            AgregatedRiskUnit aru = reserve.getContainer().getContainer().getAgregatedRiskUnit();
//            AgregatedInsuranceObject aio = reserve.getContainer().getAgregatedInsuranceObject();
//            EvaluatedCoverage cov = reserve.getEvaluatedCoverage();
//            MaintainGenericContractsImpl mgc = new MaintainGenericContractsImpl();
//            String riskUnitId = aru.getPk();
//            String insuranceObjectId = aio.getPk();
//            java.sql.Date today = null;
//
//            try {
//                String ocurrenceDateStr = aio.getDCO().getCriterioInput(AcseleConf.getProperty(OCURRENCE_DATE));
//                today = new java.sql.Date(DateUtil.getFormatToShow().parse(ocurrenceDateStr).getTime());
//            } catch (Exception e) {
//                today = new java.sql.Date(reserve.getDate().getTime());
//            }
//
//            BigDecimal monto = new BigDecimal(amount);
//
//            try {
//                mgc.generateSinisterDistributionForUAAByTypeReverse(policy, riskUnitId, insuranceObjectId, cov, today, monto, type,
//                        (int) reserve.getCurrencyId());
//            } catch (Exception e) {
//                log.error("generateReserveReinsuranceDistributionReverse Error coinsuring.", e);
//            }
//        } catch (Exception e) {
//            log.error("Error reinsuring.", e);
//        }
//    }


    /**
     * @param reserve
     * @param amount
     * @param type
     */
    private void generateConceptReserveReinsuranceDistribution(ClaimReserveByConcept reserve, double amount, int type, ReserveAdjustType typeAdjust) {

        try {
            //todo solo por prueba - no se deberia capturar esta excepcion ,
            // si ocurre un error no debe proseguir
            AgregatedRiskUnit agregatedRiskUnit = reserve.getContainer().getContainer().getAgregatedRiskUnit();
            AgregatedInsuranceObject agregatedInsuranceObject = reserve.getContainer().getAgregatedInsuranceObject();

            ReinsuranceDistributionService gcs = ReinsuranceDistributionService.Impl.getInstance();
            //AgregatedRiskUnit riskUnitId = aru;
            String insuranceObjectId = agregatedInsuranceObject.getPk();

            java.sql.Date today = null;
            try {
                String ocurrenceDateStr = agregatedInsuranceObject.getDCO().getCriterioInput(AcseleConf.getProperty(OCURRENCE_DATE));
                today = new java.sql.Date(DateUtil.getFormatToShow().parse(ocurrenceDateStr).getTime());
            } catch (Exception e) {
                today = new java.sql.Date(System.currentTimeMillis());
            }

            BigDecimal monto = new BigDecimal(amount);

            Long claimReserveAdjustId = null;
            if (type == Constants.RESERVE_CLAIM && reserve.getLastReserveAdjust() != null) {
                claimReserveAdjustId = Long.parseLong(reserve.getLastReserveAdjust().getPK());
            }

            gcs.generateClaimDistributionForUAAOtherConceptsByType(reserve, agregatedInsuranceObject, today,
                    monto, type, (int) reserve.getCurrencyId(), claim.getPK(), claimReserveAdjustId, typeAdjust);
        } catch (Exception e) {
            log.error(" Excepcion en reaseguro ", e);
        }
    }


    /**
     * Creates the requirements of the claim in data base, updating the view of the user.
     *
     * @param myRequisite
     * @return Set
     * @throws ApplicationExceptionChecked
     */
    private void processAddRequisite(RequisiteType myRequisite) throws ApplicationExceptionChecked {
        log.debug("(Claim Entity EJB)  Adding requisite");
        if (myRequisite != null) {
            log.debug("elems ");
            Enumeration elems = myRequisite.getPropiedades().elements();
            log.debug("Begin while");
            while (elems.hasMoreElements()) {
                Propiedad propiedad = (Propiedad) elems.nextElement();
                ClaimRequisite claimRequisite = new ClaimRequisite(propiedad);
                claimRequisite.setClaim(claim);
                try {
                    claimRequisite.save();
                } catch (Exception ex) {
                    log.error("Error", ex);
                    throw new ApplicationExceptionChecked("requisites.errToAdd", Severity.FATAL);
                }
                log.debug("Adding Requisite " + claimRequisite.getDescription());
            }
        }
    }

    /**
     *
     */
    private void reloadClaim() {
        if (asl) {
            return;
        } else if (claim != null) {
            log.debug("[reloadClaim] claim.getPk()" + claim.getPk());
            log.debug("[ClaimComposerWrapper - 4080]");
            claim = Claim.getInstance(Long.valueOf(claim.getPk()));
        } else {
            log.debug("[ClaimComposerWrapper - 4083]");
            try {
                this.setAgregatedPolicy(getAgregatedPolicy(claim));
            } catch (Exception e) {
                log.error("Error", e);
            }
        }
    }

    /**
     * loads a policy
     *
     * @param claim
     * @return an AgregatedPolicy object or null if something fails
     */
    public synchronized AgregatedPolicy getAgregatedPolicy(Claim claim) {
//        log.debug("********[Acsel-e v4.5] ClaimComposerWrapper.getAgregatedPolicy{}");
        PolicySession policySession = PolicySession.Impl.getInstance();
//        AgregatedPolicy policy = null;
        try {
            policySession.findByPolicyAndDateInValidity(claim.getPolicyId(), getOcurrenceDate());
            this.setAgregatedPolicy(policySession.getAgregatedPolicy());
        } catch (Exception exception) {
            log.error(exception);
        }
        return policy;
    }

    /**
     * loads a policy
     *
     * @param claimId
     * @return an AgregatedPolicy object or null if something fails
     */
    public synchronized AgregatedPolicy getAgregatedPolicy(String claimId) {
        Session session = HibernateUtil.getSession();
        Claim claim = null;
        try {
            claim = (Claim) HibernateUtil.load(Claim.class, claimId);
        } catch (Exception e) {
            log.debug(e);
//        } finally {
            //HibernateUtil.closeSession(session);
        }

        return getAgregatedPolicy(claim);
    }

    public void setAgregatedPolicyASL(String policyID, java.util.Date ocurrenceDate) {
        log.debug("********[Acsel-e v4.5] ClaimComposerWrapper.getAgregatedPolicy{}");
        PolicySession policySession = PolicySession.Impl.getInstance();
        AgregatedPolicy ap = null;
        try {
            policySession.findByPolicyAndDateInValidity(policyID, ocurrenceDate);
            ap = policySession.getAgregatedPolicy();
        } catch (Exception exception) {
            log.debug(exception);
        }
        this.setAgregatedPolicy(ap);
    }

    public void setAgregatedPolicyASL(AgregatedPolicy ap) {
        log.debug("********[Acsel-e v4.5] ClaimComposerWrapper.getAgregatedPolicy{}");
        this.setAgregatedPolicy(ap);
    }

    /**
     * This method saves an history of the claim states
     *
     * @param bean
     * @throws ApplicationExceptionChecked
     */
    private void setState(ClaimStateBean bean) throws ApplicationExceptionChecked {
        try {
            ClaimStatus newState = bean.getClaimStatus();
            CustomAuditItem customAuditItem = null;
            bean.setClaimStatusOld(claim.getClaimStatus().toString());
            String data = "for id: " + claim.getPk() + ": " + claim.getClaimStatus().toString()
                    + " -> " + newState.toString();
            if (claim.getClaimStatus() == ClaimStatus.IN_PROCESS) {
                if (newState == ClaimStatus.CLOSED) {
                    claim.setClaimStatus(newState);
                    claim.update();
                    if (bean.getCotname().equals(AcseleConf.getProperty(TEMPLATE_CLAIM_DENIED))) {
                        customAuditItem = CustomAuditItem.CLAIM_DENIAL;
                    } else {
                        customAuditItem = CustomAuditItem.CLAIM_CLOSED;
                    }
                } else if (newState == ClaimStatus.DENIED) {
                    claim.setClaimStatus(newState);
                    claim.update();
                    customAuditItem = CustomAuditItem.CLAIM_DENIAL;
                } else if (newState == ClaimStatus.IN_PROCESS) {
                    //  NO HACE NADA
                } else if ((newState == ClaimStatus.PENDING) ||
                        (newState == ClaimStatus.PAID) ||
                        (newState == ClaimStatus.CANCELED)) {
                    claim.setClaimStatus(newState);
                    claim.update();
                    customAuditItem = CustomAuditItem.CLAIM_CANCEL;
                } else {
                    throw new ApplicationExceptionChecked(Exceptions.CCInvalidNewState, Severity.INFO, data);
                }
            } else if (claim.getClaimStatus() == ClaimStatus.PENDING) {
                if (newState == ClaimStatus.DENIED) {
                    customAuditItem = CustomAuditItem.CLAIM_DENIAL;
                } else if ((newState == ClaimStatus.IN_PROCESS) ||
                        (newState == ClaimStatus.PENDING)) {
                    //  NO HACE NADA, ES UN NUEVO OBJETO
                } else if (newState == ClaimStatus.CANCELED) {
                    claim.setClaimStatus(newState);
                    claim.update();
                    if (bean.getCotname().equals(AcseleConf.getProperty(TEMPLATE_CLAIM_DENIED))) {
                        customAuditItem = CustomAuditItem.CLAIM_DENIAL;

                    } else {
                        customAuditItem = CustomAuditItem.CLAIM_CLOSED;
                    }
                } else {
                    throw new ApplicationExceptionChecked(Exceptions.CCInvalidNewState, Severity.INFO, data);
                }
            } else if (claim.getClaimStatus() == ClaimStatus.PAID) {
                if ((newState == ClaimStatus.IN_PROCESS) ||
                        (newState == ClaimStatus.PAID)) {
                    //  NO HACE NADA, ES UN NUEVO OBJETO
                } else if ((newState == ClaimStatus.PENDING) ||
                        (newState == ClaimStatus.CLOSED)) {
                    if (bean.getCotname().equals(AcseleConf.getProperty(TEMPLATE_CLAIM_DENIED))) {
                        customAuditItem = CustomAuditItem.CLAIM_DENIAL;
                    } else {
                        customAuditItem = CustomAuditItem.CLAIM_CLOSED;
                    }
                } else if (newState == ClaimStatus.CANCELED) {
                    claim.setClaimStatus(newState);
                    claim.update();
                    customAuditItem = CustomAuditItem.CLAIM_CANCEL;
                } else {
                    throw new ApplicationExceptionChecked(Exceptions.CCInvalidNewState, Severity.INFO, data);
                }
            } else if (claim.getClaimStatus() == ClaimStatus.CLOSED) {
                if (newState == ClaimStatus.RE_OPEN) {
                    claim.setClaimStatus(newState);
                    this.claim.update();
                    customAuditItem = CustomAuditItem.RE_OPEN_CLAIM;
                } else {
                    throw new ApplicationExceptionChecked(Exceptions.CCInvalidNewState, Severity.INFO, data);
                }
            } else if (claim.getClaimStatus() == ClaimStatus.RE_OPEN) {
                if (newState == ClaimStatus.DENIED) {
                    customAuditItem = CustomAuditItem.CLAIM_DENIAL;
                    claim.setClaimStatus(newState);
                    claim.update();

                } else if (newState == ClaimStatus.IN_PROCESS) {
                    //  NO HACE NADA
                } else if (newState == ClaimStatus.CANCELED) {
                    claim.setClaimStatus(newState);
                    claim.update();
                    customAuditItem = CustomAuditItem.CLAIM_CANCEL;
                } else if (newState == ClaimStatus.CLOSED) {
                    if (bean.getCotname().equals(AcseleConf.getProperty(TEMPLATE_CLAIM_DENIED))) {
                        customAuditItem = CustomAuditItem.CLAIM_DENIAL;
                    } else {
                        customAuditItem = CustomAuditItem.CLAIM_CLOSED;
                    }
                    claim.setClaimStatus(newState);
                    claim.update();
                } else if ((newState == ClaimStatus.PENDING) ||
                        (newState == ClaimStatus.PAID)) {
                    claim.setClaimStatus(newState);
                    claim.update();
                } else {
                    throw new ApplicationExceptionChecked(Exceptions.CCInvalidNewState, Severity.INFO, data);
                }
            } else if (claim.getClaimStatus() == ClaimStatus.DENIED) {
                if ((newState == ClaimStatus.IN_PROCESS) ||
                        (newState == ClaimStatus.RE_OPEN)) {
                    log.debug("Se ha reabierto el siniestro!!!!!");
                    customAuditItem = CustomAuditItem.RE_OPEN_CLAIM;
                    claim.setClaimStatus(newState);
                    claim.update();
                } else {
                    throw new ApplicationExceptionChecked(Exceptions.CCInvalidNewState, Severity.INFO, data);
                }
            }
            bean.save();
            generateAuditTrailChangeState(bean, customAuditItem);
        } catch (Exception e) {
            log.error("The available environment snapshot is: \n" + "claimId: " + this.claim.getPk());
            ExceptionUtil.handleException(Exceptions.CCErrorSettingState, e);
        }
    }

    private void generateAuditTrailChangeState(ClaimStateBean bean, CustomAuditItem customAuditItem) {
        AuditTrailManager manager = AuditTrailManager.getInstance();
        Claim claimLoaded = Claim.getInstance(Long.valueOf(bean.getClaimId()));

        Context claimContext = Context.createClaimContext(Long.valueOf(bean.getClaimId()), claimLoaded.getClaimNumber(), null);
        claimLoaded.getAgregatedPolicy().load();
        log.debug("opk=" + claimLoaded.getAgregatedPolicy().getOperationPK().getPK());
        log.debug("opkLast=" + claimLoaded.getAgregatedPolicy().getLastAppliedOperationId());
        Context policyContext = Context.createPolicyContext(Long.valueOf(claimLoaded.getPolicyId()), claimLoaded.getAgregatedPolicy().getPolicyNumber(),
                claimLoaded.getAgregatedPolicy().getLastAppliedOperationId(), null, null);
        List<Context> contexts = new ArrayList<Context>();
        ((ClaimContext) claimContext).setClaimStateId(bean.getPk());
        contexts.add(claimContext);
        contexts.add(policyContext);
        manager.generateAuditTrail(customAuditItem, contexts);

    }

    private void generateAuditTrailChangeState(Claim claim, CustomAuditItem customAuditItem, String claimNormalReservedId, String claimReservedByConceptId, String claimReserveAdjustId) {
        AuditTrailManager manager = AuditTrailManager.getInstance();
        Claim claimLoaded = Claim.getInstance(claim.getId());

        Context claimContext = Context.createClaimContext(claim.getId(), claimLoaded.getClaimNumber(), null);
        claimLoaded.getAgregatedPolicy().load();
        log.debug("opk=" + claimLoaded.getAgregatedPolicy().getOperationPK().getPK());
        log.debug("opkLast=" + claimLoaded.getAgregatedPolicy().getLastAppliedOperationId());
        Context policyContext = Context.createPolicyContext(Long.valueOf(claimLoaded.getPolicyId()), claimLoaded.getAgregatedPolicy().getPolicyNumber(),
                claimLoaded.getAgregatedPolicy().getLastAppliedOperationId(), null, null);
        List<Context> contexts = new ArrayList<Context>();
        if (!StringUtil.isEmptyOrNullValue(claimNormalReservedId))
            ((ClaimContext) claimContext).setClaimNormalReservedId(Long.valueOf(claimNormalReservedId));
        if (!StringUtil.isEmptyOrNullValue(claimReservedByConceptId))
            ((ClaimContext) claimContext).setClaimReserveByConceptId(Long.valueOf(claimReservedByConceptId));
        if (!StringUtil.isEmptyOrNullValue(claimReserveAdjustId))
            ((ClaimContext) claimContext).setClaimReserveAdjustId(Long.valueOf(claimReserveAdjustId));
        contexts.add(claimContext);
        contexts.add(policyContext);
        manager.generateAuditTrail(customAuditItem, contexts);

    }

    /**
     * @param newState int value
     * @throws ApplicationExceptionChecked
     */
    private void setState(ClaimStatus claimStatus, ClaimHistory claimHistory) throws ApplicationExceptionChecked {
        log.debug("set state -> old: " + claim.getClaimStatus().toString()
                + " new : " + claimStatus.toString());
        ClaimStateBean bean = new ClaimStateBean();

        DefaultConfigurableObject dco = claimHistory.getDCO();
        bean.setClaimId(claim.getPk());
        bean.setClaimStatus(claimStatus);

        ConfigurableObjectType defaultConfigurableObjectType = dco.getCOT();
        bean.setCotid(defaultConfigurableObjectType.getPk());
        bean.setCotname(defaultConfigurableObjectType.getDesc());
        bean.setDcoid(dco.getPk());
        bean.setOperationdate(new java.util.Date());
        setState(bean);
    }

    /**
     * @param newState int value
     * @throws ApplicationExceptionChecked
     */
    private void setState(ClaimStatus claimStatus) throws ApplicationExceptionChecked {
        log.debug("set state -> old: " + claim.getClaimStatus().toString()
                + " new : " + claimStatus.toString());
        ClaimStateBean bean = new ClaimStateBean();
        bean.setClaimId(claim.getPk());
        bean.setClaimStatus(claimStatus);
        bean.setCotid(null);
        bean.setCotname(null);
        bean.setDcoid(null);
        bean.setOperationdate(new java.util.Date());
        setState(bean);
    }

    /**
     * Updates the affected object
     *
     * @param damageTemlateName
     * @param dcoPk
     * @param receivedData
     * @return DefaultConfigurableObject
     */
    public DefaultConfigurableObject updateAffectedObject(String damageTemplateName, String dcoPk, Object receivedData) {
        log.debug("damageTemlateName = " + damageTemplateName);
        Categorias categorias = Categorias.getBean(Categorias.EXTENDED_TEMPLATES_STATE);
        ConfigurableObjectType cot =
                (ConfigurableObjectType) categorias.get(CotType.CLAIM, damageTemplateName);
        log.debug("[DEBUG] Claim Entity EJB  Claim Insurance Object Created - COT = " + cot);
        DefaultConfigurableObject dco = DefaultConfigurableObject.load(cot, Long.valueOf(dcoPk));
        updateDCO(dco, receivedData);
        return dco;
    }

    /**
     * Verify that all requisites mandatories were received
     *
     * @return tru if were received , false otherwise
     * @throws ApplicationExceptionChecked
     */
    private boolean mandatoryRequisitesReceived() throws ApplicationExceptionChecked {
        Collection<ClaimRequisite> list = claim.getRequisiteCheckList();
        if (list != null && !list.isEmpty()) {
            for (ClaimRequisite claimRequisite : list) {
                if ((claimRequisite.isMandatory()) && (!(claimRequisite.isChecked()))) {
                    throw new ApplicationExceptionChecked("generateClaimPayment.requisitesIncomplete", Severity.FATAL);
                }
            }
        }
        return true;
    }

    /**
     * It calculates the maximun obligation for the given payment order.
     *
     * @param paymentOrder The payment order witch maximun obligation must be calculated.
     * @return The maximun obligation ammopunt for the payment order parameter
     */
    private double calculateMaxObligationForPaymentOrder(PaymentOrder paymentOrder) {
        double compensateableAmount;
        double subTotalcompensateableAmount;
        double deductibleAmount;
        double refundAmount; // TODO si es 0 no debe dar negativo
        double penaltyAmount;
        double maxObligation;
        double invoiceCompensateable = ClaimUtil.getAmountInvoiceCompensateable(paymentOrder);
        if (paymentOrder.getType() == ReserveType.CONCEPT_RESERVE.getValue()) {

            compensateableAmount = paymentOrder.getAmount() != null ? paymentOrder.getAmount().doubleValue() : invoiceCompensateable;

            subTotalcompensateableAmount = compensateableAmount;
        } else {
            compensateableAmount = paymentOrder.getAmount() != null ? paymentOrder.getAmount().doubleValue() : invoiceCompensateable;
//            deductibleAmount = (paymentOrder.getDeductibleAmount() > 0) ? paymentOrder
//                    .getDeductibleAmount() : paymentOrder.getCompensateableAmount() * paymentOrder
//                    .getDeductiblePercentage();
            deductibleAmount = paymentOrder.getDeductibleAmount();
            refundAmount = (compensateableAmount > 0) ? (compensateableAmount - (paymentOrder.isDeductibleApplied() ? deductibleAmount : 0)) * (
                    paymentOrder.isRefundApplied() ? paymentOrder.getRefundPercentage() : 1) : 0;  //todo si es 0 no debe dar negativo
            penaltyAmount = refundAmount * (paymentOrder.isPenaltyApplied() ? paymentOrder.getPenaltyPercentage() : 0);
            subTotalcompensateableAmount = refundAmount - (paymentOrder.isPenaltyApplied() ? penaltyAmount : 0) - paymentOrder.getAmountFranchise();

        }
        double distributionAmount = 0;
        int sizeTaxes = paymentOrder.getPaymentOrderTaxes() != null ? paymentOrder.getPaymentOrderTaxes().size() : 0;
        if (sizeTaxes > 0) {
            distributionAmount = ClaimUtil.getAmountDistribution(paymentOrder, compensateableAmount);
        }
        maxObligation = subTotalcompensateableAmount + distributionAmount;
        return maxObligation;
    }

    /**
     * @param ruID
     * @return Collection
     */
    // TODO: 2007-01-24 (DES-GS) Mega To Do: Esto devuelve los clientes de la póliza y RU
    private List checkINDBeneficiaryPreferential(AgregatedRiskUnit aru) {
        String INDBeneficiaryPreferential = AcseleConf.getProperty("policy.INDBeneficiaryPreferential");
        String valueProperty = (String) policy.getInput().get(INDBeneficiaryPreferential);
        if (valueProperty == null) { //Se Busca en la Unidad de Riesgo
            INDBeneficiaryPreferential = AcseleConf.getProperty("ru.INDBeneficiaryPreferential");
            valueProperty = (String) aru.getInput().get(INDBeneficiaryPreferential);
            if (valueProperty != null) {
                double valor = LifeFunctions.getTransformador(INDBeneficiaryPreferential, valueProperty);
                double valueYes = Double.parseDouble(AcseleConf.getProperty("ru.INDBeneficiaryPreferentialYesValue"));
                if (valor == valueYes) {
                    return getClients(aru.getPk());
                }
            }
        } else {
            double valor = LifeFunctions.getTransformador(INDBeneficiaryPreferential, valueProperty);
            double valueYes = Double.parseDouble(AcseleConf.getProperty("policy.INDBeneficiaryPreferentialYesValue"));
            if (valor == valueYes) {
                return getClients(null);
            }
        }
        return new ArrayList();
    }

    /**
     * @param ruID
     * @param ioID
     * @return Collection
     */
    private Collection getBeneficarys(String ruID, String ioID) {
        RoleList roleList = RoleGroup.BENEFICIARY_ROLES.getRoleList();
        Collection participaciones = getParticipation(roleList, null);
        Collection ruPart = getParticipation(roleList, ruID);
        if (!ruPart.isEmpty()) {
            participaciones.addAll(ruPart);
        }
        Collection participationBeneficarys = getThirdPartyData(participaciones);

        try {
            AgregatedInsuranceObject aio = getAgregatedInsuranceObjectById(ruID, ioID);
            Collection partIO = checkBeneficiaryRoleInCollection(aio.getParticipations(null),
                    roleList);

            if (!partIO.isEmpty()) {
                participationBeneficarys.addAll(partIO);
            }
        } catch (Exception e) {
            log.error("Error", e);
        }
        return participationBeneficarys;
    }

    /**
     * @param aru
     * @return Collection
     */
    private List getClients(String ruID) {
        log.debug("SEARCH Customer in Policy");
        List resp = new ArrayList();
        Collection clients = getParticipations(ruID, RoleGroup.CLIENT_ROLES.getRoleList());
        if (!clients.isEmpty()) {
            for (Iterator iterator = clients.iterator(); iterator.hasNext(); ) {
//                String idParticipation = (String) iterator.next();
//                Participation participation = new Participation(idParticipation);
//                participation.loadByPK();
                Participation participation = (Participation) iterator.next();
                ThirdParty thir = participation.getThirdParty();
                try {

                    //com.consisint.acsele.uaa.ThirdParty tercero = thirPartyHome.findByPrimaryKey(thir.getPK());
                    ThirdParty tercero = ThirdParty.getInstance(thir.getPk());
                    log.debug("participation.getRole().getDesc() = " + participation.getRole().getDynamic().getDCO().getDesc());
                    String roleDesc = participation.getRole().getDynamic().getDCO().getDesc();
                    com.consisint.acsele.uaa.api.Role roleImpl = com.consisint.acsele.uaa.api.Role.Impl.load(roleDesc);
                    SearchThirdPartyResult bean = new SearchThirdPartyResult(thir.getPk().toString(), tercero.getName(),
                            roleImpl);
                    resp.add(bean);
                } catch (Exception ex) {
                    log.error("Error", ex);
                }
            }
        }
        return resp;
    }

    /**
     * @param participaciones
     * @param roles
     * @return Collection
     */
    private Collection checkBeneficiaryRoleInCollection(Enumeration participaciones, RoleList roleList) {
        ArrayList beneficiarys = new ArrayList();
        if (participaciones != null) {
            while (participaciones.hasMoreElements()) {
                Participation part = (Participation) participaciones.nextElement();
                com.consisint.acsele.uaa.api.Role roleImpl =
                        com.consisint.acsele.uaa.api.Role.Impl.load(part.getRole().getDynamic().getDCO().getDesc());
                if (roleList.getAll().contains(roleImpl)) {
                    try {
                        Long thirdPartyPK = null;
                        if (part.getRole() != null) {
                            thirdPartyPK = part.getThirdParty().getPk();
                            log.debug("thirdPartyPK: " + thirdPartyPK);

                            ThirdParty tercero = ThirdParty.getInstance(thirdPartyPK);
                            log.debug("tercero.getName(): " + tercero.getName());

                            SearchThirdPartyResult bean = new SearchThirdPartyResult(thirdPartyPK.toString(), tercero.getName(),
                                    tercero.getStatic().getStatus());
                            beneficiarys.add(bean);
                        }
                    } catch (Exception e) {
                        log.error("Error", e);
                    }
                }
            }
        }
        return beneficiarys;
    }

    /**
     * @param paymentOrderCollection
     * @param reserve
     * @return boolean
     */

    private boolean totalPaymentCurrent(PaymentOrderCollection paymentOrderCollection, ClaimReserve reserve, String doneby) {

        Iterator iter = paymentOrderCollection.getPaymentOrders().iterator();
//        log.debug("Reserve Current " + reserve.getAmount());
        while (iter.hasNext()) {
            PaymentOrder paymentOrder = (PaymentOrder) iter.next();
//            log.debug("[paymentOrder.isTotalPayment()] " + paymentOrder.isTotalPayment());
//            log.debug("[paymentOrder.getPK()] " + paymentOrder.getPK());
            if (paymentOrder.isTotalPayment() && paymentOrder.getPK() == null) {
                double amountToPaid;
                if (reserve instanceof ClaimNormalReserve) {
                    amountToPaid = ((ClaimNormalReserve) reserve).getAmountWithDeductible();
                } else {
                    amountToPaid = reserve.getAmount().doubleValue();
                }
                generateSystemAdjust(reserve, amountToPaid, paymentOrderCollection.getPaidAmount(), doneby);

                return true;
            }
        }
        return false;
    }

    /**
     * @param reserve
     */
    private void totalPaymentOthers(ClaimReserve reserve, String doneby) {
        Iterator orders = reserve.getPaymentOrderList().values().iterator();
//        log.debug("Reserve Others " + reserve.getAmount());
        double paidAmount = 0.0f;
        while (orders.hasNext()) {
            PaymentOrder paymentOrder = (PaymentOrder) orders.next();
            paymentOrder = PaymentOrder.load(paymentOrder.getPk());
            if (paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.CANCELED_STATE &&
                    paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.REJECTED_STATE) {
                paidAmount += paymentOrder.getAmountWithDeductible();
            }

        }
        log.debug("Paided Amount ajust system " + paidAmount);
        double amountToPaid;
        if (reserve instanceof ClaimNormalReserve) {
            amountToPaid = ((ClaimNormalReserve) reserve).getAmountWithDeductible();
        } else {
            amountToPaid = reserve.getAmount();
        }
        generateSystemAdjust(reserve, amountToPaid, paidAmount, doneby);
    }

    /**
     * @param list
     * @param paymentOrders
     * @return double
     */
    private double fillPaymentList(Map list, Collection paymentOrders, ClaimReserve claimReserve, Long claimId) {

        double diffAmount = 0;
        log.debug("paymentOrders.size() = " + paymentOrders.size());

        if (!StringUtil.isEmptyOrNullValue(claimReserve.getPk())) {
            ClaimReservePaymentOrder crpo = ClaimReservePaymentOrder.loadInstance(Long.valueOf(claimReserve.getPk()));

            if (crpo == null) {
                crpo = new ClaimReservePaymentOrder();
                crpo.setReserveId(Long.valueOf(claimReserve.getPk()));
                crpo.setDurationTime(0);
                crpo.setPeriodicity(0);
                crpo.saveInstance();
            }
        }

        for (Object order : paymentOrders) {
            PaymentOrder paymentOrder = (PaymentOrder) order;
            log.debug("paymentOrder.getPK()= " + paymentOrder.getPK());
            if (paymentOrder.getPK() == null) {
//                paymentOrder.generatePK();
                log.debug("[pk paymentOrder] " + paymentOrder.getPK());
//                paymentOrder.setClaimReserve(claimReserve);
//                paymentOrder.setReserveType(reserveType);
//                paymentOrder.setFkReserve(Long.parseLong(fkReserveIdStr));
                paymentOrder.setOnHold(0);
                paymentOrder.save();
                //TODO: This code helps us to recover duration and periodicity values, to be able to change payment order's start date.

                AuditTrailManager manager = AuditTrailManager.getInstance();
                Claim claim;
                if (!asl) {
                    claim = Claim.getInstance(claimId);
                } else {
                    claim = this.claim;
                }
                Context cliamContext = Context.createClaimContext(claimId, claim.getClaimNumber(), Long.valueOf(paymentOrder.getPK()));
                ArrayList<Context> contexts = new ArrayList<Context>();
                contexts.add(cliamContext);
                manager.generateAuditTrail(CustomAuditItem.GENERATE_PAYMENT_ORDERS, contexts);
                list.put(paymentOrder.getPk(), paymentOrder);
                log.debug("(ClaimComposerEJB) insert paymentOrder with pk " + paymentOrder.getPK());
                log.debug("paymentOrder.getAmount():" + paymentOrder.getAmount());
                diffAmount += paymentOrder.getAmount();
            } else {
                log.debug("paymentOrder.getPk():" + paymentOrder.getPk());
                log.debug("paymentOrder.getAmount():" + paymentOrder.getAmount());
//                paymentOrder.update();

                try {
                    HibernateUtil.getActualSession()
                            .evict(HibernateUtil.getActualSession().get(paymentOrder.getClass(), paymentOrder.getPk()));
                    HibernateUtil.getActualSession().update(paymentOrder);
                } catch (HibernateException e) {
                }
                if (paymentOrder.getFkReserve() == Long.valueOf(claimReserve.getPk()))
                    list.put(paymentOrder.getPk(), paymentOrder);

                diffAmount += paymentOrder.getAmountAdjust();
            }
            updatePaymentOrderInvoice(paymentOrder);
            if (paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.CANCELED_STATE) {
                diffAmount -= paymentOrder.getAmountWithDeductible();
            }
        }
        return diffAmount;
    }

    /**
     * Checks if the automatic restitution flag is on, and performs the endorsement in the coverage
     *
     * @param cnr the claim reserve
     */
    private void automaticRestitution(ClaimNormalReserve cnr) {
        if (cnr.isCoverageRestituted()) {
            return;
        }
        ClaimInsuranceObject cio = cnr.getContainer();
        ClaimRiskUnit cru = cio.getContainer();
        AgregatedRiskUnit aru = cru.getAgregatedRiskUnit();
        AgregatedInsuranceObject aio = cio.getAgregatedInsuranceObject();
        EvaluatedCoverage coverage = cnr.getEvaluatedCoverage();
        String autoRestitusionFlag = coverage.getDCO().getCriterioInput(AcseleConf.getProperty("automaticRestitution"));
        if (autoRestitusionFlag == null || "no".equals(autoRestitusionFlag.toLowerCase().trim())) {
            return;
        }
        try {
            PolicySession policySession = PolicySession.Impl.getInstance();
            //CREAR OPERATION READ ONLY � MLB
            //OperationPK opk = OperationPK.createOperation(policy.getPk());
            OperationPK opk = OperationPK.createReadOnlyOperation(policy.getPk());
            //CREAR OPERATION READ ONLY � MLB

            java.sql.Date timeStamp = new java.sql.Date(System.currentTimeMillis());
            opk.setTimeStamp(timeStamp);
            opk = policySession.findByTimeStamp(opk, policy.getPK(), timeStamp);
            policySession.setEventCoverage(opk, aru.getPK(), aio.getDesc(), coverage.getPk(), AcseleConf.getProperty(EventType.ENDORSERMENT_EVENT));

            HashMap inputs = new HashMap(1);
            String systemDate = DateUtil.getDateToShow(timeStamp);
            inputs.put(AcseleConf.getProperty(EventType.ENDORSERMENT_DATE_PROPERTY), systemDate);

            policySession.updateEventCoverageInputs(opk, aru.getPK(), aio.getDesc(), coverage.getPk(), inputs);
//                                                    coverage.getDesc(), inputs);
            policySession.evaluatePolicy();
            //evalua solo la cobertura pues a lo demas no se le applico ningun evento
            policySession.applyOperation();

            cnr.setCoverageRestituted(true);
            cnr.update();
        } catch (Exception e) {
            log.error("Error [automaticRestitution] ", e);
        }
    }

    /**
     * Obtains the ConfigurableObjectType
     *
     * @param categoryType
     * @param templateName
     * @return ConfigurableObjectType
     */
    private ConfigurableObjectType getConfigurableObjectType(CotType categoryType, String templateName) {
        return (ConfigurableObjectType) Categorias.getBean(Categorias.ALL_TEMPLATES_STATE).get(categoryType, templateName);
    }

    /**
     * Utility method that sets the paid amount and currencyID in each payment object contained in
     * the Hashtable parameter. Use this method for ClaimTotalize
     *
     * @param beneficiaryPayments Hashtable containing beneficiaries which payment are extracted
     *                            from.
     * @param currencyID
     */
    private void setPaidAmountToPaymentsInHashtable(Hashtable beneficiaryPayments, int currencyID) {
        Enumeration beneficiariesEnum = beneficiaryPayments.elements();
        while (beneficiariesEnum.hasMoreElements()) {
            Hashtable paymentsByCurrency = (Hashtable) beneficiariesEnum.nextElement();
            Enumeration payments = paymentsByCurrency.elements();
//            log.debug("setInitialAmountToPaymentsInHashtable.payments");
            while (payments.hasMoreElements()) {
                Payment payment = (Payment) payments.nextElement();
                payment.setPaidAmount(payment.getAmount());
                payment.setAmount(new Double(0F));
                payment.setPaidCurrencyId(String.valueOf(currencyID));
                payment.update();
//                log.debug("payment.getPaidAmount():" + payment.getPaidAmount());
//                log.debug("payment.getAmount():" + payment.getAmount());
            }
        }
    }

    /**
     * Utility method that sets the paid amount and currencyID in each payment object contained in
     * the Hashtable parameter. Use this method for ClaimTotalizeOneByOne
     *
     * @param payments   Hashtable containing beneficiaries which payment are extracted
     *                   from.
     * @param currencyID
     */
    private void setPaidAmountToPaymentsInHashtable(Hashtable payments, Currency currency) {
        System.out.println("### ClaimComposerWrapper.setPaidAmountToPaymentsInHashtable");

        System.out.println("payments.size = " + payments.size());
        for (Object ob : payments.values()) {
            Hashtable paymentsByCurrency = (Hashtable) ob;

            System.out.println("paymentsByCurrency.get(String.valueOf(currency.getId())) = " + paymentsByCurrency.get(String.valueOf(currency.getId())));
            if (paymentsByCurrency.get(String.valueOf(currency.getId())) != null) {
                ArrayList paymentsArray = (ArrayList) paymentsByCurrency.get(String.valueOf(currency.getId()));
                System.out.println("paymentsArray = " + paymentsArray.size());

                for (Object obPay : paymentsArray) {
                    Payment payment = (Payment) obPay;
                    System.out.println("### payment!!!! = " + payment);
                    payment.setPaidAmount(payment.getAmount());
                    payment.setAmount(new Double(0F));
                    payment.setPaidCurrencyId(String.valueOf(currency.getId()));
                    payment.update();
//                log.debug("payment.getPaidAmount():" + payment.getPaidAmount());
//                log.debug("payment.getAmount():" + payment.getAmount());
                }
            }
        }
    }

    /**
     * Utility method that sets the paid amount and currencyID in each payment object contained in
     * the Hashtable parameter. Use this method for ClaimTotalizeOneByOne
     *
     * @param payments   Hashtable containing beneficiaries which payment are extracted
     *                   from.
     * @param currencyID
     */
    private void setPaidAmountToPaymentsInVector(Vector payments, Currency currency) {
        Enumeration paymentsEnum = payments.elements();
        while (paymentsEnum.hasMoreElements()) {
            Payment payment = (Payment) paymentsEnum.nextElement();
            payment.setPaidAmount(payment.getAmount());
            payment.setAmount(new Double(0F));
            payment.setPaidCurrencyId(String.valueOf(currency.getId()));
            payment.update();
//                log.debug("payment.getPaidAmount():" + payment.getPaidAmount());
//                log.debug("payment.getAmount():" + payment.getAmount());
        }
    }

    /**
     * Utility method that sets the paid amount and currencyID in each payment object contained in
     * the Hashtable parameter. Use this method for ClaimTotalizeOneByOne
     *
     * @param payments   Hashtable containing beneficiaries which payment are extracted
     *                   from.
     * @param currencyID
     */
    private void setPaidAmountToPaymentsInHassTable(Vector payments, Currency currency) {
        Enumeration paymentsEnum = payments.elements();
        while (paymentsEnum.hasMoreElements()) {
            Payment payment = (Payment) paymentsEnum.nextElement();
            payment.setPaidAmount(payment.getAmount());
            payment.setAmount(new Double(0F));
            payment.setPaidCurrencyId(String.valueOf(currency.getId()));
            payment.update();
//                log.debug("payment.getPaidAmount():" + payment.getPaidAmount());
//                log.debug("payment.getAmount():" + payment.getAmount());
        }
    }

    /**
     * @param cr
     * @param amountToPaid
     * @param amountPaided
     */
    private void generateSystemAdjust(ClaimReserve cr, double amountToPaid, double amountPaided, String doneby) {

        ReserveAdjustType adjustType = (amountToPaid > amountPaided) ? ReserveAdjustType.DECREASE : ReserveAdjustType.INCREASE;
        double sysAmount = amountToPaid - amountPaided;

        if (cr instanceof ClaimNormalReserve) {
            try {
                sysAmount = (((ClaimNormalReserve) cr).getAmountWithDeductible() - amountPaided);
            } catch (Exception e) {
                log.error("[ERROR] ClaimComposerEJB.generateSystemAdjust -> dividiendo " + (amountToPaid - amountPaided)
                        + " entre el % de reembolso: " + ((ClaimNormalReserve) cr).getRefundPercentage(), e);
            }
        }

        ClaimReserveAdjust adjust = new ClaimReserveAdjust(cr.getDesc(), new java.util.Date(), sysAmount, ClaimReserveAdjust.SYSTEM,
                ClaimReserveAdjust.systemAdjustReasonKey, adjustType.getValue());
        adjust.setClaimReserveId(cr.getPK());
        adjust.setDoneBy(doneby);
        adjust.save();
        ClaimNormalReserve cnr = (ClaimNormalReserve) cr;
        EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
        if (cr instanceof ClaimNormalReserve) {
            ClaimValidationService claimValidationService = ((ClaimValidationService) BeanFactory.getBean(ClaimValidationService.class));
            claimValidationService.claimreOpening(claim.getId(), ec);
        }
        cr.getAdjustList().put(adjust.getPk(), adjust);

        ClaimHistorical claimHistorical = new ClaimHistorical();
        claimHistorical.generateHistoricalWithMovement(this.claim, ClaimHistoricalOperationType.ADD_RESERVE, ClaimHistoricalMovementType.CHANGE_RESERVE, Long.parseLong(adjust.getPK()), ClaimUtil.getValidatedLegacyType(cnr));
        generateReserveReinsuranceDistribution(cr, String.valueOf(sysAmount), Constants.RESERVE_CLAIM, new java.sql.Date(cr.getDate().getTime()), adjustType);
    }

    /**
     * @param paymentOrder
     */
    private void updatePaymentOrderInvoice(PaymentOrder paymentOrder) {

        //Ahora, a salvar los invoices
        ClaimInvoice invoice = null;
        Map newInvoices = new HashMap(paymentOrder.getInvoices());
        Iterator invoicesEnum = newInvoices.values().iterator();

        //Added by (MC) 2005-03-29: No entiendo bien porque esto se pone pero lo voy a probar...
        paymentOrder.getInvoices().clear();

        while (invoicesEnum.hasNext()) {
            invoice = (ClaimInvoice) invoicesEnum.next();

            Map<Long, ClaimInvoiceDetail> details = new HashMap(invoice.getInvoiceDetails());
            invoice.getInvoiceDetails().clear();

            if (invoice.getPk() < 0) {
                invoice.generatePk();
                invoice.setPaymentOrderId(paymentOrder.getPk());
                invoice.setParentPaymentOrder(paymentOrder);
                log.debug("PaymentOrder: Saving invoice object....");
                log.debug("PaymentOrder: invoice.getPk():" + invoice.getPk());

                invoice.save();
            } else {
                invoice.update();
            }
            updateInvoiceDetails(invoice, details);
            paymentOrder.getInvoices().put(invoice.getPk(), invoice);
        }

    }

    /**
     * @param invoice
     */
    private void updateInvoiceDetails(ClaimInvoice invoice, Map newInvoiceDetails) {
        //Ahora, a salvar los invoices
        Iterator invoicesDetailEnum = newInvoiceDetails.values().iterator();

        while (invoicesDetailEnum.hasNext()) {
            ClaimInvoiceDetail invoiceDetail = (ClaimInvoiceDetail) invoicesDetailEnum.next();
            if (invoiceDetail.getPK() == null) {
                invoiceDetail.generatePk();

                log.debug("ClaimComposerEJB.updateInvoiceDetails: saving invoice detail with pk=" + invoiceDetail.getPk());

                invoiceDetail.setParentInvoice(invoice);
                invoiceDetail.setClaimInvoiceId(invoice.getPk());
                invoiceDetail.save();
            } else {
                invoiceDetail.update();
            }
            invoice.getInvoiceDetails().put(invoiceDetail.getPk(), invoiceDetail);
        }
    }

    /**
     * @return the descriptions
     */
    private Collection getClaimsInsuranceObjectDesc() {
        ArrayList elementos = new ArrayList();
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit cru : claimRiskUnits) {
            Iterator affectedObjects = cru.getClaimInsuranceObjects().values().iterator();
            while (affectedObjects.hasNext()) {
                ClaimInsuranceObject iobject = (ClaimInsuranceObject) affectedObjects.next();
//                log.debug("iobject.getDesc() = " + iobject.getDesc());
                elementos.add(iobject.getDesc());
            }
        }
        return elementos;
    }

    /**
     * @param reserve
     * @param payments
     * @param cru
     * @param doneby
     * @return total
     * @throws ApplicationExceptionChecked
     * @deprecated because is terrible!
     */
    private double accountReservePaymentsOrders(ClaimReserve reserve, Hashtable payments, ClaimRiskUnit cru,
                                                String doneby) throws ApplicationExceptionChecked {

        double total = 0.0f;
        double sum;
        double mto;
        String key;
        String beneficiary;
        long thirdPartyId;
        Hashtable paymentsByCurrency;
        List currencyPayments;
        Payment payment = null;
        PaymentOrder paymentOrder;
        reserve.load();
        int reserveType = (reserve instanceof ClaimNormalReserve) ? ReserveType.NORMAL_RESERVE.getValue() : ReserveType.CONCEPT_RESERVE.getValue();
        String coverageDescription = reserve.getDesc();
        Iterator paymentsOrdersEnum = reserve.getPaymentOrderList().values().iterator();

        while (paymentsOrdersEnum.hasNext()) {
            paymentOrder = (PaymentOrder) paymentsOrdersEnum.next();
            log.debug("[accountReservePaymentsOrders] paymentOrder.getState() " + paymentOrder.getPaymentOrderStatus() + " - " + paymentOrder.getPK());
            if (paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.APPROVED_STATE) {
                continue;
            }
            beneficiary = paymentOrder.getBeneficiaryOpc().getName();
            thirdPartyId = paymentOrder.getThirdPartyId();
            //This key is used to group by coverage or beneficiary
            key = String.valueOf(thirdPartyId);
            log.debug("[accountReservePaymentsOrders] beneficiary " + beneficiary);
            log.debug("[accountReservePaymentsOrders] thirdPartyId " + key);
            log.debug("[accountReservePaymentsOrders] paymentOrder.getPk() " + paymentOrder.getPk());
            log.debug("[accountReservePaymentsOrders] paymentOrder.getAmount().doubleValue() " + paymentOrder.getAmount().doubleValue());
            log.debug("[accountReservePaymentsOrders] paymentOrder.getRefundPercentage() " + paymentOrder.getRefundPercentage());
            log.debug("[accountReservePaymentsOrders] paymentOrder.getRefundPercentage() " + paymentOrder.getRefundPercentage());
            log.debug("[accountReservePaymentsOrders] paymentOrder.getDeductibleAmount() " + paymentOrder.getDeductibleAmount());
            log.debug("[accountReservePaymentsOrders] paymentOrder.getDistributionAmount() " + paymentOrder.getDistributionAmount());


            mto = (paymentOrder.getAmount().doubleValue() * (paymentOrder.getRefundPercentage() == 0.0f ? 1.0f : paymentOrder.getRefundPercentage()))
                    - paymentOrder.getDeductibleAmount() - paymentOrder.getAmountFranchise() + paymentOrder.getDistributionAmount();

            log.debug("[accountReservePaymentsOrders] mto " + mto);
            log.debug("[payments] " + payments);
            log.debug("[payments.containsKey(key)]: " + payments.containsKey(key));
            if (payments != null && payments.containsKey(key)) {
                paymentsByCurrency = (Hashtable) payments.get(key);

                log.debug("[accountReservePaymentsOrders] " + "reserve.getCurrency().getCurrencyID() =  " + reserve.getCurrency().getId());

                String value = String.valueOf(reserve.getCurrency().getId());
                if (paymentsByCurrency.containsKey(value)) {
                    log.debug("paymentsByCurrency:" + paymentsByCurrency);
                    log.debug("paymentsByCurrency.get(value).getClass()  = " + paymentsByCurrency.get(value).getClass());
                    ArrayList paymentCurrency = (ArrayList) paymentsByCurrency.get(value);
                    Iterator paymentsCurrencyList = paymentCurrency.iterator();

                    while (paymentsCurrencyList.hasNext()) {
                        payment = (Payment) paymentsCurrencyList.next();
                        payment = Payment.load(payment.getPk());

                        boolean isNew = !payment.getPaymentOrder().equals(paymentOrder);

                        log.debug("ClaimEntityEJB.accountReservePaymentsOrders -> isNew " + isNew);

                        if (isNew) {
                            sum = payment.getAmount().doubleValue() + mto;
                            payment.setAmount(new Double(sum));
                            payment.update();
                            paymentOrder.setPayment(payment);
                            paymentOrder.update();
                        }
                        log.debug("ClaimComposerEJB.accountReservePaymentsOrders: " + "associating payment " + payment.getPk()
                                + " with ClaimRiskUnit " + cru.getPk());
                        ClaimRiskUnitPayment claimRiskUnitPayment = new ClaimRiskUnitPayment(cru, payment);
                        claimRiskUnitPayment.save();

                        log.debug("[DEBUG - ACCOUNT PAYMENTS] Payment Pendiente Updated : " + payment.getPK() + " beneficiario: "
                                + payment.getThirdParty().getName() + " amount a pagar : " + payment.getAmount() + " currency: "
                                + payment.getReserveCurrencyId() + " amount pagada: " + payment.getPaidAmount() + " en la moneda:"
                                + payment.getPaidCurrencyId() + " tasa: " + payment.getExchangeRate());
                    }

                } else {
                    payment = new Payment(paymentOrder);
                    payment.setDate(new Date());
                    payment.setAmount(new Double(mto));
                    payment.setReserveType(reserveType);
                    payment.setPaidAmount(new Double(0F));
                    payment.setCoverageDescription(coverageDescription);
                    payment.setReserveCurrencyId(String.valueOf(reserve.getCurrency().getId()));
                    payment.setPaidCurrencyId(String.valueOf(reserve.getCurrency().getId()));
                    payment.setClaimId(this.claim.getPK());
                    payment.save();
                    log.debug(
                            "ClaimComposerEJB.accountReservePaymentsOrders: associating payment " + payment.getPk() + " with ClaimRiskUnit " + cru
                                    .getPk());
                    ClaimRiskUnitPayment claimRiskUnitPayment = new ClaimRiskUnitPayment(cru, payment);
                    claimRiskUnitPayment.save();

                    log.debug("ClaimComposerEJB.accountReservePaymentsOrders: " + "associating payment " + payment.getPk() + " with ClaimRiskUnit "
                            + cru.getPk());
                    log.debug("[DEBUG - ACCOUNT PAYMENTS] Payment Nuevo!!! (estaba el tercero) : " + payment.getPK() + " beneficiario: "
                            + payment.getThirdParty().getName() + " amount a pagar : " + payment.getAmount() + " currency: " + payment.getReserveCurrencyId()
                            + " amount pagada: " + payment.getPaidAmount() + " en la moneda:" + payment.getPaidCurrencyId() + " tasa: " + payment
                            .getExchangeRate());

                    currencyPayments = (ArrayList) paymentsByCurrency.get(payment.getReserveCurrencyId());
                    if (currencyPayments == null) {
                        currencyPayments = new ArrayList();
                    }
                    currencyPayments.add(payment);
                    paymentsByCurrency.put(String.valueOf(reserve.getCurrency().getId()), currencyPayments);

                }
            } else {
                log.debug(" ... Entrando a Bloque: 1 ");
                if (payments != null) {
                    log.debug("ELSE.....");
                    paymentsByCurrency = new Hashtable();
                    payment = new Payment(paymentOrder);
                    payment.setDate(new Date());
                    payment.setAmount(new Double(mto));
                    payment.setReserveType(reserveType);
                    payment.setPaidAmount(new Double(0F));
                    payment.setCoverageDescription(coverageDescription);
                    log.debug("reserve.getCurrency().getCurrencyID() " + reserve.getCurrency().getId());
                    payment.setReserveCurrencyId(String.valueOf(reserve.getCurrency().getId()));
                    payment.setPaidCurrencyId(String.valueOf(reserve.getCurrency().getId()));
                    payment.setClaimId(this.claim.getPK());
                    payment.save();
                    log.debug("ClaimComposerEJB.accountReservePaymentsOrders: " + "associating payment " + payment.getPk()
                            + " with ClaimRiskUnit " + cru.getPk());
                    ClaimRiskUnitPayment claimRiskUnitPayment = new ClaimRiskUnitPayment(cru, payment);
                    claimRiskUnitPayment.save();

                    log.debug("ClaimComposerEJB.accountReservePaymentsOrders: associating payment " + payment.getPk() + " with ClaimRiskUnit " + cru
                            .getPk());
                    log.debug("[DEBUG - ACCOUNT PAYMENTS] Payment Nuevo!!! (NO estaba el tercero)  : " + payment.getPK() + " beneficiario: "
                            + payment.getThirdParty().getName() + " amount a pagar : " + payment.getAmount() + " currency: " + payment.getReserveCurrencyId()
                            + " amount pagada: " + payment.getPaidAmount() + " en la moneda:" + payment.getPaidCurrencyId() + " tasa: " + payment
                            .getExchangeRate());

                    currencyPayments = (ArrayList) paymentsByCurrency.get(payment.getReserveCurrencyId());

                    if (currencyPayments == null) {
                        currencyPayments = new ArrayList();
                    }

//                    paymentsByCurrency
//                            .put(String.valueOf(reserve.getCurrency().getCurrencyID()), payment);

                    currencyPayments.add(payment);
                    paymentsByCurrency.put(String.valueOf(reserve.getCurrency().getId()), currencyPayments);

                    log.debug("[key] " + key);
                    payments.put(key, paymentsByCurrency);
                }
            }
            total += mto;

        } // Payments

        return total;
    }  // InsuranceObjects


    /**
     * Accounts the amount of the approved payments one by one.
     *
     * @param reserve
     * @param payments
     * @param cru
     * @param doneby
     * @return total
     * @throws ApplicationExceptionChecked
     */
    private double accountReservePaymentsOrdersOneByOne(ClaimReserve reserve, Vector payments, ClaimRiskUnit cru, String doneby,
                                                        com.consisint.acsele.product.api.ClaimsCoverageConfiguration  cconf, ClaimTotalizeOneByOne claimTotalize) throws ApplicationExceptionChecked {
        log.info(" *** METHOD NAME  " + "accountReservePaymentsOrdersOneByOne");

        double total = 0.0f;
        double mto;
        String beneficiary;
        long thirdPartyId;
        Payment payment;
        reserve.load();
        int reserveType = (reserve instanceof ClaimNormalReserve) ? ReserveType.NORMAL_RESERVE.getValue() : ReserveType.CONCEPT_RESERVE.getValue();
        String coverageDescription = reserve.getDesc();
        List<PaymentOrder> listPayments = PaymentOrder.getPaymentOrderApprovedByClaimNormalReserveId(reserve.getPK());//22378052
        for (PaymentOrder paymentOrder : listPayments) {
            log.debug("[accountReservePaymentsOrders] paymentOrder.getPk() " + paymentOrder.getPk());
            log.debug("[accountReservePaymentsOrders] paymentOrder.getPaymentOrderStatus() " + paymentOrder.getPaymentOrderStatus());
            if (paymentOrder.getPayment() != null ||
                    paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.APPROVED_STATE) {
                continue;
            }
            beneficiary = paymentOrder.getBeneficiaryOpc().getName();
            thirdPartyId = paymentOrder.getThirdPartyId();
            log.debug("[accountReservePaymentsOrders] beneficiary " + beneficiary);
            log.debug("[accountReservePaymentsOrders] paymentOrder.getAmount().doubleValue() " + paymentOrder.getAmount().doubleValue());
            log.debug("[accountReservePaymentsOrders] paymentOrder.getRefundPercentage() " + paymentOrder.getRefundPercentage());
            log.debug("[accountReservePaymentsOrders] paymentOrder.getRefundPercentage() " + paymentOrder.getRefundPercentage());
            log.debug("[accountReservePaymentsOrders] paymentOrder.getDeductibleAmount() " + paymentOrder.getDeductibleAmount());
            log.debug("[accountReservePaymentsOrders] paymentOrder.getDistributionAmount() " + paymentOrder.getDistributionAmount());

            mto = (paymentOrder.getAmount().doubleValue() * (paymentOrder.getRefundPercentage() == 0.0f ? 1.0f : paymentOrder.getRefundPercentage())) + paymentOrder.getDistributionAmount() - paymentOrder.getAmountFranchise();

            log.debug("[accountReservePaymentsOrders] mto " + mto);
            log.debug("[payments] " + payments);

            payment = new Payment(paymentOrder);
            payment.setDate(new Date());
            payment.setAmount(mto);
            payment.setReserveType(reserveType);
            payment.setPaidAmount(new Double(0F));
            payment.setCoverageDescription(coverageDescription);
            payment.setReserveCurrencyId(String.valueOf(reserve.getCurrency().getId()));
            payment.setPaidCurrencyId(String.valueOf(reserve.getCurrency().getId()));
            payment.setClaimId(this.claim.getPK());
            payment.save();
            log.debug("ClaimComposerEJB.accountReservePaymentsOrders: " + "associating payment " + payment.getPk() + " with ClaimRiskUnit " + cru
                    .getPk());
            ClaimRiskUnitPayment claimRiskUnitPayment = new ClaimRiskUnitPayment(cru, payment);
            claimRiskUnitPayment.save();

            log.debug("[CCW][payment] '" + payment.getPK() + "' For [Cliam] '" + payment.getClaimId() + "' MONTO: '" + payment.getAmount()
                    + "'  Fecha Commitment: " + payment.getCommitmentDate() + "  Fecha Payment: " + payment.getDate());

            Calendar commitCal = Calendar.getInstance();
            commitCal.setTime(payment.getCommitmentDate());

            Calendar currentCal = Calendar.getInstance();
            currentCal.set(Calendar.HOUR, 0);
            currentCal.set(Calendar.MINUTE, 0);
            currentCal.set(Calendar.SECOND, 0);
            currentCal.set(Calendar.MILLISECOND, 0);

            log.debug("[accountReservePaymentsOrdersOneByOne] claim.CovCof Allow Adv. " + cconf.isAllowAdvancePayments() + " --- Current Date: "
                    + currentCal.getTime() + " --- Payment Date: " + commitCal.getTime());

            if ((commitCal.getTimeInMillis() <= currentCal.getTimeInMillis()) || (cconf.isAllowAdvancePayments())) {

                log.debug("ClaimComposerEJB.accountReservePaymentsOrders: associating payment " + payment.getPk() + " with ClaimRiskUnit " + cru
                        .getPk());
                log.debug("[DEBUG - ACCOUNT PAYMENTS] Payment Nuevo!!! (NO estaba el tercero)  : " + payment.getPK() + " beneficiario: "
                        + payment.getThirdParty().getName() + " amount a pagar : " + payment.getAmount() + " currency: " + payment.getReserveCurrencyId()
                        + " amount pagada: " + payment.getPaidAmount() + " en la moneda:" + payment.getPaidCurrencyId() + " tasa: "
                        + payment.getExchangeRate() + " ");

                if (payments == null) {
                    payments = new Vector();
                    if(reserveType == ReserveType.NORMAL_RESERVE.getValue()) {
                        payments.add(payment);
                        claimTotalize.setNormalReservePendingPayments(payments);
                        }else{
                        payments.add(payment);
                        claimTotalize.setConceptReservePendingPayments(payments);
                    }
                } else {
                    if (payments.contains(payment) ) {
                        claimTotalize.setNormalReservePendingPayments(payments);
                    } else {
                        if(reserveType == ReserveType.NORMAL_RESERVE.getValue()){
                            payments.add(payment);
                            claimTotalize.setNormalReservePendingPayments(payments);
                        }else{
                            payments.add(payment);
                            claimTotalize.setConceptReservePendingPayments(payments);
                        }


                    }
                }
            }
            total += mto;
        } // PaymentsOrder enumeration
        return total;
    }

    /**
     * @param currencyToExchange
     * @param currenciesList
     * @param currenciesExchangeRates
     */

    private void loadCurrencyExchangeRates(Currency currencyToExchange, Map<String, Currency> currenciesList,
                                           Map<String, Map<String, String>> currenciesExchangeRates) {
        if (!currenciesExchangeRates.containsKey(String.valueOf(currencyToExchange.getId()))) {
            Map<String, String> currencyExchangeRates = new HashMap<String, String>();
            Set<String> keys = currenciesList.keySet();
            for (String key : keys) {
                Currency currency = currenciesList.get(key);
                if (currency.equals(currencyToExchange)) {
                    currencyExchangeRates.put(String.valueOf(currencyToExchange.getId()), "1");
                } else {
                    double rate = calculateExchangeRate(currencyToExchange, currency, new Date());
                    currencyExchangeRates.put(String.valueOf(currency.getId()), String.valueOf(rate));
                }
            }
            currenciesExchangeRates.put(String.valueOf(currencyToExchange.getId()), currencyExchangeRates);
        }
    }

    /**
     * @param currencyFrom
     * @param currencyTo
     * @param date
     * @return double
     */
    private double calculateExchangeRate(Currency currencyFrom, Currency currencyTo, Date date) {
        if (currencyFrom.equals(currencyTo)) {
            return 1;
        }
        try {
            String methodToCalculaRate = AcseleConf.getProperty("methodToCalculaRate");
            CurrencyRate currencyRate = null;
            if (Integer.parseInt(methodToCalculaRate) == 1) {
                currencyRate = new CurrencyRate(currencyFrom, currencyTo, date);
            } else {
                currencyRate = new CurrencyRate(currencyFrom, currencyTo, claim.getClaimDate());
            }
            currencyRate.load();
            return currencyRate.getRate();
        } catch (Exception e) {
            log.error("Error loading rate: FROM = '" + currencyFrom + "', TO = '" + currencyTo + "', DATE = '" + DateUtil.getFormatLongShow().format(date)
                    + "'.", e);
//            return 0;
            throw new ApplicationException(Exceptions.CCErrorUnknownExchangeRate, Severity.ERROR,
                    " '" + currencyFrom + "'->'" + currencyTo + "' (" + DateUtil.getFormatLongShow().format(date) + ")", e);
        }
    }

    /**
     * Loads the payments in a map
     *
     * @param paymentsList
     */
    private void loadPaymentInMapGroupByBeneficiary(int reserveType, Collection<Payment> paymentsList, ClaimTotalize claimTotalize) throws ParseException {

        Hashtable pendings = new Hashtable();
        Hashtable cancelled = new Hashtable();
        Vector<Payment> paid = new Vector<Payment>();
        String paymentThirdPartyIdStr;
        //a String representation of the thirdpartyid field.
        log.debug("paymentList = " + paymentsList);
        java.sql.Date currentDt = new java.sql.Date(System.currentTimeMillis());
//        long productPk = Long.parseLong(policy.getProduct().getPk());

        if (paymentsList != null && !paymentsList.isEmpty()) {
            for (Payment payment : paymentsList) {
                paymentThirdPartyIdStr = payment.getThirdParty().getPK();
                if (payment.getPaymentStatus() == PaymentStatus.PAID_STATE) {
                    paid.add(payment);

                    log.debug("[DEBUG - ACCOUNT PAYMENTS] Payment Pagado : " + payment.getPK() + " beneficiario: " + payment.getThirdParty().getName()
                            + " amount a pagar : " + payment.getAmount() + " currency: " + payment.getReserveCurrencyId() + " amount pagada: "
                            + payment.getPaidAmount() + " en la moneda:" + payment.getPaidCurrencyId() + " tasa: " + payment.getExchangeRate());

                } else if (payment.getPaymentStatus() == PaymentStatus.CANCELED_STATE) {
                    if (cancelled.containsKey(paymentThirdPartyIdStr)) {
                        Hashtable paymentsByCurrency = (Hashtable) cancelled.get(paymentThirdPartyIdStr);

                        //Taking the currency list for this Payment.
                        List currencyPayments = (ArrayList) paymentsByCurrency.get(payment.getReserveCurrencyId());
                        if (currencyPayments == null) {
                            currencyPayments = new ArrayList();
                        }
                        currencyPayments.add(payment);
                        paymentsByCurrency.put(payment.getReserveCurrencyId(), currencyPayments);
                        cancelled.put(paymentThirdPartyIdStr, paymentsByCurrency);
                    } else {
                        Hashtable paymentsByCurrency = new Hashtable();
                        List currencyPayments = new ArrayList();
                        currencyPayments.add(payment);
                        paymentsByCurrency.put(payment.getReserveCurrencyId(), currencyPayments);
                        cancelled.put(paymentThirdPartyIdStr, paymentsByCurrency);
                    }

                    log.debug("[DEBUG - ACCOUNT PAYMENTS] Payment Cancelado : " + payment.getPK() + " beneficiario: " + payment.getThirdParty().getName()
                            + " amount a pagar : " + payment.getAmount() + " currency: " + payment.getReserveCurrencyId() + " amount pagada: "
                            + payment.getPaidAmount() + " en la moneda:" + payment.getPaidCurrencyId() + " tasa: " + payment.getExchangeRate());

                } else {
                    if (pendings.containsKey(paymentThirdPartyIdStr)) {
                        Hashtable paymentsByCurrency = (Hashtable) pendings.get(paymentThirdPartyIdStr);

                        //Taking the currency list for this Payment.
                        List currencyPayments = (ArrayList) paymentsByCurrency.get(payment.getReserveCurrencyId());
                        if (currencyPayments == null) {
                            currencyPayments = new ArrayList();
                        }

//                        long coveragePk = Long.parseLong(ec.getConfiguratedCoverage().getPK());
//                        ClaimsCoverageConfiguration ccconf =
//                                ClaimsCoverageConfiguration.load(productPk, coveragePk);

//                       if ((!ccconf.isAllowAdvancePayments()) ||
//                        ("-1".equals(new Integer(payment.getDate().compareTo(DateUtil.getBareDate(currentDt))).toString()))){

                        pendings.put(paymentThirdPartyIdStr, paymentsByCurrency);
                        currencyPayments.add(payment);
                        paymentsByCurrency.put(payment.getReserveCurrencyId(), currencyPayments);
//                         }
                    } else {
                        Hashtable paymentsByCurrency = new Hashtable();
                        List currencyPayments = new ArrayList();
//
//                        if ((!ccconf.isAllowAdvancePayments()) ||
//                        ("-1".equals(new Integer(payment.getDate().compareTo(DateUtil.getBareDate(currentDt))).toString()))){

                        pendings.put(paymentThirdPartyIdStr, paymentsByCurrency);
                        currencyPayments.add(payment);
                        paymentsByCurrency.put(payment.getReserveCurrencyId(), currencyPayments);
//                        }
                    }

                    log.debug("[DEBUG - ACCOUNT PAYMENTS] Payment Pendiente : " + payment.getPK() + " beneficiario: " + payment.getThirdParty().getName()
                            + " amount a pagar : " + payment.getAmount() + " currency: " + payment.getReserveCurrencyId() + " amount pagada: "
                            + payment.getPaidAmount() + " en la moneda:" + payment.getPaidCurrencyId() + " tasa: " + payment.getExchangeRate());
                }
            }

            if (reserveType == ReserveType.NORMAL_RESERVE.getValue()) {
                claimTotalize.setNormalReservePaidPayments(paid);
                claimTotalize.setNormalReservePendingPayments(pendings);
                claimTotalize.setNormalReserveCancelledPayments(cancelled);
            } else if (reserveType == ReserveType.CONCEPT_RESERVE.getValue()) {
                claimTotalize.setConceptReservePaidPayments(paid);
                claimTotalize.setConceptReservePendingPayments(pendings);
                claimTotalize.setConceptReserveCancelledPayments(cancelled);
            }
        }
    }

    /**
     * Sets the payments in the claim totalizer.
     *
     * @param reserveType
     * @param paymentsList
     * @param claimTotalize
     * @param cconf
     */
    private void setPaymentsInClaimTotalizer(int reserveType, Collection<Payment> paymentsList, ClaimTotalizeOneByOne claimTotalize,
                                             com.consisint.acsele.product.api.ClaimsCoverageConfiguration cconf) {

        Vector pendings = new Vector();
        Vector cancelled = new Vector();
        Vector<Payment> paid = new Vector<Payment>();

        //a String representation of the thirdpartyid field.
        if (paymentsList != null && !paymentsList.isEmpty()) {
            for (Payment payment : paymentsList) {
                try {
                    if (payment.getPaymentStatus() == PaymentStatus.PAID_STATE) {
                        paid.add(payment);

                        log.debug("[DEBUG - ACCOUNT PAYMENTS] Payment Pagado : " + payment.getPK() + " beneficiario: " + payment.getThirdParty().getName()
                                + " amount a pagar : " + payment.getAmount() + " currency: " + payment.getReserveCurrencyId() + " amount pagada: "
                                + payment.getPaidAmount() + " en la moneda:" + payment.getPaidCurrencyId() + " tasa: " + payment.getExchangeRate()
                                + " Fecha: " + payment.getDate());

                    } else if (payment.getPaymentStatus() == PaymentStatus.CANCELED_STATE) {
                        cancelled.add(payment);

                        log.debug("[DEBUG - ACCOUNT PAYMENTS] Payment Cancelado : " + payment.getPK() + " beneficiario: " + payment.getThirdParty().getName()
                                + " amount a pagar : " + payment.getAmount() + " currency: " + payment.getReserveCurrencyId() + " amount pagada: "
                                + payment.getPaidAmount() + " en la moneda:" + payment.getPaidCurrencyId() + " tasa: " + payment.getExchangeRate()
                                + " Fecha: " + payment.getDate());

                    } else {

                        log.debug("[CCW][payment] '" + payment.getPK() + "' For [Cliam] '" + payment.getClaimId() + "' MONTO: '" + payment.getAmount()
                                + "'  Fecha Commitment: " + payment.getCommitmentDate() + "  Fecha Payment: " + payment.getDate());

                        Calendar commitCal = Calendar.getInstance();
                        commitCal.setTime(payment.getCommitmentDate());

                        Calendar currentCal = Calendar.getInstance();
                        currentCal.add(Calendar.DATE, 1);
                        currentCal.set(Calendar.HOUR, 0);
                        currentCal.set(Calendar.MINUTE, 0);
                        currentCal.set(Calendar.SECOND, 0);
                        currentCal.set(Calendar.MILLISECOND, 0);

                        log.debug("[claim.CovCof Allow Adv.] " + cconf.isAllowAdvancePayments() + " And Current Date: " + currentCal.getTime()
                                + " And Payment Date: " + commitCal.getTime());

                        if ((commitCal.getTimeInMillis() < currentCal.getTimeInMillis()) || cconf.isAllowAdvancePayments()) {
                            pendings.add(payment);
                            log.debug("[DEBUG - ACCOUNT PAYMENTS] Payment Pendiente : " + payment.getPK() + " beneficiario: " + payment.getThirdParty().getName()
                                    + " amount a pagar : " + payment.getAmount() + " currency: " + payment.getReserveCurrencyId() + " amount pagada: "
                                    + payment.getPaidAmount() + " en la moneda:" + payment.getPaidCurrencyId() + " tasa: " + payment.getExchangeRate()
                                    + " Fecha: " + payment.getDate());
                        }
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }

            if (reserveType == ReserveType.NORMAL_RESERVE.getValue()) {
                claimTotalize.setNormalReservePaidPayments(paid);
                claimTotalize.setNormalReservePendingPayments(pendings);
                claimTotalize.setNormalReserveCancelledPayments(cancelled);
            } else if (reserveType == ReserveType.CONCEPT_RESERVE.getValue()) {
                claimTotalize.setConceptReservePaidPayments(paid);
                claimTotalize.setConceptReservePendingPayments(pendings);
                claimTotalize.setConceptReserveCancelledPayments(cancelled);
            }
        }
    }

    private boolean applyPaymentsWithType(Vector payments, int paymentsProxyVectorSize, String ruID, String ioID, Long currencyPayment, String amountPayment, Date paymentDate) throws ApplicationExceptionChecked {
        log.debug("applyPaymentsWithType(....) - Entrando");
        log.debug("paymentsProxyVectorSize = " + paymentsProxyVectorSize);
        log.debug("ruID = " + ruID);
        log.debug("ioID = " + ioID);
        log.debug("currencyPayment = " + currencyPayment);
        log.debug("amountPayment= " + amountPayment);
        log.debug("paymentDate=" + paymentDate);
        int paymentsApplied = 0;
        Enumeration paymentsEnum = payments.elements();
        boolean isFinalPayment = false;
        int i = 0;//to search each amount Payment
        List<String> amountP = StringUtil.splitAsList(amountPayment, ",");
        while (paymentsEnum.hasMoreElements()) {
            Payment payment = (Payment) paymentsEnum.nextElement();
            applyPaymentOneByOne(policy, claim, payment, ruID, ioID, currencyPayment, Double.parseDouble(amountP.get(i)) /*amountPayment*/, paymentDate);
            paymentsProxyVectorSize++;
            paymentsApplied++;
            i++;
            PaymentOrder paymentOrder = payment.getPaymentOrder();
            if (paymentOrder.getPaymentOrderStatus().equals(PaymentOrderStatus.FINAL_PAY)) {
                isFinalPayment = true;
            }
            AuditTrailManager manager = AuditTrailManager.getInstance();
            Context claimContext = Context.createClaimContext(claimId, claim.getClaimNumber(), Long.valueOf(paymentOrder.getPK()));
            ArrayList<Context> contexts = new ArrayList<Context>();
            contexts.add(claimContext);
            manager.generateAuditTrail(CustomAuditItem.FINAL_CLAIM_PAYMENT_APPROVAL, contexts);

        }

        String productPk = policy.getProduct().getPk();
        boolean existCoveragesPending = false;
        boolean isAutomaticClosing = false;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();

        for (ClaimRiskUnit cru : claimRiskUnits) {
            Collection claimInsuranceObjects = cru.getClaimInsuranceObjects().values();
            for (Iterator cioIt = claimInsuranceObjects.iterator(); cioIt.hasNext(); ) {
                ClaimInsuranceObject cio = (ClaimInsuranceObject) cioIt.next();
                Collection claimNormalReserves = cio.getNormalReserves().values();
                for (Iterator cnrIt = claimNormalReserves.iterator(); cnrIt.hasNext(); ) {
                    ClaimNormalReserve cnreserves = (ClaimNormalReserve) cnrIt.next();
                    Collection paymentOrders = cnreserves.getPaymentOrderList().values();
                    PaymentOrderCollection paymentOrderCollection = new PaymentOrderCollection(cnreserves, paymentOrders);
                    // Se libera la reserva para la cobertura en el cual se est� pagando su Final Payment Order
                    paymentsEnum = payments.elements();
                    log.debug("********************************************** payments.size(): " + payments.size());
                    while (paymentsEnum.hasMoreElements()) {
                        Payment payment = (Payment) paymentsEnum.nextElement();
                        log.debug("******************************************  payment.getPk(): " + payment.getPk());
                        log.debug("******************************************  payment.getAmount(): " + payment.getAmount());
                        log.debug("******************************************");
                        PaymentOrder paymentOrder = payment.getPaymentOrder();

                        if (paymentOrder.isFinalPayment()) {
                            isFinalPayment = true;
                            log.debug("SE LIBERA LA RESERVA");
                            freeReserves(ClaimReserveAdjust.CLAIM_FINAL_PAYMENT);
                            break;
                        }
                    }
                    EvaluatedCoverage ev = cnreserves.getEvaluatedCoverage();
                    long configuratedCoverageId = ev.getConfiguratedCoverageOA().getId();
                    ClaimsCoverageConfiguration claimConf = ClaimsCoverageConfiguration
                            .load(Long.parseLong(productPk), configuratedCoverageId);
                    //Si hay al menos una cobertura con Automatic Closing Checked
                    if (claimConf.isAutomaticClosing()) {
                        isAutomaticClosing = true;
                    }
                }
            }
        }

        //Close the claim automatically only if the option is active in the claim coverage configuration and the
        //payment order is final
        ResourceBundle messages = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
        if (isAutomaticClosing && isFinalPayment) {
            String templateName = AcseleConf.getProperty(TEMPLATE_CLAIM_CLOSED);
            log.debug("SE CIERRA EL SINIESTRO");
            try {
                setClosedState(templateName, messages.getString("valueautomaticclose"));
                ClaimStatus newState = ClaimStatus.CLOSED;
                com.consisint.acsele.openapi.claim.PaymentOrderCollection.changePaymentOrderStateToClose(com.consisint.acsele.openapi.claim.Claim.load(claim.getPk()), newState);
            } catch (Exception e) {
                //To change body of catch statement use File | Settings | File Templates.
            }
        }
        log.info("applyPaymentsWithType(....) - Antes de Salir");
        return (paymentsApplied == paymentsProxyVectorSize); //si se liquidaron todos los pagos
    }

    private boolean applyPaymentsWithType(Vector payments, int paymentsProxyVectorSize, String ruID, String ioID) throws ApplicationExceptionChecked {

        if (log.isDebugEnabled()) {
            log.debug("applyPaymentsWithType(....) - Entrando");
            log.debug("paymentsProxyVectorSize = " + paymentsProxyVectorSize);
            log.debug("ruID = " + ruID);
            log.debug("ioID = " + ioID);
        }
        int paymentsApplied = 0;
        Enumeration paymentsEnum = payments.elements();
        boolean isFinalPayment = false;
        HashSet h = new HashSet(payments);
        payments.clear();
        payments.addAll(h);
        while (paymentsEnum.hasMoreElements()) {
            Payment payment = (Payment) paymentsEnum.nextElement();
            applyPaymentOneByOne(policy, claim, payment, ruID, ioID);
            paymentsProxyVectorSize++;
            paymentsApplied++;
            PaymentOrder paymentOrder = payment.getPaymentOrder();

            if (paymentOrder.isFinalPayment())
                isFinalPayment = true;
        }

        String productPk = policy.getProduct().getPk();
        boolean existCoveragesPending = false;
        boolean isAutomaticClosing = false;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        boolean ischeckStatus = false;
        for (ClaimRiskUnit cru : claimRiskUnits) {
            Collection claimInsuranceObjects = cru.getClaimInsuranceObjects().values();
            for (Iterator cioIt = claimInsuranceObjects.iterator(); cioIt.hasNext(); ) {
                ClaimInsuranceObject cio = (ClaimInsuranceObject) cioIt.next();
                Collection claimNormalReserves = cio.getNormalReserves().values();//MTDC - ojo, aqui deberia tomar las de concepto
                for (Iterator cnrIt = claimNormalReserves.iterator(); cnrIt.hasNext(); ) {
                    ClaimNormalReserve cnreserves = (ClaimNormalReserve) cnrIt.next();
                    Collection paymentOrders = cnreserves.getPaymentOrderList().values();//para pago de concepto tiene cero ordenes
                    ischeckStatus= checkStatusPaymentOrder(paymentOrders);
                    PaymentOrderCollection paymentOrderCollection = new PaymentOrderCollection(cnreserves, paymentOrders);//para pago de concepto tiene cero
                    long finalPaymentId = paymentOrderCollection.getDefaultFinalPaymentId();

                    paymentsEnum = payments.elements();
                    if (log.isDebugEnabled())
                        log.debug("********************************************** payments.size(): " + payments.size());
                    EvaluatedCoverage ev = cnreserves.getEvaluatedCoverage();
                    long configuratedCoverageId = ev.getConfiguratedCoverageOA().getId();
                    ClaimsCoverageConfiguration claimConf = ClaimsCoverageConfiguration
                            .load(Long.parseLong(productPk), configuratedCoverageId);
                    while (paymentsEnum.hasMoreElements()) {
                        Payment payment = (Payment) paymentsEnum.nextElement();
                        if (log.isDebugEnabled()) {
                            log.debug("******************************************  payment.getPk(): " + payment.getPk());
                            log.debug("******************************************  payment.getAmount(): " + payment.getAmount());
                            log.debug("******************************************");
                        }
                        PaymentOrder paymentOrder = payment.getPaymentOrder();

                        if ("totalize".equals(AcseleConf.getProperty("claim.reinsurance.cessions.generated"))) {
//Por aqui pasa para pago por concepto
                            generateReserveReinsuranceDistribution(paymentOrder.getClaimReserve() != null ? paymentOrder.getClaimReserve() : cnreserves,
                                    String.valueOf(paymentOrder.getAmount()), Constants.PAYMENT_CLAIM, new java.sql.Date(new Date().getTime()), null);
                        }
                    }
                    if (claimConf.isAutomaticClosing()) {
                        isAutomaticClosing = true;
                    }
                }
            }
        }

        ResourceBundle messages = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
        if (isAutomaticClosing && isFinalPayment) {
            if (ischeckStatus) {
                if (log.isDebugEnabled()) log.debug("SE LIBERA LA RESERVA");
                freeReserves(ClaimReserveAdjust.CLAIM_FINAL_PAYMENT);

                String templateName = AcseleConf.getProperty(TEMPLATE_CLAIM_CLOSED);
                if (log.isDebugEnabled()) log.debug("SE CIERRA EL SINIESTRO");
                try {
                    setClosedState(templateName, messages.getString("valueautomaticclose"));
                    ClaimStatus newState = ClaimStatus.CLOSED;
                    com.consisint.acsele.openapi.claim.PaymentOrderCollection.changePaymentOrderStateToClose(com.consisint.acsele.openapi.claim.Claim.load(claim.getPk()), newState);

                } catch (Exception ignored) {
                }
            }else{
                Session session = null;
                Transaction transaction = null;
                 session = getHibernateSession();
                transaction = beginTransaction(false, session);
                commitTransaction(transaction, session);
                AuditTrailManager.generateClaimAuditTrail(new Date(), claim, CustomAuditItem.CLAIM_CLOSED, null);
                throw new ApplicationException(messages.getString("claim.inTotalizePayments"));
            }
        }
        if (log.isDebugEnabled()) log.debug("applyPaymentsWithType(....) - Antes de Salir");
        return (paymentsApplied == paymentsProxyVectorSize);
    }

    private boolean applyPaymentsWithType(Hashtable payments, int paymentsProxyVectorSize, String ruID, String ioID) throws ApplicationExceptionChecked {
        log.debug("applyPaymentsWithType(....) - Entrando");
        log.debug("paymentsProxyVectorSize = " + paymentsProxyVectorSize);
        log.debug("ruID = " + ruID);
        log.debug("ioID = " + ioID);

        int paymentsApplied = 0;
        boolean isFinalPayment = false;
        log.debug("payments.size() = " + payments.size());
        for (Object ob : payments.values()) {
            Hashtable paymentsTable = (Hashtable) ob;

            log.debug("paymentsTable.size() = " + paymentsTable.size());
            for (Object obListPay : paymentsTable.values()) {
                ArrayList paymentsList = (ArrayList) obListPay;

                for (Object obPay : paymentsList) {
                    Payment payment = (Payment) obPay;
                    log.debug("payment = " + payment);
                    applyPaymentOneByOne(policy, claim, payment, ruID, ioID);
                    paymentsProxyVectorSize++;
                    paymentsApplied++;

                    PaymentOrder paymentOrder = payment.getPaymentOrder();
                    if (paymentOrder.getPaymentOrderStatus().equals(PaymentOrderStatus.FINAL_PAY)) {
                        isFinalPayment = true;
                    }
                }
            }
        }

        String productPk = policy.getProduct().getPk();
        boolean existCoveragesPending = false;
        boolean isAutomaticClosing = false;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();

        for (ClaimRiskUnit cru : claimRiskUnits) {
            Collection claimInsuranceObjects = cru.getClaimInsuranceObjects().values();
            for (Iterator cioIt = claimInsuranceObjects.iterator(); cioIt.hasNext(); ) {
                ClaimInsuranceObject cio = (ClaimInsuranceObject) cioIt.next();
                Collection claimNormalReserves = cio.getNormalReserves().values();
                for (Iterator cnrIt = claimNormalReserves.iterator(); cnrIt.hasNext(); ) {
                    ClaimNormalReserve cnreserves = (ClaimNormalReserve) cnrIt.next();
                    Collection paymentOrders = cnreserves.getPaymentOrderList().values();
                    PaymentOrderCollection paymentOrderCollection = new PaymentOrderCollection(cnreserves, paymentOrders);
                    long finalPaymentId = paymentOrderCollection.getDefaultFinalPaymentId();

                    // Se libera la reserva para la cobertura en el cual se est� pagando su Final Payment Order
                    log.debug("********************************************** payments.size(): " + payments.size());

                    log.debug("payments.size() = " + payments.size());
                    for (Object ob : payments.values()) {
                        Hashtable paymentsTable = (Hashtable) ob;

                        log.debug("paymentsTable.size() = " + paymentsTable.size());
                        for (Object obListPay : paymentsTable.values()) {
                            ArrayList paymentsList = (ArrayList) obListPay;

                            for (Object obPay : paymentsList) {
                                Payment payment = (Payment) obPay;
                                log.debug("******************************************  payment.getPk(): " + payment.getPk());
                                log.debug("******************************************  payment.getAmount(): " + payment.getAmount());
                                log.debug("******************************************");
                                PaymentOrder paymentOrder = payment.getPaymentOrder();

                                if (paymentOrder.isFinalPayment()) {
                                    log.debug("SE LIBERA LA RESERVA");
                                    freeReserves(ClaimReserveAdjust.CLAIM_FINAL_PAYMENT);
                                    break;
                                }
                            }
                        }
                    }

                    EvaluatedCoverage ev = cnreserves.getEvaluatedCoverage();
                    long configuratedCoverageId = ev.getConfiguratedCoverageOA().getId();
                    ClaimsCoverageConfiguration claimConf = ClaimsCoverageConfiguration
                            .load(Long.parseLong(productPk), configuratedCoverageId);
                    //Si hay al menos una cobertura con Automatic Closing Checked
                    if (claimConf.isAutomaticClosing()) {
                        isAutomaticClosing = true;
                    }
                }
            }
        }

        //Close the claim automatically only if the option is active in the claim coverage configuration and the
        //payment order is final
        ResourceBundle messages = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
        if (isAutomaticClosing && isFinalPayment) {
            String templateName = AcseleConf.getProperty(TEMPLATE_CLAIM_CLOSED);
            log.debug("SE CIERRA EL SINIESTRO");
            try {
                setClosedState(templateName, messages.getString("valueautomaticclose"));
                ClaimStatus newState = ClaimStatus.CLOSED;
                com.consisint.acsele.openapi.claim.PaymentOrderCollection.changePaymentOrderStateToClose(com.consisint.acsele.openapi.claim.Claim.load(claim.getPk()), newState);
            } catch (Exception e) {
                //To change body of catch statement use File | Settings | File Templates.
            }
        }
        log.info("applyPaymentsWithType(....) - Antes de Salir");
        return (paymentsApplied == paymentsProxyVectorSize); //si se liquidaron todos los pagos
    }

    public synchronized void applyPaymentOneByOne(AgregatedPolicy policy, Claim claim, Payment payment, String ruID,
                                                  String ioID, Long currencyPayment, Double amountPayment, Date paymentDate) throws ApplicationExceptionChecked {
        log.debug("applyPaymentOneByOne(.....) - Entrando");
        log.debug("payment = " + payment.getPK());

        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        if (payment.getPaidAmount().doubleValue() > 0) {
            log.debug("payment.getPaidAmount().doubleValue() > 0 es true");
            String rfc;
            double debtAmount = 0.0d;
            double originalPaymentAmount = 0.0d;
            String personType;
            try {
                ThirdParty third = payment.getThirdParty();
                rfc = third.getDynamic().getInput("RFC");
                personType = third.getDynamic().getDCO().getDesc();
            } catch (Exception ex) { // TODO Cambiar mensajes
                log.error("Error", ex);
                throw new ApplicationExceptionChecked(Exceptions.CCErrorLoadingThirdPartyData, Severity.FATAL);
            }
            ClaimMovementBean beneficiary = applyPaymentUAA(payment, rfc, personType, date);

            if (!asl) {
                claim = Claim.getInstance(Long.valueOf(claim.getPk()));
            }
//            Enumeration listPayments = null;

            //Verifying the Payment Reserve Type.
//            if (payment.getReserveType() == ClaimReserveType.NORMAL_RESERVE) {
//                listPayments = claim.getPaymentsNormalReserveElements();
//            } else {
//                listPayments = claim.getPaymentsConceptReserveElements();
//            }
            PaymentOrder paymentOrder = payment.getPaymentOrder();
            if (paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.APPROVED_STATE) {
                throw new ApplicationException("Invalid Status In PaymentOrder", Severity.FATAL);
            }
            //Payment payment_ = new Payment(paymentOrder);
            ClaimReserve cr = paymentOrder.getClaimReserve();
            boolean pendingDebts = false;
            if (cr instanceof ClaimNormalReserve) {
                ClaimNormalReserve cnr = (ClaimNormalReserve) cr;
                EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                ClaimsCoverageConfiguration ccc = ClaimsCoverageConfiguration
                        .load(policy.getProduct().getId(),
                                ec.getConfiguratedCoverageOA().getId());

                if ((ccc.getPendingDebts() == PendingDebtsType.MANUAL.getValue()) || (ccc.getPendingDebts() == PendingDebtsType.AUTOMATIC.getValue())) {

                    pendingDebts = true;

                }


            }

            //Se generan los pagos de prima
            if (payment.getReserveType() == ReserveType.NORMAL_RESERVE.getValue() && pendingDebts) {
                log.debug("**** Pago de prima desde claim");
                List list = OpenItemImpl.findDeductions(policy.getPolicyNumber());
                Iterator<OpenItemImpl> iter = list.iterator();
                String queryOpenItemIDs = "";
                Long productId = policy.getProduct().getId();
                PremiunPayment pp = new PremiunPayment();
                double paymentAmount = payment.getAmount();
                originalPaymentAmount = payment.getAmount();
                OpenItemImpl tempOpentItem = null;
                for (int i = 0; i < list.size(); i++) {
                    tempOpentItem = (OpenItemImpl) list.get(i);
                    debtAmount += tempOpentItem.getAmount();
                }

                double difference = 0;
                while (iter.hasNext() && paymentAmount > 0) {
                    OpenItemImpl openItem = iter.next();
                    OpenItemBean openItemBean = new OpenItemBean(openItem);
                    queryOpenItemIDs += String.valueOf(openItem.getId()) + ",";
                    difference = paymentAmount - openItem.getBalance();
                    if (difference > 0) {
                        pp.cashOperationEqual(openItemBean, PaymentTransactionType.INVOICE_PAYMENT_OBJECT, queryOpenItemIDs, productId.intValue());
                        paymentAmount = difference;
                    } else {
                        pp.cashOperationNoEqual(openItemBean, PaymentTransactionType.INVOICE_PAYMENT_OBJECT, queryOpenItemIDs, productId.intValue(), Math.abs(difference));
                        paymentAmount = 0;
                    }
                }
                payment.setAmount(paymentAmount);
                payment.setPaidAmount(paymentAmount);
            } else {
                payment.setAmount(payment.getAmount());
                payment.setPaidAmount(payment.getAmount());
            }

            Date actualDate = new Date();
            payment.setDate(actualDate);
            payment.setPaymentStatus(PaymentStatus.PAID_STATE);
            //payment_.setThirdParty(payment.getThirdParty());
            //payment_.setReserveType(payment.getReserveType());
            // payment_.setCoverageDescription(payment.getCoverageDescription());
            //payment_.setReserveCurrencyId(payment.getReserveCurrencyId());
            //payment_.setPaidCurrencyId(payment.getPaidCurrencyId());
            // payment_.setClaimId(payment.getClaimId());
            payment.setExchangeRate(payment.getExchangeRate());
            payment.setDoneBy(UserInfo.getUser());
            payment.update();
            //Setting the Payment Orders.

//            Collection<PaymentOrder> enumeration = payment.getPaymentOrders();
//
//            for (PaymentOrder paymentOrder : enumeration) {
//                paymentOrder = PaymentOrder.load(new Long(paymentOrder.getPk()));

            /*ClaimReserve reserve = paymentOrder.getClaimReserve();
            ClaimRiskUnit claimRiskUnit;
            if (reserve instanceof ClaimNormalReserve) {
                claimRiskUnit = ((ClaimNormalReserve) reserve).getContainer().getContainer();
            } else {
                claimRiskUnit = ((ClaimReserveByConcept) reserve).getContainer().getContainer();
            }
            ClaimRiskUnitPayment claimRiskUnitPayment = new ClaimRiskUnitPayment(claimRiskUnit, payment_);
            claimRiskUnitPayment.save();
            log.debug("paymentOrder.getPk() = " + paymentOrder.getPk() + " - paymentOrder.getAmount() = " + paymentOrder.getAmount()
                    + " - paymentOrder.getState() = " + paymentOrder.getStateMessage() + " - paymentOrder.getPayment().getPk() = "
                    + paymentOrder.getPayment().getPk() + " - payment_" + payment_.getPk());
            ClaimRiskUnitPayment.deletePaymentsRelation(payment);*/

           /* try {
                HibernateUtil.getActualSession().evict(HibernateUtil.getActualSession().get(payment.getClass(), payment.getPk()));
                HibernateUtil.getActualSession().delete(payment);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            //payment.delete();
            log.debug("applyPaymentsWithType: Voy a Actualizar las Ordenes de Pago.....");
            List paymentOrdersToProcess = new ArrayList();
            Hashtable details = updatePaymentsOrders(policy, payment,
                    PaymentOrderStatus.PAID_STATE, beneficiary, false,
                    paymentOrdersToProcess);

            try {
                //Si el monto del pago es 0 no se generan los movimientos
                //Si la poliza es de coaseguro aceptado tampoco debe generar movimientos, solo se generara el del contrato de coaseguro.
                if (payment.getAmount() != 0 && (policy.getCoinsuranceContract() == null || policy.getCoinsuranceContract().getType() != CoinsuranceContract.COINSURANCE_ACCEPTED)) {

                    //TO DO PONER AQUI EL CHEQUEO...
                    if (pendingDebts) {
                        beneficiary.setAmount(originalPaymentAmount - debtAmount);
                    }

                    sendMovementUAA(policy, claim, payment, beneficiary, details, policy.getPolicyNumber(), ruID, ioID, paymentOrdersToProcess, String.valueOf(payment.getPaymentOrder().getThirdPartyRoleID()), currencyPayment, amountPayment, paymentDate);
                }

            } catch (Exception e) {
                log.error("Error", e);
                throw new ApplicationExceptionChecked(Exceptions.CCErrorLoadingThirdPartyData, Severity.FATAL);
            }
        } else {
            log.debug("payment.getPaidAmount().doubleValue() > 0 es false");
        }
        log.info("applyPaymentOneByOne(.....) - Saliendo");
    }

    /**
     * @param policy
     * @param claim
     * @param payment
     * @throws ApplicationExceptionChecked
     */
    public synchronized void applyPaymentOneByOne(AgregatedPolicy policy, Claim claim, Payment payment, String ruID,
                                                  String ioID) throws ApplicationExceptionChecked {
        log.debug("applyPaymentOneByOne(.....) - Entrando");
        log.debug("payment = " + payment.getPK());

        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        if (payment.getAmount().doubleValue() > 0) {
            log.debug("payment.getAmount().doubleValue() > 0 es true");
            String rfc;
            double debtAmount = 0.0d;
            double originalPaymentAmount = 0.0d;
            String personType;
            try {
                ThirdParty third = payment.getThirdParty();
                rfc = third.getDynamic().getInput("RFC");
                personType = third.getDynamic().getDCO().getDesc();
            } catch (Exception ex) { // TODO Cambiar mensajes
                log.error("Error", ex);
                throw new ApplicationExceptionChecked(Exceptions.CCErrorLoadingThirdPartyData, Severity.FATAL);
            }
            ClaimMovementBean beneficiary = applyPaymentUAA(payment, rfc, personType, date);

            if (!asl) {
                claim = Claim.getInstance(Long.valueOf(claim.getPk()));
            }
//            Enumeration listPayments = null;

            //Verifying the Payment Reserve Type.
//            if (payment.getReserveType() == ClaimReserveType.NORMAL_RESERVE) {
//                listPayments = claim.getPaymentsNormalReserveElements();
//            } else {
//                listPayments = claim.getPaymentsConceptReserveElements();
//            }
            PaymentOrder paymentOrder = payment.getPaymentOrder();
            if (paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.APPROVED_STATE) {
                throw new ApplicationException("Invalid Status In PaymentOrder", Severity.FATAL);
            }
            //Payment payment_ = new Payment(paymentOrder);
            ClaimReserve cr = paymentOrder.getClaimReserve();
            boolean pendingDebts = false;
            if (cr instanceof ClaimNormalReserve) {
                ClaimNormalReserve cnr = (ClaimNormalReserve) cr;
                EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                ClaimsCoverageConfiguration ccc = ClaimsCoverageConfiguration
                        .load(policy.getProduct().getId(),
                                ec.getConfiguratedCoverageOA().getId());

                if ((ccc.getPendingDebts() == PendingDebtsType.MANUAL.getValue()) || (ccc.getPendingDebts() == PendingDebtsType.AUTOMATIC.getValue())) {

                    pendingDebts = true;

                }

            }

            //Se generan los pagos de prima
            if (payment.getReserveType() == ReserveType.NORMAL_RESERVE.getValue() && pendingDebts) {
                log.debug("**** Pago de prima desde claim");
                List list = OpenItemImpl.findDeductions(policy.getPolicyNumber());
                Iterator<OpenItemImpl> iter = list.iterator();
                String queryOpenItemIDs = "";
                Long productId = policy.getProduct().getId();
                PremiunPayment pp = new PremiunPayment();
                double paymentAmount = payment.getAmount();
                originalPaymentAmount = payment.getAmount();
                OpenItemImpl tempOpentItem = null;
                for (int i = 0; i < list.size(); i++) {
                    tempOpentItem = (OpenItemImpl) list.get(i);
                    debtAmount += tempOpentItem.getAmount();
                }

                double difference = 0;
                while (iter.hasNext() && paymentAmount > 0) {
                    OpenItemImpl openItem = iter.next();
                    OpenItemBean openItemBean = new OpenItemBean(openItem);
                    queryOpenItemIDs += String.valueOf(openItem.getId()) + ",";
                    difference = paymentAmount - openItem.getBalance();
                    if (difference > 0) {
                        pp.cashOperationEqual(openItemBean, PaymentTransactionType.INVOICE_PAYMENT_OBJECT, queryOpenItemIDs, productId.intValue());
                        paymentAmount = difference;
                    } else {
                        pp.cashOperationNoEqual(openItemBean, PaymentTransactionType.INVOICE_PAYMENT_OBJECT, queryOpenItemIDs, productId.intValue(), Math.abs(difference));
                        paymentAmount = 0;
                    }
                }
                payment.setAmount(paymentAmount);
                payment.setPaidAmount(paymentAmount);
            } else {
                payment.setAmount(payment.getAmount());
                payment.setPaidAmount(payment.getAmount());
            }

            Date actualDate = new Date();
            payment.setDate(actualDate);
            payment.setState(PaymentOrderStatus.PAID_STATE.getValue());
            payment.setPaymentStatus(PaymentStatus.PAID_STATE);
            //payment_.setThirdParty(payment.getThirdParty());
            //payment_.setReserveType(payment.getReserveType());
            // payment_.setCoverageDescription(payment.getCoverageDescription());
            //payment_.setReserveCurrencyId(payment.getReserveCurrencyId());
            //payment_.setPaidCurrencyId(payment.getPaidCurrencyId());
            // payment_.setClaimId(payment.getClaimId());
            payment.setExchangeRate(payment.getExchangeRate());
            payment.setDoneBy(UserInfo.getUser());
            payment.update();
            //Setting the Payment Orders.

//            Collection<PaymentOrder> enumeration = payment.getPaymentOrders();
//
//            for (PaymentOrder paymentOrder : enumeration) {
//                paymentOrder = PaymentOrder.load(new Long(paymentOrder.getPk()));

            /*ClaimReserve reserve = paymentOrder.getClaimReserve();
            ClaimRiskUnit claimRiskUnit;
            if (reserve instanceof ClaimNormalReserve) {
                claimRiskUnit = ((ClaimNormalReserve) reserve).getContainer().getContainer();
            } else {
                claimRiskUnit = ((ClaimReserveByConcept) reserve).getContainer().getContainer();
            }
            ClaimRiskUnitPayment claimRiskUnitPayment = new ClaimRiskUnitPayment(claimRiskUnit, payment_);
            claimRiskUnitPayment.save();
            log.debug("paymentOrder.getPk() = " + paymentOrder.getPk() + " - paymentOrder.getAmount() = " + paymentOrder.getAmount()
                    + " - paymentOrder.getState() = " + paymentOrder.getStateMessage() + " - paymentOrder.getPayment().getPk() = "
                    + paymentOrder.getPayment().getPk() + " - payment_" + payment_.getPk());
            ClaimRiskUnitPayment.deletePaymentsRelation(payment);*/

           /* try {
                HibernateUtil.getActualSession().evict(HibernateUtil.getActualSession().get(payment.getClass(), payment.getPk()));
                HibernateUtil.getActualSession().delete(payment);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            //payment.delete();
            log.debug("applyPaymentsWithType: Voy a Actualizar las Ordenes de Pago.....");
            List paymentOrdersToProcess = new ArrayList();
            Hashtable details = updatePaymentsOrders(policy, payment,
                    PaymentOrderStatus.PAID_STATE, beneficiary, false,
                    paymentOrdersToProcess);

            try {
                //Si el monto del pago es 0 no se generan los movimientos
                //Si la poliza es de coaseguro aceptado tampoco debe generar movimientos, solo se generara el del contrato de coaseguro.
                if (payment.getAmount() != 0 && (policy.getCoinsuranceContract() == null || policy.getCoinsuranceContract().getType() != CoinsuranceContract.COINSURANCE_ACCEPTED)) {

                    //TO DO PONER AQUI EL CHEQUEO...
                    if (pendingDebts) {
                        beneficiary.setAmount(originalPaymentAmount - debtAmount);
                        ClaimNormalReserve cnr = (ClaimNormalReserve) cr;
                        EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                        ClaimsCoverageConfiguration ccc = ClaimsCoverageConfiguration
                                .load(policy.getProduct().getId(),
                                        ec.getConfiguratedCoverageOA().getId());
                        long paymentDocTypeID = -1;
                        try {
                            paymentDocTypeID = DocType.Impl.load(AcseleConf.getProperty("premiumConcept")).getId();
                        } catch (Exception e) {
                            log.warn("Cannot find document : " + AcseleConf.getProperty("premiumConcept"));
                        }
                        if(ccc.getPendingDebts() == PendingDebtsType.AUTOMATIC.getValue()) {
                            List openItemList = OpenItemImpl.findMovementsToDiscountByClaim(String.valueOf(claim.getId()));
                            double totalOpenItemsBalance = 0.0;
                            for(int i = 0; i < openItemList.size(); i++){
                                OpenItemImpl openItem = (OpenItemImpl) openItemList.get(i);
                                totalOpenItemsBalance += openItem.getBalance();
                            }
                            if(openItemList.size() > 0 && totalOpenItemsBalance <= payment.getAmount()){
                                sendMovementUAA(policy, claim, payment, beneficiary, details, policy.getPolicyNumber(), ruID, ioID, paymentOrdersToProcess, String.valueOf(payment.getPaymentOrder().getThirdPartyRoleID()));
                            }else{
                                //TODO mensaje de error, porque el monto total del balance de openitems es mayor al monto del pago
                            }
                        }
                    }else{
                        sendMovementUAA(policy, claim, payment, beneficiary, details, policy.getPolicyNumber(), ruID, ioID, paymentOrdersToProcess, String.valueOf(payment.getPaymentOrder().getThirdPartyRoleID()));
                    }
                }

            } catch (Exception e) {
                log.error("Error", e);
                ExceptionUtil.handlerException(e);
            }
        } else {
            log.debug("payment.getPaidAmount().doubleValue() > 0 es false");
        }
        log.info("applyPaymentOneByOne(.....) - Saliendo");
    }


    /**
     * @param payment
     * @param rfc
     * @param personType
     * @param date
     * @return ClaimMovementBean
     */
    private ClaimMovementBean applyPaymentUAA(Payment payment, String rfc, String personType, java.sql.Date date) {
        log.info("applyPaymentUAA(....) - Entrando");
        log.debug("rfc = " + rfc);
        log.debug("personType = " + personType);
        log.debug("date = " + date);
        String docTypeDescription = AcseleConf.getProperty("claimDoctype");
        log.debug("docTypeDescription = " + docTypeDescription);
        double amount;
        if (AcseleConf.getProperty("acseleExchange").equals("yes")) {
            amount = payment.getPaidAmount().doubleValue();
        } else {
            amount = payment.getAmount().doubleValue();
        }
        log.debug("amount = " + amount);

        ClaimMovementBean bean = new ClaimMovementBean((int) DocType.Impl.load(docTypeDescription).getId(),
                payment.getThirdParty().getPK(), rfc, personType, amount, date, null, StatusMovement.ACTIVE.getValue());

        bean.setThirdPartyName(payment.getThirdParty().getName());
        log.info("applyPaymentUAA(....) - Antes de Salir");
        return bean;
    }

    /**
     * @param policy
     * @param claim
     * @param payment
     * @param tercero
     * @param detailsMovements
     * @param policyNumber
     * @param ruID
     * @param ioID
     * @param roleId
     * @throws Exception
     */
    private void sendMovementUAA(AgregatedPolicy policy, Claim claim, Payment payment, ClaimMovementBean tercero, Hashtable detailsMovements,
                                 String policyNumber, String ruID, String ioID, List paymentOrdersToProcess, String roleId) throws Exception {

        String docTypeDescription = AcseleConf.getProperty("claimDoctype");
        if (docTypeDescription != null) {
            int docTypeID = (int) DocType.Impl.load(docTypeDescription).getId();
            if (docTypeID == tercero.getDocTypeID()) {

                if (payment.getPaymentOrder().getClaimReserve() instanceof ClaimNormalReserve) {

                    ClaimNormalReserve claimNormalReserve = (ClaimNormalReserve) payment.getPaymentOrder().getClaimReserve();
                    ClaimsCoverageConfiguration myCCC = ClaimsCoverageConfiguration.load(policy.getProduct().getId(),
                            claimNormalReserve.getEvaluatedCoverage().getConfiguratedCoverageOA().getId());

                    if (!myCCC.isConsolidatedPayment()) {  // ACSELE-6622: Do not generate the open item if coverage that is marked as "settled" group formation
                        ClaimProcessService.Impl.getInstance().processPolicyAfterPayment(payment, myCCC);
                        OpenItem openItem = sendMovementUAA(policy, claim, payment,
                                (int) ClaimProcessService.Impl.getInstance().getClaimDocType(payment, myCCC, tercero.getDocTypeID()),
                                tercero.getThirdId(), tercero.getAmount(), tercero.getDate(),
                                tercero.getParentOpemItemId(), tercero.getStatu(), policyNumber, roleId);
                        Enumeration keys = detailsMovements.keys();
                        while (keys.hasMoreElements()) {
                            String desc = (String) keys.nextElement();
                            ClaimDetailsPaymentBean claimDetailsPaymentBean = (ClaimDetailsPaymentBean) detailsMovements.get(desc);
                            long uaaDetailNew = IDDBFactory.getNextIDL("UAADetail");
                            UaaDetail uaaDetail = new UaaDetail(uaaDetailNew, openItem.getOpenItemID(), claimDetailsPaymentBean.getPk(), claimDetailsPaymentBean.getAmount());
                            uaaDetail.save();
                        }
                        //If has a manual pending debt then an openitem is generated, concept: "PagoContratante"
                        if((myCCC.getPendingDebts() == PendingDebtsType.MANUAL.getValue())){

                            HashMap<String, Object> mapDeductionApply = Funciones.calculateOpenItemsToDiscount(payment.getPaymentOrder().getFkReserve(), new Long((payment.getClaimId())));
                            if (mapDeductionApply!=null && !mapDeductionApply.isEmpty() && (Double)mapDeductionApply.get("totalDiscount") > 0.0){

                                ArrayList<String> deducedOpenitemList = (ArrayList) mapDeductionApply.get("deducedOpenItemSelected");
                                for(String doim : deducedOpenitemList){
                                    //Making new PendingDebt
                                    long paymentDocTypeID = -1;
                                    try {
                                        paymentDocTypeID =
                                                DocType.Impl.load(AcseleConf.getProperty("paymentDocument")).getId();
                                    } catch (Exception e) {
                                        log.warn("Cannot find document : " + AcseleConf.getProperty("paymentDocument"));
                                    }
                                    Integer docTypeIdPendingDebt = (int) paymentDocTypeID;
                                    OpenItem openItemPendingDebt = sendMovementUAA(policy, claim, payment, docTypeIdPendingDebt, tercero.getThirdId(), Double.valueOf(doim.split(";")[1]), tercero.getDate(), tercero.getParentOpemItemId(), StatusMovement.APPLIED.getValue(), policyNumber, roleId);
                                    //Updating OI deduction
                                    Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
                                    OpenItemImpl openItemDeduction = (OpenItemImpl) session.get(OpenItemImpl.class, Long.valueOf(doim.split(";")[0]));
                                    openItemDeduction.setStatus(StatusMovement.APPLIED.getValue(), SubStatusMovement.APPLIED.getValue(), StringUtil.EMPTY_STRING);
                                    openItemDeduction.setAppliedTo(openItemPendingDebt.getOpenItemID());
                                    session.flush();
                                }
                            }
                        }else if(myCCC.getPendingDebts() == PendingDebtsType.AUTOMATIC.getValue()){
                            List openItemList = OpenItemImpl.findMovementsToDiscountByClaim(String.valueOf(claim.getId()));
                            double totalOpenItemsDebt = 0.0;
                            if (openItemList.size() > 0) {
                                for (int i = 0; i < openItemList.size(); i++) {
                                    OpenItemImpl pendingDebtOpenItem = (OpenItemImpl) openItemList.get(i);
                                    totalOpenItemsDebt += pendingDebtOpenItem.getBalance();
                                    String pendingStatus = pendingDebtOpenItem.getStatus();
                                    if (pendingStatus.equals(StatusMovement.PENDING.getValue())) {
                                        long paymentDocTypeID = -1;
                                        try {
                                            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();

                                            pendingDebtOpenItem.setStatus(StatusMovement.APPLIED.getValue());
                                            session.flush();
                                            if(pendingDebtOpenItem == openItemList.get(openItemList.size() - 1)){
                                                paymentDocTypeID = DocType.Impl.load(AcseleConf.getProperty("paymentDocument")).getId();
                                                double totalAmount = payment.getAmount() - totalOpenItemsDebt;
                                                payment.setAmount(totalAmount);
                                                sendMovementUAA(policy, claim, payment, (int) paymentDocTypeID, tercero.getThirdId(), totalOpenItemsDebt, tercero.getDate(),
                                                        tercero.getParentOpemItemId(), StatusMovement.APPLIED.getValue(), policyNumber, roleId);
                                            }
                                        } catch (Exception e) {
                                            log.warn("Cannot find document : " + AcseleConf.getProperty("paymentDocument"));
                                        }
                                    }
                                }
                            }
                        }
                        generateLiquidate(payment.getPaymentOrder().getPk(), openItem.getOpenItemID());
                        AuditTrailManager.generateClaimPaymentAuditTrail(new Date(), payment.getPaymentOrder(), CustomAuditItem.PAYMENT_REQUESTED);
                        if (hasInvoice(payment.getPaymentOrder())) {
                            log.info("No se envio a la interfaz Exactus");
                        } else {
                            sendOpenItemsInterface(openItem, claim, Long.parseLong(tercero.getThirdId()), tercero.getAmount());
                            log.info("Se realizo el envio a la interfaz Exactus");
                        }
                    }
                } else {
                    OpenItem openItem = sendMovementUAA(policy, claim, payment, tercero.getDocTypeID(), tercero.getThirdId(), tercero.getAmount(), tercero.getDate(),
                            tercero.getParentOpemItemId(), tercero.getStatu(), policyNumber, roleId);
                    Enumeration keys = detailsMovements.keys();
                    while (keys.hasMoreElements()) {
                        String desc = (String) keys.nextElement();
                        ClaimDetailsPaymentBean claimDetailsPaymentBean = (ClaimDetailsPaymentBean) detailsMovements.get(desc);
                        UaaDetail uaaDetail = new UaaDetail(IDDBFactory.getNextIDL("UAADetail"), openItem.getOpenItemID(), claimDetailsPaymentBean.getPk(), claimDetailsPaymentBean.getAmount());
                        uaaDetail.save();
                    }
                    generateLiquidate(payment.getPaymentOrder().getPk(), openItem.getOpenItemID());
                    AuditTrailManager.generateClaimPaymentAuditTrail(new Date(), payment.getPaymentOrder(), CustomAuditItem.PAYMENT_REQUESTED);
                    if (hasInvoice(payment.getPaymentOrder())) {
                        log.info("No se envio a la interfaz Exactus");
                    } else {
                        sendOpenItemsInterface(openItem, claim, Long.parseLong(tercero.getThirdId()), tercero.getAmount());
                        log.info("Se realizo el envio a la interfaz Exactus");
                    }

                }
            }
        }
        if (payment.getReserveType() == ReserveType.NORMAL_RESERVE.getValue()) {
            PaymentOrder paymentOrder = payment.getPaymentOrder();
            if (paymentOrdersToProcess.contains(paymentOrder)) {
                if (!"false".equalsIgnoreCase(AcseleConf.getProperty("EnableEntriesGenerationInASL"))
                        && AcseleConf.getAsInteger("claimEntryGeneration") == 0) {
                    createEntrys(policy, claim, payment, paymentOrder, tercero, detailsMovements, ruID, ioID, null);
                }
            }
        } else {
            if (!"false".equalsIgnoreCase(AcseleConf.getProperty("EnableEntriesGenerationInASL"))
                    && AcseleConf.getAsInteger("claimEntryGeneration") == 0) {
                createEntrys(policy, claim, payment, tercero, detailsMovements, ruID, ioID,null);
            }
        }
    }

    /**
     * @param policy
     * @param claim
     * @param payment
     * @param tercero
     * @param detailsMovements
     * @param policyNumber
     * @param ruID
     * @param ioID
     * @param roleId
     * @param currencyPayment
     * @param amountPayment
     * @param paymentDate
     * @throws Exception
     */
    private void sendMovementUAA(AgregatedPolicy policy, Claim claim, Payment payment, ClaimMovementBean tercero, Hashtable detailsMovements,
                                 String policyNumber, String ruID, String ioID, List paymentOrdersToProcess, String roleId, Long currencyPayment, Double amountPayment, Date paymentDate) throws Exception {

        String docTypeDescription = AcseleConf.getProperty("claimDoctype");
        if (docTypeDescription != null) {
            int docTypeID = (int) DocType.Impl.load(docTypeDescription).getId();
            if (docTypeID == tercero.getDocTypeID()) {

                if (payment.getPaymentOrder().getClaimReserve() instanceof ClaimNormalReserve) {

                    ClaimNormalReserve claimNormalReserve = (ClaimNormalReserve) payment.getPaymentOrder().getClaimReserve();
                    ClaimsCoverageConfiguration myCCC = ClaimsCoverageConfiguration.load(policy.getProduct().getId(),
                            claimNormalReserve.getEvaluatedCoverage().getConfiguratedCoverageOA().getId());

                    if (!myCCC.isConsolidatedPayment()) {  // ACSELE-6622: Do not generate the open item if coverage that is marked as "settled" group formation
                        OpenItem openItem = sendMovementUAA(policy, claim, payment, tercero.getDocTypeID(), tercero.getThirdId(), tercero.getAmount(), tercero.getDate(),
                                tercero.getParentOpemItemId(), tercero.getStatu(), policyNumber, roleId, currencyPayment, amountPayment, paymentDate);
                        Enumeration keys = detailsMovements.keys();
                        while (keys.hasMoreElements()) {
                            String desc = (String) keys.nextElement();
                            ClaimDetailsPaymentBean claimDetailsPaymentBean = (ClaimDetailsPaymentBean) detailsMovements.get(desc);
                            long uaaDetailNew = IDDBFactory.getNextIDL("UAADetail");
                            UaaDetail uaaDetail = new UaaDetail(uaaDetailNew, openItem.getOpenItemID(), claimDetailsPaymentBean.getPk(), claimDetailsPaymentBean.getAmount());
                            uaaDetail.save();
                        }
                        generateLiquidate(payment.getPaymentOrder().getPk(), openItem.getOpenItemID());
                        AuditTrailManager.generateClaimPaymentAuditTrail(new Date(), payment.getPaymentOrder(), CustomAuditItem.PAYMENT_REQUESTED);
                        if (hasInvoice(payment.getPaymentOrder())) {
                            log.info("No se envio a la interfaz Exactus");
                        } else {
                            sendOpenItemsInterface(openItem, claim, Long.parseLong(tercero.getThirdId()), tercero.getAmount());
                            log.info("Se realizo el envio a la interfaz Exactus");
                        }
                    }
                } else {
                    // No es in ClaimNormalReserve
                    OpenItem openItem = sendMovementUAA(policy, claim, payment, tercero.getDocTypeID(), tercero.getThirdId(), tercero.getAmount(), tercero.getDate(),
                            tercero.getParentOpemItemId(), tercero.getStatu(), policyNumber, roleId);
                    Enumeration keys = detailsMovements.keys();
                    while (keys.hasMoreElements()) {
                        String desc = (String) keys.nextElement();
                        ClaimDetailsPaymentBean claimDetailsPaymentBean = (ClaimDetailsPaymentBean) detailsMovements.get(desc);
                        UaaDetail uaaDetail = new UaaDetail(IDDBFactory.getNextIDL("UAADetail"), openItem.getOpenItemID(), claimDetailsPaymentBean.getPk(), claimDetailsPaymentBean.getAmount());
                        uaaDetail.save();
                    }
                    generateLiquidate(payment.getPaymentOrder().getPk(), openItem.getOpenItemID());
                    AuditTrailManager.generateClaimPaymentAuditTrail(new Date(), payment.getPaymentOrder(), CustomAuditItem.PAYMENT_REQUESTED);
                    if (hasInvoice(payment.getPaymentOrder())) {
                        log.info("No se envio a la interfaz Exactus");
                    } else {
                        sendOpenItemsInterface(openItem, claim, Long.parseLong(tercero.getThirdId()), tercero.getAmount());
                        log.info("Se realizo el envio a la interfaz Exactus");
                    }

                }
            }
        }
        if (payment.getReserveType() == ReserveType.NORMAL_RESERVE.getValue()) {
            PaymentOrder paymentOrder = payment.getPaymentOrder();
            if (paymentOrdersToProcess.contains(paymentOrder)) {
                if (!"false".equalsIgnoreCase(AcseleConf.getProperty("EnableEntriesGenerationInASL"))) {
                    createEntrys(policy, claim, payment, paymentOrder, tercero, detailsMovements, ruID, ioID, null);
                }
            }
//            }
        } else {
            if (!"false".equalsIgnoreCase(AcseleConf.getProperty("EnableEntriesGenerationInASL"))) {
                createEntrys(policy, claim, payment, tercero, detailsMovements, ruID, ioID,null);
            }
        }
    }

    /**
     *
     * @param riskUnitID
     * @return String
     */
//2007-04-20 (SV)
//    private String getRiskUnitNumber(Long riskUnitID) {
//        return getRiskUnitNumber(policy, riskUnitID);
//    }

    /**
     * @param policy
     * @param riskUnitId
     * @return String
     */
    private String getRiskUnitNumber(AgregatedPolicy policy, Long riskUnitId) {
        AgregatedRiskUnit riskUnit = getAgregatedRiskUnitById(String.valueOf(riskUnitId));
        return riskUnit.getDCO().getCriterioInput(AcseleConf.getProperty("riskunit.item.number"));
    }

    /**
     * @param policy
     * @param claim
     * @param payment
     * @param docTypeID
     * @param thirdId
     * @param amount
     * @param date
     * @param parentOpemItemId
     * @param statu
     * @param policyNumber
     * @param roleId
     * @return int
     * @throws Exception
     */
    private OpenItem sendMovementUAA(AgregatedPolicy policy, Claim claim, Payment payment, int docTypeID, String thirdId, double amount,
                                     java.sql.Date date, String parentOpemItemId, String statu, String policyNumber, String roleId) throws Exception {
        log.debug("[sendMovementUAA] policy = " + policy + "claim = " + claim + "payment = " + payment);
        DocType docType = DocType.Impl.load(docTypeID);
        FinancialSession financialSession = new FinancialSessionImpl();
        OpenItemVO openItemVO = new OpenItemVO(null, null, amount, OpenItemReferenceType.CLAIM);
        openItemVO.setThirdPartyId(thirdId);
        openItemVO.setReferenceType(OpenItemReferenceType.CLAIM);
        openItemVO.setRoleId(roleId);
        Long policyID = Long.valueOf(policy.getPK());
        NewAgreement agreement = policy.getAgreement();
        Collection<ClaimRiskUnit> claimRiskUnits = ClaimRiskUnitPayment.getClaimRiskUnits(payment);
        for (ClaimRiskUnit cruBean : claimRiskUnits) {
            Long riskUnitID = Long.valueOf(cruBean.getAgregatedRiskUnit().getPk());
            OpenItemReferenceDTO openItemReferenceDTO = new OpenItemReferenceDTO(policyID, policyNumber, riskUnitID,
                    getRiskUnitNumber(policy, riskUnitID), claim.getId(), payment.getPk());
            openItemReferenceDTO.setClaimNumber(claim.getClaimNumber());
            openItemReferenceDTO.setRcoId(claim.getOperationPK());
            if (agreement != null) openItemReferenceDTO.setAgvID(agreement.getVersionId());
            openItemVO.setOpenItemReferenceDTO(openItemReferenceDTO);
        }
        openItemVO.setDocType(docType);

        Date startDate = payment.getStartDate();
        log.debug("startDate = " + startDate);
        Date endDate = payment.getEndDate();
        log.debug("endDate = " + endDate);

        openItemVO.setDocDate(new java.sql.Date(startDate.getTime()));
        openItemVO.setDueDate(new java.sql.Date(startDate.getTime()));
        openItemVO.setDateUseRecipents(new java.sql.Date(endDate.getTime()));
        if (parentOpemItemId != null) openItemVO.setParentOpenItemId(parentOpemItemId);

        if (AcseleConf.getProperty("acseleExchange").equals("yes")) { //fixme: (ALM) refactory!!
            openItemVO.setAmount(amount);
            openItemVO.setCurrency(Currency.Impl.load(Long.valueOf(payment.getPaidCurrencyId())));
            openItemVO.setSapCurrencyId(Long.valueOf(payment.getPaidCurrencyId()));
        } else {
            openItemVO.setAmount(amount);
            openItemVO.setCurrency(Currency.Impl.load(Long.valueOf(payment.getReserveCurrencyId())));
            openItemVO.setSapCurrencyId(Long.valueOf(payment.getPaidCurrencyId()));
        }

        if (statu != null) openItemVO.setStatus(statu);
        OpenItem openItem = postTrx(openItemVO, payment);
        log.debug(" openitemId:" + openItem.getPrimaryKey() + " TP Id:  TP Name:  DocType: " + docType + "\n amount " + openItemVO.getAmount()
                + " currency Orig: " + openItemVO.getCurrency() + " currency SAP: " + openItemVO.getSapCurrencyId());
        log.debug("ThirdpartyId" + payment.getThirdParty().getName());
        try {
            ClaimHistoricalMovementType claimHistoricalMovementType = null;
            PaymentOrder paymentOrder = payment.getPaymentOrder();

            if (paymentOrder.isTotalPayment()) {
                claimHistoricalMovementType = ClaimHistoricalMovementType.TOTAL_PAYMENT;
            } else {
                claimHistoricalMovementType = ClaimHistoricalMovementType.PARTIAL_PAYMENT;
            }

            ClaimReserve claimReserve = paymentOrder.getClaimReserve();
            ClaimHistorical claimHistorical = new ClaimHistorical();
            claimHistorical.generateHistoricalWithMovement(this.claim, ClaimHistoricalOperationType.PAYMENTS, claimHistoricalMovementType, Long.parseLong(payment.getPK()), ClaimUtil.getValidatedLegacyType(claimReserve));
        } catch (Exception e) {

        }
        return openItem;
    }

    /**
     * @param policy
     * @param claim
     * @param payment
     * @param docTypeID
     * @param thirdId
     * @param amount
     * @param date
     * @param parentOpemItemId
     * @param statu
     * @param policyNumber
     * @param roleId
     * @param currencyPayment
     * @param amountPayment
     * @param paymentDate
     * @return int
     * @throws Exception
     */
    private OpenItem sendMovementUAA(AgregatedPolicy policy, Claim claim, Payment payment, int docTypeID, String thirdId, double amount,
                                     java.sql.Date date, String parentOpemItemId, String statu, String policyNumber, String roleId, Long currencyPayment, Double amountPayment, Date paymentDate) throws Exception {
        log.debug("[sendMovementUAA] policy = " + policy + "claim = " + claim + "payment = " + payment);
        DocType docType = DocType.Impl.load(docTypeID);
        FinancialSession financialSession = new FinancialSessionImpl();
        OpenItemVO openItemVO = new OpenItemVO(null, null, amount, OpenItemReferenceType.CLAIM);
        openItemVO.setThirdPartyId(thirdId);
        openItemVO.setReferenceType(OpenItemReferenceType.CLAIM);
        openItemVO.setRoleId(roleId);
        Long policyID = Long.valueOf(policy.getPK());
        NewAgreement agreement = policy.getAgreement();
        Collection<ClaimRiskUnit> claimRiskUnits = ClaimRiskUnitPayment.getClaimRiskUnits(payment);
        for (ClaimRiskUnit cruBean : claimRiskUnits) {
            Long riskUnitID = Long.valueOf(cruBean.getAgregatedRiskUnit().getPk());
            OpenItemReferenceDTO openItemReferenceDTO = new OpenItemReferenceDTO(policyID, policyNumber, riskUnitID,
                    getRiskUnitNumber(policy, riskUnitID), claim.getId(), payment.getPk());
            openItemReferenceDTO.setClaimNumber(claim.getClaimNumber());
            if (agreement != null) openItemReferenceDTO.setAgvID(agreement.getVersionId());
            openItemVO.setOpenItemReferenceDTO(openItemReferenceDTO);
        }
        openItemVO.setDocType(docType);

        Date startDate = payment.getStartDate();
        log.debug("startDate = " + startDate);
        Date endDate = payment.getEndDate();
        log.debug("endDate = " + endDate);

        openItemVO.setDocDate(new java.sql.Date(startDate.getTime()));
        openItemVO.setDueDate(new java.sql.Date(startDate.getTime()));
        openItemVO.setDateUseRecipents(new java.sql.Date(endDate.getTime()));

        if (parentOpemItemId != null) openItemVO.setParentOpenItemId(parentOpemItemId);

        if (AcseleConf.getProperty("acseleExchange").equals("yes")) { //fixme: (ALM) refactory!!
            openItemVO.setAmount(amount);
            openItemVO.setCurrency(Currency.Impl.load(Long.valueOf(payment.getPaidCurrencyId())));
            openItemVO.setSapCurrencyId(Long.valueOf(payment.getPaidCurrencyId()));
        } else {
            openItemVO.setAmount(amount);
            openItemVO.setCurrency(Currency.Impl.load(Long.valueOf(payment.getReserveCurrencyId())));
            openItemVO.setSapCurrencyId(Long.valueOf(payment.getPaidCurrencyId()));
        }
        if (currencyPayment != -1 && amountPayment != null && paymentDate != null) {
            Currency currency = Currency.Impl.load(currencyPayment);
            openItemVO.setOpenItemDate(new java.sql.Date(paymentDate.getTime()));
            openItemVO.setAmount(amountPayment);
            openItemVO.setCurrency(currency);
            openItemVO.setBalance(amountPayment);
        }

        if (statu != null) openItemVO.setStatus(statu);
        OpenItem openItem = postTrx(openItemVO, payment);
        log.debug(" openitemId:" + openItem.getPrimaryKey() + " TP Id:  TP Name:  DocType: " + docType + "\n amount " + openItemVO.getAmount()
                + " currency Orig: " + openItemVO.getCurrency() + " currency SAP: " + openItemVO.getSapCurrencyId());
        log.debug("ThirdpartyId" + payment.getThirdParty().getName());
        addClaimHistorical(claim, ClaimHistoricalOperationType.PAYMENTS);
        updateClaimReinsuranceParentID(claim, openItem);
        return openItem;
    }

    private void updateClaimReinsuranceParentID(Claim claim, OpenItem openItem) {
        ArrayList<OpenItemReferenceImpl> reference = (ArrayList) OpenItemReferenceImpl.findByClaimNumber(claim.getClaimNumber());
        ArrayList allowedDocTypes = (ArrayList) StringUtil.splitAsList(AcseleConf.getProperty("allowed.claim.reinsurance.doctypes"));
        for (OpenItemReferenceImpl openItemReference : reference) {
            if (openItemReference.getComponentId() != null && allowedDocTypes.contains(openItemReference.getOpenItem().getDocType().getDescription())) {
                openItemReference.getOpenItem().setParentOpenItemIdInString(String.valueOf(openItem.getOpenItemID()));
                OpenItemImpl.updateScheduleOpenitem(openItemReference.getOpenItem());
            }
        }
    }

    /**
     * fuction updatePaymentsOrders
     *
     * @param policy
     * @param claim
     * @param payment
     * @param state
     * @param thirdMovement
     * @param isReverse
     * @return Hashtable
     */
    private Hashtable updatePaymentsOrders(AgregatedPolicy policy, Payment payment,
                                           PaymentOrderStatus paymentOrderStatus,
                                           ClaimMovementBean thirdMovement,
                                           boolean isReverse) throws ApplicationExceptionChecked {
        log.debug("updatePaymentsOrders()");
        log.debug("payment = " + payment);
        log.debug("thirdMovement = " + thirdMovement);
        Hashtable details = new Hashtable();
        PaymentOrder paymentOrder = payment.getPaymentOrder();
//        Collection<PaymentOrder> paymentOrders = payment.getPaymentOrders();
//        for (PaymentOrder paymentOrder : paymentOrders) {
//            paymentOrder = PaymentOrder.load(new Long(paymentOrder.getPk()));
        if ((paymentOrder.isTotalPayment()
                && paymentOrderStatus == PaymentOrderStatus.CANCELED_STATE) ||
                (paymentOrderStatus == PaymentOrderStatus.CANCELED_STATE && isReverse)) {
            log.debug("updatePaymentsOrders: if Canceled payment....");
            paymentOrder.setPaymentOrderStatus(paymentOrderStatus);

            paymentOrder.setHistoryStatePaymentsFile();
            canceledPaymentOrders(paymentOrder);
        } else {
            if (paymentOrderStatus == PaymentOrderStatus.PAID_STATE) {
                double compensateableAmount;
                paymentOrder = (PaymentOrder) HibernateUtil.load(paymentOrder, paymentOrder.getPk());
                double invoiceCompensateable = ClaimUtil.getAmountInvoiceCompensateable(paymentOrder);

                compensateableAmount = paymentOrder.getAmount() == null ? invoiceCompensateable : paymentOrder.getAmount();
                Hashtable paymentOrderTaxes = calculateTaxesForRole(policy, paymentOrder.getThirdPartyRoleID());

                getDetailsForRole(policy, thirdMovement, paymentOrder.getThirdPartyRoleID(), compensateableAmount, details, paymentOrderTaxes);
            }

            //Do it, only if the paymentOrder has approved state or if the action is reverse.
            if (paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.APPROVED_STATE ||
                    (paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.PAID_STATE
                            && isReverse)) {
                paymentOrder.setPaymentOrderStatus(paymentOrderStatus);
                paymentOrder.setHistoryStatePaymentsFile(); // Saving State in the history payments order
            }

            paymentOrder.save();
        }
        payment.update();
        paymentOrder.update();
//        }
        return details;
    }

    /**
     * fuction updatePaymentsOrders
     *
     * @param policy
     * @param claim
     * @param payment
     * @param state
     * @param thirdMovement
     * @param isReverse
     * @return Hashtable
     */
    private Hashtable updatePaymentsOrders(AgregatedPolicy policy, Payment payment,
                                           PaymentOrderStatus paymentOrderStatus, ClaimMovementBean thirdMovement,
                                           boolean isReverse, List paymentOrdersToProcess) throws ApplicationExceptionChecked {
        Hashtable details = new Hashtable();
        PaymentOrder paymentOrder = payment.getPaymentOrder();
//        Collection<PaymentOrder> paymentOrders = payment.getPaymentOrders();
//        for (PaymentOrder paymentOrder : paymentOrders) {
//            paymentOrder = PaymentOrder.load(new Long(paymentOrder.getPk()));ClaimReserveByConcept
        if (paymentOrder.isTotalPayment() && paymentOrderStatus == PaymentOrderStatus.CANCELED_STATE) {
            log.debug("updatePaymentsOrders: if Canceled payment....");
            paymentOrder.setPaymentOrderStatus(paymentOrderStatus);

            paymentOrder.setHistoryStatePaymentsFile();
            if ((paymentOrder.getClaimReserve() instanceof ClaimNormalReserve))
                canceledPaymentOrders(paymentOrder);
        } else {
            if (paymentOrderStatus == PaymentOrderStatus.PAID_STATE) {
                double compensateableAmount;
                paymentOrder = PaymentOrderPersister.Impl.getInstance().load(paymentOrder.getPk());
                double invoiceCompensateable = ClaimUtil.getAmountInvoiceCompensateable(paymentOrder);

                compensateableAmount = paymentOrder.getAmount() == null ? invoiceCompensateable : paymentOrder.getAmountWithDeductible();
                Hashtable paymentOrderTaxes = calculateTaxesForRole(policy, paymentOrder.getThirdPartyRoleID());

                getDetailsForRole(policy, thirdMovement, paymentOrder.getThirdPartyRoleID(), compensateableAmount, details, paymentOrderTaxes);
            }

            //Do it, only if the paymentOrder has approved state or if the action is reverse.
//                log.debug("paymentOrder = " + paymentOrder);
//                log.debug("paymentOrder.getState() = " + paymentOrder.getState());
            if (paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.APPROVED_STATE ||
                    (paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.PAID_STATE
                            && isReverse)) {
                if (paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.APPROVED_STATE) {
                    paymentOrdersToProcess.add(paymentOrder);
                }

                paymentOrder.setPaymentOrderStatus(paymentOrderStatus);
                paymentOrder.setHistoryStatePaymentsFile(); // Saving State in the history payments order
            }
            payment.update();
            paymentOrder.update();
        }
//        }
        return details;
    }

    /**
     * @param policy
     * @param claim
     * @param payment
     * @param tercero
     * @param items
     * @throws Exception
     */
    private void createEntrys(AgregatedPolicy policy, Claim claim, Payment payment, ClaimMovementBean tercero, Hashtable items, String ruID,
                              String ioID,Collection uaaDetailByOpenItemId) throws Exception {
        createEntrys(policy, claim, payment, tercero, items, false, ruID, ioID, uaaDetailByOpenItemId);
    }

    /**
     * @param policy
     * @param claim
     * @param payment
     * @param tercero
     * @param items
     * @param createAsReverse
     * @throws Exception
     */
    private void createEntrys(AgregatedPolicy policy, Claim claim, Payment payment, ClaimMovementBean tercero, Hashtable items,
                              boolean createAsReverse, String ruID, String ioID, Collection uaaDetailByOpenItemId) throws Exception {
        SymbolsClaim entrys = new SymbolsClaim();
        TablaSimbolos table = new TablaSimbolos();

        publishSymbols(entrys, table, policy, claim, null);
        Currency currency = Currency.Impl.load(Long.valueOf(payment.getPaidCurrencyId()));

        //OpenItem - Symbols
        if(uaaDetailByOpenItemId != null){
            for (Iterator iterator = uaaDetailByOpenItemId.iterator(); iterator.hasNext(); ) {
                UaaDetail uaaDetail = (UaaDetail) iterator.next();
                if (!(uaaDetail.getProperty() == null) && !uaaDetail.getProperty().equals("")) {
                    Propiedad propiedad = PropiedadImpl.getInstanceTemplate(uaaDetail.getProperty());
                    entrys.addSymbol(table, propiedad.getSimbolo(), String.valueOf(uaaDetail.getValue()), new Double(uaaDetail.getValue()));
                }
            }
        }
        if (currency.getIsoCode() == null) {
            entrys.addSymbol(table, EvaluationConstants.CURRENCY_CODE, "", new Double(0.0));
        } else {
            entrys.addSymbol(table, EvaluationConstants.CURRENCY_CODE, currency.getIsoCode(),
                    Double.valueOf(currency.getId()));
        }
        entrys.addSymbol(table, EvaluationConstants.USER_LOGIN, payment.getDoneBy(), new Double(0.0));
        entrys.addSymbol(table, EvaluationConstants.CLAIM_PAYMENT_ID, String.valueOf(payment.getPk()), new Double(payment.getPk()));
        entrys.addSymbol(table, EvaluationConstants.CLAIM_PAYMENT_DATE, DateUtil.getDateToShow(payment.getDate()),
                new Double(Funciones.dateTransformer.toNumber(DateUtil.getDateToShow(payment.getDate()))));

        // Coverages
        try {
            if ((ruID != null) && (ioID != null)) {
                AgregatedInsuranceObject aio = getAgregatedInsuranceObjectById(ruID, ioID);
                List<EvaluatedCoverage> ecList = getEvaluatedCoverageList(aio);
                for (EvaluatedCoverage ec : ecList) {
                    entrys.addSymbols(ec.getDCO(), ec.getDesc(), table);
                    entrys.addSymbol(table, EvaluationConstants.PLAN_NAME, ec.getPlan().getDesc(), new Double(0));
                    log.debug(EvaluationConstants.PLAN_NAME + " =  " + ec.getPlan().getDesc());

                    entrys.addSymbol(table, AcseleConf.getProperty("policy.plan"),
                            ec.getPlan().getDCO().getCriterioInput(AcseleConf.getProperty("policy.plan")),
                            new Double(ec.getPlan().getDCO().getCriterioInput(AcseleConf.getProperty("policy.plan"))));
                    log.debug(AcseleConf.getProperty("policy.plan") + " =  " + ec.getPlan().getDCO()
                            .getCriterioInput(AcseleConf.getProperty("policy.plan")));

                    entrys.addSymbols(ec.getDCO(), table);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error publishing coverage's symbols: ", e);
        }

        // (GS) El siguiente bloque no est� en entryReserve().  Deber�a?
        //ThirdParty thirdParty = ThirdParty.getInstance(Long.valueOf(tercero.getThirdId()));
        com.consisint.acsele.thirdparty.api.ThirdParty thirdParty = com.consisint.acsele.thirdparty.api.ThirdParty.Impl.load(Long.parseLong(tercero.getThirdId()));
        Template template = thirdParty.getDynamicData().getMetaData().getTemplate();
        entrys.addSymbol(table, "RFC", String.valueOf(template.getId()), new Double(template.getId()));

        //DefaultConfigurableObject dco = thirdParty.getDynamic().getDCO();
        entrys.addSymbols(thirdParty.getDynamicData(), "Beneficiary", table);
        entrys.addSymbol(table, "PersonType", tercero.getPersonType(), new Double(0));
        if (items != null) {
            Enumeration keys = items.keys();
            while (keys.hasMoreElements()) {
                String label = (String) keys.nextElement();
//                log.debug(" label = " + label);
                ClaimDetailsPaymentBean claimDetailsPaymentBean = (ClaimDetailsPaymentBean) items.get(label);
                entrys.addSymbol(table, label, String.valueOf(claimDetailsPaymentBean.getAmount()), new Double(claimDetailsPaymentBean.getAmount()));
            }
        }


        if (payment.getReserveType() == ReserveType.CONCEPT_RESERVE.getValue()) {
            PaymentOrder paymentOrder = payment.getPaymentOrder();
            ReserveConcept concept = ((ClaimReserveByConcept) paymentOrder.getClaimReserve()).getReserveConcept();

            entrys.addSymbol(table, EvaluationConstants.RESERVE_CONCEPT, concept.getConcept(), new Double(concept.getPk()));

            for (ThirdPartyRole thirdPartyRole : thirdParty.getThirdPartyRoleList()) {
                if(thirdPartyRole.getRole().getId() == paymentOrder.getThirdPartyRoleID()){
                    entrys.addSymbols(thirdPartyRole.getDynamicData(),thirdPartyRole.getRole().getDescription(), table);

                    entrys.addSymbol(table,thirdPartyRole.getRole().getDescription()+"_"+EvaluationConstants.THIRPARTY_PERSON_TYPE, template.getName(), new Double(template.getId()));

                    List<ThirdPartyFinancialDetail> thirdPartyFinancialDetailList = thirdParty.getThirdPartyFinancialDetailList().getAll();
                    if (!thirdPartyFinancialDetailList.isEmpty()) {
                        ThirdPartyFinancialDetail thirdPartyFinancialDetail = thirdPartyFinancialDetailList.get(0);
                        String thirdType = thirdPartyFinancialDetail.getThirdParty().getEnumTypeThirdParty().getDesc();
                        String prefix = thirdPartyRole.getRole().getDescription() + (thirdType.equals(AcseleConf.getProperty("Moralperson")) ?
                                EvaluationConstants.LEGAL_FINANCIAL_INFORMATION_PREF : EvaluationConstants.NATURAL_FINANCIAL_INFORMATION_PREF);
                        entrys.addSymbols(thirdPartyFinancialDetail.getDynamicData(), prefix, table);
                    }

                    break;
                }
            }
        }


        entrys.addSymbol(table, "ClaimAmount", String.valueOf(tercero.getAmount()), new Double(tercero.getAmount()));
        entrys.addSymbol(table, "ThirdpartyName", tercero.getThirdPartyName(), new Double(0));
        entrys.addSymbol(table, "ThirdpartyID", tercero.getThirdId(), new Double(0));
        entrys.addSymbol(table, "ClaimPk", claim.getPK(), new Double(0));

        entrys.addSymbol(table, "isReversal", String.valueOf(createAsReverse), new Double(0));

        entrys.addSymbols(policy.getProduct().getDCO(), table);
        String ocurrenceDateStr = DateUtil.getDateToShow(getOcurrenceDate());
        entrys.addSymbol(table, EvaluationConstants.OCURRENCE_DATE, ocurrenceDateStr,
                new Double(Funciones.dateTransformer.toNumber(ocurrenceDateStr)));

        if (payment.getPaymentOrder() != null && payment.getPaymentOrder().getClaimReserve() != null) {
            ClaimReserve claimReserve = payment.getPaymentOrder().getClaimReserve();
            String claimReserveDate = DateUtil.getDateToShow(claimReserve.getDate());
            entrys.addSymbol(table, EvaluationConstants.CLAIM_RESERVE_DATE, claimReserveDate, Funciones.dateTransformer.toNumber(claimReserveDate));
        }

        entrys.setPaymentID(payment.getPk());
        if (payment.getReserveType() == ReserveType.CONCEPT_RESERVE.getValue()) {
            boolean existConfiguration = SymbolsCashier.existCOTsForGenerateEntry(GroupOperationType.SINISTER, OperationType.SINI_PAYMENT_REVERSE_EXPENSES, Long.valueOf(policy.getIDProduct()), 0, 0, 0);
            int operationId = createAsReverse && existConfiguration ? OperationType.SINI_PAYMENT_REVERSE_EXPENSES : OperationType.SINI_PAYMENT_EXPENSES;
            if (createAsReverse) {
                GroupOperationType groupOpEntrys = new GroupOperationType(GroupOperationType.SINISTER);
                entrys.addSymbol(table, EvaluationConstants.PAYMENT_TYPE_REVERSED, groupOpEntrys.getOpDescByCode(operationId), new Double(0));
                log.debug(EvaluationConstants.PAYMENT_TYPE_REVERSED + " =  " + groupOpEntrys.getOpDescByCode(operationId));
                entrys.addSymbol(table, EvaluationConstants.REVERSED_AMOUNT, String.valueOf(payment.getAmount()), payment.getAmount());
                log.debug(EvaluationConstants.REVERSED_AMOUNT + " =  " + payment.getAmount());

            }
            entrys.createEntrys(operationId, Long.valueOf(policy.getIDProduct()), table, createAsReverse);
        }
    }

    /**
     * @param policy
     * @param claim
     * @param paymentOrder
     * @param tercero
     * @param items
     * @throws Exception
     */
    private void createEntrys(AgregatedPolicy policy, Claim claim, Payment payment, PaymentOrder paymentOrder, ClaimMovementBean tercero,
                              Hashtable items, String ruID, String ioID, ClaimReserve claimReserve) throws Exception {
        createEntrys(policy, claim, payment, paymentOrder, tercero, items, false, ruID, ioID, claimReserve);
    }

    /**
     * @param policy
     * @param claim
     * @param paymentOrder
     * @param tercero
     * @param items
     * @param createAsReverse
     * @throws Exception
     */
    private void createEntrys(AgregatedPolicy policy, Claim claim, Payment payment, PaymentOrder paymentOrder, ClaimMovementBean tercero,
                              Hashtable items, boolean createAsReverse, String ruID, String ioID, ClaimReserve claimReserve) throws Exception {

        SymbolsClaim entrys = new SymbolsClaim();
        TablaSimbolos table = new TablaSimbolos();
        String coverageDesc = paymentOrder.getCoverageDesc(); // TODO: Si llega en blanco, es Otros Conceptos?

        long coverageID = 0;
        long planID = 0;
        log.debug("*** coverageDesc : " + coverageDesc);

        publishSymbols(entrys, table, policy, claim, null);
        Currency currency = Currency.Impl.load(Long.valueOf(payment.getPaidCurrencyId()));



        if (currency.getIsoCode() == null) {
            entrys.addSymbol(table, EvaluationConstants.CURRENCY_CODE, "", new Double(0.0));
        } else {
            entrys.addSymbol(table, EvaluationConstants.CURRENCY_CODE, currency.getIsoCode(),
                    Double.valueOf(currency.getId()));
        }
        entrys.addSymbol(table, EvaluationConstants.USER_LOGIN, payment.getDoneBy(), new Double(0.0));

        String userWhoApprove = getUserWhoApprovePayment(paymentOrder.getPK());

        log.debug("****[PMM]**** Usuario que Aprob� el Payment Order: " + userWhoApprove);
        //New Symbol @UserAction Sup626
        entrys.addSymbol(table, EvaluationConstants.USER_ACTION, userWhoApprove, new Double(0));

        entrys.addSymbol(table, EvaluationConstants.CLAIM_PAYMENT_ID, String.valueOf(payment.getPk()), new Double(payment.getPk()));

        log.debug(EvaluationConstants.CLAIM_PAYMENT_ID + " =  " + payment.getPk());

        entrys.addSymbol(table, EvaluationConstants.CLAIM_PAYMENT_DATE, DateUtil.getDateToShow(payment.getDate()),
                new Double(Funciones.dateTransformer.toNumber(DateUtil.getDateToShow(payment.getDate()))));
        log.debug(EvaluationConstants.CLAIM_PAYMENT_DATE + " =  " + DateUtil.getDateToShow(payment.getDate()));
        entrys.addSymbols(policy.getProduct().getDCO(), table);

        String ocurrenceDateStr = DateUtil.getDateToShow(getOcurrenceDate());
        entrys.addSymbol(table, EvaluationConstants.OCURRENCE_DATE, ocurrenceDateStr,
                new Double(Funciones.dateTransformer.toNumber(ocurrenceDateStr)));

        log.debug(EvaluationConstants.OCURRENCE_DATE + " =  " + ocurrenceDateStr);

        // Coverages
        try {
            if ((ruID != null) && (ioID != null)) {
                AgregatedInsuranceObject aio = getAgregatedInsuranceObjectById(ruID, ioID);
                List<EvaluatedCoverage> ecList = getEvaluatedCoverageList(aio);
                entrys.addSymbol(table, EvaluationConstants.INSURANCE_OBJECT_NAME, aio.getDesc(), new Double(0));
                log.debug(EvaluationConstants.INSURANCE_OBJECT_NAME + " =  " + aio.getDesc());


                for (EvaluatedCoverage ec : ecList) {
                    entrys.addSymbols(ec.getDCO(), ec.getDesc(), table);
                    if (ec.getDesc().equalsIgnoreCase(coverageDesc)) {
                        entrys.addSymbol(table, EvaluationConstants.PLAN_NAME, ec.getPlan().getDesc(), new Double(0));
                        log.debug(EvaluationConstants.PLAN_NAME + " =  " + ec.getPlan().getDesc());
                        entrys.addSymbol(table, AcseleConf.getProperty("policy.plan"),
                                ec.getPlan().getDCO().getCriterioInput(AcseleConf.getProperty("policy.plan")),
                                new Double(ec.getPlan().getDCO().getCriterioInput(AcseleConf.getProperty("policy.plan"))));
                        log.debug(AcseleConf.getProperty("policy.plan") + " =  " + ec.getPlan().getDCO()
                                .getCriterioInput(AcseleConf.getProperty("policy.plan")));
                        coverageID = ec.getConfiguratedCoverageOA().getId();
                        planID = Long.valueOf(ec.getPlan().getPk());
                        entrys.addSymbols(ec.getDCO(), table);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error publishing coverage's symbols: ", e);
        }

        // (GS) El siguiente bloque no est� en entryReserve().  Deberia?
        ThirdParty thirdParty = ThirdParty.getInstance(Long.valueOf(tercero.getThirdId()));
        entrys.addSymbol(table, "RFC", String.valueOf(thirdParty.getStatic().getCotID()), new Double(0));

        DefaultConfigurableObject dco = thirdParty.getDynamic().getDCO();
        entrys.addSymbols(dco.getInput(), dco.getValues(), "Beneficiary", table);
        entrys.addSymbol(table, "PersonType", tercero.getPersonType(), new Double(0));
        if (items != null) {
            Enumeration keys = items.keys();
            while (keys.hasMoreElements()) {
                String label = (String) keys.nextElement();
                ClaimDetailsPaymentBean claimDetailsPaymentBean = (ClaimDetailsPaymentBean) items.get(label);
                entrys.addSymbol(table, label, String.valueOf(claimDetailsPaymentBean.getAmount()), new Double(claimDetailsPaymentBean.getAmount()));
            }
        }

        entrys.addSymbol(table, "ThirdpartyID", tercero.getThirdId(), new Double(0));
        //VT - 8/30/2007
        entrys.addSymbol(table, EvaluationConstants.IS_RESERVAL, String.valueOf(createAsReverse), new Double(0));

        entrys.addSymbol(table, EvaluationConstants.CLAIM_AMOUNT, String.valueOf(tercero.getAmount()), new Double(tercero.getAmount()));
        log.debug(EvaluationConstants.CLAIM_AMOUNT + " =  " + tercero.getAmount());

        entrys.addSymbol(table, EvaluationConstants.BENEFICIARY_NAME, tercero.getThirdPartyName(), new Double(0));
        log.debug(EvaluationConstants.BENEFICIARY_NAME + " =  " + tercero.getThirdPartyName());

        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_AMOUNT, String.valueOf(paymentOrder.getAmount()), paymentOrder.getAmount());
        entrys.addSymbol(table, StringUtil.isEmptyOrNullValue(AcseleConf.getProperty("Monto")) ? "Amount" : AcseleConf.getProperty("Monto"),
                String.valueOf(paymentOrder.getAmount()), paymentOrder.getAmount());
        log.debug(EvaluationConstants.PAYMENT_ORDER_AMOUNT + " =  " + paymentOrder.getAmount());
        //entrys.setCriterioInputAndValue(AcseleConf.getProperty("Monto"), paymentOrder.getAmount(), paymentOrder.getAmount());
        Double deductibleContable = paymentOrder.getDeductibleContable();
        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_DEDUCTIBLE_AMOUNT, deductibleContable.toString(), deductibleContable);
        log.debug(EvaluationConstants.PAYMENT_ORDER_DEDUCTIBLE_AMOUNT + " =  " + deductibleContable);

        Double deductibleEditContable = paymentOrder.getDeductibleAmount();
        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_EDIT_DEDUCTIBLE_AMOUNT, deductibleEditContable.toString(), deductibleEditContable);

        long paymentOrderPk = paymentOrder.getPk();
        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_PK, String.valueOf(paymentOrderPk), new Double(paymentOrderPk));
        log.debug(EvaluationConstants.PAYMENT_ORDER_PK + " =  " + paymentOrderPk);

        double retentionAmount = paymentOrder.getRetentionAmount();
        entrys.addSymbol(table, EvaluationConstants.PAYMENT_RETENTION_AMOUNT, String.valueOf(retentionAmount), new Double(retentionAmount));
        log.debug(EvaluationConstants.PAYMENT_RETENTION_AMOUNT + " =  " + retentionAmount);

        double retentionType = paymentOrder.getDeductibleType();
        entrys.addSymbol(table, EvaluationConstants.PAYMENT_RETENTION_TYPE, String.valueOf(retentionType), new Double(retentionType));
        log.debug(EvaluationConstants.PAYMENT_RETENTION_TYPE + " =  " + retentionType);

        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_COMMITMENT_DATE, DateUtil.getDateToShow(paymentOrder.getCommitmentDate()),
                Funciones.dateTransformer.toNumber(DateUtil.getDateToShow(paymentOrder.getCommitmentDate())));
        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_END_DATE, DateUtil.getDateToShow(paymentOrder.getEndDate()),
                Funciones.dateTransformer.toNumber(DateUtil.getDateToShow(paymentOrder.getEndDate())));


        log.debug(EvaluationConstants.PAYMENT_ORDER_COMMITMENT_DATE + " =  " + DateUtil.getDateToShow(paymentOrder.getCommitmentDate()));
        log.debug(EvaluationConstants.PAYMENT_ORDER_END_DATE + " =  " + DateUtil.getDateToShow(paymentOrder.getEndDate()));

        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_CREATION_DATE, DateUtil.getDateToShow(paymentOrder.getDate()), new Double(0));
        log.debug(EvaluationConstants.PAYMENT_ORDER_CREATION_DATE + " =  " + DateUtil.getDateToShow(paymentOrder.getDate()));

        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_REASON, String.valueOf(paymentOrder.getReason()), new Double(0));
        log.debug(EvaluationConstants.PAYMENT_ORDER_REASON + " =  " + paymentOrder.getReason());

        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_TYPE, String.valueOf(paymentOrder.getType()), new Double(0));
        log.debug(EvaluationConstants.PAYMENT_ORDER_TYPE + " =  " + paymentOrder.getType());

        entrys.addSymbol(table, EvaluationConstants.COVERAGE_NAME, String.valueOf(coverageDesc), new Double(0));
        log.debug(EvaluationConstants.COVERAGE_NAME + " =  " + coverageDesc);
        if (createAsReverse) {
            entrys.addSymbol(table, EvaluationConstants.REVERSED_AMOUNT, String.valueOf(paymentOrder.getAmount()), paymentOrder.getAmount());
            log.debug(EvaluationConstants.REVERSED_AMOUNT + " =  " + paymentOrder.getAmount());
        }
        List<CoverageInvoice> invoiceList = CoverageInvoice.Impl.loadInvoiceByPaymentOrder(paymentOrderPk);
        if (invoiceList.size() > 0) {
            CoverageInvoice coverageInvoice = invoiceList.get(0);
            DynamicData coverageInvoiceDynamicData = coverageInvoice.getDynamicData();
            if (coverageInvoiceDynamicData != null) {
                entrys.addSymbols(coverageInvoiceDynamicData, table);
            }

            DynamicData taxesDynamicData = coverageInvoice.getTaxDynamicData();
            if (taxesDynamicData != null) {
                entrys.addSymbols(taxesDynamicData, table);
            }
        }

        entrys.setPaymentID(payment.getPk());//todo preguntar para que es esto!!!

        log.debug("************************** createEntrys(AgregatedPolicy policy, Claim claim, Payment payment, PaymentOrder paymentOrder, ClaimMovementBean tercero,Hashtable items, boolean createAsReverse, String ruID, String ioID, ClaimReserve claimReserve)");

        if (claimReserve == null) {  //Obtener el claimNormalReserve del Pago para garantizar que también se generen los entries cuando sea un pago de reserva por concepto
            claimReserve = paymentOrder.getClaimReserve();
        }
        DefaultConfigurableObject dco2 = policy.getAgregatedPolicy().getDCO();
        entrys.addSymbols(dco2, table);
        int daysAccumlated = calculateDaysAccumated(claimReserve.getPaymentOrderList());
        entrys.addSymbol(table, EvaluationConstants.DAYS_ACCUMULATED, String.valueOf(daysAccumlated), Double.valueOf(daysAccumlated));

        String claimReserveDate = DateUtil.getDateToShow(claimReserve.getDate());
        entrys.addSymbol(table, EvaluationConstants.CLAIM_RESERVE_DATE, claimReserveDate, Funciones.dateTransformer.toNumber(claimReserveDate));
        GroupOperationType groupOpEntrys = new GroupOperationType(GroupOperationType.SINISTER);

        if (claimReserve instanceof ClaimNormalReserve) {

            boolean existConfiguration = SymbolsCashier.existCOTsForGenerateEntry(GroupOperationType.SINISTER, OperationType.SINI_PAYMENT_REVERSE_COVERAGE, Long.valueOf(policy.getIDProduct()), 0, 0, 0);
            int operationId = createAsReverse && existConfiguration ? OperationType.SINI_PAYMENT_REVERSE_COVERAGE : OperationType.SINI_PAYMENT_COVERAGE;
            EvaluatedCoverage evc = ((ClaimNormalReserve) claimReserve).getEvaluatedCoverage();
            if (createAsReverse) {
                entrys.addSymbol(table, EvaluationConstants.PAYMENT_TYPE_REVERSED, groupOpEntrys.getOpDescByCode(operationId), new Double(0));
                log.debug(EvaluationConstants.PAYMENT_TYPE_REVERSED + " =  " + groupOpEntrys.getOpDescByCode(operationId));
            }
            entrys.createEntrys(operationId, policy.getIDProduct() != null ? Long.valueOf(policy.getIDProduct()) : 0, evc.getPlan().getId(), Long.parseLong(evc.getConfiguratedCoverage().getPK()), table, createAsReverse);
        } else if (claimReserve instanceof ClaimReserveByConcept) {//Reserva de gatos

            entrys.createEntrys(OperationType.SINI_RESERVE_EXPENSES, policy.getIDProduct() != null ? Long.valueOf(policy.getIDProduct()) : 0, planID, coverageID, table, createAsReverse);
        }
        log.debug("createEntrys(AgregatedPolicy policy, Claim claim, Payment payment, PaymentOrder paymentOrder, ClaimMovementBean tercero,Hashtable items, boolean createAsReverse, String ruID, String ioID, ClaimReserve claimReserve) ************************************ ");
    }


    /**
     * Create entries in the approvement of a payment order
     * @param policy
     * @param claim
     * @param paymentOrder
     * @param ruID
     * @param ioID
     * @param claimReserve
     * @param createAsReserve
     * @throws Exception
     */
    public void createEntrys(AgregatedPolicy policy, Claim claim, PaymentOrder paymentOrder, String ruID, String ioID, ClaimReserve claimReserve, boolean createAsReserve) throws Exception {

        SymbolsClaim entrys = new SymbolsClaim();
        TablaSimbolos table = new TablaSimbolos();
        String coverageDesc = paymentOrder.getCoverageDesc();

        long coverageID = 0;
        long planID = 0;
        log.debug("*** coverageDesc : " + coverageDesc);

        publishSymbols(entrys, table, policy, claim, null);
        String userWhoApprove = getUserWhoApprovePayment(paymentOrder.getPK());
        log.debug("****[PMM]**** Usuario que Aprob� el Payment Order: " + userWhoApprove);

        entrys.addSymbol(table, EvaluationConstants.USER_ACTION, userWhoApprove, 0d);
        entrys.addSymbols(policy.getProduct().getDCO(), table);

        String ocurrenceDateStr = DateUtil.getDateToShow(getOcurrenceDate());
        entrys.addSymbol(table, EvaluationConstants.OCURRENCE_DATE, ocurrenceDateStr, Funciones.dateTransformer.toNumber(ocurrenceDateStr));

        log.debug(EvaluationConstants.OCURRENCE_DATE + " =  " + ocurrenceDateStr);

        // Coverages
        try {
            if ((ruID != null) && (ioID != null)) {
                AgregatedInsuranceObject aio = getAgregatedInsuranceObjectById(ruID, ioID);
                List<EvaluatedCoverage> ecList = getEvaluatedCoverageList(aio);
                entrys.addSymbol(table, EvaluationConstants.INSURANCE_OBJECT_NAME, aio.getDesc(), new Double(0));
                log.debug(EvaluationConstants.INSURANCE_OBJECT_NAME + " =  " + aio.getDesc());

                for(EvaluatedCoverage ec: ecList){
                    entrys.addSymbols(ec.getDCO(), ec.getDesc(), table);
                    if (ec.getDesc().equalsIgnoreCase(coverageDesc)) {
                        entrys.addSymbol(table, EvaluationConstants.PLAN_NAME, ec.getPlan().getDesc(), new Double(0));
                        log.debug(EvaluationConstants.PLAN_NAME + " =  " + ec.getPlan().getDesc());
                        entrys.addSymbol(table, AcseleConf.getProperty("policy.plan"),
                                ec.getPlan().getDCO().getCriterioInput(AcseleConf.getProperty("policy.plan")),
                                new Double(ec.getPlan().getDCO().getCriterioInput(AcseleConf.getProperty("policy.plan"))));
                        log.debug(AcseleConf.getProperty("policy.plan") + " =  " + ec.getPlan().getDCO()
                                .getCriterioInput(AcseleConf.getProperty("policy.plan")));
                        coverageID = ec.getConfiguratedCoverageOA().getId();
                        planID = Long.valueOf(ec.getPlan().getPk());
                        entrys.addSymbols(ec.getDCO(), table);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error publishing coverage's symbols: ", e);
        }

        ThirdParty thirdParty = paymentOrder.getThirdParty();
        entrys.addSymbol(table, "RFC", String.valueOf(thirdParty.getStatic().getCotID()), 0d);

        DefaultConfigurableObject dco = thirdParty.getDynamic().getDCO();
        entrys.addSymbols(dco.getInput(), dco.getValues(), "Beneficiary", table);

        entrys.addSymbol(table, "ThirdpartyID", thirdParty.getPK(), 0d);
        entrys.addSymbol(table, EvaluationConstants.BENEFICIARY_NAME, thirdParty.getName(), 0d);
        log.debug(EvaluationConstants.BENEFICIARY_NAME + " =  " + thirdParty.getName());

        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_AMOUNT, String.valueOf(paymentOrder.getAmount()), paymentOrder.getAmount());
        entrys.addSymbol(table, StringUtil.isEmptyOrNullValue(AcseleConf.getProperty("Monto")) ? "Amount" : AcseleConf.getProperty("Monto"),
                String.valueOf(paymentOrder.getAmount()), paymentOrder.getAmount());
        log.debug(EvaluationConstants.PAYMENT_ORDER_AMOUNT + " =  " + paymentOrder.getAmount());

        Double deductibleContable = paymentOrder.getDeductibleContable();
        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_DEDUCTIBLE_AMOUNT, deductibleContable.toString(), deductibleContable);
        log.debug(EvaluationConstants.PAYMENT_ORDER_DEDUCTIBLE_AMOUNT + " =  " + deductibleContable);

        long paymentOrderPk = paymentOrder.getPk();
        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_PK, String.valueOf(paymentOrderPk), (double) paymentOrderPk);
        log.debug(EvaluationConstants.PAYMENT_ORDER_PK + " =  " + paymentOrderPk);

        double retentionAmount = paymentOrder.getRetentionAmount();
        entrys.addSymbol(table, EvaluationConstants.PAYMENT_RETENTION_AMOUNT, String.valueOf(retentionAmount), retentionAmount);
        log.debug(EvaluationConstants.PAYMENT_RETENTION_AMOUNT + " =  " + retentionAmount);

        double retentionType = paymentOrder.getDeductibleType();
        entrys.addSymbol(table, EvaluationConstants.PAYMENT_RETENTION_TYPE, String.valueOf(retentionType), retentionType);
        log.debug(EvaluationConstants.PAYMENT_RETENTION_TYPE + " =  " + retentionType);

        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_COMMITMENT_DATE, DateUtil.getDateToShow(paymentOrder.getCommitmentDate()), 0d);
        log.debug(EvaluationConstants.PAYMENT_ORDER_COMMITMENT_DATE + " =  " + DateUtil.getDateToShow(paymentOrder.getCommitmentDate()));

        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_CREATION_DATE, DateUtil.getDateToShow(paymentOrder.getDate()),0d);
        log.debug(EvaluationConstants.PAYMENT_ORDER_CREATION_DATE + " =  " + DateUtil.getDateToShow(paymentOrder.getDate()));

        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_REASON, String.valueOf(paymentOrder.getReason()), 0d);
        log.debug(EvaluationConstants.PAYMENT_ORDER_REASON + " =  " + paymentOrder.getReason());

        entrys.addSymbol(table, EvaluationConstants.PAYMENT_ORDER_TYPE, String.valueOf(paymentOrder.getType()), 0d);
        log.debug(EvaluationConstants.PAYMENT_ORDER_TYPE + " =  " + paymentOrder.getType());

        entrys.addSymbol(table, EvaluationConstants.COVERAGE_NAME, String.valueOf(coverageDesc), 0d);
        log.debug(EvaluationConstants.COVERAGE_NAME + " =  " + coverageDesc);

        List<CoverageInvoice> invoiceList = CoverageInvoice.Impl.loadInvoiceByPaymentOrder(paymentOrderPk);

        if (claimReserve == null) {  //Obtener el claimNormalReserve del Pago para garantizar que también se generen los entries cuando sea un pago de reserva por concepto
            claimReserve = paymentOrder.getClaimReserve();
        }
        log.debug("## claimReserve.getPaymentOrderList()=" + claimReserve.getPaymentOrderList());
        int daysAccumlated=calculateDaysAccumated(claimReserve.getPaymentOrderList());
        entrys.addSymbol(table, EvaluationConstants.DAYS_ACCUMULATED, String.valueOf(daysAccumlated), (double) daysAccumlated);
        log.debug("## daysAccumlated=" + daysAccumlated);

        for (CoverageInvoice coverageInvoice : invoiceList){
            DynamicData coverageInvoiceDynamicData = coverageInvoice.getDynamicData();
            if (coverageInvoiceDynamicData != null) {
                entrys.addSymbols(coverageInvoiceDynamicData, table);
            }

            DynamicData taxesDynamicData = coverageInvoice.getTaxDynamicData();
            if (taxesDynamicData != null) {
                entrys.addSymbols(taxesDynamicData, table);
            }

            if (claimReserve instanceof ClaimNormalReserve) {
                EvaluatedCoverage evc = ((ClaimNormalReserve) claimReserve).getEvaluatedCoverage();
                entrys.createEntrys(5, Long.valueOf(policy.getIDProduct()), evc.getPlan().getId(), Long.parseLong(evc.getConfiguratedCoverage().getPK()), table, createAsReserve);
            } else if (claimReserve instanceof ClaimReserveByConcept) {
                entrys.createEntrys(6, Long.valueOf(policy.getIDProduct()), planID, coverageID, table, createAsReserve);
            }
        }

//        if (invoiceList.size() > 0) {
//            CoverageInvoice coverageInvoice = invoiceList.get(0);
//            DynamicData coverageInvoiceDynamicData = coverageInvoice.getDynamicData();
//            if (coverageInvoiceDynamicData != null) {
//                entrys.addSymbols(coverageInvoiceDynamicData, table);
//            }
//
//            DynamicData taxesDynamicData = coverageInvoice.getTaxDynamicData();
//            if (taxesDynamicData != null) {
//                entrys.addSymbols(taxesDynamicData, table);
//            }
//        }

//        if (claimReserve instanceof ClaimNormalReserve) {
//            EvaluatedCoverage evc = ((ClaimNormalReserve) claimReserve).getEvaluatedCoverage();
//            entrys.createEntrys(5, Long.valueOf(policy.getIDProduct()), evc.getPlan().getId(), Long.parseLong(evc.getConfiguratedCoverage().getPK()), table, createAsReserve);
//        } else if (claimReserve instanceof ClaimReserveByConcept) {
//            entrys.createEntrys(6, Long.valueOf(policy.getIDProduct()), planID, coverageID, table, createAsReserve);
//        }
    }

    //This method returns the user making the approval of the payment order. Sup626

    private String getUserWhoApprovePayment(String paymentsId) throws SQLException {
        Session session = null;
        String result = null;
        int claimstate = PaymentOrderStatus.APPROVED_STATE.getValue();
        try {
            session = HibernateUtil.getSession();
            Query query = session.getNamedQuery("claimstate.loadUserApproveClaimPayments");
            query.setString(0, paymentsId);
            query.setInteger(1, claimstate);
            result = (String) query.uniqueResult();
            log.debug("***[PMM]*** El Resultado del Query es: " + result);
        } catch (Exception e) {
            log.debug("Some problems loading user who approve the payment");
//        } finally {
//            HibernateUtil.closeSession(session);
        }
        return result;
    }

    /**
     * @param paymentOrder
     */
    private void canceledPaymentOrders(PaymentOrder paymentOrder) {
        if (paymentOrder != null) {

            ClaimReserve cnr = (ClaimReserve) paymentOrder.getClaimReserve();
            //  ClaimReserveByConcept cp = (ClaimReserveByConcept) paymentOrder.getClaimReserve();

            cnr.load();
            //   log.debug("Paso el Load " + cnr.getDesc() + "******" + cnr.getEvaluatedCoverage().getPk());
            ClaimReserveAdjust adjustSystem = cnr.searchSystemAdjust();
            log.debug("[canceledPaymentOrders] adjustSystem " + adjustSystem);
            if (adjustSystem != null) {
                log.debug("Adjust " + adjustSystem.getPK());
                adjustSystem.setState(ClaimReserveAdjust.CANCEL);
                adjustSystem.update();
                ClaimInsuranceObject cio = cnr.getContainer();
                cancelAdjustOthers(cio);
            }
            paymentOrder.update();
        }
    }

    /**
     * @param policy
     * @param tercero
     * @param roleId
     * @param compensateableAmount
     * @param detailsMovements
     * @param taxesList
     * @throws ApplicationExceptionChecked
     */
    public void getDetailsForRole(AgregatedPolicy policy, ClaimMovementBean tercero, int roleId, double compensateableAmount,
                                  Hashtable detailsMovements, Map taxesList) throws ApplicationExceptionChecked {
        com.consisint.acsele.uaa.api.Role roleImpl = com.consisint.acsele.uaa.api.Role.Impl.load(roleId);
        log.debug("roleImpl" + roleImpl.getDescription());
        AgregatedRole role = (AgregatedRole) policy.getProduct().getAgregatedRolesLevelPolicy()
                .get(roleImpl.getDescription());
        if (role != null) {
//            role.load();
            ConfiguratedOperation confTax = role.getClaimTaxDistribution();
            if ((confTax == null) || (confTax.getConfigurableObject() == null)) {
                return;
            }
            ConfigurableObjectType operationType = confTax.getConfigurableObject();
//            log.debug("operationType " + operationType);
            operationType.load();
            DefaultConfigurableObject operation = DefaultConfigurableObject.loadByDefault(operationType);
            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            try {
                calculate(operation, evaluator, tercero, compensateableAmount, detailsMovements, taxesList);
            } catch (Exception ex) {
                log.error("Error", ex);
            }
        }
    }

    /**
     * @param cio
     */
    private void cancelAdjustOthers(ClaimInsuranceObject cio) {
        cio.load();
        if (cio.getNormalReserves() != null && !cio.getNormalReserves().isEmpty()) {
            Iterator claimReserves = cio.getNormalReserves().values().iterator();
            while (claimReserves.hasNext()) {
                ClaimNormalReserve cnr = (ClaimNormalReserve) claimReserves.next();
                cnr.load();
                ClaimReserveAdjust adjust = cnr.searchSystemAdjust();
                log.debug("Adjust Other CNR" + adjust);
                if (adjust != null) {
                    log.debug("Adjust pk" + adjust.getPK());
                    adjust.setState(ClaimReserveAdjust.CANCEL);
                    adjust.update();
                }
            }
        }
        if (cio.getReservesByConcept() != null && !cio.getReservesByConcept().isEmpty()) {
            Iterator claimReserves = cio.getReservesByConcept().values().iterator();
            while (claimReserves.hasNext()) {
                ClaimReserveByConcept cnr = (ClaimReserveByConcept) claimReserves.next();
                cnr.load();
                ClaimReserveAdjust adjust = cnr.searchSystemAdjust();
                log.debug("Adjust Other CNR" + adjust);
                if (adjust != null) {
                    log.debug("Adjust pk" + adjust.getPK());
                    adjust.setState(ClaimReserveAdjust.CANCEL);
                    adjust.update();
                }
            }
        }
    }

    /**
     * @param operation
     * @param evaluator
     * @param tercero
     * @param compensateableAmount
     * @param detailsMovements
     * @param taxesList
     * @throws Exception
     */
    public void calculate(DefaultConfigurableObject operation, ExpresionEvaluator evaluator, ClaimMovementBean tercero, double compensateableAmount,
                          Hashtable detailsMovements, Map taxesList) throws Exception {

        Vector vector = operation.getCOT().getPropiedades();
        //Vector openItemsColumns = new Vector();
        for (int i = 0; i < vector.size(); i++) {
            PropiedadImpl property = (PropiedadImpl) vector.elementAt(i);
            //conceptProperty.load();
            tercero.setDocTypeID((int) DocType.Impl.load(property.getConcept()).getId());
            UAADetailType uaaDetailType = property.getUAADetailType();
            if (uaaDetailType != null) {
                log.debug("uaaDetailType.size()=" + uaaDetailType.size());
                if (uaaDetailType.size() > 0) {
                    evaluateDetailsFormula(uaaDetailType, evaluator, compensateableAmount, detailsMovements, taxesList);
                }
            }
        }
    }

    /**
     * @param uaaDetailType
     * @param evaluator
     * @param amountPayment
     * @param detailsMovements
     * @param taxesList
     * @throws Exception
     */
    private void evaluateDetailsFormula(UAADetailType uaaDetailType, ExpresionEvaluator evaluator, double amountPayment, Hashtable detailsMovements,
                                        Map taxesList) throws Exception {
        double detailValue;
        DefaultConfigurableObject dco_uaaDetailType = DefaultConfigurableObject.loadByDefault(uaaDetailType.getConfigurableObject());
        dco_uaaDetailType.evaluate(evaluator);
        Vector details = uaaDetailType.getPropiedades();

        boolean continueDetailIteration = false;

        for (int j = 0; j < details.size(); j++) {
            Propiedad detail = (Propiedad) details.elementAt(j);

            //debo iterar por el Map de taxes, para buscar cuales impuestos estan aplicados
            // y cuales no...
            Iterator taxesListEnum = taxesList.values().iterator();
            while (taxesListEnum.hasNext()) {
                PaymentOrderTax tax = (PaymentOrderTax) taxesListEnum.next();
                if (tax.getName().equals(detail.getDesc()) && !tax.isTaxApplied()) {
                    continueDetailIteration = true;
                    break;
                }
            }
            /*
            si existe un impuesto asociado al rol, pero no aplicado a la orden de pago,
            no lo env�o al UAA, sigo en la siguiente iteraci�n...
            */
            if (continueDetailIteration) {
                //reseteo el flag....
                continueDetailIteration = false;
                continue;
            }

            detailValue = evaluator.get(uaaDetailType.getDesc() + "_" + detail.getDesc());
            double retention = detailValue * amountPayment;
            log.debug("detail.getDesc(): " + detail.getDesc());
            log.debug("detailValue " + detailValue);
            if ((detail.getDesc() != null) && (detailsMovements.containsKey(detail.getDesc()))) {
                ClaimDetailsPaymentBean claimDetailsPaymentBean = (ClaimDetailsPaymentBean) detailsMovements.get(detail.getDesc());
                retention += claimDetailsPaymentBean.getAmount();
                log.debug("Duplicate Details " + detail.getDesc() + " amount: " + retention);
                claimDetailsPaymentBean.setAmount(retention);
                detailsMovements.put(detail.getDesc(), claimDetailsPaymentBean);
            } else {
                log.debug("Create Details: " + detail.getDesc() + " amount: " + retention);
                ClaimDetailsPaymentBean claimDetailsPaymentBean = new ClaimDetailsPaymentBean(detail.getPK(), detail.getDesc(), retention);
                detailsMovements.put(detail.getDesc(), claimDetailsPaymentBean);
            }
        }
    }


    /**
     * This method will be used if the reversal of the payments will have to be
     * associated to a third party.
     *
     * @param payments
     * @param policyNumber
     * @return boolean
     * @throws ApplicationExceptionChecked
     */
    private boolean applyReverseWithTypeOneByOne(Map payments, String policyNumber, String ruID,
                                                 String ioID) throws ApplicationExceptionChecked {
        log.debug("applyReverseWithTypeOneByOne()");
        Set keys = payments.keySet();
        Iterator iterator = keys.iterator();
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            Payment payment = (Payment) payments.get(key);
            // payment = (Payment) HibernateUtil.load(payment, payment.getPk());
            reversePaymentUAA(payment, date, key, policyNumber, ruID, ioID);

            //ACSELE-27878
            if (UserInfo.getCurrentInstance().equalsIgnoreCase("ALFA")) {
                updatePaymentsOrders(policy, payment, PaymentOrderStatus.CANCELED_STATE, null, true);
                payment.setPaymentStatus(PaymentStatus.CANCELED_STATE);

            } else {
                updatePaymentsOrders(policy, payment, PaymentOrderStatus.APPROVED_STATE, null, true);
            }

        }
        return true;
    }


    /**
     * @param payment
     * @param date
     * @param parentOpenItemId
     * @param policyNumber
     * @throws ApplicationExceptionChecked
     */
    private void reversePaymentUAA(Payment payment, java.sql.Date date, String parentOpenItemId, String policyNumber, String ruID,
                                   String ioID) throws ApplicationExceptionChecked {
        log.debug("method: reversePaymentUAA()");
        log.debug("payment = " + payment);
        String docTypeDescription = AcseleConf.getProperty("claimDoctypeReverse");

        double amount;

        if (AcseleConf.getProperty("acseleExchange").equals("yes")) {
            amount = payment.getPaidAmount().doubleValue();
        } else {
            amount = payment.getAmount().doubleValue();
        }

        try {
          /*ACSELE-27878 No generar openitem de contrapartida
                sendMovementUAA(policy, claim, payment, (int) DocType.Impl.load(docTypeDescription).getId(),
                        payment.getThirdParty().getPK(), amount, date, parentOpenItemId, StatusMovement.APPLIED.getValue(), policyNumber, String.valueOf(payment.getPaymentOrder().getThirdPartyRoleID()));
           */


            OpenItem openItem = openItemHome.findByPrimaryKey(parentOpenItemId);
            Collection uaaDetailByOpenItemId = UaaDetailManager.getUaaDetailByOpenItemId(Long.parseLong(parentOpenItemId));


            if (UserInfo.getCurrentInstance().equalsIgnoreCase("ALFA"))
                openItem.setStatus(StatusMovement.CANCELLED.getValue());
            else
                openItem.setStatus(StatusMovement.APPLIED.getValue());


            if (!"false".equalsIgnoreCase(AcseleConf.getProperty("EnableEntriesGenerationInASL"))) {

                reverseEntries(payment, date, ruID, ioID, uaaDetailByOpenItemId);
            }


        } catch (Exception ex) {
            log.error("Error", ex);
            throw new ApplicationExceptionChecked("claim.errorSendMovement", Severity.FATAL);
        }
    }

    /**
     * @param payment
     * @param date
     * @throws Exception
     */
    private void reverseEntries(Payment payment, java.sql.Date date, String ruID, String ioID, Collection uaaDetailByOpenItemId) throws Exception {
        ThirdParty third = payment.getThirdParty();
        String rfc = third.getDynamic().getInput("RFC");
        String personType = third.getDynamic().getDCO().getDesc();
        ClaimMovementBean beneficiary = applyPaymentUAA(payment, rfc, personType, date);
        Hashtable details = new Hashtable();
        PaymentOrder paymentOrder = payment.getPaymentOrder();
        Hashtable paymentOrderTaxes = calculateTaxesForRole(paymentOrder.getThirdPartyRoleID());
        getDetailsForRole(policy, beneficiary, paymentOrder.getThirdPartyRoleID(), paymentOrder.getAmountWithDeductible(), details,
                paymentOrderTaxes);
        if (payment.getReserveType() == ReserveType.NORMAL_RESERVE.getValue()) {
            if (paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.PAID_STATE) {
                createEntrys(policy, claim, payment, paymentOrder, beneficiary, details, true, ruID, ioID, null);
            }
        } else {
            createEntrys(policy, claim, payment, beneficiary, details, true, null, null, uaaDetailByOpenItemId);
        }
    }


    /**
     * Sets the claim inputs for the inputs in the list if any property match
     *
     * @param dcotoFill   dco to set the properties inputs values
     * @param inputsTable a table that map simbols withs inputs
     */
    private void setDCOPropertiesInputs(DefaultConfigurableObject dcotoFill, Map<String, String> inputsTable) {

        Enumeration symbols = dcotoFill.getCOT().getSymbols().elements();
        while (symbols.hasMoreElements()) {

            String symbol = (String) symbols.nextElement();
            if (dcotoFill.getCriterioInput(symbol) == null || "".equals(dcotoFill.getCriterioInput(symbol))) {
                String inputToSet = inputsTable.get(symbol);
                if (inputToSet != null) {
                    dcotoFill.setCriterioInput(symbol, inputToSet);
                }
            }
        }
    }

    /**
     * @param paymentOrder
     */
    private void updateAmountPayment(PaymentOrder paymentOrder) {
        Collection<Payment> payList = null;
        if (paymentOrder.getClaimReserve() instanceof ClaimNormalReserve) {
            payList = claim.getPaymentsNormalReserveList();
        } else {
            payList = claim.getPaymentsConceptReserveList();
        }
        if (payList != null && !payList.isEmpty()) {
            for (Payment payment : payList) {
                log.debug("payment.getPK(): " + payment.getPK());
                PaymentOrder paymentOrderAct = payment.getPaymentOrder();
//                Collection<PaymentOrder> paymentOrders = payment.getPaymentOrders();
//                for (PaymentOrder paymentOrderAct :paymentOrders) {
//                    paymentOrderAct = PaymentOrder.load(new Long(paymentOrderAct.getPk()));
                double amountDist = paymentOrderAct.getDistributionAmount();
                if (paymentOrderAct.getPK().equalsIgnoreCase(paymentOrder.getPK())) {
                    double amountPayment = payment.getPendingAmount().doubleValue();
                    double dif = amountPayment - (paymentOrder.getAmountWithDeductible() - amountDist - paymentOrder.getAmountFranchise());
                    log.debug("dif: " + dif);
                    if (dif > 0) {
                        payment.setAmountFranchise(paymentOrder.getAmountFranchise());
                        payment.setAmount(new Double(dif));
                        payment.update();
                    } else {
                        log.debug("Delete payment");
                        payment.delete();
                    }
                }
//                }
            }
        }
    }

    private void associateEvaluatedCoverage(ClaimInsuranceObject cio, ClaimNormalReserve cnr, String login, EvaluatedCoverage ec, CoverageForm form,
                                            Connection conn) {
        log.debug("***** INICIO TRAZAS MIRIAM-EDGAR *****");
        log.debug("ec = '" + ec + "'");
        com.consisint.acsele.thirdparty.api.ThirdParty insurancedThirdParty = null;
        Plan plan = ec.getPlan();
        String planId = plan.getPk();
        log.debug("planId = '" + planId + "'");
        long configuratedCoverage = ec.getConfiguratedCoverageOA().getId();
        log.debug("configuratedCoverage = '" + configuratedCoverage + "'");
        long productPk = Long.parseLong(policy.getProduct().getPk());
        log.debug("productPk = '" + productPk + "'");
        Double reserveLimit = 0.0;

        // ClaimsCoverageConfiguration coverageConf = null;
        log.debug("form.isErrorChecked() = " + form.getErrorChecked());
        ClaimsCoverageConfiguration coverageConf = ClaimsCoverageConfiguration.load(productPk, configuratedCoverage);
        if (coverageConf != null) {
            if (coverageConf.getReelegibility()) {
                log.debug("cnr.getDesc() = '" + cnr.getDesc() + "'");
                log.debug("coverageConf.getPeriod() = '" + coverageConf.getPeriod() + "'");
                log.debug("coverageConf.getBenefitPeriod() = '" + coverageConf.getBenefitPeriod() + "'");

                boolean invalidPeriod = isInvalidPeriod(cnr.getDesc(), coverageConf.getPeriod(), form, coverageConf.getBenefitPeriod(),
                        cio.getAgregatedInsuranceObject().getPk());
                if (invalidPeriod && !form.getIsDistributionAjust()) {
                    log.debug("Removing CNR (invalid period)");
                    log.debug("cnr = '" + cnr + "'");
                    log.debug("cnr.getPk() = '" + cnr.getPk() + "'");
                    try {
                        cio.remove(cnr, conn);
                    } catch (Exception e) {
                        log.error("Error removing cnr '" + cnr + "' (" + cnr.getPk() + ").  Check if it was saved...", e);
                    }
                    throw new ApplicationException(Exceptions.CCCoverageInvalidPeriod, Severity.INFO);
                }
            } else {
                if ((form.getIsDistributionAjust() && !isValidated && isCoverageAssociated(cnr.getDesc(), cio.getAgregatedInsuranceObject().getPk()))
                        || (!form.getIsDistributionAjust() && isCoverageAssociated(cnr.getDesc(), cio.getAgregatedInsuranceObject().getPk()))) {
                    // TODO: Acaso yo no deberia validar esto ANTES de pegar la cobertura?
                    log.error("Removing CNR (coverage associated): The coverage '" + cnr.getDesc() + "' from policy '" + claim.getPolicyId()
                            + "' is already in another claim...");
                    log.debug("cnr = '" + cnr + "'");
                    log.debug("cnr.getPk() = '" + cnr.getPk() + "'");
                    try {
                        cio.remove(cnr, conn);
                    } catch (Exception e) {
                        log.error("Error removing cnr '" + cnr + "' (" + cnr.getPk() + ").  Check if it was saved...", e);
                    }
                    throw new ApplicationException(Exceptions.CCCoverageAlreadyAssociated, Severity.INFO);
                }
            }
        }

        if (StringUtil.isEmptyOrNullValue(form.getErrorChecked()) && !form.getIsDistributionAjust()) {
            coverageConf = validateConfiguration(productPk, configuratedCoverage, ec.getDesc(), form);
        }
        double maxBenefitAmount = Double.parseDouble(form.getMaxBenefitAmount());
        Double amount = maxBenefitAmount;

        if (maxBenefitAmount == 0) {
            claim.setClaimStatus(ClaimStatus.DENIED);
            claim.setClaimSubStatus(ClaimSubStatus.DENIED);
            claim.update();
        }

//        if (form.getPaymentType() == ClaimNormalReserve.PAYMENT_TYPE_SCHEDULED) {
//            amount = new Double(maxBenefitAmount * benefitPayments);
//        }

        String covCurrencyId = ec.getDCO().getCriterioValue(AcseleConf.getProperty("monedaProperty"));
        covCurrencyId = (covCurrencyId == null) ? policy.getDCO().getCriterioValue(AcseleConf.getProperty("monedaProperty")) : covCurrencyId;

        log.debug("ClaimComposerEJB.associateEvaluatedCoverage -> covCurrencyId : " + covCurrencyId + " currencyIdStr: " + form.getCurrency());
        if (StringUtil.isEmptyOrNullValue(covCurrencyId)) {
            long currencyPk = Long.valueOf(policy.getFinancialPlan().getCurrencyPk());
            covCurrencyId = String.valueOf(currencyPk);
        }
        double rate;
        Double covCurrencyDouble = Double.valueOf(covCurrencyId);
        if (String.valueOf(Math.round(Double.valueOf(covCurrencyId))).equals(form.getCurrency())) {
            rate = 1.0;
        } else {
            rate = calculateExchangeRate(Currency.Impl.load(covCurrencyDouble.longValue()),
                    Currency.Impl.load(Long.valueOf(form.getCurrency())),
                    cnr.getDate());
        }
        log.debug("[Acsel-e] form.getReserveLimit() = " + form.getReserveLimit());
        double limit = Double.parseDouble(form.getReserveLimit()) * rate;
        reserveLimit = Double.parseDouble(form.getReserveLimit()) * rate;
        double limitMin = Double.parseDouble(form.getReserveLimitMin()) * rate;
        log.debug("[Acsel-e] limit = " + limit);
        log.debug("[Acsel-e] limitMin = " + limitMin);
        log.debug("[Acsel-e] amount = " + amount);
        boolean exc = limit < amount || limitMin > amount;
        log.debug(" Modeda de la cov : " + covCurrencyId + " Moneda seleccionada: " + form.getCurrency() + " tasa de cambio : " + rate
                + " reserva limite al cambio: " + limit +
                "Amount: "+amount.doubleValue()+ " se excede? " + exc);
        if (exc) {
            log.debug("Removing CNR");
            try {
                cio.remove(cnr, conn);
            } catch (Exception e) {
                log.error("Error removing cnr '" + cnr + "' (" + cnr.getPk() + ").  Check if it was saved...", e);
            }
            if (cnr.isDistributed())
                throw new ApplicationException(Exceptions.CCLimitReserveExchangeMaxMinDistrib, Severity.ERROR, " (" + cnr + ")");
            else
                throw new ApplicationException(Exceptions.CCLimitReserveExchangeMaxMin, Severity.ERROR, " (" + cnr + ")");
        }

        Currency currency = Currency.Impl.load(Long.valueOf(form.getCurrency()));
        cnr.setCurrency(currency);
        String systemProperty = AcseleConf.getProperty("refundPercentage");
        String refundPercentage_ = ec.getDCO().getCriterioValue(systemProperty);
        log.debug("Claim Entity refundPercentage_ " + refundPercentage_);
        try {
            if (refundPercentage_ == null) {
                cnr.setRefundPercentage(0);
            } else {
                cnr.setRefundPercentage(Double.parseDouble(refundPercentage_));
            }
            if (cnr.getRefundPercentage() == 0) {
                cnr.setRefundPercentage(1);
                log.error("Refund percentage's value was 0.  Setting value of 1.  Check the value of property '" + systemProperty + "' on Coverage '"
                        + ec.getDesc() + "'.");
            }
        } catch (Exception e) {
            cnr.setRefundPercentage(1);
            log.error("Refund percentage's value was not found.  Setting value of 1.  Check the value of property '" + systemProperty
                    + "' on Coverage '" + ec.getDesc() + "'.", e);
        }
        cnr.setRefundApplied(true);

        cnr.setPenaltyPercentage(0);
        cnr.setPenaltyApplied(true);
        if (cnr.isDistributed()) {
            List<ClaimNormalReserve> listCnr = cnr.getListClaimNormalReserveByCoverage(cnr.getEvaluatedCoverage().getPk(), cnr.getContainer().getPk());
            if (listCnr != null && listCnr.size() > 0) {
                cnr.setReserveLimit(listCnr.get(0).getReserveLimit());
            } else {
                cnr.setReserveLimit(reserveLimit);
            }
        } else {
            cnr.setReserveLimit(reserveLimit);
        }
        // cnr.setMaxBenefitAmount(maxBenefitAmount);
        cnr.setCause(form.getCause());
        cnr.setDetail(form.getDetail());
        cnr.setPathologies(form.getSelectedPathologies());
        cnr.setOperationPK(ec.getObjectDCO().getObjectPK().getOperationId());
        cnr.save();
        cio.add(cnr, conn);

        //Initial Adjust
        ClaimReserveAdjust cra = null;
        if (form.getIsDistributionAjust()) {
            if(form.getReason().equals("") || form.getReason()==null) {
                cra = new ClaimReserveAdjust(cnr.getDesc(), new java.util.Date(), cnr.getMaxBenefitAmount(), login,
                        ClaimReserveAdjust.initialAdjustReasonKey, ReserveAdjustType.INITIAL.getValue());
            }else{
                cra = new ClaimReserveAdjust(cnr.getDesc(), new java.util.Date(), cnr.getMaxBenefitAmount(), login,
                        form.getReason(), ReserveAdjustType.INITIAL.getValue());
            }
        } else {
            cra = new ClaimReserveAdjust(cnr.getDesc(), new java.util.Date(), amount, login, ClaimReserveAdjust.initialAdjustReasonKey,
                    ReserveAdjustType.INITIAL.getValue());
        }
        cra.setClaimReserveId(cnr.getPK());
        cnr.setInitialAdjust(cra);
        cnr.update();

        generateAuditTrailChangeState(this.claim, CustomAuditItem.ADD_COVERAGE, cnr.getPk(), null, cra.getPk());
        try {
            ClaimHistorical claimHistorical = new ClaimHistorical();
            claimHistorical.generateHistoricalWithMovement(this.claim, ClaimHistoricalOperationType.CREATE_CLAIM, ClaimHistoricalMovementType.CLAIM_CREATE, Long.parseLong(cra.getPK()), ClaimUtil.getValidatedLegacyType(cnr));
        } catch (Exception e) {

        }
        entryReserve(amount.doubleValue(), planId, configuratedCoverage, 0, ec, cnr.getDoneBy(),
                cnr.getCurrency() == null ? "" : cnr.getCurrency().getIsoCode(), cnr);

        try {
            generateReserveReinsuranceDistribution(cnr, amount.toString(), Constants.RESERVE_CLAIM, new java.sql.Date(cnr.getDate().getTime()), ReserveAdjustType.INITIAL);

            LetterGenerator letterGenerator = (LetterGenerator) BeanFactory.getBean(LetterGenerator.LETTER_GENERATOR);
            String incomingStatus = LetterGenerator.CREATE;
            String timestamp = DateUtil.getDateToShow(claim.getClaimDate()) + " 00:00:00";
            Collection<com.consisint.acsele.thirdparty.api.ThirdParty> insuranceThirdpartyByEC = policy.getInsuranceThirdpartyByEC(RoleGroup.INSURANCE_ROLES.getRoleList());
            if (insuranceThirdpartyByEC.size() > 0) {
                Iterator<com.consisint.acsele.thirdparty.api.ThirdParty> iterator = insuranceThirdpartyByEC.iterator();
                insurancedThirdParty = iterator.next();

            }
            TablaSimbolos symbolsTable = this.fillSymbolTableClaim(claim.getPk(), new TablaSimbolos());

            letterGenerator
                    .generateAutomaticLetterClaimByProductAndCoverage(incomingStatus, symbolsTable, claim.getPk(),
                            timestamp, true, ec.getConfiguratedCoverageOA().getId(),
                            String.valueOf(productPk),
                            insurancedThirdParty == null ? UserInfo.getLanguage() : (insurancedThirdParty.getConfiguratedLanguageByCustomer()),
                            insurancedThirdParty == null ? UserInfo.getLanguage() : (insurancedThirdParty.getConfiguratedLanguageByDefault()));
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    private boolean isCoverageAssociated(String coverageDesc, String agregatedInsuranceObjectId) {
        Query query = HibernateUtil.getQuery("claim.countClaimsWithCoverageByPolicy");
        query.setString(0, claim.getPolicyId());
        query.setString(1, coverageDesc);
        query.setString(2, agregatedInsuranceObjectId);
        log.debug("[isCoverageAssociated] policyId = " + claim.getPolicyId() + " - coverageDesc = " + coverageDesc
                + " - agregatedInsuranceObjectId = " + agregatedInsuranceObjectId);
        Integer count = (Integer) HibernateUtil.executeQueryOneRow(query);
        isValidated = true;
        return count != null && count.intValue() > 0;
    }

    private boolean isInvalidPeriod(String coverageDesc, Integer period, CoverageForm form, int benefitPeriod, String agregatedInsuranceObjectId) {

        //log.debug("******************period = " + period);
        //log.debug("******************coverageDesc = " + coverageDesc);
        //Query query = HibernateUtil.getQuery("claim.countClaimsWithCoverageByPolicy");
        try {
            Query query = HibernateUtil.getQuery("claim.DateClaimsWithCoverageByPolicy");
            query.setString(0, claim.getPolicyId());
            query.setString(1, coverageDesc);
            query.setString(2, agregatedInsuranceObjectId);

            log.debug("[isInvalidPeriod] policyId = " + claim.getPolicyId() + " - coverageDesc = " + coverageDesc + " - agregatedInsuranceObjectId = "
                    + agregatedInsuranceObjectId);

            //Integer count = (Integer) HibernateUtil.executeQueryOneRow(query);
            List coverageDates = HibernateUtil.executeQuery(query);
            //log.debug("**********************Ejecute el query " + query.toString());
            Iterator it = coverageDates.iterator();
            Date coverageMax = null;
            //log.debug("******************coverageMax  inicial= " + coverageMax);
            while (it.hasNext()) {
                //log.debug("ESTE ES EL PRIMER ELEMENTO" + it.next());
                Date coverageDate = (Date) it.next();
                if (coverageMax == null || coverageDate.after(coverageMax)) {
                    coverageMax = coverageDate;
                }

            }
            //log.debug("******************coverageMax  final= " + coverageMax);
            //log.debug("*******ESTE ES EL TIPO DEL PERIODO" + form.getPeriodLength());

            Date ocurrenceDate = DateUtil.getFormatToShow().parse(form.getOcurrenceDate());
            //log.debug("**********************ocurrenceDate = " + ocurrenceDate);
            // if (benefitPeriod == ClaimNormalReserve.PERIODICITY_DAYS) {
            if (period.intValue() > 0) {
                return coverageMax != null && DateUtil.daysBetween(coverageMax, ocurrenceDate) <= period.intValue();
            }
//            } else if (benefitPeriod == ClaimNormalReserve.PERIODICITY_MONTHS) {
//                log.debug("*** PERIODICY IS MONTHS ***");
//                log.debug("coverageMax:"+coverageMax);
//                if(coverageMax != null){
//                    Date monthsDate = new Date(
//                            (long) Funciones.addMonths(coverageMax.getTime(), period.intValue()));
//                    //log.debug("********************ESTE ES monthsDate" + monthsDate);
//                    log.debug("monthsDate:"+monthsDate);
//                    log.debug("ocurrenceDate:"+ocurrenceDate);
//                    log.debug("period.intValue():"+period.intValue());
//                    log.debug("monthsDate.before(ocurrenceDate):"+monthsDate.before(ocurrenceDate));
//                    return monthsDate.before(ocurrenceDate);
//                }
//            }
        } catch (Exception e) {
            log.error("Error checking validity period for coverage '" + coverageDesc + "', period '" + period + "' and benefitPeriod '"
                    + benefitPeriod + "'.  Assuming valid...", e);
        }
        //return true;
        return false;


    }

    public ClaimsCoverageConfiguration checkCoverageConfiguration(long productPk, long coveragePk, String coverageDesc, CoverageForm form) {
        return validateConfiguration(productPk, coveragePk, coverageDesc, form);
    }

    public ClaimsCoverageConfiguration checkCoverageConfiguration(com.consisint.acsele.openapi.product.Product product, long coveragePk, String coverageDesc, CoverageForm form) {
        return validateConfiguration(product.getId(), coveragePk, coverageDesc, form);
    }


    private ClaimsCoverageConfiguration validateConfiguration(long productPk, long coveragePk, String coverageDesc, CoverageForm form) {

        ClaimsCoverageConfiguration claimConfiguration = ClaimsCoverageConfiguration.load(productPk, coveragePk);

        if (claimConfiguration == null) {
            log.debug("[Acsel-e] There isn't a Coverage Configuration");
            return null;
        }
//        log.debug("[Acsel-e] claimConfiguration = " + claimConfiguration);
        if (claimConfiguration.getOpenClaims()) {
            if (checkClaimByPolicyAndStatus(ClaimStatus.IN_PROCESS.getValue(), coverageDesc)) {
                throw new ApplicationException(Exceptions.CCOpenClaimCheck, Severity.INFO);
            }
        }

        if (claimConfiguration.getDeniedClaims()) {
            if (checkClaimByPolicyAndStatus(ClaimStatus.DENIED.getValue(), coverageDesc)) {
                throw new ApplicationException(Exceptions.CCRejectedClaimCheck, Severity.INFO);
            }
        }

        if (claimConfiguration.getClosedClaims()) {
            if (checkClaimByPolicyAndStatus(ClaimStatus.CLOSED.getValue(), coverageDesc)) {
                throw new ApplicationException(Exceptions.CCClosedClaimCheck, Severity.INFO);
            }
        }

        return claimConfiguration;
    }

    /**
     * Check the claim Policy and status
     *
     * @param status
     * @param coverageDesc
     * @return boolean
     */
    private boolean checkClaimByPolicyAndStatus(int status, String coverageDesc) {
        log.info("checkClaimByPolicyAndStatus(..) - Entrando");
        log.debug("status = " + status);
        log.debug("coverageDesc = " + coverageDesc);
        Query query = HibernateUtil.getQuery("claim.findByPolicypkAndState");

        long policyId = 0;
        if (policyOA != null) {
            policyId = policyOA.getId();
        } else {
            policyId = policy.getId();
        }
        query.setLong(0, policyId);
        log.debug("policy.getPk() -> " + policyId);


        if (status == ClaimStatus.DENIED.getValue()) {
            query.setInteger(1, ClaimStatus.CLOSED.getValue());
        } else {
            query.setInteger(1, status);
        }
        query.setString(2, coverageDesc);
        List claims = HibernateUtil.executeQuery(query);
        String templateName;
        String templateNameD = "";
        if (status == ClaimStatus.IN_PROCESS.getValue()) {
            if (claims.size() > 0) {
                return true;
            }
        } else if (status == ClaimStatus.DENIED.getValue()) {
            templateNameD = AcseleConf.getProperty(TEMPLATE_CLAIM_DENIED);
            for (int i = 0; i < claims.size(); i++) {
                Claim cl = (Claim) claims.get(i);
                ClaimStateBean state = cl.getLastClaimState();
                if (templateNameD.equals(state.getCotname())) {
                    return true;
                }
            }
        } else if (status == ClaimStatus.CLOSED.getValue()) {
            templateName = AcseleConf.getProperty(TEMPLATE_CLAIM_CLOSED);
            for (int i = 0; i < claims.size(); i++) {
                Claim cl = (Claim) claims.get(i);
                ClaimStateBean state = cl.getLastClaimState();
                if (templateName.equals(state.getCotname())) {
                    return true;
                }
            }
        }
        log.info("checkClaimByPolicyAndStatus(..) - Saliendo");
        return false;
    }

    /**
     * Find Claim by Policy and Date
     *
     * @param opks
     * @param agregatedPolicy
     * @param product
     * @return Date
     */
    public static java.util.Date findClaimByPolicyAndDate(Collection opks, AgregatedPolicy agregatedPolicy, Product product) {

        String policyid = agregatedPolicy.getOperationPK().getItem();
        Iterator iterOpk = opks.iterator();
        java.util.Date maxDate = new Date(0, 0, 1);
        java.util.Date auxDate = new Date(0, 0, 1);

        maxDate = getMaxDateToCancel(iterOpk, policyid, product, maxDate);

        if (auxDate.compareTo(maxDate) == 0) {
            return null;
        }

        return maxDate;
    }

    /**
     * Get the greatest date to cancel
     *
     * @param iterOpk
     * @param policyid
     * @return Date
     */
    public static Date getMaxClaimDate(Iterator iterOpk, String policyid) {
        java.util.Date maxDate = new java.util.Date(0, 0, 1);
        if (iterOpk.hasNext()) {
            Session session = null;
            try {
                session = HibernateUtil.getSession();
                while (iterOpk.hasNext()) {
                    Properties properOpk = (Properties) iterOpk.next();
                    StringBuffer select = new StringBuffer(50);
                    select.append("Select {c.*} FROM Claim c, POLICYDCO p WHERE c.policyId=").append(policyid)
//                    .append(" AND c.POLICYDATE >= p.INITIALDATE AND c.claimDate <= p.FINISHDATE")
                            .append(" AND p.OPERATIONPK=").append(properOpk.getProperty("OPERATIONPK"));

                    Query queryHibernate = HibernateUtil.createSQLQuery(select.toString(), "c", Claim.class);
                    List claims = HibernateUtil.executeQuery(queryHibernate);
                    Iterator iterClaim = claims.iterator();
                    while (iterClaim.hasNext()) {
                        Claim claim = (Claim) iterClaim.next();
                        ClaimStatus status = claim.getClaimStatus();
                        if (status == ClaimStatus.IN_PROCESS ||
                                status == ClaimStatus.RE_OPEN ||
                                status == ClaimStatus.PENDING) {
                            maxDate = getMaxDateClaim(claim, maxDate);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error in find Claim by policy ", e);
                throw new TechnicalException(Exceptions.CCSystemError, Severity.FATAL, e);
//            } finally {
                //HibernateUtil.closeSession(session);
            }
        }
        return maxDate;
    }

    /**
     * Get the greatest date to cancel
     *
     * @param iterOpk
     * @param policyid
     * @param product
     * @param maxDate
     * @return Date
     */
    public static Date getMaxDateToCancel(Iterator iterOpk, String policyid, Product product, Date maxDate) {
        if (iterOpk.hasNext()) {
            try {
                Collection collecPayments;
                while (iterOpk.hasNext()) {
                    Properties properOpk = (Properties) iterOpk.next();
                    String query = "Select c.claimid FROM Claim c, POLICYDCO p WHERE c.policyId = p.agregatedobjectid AND c.policyId= ? AND p.OPERATIONPK= ? ";
                    List<CaseInsensitiveProperties> props = JDBCUtil.doQueryListWithParams(query, new SqlQueryParam[]{
                            new SqlQueryParam(Types.NUMERIC, Long.valueOf(policyid)),
                            new SqlQueryParam(Types.NUMERIC, Long.valueOf(properOpk.getProperty("OPERATIONPK")))});

                    for (CaseInsensitiveProperties prop : props) {
                        Claim claim = ClaimPersister.Impl.getInstance().load(Long.valueOf(prop.getProperty("claimid")));
                        ClaimStatus status = claim.getClaimStatus();
                        collecPayments = getPaymentsByClaim(claim, product);

                        if (collecPayments.isEmpty()) {
                            if (status == ClaimStatus.IN_PROCESS ||
                                    status == ClaimStatus.RE_OPEN ||
                                    status == ClaimStatus.PENDING) {
                                maxDate = getMaxDateClaim(claim, maxDate);
                            }
                        } else {
                            maxDate = getMaxDatePaymentOrder(collecPayments, maxDate);
                        }
                    }
                }
            } catch (Exception e) {
                throw new TechnicalException(Exceptions.CCSystemError, Severity.FATAL, e);
            }
        }
        return maxDate;
    }

    /**
     * Get the greatest Claim's Ocuurrence Date
     *
     * @param claim
     * @param maxDate
     * @return Date
     */
    public static java.util.Date getMaxDateClaim(Claim claim, Date maxDate) {
        Date newMaxDate = claim.getOcurrenceDate();
        maxDate = (newMaxDate.compareTo(maxDate) > 0) ? newMaxDate : maxDate;

        return maxDate;
    }

    /**
     * Get the greatest PaymentOrder's Date
     *
     * @param collecPayments
     * @param maxDate
     * @return
     */
    public static java.util.Date getMaxDatePaymentOrder(Collection collecPayments, Date maxDate) {
        if (!collecPayments.isEmpty()) {
            //busco si tiene pagos pendients y luego la fecha mas actual
            Iterator iterPayments = collecPayments.iterator();
            while (iterPayments.hasNext()) {
                PaymentOrder paymentOrder = (PaymentOrder) iterPayments.next();
                PaymentOrderStatus statusPayment = paymentOrder.getPaymentOrderStatus();
                if (statusPayment == PaymentOrderStatus.APPROVED_STATE ||
                        statusPayment == PaymentOrderStatus.PENDING_STATE ||
                        statusPayment == PaymentOrderStatus.PENDING_APPROVE_STATE) {
                    //buscar el q tenga el pago mas actual.
                    Date newMaxDate = paymentOrder.getCommitmentDate();
                    maxDate = (newMaxDate.compareTo(maxDate) > 0) ? newMaxDate : maxDate;

                }
            }
        }
        return maxDate;
    }

    /**
     * This method get all the payments orders with the status PAID_STATE,APPROVED_STATE,
     * PENDING_STATE,PENDING_APPROVE_STATE given a claim
     *
     * @param claim is the claim to get payments orders of
     * @return collection of paypament orders
     */
    public static Collection getAllPaymentsByClaim(Claim claim) {
        Collection collecPayments = new Vector();
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit cru : claimRiskUnits) {
            Iterator InsObject = cru.getClaimInsuranceObjects().values().iterator();
            while (InsObject.hasNext()) {
                ClaimInsuranceObject cio = (ClaimInsuranceObject) InsObject.next();
                Iterator normalReserve = cio.getNormalReserves().values().iterator();
                while (normalReserve.hasNext()) {
                    ClaimNormalReserve nr = (ClaimNormalReserve) normalReserve.next();
                    Collection payments = nr.getPaymentOrderList().values();
                    if (!payments.isEmpty()) {
                        Iterator iterPayments = payments.iterator();
                        while (iterPayments.hasNext()) {
                            PaymentOrder paymentOrder = (PaymentOrder) iterPayments.next();
                            PaymentOrderStatus statusPayment = paymentOrder.getPaymentOrderStatus();
                            if (statusPayment == PaymentOrderStatus.PAID_STATE ||
                                    statusPayment == PaymentOrderStatus.APPROVED_STATE ||
                                    statusPayment == PaymentOrderStatus.PENDING_STATE ||
                                    statusPayment == PaymentOrderStatus.PENDING_APPROVE_STATE) {
                                collecPayments.add(paymentOrder);
                            }
                        }
                    }
                }
            }
        }
        return collecPayments;
    }

    public static Collection getPaymentsByClaim(Claim claim, Product product) {

        Collection collecPayments = new Vector();
        Enumeration enumPlans = product.getPlanes().getHashtable().elements();

        AgregatedInsuranceObjectType iot = null;
        ConfiguratedCoverage cc = null;
        ClaimsCoverageConfiguration ccc = null;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit cru : claimRiskUnits) {
            Iterator InsObject = cru.getClaimInsuranceObjects().values().iterator();
            while (InsObject.hasNext()) {
                ClaimInsuranceObject cio = (ClaimInsuranceObject) InsObject.next();
                Iterator normalReserve = cio.getNormalReserves().values().iterator();
                while (normalReserve.hasNext()) {
                    ClaimNormalReserve nr = (ClaimNormalReserve) normalReserve.next();
                    //Hago la validacion si esta activa o no la bandera de check d la coverage
                    while (enumPlans.hasMoreElements()) {
                        Plan plan = (Plan) enumPlans.nextElement();
                        String template = AgregatedInsuranceObject.getTemplateDesc(cio.getDesc());
                        iot = plan.getInsuranceObjectType(AgregatedInsuranceObject.getTemplateDesc(cio.getDesc()));
                        if (iot != null) {
                            cc = iot.getConfiguratedCoverage(nr.getDesc());
                            break;
                        }

                    }
                    if (iot != null && cc != null) {
                        ccc = ClaimsCoverageConfiguration.load(Long.parseLong(product.getPk()), Long.parseLong(cc.getPk()));
                        Collection payments = nr.getPaymentOrderList().values();
                        if (!payments.isEmpty()) {
                            Iterator iterPayments = payments.iterator();
                            while (iterPayments.hasNext()) {
                                PaymentOrder paymentOrder = (PaymentOrder) iterPayments.next();
                                PaymentOrderStatus statusPayment = paymentOrder.getPaymentOrderStatus();
                                if (statusPayment == PaymentOrderStatus.APPROVED_STATE ||
                                        statusPayment == PaymentOrderStatus.PENDING_STATE ||
                                        statusPayment == PaymentOrderStatus.PENDING_APPROVE_STATE) {
                                    if (ccc.isCanCancel()) {
                                        collecPayments.add(paymentOrder);
                                    } else {

                                        throw new ApplicationException(Exceptions.PPErrorCancelCoverage, Severity.FATAL);

                                    }


                                }
                            }

                        }
                    }

                }

            }

        }
        return collecPayments;
    }

    /**
     * @param cruDesc
     * @param cioDesc
     * @param cnrDesc
     * @return
     * @throws ApplicationExceptionChecked
     */
    //TODO: Este método no se usa
    public String getCoverageBough(String cruDesc, String cioDesc, String cnrDesc) throws ApplicationExceptionChecked {

        AgregatedRiskUnit aru = policy.getAgregatedRiskUnit(cruDesc);
        AgregatedInsuranceObject aio = aru.getInsuranceObject(cioDesc);
        EvaluatedCoverage evCov = aio.getEvaluatedCoverage(cnrDesc);
        // evCov.load();
        evCov = (EvaluatedCoverage) HibernateUtil.load(evCov, evCov.getPk());

        return evCov.getDCO().getCriterioInput(AcseleConf.getProperty("coverageBough"));
    }

    /**
     * @return Collection
     */
    public Collection<SearchBean> getRistUnits() {
        log.info(" *** METHOD NAME  " + "getRistUnits");
        List<SearchBean> result = new ArrayList<SearchBean>();
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        if (claimRiskUnits == null || claimRiskUnits.isEmpty()) {
            Enumeration riskUnitsPol = policy.getRiskUnits();  // RU de la p�liza
            while (riskUnitsPol.hasMoreElements()) {
                AgregatedRiskUnit riskUnit = (AgregatedRiskUnit) riskUnitsPol.nextElement();
                SearchBean bean = new SearchBean(riskUnit.getPK(), riskUnit.getDesc());
                result.add(bean);
            }
        } else {
            for (ClaimRiskUnit claimRiskUnit : claimRiskUnits) {
                log.debug("****** riskUnit = " + claimRiskUnit);
                AgregatedRiskUnit agRU = claimRiskUnit.getAgregatedRiskUnit();
                log.debug("***** AgregatedRiskUnit.getDesc() = " + agRU.getDesc());
                SearchBean bean = new SearchBean(agRU.getPK(), agRU.getDesc());
                log.debug("***** SearchBean.getDesc() = " + bean.getDesc());
                result.add(bean);
            }
        }
        return result;
    }

    /**
     * @param ruID
     * @return Collection
     * @throws ApplicationExceptionChecked
     */
    public Collection getAffectedObjects(String ruID) throws ApplicationExceptionChecked {
        log.info(" *** METHOD NAME  " + "getAffectedObjects");
        Vector result = new Vector();
        log.debug("[getAffectedObjects] " + ruID);
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        ClaimRiskUnit claimRiskUnit = null;
        for (ClaimRiskUnit cru : claimRiskUnits) {
            if (cru.getAgregatedRiskUnit().getPk().equals(ruID)) {
                claimRiskUnit = cru;
                break;
            }
        }
        if (claimRiskUnit != null) {
            Iterator affectedObjects = claimRiskUnit.getClaimInsuranceObjects().values().iterator();
            while (affectedObjects.hasNext()) {
                ClaimInsuranceObject iobject = (ClaimInsuranceObject) affectedObjects.next();
                iobject.load();
//                    log.debug("[iobject] " + iobject.getDesc());
                iobject.setContainer(claimRiskUnit);
                iobject.setClaimRiskUnitId(Long.parseLong(claimRiskUnit.getPk()));
                SearchBean bean = new SearchBean(iobject.getPk(), iobject.getDesc(), iobject.getLabel());
                result.add(bean);
            }
        }
        return result;
    }

    /**
     * @param ruID String
     * @return Collection
     */
    public Collection getAgregatedInsuranceObjects(String ruID) {
        log.info(" *** METHOD NAME  " + "getAgregatedInsuranceObjects");

        Vector io = new Vector();
        Enumeration ecovs = null;
        HashMap covs = new HashMap();
        EventClaim eventClaim = claim.getEventClaim();
        Enumeration claimTypes = eventClaim.elements();
        while (claimTypes.hasMoreElements()) { //Cargamos los Claim Types...
            ClaimType claimType = (ClaimType) claimTypes.nextElement();
            log.debug("***** claimType = " + claimType);
            Collection<EventClaimCoverage> eventClaimCoverages = claimType.getEventClaimCoverages().values();
            for (EventClaimCoverage eventClaimCoverage : eventClaimCoverages) { //Cargamos las coberturas asociadas a cada Claim Type.
                String descCov = eventClaimCoverage.getDesc();
                log.debug("***** eventClaimCoverage = " + descCov);
                covs.put(descCov, descCov);
            }
        }

        Collection eventClaimsCoverages = covs.values();

        boolean found = false;
        if (isRetroactive()) {
            io = getInsuranceObjectByLastOperation(ruID, eventClaimsCoverages);
        } else {
            io = getInsuranceObjectsByEffectiveDate(ruID, eventClaimsCoverages);
        }
        return io;
    }

    private Vector getInsuranceObjectByLastOperation(String ruID, Collection eventClaimsCoverages) {
        Vector io = new Vector();
        Enumeration ecovs = null;
        HashMap covs = new HashMap();
        boolean found = false;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        ClaimRiskUnit claimRiskUnit = null;
        for (ClaimRiskUnit cru : claimRiskUnits) {
            if (cru.getAgregatedRiskUnit().getPk().equals(ruID)) {
                claimRiskUnit = cru;
                break;
            }
        }
        AgregatedPolicy pol = (AgregatedPolicy) Policy.Impl.loadById(policy.getId()); //para garantizar que es la última operación
        AgregatedRiskUnit riskUnit = pol.getAgregatedRiskUnit(ruID);
        Enumeration iods = riskUnit.getInsuranceObjects();
        while (iods.hasMoreElements()) {
            AgregatedInsuranceObject aio = (AgregatedInsuranceObject) iods.nextElement();
            log.debug("YAM aio.getCoverageIDs() = " + aio.getCoverageIDs());
            if (aio != null) {
                ecovs = aio.getEvaluatedCoverages();
                while (ecovs.hasMoreElements()) {
                    EvaluatedCoverage ecov = (EvaluatedCoverage) ecovs.nextElement();
                    String covDesc = ecov.getDesc(); // La cobertura Asociada al IO
                    Iterator iterator = eventClaimsCoverages.iterator();
                    while (iterator.hasNext()) {
                        String eventClaimDesc = (String) iterator.next();
                        if (eventClaimDesc.equalsIgnoreCase(covDesc) && !found) {
                            found = true;
                            Collection<Participation> aioInsured = aio.getParticipationByRole(RoleGroup.INSURANCE_ROLES.getRoleList());
                            if (aioInsured.size() == 0) {
                                SearchBean search = new SearchBean(aio.getPK(), aio.getDesc(), aio.getLabel(), aio.getOperationPK().getId());
                                search.setCruObj(claimRiskUnit);
                                io.add(search);
                            } else {
                                for (Participation part : aioInsured) {
                                    com.consisint.acsele.thirdparty.api.ThirdParty insured = com.consisint.acsele.thirdparty.api.ThirdParty.Impl.load(part.getThirdPartyID());
                                    SearchBean search = new SearchBean(aio.getPK(), aio.getDesc(), aio.getLabel() + insured.getName(), aio.getOperationPK().getId());
                                    search.setCruObj(claimRiskUnit);
                                    io.add(search);
                                }
                            }
                        }
                    }
                }
                found = false;
            }
        }
        return io;
    }

    private Vector getInsuranceObjectsByEffectiveDate(String ruID, Collection eventClaimsCoverages) {
        Vector io = new Vector();
        Enumeration ecovs = null;
        HashMap covs = new HashMap();
        boolean found = false;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        ClaimRiskUnit claimRiskUnit = null;
        for (ClaimRiskUnit cru : claimRiskUnits) {
            if (cru.getAgregatedRiskUnit().getPk().equals(ruID)) {
                claimRiskUnit = cru;
                break;
            }
        }
        AgregatedRiskUnit aru = claimRiskUnit.getAgregatedRiskUnit();
        aru.setAgregatedPolicy(policy);

        List<AgregatedInsuranceObject> iods = new ArrayList<AgregatedInsuranceObject>();
        try {
            iods = DBAgregatedInsuranceObject.getListByEffectiveDate(aru, DateUtil.getSqlDate(getOcurrenceDate()));
        } catch (Exception e) {

        }
        for (AgregatedInsuranceObject aio : iods) {
            if (aio != null) {
                List<EvaluatedCoverage> coverages = getEvaluatedCoverageList(aio);
                for (EvaluatedCoverage ecov : coverages) {
                    String covDesc = ecov.getDesc(); // La cobertura Asociada al IO
                    Iterator iterator = eventClaimsCoverages.iterator();
                    while (iterator.hasNext()) {
                        String eventClaimDesc = (String) iterator.next();
                        if (eventClaimDesc.equalsIgnoreCase(covDesc) && !found) {
                            found = true;
                            Collection<Participation> aioInsured = aio.getParticipationByRole(RoleGroup.INSURANCE_ROLES.getRoleList());
                            if (aioInsured.size() == 0) {
                                SearchBean search = new SearchBean(aio.getPK(), aio.getDesc(), aio.getLabel(), aio.getOperationPK().getId());
                                search.setCruObj(claimRiskUnit);
                                io.add(search);
                            } else {
                                for (Participation part : aioInsured) {
                                    com.consisint.acsele.thirdparty.api.ThirdParty insured = com.consisint.acsele.thirdparty.api.ThirdParty.Impl.load(part.getThirdPartyID());
                                    SearchBean search = new SearchBean(aio.getPK(), aio.getDesc(), aio.getLabel() + insured.getName(), aio.getOperationPK().getId());
                                    search.setCruObj(claimRiskUnit);
                                    io.add(search);
                                }
                            }
                        }
                    }
                }
                found = false;
            }
        }
        return io;
    }

    /**
     * @param participations Collection
     * @return Collection
     */
    public Collection getThirdPartyData(Collection participations) {
        ArrayList datos = new ArrayList();
        for (Iterator iterator = participations.iterator(); iterator.hasNext(); ) {
//            String idParticipation = (String) iterator.next();
//            Participation participation = new Participation(idParticipation);
//            participation.loadByPK();
            Participation participation = (Participation) iterator.next();
            try {
                ThirdParty thir = participation.getThirdParty();
                ThirdParty tercero = ThirdParty.getInstance(thir.getPk());
                SearchThirdPartyResult bean = new SearchThirdPartyResult(thir.getPk().toString(), tercero.getName(), tercero.getStatic().getStatus());
                bean.setRole(com.consisint.acsele.uaa.api.Role.Impl.load(participation.getRole().getDynamic().getDCO().getDesc()));
                datos.add(bean);
            } catch (Exception e) {
                log.error("Error", e);
            }
        }
        return datos;
    }

    /**
     * @param roles
     * @param id
     * @return Collection
     */
    private Collection getParticipation(RoleList roleList, String id) {
        try {
            if (id == null) {
                return policy.getParticipationByRole(roleList);
            } else {
                AgregatedRiskUnit aru = getAgregatedRiskUnitById(id);
                return aru.getParticipationByRole(roleList);
            }
        } catch (Exception ex) {
            log.error(ex);
        }
        return new ArrayList();
    }

    /**
     * @param ruID
     * @param r
     * @return
     */
    public Collection getParticipations(String ruID, RoleList roleList) {
        Collection participaciones = getParticipation(roleList, null);
        Collection ruPart = new ArrayList();
        if (!StringUtil.isEmptyOrNullValue(ruID)) {
            ruPart = getParticipation(roleList, ruID);
        }
        if (!ruPart.isEmpty()) {
            participaciones.addAll(ruPart);
        }
        log.debug("participaciones: " + participaciones.size());
        return participaciones;
    }

    /**
     * @param affecteds
     * @param cnr
     * @param crc
     * @return long
     * @throws ApplicationExceptionChecked
     */
    public long getCRU(Vector affecteds, Vector cnr, Vector crc) throws ApplicationExceptionChecked {
        Session session = null;
        try {
            session = getHibernateSession();
            log.debug("## claim.getClaimNumber()=" + claim.getClaimNumber());
            Collection<ClaimRiskUnit> claimRUList = claim.getClaimRiskUnitsList();// claimRUList.get(0).getPk() =  claimriskunitid=123580
            log.debug("## claimRUList=" + claimRUList);
            if (claimRUList == null || claimRUList.isEmpty()) {
                log.debug("IS NULL Claim RISK UNITS");
            } else {
                long idRU = 0;
                boolean first = true;
                log.debug("## claimRUList.i=" + claimRUList.size());
                for (ClaimRiskUnit cru : claimRUList) {
                    log.debug("## cru.getClaimInsuranceObjects()=" + cru.getClaimInsuranceObjects() + "," + cru.getPK());// pk = claimriskunitid=123580
                    cru.load();
                    if (first) {
                        idRU = Long.parseLong(cru.getDesc());
                        first = false;
                    }
                    affecteds.addAll(getAffectedObjects(cru, cnr, crc));
                }
                return idRU;
            }
        } catch (ApplicationException ae) {
            log.error("Error", ae);
            throw ae;
        } catch (Exception ex) {
            log.debug("[Exception]", ex);
        } finally {
            closeHibernateSession(session);
        }
        return 0;
    }

    /**
     * TODO: (GS) Todo este c�digo est� duplicado de ApplicationHandler.numberPolicy(). Refactor.
     *
     * @param conn
     * @return String
     */
    private String getNumberClaim(Connection conn) throws Exception {

        //------------------------------------------------------------------------------------------
        // Validamos de que exista configurado en el Acele.properties la propiedad "policy.office"
        //------------------------------------------------------------------------------------------
        String dummy = AcseleConf.getProperty("policy.office");
        if ((dummy == null) || (dummy.length() == 0)) {
            log.error("*** numberClaim: No existe propiedad en Acsele: policy.office");
            return null;
        }
//        String company = policy.getProduct().getDCO().getCriterioValue(dummy);
//        if (company.indexOf(".") > 0) {
//            company = company.substring(0, company.indexOf("."));
//        }
        DefaultConfigurableObject dco = policy.getDCO();

        //------------------------------------------------------------------------------------------
        // Validamos de que exista configurado en el Acele.properties la propiedad "policy.branch"
        //------------------------------------------------------------------------------------------
        dummy = AcseleConf.getProperty("policy.branch");
        if ((dummy == null) || (dummy.length() == 0)) {
            ExceptionUtil.handleException(Exceptions.PPSucursalWasNotObtained, new Exception());
        }

        //------------------------------------------------------------------------------------------
        // Validamos de que la propiedad "Sucursal" exista en la p�liza
        //------------------------------------------------------------------------------------------
//        String branch = dco.getCriterioInput(dummy);
        String branchValue = dco.getCriterioValue(dummy);
        if ((branchValue == null) || (branchValue.length() == 0)) {
            ExceptionUtil.handleException(Exceptions.PPSucursalWasNotObtained, new Exception());
        }
//        if (branchValue.indexOf(".") > 0) {
//            branchValue = branchValue.substring(0, branchValue.indexOf("."));
//        }

        //------------------------------------------------------------------------------------------
        // Validamos de que exista configurado en el Acele.properties la propiedad "policy.product"
        //------------------------------------------------------------------------------------------
        dummy = AcseleConf.getProperty("policy.product");
        if ((dummy == null) || (dummy.length() == 0)) {
            log.error("*** numberPolicy: No existe propiedad en Acsele: policy.product");
            return null;
        }
//        prod = policy.getProduct().getDCO().getCriterioInput(dummy);
//        String prod = policy.getProduct().getDCO().getCriterioValue(dummy);
//        if (prod.indexOf(".") > 0) {
//            prod = prod.substring(0, prod.indexOf("."));
//        }

//        log.debug("--company " + company + " branch " + branch + " prod " + prod + " branchValue " +
//                  branchValue);

        //------------------------------------------------------------------------------------------
        // Ser� que podemos convertir el n�mero de la Sucursal a un valor entero?
        //------------------------------------------------------------------------------------------
        /*
        try {
            branchValue = String.valueOf((new Double(branchValue).intValue()));
        } catch (Exception e) {
            log.error("Error", e);
            return null;
        }
          */
        String propCountry = AcseleConf.getProperty("policy.country");
        if ((propCountry == null) || (propCountry.length() == 0)) {
            log.error("*** numberPolicy: No existe propiedad en Acsele: policy.country");
            return null;
        }
        //TODO: review this
        ExpresionEvaluator ev = ExpresionEvaluator.createExpresionEvaluator(publishSymbolsTable(this.claim, null));

        ev.addSymbols(policy.getProduct().getDCO());
        ev.addSymbols(policy.getDCO());
        return NumberGenerator.generateClaimNumber(ev.getTablaSimbolos(), conn);
    }

    /**
     * @param agregatedPolicy
     * @throws Exception
     */
    public void setAgregatedPolicy(AgregatedPolicy agregatedPolicy) {
        try {
//            log.debug("[RL] Setting Policy (composer)... " + agregatedPolicy);
//            log.debug("agregatedPolicy.getInitialDate() = '" + agregatedPolicy.getInitialDate() +
//                      "'");
//            log.debug("agregatedPolicy.getFinishDate() = '" + agregatedPolicy.getFinishDate() +
//                      "'");
//            new Exception().printStackTrace();
            this.policy = agregatedPolicy;
        } catch (Exception e) {
            log.error("Error", e);
            throw new ApplicationException(Exceptions.PPReviewingOperationError, Severity.ERROR);
        }
    }

    /**
     * @return AgregatedPolicy
     * @throws Exception
     */
    public AgregatedPolicy getAgregatedPolicy() throws Exception {
        return this.policy;
    }

    /**
     * @param request
     * @return Claim
     * @throws StackException
     */
    public Claim createClaim(ClientRequest request) throws StackException {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getHibernateSession();
            transaction = beginTransaction(false, session);
            ClaimCreationBean claimCreationBean = (ClaimCreationBean) request.getAttribute("data");
            createClaim(claimCreationBean, policy);
//          updateComments(claim.getPk(), session);

            String fail = (String) request.getAttribute("fail");
            //TODO: Esto fue colocado para realizar una prueba junit. Este bloque de c�digo aqui est� realmente horrible
            //TODO: lo m�s sensato ser�a que la transacci�n se iniciara fuera de este m�todo, sobre todo ahora que
            //TODO: vamos a tener creacion masiva de reclamos.
            if (fail != null && fail.equals("true")) {
                throw new Exception("The fail flag is active. So, This exception has been intended thrown");
            }
            commitTransaction(transaction, session);
            return this.claim;
        } catch (Exception e) {
            rollbackTransaction(transaction, session);
            log.error("Error", e);
            if (e instanceof AcseleException) {
                throw (AcseleException) e;
            } else {
                throw new TechnicalException(Exceptions.CCSystemError, Severity.FATAL, e);
            }
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * Updates the comments with the policyId
     *
     * @param claimId
     */
    private void updateComments(String claimId, Session session) {
        try {
            Query query = HibernateUtil.getQuery("claimnote.loadByClaimID");
            query.setLong(0, Long.parseLong(claimId));
            List list = HibernateUtil.executeQuery(query);
//            List list = session.find(query.getQueryString());
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                Object[] objetos = (Object[]) iter.next();
                ClaimNote claimNote = (ClaimNote) session.load(ClaimNote.class, (Long) objetos[0]);
                claimNote.setIdPolicy(policy.getPk());
            }
        } catch (Exception e) {
            log.error("Error updating comments.  Comments will be ignored: ", e);
        }
    }

    /**
     * @param claimCreationBean
     * @param agregated
     * @throws StackException
     */
    public void createClaim(ClaimCreationBean claimCreationBean, AgregatedPolicy agregated) throws Exception {
        log.info(" *** METHOD NAME  " + "createClaim");
        try {
            setAgregatedPolicy(agregated);
            EventClaim eventClaim = policy.getProduct().getEventClaimByPK(Long.parseLong(claimCreationBean.getEventTypeId()));
            validateEventClaimPolicy(eventClaim, agregated);
            long declarationId = StringUtil.isEmptyOrNullValue(claimCreationBean.getDeclarationPk()) ? 0 :
                    Long.valueOf(claimCreationBean.getDeclarationPk());

            //If "new claim" option is still available
//            if (claimIdLocal == null) {
//                claimIdLocal = String.valueOf(generateNextId("CLAIM"));
//            }
            log.debug("claimIdLocal>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> = " + declarationId);

            this.claim = new Claim(policy.getPK(), claimCreationBean.getOcurrenceDate());
            this.claim.setEventClaim(eventClaim);
            this.claim.setNotification(claimCreationBean.getNotification());
            this.claim.setExcessClaimEventID(claimCreationBean.getExcessClaimEventId());

            if (StringUtil.isNumber(claimCreationBean.getUserAgency())) {
                this.claim.setGlobalAgencyId(Long.parseLong(claimCreationBean.getUserAgency()));
            }

            if (claimCreationBean.getClaimDate() != null) {
                claim.setClaimDate(claimCreationBean.getClaimDate());
            }
            claim.setPolicy(policy);

            // 2005-05-27 (BCH) HotFix
            //            String claimNumber = getNumberClaim(HibernateUtil.getConnection());
            //
            //            this.claim.setClaimNumber(claimNumber);
            if (StringUtil.isEmptyOrNullValue(claim.getExcessClaimEventID())) {
                claim.setExcessClaimEventID(null);
            }
            this.claim.setAmountToPay(0.0f);
            this.claim.setOperationPK(policy.getOperationPK().getPk());
            this.claim.save();
            if (declarationId > 0) {
                ClaimDeclarationImpl declaration = ClaimDeclarationImpl.load(declarationId);
                declaration.setClaim(claim);
            }
            this.claimId = Long.valueOf(claim.getPk());
            log.debug("***** claimId = " + this.claimId);

            Collection selectedClaimRiskUnit = claimCreationBean.getSelectedRiskUnits();
            log.debug("*****1selectedClaimRiskUnit = " + selectedClaimRiskUnit);
            if (selectedClaimRiskUnit == null) {
                Enumeration enumer = policy.getRiskUnits();
                selectedClaimRiskUnit = new ArrayList();
                while (enumer.hasMoreElements()) {
                    AgregatedRiskUnit aru = (AgregatedRiskUnit) enumer.nextElement();
                    selectedClaimRiskUnit.add(aru.getPk() + "-" + aru.getOperationPK().getPK());
                }
            }
            Enumeration riskUnits = getSelectedRiskUnitEnumeration(selectedClaimRiskUnit);
            log.debug("-----2selectedClaimRiskUnit=" + selectedClaimRiskUnit);
            while (riskUnits.hasMoreElements()) {
                AgregatedRiskUnit aru = (AgregatedRiskUnit) riskUnits.nextElement();
                log.debug("-----aru=" + aru);
                ClaimRiskUnit cru = new ClaimRiskUnit(aru);
                cru.setContainer(this.claim);
                cru.setOperationPK(aru.getOperationPK().getPk());
                cru.save();
            }
            this.claim.update();
            HibernateUtil.flush();
        } catch (Exception e) {
            ExceptionUtil.handleException(Exceptions.CCCouldNotCreate, e);
        }
    }

    public static void validateEventClaimPolicy(EventClaim eventClaim, AgregatedPolicy policy) {
        Map<Long, CoverageTitle> coverages = new HashMap<Long, CoverageTitle>();
        Collection<ClaimType> claimTypes = eventClaim.getAllClaimTypes().values();
        for (ClaimType claimType : claimTypes) {
            Collection<EventClaimCoverage> claimCoverages = claimType.getAllEventClaimCoverages().values();
            for (EventClaimCoverage claimCoverage : claimCoverages) {
                coverages.put(claimCoverage.getCoverageTitle().getId(),claimCoverage.getCoverageTitle());

            }
        }

        Enumeration<AgregatedRiskUnit> riskUnits = policy.getRiskUnits();
        while (riskUnits.hasMoreElements()) {
            Enumeration<AgregatedInsuranceObject> insuranceObjects = riskUnits.nextElement().getInsuranceObjects();
            while (insuranceObjects.hasMoreElements()) {
                Enumeration<EvaluatedCoverage> evaluatedCoverages = insuranceObjects.nextElement().getEvaluatedCoverages();
                while (evaluatedCoverages.hasMoreElements()) {
                    if (coverages.containsKey(evaluatedCoverages.nextElement().getConfiguratedCoverage().getCoverageTitle().getId())){
                        return;
                    }

                }
            }
        }
        throw new ApplicationException("The policy selected doesn't have any coverages needed for the event claim "+eventClaim.getDesc()+".", Severity.ERROR);
    }

    public Collection getClaimRiskUnits() throws ApplicationExceptionChecked {
        log.info(" *** METHOD NAME  " + "getClaimRiskUnits");
        Vector crus = new Vector();
        Collection<ClaimRiskUnit> claimRiskUnits = getClaim().getClaimRiskUnitsList();
        log.debug("claimRiskUnits=" + claimRiskUnits);
        for (ClaimRiskUnit cru : claimRiskUnits) {
            SearchBean search = new SearchBean(cru.getPk(), cru.getDesc());
            crus.add(search);
        }
        return crus;
    }

    /**
     * @param ruID
     * @param role
     * @return
     * @throws Exception
     */
    public Collection getParticipationsData(String ruID, RoleList roleList) throws Exception {
        return getThirdPartyData(getParticipations(ruID, roleList));
    }

    /**
     * @param aio
     * @param policyForce
     * @return
     * @throws ApplicationExceptionChecked
     */
    public Collection getCoveragesIO(AgregatedInsuranceObject aio, boolean policyForce) throws ApplicationExceptionChecked {
        Vector result = new Vector();
        reloadClaim();

        ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();


        ClaimUtil.fillEvaluator(policy.getProduct().getDCO(), evaluator);
        ClaimType claimType = null;

        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        ClaimInsuranceObject claimInsuranceObject = null;
        for (ClaimRiskUnit claimRiskUnit : claimRiskUnits) {
            if (claimRiskUnit.getAgregatedRiskUnit() != null) {
                ClaimUtil.fillEvaluator(claimRiskUnit.getAgregatedRiskUnit().getDCO(), evaluator);
            }
            evaluator.getTablaSimbolos().put(EvaluationConstants.RISK_UNIT_PK, claimRiskUnit.getRiskUnitId(), Double.valueOf(claimRiskUnit.getRiskUnitId()));
            claimInsuranceObject = claimRiskUnit.getClaimInsuranceObjects().get(aio.getDesc());
            if (claimInsuranceObject != null) {
                claimType = (ClaimType) claimInsuranceObject.getDamage().getCOT();
                evaluator.evaluateConfigurableObject(claimInsuranceObject.getDamage());
//                ClaimUtil.fillEvaluator(claimInsuranceObject.getDamage(), evaluator);
                break;
            }
        }
        EventClaim eventClaim = claim.getEventClaim();
        log.debug("Event: " + eventClaim.getDesc());
        ClaimType realClaimType = eventClaim.getClaimTypeByPK(claimType.getId());
        evaluator.addSymbol(EvaluationConstants.CLAIM_NUMBER, claim.getClaimNumber(), 0.0, false);

        List<EvaluatedCoverage> ecList = getEvaluatedCoverageList(aio);
        for (EvaluatedCoverage ec : ecList) {
            Hashtable has = new Hashtable();
            ec.publishAllSymbolsToUp(evaluator);
            //Las coberturas configuradas en el EventClaim en el Product Tool.
            has.putAll(realClaimType.getEventClaimCoverages());
            // Voy a comparar por descripciones pues no se si los PKs son similares.
            boolean notContained = true;
            Enumeration en = has.elements();
            log.debug("evaluatedCoverage.getDesc(): " + ec.getDesc());
            while (en.hasMoreElements() && notContained) {
                EventClaimCoverage ecc = (EventClaimCoverage) en.nextElement();
                log.debug("ecc.getDesc(): " + ecc.getDesc());
                if (ecc.getDesc().equalsIgnoreCase(ec.getDesc())) {
                    notContained = false;
                }
            }
            if (notContained) {
                log.debug("paso por aqui");
                continue;
            }

            int productType = policy.getProduct().getProductBehaviour().getProductType();
            //evaluatedCoverage.getState().

            boolean isLifeOrWarranty = (productType == ProductBehaviour.LIFE_PRODUCT) || (productType == ProductBehaviour.WARRANTY_PRODUCT);
            log.debug("isLifeOrWarranty = " + isLifeOrWarranty);

            evaluator.addSymbol(EvaluationConstants.CURRENT_ID_COVERAGE, ec.getPk(), new Double(0), true);
            String ocurrenceDateStr = DateUtil.getDateToShow(getOcurrenceDate());
            evaluator.addSymbol(AcseleConf.getProperty(OCURRENCE_DATE), ocurrenceDateStr,
                    new Double(Funciones.dateTransformer.toNumber(ocurrenceDateStr)), true);
            ClaimUtil.fillEvaluator(ec.getDCO(), evaluator);

            if (aio != null) {
                evaluator.getTablaSimbolos().put("INSURANCEOBJECTPK", aio.getPk(), Double.valueOf(aio.getPk()));
            }

            String currencyName = evaluatorPublishPolicyCurrency(evaluator, ec);

            AgregatedRiskUnit aru = claimInsuranceObject.getContainer().getAgregatedRiskUnit();
            ClaimsCoverageConfiguration ccc = this.getClaimCoverageConfiguration(aru.getPk(), aio.getPrimaryKey(), ec);
            String resLimitExp = ccc.getMaxReserveAmount();
            String resLimitExpMin = ccc.getMinReserveAmount();
            String resInitialExp = ccc.getInitialReserveAmount();
            // TODO (RR) si algun dia se quita el calculo de la reserva limite recordar de quitar tambien
            // el evaluador

            double reservLimitMin = evaluator.evaluate(resLimitExpMin);
            double reservInitial = evaluator.evaluate(resInitialExp);
            evaluator.getTablaSimbolos().put("InitialReserve", String.valueOf(reservInitial), reservInitial);
            double reservLimit = evaluator.evaluate(resLimitExp);

            reservLimit = PrecisionUtil.round(reservLimit, PrecisionUtil.CLAIM);
            reservLimitMin = PrecisionUtil.round(reservLimitMin, PrecisionUtil.CLAIM);

            String reseveLimit = String.valueOf(reservLimit);  //agotamiento de coberturas
            String reseveInitial = String.valueOf(reservInitial);
            evaluator.addSymbol(EvaluationConstants.RESERVE_COVERAGE, reseveLimit, reservLimit, false);
            ClaimComposerWrapper.addClaimHistorical(this.getClaim(), ClaimHistoricalOperationType.REJECTED_PAYMENT_ORDER);
            String reseveLimitMin = String.valueOf(reservLimitMin);  //agotamiento de coberturas
            //String reseveInitial = String.valueOf(reservInitial);
            evaluator.addSymbol(EvaluationConstants.RESERVE_COVERAGE_MIN, reseveLimitMin, reservLimitMin, false);

            SearchBean search = null;
            //if(!reserve.getEvaluatedCoverage().isValidDate(reserve.getContainer().getContainer().getContainer().getOcurrenceDate())){

            //Verifica si la fecha de ocurrencia es retroactiva
            Date dateClaim;
            Date retroactiveDate = com.consisint.acsele.openapi.claim.ClaimValidator.getRetroactivityDateClaim(getOcurrenceDate(), String.valueOf(policy.getId()));
            if (retroactiveDate != null) {
                dateClaim = retroactiveDate;
            } else
                dateClaim = getOcurrenceDate();

            if ((!isLifeOrWarranty) && !((ec.getInitialDate().compareTo(dateClaim) <= 0))
                    || (policy.getOperationPK().getStatus() != Versionable.STATUS_APPLIED)) {
                // no esta vigente...policy.getStatus()-->Cambio para el ticket TckN4441

                ResourceBundle rb = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
                search = new SearchBean(ec.getPK(), ec.getDesc(), ec.getDesc() + "(" + rb.getString("claim.coverage.date") + ")");
                search.setStatus(CoverageReserveStatus.NOT_IN_FORCE.getValue());
                //continue;
            } else {
                search = new SearchBean(ec.getPK(), ec.getDesc());
                search.setStatus(CoverageReserveStatus.IN_ANALYSIS.getValue());
            }

            if (reseveLimit != null) {
                search.setAmount(reseveLimit);
            }
            if (reseveLimitMin != null) {
                search.setAmountMin(reseveLimitMin);
            }
            if (reseveInitial != null) {
                search.setReserveAmount(reseveInitial);
            }

            search.setCurrency(currencyName);

            search.setMaxNumberOfPayments(this.getMaximumBenefitPayments(ec));
            search.setBeneficiaries(getBeneficiariesForPayments(ec));
            search.setEvaluatedCoverage(ec);
            result.add(search);
        }
        return result;
    }

    public Map<String, SearchBean> getAvailableCoverages(String ruId, String ioId, boolean policyForce) throws ApplicationExceptionChecked {
        AgregatedInsuranceObject aio = claim.getClaimInsuranceObject(ioId).getAgregatedInsuranceObject();
        log.debug("aio en getAvailableCoverages: " + aio);
        Collection datos = getCoveragesIO(aio, policyForce);
        log.debug("Coberturas Filtradas: " + datos);
        Collection covAffecteds = getClaimNormalReserves(ruId, ioId);
        log.debug("Coberturas covAffecteds: " + covAffecteds);
        if (covAffecteds != null) {
            datos.removeAll(covAffecteds);
        }
        Map<String, SearchBean> availableCoverages = new HashMap<String, SearchBean>(1);
        Iterator iterator = datos.iterator();
        while (iterator.hasNext()) {
            SearchBean bean = (SearchBean) iterator.next();
            availableCoverages.put(bean.getPk(), bean);
        }
        log.debug("Coberturas availableCoverages: " + availableCoverages);
        return availableCoverages;
    }

    public Map<String, SearchBean> getAvailableCoveragesToEval(String ruId, String ioId, boolean policyForce) throws ApplicationExceptionChecked {
        AgregatedInsuranceObject aio = getAgregatedInsuranceObjectById(ruId, ioId);
        log.debug("aio en getAvailableCoverages: " + aio);
        Collection datos = getCoveragesIO(aio, policyForce);
        log.debug("Coberturas Filtradas: " + datos);
        Collection covAffecteds = getClaimNormalReserves(ruId, ioId);
        log.debug("Coberturas covAffecteds: " + covAffecteds);
        Map<String, SearchBean> availableCoverages = new HashMap<String, SearchBean>(1);
        Iterator iterator = datos.iterator();
        while (iterator.hasNext()) {
            SearchBean bean = (SearchBean) iterator.next();
            availableCoverages.put(bean.getPk(), bean);
        }
        log.debug("Coberturas availableCoverages: " + availableCoverages);
        return availableCoverages;
    }

    /**
     * @param cruId
     * @param affectedId
     * @return Collection
     * @throws ApplicationExceptionChecked
     */
    public Collection getReservedByConceptsForIo(String cruId, String affectedId) throws ApplicationExceptionChecked {
        reloadClaim();
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        ClaimRiskUnit claimRiskUnit = null;
        for (ClaimRiskUnit cru : claimRiskUnits) {
            if (cru.getAgregatedRiskUnit().getPk().equals(cruId)) {
                claimRiskUnit = cru;
                break;
            }
        }
        if (claimRiskUnit == null) {
            return new Vector();
        }
        ClaimInsuranceObject cio = claimRiskUnit.getClaimInsuranceObjects().get(affectedId);
        if (cio == null) {
            return new Vector();
        }
        return getReservedByConceptsForIo(cio, claimRiskUnit.getDesc());
    }

    /**
     * @param cio
     * @param cru
     * @return Collection
     */
    public Collection getClaimNormalReserves(ClaimInsuranceObject cio, String cru) {
        Vector result = new Vector();
        List correspondenceHistories = null;
        reloadClaim();
        if (cio != null) {
            cio.load();
            Map normalReserves = cio.getNormalReserves();
            if (normalReserves != null) {
                Collection reservesCollection = normalReserves.values();
                Iterator reserves = reservesCollection.iterator();

                while (reserves.hasNext()) {
                    String coverage = "";
                    ClaimNormalReserve reserve = (ClaimNormalReserve) reserves.next();
                    reserve = ClaimNormalReserve.getInstance(Long.valueOf(reserve.getPk()));
                    log.debug("[Reserve] " + reserve.getDesc());
                    double paidAmount = reserve.getAmountPaymentsforReserve();
                    double payAmount = reserve.getAmountPaymentsforReserveByStatus(new Date(), PaymentOrderStatus.PAID_STATE.getValue());
                    double amountToPay = paidAmount - payAmount;
                    boolean hasSendingPlan = false;
                    try {
                        correspondenceHistories = CorrespondenceHistory.getPendingCorrespondenceByCnrID(Long.valueOf(reserve.getPk()));
                        log.info("ClaimComposerWrapper Size de correspondenceHistories : " + correspondenceHistories.size());

                        boolean condition = correspondenceHistories == null || correspondenceHistories.isEmpty();
                        log.debug("condition = " + condition);
                        if (condition) {
                            hasSendingPlan = false;
                        } else {
                            hasSendingPlan = true;
                        }
                    } catch (Exception e) {
                        log.error("Error loading the corresponding Histories: " + e);
                    }
                    SearchBean bean;
                    log.debug("policy = " + policy);
                    log.debug("policy.getProduct = " + policy.getProduct());
                    log.debug("policy.getProduct().getProductBehaviour() = " + policy.getProduct().getProductBehaviour());
                    int productType = policy.getProduct().getProductBehaviour().getProductType();
                    log.debug("productType = " + productType);
                    boolean isLifeOrWarranty = productType == ProductBehaviour.LIFE_PRODUCT || productType == ProductBehaviour.WARRANTY_PRODUCT;
                    log.debug("isLifeOrWarranty = " + isLifeOrWarranty);

                    //Verifica si la fecha de ocurrencia es retroactiva
                    Date dateClaim;
                    Date retroactiveDate = com.consisint.acsele.openapi.claim.ClaimValidator.getRetroactivityDateClaim(reserve.getContainer().getContainer().getContainer().getOcurrenceDate(), String.valueOf(policy.getId()));
                    log.debug("retroactiveDate = " + retroactiveDate);
                    if (retroactiveDate != null) {
                        dateClaim = retroactiveDate;
                    } else
                        dateClaim = reserve.getContainer().getContainer().getContainer().getOcurrenceDate();

                    if ((!isLifeOrWarranty) && !reserve.getEvaluatedCoverage().isValidDate(dateClaim)) {
                        if (!StringUtil.isEmptyOrNullValue(claim.getDoneBy())) {
                            bean = new SearchBean(reserve.getPK(), reserve.getDesc(), "true");
                            bean.setStatus(CoverageReserveStatus.NOT_IN_FORCE.getValue());
                            if (reserve.getDoneBy().equals("migrationlbc")) {
                                bean = new SearchBean(reserve.getPK(), reserve.getDesc());
                                bean.setStatus(CoverageReserveStatus.ACCEPTED.getValue());
                                reserve.setMigrationMark(1);
                            }
                        } else {
                            bean = new SearchBean(reserve.getPK(), reserve.getDesc(), "true");
                            bean.setStatus(CoverageReserveStatus.NOT_IN_FORCE.getValue());
                        }
                    } else {
                        bean = new SearchBean(reserve.getPK(), reserve.getDesc());
                        bean.setStatus(CoverageReserveStatus.getInstance(reserve.getStatus()).getValue());
                    }
                    //If we don't have sending plans configurated the button followed up letters will be disabled
                    log.debug("Setting bean params.");
                    bean.setReserveAmount(ClaimUtil.formatNumber(reserve.getAmount()));
                    bean.setDeductibleAmount(ClaimUtil.formatNumber(reserve.getDeductibleAmount()));
                    bean.setReserveCurrency(reserve.getCurrency().getDescription());
                    bean.setAmount(ClaimUtil.formatNumber(paidAmount));
                    bean.setCru(cru);
                    bean.setCruObj(cio.getContainer());
                    bean.setIoId(cio.getDesc());
                    bean.setEvaluatedCoverageID(reserve.getEvaluatedCoverage().getPk());
                    bean.setHasSendingPlans(hasSendingPlan);
                    bean.setPaidAmount(ClaimUtil.formatNumber(payAmount));
                    bean.setAmountToPay(ClaimUtil.formatNumber(amountToPay));
                    bean.setEvaluatedCoverage(reserve.getEvaluatedCoverage());
                    result.add(bean);
                }
            }
        }
        return result;
    }

    /**
     * @param cruId
     * @param affectedId
     * @return
     * @throws ApplicationExceptionChecked
     */
    public Collection getClaimNormalReserves(String cruId, String affectedId) throws ApplicationExceptionChecked {
        reloadClaim();
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        ClaimRiskUnit claimRiskUnit = null;
        for (ClaimRiskUnit cru : claimRiskUnits) {
            if (cru.getAgregatedRiskUnit().getPk().equals(cruId)) {
                claimRiskUnit = cru;
                break;
            }
        }
        log.debug("claimRiskUnit = " + claimRiskUnit);
        if (claimRiskUnit == null) {
            return new Vector();
        }
        Map<String, ClaimInsuranceObject> claimInsuranceObjects = claimRiskUnit.getClaimInsuranceObjects();
        log.debug("claimInsuranceObjects = " + claimInsuranceObjects.size());
        ClaimInsuranceObject cio = claimInsuranceObjects.get(affectedId);
        log.debug("cio = " + cio);
        if (cio == null) {
            Set<Map.Entry<String, ClaimInsuranceObject>> entries = claimInsuranceObjects.entrySet();
            Iterator<Map.Entry<String, ClaimInsuranceObject>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ClaimInsuranceObject> c = iterator.next();
                String key = c.getKey();
                ClaimInsuranceObject value = c.getValue();
                log.debug("key=" + key);
                log.debug("value=" + value);
            }
            return new Vector();
        }
//        log.debug("[ClaimComposerWrapper - 7237]");
        return getClaimNormalReserves(cio, claimRiskUnit.getDesc());
    }

    /**
     * @param cruId
     * @param affectedId
     * @return SearchBean
     */
    public SearchBean getAmountInClaimNormalReserves(String cruId, String affectedId) {
        Session session = null;
        try {
            session = getHibernateSession();
            Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
            ClaimRiskUnit claimRiskUnit = null;
            for (ClaimRiskUnit cru : claimRiskUnits) {
                if (cru.getAgregatedRiskUnit().getPk().equals(cruId)) {
                    claimRiskUnit = cru;
                    break;
                }
            }
            if (claimRiskUnit == null) {
                return null;
            }
            return getAmountInClaimNormalReserves(claimRiskUnit.getClaimInsuranceObjects().get(affectedId));
        } catch (Exception e) {
            log.error("Error", e);
            return null;
        } finally {
            closeHibernateSession(session);
        }
    }

    /**
     * @param cio
     * @return SearchBean
     */
    private SearchBean getAmountInClaimNormalReserves(ClaimInsuranceObject cio) {
        double reserveAmount = 0.0;
        double paymentAmount = 0.0;
        SearchBean bean = new SearchBean();
        if (cio != null) {
            cio = (ClaimInsuranceObject) HibernateUtil.load(ClaimInsuranceObject.class, cio.getPk());
            if (cio.getNormalReserves() != null && cio.getNormalReserves().size() > 0) {
                Iterator reserves = cio.getNormalReserves().values().iterator();
                while (reserves.hasNext()) {
                    ClaimNormalReserve reserve = (ClaimNormalReserve) reserves.next();
//  Comentado por JCE 2009-11-13 (Esto ya deberia estar cargado por Hibernate)
//                    reserve.setContainer(cio);
                    reserve.load();
                    Iterator payments = reserve.getPaymentOrderList().values().iterator();
                    double paidAmount = 0.0;
                    while (payments.hasNext()) {
                        PaymentOrder paymentOrder = (PaymentOrder) payments.next();
//                        paymentOrder = PaymentOrder.load(new Long(paymentOrder.getPk()));
                        if (paymentOrder != null && paymentOrder.getPaymentOrderStatus() != PaymentOrderStatus.CANCELED_STATE) {
                            paidAmount += paymentOrder.getAmount();
                        }
                    }
//                    reserveAmount += reserve.getAmount().doubleValue(); (JCE) 2009-10-29
                    reserveAmount += reserve.getAmountWithDeductible();
                    paymentAmount += paidAmount;
                }

            }
            bean.setReserveAmount(ClaimUtil.formatNumber(reserveAmount));
            bean.setAmount(ClaimUtil.formatNumber(paymentAmount));
        }
        return bean;
    }

    /**
     * @param cru
     * @param cnr
     * @param crc
     * @return Collection
     */
    private Collection getAffectedObjects(ClaimRiskUnit cru, Vector cnr, Vector crc) {
        Vector result = new Vector();
        String cruId = cru.getDesc();
        if (cru.getClaimInsuranceObjects() != null) {

            log.debug("####### cru.getClaimInsuranceObjects().size() = " + cru.getClaimInsuranceObjects().size());
            Iterator affectedObjects = cru.getClaimInsuranceObjects().values().iterator();
            while (affectedObjects.hasNext()) {
                ClaimInsuranceObject iobject = (ClaimInsuranceObject) affectedObjects.next();
                log.debug("## iobject.getDamageDcoId() =" + iobject.getDamageDcoId());
                iobject.load();
                iobject.setClaimRiskUnitId(Long.parseLong(cru.getPk()));
                SearchBean bean = new SearchBean(iobject.getPk(), iobject.getDesc(), iobject.getLabel());
                bean.setCru(cruId);
                bean.setIoId(iobject.getAioId());
                result.add(bean);
                cnr.addAll(getClaimNormalReserves(iobject, cruId));
                crc.addAll(getReservedByConceptsForIo(iobject, cruId));
            }
        }
        return result;
    }

    /**
     * @param cio
     * @param cru
     * @return Collection
     */
    private Collection getReservedByConceptsForIo(ClaimInsuranceObject cio, String cru) {
        Vector result = new Vector();
        if (cio != null) {
            cio = (ClaimInsuranceObject) HibernateUtil.load(ClaimInsuranceObject.class, cio.getPk());
            if (cio.getReservesByConcept() != null) {
                Iterator reserves = cio.getReservesByConcept().values().iterator();
                while (reserves.hasNext()) {
                    ClaimReserveByConcept reserve = (ClaimReserveByConcept) reserves.next();
                    reserve = (ClaimReserveByConcept) HibernateUtil.load(ClaimReserveByConcept.class, reserve.getPk());
                    SearchBean bean = new SearchBean(reserve.getPK(), reserve.getDesc());
                    bean.setReserveAmount(ClaimUtil.formatNumber(reserve.getAmount()));
                    bean.setReserveCurrency(reserve.getCurrency().getDescription());
                    double paidAmount = reserve.getAmountPaymentsforReserve();
                    bean.setAmount(ClaimUtil.formatNumber(paidAmount));
                    bean.setCru(cru);
                    bean.setIoId(cio.getDesc());
                    result.add(bean);
                }
            }
        }
        return result;
    }

    /**
     * Returns all the thirdParties with beneficiary, client or insurance Roles as a Collection
     * of SearchThirdPartyResult objects
     *
     * @param ioID
     * @param claimReserveId
     */
    public Collection getPolicyThirdParties(String ioID, String claimReserveId) {
        Collection col = getConfiguratedCoverageRoles(ioID, claimReserveId);
        Vector roles = new Vector(col);
        Vector result = new Vector();
        try {
            Vector participations = new Vector(policy.getAllParticipations());
            for (int i = 0; i < participations.size(); i++) {
                Participation participation = (Participation) participations.elementAt(i);
                SearchThirdPartyResult bean = getBeanFromParticipation(participation.getThirdParty().getPk(), participation.getRole());

                if (bean.isActive() && !result.contains(bean)) {
                    result.addElement(bean);
                }
            }
        } catch (Exception e) {
            log.error("Error in getPolicyThirdParties", e);
        }
        return result;
    }

    /**
     * @param ioID
     * @param claimReserveId
     * @return Collection
     */
    public Collection getConfiguratedCoverageRoles(String ioID, String claimReserveId) {
        Plan plan = this.policy.getProduct().get(AgregatedInsuranceObject.getPlanDesc(ioID));
        AgregatedInsuranceObjectType aiot = plan.getInsuranceObjectType(AgregatedInsuranceObject.getTemplateDesc(ioID));
//        log.debug("claimReserveId = " + claimReserveId);
//        log.debug("aiot.getCoverageTypes() = " + aiot.getCoverageTypes().getHashtable());
        ConfiguratedCoverage cc = aiot.getConfiguratedCoverage(claimReserveId);
//        log.debug("[>>>>>] cc.getPk() = " + cc.getPk());
        return cc.getRolesDescriptions();
    }

    /**
     * @param thirdPartyPk
     * @param role
     * @return SearchThirdPartyResult
     */
    public static SearchThirdPartyResult getBeanFromParticipation(Long thirdPartyPk, Role role) {
        com.consisint.acsele.thirdparty.api.ThirdParty thirdParty = ThirdPartyImpl.Impl.load(thirdPartyPk);
        String roleDesc = role.getDynamic().getDCO().getDesc();
        com.consisint.acsele.uaa.api.Role roleImpl = com.consisint.acsele.uaa.api.Role.Impl.load(roleDesc);
        SearchThirdPartyResult bean = new SearchThirdPartyResult("" + thirdParty.getId(), thirdParty.getName(),
                roleImpl);
        bean.setActive(thirdParty.isEnabled());
        return bean;
    }

    /**
     * @param evaluatedCoverageId
     * @return Collection
     */
    public Collection getCoverageThirdPartiesWithRole(String evaluatedCoverageId) {
        Set result = new HashSet(1);
        Collection objs = policy.getParticipationsWithRole(evaluatedCoverageId);
//        Collection pks = policy.getParticipationsWithRole(evaluatedCoverageId);      PARA QUE CARGAR LOS PKS PARA ESO CARGO EL OBJETO DE ONE!
        for (Iterator iterator = objs.iterator(); iterator.hasNext(); ) {
//            String participationPk = (String) iterator.next();
//            Participation participation = new Participation(participationPk);
            Participation participation = (Participation) iterator.next();
            ;
//            participation.loadByPK();
            SearchThirdPartyResult bean = getBeanFromParticipation(participation.getThirdParty().getPk(), participation.getRole());
            result.add(bean);
        }
        return result;
    }

    /**
     * Returns all the beneficiry by Coverage
     */
    private Collection getCoverageThirdParties(EvaluatedCoverage evaluatedCoverage) throws Exception {
        Set result = new HashSet(1);

        Session session = null;
        try {
            session = getHibernateSession();
            ConfiguratedCoverage configuratedCoverage = evaluatedCoverage.getConfiguratedCoverage();
            configuratedCoverage.load();
            Vector thirdParties = configuratedCoverage.getThirdParties();
            Hashtable thirdPartyRoles = configuratedCoverage.getThirdPartyAndRoleIds();
            for (int i = 0; i < thirdParties.size(); i++) {
                ThirdParty thirdParty = (ThirdParty) thirdParties.get(i);
                String thirdPartyId = thirdParty.getPk().toString();
                long roleId = Long.parseLong((String) thirdPartyRoles.get(thirdPartyId));
                Role role = Role.getInstance(roleId, thirdParty.getPk());
                SearchThirdPartyResult bean = getBeanFromParticipation(thirdParty.getPk(), role);
                result.add(bean);
            }
        } catch (Exception e) {
            log.error("Error", e);
            throw e;
        } finally {
            closeHibernateSession(session);
        }
        return result;
    }

    /**
     * @param evaluatedCoverage
     * @return Collection
     * @throws Exception
     */
    private Collection getPreferentialThirdParties(EvaluatedCoverage evaluatedCoverage) throws Exception {
        Set result = new HashSet(1);

        Session session = null;
        try {
            session = getHibernateSession();
            Collection<Participation> participations = evaluatedCoverage.getParticipationCollection();
            for (Participation participation : participations) {
                ThirdParty thirdParty = ThirdParty.getInstance(participation.getThirdParty().getPk());
                String roleDesc = participation.getRole().getDynamic().getDCO().getDesc();
                com.consisint.acsele.uaa.api.Role roleImpl = com.consisint.acsele.uaa.api.Role.Impl.load(roleDesc);

                SearchThirdPartyResult bean = new SearchThirdPartyResult(participation.getThirdParty().getPk().toString(), thirdParty.getName(),
                        roleImpl);
                bean.setActive(thirdParty.getStatic().getStatus());

                if ((participation.isPreferencial() == Util.TRUE)) {
                    result.add(bean);
                }
            }
        } catch (Exception e) {
            log.error("Error", e);
            throw e;
        } finally {
            closeHibernateSession(session);
        }
        return result;
    }

    /**
     * Verifies if the state is a cancel state
     *
     * @return a boolean
     */
    public boolean isCancelState(String state) {
        String cancelledStates = AcseleConf.getProperty("cancelledstates");

        StringTokenizer st = new StringTokenizer(cancelledStates, ",");
        while (st.hasMoreTokens()) {
            if (st.nextToken().equals(state)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies if the state is a suspended state
     *
     * @return a boolean
     */
    private boolean isSuspendedState(String state) {
        String suspendedState = AcseleConf.getProperty("suspendedState");

        StringTokenizer st = new StringTokenizer(suspendedState, ",");
        while (st.hasMoreTokens()) {
            if (st.nextToken().equals(state)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a new states for the given payment orders
     *
     * @param paymentOrders
     * @param state
     */
    public void setPaymentOrdersState(Collection paymentOrders, PaymentOrderStatus paymentOrderStatus) {

        Session session = null;
        Transaction transaction = null;
        Hashtable<String, String> listNormal = new Hashtable<String, String>();   //For payment with the state Pending.
        Hashtable<String, String> listNormal_ = new Hashtable<String, String>();  //For payment with the state Canceled.
        Hashtable<String, String> listConcept = new Hashtable<String, String>();  //For payment with the state Pending.
        Hashtable<String, String> listConcept_ = new Hashtable<String, String>(); //For payment with the state Canceled.

        log.debug("Starting setPaymentOrdersState...");
        try {
            session = getHibernateSession();
            transaction = beginTransaction(false, session);
            if (!asl) {
                claim = Claim.getInstance(Long.valueOf(claim.getPk()));
            }
            Collection<Payment> paymentsNormalReserve = claim.getPaymentsNormalReserveList();
            Collection<Payment> paymentsConceptReserve = claim.getPaymentsConceptReserveList();

            //Loading the Normal Reserve Payments with Pending, Cancelled and Paid status.
            for (Payment payment_ : paymentsNormalReserve) {
                log.debug("[**] payment_.getPk() = " + payment_.getPk() + "(Normal Reserve)");
                log.debug("[**] payment_.getState()  = " + payment_.getPaymentStatus());
                if (payment_.getPaymentStatus() == PaymentStatus.PENDING_STATE) {
                    listNormal.put(payment_.getThirdParty().getPK(), String.valueOf(payment_.getPk()));
                }
                if ((payment_.getPaymentStatus() == PaymentStatus.CANCELED_STATE) ||
                        (payment_.getPaymentStatus() == PaymentStatus.PENDING_APPROVE_STATE)) {
                    listNormal_.put(payment_.getThirdParty().getPK(), String.valueOf(payment_.getPk()));
                }
            }

            //Loading the Concept Reserve Payments with Pending, Cancelled and Paid status.
            for (Payment payment_ : paymentsConceptReserve) {
                if (payment_.getPaymentStatus() == PaymentStatus.PENDING_STATE) {
                    listConcept.put(payment_.getThirdParty().getPK(), String.valueOf(payment_.getPk()));
                }
                if (payment_.getPaymentStatus() == PaymentStatus.CANCELED_STATE) {
                    listConcept_.put(payment_.getThirdParty().getPK(), String.valueOf(payment_.getPk()));
                }
            }

            log.debug("[**] listNormal_ = " + listNormal_);
            log.debug("[**] listNormal = " + listNormal);
            Iterator iterator = paymentOrders.iterator();
            //Hibernate wasn't updating the payment orders so I have to do this :S
            while (iterator.hasNext()) {
                PaymentOrder paymentOrder = (PaymentOrder) iterator.next();
                PaymentOrder paymentOrderFromSession = PaymentOrderService.Impl.getInstance().load(paymentOrder.getPk());
                paymentOrder.setPaymentOrderStatus(paymentOrderStatus);
                log.debug("[**] paymentOrder.getPk()+ = " + paymentOrder.getPk() + " - paymentOrder.getAmount()= " + paymentOrder.getAmount()
                        + " - state = " + paymentOrderStatus);

                // save State in the history payments order
//                log.debug("Grabo en el Historico de Pagos.....");

                paymentOrder.setHistoryStatePaymentsFile();
                paymentOrderFromSession.setPaymentOrderStatus(paymentOrderStatus);

                //Verifying the Payment Order Reserve Type.
                boolean isNormal = false;
                if (paymentOrder.getClaimReserve() instanceof ClaimNormalReserve) {
                    isNormal = true;
                }
                if (paymentOrderStatus == PaymentOrderStatus.APPROVED_STATE) {
                    //We need to register the date when the payment order is approved,
                    //we should do that with every status
                    log.debug("Approving the payment order...");
                    Date actualDate = new Date();
                    paymentOrder.setOperationDate(actualDate);
                    paymentOrderFromSession.setOperationDate(actualDate);
                    String allow = AcseleConf.getProperty("allowTotalize");
//                    log.debug("allow:" + allow);

                    if (!allow.equalsIgnoreCase("yes")) {
                        long id = paymentOrder.getThirdPartyId();
                        int state_ = PaymentStatus.PENDING_STATE.getValue();

                        if (isNormal) {
                            listNormal = processPaymentState(listNormal, id, paymentOrder, actualDate, paymentOrderFromSession, session, state_);
                        } else {
                            listConcept = processPaymentState(listConcept, id, paymentOrder, actualDate, paymentOrderFromSession, session, state_);
                        }
                    }

                    ClaimComposerWrapper.addClaimHistorical(this.getClaim(), ClaimHistoricalOperationType.APPROVED_PAYMENT_ORDER);

                    createClaimEntry(policy, paymentOrder);

                } else if (paymentOrderStatus == PaymentOrderStatus.CANCELED_STATE) {
                    long id = paymentOrder.getThirdPartyId();
                    Date actualDate = new Date();
                    paymentOrder.setOperationDate(actualDate);
                    paymentOrderFromSession.setOperationDate(new Date());
                    int state_ = PaymentStatus.CANCELED_STATE.getValue();

                    //Verifying if exist a Payment for this TP with status Cancelled.
                    if (isNormal) {

                        // if the paymentorder is related to a pending payment then brings the pending payment
                        // to the cancel state list.
                        log.debug("[DFR] trying to find if the paymentorder was related to any payment in pending state: (" + id + ")" + listNormal
                                .containsKey(String.valueOf(id)));
                        if (listNormal.containsKey(String.valueOf(id))) {
                            String pendingPaymentPk = listNormal.get(String.valueOf(id));
                            Payment pendingPayment = Payment.load(Long.valueOf(pendingPaymentPk));
                            listNormal.remove(String.valueOf(id));
                            listNormal_.put(String.valueOf(id), pendingPayment.getPK());
                        }
                        Payment paymentOld = paymentOrder.getPayment();

                        if (paymentOld != null) {
                            HibernateUtil.getActualSession()
                                    .evict(HibernateUtil.getActualSession().get(paymentOld.getClass(), paymentOld.getPk()));
                        }

                    } else {

                        if (listConcept.containsKey(String.valueOf(id))) {
                            String pendingPaymentPk = listConcept.get(String.valueOf(id));
                            Payment pendingPayment = Payment.load(Long.valueOf(pendingPaymentPk));
                            listConcept.remove(String.valueOf(id));
                            listConcept_.put(String.valueOf(id), pendingPayment.getPK());
                        }
                        Payment paymentOld = paymentOrder.getPayment();

                        if (paymentOld != null) {
                            HibernateUtil.getActualSession()
                                    .evict(HibernateUtil.getActualSession().get(paymentOld.getClass(), paymentOld.getPk()));
                        }
                    }
                    AuditTrailManager.generateClaimPaymentAuditTrail(new Date(), paymentOrder, CustomAuditItem.CANCEL_PAYMENT_ORDER);
                    //----  Revertir Operaciones de reaseguro (ACSELE-9387) -----
                    if (!"totalize".equals(AcseleConf.getProperty("claim.reinsurance.cessions.generated"))) {
                        try {
                            ClaimNormalReserve cnr = (ClaimNormalReserve) paymentOrder.getClaimReserve();
                            generateReserveReinsuranceDistribution(cnr, "-" + paymentOrder.getAmount().toString(), Constants.PAYMENT_CLAIM, new java.sql.Date(cnr.getDate().getTime()), null);
                        } catch (Exception e) {
                            log.error("Error reinsuring.", e);
                        }
                    }
                    //------------------------------------------------------------------
                    ClaimComposerWrapper.addClaimHistorical(this.getClaim(), ClaimHistoricalOperationType.PAYMENT_ORDER_CANCELED);

                } else if (paymentOrderStatus == PaymentOrderStatus.REJECTED_STATE) {
                    log.debug("Rejecting the payment order...");
                    log.debug("[**] claim = " + claim);
                    log.debug("[**] claim.getPK() = " + claim.getPK());
                    log.debug("[**] claim.getClaimNumber() = " + claim.getClaimNumber());
                    log.debug("[**] paymentOrder.getPk() = " + paymentOrder.getPk());

                    AuditTrailManager manager = AuditTrailManager.getInstance();

                    if (policy == null) {
                        policy = claim.getAgregatedPolicy();
                    }
                    if (operationPK == null) {
                        getOperationPK();
                    }
                    log.debug("[**] policy = " + policy);
                    log.debug("[**] operationPK = " + operationPK);
                    log.debug("[**] policy.getPolicyNumber() = " + policy.getPolicyNumber());
                    AuditTrailManager.generateClaimPaymentAuditTrail(new Date(), paymentOrder, CustomAuditItem.PAYMENT_DENIAL);
                }
                session.update(paymentOrder);
            }

            HibernateUtil.cleanCache();
            commitTransaction(transaction, session);
            log.debug("Ending setPaymentOrdersState...");
        } catch (Exception e) {
            log.error("There was an error changing the state of the paymentorder: ", e);
            rollbackTransaction(transaction, session);
            throw new TechnicalException(Exceptions.CCErrorChangingOrderPaymentState, Severity.FATAL, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    /* This method permit to verify if exist a Payment created for a TP.
If it exist...update the values, If not exist...it is created*/

    private Hashtable processPaymentState(Hashtable<String, String> list, long id, PaymentOrder paymentOrder, Date actualDate, PaymentOrder paymentOrderFromSession,
                                          Session session, int state) {

        try {
            Payment payment = new Payment(paymentOrder);
            double mto = (paymentOrder.getAmount().doubleValue() * (paymentOrder.getRefundPercentage() == 0.0f ? 1.0f
                    : paymentOrder.getRefundPercentage()));
            mto -= paymentOrder.getDistributionAmount();
            mto -= paymentOrder.getAmountFranchise();
            payment.setDate(actualDate);
            payment.setPaymentStatus(PaymentStatus.getInstance(state));
            payment.setThirdParty(paymentOrder.getThirdParty());
            payment.setAmount(new Double(mto));
            payment.setPaidAmount(new Double(0F));
            paymentOrder.setPayment(payment);
            ClaimReserve reserve = paymentOrder.getClaimReserve();
            ClaimRiskUnit claimRiskUnit;
            if (reserve instanceof ClaimNormalReserve) {
                payment.setReserveType(ReserveType.NORMAL_RESERVE.getValue());
                claimRiskUnit = ((ClaimNormalReserve) reserve).getContainer().getContainer();
            } else {
                payment.setReserveType(ReserveType.CONCEPT_RESERVE.getValue());
                claimRiskUnit = ((ClaimReserveByConcept) reserve).getContainer().getContainer();
            }
            payment.setCoverageDescription(reserve.getDesc());
            payment.setReserveCurrencyId(String.valueOf(reserve.getCurrency().getId()));
            payment.setPaidCurrencyId(String.valueOf(reserve.getCurrency().getId()));
            payment.setClaimId(this.claim.getPK());
            payment.setDoneBy(UserInfo.getUser());
            payment.save();
            ClaimRiskUnitPayment claimRiskUnitPayment = new ClaimRiskUnitPayment(claimRiskUnit, payment);
            claimRiskUnitPayment.save();
            list.put(payment.getThirdParty().getPK(), String.valueOf(payment.getPk()));
            paymentOrderFromSession.setPayment(payment);
            session.flush();
            return list;
        } catch (Exception e) {

            throw new ApplicationException("Error en processPaymentState", Severity.FATAL);
        }
    }


    /**
     * Sets the reserve amount to 0, the reserves by concept are conditioned
     * by the "freeReserveConcepts" property
     */
    public void freeReserves(String reason) {
        ClaimInsuranceObject cio;
        Iterator paymentsReserve;
        double amountPayments;
        double normalReserveAmount;
        double amountEntry;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit ru : claimRiskUnits) {
            for (ClaimInsuranceObject claimInsuranceObject : ru.getClaimInsuranceObjects().values()) { //the insurance object list
                cio = claimInsuranceObject;
                cio = (ClaimInsuranceObject) HibernateUtil.load(ClaimInsuranceObject.class, cio.getPk());
                paymentsReserve = cio.getNormalReserves().values().iterator();
                while (paymentsReserve.hasNext()) {  //the reserve payments list
                    ClaimNormalReserve cnr = (ClaimNormalReserve) paymentsReserve.next();
                    try {
                        cnr.load();
                        EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                        Plan plan = ec.getPlan();
                        String planId = plan.getPk();
                        long configuratedCoverage = ec.getConfiguratedCoverageOA().getId();


                        //amountPayments = cnr.getAmountPaymentsforReserve();
                        amountPayments = cnr.getAmountPaymentsforFreeReserves(new Date());
//                        double normalReserveAmount = cnr.getAmount().doubleValue(); (JCE) 2009-10-27
                        normalReserveAmount = cnr.getAmountWithDeductible();
                        amountEntry = normalReserveAmount - amountPayments;

                        entryReserve(amountEntry, planId, configuratedCoverage,
                                ClaimReserveAdjust.LIBERATION, ec, cnr.getDoneBy(),
                                cnr.getCurrency().getIsoCode(), cnr);

                        ClaimReserveAdjust claimReserveAdjust = cnr.freeReserve(reason);
                        try {
                            ClaimHistorical claimHistorical = new ClaimHistorical();
                            String legacyType = cnr.getLegacyType();
                            legacyType = legacyType == null ? OIMTPClaimEnum.ADMINISTRATIVA.getLabel() : legacyType;
                            claimHistorical.generateHistoricalWithMovement(claim, ClaimHistoricalOperationType.ADD_RESERVE, ClaimHistoricalMovementType.CHANGE_RESERVE, Long.parseLong(claimReserveAdjust.getPK()), legacyType);
                        } catch (Exception e) {

                        }
                        generateReserveReinsuranceDistribution(cnr, String.valueOf(claimReserveAdjust.getAmount() * -1), Constants.RESERVE_CLAIM, new java.sql.Date(new Date().getTime()), ReserveAdjustType.DECREASE);
                    } catch (ApplicationExceptionChecked applicationExceptionChecked) {
                        log.error("The amount of the reserve " + cnr.getDesc() + " is already 0");
                    }
                }
                if (AcseleConf.getProperty("freeReserveConcepts").equals("1")) {
                    paymentsReserve = cio.getReservesByConcept().values().iterator();
                    while (paymentsReserve.hasNext()) {  //the reserve payments list
                        ClaimReserveByConcept cnr = (ClaimReserveByConcept) paymentsReserve.next();
                        try {
//                            amountPayments = cnr.getAmountPaymentsforReserve();
                            amountPayments = cnr.getAmountPaymentsforFreeReserves(new Date());
                            amountEntry = cnr.getAmount() - amountPayments;
                            entryReserve(amountEntry, ClaimReserveAdjust.LIBERATION,
                                    cnr.getDoneBy(), cnr.getCurrency().getIsoCode(), cnr);
                            cnr.freeReserve(ClaimReserveAdjust.CLAIM_CLOSE_ADJUST);
                        } catch (ApplicationExceptionChecked applicationExceptionChecked) {
                            log.error("The amount of the reserve " + cnr.getDesc() + " is already 0");
                        }
                    }
                }
            }
        }
    }

    /**
     * After the claim was closed, when reopen, we have to rollback the reserves
     */
    public void rollbackReserves() {
        ClaimInsuranceObject cio;
        //Iterator paymentsReserve;
        ClaimReserveAdjust newReserveAdjust, lastReserveAdjust;
        Double lastAmount;
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit ru : claimRiskUnits) {
            Iterator io = ru.getClaimInsuranceObjects().values().iterator();
            while (io.hasNext()) { //the insurance object list
                cio = (ClaimInsuranceObject) io.next();
                cio = (ClaimInsuranceObject) HibernateUtil.load(ClaimInsuranceObject.class, cio.getPk());
                //Normal Reserves
                //I didn't extract this on a method because it wont be usefull for other things
                Iterator paymentsReserve = cio.getNormalReserves().values().iterator();
                while (paymentsReserve.hasNext()) {  //the reserve payments list
                    ClaimNormalReserve cnr = (ClaimNormalReserve) paymentsReserve.next();
                    lastReserveAdjust = cnr.getLastReserveAdjust();
                    //SupN929: Validation reason to reopen the Claim
                    String reason = lastReserveAdjust.getReason();
                    if ((!StringUtil.isEmptyOrNullValue(reason)) && (reason.equals(ClaimReserveAdjust.CLAIM_CLOSE_ADJUST) || reason.equals(ClaimReserveAdjust.CLAIM_FINAL_PAYMENT))) {
                        lastAmount = lastReserveAdjust.getAmount();
                        newReserveAdjust = new ClaimReserveAdjust(cnr.getDesc(), new Date(), lastAmount, AcseleConf.getProperty("systemUser"),
                                ClaimReserveAdjust.CLAIM_REOPEN_ADJUST, ReserveAdjustType.INCREASE.getValue());

                        newReserveAdjust.setClaimReserveId(cnr.getPk());
                        newReserveAdjust.save();
                        try {
                            ClaimHistorical claimHistorical = new ClaimHistorical();
                            claimHistorical.generateHistoricalWithMovement(claim, ClaimHistoricalOperationType.ADD_RESERVE, ClaimHistoricalMovementType.CHANGE_RESERVE, Long.parseLong(newReserveAdjust.getPK()), ClaimUtil.getValidatedLegacyType(cnr));
                        } catch (Exception e) {

                        }
                        cnr.addClaimReserveAdjust(newReserveAdjust);
                        EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                        ClaimValidationService claimValidationService = ((ClaimValidationService) BeanFactory.getBean(ClaimValidationService.class));
                        claimValidationService.claimreOpening(claim.getId(), ec);
                        Plan plan = ec.getPlan();
                        String planId = plan.getPk();
                        long configuratedCoverage = ec.getConfiguratedCoverageOA().getId();

                        double amountPayments = cnr.getAmountPaymentsforReserve();
                        double normalReserveAmount = cnr.getAmountWithDeductible();
                        double amountEntry = normalReserveAmount - amountPayments;

                        entryReserve(amountEntry, planId, configuratedCoverage, ReserveAdjustType.INCREASE.getValue(), ec, cnr.getDoneBy(),
                                cnr.getCurrency().getIsoCode(), cnr);
                        generateReserveReinsuranceDistribution(cnr, String.valueOf(newReserveAdjust.getAmount()), Constants.RESERVE_CLAIM, new java.sql.Date(cnr.getDate().getTime()), ReserveAdjustType.INCREASE);
                    }
                }
                //Reserves by Concept
                if (AcseleConf.getProperty("freeReserveConcepts").equals("1")) {
                    paymentsReserve = cio.getReservesByConcept().values().iterator();
                    while (paymentsReserve.hasNext()) {  //the reserve payments list
                        ClaimReserveByConcept crc = (ClaimReserveByConcept) paymentsReserve.next();
                        lastReserveAdjust = crc.getLastReserveAdjust();
                        //SupN929: Validation reason to reopen the Claim
                        String reason = lastReserveAdjust.getReason();
                        if ((!StringUtil.isEmptyOrNullValue(reason)) && reason.equals(ClaimReserveAdjust.CLAIM_CLOSE_ADJUST)) {
                            lastAmount = lastReserveAdjust.getAmount();
                            newReserveAdjust = new ClaimReserveAdjust(crc.getDesc(), new Date(), lastAmount, AcseleConf.getProperty("systemUser"),
                                    ClaimReserveAdjust.CLAIM_REOPEN_ADJUST, ReserveAdjustType.INCREASE.getValue());

                            newReserveAdjust.setClaimReserveId(crc.getPk());
                            newReserveAdjust.save();
                            ClaimNormalReserve cnr = (ClaimNormalReserve) paymentsReserve.next();
                            EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                            ClaimValidationService claimValidationService = ((ClaimValidationService) BeanFactory.getBean(ClaimValidationService.class));
                            claimValidationService.claimreOpening(claim.getId(), ec);
                            crc.addClaimReserveAdjust(newReserveAdjust);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param cause
     * @param detail
     * @param pathology
     * @return boolean
     */
    public boolean isClaimExclusion(String cause, String detail, String pathology) {
        return claimExclusions.isExclusion(cause, detail, pathology);
    }

    /**
     * @param normalReservePk
     * @return DefaultConfigurableObject
     */
    public DefaultConfigurableObject getAnalysisDco(String normalReservePk) {
        DefaultConfigurableObject dco = null;
        Session session = null;
        try {
            session = getHibernateSession();
            ClaimNormalReserve normalReserve = (ClaimNormalReserve) HibernateUtil.load(ClaimNormalReserve.class, normalReservePk);
            String templateName = AcseleConf.getProperty("OTClaimAnalysis");
            Categorias categories = Categorias.getBean(Categorias.ALL_TEMPLATES_STATE);
            ConfigurableObjectType cot = (ConfigurableObjectType) categories.get(CotType.OTHER, templateName);
            String dcoId = normalReserve.getAnalysisDcoId();
            if (StringUtil.isEmptyOrNullValue(dcoId)) {
                dco = DefaultConfigurableObject.loadByDefault(cot);
            } else {
                dco = DefaultConfigurableObject.load(cot, Long.valueOf(dcoId));
            }
        } catch (Exception ex) {
            log.error("Error", ex);
//        } finally {
            //HibernateUtil.closeSession(session);
        }
        return dco;
    }

    /**
     * Returns all the thirdparties associated to the coverage passed as a parameter
     *
     * @param normalReservePk the ClaimNormalReserve pk
     * @return a collection of SearhThirdPartyResult objects
     */
    public Collection getBeneficiariesForPayments(int normalReservePk) {
        Session session = null;
        Collection beneficiaries = null;
        ClaimNormalReserve normalReserve;

        try {
            session = HibernateUtil.getSession();
            normalReserve = (ClaimNormalReserve) HibernateUtil.load(ClaimNormalReserve.class, String.valueOf(normalReservePk));
            beneficiaries = getBeneficiariesForPayments(normalReserve.getEvaluatedCoverage());
        } finally {
            if (session != null) {
                //HibernateUtil.closeSession(session);
            }
        }
        return beneficiaries;
    }

    /**
     * Returns all the thirdparties associated to the coverage passed as a parameter
     *
     * @param evaluatedCoverage the coverage
     * @return a collection of SearhThirdPartyResult objects
     */
    private Collection<SearchThirdPartyResult> getBeneficiariesForPayments(EvaluatedCoverage evaluatedCoverage) {
        log.debug("********[Acsel-e v4.5] ClaimComposerWrapper.getBeneficiariesForPayments{}");
        //Vars
        Collection<SearchThirdPartyResult> result = new ArrayList<SearchThirdPartyResult>();
        Collection<Participation> participations = evaluatedCoverage.getParticipationCollection();
        Iterator iterator = participations.iterator();
        log.debug("********[Acsel-e v4.5] {Iterando}");
        //Creating the beans
        for (Participation participation : participations) {
            log.debug("********[Acsele v4.5] ClaimComposerWrapper.getBeneficiariesForPayments(){" + "9877}participation = " + participation);
            SearchThirdPartyResult thirdParty = new SearchThirdPartyResult(participation);
            result.add(thirdParty);
        }
        return result;
    }

    /**
     * Construct a new Collection with the manual beneficiaries selected
     *
     * @param beneficiariesSelection
     */
    public void setManualBeneficiaries(String beneficiariesSelection) {
        StringTokenizer st = new StringTokenizer(beneficiariesSelection, "$");
        this.beneficiaries = new ArrayList();
        while (st.hasMoreTokens()) {
            String thirdPartyName = st.nextToken();
            String thirdParty = st.nextToken();
            String[] thirdPartyStrings = thirdParty.split("\\|");
            String thirdPartyID = thirdPartyStrings[0];
            long roleID = Long.valueOf(thirdPartyStrings[1]);
            com.consisint.acsele.uaa.api.Role roleImpl = com.consisint.acsele.uaa.api.Role.Impl.load(roleID);
            SearchThirdPartyResult str = new SearchThirdPartyResult(thirdPartyID, thirdPartyName, roleImpl);
            beneficiaries.add(str);
        }

    }

    public void setManualBeneficiaries(String thirdPartyID, String thirdPartyName, String roleID) {
        this.beneficiaries = new ArrayList();
        long rID = Long.valueOf(roleID);
        com.consisint.acsele.uaa.api.Role role = com.consisint.acsele.uaa.api.Role.Impl.load(rID);
        SearchThirdPartyResult str = new SearchThirdPartyResult(thirdPartyID, thirdPartyName, role);
        beneficiaries.add(str);
    }

    /**
     * Get the manual beneficiaries selected
     *
     * @return beneficiaries
     */
    public Collection getManualBeneficiaries() {
        return beneficiaries;
    }

    public Collection getClaimNormalReserves() {
        log.debug("-------- ***** getClaimNormalReserves **** --------");
        Map result = new HashMap();
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        log.debug("claimRiskUnits = " + claimRiskUnits);
        for (ClaimRiskUnit cru : claimRiskUnits) {
            Iterator claimInsuranceObject = cru.getClaimInsuranceObjects().values().iterator();
            log.debug("***cru = " + cru);
            while (claimInsuranceObject.hasNext()) {
                ClaimInsuranceObject cio = (ClaimInsuranceObject) claimInsuranceObject.next();
                log.debug("***cio = " + cio);
                result.putAll(cio.getNormalReserves());
            }
        }

        Collection normalReserves =  result.values();
        for(Iterator reserveIterator = normalReserves.iterator();  reserveIterator.hasNext();) {
            ClaimNormalReserve claimNormalReserve = (ClaimNormalReserve) reserveIterator.next();
            claimNormalReserve.load();
        }
        return normalReserves;
    }

    public Map getClaimNormalReservesMap() {
        Map result = new HashMap();
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit cru : claimRiskUnits) {
            Iterator claimInsuranceObject = cru.getClaimInsuranceObjects().values().iterator();
            while (claimInsuranceObject.hasNext()) {
                ClaimInsuranceObject cio = (ClaimInsuranceObject) claimInsuranceObject.next();
                result.putAll(cio.getNormalReserves());
            }
        }
        return result;
    }

    /**
     * Returns all the thirdparties associated to the claimInsuranceObject passed as a parameter
     *
     * @param ioId Insurance Object id
     * @return a collection of SearhThirdPartyResult objects
     */

    public Collection getThirdPartiesFromInsuranceObject(String ruId, String ioId) {
        return getThirdPartiesFromInsuranceObject(ruId, ioId, null);
    }

    public Collection getThirdPartiesFromInsuranceObject(Collection<Participation> participations, RoleList roles) throws Exception {

        Collection<SearchThirdPartyResult> result = new ArrayList<SearchThirdPartyResult>();


        Participation participation;
        SearchThirdPartyResult thirdParty;
        Iterator iterator;

        //Looking for the participations


        iterator = participations.iterator();

        //Creating the beans
        while (iterator.hasNext()) {
            participation = (Participation) iterator.next();
            com.consisint.acsele.uaa.api.Role roleImpl =
                    com.consisint.acsele.uaa.api.Role.Impl.load(participation.getRole().getDynamic().getDCO().getDesc());

            if ((roles == null) || (roles.size() <= 0) || (roles.getAll().contains(roleImpl))) {
                thirdParty = new SearchThirdPartyResult(participation);
                result.add(thirdParty);
            }
        }
        return result;
    }

    public Collection<SearchThirdPartyResult> getThirdPartiesFromInsuranceObject(String ruId, String ioId, RoleList roles) {
        Collection<SearchThirdPartyResult> result = new ArrayList<SearchThirdPartyResult>();

        AgregatedInsuranceObject agregatedInsuranceObject;
        Collection participations;
        Participation participation;
        SearchThirdPartyResult thirdParty;
        Iterator iterator;

        //Looking for the participations
        agregatedInsuranceObject = getAgregatedInsuranceObjectById(ruId, ioId);
        participations = agregatedInsuranceObject.getParticipationCollection();
        iterator = participations.iterator();

        //Creating the beans
        while (iterator.hasNext()) {
            participation = (Participation) iterator.next();
            com.consisint.acsele.uaa.api.Role roleImpl =
                    com.consisint.acsele.uaa.api.Role.Impl.load(participation.getRole().getDynamic().getDCO().getDesc());

            if ((roles == null) || (roles.size() <= 0) || (roles.getAll().contains(roleImpl))) {
                thirdParty = new SearchThirdPartyResult(participation);
                result.add(thirdParty);
            }
        }

        return result;
    }

    public Collection<SearchThirdPartyResult> getInsuranceThirdParty(String ruId, String ioId, RoleList roles) {
        Collection<SearchThirdPartyResult> result = new ArrayList<SearchThirdPartyResult>();
        for (Participation participation : getInsuranceParticipation(ruId, ioId, roles)) {
            SearchThirdPartyResult bean = getBeanFromParticipation(participation.getThirdParty().getPk(), participation.getRole());
            result.add(bean);
        }
        return result;
    }

    public Collection<Participation> getInsuranceParticipation(String ruId, String ioId, RoleList roles) {
        AgregatedInsuranceObject aio;
        aio = getAgregatedInsuranceObjectById(ruId, ioId);
        Collection<Participation> result = getParticipationsRecursive(aio, roles);
        return result;
    }

    public Collection<SearchThirdPartyResult> getThirdPartiesFromInsuranceObjectByDesc(String ruId, String ioDesc, RoleList roles) {
        Collection<SearchThirdPartyResult> result = new ArrayList<SearchThirdPartyResult>();

        AgregatedInsuranceObject agregatedInsuranceObject = null;
        Collection participations;
        Participation participation;
        SearchThirdPartyResult thirdParty;
        Iterator iterator;
        AgregatedRiskUnit aru = getAgregatedRiskUnitById(ruId);
        List<AgregatedInsuranceObject> aruList = getAgregatedInsuranceObjectList(aru);
        for (AgregatedInsuranceObject aio : aruList) {
            System.out.println("### ioDesc = " + ioDesc);
            System.out.println("agregatedInsuranceObject1.getDesc() = " + aio.getDesc());

            if (aio.getDesc().equals(ioDesc)) {
                agregatedInsuranceObject = aio;
            }
        }
        //Looking for the participations
        //     agregatedInsuranceObject = policy.getAgregatedRiskUnit(ruId).getDCO(ioDesc);
        if (agregatedInsuranceObject != null) {
            participations = agregatedInsuranceObject.getParticipationCollection();
            iterator = participations.iterator();

            //Creating the beans
            while (iterator.hasNext()) {
                participation = (Participation) iterator.next();
                com.consisint.acsele.uaa.api.Role roleImpl =
                        com.consisint.acsele.uaa.api.Role.Impl.load(participation.getRole().getDynamic().getDCO().getDesc());

                if ((roles == null) || (roles.size() <= 0) || (roles.getAll().contains(roleImpl))) {
                    thirdParty = new SearchThirdPartyResult(participation);
                    result.add(thirdParty);
                }
            }
        }
        return result;
    }

    public ClaimNormalReserve getClaimNormalReservesMapByURid(String riskUnitId, String coverageDesc, String ioId) {
        Map result = new HashMap();
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        for (ClaimRiskUnit cru : claimRiskUnits) {
            if (cru.getRiskUnitId().equals(riskUnitId)) {
                Iterator claimInsuranceObject = cru.getClaimInsuranceObjects().values().iterator();
                while (claimInsuranceObject.hasNext()) {
                    ClaimInsuranceObject cio = (ClaimInsuranceObject) claimInsuranceObject.next();
                    if (cio.getAioId().equals(ioId)) {
                        result.putAll(cio.getNormalReserves());
                    }
                }
            }
        }
        return (ClaimNormalReserve) result.get(coverageDesc);
    }

    /**
     * @param evaluatedCoverageId
     * @return Double
     */
    public int getMaximumBenefitPayments(EvaluatedCoverage evaluatedCoverage) {
        String property = "TTNumberOfBenefitPayments";
        Double numberOfPayments = null;

        Product product = policy.getProduct();
        AgregatedInsuranceObject agregatedInsuranceObject = evaluatedCoverage.getAgregatedInsuranceObject();

        //From the product
        numberOfPayments = (Double) product.getDCO().getValues().get(property);

        //from the InsuranceObject
        if (numberOfPayments == null) {
            numberOfPayments = (Double) agregatedInsuranceObject.getDCO().getValues().get(property);

        }

        //from the coverage
        if (numberOfPayments == null) {
            numberOfPayments = (Double) evaluatedCoverage.getDCO().getValues().get(property);

        }

        return numberOfPayments == null ? -1 : numberOfPayments.intValue();
    }

    /**
     * Generate Scheduled with first payments
     *
     * @param ruId
     * @param ioId
     * @param thirdPartyId
     * @param periodicity
     * @param amount
     * @param numberOfPayments
     * @param benefitDuration
     * @param initialPayment
     * @param paymentType
     * @throws Exception
     */
    public String generateScheduledWithFirstPayments(String ruId, String ioId, String thirdPartyId, int periodicity, int benefitDuration, Double amount,
                                                     double deducedAmount, int numberOfPayments, ClaimReserveBenefit benefit, double initialPayment, int paymentType,
                                                     BeneficiariesAndPercentageForm element, Collection selectedRiskUnits, double firstQuote, double finalQuote, double remainQuote) throws Exception {

        ClaimNormalReserve normalReserve = benefit.getClaimNormalReserve();
        Session session = null;
        Transaction transaction = null;
        int typeScheduled = 2;
        String message = null;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }
            PaymentOrder paymentOrder;
            SearchThirdPartyResult thirdParty;
            String thirdPartyName = null;
            long thirdPartyRoleId = 0;
            Iterator thirdPartiesIterator;
            Date commitmentDate, currentDate, today;
            Calendar calendar;
            int days = 15;
            double lastPayments = 0;
            double percentage = 0;
            RoleList rolesBeneficiaries = RoleGroup.BENEFICIARY_ROLES.getRoleList();
            thirdPartyName = element.getName();
            thirdPartyRoleId = Long.parseLong(element.getRoleId());
            percentage = Double.parseDouble(element.getPercentage());
            ///// payment ////
            if (paymentType >= 0 && amount > 0 && initialPayment >= 0) {

                ClientResponse response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                        normalReserve.getPk(), true);
                PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
                EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();

                checkClaimConfiguration(amount, numberOfPayments, paymentOrderCollection, ec, typeScheduled, selectedRiskUnits);
                ClaimsCoverageConfiguration configuration = ClaimsCoverageConfiguration
                        .load(Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()),
                                ec.getConfiguratedCoverageOA().getId());
                today = new Date();
                currentDate = benefit.getStartDate();
                calendar = Calendar.getInstance(UserInfo.getLocale());
                calendar.setTime(currentDate);
                if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY && (calendar.get(Calendar.DAY_OF_MONTH) != 1
                        && calendar.get(Calendar.DAY_OF_MONTH) != days)) {
                    if (calendar.get(Calendar.DAY_OF_MONTH) > days) {
                        calendar.add(Calendar.MONTH, 1);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                    } else {
                        calendar.set(Calendar.DAY_OF_MONTH, days);
                    }
                    currentDate = calendar.getTime();
                }
                Calendar calendarEndDate = Calendar.getInstance(UserInfo.getLocale());

                boolean unicaCuota = false;

                for (int indexNumberOfPayments = 1; (indexNumberOfPayments <= (numberOfPayments + 1)) || (unicaCuota); indexNumberOfPayments++) {

                    if (initialPayment > amount) {
                        initialPayment = amount;
                    }
                    if (initialPayment == amount || numberOfPayments == 0) {
                        numberOfPayments = -1;
                    }
                    int numberOfPaymentsQuote = numberOfPayments;

                    if (firstQuote > 0) {
                        numberOfPaymentsQuote = numberOfPaymentsQuote - 1;
                    }
                    if (finalQuote > 0) {
                        numberOfPaymentsQuote = numberOfPaymentsQuote - 1;
                    }

                    if (numberOfPayments >= 1) {
                        if ((indexNumberOfPayments == 2 && firstQuote > 0) || (indexNumberOfPayments == 1 && initialPayment == 0 && firstQuote > 0)) {
                            lastPayments = firstQuote;
                        } else {
                            lastPayments = (amount) / numberOfPaymentsQuote;
                        }
                        if (unicaCuota) {
                            lastPayments = (amount);
                        }
                        if ((indexNumberOfPayments == (numberOfPayments + 1)) && (finalQuote > 0)) {
                            lastPayments = finalQuote;
                        }
                    }

                    calendar.setTime(currentDate);
                    if (periodicity == ClaimNormalReserve.PERIODICITY_DAYS) {
                        calendar.add(Calendar.DAY_OF_MONTH, benefitDuration);
                    } else if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY) {
                        if (calendar.get(Calendar.DAY_OF_MONTH) + 1 > days) {
                            calendar.add(Calendar.MONTH, 1);
                            calendar.set(Calendar.DAY_OF_MONTH, 1);
                        } else {
                            calendar.set(Calendar.DAY_OF_MONTH, days);
                        }
                    } else if (periodicity == ClaimNormalReserve.PERIODICITY_WEEKLY) {
                        calendar.add(Calendar.DAY_OF_MONTH, 7);
                    } else if (periodicity == ClaimNormalReserve.PERIODICITY_BIWEEKLY) {
                        calendar.add(Calendar.DAY_OF_MONTH, 14);
                    } else {
                        calendar.add(Calendar.MONTH, benefitDuration);
                    }
                    if (numberOfPayments == 1) {
                        calendarEndDate.setTime(currentDate);
                    } else {
                        calendarEndDate.setTime(calendar.getTime());
                        calendarEndDate.add(Calendar.DATE, -1);
                    }
                    commitmentDate = currentDate;
                    if (configuration != null && configuration.getFistPaymentDate() != null && !configuration.getFistPaymentDate().equals("") && indexNumberOfPayments == 1) {
                        TablaSimbolos symbolsTable = new TablaSimbolos();
                        Double date = this.evaluate(configuration.getFistPaymentDate(), symbolsTable, ec);
                        Date calcCommitmentDate = Funciones.transformDate(date);
                        log.debug("CCW generateScheduledWithFirstPayments Commitment date " + commitmentDate);
                        commitmentDate = calcCommitmentDate;

                    }
                    if (configuration.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                        commitmentDate = DateUtil.sumDaysToDate(currentDate, configuration.getWaitingPeriod());
                        log.debug("CCW generateScheduledWithFirstPayments Commitment date RETROACTIVE_TYPE " + commitmentDate);
                    }
                    paymentOrder = new PaymentOrder(benefit);
                    paymentOrder.setDate(today);

                    paymentOrder.setStartDate(currentDate);
                    paymentOrder.setCommitmentDate(commitmentDate);
                    paymentOrder.setEndDate(calendarEndDate.getTime());
                    currentDate = calendar.getTime();

                    if (message == null) {

                        paymentOrder.setBeneficiary(thirdPartyName);
                        paymentOrder.setDistributionAmount(0);
                        paymentOrder.setDoneBy(UserInfo.getUser());
                        paymentOrder.setParticipationPercentage(percentage);
                        paymentOrder.setPenaltyPercentage(0);
                        paymentOrder.setReason("");
                        paymentOrder.setRefundPercentage(0);
                        paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
                        paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
                        long thirdPartyIdn = Long.parseLong(thirdPartyId);
                        ClaimBeneficiaryPersister claimBeneficiaryPersister = ClaimBeneficiaryPersister.Impl.getInstance();
                        ClaimBeneficiaryPK pk = new ClaimBeneficiaryPK(benefit.getId(), thirdPartyIdn, paymentOrder.getThirdPartyRoleID());
                        ClaimBeneficiary claimBeneficiary = claimBeneficiaryPersister.load(pk);
                        if (claimBeneficiary != null && claimBeneficiary.getRecipientThirdParty() != null) {
                            paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                            paymentOrder.setThirdPartyId(claimBeneficiary.getRecipientThirdParty().getId());
                        } else {
                            paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                            paymentOrder.setThirdPartyId(thirdPartyIdn);
                        }
                        paymentOrder.setType(PaymentOrderType.PARTIAL.getValue());
                        paymentOrder.setOnHold(0);
                        paymentOrder.setCoverageDesc(ec.getDesc());

                        if (numberOfPayments == -1) {
                            paymentOrder.setAmount(amount + initialPayment);
                        } else if (numberOfPayments == 1 && !unicaCuota) {
                            paymentOrder.setAmount(initialPayment);
                            unicaCuota = true;
                        } else if (unicaCuota) {
                            paymentOrder.setAmount(lastPayments);
                            unicaCuota = false;
                        } else {
                            if (indexNumberOfPayments == 1) {
                                paymentOrder.setAmount(initialPayment);
                            } else {
                                paymentOrder.setAmount(lastPayments);
                            }
                        }

                        paymentOrder.save();
                        normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);

                        if (deducedAmount > 0) {
                            PaymentOrderDetail paymentOrderDetail = new PaymentOrderDetail();
                            paymentOrderDetail.setPk(paymentOrder.getPk());
                            paymentOrderDetail.setDeducedAmount(deducedAmount);
                            paymentOrderDetail.save();
                        }
                    }
                    if (message == null) {
                        if (startTx) {
                            HibernateUtil.commit(transaction, session);
                        }
                    }
                }

            }
            generateReserveReinsuranceDistribution(normalReserve, String.valueOf(amount), Constants.PAYMENT_CLAIM,
                    new java.sql.Date(normalReserve.getDate().getTime()), null);
        } catch (Exception e) {
            session.clear();
            if (startTx) {
                HibernateUtil.rollBack(transaction);
            }

            ExceptionUtil.handlerException(e);

        }
        return message;
    }


    /**
     * Generate Scheduled Multiple Payments
     *
     * @param ruId
     * @param ioId
     * @param thirdPartyId
     * @param periodicity
     * @param amount
     * @param numberOfPayments
     * @param benefitDuration
     * @throws Exception
     */
    public String generateScheduledPayments(String ruId, String ioId, String thirdPartyId, int periodicity, int benefitDuration, Double amount,
                                            double deducedAmount, int numberOfPayments, ClaimReserveBenefit benefit, Collection selectedRiskUnits) throws Exception {
        ClaimNormalReserve normalReserve = benefit.getClaimNormalReserve();
        ClientResponse response;
        CoverageReserve coverageReserve = new CoverageReserve(normalReserve, this);
        Double reserveAmount = coverageReserve.getAmount();
        Double participationP = (amount * 100) / reserveAmount;
        Session session = null;
        Transaction transaction = null;
        int typeScheduled = 2;
        String message = null;
        Collection thirdPartiesFromInsuranceObject;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }
            PaymentOrder paymentOrder;
            Collection thirdParties;
            SearchThirdPartyResult thirdParty;
            String thirdPartyName = null;
            int thirdPartyRoleId = 0;
            Iterator thirdPartiesIterator;
            Date commitmentDate, currentDate, today;
            Calendar calendar;
            int days = 15;
            Double paymentAmount = amount;
            log.info("medicion tiempo>> buscando  beneficiarios " + new Date());
            thirdParties = getBeneficiariesForPayments(normalReserve.getEvaluatedCoverage());
            log.info("medicion tiempo>>luego de buscar  beneficiarios " + new Date());
            if (normalReserve != null) {
                ClaimInsuranceObject claimInsuranceObject = normalReserve.getContainer();
                AgregatedInsuranceObject agregatedInsuranceObject = claimInsuranceObject.getAgregatedInsuranceObject();
                Collection<Participation> participationCollection = agregatedInsuranceObject.getParticipationCollection();
                log.info("medicion tiempo>> buscando  terceros del objeto asegurado " + new Date());
                thirdPartiesFromInsuranceObject = getThirdPartiesFromInsuranceObject(participationCollection, null);
                log.info("medicion tiempo>> despues  terceros del objeto asegurado " + new Date());
            } else {
                thirdPartiesFromInsuranceObject = this.getThirdPartiesFromInsuranceObject(ruId, ioId);
            }

            thirdParties.addAll(thirdPartiesFromInsuranceObject);

            Collection manualBeneficiaries = this.getManualBeneficiaries();
            if (manualBeneficiaries != null && !manualBeneficiaries.isEmpty()) {
                thirdParties.addAll(manualBeneficiaries);
            }

            thirdPartiesIterator = thirdParties.iterator();
            while (thirdPartiesIterator.hasNext()) {
                thirdParty = (SearchThirdPartyResult) thirdPartiesIterator.next();
                if (thirdParty.getThirdPartyId().trim().equals(thirdPartyId.trim())) {
                    thirdPartyName = thirdParty.getThidPartyName();
                    thirdPartyRoleId = thirdParty.getRoleID();
                }
            }


            response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                    normalReserve.getPk(), true);


            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();
            checkClaimConfiguration(amount, numberOfPayments, paymentOrderCollection, ec, typeScheduled, selectedRiskUnits);
            log.debug("********[Acsele v4.5] ClaimComposerWrapper.generateScheduledPayments(){" + "6860}thirdPartyName = " + thirdPartyName);
            ClaimsCoverageConfiguration configuration = ClaimsCoverageConfiguration
                    .load(Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()),
                            ec.getConfiguratedCoverageOA().getId());
            today = new Date();
            currentDate = getConfiguredStartDate(ruId, ioId, normalReserve.getEvaluatedCoverage());
            calendar = Calendar.getInstance(UserInfo.getLocale());
            calendar.setTime(currentDate);
            if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY && (calendar.get(Calendar.DAY_OF_MONTH) != 1
                    && calendar.get(Calendar.DAY_OF_MONTH) != days)) {
                if (calendar.get(Calendar.DAY_OF_MONTH) > days) {
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, days);
                }
                currentDate = calendar.getTime();
            }
            Calendar calendarEndDate = Calendar.getInstance(UserInfo.getLocale());
            for (int i = 0; i < numberOfPayments; i++) {
                calendar.setTime(currentDate);
                if (periodicity == ClaimNormalReserve.PERIODICITY_DAYS) {
                    calendar.add(Calendar.DAY_OF_MONTH, benefitDuration);
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY) {
                    if (calendar.get(Calendar.DAY_OF_MONTH) + 1 > days) {
                        calendar.add(Calendar.MONTH, 1);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                    } else {
                        calendar.set(Calendar.DAY_OF_MONTH, days);
                    }
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_WEEKLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 7);
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_BIWEEKLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 14);
                } else {
                    calendar.add(Calendar.MONTH, benefitDuration);
                }
                if (numberOfPayments == 1) {
                    calendarEndDate.setTime(currentDate);
                } else {
                    calendarEndDate.setTime(calendar.getTime());
                    calendarEndDate.add(Calendar.DATE, -1);
                }
                commitmentDate = currentDate;
                if (configuration != null && configuration.getFistPaymentDate() != null && !configuration.getFistPaymentDate().equals("") && i == 0) {
                    TablaSimbolos symbolsTable = new TablaSimbolos();
                    Double date = this.evaluate(configuration.getFistPaymentDate(), symbolsTable, ec);
                    Date calcCommitmentDate = Funciones.transformDate(date);
                    log.debug("CCW generateScheduledPayments Commitment date " + calcCommitmentDate);
                    commitmentDate = calcCommitmentDate;
                }
                if (configuration.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                    commitmentDate = DateUtil.sumDaysToDate(this.getClaim().getOcurrenceDate(), configuration.getWaitingPeriod());
                    log.debug("CCW generateScheduledPayments Commitment date RETROACTIVE_TYPE " + commitmentDate);
                }
                paymentOrder = new PaymentOrder(benefit);
                paymentOrder.setDate(today);
                paymentOrder.setStartDate(currentDate);
                paymentOrder.setCommitmentDate(commitmentDate);
                paymentOrder.setEndDate(calendarEndDate.getTime());
                currentDate = calendar.getTime();

                message = verifyDatesPaymentOrder(normalReserve.getPk(), paymentOrder.getStartDate(), paymentOrder.getEndDate(), Long.parseLong(thirdPartyId), configuration.isAllowPaymentsInOneDay());

                if (message == null) {
                    paymentOrder.setAmount(paymentAmount);
                    paymentOrder.setBeneficiary(thirdPartyName);
                    paymentOrder.setDistributionAmount(0);
                    paymentOrder.setDoneBy(UserInfo.getUser());
                    paymentOrder.setParticipationPercentage(participationP);
                    paymentOrder.setPenaltyPercentage(0);
                    paymentOrder.setReason("");
                    paymentOrder.setRefundPercentage(0);
                    paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
                    paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
                    long thirdPartyIdn = Long.parseLong(thirdPartyId);
                    ClaimBeneficiaryPersister claimBeneficiaryPersister = ClaimBeneficiaryPersister.Impl.getInstance();
                    ClaimBeneficiaryPK pk = new ClaimBeneficiaryPK(benefit.getId(), thirdPartyIdn, paymentOrder.getThirdPartyRoleID());
                    ClaimBeneficiary claimBeneficiary = claimBeneficiaryPersister.load(pk);
                    if (claimBeneficiary != null && claimBeneficiary.getRecipientThirdParty() != null) {
                        paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                        paymentOrder.setThirdPartyId(claimBeneficiary.getRecipientThirdParty().getId());
                    } else {
                        paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                        paymentOrder.setThirdPartyId(thirdPartyIdn);
                    }
                    paymentOrder.setType(PaymentOrderType.PARTIAL.getValue());
                    paymentOrder.setOnHold(0);
                    paymentOrder.setCoverageDesc(ec.getDesc());
                    paymentOrder.save();
                    normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);

                    if (deducedAmount > 0) {
                        PaymentOrderDetail paymentOrderDetail = new PaymentOrderDetail();
                        paymentOrderDetail.setPk(paymentOrder.getPk());
                        paymentOrderDetail.setDeducedAmount(deducedAmount);
                        paymentOrderDetail.save();
                    }
                } else {
                    throw new ApplicationException(Exceptions.CCErrorSamePeriodPaid, Severity.ERROR);
                }
            }
            if (message == null) {
                if (startTx) {
                    HibernateUtil.commit(transaction, session);
                }
            }
            generateReserveReinsuranceDistribution(normalReserve, String.valueOf(amount), Constants.PAYMENT_CLAIM,
                    new java.sql.Date(normalReserve.getDate().getTime()), null);
        } catch (Exception e) {
            session.clear();
            if (startTx) {
                HibernateUtil.rollBack(transaction);
            }
            log.error("Error generating the payment orders.", e);
            ExceptionUtil.handlerException(e);
        }
        return message;
    }

    /**
     * Generate Scheduled Multiple Payments
     *
     * @param ruId
     * @param ioId
     * @param thirdPartyId
     * @param periodicity
     * @param amount
     * @param numberOfPayments
     * @param benefitDuration
     * @throws Exception
     */
    public PaymentOrder generateScheduledPayments(String ruId, String ioId, String thirdPartyId, int periodicity, int benefitDuration, Double amount,
                                                  double deducedAmount, int numberOfPayments, ClaimReserveBenefit benefit, boolean allParticipation, Collection selectedRiskUnits) throws Exception {
        ClaimNormalReserve normalReserve = benefit.getClaimNormalReserve();
        ClientResponse response;
        CoverageReserve coverageReserve = new CoverageReserve(normalReserve, this);
        Double reserveAmount = coverageReserve.getAmount();
        Double participationP = (amount * 100) / reserveAmount;
        Session session = null;
        Transaction transaction = null;
        int typeScheduled = 2;
        String message = null;
        Collection thirdPartiesFromInsuranceObject;
        PaymentOrder paymentOrder = null;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }
//            int paymentOrderPk;
            Collection thirdParties;
            SearchThirdPartyResult thirdParty;
            String thirdPartyName = null;
            int thirdPartyRoleId = 0;
            Iterator thirdPartiesIterator;
            Date commitmentDate, currentDate, today;
            Calendar calendar;
            int days = 15;
            //Double paymentAmount = new Double(amount.doubleValue() / numberOfPayments);
            Double paymentAmount = amount; //Verificar contra la validacion javascript que hace en claimBenefit.jsp en la funcion loadBenefitPayments(...)
            log.info("medicion tiempo>> buscando  beneficiarios " + new Date());
            thirdParties = getBeneficiariesForPayments(normalReserve.getEvaluatedCoverage());
            log.info("medicion tiempo>>luego de buscar  beneficiarios " + new Date());
            if (normalReserve != null) {
                ClaimInsuranceObject claimInsuranceObject = normalReserve.getContainer();
                AgregatedInsuranceObject agregatedInsuranceObject = claimInsuranceObject.getAgregatedInsuranceObject();
                Collection<Participation> participationCollection = agregatedInsuranceObject.getParticipationCollection();
                log.info("medicion tiempo>> buscando  terceros del objeto asegurado " + new Date());
                thirdPartiesFromInsuranceObject = getThirdPartiesFromInsuranceObject(participationCollection, null);
                log.info("medicion tiempo>> despues  terceros del objeto asegurado " + new Date());
            } else {
                thirdPartiesFromInsuranceObject = this.getThirdPartiesFromInsuranceObject(ruId, ioId);
            }

            thirdParties.addAll(thirdPartiesFromInsuranceObject);

            Collection manualBeneficiaries = this.getManualBeneficiaries();
            if (manualBeneficiaries != null && !manualBeneficiaries.isEmpty()) {
                thirdParties.addAll(manualBeneficiaries);
            }

            thirdPartiesIterator = thirdParties.iterator();
            while (thirdPartiesIterator.hasNext()) {
                thirdParty = (SearchThirdPartyResult) thirdPartiesIterator.next();
                if (thirdParty.getThirdPartyId().trim().equals(thirdPartyId.trim())) {
                    thirdPartyName = thirdParty.getThidPartyName();
                    thirdPartyRoleId = thirdParty.getRoleID();
                }
            }


            response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                    normalReserve.getPk(), allParticipation);


            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();
            checkClaimConfiguration(amount, numberOfPayments, paymentOrderCollection, ec, typeScheduled, selectedRiskUnits);
            log.debug("********[Acsele v4.5] ClaimComposerWrapper.generateScheduledPayments(){" + "6860}thirdPartyName = " + thirdPartyName);
            ClaimsCoverageConfiguration configuration = ClaimsCoverageConfiguration
                    .load(Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()),
                            ec.getConfiguratedCoverageOA().getId());
            today = new Date();
            //  obtiene fecha Inicial del campo ocurrenceDate del claim y/o aplicando propiedades de la cobertura
            currentDate = getConfiguredStartDate(ruId, ioId, normalReserve.getEvaluatedCoverage());
            calendar = Calendar.getInstance(UserInfo.getLocale());
            calendar.setTime(currentDate);
            if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY && (calendar.get(Calendar.DAY_OF_MONTH) != 1
                    && calendar.get(Calendar.DAY_OF_MONTH) != days)) {
                if (calendar.get(Calendar.DAY_OF_MONTH) > days) {
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, days);
                }
                currentDate = calendar.getTime();
            }
            Calendar calendarEndDate = Calendar.getInstance(UserInfo.getLocale());
            for (int i = 0; i < numberOfPayments; i++) {
                calendar.setTime(currentDate);
                if (periodicity == ClaimNormalReserve.PERIODICITY_DAYS) {
                    calendar.add(Calendar.DAY_OF_MONTH, benefitDuration);
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY) {
                    if (calendar.get(Calendar.DAY_OF_MONTH) + 1 > days) {
                        calendar.add(Calendar.MONTH, 1);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                    } else {
                        calendar.set(Calendar.DAY_OF_MONTH, days);
                    }
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_WEEKLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 7);
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_BIWEEKLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 14);
                } else {    // ClaimNormalReserve.PERIODICITY_MONTHS or PERIODICITY_NONE
                    calendar.add(Calendar.MONTH, benefitDuration);
                }
                if (numberOfPayments == 1) {
                    calendarEndDate.setTime(currentDate);
                } else {
                    calendarEndDate.setTime(calendar.getTime());
                    calendarEndDate.add(Calendar.DATE, -1);
                }
                commitmentDate = currentDate;
                if (configuration != null && configuration.getFistPaymentDate() != null && !configuration.getFistPaymentDate().equals("") && i == 0) {
                    TablaSimbolos symbolsTable = new TablaSimbolos();
                    Double date = this.evaluate(configuration.getFistPaymentDate(), symbolsTable, ec);
                    Date calcCommitmentDate = Funciones.transformDate(date);
                    log.debug("CCW generateScheduledPayments Commitment date " + calcCommitmentDate);
                    commitmentDate = calcCommitmentDate;
                }
                if (configuration.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                    commitmentDate = DateUtil.sumDaysToDate(this.getClaim().getOcurrenceDate(), configuration.getWaitingPeriod());
                    log.debug("CCW generateScheduledPayments Commitment date RETROACTIVE_TYPE " + commitmentDate);
                }
                paymentOrder = new PaymentOrder(benefit);
                paymentOrder.setDate(today);
                paymentOrder.setStartDate(currentDate);
                paymentOrder.setCommitmentDate(commitmentDate);
                paymentOrder.setEndDate(calendarEndDate.getTime());
                currentDate = calendar.getTime();

                message = verifyDatesPaymentOrder(normalReserve.getPk(), paymentOrder.getStartDate(), paymentOrder.getEndDate(), Long.parseLong(thirdPartyId), configuration.isAllowPaymentsInOneDay());

                if (message == null) {
                    paymentOrder.setAmount(paymentAmount);
                    paymentOrder.setBeneficiary(thirdPartyName);
                    paymentOrder.setDistributionAmount(0);
                    paymentOrder.setDoneBy(UserInfo.getUser());
                    paymentOrder.setParticipationPercentage(participationP);
                    paymentOrder.setPenaltyPercentage(0);
                    paymentOrder.setReason("");
                    paymentOrder.setRefundPercentage(0);
                    paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
                    paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
                    //benefit recipient for payments
                    long thirdPartyIdn = Long.parseLong(thirdPartyId);
                    ClaimBeneficiaryPersister claimBeneficiaryPersister = ClaimBeneficiaryPersister.Impl.getInstance();
                    ClaimBeneficiaryPK pk = new ClaimBeneficiaryPK(benefit.getId(), thirdPartyIdn, paymentOrder.getThirdPartyRoleID());
                    ClaimBeneficiary claimBeneficiary = claimBeneficiaryPersister.load(pk);
                    if (claimBeneficiary != null && claimBeneficiary.getRecipientThirdParty() != null) {
                        paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                        paymentOrder.setThirdPartyId(claimBeneficiary.getRecipientThirdParty().getId());
                    } else {
                        paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                        paymentOrder.setThirdPartyId(thirdPartyIdn);
                    }
                    paymentOrder.setType(PaymentOrderType.PARTIAL.getValue());
                    paymentOrder.setOnHold(0);
                    paymentOrder.setCoverageDesc(ec.getDesc());
                    paymentOrder.save();
                    normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);

                    if (deducedAmount > 0) {
                        PaymentOrderDetail paymentOrderDetail = new PaymentOrderDetail();
                        paymentOrderDetail.setPk(paymentOrder.getPk());
                        paymentOrderDetail.setDeducedAmount(deducedAmount);
                        paymentOrderDetail.save();
                    }
                } else {
                    throw new ApplicationException(Exceptions.CCErrorSamePeriodPaid, Severity.ERROR);
                }
            }
            if (message == null) {
                if (startTx) {
                    HibernateUtil.commit(transaction, session);
                }
            }
        } catch (Exception e) {
            session.clear();
            if (startTx) {
                HibernateUtil.rollBack(transaction);
            }
            log.debug("Error generating the payment orders.", e);
            ExceptionUtil.handlerException(e);
//        } finally {
            //HibernateUtil.closeSession(session);
        }
        return paymentOrder;
    }

    /**
     * Generate Unique Payment
     *
     * @param ruId
     * @param ioId
     * @param thirdPartyId
     * @param periodicity
     * @param amount
     * @throws Exception
     */
    public void generateUniquePayments(String ruId, String ioId, String thirdPartyId, int periodicity, Double amount, double deducedAmount, ClaimReserveBenefit benefit,
                                       Date startDate, Date endDate, Date commitDate, BeneficiariesAndPercentageForm element, Collection selectedRiskUnits) throws Exception {
        ClaimNormalReserve normalReserve = benefit.getClaimNormalReserve();
        CoverageReserve coverageReserve = new CoverageReserve(normalReserve, this);
        Double reserveAmount = coverageReserve.getAmount();
        Double participationP = (amount * 100) / reserveAmount;
        Session session = null;
        Transaction transaction = null;
        int typeScheduled = 2;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }
            PaymentOrder paymentOrder;
            Collection thirdParties;
            SearchThirdPartyResult thirdParty;
            String thirdPartyName;
            long thirdPartyRoleId;
            Iterator thirdPartiesIterator;
            Date commitmentDate, currentDate, today;
            Calendar calendar;
            int days = 15;
            Double paymentAmount = amount;
            thirdPartyName = element.getName();
            thirdPartyRoleId = Long.parseLong(element.getRoleId());
            ClientResponse response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                    normalReserve.getPk(), true);
            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();
            checkClaimConfiguration(amount, 1, paymentOrderCollection, ec, typeScheduled, selectedRiskUnits);
            log.debug("********[Acsele v4.5] ClaimComposerWrapper.generateScheduledPayments(){" + "10191}thirdPartyName = " + thirdPartyName);
            today = new Date();
            currentDate = getConfiguredStartDate(ruId, ioId, normalReserve.getEvaluatedCoverage());
            calendar = Calendar.getInstance(UserInfo.getLocale());
            calendar.setTime(currentDate);
            if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY && (calendar.get(Calendar.DAY_OF_MONTH) != 1
                    && calendar.get(Calendar.DAY_OF_MONTH) != days)) {
                if (calendar.get(Calendar.DAY_OF_MONTH) > days) {
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, days);
                }
                currentDate = calendar.getTime();
            }
            Calendar calendarEndDate = Calendar.getInstance(UserInfo.getLocale());

            calendar.setTime(currentDate);
            if (periodicity == ClaimNormalReserve.PERIODICITY_DAYS) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            } else if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY) {
                if (calendar.get(Calendar.DAY_OF_MONTH) + 1 > days) {
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, days);
                }
            } else if (periodicity == ClaimNormalReserve.PERIODICITY_WEEKLY) {
                calendar.add(Calendar.DAY_OF_MONTH, 7);
            } else if (periodicity == ClaimNormalReserve.PERIODICITY_BIWEEKLY) {
                calendar.add(Calendar.DAY_OF_MONTH, 14);
            } else {
                calendar.add(Calendar.MONTH, 1);
            }

            calendarEndDate.setTime(currentDate);
            commitmentDate = currentDate;
            paymentOrder = new PaymentOrder(benefit);
            paymentOrder.setDate(today);

            ClaimsCoverageConfiguration configuration = ClaimsCoverageConfiguration
                    .load(Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()),
                            ec.getConfiguratedCoverageOA().getId());

            if (configuration != null && configuration.getFistPaymentDate() != null && !configuration.getFistPaymentDate().equals("")) {
                TablaSimbolos symbolsTable = new TablaSimbolos();
                Double date = this.evaluate(configuration.getFistPaymentDate(), symbolsTable, ec);
                Date calcCommitmentDate = Funciones.transformDate(date);
                log.debug("Commitment date: " + calcCommitmentDate);
                commitmentDate = calcCommitmentDate;
            }
            paymentOrder.setStartDate(startDate);
            paymentOrder.setCommitmentDate(commitDate);
            paymentOrder.setEndDate(endDate);
            currentDate = calendar.getTime();
            paymentOrder.setAmount(paymentAmount);
            paymentOrder.setBeneficiary(thirdPartyName);
            paymentOrder.setDistributionAmount(0);
            paymentOrder.setDoneBy(UserInfo.getUser());
            paymentOrder.setParticipationPercentage(participationP);
            paymentOrder.setPenaltyPercentage(0);
            paymentOrder.setReason("");
            paymentOrder.setRefundPercentage(0);
            paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
            paymentOrder.setThirdPartyId(Long.parseLong(thirdPartyId));
            paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
            paymentOrder.setType(com.consisint.acsele.claim.api.paymentorder.PaymentOrderType.PARTIAL.getValue());
            paymentOrder.setOnHold(0);
            paymentOrder.setCoverageDesc(ec.getDesc());
            paymentOrder.save();
            normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);

            if (deducedAmount > 0) {
                PaymentOrderDetail paymentOrderDetail = new PaymentOrderDetail();
                paymentOrderDetail.setPk(paymentOrder.getPk());
                paymentOrderDetail.setDeducedAmount(deducedAmount);
                paymentOrderDetail.save();
            }

            if (startTx) {
                HibernateUtil.commit(transaction, session);
            }
            generateReserveReinsuranceDistribution(normalReserve, String.valueOf(amount), Constants.PAYMENT_CLAIM,
                    new java.sql.Date(normalReserve.getDate().getTime()), null);
        } catch (Exception e) {
            session.clear();
            if (startTx) {
                HibernateUtil.rollBack(transaction);
            }
            log.error("Error generating the payment orders.", e);
            ExceptionUtil.handlerException(e);
        }
    }


    public PaymentOrder generateUniquePayments(String ruId, String ioId, String thirdPartyId, int periodicity, Double amount, double deducedAmount, ClaimReserveBenefit benefit,
                                               Date startDate, Date endDate, Date commitDate, String thirdPartyName, Long thirdPartyRoleId) throws Exception {
        ClaimNormalReserve normalReserve = benefit.getClaimNormalReserve();
        CoverageReserve coverageReserve = new CoverageReserve(normalReserve, this);
        Double reserveAmount = coverageReserve.getAmount();
        Double participationP = (amount * 100) / reserveAmount;
        Session session = null;
        Transaction transaction = null;
        int typeScheduled = 2;
        PaymentOrder paymentOrder = null;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }

//            int paymentOrderPk;
            Collection thirdParties;
            SearchThirdPartyResult thirdParty;
            Iterator thirdPartiesIterator;
            Date commitmentDate, currentDate, today;
            Calendar calendar;
            int days = 15;
            //Double paymentAmount = new Double(amount.doubleValue() / numberOfPayments);
            Double paymentAmount = amount; //Verificar contra la validacion javascript que hace en claimBenefit.jsp en la funcion loadBenefitPayments(...)
            ClientResponse response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                    normalReserve.getPk(), true);
            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();
            checkClaimConfiguration(amount, 1, paymentOrderCollection, ec, typeScheduled);
            log.debug("********[Acsele v4.5] ClaimComposerWrapper.generateScheduledPayments(){" + "10191}thirdPartyName = " + thirdPartyName);
            today = new Date();
            //  obtiene fecha Inicial del campo ocurrenceDate del claim y/o aplicando propiedades de la cobertura
            currentDate = getConfiguredStartDate(ruId, ioId, normalReserve.getEvaluatedCoverage());
            calendar = Calendar.getInstance(UserInfo.getLocale());
            calendar.setTime(currentDate);
            if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY && (calendar.get(Calendar.DAY_OF_MONTH) != 1
                    && calendar.get(Calendar.DAY_OF_MONTH) != days)) {
                if (calendar.get(Calendar.DAY_OF_MONTH) > days) {
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, days);
                }
                currentDate = calendar.getTime();
            }
            Calendar calendarEndDate = Calendar.getInstance(UserInfo.getLocale());

            calendar.setTime(currentDate);
            if (periodicity == ClaimNormalReserve.PERIODICITY_DAYS) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            } else if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY) {
                if (calendar.get(Calendar.DAY_OF_MONTH) + 1 > days) {
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, days);
                }
            } else if (periodicity == ClaimNormalReserve.PERIODICITY_WEEKLY) {
                calendar.add(Calendar.DAY_OF_MONTH, 7);
            } else if (periodicity == ClaimNormalReserve.PERIODICITY_BIWEEKLY) {
                calendar.add(Calendar.DAY_OF_MONTH, 14);
            } else {    // ClaimNormalReserve.PERIODICITY_MONTHS or PERIODICITY_NONE
                calendar.add(Calendar.MONTH, 1);
            }

            calendarEndDate.setTime(currentDate);
            commitmentDate = currentDate;
            paymentOrder = new PaymentOrder(benefit);
            paymentOrder.setDate(today);
            paymentOrder.setBeneficiaryOpc(ThirdParty.getInstance(Long.valueOf(thirdPartyId)));
            ClaimsCoverageConfiguration configuration = ClaimsCoverageConfiguration
                    .load(Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()),
                            ec.getConfiguratedCoverageOA().getId());

            if (configuration != null && configuration.getFistPaymentDate() != null && !configuration.getFistPaymentDate().equals("")) {
                TablaSimbolos symbolsTable = new TablaSimbolos();
                Double date = this.evaluate(configuration.getFistPaymentDate(), symbolsTable, ec);
                Date calcCommitmentDate = Funciones.transformDate(date);
                log.debug("Commitment date: " + calcCommitmentDate);
                commitmentDate = calcCommitmentDate;
            }
            paymentOrder.setStartDate(startDate);
            paymentOrder.setCommitmentDate(commitDate);
            paymentOrder.setEndDate(endDate);
            currentDate = calendar.getTime();
            paymentOrder.setAmount(paymentAmount);
            paymentOrder.setBeneficiary(thirdPartyName);
            paymentOrder.setDistributionAmount(0);
            paymentOrder.setDoneBy(UserInfo.getUser());
            paymentOrder.setParticipationPercentage(participationP);
            paymentOrder.setPenaltyPercentage(0);
            paymentOrder.setReason("");
            paymentOrder.setRefundPercentage(0);
            paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
            paymentOrder.setThirdPartyId(Long.parseLong(thirdPartyId));
            paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
            paymentOrder.setType(com.consisint.acsele.claim.api.paymentorder.PaymentOrderType.PARTIAL.getValue());
            paymentOrder.setOnHold(0);
            paymentOrder.setCoverageDesc(ec.getDesc());
            paymentOrder.save();
            normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);

            if (deducedAmount > 0) {
                PaymentOrderDetail paymentOrderDetail = new PaymentOrderDetail();
                paymentOrderDetail.setPk(paymentOrder.getPk());
                paymentOrderDetail.setDeducedAmount(deducedAmount);
                paymentOrderDetail.save();
            }

            if (startTx) {
                HibernateUtil.commit(transaction, session);
            }
        } catch (Exception e) {
            session.clear();
            if (startTx) {
                HibernateUtil.rollBack(transaction);
            }
            log.debug("Error generating the payment orders.", e);
            ExceptionUtil.handlerException(e);
//        } finally {
            //HibernateUtil.closeSession(session);
        }

        return paymentOrder;
    }

    public PaymentOrder generateUniquePayments(String ruId, String ioId, com.consisint.acsele.thirdparty.api.ThirdParty thirdParty, int periodicity, Double amount,
                                               ClaimNormalReserve normalReserve, long thirdPartyRoleId, Date startDate, Date endDate,
                                               Date commitDate, Collection selectedRiskUnits) throws Exception {
        Session session = null;
        Transaction transaction = null;
        int typeScheduled = 2;
        PaymentOrder paymentOrder = null;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }
            String thirdPartyName = thirdParty.getName();
            Date today;
            Calendar calendar;
            int days = 15;

            Double paymentAmount = amount; //Verificar contra la validacion javascript que hace en claimBenefit.jsp en la funcion loadBenefitPayments(...)
            ClientResponse response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                    normalReserve.getPk(), true);
            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();

            checkClaimConfiguration(amount, 1, paymentOrderCollection, ec, typeScheduled, selectedRiskUnits);
            today = new Date();

            paymentOrder = new PaymentOrder(normalReserve);
            paymentOrder.setDate(today);
            //paymentOrder.setDate(this.getClaim().getOcurrenceDate());
            paymentOrder.setStartDate(startDate);
            paymentOrder.setCommitmentDate(commitDate);
            paymentOrder.setEndDate(endDate);
            paymentOrder.setAmount(paymentAmount);
            paymentOrder.setBeneficiary(thirdPartyName);
            paymentOrder.setDistributionAmount(0);
            paymentOrder.setDoneBy(UserInfo.getUser());
            paymentOrder.setParticipationPercentage(0);
            paymentOrder.setPenaltyPercentage(0);
            paymentOrder.setReason("");
            paymentOrder.setRefundPercentage(0);
            paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
            paymentOrder.setThirdPartyId(thirdParty.getId());
            paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
            paymentOrder.setType(PaymentOrderType.PARTIAL.getValue());
            paymentOrder.setOnHold(0);
            paymentOrder.setCoverageDesc(ec.getDesc());
            paymentOrder.save();
            normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);

            if (startTx) {
                HibernateUtil.commit(transaction, session);
            }
        } catch (Exception e) {
            session.clear();
            if (startTx) {
                HibernateUtil.rollBack(transaction);
            }
            log.debug("Error generating the payment orders.", e);
            ExceptionUtil.handlerException(e);
//        } finally {
            //HibernateUtil.closeSession(session);
        }
        return paymentOrder;
    }

    /**
     * Generate Variables Payments
     *
     * @param ruId
     * @param ioId
     * @param thirdPartyId
     * @param periodicity
     * @param amount
     * @param numberOfPayments
     * @param benefitDuration
     * @throws Exception
     */
    public void generateVariablesPayments(String ruId, String ioId, String thirdPartyId, String normalReservePk, int periodicity, int benefitDuration,
                                          Double amount
                                          /*Monto del beneficio*/, int numberOfPayments, Date startDate, Collection selectedRiskUnits) throws Exception {
        /* TODO: OJO ACTUALIZAR la GENERACION DE FECHAS ********************************/
        Session session = null;
        Transaction transaction = null;
        int typeScheduled = 2;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }
            PaymentOrder paymentOrder;
            Collection thirdParties;
            SearchThirdPartyResult thirdParty;
            String thirdPartyName = null;
            int thirdPartyRoleId = 0;
            Iterator thirdPartiesIterator;
            Date commitmentDate, today;
            Calendar calendar;
            int days = 15;
            ClaimNormalReserve normalReserve = (ClaimNormalReserve) HibernateUtil.load(ClaimNormalReserve.class, normalReservePk);
            thirdParties = getBeneficiariesForPayments(normalReserve.getEvaluatedCoverage());
            thirdParties.addAll(this.getThirdPartiesFromInsuranceObject(ruId, ioId));

            Collection manualBeneficiaries = this.getManualBeneficiaries();
            if (!manualBeneficiaries.isEmpty()) {
                thirdParties.addAll(manualBeneficiaries);
            }

            thirdPartiesIterator = thirdParties.iterator();
            while (thirdPartiesIterator.hasNext()) {
                thirdParty = (SearchThirdPartyResult) thirdPartiesIterator.next();
                if (thirdParty.getThirdPartyId().trim().equals(thirdPartyId.trim())) {
                    thirdPartyName = thirdParty.getThidPartyName();
                    thirdPartyRoleId = thirdParty.getRoleID();
                }
            }
            ClientResponse response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                    normalReserve.getPk(), true);
            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();
            checkClaimConfiguration(amount, numberOfPayments, paymentOrderCollection, ec, typeScheduled, selectedRiskUnits);

            calendar = Calendar.getInstance(UserInfo.getLocale());
            long confCovId = ec.getConfiguratedCoverageOA().getId();
            List variablesPayment = ClaimsCoverageConfiguration
                    .loadVariablesPaymentByCovIdAndProId(confCovId, Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()));
            if (variablesPayment == null) {
                variablesPayment = new ArrayList();
            }
            //TODO: A�adir a la lista de variables payment el payment restante.
            ClaimsCoverageConfiguration ccc = ClaimsCoverageConfiguration
                    .load(Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()), confCovId);
            //Change date to generate retroactive
            if (ccc.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                today = this.getClaim().getOcurrenceDate();
            } else {
                today = new Date();
            }
            ExpresionEvaluator evaluator = new ExpresionEvaluator();
            long idClaim = this.getClaim().getId();
            this.fillSymbolTableClaim(String.valueOf(idClaim), evaluator.getTablaSimbolos());

            VariablesPayment remainingPayment = ccc.getRemainingVariablePayment(evaluator);
            variablesPayment.add(remainingPayment);
            double benefitAmount = amount.doubleValue();
            int i = 0;
            for (Iterator iter = variablesPayment.iterator(); iter.hasNext() && benefitAmount > 0; i++) {
                calendar.setTime(startDate);
                if (periodicity == ClaimNormalReserve.PERIODICITY_DAYS) {
                    if (i != 0) {
                        calendar.add(Calendar.DAY_OF_MONTH, benefitDuration);
                    }
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);

                    if (calendar.get(Calendar.DAY_OF_MONTH) > days) {
                        calendar.add(Calendar.MONTH, 1);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        calendar.add(Calendar.DAY_OF_MONTH, -1);

                        calendar.setTime(calendar.getTime());
                    } else {
                        calendar.set(Calendar.DAY_OF_MONTH, days);
                        calendar.setTime(calendar.getTime());
                    }
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_WEEKLY) {
                    if (i != 0) {
                        calendar.add(Calendar.DAY_OF_MONTH, 7);
                    }
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_BIWEEKLY) {
                    if (i != 0) {
                        calendar.add(Calendar.DAY_OF_MONTH, 14);
                    }
                } else {
                    if (i != 0) {
                        calendar.add(Calendar.MONTH, benefitDuration);
                    }
                }

                if (ccc.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                    commitmentDate = DateUtil.sumDaysToDate(this.getClaim().getOcurrenceDate(), ccc.getWaitingPeriod());
                    log.debug("CCW generateVariablesPayments Commitment date RETROACTIVE_TYPE " + commitmentDate);
                } else {
                    commitmentDate = calendar.getTime();
                    log.debug("CCW generateVariablesPayments Commitment date " + commitmentDate);
                }
                VariablesPayment vp = (VariablesPayment) iter.next();
                paymentOrder = new PaymentOrder(normalReserve);
                //TODO: Validar que el monto restante cubre el monto del Variable Payment.
                if (benefitAmount > vp.getAmount()) {
                    paymentOrder.setAmount(new Double(vp.getAmount()));
                    benefitAmount -= vp.getAmount();
                } else {
                    paymentOrder.setAmount(new Double(benefitAmount));
                    benefitAmount = 0.0;
                }
                paymentOrder.setBeneficiary(thirdPartyName);
                paymentOrder.setCommitmentDate(commitmentDate);
                paymentOrder.setEndDate(commitmentDate);
                paymentOrder.setDate(today);
                paymentOrder.setDistributionAmount(0);
                paymentOrder.setDoneBy(UserInfo.getUser());
                paymentOrder.setParticipationPercentage(0);
                paymentOrder.setPenaltyPercentage(0);
                //I should use a default reason, if i have the time
                paymentOrder.setReason("");
                paymentOrder.setRefundPercentage(0);
                paymentOrder.setStartDate(startDate);
                paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
                paymentOrder.setThirdPartyId(Long.parseLong(thirdPartyId));
                paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
                paymentOrder.setType(PaymentOrderType.PARTIAL.getValue());
                paymentOrder.setOnHold(0);
                paymentOrder.setCoverageDesc(ec.getDesc());
                paymentOrder.save();

                // save State in the history payments order
                normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);
                startDate = commitmentDate;
            }
            if (startTx) {
                HibernateUtil.commit(transaction, session);
            }
        } catch (Exception e) {
            session.clear();
            if (startTx) {
                HibernateUtil.rollBack(transaction);
            }
            log.debug("Error generating the payment orders.", e);
            ExceptionUtil.handlerException(e);
//        } finally {
            //HibernateUtil.closeSession(session);
        }
    }

    public String generateVariablesPayments(String ruId, String ioId, String thirdPartyId, int periodicity, int benefitDuration, Double amount
            , double deducedAmount, int numberOfPayments, Date startDate,
                                            ClaimReserveBenefit benefit, Collection selectedRiskUnits) throws Exception {
        ClaimNormalReserve normalReserve = benefit.getClaimNormalReserve();
        Session session = null;
        Transaction transaction = null;
        int typeScheduled = 2;
        String message = null;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }
            PaymentOrder paymentOrder;
            Collection thirdParties;
            SearchThirdPartyResult thirdParty;
            String thirdPartyName = null;
            int thirdPartyRoleId = 0;
            Iterator thirdPartiesIterator;
            Date commitmentDate, today;
            Calendar calendar;
            int days = 15;

            thirdParties = getBeneficiariesForPayments(normalReserve.getEvaluatedCoverage());
            thirdParties.addAll(this.getThirdPartiesFromInsuranceObject(ruId, ioId));

            Collection manualBeneficiaries = this.getManualBeneficiaries();
            if (!manualBeneficiaries.isEmpty()) {
                thirdParties.addAll(manualBeneficiaries);
            }

            thirdPartiesIterator = thirdParties.iterator();
            while (thirdPartiesIterator.hasNext()) {
                thirdParty = (SearchThirdPartyResult) thirdPartiesIterator.next();
                if (thirdParty.getThirdPartyId().trim().equals(thirdPartyId.trim())) {
                    thirdPartyName = thirdParty.getThidPartyName();
                    thirdPartyRoleId = thirdParty.getRoleID();
                }
            }
            ClientResponse response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                    normalReserve.getPk(), true);
            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();
            checkClaimConfiguration(amount, numberOfPayments, paymentOrderCollection, ec, typeScheduled, selectedRiskUnits);

            calendar = Calendar.getInstance(UserInfo.getLocale());

            long confCovId = ec.getConfiguratedCoverageOA().getId();

            List variablesPayment = ClaimsCoverageConfiguration
                    .loadVariablesPaymentByCovIdAndProId(confCovId, Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()));
            if (variablesPayment == null) {
                variablesPayment = new ArrayList();
            }
            ClaimsCoverageConfiguration ccc = ClaimsCoverageConfiguration
                    .load(Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()), confCovId);

            if (ccc.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                today = this.getClaim().getOcurrenceDate();
            } else {
                today = new Date();
            }
            ExpresionEvaluator evaluator = new ExpresionEvaluator();
            long claimId = this.getClaim().getId();
            this.fillSymbolTableClaim(String.valueOf(claimId), evaluator.getTablaSimbolos());

            VariablesPayment remainingPayment = ccc.getRemainingVariablePayment(evaluator);
            variablesPayment.add(remainingPayment);
            Calendar calendarEndDate = Calendar.getInstance(UserInfo.getLocale());
            double benefitAmount = amount.doubleValue();
            int i = 0;
            for (Iterator iter = variablesPayment.iterator(); iter.hasNext() && benefitAmount > 0; i++) {
                calendar.setTime(startDate);
                if (periodicity == ClaimNormalReserve.PERIODICITY_DAYS) {
                    calendar.add(Calendar.DAY_OF_MONTH, benefitDuration);
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    if (calendar.get(Calendar.DAY_OF_MONTH) > days) {
                        calendar.add(Calendar.MONTH, 1);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        calendar.add(Calendar.DAY_OF_MONTH, -1);
                        calendar.setTime(calendar.getTime());
                    } else {
                        calendar.set(Calendar.DAY_OF_MONTH, days);
                        calendar.setTime(calendar.getTime());
                    }
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_WEEKLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 7);
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_BIWEEKLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 14);
                } else {
                    calendar.add(Calendar.MONTH, benefitDuration);
                }

                if (ccc.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                    commitmentDate = DateUtil.sumDaysToDate(this.getClaim().getOcurrenceDate(), ccc.getWaitingPeriod());
                    log.debug("CCW generateVariablesPayments Commitment date RETROACTIVE_TYPE " + commitmentDate);
                } else {
                    commitmentDate = startDate;
                    log.debug("CCW generateVariablesPayments Commitment date " + commitmentDate);
                }
                calendarEndDate.setTime(calendar.getTime());
                calendarEndDate.add(Calendar.DATE, -1);
                VariablesPayment vp = (VariablesPayment) iter.next();
                paymentOrder = new PaymentOrder(benefit);
                if (benefitAmount > vp.getAmount()) {
                    paymentOrder.setAmount(new Double(vp.getAmount()));
                    benefitAmount -= vp.getAmount();
                } else {
                    paymentOrder.setAmount(new Double(benefitAmount));
                    benefitAmount = 0.0;
                }
                paymentOrder.setBeneficiary(thirdPartyName);
                paymentOrder.setCommitmentDate(commitmentDate);
                paymentOrder.setEndDate(calendarEndDate.getTime());

                message = verifyDatesPaymentOrder(normalReserve.getPk(), startDate, paymentOrder.getEndDate(), Long.parseLong(thirdPartyId), ccc.isAllowPaymentsInOneDay());

                if (message == null) {
                    paymentOrder.setDate(today);
                    paymentOrder.setDistributionAmount(0);
                    paymentOrder.setDoneBy(UserInfo.getUser());
                    paymentOrder.setParticipationPercentage(0);
                    paymentOrder.setPenaltyPercentage(0);
                    paymentOrder.setReason("");
                    paymentOrder.setRefundPercentage(0);
                    paymentOrder.setStartDate(startDate);
                    paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
                    paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
                    //benefit recipient for payments
                    long thirdPartyIdn = Long.parseLong(thirdPartyId);
                    ClaimBeneficiaryPersister claimBeneficiaryPersister = ClaimBeneficiaryPersister.Impl.getInstance();
                    ClaimBeneficiaryPK pk = new ClaimBeneficiaryPK(benefit.getId(), thirdPartyIdn, paymentOrder.getThirdPartyRoleID());
                    ClaimBeneficiary claimBeneficiary = claimBeneficiaryPersister.load(pk);
                    if (claimBeneficiary != null && claimBeneficiary.getRecipientThirdParty() != null) {
                        paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                        paymentOrder.setThirdPartyId(claimBeneficiary.getRecipientThirdParty().getId());
                    } else {
                        paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                        paymentOrder.setThirdPartyId(thirdPartyIdn);
                    }
                    paymentOrder.setType(PaymentOrderType.PARTIAL.getValue());
                    paymentOrder.setOnHold(0);
                    paymentOrder.setCoverageDesc(ec.getDesc());
                    paymentOrder.save();
                    normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);
                    startDate = calendar.getTime();

                    if (deducedAmount > 0) {
                        PaymentOrderDetail paymentOrderDetail = new PaymentOrderDetail();
                        paymentOrderDetail.setPk(paymentOrder.getPk());
                        paymentOrderDetail.setDeducedAmount(deducedAmount);
                        paymentOrderDetail.save();
                    }
                }
            }
            if (message == null) {
                benefit.setBenefitPayments(i + 1);
                if (startTx) {
                    HibernateUtil.commit(transaction, session);
                }
            }
            generateReserveReinsuranceDistribution(normalReserve, String.valueOf(amount), Constants.PAYMENT_CLAIM,
                    new java.sql.Date(normalReserve.getDate().getTime()), null);
        } catch (Exception e) {
            session.clear();
            if (startTx) {
                HibernateUtil.rollBack(transaction);
            }
            log.debug("Error generating the payment orders.", e);
            ExceptionUtil.handlerException(e);
        }
        return message;
    }

    /**
     * Generate benefit multiple payments
     *
     * @param ruId
     * @param ioId
     * @param thirdPartyId
     * @param periodicity
     * @param amount
     * @param numberOfPayments
     * @param startDate
     * @param endDate
     * @param selectedRiskUnits
     * @throws Exception
     */
    public String generateMultiplePayments(String ruId, String ioId, String thirdPartyId, ClaimNormalReserve normalReserve, int periodicity,
                                           Double amount, double deducedAmount, int numberOfPayments, Date startDate, Date endDate,
                                           ClaimReserveBenefit benefit, double totalPercentage, BeneficiariesAndPercentageForm element, Collection selectedRiskUnits, double firstQuote, double finalQuote, double remainQuote) throws Exception {
        Session session = null;
        Transaction transaction = null;
        String message = null;
        int typeMultiple = 1;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }
            PaymentOrder paymentOrder;
            Collection thirdParties;
            SearchThirdPartyResult thirdParty;
            String thirdPartyName;
            long thirdPartyRoleId;
            Iterator thirdPartiesIterator;
            Date commitmentDate, currentDate, today;
            Calendar calendar;
            int numberOfPaymentsQuote = numberOfPayments;
            if (firstQuote > 0) {
                numberOfPaymentsQuote = numberOfPaymentsQuote - 1;
            }
            if (finalQuote > 0) {
                numberOfPaymentsQuote = numberOfPaymentsQuote - 1;
            }
            Double paymentAmount = new Double(amount.doubleValue() - firstQuote - finalQuote) / numberOfPaymentsQuote;
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();
            thirdPartyName = element.getName();
            thirdPartyRoleId = Long.parseLong(element.getRoleId());

            ClientResponse response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                    normalReserve.getPk(), true);

            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
            checkClaimConfiguration(amount, numberOfPayments, paymentOrderCollection, ec, typeMultiple, selectedRiskUnits);

            ClaimsCoverageConfiguration ccc = ClaimsCoverageConfiguration
                    .load(Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()),
                            ec.getConfiguratedCoverageOA().getId());

            if (ccc.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                today = this.getClaim().getOcurrenceDate();
            } else {
                today = new Date();
            }
            calendar = Calendar.getInstance(UserInfo.getLocale());
            Calendar calendarEndDate = Calendar.getInstance(UserInfo.getLocale());

            int difference = (int) (DateUtil.daysBetween(startDate, endDate) / numberOfPayments);
            for (int i = 0; i < numberOfPayments; i++) {
                calendar.setTime(startDate);
                calendarEndDate.setTime(startDate);
                calendar.add(Calendar.DATE, i * difference);
                calendarEndDate.add(Calendar.DATE, ((i + 1) * difference) - 1 * ((difference > 0 ? 1 : 0)));
                commitmentDate = calendar.getTime();
                log.debug("StarDate = " + calendar.getTime().toString());
                log.debug("CommitmentDate = " + calendar.getTime().toString());
                log.debug("EndDate = " + calendarEndDate.getTime().toString());
                paymentOrder = new PaymentOrder(benefit);
                paymentOrder.setDate(today);
                paymentOrder.setStartDate(calendar.getTime());
                if (ccc != null && ccc.getFistPaymentDate() != null && !ccc.getFistPaymentDate().equals("") && i == 0) {
                    TablaSimbolos symbolsTable = new TablaSimbolos();
                    Double date = this.evaluate(ccc.getFistPaymentDate(), symbolsTable, ec);
                    Date calcCommitmentDate = Funciones.transformDate(date);
                    log.debug("CCW Commitment date: [1] " + calcCommitmentDate);
                    commitmentDate = calcCommitmentDate;
                }
                if (ccc.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                    commitmentDate = DateUtil.sumDaysToDate(this.getClaim().getOcurrenceDate(), ccc.getWaitingPeriod());
                    log.debug("CCW Commitment date: [2] RETROACTIVE_TYPE " + commitmentDate);
                }
                paymentOrder.setCommitmentDate(commitmentDate);
                if (i == (numberOfPayments - 1)) {
                    paymentOrder.setEndDate(endDate);
                } else {
                    paymentOrder.setEndDate(calendarEndDate.getTime());
                }

                message = verifyDatesPaymentOrder(normalReserve.getPk(), startDate, endDate, Long.parseLong(thirdPartyId), ccc.isAllowPaymentsInOneDay());
                if (message == null) {
                    if (i == 0 && firstQuote > 0) {
                        paymentAmount = firstQuote;
                    } else {
                        paymentAmount = new Double(amount.doubleValue() - firstQuote - finalQuote) / numberOfPaymentsQuote;
                    }
                    if (i == numberOfPayments - 1 && finalQuote > 0) {
                        paymentAmount = finalQuote;
                    }
                    paymentOrder.setAmount(paymentAmount);
                    paymentOrder.setBeneficiary(thirdPartyName);
                    paymentOrder.setDistributionAmount(0);
                    paymentOrder.setDoneBy(UserInfo.getUser());
                    paymentOrder.setParticipationPercentage(totalPercentage);
                    paymentOrder.setPenaltyPercentage(0);
                    paymentOrder.setReason("");
                    paymentOrder.setRefundPercentage(0);
                    paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
                    paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
                    long thirdPartyIdn = Long.parseLong(thirdPartyId);
                    ClaimBeneficiaryPersister claimBeneficiaryPersister = ClaimBeneficiaryPersister.Impl.getInstance();
                    ClaimBeneficiaryPK pk = new ClaimBeneficiaryPK(benefit.getId(), thirdPartyIdn, paymentOrder.getThirdPartyRoleID());
                    ClaimBeneficiary claimBeneficiary = claimBeneficiaryPersister.load(pk);
                    if (claimBeneficiary != null && claimBeneficiary.getRecipientThirdParty() != null) {
                        paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                        paymentOrder.setThirdPartyId(claimBeneficiary.getRecipientThirdParty().getId());
                    } else {
                        paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                        paymentOrder.setThirdPartyId(thirdPartyIdn);
                    }
                    paymentOrder.setType(PaymentOrderType.PARTIAL.getValue());
                    paymentOrder.setOnHold(0);
                    paymentOrder.setCoverageDesc(ec.getDesc());
                    paymentOrder.save();
                    normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);

                    if (deducedAmount > 0) {
                        PaymentOrderDetail paymentOrderDetail = new PaymentOrderDetail();
                        paymentOrderDetail.setPk(paymentOrder.getPk());
                        paymentOrderDetail.setDeducedAmount(deducedAmount);
                        paymentOrderDetail.save();
                    }
                }

                generateReserveReinsuranceDistribution(normalReserve, String.valueOf(amount), Constants.PAYMENT_CLAIM,
                        new java.sql.Date(normalReserve.getDate().getTime()), null);
            }
            if (message == null) {
                if (startTx) {
                    HibernateUtil.commit(transaction, session);
                }
            }

        } catch (Exception e) {
            session.clear();
            if (startTx) {
                HibernateUtil.rollBack(transaction);
            }
            ExceptionUtil.handleException("Error while trying to generate the payment orders", e);
        } finally {
            log.debug("generateMultiplePayments(.........) - Saliendo");
        }
        return message;
    }

    /**
     * Check the claim coverage configuration values
     *
     * @param amount
     * @param numberOfPayments
     * @param paymentOrderCollection
     * @param ec
     * @param type
     * @param selectedRiskUnits
     * @throws Exception
     */
    private void checkClaimConfiguration(Double amount, int numberOfPayments, PaymentOrderCollection paymentOrderCollection, EvaluatedCoverage ec,
                                         int type, Collection selectedRiskUnits) throws Exception {
        int benefitPayments = numberOfPayments;
        double benefitAmount = 0.0;

        if (type == 1) {
            //El tipo de beneficio es multiple
            benefitAmount = amount.doubleValue() / benefitPayments;
        }

        if (type == 2) {
            //El tipo de beneficio es agendado
            benefitAmount = amount.doubleValue();
        }

        if (paymentOrderCollection != null) {
            benefitPayments += paymentOrderCollection.getBenefitPayments();
            benefitAmount += paymentOrderCollection.getBenefitsAmount();
        }

        long confCovId = ec.getConfiguratedCoverageOA().getId();
        String coverageDesc = ec.getConfiguratedCoverageOA().getDesc();
        log.debug("coverageDesc" + coverageDesc);

        ClaimsCoverageConfiguration configuration = ClaimsCoverageConfiguration
                .load(this.getAgregatedPolicy().getProduct().getId(), confCovId);

        //******************* mew validator
        TablaSimbolos symbolsTable = new TablaSimbolos();
        int maxBenefitPayment = (int) this.evaluate(configuration.getMaxBenefitPayment(), symbolsTable, ec);
        if (maxBenefitPayment > 0.0 && maxBenefitPayment < benefitPayments) {
            throw new ApplicationException(Exceptions.CCMaximunPaymentReached, Severity.INFO);
            //return goErrorForward(ae.getKeyCode());
        }

//            double maxBenefAmount = configuration.getMaxBenefitAmount().doubleValue();
//        log.debug("[*10250*] -YJA- ClaimComposerWrapper.checkClaimConfiguration");
        double maxBenefAmount = this.evaluate(configuration.getMaxAmount(), symbolsTable, null);
//        log.debug("[*10254*] -YJA- maxBenefAmount = " + maxBenefAmount);

        Currency claimReserveCurrency = paymentOrderCollection.getClaimReserve().getCurrency();
        Currency policyCurrecy = policy.getCurrency();
        if (!claimReserveCurrency.equals(policyCurrecy)) {
            double rate = calculateExchangeRate(claimReserveCurrency, policyCurrecy, new Date());
            benefitAmount *= rate;
        }

        if (maxBenefAmount > 0 && maxBenefAmount < benefitAmount) {
            throw new ApplicationException(Exceptions.CCMaxAmoutPerBenefit, Severity.INFO);
            // return goErrorForward(ae.getKeyCode());
        }

        if ("false".equalsIgnoreCase(AcseleConf.getProperty("validationAtApprovePaymentTime"))) {
            String eventClaimId = String.valueOf(this.getClaim().getEventClaim().getId());
            ClaimValidator.validatePolicyActive(this.getAgregatedPolicy(), this.getClaim().getOcurrenceDate(), eventClaimId, ec, selectedRiskUnits);            //         ClaimValidator.validateMaximumBenefitAllowedForInsured(this.claim, amount,false);
            //ClaimValidator.validatePolicyActive(composer.getAgregatedPolicy(), composer.getClaim().getOcurrenceDate(), eventClaim.getDesc(), getHttpServletRequest());
            log.debug("rjc - ClaimComposerWrapper: validateMaximumAmountByCoverage");
            ClaimValidator.validateMaximumAmountByCoverage(claim, configuration, amount, coverageDesc, false, ec, paymentOrderCollection.getClaimReserve());
            ClaimValidator.validateMaximumBenefitPayment(claim, amount, false);
            ClaimValidator.validateExclusionPeriodPayment(claim, configuration, false);
            //        ClaimValidator.validateOpenItemPremiumIsPaid(this.getClaim().getPolicyId(),
            //                                                     this.getClaim().getOcurrenceDate());
            if (configuration.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                ClaimValidator.validateRetroactiveWaitingPeriod(this.claim, configuration, false);
            }
            ClaimValidator.validateOpenItemPremiumIsPaid(this.getClaim().getPolicyId(), this.getClaim().getOcurrenceDate());
        }


    }


    private void checkClaimConfiguration(Double amount, int numberOfPayments, PaymentOrderCollection paymentOrderCollection, EvaluatedCoverage ec,
                                         int type) throws Exception {
        int benefitPayments = numberOfPayments;
        double benefitAmount = 0.0;

        if (type == 1) {
            benefitAmount = amount.doubleValue() / benefitPayments;
        }

        if (type == 2) {
            benefitAmount = amount.doubleValue();
        }

        if (paymentOrderCollection != null) {
            benefitPayments += paymentOrderCollection.getBenefitPayments();
            benefitAmount += paymentOrderCollection.getBenefitsAmount();
        }

        long confCovId = ec.getConfiguratedCoverageOA().getId();
        String coverageDesc = ec.getConfiguratedCoverageOA().getDesc();
        log.debug("coverageDesc" + coverageDesc);

        ClaimsCoverageConfiguration configuration = ClaimsCoverageConfiguration
                .load(this.getAgregatedPolicy().getProduct().getId(), confCovId);

        //******************* mew validator
        TablaSimbolos symbolsTable = new TablaSimbolos();
        int maxBenefitPayment = (int) this.evaluate(configuration.getMaxBenefitPayment(), symbolsTable, ec);
        if (maxBenefitPayment > 0.0 && maxBenefitPayment < benefitPayments) {
            throw new ApplicationException(Exceptions.CCMaximunPaymentReached, Severity.INFO);
            //return goErrorForward(ae.getKeyCode());
        }

        double maxBenefAmount = this.evaluate(configuration.getMaxAmount(), symbolsTable, null);
        Currency claimReserveCurrency = paymentOrderCollection.getClaimReserve().getCurrency();
        Currency policyCurrecy = policy.getCurrency();
        if (!claimReserveCurrency.equals(policyCurrecy)) {
            double rate = calculateExchangeRate(claimReserveCurrency, policyCurrecy, new Date());
            benefitAmount *= rate;
        }

        if (maxBenefAmount > 0 && maxBenefAmount < benefitAmount) {
            throw new ApplicationException(Exceptions.CCMaxAmoutPerBenefit, Severity.INFO);
        }

        if ("false".equalsIgnoreCase(AcseleConf.getProperty("validationAtApprovePaymentTime"))) {
            String eventClaimId = this.getClaim().getEventClaim().getDesc();
            ClaimValidator.validatePolicyActive(this.getAgregatedPolicy(), this.getClaim().getOcurrenceDate(), eventClaimId, ec);            //         ClaimValidator.validateMaximumBenefitAllowedForInsured(this.claim, amount,false);
            log.debug("rjc - ClaimComposerWrapper: validateMaximumAmountByCoverage");
            ClaimValidator.validateMaximumAmountByCoverage(claim, configuration, amount, coverageDesc, false, ec, paymentOrderCollection.getClaimReserve());
            ClaimValidator.validateMaximumBenefitPayment(claim, amount, false);
            ClaimValidator.validateExclusionPeriodPayment(claim, configuration, false);
            //        ClaimValidator.validateOpenItemPremiumIsPaid(this.getClaim().getPolicyId(),
            //                                                     this.getClaim().getOcurrenceDate());
            if (configuration.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                ClaimValidator.validateRetroactiveWaitingPeriod(this.claim, configuration, false);
            }
            ClaimValidator.validateOpenItemPremiumIsPaid(this.getClaim().getPolicyId(), this.getClaim().getOcurrenceDate());
        }


    }


    public void validatePayments(ClaimsCoverageConfiguration configuration, Double amount, String coverageDesc) throws Exception {
        try {
            ClaimValidator.validatePolicyActive(this.getAgregatedPolicy(), this.getClaim().getOcurrenceDate());
            // ClaimValidator.validateMaximumBenefitAllowedForInsured(this.claim, amount,true);
            // ClaimValidator.validateMaximumAmountByCoverage(this.claim, configuration, amount,coverageDesc,true );
            ClaimValidator.validateMaximumBenefitPayment(this.claim, amount, true);
            ClaimValidator.validateExclusionPeriodPayment(this.claim, configuration, true);
            //        ClaimValidator.validateOpenItemPremiumIsPaid(this.getClaim().getPolicyId(),
            //                                                     this.getClaim().getOcurrenceDate());
            if (configuration.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                ClaimValidator.validateRetroactiveWaitingPeriod(this.claim, configuration, true);
            }
            ClaimValidator.validateOpenItemPremiumIsPaid(this.getClaim().getPolicyId(), this.getClaim().getOcurrenceDate());
        } catch (Exception e) {
            log.error("[ERROR] Validation payments to try approve]");
            //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }
    }

    /**
     * @param paymentOrder
     * @param dateStr
     */
    public void setCommitmentDate(PaymentOrder paymentOrder, String dateStr) {
        SimpleDateFormat sdf = DateUtil.getFormatToShow();
        try {
            String days = AcseleConf.getProperty("commitmentDays");
            Date inputDate = sdf.parse(dateStr);
            Date maxDate = Funciones.sumDaysToDate(inputDate, days);
            if (inputDate.compareTo(maxDate) <= 0) {
                paymentOrder.setCommitmentDate(inputDate);
            }
        } catch (ParseException e) {
            log.error("[ERROR]UpdateClaimPaymentRequestHandlerHttp.setCommitmentDate " + e.getLocalizedMessage());
        }
    }

    /**
     * @param riskUnitDesc
     * @param affectedObjectDesc
     * @param coverageDesc
     * @return ConfiguratedCauses
     */
    public ConfiguratedCauses getCausesForCoverage(String riskUnitDesc, String affectedObjectDesc, String coverageDesc) {
//        log.debug("affectedObjectDesc = " + affectedObjectDesc);
        Collection<ClaimRiskUnit> claimRiskUnits = claim.getClaimRiskUnitsList();
        ClaimRiskUnit claimRiskUnit = null;
        for (ClaimRiskUnit cru : claimRiskUnits) {
            if (cru.getAgregatedRiskUnit().getPk().equals(riskUnitDesc)) {
                claimRiskUnit = cru;
                break;
            }
        }
        if (claimRiskUnit == null) {
            return null;
        }
        ClaimInsuranceObject claimInsuranceObject = claimRiskUnit.getClaimInsuranceObjects().get(affectedObjectDesc);
        ConfigurableObjectType claimType = claimInsuranceObject.getDamage().getCOT();
        EventClaim eventClaim = claim.getEventClaim();
        ClaimType realClaimType = eventClaim.getClaimTypeByPK(claimType.getId());
        Collection<EventClaimCoverage> eventClaimCoverages = realClaimType.getEventClaimCoverages().values();
        EventClaimCoverage eventClaimCoverage = null;
        for (EventClaimCoverage ecc : eventClaimCoverages) {
            if (ecc.getCoverageTitle().getDesc().equalsIgnoreCase(coverageDesc)) {
                eventClaimCoverage = ecc;
                break;
            }
        }
        if (eventClaimCoverage == null) {
            throw new ApplicationException("This " + coverageDesc + " Doesn't have EventClaimCoverage", Severity.ERROR);
        }
        claimExclusions = new ClaimExclusions(eventClaimCoverage);
        return claimExclusions.getCausesForCoverage();
    }

    /**
     * Get the claim status
     *
     * @return Collection
     */
    public Collection getClaimStatus() {
        List list = null;
        String claimID = claim.getPK();
        try {
            Query query = HibernateUtil.getQuery("claimstate.loadClaimStates");
            query.setParameter(0, claimID);
            list = query.list();
        } catch (HibernateException e) {

        }
        return list;
    }

    /**
     * Returns the coverage configuration
     *
     * @param riskUnitId          The riskUnitId
     * @param insuranceObjectId   the insuranceObjectId
     * @param evaluatedCoverageId
     * @return the coverage configuration
     */
    public ClaimsCoverageConfiguration getClaimCoverageConfiguration(String riskUnitId, String insuranceObjectId, String evaluatedCoverageId) throws Exception {
        EvaluatedCoverage ec = searchEvaluatedCoverage(riskUnitId, insuranceObjectId, evaluatedCoverageId);
        return getClaimCoverageConfiguration(riskUnitId, insuranceObjectId, ec);
    }

    public ClaimsCoverageConfiguration getClaimCoverageConfiguration(String riskUnitId, String insuranceObjectId, EvaluatedCoverage ec) {
        return getClaimCoverageConfiguration(riskUnitId, insuranceObjectId, ec, policy);
    }

    public ClaimsCoverageConfiguration getClaimCoverageConfiguration(String riskUnitId, String insuranceObjectId, EvaluatedCoverage ec, AgregatedPolicy agregatedPolicy) {
        this.policy = agregatedPolicy;
        long coveragePk = ec.getConfiguratedCoverageOA().getId();
        long productPk = policy.getProduct().getId();
        return ClaimsCoverageConfiguration.load(productPk, coveragePk);
    }

    /**
     * The start date of the automatic payments
     *
     * @param ccconf
     * @return the start date
     */
    public Date getConfiguredStartDate(ClaimsCoverageConfiguration ccconf, EvaluatedCoverage evaluatedCoverage) {
        Date ocurrenceDate = getOcurrenceDate();
        log.debug("...getConfiguredStartDate ocurrenceDate = " + ocurrenceDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ocurrenceDate);

        //Get number of payments (benefitDuration)
        int benefitDurationInteger = ClaimValidator.getBenefitDurationPayments(ccconf, evaluatedCoverage, this);

        int CalcType = ccconf.getPaymentDateCalculation();
        boolean eliminationOnBenefit = ccconf.getEliminationOnBenefit();
        if (CalcType == ClaimsCoverageConfiguration.FP_NORMAL) {
            //  fecha = fecha_ocurrencia
            log.debug("...Calendar FP_NORMAL  = " + calendar.getTime());
        } else if (CalcType == ClaimsCoverageConfiguration.FP_ELIMINATION) {
            //  fecha = fecha_ocurrencia + waiting_period + benefit_period
            calendar.add(Calendar.DAY_OF_YEAR, ccconf.getWaitingPeriod());
            int benefitPeriod = ccconf.getBenefitPeriod();
            switch (benefitPeriod) {

                case ClaimsCoverageConfiguration.BD_MONTHS:
                    if (!eliminationOnBenefit) {
                        calendar.add(Calendar.MONTH, benefitDurationInteger);
                    }
                    break;
                case ClaimsCoverageConfiguration.BD_DAYS:
                    if (!eliminationOnBenefit) {
                        calendar.add(Calendar.DAY_OF_YEAR, benefitDurationInteger);
                    }
                    break;
                case ClaimsCoverageConfiguration.BD_SEMIMONTHLY:
                    calendar.add(Calendar.DAY_OF_YEAR, 15);
                    break;
                case ClaimsCoverageConfiguration.BD_WEEKLY:
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case ClaimsCoverageConfiguration.BD_BIWEEKLY:
                    calendar.add(Calendar.WEEK_OF_YEAR, 2);
                    break;

                default:
                    log.error("******* Error El valor de la costante benefitPeriod no esta en el range permitido ");
                    calendar.add(Calendar.DAY_OF_YEAR, benefitDurationInteger);
            }

            log.debug("...Calendar FP_ELIMINATION  = " + calendar.getTime());
        } else if (CalcType == ClaimsCoverageConfiguration.FP_RETROACTIVE) {
            //  fecha = fecha_ocurrencia + waiting_period
            calendar.add(Calendar.DAY_OF_YEAR, ccconf.getWaitingPeriod());
            log.debug("...Calendar FP_RETROACTIVE  = " + calendar.getTime());
        } else if (CalcType == ClaimsCoverageConfiguration.FP_FIRSTDAY) {
            //  fecha = firstDayOfMonth(fecha_ocurrencia)
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            log.debug("...Calendar First Day  = " + calendar.getTime());

        }
        log.debug(" (RAP)Calendar DATE = " + calendar.getTime());
        return calendar.getTime();

    }

    /**
     * The start date of the automatic payments
     *
     * @param riskUnitId
     * @param insuranceObjectId
     * @param evaluatedCoverageId
     * @return the start date
     */
    public Date getConfiguredStartDate(String riskUnitId, String insuranceObjectId, String evaluatedCoverageId) throws Exception {
        ClaimsCoverageConfiguration conf = getClaimCoverageConfiguration(riskUnitId, insuranceObjectId, evaluatedCoverageId);
        EvaluatedCoverage ec = this.searchEvaluatedCoverage(riskUnitId, insuranceObjectId, evaluatedCoverageId);
        return getConfiguredStartDate(conf, ec);
    }

    private Date getConfiguredStartDate(String riskUnitId, String insuranceObjectId, EvaluatedCoverage evaluatedCoverage) {
        ClaimsCoverageConfiguration conf = getClaimCoverageConfiguration(riskUnitId, insuranceObjectId, evaluatedCoverage);
        return getConfiguredStartDate(conf, evaluatedCoverage);
    }

    public boolean isStartTx() {
        return startTx;
    }

    public void setStartTx(boolean startTx) {
        this.startTx = startTx;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
        this.claimId = Long.parseLong(claim.getPk());
    }


    public double evaluate(String expresion, TablaSimbolos symbolsTable, EvaluatedCoverage ec) {
        try {

            if (symbolsTable == null || symbolsTable.size() == 0 || ec != null) {
                symbolsTable.addTableSimbol(publishSymbolsTable(ec));
            }
            log.debug(
                    "[*10576*] -YJA- ClaimComposerWrapper.evaluate symbolsTable.toString() " +
                            symbolsTable.toString());
            GNUEvaluator parser = GNUEvaluator.getInstance();
            return parser.evaluate(expresion, symbolsTable);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
//            log.debug("[*10437*] Error evaluating symbols ");

            return -1;
        }
    }

    private TablaSimbolos publishSymbolsTable(EvaluatedCoverage ec) {
        try {
            return publishSymbolsTable(this.getClaim(), ec);
        } catch (Exception e) {
//            log.debug("[*10437*] Error publish symbols ");

            return null;
        }
    }

    private TablaSimbolos publishSymbolsTable(Claim claim, EvaluatedCoverage ec) {
        TablaSimbolos symbolsTable = new TablaSimbolos();
//        log.debug(" ClaimComposerWrapper.publishSymbolsTable: publish symbols ");
        try {
            this.publishSymbols(new SymbolsClaim(), symbolsTable, this.getAgregatedPolicy(), claim, ec);
//                                this.getClaim(), ec);
            return symbolsTable;
        } catch (Exception e) {
//            log.debug("[*10437*] Error publish symbols ");

            return null;
        }
    }

    /**
     * Claim Historical
     *
     * @param claim
     * @param typeOperation
     */
    public static void addClaimHistorical(Claim claim, long typeOperation) {
        Timestamp timeStamp = new Timestamp(new Date().getTime());
        ClaimHistorical claimHistorical = new ClaimHistorical();
        claimHistorical.setClaim(claim);
        claimHistorical.setOcurrenceDate(timeStamp);
        claimHistorical.setTypeOperation((int) typeOperation);
        claimHistorical.setUserInfo(UserInfo.getGlobalUser());
        claimHistorical.save();
//        ClaimHistoricalManager.getInstance().save(claimHistorical);
    }

    public static void addClaimHistorical(Claim claim, ClaimHistoricalOperationType typeOperation) {
        Timestamp timeStamp = new Timestamp(new Date().getTime());
        ClaimHistorical claimHistorical = new ClaimHistorical();
        claimHistorical.setClaim(claim);
        claimHistorical.setOcurrenceDate(timeStamp);
        claimHistorical.setClaimHistoricalOperationType(typeOperation);
        claimHistorical.setUserInfo(UserInfo.getGlobalUser());
        if (typeOperation.getValue() == ClaimHistoricalOperationType.CREATE_CLAIM.getValue()) {
            claimHistorical.setLegacyType(OIMTPClaimEnum.ADMINISTRATIVA.getLabel());
        }
        claimHistorical.save();
    }


    public ClientResponse applyPayments(ClaimTotalizeOneByOne claimTotalize, boolean openTx, String ruID,
                                        String ioID) throws ApplicationExceptionChecked, RemoteException {
        return applyPayments(claimTotalize, null, openTx, ruID, ioID, null);
    }


    public ClientResponse applyPayments(ClaimTotalizeOneByOne claimTotalize, boolean openTx, String ruID, String ioID, Long currencyPayment, String amountPayment, Date paymentDate) throws ApplicationExceptionChecked, RemoteException {

        return applyPayments(claimTotalize, null, openTx, ruID, ioID, currencyPayment, amountPayment, paymentDate, null);
    }

    public ClientResponse applyPayments(ClaimTotalize claimTotalize, boolean openTx, String ruID,
                                        String ioID) throws ApplicationExceptionChecked, RemoteException {
        return applyPayments(claimTotalize, null, openTx, ruID, ioID, null);
    }

    /**
     * This method verifies if the given payment order can be approved by the current user.
     *
     * @param paymentOrder Payment order to approve.
     * @return True if the user can approve the order.
     * False, otherwise.
     */
    public boolean verifyApprovement(PaymentOrder paymentOrder) {

        double amountToApprove = paymentOrder.getAmountWithDeductible();
        User u = UserInfo.loadUser();
        //Este hashTable tiene el pk del grupo como key y la formula asociada a ese grupo.
        List<GlobalGroup> groupsPkAndRights = UserInfo.groupsCanDo(Rights.CAN_PAYMENT_POLICYTOCLAIM);

        String policyId = claim.getPolicyId();
        String productPk = AgregatedPolicy.findProducIdkByPolicyId(policyId);
        Product product = Productos.getInstance().getByPK(productPk);
        String productName = product.getDesc();
        String productCode;
        try {
            //Busco en bd el valor asociado a la propiedad que define el c�digo
            // del producto.
            productCode = (String) product.getDCO().getInput().get(AcseleConf.getProperty("policy.product"));
        } catch (Exception ex) {
            productCode = "0";
        }
        ExpresionEvaluator evaluator = doPublishClaimSymbols(productCode, productName, paymentOrder, u, String.valueOf(claimId));

        for (GlobalGroup globalGroup : groupsPkAndRights) {
            GlobalPolicyToClaim policyToClaim = globalGroup.getPolicyToClaim();
            if (policyToClaim != null) {
                String formula = policyToClaim.getFormula();
                evaluator.addSymbol(EvaluationConstants.GROUP, globalGroup.getName(), new Double(globalGroup.getId()), false);
                double amount = evaluator.evaluate(formula);
                if (amount >= amountToApprove) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Publishes the symbols needed for claim approval process
     *
     * @param productCode
     * @param productName
     * @param paymentOrder
     * @param u
     * @return
     */
    public ExpresionEvaluator doPublishClaimSymbols(String productCode, String productName, PaymentOrder paymentOrder, User u, String claimId) {
        ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
        fillSymbolTableClaim(claimId, evaluator.getTablaSimbolos());
        evaluator.addSymbol(EvaluationConstants.PRODUCT_CODE, productCode, new Double(productCode), false);
        evaluator.addSymbol(EvaluationConstants.PRODUCT_NAME, productName, new Double(0), false);
        int type = paymentOrder.getClaimReserve() instanceof ClaimNormalReserve ? ReserveType.NORMAL_RESERVE.getValue() : ReserveType.CONCEPT_RESERVE.getValue();
        evaluator.addSymbol(EvaluationConstants.PAYMENT_ORDER_TYPE, String.valueOf(type), new Double(type), false);

        evaluator.addSymbol(EvaluationConstants.USER, u.getLogin(), new Double(u.getPk()), false);

        String date = DateUtil.getDateToShow(new java.util.Date());
        evaluator.addSymbol(EvaluationConstants.SYSTEM_DATE, date, new Double(Funciones.dateTransformer.toNumber(date)), false);

        if ((UserInfo.getUserObject() != null) && (!StringUtil.isEmptyOrNullValue(UserInfo.getUserObject().getUserType()))) {
            evaluator.addSymbol(EvaluationConstants.USER_TYPE, UserInfo.getUserObject().getUserType(), 0.0, false);
        }

        try {
            Currency currency = paymentOrder.getClaimReserve().getCurrency();
            evaluator.addSymbol(EvaluationConstants.CURRENCY_CODE, currency.getIsoCode(),
                    Double.valueOf(currency.getId()), false);
        } catch (Exception e) {
            log.warn("Error publishing reserve's currency for reserve '" + paymentOrder.getClaimReserve().getPk() + "' of type '" + type + "'.");
        }
        return evaluator;
    }

    public OperationPK getOperationPK() {
        if (operationPK == null) {
            AggregateObject policy = claim.getAgregatedPolicy();
            operationPK = policy.getObjectManager().getOperationPkByOccurrenceDateOfClaim(policy.getPk(), getOcurrenceDate());
        }
        return operationPK;
    }

    public void setOperationPK(OperationPK operationPK) {
        this.operationPK = operationPK;
    }

    /**
     * This method returns the product type of the claim
     *
     * @return Integer
     */
    public Integer getProductType() {
        Integer result = null;
        ProductBehaviour productBehaviour = null;

        if (claim != null) {
            AgregatedPolicy policy = claim.getAgregatedPolicy();
            Product product = policy.getProduct();
            productBehaviour = product.getProductBehaviour();
        }

        if (productBehaviour != null) {
            result = productBehaviour.getProductType();
        }

        return result;
    }

    public double verifyUnpaidLoans(String idCoverage, double unpaid_loans, double amountBenefitTotal, ProductBehaviour productBehaviour,
                                    double mathReserve) {
        double reserveMovemenTipe4;
        if ((productBehaviour.getProductType() == ProductBehaviour.LIFE_PRODUCT) && (mathReserve > 0)) {
            ReserveMovement ReserveMovement = new ReserveMovement();
            double sum_tipe2Y3 = ReserveMovement.getLoanAmount(Long.valueOf(idCoverage));
            reserveMovemenTipe4 = ReserveMovement.getAmountByType(Long.valueOf(idCoverage), ReserveOperationType.LOAN_PAYMENT);
            //double sum_tipe2Y3=ReserveMovement.getLoanAmount(1500710);
            log.debug("sum_tipe2Y3: " + sum_tipe2Y3);
            //reserveMovemenTipe4 = ReserveMovement.getAmountByType(1500710,ReserveOperationType.LOAN_PAYMENT);
            log.debug("reserveMovemenTipe4: " + reserveMovemenTipe4);
            unpaid_loans = sum_tipe2Y3 - reserveMovemenTipe4;
        }

        return unpaid_loans;
    }

    public String verifyDatesPaymentOrder(String idReserve, Date startDate, Date endDate, long thirdPartyId, boolean allowPaymentsInOneDay) {
        String message, message1, message2 = "";
        String paymentOrderData = "";
        boolean exist = false;

        try {
            ResourceBundle rb = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
            message = rb.getString("validateDatePayment");
            message1 = rb.getString("paymentOrderLabel");
            message2 = rb.getString("periodLabel");

            List listPayments = PaymentOrder.getPaymentOrderByClaimNormalReserveId(idReserve);

            Iterator it = listPayments.iterator();
            while (it.hasNext()) {
                PaymentOrder paymentOrderClaim = (PaymentOrder) it.next();
                if (!allowPaymentsInOneDay) {
                    if (((startDate.after(paymentOrderClaim.getStartDate()) && startDate.before(paymentOrderClaim.getEndDate())) ||
                            (endDate.after(paymentOrderClaim.getStartDate()) && endDate.before(paymentOrderClaim.getEndDate())) ||
                            (startDate.after(paymentOrderClaim.getStartDate()) && endDate.before(paymentOrderClaim.getEndDate())) ||
                            (startDate.equals(paymentOrderClaim.getStartDate())) ||
                            startDate.equals(paymentOrderClaim.getEndDate()) || endDate.equals(paymentOrderClaim.getStartDate())) && (thirdPartyId == paymentOrderClaim.getThirdPartyId())) {
                        exist = true;
                        paymentOrderData = message1 + paymentOrderClaim.getPK() + ", " + message2 + DateUtil.getDateToShow(paymentOrderClaim.getStartDate()) + " - " + DateUtil.getDateToShow(paymentOrderClaim.getEndDate()) + ". ";
                        message = message + paymentOrderData;
                    }
                }
            }

            if (exist) {
                return message;
            } else {
                return null;
            }
        } catch (Exception e) {

            return null;
        }
    }

    public String verifyDatesPaymentOrder(String idReserve, Date startDate, Date endDate, boolean allowPaymentsInOneDay) {
        String message, message1, message2 = "";
        String paymentOrderData = "";
        boolean exist = false;

        try {
            ResourceBundle rb = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
            message = rb.getString("validateDatePayment");
            message1 = rb.getString("paymentOrderLabel");
            message2 = rb.getString("periodLabel");

            List listPayments = PaymentOrder.getPaymentOrderByClaimNormalReserveId(idReserve);

            Iterator it = listPayments.iterator();
            while (it.hasNext()) {
                PaymentOrder paymentOrderClaim = (PaymentOrder) it.next();
                if (!allowPaymentsInOneDay) {
                    if ((startDate.after(paymentOrderClaim.getStartDate()) && startDate.before(paymentOrderClaim.getEndDate())) ||
                            (endDate.after(paymentOrderClaim.getStartDate()) && endDate.before(paymentOrderClaim.getEndDate())) ||
                            (startDate.after(paymentOrderClaim.getStartDate()) && endDate.before(paymentOrderClaim.getEndDate())) ||
                            (startDate.before(paymentOrderClaim.getStartDate()) && endDate.after(paymentOrderClaim.getEndDate())) ||
                            (startDate.equals(paymentOrderClaim.getStartDate()) || endDate.equals(paymentOrderClaim.getEndDate())) ||
                            startDate.equals(paymentOrderClaim.getEndDate()) || endDate.equals(paymentOrderClaim.getStartDate())) {
                        exist = true;
                        paymentOrderData = message1 + paymentOrderClaim.getPK() + ", " + message2 + DateUtil.getDateToShow(paymentOrderClaim.getStartDate()) + " - " + DateUtil.getDateToShow(paymentOrderClaim.getEndDate()) + ". ";
                        message = message + paymentOrderData;
                    }
                }
            }

            if (exist) {
                return message;
            } else {
                return null;
            }
        } catch (Exception e) {

            return null;
        }
    }

    public double deducedRelatedLifeCoverage(ClaimNormalReserve reserve, double amount, ClientResponse response) {
        double deducedByCovRelLife = 0;
        String message = null;
        try {
            if (reserve.getEvaluatedCoverage().getConfiguratedCoverage().getCoverageTitle() != null) {
                ClaimCovRelationshipPersister loader = new ClaimCovRelationshipHibPersister();
                List<ClaimCovRelationship> ccrsList = loader.getByCovTitleProdIdAndType(
                        reserve.getEvaluatedCoverage().getConfiguratedCoverage().getCoverageTitle().getId(),
                        reserve.getEvaluatedCoverage().getAgregatedPolicy().getProduct().getId(),
                        ClaimCovRelationshipPersister.PAY_DIFF_TYPE);
                for (ClaimCovRelationship ccrs : ccrsList) {
                    List<ClaimNormalReserve> cnrList = this.getClaim().getNormalReserves();
                    for (ClaimNormalReserve cnr : cnrList) {
                        EvaluatedCoverage ec = cnr.getEvaluatedCoverage();
                        if (ccrs.getClaimCovRelationshipPk().getCoverageID() == ec.getConfiguratedCoverageOA().getId()) {
                            Map<Long, PaymentOrder> paymentOrdList = cnr.getPaymentOrderList();
                            double rate = 1;
                            if (paymentOrdList != null) {
                                for (PaymentOrder paymentOrder : paymentOrdList.values()) {
                                    if (paymentOrder.getState() == PaymentOrderStatus.PAID_STATE.getValue()) {
                                        if (reserve.getCurrencyId() != cnr.getCurrencyId()) {
                                            CurrencyRate currencyRate = new CurrencyRate(cnr.getCurrency(), reserve.getCurrency(), reserve.getDate());
                                            currencyRate.load();
                                            rate = currencyRate.getRate();
                                            deducedByCovRelLife += rate * paymentOrder.getAmount();
                                            log.debug("Rate for amount: " + rate + ", amount: " + paymentOrder.getAmount());
                                        } else {
                                            deducedByCovRelLife += paymentOrder.getAmount();
                                        }
                                        log.debug("Deduced by related coverage in life. Coverage related: " + cnr.getDesc() + ". Amount to deduced: " + paymentOrder.getAmount());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionUtil.handleException("Error trying to get deduced by related life coverage", e);
        }
        if (response != null && deducedByCovRelLife >= amount) {
            message = "error.by.coverage.deduction";
            response.setMessage(message);

        }
        return deducedByCovRelLife;
    }

    public static Vector findClaims(String idPolicy) {

        Statement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        Vector info = new Vector();

        StringBuffer query = new StringBuffer(1000);
        String nroClaim;
        try {
            conn = JDBCUtil.getConnection();
            stmt = conn.createStatement();


            PolicyType pt = new PolicyType("");
            String prepolicyNameTable = pt.getPredefinedCOT().getTableName();

            query.append("SELECT c.claimnumber ");
            /* query.append("SELECT c.claimID, ap.agregatedpolicyid, pr.description ");
         query.append("productdescription, c.claimNumber, ppcy.").append(AcseleConf.getProperty("policyIdentification")).append("Input ");*/
            query.append("FROM POLICYDCO PDCO, agregatedpolicy AP, product PR, CLAIM C , ")
                    .append(prepolicyNameTable).append(" PPCY, ContextOperation ctx ");
            query.append("WHERE AP.agregatedpolicyid = PDCO.agregatedObjectId ");
            query.append("AND AP.productid = PR.productid ");
            query.append("AND AP.OPERATIONPK = PDCO.OPERATIONPK ");
            query.append("AND C.POLICYID = PDCO.agregatedObjectId ");
            query.append("AND PPCY.PK = PDCO.DCOID AND PDCO.operationpk = ctx.id ");
            query.append("AND AP.agregatedpolicyid= ").append(idPolicy);

            query.append(" ORDER BY c.claimid");
            log.debug(">>>>>>>>>>>>>query=" + query);
            rs = stmt.executeQuery(query.toString());

            while (rs.next()) {
                nroClaim = rs.getString(1);   // changed by 4 from 1 previously.

                info.addElement(nroClaim);
            }
        } catch (SQLException SQLEx) {
            log.error("Error.  Query = " + query, SQLEx);
            throw new TechnicalException("Exception in FindAllClaimsHandlerHttp.findClaims ",
                    Severity.ERROR, SQLEx);
        } finally {
            JDBCUtil.closeConnection(rs, stmt, conn,
                    "Exception in FindAllClaimsHandlerHttp.findClaims closing the DB");
        }
        return info;
    }

    public String getValidations(long claimId, ClientRequest request, List<Integer> validationTypes) {
        List<ValidationDamageClaim> validationDamageClaims = null;

        com.consisint.acsele.claim.api.Claim claim = com.consisint.acsele.claim.api.Claim.Impl.getInstance(claimId);

        long eventClaimId = claim.getClaimEvent().getId();

        validationDamageClaims = ValidationDamageClaimPersister.Impl.getInstance().listValidationsByTypes(eventClaimId, validationTypes);

        if (validationDamageClaims == null) {
            validationDamageClaims = new ArrayList<ValidationDamageClaim>();
        }
        int count = 0;
        String validationType = null;
        GNUEvaluator traductor = GNUEvaluator.getInstance();
        ExpresionEvaluator evaluator = this.fillSymbolClaim(String.valueOf(claim.getId()), new TablaSimbolos());

        String claimRiskUnitId = (String) request.getAttribute("aruId");
        if (StringUtil.isEmptyOrNullValue(claimRiskUnitId)) {
            claimRiskUnitId = (String) request.getAttribute("ruID");
        }
        if (!StringUtil.isEmptyOrNullValue(claimRiskUnitId)) {
            ClaimRiskUnit claimRiskUnit = null;
            Collection<ClaimRiskUnit> claimRiskUnits = this.claim.getClaimRiskUnitsList();
            for (ClaimRiskUnit cru : claimRiskUnits) {
                String pk = cru.getAgregatedRiskUnit().getPk();
                if (pk.equals(claimRiskUnitId)) {
                    claimRiskUnit = cru;
                }
            }

            if (claimRiskUnit != null && claimRiskUnit.getAgregatedRiskUnit() != null) {
                evaluator.addSymbols(claimRiskUnit.getAgregatedRiskUnit().getDCO(), EvaluationConstants.RU_PREFIX);

                if (claimRiskUnit.getAgregatedRiskUnit().getInitialDate() != null) {
                    String inputInitialDate = DateUtil.getDateToShow(claimRiskUnit.getAgregatedRiskUnit().getInitialDate());
                    evaluator.addSymbol(EvaluationConstants.RISK_UNIT_INITIAL_DATE, inputInitialDate, Funciones.dateTransformer.toNumber(inputInitialDate), false);
                }
                if (claimRiskUnit.getAgregatedRiskUnit().getFinishDate() != null) {
                    String inputFinishDate = DateUtil.getDateToShow(claimRiskUnit.getAgregatedRiskUnit().getFinishDate());
                    evaluator.addSymbol(EvaluationConstants.RISK_UNIT_END_DATE, inputFinishDate, Funciones.dateTransformer.toNumber(inputFinishDate), false);
                }

                String ioId = (String) request.getAttribute("ioID");
                if (!StringUtil.isEmptyOrNullValue(ioId)) {
                    AgregatedInsuranceObject agregatedInsuranceObject = getAgregatedInsuranceObject(claimRiskUnit.getAgregatedRiskUnit(), ioId);
                    if (agregatedInsuranceObject != null) {
                        evaluator.addSymbols(agregatedInsuranceObject.getDCO(), EvaluationConstants.IO_PREFIX);
                        if (agregatedInsuranceObject.getInitialDate() != null) {
                            String inputInitialDate = DateUtil.getDateToShow(agregatedInsuranceObject.getInitialDate());
                            evaluator.addSymbol(EvaluationConstants.IO_INITIAL_DATE, inputInitialDate, Funciones.dateTransformer.toNumber(inputInitialDate), false);
                        }
                        if (agregatedInsuranceObject.getFinishDate() != null) {
                            String inputFinishDate = DateUtil.getDateToShow(agregatedInsuranceObject.getFinishDate());
                            evaluator.addSymbol(EvaluationConstants.IO_END_DATE, inputFinishDate, Funciones.dateTransformer.toNumber(inputFinishDate), false);
                        }
                    }
                }

            }

        }

        for (ValidationDamageClaim listDmgClaim : validationDamageClaims) {

            boolean condition = traductor.evaluateLogical(listDmgClaim.getCondition(), evaluator.getTablaSimbolos());
            listDmgClaim.setValidity(condition);
            if (!condition) {
                validationType = ProductLanguageHandler.getValidationTypeClaimString(ProductLanguageHandler.EVENT_ERROR_VALIDATION_TYPE);
                request.putAttribute("ClaimDamageValidations", validationDamageClaims);
                break;
            } else {
                validationType = ProductLanguageHandler.getValidationTypeClaimString(ProductLanguageHandler.EVENT_WARNING_VALIDATION_TYPE);
            }

        }

        return validationType;
    }


    /**
     * Generate Scheduled Multiple Payments
     *
     * @param ruId
     * @param ioId
     * @param thirdPartyId
     * @param periodicity
     * @param amount
     * @param numberOfPayments
     * @param benefitDuration
     * @throws Exception
     */
    public PaymentOrder generateScheduledPayment(String ruId, String ioId, String thirdPartyId, int periodicity, int benefitDuration, Double amount,
                                                 double deducedAmount, int numberOfPayments, ClaimReserveBenefit benefit, Collection selectedRiskUnits) throws Exception {
        ClaimNormalReserve normalReserve = benefit.getClaimNormalReserve();
        CoverageReserve coverageReserve = new CoverageReserve(normalReserve, this);
        Double reserveAmount = coverageReserve.getAmount();
        Double participationP = (amount * 100) / reserveAmount;
        Session session = null;
        Transaction transaction = null;
        int typeScheduled = 2;
        PaymentOrder paymentOrder = null;
        String message = null;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }

//            int paymentOrderPk;
            Collection thirdParties;
            SearchThirdPartyResult thirdParty;
            String thirdPartyName = null;
            int thirdPartyRoleId = 0;
            Iterator thirdPartiesIterator;
            Date commitmentDate, currentDate, today;
            Calendar calendar;
            int days = 15;
            //Double paymentAmount = new Double(amount.doubleValue() / numberOfPayments);
            Double paymentAmount = amount; //Verificar contra la validacion javascript que hace en claimBenefit.jsp en la funcion loadBenefitPayments(...)
            thirdParties = getBeneficiariesForPayments(normalReserve.getEvaluatedCoverage());
            thirdParties.addAll(this.getThirdPartiesFromInsuranceObject(ruId, ioId));

            Collection manualBeneficiaries = this.getManualBeneficiaries();
            if (manualBeneficiaries != null && !manualBeneficiaries.isEmpty()) {
                thirdParties.addAll(manualBeneficiaries);
            }

            thirdPartiesIterator = thirdParties.iterator();
            while (thirdPartiesIterator.hasNext()) {
                thirdParty = (SearchThirdPartyResult) thirdPartiesIterator.next();
                if (thirdParty.getThirdPartyId().trim().equals(thirdPartyId.trim())) {
                    thirdPartyName = thirdParty.getThidPartyName();
                    thirdPartyRoleId = thirdParty.getRoleID();
                }
            }
            ClientResponse response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                    normalReserve.getPk(), true);
            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();
            checkClaimConfiguration(amount, numberOfPayments, paymentOrderCollection, ec, typeScheduled, selectedRiskUnits);
            log.debug("********[Acsele v4.5] ClaimComposerWrapper.generateScheduledPayments(){" + "6860}thirdPartyName = " + thirdPartyName);
            ClaimsCoverageConfiguration configuration = ClaimsCoverageConfiguration
                    .load(Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()),
                            ec.getConfiguratedCoverageOA().getId());
            today = new Date();
            //  obtiene fecha Inicial del campo ocurrenceDate del claim y/o aplicando propiedades de la cobertura
            currentDate = getConfiguredStartDate(ruId, ioId, normalReserve.getEvaluatedCoverage());
            calendar = Calendar.getInstance(UserInfo.getLocale());
            calendar.setTime(currentDate);
            if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY && (calendar.get(Calendar.DAY_OF_MONTH) != 1
                    && calendar.get(Calendar.DAY_OF_MONTH) != days)) {
                if (calendar.get(Calendar.DAY_OF_MONTH) > days) {
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, days);
                }
                currentDate = calendar.getTime();
            }
            Calendar calendarEndDate = Calendar.getInstance(UserInfo.getLocale());
            for (int i = 0; i < numberOfPayments; i++) {
                calendar.setTime(currentDate);
                if (periodicity == ClaimNormalReserve.PERIODICITY_DAYS) {
                    calendar.add(Calendar.DAY_OF_MONTH, benefitDuration);
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY) {
                    if (calendar.get(Calendar.DAY_OF_MONTH) + 1 > days) {
                        calendar.add(Calendar.MONTH, 1);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                    } else {
                        calendar.set(Calendar.DAY_OF_MONTH, days);
                    }
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_WEEKLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 7);
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_BIWEEKLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 14);
                } else {    // ClaimNormalReserve.PERIODICITY_MONTHS or PERIODICITY_NONE
                    calendar.add(Calendar.MONTH, benefitDuration);
                }
                if (numberOfPayments == 1) {
                    calendarEndDate.setTime(currentDate);
                } else {
                    calendarEndDate.setTime(calendar.getTime());
                    calendarEndDate.add(Calendar.DATE, -1);
                }
                commitmentDate = currentDate;
                if (configuration != null && configuration.getFistPaymentDate() != null && !configuration.getFistPaymentDate().equals("") && i == 0) {
                    TablaSimbolos symbolsTable = new TablaSimbolos();
                    Double date = this.evaluate(configuration.getFistPaymentDate(), symbolsTable, ec);
                    Date calcCommitmentDate = Funciones.transformDate(date);
                    log.debug("Commitment date: " + calcCommitmentDate);
                    commitmentDate = calcCommitmentDate;

                }
                paymentOrder = new PaymentOrder(benefit);
                paymentOrder.setDate(today);
                //paymentOrder.setDate(this.getClaim().getOcurrenceDate());
                paymentOrder.setStartDate(currentDate);
                paymentOrder.setCommitmentDate(commitmentDate);
                paymentOrder.setEndDate(calendarEndDate.getTime());
                currentDate = calendar.getTime();
                boolean existBenefit = false;
                if (benefit != null) {
                    com.consisint.acsele.thirdparty.api.ThirdParty thirdPartyBenefit = benefit.getThirdParty();
                    if (thirdPartyBenefit != null) {
                        //  message = verifyDatesPaymentOrder(normalReserve.getPk(), paymentOrder.getStartDate(), paymentOrder.getEndDate(),thirdPartyBenefit.getThirdPartyID());
                        existBenefit = true;
                    }
                }
                if (!existBenefit)
                    message = verifyDatesPaymentOrder(normalReserve.getPk(), paymentOrder.getStartDate(), paymentOrder.getEndDate());

                if (message == null) {
                    paymentOrder.setAmount(paymentAmount);
                    paymentOrder.setBeneficiary(thirdPartyName);
                    paymentOrder.setDistributionAmount(0);
                    paymentOrder.setDoneBy(UserInfo.getUser());
                    paymentOrder.setParticipationPercentage(participationP);
                    paymentOrder.setPenaltyPercentage(0);
                    paymentOrder.setReason("");
                    paymentOrder.setRefundPercentage(0);
                    paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
                    paymentOrder.setThirdPartyId(Long.parseLong(thirdPartyId));
                    paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
                    paymentOrder.setType(PaymentOrderType.PARTIAL.getValue());
                    paymentOrder.setOnHold(0);
                    paymentOrder.setCoverageDesc(ec.getDesc());
                    paymentOrder.setThirdBeneficiaryId(Long.parseLong(thirdPartyId));
                    paymentOrder.save();
                    normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);

                    if (deducedAmount > 0) {
                        PaymentOrderDetail paymentOrderDetail = new PaymentOrderDetail();
                        paymentOrderDetail.setPk(paymentOrder.getPk());
                        paymentOrderDetail.setDeducedAmount(deducedAmount);
                        paymentOrderDetail.save();
                    }
                } else {
                    throw new ApplicationException(Exceptions.CCErrorSamePeriodPaid, Severity.ERROR);
                }
            }
            if (message == null) {
                if (startTx) {
                    HibernateUtil.commit(transaction, session);
                }
            }
        } catch (Exception e) {
            session.clear();
           /* if (startTx) {
                HibernateUtil.rollBack(transaction);
            } */
            log.debug("Error generating the payment orders.", e);
            ExceptionUtil.handlerException(e);
//        } finally {
            //HibernateUtil.closeSession(session);
        }
        return paymentOrder;
    }


    public String verifyDatesPaymentOrder(String idReserve, Date startDate, Date endDate, long thirdPartyId) {
        String message, message1, message2 = "";
        String paymentOrderData = "";
        boolean exist = false;

        try {
            ResourceBundle rb = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
            message = rb.getString("validateDatePayment");
            message1 = rb.getString("paymentOrderLabel");
            message2 = rb.getString("periodLabel");

            List listPayments = PaymentOrder.getPaymentOrderByClaimNormalReserveId(idReserve);

            Iterator it = listPayments.iterator();

            log.debug("Payment order creation date");
            while (it.hasNext()) {
                PaymentOrder paymentOrderClaim = (PaymentOrder) it.next();
                log.debug("payment order id " + paymentOrderClaim.getPK());
                log.debug("Start Date PO " + paymentOrderClaim.getStartDate().toString());
                log.debug("Start Date " + startDate.toString());
                log.debug("End Date PO " + paymentOrderClaim.getEndDate());
                log.debug("End Date " + endDate.toString());
                log.debug("ThirdPartyID " + paymentOrderClaim.getThirdPartyId());

                if (((startDate.after(paymentOrderClaim.getStartDate()) && startDate.before(paymentOrderClaim.getEndDate())) ||
                        (endDate.after(paymentOrderClaim.getStartDate()) && endDate.before(paymentOrderClaim.getEndDate())) ||
                        (startDate.after(paymentOrderClaim.getStartDate()) && endDate.before(paymentOrderClaim.getEndDate())) ||
                        (startDate.equals(paymentOrderClaim.getStartDate())) ||
                        startDate.equals(paymentOrderClaim.getEndDate()) || endDate.equals(paymentOrderClaim.getStartDate())) && (thirdPartyId == paymentOrderClaim.getThirdPartyId())) {
                    exist = true;
                    paymentOrderData = message1 + paymentOrderClaim.getPK() + ", " + message2 + DateUtil.sdf.format(paymentOrderClaim.getStartDate()) + " - " + DateUtil.sdf.format(paymentOrderClaim.getEndDate()) + ". ";
                    message = message + paymentOrderData;
                }
            }


            if (exist) {
                return message;
            } else {
                return null;
            }
        } catch (Exception e) {

            return null;
        }
    }

    public String verifyDatesPaymentOrder(String idReserve, Date startDate, Date endDate) {
        String message, message1, message2 = "";
        String paymentOrderData = "";
        boolean exist = false;

        try {
            ResourceBundle rb = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
            message = rb.getString("validateDatePayment");
            message1 = rb.getString("paymentOrderLabel");
            message2 = rb.getString("periodLabel");

            List listPayments = PaymentOrder.getPaymentOrderByClaimNormalReserveId(idReserve);

            Iterator it = listPayments.iterator();
            while (it.hasNext()) {
                PaymentOrder paymentOrderClaim = (PaymentOrder) it.next();
                if ((startDate.after(paymentOrderClaim.getStartDate()) && startDate.before(paymentOrderClaim.getEndDate())) ||
                        (endDate.after(paymentOrderClaim.getStartDate()) && endDate.before(paymentOrderClaim.getEndDate())) ||
                        (startDate.after(paymentOrderClaim.getStartDate()) && endDate.before(paymentOrderClaim.getEndDate())) ||
                        (startDate.before(paymentOrderClaim.getStartDate()) && endDate.after(paymentOrderClaim.getEndDate())) ||
                        (startDate.equals(paymentOrderClaim.getStartDate()) || endDate.equals(paymentOrderClaim.getEndDate())) ||
                        startDate.equals(paymentOrderClaim.getEndDate()) || endDate.equals(paymentOrderClaim.getStartDate())) {
                    exist = true;
                    paymentOrderData = message1 + paymentOrderClaim.getPK() + ", " + message2 + DateUtil.sdf.format(paymentOrderClaim.getStartDate()) + " - " + DateUtil.sdf.format(paymentOrderClaim.getEndDate()) + ". ";
                    message = message + paymentOrderData;
                }
            }

            if (exist) {
                return message;
            } else {
                return null;
            }
        } catch (Exception e) {

            return null;
        }
    }

    private class ComparatorDate implements Comparator<PaymentOrder> {
        public int compare(PaymentOrder o1, PaymentOrder o2) {
            return o1.getEndDate().compareTo(o2.getEndDate());
        }
    }

    public void evaluateDamageValidations(com.consisint.acsele.claim.api.Claim claim, List<ValidationDamageClaim> validationDamageClaims, long ioId) {
        GNUEvaluator traductor = GNUEvaluator.getInstance();
        try {
            ExpresionEvaluator evaluator = this.fillSymbolClaim(String.valueOf(claim.getId()), new TablaSimbolos());
            ClaimInsuranceObject cio = null;
            if (ioId != 0) {
                cio = ClaimInsuranceObject.getInstance(ioId);
                if (cio != null) {
                    addSymbols(evaluator, cio, getAgregatedPolicy());
                }
            }
            for (ValidationDamageClaim validation : validationDamageClaims) {
                TablaSimbolos symbolsTable = evaluator.getTablaSimbolos();
                boolean validity = traductor.evaluateLogical(validation.getCondition(), symbolsTable);
                validation.setValidity(validity);
                if (!validity) {
                    //Se genera Traza
                    Map<String, Long> agregatedObjects = getAggregatedObjects(claim, cio);
                    if (validation.isTrackeable()) {
                        Long claimId = null;

                        if (claim.getId() != 0) {
                            claimId = claim.getId();
                        }

                        Long operationId = null;
                        Policy pol = claim.getPolicy();
                        if (pol != null) {
                            operationId = pol.getOperationPK().getPk();
                        }

                        AuditTrailManager.generateValidationAuditTrail((CustomAuditItem) CustomAuditItemManager.getPersistance().getInstance(validation.getCustomAuditItemId()),
                                claim.getClaimNumber(), validation.getMessage(),
                                agregatedObjects.get("agregatedpolicy"), agregatedObjects.get("agregatedriskunitid"),
                                agregatedObjects.get("agregatedinsuranceobject"), null, null, null, claimId, operationId);
                    }
                    ValidationUtil.sendNotifications(validation.getNotificationId(), validation.getType(),
                            validation.getMessage(), symbolsTable);
                }
            }
        } catch (Exception ex) {
            log.error("Error in evaluateDamageValidations: " + ex.getMessage());
        }
    }

    public void addSymbols(ExpresionEvaluator evaluator, ClaimInsuranceObject cio, AgregatedPolicy agregatedPolicy) {
        try {
            Participation participation = cio.getAgregatedInsuranceObject().getInsuranceParticipation();
            DefaultConfigurableObject damage = cio.getDamage();
            evaluator.addSymbols(damage);
            if (participation != null) {
                ThirdParty thirdParty = participation.getThirdParty();
                String description = ConfigurableObjectTypeObjectManager.getDescription(thirdParty.getStatic().getCotID(), CotType.THIRDPARTY);
                thirdParty.getDynamic().evaluate(description, evaluator);
                evaluator.addSymbol(EvaluationConstants.THIRDPARTY_ID, String.valueOf(thirdParty.getId()), new Double(thirdParty.getId()), false);
                evaluator.addSymbol(EvaluationConstants.THIRDPARTY_NAME, thirdParty.getName(), new Double(0), false);
            }

            ClaimRiskUnit claimRiskUnit = cio.getContainer();

            if (claimRiskUnit != null && claimRiskUnit.getAgregatedRiskUnit() != null) {
                evaluator.addSymbols(claimRiskUnit.getAgregatedRiskUnit().getDCO(), EvaluationConstants.RU_PREFIX);

                if (claimRiskUnit.getAgregatedRiskUnit().getInitialDate() != null) {
                    String inputInitialDate = DateUtil.getDateToShow(claimRiskUnit.getAgregatedRiskUnit().getInitialDate());
                    evaluator.addSymbol(EvaluationConstants.RISK_UNIT_INITIAL_DATE, inputInitialDate, Funciones.dateTransformer.toNumber(inputInitialDate), false);
                }
                if (claimRiskUnit.getAgregatedRiskUnit().getFinishDate() != null) {
                    String inputFinishDate = DateUtil.getDateToShow(claimRiskUnit.getAgregatedRiskUnit().getFinishDate());
                    evaluator.addSymbol(EvaluationConstants.RISK_UNIT_END_DATE, inputFinishDate, Funciones.dateTransformer.toNumber(inputFinishDate), false);
                }
            }

            AgregatedInsuranceObject agregatedInsuranceObject = cio.getAgregatedInsuranceObject();
            if (agregatedInsuranceObject != null) {
                evaluator.addSymbols(agregatedInsuranceObject.getDCO(), EvaluationConstants.IO_PREFIX);
                if (agregatedInsuranceObject.getInitialDate() != null) {
                    String inputInitialDate = DateUtil.getDateToShow(agregatedInsuranceObject.getInitialDate());
                    evaluator.addSymbol(EvaluationConstants.IO_INITIAL_DATE, inputInitialDate, Funciones.dateTransformer.toNumber(inputInitialDate), false);
                }
                if (agregatedInsuranceObject.getFinishDate() != null) {
                    String inputFinishDate = DateUtil.getDateToShow(agregatedInsuranceObject.getFinishDate());
                    evaluator.addSymbol(EvaluationConstants.IO_END_DATE, inputFinishDate, Funciones.dateTransformer.toNumber(inputFinishDate), false);
                }
            }

            Collection participations = agregatedPolicy.getInsuredParticipations();

            policy.getInsuredThirdParty();
            Iterator participationsIt = participations.iterator();
            Participation part;
            ThirdParty tp;

            if (participationsIt.hasNext()) {
                part = (Participation) participationsIt.next();
                tp = part.getThirdParty();

                com.consisint.acsele.thirdparty.persistent.Role role = part.getRole();
                DefaultConfigurableObject roleDCO = part.getRole().getDynamic().getDCO();
                long thirdPartyID = tp.getPk();
                ThirdParty thirdParty = ThirdParty.getInstance(thirdPartyID);
                if (thirdParty != null) {
                    String description = ConfigurableObjectTypeObjectManager.getDescription(thirdParty.getStatic().getCotID(), CotType.THIRDPARTY);
                    thirdParty.getDynamic().evaluate(description, evaluator);
                    evaluator.addSymbol(EvaluationConstants.THIRDPARTY_ID, String.valueOf(thirdPartyID), new Double(thirdPartyID), false);
                    evaluator.addSymbol(EvaluationConstants.THIRDPARTY_NAME, thirdParty.getName(), new Double(0), false);

                }
                if (roleDCO != null) {
                    roleDCO.evaluate(evaluator);
                    evaluator.addSymbol(EvaluationConstants.ROLE_ID, String.valueOf(role.getRole().getId()), new Double(role.getRole().getId()), false);
                    evaluator.addSymbol(EvaluationConstants.ROLE_DESC, String.valueOf(role.getRole().getDescription()), new Double(0), false);
                }
            }
        } catch (Exception ex) {
            log.error("Error addSymbols: " + ex.getMessage());
        }
    }

    public void evaluateReserveValidations(com.consisint.acsele.claim.api.Claim claim, List<ValidationCoverageClaim> validationRowClaims, String cnrId, TablaSimbolos symbols) {
        GNUEvaluator traductor = GNUEvaluator.getInstance();
        try {
            ExpresionEvaluator evaluator = this.fillSymbolClaim(String.valueOf(claim.getId()), symbols);
            AgregatedPolicy agregatedPolicy = getAgregatedPolicy();
            ClaimNormalReservePersister normalReservePersister = ClaimNormalReservePersister.Impl.getIntance();
            long cioID = normalReservePersister.getClaimInsuranceObjectById(cnrId);
            ClaimInsuranceObject cio = ClaimInsuranceObject.getInstance(cioID);
            if (cio != null)
                addSymbols(evaluator, cio, agregatedPolicy);
            for (ValidationCoverageClaim validation : validationRowClaims) {
                TablaSimbolos symbolsTable = evaluator.getTablaSimbolos();
                boolean validity = traductor.evaluateLogical(validation.getCondition(), symbolsTable);
                validation.setValidity(validity);
                if (!validity) {
                    validation.setMessageEvaluated(traductor.evaluateMessage(validation.getMessage(), symbolsTable));
                    //Se genera Traza
                    if (validation.isTrackeable()) {
                        Map<String, Long> agregatedObjects = getAggregatedObjects(claim, cio);
                        Long evaluatedCoverageId = null;
                        try {
                            if (cnrId != null && normalReservePersister.load(Long.parseLong(cnrId)) != null) {
                                EvaluatedCoverage evaluatedCoverage = normalReservePersister.load(Long.parseLong(cnrId)).getEvaluatedCoverage();
                                evaluatedCoverageId = evaluatedCoverage.getId();
                            }
                        } catch (Exception ignore) {
                        }

                        Long claimId = null;

                        if (claim.getId() != 0) {
                            claimId = claim.getId();
                        }

                        Long operationId = null;
                        Policy pol = claim.getPolicy();
                        if (pol != null) {
                            operationId = pol.getOperationPK().getPk();
                        }

                        AuditTrailManager.generateValidationAuditTrail((CustomAuditItem) CustomAuditItemManager.getPersistance().getInstance(validation.getCustomAuditItemId()),
                                claim.getClaimNumber(), validation.getMessage(),
                                agregatedObjects.get("agregatedpolicy"), agregatedObjects.get("agregatedriskunitid"),
                                agregatedObjects.get("agregatedinsuranceobject"), evaluatedCoverageId, null, null, claimId, operationId);
                    }
                    ValidationUtil.sendNotifications(validation.getNotificationId(), validation.getValidationType(),
                            validation.getMessage(), symbolsTable);
                }
            }
        } catch (Exception ex) {
            log.error("Error in evaluateReserveValidations: " + ex.getMessage());
        }
    }

    private String getThirdPartyAddressRole(RoleList requiredRoleList, Collection<Participation> participationCollection) {
        com.consisint.acsele.thirdparty.api.ThirdParty thirdParty;
        String thirdPartyCompleteAddress = "";

        outerLoop:
        for (Participation participation : participationCollection) {
            Role roleParticipation = participation.getRole();
            for (com.consisint.acsele.uaa.api.Role role : requiredRoleList) {
                if (roleParticipation.getRole().getDescription().equalsIgnoreCase(role.getDescription())) {
                    if (participation.getAddress() != null && participation.getAddress().getDynamic() != null) {
                        thirdPartyCompleteAddress = participation.getAddress().getDynamic().getAddress();
                        break outerLoop;
                    }
                }
            }
        }
        return thirdPartyCompleteAddress;
    }

    private void sendOpenItemsInterface(OpenItem openItem, Claim claim, long thidPartyId, double amount) {
        try {
            OpenItemService instance = OpenItemService.Impl.getInstance();
            if (instance != null) {
                instance.sendCreateToExactus(openItem.getOpenItemID(), claim, thidPartyId, amount, -1);
                //instance.processInterfaceOutFromClaim(claim);
            }
            // llama al servicio para asociarlo
            CRMInternalServices crmServices = CRMInternalServices.Impl.getInstance();
            String messageCRM = null;
            if(crmServices != null) messageCRM = crmServices.processClaimComposer(claim, openItem);
            if(messageCRM != null) setResponseMsg(messageCRM);

        } catch (Throwable e) {
            log.error("No se envio el OI Id:" + openItem.getOpenItemID() + ".No se pudo procesar la interfaz :\n" + e.getMessage());
        }
    }

    public static void setResponseMsg(String text){
        Toolkit.getDefaultToolkit().beep();
        JOptionPane optionPane = new JOptionPane(text,JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = optionPane.createDialog("Response!");
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }

    /**
     * Generate Scheduled Multiple Payments
     *
     * @param ruId
     * @param ioId
     * @param thirdPartyId
     * @param periodicity
     * @param amount
     * @param numberOfPayments
     * @param benefitDuration
     * @throws Exception
     */
    public String generateScheduledPaymentToThirdParty(String ruId, String ioId, Participation participation, int periodicity, int benefitDuration, Double amount,
                                                       double deducedAmount, int numberOfPayments, ClaimReserveBenefit benefit, Collection selectedRiskUnits) throws Exception {
        ClaimNormalReserve normalReserve = benefit.getClaimNormalReserve();
        ClientResponse response;
        CoverageReserve coverageReserve = new CoverageReserve(normalReserve, this);
        Double reserveAmount = coverageReserve.getAmount();
        Double participationP = (amount * 100) / reserveAmount;
        Session session = null;
        Transaction transaction = null;
        int typeScheduled = 2;
        String message = null;
        Collection thirdPartiesFromInsuranceObject;
        try {
            session = HibernateUtil.getSession();
            if (startTx) {
                transaction = HibernateUtil.beginTransaction(session);
            }
            PaymentOrder paymentOrder;
//            int paymentOrderPk;
            Collection thirdParties;
            SearchThirdPartyResult thirdParty;
            String thirdPartyName = null;
            int thirdPartyRoleId = 0;
            Iterator thirdPartiesIterator;
            Date commitmentDate, currentDate, today;
            Calendar calendar;
            int days = 15;
            //Double paymentAmount = new Double(amount.doubleValue() / numberOfPayments);
            Double paymentAmount = amount; //Verificar contra la validacion javascript que hace en claimBenefit.jsp en la funcion loadBenefitPayments(...)
            log.info("medicion tiempo>> buscando  beneficiarios " + new Date());
            log.info("medicion tiempo>>luego de buscar  beneficiarios " + new Date());
            if (normalReserve != null) {
                ClaimInsuranceObject claimInsuranceObject = normalReserve.getContainer();
                AgregatedInsuranceObject agregatedInsuranceObject = claimInsuranceObject.getAgregatedInsuranceObject();
                Collection<Participation> participationCollection = agregatedInsuranceObject.getParticipationCollection();
                log.info("medicion tiempo>> buscando  terceros del objeto asegurado " + new Date());
                thirdPartiesFromInsuranceObject = getThirdPartiesFromInsuranceObject(participationCollection, null);
                log.info("medicion tiempo>> despues  terceros del objeto asegurado " + new Date());
            } else {
                thirdPartiesFromInsuranceObject = this.getThirdPartiesFromInsuranceObject(ruId, ioId);
            }

            thirdPartyName = participation.getThirdParty().getName();
            thirdPartyRoleId = (int) com.consisint.acsele.uaa.api.Role.Impl.load(participation.getRole().getDynamic().getDCO().getDesc()).getId();

            response = getPaymentsOrders(null, this.policy.getPK(), ruId, ioId, String.valueOf(normalReserve.getType()),
                    normalReserve.getPk(), true);

            PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) response.getAttribute("paymentOrders");
            EvaluatedCoverage ec = normalReserve.getEvaluatedCoverage();
            checkClaimConfiguration(amount, numberOfPayments, paymentOrderCollection, ec, typeScheduled, selectedRiskUnits);
            log.debug("********[Acsele v4.5] ClaimComposerWrapper.generateScheduledPayments(){" + "6860}thirdPartyName = " + thirdPartyName);
            ClaimsCoverageConfiguration configuration = ClaimsCoverageConfiguration
                    .load(Long.parseLong(this.getAgregatedPolicy().getProduct().getPk()),
                            ec.getConfiguratedCoverageOA().getId());
            today = new Date();
            //  obtiene fecha Inicial del campo ocurrenceDate del claim y/o aplicando propiedades de la cobertura
            currentDate = getConfiguredStartDate(ruId, ioId, normalReserve.getEvaluatedCoverage());
            calendar = Calendar.getInstance(UserInfo.getLocale());
            calendar.setTime(currentDate);
            if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY && (calendar.get(Calendar.DAY_OF_MONTH) != 1
                    && calendar.get(Calendar.DAY_OF_MONTH) != days)) {
                if (calendar.get(Calendar.DAY_OF_MONTH) > days) {
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, days);
                }
                currentDate = calendar.getTime();
            }
            Calendar calendarEndDate = Calendar.getInstance(UserInfo.getLocale());
            for (int i = 0; i < numberOfPayments; i++) {
                calendar.setTime(currentDate);
                if (periodicity == ClaimNormalReserve.PERIODICITY_DAYS) {
                    calendar.add(Calendar.DAY_OF_MONTH, benefitDuration);
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_SEMIMONTHLY) {
                    if (calendar.get(Calendar.DAY_OF_MONTH) + 1 > days) {
                        calendar.add(Calendar.MONTH, 1);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                    } else {
                        calendar.set(Calendar.DAY_OF_MONTH, days);
                    }
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_WEEKLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 7);
                } else if (periodicity == ClaimNormalReserve.PERIODICITY_BIWEEKLY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 14);
                } else {    // ClaimNormalReserve.PERIODICITY_MONTHS or PERIODICITY_NONE
                    calendar.add(Calendar.MONTH, benefitDuration);
                }
                if (numberOfPayments == 1) {
                    calendarEndDate.setTime(currentDate);
                } else {
                    calendarEndDate.setTime(calendar.getTime());
                    calendarEndDate.add(Calendar.DATE, -1);
                }
                commitmentDate = currentDate;
                if (configuration != null && configuration.getFistPaymentDate() != null && !configuration.getFistPaymentDate().equals("") && i == 0) {
                    TablaSimbolos symbolsTable = new TablaSimbolos();
                    Double date = this.evaluate(configuration.getFistPaymentDate(), symbolsTable, ec);
                    Date calcCommitmentDate = Funciones.transformDate(date);
                    log.debug("CCW generateScheduledPayments Commitment date " + calcCommitmentDate);
                    commitmentDate = calcCommitmentDate;
                }
                if (configuration.getPaymentDateCalculation() == ClaimsCoverageConfiguration.RETROACTIVE_TYPE) {
                    commitmentDate = DateUtil.sumDaysToDate(this.getClaim().getOcurrenceDate(), configuration.getWaitingPeriod());
                    log.debug("CCW generateScheduledPayments Commitment date RETROACTIVE_TYPE " + commitmentDate);
                }
                paymentOrder = new PaymentOrder(benefit);
                paymentOrder.setDate(today);
                paymentOrder.setStartDate(currentDate);
                paymentOrder.setCommitmentDate(commitmentDate);
                paymentOrder.setEndDate(calendarEndDate.getTime());
                currentDate = calendar.getTime();

                message = verifyDatesPaymentOrder(normalReserve.getPk(), paymentOrder.getStartDate(), paymentOrder.getEndDate(), participation.getThirdParty().getId(), configuration.isAllowPaymentsInOneDay());

                if (message == null) {
                    paymentOrder.setAmount(paymentAmount);
                    paymentOrder.setBeneficiary(thirdPartyName);
                    paymentOrder.setDistributionAmount(0);
                    paymentOrder.setDoneBy(UserInfo.getUser());
                    paymentOrder.setParticipationPercentage(participationP);
                    paymentOrder.setPenaltyPercentage(0);
                    paymentOrder.setReason("");
                    paymentOrder.setRefundPercentage(0);
                    paymentOrder.setPaymentOrderStatus(PaymentOrderStatus.PENDING_STATE);
                    paymentOrder.setThirdPartyRoleID(thirdPartyRoleId);
                    //benefit recipient for payments
                    long thirdPartyIdn = participation.getThirdParty().getId();
                    ClaimBeneficiaryPersister claimBeneficiaryPersister = ClaimBeneficiaryPersister.Impl.getInstance();
                    ClaimBeneficiaryPK pk = new ClaimBeneficiaryPK(benefit.getId(), thirdPartyIdn, paymentOrder.getThirdPartyRoleID());
                    ClaimBeneficiary claimBeneficiary = claimBeneficiaryPersister.load(pk);
                    if (claimBeneficiary != null && claimBeneficiary.getRecipientThirdParty() != null) {
                        paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                        paymentOrder.setThirdPartyId(claimBeneficiary.getRecipientThirdParty().getId());
                    } else {
                        paymentOrder.setThirdBeneficiaryId(thirdPartyIdn);
                        paymentOrder.setThirdPartyId(thirdPartyIdn);
                    }
                    paymentOrder.setType(PaymentOrderType.PARTIAL.getValue());
                    paymentOrder.setOnHold(0);
                    paymentOrder.setCoverageDesc(ec.getDesc());
                    paymentOrder.save();
                    normalReserve.getPaymentOrderList().put(paymentOrder.getPk(), paymentOrder);

                    if (deducedAmount > 0) {
                        PaymentOrderDetail paymentOrderDetail = new PaymentOrderDetail();
                        paymentOrderDetail.setPk(paymentOrder.getPk());
                        paymentOrderDetail.setDeducedAmount(deducedAmount);
                        paymentOrderDetail.save();
                    }
                } else {
                    throw new ApplicationException(Exceptions.CCErrorSamePeriodPaid, Severity.ERROR);
                }
            }
            if (message == null) {
                if (startTx) {
                    HibernateUtil.commit(transaction, session);
                }
            }
        } catch (Exception e) {
            session.clear();
            if (startTx) {
                HibernateUtil.rollBack(transaction);
            }
            log.debug("Error generating the payment orders.", e);
            ExceptionUtil.handlerException(e);
//        } finally {
            //HibernateUtil.closeSession(session);
        }
        return message;
    }

    public void setPaymentBranchOffice(Collection paymentOrders, Long branchOfficeId) {
        Session session = null;
        Transaction transaction = null;
        log.debug("Begin setPaymentBranchOffice...");
        try {
            session = getHibernateSession();
            transaction = beginTransaction(false, session);
            Iterator iterator = paymentOrders.iterator();
            while (iterator.hasNext()) {
                PaymentOrder paymentOrder = (PaymentOrder) iterator.next();
                if (paymentOrder.getClaimReserve() instanceof ClaimNormalReserve
                        && paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.APPROVED_STATE) {
                    paymentOrder.setBranchOfficeId(branchOfficeId);
                    paymentOrder.updateBranchOffice();
                }
            }
            commitTransaction(transaction, session);
            log.debug("Ending setPaymentBranchOffice...");
        } catch (Exception e) {
            log.error("There was an error changing the branch of the Payment Order: ", e);
            rollbackTransaction(transaction, session);
            throw new TechnicalException(Exceptions.CCErrorChangingOrderPaymentState, Severity.FATAL, e);
        } finally {
            closeHibernateSession(session);
        }
    }

    private Collection<Participation> getParticipationsRecursive(AggregateObject agg, RoleList roleList) {
        Collection<Participation> participationsResult = Collections.EMPTY_LIST;
        try {
            if (agg == null) return Collections.EMPTY_LIST;
            Collection<Participation> participations = /*(agg instanceof EvaluatedCoverage)? ((EvaluatedCoverage) agg).getParticipationByRole(roleList):*/
                    (agg instanceof AgregatedInsuranceObject) ? ((AgregatedInsuranceObject) agg).getParticipationByRole(roleList) :
                            (agg instanceof AgregatedRiskUnit) ? ((AgregatedRiskUnit) agg).getParticipationByRole(roleList) :
                                    (agg instanceof AgregatedPolicy) ? ((AgregatedPolicy) agg).getParticipationByRole(roleList) : participationsResult;
            if (participations != null && !participations.isEmpty()) return participations;
            if (agg instanceof AgregatedPolicy) return Collections.EMPTY_LIST;
            AggregateObject nexAgg = (agg instanceof AgregatedInsuranceObject) ? ((AgregatedInsuranceObject) agg).getAgregatedRiskUnit() :
                    (agg instanceof AgregatedRiskUnit) ? ((AgregatedRiskUnit) agg).getAgregatedPolicy() : null;
            return getParticipationsRecursive(nexAgg, roleList);
        } catch (Exception e) {
            log.debug("There aren't participations in Level " + agg.getClass().getSimpleName());
        }
        return participationsResult;
    }


    private ExpresionEvaluator createExpresion(AgregatedInsuranceObject aio) {
        ExpresionEvaluator expresionEvaluator = ExpresionEvaluator.createEvaluator();

        InsuranceObjectInfo insuranceObjectInfo = new InsuranceObjectInfo();
        insuranceObjectInfo.setAgregatedInsuranceObject(aio);
        expresionEvaluator = aio.publishAllSymbolsToUp(expresionEvaluator);

        return expresionEvaluator;
    }

    private DefaultConfigurableObject evaluateDCO(ExpresionEvaluator expresionEvaluator, DefaultConfigurableObject damage, ConfigurableObjectType cot) {
        expresionEvaluator.evaluateConfigurableObject(damage);
        return damage;
    }


    public Enumeration getSelectedRiskUnitEnumeration(Collection selectedRiskUnits, Date ocurrenceDate) {
        log.debug("------------------ getSelectedRiskUnitEnumeration ---------------------");
        this.ocurrenceDate = ocurrenceDate;
        if (isRetroactive()) {
            return policy.getSelectedRiskUnits(selectedRiskUnits);
        } else {
            if (selectedRiskUnits == null || selectedRiskUnits.isEmpty()) {
                return null;
            }
            Vector<AgregatedRiskUnit> finalRiskUnits = new Vector<AgregatedRiskUnit>();
            Iterator iterator = selectedRiskUnits.iterator();
            while (iterator.hasNext()) {
                String[] ruOp = ((String) iterator.next()).split("-");
                if (ruOp.length <= 1) throw new TechnicalException("RU or Opk invalid!!!!!!");
                long ruId = Long.parseLong(ruOp[0]);
                long opPk = Long.parseLong(ruOp[1]);
                AgregatedRiskUnit agregatedRiskUnit = DBRiskUnitManager.loadRiskUnitByIdAndOp(ruId, opPk, policy);
                if (agregatedRiskUnit != null) {
                    finalRiskUnits.addElement(agregatedRiskUnit);
                }
            }
            return finalRiskUnits.elements();
        }
    }

    public Enumeration getSelectedRiskUnitEnumeration(Collection selectedRiskUnits) {
        return getSelectedRiskUnitEnumeration(selectedRiskUnits, null);
    }

    public AgregatedInsuranceObject getAgregatedInsuranceObject(AgregatedRiskUnit aru, String ioID) {
        AgregatedInsuranceObject aio;
        if (isRetroactive()) {
            return aru.getInsuranceObject(ioID);
        } else {
            List<AgregatedInsuranceObject> iods = new ArrayList<AgregatedInsuranceObject>();
            try {
                iods = DBAgregatedInsuranceObject.getListByEffectiveDate(aru, getOcurrenceDate());
            } catch (Exception e) {

            }
            for (AgregatedInsuranceObject io : iods) {
                if (ioID.equals(io.getDesc()) || (ioID.equals(String.valueOf(io.getId())))) {
                    return io;
                }
            }
        }
        return null;
    }

    public EvaluatedCoverage getEvaluatedCoverage(AgregatedInsuranceObject aio, String ecID) throws Exception {
        if (isRetroactive()) {
            return aio.getEvaluatedCoverage(ecID);
        } else {
            List<EvaluatedCoverage> ecds = new ArrayList<EvaluatedCoverage>();
            try {
                ecds = DBEvaluatedCoverageManager.getListByEffectiveDate(aio, getOcurrenceDate());
            } catch (Exception e) {

            }
            for (EvaluatedCoverage evaluatedCoverage : ecds) {
                if (ecID.equals(String.valueOf(evaluatedCoverage.getId())) || ecID.equals(evaluatedCoverage.getDescription())) {
                    return evaluatedCoverage;
                }
            }
        }
        return null;
    }

    public List<EvaluatedCoverage> getEvaluatedCoverageList(AgregatedInsuranceObject aio) {
        List<EvaluatedCoverage> ecList;
        if (isRetroactive()) {
            ecList = Collections.list(aio.getEvaluatedCoverages());

        } else {
            ecList = DBEvaluatedCoverageManager.getListByEffectiveDate(aio, getOcurrenceDate());
        }
        return ecList;
    }

    public List<AgregatedInsuranceObject> getAgregatedInsuranceObjectList(AgregatedRiskUnit aru) {
        List<AgregatedInsuranceObject> aioList;
        if (isRetroactive()) {
            aioList = Collections.list(aru.getInsuranceObjects());
        } else {
            aioList = DBAgregatedInsuranceObject.getListByEffectiveDate(aru, getOcurrenceDate());
        }
        return aioList;
    }

    public boolean isRetroactive() {
        return isRetroactive(claim);
    }

    public boolean isRetroactive(Claim claim) {
        if (isRetroactive == null) {
            com.consisint.acsele.claim.engine.validation.ClaimValidator claimValidator = com.consisint.acsele.claim.engine.validation.ClaimValidator.Impl.getInstance();
            String policyID = String.valueOf(policy.getId());
            if (ocurrenceDate == null) {
                ocurrenceDate = getOcurrenceDate(claim);
            }

            if (claimValidator.allowRetroactive(policyID) ||
                    (claimValidator.checkPolicyRetroactivityDate(policyID, DateUtil.getSqlDate(ocurrenceDate)))) {
                isRetroactive = true;
            } else {
                isRetroactive = false;
            }
        }
        return isRetroactive;
    }

    public AgregatedRiskUnit getAgregatedRiskUnitById(String id) {
        AgregatedRiskUnit aru;
        if (isRetroactive()) {
            aru = policy.getAgregatedRiskUnit(id);
        } else {
            aru = claim.getClaimRiskUnit(id).getAgregatedRiskUnit();
        }
        return aru;
    }

    public AgregatedInsuranceObject getAgregatedInsuranceObjectById(String ruID, String ioID) {
        AgregatedInsuranceObject aio;
        if (isRetroactive()) {
            aio = policy.getAgregatedRiskUnit(ruID).getInsuranceObject(ioID);
        } else {
            aio = claim.getClaimInsuranceObject(ioID).getAgregatedInsuranceObject();
        }
        return aio;
    }

    public EvaluatedCoverage getEvaluatedCoverageById(String ruID, String ioID, String ecID) throws Exception {
        EvaluatedCoverage ec;
        if (isRetroactive()) {
            ec = policy.getAgregatedRiskUnit(ruID).getInsuranceObject(ioID)
                    .getEvaluatedCoverageByDesc(ecID);
        } else {
            AgregatedInsuranceObject aio = claim.getClaimInsuranceObject(ioID).getAgregatedInsuranceObject();
            ec = getEvaluatedCoverage(aio, ecID);
        }
        return ec;
    }


    public Boolean adjust(Vector covAffecteds) {
        log.info("---------------- //// adjust ///--------------------" + covAffecteds.size());
        Iterator j = covAffecteds.iterator();
        SearchBean searchBean = null;
        ClaimComposer composer;

        try {
            // composer = ClaimSessionUtil.getSession(getSession(), LoadCoveragesAction.class.getName());
            ClientResponse clientResponse = null;
            while (j.hasNext()) {
                searchBean = (SearchBean) j.next();
                log.debug("--- searchBean = " + searchBean);
                String ruId = searchBean.getCru();
                String ioId = searchBean.getIoId();
                String policyId = claim.getPolicyId();
                log.debug("--- policyId = " + policyId);
                String pk = searchBean.getPk();
                log.debug("--- pk = " + pk);
                clientResponse = getPaymentsOrders(ioId, policyId, ruId, ioId, "0", pk, true);
                log.debug("--- clientResponse = " + clientResponse);
                if (clientResponse != null && clientResponse.getResult()) {
                    PaymentOrderCollection paymentOrderCollection = (PaymentOrderCollection) clientResponse.getAttribute("paymentOrders");
                    log.debug("--- paymentOrderCollection = " + paymentOrderCollection);

                    if (!(paymentOrderCollection.getPaymentOrders().size() > 0)) {
                        Map datos = getAvailableCoveragesToEval(ruId, ioId, false);
                        String evaluatedCoverageID = searchBean.getEvaluatedCoverageID();
                        SearchBean searchBeanNewCalculus = (SearchBean) datos.get(evaluatedCoverageID);
                        log.debug("--- evaluatedCoverageID = " + evaluatedCoverageID);
                        String reserveLimitAmount = searchBeanNewCalculus.getAmount();
                        String reserveAmount = searchBeanNewCalculus.getReserveAmount();
                        log.debug("--- reserveLimitAmount = " + reserveLimitAmount + " - reserveAmount = " + reserveAmount);
                        String searchBeanReserveAmount = searchBean.getReserveAmount();
                        log.debug("--- searchBeanReserveAmount = " + searchBeanReserveAmount);

                        if (Double.parseDouble(reserveAmount) == 0) {
                            log.debug("El monto de la reserva arrojo cero, hay que validar si es porque se configuro vacio.");
                            long productPk = policy.getProduct().getId();
                            AgregatedInsuranceObject aio = policy.getAgregatedRiskUnit(ruId).getInsuranceObject(ioId);
                            EvaluatedCoverage evaluatedCoverage = aio.getEvaluatedCoverage(evaluatedCoverageID);
                            ConfiguratedCoverage configuratedCoverage = evaluatedCoverage.getConfiguratedCoverage();
                            String coveragePk = configuratedCoverage.getPK();
                            log.debug("productPk = " + productPk + " - coveragePk =" + coveragePk);
                            ClaimsCoverageConfiguration ccc = ClaimsCoverageConfiguration
                                    .find(productPk, Long.parseLong(coveragePk));
                            String initialReserveAmount = ccc.getInitialReserveAmount();
                            log.debug("--- initialReserveAmount = '" + initialReserveAmount + "'");
                            if (StringUtil.isEmptyOrNullValue(initialReserveAmount)) {
                                log.debug("No aplica el ajuste");
                                return false;
                            }
                        }

                        if (Double.parseDouble(searchBeanReserveAmount) != Double.parseDouble(reserveAmount)) {
                            log.debug("El monto de la reserva ha sufrido cambios...");
                            ClaimReserveAdjust adjust = new ClaimReserveAdjust();
                            adjust.setDate(new Date());
                            adjust.setClaimReserveId(pk);

                            double newAmount = Math.abs(Double.parseDouble(searchBeanReserveAmount) - Double.parseDouble(reserveAmount));
                            log.debug("newAmount = " + newAmount);
                            adjust.setAmount(newAmount);
                            String calculusDesc = searchBeanNewCalculus.getDesc();
                            adjust.setDesc(calculusDesc);
                            log.debug("calculusDesc: " + calculusDesc);
                            adjust.setReason("AUTO ADJUST");

                            if (Double.parseDouble(searchBeanReserveAmount) < Double.parseDouble(reserveAmount)) {
                                adjust.setType(ReserveAdjustType.INCREASE.getValue());
                                adjust.save();
                                AgregatedInsuranceObject aio = policy.getAgregatedRiskUnit(ruId).getInsuranceObject(ioId);
                                EvaluatedCoverage evaluatedCoverage = aio.getEvaluatedCoverage(evaluatedCoverageID);
                                ClaimValidationService claimValidationService = ((ClaimValidationService) BeanFactory.getBean(ClaimValidationService.class));
                                claimValidationService.claimreOpening(claim.getId(), evaluatedCoverage);
                            } else if (Double.parseDouble(searchBeanReserveAmount) > Double.parseDouble(reserveAmount)) {
                                adjust.setType(ReserveAdjustType.DECREASE.getValue());
                                adjust.save();
                                AgregatedInsuranceObject aio = policy.getAgregatedRiskUnit(ruId).getInsuranceObject(ioId);
                                EvaluatedCoverage evaluatedCoverage = aio.getEvaluatedCoverage(evaluatedCoverageID);
                                ClaimValidationService claimValidationService = ((ClaimValidationService) BeanFactory.getBean(ClaimValidationService.class));
                                claimValidationService.claimreOpening(claim.getId(), evaluatedCoverage);
                            }
                            return true;
                        }
                    } else {
                        log.debug("El siniestro tiene pagos generados.");
                        return false;
                    }
                }
            }

        } catch (Exception e) {

        }
        return null;
    }

    public void generateLiquidate(long paymentOrderToLiquidate, long openItemID) {

        LiquidatedPaymentOrderId liquidatedPaymentOrderId = new LiquidatedPaymentOrderId(openItemID, paymentOrderToLiquidate);
        LiquidatedPaymentOrder liquidatedPaymentOrder = new LiquidatedPaymentOrder(liquidatedPaymentOrderId);
        liquidatedPaymentOrder.save(liquidatedPaymentOrder);
    }

    private Map<String, Long> getAggregatedObjects(com.consisint.acsele.claim.api.Claim claim, ClaimInsuranceObject claimInsuranceObject) {
        Map<String, Long> agregatedObjects = new HashMap<String, Long>();
        Long agregatedPolicyId = (claim.getPolicy().getId() != 0 ? claim.getPolicy().getId() : null);
        Long agregatedRiskUnitId = null;
        Long agregatedInsuranceObjectId = null;

        if (claimInsuranceObject != null && claimInsuranceObject.getAgregatedInsuranceObject() != null) {
            agregatedInsuranceObjectId = claimInsuranceObject.getAgregatedInsuranceObject().getId() != 0 ? claimInsuranceObject.getAgregatedInsuranceObject().getId() : null;
            agregatedRiskUnitId = claimInsuranceObject.getAgregatedInsuranceObject().getRiskUnit() != null ? claimInsuranceObject.getAgregatedInsuranceObject().getRiskUnit().getId() : null;
            agregatedRiskUnitId = (agregatedRiskUnitId != 0l ? agregatedRiskUnitId : null);
        }

        agregatedObjects.put("agregatedpolicy", agregatedPolicyId);
        agregatedObjects.put("agregatedriskunitid", agregatedRiskUnitId);
        agregatedObjects.put("agregatedinsuranceobject", agregatedInsuranceObjectId);
        return agregatedObjects;
    }

    /**
     * Create an entry with new symbols , only for life products
     * Crea un asiento contable segun los nuevos simbolos solo para los productos de vida
     * ACSELE-16128
     *
     * @param policy
     * @param paymentOrder
     */
    public static void createClaimEntry(Policy policy, PaymentOrder paymentOrder) {

        if (policy.getProduct().getProductType() != com.consisint.acsele.openapi.product.ProductType.LIFE)
            return;

        double pendingPremium;
        double loansAmount;
        double amountInterestLoans;
        double totalDeductions;
        Locale locale = new Locale(UserInfo.getLanguage(), UserInfo.getCountry());

        ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
        evaluator = ((AgregatedPolicy) policy).publishAllSymbols(evaluator);
        String pendingPremiumValue = "0";
        String loansAmountValue = "0";
        String amountInterestLoansValue = "0";
        if (policy.getProduct().getProductBehavior() != null) {
            pendingPremiumValue = policy.getProduct().getProductBehavior().getDeductionPremiumsOverDue() != null ?
                    (policy.getProduct().getProductBehavior().getDeductionPremiumsOverDue().split(",").length > 1 ?
                            policy.getProduct().getProductBehavior().getDeductionPremiumsOverDue().split(",")[1] :
                            (policy.getProduct().getProductBehavior().getDeductionPremiumsOverDue().split(";").length > 1 ?
                                    policy.getProduct().getProductBehavior().getDeductionPremiumsOverDue().split(";")[1] : "0")) : "0";
            loansAmountValue = policy.getProduct().getProductBehavior().getDeductionLoansAmount() != null ?
                    (policy.getProduct().getProductBehavior().getDeductionLoansAmount().split(",").length > 1 ?
                            policy.getProduct().getProductBehavior().getDeductionLoansAmount().split(",")[1] :
                            (policy.getProduct().getProductBehavior().getDeductionLoansAmount().split(";").length > 1 ?
                                    policy.getProduct().getProductBehavior().getDeductionLoansAmount().split(";")[1] : "0")) : "0";
            amountInterestLoansValue = policy.getProduct().getProductBehavior().getDeductionLoansInterest() != null ?
                    (policy.getProduct().getProductBehavior().getDeductionLoansInterest().split(",").length > 1 ?
                            policy.getProduct().getProductBehavior().getDeductionLoansInterest().split(",")[1] :
                            (policy.getProduct().getProductBehavior().getDeductionLoansInterest().split(";").length > 1 ?
                                    policy.getProduct().getProductBehavior().getDeductionLoansInterest().split(";")[1] : "0")) : "0";
        }

        if (NumberUtils.isNumber(pendingPremiumValue)) {
            pendingPremium = Double.valueOf(pendingPremiumValue);
        } else {
            pendingPremium = evaluator.evaluate(pendingPremiumValue);
        }

        if (NumberUtils.isNumber(loansAmountValue)) {
            loansAmount = Double.valueOf(loansAmountValue);
        } else {
            loansAmount = evaluator.evaluate(loansAmountValue);
        }

        if (NumberUtils.isNumber(amountInterestLoansValue)) {
            amountInterestLoans = Double.valueOf(amountInterestLoansValue);
        } else {
            amountInterestLoans = evaluator.evaluate(amountInterestLoansValue);
        }

        double totalDeduction = pendingPremium + loansAmount + amountInterestLoans;

        evaluator.addSymbol(EvaluationConstants.PENDINGPREMIUN, StringUtil.formatDoubleLocaleAndTruncate(pendingPremium, locale), pendingPremium, true);
        evaluator.addSymbol(EvaluationConstants.AMOUNT_LOANS, StringUtil.formatDoubleLocaleAndTruncate(loansAmount, locale), loansAmount, true);
        evaluator.addSymbol(EvaluationConstants.AMOUNT_FOR_INTEREST_LOANS, StringUtil.formatDoubleLocaleAndTruncate(amountInterestLoans, locale), amountInterestLoans, true);
        evaluator.addSymbol(EvaluationConstants.TOTAL_DEDUCTIONS, StringUtil.formatDoubleLocaleAndTruncate(totalDeduction, locale), totalDeduction, true);
        evaluator.addSymbol(EvaluationConstants.ENTRY_DATE, DateUtil.getDateFormated(Calendar.getInstance().getTime()), new Double(DateUtil.getValueFromDate(Calendar.getInstance().getTime())), true);
        evaluator.addSymbol(EvaluationConstants.ENTRY_AMOUNT, StringUtil.formatDoubleLocaleAndTruncate(totalDeduction, locale), totalDeduction, true);
        evaluator.addSymbol(EvaluationConstants.CLAIM_AMOUNT, StringUtil.formatDoubleLocaleAndTruncate(paymentOrder.getAmount().doubleValue(), locale), paymentOrder.getAmount().doubleValue(), true);

        List<String> concepts = new ArrayList<String>();
        String expensesConceptProp = AcseleConf.getProperty("expensesConcept");
        if (expensesConceptProp != null && !expensesConceptProp.isEmpty()) {
            concepts.add(expensesConceptProp);
            double expensesConcept = OpenItemImpl.findDeductionsByConcepts(policy.getPolicyNumber(), concepts);
            evaluator.addSymbol(EvaluationConstants.AMOUNT_OVERDUE_PREMIUMS, StringUtil.formatDoubleLocaleAndTruncate(expensesConcept, locale), expensesConcept, true);
        }

        CreateEntrys entrys = new CreateEntrys(GroupOperationType.SINISTER, OperationType.SINI_PAYMENT_COVERAGE, 0, evaluator.getTablaSimbolos());
        entryService.executeCreate(false, entrys);
    }

    /**
     * evaluates if a Peyment Order has an invoice
     * evalua si la orden de pago posee factura asociada
     * ACSELE-17797
     *
     * @param paymentOrder
     */
    private boolean hasInvoice(PaymentOrder paymentOrder) {
        CoverageInvoice coverageInvoice = null;

        if (paymentOrder != null && paymentOrder.getClaimReserveBenefit() != null)
            coverageInvoice = CoverageInvoice.Impl.loadInvoiceByBenefitAndPayment(paymentOrder.getClaimReserveBenefit().getId(), paymentOrder.getPk());

        return coverageInvoice != null;

    }

    private OpenItem postTrx(OpenItemVO openItemVO, Payment payment) throws Exception {
        try {
            OpenItemImpl oi = new OpenItemImpl(openItemVO.getOpenItemId(), openItemVO.getParentOpenItemId(), openItemVO.getThirdPartyId(),
                    openItemVO.getOpenItemReferenceDTO(), openItemVO.getAmount(), openItemVO.getDocDate(), openItemVO.getDueDate(), openItemVO.getOpenItemDate(),
                    openItemVO.getDateUseRecipents(), openItemVO.getAppliedTo(), openItemVO.getDocType().getId(),
                    openItemVO.getCurrencyID(),
                    openItemVO.getStatus(), openItemVO.getBalance(),
                    openItemVO.getRoleId(), openItemVO.getReferenceType(), openItemVO.getOperationPK(),
                    openItemVO.getSubStatus(), openItemVO.getSapCurrencyId());

            if (payment != null && payment.getPaymentOrder() != null && payment.getPaymentOrder().getClaimReserve() != null) {
                ClaimReserve reserve = payment.getPaymentOrder().getClaimReserve();
                TablaSimbolos symbolTable = new TablaSimbolos();
                if (reserve instanceof ClaimNormalReserve) {
                    symbolTable.put(EvaluationConstants.CLAIM_RESERVE_TYPE, "ClaimNormalReserve", new Double(1));
                } else if (reserve instanceof ClaimReserveByConcept) {
                    symbolTable.put(EvaluationConstants.CLAIM_RESERVE_TYPE, "ClaimReserveByConcept", new Double(2));
                    ReserveConcept concept = ((ClaimReserveByConcept) reserve).getReserveConcept();
                    if (concept != null) {
                        symbolTable.put(EvaluationConstants.RESERVE_CONCEPT, concept.getConcept(), new Double(concept.getPk()));
                        symbolTable.put(EvaluationConstants.RESERVE_CONCEPT_DESC, concept.getConcept(), new Double(concept.getPk()));
                    }
                }
                oi.setSymbolTable(symbolTable);
            }

            oi.save();
            openItemVO.setOpenItemId(String.valueOf(oi.getOpenItemID()));
            openItemVO.setAppliedTo(String.valueOf(oi.getAppliedTo()));

            return oi;
        } catch (Exception e) {
            log.error("General error", e);
            throw e;
        }
    }

    /**
     * Method for obtaining the symbol table of a claim
     *
     * @param Claim
     * @param EvaluatedCoverage
     */

    public TablaSimbolos publishSymbols(Claim claim, EvaluatedCoverage ec) {

        TablaSimbolos symbolsTable = new TablaSimbolos();

        this.publishSymbols(new SymbolsClaim(), symbolsTable, claim.getAgregatedPolicy(), claim, ec);
        return symbolsTable;

    }


    public boolean isAsl() {
        return asl;
    }

    public void setAsl(boolean asl) {
        this.asl = asl;
    }

    public OpenItemHome getOpenItemHome() {
        return openItemHome;
    }

    public void setOpenItemHome(OpenItemHome openItemHome) {
        this.openItemHome = openItemHome;
    }

    public void changeClaimnrmalReserveStatus(ClaimNormalReserve claimNormalReserve, int coverageReserveStatus) throws Exception {


        if (claimNormalReserve != null) {
            claimNormalReserve.setNormalReserveStatus(String.valueOf(coverageReserveStatus), claimNormalReserve.getAnalysisDcoId());
            if (CoverageReserveStatus.ACCEPTED.getValue() == coverageReserveStatus) {
                ClaimInsuranceObjectComposer insuranceObjectComposer = ClaimInsuranceObjectComposer.getInstance();
                insuranceObjectComposer.excludeInsuranceObjectsPolicy(getAgregatedPolicy(), claimNormalReserve.getEvaluatedCoverage().getConfiguratedCoverageOA().getClaimsCoverageConfiguration(), claimNormalReserve);
            }
        }
    }

    public boolean checkStatusPaymentOrder(Collection paymentOrders){
        Boolean checkStatus = true;
       for (Object objectpaymentOrder: paymentOrders){
            PaymentOrder paymentOrder = (PaymentOrder) objectpaymentOrder;
            if (paymentOrder != null &&
                    paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.PENDING_STATE ||
                    paymentOrder.getPaymentOrderStatus() == PaymentOrderStatus.APPROVED_STATE) {
                checkStatus = false;
                break;
            }
        }

        return checkStatus;
    }
}