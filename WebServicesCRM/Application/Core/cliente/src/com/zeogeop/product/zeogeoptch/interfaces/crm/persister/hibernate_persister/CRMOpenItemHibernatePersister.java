package com.consisint.acsele.interseguro.interfaces.crm.persister.hibernate_persister;

import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMOpenItem;
import com.consisint.acsele.interseguro.interfaces.crm.persister.CRMOpenItemPersister;
import com.consisint.acsele.persistence.hibernate.AcseleHibernateSessionProvider;
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
public class CRMOpenItemHibernatePersister implements CRMOpenItemPersister {
    private static final AcseleLogger log = AcseleLogger.getCurrentClassLogger();
    //private boolean isCaching;

    private CRMOpenItemHibernatePersister() {
    }

    @Override
    public CRMOpenItem loadByOpmId(long opmId) throws TechnicalException {
        if (opmId <= 0) throw new TechnicalException(Exceptions.HSErrorLoadingObject, new Exception("Invalid Value - Id"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            Criteria crit = session.createCriteria(CRMOpenItem.class);
            crit.add(Expression.eq("pk.opmId", opmId));
            return (CRMOpenItem) crit.uniqueResult();
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
    public List<CRMOpenItem> loadByCRMNumber(String crmNumber) throws TechnicalException {
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            Criteria crit = session.createCriteria(CRMOpenItem.class);
            crit.add(Expression.eq("pk.crmCase.crmNumber", crmNumber));
            ArrayList<CRMOpenItem> crmOpenItem = new ArrayList<CRMOpenItem>(crit.list());
            return crmOpenItem;
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
    public void save(CRMOpenItem crmOpenItem){
        if (crmOpenItem == null || crmOpenItem.getPk() == null)  throw new TechnicalException(Exceptions.HSErrorSavingObject, new Exception("Invalid Value CRM IS NULL"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            session.save(crmOpenItem);
            session.flush();
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorSavingObject, e);}
    }

    public void update(CRMOpenItem crmOpenItem) throws TechnicalException {
        if (crmOpenItem == null)  throw new TechnicalException(Exceptions.HSErrorSavingObject, new Exception("Invalid Value CRM IS NULL"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            session.update(crmOpenItem);
            session.flush();
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorSavingObject, e);}
    }
    //
    public void delete(CRMOpenItem crmOpenItem) throws TechnicalException {
        if (crmOpenItem == null || crmOpenItem.getPk() == null) throw new TechnicalException(Exceptions.HSErrorDeletingObject,
                    new Exception("Invalid Value CRM IS NULL"));
        try {
            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
            session.delete(crmOpenItem);
            session.flush();
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorDeletingObject, e);
        }
    }

    @Override
    public int countAll() throws TechnicalException {
        return 0;
    }

}
