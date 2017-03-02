package com.consisint.acsele.document.persister;

import com.consisint.acsele.document.DocumentHistoryImpl;
import com.consisint.acsele.document.api.Document;
import com.consisint.acsele.document.api.DocumentHistory;
import com.consisint.acsele.reports.LetterHistoryClaim;
import com.consisint.acsele.reports.LetterHistoryPolicy;
import com.consisint.acsele.util.BeanFactory;
import com.consisint.acsele.util.error.TechnicalException;
import com.consisint.acsele.util.logging.AcseleLogger;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * Title: DocumentHistoryPersister.java <br>
 * Copyright: (c) 2015 Consis International <br>
 * Company: Consis International <br>
 * @author Consis International (CON)
 * @author Julio Morales (JCM)
 * @version Acsel-e v13.x
 *
 **/
public interface DocumentHistoryPersister {

    DocumentHistory loadByPk(long documentHistoryId);

    void save(DocumentHistory documentHistory) throws TechnicalException;

    List<LetterHistoryClaim> loadByClaimId(long claimId);

    List<DocumentHistoryImpl> loadByLetterIdCRM(long letterId);

    List<DocumentHistory> loadAll() throws TechnicalException;

    List<LetterHistoryPolicy> loadAllLetterHistoryPolicy() throws TechnicalException;

    List<LetterHistoryClaim> loadAllLetterHistoryClaim() throws TechnicalException;

    List<LetterHistoryPolicy> searchLetterHistoryPolicy(Date fromDate, Date toDate, String policyNumber, String nameEvent) throws TechnicalException;

    Map<String, List<DocumentHistory>> loadAllMap() throws TechnicalException;

    class Impl {
        private static final AcseleLogger log = AcseleLogger.getCurrentClassLogger();
        private static DocumentHistoryPersister persister = getBean();

        private static DocumentHistoryPersister getBean() {
            try {
                DocumentHistoryPersister bean = (DocumentHistoryPersister) BeanFactory.getBean(DocumentHistoryPersister.class);
                return bean;
            } catch (Throwable e) {
                log.error("[DocumentHistoryPersister] ", e);
                return null;
            }
        }

        public static DocumentHistoryPersister getInstance() {
            return persister;
        }

        public static void save(DocumentHistory documentHistory) {
            persister.save(documentHistory);
        }

        public static Map<String, List<DocumentHistory>> loadAllMap() {
            return persister.loadAllMap();
        }

        public static List<LetterHistoryPolicy> loadAllLetterHistoryPolicy() {
            return persister.loadAllLetterHistoryPolicy();
        }

        public static List<LetterHistoryClaim> loadAllLetterHistoryClaim() {
            return persister.loadAllLetterHistoryClaim();
        }

        public static List<LetterHistoryPolicy> searchLetterHistoryPolicy(Date fromDate, Date toDate, String policyNumber, String nameEvent) {
            return persister.searchLetterHistoryPolicy(fromDate, toDate, policyNumber, nameEvent);
        }

        public static DocumentHistory createNewInstanceLetterHistoryPolicy(String user, String description, long docPk, Date timestamp, String eventName, long policyId, Date movDate, long operationId, String policyNumber, String documentName) {
            return new LetterHistoryPolicy(user, docPk, description, timestamp, policyId, eventName, movDate, operationId, policyNumber, documentName);
        }

        public static DocumentHistory createNewInstanceLetterHistoryClaim(String user, String description, long docPk, Date timestamp, long claimId, int operationId, String policyNumber, String claimNumber, String documentName) {
            return new LetterHistoryClaim(user, description, docPk, timestamp, claimId, operationId, policyNumber, claimNumber, documentName);
        }

        public static DocumentHistory createNewInstanceDocumentHistory(String user, String description, long docPk, Date timestamp, String documentName) {
            return new DocumentHistoryImpl(user, description, docPk, timestamp, documentName);
        }

        public static Document getDocument(DocumentHistoryImpl documentHistory) {
            return DocumentEngineHibernatePersister.INSTANCE.getById(documentHistory.getDocPk()); //DocumentEnginePersister.Impl.getById(Long.valueOf(documentHistory.getDocPk()));
        }

        public static List<LetterHistoryClaim> loadByClaimId(long claimId) {
            return persister.loadByClaimId(claimId);
        }

        public static List<DocumentHistoryImpl> loadByLetterIdCRM(long letterId) {
            return persister.loadByLetterIdCRM(letterId);
        }

        public static DocumentHistory loadById(long documentHistoryId) {
            try {
                return persister.loadByPk(documentHistoryId);
            } catch (Exception e) {
                return null;
            }
        }
    }




}

