package com.consisint.acsele.workflow.claimInterfaces.actions;

import com.consisint.acsele.util.ClientRequest;
import com.consisint.acsele.util.ClientResponse;
import com.consisint.acsele.util.context.CRMInternalServices;
import com.consisint.acsele.util.error.ApplicationException;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.workflow.claimapi.Claim;
import com.consisint.acsele.workflow.claimapi.ClaimComposer;
import com.consisint.acsele.workflow.claimapi.ClaimSessionUtil;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lmarin on 2/2/17.
 */
public class ClientCrmAction extends GenericAction {
    private static final AcseleLogger log = AcseleLogger.getLogger(ClientCrmAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException, ServletException {

        super.execute(mapping, form, request, response);

        String typeAssociation      = request.getParameter("typeAssociation");
        String groupName1      = request.getParameter("groupName1");
        String groupName2      = request.getParameter("groupName2");
        String formName        = request.getParameter("formName");
        String plantilla        = request.getParameter("plantilla");

        long policyId = ((Claim) request.getSession().getAttribute("Claim")).getPolicy().getPolicyPk();

        // llama al servicio para asociarlo
        CRMInternalServices crmServices = CRMInternalServices.Impl.getInstance();
        if(crmServices != null) request = crmServices.processExecuteCrm(policyId,request);

        request.setAttribute("typeAssociation", typeAssociation);
        request.setAttribute("groupName1", groupName1);
        request.setAttribute("groupName2", groupName2);
        request.setAttribute("formName", formName);
        request.setAttribute("plantilla",plantilla);

        return mapping.findForward("success");

    }

    /**
     * Process the execute method of ClaimComposer
     * @param request
     * @return ClientResponse
     */
    public static void updateCrm(String crmNumber, String operationId, String associationType) {
        // llama al servicio para asociarlo
        CRMInternalServices crmServices = CRMInternalServices.Impl.getInstance();
        if(crmServices != null) crmServices.processUpdateCrm(crmNumber, operationId, associationType);
    }

}
