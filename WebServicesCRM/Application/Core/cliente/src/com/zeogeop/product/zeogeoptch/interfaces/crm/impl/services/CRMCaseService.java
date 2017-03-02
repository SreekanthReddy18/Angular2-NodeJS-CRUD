package com.consisint.acsele.interseguro.interfaces.crm.impl.services;

import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMCase;
import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMOpenItem;
import com.consisint.acsele.interseguro.interfaces.crm.entity.PK;
import com.consisint.acsele.interseguro.interfaces.crm.persister.CRMCasePersister;
import com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.enums.StatusAssociationTypeCRM;
import com.consisint.acsele.product.server.HistoryDocumentGenerated;
import com.consisint.acsele.util.BeanFactory;
import com.consisint.acsele.util.error.TechnicalException;
import com.consisint.acsele.util.logging.AcseleLogger;

/**
 * Created by ext.dpalma on 25/01/2017.
 */
public interface CRMCaseService {

    CRMCasePersister getCRMCasePersister();

    CRMCase load(CRMCase crmCase) throws TechnicalException;

    String create(CRMCase crmCase) throws TechnicalException;

    String update(CRMCase crmCase) throws TechnicalException;

    String delete(CRMCase crmCase) throws TechnicalException;

    String sendInfoToCRM(CRMCase crmCase);

    class Impl {
        private static final AcseleLogger log = AcseleLogger.getCurrentClassLogger();
        private static CRMCaseService instance = getBean();

        private synchronized static CRMCaseService getBean(){
            try{
                return(CRMCaseService) BeanFactory.getBean(CRMCaseService.class);
            } catch (Throwable e){
                log.error("[CRMCasePersister] ", e);
                return null;
            }
        }

        public static CRMCaseService getInstance() {
            return instance == null? instance = getBean() : instance;
        }

        public static CRMCase loadByCRMNumber(String crmNumber) {
            return instance.getCRMCasePersister().loadByCRMNumber(crmNumber);
        }

        public static CRMCase createNew(String numeroCaso, Long idPoliza, String numeroPoliza, String medioRespuesta, int status) {
            return CRMCasePersister.Impl.createNew(numeroCaso, idPoliza, numeroPoliza, medioRespuesta, status);
        }

        public static String sendInfoToCRM(CRMCase crmCaseMovement) {
            return instance.sendInfoToCRM(crmCaseMovement);
        }

        public static String updateParcial(String crmNumber, String associationType, Long claimId, Long coverageId) {
            String out = null;
            try {
                CRMCasePersister persister = instance.getCRMCasePersister();
                CRMCase crmCase = persister.loadByCRMNumber(crmNumber);
                if(crmCase.getAssociationType() != null) throw new Exception("No se puede reasociar un tipo a un CasoCRM ya sociado");
                crmCase.setAssociationType(StatusAssociationTypeCRM.valueOf(associationType).getValue());
                if(claimId > 0) crmCase.setClaimId(claimId);
                if(coverageId > 0) crmCase.setCcvId(coverageId);
                persister.update(crmCase);
            } catch (Exception e) {
                out = e.getMessage();
                e.printStackTrace();
            }
            return out;
        }

        public static String updateLetter(String crmNumber, Long letterId) {
            String out = null;
            try {
                CRMCasePersister persister = instance.getCRMCasePersister();
                CRMCase crmCase = persister.loadByCRMNumber(crmNumber);
                HistoryDocumentGenerated letterHistory = new HistoryDocumentGenerated();
                if(letterId > 0) crmCase.setHltId(letterId);
                persister.update(crmCase);
            } catch (Exception e) {
                out = e.getMessage();
                e.printStackTrace();
            }
            return out;
        }

        public static String updateOpenItem(String crmNumber, Long openItemId) {
            String out = null;
            try {
                CRMCasePersister persister = instance.getCRMCasePersister();
                CRMCase crmCase = persister.loadByCRMNumber(crmNumber);
                CRMOpenItem openItem = new CRMOpenItem(new PK(crmCase, openItemId));
                crmCase.getCrmOpenItems().add(openItem);
                persister.update(crmCase);
            } catch (Exception e) {
                out = e.getMessage();
                e.printStackTrace();
            }
            return out;
        }
    }
}
