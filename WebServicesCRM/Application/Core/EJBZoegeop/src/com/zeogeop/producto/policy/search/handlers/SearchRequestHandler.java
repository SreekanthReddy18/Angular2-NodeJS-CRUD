package com.consisint.acsele.policy.search.handlers;

import com.consisint.acsele.AggregateObject;
import com.consisint.acsele.UserInfo;
import com.consisint.acsele.Versionable;
import com.consisint.acsele.persistent.managers.AggregatedManager;
import com.consisint.acsele.persistent.managers.AgregatedInsuranceObjectManager;
import com.consisint.acsele.persistent.managers.AgregatedRiskUnitManager;
import com.consisint.acsele.policy.search.QueryObject;
import com.consisint.acsele.policy.search.QueryParameter;
import com.consisint.acsele.policy.search.SearchResult;
import com.consisint.acsele.policy.search.util.SearchUtil;
import com.consisint.acsele.policy.servlet.PolicyGenericRequestHandler;
import com.consisint.acsele.policy.util.ThirdpartyUtil;
import com.consisint.acsele.product.server.Product;
import com.consisint.acsele.product.server.Productos;
import com.consisint.acsele.template.server.*;
import com.consisint.acsele.uaa.api.Role;
import com.consisint.acsele.uaa.api.RoleGroup;
import com.consisint.acsele.util.*;
import com.consisint.acsele.util.dbtranslator.DBTranslator;
import com.consisint.acsele.util.dbtranslator.DBTranslatorFactory;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.util.session.SessionConstants;
import com.consisint.acsele.util.webcontroller.ClientContext;
import com.consisint.acsele.util.webcontroller.RequestHandler;
import com.consisint.acsele.workflow.beans.impls.persister.ProductProcessPersister;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A Handler class which performs an advanced policies search.
 * This class was refactored, with the purpose of replacing the one use view
 * used in query intermediate.  This change was also made because in a environment
 * multiuser, this scheme does not work, besides, in case of an error SQL,
 * one is created view and it is never eliminated.
 * @author Consis International (CON)
 * @since Acsel-e v2.2
 *
 */

public class SearchRequestHandler extends PolicyGenericRequestHandler {

    private static final AcseleLogger log = AcseleLogger.getLogger(SearchRequestHandler.class);
    public static final int PARTICIPATION_POLICY_LEVEL = 1;
    public static final int PARTICIPATION_IO_LEVEL = 2;

    boolean isClaim = false;
    private String url = null;
    private Vector policyIDs = null;

    private Categorias defaultTemplates;
    private Categorias extendedTemplates;
    public static final String IS_SEARCH = "isSearch";
    public static final String TYPE_OF_SEARCH = "typeOfSearch";
    private static final String PRODUCT_ID = "productId";
    private String productId = null;
    private static final Hashtable temporarySearchTables = new Hashtable();
    private static TimedSession timedSession = null;
    public static final int CONVENTIONAL_SEARCH = -1;
    public static final int ENHANCED_SEARCH = 0;
    private double propResult = 0;

    /**
     *
     */
    public SearchRequestHandler() {
        super();
        setForward(true);
        defaultTemplates = Categorias.getBean(Categorias.DEFAULT_TEMPLATES_STATE);
        extendedTemplates = Categorias.getBean(Categorias.EXTENDED_TEMPLATES_STATE);
    }

    /**
     * @param context The Client Context
     * @noinspection ThrowableInstanceNeverThrown
     */
    public void handleRequest(ClientContext context) {
        log.debug("****** Entering to SearchRequestHandler.handleRequest ******");

        try {
            this.setUrl(context);

            ParameterFinder finder = new ParameterFinder(context.getRequest());
            context.removeSessionAttribute("searchresult");
            context.removeSessionAttribute("policies");
            String module = (String) context.getSession().getAttribute("module");
            module = StringUtil.isEmptyOrNullValue(module) ? "" : module;
            String search_type = RequestUtil.getValue(context.getRequest(), "search_type");
            String searchNumPage = RequestUtil.getValue(context.getRequest(), "searchNumPage");
            int typeOfSearch = StringUtil.isEmptyOrNullValue(RequestUtil.getValue(
                    context.getRequest(), SearchRequestHandler.TYPE_OF_SEARCH)) ?
                                                                                SearchRequestHandler.CONVENTIONAL_SEARCH :
                                                                                Integer.parseInt(
                                                                                        RequestUtil.getValue(
                                                                                                context.getRequest(),
                                                                                                SearchRequestHandler.TYPE_OF_SEARCH));
            String search_param = RequestUtil.getValue(context.getRequest(), "search_param");
            String numTotalPage = RequestUtil.getValue(context.getRequest(), "numTotalPage");
            String rowDisplay = AcseleConf.getProperty("recordDisplay");
            String executeCount = RequestUtil.getValue(context.getRequest(), "executeCount");
            String template = RequestUtil.getValue(context.getRequest(), "template");
            String cancelNonPayment =
                    RequestUtil.getValue(context.getRequest(), "cancelNonPayment");
            String initialDate = DateUtil.getISOParseFormated(RequestUtil.getValue(context.getRequest(), "initialDate"));
            String cutDate = DateUtil.getISOParseFormated(RequestUtil.getValue(context.getRequest(), "cutDate"));
            productId = RequestUtil.getValue(context.getRequest(), "_productId");

            Integer status = Integer.valueOf(finder.getParameterDefaultValue("state", Versionable.STATUS_APPLIED).toString());

            log.debug("**********************   ***  status = '" + status + "'");

            temporarySearchTables.put("MSQ1", "ST_MSQ");
            temporarySearchTables.put("CSQ1", "ST_CSQ");
            temporarySearchTables.put("ISQ1", "ST_ISQ1");
            temporarySearchTables.put("ISQ2", "ST_ISQ2");
            temporarySearchTables.put("ISQ3", "ST_ISQ3");
            temporarySearchTables.put("TSQ1", "ST_TSQ");
            temporarySearchTables.put("SQT1", "ST_SQT");

            switch (typeOfSearch) {
                case SearchRequestHandler.CONVENTIONAL_SEARCH:
                    log.debug("Performing a policy search using the conventional (normal) way...");
                    timedSession = new TimedSession((Long.valueOf(UserInfo.getUserID())),
                                                    TimedSession.TIMED_SESSION_TYPE_OF_USES_POLICY_SEARCH,
                                                    true);
                    break;
                case SearchRequestHandler.ENHANCED_SEARCH:
                    log.debug(
                            "Performing a policy search using the enhanced (experimental - with temporary tables) way...");
                    timedSession =
                            TimedSessionManager.getInstance(TimedSessionManager.DEFAULT_INT_VALUE)
                                    .getCurrent(
                                            TimedSession.TIMED_SESSION_TYPE_OF_USES_POLICY_SEARCH);

                    if ("true".equals(executeCount)) {
                        timedSession.setSessionStatus(TimedSession.TIMED_SESSION_STATUS_ACTIVE);
                    }

                    if (timedSession != null) {
                        timedSession.setTablesToBeTimedSessioned(
                                new ArrayList(temporarySearchTables.values()));

                        switch (timedSession.getSessionStatus()) {

                            case TimedSession.TIMED_SESSION_STATUS_ACTIVE:

                                break;
                            case TimedSession.TIMED_SESSION_STATUS_NOT_SETTED:
                                context.putSessionObject(RequestHandler.ERROR_MESSAGE,
                                                         getMessage(context)
                                                                 .getString(
                                                                 "timedSession.not_setted"));
                                ExceptionUtil.handlerException(new Exception(getMessage(context)
                                        .getString("timedSession.not_setted")));
                                break;
                            case TimedSession.TIMED_SESSION_STATUS_TIMED_OUT:
                                context.putSessionObject(RequestHandler.ERROR_MESSAGE,
                                                         getMessage(context)
                                                                 .getString(
                                                                 "timedSession.timed_out"));
                                ExceptionUtil.handlerException(new Exception(getMessage(context)
                                        .getString("timedSession.timed_out")));
                                break;
                            case TimedSession.TIMED_SESSION_STATUS_USER_HAVENT_A_SESSION:
                                context.putSessionObject(RequestHandler.ERROR_MESSAGE,
                                                         getMessage(context)
                                                                 .getString(
                                                                 "timedSession.user_havent_a_session"));
                                ExceptionUtil.handlerException(new Exception(getMessage(context)
                                        .getString("timedSession.user_doesnt_exists")));
                                break;
                            case TimedSession.TIMED_SESSION_STATUS_NO_MORE_SESSIONS_AVAILABLE:
                                context.putSessionObject(RequestHandler.ERROR_MESSAGE,
                                                         getMessage(context)
                                                                 .getString(
                                                                 "timedSession.no_more_sessions_available"));
                                ExceptionUtil.handlerException(new Exception(getMessage(context)
                                        .getString("timedSession.no_more_sessions_available")));
                                break;
                            case TimedSession.TIMED_SESSION_STATUS_RESETED:
                                context.putSessionObject(RequestHandler.ERROR_MESSAGE,
                                                         getMessage(context)
                                                                 .getString(
                                                                 "timedSession.reseted"));
                                ExceptionUtil.handlerException(new Exception(getMessage(context)
                                        .getString("timedSession.reseted")));
                                break;
                            default:
                                context.putSessionObject(RequestHandler.ERROR_MESSAGE,
                                                         getMessage(context)
                                                                 .getString(
                                                                 "timedSession.unknownError"));
                                ExceptionUtil.handlerException(new Exception(getMessage(context)
                                        .getString("timedSession.unknownError")));
                                break;
                        }
                    } else {
                        context.putSessionObject(RequestHandler.ERROR_MESSAGE, getMessage(context)
                                .getString("timedSession.no_more_sessions_available"));
                        ExceptionUtil.handlerException(new Exception(getMessage(context)
                                .getString("timedSession.no_more_sessions_available")));
                    }
                    break;

                default:
                    ExceptionUtil.handlerException(new Exception(
                            getMessage(context).getString("search.policy.unknown.type")));
                    break;
            }


            if (StringUtil.isEmptyOrNullValue(searchNumPage)) {
                deleteDataFromTables();
            }

            String search_param_value =
                    StringUtil.isEmptyOrNullValue(search_param) ? "0" : search_param;
            int index = Integer.parseInt(search_param_value);

            if (module.equals("claim") || module.equals("policy") || module.equals("reinsurance") ||
                    module.equals("isSearchPolicyCreateFacultative")|| module.equals("riRedistOperation") || module.equals("coinsurance") || module.equals("anotherPage")) {
                isClaim = true;
            }
            String numPage =
                    searchNumPage == null || searchNumPage.trim().equals("") ? "1" : RequestUtil
                            .getValue(context.getRequest(), "searchNumPage");
            if ("np".equals(search_type)) {

                numPage = String.valueOf(Integer.parseInt(numPage) + 1);

            } else if ("pp".equals(search_type)) {
                numPage = String.valueOf(Integer.parseInt(numPage) - 1);
            } else if ("lp".equals(search_type)) {
                numPage = String.valueOf(numTotalPage);
            } else if ("fp".equals(search_type)) {
                numPage = String.valueOf(1);
            }

            QueryObject queryObject = (QueryObject) context.getSessionObject("queryobject");
            StringBuilder query = new StringBuilder();

            if (queryObject.getParameterValue("_orderField") != null) {
                context.getSession()
                        .setAttribute("_order", queryObject.getParameterValue("_orderField"));
            }

            String parameter;
            if (queryObject.getParameterValue("_orderField") == null) {
                parameter = (String) context.getSession().getAttribute("_order");
            } else {
                parameter =queryObject.getParameterValue("_orderField");
            }
            int orderField = parameter == null ? 0 : Integer.parseInt(parameter);

            boolean isPendingChange = !StringUtil.isEmptyOrNullValue(context.getParameter("isPendingChange")) || context.getRequestObject("isPendingChange") != null;
            boolean selCount = false;

            if (queryObject != null) {

                QueryObject queryObjectSave = new QueryObject();
                if (queryObject.size() > 1) {
                    for (int i = 0; i < queryObject.size(); i++) {
                        if (queryObject.get(i).toString().contains(template)) {
                            queryObjectSave.add((QueryParameter) queryObject.get(i));
                        }
                    }
                    queryObject = queryObjectSave;
                }

                // search third party information
                thirdPartyInformation(queryObject, context);

                switch (typeOfSearch) {
                    case SearchRequestHandler.CONVENTIONAL_SEARCH:

                        query = buildQueryConventional(queryObject, search_type, selCount, index,
                                                       Integer.parseInt(numPage), isPendingChange,
                                                       initialDate, cutDate, status.intValue());
                        break;
                    case SearchRequestHandler.ENHANCED_SEARCH:
                        query = buildQueryEnhanced(queryObject, search_type, selCount, index,
                                                   Integer.parseInt(numPage), isPendingChange,
                                                   template, initialDate, cutDate);
                        break;
                    default:
                        break;
                }
            }
            String searchFormPolicyAttribute = context.getParameter("entered");
            boolean isInSearchformPolicy = false;

            if(searchFormPolicyAttribute != null){
                isInSearchformPolicy = searchFormPolicyAttribute.equals("null") ? false : true;
            }

            if(isInSearchformPolicy){
                String porAgregar = " AND pdco.agregatedobjectid = cl.policyid and cl.state <> 1 ";
                String porAgregarDos = ", Claim cl ";
                query.replace(query.indexOf("WHERE"), query.indexOf("WHERE", porAgregarDos.length()), porAgregarDos);
                query.replace(query.indexOf("GROUP"), query.indexOf("GROUP", porAgregar.length()), porAgregar);
            }

            if(module.equals("claim") && context.getRequest().getSession().getAttribute("operationTypeId")!=null &&
                    ProductProcessPersister.Impl.getInstance().hasProductsAssociatedByOperationTypeId((Long) context.getRequest().getSession().getAttribute("operationTypeId"))){

                Long operationTypeId = (Long) context.getRequest().getSession().getAttribute("operationTypeId");
                Hashtable<String, String> products = ProductProcessPersister.Impl.getInstance().getActiveProductNamesByProcess(operationTypeId);
                String productKeys = new ArrayList(products.keySet()).toString();
                String wfProductQuery = " AND AP.PRODUCTID IN (" + productKeys.substring(1, productKeys.toString().length()-1) + ") ";
                query.replace(query.indexOf("GROUP"), query.indexOf("GROUP", wfProductQuery.length()), wfProductQuery);
            }

            Locale currentlocale = (Locale) context.getSessionObject(SessionConstants.LOCALE);
            RequestUtil.printRequest(context.getRequest(), null);

            String totalPageInt;
            int totalPage;
            SearchResult searchResult = search(query, currentlocale, context);
            if (propResult == 0) {
                totalPageInt = numTotalPage;
            } else {
                totalPage = (int) Math.ceil(propResult / Double.parseDouble(rowDisplay));
                totalPageInt = String.valueOf(totalPage);
            }

            if (orderField!=0){
                searchResult.sort(orderField);
            }
            if (isPendingChange) {
                context.putRequestObject("isPendingChange", "yes");
            }
            
            String lifeOperation = context.getParameter("lifeOperation");
            lifeOperation = lifeOperation != null ? lifeOperation : (String) context.getAttribute("lifeOperation");
            boolean isLifeOperation = !StringUtil.isEmptyOrNullValue(lifeOperation);
            if (isLifeOperation) {
                context.putRequestObject("lifeOperation", lifeOperation);
            }

            context.putSessionObject("searchresult", searchResult);
            context.putRequestObject("propResult", propResult);
            context.putRequestObject("numTotalPage", totalPageInt);
            if (module.equals("isSearchPolicyCreateFacultative")){
                context.putRequestObject(IS_SEARCH, "false");
            }else{
                context.putRequestObject(IS_SEARCH, "true");
            }
            context.putRequestObject("search_type", search_type);
            context.putRequestObject("searchNumPage", numPage);
            context.putSessionObject("queryobject", queryObject);
            context.putSessionObject("cancelNonPayment", cancelNonPayment);

            if (!StringUtil.isEmptyOrNullValue(initialDate)) {
                context.putSessionObject("initialDate", initialDate);
            }
            if (!StringUtil.isEmptyOrNullValue(cutDate)) {
                context.putSessionObject("cutDate", cutDate);
            }
        } catch (NumberFormatException e) {
            ExceptionUtil.handlerException(new Exception());
        }
    }

