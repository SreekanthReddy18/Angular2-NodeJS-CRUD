package com.consisint.acsele.claim.persister.jdbc;

import com.consisint.acsele.AggregateObject;
import com.consisint.acsele.ClientInfo;
import com.consisint.acsele.Versionable;
import com.consisint.acsele.claim.bean.ClaimSearchParameters;
import com.consisint.acsele.claim.category.EnumTypeClaimSearch;
import com.consisint.acsele.openapi.product.Product;
import com.consisint.acsele.product.claimcause.api.ClaimEvent;
import com.consisint.acsele.template.api.Template;
import com.consisint.acsele.template.api.Transformer;
import com.consisint.acsele.template.server.*;
import com.consisint.acsele.uaa.api.RoleGroup;
import com.consisint.acsele.uaa.api.RoleList;
import com.consisint.acsele.util.*;
import com.consisint.acsele.util.dbtranslator.DBTranslator;
import com.consisint.acsele.util.dbtranslator.DBTranslatorFactory;
import com.consisint.acsele.util.error.ApplicationException;
import com.consisint.acsele.util.error.Severity;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.template.api.Property;
import com.consisint.acsele.template.api.TipoTransformerFunction;

import java.util.*;

/**
 * Title: QueryBuilderClaim <br>
 * Copyright: (c) 2015 Consis International<br>
 * Company: Consis International<br>
 *
 * @author Consis International (CON)
 * @version Acsel-e v13.7
 *
 * Builder of Query for Claim Advanced Search
 */

public class QueryBuilderClaim {

    private static final AcseleLogger log = AcseleLogger.getLogger(QueryBuilderClaim.class);

    private static final QueryBuilderClaim INSTANCE = new QueryBuilderClaim();
    private String prdTbl = "PRODUCT";    //  PRODUCT
    private String operTbl = "CONTEXTOPERATION";  //CONTEXTOPERATION
    private String claimTbl = "CLAIM";
    private String claimRUTbl = "CLAIMRISKUNIT";
    private String claimInsuranceObjetTbl = "CLAIMINSURANCEOBJECT";
    private String tpTbl = "STTE_THIRDPARTY";
    private String roleTbl = "THIRDPARTYROLE";
    private String collpayPartTbl = "COLLECTOR";
    private String collBranPayPartTbl = "STCA_COLLECTORBRANCH";
    private String stateTbl = "STATE";
    private String crmTbl = "EXT_CRMCASE";    //  EXT_CRMCASE
    private String nroCaso = "";
    private String auditTrail = "STAD_AUDITTRAIL";

    private String polTbl = AggregateObject.getTable(AggregateObject.POLICY).toUpperCase();   //  AGREGATEDPOLICY
    private String polTblDCO = AggregateObject.getTableVersion(AggregateObject.POLICY).toUpperCase(); //  POLICYDCO
    private String ruTblDCO = AggregateObject.getTableVersion(AggregateObject.RISK_UNIT).toUpperCase(); //  RISKUNITDCO
    private String ioTblDCO = AggregateObject.getTableVersion(AggregateObject.INSURANCE_OBJECT).toUpperCase();    //  INSURANCEOBJECTDCO
    private String covTblDCO = AggregateObject.getTableVersion(AggregateObject.COVERAGE).toUpperCase(); //  COVERAGEDCO
    private String covPartTbl = AggregateObject.getTable(AggregateObject.COVERAGE_PARTICIPATION).toUpperCase();
    private String ioPartTbl = AggregateObject.getTable(AggregateObject.INSURANCE_OBJECT_PARTICIPATION).toUpperCase();
    private String ruPartTbl = AggregateObject.getTable(AggregateObject.RISK_UNIT_PARTICIPATION).toUpperCase();
    private String polPartTbl = AggregateObject.getTable(AggregateObject.POLICY_PARTICIPATION).toUpperCase();
    private String polPartTblDco = AggregateObject.getTableVersion(AggregateObject.POLICY_PARTICIPATION).toUpperCase();
    private RoleList clientsId = RoleGroup.CLIENT_ROLES.getRoleList();

    private QueryBuilderClaim() {
    }

    public static QueryBuilderClaim getInstance() {
        return INSTANCE;
    }

    private String preQuerySelectClaim( ) {
        return preQueryAndGroupByQueryClaim(true);
    }

    private String groupByQueryClaim( ) {
        return preQueryAndGroupByQueryClaim(false);
    }

