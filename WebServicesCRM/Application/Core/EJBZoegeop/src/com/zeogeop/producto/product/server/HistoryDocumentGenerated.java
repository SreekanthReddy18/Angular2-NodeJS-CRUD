package com.consisint.acsele.product.server;

import com.consisint.acsele.util.HibernateUtil;
import com.consisint.acsele.persistent.IDDBFactory;

import java.util.Date;


import net.sf.hibernate.Session;

/**
 * Title: HistoryLetterGenerated.java <br>
 * Copyright: (c) 2008 Consis International<br>
 * Company: Consis International<br>
 * @author Yimy Alvarez (SUN)
 * @version Acsel-e v8
 * <br>
 * Changes:
 * <br>
 *  <ul>
 *      <li> 2008-05-13 (YJA) Creation. </li>
 *      <li> 2011-07-29 (JCM)  Refactoring to handled/extend new DocumentEngine.(ACSELE-1012) </li>
 *      <li> 2012-07-12 (RL)  SONAR - Empty Finally Block.</li>
 *      <li> 2012-07-19 (RL)  SONAR - Java5 migration - Long instantiation.</li>
 *     
 * </ul>
 */
public class HistoryDocumentGenerated {

    private long historyDocumentId;
    private long configuratedCovId;
    private long docPk;
    private long thirdPartyId;
    private long rolId;
    private long userId;
    private boolean email;
    private Date generationDate;


    public long getConfiguratedCovId() {
        return configuratedCovId;
    }

    public void setConfiguratedCovId(long configuratedCovId) {
        this.configuratedCovId = configuratedCovId;
    }

    public long getDocPk() {
        return docPk;
    }

    public void setDocPk(long docPk) {
        this.docPk = docPk;
    }

    public long getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(long thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }

    public long getRolId() {
        return rolId;
    }

    public void setRolId(long rolId) {
        this.rolId = rolId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getHistoryDocumentId() {
        return historyDocumentId;
    }

    public void setHistoryDocumentId(long historyDocumentId) {
        this.historyDocumentId = historyDocumentId;
    }

    public boolean isEmail() {
        return email;
    }

    public void setEmail(boolean email) {
        this.email = email;
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    public static void saveHistoryGeneratedLetters(long configuratedCoverageId, long rolId, long letterId,
                                             long thirdpartyId, long userId, boolean sendMail,
                                             Date generationDate) {

          Session session = null;

          try {
              session = HibernateUtil.getSession();
              HistoryDocumentGenerated historyLetter= new HistoryDocumentGenerated();

              long id = IDDBFactory.getNextIDL("STCL_HistoryLetter");

              historyLetter.setHistoryDocumentId(id);
              historyLetter.setConfiguratedCovId(configuratedCoverageId);
              historyLetter.setEmail(sendMail);
              historyLetter.setGenerationDate(generationDate);
              historyLetter.setUserId(userId);
              historyLetter.setThirdPartyId(thirdpartyId);
              historyLetter.setRolId(rolId);
              historyLetter.setDocPk(letterId);
              HibernateUtil.save(historyLetter, historyLetter.getHistoryDocumentId());
          } catch (Exception e) {

              e.printStackTrace();
//          } finally {
              //HibernateUtil.closeSession(session);
          }

    }


}