    private  void thirdPartyInformation(QueryObject queryObject, ClientContext context) {
        Map<String, String> thirdParty = new HashMap<String, String>();
        QueryParameter queryParameter = (QueryParameter) queryObject.get(0);
        Enumeration keys = queryParameter.keys();

        while (keys.hasMoreElements()) {
            String parameterKey = (String) keys.nextElement();

            Object obj = queryParameter.getParameter(parameterKey);
            if (obj instanceof String) {
                String parameterValue = (String) obj;

                if (!parameterKey.contains("Validator_") && !parameterKey.equalsIgnoreCase("template")) {
                    thirdParty.put(parameterKey, parameterValue);
                }
            }
        }
        context.getRequest().setAttribute("thirdParty", thirdParty);
    }


    /**
     * Delete data from temporary tables
     */

    private void deleteDataFromTables() {
        for (Iterator tablesToBeDeleted = temporarySearchTables.values().iterator();
             tablesToBeDeleted.hasNext();) {
            String tableToBeDeleted = (String) tablesToBeDeleted.next();
            log.debug("[deleteDataFromTables] Deleting data from temporary table: " +
                      tableToBeDeleted);
            doUpdate("TRUNCATE TABLE " + tableToBeDeleted);
        }
    }

    /**
     * Delete data just from intermediate temporary tables
     */
    private void deleteDataFromIntermediateTables() {
        Hashtable interHashtable = new Hashtable();
        interHashtable.putAll(temporarySearchTables);
        interHashtable.remove("SQT1");
        for (Iterator tablesToBeDeleted = interHashtable.values().iterator();
             tablesToBeDeleted.hasNext();) {
            String tableToBeDeleted = (String) tablesToBeDeleted.next();
            log.debug("[deleteDataFromIntermediateTables] Deleting data from temporary table: " +
                      tableToBeDeleted);
            doUpdate("TRUNCATE TABLE " + tableToBeDeleted);
        }
    }

    /**
     * @param queryObject     String
     * @param type            The type
     * @param count           The count
     * @param isPendingChange If there is a pending change
     * @param template        The template
     * @param initialDate     The initial date
     * @param cutDate         The current date
     * @return StringBuilder
     */
    private StringBuilder buildQueryEnhanced(QueryObject queryObject, String type, boolean count,
                                            boolean isPendingChange, String template,
                                            String initialDate, String cutDate) {
        return buildQueryEnhanced(queryObject, type, count, 0, 0, isPendingChange, template,
                                  initialDate, cutDate);
    }

    /**
     * @param queryObject     QueryObject
     * @param type            Type
     * @param count           if is desired a count query
     * @param index           The Index
     * @param numTotalPage    The Total number of rows of the complete resultset
     * @param isPendingChange If there is a pending change
     * @param template        The template
     * @param initialDate     The initial date
     * @param cutDate         The current date
     * @return StringBuilder
     */

    private StringBuilder buildQueryEnhanced(QueryObject queryObject, String type, boolean count,
                                            int index, int numTotalPage, boolean isPendingChange,
                                            String template, String initialDate, String cutDate) {
        StringBuilder query = new StringBuilder();
        StringBuilder queryMainTable = new StringBuilder().append("");
        boolean isTableFilled = false;
        boolean isThirdParty;
        Enumeration parameters = queryObject.elements();
        java.sql.Date searchDate = new java.sql.Date(System.currentTimeMillis());
        String atFields[] = {PRODUCT_ID};
        String vtFields[] = {"agregatedobjectid", "dcoid", "stateid", "initialDate", "finishDate",
                             "time_stamp", "sub_status"};
        String versionTable = AggregateObject.getTableVersion(AggregateObject.POLICY);
        String versionTableAlias = AggregateObject.getTableAlias(versionTable);
        String queryFieldsWithOutAlias;
        queryFieldsWithOutAlias = StringUtil.unsplit(atFields, ", ");
        queryFieldsWithOutAlias += ", ";
        queryFieldsWithOutAlias += StringUtil.unsplit(vtFields, ", ");

        queryMainTable.append("INSERT INTO ").append(temporarySearchTables.get("MSQ1").toString()).append(" ");
        StringBuffer universalQuery = new StringBuffer(
                AggregatedManager.getUniversalVersionQueryByAggregated(AggregateObject.POLICY,
                                                                       atFields, vtFields,
                                                                       isPendingChange, count,
                                                                       Versionable.STATUS_APPLIED,null));

        if (!StringUtil.isEmptyOrNullValue(initialDate) &&
            !StringUtil.isEmptyOrNullValue(cutDate)) {
            String cancelledStates = getCancelledStates();
            String buff = !StringUtil.isEmptyOrNullValue(cancelledStates) ?
                          " AND " + versionTableAlias + ".stateid NOT IN " + cancelledStates : "";

            DBTranslator dbTranslator = DBTranslatorFactory.getDBClass();
            universalQuery.append(" AND ").append(versionTableAlias).append(".FINISHDATE BETWEEN ")
                    .append(dbTranslator.getDateFormat(initialDate, "yyyy-MM-dd")).append(" AND ")
                    .append(dbTranslator.getDateFormat(cutDate, "yyyy-MM-dd"))
                    .append(buff);
        }
        queryMainTable.append(universalQuery);

        while (parameters.hasMoreElements()) {
            QueryParameter queryParameter = (QueryParameter) parameters.nextElement();
            isThirdParty = CotType.THIRDPARTY.getDescription().equals(queryParameter.getCategory());
            if (isThirdParty && template.equalsIgnoreCase("ThirdPartyTemplate")) {
                isTableFilled = insertIntoTemporaryTablesForThirdParties(queryParameter,
                                                                         queryMainTable,
                                                                         searchDate);
            } else if (!isThirdParty && !template.equalsIgnoreCase("ThirdPartyTemplate")) {
                isTableFilled = insertIntoTemporaryTablesForPolicy(queryParameter, queryMainTable,
                                                                   searchDate);
            }
        }

        if (isTableFilled) {
            query.append("SELECT ").append(count ? "count(1) as cnt" : "*").append(" FROM ") // DB2 Certification
                    .append(temporarySearchTables.get("SQT1").toString())
                    .append(" WHERE 1=1 ");

            AggregatedManager.addQueryPostfix(type, query, versionTableAlias, index,
                                              queryFieldsWithOutAlias, numTotalPage, !count,false, null);
        }
        log.debug("[buildQueryEnhanced] query: " + query);
        return query;
    }


    /**
     * @param queryObject     String
     * @param type            The page requested
     * @param count           Indicates if the request is for count of the compelte resultset or for a simple page
     * @param isPendingChange If there is a pending change
     * @param initialDate     The initial date
     * @param cutDate         The current date
     * @return StringBuilder
     */
    private StringBuilder buildQueryConventional(QueryObject queryObject, String type, boolean count,
                                                boolean isPendingChange, String initialDate,
                                                String cutDate, int status) {

        return buildQueryConventional(queryObject, type, count, 0, 0, isPendingChange, initialDate,
                                      cutDate, status);
    }


    /**
     * @param queryObject     QueryObject
     * @param type            Type
     * @param count           if is desired a count query
     * @param index           The Index
     * @param numTotalPage    The Total number of rows of the complete resultset
     * @param isPendingChange If there is a pending change
     * @param initialDate     The initial date
     * @param cutDate         The current date
     * @return StringBuilder
     */