    private String preQueryAndGroupByQueryClaim(boolean isPreQuery) {
        String propertyPolicyNumberName = AcseleConf.getProperty("policyIdentification");
        if (StringUtil.isEmptyOrNullValue(propertyPolicyNumberName)) {
            throw new ApplicationException("System Property policyIdentification is EMPTY or NULL Value", Severity.FATAL);
        } else {
            propertyPolicyNumberName = propertyPolicyNumberName.toUpperCase();
        }

        String propertyProposalNumberName = AcseleConf.getProperty("policy.numberSol");
        if (StringUtil.isEmptyOrNullValue(propertyProposalNumberName)) {
            log.error("System Property policy.numberSol is EMPTY or NULL Value");
        } else {
            propertyProposalNumberName = propertyProposalNumberName.toUpperCase();
        }

        String propertyQuoteNumberName = AcseleConf.getProperty("policy.numberCot");
        if (StringUtil.isEmptyOrNullValue(propertyQuoteNumberName)) {
            log.error("System Property policy.numberCot is EMPTY or NULL Value");
        } else {
            propertyQuoteNumberName = propertyQuoteNumberName.toUpperCase();
        }

        String propertyEmissionDate = AcseleConf.getProperty("FecEmision");
        if (StringUtil.isEmptyOrNullValue(propertyEmissionDate)) {
            throw new ApplicationException("System Property FecEmision is EMPTY or NULL Value", Severity.FATAL);
        } else {
            propertyEmissionDate = propertyEmissionDate.toUpperCase();
        }

        String propertyThirdpartyName = AcseleConf.getProperty("thirdparty.searchResultLinkProperties");
        if (StringUtil.isEmptyOrNullValue(propertyThirdpartyName)) {
            throw new ApplicationException("System Property thirdparty.searchResultLinkProperties is EMPTY or NULL Value", Severity.FATAL);
        } else {
            propertyThirdpartyName = propertyThirdpartyName.toUpperCase();
        }

        ConfigurableObjectType prePolTemplate = Categorias.getDefaultTemplate(CotType.POLICY);
        String prePolTbl = prePolTemplate.getDesc().toUpperCase();

        StringBuilder sb = new StringBuilder();
        if(isPreQuery) {
            sb.append("SELECT ");
        }
        else {
            sb.append(" GROUP BY ");
        }
        sb.append(claimTbl).append(".CLAIMID, ");
        sb.append(claimTbl).append(".CLAIMNUMBER, ");
        sb.append(claimTbl).append(".POLICYDATE, ");
        sb.append(polTbl).append(".PRODUCTID, ");
        sb.append(polTbl).append(".").append(polTbl).append("ID, ");
        sb.append(polTblDCO).append(".STATEID, ");
        sb.append(polTblDCO).append(".INITIALDATE, ");
        sb.append(polTblDCO).append(".FINISHDATE, ");
        sb.append(operTbl).append(".ID, ");
        if(isPreQuery) {
            sb.append(claimTbl).append(".STATE AS STATEID_CLAIM, ");
            sb.append(prePolTbl).append(".").append(propertyEmissionDate).append("VALUE AS EMISSIONDATE, ");
            sb.append(prdTbl).append(".DESCRIPTION AS PRODUCT, ");
            sb.append(prePolTbl).append(".").append(propertyPolicyNumberName).append("INPUT AS POLICYNUMBER, ");
            sb.append(prePolTbl).append(".").append(StringUtil.isEmpty(propertyProposalNumberName) ? propertyPolicyNumberName : propertyProposalNumberName).append("INPUT AS PROPOSALNUMBER, ");
            sb.append(prePolTbl).append(".").append(StringUtil.isEmpty(propertyQuoteNumberName) ? propertyPolicyNumberName : propertyQuoteNumberName).append("INPUT AS QUOTENUMBER, ");
            sb.append(stateTbl).append(".").append("DESCRIPTION AS STATE, ");
            sb.append("preTPTbl.").append(propertyThirdpartyName).append("INPUT AS THIRDPARTYNAME, ");
            sb.append(polPartTblDco).append(".PAYMENTMODETEMPLATENAME AS PAYMENTMODE, ");
            sb.append(collpayPartTbl).append(".NAME AS VIA, ");
            sb.append(collBranPayPartTbl).append(".CTOB_NAME AS SUBVIA, ");
            sb.append(operTbl).append(".USER_NAME AS USER_NAME, ");
            sb.append(crmTbl).append(".").append("CRMNUMBER");
            sb.append(auditTrail).append(".ADT_USER AS USER_NAME ");
            if(ClientInfo.isClientRunning("Interseguro") && !StringUtil.isEmpty(getNroCaso())){
                sb.append(",").append(crmTbl).append(".CRMNUMBER ");
            }
        }
        else {
            sb.append(claimTbl).append(".STATE, ");
            sb.append(prePolTbl).append(".").append(propertyEmissionDate).append("VALUE, ");
            sb.append(prdTbl).append(".DESCRIPTION, ");
            sb.append(prePolTbl).append(".").append(propertyPolicyNumberName).append("INPUT, ");
            sb.append(prePolTbl).append(".").append(StringUtil.isEmpty(propertyProposalNumberName) ? propertyPolicyNumberName : propertyProposalNumberName).append("INPUT, ");
            sb.append(prePolTbl).append(".").append(StringUtil.isEmpty(propertyQuoteNumberName) ? propertyPolicyNumberName : propertyQuoteNumberName).append("INPUT, ");
            sb.append(stateTbl).append(".").append("DESCRIPTION, ");
            sb.append("preTPTbl.").append(propertyThirdpartyName).append("INPUT, ");
            sb.append(polPartTblDco).append(".PAYMENTMODETEMPLATENAME, ");
            sb.append(collpayPartTbl).append(".NAME, ");
            sb.append(collBranPayPartTbl).append(".CTOB_NAME, ");
            sb.append(operTbl).append(".USER_NAME, ");
            sb.append(crmTbl).append(".").append("CRMNUMBER");
            sb.append(auditTrail).append(".ADT_USER");
            if(ClientInfo.isClientRunning("Interseguro") && !StringUtil.isEmpty(getNroCaso())){
                sb.append(",").append(crmTbl).append(".CRMNUMBER ");
            }
        }


        return sb.toString();
    }

