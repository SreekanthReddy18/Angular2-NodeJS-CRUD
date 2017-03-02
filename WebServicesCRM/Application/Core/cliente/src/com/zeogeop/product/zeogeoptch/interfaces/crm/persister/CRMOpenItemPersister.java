package com.consisint.acsele.interseguro.interfaces.crm.persister;

import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMOpenItem;
import com.consisint.acsele.util.BeanFactory;
import com.consisint.acsele.util.error.TechnicalException;
import com.consisint.acsele.util.logging.AcseleLogger;

import java.util.List;

/**
 * Created by Darwin on 24/01/2017.
 */
public interface CRMOpenItemPersister {

    List<CRMOpenItem> loadByCRMNumber(String crmNumber) throws TechnicalException;

    void save(CRMOpenItem crmCase);

    void update(CRMOpenItem crmCase);

    void delete(CRMOpenItem crmCase);

    int countAll() throws TechnicalException;

    class Impl {
        private static final AcseleLogger log = AcseleLogger.getCurrentClassLogger();
        private static final CRMOpenItemPersister persister = getBean();

        private static CRMOpenItemPersister getBean(){
            try{
                return (CRMOpenItemPersister) BeanFactory.getBean(CRMOpenItemPersister.class);
            } catch (Throwable e){
                log.error("[CRMOpenItemPersister] ", e);
                return null;
            }
        }

        public static CRMOpenItemPersister getInstance() {
            return persister;
        }

    }

    CRMOpenItem loadByOpmId(long opmId);

}


