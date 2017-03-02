package com.consisint.acsele.workflow.claimInterfaces.actions;

import com.consisint.acsele.DefaultConfigurableObject;
import com.consisint.acsele.util.context.CRMInternalServices;
import com.consisint.acsele.workflow.claimapi.historical.ClaimHistoricalOperationType;
import com.consisint.acsele.template.server.Categorias;
import com.consisint.acsele.template.server.ConfigurableObjectType;
import com.consisint.acsele.template.server.CotType;
import com.consisint.acsele.util.AcseleConf;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.workflow.util.FormWorkflowUtil;
import com.consisint.acsele.workflow.claimapi.ClaimComposerWrapper;
import com.consisint.acsele.workflow.claimapi.ClaimComposer;
import com.consisint.acsele.workflow.claimapi.ClaimSessionUtil;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Hashtable;

/**
 * This class loads the template for the reserve refusal
 * Title: LoadRefuseReserveTemplateAction.java <br>
 * Copyright: (c) 2006 Consis International<br>
 * Company: Consis International<br>
 * @author Consis International (CON)
 * @author Raul De La Rosa (RDN)
 * @author Gorka Siverio (GS)
 * @author Belkys Hern�ndez (BCH)
 * @author Jose Manuel Leon (JML)
 * @author Jonathan Rendon (JR)
 * @version Acsel-e v4.0
 * <br>
 * Changes:<br>
 * <ul>
 *      <li> 2006-02-01 (RDN) Creation</li>
 *      <li> 2006-03-21 (RDN) Corrections </li>
 *      <li> 2006-04-03 (GS-BCH) Categorias becomes Singleton II. </li>
 *      <li> 2006-04-17 (GS)  Brazil III: Coverages can go to Analysis state. </li>
 *      <li> 2006-06-30 (GS)  Changes in Categorias' handling. </li>
 *      <li> 2008-03-10 (RL) Changes to use the redirect. </li>
 *      <li> 2009-06-12 (JML) Added addClaimHistorical </li>
 *      <li> 2009-09-28 (YJA) Eliminate property 'OTRefuseReserve'  </li>
 *      <li> 2011-10-04 (JR) Minor Changes  </li>
 * </ul>
 */

public class LoadRefuseReserveTemplateAction extends GenericAction {

    private static final AcseleLogger log =
            AcseleLogger.getLogger(LoadRefuseReserveTemplateAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException, ServletException {
        super.execute(mapping, form, request, response);
        try {
            StringBuffer urlForward = new StringBuffer();
            request.getSession().removeAttribute("data");


            // The 'OTRefuseReserve' property was been using only here and it had the same value that
            // 'templateclaimdenied' property.  Their use are the same.  'OTRefuseReserve' must be eliminated.

            String templateName = AcseleConf.getProperty(ClaimComposerWrapper.TEMPLATE_CLAIM_DENIED);
            Categorias templates = Categorias.getBean(Categorias.ALL_TEMPLATES_STATE);
            ConfigurableObjectType cot =
                    (ConfigurableObjectType) templates.get(CotType.OTHER, templateName);
            Hashtable dataFormHashtable =
                    DefaultConfigurableObject.loadByDefault(cot).toHashtable();

            urlForward.append("../../claimInterfaces/saveTemplate.do?forwardName=setRefusalStatus");
            String normalReservePk = request.getParameter("claimReservePK");
            dataFormHashtable.put("normalReservePk", normalReservePk);
            dataFormHashtable.put("type", "insert");
            dataFormHashtable.put("configurableObjectType", cot);
            dataFormHashtable.put("urlForward", urlForward.toString());
            dataFormHashtable.put("templateName",templateName);
            dataFormHashtable.put("claimId",getSession().getAttribute("claimId"));

            String claimId = (String) getSession().getAttribute("claimId");

            // llama al servicio para asociarlo
            CRMInternalServices crmServices = CRMInternalServices.Impl.getInstance();
            if(crmServices != null) dataFormHashtable
                    = crmServices.processLoadRefuseReserveAction(claimId,dataFormHashtable);

//            putRequestObject("urlForward", urlForward.toString());
//            putRequestObject("notCloseWindow", "true");
            putRequestObject(request, "templateName",templateName);
            //Sorry for this but it's necesary to show the template, for now I don't know another way
            putSessionObject(request, "parametersform", dataFormHashtable);
            String coverageId = request.getParameter("claimReserveId");
            if (coverageId != null) {
                putSessionObject(request, "coverageId", coverageId);
            }

            ActionForward forward = mapping.findForward("loadTemplate");
            log.debug("forward.getPath()= "+forward.getPath());
            String redirectUrl=forward.getPath()+"&urlForward="+urlForward+"&notCloseWindow=true";
            log.debug("redirectUrl= "+redirectUrl);
            ActionForward redirect = new ActionForward(redirectUrl, false);
            redirect.setName("loadTemplate");
            redirect.setRedirect(forward.getRedirect());

            ClaimComposer composer = ClaimSessionUtil.getSession(request.getSession(),
                    LoadRefuseReserveTemplateAction.class.getName());
            ClaimComposerWrapper.addClaimHistorical(composer.getClaim(), ClaimHistoricalOperationType.REJECT);


            if(crmServices != null) crmServices.processEditClaimObjectAction(composer);

            return redirect;

            //return goTo("loadTemplate");
        } catch (Exception e) {
            log.error("Error", e);
        }
        return goErrorForward(mapping, request);
    }

}