    private String preQueryClaim() {
        ConfigurableObjectType prePolTemplate = Categorias.getDefaultTemplate(CotType.POLICY);
        String prePolTbl = prePolTemplate.getDesc().toUpperCase();

        ConfigurableObjectType preTPTblCOT = Categorias.getDefaultTemplate(CotType.THIRDPARTY);
        String preTPTbl = preTPTblCOT.getDesc().toUpperCase();

        StringBuffer sb = new StringBuffer();
        sb.append(preQuerySelectClaim());
        sb.append(" FROM ").append(prdTbl).append(" INNER JOIN ");
        sb.append(polTbl).append(" ON ").append(prdTbl).append(".").append(prdTbl).append("ID = ").append(polTbl).append(".").append(prdTbl).append("ID INNER JOIN ");
        sb.append(claimTbl).append(" ON ").append(claimTbl).append(".POLICYID = ").append(polTbl).append(".AGREGATEDPOLICYID INNER JOIN ");
        sb.append(operTbl).append(" ON ").append(polTbl).append(".AGREGATEDPOLICYID = ").append(operTbl).append(".ITEM AND ").append(operTbl).append(".STATUS = ")
                .append(Versionable.STATUS_APPLIED).append(" AND ");
        sb.append(operTbl).append(".TIME_STAMP = (SELECT MAX (CTX.TIME_STAMP) FROM ").append(operTbl).append(" CTX WHERE CTX.ITEM = ")
                .append(operTbl).append(".ITEM AND CTX.STATUS = ").append(Versionable.STATUS_APPLIED).append(") ");
        sb.append(" INNER JOIN ").append(polTblDCO).append(" ON ").append(operTbl).append(".ID = ").append(polTblDCO).append(".OPERATIONPK INNER JOIN ");
        sb.append(stateTbl).append(" ON ").append(polTblDCO).append(".STATEID = ").append(stateTbl).append(".STATEID ");

        sb.append(joinTemplateToDCO(prePolTbl, polTblDCO));

        sb.append("LEFT JOIN ").append(polPartTbl).append(" polPart").append(" ON ").append("polPart").append(".OPERATIONPK = ");
        sb.append(polTbl).append(".OPERATIONPK AND polPart.ROL_ID IN (");
        sb.append(RoleUtils.breakCommaSeparatedRoleId(clientsId)).append(") ");
        sb.append("LEFT JOIN ").append(tpTbl).append(" tptTbl ON polPart.THIRDPARTYID = tptTbl.TPT_ID ");
        sb.append("LEFT JOIN ").append(preTPTbl).append(" preTPTbl ON tptTbl.IDDCO = preTPTbl.PK ");

        sb.append("LEFT JOIN ").append(polPartTblDco)
                .append(" ON ")
                .append(polPartTblDco).append(".OPERATIONPK = ").append("polPart").append(".OPERATIONPK ")
                .append("AND ").append(polPartTblDco)
                .append(".AGREGATEDOBJECTID = ").append("polPart").append(".AGREGATEDOBJECTID ");



        sb.append("LEFT JOIN ").append(collpayPartTbl).append(" ON ")
                .append(collpayPartTbl).append(".ID = ").append(polPartTblDco).append(".CTO_ID ");
        sb.append("LEFT JOIN ").append(collBranPayPartTbl)
                .append(" ON ").append(collBranPayPartTbl).append(".CTOB_ID = ").append(polPartTblDco).append(".CTOB_ID ");

        sb.append(" INNER JOIN ").append(auditTrail).append(" ON ").append(auditTrail).append(".ADT_IDENTIFIER = ").append(claimTbl).append(".CLAIMNUMBER");

        sb.append("LEFT JOIN ").append(crmTbl)
                .append(" ON ").append(crmTbl).append(".CLAIMID = ").append(claimTbl).append(".CLAIMID");

        return sb.toString();
    }

    private String querySimpleSearchClaim(String claimNumber,Date occurrenceDate, String policyNumber, String productName, String crmNumber) {
        StringBuffer query = new StringBuffer();
        query.append(preQueryClaim());
        if(ClientInfo.isClientRunning("Interseguro") && !StringUtil.isEmpty(crmNumber)){
            recreateQuery(query, claimNumber, occurrenceDate, policyNumber, productName, crmNumber);
        } else {
            query.append(" WHERE ");
            query.append(addContextStatusCondition());
            query.append(addClaimNumberCondition(claimNumber));
            query.append(addOcurrenceDateCondition(occurrenceDate));
            query.append(addPolicyNumberCondition(policyNumber));
            query.append(addProductNameCondition(productName));
            query.append(groupByQueryClaim());
        }
        query.append(addCRMNumberCondition(crmNumber));
        query.append(groupByQueryClaim());
        setNroCaso("");
        return query.toString();
    }