    private StringBuilder buildQueryConventional(QueryObject queryObject, String type, boolean count,
                                                int index, int numTotalPage,
                                                boolean isPendingChange, String initialDate,
                                                String cutDate, int status) {
        Enumeration parameters = queryObject.elements();
        java.sql.Date searchDate = new java.sql.Date(System.currentTimeMillis());
        String atFields[] = {PRODUCT_ID};
        String vtFields[] = {"agregatedobjectid", "dcoid", "stateid", "initialDate", "finishDate", "time_stamp", "sub_status"};
        String ctFields[] = {"auditdate"};

        String versionTable = AggregateObject.getTableVersion(AggregateObject.POLICY);
        String versionTableAlias = AggregateObject.getTableAlias(versionTable);
        String aggregatedTable = AggregateObject.getTable(AggregateObject.POLICY);
        String aggregatedTableAlias = AggregateObject.getTableAlias(aggregatedTable);
        String contextOperation=AggregateObject.getTable(AggregateObject.CONTEXT_OPERATION);
        String contextOperationAlias=AggregateObject.getTableAlias(contextOperation);

        long[] state = AggregatedManager.getStatesIdsByDescription(RequestUtil.getValue(context.getRequest(), "_stateProductId"));

        String crmNumberCase = StringUtil.defaultIfEmptyOrNull(RequestUtil.getValue(context.getRequest(), "_crmNumberCase"), null);

        String queryFields;

        queryFields = aggregatedTableAlias + "." + StringUtil.unsplit(atFields, ", " + aggregatedTableAlias + ".");
        queryFields += ", ";
        queryFields += versionTableAlias + "." + StringUtil.unsplit(vtFields, ", " + versionTableAlias + ".");
        queryFields += ", ";
        queryFields += contextOperationAlias + "." + StringUtil.unsplit(ctFields, ", " + contextOperationAlias + ".");

        boolean isThirdParty;
        boolean isOk = true;
        boolean isFacultative = false;
        String isSearchPolicyCreateFacultative="";
        StringBuilder universalQueryThirdParty = new StringBuilder();

        QueryParameter queryParameterThirdParty = null;
        while (parameters.hasMoreElements()) {
            QueryParameter queryParameter = (QueryParameter) parameters.nextElement();

            isThirdParty = CotType.THIRDPARTY.getDescription().equals(queryParameter.getCategory());
            if (isThirdParty) {
                universalQueryThirdParty
                        .append((SearchUtil.buildQueryForThirdParties(queryParameter)).toString());

            } else {
                universalQueryThirdParty.append(buildQueryForPolicies(queryParameter, searchDate));
            }
            if(isOk){
                queryParameterThirdParty = queryParameter;
                isOk = false;
            }
        }

        if (!(StringUtil.isEmptyOrNullValue(String.valueOf(context.getSession().getAttribute("isSearchPolicyCreateFacultative"))))){
            if (context.getSession().getAttribute("isSearchPolicyCreateFacultative").equals("true")) {
                isSearchPolicyCreateFacultative ="false";
                context.getSession().removeAttribute("isSearchPolicyCreateFacultative");
            }
        }
        if(!(StringUtil.isEmptyOrNullValue(queryObject.getParameterValue("isFacultative")))) {
            if (queryObject.getParameterValue("isFacultative").equals("true")) {
                isFacultative = true;
            }
        }else if(!(StringUtil.isEmptyOrNullValue(queryObject.getParameterValue("isReinsurance")))){
            if (queryObject.getParameterValue("isReinsurance").equals("true")) {
                queryParameterThirdParty.setParameter("isReinsurance", queryObject.getParameterValue("isReinsurance"));
            }
        }

        StringBuilder universalQuery;
        if(isFacultative) {
            universalQuery = new StringBuilder(
                    AggregatedManager.getUniversalVersionQueryByAggregated(AggregateObject.POLICY,
                            atFields, vtFields, ctFields,
                            isPendingChange, count, status, state, queryParameterThirdParty, isFacultative));
        }else {
            universalQuery = new StringBuilder(
                    AggregatedManager.getUniversalVersionQueryByAggregated(AggregateObject.POLICY,
                            atFields, vtFields, ctFields,
                            isPendingChange, count, status, state, queryParameterThirdParty,isSearchPolicyCreateFacultative));
        }

        if (!StringUtil.isEmptyOrNullValue(initialDate) &&
            !StringUtil.isEmptyOrNullValue(cutDate)) {
            String cancelledStates = getCancelledStates();
            String buff = !StringUtil.isEmptyOrNullValue(cancelledStates) ?
                          " AND " + versionTableAlias + ".stateid NOT IN " + cancelledStates : "";

            DBTranslator dbTranslator = DBTranslatorFactory.getDBClass();
            universalQuery.append(" AND ").append(versionTableAlias).append(".FINISHDATE BETWEEN ")
                    .append(dbTranslator.getDateFormat(initialDate, "yyyy-MM-dd")).append(" AND ")
                    .append(dbTranslator.getDateFormat(cutDate, "yyyy-MM-dd"))
                    .append(buff);
        }

        while (parameters.hasMoreElements()) {
            QueryParameter queryParameter = (QueryParameter) parameters.nextElement();

            isThirdParty = CotType.THIRDPARTY.getDescription().equals(queryParameter.getCategory());
            if (isThirdParty) {
                universalQuery.append((SearchUtil.buildQueryForThirdParties(queryParameter)).toString());
            } else {
                universalQuery.append(buildQueryForPolicies(queryParameter, searchDate));
            }
        }
        AggregatedManager.addQueryPostfix(type, universalQuery.append(universalQueryThirdParty),
                versionTableAlias, index,
                queryFields, numTotalPage,
                !count, true, crmNumberCase);
        log.debug("[buildQueryConventional] query: " + universalQuery);
        return universalQuery;
    }

    /**
     * @param queryParameter QueryParameter
     * @param queryMainTable StringBuilder
     * @param searchDate     java.sql.Date
     * @return boolean
     */
    private boolean insertIntoTemporaryTablesForPolicy(QueryParameter queryParameter,
                                                       StringBuilder queryMainTable,
                                                       java.sql.Date searchDate) {

        SimpleDateFormat sdf = DateUtil.sdft;
        java.util.Date globalinitialDate;
        java.util.Date globalfinalDate;
        java.util.Date initialDate;
        java.util.Date finalDate;
        boolean cont = true;
        if (askIfTableIsEmpty(new StringBuilder().append(temporarySearchTables.get("SQT1")))) {
            int clientNameSubQueryResult = 0;
            int templateSubQueryResult = 0;
            int insuredNameSubQueryResult = 0;
            int queryMainTableResult;
            int searchTempResult = 0;
            boolean emptyParameters;
            String productId = (String) queryParameter.getParameter("_productId");
            queryParameter.removeParameter("_productId");
            String clientName = (String) queryParameter.getParameter("_clientName");
            queryParameter.removeParameter("_clientName");
            String insuredName = (String) queryParameter.getParameter("_insuredName");
            queryParameter.removeParameter("_insuredName");
            String template = (String) queryParameter.getParameter("template");
            queryParameter.removeParameter("template");
            String category = queryParameter.getCategory();
            CotType cotType = CotType.getCotType(category);
            ConfigurableObjectType cot;

            if (defaultTemplates.get(cotType, template) == null) {
                cot = (ConfigurableObjectType) extendedTemplates.get(cotType, template);
            } else {
                cot = (ConfigurableObjectType) defaultTemplates.get(cotType, template);
            }

            String dcoQuery = SearchUtil.getDefaultConfigurableObjectQuery(cot, queryParameter, false);
            emptyParameters = StringUtil.isEmptyOrNullValue(clientName) &&
                              StringUtil.isEmptyOrNullValue(insuredName) &&
                              StringUtil.isEmptyOrNullValue(productId) &&
                              StringUtil.isEmptyOrNullValue(dcoQuery);
            globalinitialDate = new java.util.Date();
            log.debug("[insertIntoTemporaryTablesForPolicy] ******* Data Insersion Begins at: " +
                      sdf.format(globalinitialDate) + " *******");

            if (!StringUtil.isEmptyOrNullValue(dcoQuery)) {
                StringBuffer templateSubQuery = new StringBuffer();
                templateSubQuery.append("INSERT INTO ");
                templateSubQuery.append(temporarySearchTables.get("TSQ1").toString());
                templateSubQuery.append(" ");
                templateSubQuery.append(dcoQuery);
                templateSubQuery.append(" ORDER BY obj.pk ASC");
                log.debug("[insertIntoTemporaryTablesForPolicy] Inserting into Auxiliary Table templateSubQuery (ST_TSQ): " + templateSubQuery);
                initialDate = new java.util.Date();
                templateSubQueryResult = doUpdate(templateSubQuery.toString());
                finalDate = new java.util.Date();
                log.debug("[insertIntoTemporaryTablesForPolicy] ******* Number of records: " +
                          templateSubQueryResult + " | inserted in: " +
                          sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                          " seconds");
                cont = templateSubQueryResult != 0;
            }
            if ((!StringUtil.isEmptyOrNullValue(clientName) && cont) || emptyParameters) {
                StringBuffer clientNameSubQuery = new StringBuffer();
                clientNameSubQuery.append("INSERT INTO ");
                clientNameSubQuery.append(temporarySearchTables.get("CSQ1").toString());
                clientNameSubQuery.append(" ");
                String tableVersion = AggregateObject.
                        getTableVersion(AggregateObject.POLICY_PARTICIPATION);
                String tableObject = AggregateObject.
                        getTable(AggregateObject.POLICY_PARTICIPATION);
                StringBuffer queryUniversalParticipationOrdered =
                        getUniversalParticipationQueryOrdered(clientName != null ? clientName : "",
                                                              ThirdpartyUtil.getClientRoles(),
                                                              searchDate, tableVersion,
                                                              tableObject);
                clientNameSubQuery.append(queryUniversalParticipationOrdered);
                log.debug(
                        "[insertIntoTemporaryTablesForPolicy] Inserting into Auxiliary Table clientNameSubQuery (ST_CSQ): " +
                        clientNameSubQuery);
                initialDate = new java.util.Date();
                clientNameSubQueryResult = doUpdate(clientNameSubQuery.toString());
                finalDate = new java.util.Date();
                log.debug("[insertIntoTemporaryTablesForPolicy] ******* Number of records: " +
                          clientNameSubQueryResult + " | inserted in: " +
                          sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                          " seconds");
                cont = clientNameSubQueryResult != 0;
            }
            if ((!StringUtil.isEmptyOrNullValue(insuredName) && cont) || emptyParameters) {
                StringBuffer insuredNameSubQuery1 = new StringBuffer();
                insuredNameSubQuery1.append("INSERT INTO ");
                insuredNameSubQuery1.append(temporarySearchTables.get("ISQ1").toString());
                insuredNameSubQuery1.append(" ");
                insuredNameSubQuery1.append(AggregatedManager
                        .getUniversalVersionQueryByAggregatedOrdered(AggregateObject.RISK_UNIT, null, new String[]{
                        "agregatedObjectId, agregatedparentid"}));
                log.debug("[insertIntoTemporaryTablesForPolicy] Inserting into Auxiliary Table insuredNameSubQuery1 (ST_ISQ1): " + insuredNameSubQuery1);
                initialDate = new java.util.Date();
                insuredNameSubQueryResult = doUpdate(insuredNameSubQuery1.toString());
                finalDate = new java.util.Date();
                log.debug("[insertIntoTemporaryTablesForPolicy] ******* Number of records: " +
                          insuredNameSubQueryResult + " | inserted in: " +
                          sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                          " seconds");
                cont = insuredNameSubQueryResult != 0;
                if (cont) {
                    StringBuffer insuredNameSubQuery2 = new StringBuffer();
                    insuredNameSubQuery2.append("INSERT INTO ");
                    insuredNameSubQuery2.append(temporarySearchTables.get("ISQ2").toString());
                    insuredNameSubQuery2.append(" ");
                    insuredNameSubQuery2.append(AggregatedManager
                            .getUniversalVersionQueryByAggregatedOrdered(
                            AggregateObject.INSURANCE_OBJECT, null,
                            new String[]{"agregatedObjectId, agregatedparentid"}));
                    log.debug(
                            "[insertIntoTemporaryTablesForPolicy] Inserting into Auxiliary Table insuredNameSubQuery2 (ST_ISQ2): " +
                            insuredNameSubQuery2);
                    initialDate = new java.util.Date();
                    insuredNameSubQueryResult = doUpdate(insuredNameSubQuery2.toString());
                    finalDate = new java.util.Date();
                    log.debug("[insertIntoTemporaryTablesForPolicy] ******* Number of records: " +
                              insuredNameSubQueryResult + " | inserted in: " +
                              sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                              " seconds");
                    cont = insuredNameSubQueryResult != 0;
                }
                if (cont) {
                    StringBuffer insuredNameSubQuery3 = new StringBuffer();
                    insuredNameSubQuery3.append("INSERT INTO ");
                    insuredNameSubQuery3.append(temporarySearchTables.get("ISQ3").toString());
                    insuredNameSubQuery3.append(" ");
                    String tableVersion = AggregateObject
                            .getTableVersion(AggregateObject.INSURANCE_OBJECT_PARTICIPATION);
                    String tableObject = AggregateObject
                            .getTable(AggregateObject.INSURANCE_OBJECT_PARTICIPATION);
                    StringBuffer queryUniversalParticipationOrdered =
                            getUniversalParticipationQueryOrdered(
                                    insuredName != null ? insuredName : "",
                                    ThirdpartyUtil.getInsuredRoles(), searchDate, tableVersion,
                                    tableObject);
                    insuredNameSubQuery3.append(queryUniversalParticipationOrdered);
                    log.debug(
                            "[insertIntoTemporaryTablesForPolicy] Inserting into Auxiliary Table insuredNameSubQuery3 (ST_ISQ3): " +
                            insuredNameSubQuery3);
                    initialDate = new java.util.Date();
                    insuredNameSubQueryResult = doUpdate(insuredNameSubQuery3.toString());
                    finalDate = new java.util.Date();
                    log.debug("[insertIntoTemporaryTablesForPolicy] ******* Number of records: " +
                              insuredNameSubQueryResult + " | inserted in: " +
                              sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                              " seconds");
                    cont = insuredNameSubQueryResult != 0;
                }
            }
            if (cont || emptyParameters) {
                if (!StringUtil.isEmptyOrNullValue(productId)) {
                    queryMainTable.append(" AND productid = ").append(productId);
                }
                queryMainTable.append(" ORDER BY agregatedobjectid ASC");
                log.debug(
                        "[insertIntoTemporaryTablesForPolicy] Inserting into Auxiliary Table MasterTable (ST_MSQ): " +
                        queryMainTable);
                initialDate = new java.util.Date();
                queryMainTableResult = doUpdate(queryMainTable.toString());
                finalDate = new java.util.Date();
                log.debug("[insertIntoTemporaryTablesForPolicy] ******* Number of records: " +
                          queryMainTableResult + " | inserted in: " +
                          sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                          " seconds");
                cont = queryMainTableResult != 0;
                if (cont) {
                    StringBuffer SearchTemp = new StringBuffer();
                    SearchTemp.append("INSERT INTO ");
                    SearchTemp.append(temporarySearchTables.get("SQT1").toString());
                    SearchTemp.append(" SELECT ");
                    SearchTemp.append(temporarySearchTables.get("MSQ1").toString());
                    SearchTemp.append(".* ");
                    SearchTemp.append("  FROM ");
                    SearchTemp.append(temporarySearchTables.get("MSQ1").toString());
                    if (clientNameSubQueryResult != 0) {
                        SearchTemp.append(",");
                        SearchTemp.append(temporarySearchTables.get("CSQ1").toString());
                    }
                    if (insuredNameSubQueryResult != 0) {
                        SearchTemp.append(", ");
                        SearchTemp.append(temporarySearchTables.get("ISQ1").toString());
                        SearchTemp.append(", ");
                        SearchTemp.append(temporarySearchTables.get("ISQ2").toString());
                        SearchTemp.append(", ");
                        SearchTemp.append(temporarySearchTables.get("ISQ3").toString());
                    }
                    if (templateSubQueryResult != 0) {
                        SearchTemp.append(", ");
                        SearchTemp.append(temporarySearchTables.get("TSQ1").toString());
                    }
                    SearchTemp.append(" WHERE 1=1 ");
                    if (clientNameSubQueryResult != 0) {
                        SearchTemp.append(" AND ")
                                .append(temporarySearchTables.get("MSQ1").toString())
                                .append(".agregatedobjectid = ")
                                .append(temporarySearchTables.get("CSQ1").toString())
                                .append(".agregatedparentid");
                    }
                    if (insuredNameSubQueryResult != 0) {
                        SearchTemp.append(" AND ")
                                .append(temporarySearchTables.get("MSQ1").toString())
                                .append(".agregatedobjectid = ")
                                .append(temporarySearchTables.get("ISQ1").toString())
                                .append(".agregatedparentid");
                        SearchTemp.append(" AND ")
                                .append(temporarySearchTables.get("ISQ1").toString())
                                .append(".agregatedobjectid = ")
                                .append(temporarySearchTables.get("ISQ2").toString())
                                .append(".agregatedparentid");
                        SearchTemp.append(" AND ")
                                .append(temporarySearchTables.get("ISQ2").toString())
                                .append(".agregatedobjectid = ")
                                .append(temporarySearchTables.get("ISQ3").toString())
                                .append(".agregatedparentid");
                    }
                    if (templateSubQueryResult != 0) {
                        SearchTemp.append("   AND ")
                                .append(temporarySearchTables.get("MSQ1").toString())
                                .append(".dcoid = ")
                                .append(temporarySearchTables.get("TSQ1").toString())
                                .append(".pk");
                    }

                    SearchTemp.append(" ORDER BY ")
                            .append(temporarySearchTables.get("MSQ1").toString())
                            .append(".initialdate DESC");
                    log.debug(
                            "[insertIntoTemporaryTablesForPolicy] Inserting into Auxiliary Table SearchTemp (ST_SQT): " +
                            SearchTemp);
                    initialDate = new java.util.Date();
                    searchTempResult = doUpdate(SearchTemp.toString());
                    finalDate = new java.util.Date();
                    log.debug("******* Number of records: " + searchTempResult +
                              " | inserted in: " +
                              sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                              " seconds");
                }
                cont = searchTempResult != 0;
                globalfinalDate = new java.util.Date();
                log.debug("[insertIntoTemporaryTablesForPolicy] ******* Data Insertion Ends at: " +
                          sdf.format(globalfinalDate) + " *******");
                log.debug(
                        "[insertIntoTemporaryTablesForPolicy] ******* Data Insertion elapsed time: " +
                        sdf.format(
                                new Time(globalfinalDate.getTime() - globalinitialDate.getTime())) +
                                                                                                   " seconds " +
                                                                                                   "*******");
                deleteDataFromIntermediateTables();
            }
        } else {
            log.debug(
                    "[insertIntoTemporaryTablesForThirdParties] ****** The table 'ST_SQT' was previously " +
                    "filled in the first request executed. The no Auxiliary tables was needed. The request used " +
                    "no join between any temporarySearchTables, instead of that, it'd paginated over the pre-filled " +
                    "('ST_SQT') table ******");
        }
        return cont;
    }

