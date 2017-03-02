package com.consisint.acsele.interseguro.interfaces.crm.impl;

import com.consisint.acsele.UserInfo;
import com.consisint.acsele.interseguro.interfaces.crm.entity.LogCRMCase;
import com.consisint.acsele.interseguro.interfaces.crm.impl.services.LogCRMCaseService;
import com.consisint.acsele.interseguro.interfaces.crm.persister.LogCRMCasePersister;
import com.consisint.acsele.util.CommonMessages;
import com.consisint.acsele.util.error.ApplicationException;
import com.consisint.acsele.util.error.Exceptions;
import com.consisint.acsele.util.error.TechnicalException;
import net.sf.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.ResourceBundle;

import static com.consisint.acsele.util.CommonMessages.COMUNMESSAGES;

/**
 * Title: LogCRMCaseImpl.java <br>
 * Copyright: (c) 2017 Consis International<br>
 * Company: Consis International<br>
 *
 * @author Consis International (CON)
 * @author Darwin Palma (DP)
 * @version Acsel-e
 */
public class LogCRMCaseServiceImpl implements LogCRMCaseService {

    private LogCRMCasePersister logCRMCasePersister;

    private ResourceBundle comunMessageBundle = ResourceBundle.getBundle(COMUNMESSAGES, UserInfo.getLocale());

    @Override
    public List<LogCRMCase> load(LogCRMCase crmCase) throws ApplicationException, TechnicalException {
        return logCRMCasePersister.loadByCRMNumber(crmCase.getCrmNumber());
    }

    @Override
    public String create(LogCRMCase crmCase) throws TechnicalException {
        try {
            logCRMCasePersister.save(crmCase);
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
    public String update(LogCRMCase crmCase) throws TechnicalException {
        try {
            logCRMCasePersister.update(crmCase);
        }catch(TechnicalException e){
            throw new TechnicalException(Exceptions.HSErrorSavingObject, e);
        }
        return null;
    }

    @Override
    public String delete(LogCRMCase crmCase) throws TechnicalException {
        try {
            logCRMCasePersister.delete(crmCase);
        }catch(TechnicalException e){
            return e.getMessage();
        }
        return null;
    }

    public LogCRMCasePersister getLogCRMCasePersister() {
        return logCRMCasePersister;
    }

    public void setLogCRMCasePersister(LogCRMCasePersister logCRMCasePersister) {
        this.logCRMCasePersister = logCRMCasePersister;
    }

}