    private String queryAdvancedSearchClaim(String productName, String polTemplate, Map<String, String> policyMap, String ruTemplate, Map<String, String> ruMap,
                             String ioTemplate, Map<String, String> ioMap, String claimEventTemplate, Map<String, String> claimEventMap,
                             boolean isOrCondition, String partTbl, String partTblDco, String tpTemplate, Map<String, String> tpMap,
                             String roleTemplate, Map<String, String> roleMap
                             , String crmNumber) {
        ConfigurableObjectType prePolTemplate = Categorias.getDefaultTemplate(CotType.POLICY);
        String prePolTbl = prePolTemplate.getDesc().toUpperCase();
        List<String> propsPrePolTbl = prePolTemplate.getPropertyNames();

        ConfigurableObjectType preRUTemplate = Categorias.getDefaultTemplate(CotType.RISK_UNIT);
        String preRUTbl = preRUTemplate.getDesc().toUpperCase();
        List<String> propsPreRUTbl = preRUTemplate.getPropertyNames();

        ConfigurableObjectType preIoTemplate = Categorias.getDefaultTemplate(CotType.INSURANCE_OBJECT);
        String preIOTbl = preIoTemplate.getDesc().toUpperCase();
        List<String> propsPreIOTbl = preIoTemplate.getPropertyNames();

        ConfigurableObjectType preTPTblCOT = Categorias.getDefaultTemplate(CotType.THIRDPARTY);
        String preTPTbl = preTPTblCOT.getDesc().toUpperCase();
        List<String> propertiesPreThirdTable = preTPTblCOT.getPropertyNames();

        ConfigurableObjectType preRoleTblCOT = Categorias.getDefaultTemplate(CotType.ROLE);
        String preRoleTbl = preRoleTblCOT.getDesc().toUpperCase();
        List<String> propertiesPreRoleTable = preRoleTblCOT.getPropertyNames();

        StringBuffer query = new StringBuffer();
        query.append(preQueryClaim());
        if (!MapUtil.isEmptyOrNull(policyMap)) {
            query.append(joinTemplateToDCO(polTemplate, polTblDCO));
        }
        if (!MapUtil.isEmptyOrNull(ruMap) || !MapUtil.isEmptyOrNull(claimEventMap) || claimEventTemplate != null) {
            query.append(joinTblToOperationPk(ruTblDCO));
            query.append(joinTemplateToDCO(preRUTbl, ruTblDCO));
            query.append(joinTemplateToDCO(ruTemplate, ruTblDCO));

            if(!StringUtil.isEmptyOrNullValue(claimEventTemplate)) {
                StringBuilder sb = new StringBuilder(" INNER JOIN ");
                sb.append(claimRUTbl).append(" ON ").append(claimTbl).append(".CLAIMID = ").append(claimRUTbl).append(".CLAIMID ");
                sb.append(" INNER JOIN ");
                sb.append(claimInsuranceObjetTbl.toUpperCase()).append(" ON ").append(claimRUTbl).append(".CLAIMRISKUNITID = ").append(claimInsuranceObjetTbl.toUpperCase()).append(".CLAIMRISKUNITID ");
                sb.append(" INNER JOIN ");
                sb.append(claimEventTemplate.toUpperCase()).append(" ON ").append(claimInsuranceObjetTbl).append(".DAMAGEDCOID = ").append(claimEventTemplate.toUpperCase()).append(".PK ");
                query.append(sb);
            }
        }
        if (!MapUtil.isEmptyOrNull(ioMap)) {
            query.append(joinTblToOperationPk(ioTblDCO));
            query.append(joinTemplateToDCO(preIOTbl, ioTblDCO));
            query.append(joinTemplateToDCO(ioTemplate, ioTblDCO));
        }
        /** Agregamos el join si hay que buscar en determinado nivel de participacion **/
        if (!MapUtil.isEmptyOrNull(tpMap) || !MapUtil.isEmptyOrNull(roleMap)) {
            query.append(joinTblToOperationPk(partTblDco));
            query.append(joinTblToTblDco(partTbl, partTblDco));
            /** Agregar filtrado por Rol **/
            query.append(joinTblRoleWithParticipation(roleTemplate, partTbl));
        }
        /* Agregamos los join a la tabla de ThirdParty, la plantilla predeterminada y otra platilla X */
        if (!MapUtil.isEmptyOrNull(tpMap)) {
            query.append(joinThirdParty(partTbl)).append(joinPreThirdParty()).append(joinThirdPartyTemplate(tpTemplate));
        }
        /* Agregamos los join a la tabla de Roles, la plantilla predeterminada y otra platilla X */
        if (!MapUtil.isEmptyOrNull(roleMap)) {
            query.append(joinRole(partTbl)).append(joinPreRole()).append(joinRoleTemplate(roleTemplate));
        }

        query.append(" WHERE ");
        query.append(addContextStatusCondition());
        query.append(addProductNameCondition(productName));
        query.append(addTemplateCondition(prePolTbl, propsPrePolTbl, polTemplate, policyMap, isOrCondition));
        query.append(addTemplateCondition(preRUTbl, propsPreRUTbl, ruTemplate, ruMap, isOrCondition));
        query.append(addTemplateCondition(preIOTbl, propsPreIOTbl, ioTemplate, ioMap, isOrCondition));
        query.append(addTemplateCondition(null, null, claimEventTemplate, claimEventMap, isOrCondition));

        query.append(addTemplateCondition(preTPTbl, propertiesPreThirdTable, tpTemplate, tpMap, false));
        query.append(addTemplateCondition(preRoleTbl, propertiesPreRoleTable, roleTemplate, roleMap, false));
        query.append(addCRMNumberCondition(crmNumber));
        query.append(groupByQueryClaim());
        setNroCaso("");
        return query.toString();
    }

