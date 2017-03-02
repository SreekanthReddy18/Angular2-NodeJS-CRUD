package com.consisint.acsele.util.context;

import com.consisint.acsele.document.letter.Letter;
import com.consisint.acsele.uaa.OpenItem;
import com.consisint.acsele.util.BeanFactory;
import com.consisint.acsele.workflow.claimapi.Claim;
import com.consisint.acsele.workflow.claimapi.ClaimComposer;

import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;

/**
 * Created by ext.dpalma on 22/02/2017.
 */
public interface CRMInternalServices {
    String processClaimComposer(Claim claim, OpenItem openItem);

    String processThirdParty(String claimId);

    void processThirdPartyPublish(String claimId, Letter letterToSend);

    void processEditClaimObjectAction(ClaimComposer composer);

    Hashtable processLoadRefuseReserveAction(String claimId, Hashtable dataFormHashtable);

    void processAccountAction(String claimId);

    void processSetStateWithData(String claimId);

    void processUpdateCrm(String crmNumber, String operationId, String associationType);

    HttpServletRequest processExecuteCrm(long policyId, HttpServletRequest request);

    String processClientCRMUpdateAction(String crmNumber, String associationType, Long claimID, Long coverageId);

    class Impl{
        public static CRMInternalServices getInstance(){
            try {
                CRMInternalServices bean = (CRMInternalServices) BeanFactory.getBean(CRMInternalServices.class);
                return bean;
            } catch (Throwable e) {
                return null;
            }
        }
    }
}
