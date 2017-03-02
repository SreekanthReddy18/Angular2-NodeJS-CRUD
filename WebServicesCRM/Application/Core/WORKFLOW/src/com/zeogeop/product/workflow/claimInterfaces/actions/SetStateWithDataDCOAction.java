package com.consisint.acsele.workflow.claimInterfaces.actions;

import com.consisint.acsele.DefaultConfigurableObject;
import com.consisint.acsele.UserInfo;
import com.consisint.acsele.cashierstand.report.PaymentOperationOpenItem;
import com.consisint.acsele.cashierstand.util.CashierStandUtil;
import com.consisint.acsele.claim.api.ClaimStatus;
import com.consisint.acsele.claim.api.ProductClaimDocuments;
import com.consisint.acsele.claim.bean.impl.ClaimNormalReserveImpl;
import com.consisint.acsele.claim.service.util.ChangeStateServiceUtil;
import com.consisint.acsele.interfaces.axa.oim.bean.OIMTPClaimEnum;
import com.consisint.acsele.claim.api.ProductClaimDocuments;
import com.consisint.acsele.claim.api.ProductClaimDocumentsList;
import com.consisint.acsele.document.DocumentEngine;
import com.consisint.acsele.document.letter.Letter;
import com.consisint.acsele.interfaces.axa.oim.bean.OIMTPClaimEnum;
import com.consisint.acsele.letters.strategy.LetterGenerator;
import com.consisint.acsele.letters.strategy.LetterStrategy;
import com.consisint.acsele.openapi.claim.PaymentOrderCollection;
import com.consisint.acsele.policy.api.*;
import com.consisint.acsele.policy.server.AgregatedPolicy;
import com.consisint.acsele.policy.server.EvaluatedCoverage;
import com.consisint.acsele.policy.server.beans.ContextOperation;
import com.consisint.acsele.product.server.ClaimsCoverageConfiguration;
import com.consisint.acsele.template.lifecycle.beans.api.EventType;
import com.consisint.acsele.template.server.ConfigurableObjectType;
import com.consisint.acsele.template.server.Propiedad;
import com.consisint.acsele.uaa.OpenItem;
import com.consisint.acsele.uaa.OpenItemImpl;
import com.consisint.acsele.uaa.api.StatusMovement;
import com.consisint.acsele.util.*;
import com.consisint.acsele.util.context.CRMInternalServices;
import com.consisint.acsele.util.error.ApplicationException;
import com.consisint.acsele.util.evaluator.ExpresionEvaluator;
import com.consisint.acsele.util.evaluator.TablaSimbolos;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.workflow.claimInterfaces.forms.Re_eligibilityDateOfCoverageBean;
import com.consisint.acsele.workflow.claimapi.*;
import com.consisint.acsele.workflow.claimapi.historical.ClaimHistoricalMovementType;
import com.consisint.acsele.workflow.claimapi.historical.ClaimHistoricalOperationType;
import com.consisint.acsele.workflow.claimapi.recovery.ClaimUtil;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

import java.io.File;

import com.consisint.acsele.claim.api.ProductClaimDocuments;
import com.consisint.acsele.claim.api.ProductClaimDocumentsList;
import com.consisint.acsele.document.DocumentEngine;
import com.consisint.acsele.document.letter.Letter;
import com.consisint.acsele.letters.strategy.LetterGenerator;
import com.consisint.acsele.util.evaluator.ExpresionEvaluator;
import com.consisint.acsele.util.evaluator.TablaSimbolos;