    private String joinTblRoleWithParticipation(String roleTemplate, String template) {
        if (StringUtil.isEmptyOrNullValue(roleTemplate)) return "";
        StringBuffer sb = new StringBuffer(" INNER JOIN STTS_ROLE ")
                .append(" ON STTS_ROLE.ROL_ID = ").append(template).append(".ROL_ID AND STTS_ROLE.ROL_DESCRIPTION = '").append(roleTemplate).append("' ");
        return sb.toString();
    }

    private String addContextStatusCondition() {
        StringBuilder sb = new StringBuilder("");
        sb.append(operTbl).append(".STATUS = ").append(Versionable.STATUS_APPLIED);
        return sb.toString();
    }

    private String addTemplateCondition(String preTemplate, List<String> preTemplateProps, String templateName, Map<String, String> valuesMap, boolean isOrCondition){
        log.debug("Adding template conditions...");
        if(preTemplate == null && templateName == null){
            return "";
        }

        if(!MapUtil.isEmptyOrNull(valuesMap)){
            log.debug("Setting template values in "+preTemplate + " and "+ templateName);
            StringBuilder sb = new StringBuilder(" ");
            Set<String> keys = valuesMap.keySet();
            int count = 0;

            if(StringUtil.isEmptyOrNullValue(templateName)){   //Search only in Default template
                for(String key : keys){
                    String value = valuesMap.get(key);
                     if(!StringUtil.isEmptyOrNullValue(value)){
                        if(count == 0) {
                            if (isOrCondition) {
                                sb.append(" OR ");
                            } else {
                                sb.append(" AND ");
                            }
                        }
                         String symbol = key.toUpperCase();
                         PropiedadImpl propiedad = Propiedades.getInstance(Propiedad.TEMPLATE_PROPERTY).get(key);
                         if (propiedad.getTipo().equals(Presentador.TEXTAREA) || propiedad.isTest()) {
                             DBTranslator dbtranslator = DBTranslatorFactory.getDBClass();
                             String containsFunction = dbtranslator.getContainsFunction(preTemplate + "." + symbol + "INPUT", value) + " ";
                             sb.append(containsFunction);
                         } else if (propiedad.getTipo().equals(Presentador.LISTA)) {

                             Double realValue = getRealValue(key, value);

                             if (realValue != null) {
                                 sb.append(preTemplate).append(".").append(symbol).append("VALUE = '").append(realValue).append("' ");
                             } else {
                                 if (AcseleConf.getProperty("search.like").equals(AcseleConstants.TRUE)) {
                                     sb.append(preTemplate).append(".").append(symbol).append("INPUT LIKE '").append(value).append("' ");
                                 } else {
                                     sb.append(preTemplate).append(".").append(symbol).append("INPUT = '").append(value).append("' ");
                                 }
                             }

                         } else if(Property.Impl.load(key).getTipoTransformerFunction().equals(TipoTransformerFunction.DATE)) {
                             if (AcseleConf.getProperty("search.like").equals(AcseleConstants.TRUE)) {
                                 sb.append(preTemplate).append(".").append(symbol).append("VALUE LIKE '").append(DateUtil.getValueFromDate(value)+ ".0").append("' ");
                             } else {
                                 sb.append(preTemplate).append(".").append(symbol).append("VALUE = '").append(DateUtil.getValueFromDate(value)+ ".0").append("' ");
                             }
                         } else {
                             if (AcseleConf.getProperty("search.like").equals(AcseleConstants.TRUE)) {
                                 sb.append(preTemplate).append(".").append(symbol).append("INPUT LIKE '").append(value).append("' ");
                             } else {
                                 sb.append(preTemplate).append(".").append(symbol).append("INPUT = '").append(value).append("' ");
                             }
                         }

                    count++;

                    if(count < keys.size()){
                        sb.append(" AND ");
                    }
                }
                }
            }else{
                templateName = templateName.toUpperCase();
                if (isOrCondition) {
                    sb.append(" AND ( ");
                }
                for(String key : keys){
                    String value = valuesMap.get(key);

                    if(!StringUtil.isEmptyOrNullValue(value)){
                        if(count == 0 && !isOrCondition) {
                            sb.append(" AND ");
                        } else  if (count >= 1 && isOrCondition){
                            sb.append(" OR ");
                        }

                        String symbol = key.toUpperCase();
                        String table = templateName;
                        if (preTemplateProps!= null && preTemplateProps.contains(key)) {
                            table = preTemplate;
                        }

                        PropiedadImpl propiedad = Propiedades.getInstance(Propiedad.TEMPLATE_PROPERTY).get(key);

                        if (propiedad.getTipo().equals(Presentador.TEXTAREA) || propiedad.isTest()) {
                            DBTranslator dbtranslator = DBTranslatorFactory.getDBClass();
                            String containsFunction = dbtranslator.getContainsFunction(table + "." + symbol + "INPUT", value) + " ";
                            sb.append(containsFunction);
                        } else if (propiedad.getTipo().equals(Presentador.LISTA)) {

                            Double realValue = getRealValue(key, value);

                            if (realValue != null) {
                                sb.append(table).append(".").append(symbol).append("VALUE = '").append(realValue).append("' ");
                            } else {
                                if (AcseleConf.getProperty("search.like").equals(AcseleConstants.TRUE)) {
                                    sb.append(table).append(".").append(symbol).append("INPUT LIKE '").append(value).append("' ");
                                } else {
                                    sb.append(table).append(".").append(symbol).append("INPUT = '").append(value).append("' ");
                                }
                            }

                        } else {
                            if (AcseleConf.getProperty("search.like").equals(AcseleConstants.TRUE)) {
                                sb.append(table);
                                sb.append(".").append(symbol).append("INPUT LIKE '").append(value).append("' ");
                            } else {
                                sb.append(table);
                                sb.append(".").append(symbol).append("INPUT = '").append(value).append("' ");
                            }
                        }

                        count++;

                        if(count < keys.size() && !isOrCondition){
                           sb.append(" AND ");
                        }
                    }
                }

                if (isOrCondition) {
                    sb.append(" ) ");
                }
            }
            return sb.toString();
        }
        return "";
    }