    /**
     * This method improves the thirdparty search process, using auxiliary tables to hold
     * intermediary subquery values. It's based on the method createTablesQuery, which does the same
     * but in the policy search.
     *
     * @param queryParameter The parameters for querying
     * @param queryMainTable The main query
     * @param searchDate     The initial date for querying
     * @return true if tables were created, false otherwise
     */
    private boolean insertIntoTemporaryTablesForThirdParties(QueryParameter queryParameter,
                                                             StringBuilder queryMainTable,
                                                             java.sql.Date searchDate) {

        SimpleDateFormat sdf = DateUtil.sdft;
        java.util.Date globalinitialDate;
        java.util.Date globalfinalDate;
        java.util.Date initialDate;
        java.util.Date finalDate;
        boolean cont = true;


        if (askIfTableIsEmpty(new StringBuilder().append(temporarySearchTables.get("SQT1")))) {
            int searchTempResult = 0;
            int participationSubQueryResult;
            int queryMainTableResult;
            int templateSubQueryResult;
            boolean emptyParameters;

            String participationName = (String) queryParameter.getParameter("_participation");
            log.debug("[insertIntoTemporaryTablesForThirdParties] participationName: " +
                      participationName);
            queryParameter.removeParameter("_participation");
            String roleName = (String) queryParameter.getParameter("_role");
            log.debug("[insertIntoTemporaryTablesForThirdParties] roleName: " + roleName);
            queryParameter.removeParameter("_role");
            String template = (String) queryParameter.getParameter("template");
            String category = queryParameter.getCategory();
            CotType cotType = CotType.getCotType(category);
            ConfigurableObjectType cot;

            if (defaultTemplates.get(cotType, template) == null) {
                cot = (ConfigurableObjectType) extendedTemplates.get(cotType, template);
            } else {
                cot = (ConfigurableObjectType) defaultTemplates.get(cotType, template);
            }
            boolean isLike = AcseleConf.getProperty("search.like").equals(AcseleConstants.TRUE)?true:false;
            String dcoQuery =
                    SearchUtil.getDefaultConfigurableObjectQuery(cot, queryParameter, isLike);
            emptyParameters = StringUtil.isEmptyOrNullValue(participationName) &&
                              StringUtil.isEmptyOrNullValue(roleName) &&
                              StringUtil.isEmptyOrNullValue(dcoQuery);

            globalinitialDate = new java.util.Date();
            log.debug(
                    "[insertIntoTemporaryTablesForThirdParties] ******* Data Insersion Begins at: " +
                    sdf.format(globalinitialDate) + " *******");
            if (!StringUtil.isEmptyOrNullValue(dcoQuery)) {
                StringBuffer templateSubQuery = new StringBuffer();
                templateSubQuery.append("INSERT INTO ");
                templateSubQuery.append(temporarySearchTables.get("TSQ1").toString());
                templateSubQuery.append(" ");
                templateSubQuery.append(dcoQuery);
                templateSubQuery.append(" ORDER BY obj.pk ASC");
                log.debug(
                        "[insertIntoTemporaryTablesForThirdParties] Inserting into Auxiliary Table templateSubQuery (ST_TSQ): " +
                        templateSubQuery);
                initialDate = new java.util.Date();
                templateSubQueryResult = doUpdate(templateSubQuery.toString());
                finalDate = new java.util.Date();
                log.debug("[insertIntoTemporaryTablesForThirdParties] ******* Number of records: " +
                          templateSubQueryResult + " | inserted in: " +
                          sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                          " seconds");
                cont = templateSubQueryResult != 0;
            }
            if (cont || emptyParameters) {
                StringBuffer subQuery1 = new StringBuffer();
                subQuery1.append(makeThirdPartyQueryHeader(dcoQuery).toString());
                String queryOrderedThirdPartyForPartPol =
                        getUniversalQueryOrderedThirdPartyForPartObj(searchDate, "PC1", "PC", "PCO",
                                                                     roleName, true,
                                                                     "STPO_POLICYPARTICIPATION",
                                                                     "STPO_POLICYPARTICIPATIONDCO");
                subQuery1.append(" AND (agregatedpolicy.agregatedpolicyid IN ")
                        .append(queryOrderedThirdPartyForPartPol);
                subQuery1.append("))");
                StringBuffer subQuery2 = new StringBuffer();
                subQuery2.append(makeThirdPartyQueryHeader(dcoQuery).toString());
                subQuery2.append(" AND (agregatedpolicy.agregatedpolicyid IN ")
                        .append(getUniversalQueryOrderedThirdParty(searchDate, "rudco1", "rudco",
                                                                   "RiskUnitDCO", roleName, false));
                subQuery2.append("))");
                if ((participationName != null) && participationName.equalsIgnoreCase("policy")) {
                    log.debug(
                            "[insertIntoTemporaryTablesForThirdParties] Inserting into Auxiliary Table subQuery1(ST_ISQ3): " +
                            subQuery1);
                    initialDate = new java.util.Date();
                    participationSubQueryResult = doUpdate(subQuery1.toString());
                    finalDate = new java.util.Date();
                    log.debug(
                            "[insertIntoTemporaryTablesForThirdParties] ******* Number of records: " +
                            participationSubQueryResult + " | inserted in: " +
                            sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                            " seconds");
                } else
                if ((participationName != null) && participationName.equalsIgnoreCase("riskunit")) {
                    log.debug(
                            "[insertIntoTemporaryTablesForThirdParties] Inserting into Auxiliary Table subQuery2(ST_ISQ3): " +
                            subQuery2);
                    initialDate = new java.util.Date();
                    participationSubQueryResult = doUpdate(subQuery2.toString());
                    finalDate = new java.util.Date();
                    log.debug(
                            "[insertIntoTemporaryTablesForThirdParties] ******* Number of records: " +
                            participationSubQueryResult + " | inserted in: " +
                            sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                            " seconds");
                } else {
                    //Si se entra por aqu? es porque el usuario no especific? la participaci?n.
                    log.debug(
                            "[insertIntoTemporaryTablesForThirdParties] Inserting into Auxiliary Table subQuery1(ST_ISQ3): " +
                            subQuery1);
                    initialDate = new java.util.Date();
                    participationSubQueryResult = doUpdate(subQuery1.toString());
                    finalDate = new java.util.Date();
                    log.debug(
                            "[insertIntoTemporaryTablesForThirdParties] ******* Number of records: " +
                            participationSubQueryResult + " | inserted in: " +
                            sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                            " seconds");

                    log.debug(
                            "[insertIntoTemporaryTablesForThirdParties] Inserting into Auxiliary Table subQuery2(ST_ISQ3): " +
                            subQuery2);
                    initialDate = new java.util.Date();
                    participationSubQueryResult += doUpdate(subQuery2.toString());
                    finalDate = new java.util.Date();
                    log.debug(
                            "[insertIntoTemporaryTablesForThirdParties] ******* Number of records: " +
                            participationSubQueryResult + " | inserted in: " +
                            sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                            " seconds");
                }
                cont = participationSubQueryResult != 0;
            }

            if (cont || emptyParameters) {
                //Se agregan los par?metros finales de la b?squeda (si existen)

                log.debug(
                        "[insertIntoTemporaryTablesForThirdParties] Inserting into Auxiliary Table MasterTable (ST_MSQ): " +
                        queryMainTable);
                initialDate = new java.util.Date();
                queryMainTableResult = doUpdate(queryMainTable.toString());
                finalDate = new java.util.Date();
                log.debug("[insertIntoTemporaryTablesForThirdParties] ******* Number of records: " +
                          queryMainTableResult + " | inserted in: " +
                          sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                          " seconds");
                cont = queryMainTableResult != 0;
                if (cont) {
                    StringBuffer SearchTemp = new StringBuffer();
                    SearchTemp.append("INSERT INTO ");
                    SearchTemp.append(temporarySearchTables.get("SQT1").toString());
                    SearchTemp.append(" SELECT ");
                    SearchTemp.append(temporarySearchTables.get("MSQ1").toString());
                    SearchTemp.append(".* ");
                    SearchTemp.append("  FROM ");
                    SearchTemp.append(temporarySearchTables.get("MSQ1").toString());
                    SearchTemp.append(", ").append(temporarySearchTables.get("ISQ3"));
                    SearchTemp.append(" WHERE ").append(temporarySearchTables.get("MSQ1"))
                            .append(".agregatedobjectid")
                            .append(" = ").append(temporarySearchTables.get("ISQ3"))
                            .append(".agregatedparentid");
                    SearchTemp.append(" ORDER BY ")
                            .append(temporarySearchTables.get("MSQ1").toString())
                            .append(".initialdate DESC");
                    log.debug(
                            "[insertIntoTemporaryTablesForThirdParties] Inserting into Auxiliary Table SearchTemp (ST_SQT): " +
                            SearchTemp);
                    initialDate = new java.util.Date();
                    searchTempResult = doUpdate(SearchTemp.toString());
                    finalDate = new java.util.Date();
                    log.debug(
                            "[insertIntoTemporaryTablesForThirdParties] ******* Number of records: " +
                            searchTempResult + " | inserted in: " +
                            sdf.format(new Time(finalDate.getTime() - initialDate.getTime())) +
                            " seconds");
                }
                cont = searchTempResult != 0;
                globalfinalDate = new java.util.Date();
                log.debug(
                        "[insertIntoTemporaryTablesForThirdParties] ******* Data Insertion Ends at: " +
                        sdf.format(globalfinalDate) + " *******");
                log.debug(
                        "[insertIntoTemporaryTablesForThirdParties] ******* Data Insertion elapsed time: " +
                        sdf.format(
                                new Time(globalfinalDate.getTime() - globalinitialDate.getTime())) +
                                                                                                   " seconds " +
                                                                                                   "*******");
                deleteDataFromTables();
            } else {
                log.debug(
                        "[insertIntoTemporaryTablesForThirdParties] ****** The table 'ST_SQT' was previously " +
                        "filled in the first request executed. The no Auxiliary tables was needed. The request used " +
                        "no join between any temporarySearchTables, instead of that, it'd paginated over the pre-filled " +
                        "('ST_SQT') table ******");
            }
        }
        return cont;
    }