/**
 * It sets the claim state and DCO info in the session. <p>
 * Title: SetStateWithDataDCOAction.java <br>
 * Copyright: (c) 2003 Consis International<br>
 * Company: Consis International<br>
 *
 * @author Consis International (CON)
 * @author Nelson Crespo (NC)
 * @author Gorka Siverio (GS)
 * @author Reynaldo Urdaneta (RU)
 * @author Sabelia Ruiz (SRC)
 * @version Acsel-e v2.2
 *          <br>
 *          Changes:<br>
 *          <ul>
 *          <li> 2005-01-28 (NC)  Creation
 *          <li> 2006-08-07 (GS)  DefaultConfigurableObject.loadPk() deleted. </li>
 *          <li> 2008-03-18 (LAD) DCO parameter setting fixed </li>
 *          <li> 2010-04-23 (JAC) changes for REQ-393 Code review.</li>
 *          <li> 2010-04-29 (RU) include call of method changePaymentOrderStateToClose of class PaymentOrderCollection.</li>
 *          <li> 2010-08-02 (SRC) include call of method changePaymentOrderStateToOpen of class PaymentOrderCollection</li>
 *          <li> 2011-04-11 (RL)  New getNextIDDCO() calls. </li>
 *          <ul>
 */

public class SetStateWithDataDCOAction extends GenericAction {

    private static final AcseleLogger log = AcseleLogger.getLogger(SetStateWithDataDCOAction.class);

    private Map auxParameters;

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException, ServletException {
        super.execute(mapping, form, request, response);
        int result = executeSetStateWithDataDCO(request);
        if(result==1){
            return goSuccess(mapping, request);
        }else{
            return goErrorForward(mapping, request);
        }
    }