    private Double getRealValue(String key, String input) {

        Double value = null;

        Property property = Property.Impl.load(key);

        for(Transformer transformer : property.getTransformerList().getAll()) {

            if (transformer.getInput().equalsIgnoreCase(input) || transformer.getRealDesc().equalsIgnoreCase(input)) {
                value = transformer.getValue();
                break;
            }

        }

        return value;
    }

    private String addPolicyNumberCondition(String policyNumber) {
        if (!StringUtil.isEmptyOrNullValue(policyNumber)) {
            String propertyPolicyNumberName = AcseleConf.getProperty("policyIdentification");
            ConfigurableObjectType prePolTemplate = Categorias.getDefaultTemplate(CotType.POLICY);
            String prePolTbl = prePolTemplate.getDesc().toUpperCase();

            StringBuffer sb = new StringBuffer(" AND ");
            sb.append(prePolTbl).append(".").append(propertyPolicyNumberName).append("INPUT = '").append(policyNumber).append("' ");
            return sb.toString();
        }
        return "";
    }

    private String addClaimNumberCondition(String claimNumber) {
        if (!StringUtil.isEmptyOrNullValue(claimNumber)) {
            StringBuffer sb = new StringBuffer(" AND ");
            sb.append(claimTbl).append(".CLAIMNUMBER LIKE '%").append(claimNumber).append("%' ");
            return sb.toString();
        }
        return "";
    }

    private String addOcurrenceDateCondition(Date ocurrenceDate) {
        if (ocurrenceDate != null) {
            DBTranslator dbTranslator = DBTranslatorFactory.getDBClass();
            StringBuffer sb = new StringBuffer(" AND ");
            sb.append(claimTbl).append(".POLICYDATE = ").append(dbTranslator.getDateFormat(ocurrenceDate)).append(" ");
            return sb.toString();
        }
        return "";
    }

    private String addProductNameCondition(String productName) {
        if (!StringUtil.isEmptyOrNullValue(productName)) {
            StringBuffer sb = new StringBuffer(" AND ");
            sb.append(prdTbl).append(".DESCRIPTION LIKE '").append(productName).append("' ");
            return sb.toString();
        }
        return "";
    }

    private String addCRMNumberCondition(String crmNumber) {
        if (!StringUtil.isEmptyOrNullValue(crmNumber)) {
            StringBuffer sb = new StringBuffer(" AND ");
            sb.append(crmTbl).append(".CRMNUMBER LIKE '").append(crmNumber).append("' ");
            return sb.toString();
        }
        return "";
    }

    private String joinTemplateToDCO(String template, String tblDCO) {
        if (template != null && tblDCO != null) {
            template = template.toUpperCase();
            StringBuilder sb = new StringBuilder(" INNER JOIN ");
            sb.append(template).append(" ON (").append(tblDCO).append(".DCOID = ").append(template).append(".PK )");

            return sb.toString();
        }
        return "";
    }

    private String joinTblToOperationPk(String template) {
        if (template != null) {
            template = template.toUpperCase();
            StringBuilder sb = new StringBuilder(" INNER JOIN ");
            sb.append(template).append(" ON ").append("AGREGATEDPOLICY.OPERATIONPK = ").append(template).append(".OPERATIONPK ");
            return sb.toString();
        }
        return "";
    }

    private String joinTblToTblDco(String template, String templateDco) {
        if (!StringUtil.isEmptyOrNullValue(template) && !StringUtil.isEmptyOrNullValue(templateDco)) {
            template = template.toUpperCase();
            StringBuilder sb = new StringBuilder(" INNER JOIN ");
            sb.append(template).append(" ON ").append(templateDco).append(".AGREGATEDOBJECTID = ").append(template).append(".AGREGATEDOBJECTID ");
            return sb.toString();
        }
        return "";
    }

