package com.consisint.acsele.workflow.claimInterfaces.actions;

import com.consisint.acsele.claim.api.ClaimDeclaration;
import com.consisint.acsele.persistent.persisters.ValidationDamageClaimPersister;
import com.consisint.acsele.product.applet.ValidationDamageClaim;
import com.consisint.acsele.util.error.ApplicationException;
import com.consisint.acsele.util.evaluator.EvaluationConstants;
import com.consisint.acsele.util.evaluator.ExpresionEvaluator;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.util.security.ParseSecurityConditionException;
import com.consisint.acsele.util.security.SecurityException;
import com.consisint.acsele.workflow.claimapi.Claim;
import com.consisint.acsele.template.server.ClaimType;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class process the load action requested by the servlet <p>
 * Title: LoadClaimAction.java<br>
 * Copyright: (c) 2003 Consis International<br>
 * Company: Consis International<br>
 */
public class LoadClaimAction extends GenericAction {

    private static final AcseleLogger log = AcseleLogger.getLogger(LoadClaimAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException, ServletException {
        super.execute(mapping, form, request, response);

        try {
            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();
            evaluator.addSymbol(EvaluationConstants.CRMCase, "1000", 0.0, true);
            String productDescription = request.getParameter("productDescription");
            String fromWorkflow = request.getParameter("fromWF");
            String fromVaadin = request.getParameter("way");
            if (fromVaadin == null || !fromVaadin.equals("vaadin")) {
                if (fromWorkflow == null || !fromWorkflow.equals("true")) {
                    if (productDescription == null || productDescription.trim().isEmpty()) { // input hidden 'productDescription' assigned in js (validateRadio function)
                        String parameter = request.getParameter("claimId") == null ? request.getParameter("idpolicy") :
                                request.getParameter("claimId");
                        request.setAttribute("declarationPk", parameter);
                        return mapping.findForward("loadDeclaration");
                    }
                }
            }
            removeAttributesPrevious(request);
            processRequest(request);
            Claim claim = (Claim) getSessionObject(request, "Claim");
            request.setAttribute("template", claim.getClaimEvent().getDesc());

            List<ValidationDamageClaim> validations = getValidation(request);
            if(validations != null && validations.size() != 0){
                putSessionObject(request, "ClaimDamageValidationsCoverage",validations);
                request.setAttribute("ClaimDamageValidationsCoverage", validations);
            }

            if(request.getParameter("onlyBenefit") != null){
                request.setAttribute("onlyBenefit", request.getParameter("onlyBenefit"));
            }

            return goSuccess(mapping, request);

        } catch (SecurityException e) {
            log.error(e);
            putSessionObject(request, "errorMessage", e.getMessage());
            putSessionObject(request, "requestHandlerClassName", this.getClass().getName());
            putSessionObject(request, "action", "securityExceptionPage");//todo cambiarlo
        } catch (ApplicationException e) {
            log.debug("74**** fallo validacion. .  .   ");
            putSessionObject(request, "errorMessage", e.getMessage());
            putSessionObject(request, "requestHandlerClassName", this.getClass().getName());
            log.warn(" Error loading claim. The premium is not paid " , e);
            return goErrorForward(mapping, request);    //goSuccess();
        } catch (ParseSecurityConditionException e) {
            log.error(e);
            putSessionObject(request, "errorMessage", e.getMessage());
            putSessionObject(request, "requestHandlerClassName", this.getClass().getName());
            putSessionObject(request, "action", "securityExceptionPage");//todo cambiarlo
        } catch (Exception ex) {
            log.error(ex);
        }
        return goErrorForward(mapping, request);
    }

    private List<ValidationDamageClaim> getValidation(HttpServletRequest request) {
        List<ValidationDamageClaim> validationDamageClaims = null;

        long claimId = 0;
        try{
           claimId = Long.parseLong(request.getParameter("claimId"));
        } catch (NumberFormatException e) {
            log.debug(e);
        }
        if(claimId != 0){
        com.consisint.acsele.claim.api.Claim claim = com.consisint.acsele.claim.api.Claim.Impl.getInstance(claimId);
        long eventClaimId = claim.getClaimEvent().getId();
        com.consisint.acsele.product.server.persistent.ClaimEventPersister persister = com.consisint.acsele.product.server.persistent.ClaimEventPersister.Impl.getInstance();
        com.consisint.acsele.product.server.EventClaim eventClaim = persister.load(eventClaimId);

            if(eventClaimId != 0){
                 long plantillaId = 0;

                 for(com.consisint.acsele.template.api.Template templ : eventClaim.getClaimDamages()){
                    if(((ClaimType) templ).getEventClaim().getId() == eventClaimId){
                          plantillaId = templ.getId();
                     }
                }

                 validationDamageClaims = ValidationDamageClaimPersister.Impl.getInstance().listValidations(eventClaimId, plantillaId);
            }else {
                validationDamageClaims = new ArrayList<ValidationDamageClaim>();
            }
        }

        if(validationDamageClaims == null){
            validationDamageClaims = new ArrayList<ValidationDamageClaim>();
        }
        return validationDamageClaims;
    }

    protected void processRequest(HttpServletRequest request) throws Exception {
        String claimId = request.getParameter("claimId") == null ? request.getParameter("idpolicy") :
                request.getParameter("claimId");
        log.debug(" getParameter(\"claimId\") = " + request.getParameter("claimId"));
        log.debug(" getParameter(\"idpolicy\") = " + request.getParameter("idpolicy"));
        log.debug(" claimId = " + claimId);
        if(claimId!=null) {
            findClaim(request, claimId);
        }
    }
}