    public int executeSetStateWithDataDCO(HttpServletRequest request) {  //if you modify here please review executeSetStateWithDataDCOforParser
        log.debug("SetStateWithDataDCO.handleRequest");
        ResourceBundle messages = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
        Vector claimsVector = new Vector();
        Long dcoClaimDenied = null;

        try {
            ClaimReserve reserveHist = null;
            Hashtable dataFormHashtable = getDataFormHashtable(request);
            ConfigurableObjectType cot =
                    (ConfigurableObjectType) dataFormHashtable.get("configurableObjectType");
            putSessionObject(request, "cotid", cot.getPK());
            putSessionObject(request, "cotname", cot.getDesc());
            DefaultConfigurableObject rejectClaimDCO = setParameterInDCO(request, cot);
            putSessionObject(request, "dcoid", rejectClaimDCO.getPk());
            dcoClaimDenied = Long.valueOf(getSessionObject(request, "dcoid").toString());
            callSetStateClaim(request, request.getParameter("productRejectMotive"), dcoClaimDenied);
            log.debug("SetStateWithDataDCO ------- request.getParameter('productRejectMotive') : " + request.getParameter("productRejectMotive"));
            String rejectMotive = request.getParameter("productRejectMotive");
            log.debug("SetStateWithDataDCO ------- rejectMotive : " + rejectMotive);
            if (StringUtil.isEmptyOrNullValue(rejectMotive)) rejectMotive = "0";

            List coverages = new ArrayList();
            HashMap coverageInfo = (HashMap) request.getAttribute("coverageInfo");
            RequestUtil.printRequest(request, null);
            Iterator iterCovKeys = coverageInfo.keySet().iterator();
            Claim claim = (Claim) coverageInfo.get("claim");
            Date ocurrenceDate = claim.getOcurrenceDate();

            AgregatedPolicy policy = claim.getAgregatedPolicy();
            ContextOperation contextOperation = ContextOperation.Impl.load(policy.getId(), ocurrenceDate);
            boolean preventPremiumOthersClaim = false;
            /* Buscar otro siniestro para la misma poliza con coberturas marcadas,*/
            ClaimComposerWrapper ccw = new ClaimComposerWrapper();
            claimsVector = ccw.findClaims(String.valueOf(policy.getId()));
            if (claimsVector.size() > 1)
                for (int i = 0; i < claimsVector.size(); i++) {
                    if (!(claim.getClaimNumber().equals(claimsVector.get(i).toString()))) {
                        Claim claimVector = Claim.loadByClaimNumber(claimsVector.get(i).toString());

                        Iterator<ClaimRiskUnit> claimRUList = claimVector.getClaimRiskUnitsList().iterator();
                        ClaimRiskUnit cru;
                        if (claimRUList.hasNext()) {
                            cru = claimRUList.next();
                            Collection<ClaimInsuranceObject> cioList = cru.getClaimInsuranceObjectsList();
                            Iterator<ClaimInsuranceObject> itCio = cioList.iterator();
                            ClaimInsuranceObject cio = null;
                            if (itCio.hasNext()) {
                                cio = itCio.next();
                                Iterator iteratorNR = cio.getNormalReserves().values().iterator();
                                while (iteratorNR.hasNext()) {
                                    ClaimNormalReserve reserve = (ClaimNormalReserve) iteratorNR.next();
                                    reserveHist = reserve;
                                    EvaluatedCoverage ecV = reserve.getEvaluatedCoverage();
                                    ClaimsCoverageConfiguration cccV = ClaimsCoverageConfiguration.find(policy.getProduct().getId(), ecV.getConfiguratedCoverageOA().getId());
                                    if (cccV.isPreventPremium()) preventPremiumOthersClaim = true;
                                }
                            }
                        }

                    }
                }
                /* Buscar otro siniestro para la misma poliza con coberturas marcadas,*/


            Re_eligibilityDateOfCoverageBean re_eligibilityDateOfCoverageBean = null;
            while (iterCovKeys.hasNext()) {
                Object obj = coverageInfo.get(iterCovKeys.next());
                if (obj instanceof ArrayList) {
                    ArrayList coverage = (ArrayList) obj;
                    ClaimNormalReserve normalReserve =
                            (ClaimNormalReserve) coverage.get(0);
                    ClaimsCoverageConfiguration ccc =
                            (ClaimsCoverageConfiguration) coverage.get(1);
                    if (ccc.getReelegibility()) {
                        Date reeligibilityDate = ccc.getReElegibilityDate(ocurrenceDate);
                        re_eligibilityDateOfCoverageBean = new Re_eligibilityDateOfCoverageBean(normalReserve.getDesc(), DateUtil.getDateToShow(reeligibilityDate));
                    } else {
                        re_eligibilityDateOfCoverageBean = new Re_eligibilityDateOfCoverageBean(normalReserve.getDesc(), messages.getString("confirmationChangeState.norelectable"));
                    }
                    coverages.add(re_eligibilityDateOfCoverageBean);

                    if (!preventPremiumOthersClaim) {
                        if (ccc != null) {
                            if (ccc.isPreventPremium()) {
                                policy.setPreventPremiun(0);
                                policy.getObjectManager().update(policy, null);
                                /*OPENITEMS*/
                                long opeId = contextOperation.getId();
                                Collection OpenItems = OpenItemImpl.findByOperationPK(opeId);
                                Iterator iterator = OpenItems.iterator();
                                OpenItem openItem = null;
                                OpenItemImpl openItemImpl = null;
                                PaymentOperationOpenItem paymentOperationOpenItem = null;
                                while (iterator.hasNext()) {
                                    openItemImpl = (OpenItemImpl) iterator.next();
                                    long openItemID = openItemImpl.getOpenItemID();
                                    openItem = CashierStandUtil.findOpenItem(openItemID);
                                    log.debug("SetStateWithDataDCO ------- openItemImpl.getStatus(): " + openItemImpl.getStatus());

                                    if (StatusMovement.PENDING.getValue().equalsIgnoreCase(openItemImpl.getStatus())) {
                                        log.debug("SetStateWithDataDCO ------- openItemImpl.PENDING");
                                        openItemImpl.setStatus(StatusMovement.ACTIVE.getValue());
                                        log.debug("SetStateWithDataDCO ------- openItemImpl.ACTIVE to : " + openItem.getStatus());
                                    }
                                }
                                /*OPENITEMS*/
                            }
                        }
                    }
                }
            }
            putSessionObject(request, "coverages", coverages);
            if (ClaimStatus.CLOSED == claim.getClaimStatus() || ClaimStatus.DENIED == claim.getClaimStatus()) {
                PaymentOrderCollection.changePaymentOrderStateToClose(com.consisint.acsele.openapi.claim.Claim.load(claim.getPk()), claim.getClaimStatus());

                /* GENERAR DOCUMENTOS PARA MOTIVOS DE RECHAZO DE ACUERDO AL PRODUCTO DEL CLAIM */
                /* AGREGAR SIMBOLOS */

                TablaSimbolos symbolsTable = new TablaSimbolos();

                ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
                boolean is_symbolsTable = false;
                Collection<ClaimRiskUnit> crus = claim.getClaimRiskUnitsList();
                for (ClaimRiskUnit cru : crus) {
                    Collection<ClaimInsuranceObject> cios = cru.getClaimInsuranceObjectsList();
                    for (ClaimInsuranceObject cio : cios) {
                        Iterator iteratorNR = cio.getNormalReserves().values().iterator();
                        while (iteratorNR.hasNext()) {
                            ClaimNormalReserve reserve = (ClaimNormalReserve) iteratorNR.next();
                            reserveHist = reserve;
                            symbolsTable = ccw.fillSymbolTableClaim(claim.getPk(), reserve, symbolsTable, Long.parseLong(rejectMotive), dcoClaimDenied);

                            is_symbolsTable = true;
                        }
                    }
                }
                if (!is_symbolsTable) {
                    symbolsTable = ccw.fillSymbolTableClaim(claim.getPk(), symbolsTable, Long.parseLong(rejectMotive));
                }

                //Validar si se Rehabilita la póliza
                ChangeStateServiceUtil.validateRehabilitationPolicy(claim, ccw, policy, symbolsTable);

                Policy policyApi = claim.getPolicy();

                String timeStamp = DateUtil.sdfl.format(Calendar.getInstance().getTime());

                List<ProductClaimDocuments> listProductClaimDocuments = ChangeStateServiceUtil.listProductClaimDocuments(policyApi.getProduct().getId());
                //List<Letter> letterToPdf = new ArrayList<Letter>();
                List<File> list = new ArrayList<File>();
                List<File> listSend = new ArrayList<File>();
                int for_i = 0;
                String objectName = policyApi.getProduct().getName() + "_claim_" + timeStamp;
                for (ProductClaimDocuments pcd : listProductClaimDocuments) {
                    log.debug("doc.getName().getDocument().getName(): " + pcd.getDocument().getName());
                    log.debug("doc.getName().getFormulate(): " + pcd.getFormulate());
                    if (!StringUtil.isEmptyOrNullValue(pcd.getFormulate())) {
                        boolean evalFormulate = evaluator.evaluateLogical(pcd.getFormulate());
                        if (evalFormulate) {
                            DocumentEngine document = (DocumentEngine) pcd.getDocument();
                            Letter letterToSend = Letter.load(Long.valueOf(document.getPk()));
                            List<Letter> letterToPdf = new ArrayList<Letter>();
                            letterToPdf.add(letterToSend);
                            //Letter letterByDate = DocumentEngineHibernatePersister.loadLetterByName(letterToSend.getName());

                            if (letterToSend != null) {
                                LetterStrategy strategy = (LetterStrategy) BeanFactory.getBean(letterToSend.getStrategy());
                                LetterGenerator letterGenerator = LetterGenerator.getInstance(strategy);
                                if (letterToPdf.size() > 0) {
                                    list = letterGenerator.generateLettersForProductClaimDocuments(symbolsTable, claim.getPk(), letterToPdf, objectName, LetterGenerator.FROM_CLAIM, AcseleLabelsConstants.DEFAULT_VALUE, AcseleLabelsConstants.DEFAULT_VALUE);
                                    listSend.add(for_i, list.get(0));
                                    for_i = for_i + 1;
                                }
                            }
                        }
                    }
                }

                putSessionObject(request, "autoletter", listSend.size() > 0 ? "1" : "0");
                //putSessionObject("letterAutomatic", list);
                request.setAttribute("letterAutomatic", listSend);
                putSessionObject(request, "freeText", "0");
                putSessionObject(request, "printLetter", listSend.size() > 0 ? "true" : "false");

            } else {
                PaymentOrderCollection.changePaymentOrderStateToOpen(com.consisint.acsele.openapi.claim.Claim.load(claim.getPk()), claim.getClaimStatus());
            }

            CRMInternalServices crmServices = CRMInternalServices.Impl.getInstance();
            if(crmServices != null) crmServices.processSetStateWithData(claim.getPk());

            ClaimHistoricalMovementType claimHistoricalMovementType = ClaimHistoricalMovementType.CANCELLATION_CLAIN;
            ClaimHistoricalOperationType claimHistoricalOperationType = ClaimHistoricalOperationType.CLOSE;

            if (!cot.getDesc().equals(AcseleConf.getProperty("templateclaimclosed")) && !cot.getDesc().equals(AcseleConf.getProperty("templateclaimdenied"))) {
                claimHistoricalMovementType = ClaimHistoricalMovementType.REOPEN_CLAIN;
                claimHistoricalOperationType = ClaimHistoricalOperationType.REOPEN;
            }

            try {
                ClaimHistorical claimHistorical = new ClaimHistorical();
                claimHistorical.generateHistoricalWithMovement(claim, claimHistoricalOperationType, claimHistoricalMovementType, 0, ClaimUtil.getValidatedLegacyType(reserveHist));
            } catch (Exception e) {

            }

            //Si rejectMotive es distinto a "0" entonces el siniestro se esta rechazando.
            // Solo se debe realizar la inclusion al rechazar el siniestro
            if (!StringUtil.trulyEquals(rejectMotive, NumberUtil.ZERO_STRING)) {
                ChangeStateServiceUtil.includeIO(coverageInfo, rejectClaimDCO, claim);
            }

            //return goSuccess();
            return 1;
        } catch (ApplicationException ae) {
            log.debug("ae.getKeyCode():" + ae.getKeyCode());
            putSessionObject(request, "info", ae.getKeyCode());
            //return goErrorForward();
            return 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        //return goErrorForward();
        return 0;
    }


    private Hashtable getDataFormHashtable(HttpServletRequest request) {
        return (Hashtable) getSessionObject(request, "parametersform");
    }

    /**
     * Set the parameter from request in DCO in order to update
     *
     * @param cot c
     * @return String
     * @throws Exception e
     */
    public DefaultConfigurableObject setParameterInDCO(HttpServletRequest request, ConfigurableObjectType cot) throws Exception {
        DefaultConfigurableObject dco = getDefaultConfigurableObject(request, cot);
        Enumeration iter = cot.getPropiedades().elements();
        while (iter.hasMoreElements()) {
            Propiedad property = (Propiedad) iter.nextElement();
            String input = request.getParameter(property.getDesc());
            log.debug("Setting Parameter In DCO : property = " + property.getDesc() + ", input = " + input);
            input = (input == null) ? "" : input;
            dco.setCriterioInput(property, input);
        }
        updateDCO(request, dco);
        return dco;
    }

    /**
     * Gets the DefaultConfigurableObject object
     *
     * @param cot c
     * @return DefaultConfigurableObject
     * @throws Exception e
     */
    private DefaultConfigurableObject getDefaultConfigurableObject(HttpServletRequest request, ConfigurableObjectType cot)
            throws Exception {
        if (getType(request).equals("insert")) {
            return DefaultConfigurableObject.create(cot);
        } else {
            String idDCO = request.getParameter("idobject");
            return DefaultConfigurableObject.load(cot, Long.valueOf(idDCO));
        }
    }

    /**
     * Execute the update in DCO
     *
     * @param dco The default configurable object
     */
    private void updateDCO(HttpServletRequest request, DefaultConfigurableObject dco) {
        if ("insert".equals(getType(request))) {
            dco.save();
        } else {
            dco.updateEditable();
        }
    }

    /**
     * Get the type
     *
     * @return String
     */
    private String getType(HttpServletRequest request) {
        Hashtable dataFormHashtable = getDataFormHashtable(request);
        return (String) dataFormHashtable.get("type");
    }


    private EventType getEvenType(String inclusionE, Map<String, EventType> eventTypeMap) {
        String[] events;
        EventType eventType = null;
        if (!StringUtil.isEmptyOrNullValue(inclusionE)) {
            events = inclusionE.split(StringUtil.COMMA);
            for (String event : events) {
                eventType = eventTypeMap.get(event);
                if (eventType != null) {
                    return eventType;
                }
            }
        }
        return eventType;
    }
}
