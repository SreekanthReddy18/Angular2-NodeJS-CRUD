package com.consisint.acsele.interseguro.interfaces.crm.persister.hibernate_persister;

import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMCase;
import com.consisint.acsele.interseguro.interfaces.crm.entity.LogCRMCase;
import com.consisint.acsele.interseguro.interfaces.crm.persister.LogCRMCasePersister;
import com.consisint.acsele.persistence.hibernate.AcseleHibernateSessionProvider;
import com.consisint.acsele.persistent.IDDBFactory;
import com.consisint.acsele.util.StringUtil;
import com.consisint.acsele.util.error.Exceptions;
import com.consisint.acsele.util.error.TechnicalException;
import com.consisint.acsele.util.logging.AcseleLogger;
import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ext.dpalma on 24/01/2017.
 */
public class LogCRMCaseHibernatePersister implements LogCRMCasePersister {
    private static final AcseleLogger log = AcseleLogger.getCurrentClassLogger();
    //private boolean isCaching;

    private LogCRMCaseHibernatePersister() {
    }

    @Override
    public List<LogCRMCase> loadByElCRMId(long elCRMId) throws TechnicalException {
        if (elCRMId <= 0) throw new TechnicalException(Exceptions.HSErrorLoadingObject, new Exception("Invalid Value - Id"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            return (List<LogCRMCase>) session.load(LogCRMCase.class, elCRMId);
        } catch (ObjectNotFoundException e) {
            return null;
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingObject, e);
        } catch (Exception e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingObject, e);
        }
    }

    @Override
    public List<LogCRMCase> loadByCRMNumber(String crmNumber) throws TechnicalException {
        try {
//            return  (LogCRMCase) AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession().load(LogCRMCase.class, crmNumber);
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            Criteria crit = session.createCriteria(LogCRMCase.class);
            crit.add(Expression.eq("crmNumber", crmNumber));
            return new ArrayList<LogCRMCase>(crit.list());
        } catch (ObjectNotFoundException e) {
            return null;
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingObject, e);
        } catch (Exception e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingObject, e);
        }
    }

    /*@Override
    public CRMCase loadByPolicyId(int policyId) throws TechnicalException {
        try {
            Session session = HibernateUtil.getSession();
            Criteria crit = session.createCriteria(InterfaceSystem.class);
            crit.add(Expression.eq("policyId", policyId));
            return (CRMCase) crit.uniqueResult();
        } catch (ObjectNotFoundException e) {
            return null;
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingObject, e);
        } catch (Exception e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingObject, e);
        }
    }*/

    @Override
    public void save(LogCRMCase logCRMCase){
        if (logCRMCase == null)  throw new TechnicalException(Exceptions.HSErrorSavingObject, new Exception("Invalid Value CRM IS NULL"));
        try {
            logCRMCase.setElCRMId(IDDBFactory.getNextIDL("EXT_CRMCASE"));
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            session.save(logCRMCase);
            session.flush();
            session.evict(logCRMCase);
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorSavingObject, e);}
    }

    public void update(LogCRMCase logCRMCase) throws TechnicalException {
        if (logCRMCase == null) {
            throw new TechnicalException(Exceptions.HSErrorSavingObject,
                    new Exception("Invalid Value CRM IS NULL"));
        }
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            session.update(logCRMCase);
            session.flush();
            session.evict(logCRMCase);
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorSavingObject, e);}
    }
    //
    public void delete(LogCRMCase logCRMCase) throws TechnicalException {
        if (logCRMCase == null || StringUtil.isEmpty(logCRMCase.getCrmNumber())) throw new TechnicalException(Exceptions.HSErrorDeletingObject, new Exception("Invalid Value CRM IS NULL"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            session.delete(logCRMCase);
            session.flush();
            session.evict(logCRMCase);
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorDeletingObject, e);
        }
    }

    @Override
    public int countAll() throws TechnicalException {
        return 0;
    }

    @Override
    public void deleteByCRMCase(CRMCase o) {
        try {
            final List<LogCRMCase> logCRMCases = loadByCRMNumber(o.getCrmNumber());
            for (LogCRMCase logCRMCase : logCRMCases) {
                delete(logCRMCase);
            }
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
    }

}
