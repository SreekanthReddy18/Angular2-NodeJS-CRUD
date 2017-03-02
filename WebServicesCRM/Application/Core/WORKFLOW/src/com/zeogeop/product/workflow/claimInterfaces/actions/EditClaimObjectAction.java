package com.consisint.acsele.workflow.claimInterfaces.actions;

import com.consisint.acsele.claim.api.AffectedInsuranceObject;
import com.consisint.acsele.claim.api.CoverageReserveStatus;
import com.consisint.acsele.openapi.policy.DynamicData;
import com.consisint.acsele.openapi.policy.DynamicMetaData;
import com.consisint.acsele.persistent.persisters.ValidationDamageClaimPersister;
import com.consisint.acsele.policy.api.Coverage;
import com.consisint.acsele.policy.server.PolicySystem;
import com.consisint.acsele.product.applet.EnumValidationCovClaim;
import com.consisint.acsele.product.applet.ValidationDamageClaim;
import com.consisint.acsele.product.server.ClaimsCoverageConfiguration;
import com.consisint.acsele.reinsurance.reinsuranceconfiguration.ContractComponent;
import com.consisint.acsele.reinsurance.reinsuranceconfiguration.ReinsuranceContract;
import com.consisint.acsele.reinsurance.reinsurancemanagers.AccessContractsManager;
import com.consisint.acsele.reinsurance.testinterface.AccessContracts;
import com.consisint.acsele.reinsurance.testinterface.AccessContractsImpl;
import com.consisint.acsele.util.*;
import com.consisint.acsele.util.context.CRMInternalServices;
import com.consisint.acsele.util.error.ApplicationExceptionChecked;
import com.consisint.acsele.util.evaluator.EvaluationConstants;
import com.consisint.acsele.util.evaluator.EvaluationContext;
import com.consisint.acsele.util.evaluator.ExpresionEvaluator;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.workflow.claimInterfaces.forms.EditForm;
import com.consisint.acsele.workflow.claimInterfaces.forms.SearchBean;
import com.consisint.acsele.workflow.claimapi.Claim;
import com.consisint.acsele.workflow.claimapi.ClaimComposer;
import com.consisint.acsele.workflow.claimapi.ClaimComposerWrapper;
import com.consisint.acsele.workflow.claimapi.ClaimSessionUtil;
import com.consisint.acsele.workflow.claimapi.historical.ClaimHistoricalOperationType;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

/**
 * This class process the edit Object action requested by the client.<p>
 * Title: EditClaimObjectAction.java <br>
 * Copyright: (c) 2003 Consis International<br>
 * Company: Consis International<br>
 */
public class EditClaimObjectAction extends GenericAction {