    private String joinThirdParty(String participationTbl) {
        StringBuffer sb = new StringBuffer(" INNER JOIN ");
        sb.append(tpTbl).append(" ON ").append(participationTbl).append(".THIRDPARTYID = ").append(tpTbl).append(".TPT_ID ");
        return sb.toString();
    }

    private String joinPreThirdParty() {
        ConfigurableObjectType preTPTblCOT = Categorias.getDefaultTemplate(CotType.THIRDPARTY);
        String preTPTbl = preTPTblCOT.getDesc().toUpperCase();

        StringBuilder sb = new StringBuilder(" INNER JOIN ");
        sb.append(preTPTbl).append(" ON ").append(tpTbl).append(".IDDCO = ").append(preTPTbl).append(".PK ");
        return sb.toString();
    }

    private String joinThirdPartyTemplate(String tpTemplate) {
        if (!StringUtil.isEmptyOrNullValue(tpTemplate)) {
            StringBuilder sb = new StringBuilder(" ");
            tpTemplate = tpTemplate.toUpperCase();
            sb.append(" INNER JOIN ").append(tpTemplate).append(" ON ").append(tpTbl).append(".IDDCO = ").append(tpTemplate).append(".PK ");
            return sb.toString();
        }
        return "";
    }

    private String joinRole(String participationTbl) {
        StringBuilder sb = new StringBuilder(" INNER JOIN ");
        sb.append(roleTbl).append(" ON (").append(participationTbl).append(".ROL_ID = ").append(roleTbl).append(".ROL_ID AND ");
        sb.append(participationTbl).append(".THIRDPARTYID = ").append(roleTbl).append(".THIRDPARTYID) ");
        return sb.toString();
    }

    private String joinPreRole() {
        ConfigurableObjectType preRoleTblCOT = Categorias.getDefaultTemplate(CotType.ROLE);
        String preRoleTbl = preRoleTblCOT.getDesc().toUpperCase();

        StringBuilder sb = new StringBuilder(" INNER JOIN ");
        sb.append(preRoleTbl).append(" ON ").append(roleTbl).append(".IDDCOROLE = ").append(preRoleTbl).append(".PK ");
        return sb.toString();
    }

    private String joinRoleTemplate(String roleTemplate) {
        if (!StringUtil.isEmptyOrNullValue(roleTemplate)) {
            roleTemplate = roleTemplate.toUpperCase();
            StringBuilder sb = new StringBuilder(" INNER JOIN ");
            sb.append(roleTemplate).append(" ON ").append(roleTbl).append(".IDDCOROLE = ").append(roleTemplate).append(".PK ");
            return sb.toString();
        }
        return "";
    }

    public List<String> generateQueryForClaim(ClaimSearchParameters searchParam) {
        if(searchParam.getEnumTypeClaimSearch() == EnumTypeClaimSearch.ADVANCED_SEARCH) {
            return generateQueryForAdvancedSearchClaim(searchParam);
        }
        else {
            return generateQueryForSimpleSearchClaim(searchParam);
        }
    }

    public List<String> generateQueryForSimpleSearchClaim(ClaimSearchParameters searchParam) {
        List<String> queries = new ArrayList<String>();
        queries.add(querySimpleSearchClaim(searchParam.getClaimNumber(), searchParam.getOccurrenceDate(), searchParam.getPolicyNumber(), searchParam.getProductName(),searchParam.getCrmNumber()));
        return queries;
    }

    private Set<String> getDamageTemplatesByProduct(String productName, String selectedTemplate, Map<String, String> claimEventMap) {
        Set<String> res = new TreeSet<String>();
        if (StringUtil.isEmptyOrNullValue(selectedTemplate)) {
            if (claimEventMap != null && !claimEventMap.isEmpty()) {
                try {
                    Product product = Product.Impl.getProduct(productName);
                    Collection<ClaimEvent> claimEvents = product.getClaimEvents();
                    for (ClaimEvent claimEvent : claimEvents) {
                        Collection<Template> claimDamages = claimEvent.getClaimDamages();
                        for (Template claimDamage : claimDamages) {
                            res.add(claimDamage.getName());
                        }
                    }
                    return res;
                } catch (Exception e) {
                    log.error(e);
                    res.clear();
                }
            }
        }
        res.add(StringUtil.isEmptyOrNullValue(selectedTemplate) ? "" : selectedTemplate);
        return res;
    }