    /**
     * @param queryParameter QueryParameter
     * @param searchDate     Date
     * @return StringBuffer
     */
    private StringBuilder buildQueryForPolicies(QueryParameter queryParameter,
                                               java.sql.Date searchDate) {

        String[] parentField = new String[]{"agregatedParentId"};
        String category = queryParameter.getCategory();
        StringBuilder queryAll = new StringBuilder(1000);
        //si la linea productId = RequestUtil.getValue(context.getRequest(), "_productId"); (linea 223 aprox) tiene un
        // valor, y se llama a este metodo por busqueda convencional,
        //la linea queryParameter.getParameter("_productId") dara un ClassCastException. El ticket que origino este caso es ACSELE-6026.
        // Como solucion temporal se chequeara si productId es null (MSY)
        if (StringUtil.isEmptyOrNullValue(productId)) {
            productId = (String) queryParameter.getParameter("_productId");
        }
        String policyId = (String) queryParameter.getParameter("_policyId");
        String clientName = queryParameter.getParameter("_clientName") == null?
                null:queryParameter.getParameter("_clientName") instanceof String?
                (String) queryParameter.getParameter("_clientName"):
                (String) ((HashMap) queryParameter.getParameter("_clientName")).get("_clientName0");
        String insuredName = queryParameter.getParameter("_insuredName") == null?
                null:queryParameter.getParameter("_insuredName") instanceof String?
                (String) queryParameter.getParameter("_insuredName"):
                (String) ((HashMap) queryParameter.getParameter("_insuredName")).get("_insuredName0");
        String template = (String) queryParameter.getParameter("template");
        String fromdate = (String) queryParameter.getParameter("_fromDateShow");
        String todate = (String) queryParameter.getParameter("_toDateShow");
        CotType cotType = CotType.getCotType(category);

        ConfigurableObjectType cot;
        if (defaultTemplates.get(cotType, template) == null) {
            cot = (ConfigurableObjectType) extendedTemplates.get(cotType, template);
        } else {
            cot = (ConfigurableObjectType) defaultTemplates.get(cotType, template);
        }

        Enumeration keys = queryParameter.keys();
        ConfigurableObjectType defaultTemplate = SearchUtil.getDefaultTemplate(cotType);
        String defaultTemplateName = defaultTemplate.toString();
        boolean isUsingDinamicTemplate =
                template != null && !defaultTemplateName.equalsIgnoreCase(template);
        boolean thereAreParameters = keys.hasMoreElements() || isUsingDinamicTemplate;

        String pAlias = AggregateObject
                .getTableAlias(AggregateObject.getTableVersion(AggregateObject.POLICY));

        String aggregatedTable = AggregateObject.getTable(AggregateObject.POLICY);
        String aggregatedTableAlias = AggregateObject.getTableAlias(aggregatedTable);

        if (thereAreParameters && !category.equalsIgnoreCase(CotType.POLICY.getDescription())) {
            queryAll.append(" AND ").append(pAlias).append(".agregatedobjectid in (");
        }

        if (category.equalsIgnoreCase(CotType.POLICY.getDescription())) {
            if (!StringUtil.isEmptyOrNullValue(productId)) {
                queryAll.append(" AND productid IN (").append(productId).append(") ");
            }
            if (policyId != null) {
                queryAll.append(" AND ").append(aggregatedTableAlias).append(".agregatedpolicyid = ").append(policyId).append(" ");
            }

            if (fromdate != null) {
                queryAll.append(" AND pdcoctx.time_stamp >= to_date('").append(fromdate).append("','dd-mm-yyyy') ");
            }

            if (todate != null) {
                queryAll.append(" AND pdcoctx.time_stamp <= to_date('").append(todate).append("','dd-mm-yyyy') ");
            }

            if (clientName != null) {
                queryAll.append(getPolicyParticipationQuery(clientName,
                                                            ThirdpartyUtil.getClientRoles(),
                                                            searchDate));
                queryAll.append(" AND rownum <= 100 ");
            }

            if (insuredName != null) {
                queryAll.append(getInsuranceObjectParticipationQuery(insuredName,
                                                                     ThirdpartyUtil.getInsuredRoles(),
                                                                     searchDate));
                queryAll.append(" AND rownum <= 100 ");
            }

        } else
        if (thereAreParameters && category.equalsIgnoreCase(CotType.RISK_UNIT.getDescription())) {
            queryAll.append(AgregatedRiskUnitManager.getQueryByDate(searchDate, parentField, null));
        } else if (thereAreParameters &&
                   category.equalsIgnoreCase(CotType.INSURANCE_OBJECT.getDescription())) {
            queryAll.append("SELECT agregatedriskunit.agregatedpolicyid ");
            queryAll.append(" FROM agregatedriskunit ");
            queryAll.append(" WHERE agregatedriskunitid IN (");
            queryAll.append(
                    AgregatedInsuranceObjectManager.getQueryByDate(searchDate, parentField));
        }

        String dcoQuery = SearchUtil.getDefaultConfigurableObjectQuery(cot, queryParameter, false);

        if (dcoQuery != null) {
            queryAll.append(" AND dcoid IN (").append(dcoQuery).append(")");
        }

        if (thereAreParameters &&
            category.equalsIgnoreCase(CotType.INSURANCE_OBJECT.getDescription())) {
            queryAll.append(")");
        }

        if (thereAreParameters && !category.equalsIgnoreCase(CotType.POLICY.getDescription())) {
            queryAll.append(")");
        }

        log.debug("[buildQueryForPolicies] query: " + queryAll);
        return queryAll;
    }


    private StringBuffer makeThirdPartyQueryHeader(String dcoQuery) {
        StringBuffer subQuery = new StringBuffer();    //Primera parte del query
        subQuery.append("INSERT INTO ");
        subQuery.append(temporarySearchTables.get("ISQ3").toString());
        subQuery.append(" ");
        subQuery.append("(SELECT DISTINCT agregatedpolicy.agregatedpolicyid ");
        subQuery.append(
                "FROM agregatedpolicy, STPO_POLICYPARTICIPATIONDCO pv, STPO_POLICYPARTICIPATION pvo");
        subQuery.append(!StringUtil.isEmptyOrNullValue(dcoQuery) ?
                        ", stte_thirdparty tp, " + temporarySearchTables.get("TSQ1") + " dco " :
                        "");
        subQuery.append(", ContextOperation ctx WHERE ctx.status = ");
        subQuery.append(Versionable.STATUS_APPLIED);
        subQuery.append(" AND pv.operationpk = ctx.ID pv.agregatedobjectid = pvo.agregatedobjectid");
        if (!StringUtil.isEmptyOrNullValue(dcoQuery)) {
            subQuery.append(" AND pvo.thirdpartyid = tp.TPT_Id ");
            subQuery.append(" AND tp.idDco = dco.pk");
        }

        log.debug("[makeThirdPartyQueryHeader] query: " + subQuery);
        return subQuery;
    }

