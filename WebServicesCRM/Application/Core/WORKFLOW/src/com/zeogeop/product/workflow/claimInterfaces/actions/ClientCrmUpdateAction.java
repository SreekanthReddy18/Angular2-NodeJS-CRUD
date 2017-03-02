package com.consisint.acsele.workflow.claimInterfaces.actions;

import com.consisint.acsele.util.context.CRMInternalServices;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.workflow.claimInterfaces.forms.SearchBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by lmarin on 2/2/17.
 */
public class ClientCrmUpdateAction extends GenericAction {
    private static final AcseleLogger log = AcseleLogger.getLogger(ClientCrmUpdateAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException, ServletException {

        super.execute(mapping, form, request, response);
        String associationType      = request.getParameter("typeAssociation");
        String crmNumber      = request.getParameterValues("listCRM")[1];
        request.getSession().setAttribute("listCRM", crmNumber);
        Long claimID = 0L;
        Long coverageId = 0L;
        if(request.getSession().getAttribute("claimId")!=null){
            claimID = Long.parseLong(String.valueOf(request.getSession().getAttribute("claimId")));
        }
        if(((SearchBean)((Vector)request.getSession().getAttribute("covAffecteds")).get(0)).getEvaluatedCoverage().getConfiguratedCoverage().getPk()!=null){
            coverageId = Long.parseLong(String.valueOf(((SearchBean)((Vector)request.getSession().getAttribute("covAffecteds")).get(0)).getEvaluatedCoverage().getConfiguratedCoverage().getPk()));
        }
        String error = null;
        // llama al servicio para asociarlo
        CRMInternalServices crmServices = CRMInternalServices.Impl.getInstance();
        if(crmServices != null) error = crmServices.processClientCRMUpdateAction(crmNumber, associationType, claimID, coverageId);

        if(error != null){
            response.getWriter().write(error);
        }

        return null;
    }


}