    public List<String> generateQueryForAdvancedSearchClaim(ClaimSearchParameters searchParam) {
        List<String> queries = new ArrayList<String>();

        String polTemplate = null;
        if (!StringUtil.isEmptyOrNullValue(searchParam.getProductName())) {
            Product product = Product.Impl.getProduct(searchParam.getProductName());
            if (product != null) {
                polTemplate = product.getPolicyTemplate().getName();
            }
        }

        if (searchParam.getMapThirdPartyValues() != null || searchParam.getMapRoleValues() != null) {
            String covPartTblDco = AggregateObject.getTableVersion(AggregateObject.COVERAGE_PARTICIPATION).toUpperCase();
            String ioPartTblDco = AggregateObject.getTableVersion(AggregateObject.INSURANCE_OBJECT_PARTICIPATION).toUpperCase();
            String ruPartTblDco = AggregateObject.getTableVersion(AggregateObject.RISK_UNIT_PARTICIPATION).toUpperCase();
            for (String templateName : getDamageTemplatesByProduct(searchParam.getProductName(), searchParam.getTemplateNameClaimEvent(), searchParam.getMapClaimEventValues())) {
                String queryCovLevel = queryAdvancedSearchClaim(searchParam.getProductName(), polTemplate, searchParam.getMapPolicyValues(),
                        searchParam.getTemplateNameRU(), searchParam.getMapRUValues(),
                        searchParam.getTemplateNameIO(), searchParam.getMapIOValues(),
                            templateName, searchParam.getMapClaimEventValues(), false,
                        covPartTbl, covPartTblDco, searchParam.getTemplateNameThirdParty(), searchParam.getMapThirdPartyValues(),
                        searchParam.getTemplateNameRole(), searchParam.getMapRoleValues(), searchParam.getCrmNumber());
                queries.add(queryCovLevel);

                String queryIOLevel = queryAdvancedSearchClaim(searchParam.getProductName(), polTemplate, searchParam.getMapPolicyValues(),
                        searchParam.getTemplateNameRU(), searchParam.getMapRUValues(),
                        searchParam.getTemplateNameIO(), searchParam.getMapIOValues(),
                            templateName, searchParam.getMapClaimEventValues(), false,
                        ioPartTbl, ioPartTblDco, searchParam.getTemplateNameThirdParty(), searchParam.getMapThirdPartyValues(),
                        searchParam.getTemplateNameRole(), searchParam.getMapRoleValues(),searchParam.getCrmNumber());
                queries.add(queryIOLevel);

                String queryRULevel = queryAdvancedSearchClaim(searchParam.getProductName(), polTemplate, searchParam.getMapPolicyValues(),
                        searchParam.getTemplateNameRU(), searchParam.getMapRUValues(),
                        searchParam.getTemplateNameIO(), searchParam.getMapIOValues(),
                            templateName, searchParam.getMapClaimEventValues(), false,
                        ruPartTbl, ruPartTblDco, searchParam.getTemplateNameThirdParty(), searchParam.getMapThirdPartyValues(),
                        searchParam.getTemplateNameRole(), searchParam.getMapRoleValues(),searchParam.getCrmNumber());
                queries.add(queryRULevel);

                String queryPolLevel = queryAdvancedSearchClaim(searchParam.getProductName(), polTemplate, searchParam.getMapPolicyValues(),
                        searchParam.getTemplateNameRU(), searchParam.getMapRUValues(),
                        searchParam.getTemplateNameIO(), searchParam.getMapIOValues(),
                            templateName, searchParam.getMapClaimEventValues(), false,
                        polPartTbl, polPartTblDco, searchParam.getTemplateNameThirdParty(), searchParam.getMapThirdPartyValues(),
                        searchParam.getTemplateNameRole(), searchParam.getMapRoleValues(),searchParam.getCrmNumber());
                queries.add(queryPolLevel);
            }
        }
        else {
            for (String templateName : getDamageTemplatesByProduct(searchParam.getProductName(), searchParam.getTemplateNameClaimEvent(), searchParam.getMapClaimEventValues())) {
                queries.add(queryAdvancedSearchClaim(searchParam.getProductName(), polTemplate, searchParam.getMapPolicyValues(),
                        searchParam.getTemplateNameRU(), searchParam.getMapRUValues(),
                        searchParam.getTemplateNameIO(), searchParam.getMapIOValues(),
                        templateName, searchParam.getMapClaimEventValues(),
                        false, null, null, null, null, null, null,searchParam.getCrmNumber()
                ));
            }
        }

        return queries;
    }

    /* Implementacion de Filtro de Casos CRM de Interseguro */
    private void recreateQuery(StringBuffer query, String claimNumber,Date occurrenceDate, String policyNumber, String productName, String nroCaso) {
        query.append(" LEFT JOIN ").append(QueryBuilderClaim.getInstance().crmTbl)
                .append(" ON ").append(QueryBuilderClaim.getInstance().crmTbl).append(".CLAIMID = ").append(QueryBuilderClaim.getInstance().claimTbl).append(".CLAIMID");
        query.append(" WHERE ");
        query.append(QueryBuilderClaim.getInstance().addContextStatusCondition());
        query.append(QueryBuilderClaim.getInstance().addClaimNumberCondition(claimNumber));
        query.append(QueryBuilderClaim.getInstance().addOcurrenceDateCondition(occurrenceDate));
        query.append(QueryBuilderClaim.getInstance().addPolicyNumberCondition(policyNumber));
        query.append(QueryBuilderClaim.getInstance().addProductNameCondition(productName));
        query.append(QueryBuilderClaim.getInstance().addCRMNumberCondition(nroCaso));
        setNroCaso(nroCaso);
        query.append(QueryBuilderClaim.getInstance().groupByQueryClaim());
    }

    private String getNroCaso() {
        return nroCaso;
    }

    private void setNroCaso(String nroCaso) {
        this.nroCaso = nroCaso;
    }
}