package com.consisint.acsele.interseguro.interfaces.crm.impl.services;

import com.consisint.acsele.interseguro.interfaces.crm.entity.LogCRMCase;
import com.consisint.acsele.util.error.TechnicalException;

import java.util.List;

/**
 * Created by ext.dpalma on 25/01/2017.
 */
public interface LogCRMCaseService {

    List<LogCRMCase> load(LogCRMCase crmCase) throws TechnicalException;

    String create(LogCRMCase crmCase) throws TechnicalException;

    String update(LogCRMCase crmCase) throws TechnicalException;

    String delete(LogCRMCase crmCase) throws TechnicalException;

}
