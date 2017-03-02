package com.consisint.acsele.document.persister.hibernate;

import com.consisint.acsele.document.DocumentHistoryImpl;
import com.consisint.acsele.document.api.DocumentHistory;
import com.consisint.acsele.document.persister.DocumentHistoryPersister;
import com.consisint.acsele.persistent.IDDBFactory;
import com.consisint.acsele.reports.LetterHistoryClaim;
import com.consisint.acsele.reports.LetterHistoryPolicy;
import com.consisint.acsele.util.HibernateUtil;
import com.consisint.acsele.util.error.Exceptions;
import com.consisint.acsele.util.error.Severity;
import com.consisint.acsele.util.error.TechnicalException;
import com.consisint.acsele.util.logging.AcseleLogger;
import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import java.util.*;

/**
 *
 * Title: DocumentHistoryHibernatePersister.java <br>
 * Copyright: (c) 2015 Consis International <br>
 * Company: Consis International <br>
 * @author Consis International (CON)
 * @author Julio Morales (JCM)
 * @version Acsel-e v13.x
 *
 **/
public class DocumentHistoryHibernatePersister implements DocumentHistoryPersister {
    private static final AcseleLogger log = AcseleLogger.getCurrentClassLogger();

    @Override
    public DocumentHistory loadByPk(long pk) {
        if (pk <= 0) throw new TechnicalException(Exceptions.HSErrorLoadingObject, new Exception("Invalid Value"));
        try {
            Session session = HibernateUtil.getSession();
            return (DocumentHistory) session.load(DocumentHistoryImpl.class, pk);
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingObject, e);
        }
    }

    @Override
    public void save(DocumentHistory docHistory) throws TechnicalException {

            if (docHistory == null) throw new TechnicalException(Exceptions.HSErrorSavingObject, new Exception("Invalid Value"));
            try {
                docHistory.setPk(IDDBFactory.getNextIDL("STRP_LETTERHISTORY"));
            } catch (Exception e) {
                log.error("Error generating DocumentHistory identifier.", e);
                throw new TechnicalException(Exceptions.JSErrorGeneratingIdentifier, Severity.ERROR, e);
            }
            try {
                Session session = HibernateUtil.getSession();
                session.save(docHistory);
                session.flush();
            } catch (HibernateException e) {
                log.error(e);
                throw new TechnicalException(Exceptions.HSErrorSavingObject, e);
            }
    }

    @Override
    public List<LetterHistoryClaim> loadByClaimId(long claimId) {
        try {
            Query query = HibernateUtil.getQuery("letterHistory.loadLetterHistoryClaim.ByClaimId");
            query.setLong("claimId", claimId);
            return new ArrayList<LetterHistoryClaim>(query.list());
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        } catch (Exception e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        }
    }

    public List<DocumentHistoryImpl> loadByLetterIdCRM(long letterId) {
        try {
            Query query = HibernateUtil.getQuery("letterHistory.loadLetterHistoryDocument.ByLetterId.CRM");
            query.setLong("docPk", letterId);
            return new ArrayList<DocumentHistoryImpl>(query.list());
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        } catch (Exception e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        }
    }
    @Override
    public List<DocumentHistory> loadAll() throws TechnicalException {
        try {
            Session session = HibernateUtil.getSession();
            Criteria crit = session.createCriteria(DocumentHistoryImpl.class);
            return new ArrayList<DocumentHistory>(crit.list());
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        } catch (Exception e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        }
    }
    @Override
    public List<LetterHistoryPolicy> loadAllLetterHistoryPolicy() throws TechnicalException {
        try {
            Query query = HibernateUtil.getQuery("letterHistory.loadLetterHistoryPolicy");
            return new ArrayList<LetterHistoryPolicy>(query.list());
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        } catch (Exception e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        }
    }
    @Override
    public List<LetterHistoryClaim> loadAllLetterHistoryClaim() throws TechnicalException {
        try {
            Query query = HibernateUtil.getQuery("letterHistory.loadLetterHistoryClaim");
            return new ArrayList<LetterHistoryClaim>(query.list());
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        } catch (Exception e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        }


    }
    @Override
    public Map<String, List<DocumentHistory>> loadAllMap() throws TechnicalException {
        List<DocumentHistory> allList_pol = new ArrayList<DocumentHistory>(loadAllLetterHistoryPolicy());
        List<DocumentHistory> allList_cla = new ArrayList<DocumentHistory>(/*loadAllLetterHistoryClaim()*/);
        Map<String, List<DocumentHistory>> map = new Hashtable<String, List<DocumentHistory>>();
        map.put(DocumentHistory.POLICY_DATA, allList_pol);
        map.put(DocumentHistory.CLAIM_DATA, allList_cla);
        return map;
    }
    @Override
    public List<LetterHistoryPolicy> searchLetterHistoryPolicy(Date fromDate, Date toDate, String policyNumber, String nameEvent) throws TechnicalException{
        try {
            Query query = HibernateUtil.getQuery("letterHistory.searchLetterHistoryPolicy");
            query.setDate("fromDate", fromDate);
            query.setDate("toDate", toDate);
            query.setString("policyNumber", policyNumber);
            query.setString("nameEvent", nameEvent);
            return new ArrayList<LetterHistoryPolicy>(query.list());
        } catch (HibernateException e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        } catch (Exception e) {
            log.error(e);
            throw new TechnicalException(Exceptions.HSErrorLoadingList, e);
        }
    }


}
