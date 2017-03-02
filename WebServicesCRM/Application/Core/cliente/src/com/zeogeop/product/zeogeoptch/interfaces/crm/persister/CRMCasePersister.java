package com.consisint.acsele.interseguro.interfaces.crm.persister;

import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMCase;
import com.consisint.acsele.util.BeanFactory;
import com.consisint.acsele.util.error.TechnicalException;
import com.consisint.acsele.util.logging.AcseleLogger;

import java.util.List;

/**
 * Created by Darwin on 24/01/2017.
 */
public interface CRMCasePersister {

    CRMCase loadByCRMNumber(String crmNumber) throws TechnicalException;

    void save(CRMCase crmCase);

    void update(CRMCase crmCase);

    void delete(CRMCase crmCase);

    int countAll() throws TechnicalException;

    class Impl {
        private static final AcseleLogger log = AcseleLogger.getCurrentClassLogger();
        private static final CRMCasePersister persister = getBean();

        private static CRMCasePersister getBean(){
            try{
                return (CRMCasePersister) BeanFactory.getBean(CRMCasePersister.class);
            } catch (Throwable e){
                log.error("[CRMCasePersister] ", e);
                return null;
            }
        }

        public static CRMCasePersister getInstance() {
            return persister;
        }

        public static CRMCase createNew(final String crmNumber, final Long policyId, final String policyNumber, final String mediumAnswer, final Integer status) {
            CRMCase casoCrm = new CRMCase(crmNumber, policyId, policyNumber, mediumAnswer, status);
            getInstance().save(casoCrm);
            return casoCrm;
        }

        public static void update(CRMCase casoCRM) {
            getInstance().update(casoCRM);
        }

    }

    List<CRMCase> loadByIdPolicy(long idPolicy);

    List<CRMCase> loadByIdPolicyAssign(long idPolicy);

    CRMCase loadByClaimId (long claimId);

    List<CRMCase> loadAll() throws TechnicalException;

    List<CRMCase> loadByIdPolicyAsociated(long idPolicy);

}


