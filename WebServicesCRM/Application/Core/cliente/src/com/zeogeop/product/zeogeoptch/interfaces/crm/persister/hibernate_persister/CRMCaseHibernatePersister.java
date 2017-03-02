package com.consisint.acsele.interseguro.interfaces.crm.persister.hibernate_persister;

import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMCase;
import com.consisint.acsele.interseguro.interfaces.crm.persister.CRMCasePersister;
import com.consisint.acsele.persistence.hibernate.AcseleHibernateSessionProvider;
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
public class CRMCaseHibernatePersister implements CRMCasePersister {
    private static final AcseleLogger log = AcseleLogger.getCurrentClassLogger();

    private CRMCaseHibernatePersister() {
    }

    @Override
    public List<CRMCase> loadByIdPolicy(long idPolicy) throws TechnicalException {
        if (idPolicy <= 0) throw new TechnicalException(Exceptions.HSErrorLoadingObject, new Exception("Invalid Value - Id"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            Criteria crit = session.createCriteria(CRMCase.class);
            crit.add(Expression.eq("policyId", idPolicy));
            return new ArrayList<CRMCase>(crit.list());
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
    public List<CRMCase> loadByIdPolicyAssign(long idPolicy) throws TechnicalException {
        if (idPolicy <= 0) throw new TechnicalException(Exceptions.HSErrorLoadingObject, new Exception("Invalid Value - Id"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            Criteria crit = session.createCriteria(CRMCase.class);
            crit.add(Expression.eq("policyId", idPolicy));
            crit.add(Expression.isNull("associationType"));
            return new ArrayList<CRMCase>(crit.list());
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

    public List<CRMCase> loadByIdPolicyAsociated(long idPolicy) throws TechnicalException {
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            Criteria crit = session.createCriteria(CRMCase.class);
            crit.add(Expression.eq("policyId", idPolicy));
            crit.add(Expression.isNotNull("associationType"));
            return new ArrayList<CRMCase>(crit.list());
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

    public CRMCase loadByClaimId(long claimId) throws TechnicalException {
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            Criteria crit = session.createCriteria(CRMCase.class);
            crit.add(Expression.eq("claimId", claimId));
            CRMCase caseCRM = (CRMCase) crit.uniqueResult();
            return caseCRM;
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
    public CRMCase loadByCRMNumber(String crmNumber) throws TechnicalException {
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            Criteria crit = session.createCriteria(CRMCase.class);
            crit.add(Expression.eq("crmNumber", crmNumber));
            CRMCase caseCRM = (CRMCase) crit.uniqueResult();
            return caseCRM;
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
    public void save(CRMCase crmCase){
        if (crmCase == null)  throw new TechnicalException(Exceptions.HSErrorSavingObject, new Exception("Invalid Value CRM IS NULL"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            session.save(crmCase);
            session.flush();
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorSavingObject, e);
        }
    }

    public void update(CRMCase crmCase) throws TechnicalException {
        if (crmCase == null) throw new TechnicalException(Exceptions.HSErrorSavingObject, new Exception("Invalid Value CRM IS NULL"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            session.update(crmCase);
            session.flush();
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorSavingObject, e);
        }
    }
    //
    public void delete(CRMCase crmCase) throws TechnicalException {
        if (crmCase == null || StringUtil.isEmpty(crmCase.getCrmNumber()))  throw new TechnicalException(Exceptions.HSErrorDeletingObject, new Exception("Invalid Value CRM IS NULL"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            session.delete(crmCase);
            session.flush();
            session.evict(crmCase);
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
    public List<CRMCase> loadAll() throws TechnicalException {
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            Criteria crit = session.createCriteria(CRMCase.class);
            return new ArrayList<CRMCase>(crit.list());
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        } catch (Exception e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        }
    }

}