    /**
     * Auxiliary method that helps the thirdparty query's construction.
     *
     * @param searchDate The search date
     * @param alias      Alias 1
     * @param alias2     Alias 2
     * @param tableName  The table name
     * @param roleName   The role name
     * @param flag       Flag
     * @return String
     */
    private String getUniversalQueryOrderedThirdParty(java.sql.Date searchDate, String alias,
                                                      String alias2, String tableName,
                                                      String roleName, boolean flag) {
        StringBuffer queryRes = new StringBuffer();
        DBTranslator dbTranslator = DBTranslatorFactory.getDBClass();
        queryRes.append("(SELECT agregatedparentid ")
                .append("FROM  ").append(tableName).append(" ").append(alias2)
                .append(", ContextOperation ctx WHERE  NOT EXISTS (");
        queryRes.append("SELECT 1 ");
        queryRes.append("FROM ").append(tableName).append(" ").append(alias);
        queryRes.append(", ContextOperation ctx1 WHERE ctx1.time_stamp <=");
        queryRes.append(dbTranslator.getDateFormat(DateUtil.getFormatLongShow().format(searchDate)));
        queryRes.append("AND ").append(alias).append(" ctx1.TIME_STAMP > ctx.TIME_STAMP");
        queryRes.append(" AND ctx1.status != ")
                .append(Versionable.STATUS_TEMPORARY);
        queryRes.append(" AND ").append(alias).append(".status != ")
                .append(Versionable.STATUS_REVERSED);
        queryRes.append(" AND ").append(alias).append(".agregatedobjectid = ")
                .append(alias2).append(".agregatedobjectid and ctx.ID = ").append(alias).append(".operationpk)");
        queryRes.append(" AND ctx.status = ").append(Versionable.STATUS_APPLIED)
                .append(" AND ctx.TIME_STAMP <= ")
                .append(dbTranslator.getDateFormat(DateUtil.getFormatLongShow().format(searchDate))).append(" AND ctx.ID = ").append(alias2).append("operationpk");
        if (flag) {

            queryRes.append(" AND pv.agregatedobjectId = ").append(alias2)
                    .append(".agregatedobjectId");
            queryRes.append(!StringUtil.isEmptyOrNullValue(roleName) ?
                            " AND pvo.rol_Id = " + Role.Impl.load(roleName).getId() : "");
        } else {
            queryRes.append(" AND ").append(alias2).append(".AGREGATEDObjectID IN ");
            String queryOrderedThirdPartyForPartPol = getUniversalQueryOrderedThirdPartyForPartObj(
                    searchDate, "PC1", "PC", "PCO", roleName, true, "STPO_POLICYPARTICIPATION",
                    "STPO_POLICYPARTICIPATIONDCO");
            queryRes.append(queryOrderedThirdPartyForPartPol);


        }
        queryRes.append(")");
        log.debug("[getUniversalQueryOrderedThirdParty] query: " + queryRes);
        return queryRes.toString();
    }

    /**
     * Auxiliary method that helps the thirdparty query's construction.
     *
     * @param searchDate    The search date
     * @param aliasVersion  Alias 1
     * @param aliasVersion2 Alias 2
     * @param aliasObject   The alias object
     * @param roleName      The role name
     * @param flag          Indicates wicth table version to use
     * @param tableVersion  The table version
     * @param tableObject   The table object
     * @return String
     */
    private String getUniversalQueryOrderedThirdPartyForPartObj(java.sql.Date searchDate,
                                                                String aliasVersion,
                                                                String aliasVersion2,
                                                                String aliasObject, String roleName,
                                                                boolean flag, String tableObject,
                                                                String tableVersion) {
        StringBuffer queryRes = new StringBuffer();
        DBTranslator dbTranslator = DBTranslatorFactory.getDBClass();
        queryRes.append("(SELECT agregatedparentid FROM ")
                .append(tableVersion).append(" ").append(aliasVersion2).append(", ")
                .append(tableObject).append(" ").append(aliasObject)
                .append(", ContextOperation ctx WHERE NOT EXISTS (SELECT 1 FROM ")
                .append(tableVersion).append(" ").append(aliasVersion).append(", ContextOperation ctx1 WHERE ctx1.time_stamp <=")
                .append(dbTranslator.getDateFormat(DateUtil.getFormatLongShow().format(searchDate)))
                .append("AND ctx1.TIME_STAMP > ")
                .append(aliasVersion2).append(".TIME_STAMP")
                .append(" AND ctx1.status != ")
                .append(Versionable.STATUS_TEMPORARY)
                .append(" AND ").append(aliasVersion).append(".status != ")
                .append(Versionable.STATUS_REVERSED)
                .append(" AND ctx1.ID = ").append(aliasVersion).append(".operationpk AND ").append(aliasVersion).append(".agregatedobjectid = ")
                .append(aliasVersion2).append(".agregatedobjectid)")
                .append(" AND ctx.status = ").append(Versionable.STATUS_APPLIED)
                .append(" AND ").append(aliasVersion2).append(".agregatedobjectid = ")
                .append(aliasObject).append(".agregatedobjectid")
                .append(" AND ctx.TIME_STAMP <= ")
                .append(dbTranslator.getDateFormat(DateUtil.getFormatLongShow().format(searchDate)))
                .append(" AND ctx.ID = ").append(aliasVersion2).append(".operationpk");
        if (flag) {
            queryRes.append(" AND pv.agregatedobjectId = ").append(aliasVersion2)
                    .append(".agregatedobjectId");
            queryRes.append(!StringUtil.isEmptyOrNullValue(roleName) ?
                            " AND pvo.rol_Id = " + Role.Impl.load(roleName).getId() : "");
        } else {
            queryRes.append(" AND ").append(aliasVersion2).append(".AGREGATEDObjectID IN ");
            String queryOrderedThirdPartyForPartPol = getUniversalQueryOrderedThirdPartyForPartObj(
                    searchDate, "PC1", "PC", "PCO", roleName, true, "STPO_POLICYPARTICIPATION",
                    "STPO_POLICYPARTICIPATIONDCO");
            queryRes.append(queryOrderedThirdPartyForPartPol);

        }
        queryRes.append(")");

        log.debug("[getUniversalQueryOrderedThirdPartyForPartObj] query: " + queryRes);
        return queryRes.toString();
    }

    /**
     * @param query      StringBuilder
     * @param userLocale Locale
     * @return SearchResult
     */
    private SearchResult search(StringBuilder query, Locale userLocale, ClientContext context) {

        log.debug("SearchRequestHandler   search  query  = '  " + query);

        ResourceBundle rb = ResourceBundle.getBundle("SearchMessageBundle", userLocale);
        SearchResult searchResult = null;
        Vector policies = doSelect(query);
        setPolicyIDs(policies);
        this.propResult = policies.size();

        try {
            Vector column = new Vector();

            String predefinedPolicyTemplate =
                    SearchUtil.getDefaultTemplate(CotType.POLICY).getTableName();


            List result = JDBCUtil.doQueryList("SELECT stateid, Description FROM state ",
                                               timedSession.getSession().connection());
            Hashtable states = new Hashtable();

            for (Iterator iter = result.iterator(); iter.hasNext();) {
                Properties prop = (Properties) iter.next();
                states.put(prop.getProperty("stateid"), prop.getProperty("description"));
            }

            List statesVector = StringUtil
                    .splitAsList(AcseleConf.getProperty("StatesPolicyEnable"), ",");

            log.debug("statesVector = '" + statesVector + "'");


            Map products = Productos.getInstance().getMappingCopy();
            List<String> policyAdded = new ArrayList<String>();

            for (Iterator iter = getPolicyIDs().iterator(); iter.hasNext();) {
                CaseInsensitiveProperties cip = (CaseInsensitiveProperties) iter.next();
                String policyId = cip.getProperty("agregatedobjectid");

                if(!policyAdded.contains(policyId)){
                    Vector dataRow = new Vector();
                    try {
                        long stateId = Long.parseLong(cip.getProperty("stateid"));
                        int subStatus = Integer.parseInt(cip.getProperty("SUB_STATUS"));
                        String policySubStatus = PolicySubStatus.strPolicySubStatus(subStatus);
                        String policyState = (String) states.get(Long.toString(stateId));
                        log.debug("policyState = '" + policyState + "'");
                        log.debug("policySubStatus = '" + policySubStatus + "'");
                             boolean enable = statesVector.contains(policyState);

                    String initialDateStr = cip.getProperty("initialDate");
                    String finishDateStr = cip.getProperty("finishDate");
                    String time_stampStr = cip.getProperty("time_stamp");

                    Date searchDate;
                        try{
                            searchDate = new Date(DateUtil.getFormatLong().parse(time_stampStr).getTime());
                        }
                        catch (ParseException e){
                            searchDate =  DateUtil.getDateSqlParse(time_stampStr) ;
                        }

                    if (cip.getProperty("stateid") != null && initialDateStr != null &&
                            finishDateStr != null && (isClaim && enable) || (isClaim) ) {

                        String dcoId = cip.getProperty("dcoid");
                        String productId = cip.getProperty(PRODUCT_ID);

                        String policyNumber =
                                getPolicyNumber(dcoId, stateId, predefinedPolicyTemplate);

                        if (policyNumber != null && "".equals(policyNumber.trim())) {
                            policyNumber = policyId;
                        }

                        Collection clientParticipations = getClientParticipations(policyId);

                        StringBuffer clients = new StringBuffer(100);
                        for (Iterator iterPart = clientParticipations.iterator();
                             iterPart.hasNext();) {
                            String thirdParty = (String) iterPart.next();
                            clients.append(thirdParty);
                            if (iterPart.hasNext()) {
                                clients.append(", ");
                            }
                        }

                        Collection insuredParticipations = getInsuredParticipations(policyId, searchDate, PARTICIPATION_POLICY_LEVEL);
                        insuredParticipations.addAll(getInsuredParticipations(policyId, searchDate, PARTICIPATION_IO_LEVEL));


                        StringBuffer insured = new StringBuffer(100);

                        boolean policyConcurrency = false;

                        if(!StringUtil.isEmptyOrNullValue(productId)){ // ACSELE-10445: is concurrent, entonces es colectiva
                            long productIdL = Long.valueOf(productId);
                            com.consisint.acsele.openapi.product.Product ptem = com.consisint.acsele.openapi.product.Product.Impl.getProduct(productIdL);
                            policyConcurrency = ptem.getProductBehavior().getPolicyConcurrency();
                        }

                        int inx = 0;
                        if(!policyConcurrency){
                            for (Iterator iterPart = insuredParticipations.iterator();   //ACSELE-10444: only show 2 assured.
                                 iterPart.hasNext();) {
                                String thirdParty = (String) iterPart.next();

                                insured.append(thirdParty);
                                ++inx;
                                if(inx>1){
                                    break;
                                }
                                if (iterPart.hasNext()) {
                                    insured.append(", ");
                                }
                            }
                        } else{     // ACSELE-10445: is concurrent, entonces es colectiva
                            insured.append("--");
                        }


                            insertData(column, products, policyId, dataRow, policySubStatus, policyState, initialDateStr, finishDateStr, productId, policyNumber, clients, insured);

                    }

                    } catch (Exception e) {
                        log.error("Error getting policy " + policyId, e);
                    }

                    policyAdded.add(policyId);
                }
            }

            //construct search result object

            Vector columnHeadersVector = new Vector();
            columnHeadersVector.add(rb.getString("search.label.policyId"));
            columnHeadersVector.add(rb.getString("search.label.policyNumber"));
            columnHeadersVector.add(rb.getString("search.label.product"));
            columnHeadersVector.add(rb.getString("search.label.initialDate"));
            columnHeadersVector.add(rb.getString("search.label.finishDate"));
            columnHeadersVector.add(rb.getString("search.label.state"));

            columnHeadersVector.add(rb.getString("search.label.clients"));
            columnHeadersVector.add(rb.getString("search.label.insured"));

            searchResult = new SearchResult(column, columnHeadersVector);

        } catch (Exception e) {
            log.error("Error executing search process", e);
        }

        return searchResult;
    }

    private void insertData(Vector column, Map products, String policyId, Vector dataRow, String policySubStatus, String policyState, String initialDateStr, String finishDateStr, String productId, String policyNumber, StringBuffer clients, StringBuffer insured) {
        dataRow.add(Long.valueOf(policyId));
        dataRow.add(policyNumber);
        String productName = (String) products.get(productId);
        dataRow.add(productName);
        dataRow.add(initialDateStr);
        dataRow.add(finishDateStr);
        dataRow.add(policyState);

        dataRow.add(clients.toString());
        dataRow.add(insured.toString());

        column.add(dataRow);
    }



    private Properties getTotalRow(StringBuilder query) {
        try {
            return JDBCUtil.doQueryOneRow(query.toString(), timedSession.getSession().connection());

        } catch (Exception e) {
            log.error("Error getting count information: QUERY: " + query, e);
        }
        return null;
    }

    private Collection getClientParticipations(String policyId) {
        if (ThirdpartyUtil.getClientRoles().isEmpty()) {
            return new Vector();
        } else {
            String query = null;
            try {
                query = getClientParticipationQuery(policyId).toString();

                return JDBCUtil
                        .doQueryOneColumnVector(query, timedSession.getSession().connection());
            } catch (Exception e) {
                log.error("Error getting client information: QUERY: " + query, e);
                return new Vector();
            }
        }
    }

