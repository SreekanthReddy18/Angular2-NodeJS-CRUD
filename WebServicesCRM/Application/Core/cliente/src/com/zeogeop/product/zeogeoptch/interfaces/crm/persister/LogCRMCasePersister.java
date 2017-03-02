package com.consisint.acsele.interseguro.interfaces.crm.persister;

import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMCase;
import com.consisint.acsele.interseguro.interfaces.crm.entity.LogCRMCase;
import com.consisint.acsele.util.BeanFactory;
import com.consisint.acsele.util.error.TechnicalException;
import com.consisint.acsele.util.logging.AcseleLogger;

import java.util.Date;
import java.util.List;

/**
 * Created by Darwin on 24/01/2017.
 */
public interface LogCRMCasePersister {

    List<LogCRMCase> loadByCRMNumber(String crmNumber) throws TechnicalException;

    void save(LogCRMCase crmCase);

    void update(LogCRMCase crmCase);

    void delete(LogCRMCase crmCase);

    int countAll() throws TechnicalException;

    void deleteByCRMCase(CRMCase o);

    class Impl {
        private static final AcseleLogger log = AcseleLogger.getCurrentClassLogger();
        private static final LogCRMCasePersister persister = getBean();

        private static LogCRMCasePersister getBean(){
            try{
                return (LogCRMCasePersister) BeanFactory.getBean(LogCRMCasePersister.class);
            } catch (Throwable e){
                log.error("[CRMCasePersister] ", e);
                return null;
            }
        }

        public static LogCRMCasePersister getInstance() {
            return persister;
        }

        public static void createNew(String nroCaso, Date date, String movementStatus, String jsonIn, String jsonOut) {
            LogCRMCasePersister.Impl.getInstance().save(new LogCRMCase(nroCaso, date, movementStatus, jsonIn, jsonOut));
        }
    }

    List<LogCRMCase> loadByElCRMId(long elCRMId);

}


