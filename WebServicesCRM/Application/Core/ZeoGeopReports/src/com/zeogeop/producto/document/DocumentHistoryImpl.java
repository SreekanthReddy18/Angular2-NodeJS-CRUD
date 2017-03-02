package com.consisint.acsele.document;

import com.consisint.acsele.document.api.Document;
import com.consisint.acsele.document.api.DocumentHistory;
import com.consisint.acsele.document.persister.DocumentHistoryPersister;
import com.consisint.acsele.persistence.hibernate.AcseleHibernateSessionProvider;
import com.consisint.acsele.util.error.Exceptions;
import com.consisint.acsele.util.error.TechnicalException;
import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * Title: DocumentHistoryImpl.java <br>
 * Copyright: (c) 2015 Consis International <br>
 * Company: Consis International <br>
 * @author Consis International (CON)
 * @author Julio Morales (JCM)
 * @version Acsel-e v13.x
 *
 **/
public class DocumentHistoryImpl implements DocumentHistory {
    private static final long serialVersionUID = -7112997705571487806L;

    private long pk;
    private String user;
    private String description;
    private long docPk;
    private Date timestamp;
	private String documentName;

    public DocumentHistoryImpl() {
    }

    public DocumentHistoryImpl(String user, String description, long docPk, Date timestamp, String documentName) {
        this.user = user;
        this.docPk = docPk;
        this.timestamp = timestamp;
        this.description = description;
        this.documentName = documentName;
    }

    public DocumentHistoryImpl(long pk, String user, long docPk, String description, Date timestamp, String documentName) {
        this(user, description, docPk, timestamp, documentName);
        this.pk = pk;
    }

    @Override
    public long getPk() {
        return pk;
    }

    @Override
    public void setPk(long pk) {
        this.pk = pk;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() { return description; }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getDocPk() {
        return docPk;
    }

    @Override
    public void setDocPk(long docPk) {
        this.docPk = docPk;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

	@Override
	public String getDocumentName() {
        return documentName;
    }

	@Override
    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
	
    @Override
    public void save() {
        DocumentHistoryPersister.Impl.save(this);
    }

    @Override
    public Document getDocument() {
        return DocumentHistoryPersister.Impl.getDocument(this);
    }


//    public static List<HistoryDocumentGenerated> loadByLetterId(Long letterId) throws TechnicalException {
//        try {
//            Session session = AcseleHibernateSessionProvider.getUserDbProvider().getHibernateDbSession();
//            Criteria crit = session.createCriteria(HistoryDocumentGenerated.class);
////            crit.add(Expression.eq("generationDate", generationDate));
//            crit.add(Expression.eq("docPk", letterId));
//            crit.addOrder(Order.desc("historyDocumentId"));
//            return new ArrayList<HistoryDocumentGenerated>(crit.list());
//        } catch (ObjectNotFoundException e) {
//            return null;
//        } catch (HibernateException e) {
//            throw new TechnicalException(Exceptions.HSErrorLoadingObject, e);
//        } catch (Exception e) {
//            throw new TechnicalException(Exceptions.HSErrorLoadingObject, e);
//        }
//    }

}
