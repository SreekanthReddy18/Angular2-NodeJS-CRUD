package com.consisint.acsele.interseguro.interfaces.crm.impl;

import com.consisint.acsele.UserInfo;
import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMCase;
import com.consisint.acsele.interseguro.interfaces.crm.impl.services.CRMCaseService;
import com.consisint.acsele.interseguro.interfaces.crm.persister.CRMCasePersister;
import com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.EventNotificationWorker;
import com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.workers.EventNotificationWorkerCRMClaim;
import com.consisint.acsele.util.CommonMessages;
import com.consisint.acsele.util.error.ApplicationException;
import com.consisint.acsele.util.error.Exceptions;
import com.consisint.acsele.util.error.TechnicalException;
import net.sf.hibernate.exception.ConstraintViolationException;

import java.util.ResourceBundle;

import static com.consisint.acsele.util.CommonMessages.COMUNMESSAGES;

/**
 * Title: CRMCaseImpl.java <br>
 * Copyright: (c) 2017 Consis International<br>
 * Company: Consis International<br>
 *
 * @author Consis International (CON)
 * @author Darwin Palma (DP)
 * @version Acsel-e
 */
public class CRMCaseServiceImpl implements CRMCaseService {

    private CRMCasePersister crmCasePersister;

    private ResourceBundle comunMessageBundle = ResourceBundle.getBundle(COMUNMESSAGES, UserInfo.getLocale());

    @Override
    public CRMCase load(CRMCase crmCase) throws ApplicationException, TechnicalException {
        return crmCasePersister.loadByCRMNumber(crmCase.getCrmNumber());
    }

    @Override
    public String create(CRMCase crmCase) throws TechnicalException {
        try {
            crmCasePersister.save(crmCase);
        }catch(TechnicalException e){
            if(ConstraintViolationException.class.equals(e.getCause().getClass())){
                return comunMessageBundle.getString(CommonMessages.COMUNMESSAGES_EXCEPTION_SAVE_DUPLICADO);
            }else{
                return e.getMessage();
            }
        }
        return null;
    }

    @Override
    public String update(CRMCase crmCase) throws TechnicalException {
        try {
            crmCasePersister.update(crmCase);
        }catch(TechnicalException e){
            throw new TechnicalException(Exceptions.HSErrorSavingObject, e);
        }
        return null;
    }

    @Override
    public String delete(CRMCase crmCase) throws TechnicalException {
        try {
            crmCasePersister.delete(crmCase);
        }catch(TechnicalException e){
            return e.getMessage();
        }
        return null;
    }

    public CRMCasePersister getCRMCasePersister() {
        return crmCasePersister;
    }

    public void setCRMCasePersister(CRMCasePersister crmCasePersister) {
        this.crmCasePersister = crmCasePersister;
    }

    public String sendInfoToCRM(CRMCase crmCase){
        StringBuilder error = new StringBuilder();
        try {
            EventNotificationWorker notificationWorker = new EventNotificationWorkerCRMClaim(error);
            notificationWorker.setObjectToSendOther(crmCase);
            notificationWorker.start();
            notificationWorker.join();  // Es sincrono segun el requerimiento para mostrar si fallo y enviar un mensaje al Front
        } catch (InterruptedException e) {
            e.printStackTrace();
            error.append("Error al Procesar el EventNotificationWorker, "+e.getMessage());
        }
        return error.length() > 0 ? error.toString() : null;
    }

}