    private static final AcseleLogger log = AcseleLogger.getLogger(EditClaimObjectAction.class);
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException, ServletException {
        super.execute(mapping, form, request, response);
        AffectedInsuranceObject affio = null;
        EditForm forma = null;
        if (form != null) {
            forma = (EditForm) form;
            log.debug("Form In Session");
        }

        ClientResponse clientResponse;

        String action = forma.getAction();
        boolean isEvalDatePost = true;

        try {

            ClaimComposer composer = ClaimSessionUtil
                    .getSession(request.getSession(), EditClaimObjectAction.class.getName());

            String ioID = getInsuranceObjectIdParameterValue(request);
            if (!StringUtil.isEmptyOrNullValue(ioID)) {
                forma.setSourceNode(ioID);
                putSessionObject(request, "ioID", ioID);
            }

            String riskUnitID = getRiskUnitIdParameterValue(request, forma);
            if (!StringUtil.isEmptyOrNullValue(ioID) && !StringUtil.isEmptyOrNullValue(riskUnitID)) {
                forma.setParentSource(riskUnitID);
            }

            log.debug("forma.getAction() = " + forma.getAction());
            log.debug("forma.getSourceNode() = " + forma.getSourceNode());
            log.debug(
                    "PolicySystem.CLAIM_INSURANCE_OBJECT = " + PolicySystem.CLAIM_INSURANCE_OBJECT);
            log.debug("forma.getCruId() = " + forma.getCruId());
            log.debug("forma.getCioId() = " + forma.getCioId());
            log.debug("forma.getCnrId() = " + forma.getCnrId());
            log.debug("forma.getParentSource() = " + forma.getParentSource());
            log.debug("forma.getDamageDcoPk() = " + forma.getDamageDcoPk());
            log.debug("forma.getTargetNode() = " + forma.getTargetNode());

            RequestUtil.printRequest(request, log);

            clientResponse = composer.edit(forma.getAction(), ioID,
                    PolicySystem.CLAIM_INSURANCE_OBJECT, forma.getCruId(),
                    forma.getCioId(), forma.getCnrId(),
                    forma.getParentSource(), forma.getDamageDcoPk(),
                    forma.getTargetNode());

            Vector<SearchBean> affecteds = new Vector<SearchBean>();
            Vector<SearchBean> covAffecteds = new Vector<SearchBean>();
            Vector<SearchBean> reserveConcepts = new Vector<SearchBean>();
            composer.getCRU(affecteds, covAffecteds, reserveConcepts);

            if (!covAffecteds.isEmpty()) {
                String ramoColumnSystemProperty = AcseleConf.getProperty("ramoColumn");
                Iterator j = covAffecteds.iterator();
                Coverage coverage;
                Claim theClaim = composer.getClaim();
                ReinsuranceContract reinsuranceContract = null;
                AccessContracts access = new AccessContractsImpl();
                Double branch;
                Enumeration<ContractComponent> theContractComponents = null;
                boolean isRetentionType;
                SearchBean searchBean = null;

                while (j.hasNext()) {
                    searchBean = (SearchBean) j.next();
                    ioID = searchBean.getIoId();
                    coverage = searchBean.getEvaluatedCoverage();
                    branch = coverage.getDynamicData().getValue(ramoColumnSystemProperty);

                    AccessContractsManager manager = new AccessContractsManager();
                    String rgPK = access.getReinsuranceGroupByCode(String.valueOf(branch.intValue()));
                    String rcPK=manager.getFacultativeContractByFacultativeReinsured(theClaim.getPolicyId(), null, rgPK);
                    if (!StringUtil.isEmptyOrNullValue(rgPK) && StringUtil.isEmptyOrNullValue(rcPK) ) //carga
                        rcPK = manager.getPublishedContractByGroupAndDate(rgPK, new java.sql.Date(theClaim.getOcurrenceDate().getTime()), (theClaim.getAgregatedPolicy() == null ? null : theClaim.getAgregatedPolicy().getProduct().getId()));
                    if (!StringUtil.isEmptyOrNullValue(rcPK))
                        reinsuranceContract = ReinsuranceContract.load(rcPK);

                    if (null != reinsuranceContract) {
                        isRetentionType = true;
                        theContractComponents = reinsuranceContract.getContractComponents();
                        ContractComponent contractComponent = null;
                        while (null != theContractComponents && theContractComponents.hasMoreElements() && isRetentionType) {
                            contractComponent = theContractComponents.nextElement();
                            if (!contractComponent.getTypeOfComponent().equalsIgnoreCase("Retention")) {
                                isRetentionType = false;
                            }
                        }

                        if (!isRetentionType) {
                            searchBean.setLabel("(R) " + searchBean.getDesc());
                            searchBean.setDesc("(R) " + searchBean.getDesc());
                        }
                    }

                    Date ocurrenceDate = theClaim.getOcurrenceDate();
                    Date finishDatePol = theClaim.getAgregatedPolicy().getFinishDate();
                    ClaimsCoverageConfiguration ccc = composer.getClaimCoverageConfiguration(riskUnitID, ioID, searchBean.getEvaluatedCoverageID());
                    ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
                    log.debug("searchBeanF: " + searchBean);
                    log.debug("searchBeanF.getDesc(): " + searchBean.getDesc());
                    log.debug("finishDatePol = " + finishDatePol);
                    if (finishDatePol != null) {
                        if (ocurrenceDate.after(finishDatePol)) {
                            Date dateCompare = finishDatePol;
                            if (ccc.getIsClaimPolicyPost()) {
                                double evalFormulateDays = evaluator.evaluate(ccc.getClaimFormulaPost());
                                dateCompare = DateUtil.sumDaysToDate(finishDatePol, (int) evalFormulateDays);
                                if (ocurrenceDate.before(dateCompare)) {
                                    searchBean.setLabel("false");
                                    searchBean.setDesc(searchBean.getDesc());
                                    searchBean.setStatus(CoverageReserveStatus.IN_ANALYSIS.getValue());
                                    log.debug("searchBean.getDesc(): " + searchBean.getDesc());
                                    isEvalDatePost = false;
                                }
                            }
                        }
                    }
                }
            }

            putReserve(request, covAffecteds, true);
            putReserve(request, reserveConcepts, false);


            putSessionObject(request, "sourceNode", forma.getSourceNode());
            Enumeration enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) {
                String nameAtt = (String) enumeration.nextElement();
                request.setAttribute(nameAtt, request.getParameter(nameAtt));
            }

            log.debug("action = " + action);
            if (action.equals("editStatisticTitles")) {
                putSessionObject(request, "titlesParentsCollection",
                        clientResponse.getAttribute("responsedata"));
                ClaimComposerWrapper.addClaimHistorical(composer.getClaim(),
                        ClaimHistoricalOperationType.EDIT_OBJECT_AFFECTED);
            } else if ((action.equals("editReserve")) || (action.equals("editBenefit"))) {
                putSessionObject(request, "adjustCollection", clientResponse.getAttribute("responsedata"));
                ClaimComposerWrapper.addClaimHistorical(composer.getClaim(),
                        ClaimHistoricalOperationType.EDIT_RESERVE);

            } else if (action.equalsIgnoreCase("editAffectedObject")) {
                if (clientResponse != null) {
                    List validationError = Arrays.asList(EnumValidationCovClaim.ERROR.getValue(),EnumValidationCovClaim.WARNING.getValue(),EnumValidationCovClaim.NOTIFICATION.getValue());
                    List validationView = Arrays.asList(EnumValidationCovClaim.VIEW.getValue());

                    Hashtable data = (Hashtable) clientResponse.getAttribute("responsedata");
                    Claim claim = composer.getClaim();
                    addSymbols(Long.valueOf(claim.getPk()), ioID, affio);

                    data.put("sourceNode", forma.getSourceNode());
                    data.put("classSource", forma.getClassSource());
                    data.put("aruId", forma.getCruId());
                    putSessionObject(request, "dataForm", data);

                    if(getSessionObject(request, "applyDamageValidations") != null){
                        List<ValidationDamageClaim> claimDamageErrorsValidations = getClaimDamageValidations(composer,0 , validationError);
                        for(ValidationDamageClaim validation : claimDamageErrorsValidations){
                            if (!validation.isValidFormula() && (validation.getType() == 0 || validation.getType() == 2)){
                                putSessionObject(request, "invalidDamage", true);
                                break;
                            }
                        }
                        putSessionObject(request, "ClaimDamageValidations", claimDamageErrorsValidations);
                    }

                    List<ValidationDamageClaim> claimDamageViewValidations = getClaimDamageValidations(composer,0 ,validationView);
                    putSessionObject(request, "ClaimDamageValidationsCoverage", claimDamageViewValidations);
                    ClaimComposerWrapper.addClaimHistorical(composer.getClaim(),
                            ClaimHistoricalOperationType.EDIT_OBJECT_AFFECTED);

                    request.setAttribute("coverageList", covAffecteds);
                    // llama al servicio para asociarlo
                    CRMInternalServices crmServices = CRMInternalServices.Impl.getInstance();
                    if(crmServices != null) crmServices.processEditClaimObjectAction(composer);

                }
            }
            log.debug("Is New the Insurance Object Affected: " + request.getParameter("goTo"));
        } catch (Exception ex) {
            log.error("Caught an exception.", ex);
        }
        request.setAttribute("isEvalDatePost", isEvalDatePost);
        return goTo(mapping, request, request.getParameter("goTo"));
    }

    private String getInsuranceObjectIdParameterValue(HttpServletRequest request) {
        String insuranceObjectId = StringUtil.isEmptyOrNullValue(request.getParameter("ioID")) ? (String)request.getAttribute("ioID") : request.getParameter("ioID");
        if (StringUtil.isEmptyOrNullValue(insuranceObjectId)) {
            log.warn("getInsuranceObjectIdParameterValue - Request parameter value empty");
            insuranceObjectId = (String) request.getSession().getAttribute("ioID");
            if (StringUtil.isEmptyOrNullValue(insuranceObjectId)) {
                log.warn("getInsuranceObjectIdParameterValue - Session parameter value empty");
            } else {
                log.debug("insuranceObjectID: " + insuranceObjectId);
            }
        } else {
            log.debug("insuranceObjectID: " + insuranceObjectId);
        }
        return insuranceObjectId;
    }

    private String getRiskUnitIdParameterValue(HttpServletRequest request, EditForm forma) {
        String riskUnitID = StringUtil.isEmptyOrNullValue(request.getParameter("ruID")) ? (String)request.getAttribute("ruID") : request.getParameter("ruID");
        if (StringUtil.isEmptyOrNullValue(riskUnitID)) {
            log.warn("getRiskUnitIdParameterValue - Request parameter value empty");
            riskUnitID = forma.getCruId();
            if (StringUtil.isEmptyOrNullValue(riskUnitID)) {
                log.warn("getRiskUnitIdParameterValue - Session / form parameter value empty");
            } else {
                log.debug("riskUnitID: " + riskUnitID);
            }
        } else {
            log.debug("riskUnitID: " + riskUnitID);
        }
        return riskUnitID;
    }

    private void addSymbols(long claimID, String ioDescription, AffectedInsuranceObject affio) {
        if (!StringUtil.isEmptyOrNullValue(ioDescription)) {
            com.consisint.acsele.claim.api.Claim claim = com.consisint.acsele.claim.api.Claim.Impl.getInstance(claimID);
            affio = claim.getAffectedInsuranceObject(ioDescription);
            if (affio != null) {
                DynamicData dynamicData = affio.getDynamicData();
                if (dynamicData != null) {
                    DynamicMetaData metaData = dynamicData.getMetaData();
                    if (metaData.getTemplate() != null) {
                        String damageTemplate = metaData.getTemplate().getName();

                        EvaluationContext.add(EvaluationConstants.DAMAGE_TEMPLATE, damageTemplate);
                        EvaluationContext.add(EvaluationConstants.POLICY_PK, claim.getPolicy().getId());
                        EvaluationContext.add(EvaluationConstants.IO_AFFECTED, ioDescription);
                    }
                }
            }

        }
    }

    private List<ValidationDamageClaim> getClaimDamageValidations(ClaimComposer composer, long ioId, List<Integer>validationTypes) throws ApplicationExceptionChecked, RemoteException {
        List<ValidationDamageClaim> validationDamageClaims = null;
        Claim claim = composer.getClaim();
        Long eventClaimId = claim.getEventClaimId();

        validationDamageClaims = ValidationDamageClaimPersister.Impl.getInstance().listValidationsByTypes(eventClaimId, validationTypes);

        if (validationDamageClaims == null) {
            validationDamageClaims = new ArrayList<ValidationDamageClaim>();
        }

        if (validationDamageClaims != null && validationDamageClaims.size() > 0) {
            composer.evaluateDamageValidations(claim, validationDamageClaims, ioId);
        }

        return validationDamageClaims;
    }

}
