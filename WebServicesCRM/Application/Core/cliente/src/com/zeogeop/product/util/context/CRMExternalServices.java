package com.consisint.acsele.util.context;

import com.consisint.acsele.ClientInfo;
import com.consisint.acsele.CriteriaDCO;
import com.consisint.acsele.DefaultConfigurableObjectManager;
import com.consisint.acsele.RelationalOperator;
import com.consisint.acsele.document.DocumentHistoryImpl;
import com.consisint.acsele.document.letter.Letter;
import com.consisint.acsele.document.persister.DocumentHistoryPersister;
import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMCase;
import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMOpenItem;
import com.consisint.acsele.interseguro.interfaces.crm.impl.CRMCaseServiceImpl;
import com.consisint.acsele.interseguro.interfaces.crm.persister.CRMCasePersister;
import com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.workers.EventNotificationWorkerCRMClaim;
import com.consisint.acsele.template.api.Template;
import com.consisint.acsele.template.server.ConfigurableObjectType;
import com.consisint.acsele.uaa.OpenItem;
import com.consisint.acsele.util.AcseleConf;
import com.consisint.acsele.util.error.ApplicationExceptionChecked;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.workflow.claimapi.Claim;
import com.consisint.acsele.workflow.claimapi.ClaimComposer;

import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by ext.dpalma on 22/02/2017.
 */
public class CRMExternalServices implements CRMInternalServices {
    private static final AcseleLogger log = AcseleLogger.getLogger(CRMExternalServices.class);

    @Override
    public String processClaimComposer(Claim claim, OpenItem openItem) {
        String messageCRM = null;
        if(claim.getPk()!=null && ClientInfo.isClientRunning("Interseguro")){
            CRMCase crmConsult = CRMCasePersister.Impl.getInstance().loadByClaimId(Long.parseLong(claim.getPk()));
            if(crmConsult!=null){
                CRMCaseServiceImpl.Impl.updateOpenItem(crmConsult.getCrmNumber(),openItem.getOpenItemID());
                CRMCase crmPublish = CRMCasePersister.Impl.getInstance().loadByClaimId(Long.parseLong(claim.getPk()));
                CRMCaseServiceImpl.Impl.getInstance().sendInfoToCRM(crmPublish);
                messageCRM = EventNotificationWorkerCRMClaim.getMessageResponse();
            }
        }
        return messageCRM;
    }

    @Override
    public String processThirdParty(String claimID) {
        CRMCase crmPublish = new CRMCase();
        String crmNumber = "";
        if(claimID!=null && ClientInfo.isClientRunning("Interseguro")){
            crmPublish = CRMCasePersister.Impl.getInstance().loadByClaimId(Long.parseLong(claimID));
            if(crmPublish!=null){
                crmNumber = crmPublish.getCrmNumber();
            }
        }
        return crmNumber;
    }


    public void processSetStateWithData(String claimID) {
        if(claimID!=null && ClientInfo.isClientRunning("Interseguro")){
            CRMCase crmConsult = CRMCasePersister.Impl.getInstance().loadByClaimId(Long.parseLong(claimID));
            if(crmConsult!=null){
                CRMCaseServiceImpl.Impl.getInstance().sendInfoToCRM(crmConsult);
            }
        }
    }

    public void processUpdateCrm(String crmNumber, String operationId, String associationType) {
        CRMCase crm = new CRMCase();
        crm.setCrmNumber(crmNumber);
        crm.setAssociationType(Integer.valueOf(associationType));
        crm = CRMCasePersister.Impl.getInstance().loadByCRMNumber(crm.getCrmNumber());

        if (associationType.equals("1")) {
            Set<CRMOpenItem> openIemList = new HashSet<CRMOpenItem>();
            CRMOpenItem openitem = new CRMOpenItem();
            openitem.getPk().setCrmCase(crm);
            openitem.getPk().setOpmId(Long.valueOf(operationId));
            openIemList.add(openitem);
            crm.setCrmOpenItems(openIemList);
        } else if (associationType.equals("2")) {
            crm.setClaimId(Long.valueOf(operationId));
        } else if (associationType.equals("3")) {
            crm.setCcvId(Long.valueOf(operationId));
        } else if (associationType.equals("4")) {
            crm.setHltId(Long.valueOf(operationId));
        }
        // actualizando el crm
        CRMCasePersister.Impl.getInstance().update(crm);
    }

