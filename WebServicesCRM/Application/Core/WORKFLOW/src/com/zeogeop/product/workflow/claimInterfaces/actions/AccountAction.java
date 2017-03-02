package com.consisint.acsele.workflow.claimInterfaces.actions;

import com.consisint.acsele.claim.deducedopenitem.model.DeducedOpenitemModel;
import com.consisint.acsele.claim.deducedopenitem.service.DeducedOpenitemService;
import com.consisint.acsele.claim.deducedopenitem.service.impl.DeducedOpenitemServiceImpl;
import com.consisint.acsele.product.server.ClaimsCoverageConfiguration;
import com.consisint.acsele.util.ClientResponse;
import com.consisint.acsele.util.ParameterFinder;
import com.consisint.acsele.util.context.CRMInternalServices;
import com.consisint.acsele.util.error.ApplicationExceptionChecked;
import com.consisint.acsele.util.evaluator.Funciones;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.workflow.claimapi.Claim;
import com.consisint.acsele.workflow.claimapi.ClaimComposer;
import com.consisint.acsele.workflow.claimapi.ClaimComposerWrapper;
import com.consisint.acsele.workflow.claimapi.ClaimSessionUtil;
import com.consisint.acsele.workflow.claimapi.Payment;
import com.consisint.acsele.workflow.claimapi.historical.ClaimHistoricalOperationType;
import com.consisint.acsele.workflow.claimapi.recovery.ClaimUtil;
import com.consisint.acsele.workflow.claimtool.ClaimLanguageHandler;
import com.consisint.acsele.workflow.claimtool.bean.ClaimTotalizeOneByOne;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Version: AcseleV
 * Company: Consis International<br>
 */

public class AccountAction extends GenericAction {

    private static final AcseleLogger log = AcseleLogger.getLogger(AccountAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws IOException, ServletException {
        log.info("AccountAction.execute()");
        super.execute(mapping, form, request, response);
        try {
            String doneby = getDoneBy(request);
            ClaimComposer composer = ClaimSessionUtil.getSession(request.getSession(), AccountAction.class.getName());
            String claimID = composer.getClaim().getPK();
            String coverageDesc = (String) request.getSession().getAttribute("claimcovID");
            request.getSession().removeAttribute("claimcovID");
            String ruID = request.getParameter("ruID");
            String ioDesc = request.getParameter("ioID");
            log.debug("******** Coverage Desc : '" + coverageDesc + "' ");
            log.debug("******** RU ID : '" + ruID + "' " + "  OI ID : '" + ioDesc + "'");
            ClientResponse clientResponse = composer.accountPaymentsOneByOne(claimID, doneby, true, coverageDesc, ruID, ioDesc);






            if (clientResponse.getResult()) {
                ClaimLanguageHandler languageHandler = ClaimLanguageHandler.getInstance((Locale) this.getSessionObject(request, Globals.LOCALE_KEY));
                String claimNumber = (String) clientResponse.getAttribute("claimNumber");
                Integer claimStateParam = (Integer) clientResponse.getAttribute("claimstate");
                String claimDesc = ClaimUtil.getClaimDescription(claimNumber, claimStateParam.intValue(), languageHandler);

                putRequestObject(request, "claimTotalize", clientResponse.getAttribute("claimTotalize"));
                putSessionObject(request, "claimTotalize", clientResponse.getAttribute("claimTotalize"));
                putSessionObject(request, "claimdesc", claimDesc);
                putSessionObject(request, "claimstate", claimStateParam);


                ClaimTotalizeOneByOne claimTotalizeOneByOne = (ClaimTotalizeOneByOne)clientResponse.getAttribute("claimTotalize");

                if (claimTotalizeOneByOne.getNormalReservePendingPayments().size() > 0) {


                    HashMap<String, Object> rp = Funciones.calculateOpenItemsToDiscount(((Payment) (claimTotalizeOneByOne.getNormalReservePendingPayments().get(0))).getPaymentOrder().getFkReserve(), new Long((((Payment) (claimTotalizeOneByOne.getNormalReservePendingPayments().get(0))).getClaimId())));

                   

                    boolean isManualDeduction = (Boolean) rp.get("isManualDeduction");

                    double totalDiscount = 0.0d;
                    boolean appliedOpenItems = false;


                    if (isManualDeduction) {


                     
                        if ((Boolean) rp.get("deducedOpenItemApplied")) {

                            totalDiscount = (Double) rp.get("totalDiscount");

                            appliedOpenItems = true;

                            request.setAttribute("appliedOpenItems", appliedOpenItems);
                            request.setAttribute("totalDiscount", totalDiscount);
                            double totalAmount = new Double(0.0d);

                            for(int i=0 ;i<claimTotalizeOneByOne.getConceptReservePendingPayments().size();i++){
                                totalAmount =  ((Payment)claimTotalizeOneByOne.getConceptReservePendingPayments().get(i)).getAmount();
                            }

                            for(int j=0 ;j<claimTotalizeOneByOne.getNormalReservePendingPayments().size();j++){
                                totalAmount =  ((Payment)claimTotalizeOneByOne.getNormalReservePendingPayments().get(j)).getAmount();
                            }

                            request.setAttribute("totalAmount", totalAmount);
                        } else {

                            request.setAttribute("appliedOpenItems", appliedOpenItems);

                        }


                        ArrayList<DeducedOpenitemModel> deducedOpenitemList = (ArrayList<DeducedOpenitemModel>) rp.get("deducedOpenitemList");

                        updateDeducedOpenItemClaimPaymentId(deducedOpenitemList, ((Payment) (claimTotalizeOneByOne.getNormalReservePendingPayments().get(0))).getPk());


                    } else {

                        request.setAttribute("appliedOpenItems", appliedOpenItems);
                        request.setAttribute("totalDiscount", totalDiscount);


                    }


                }













            }







            putRequestObject(request, "ruID", ruID);
            putRequestObject(request, "ioID", ioDesc);
            putRequestObject(request, "isLinkClaimDisable",request.getSession().getAttribute("isLinkClaimDisable"));
            ClaimComposerWrapper.addClaimHistorical(composer.getClaim(), ClaimHistoricalOperationType.TOTALIZE);
            // llama al servicio para asociarlo
            CRMInternalServices crmServices = CRMInternalServices.Impl.getInstance();
            if(crmServices != null) crmServices.processAccountAction(claimID);

            return goSuccess(mapping, request);
        } catch (ApplicationExceptionChecked aec) {
            log.error("[ApplicationExceptionChecked]", aec);
            return goErrorForward(mapping, request, aec.getKeyCode());
        } catch (Exception ex) {
            log.error("[AccountAction] => Caught an exception.", ex);
            return goErrorForward(mapping, request, "claim.errorLoadTotalize");
        }
    }




    /**
     *
     * Updates deduced openItems with the claimPaymentId
     *
     * @param deducedOpenitemList
     * @param pk
     */
    private void updateDeducedOpenItemClaimPaymentId(ArrayList<DeducedOpenitemModel> deducedOpenitemList, long claimPaymentId) {

        DeducedOpenitemService deducedOpenitemService = new DeducedOpenitemServiceImpl();

        for (DeducedOpenitemModel deducedOpenItem : deducedOpenitemList) {

            deducedOpenItem.setClaimPaymentId(claimPaymentId);

            deducedOpenitemService.save(deducedOpenItem);


        }

    }


}