    public Collection getInsuredParticipations(String policyId, Date searchDate, int level) {
        if (ThirdpartyUtil.getInsuredRoles().isEmpty()) {
            return new Vector();
        } else {
            String query = null;
            try {

                query = getInsuredParticipationQuery(policyId, searchDate, level).toString();

                return JDBCUtil
                        .doQueryOneColumnVector(query, timedSession.getSession().connection());
            } catch (Exception e) {
                e.printStackTrace(System.out);
                log.error("Error getting insured information: QUERY: " + query, e);
                return new Vector();
            }
        }
    }

    /**
     * @param name  String
     * @param roles Collection
     * @param date  Date
     * @return StringBuffer
     */
    private StringBuffer getPolicyParticipationQuery(String name, Collection roles,
                                                     java.sql.Date date) {
        DBTranslator dbTranslator = DBTranslatorFactory.getDBClass();
        String tableVersion = AggregateObject.getTableVersion(AggregateObject.POLICY_PARTICIPATION);
        String tableObject = AggregateObject.getTable(AggregateObject.POLICY_PARTICIPATION);
        String thirdPartyTemplate =
                SearchUtil.getDefaultTemplate(CotType.THIRDPARTY).getTableName();
        StringBuffer query = new StringBuffer(500)
                .append(" AND agregatedPolicyId IN (")
                .append(AggregatedManager.getUniversalQueryByDate(tableVersion,tableObject,
                                                                  new String[]{"agregatedParentId"},
                                                                  date))
                .append(" AND ").append(AggregateObject.getTableAlias(tableVersion)).append(".STATUS != ").append(Versionable.STATUS_CLOSED)
                .append(" AND agregatedParentId = agregatedPolicyId")
                .append(" AND thirdPartyId IN (SELECT tp.TPT_Id FROM STTE_ThirdParty tp,")
                .append(thirdPartyTemplate).append(" dco WHERE tp.idDCO = dco.pk")
                .append(" AND ").append(dbTranslator.toUpperCase(getThirdPartyConcatFields("%")))
                .append(" like '%").append(name.toUpperCase()).append("%')");

        makeRoleQuery(roles, query, "");

        query.append(")");
        log.debug("[getPolicyParticipationQuery] query: " + query);
        return query;
    }

    /**
     * @param name  String
     * @param roles Collection
     * @param date  Date
     * @return StringBuffer
     */
    private StringBuffer getInsuranceObjectParticipationQuery(String name, Collection roles,
                                                              java.sql.Date date) {
        DBTranslator dbTranslator = DBTranslatorFactory.getDBClass();
        StringBuffer query = new StringBuffer(500);
        String rAlias = AggregateObject
                .getTableAlias(AggregateObject.getTableVersion(AggregateObject.RISK_UNIT));
        String ioAlias = AggregateObject
                .getTableAlias(AggregateObject.getTableVersion(AggregateObject.INSURANCE_OBJECT));

        String iopTableVersion = AggregateObject
                .getTableVersion(AggregateObject.INSURANCE_OBJECT_PARTICIPATION);
        String iopTableObject = AggregateObject
                .getTable(AggregateObject.INSURANCE_OBJECT_PARTICIPATION);
        String thirdPartyTemplate =
                SearchUtil.getDefaultTemplate(CotType.THIRDPARTY).getTableName();

        query.append(" AND agregatedPolicyId IN (");
        String[] parentField = new String[]{"agregatedParentId"};
        query.append(AggregatedManager.getUniversalVersionQueryByAggregated(
                AggregateObject.RISK_UNIT, null, parentField,null));
        query.append(" AND agregatedPolicyId = ").append(rAlias).append(".agregatedParentId");
        query.append(" AND ").append(rAlias).append(".agregatedObjectId IN (");
        query.append(AggregatedManager.getUniversalVersionQueryByAggregated(
                AggregateObject.INSURANCE_OBJECT, null, parentField,null));
        query.append(" AND ").append(ioAlias).append(".agregatedObjectId IN (");
        query.append(AggregatedManager.getUniversalQueryByDate(iopTableVersion, iopTableObject, parentField, date));
        query.append(" AND ").append(AggregateObject.getTableAlias(iopTableVersion)).append(".STATUS != ").append(Versionable.STATUS_CLOSED);
        query.append(" AND agregatedParentId = ").append(ioAlias).append(".agregatedobjectId");
        query.append(" AND thirdPartyId IN (SELECT tp.TPT_Id FROM STTE_ThirdParty tp,")
                .append(thirdPartyTemplate).append(" dco WHERE tp.idDCO = dco.pk AND ")
                .append(dbTranslator.toUpperCase(getThirdPartyConcatFields("%")))
                .append(" like '%").append(name.toUpperCase()).append("%')");

        makeRoleQuery(roles, query, "");

        query.append(")))))");
        log.debug("[getInsuranceObjectParticipationQuery] query: " + query);
        return query;
    }

    private String getCancelledStates() {
        StringBuffer sb = new StringBuffer("(");
        Product product = null;
        if (!StringUtil.isEmptyOrNullValue(productId)) {
            product = Productos.getInstance().getByIdDesc(productId);
        }
        if (product == null) {
            return null;
        }
        String cancelledStates = AcseleConf.getProperty(EventType.CANCELLED_STATES);
        List myStates = StringUtil.splitAsList(cancelledStates, ",");

        LifeCycle lifeCycle =
                product.getAgregatedPolicyType().getCoverageAgregatedLifeCycle().getLifeCycle();
        List states = lifeCycle.getStates();

        for (int i = 0; i < myStates.size(); i++) {
            String str = (String) myStates.get(i);
            for (int j = 0; j < states.size(); j++) {
                LifeCycleState lcs = (LifeCycleState) states.get(j);
                if (lcs.getDesc().equals(str)) {
                    sb.append(lcs.getPk()).append(",");
                    break;
                }
            }
        }
        int pos = sb.lastIndexOf(",");
        if (pos != -1) {
            sb.deleteCharAt(pos);
        }
        if (sb.length() > 1) {
            sb.append(")");
        }
        return sb.toString();
    }