    @Override
    public HttpServletRequest processExecuteCrm(long policyId, HttpServletRequest request){
        List<CRMCase> crmCase = CRMCasePersister.Impl.getInstance().loadByIdPolicyAssign(policyId); // Manejar excepcion cuando ya exista una asociacion .....
        List<CRMCase> crmCaseAsociated = CRMCasePersister.Impl.getInstance().loadByIdPolicyAsociated(policyId); // Manejar excepcion cuando ya exista una asociacion .....
        request.setAttribute("crmList",crmCase);
        request.setAttribute("crmCaseAsociated",crmCaseAsociated);

        return request;
    }


    @Override
    public String processClientCRMUpdateAction(String crmNumber, String associationType, Long claimID, Long coverageId) {
        return CRMCaseServiceImpl.Impl.updateParcial(crmNumber, associationType, claimID, coverageId);
    }

    @Override
    public void processThirdPartyPublish(String claimID, Letter letterToSend) {
        CRMCase crmPublish = new CRMCase();
        String crmNumber = "";
        if(claimID!=null && ClientInfo.isClientRunning("Interseguro")){
            crmPublish = CRMCasePersister.Impl.getInstance().loadByClaimId(Long.parseLong(claimID));
            if(crmPublish!=null){
                crmNumber = crmPublish.getCrmNumber();
            }
        }

        if (crmPublish != null) {
            List<DocumentHistoryImpl> letterHistoryDocuments = DocumentHistoryPersister.Impl.loadByLetterIdCRM(letterToSend.getPk());
            letterHistoryDocuments.listIterator();
            long sdocId = letterHistoryDocuments.get(0).getDocPk();
            long htlId = letterHistoryDocuments.get(0).getPk();
            if (sdocId == letterToSend.getPk() && ClientInfo.isClientRunning("Interseguro")) {
                if (isValidLetterCRM(letterToSend.getName())) {
                    CRMCaseServiceImpl.Impl.updateLetter(crmNumber, htlId);
                    CRMCaseServiceImpl.Impl.getInstance().sendInfoToCRM(crmPublish);
                }
            }
        }
    }

    @Override
    public void processAccountAction(String claimID) {
        if (claimID != null && ClientInfo.isClientRunning("Interseguro")) {
            CRMCase crmPublish = CRMCasePersister.Impl.getInstance().loadByClaimId(Long.parseLong(claimID));
            if (crmPublish != null) {
                CRMCaseServiceImpl.Impl.getInstance().sendInfoToCRM(crmPublish);
            }
        }
    }

    @Override
    public void processEditClaimObjectAction(ClaimComposer composer) {
        try {
            if(composer.getClaim()!=null && ClientInfo.isClientRunning("Interseguro")){
                CRMCase crmPublish = CRMCasePersister.Impl.getInstance().loadByClaimId(composer.getClaim().getId());
                if(crmPublish!=null){
                    CRMCaseServiceImpl.Impl.getInstance().sendInfoToCRM(crmPublish);
                }
            }
        } catch (ApplicationExceptionChecked applicationExceptionChecked) {
            log.error("[ApplicationExceptionChecked]", applicationExceptionChecked);
        } catch (RemoteException e) {
            log.error("[RemoteException]", e);
        }
    }


    @Override
    public Hashtable processLoadRefuseReserveAction(String claimID, Hashtable dataFormHashtable) {
        if(claimID!=null && ClientInfo.isClientRunning("Interseguro")) {
            com.consisint.acsele.openapi.claim.Claim claim = com.consisint.acsele.openapi.claim.Claim.findByPk(claimID);
            List<CRMCase> lCrmCase = CRMCasePersister.Impl.getInstance().loadByIdPolicy(Long.parseLong(claim.getPolicyId()));
            dataFormHashtable.put("lCrmCase", lCrmCase);
        }
        return dataFormHashtable;
    }


    private boolean isValidLetterCRM(String letter){
        if(letter!=null){
            if(!existInTDLetter(letter)) return false;
        }else{
            return false;
        }
        return true;
    }
    public boolean existInTDLetter(String letterName){
        List<CriteriaDCO> criteria = new LinkedList<CriteriaDCO>(Arrays.asList(new CriteriaDCO[]{new CriteriaDCO(AcseleConf.getProperty("letterCRMList"), RelationalOperator.EQ, letterName, true)}));
        Template cot = ConfigurableObjectType.Impl.load("TDEXTLETTERCASOCRM");
        List<com.consisint.acsele.DefaultConfigurableObject> dcos = DefaultConfigurableObjectManager.loadAllByCOT(cot, criteria);
        if(dcos.size() == 1) return true;
        return false;
    }
}