    /**
     * @param table StringBuilder
     * @return boolean
     */
    private boolean askIfTableIsEmpty(StringBuilder table) {
        Properties prop = null;
        if (!StringUtil.isEmptyOrNullValue(table.toString())) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT count(1) as cnt FROM ").append(table); // DB2 Certification
            try {
                prop = getTotalRow(query);
            } catch (Exception e) {
                log.error("Error during the search process - Query: " + query, e);
            }
        }
        return (prop != null && Integer.parseInt(prop.getProperty("cnt")) == 0); // DB2 Certification
    }

    /**
     * @param query StringBuffer
     * @return Vector
     */
    private static int doUpdate(String query) {
        int success = 0;
        if (!StringUtil.isEmptyOrNullValue(query)) {
            try {
                success = JDBCUtil.doUpdate(query, timedSession.getSession().connection());
            } catch (Exception e) {
                log.error("Error during the search process - Query: " + query, e);
            }
        }
        return success;
    }

    /**
     * @param query StringBuilder
     * @return Vector
     */
    private static Vector doSelect(StringBuilder query) {
        Vector results = new Vector();
        if (!StringUtil.isEmptyOrNullValue(query.toString())) {
            try {
                results = JDBCUtil.doQueryVector(query.toString(),
                                                 timedSession.getSession().connection());
            } catch (Exception e) {
                log.error("Error during the search process - Query: " + query, e);
            }
        }
        return results;
    }

    /**
     * @param context ClientContext
     */
    private void setUrl(ClientContext context) {
        String urlCode = (String) context.getSessionObject("urlCode");
        String command = context.getParameter("command");

        if (urlCode == null) {
            this.url = bundle.getString(command);
        } else {
            String finalURL;
            if (isForward() &&
                bundle.getString(urlCode).indexOf(AcseleConf.getProperty("webApp")) == 0) {
                int initIndex = AcseleConf.getProperty("webApp").length();
                finalURL = bundle.getString(urlCode).substring(initIndex);
            } else {
                finalURL = bundle.getString(urlCode);
            }
            this.url = finalURL;
        }
    }

    /**
     * @return String
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @return String
     */
    public String getNextPage() {
        return this.getUrl();
    }

    /**
     * @return Vector
     */
    private Vector getPolicyIDs() {
        return this.policyIDs;
    }

    /**
     * @param policyIDs Vector
     */
    private void setPolicyIDs(Vector policyIDs) {
        this.policyIDs = policyIDs;
    }

    /**
     * @param dcoId          String
     * @param stateId        int
     * @param policyTemplate String
     * @return String
     */
    private String getPolicyNumber(String dcoId, long stateId, String policyTemplate) {

        String numberProperty = AcseleConf.getProperty("policyIdentification");

        StringBuilder queryLoad = new StringBuilder(100);
        queryLoad.append("SELECT ");
        queryLoad.append(numberProperty).append("INPUT FROM ").append(policyTemplate);
        queryLoad.append(" WHERE ").append(" PK = ").append(dcoId);

        String policyNumber = null;

        try {

            Properties prop = JDBCUtil.doQueryOneRow(queryLoad.toString(),
                                                     timedSession.getSession().connection());
            policyNumber = prop.getProperty(numberProperty + "INPUT");

        } catch (Exception e) {
            log.error("getPolicyNumber() -> " + queryLoad, e);
        }

        if (policyNumber == null) {
            policyNumber = "";
        }

        return policyNumber;
    }

    /**
     * @param cotType CotType
     * @return String
     */
    private ConfigurableObjectType getDefaultTemplate(CotType cotType) {
        return (ConfigurableObjectType) defaultTemplates.getCollection(cotType).get(0);
    }

    /**
     * Finds the given policy's clients
     *
     * @param policyId String
     * @return StringBuilder
     */
    private StringBuilder getClientParticipationQuery(String policyId) {


        String poTableVersion = AggregateObject.getTableVersion(AggregateObject.POLICY);


        StringBuilder queryLoad = new StringBuilder(500);

        String thirdPartyFields = getThirdPartyConcatFields();
        String thirdPartyTemplate =
                SearchUtil.getDefaultTemplate(CotType.THIRDPARTY).getTableName();



        queryLoad.append("SELECT ").append(thirdPartyFields)
                .append(" name FROM STTE_ThirdParty tp, ").append(thirdPartyTemplate)
                .append(" dco WHERE dco.pk = tp.idDCO AND tp.tpt_id in (select stp.thirdpartyid from stpo_policyparticipation stp where agregatedobjectid in (select agregatedobjectid from stpo_policyparticipationdco where stpo_policyparticipationdco.operationpk = ((select max(ctx.id) from ")
                .append(poTableVersion)
                .append(" pdco, ContextOperation ctx where pdco.operationpk = ctx.id and ctx.status = ")
                .append(Versionable.STATUS_APPLIED).append(" and ctx.item = ").append(policyId)
                .append(")) and (stpo_policyparticipationdco.status <> ")
                .append(Versionable.STATUS_CLOSED)
                .append(" OR stpo_policyparticipationdco.status is null)) and stp.rol_id IN ").append(StringUtil.getQueryINUsingList(
                        CollectionUtils.collect(ThirdpartyUtil.getClientRoles(),
                                                new Transformer() {
                                                    public Object transform(Object o) {
                                                        String roleDescription = (String) o;
                                                        return Role.Impl.load(roleDescription).getId();
                                                    }
                                                }), false)).append(")");


        log.debug("**** getClientParticipationQuery: " + queryLoad);
        return queryLoad;
    }

    /**
     * This method overloads the same method in AggregatedManager.
     * This was created so that the clients would show on the policy' search.
     * Study this case and find a logical solution.
     *
     * @param tableVersion String
     * @param fields       String[]
     * @return String
     */


    private String getThirdPartyConcatFields() {
        return getThirdPartyConcatFields(" ");
    }

    private String getThirdPartyConcatFields(String link) {
        String value = AcseleConf.getProperty("thirdparty.searchResultLinkProperties");
        String thirdPartyFields = "";
        if (value != null) {
            String[] elements = value.split(",");
            List inputs = new ArrayList(elements.length);
            for (int i = 0; i < elements.length; i++) {
                inputs.add(elements[i] + "INPUT");
            }
            DBTranslator dbTranslator = DBTranslatorFactory.getDBClass();
            thirdPartyFields = dbTranslator.concat(inputs, link);
        }
        return thirdPartyFields;
    }

    /**
     * Finds the given policy's insured
     *
     * @param policyId   String
     * @param searchDate Date
     * @return StringBuilder
     */
    public StringBuffer getInsuredParticipationQuery(String policyId, java.sql.Date searchDate, int level) {
        String thirdPartyTemplate = SearchUtil.getDefaultTemplate(CotType.THIRDPARTY).getTableName();
        String insuredRoleList = "";

        for (Role role : RoleGroup.INSURANCE_ROLES.getRoleList()){
            insuredRoleList += role.getId()+",";
        }
        insuredRoleList = insuredRoleList.substring(0, insuredRoleList.length()-1);
        String predeterminedName = AcseleConf.getProperty("thirdparty.searchResultLinkProperties");
        String codeIdentifier = AcseleConf.getProperty("thirdPartyPrimaryKey");
        StringBuffer queryLoad = null;

        if (level == PARTICIPATION_IO_LEVEL) {
            queryLoad = new StringBuffer(
                    "SELECT PTP."+predeterminedName+"INPUT INSURED,\n" +
                            " IOP.THIRDPARTYID, PTP."+codeIdentifier+"INPUT CODE\n"+
                            " FROM\n" +
                            "  (SELECT MAX(O.ID) ID\n" +
                            "  FROM CONTEXTOPERATION O\n" +
                            "  WHERE O.ITEM = "+ policyId +
                            "  AND O.STATUS = "+ Versionable.STATUS_APPLIED +
                            "  AND TO_DATE(O.TIME_STAMP, 'yyyy-MM-dd HH24:MI:ss') <= TO_DATE('"+ DateUtil.sdfpl.format(searchDate) +"', 'yyyy-MM-dd HH24:MI:ss')"+
                            "  ) OP\n" +
                            " INNER JOIN STPO_INSOBJPARTICIPATIONDCO IOPDCO\n" +
                            " ON OP.ID = IOPDCO.OPERATIONPK\n" +
                            " AND (IOPDCO.STATUS != "+ Versionable.STATUS_CLOSED +
                            " OR IOPDCO.STATUS   IS NULL)" +
                            " INNER JOIN STPO_INSOBJPARTICIPATION IOP\n" +
                            " ON IOP.AGREGATEDOBJECTID = IOPDCO.AGREGATEDOBJECTID\n" +
                            " AND IOP.ROL_ID IN ("+ insuredRoleList +")\n" +
                            " INNER JOIN "+ thirdPartyTemplate +" PTP\n" +
                            " ON PTP.STATIC = IOP.THIRDPARTYID\n" +
                            " GROUP BY PTP."+predeterminedName+"INPUT, IOP.THIRDPARTYID, PTP."+codeIdentifier+"INPUT \n" +
                            " ORDER BY PTP."+predeterminedName+"INPUT"
            );
        } else if (level == PARTICIPATION_POLICY_LEVEL) {
            queryLoad = new StringBuffer(
                    "SELECT PTP."+predeterminedName+"INPUT INSURED,\n" +
                            " POLP.THIRDPARTYID, PTP."+codeIdentifier+"INPUT CODE\n"+
                            " FROM\n" +
                            "  (SELECT MAX(O.ID) ID\n" +
                            "  FROM CONTEXTOPERATION O\n" +
                            "  WHERE O.ITEM = "+ policyId +
                            "  AND O.STATUS = "+ Versionable.STATUS_APPLIED +
                            "  AND O.TIME_STAMP <= TO_DATE('"+ DateUtil.sdfpl.format(searchDate) +"', 'yyyy-MM-dd HH24:MI:ss')"+
                            "  ) OP\n" +
                            " INNER JOIN STPO_POLICYPARTICIPATIONDCO POLPDCO\n" +
                            " ON OP.ID = POLPDCO.OPERATIONPK\n" +
                            " AND (POLPDCO.STATUS != "+ Versionable.STATUS_CLOSED +
                            " OR POLPDCO.STATUS   IS NULL)" +
                            " INNER JOIN STPO_POLICYPARTICIPATION POLP\n" +
                            " ON POLP.AGREGATEDOBJECTID = POLPDCO.AGREGATEDOBJECTID\n" +
                            " AND POLP.ROL_ID IN ("+ insuredRoleList +")\n" +
                            " INNER JOIN "+ thirdPartyTemplate +" PTP\n" +
                            " ON PTP.STATIC = POLP.THIRDPARTYID\n" +
                            " GROUP BY PTP."+predeterminedName+"INPUT, POLP.THIRDPARTYID, PTP."+codeIdentifier+"INPUT \n" +
                            " ORDER BY PTP."+predeterminedName+"INPUT"
            );
        }

        log.debug("[getInsuredParticipationQuery] query: " + queryLoad);
        return queryLoad;
    }

    /**
     * @param name         String
     * @param roles        Collection
     * @param date         Date
     * @param tableVersion The table version
     * @param tableObject  The table object
     * @return StringBuffer
     */
    private StringBuffer getUniversalParticipationQueryOrdered(String name, Collection roles,
                                                               java.sql.Date date,
                                                               String tableVersion,
                                                               String tableObject) {
        DBTranslator dbTranslator = DBTranslatorFactory.getDBClass();
        String thirdPartyTemplate =
                SearchUtil.getDefaultTemplate(CotType.THIRDPARTY).getTableName();
        String alias = AggregateObject.getTableAlias(tableVersion);
        String aliasObj = AggregateObject.getTableAlias(tableObject);
        StringBuffer query = new StringBuffer();

        query.append(" SELECT DISTINCT ").append(alias).append("3.agregatedParentId ")
                .append(" FROM (SELECT DISTINCT ").append(aliasObj).append(".agregatedParentId, ")
                .append(" MAX (ctx.time_stamp) AS time_stamp FROM ")
                .append(tableVersion).append(" ").append(alias).append(", ")
                .append(tableObject).append(" ").append(aliasObj).append(", ContextOperation ctx WHERE ctx.time_stamp <= ")
                .append(dbTranslator.getDateFormat(DateUtil.getFormatLongShow().format(date)))
                .append(" AND ctx.STATUS = ")
                .append(Versionable.STATUS_APPLIED).append(" AND ctx.ID = ").append(alias)
                .append(".operationpk AND ").append(alias).append(".agregatedobjectid = ")
                .append(aliasObj).append(".agregatedobjectid ");
        makeRoleQuery(roles, query, "");
        query.append(" GROUP BY ").append(aliasObj).append(".agregatedparentid) ").append(alias)
                .append("1,")
                .append(" (SELECT tp.TPT_Id FROM STTE_ThirdParty tp,")
                .append(thirdPartyTemplate).append(" dco WHERE tp.idDCO = dco.pk")
                .append(" AND ").append(dbTranslator.toUpperCase(getThirdPartyConcatFields("%")))
                .append(" like '%").append(name.toUpperCase()).append("%') ").append(alias)
                .append("2, ")
                .append(tableObject).append(" ").append(alias).append("3 ")
                .append(" WHERE ").append(alias)
                .append("3.thirdpartyid = ").append(alias).append("2.tpt_id ")
                .append(" AND ").append(alias).append("3.agregatedparentid = ")
                .append(alias).append("1.agregatedparentid ")
                .append(" AND ").append(alias)
                .append("3.time_stamp = ").append(alias).append("1.time_stamp ")
                .append(" ORDER BY ").append(alias).append("3.agregatedparentid ASC");
        log.debug("[getUniversalParticipationQueryOrdered] query: " + query);
        return query;

    }

    /**
     * @param roles      Collection
     * @param outerQuery StringBuffer
     * @param prefix     prefix
     */
    public static void makeRoleQuery(Collection roles, StringBuffer outerQuery, String prefix) {
        if (!roles.isEmpty()) {
            boolean isOK = true;
            StringBuffer query = new StringBuffer(20);
            query.append(" AND (");
            for (Iterator iterator = roles.iterator(); iterator.hasNext();) {
                String roleDescription = (String) iterator.next();
                try {
                    long roleId = Role.Impl.load(roleDescription).getId();
                    query.append(prefix).append("rol_id = ").append(roleId)
                            .append(iterator.hasNext() ? " OR " : ")");
                } catch (Exception e) {
                    log.error("Error building query: Cannot find role '" + roleDescription + "'");
                    log.error("The query will not use Roles in this search", e);
                    isOK = false;
                }
            }
            if (isOK) {
                outerQuery.append(query);
            }
        }
    }


    /**
     * @param cot            ConfigurableObjectType
     * @param queryParameter QueryParameter
     * @param useLike        boolean
     * @return String
     */
    public String getDefaultConfigurableObjectQuery(ConfigurableObjectType cot,
                                                    QueryParameter queryParameter,
                                                    boolean useLike) {

        DBTranslator dbTranslator = DBTranslatorFactory.getDBClass();
        StringBuffer query = new StringBuffer().append("SELECT obj.pk ");
        StringBuffer fromQuery = new StringBuffer().append(" FROM ").append(cot.getTableName())
                .append(" obj");
        StringBuffer whereQuery = new StringBuffer().append(" WHERE ");

        boolean isFirstDefault = true;

        ConfigurableObjectType defaultTemplate = getDefaultTemplate(cot.getCotType());

        boolean isUsingExtendedTemplate = !cot.isPreDefined() && defaultTemplate != null;
        log.debug("isUsingExtendedTemplate = " + isUsingExtendedTemplate);
        log.debug(" cot.getDesc() " + cot.getDesc());
        if (isUsingExtendedTemplate) {
            String defaultTemplateName = defaultTemplate.getTableName();
            fromQuery.append(", ").append(defaultTemplateName).append(" def");
            whereQuery.append(" obj.pk = def.pk AND ");
            log.debug("defaultTemplateName = " + defaultTemplateName);
        }

        Enumeration keys = queryParameter.keys();

        while (keys.hasMoreElements()) {
            String parameter = (String) keys.nextElement();
            String parameterValue = (String) queryParameter.getParameter(parameter);
            if (isUsingExtendedTemplate && (defaultTemplate.containsKey(parameter))) {
                //The property belongs to the default template
                if (!isFirstDefault) {
                    whereQuery.append(" AND ");
                }
                whereQuery.append(dbTranslator.toUpperCase("def." + parameter + "INPUT"));
                whereQuery.append(getDCOCondition(parameterValue, useLike));
                isFirstDefault = false;
            } else if (cot.containsKey(parameter)) {
                //The property belongs to the given template
                if (!isFirstDefault) {
                    whereQuery.append(" AND ");
                }
                whereQuery.append(dbTranslator.toUpperCase("obj." + parameter + "INPUT"));
                whereQuery.append(getDCOCondition(parameterValue, useLike));
                isFirstDefault = false;
            }
        }
        String res = query.append(fromQuery).append(whereQuery).toString();
        log.debug("[getDefaultConfigurableObjectQuery] query: " + query);
        return isFirstDefault ? null : res;
    }

    private static String getDCOCondition(String parameterValue, boolean useLike) {
        if (useLike) {
            return " LIKE '%" + parameterValue.toUpperCase() + "%'";
        } else {
            return " = '" + parameterValue.toUpperCase() + "'";
        }
    }

    /**
     *
     * @param participationIO
     * @return
     */
    public String getThirdPartyList(String thirdparty){
        StringBuilder query = new StringBuilder(500);
        String predeterminedName = AcseleConf.getProperty("thirdparty.searchResultLinkProperties");
        String codeIdentifier = AcseleConf.getProperty("thirdPartyPrimaryKey");
        String templateName = Categorias.getDefaultTemplate(CotType.THIRDPARTY).getName();
        String tpValue = (StringUtil.isEmptyOrNullValue(thirdparty)) ? "''" : thirdparty;
        query.append("SELECT  PTP.").append(predeterminedName).append("INPUT INSURED, ").append("PTP.STATIC THIRDPARTYID, ")
                .append("PTP.").append(codeIdentifier).append("INPUT").append(" CODE")
                .append(" FROM ").append(templateName).append(" PTP ").append("where PTP.STATIC in ").append("(").append(tpValue).append(")");
        return query.toString();
    }

}
