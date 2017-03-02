package com.zoegeop.producto.persistent.managers;

import com.zoegeop.producto.AggregateObject;
import com.zoegeop.producto.ClientInfo;
import com.zoegeop.producto.Versionable;
import com.zoegeop.producto.entry.scheduler.EntryHistorical;
import com.zoegeop.producto.policy.context.ContextOperationManager;
import com.zoegeop.producto.policy.search.QueryParameter;
import com.zoegeop.producto.policy.search.util.SearchUtil;
import com.zoegeop.producto.policy.server.AggregatedFund;
import com.zoegeop.producto.policy.server.EvaluatedCoverage;
import com.zoegeop.producto.policy.server.Participation;
import com.zoegeop.producto.policy.server.beans.StatusContextOperation;
import com.zoegeop.producto.policy.session.OperationPK;
import com.zoegeop.producto.product.server.Product;
import com.zoegeop.producto.reinsurance.reinsuranceconfiguration.Constants;
import com.zoegeop.producto.template.server.*;
import com.zoegeop.producto.util.*;
import com.zoegeop.producto.util.dbtranslator.DBTranslator;
import com.zoegeop.producto.util.dbtranslator.DBTranslatorFactory;
import com.zoegeop.producto.util.error.ApplicationException;
import com.zoegeop.producto.util.error.Exceptions;
import com.zoegeop.producto.util.error.Severity;
import com.zoegeop.producto.util.error.TechnicalException;
import com.zoegeop.producto.util.logging.AcseleLogger;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Perform SQL-Operations as of SELECT, UPDATE and DELETE on the "aggregatedObject"-object instance
 * of the AggregateObject class.<p>
 * Title: AggregatedManager.java <br>
 * Copyright: (c) 2003 Consis International<br>
 * Company: Consis International<br>
 * @author Consis International (CON)
 */

public abstract class AggregatedManager implements Serializable {



    private static final AcseleLogger log = AcseleLogger.getLogger(AggregatedManager.class);
    public static final String TABLE_DCO = "<TABLE_DCO>";
    private static final int RECORD_DISPLAY = 15;

    // TODO Hay muchos m�todos que no se sobrescriben, sino que quedan duplicados. Ej applyversion
    // TODO

    /**
     * Gets query to get object by last applied operation
     * @param aggregatedObject to be recovered
     * @param conn Connection
     * @return query to use
     */
    protected static PreparedStatement getQueryByLastAppliedOperation(
            AggregateObject aggregatedObject, Connection conn) {
        return getQueryByOperationPK(aggregatedObject, aggregatedObject.getLastAppliedOperationId(),
                                     conn);
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param dateOperation Date
     * @param conn Connection
     * @return query to use
     */
    public static PreparedStatement getQueryByDate(AggregateObject aggregatedObject,
                                                   java.util.Date dateOperation, Connection conn) {

        PreparedStatement statement = null;
        StringBuilder queryLoad = new StringBuilder(500);

        queryLoad.append(getUniversalQueryByDate(aggregatedObject, new String[]{"*, ctx.time_stamp as lasttimestamp "}, dateOperation));

        if (aggregatedObject != null) {
            queryLoad.append(" AND agregatedobjectid = ? order by lasttimestamp desc");
        }
        if(log.isDebugEnabled()) log.debug("[getQueryByDate] = " + queryLoad);

        try {
            Timestamp date = new Timestamp(dateOperation.getTime());
            statement = conn.prepareStatement(queryLoad.toString());

            statement.setTimestamp(1, date);
            statement.setInt(2, Versionable.STATUS_APPLIED);
            statement.setInt(3, Versionable.STATUS_APPLIED);
            statement.setTimestamp(4, date);
            if (aggregatedObject != null) {
                statement.setLong(5, Long.valueOf(aggregatedObject.getPk()));
            }

        } catch (SQLException e) {
            JDBCUtil.closeQuietly(statement);
            throw new TechnicalException("Error creating prepared statement.", Severity.FATAL, e);
        }

        return statement;
    }

    public static PreparedStatement getQueryByIdAndStatus(AggregateObject aggregatedObject,
                                                    Connection conn) {

        PreparedStatement statement = null;
        StringBuilder queryLoad = new StringBuilder(500);

        queryLoad.append(getUniversalQueryByIdAndStatus(aggregatedObject, new String[]{"*, ctx.time_stamp as lasttimestamp "}));

        if (aggregatedObject != null) {
            queryLoad.append(" AND agregatedobjectid = ? order by lasttimestamp");
        }
        if(log.isDebugEnabled()) log.debug("[getQueryByDate] = " + queryLoad);

        try {


            statement = conn.prepareStatement(queryLoad.toString());


            statement.setInt(1, Versionable.STATUS_APPLIED);
            statement.setInt(2, Versionable.STATUS_APPLIED);

            if (aggregatedObject != null) {
                statement.setLong(3, Long.valueOf(aggregatedObject.getPk()));
            }

        } catch (SQLException e) {
            JDBCUtil.closeQuietly(statement);
            throw new TechnicalException("Error creating prepared statement.", Severity.FATAL, e);
        }

        return statement;
    }


    public static PreparedStatement getQueryByEffectiveDate(AggregateObject aggregatedObject,
                                                   java.util.Date dateOperation, Connection conn, boolean simulated) {
        PreparedStatement statement = null;
        StringBuilder queryLoad = new StringBuilder(500);

        queryLoad.append(getUniversalQueryByEffectiveDate(aggregatedObject, new String[]{"*, ctx.effective_date as effectiveDate "}, dateOperation));
        if(log.isDebugEnabled()) log.debug("[getQueryByDate] = " + queryLoad);
        try {
            Timestamp date = new Timestamp(dateOperation.getTime());
            statement = conn.prepareStatement(queryLoad.toString());

            if(simulated) statement.setInt(1, Versionable.STATUS_SIMULATED);
            else statement.setInt(1, Versionable.STATUS_APPLIED);
            statement.setTimestamp(2, date);
            statement.setTimestamp(3, date);
            statement.setLong(4, Long.valueOf(aggregatedObject.getPk()));
        } catch (SQLException e) {
            JDBCUtil.closeQuietly(statement);
            throw new TechnicalException("Error creating prepared statement.", Severity.FATAL, e);
        }
        return statement;
    }

    /**
     *
     * @return String[]
     */
    protected static String[] getFields() {
        return new String[]{"*"};
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param fields String[]
     * @param timeStamp Date
     * @return query to use
     */
    public static String getUniversalQueryByDate(AggregateObject aggregatedObject, String[] fields,
                                                 java.util.Date timeStamp) {
        String tableVersion =
                aggregatedObject == null ? TABLE_DCO : aggregatedObject.getTableVersion();
        return getUniversalQueryByDateWithPrepare(tableVersion, fields, timeStamp);
    }

    public static String getUniversalQueryByIdAndStatus(AggregateObject aggregatedObject, String[] fields
                                                 ) {
        String tableVersion =
                aggregatedObject == null ? TABLE_DCO : aggregatedObject.getTableVersion();
        return getUniversalQueryByIdAndStatusWithPrepare(tableVersion, fields);
    }


    public static String getUniversalQueryByEffectiveDate(AggregateObject aggregatedObject, String[] fields,
                                                 java.util.Date timeStamp) {
        String tableVersion =
                aggregatedObject == null ? TABLE_DCO : aggregatedObject.getTableVersion();
        return getUniversalQueryByDateNew(tableVersion, fields, timeStamp);
    }
    /**
     *
     * @param agregatedType agregatedType
     * @param fieldsVt fieldsVt
     * @param initialDateEmission initialDateEmission
     * @param finalDateEmission finalDateEmission
     * @return query to use
     * @param fieldsAt fieldsAt
     * @param product productPk
     */
    public static String getQueryByDateAndProductPk(String agregatedType, String[] fieldsVt,
                                                    String fieldsAt,
                                                    java.util.Date initialDateEmission,
                                                    java.util.Date finalDateEmission,
                                                    Product product) {
        DBTranslator dbtranslator = DBTranslatorFactory.getDBClass();
        StringBuilder queryLoad = new StringBuilder();
        String queryFieldsVt;

        String tableVersion = AggregateObject.getTableVersion(agregatedType);
        String agregateTable = AggregateObject.getTable(agregatedType);

        String aliasVt = AggregateObject.getTableAlias(tableVersion);
        String aliasAt = AggregateObject.getTableAlias(AggregateObject.getTable(agregatedType));

        queryFieldsVt = fieldsVt == null || fieldsVt.length < 1 ? "*" :
                        aliasVt + "." + StringUtil.unsplit(fieldsVt, ", " + aliasVt + ".");

        queryLoad.append("SELECT ").append(aliasAt).append(".").append(fieldsAt).append(", ")
                .append(queryFieldsVt).append(" FROM ").append(tableVersion).append(" ")
                .append(aliasVt).append(" , ").append(agregateTable).append(" ").append(aliasAt)
                .append(", ContextOperation ctx").append(" WHERE ctx.STATUS = ")
                .append(Versionable.STATUS_APPLIED).append(" AND ctx.id = ").append(aliasVt)
                .append(".operationpk");

        queryLoad.append(EntryHistorical.getCompleteQuery(
                new StringBuilder(aliasAt).append(".operationpk").toString()));

        if (initialDateEmission != null) {
            String initDate = DateUtil.getDateToShow(initialDateEmission) + " 00:00:00";
            queryLoad.append(" AND ctx.TIME_STAMP >= ")
                    .append(dbtranslator.getDateFormat(initDate));
        }
        if (finalDateEmission != null) {
            String endDate = DateUtil.getDateToShow(finalDateEmission) + " 23:59:59";
            queryLoad.append(" AND ctx.TIME_STAMP <= ")
                    .append(dbtranslator.getDateFormat(endDate));
        }
        if (product != null) {
            queryLoad.append(" AND ").append(aliasAt).append(".productId = ")
                    .append(product.getPk());
        }
        queryLoad.append(" AND ").append(aliasAt).append(".operationpk = ").append(aliasVt)
                .append(".operationpk");

        if (log.isDebugEnabled())log.debug("QueryLoad = " + String.valueOf(queryLoad));

        return String.valueOf(queryLoad);
    }

    /**
     *
     * @param tableVersion tableVersion
     * @param fields fields
     * @param timeStamp timeStamp
     * @return query to use
     */
    public static String getUniversalQueryByDate(String tableVersion, String[] fields,
                                                 java.util.Date timeStamp) {
        // TODO: 2006-04-15 (GS) In the search, this timestamp is allways the same.
        DBTranslator dbtranslator = DBTranslatorFactory.getDBClass();
        StringBuilder queryLoad = new StringBuilder();
        // 2009-08-27 (GS) Correccion error Produccion 6.8.0 Luisa
        String date23 = DateUtil.getFormatLongShow().format(timeStamp); // Must have time = 23:59:59

        String alias = AggregateObject.getTableAlias(tableVersion);

        String queryFields = (fields == null || fields.length < 1) ? "*" :
                      alias + "." + StringUtil.unsplit(fields, ", " + alias + ".");

        queryLoad.append("SELECT ").append(queryFields).append(" FROM ").append(tableVersion)
                .append(" ").append(alias).append(", ContextOperation ctx ");
        queryLoad.append(" WHERE /*NOT EXISTS(SELECT 1 FROM ").append(tableVersion).append(" ")
                .append(alias).append("1, ContextOperation ctx1 ");
        queryLoad.append(" WHERE ").append(alias).append("1.TIME_STAMP <= ")
                .append(dbtranslator.getDateFormat(date23));
        queryLoad.append(" AND ").append(alias).append("1.TIME_STAMP > ").append(alias)
                .append(".TIME_STAMP ");
        queryLoad.append(" AND ctx1.status = ").append(Versionable.STATUS_APPLIED);
        queryLoad.append(" AND ").append(alias).append("1.agregatedobjectid = ").append(alias)
                .append(".agregatedobjectid AND ctx1.id = ").append(alias).append("1.operationpk").append(") ");
        queryLoad.append(" AND*/ ctx.STATUS = ").append(Versionable.STATUS_APPLIED)
                .append(" AND ctx.id = ").append(alias).append(".operationpk");

        return String.valueOf(queryLoad);
    }

    /**
     *
     * 
     * @param tableVersion tableVersion
     * @param tableObject tableObject
     * @param fields fields
     * @param timeStamp timeStamp
     * @return query to use
     */
    public static String getUniversalQueryByDate(String tableVersion, String tableObject, String[] fields,
                                                 java.util.Date timeStamp) {
        // TODO: 2006-04-15 (GS) In the search, this timestamp is allways the same.
        DBTranslator dbtranslator = DBTranslatorFactory.getDBClass();
        StringBuilder queryLoad = new StringBuilder(500);
        // 2009-08-27 (GS) Correccion error Produccion 6.8.0 Luisa
        String date23 = DateUtil.getFormatLongShow().format(timeStamp); // Must have time = 23:59:59

        String aliasVersion = AggregateObject.getTableAlias(tableVersion);
        String aliasObject = AggregateObject.getTableAlias(tableObject);

        String queryFields = (fields == null || fields.length < 1) ? "*" :
                      aliasObject + "." + StringUtil.unsplit(fields, ", " + aliasObject + ".");

        queryLoad.append("SELECT ").append(queryFields).append(" FROM ").append(tableVersion)
                .append(" ").append(aliasVersion).append(", ContextOperation ctx ")
                .append(", ").append(tableObject).append(" ").append(aliasObject);
        queryLoad.append(" WHERE NOT EXISTS(SELECT 1 FROM ").append(tableVersion).append(" ")
                .append(aliasVersion).append("1, ContextOperation ctx1 WHERE ").append(aliasVersion).append("1.TIME_STAMP <= ")
                .append(dbtranslator.getDateFormat(date23));
        queryLoad.append(" AND ").append(aliasVersion).append("1.TIME_STAMP > ").append(aliasVersion)
                .append(".TIME_STAMP ");
        queryLoad.append(" AND ctx1.status != ").append(Versionable.STATUS_TEMPORARY);
        queryLoad.append(" AND ").append(aliasVersion).append("1.status != ").append(Versionable.STATUS_REVERSED);
        queryLoad.append(" AND ").append(aliasVersion).append("1.agregatedobjectid = ").append(aliasVersion)
                .append(".agregatedobjectid AND ctx1.id = ").append(aliasVersion).append("1.operationpk").append(") ");
        queryLoad.append(" AND ctx.STATUS = ").append(Versionable.STATUS_APPLIED)
                .append(" AND ctx.id = ").append(aliasVersion).append(".operationpk AND ctx.TIME_STAMP <= ")
                .append(dbtranslator.getDateFormat(date23));
        queryLoad.append(" AND ").append(aliasVersion).append(".agregatedobjectid = ")
                .append(aliasObject).append(".agregatedobjectid");

        return String.valueOf(queryLoad);
    }

    public static String getUniversalQueryByDateWithPrepare(String tableVersion, String[] fields,
                                                 java.util.Date timeStamp) {
        // TODO: 2006-04-15 (GS) In the search, this timestamp is allways the same.
        StringBuilder queryLoad = new StringBuilder(500);
        String queryFields;
        String alias = AggregateObject.getTableAlias(tableVersion);

        queryFields = fields == null || fields.length < 1 ? "*" :
                      alias + "." + StringUtil.unsplit(fields, ", " + alias + ".");

        queryLoad.append("SELECT ").append(queryFields).append(" FROM ").append(tableVersion)
                .append(" ").append(alias)
                .append(", ContextOperation ctx WHERE NOT EXISTS(SELECT 1 FROM ")
                .append(tableVersion).append(" ").append(alias)
                .append("1, ContextOperation ctx1 WHERE ctx1.TIME_STAMP <= ? AND ctx1.TIME_STAMP > ctx.TIME_STAMP AND ctx1.status = ? AND ")
                .append(alias)
                .append("1.agregatedobjectid = ").append(alias)
                .append(".agregatedobjectid AND ctx.id = ").append(alias)

                //  Añadido por JCE y EMGC validando que las operaciones y los id de la poliza sean los mismos 2013-07-23
                .append("1.operationpk AND ctx1.item = ").append(alias)
                .append(".agregatedobjectid) AND ctx.STATUS = ? AND ctx.id = ").append(alias)
                .append(".operationpk AND ctx.TIME_STAMP <= ?  ");

        return String.valueOf(queryLoad);
    }

    public static String getUniversalQueryByIdAndStatusWithPrepare(String tableVersion, String[] fields)
    {
        // TODO: 2006-04-15 (GS) In the search, this timestamp is allways the same.
        StringBuilder queryLoad = new StringBuilder(500);
        String queryFields;
        String alias = AggregateObject.getTableAlias(tableVersion);

        queryFields = fields == null || fields.length < 1 ? "*" :
                alias + "." + StringUtil.unsplit(fields, ", " + alias + ".");

        queryLoad.append("SELECT ").append(queryFields).append(" FROM ").append(tableVersion)
                .append(" ").append(alias)
                .append(", ContextOperation ctx WHERE NOT EXISTS(SELECT 1 FROM ")
                .append(tableVersion).append(" ").append(alias)
                .append("1, ContextOperation ctx1 WHERE  ctx1.TIME_STAMP > ctx.TIME_STAMP AND ctx1.status = ? AND ")
                .append(alias)
                .append("1.agregatedobjectid = ").append(alias)
                .append(".agregatedobjectid AND ctx.id = ").append(alias)

                //  Añadido por JCE y EMGC validando que las operaciones y los id de la poliza sean los mismos 2013-07-23
                .append("1.operationpk AND ctx1.item = ").append(alias)
                .append(".agregatedobjectid) AND ctx.STATUS = ? AND ctx.id = ").append(alias)
                .append(".operationpk ");

        return String.valueOf(queryLoad);
    }




    public static String getUniversalQueryByDateNew(String tableVersion, String[] fields,
                                                            java.util.Date timeStamp) {
        StringBuilder queryLoad = new StringBuilder(500);
        String queryFields;

        String alias = AggregateObject.getTableAlias(tableVersion);

        queryFields = fields == null || fields.length < 1 ? "*" :
                alias + "." + StringUtil.unsplit(fields, ", " + alias + ".");

        queryLoad.append("SELECT * FROM ( SELECT ").append(queryFields).append(" FROM ").append(tableVersion)
                .append(" ").append(alias)
                .append(", ContextOperation ctx WHERE ctx.STATUS = ? AND ctx.id = ").append(alias)
                .append(".operationpk AND ").append(alias).append(".initialdate <= ? AND ctx.effective_date <= ? AND ").append(alias)
                .append(".agregatedobjectid = ? ORDER BY ctx.effective_date desc) WHERE ROWNUM = 1");

        return String.valueOf(queryLoad);
    }
    /**
     * Returns the last applied version query of an <code>AggregateObject</code> according to the
     * aggregated table
     * @param aggregatedType String
     * @param atFields String[]
     * @param vtFields String[]
     * @return String
     * @deprecated Don't pass the fields as String[], change it to use the String method, so that we don't need to parse the arrays again into a String.
     */
    public static String getUniversalVersionQueryByAggregatedOrdered(String aggregatedType,
                                                                     String[] atFields,
                                                                     String[] vtFields) {
        return getUniversalVersionQueryByAggregatedOrdered(aggregatedType, atFields, vtFields,
                                                           false);
    }

    /**
     * Returns the last applied version query of an <code>AggregateObject</code> according to the
     * aggregated table
     * @param aggregatedType String
     * @param atFields String[]
     * @param vtFields String[]
     * @return String
     * @deprecated Don't pass the fields as String[], change it to use the String method, so that we don't need to parse the arrays again into a String.
     */
    public static String getUniversalVersionQueryByAggregated(String aggregatedType,
                                                              String[] atFields,
                                                              String[] vtFields,long[] state) {
        return getUniversalVersionQueryByAggregated(aggregatedType, atFields, vtFields, false,
                                                    false, Versionable.STATUS_APPLIED,state);
    }

    /**
     * Returns the last applied version query of an <code>AggregateObject</code> according to the
     * aggregated table
     * @param aggregatedType String
     * @param atFields String[]
     * @param vtFields String[]
     * @param isPendingChange boolean
     * @return String
     * @deprecated Don't pass the fields as String[], change it to use the String method,
     *             so that we don't need to parse the arrays again into a String.
     */
    // TODO: No recibir los String[], sino el String ya correctamente armado
    public static String getUniversalVersionQueryByAggregatedOrdered(String aggregatedType,
                                                                     String[] atFields,
                                                                     String[] vtFields,
                                                                     boolean isPendingChange) {
        String aggregatedTable = AggregateObject.getTable(aggregatedType);
        String versionTable = AggregateObject.getTableVersion(aggregatedType);

        String aggregatedTableAlias = AggregateObject.getTableAlias(aggregatedTable);
        String versionTableAlias = AggregateObject.getTableAlias(versionTable);

        boolean thereAreAggregatedFields = atFields != null && atFields.length > 0;
        boolean thereAreVersionFields = vtFields != null && vtFields.length > 0;

        String queryFields;

        if (thereAreAggregatedFields || thereAreVersionFields) {
            queryFields = thereAreAggregatedFields ? aggregatedTableAlias + StringUtil.DOT +
                                                     StringUtil.unsplit(atFields, ", " +
                                                                                  aggregatedTableAlias +
                                                                                  StringUtil.DOT) :
                          StringUtil.EMPTY_STRING;
            queryFields += thereAreAggregatedFields && thereAreVersionFields ? ", " :
                           StringUtil.EMPTY_STRING;
            queryFields += thereAreVersionFields ? versionTableAlias + StringUtil.DOT + StringUtil
                    .unsplit(vtFields, ", " + versionTableAlias + StringUtil.DOT) :
                           StringUtil.EMPTY_STRING;

        } else {
            queryFields = StringUtil.ASTERISK;
        }

        return getUniversalVersionQueryByAggregatedOrdered(aggregatedType, queryFields,
                                                           isPendingChange);
    }


    /**
     * Returns the alias.fields according to the aggregated table
     * @param aggregatedType String
     * @param atFields String[]
     * @param vtFields String[]
     * @return String
     */
    public static String getTemporaryTableFields(String aggregatedType, String[] atFields,
                                                 String[] vtFields) {
        String aggregatedTable = AggregateObject.getTable(aggregatedType);
        String versionTable = AggregateObject.getTableVersion(aggregatedType);

        String aggregatedTableAlias = AggregateObject.getTableAlias(aggregatedTable);
        String versionTableAlias = AggregateObject.getTableAlias(versionTable);

        boolean thereAreAggregatedFields = atFields != null && atFields.length > 0;
        boolean thereAreVersionFields = vtFields != null && vtFields.length > 0;

        String queryFields;

        if (thereAreAggregatedFields || thereAreVersionFields) {
            queryFields = thereAreAggregatedFields ? aggregatedTableAlias + StringUtil.DOT +
                                                     StringUtil.unsplit(atFields, ", " +
                                                                                  aggregatedTableAlias +
                                                                                  StringUtil.DOT) :
                          StringUtil.EMPTY_STRING;
            queryFields += thereAreAggregatedFields && thereAreVersionFields ? ", " :
                           StringUtil.EMPTY_STRING;
            queryFields += thereAreVersionFields ? versionTableAlias + StringUtil.DOT + StringUtil
                    .unsplit(vtFields, ", " + versionTableAlias + StringUtil.DOT) :
                           StringUtil.EMPTY_STRING;

        } else {
            queryFields = "*";
        }

        return queryFields;
    }


    public static String getUniversalVersionQueryByAggregated(String aggregatedType,
                                                              String[] atFields, String[] vtFields,
                                                              boolean isPendingChange,
                                                              boolean count, int status, long[] state) {
        return getUniversalVersionQueryByAggregated(aggregatedType,atFields,vtFields,null,isPendingChange,count,status, state, null,null);

    }



    /**
     * Returns the last applied version query of an <code>AggregateObject</code> according to the
     * aggregated table
     *
     * @param aggregatedType String
     * @param atFields String[]
     * @param vtFields String[]
     * @param isPendingChange boolean
     * @param count boolean
     * @return String
     * @deprecated Don't pass the fields as String[], change it to use the String method,
     *             so that we don't need to parse the arrays again into a String.
     */
    // TODO: No recibir los String[], sino el String ya correctamente armado
    public static String getUniversalVersionQueryByAggregated(String aggregatedType,
                                                              String[] atFields, String[] vtFields, String[] ctFields ,
                                                              boolean isPendingChange,
                                                              boolean count, int status, long [] state, QueryParameter queryParameterThirdParty, boolean isFacultative) {
        String queryFields = getQueryFields(aggregatedType, atFields, vtFields, ctFields);

        return getUniversalVersionQueryByAggregated(aggregatedType, queryFields, isPendingChange,
                                                    count, status, state, queryParameterThirdParty, isFacultative);
    }

    public static String getUniversalVersionQueryByAggregated(String aggregatedType,
                                                              String[] atFields, String[] vtFields, String[] ctFields ,
                                                              boolean isPendingChange,
                                                              boolean count, int status, long [] state, QueryParameter queryParameterThirdParty,String isSearchPolicyCreateFacultative) {
        String queryFields = getQueryFields(aggregatedType, atFields, vtFields, ctFields);

        return getUniversalVersionQueryByAggregated(aggregatedType, queryFields, isPendingChange,
                count, status, state, queryParameterThirdParty,isSearchPolicyCreateFacultative);
    }

    public static String getQueryFields(String aggregatedType,
                            String[] atFields, String[] vtFields, String[] ctFields){
        String aggregatedTable = AggregateObject.getTable(aggregatedType);
        String versionTable = AggregateObject.getTableVersion(aggregatedType);
        String contextTable = AggregateObject.getTable(AggregateObject.CONTEXT_OPERATION);

        String aggregatedTableAlias = AggregateObject.getTableAlias(aggregatedTable);
        String versionTableAlias = AggregateObject.getTableAlias(versionTable);
        String contextTableAlias = AggregateObject.getTableAlias(contextTable);

        boolean thereAreAggregatedFields = atFields != null && atFields.length > 0;
        boolean thereAreVersionFields = vtFields != null && vtFields.length > 0;
        boolean thereAreContextFields = ctFields != null && ctFields.length > 0;

        String queryFields = "";

        if (thereAreAggregatedFields || thereAreVersionFields || thereAreContextFields) {
            queryFields =thereAreContextFields?"row_number()OVER(order by ":"";
            queryFields += thereAreContextFields?contextTableAlias + StringUtil.DOT +
                    StringUtil.unsplit(ctFields, "," +
                            contextTableAlias +
                            StringUtil.DOT):
                    StringUtil.EMPTY_STRING;
            queryFields += thereAreContextFields?" desc) rn, ":"";
            queryFields += thereAreAggregatedFields ? aggregatedTableAlias + StringUtil.DOT +
                    StringUtil.unsplit(atFields, ", " +
                            aggregatedTableAlias +
                            StringUtil.DOT) :
                    StringUtil.EMPTY_STRING;
            queryFields += thereAreAggregatedFields && thereAreVersionFields ? ", " :
                    StringUtil.EMPTY_STRING;
            queryFields += thereAreVersionFields ? versionTableAlias + StringUtil.DOT + StringUtil
                    .unsplit(vtFields, ", " + versionTableAlias + StringUtil.DOT) :
                    StringUtil.EMPTY_STRING;

        } else {
            queryFields = "*";
        }

        return queryFields;
    }

    /**
     * Returns the last applied version query of an <code>AggregateObject</code> according to the
     * aggregated table
     * @param aggregatedType String
     * @param queryFields String
     * @param isPendingChange boolean
     * @return String
     */
    public static String getUniversalVersionQueryByAggregatedOrdered(String aggregatedType,
                                                                     String queryFields,
                                                                     boolean isPendingChange) {
        StringBuilder query = new StringBuilder();

        String aggregatedTable = AggregateObject.getTable(aggregatedType);
        String versionTable = AggregateObject.getTableVersion(aggregatedType);

        String aggregatedTableAlias = AggregateObject.getTableAlias(aggregatedTable);
        String versionTableAlias = AggregateObject.getTableAlias(versionTable);

        query.append("SELECT ").append(queryFields).append(" FROM ");
        String predefinedTableName = StringUtil.EMPTY_STRING;

        query.append(aggregatedTable).append(" ").append(aggregatedTableAlias).append(", ")
                .append(versionTable).append(" ").append(versionTableAlias)
                .append(", ContextOperation ctx WHERE ").append(versionTableAlias)
                .append(".agregatedobjectid = ")
                .append(aggregatedTableAlias).append(".").append(aggregatedTable).append("Id")
                .append(" AND ").append(aggregatedTableAlias).append(".operationpk = ")
                .append(versionTableAlias).append(".operationPk").append(" AND ctx.id = ")
                .append(versionTableAlias).append(".operationpk").append(" AND ctx.status = ").
                append(Versionable.STATUS_APPLIED);


        query.append(" ORDER BY agregatedparentid ASC");

        return String.valueOf(query);
    }

    /**
     * Returns the last applied version query of an <code>AggregateObject</code> according to the
     * aggregated table
     * @param aggregatedType String
     * @param queryFields String
     * @param count boolean
     * @param isPendingChange boolean
     * @return String
     */
    public static String getUniversalVersionQueryByAggregated(String aggregatedType,
                                                              String queryFields,
                                                              boolean isPendingChange,
                                                              boolean count, long [] state, QueryParameter queryParameterThirdParty) {
        return getUniversalVersionQueryByAggregated(aggregatedType, queryFields, isPendingChange,
                count, Versionable.STATUS_APPLIED, state, queryParameterThirdParty,null);
    }

    /**
     * Returns the last applied version query of an <code>AggregateObject</code> according to the
     * aggregated table
     *
     *
     * @param aggregatedType String
     * @param queryFields String
     * @param isPendingChange boolean
     * @param count boolean
     * @param state
     * @return String
     */
    public static String getUniversalVersionQueryByAggregated(String aggregatedType,
                                                              String queryFields,
                                                              boolean isPendingChange,
                                                              boolean count, int status, long[] state, QueryParameter queryParameterThirdParty,String isSearchPolicyCreateFacultative) {
        StringBuilder query = new StringBuilder();
        StringBuilder queryThirdParty = new StringBuilder();

        String aggregatedTable = AggregateObject.getTable(aggregatedType);
        String aggregatedTableAlias = AggregateObject.getTableAlias(aggregatedTable);
        if(log.isDebugEnabled()) {
            log.debug("aggregatedTableAlias = '" + aggregatedTableAlias + "'");
            log.debug("aggregatedTable = '" + aggregatedTable + "'");
        }
        String versionTable = AggregateObject.getTableVersion(aggregatedType);
        String versionTableAlias = AggregateObject.getTableAlias(versionTable);
        if(log.isDebugEnabled()) {
            log.debug("versionTableAlias = '" + versionTableAlias + "'");
            log.debug("versionTable = '" + versionTable + "'");
        }
        query = buildQuerySelectSection(query, count, queryFields);
        query = buildQueryFormSection(query, aggregatedTable, aggregatedTableAlias, versionTable, versionTableAlias,
                false, state, queryParameterThirdParty, queryThirdParty, "", "");
        query = buildQueryWhereSection(query, aggregatedTable, aggregatedTableAlias, versionTableAlias, false,
                state, "",isSearchPolicyCreateFacultative);
        if(queryParameterThirdParty!=null && queryParameterThirdParty.getParameter("isReinsurance") != null ){
            boolean isreinsurance = Boolean
                    .parseBoolean(queryParameterThirdParty.getParameter("isReinsurance").toString());
            if(isreinsurance) {

                query.append(" AND ").append("reins").append(".").append(aggregatedTable).append("Id = ");
                query.append(aggregatedTableAlias).append(".").append(aggregatedTable).append("Id ");
            }
        }

        if(log.isDebugEnabled())log.debug("Query Aggregated Manager = " + query);

        return String.valueOf(query);
    }

    public static String getUniversalVersionQueryByAggregated(String aggregatedType,
                                                              String queryFields,
                                                              boolean isPendingChange,
                                                              boolean count, int status, long[] state, QueryParameter queryParameterThirdParty, boolean isFacultative) {
        StringBuilder query = new StringBuilder();
        StringBuilder queryThirdParty = new StringBuilder();

        String aggregatedTable = AggregateObject.getTable(aggregatedType);
        String aggregatedTableAlias = AggregateObject.getTableAlias(aggregatedTable);
        if(log.isDebugEnabled()) {
            log.debug("aggregatedTableAlias = '" + aggregatedTableAlias + "'");
            log.debug("aggregatedTable = '" + aggregatedTable + "'");
        }
        String versionTable = AggregateObject.getTableVersion(aggregatedType);
        String versionTableAlias = AggregateObject.getTableAlias(versionTable);
        if(log.isDebugEnabled()) {
            log.debug("versionTableAlias = '" + versionTableAlias + "'");
            log.debug("versionTable = '" + versionTable + "'");
        }
        String facultativePolicyTable = AggregateObject.getTable(AggregateObject.FACULTATIVE_POLICY);
        String facultativePolicyAlias = AggregateObject.getTableAlias(AggregateObject.FACULTATIVE_POLICY);
        if(log.isDebugEnabled()) {
            log.debug("versionTableAlias = '" + facultativePolicyAlias + "'");
            log.debug("versionTable = '" + facultativePolicyTable + "'");
        }

        query = buildQuerySelectSection(query, count, queryFields);
        query = buildQueryFormSection(query, aggregatedTable, aggregatedTableAlias, versionTable, versionTableAlias,
                isFacultative, state, queryParameterThirdParty, queryThirdParty, facultativePolicyTable, facultativePolicyAlias);
        query = buildQueryWhereSection(query, aggregatedTable, aggregatedTableAlias, versionTableAlias, isFacultative,
                state, facultativePolicyAlias,null);

        if(log.isDebugEnabled())log.debug("Query Aggregated Manager = " + query);

        return String.valueOf(query);
    }

    public static StringBuilder buildQuerySelectSection(StringBuilder query, boolean count, String queryFields){

        if (count) {
            query.append("SELECT count(1) as cnt"); // DB2 Certification
        } else {
            query.append("SELECT * FROM( SELECT ").append(queryFields);
        }

        return query;
    }

    public static StringBuilder buildQueryFormSection(StringBuilder query, String aggregatedTable,
                                                     String aggregatedTableAlias, String versionTable,
                                                     String versionTableAlias, boolean isFacultative,
                                                     long[] state, QueryParameter queryParameterThirdParty,
                                                     StringBuilder queryThirdParty, String facultativePolicyTable,
                                                     String facultativePolicyAlias){

        query.append(" FROM ");
        query.append(aggregatedTable).append(" ").append(aggregatedTableAlias).append(", ")
                .append(versionTable).append(" ").append(versionTableAlias);
        if(isFacultative){
            query.append(", ").append(facultativePolicyTable).append(" ").append(facultativePolicyAlias);
        }
        query.append(", ContextOperation ").append(versionTableAlias).append("ctx ");

        if(queryParameterThirdParty != null && queryParameterThirdParty.getParameter("isReinsurance") != null ){
            boolean isReinsurance = Boolean
                    .parseBoolean(queryParameterThirdParty.getParameter("isReinsurance").toString());
            if(isReinsurance) {
                query.append(", "+Constants.REINSURANCE_OPERATION).append(" reins ");
            }
        }

        if(queryParameterThirdParty!=null) {
            String template = (String) queryParameterThirdParty.getParameter("template");

            if (template.equalsIgnoreCase("NaturalPerson") || template.equalsIgnoreCase("LegalPerson")) {

                queryThirdParty.append(", (");
                Categorias defaultTemplates = Categorias.getBean(Categorias.DEFAULT_TEMPLATES_STATE);
                Categorias extendedTemplates = Categorias.getBean(Categorias.EXTENDED_TEMPLATES_STATE);

                String tableName = queryParameterThirdParty.getCategory();
                CotType cotType = CotType.getCotType(tableName);

                ConfigurableObjectType cot;
                Object defCOT = defaultTemplates.get(cotType, template);
                if (defCOT == null) {
                    cot = (ConfigurableObjectType) extendedTemplates.get(cotType, template);
                } else {
                    cot = (ConfigurableObjectType) defCOT;
                }
                boolean isLike = AcseleConf.getProperty("search.like").equals(AcseleConstants.TRUE) ? true : false;
                queryThirdParty.append(SearchUtil.getDefaultConfigurableObjectQuery(cot, queryParameterThirdParty, isLike));
                queryThirdParty.append(") dco ");
                query.append(queryThirdParty);
            }
        }
        if (state != null && 0 < state.length) {
            query.append(", State ").append("st ");
        }
        return query;
    }

    public static StringBuilder buildQueryWhereSection(StringBuilder query, String aggregatedTable,
                                                      String aggregatedTableAlias, String versionTableAlias,
                                                      boolean isFacultative, long[] state, String facultativePolicyAlias,String isSearchPolicyCreateFacultative){
        query.append("WHERE ");
        if (state != null && 0 < state.length) {
            query.append("st.stateid =").append(versionTableAlias).append(".stateid ");
            query.append(" AND ").append(" st.stateid in ").append("(");
            for (int i = 0; i < state.length; i++) {
                query.append(state[i]);
                if (i < state.length - 1) {
                    query.append(",");
                }
            }
            query.append(") AND ");
        }

        if (isSearchPolicyCreateFacultative != null && isSearchPolicyCreateFacultative != "" && isSearchPolicyCreateFacultative.equals("true")) {
            query.append(versionTableAlias).append(".agregatedobjectid = ");
            query.append(aggregatedTableAlias).append(".").append(aggregatedTable).append("Id");
        }else{
            query.append(versionTableAlias).append(".operationpk = ");
            query.append(aggregatedTableAlias).append(".operationpk");
        }

        if (isFacultative) {
            query.append(" AND ")
                    .append(aggregatedTableAlias).append(".").append(aggregatedTable).append("Id = ")
                    .append(facultativePolicyAlias).append(".pol_id");
        }
        query.append(" AND ")
                .append(versionTableAlias).append("ctx.id = ")
                .append(versionTableAlias).append(".operationpk")
                .append(" AND ");
        if (!StringUtil.isEmptyOrNullValue(isSearchPolicyCreateFacultative) && isSearchPolicyCreateFacultative.equals("true")) {
            query.append(versionTableAlias).append("ctx.STATUS!=");
        }else{
            query.append(versionTableAlias).append("ctx.STATUS=");
        }
        query.append(StatusContextOperation.STATUS_APPLIED.getValue());

        return query;
    }

    public static void addQueryPostfix(String type, StringBuilder query, String versionTableAlias,
                                       int index, String queryFields, int numPage,
                                       boolean maxResultNumber, boolean fromPolicySearch, String nroCaso) {
        int recordDisplay = RECORD_DISPLAY;

        if (maxResultNumber) {

            try {
                recordDisplay = Integer.parseInt(AcseleConf.getProperty("recordDisplay"));
                if(log.isDebugEnabled())log.debug(">>> recordDisplay = " + recordDisplay);
            } catch (Exception e) {
                log.error("Can't parse the line number.  Using default value of " + RECORD_DISPLAY);
            }


        }
        if ((maxResultNumber && !"lp".equals(type) && !"pp".equals(type) && !"np".equals(type)) ||
                "fp".equals(type)) {
            if (fromPolicySearch){
                if(ClientInfo.isClientRunning("Interseguro") && !StringUtil.isEmpty(nroCaso)){
                    recreateQuery(query, queryFields, nroCaso);
                } else {
                    query.append(" GROUP BY ").append(queryFields).append(" order by pdcoctx.auditdate desc)");
                    query.append(" WHERE rn <= ").append(AcseleConf.getProperty("queryRowLimit"));
                }

            }else{
                query.append(" AND rownum <= ").append(AcseleConf.getProperty("queryRowLimit"));
            }
        }




        if ("pp".equals(type)) {

            if (fromPolicySearch){
            query.append(" GROUP BY ").append(queryFields).append(" order by pdcoctx.auditdate desc) where  rn BETWEEN ");
            }else{
                query.append(" GROUP BY rownum, ").append(queryFields).append(" HAVING rownum BETWEEN ");
            }
            query.append((numPage - 1) * recordDisplay + 1).append(" AND ")
                    .append((numPage * recordDisplay));
        } else if ("np".equals(type)) {

            if (fromPolicySearch){
                query.append(" GROUP BY ").append(queryFields).append(" order by pdcoctx.auditdate desc) where  rn BETWEEN ");
            }else{
                query.append(" GROUP BY rownum, ").append(queryFields).append(" HAVING rownum BETWEEN ");
            }

                query.append((numPage - 1) * recordDisplay + 1).append(" AND ")
                    .append((numPage * recordDisplay));

        } else if ("lp".equals(type)) {


             if (fromPolicySearch){
                 query.append(" GROUP BY ").append(queryFields).append(" order by pdcoctx.auditdate desc) where  rn BETWEEN ");
             }else{
                 query.append(" GROUP BY rownum, ").append(queryFields).append(" HAVING rownum BETWEEN ");
             }
             query.append(((numPage - 1) * recordDisplay) + 1).append(" AND ")
                    .append((numPage * recordDisplay));
        }
    }

    // TODO: Estudiar si es posible hacer que reciba un arreglo de Strings, para eliminar el '*'

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param idOperation long
     * @param conn Connection
     * @return query to use
     */
    protected static PreparedStatement getQueryByOperationPK(AggregateObject aggregatedObject,
                                                             long idOperation, Connection conn) {
        PreparedStatement preparedStatement = null;
        String queryLoad = String.format("SELECT ctx.time_stamp as lasttimestamp, ctx.status as status, OUTER.* FROM %s OUTER, ContextOperation ctx WHERE agregatedObjectId = ? AND operationPk = ? AND OUTER.STATUS <> ? AND OUTER.STATUS <> ? and ctx.ID = OUTER.operationpk",aggregatedObject.getTableVersion());
        String aggregatedObjectPk = aggregatedObject.getPk();
        Long operationPK = idOperation;

        if (log.isDebugEnabled()) {
            log.debug("[method getQueryByOperationPK] queryLoad = " + queryLoad);
            log.debug("**agregatedObject = "+aggregatedObject.getClass());
            log.debug("**agregatedObjectId = "+aggregatedObject.getPk());
            log.debug("**operationPk = "+operationPK);
        }
        try {
            preparedStatement = conn.prepareStatement(queryLoad);
            preparedStatement.setLong(1, Long.valueOf(aggregatedObjectPk));
            preparedStatement.setLong(2, operationPK);
            preparedStatement.setInt(3, Versionable.STATUS_CLOSED);
            preparedStatement.setInt(4, Versionable.STATUS_CLOSED);

        } catch (SQLException e) {
            JDBCUtil.closeQuietly(preparedStatement);
            throw new TechnicalException("Error creating prepared statement", Severity.FATAL, e);
        }
        return preparedStatement;
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @return query to use
     */
    protected static String getQueryByOperationPKWithReversed(AggregateObject aggregatedObject) {
        StringBuilder queryLoad = new StringBuilder();
        queryLoad.append("SELECT OUTER.*, ctx.time_stamp as lasttimestamp, ctx.status as status FROM ")
                .append(aggregatedObject.getTableVersion())
                .append(" OUTER, ContextOperation ctx ");
        queryLoad
                .append(" WHERE agregatedObjectId = ? AND operationPk = ? AND OUTER.STATUS <> ? and ctx.ID = OUTER.operationpk");
        return String.valueOf(queryLoad);
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param idOperation int
     * @param stm PreparedStatement
     */
    protected static void bindQueryByOperationPKWithReversed(AggregateObject aggregatedObject,
                                                             long idOperation,
                                                             PreparedStatement stm) {

        long operationPk = idOperation;
        String aggregatedObjectPK = aggregatedObject.getPk();

        try {
            stm.setLong(1, Long.valueOf(aggregatedObjectPK));
            stm.setLong(2, operationPk);
            stm.setInt(3, Versionable.STATUS_CLOSED);

        } catch (SQLException e) {
            log.error("Error", e);
            throw new TechnicalException("Error binding variables.", Severity.FATAL, e);
        }

    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @return query to use
     */
    protected String getUndoVersionQuery(AggregateObject aggregatedObject) {
        DBTranslator dbtranslator = DBTranslatorFactory.getDBClass();
        String dcoID = aggregatedObject.getIDDCO();
        String aggregateObjectID = aggregatedObject.getPk();
        String oldTimeStampStr = DateUtil.getFormatLongShow().format(aggregatedObject.getTimeStamp());

        StringBuilder queryDelete = new StringBuilder();
        queryDelete.append("DELETE ").append(aggregatedObject.getTableVersion());
        queryDelete.append(" WHERE DCOID = ").append(dcoID);
        queryDelete.append(" AND AGREGATEDOBJECTID = ").append(aggregateObjectID);
        queryDelete.append(" AND  TIME_STAMP = ")
                .append(dbtranslator.getDateFormat(oldTimeStampStr));

        return String.valueOf(queryDelete);
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @throws SQLException error
     */
    public abstract void save(AggregateObject aggregatedObject) throws SQLException;

    /**
     * Updates the Object
     * @param aggregateObject AggregateObject
     * @throws SQLException error
     */
    public void update(AggregateObject aggregateObject) throws SQLException {
        update(aggregateObject, null);
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param conParam Connection
     * @throws SQLException error
     */
    public abstract void update(AggregateObject aggregatedObject, Connection conParam)
            throws SQLException;

    /**
     * checkTemporaryVersion()
     * Checks the creation or modification of Temporary Versions
     * @param aggregatedObject AggregateObject
     * @param conParam Connection
     * @param returN StringBuffer
     * @return boolean true if ok, false otherwise
     * @throws Exception error
     */
    private boolean checkTemporaryVersion(AggregateObject aggregatedObject, Connection conParam,
                                          StringBuilder returN) throws Exception {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        String query = "";

        try {
            conn = (conParam == null) ? JDBCUtil.openUserDbConnection() : conParam;
            query = "SELECT MAX(vt.TIME_STAMP) as TS FROM " + aggregatedObject.getTableVersion() +
            " vt, ContextOperation ctx WHERE ctx.TIME_STAMP > ? AND ctx.STATUS = ? AND vt.operationpk = ctx.id and AGREGATEDOBJECTID = ?";

            Date timeStampObj = aggregatedObject.getTimeStamp();
            Timestamp timeStamp = new java.sql.Timestamp(timeStampObj.getTime());
            Long aggregatedObjectId = aggregatedObject.getId();

            stmt = conn.prepareStatement(query);
            stmt.setTimestamp(1, timeStamp);
            stmt.setInt(2, Versionable.STATUS_APPLIED);
            stmt.setLong(3, aggregatedObjectId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                String ts = rs.getString("TS");
                if (returN != null) {
                    returN.append(ts);
                }
                return StringUtil.isEmptyOrNullValue(ts) || ts.trim().equalsIgnoreCase("NULL");
            }
            return true;
        } catch (SQLException e) {
            log.error("checkTemporaryVersion - Error al validar la version temporal.  Query = " +
                      query, e);
            throw new TechnicalException(
                    "Error en AggregatedManager al validar la version temporal", Severity.FATAL, e);
        } finally {
            JDBCUtil.closeQuietly(rs, stmt, conParam == null ? conn : null);
        }
    }

    /**
     * Creates a Temporary Version of the Object
     * @param aggregatedObject AggregateObject
     * @param conParam Connection
     * @throws Exception
     */
    public void createTemporaryVersion(AggregateObject aggregatedObject, Connection conParam)
            throws Exception {

        String actEvents = AcseleConf.getProperty(EventType.ACTIVATE_EVENTS);
        List list = StringUtil.splitAsList(actEvents, StringUtil.COMMA);
        boolean skip = list.contains(aggregatedObject.getAgregatedPolicy().getEventTypeDesc());

        long ini;
        if (MeasuresLog.ISON) ini = System.currentTimeMillis();

        aggregatedObject.getAgregatedPolicy()
                    .setOldOperationPK(aggregatedObject.getAgregatedPolicy().getOperationPK());

        OperationPK opk = aggregatedObject.generatedOperationPK();
        if (log.isDebugEnabled()) log.debug("Starting createTemporaryVersion.. Object = " + aggregatedObject+" - opk = "+opk);

        StringBuilder currentDate = new StringBuilder(10);
        if (skip || checkTemporaryVersion(aggregatedObject, conParam, currentDate)) {
            if (MeasuresLog.ISON) {
                try {
                    MeasuresLog.getCurrentObject().addChild("CheckTemporary Version")
                            .setTime(System.currentTimeMillis() - ini);
                } catch (Exception e) {
                    log.error("Error at create Temporary Version ",e);
                }
                ini = System.currentTimeMillis();
            }
            if (checkNewTemporaryVersion(aggregatedObject, conParam)) {

                PreparedStatement stmt = null;
                Connection conn = null;

                /**
                 * La version temporal que se va a crear se crea siempre usando la Fecha de la Operacion
                 * y en un status de Versionable.STATUS_TEMPORARY. Esto significa que pueden crearse
                 * varias versiones temporales para esta tabla en un mismo instante de tiempo, generadas
                 * por distintas Operaciones. Sin embargo al final solo una sera aplicada, basandose en
                 * un algoritmo PRIMERO QUE LLEGUE, PRIMERO EN APLICAR.
                 */
                String query;
                try {
                    conn = (conParam == null) ? JDBCUtil.openUserDbConnection() : conParam;

                    query = getCreateTemporaryVersionQuery(aggregatedObject);
                    stmt = conn.prepareStatement(query);
                    aggregatedObject.setStatus(Versionable.STATUS_TEMPORARY);
                    bindCreateTemporaryVersionQuery(aggregatedObject, stmt);

                    stmt.executeUpdate();

                } catch (Exception e) {
                    if(aggregatedObject.getAgregatedPolicy().getEventType() != null
                            && aggregatedObject.getAgregatedPolicy().getEventType().isAllowSimulation()){
                        log.error("Error at create Temporary Version for a Simulated Object", e);
                    } else {
                        throw new TechnicalException(Exceptions.PPErrorCreatingTemporaryVersion,
                                Severity.FATAL, e);
                    }
                } finally {
                    JDBCUtil.closeQuietly(stmt, conParam == null ? conn : null);
                }

                if (MeasuresLog.ISON) {
                    try {
                        MeasuresLog.getCurrentObject().addChild("Query will be executed = " + query)
                                .setTime(System.currentTimeMillis() - ini);
                    } catch (Exception e) {
                        log.error("Error at create Temporary Version ", e);
                    }
                }
            }
        } else {
            log.error(
                    "Policy's Event Date lesser than Policy's last applied Date.  Check previous traces...");
            throw new ApplicationException(Exceptions.PPEventDateLesserThanLastApplied,
                                           Severity.FATAL, " ('" + aggregatedObject.getTimeStamp() +
                                                           "' " + Exceptions
                    .getMessage(Exceptions.PPiSLesserThan) + " '" + currentDate + "')");
        }
    }

    protected boolean checkNewTemporaryVersion(AggregateObject aggregatedObject, Connection conParam) throws Exception {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        Long aggregatedOperationPKID = aggregatedObject.getOperationPK().getId();
        Long aggregatedObjectID = aggregatedObject.getId();

        StringBuilder query = new StringBuilder();
        if(log.isDebugEnabled()) {
            log.debug("AggregateObject " + aggregatedObject.getClass().getName());
            log.debug("aggregatedObject.getOperationPK() = " + aggregatedObject.getOperationPK());
            log.debug("aggregatedObject.getId() = " + aggregatedObject.getId());
        }
        try {
            conn = (conParam == null) ? JDBCUtil.openUserDbConnection() : conParam;

            query.append("SELECT * FROM ").append(aggregatedObject.getTableVersion());
            query.append(" vt WHERE vt.OPERATIONPK = ? AND vt.AGREGATEDOBJECTID = ?");

            if(log.isDebugEnabled())log.debug("query = " + query);

            stmt = conn.prepareStatement(query.toString());
            stmt.setLong(1, aggregatedOperationPKID);
            stmt.setLong(2, aggregatedObjectID);

            rs = stmt.executeQuery();
            return !rs.next();
        } catch (SQLException e) {
            log.error("checkTemporaryVersion - Error al validar la version temporal.  Query = " +
                    query, e);
            throw new TechnicalException(
                    "Error en AggregatedManager al validar la version temporal", Severity.FATAL, e);
        } finally {
            JDBCUtil.closeQuietly(rs, stmt, conParam == null ? conn : null);
        }
    }


    /**
     *
     * @param aggregatedObject AggregateObject
     * @return String
     */
    protected abstract String getCreateTemporaryVersionQuery(AggregateObject aggregatedObject);

    protected abstract void bindCreateTemporaryVersionQuery(AggregateObject aggregatedObject,
                                                            PreparedStatement stm);

    /**
     * Updates a Temporary Version of the Object
     * @param aggregatedObject AggregateObject
     * @param conParam Connection
     * @throws Exception error
     */
    public void updateTemporaryVersion(AggregateObject aggregatedObject, Connection conParam)
            throws Exception {

        //TODO: @Performance@
        String actEvents = AcseleConf.getProperty(EventType.ACTIVATE_EVENTS);
        java.util.List list = StringUtil.splitAsList(actEvents, StringUtil.COMMA);
        boolean skip =
                list.contains(aggregatedObject.getAgregatedPolicy().getEventTypeDesc());

        if (skip || checkTemporaryVersion(aggregatedObject, conParam, null)) {
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = (conParam == null) ? JDBCUtil.openUserDbConnection() : conParam;
                String queryToBind = getUpdateTemporaryVersionQuery(aggregatedObject);
                stmt = conn.prepareStatement(queryToBind);
                bindUpdateTemporaryVersionQuery(aggregatedObject, stmt);

                stmt.executeUpdate();

            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new TechnicalException(Exceptions.PPErrorUpdatingTemporaryVersion,
                                             Severity.FATAL, e);
            } finally {
                JDBCUtil.closeQuietly(null, stmt, conParam == null ? conn : null);
            }
        } else {
            throw new ApplicationException(
                    "Error en AgregatedPolicyManager al validar la versi�n temporal",
                    Severity.FATAL);
        }
    }

    /**
     * Applies a Temporary Version
     * @param aggregatedObject AggregateObject
     * @param conParam Connection
     * @param newTimeStamp Date
     * @throws Exception error
     */
    public void applyVersion(AggregateObject aggregatedObject, Connection conParam,
                             Date newTimeStamp) throws Exception {


                AggregatedManager.applyAggregated(aggregatedObject, newTimeStamp, conParam);

    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param newTimeStamp Date
     * @param conn Connection
     * @throws SQLException error
     */
    private static void applyAggregated(AggregateObject aggregatedObject, Date newTimeStamp,
                                        Connection connParam) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {

            conn = (connParam == null) ? JDBCUtil.openUserDbConnection() : connParam;
            String queryUpdateAggregated =
                    AggregatedManager.getApplyAggregateQuery(aggregatedObject, newTimeStamp);

            stmt = conn.prepareStatement(queryUpdateAggregated);
            bindApplyAggregateQuery(aggregatedObject, newTimeStamp, stmt);
            stmt.executeUpdate();

            long operationId = Long.valueOf(aggregatedObject.getOperationPK().getPK());
            aggregatedObject.setLastAppliedOperationId(operationId);
            aggregatedObject.setLastAppliedTimeStamp(newTimeStamp);
        } finally {
            JDBCUtil.closeQuietly(null, stmt, (connParam == null) ? conn : null);
        }
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param timeStamp Date
     * @return query to use
     */
    private static String getApplyAggregateQuery(AggregateObject aggregatedObject, Date timeStamp) {
        DBTranslator dbtranslator = DBTranslatorFactory.getDBClass();
        String field;
        if (aggregatedObject instanceof Participation) {
            field = "AGREGATEDOBJECTID";
        } else if (aggregatedObject instanceof AggregatedFund) {
            field = "AFU_Id";
        } else {
            field = aggregatedObject.getTable() + "Id";
        }

        StringBuilder queryUpdateAggregated = new StringBuilder();

        queryUpdateAggregated.append("UPDATE ").append(aggregatedObject.getTable());
        queryUpdateAggregated.append(dbtranslator.addRowLock())
                .append(" SET time_stamp = ?, operationPk = ? WHERE ").append(field).append(" = ?");

        return String.valueOf(queryUpdateAggregated);
    }

    private static void bindApplyAggregateQuery(AggregateObject aggregatedObject, Date timeStamp,
                                                PreparedStatement stm) {
        String operationPK = aggregatedObject.getOperationPK().getPK();
        String aggregatedObjectPk = aggregatedObject.getPk();

        try {
            stm.setDate(1, timeStamp);
            stm.setLong(2, Long.valueOf(operationPK));
            stm.setLong(3, Long.valueOf(aggregatedObjectPk));
        } catch (SQLException e) {
            log.error("Error.", e);
            throw new TechnicalException("Problems binding variables.", Severity.FATAL, e);
        }
    }

    /**
     *
     * @param aggregatedObject AgregatedObject
     * @return query to use
     */
    private String getApplyVersionQuery(AggregateObject aggregatedObject) {
//        DBTranslator dbtranslator = DBTranslatorFactory.getDBClass();
//        OperationPK idOperation = aggregatedObject.getOperationPK();

        /**
         * En la version 2.0 las actualizaciones de las versiones se hacian tomando en cuenta el
         * TimeStamp, sin embargo conn l cambio de manejo en base a operaciones, estas
         * actualizaciones se realizan en base al ID de la Operacion. Se asume que una operacion
         * solo puede estar generando un cambio en la tabla de versionamiento de cualquier
         * componente.
         */
        StringBuilder queryUpdate = new StringBuilder();

        queryUpdate.append("UPDATE ").append(aggregatedObject.getTableVersion())
                .append(" SET auditDate = ?, time_stamp = ?, STATUS = ? WHERE DCOID = ? AND AGREGATEDOBJECTID = ? AND OPERATIONPK = ? AND STATUS <> ? AND STATUS <> ?");

        return String.valueOf(queryUpdate);
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @return String
     */
    protected abstract String getUpdateTemporaryVersionQuery(AggregateObject aggregatedObject);

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param stm PreparedStatement
     */
    protected abstract void bindUpdateTemporaryVersionQuery(AggregateObject aggregatedObject,
                                                            PreparedStatement stm);

    /**
     * Loads the Object
     * This method is used when loading an AggregateObject by OperationPK
     * @param aggregatedObject AggregateObject
     * @param operationPk OperationPK
     * @throws SQLException error
     */
    public void load(AggregateObject aggregatedObject, OperationPK operationPk)
            throws SQLException {
        load(aggregatedObject, operationPk, null);
    }

    public void loadWithReversed(AggregateObject aggregatedObject, OperationPK operationPk)
            throws SQLException {
        load(aggregatedObject, operationPk, null);
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param opk OperationPK
     * @param conParam Connection
     * @throws SQLException
     * TODO (GS) Corregir
     * Corregir todos los loads para que reciban s�lo un OPK o s�lo una fecha.  No tiene sentido
     * recibir ambos. La carga siempre es la misma.  En el caso particular de AIO, al menos, revisar
     * que se hacer dos o tres loads, lo que a�ade peso a la carga.  Corregir esa situaci�n tambi�n.
     */
    public abstract void load(AggregateObject aggregatedObject, OperationPK opk,
                              Connection conParam) throws SQLException;

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param restoredOperation OperationPK
     * @param setState boolean
     * @param conParam Connection
     * @return boolean
     * @throws Exception error
     */
    public static boolean reverseVersion(AggregateObject aggregatedObject,
                                         OperationPK restoredOperation, boolean setState,
                                         Connection conParam) throws Exception {

        Connection conn = null;
        Statement stmt = null;
        int result = 0;
        String queryReverse = null;

        try {
            queryReverse = AggregatedManager.getReverseVersionQuery(aggregatedObject, setState, restoredOperation);

            if(log.isDebugEnabled()) {
                log.debug("queryReverse = " + queryReverse);
                log.debug("conParam = " + conParam);
            }
            conn = (conParam == null) ? JDBCUtil.openUserDbConnection() : conParam;
            stmt = conn.createStatement();

            if(log.isDebugEnabled())log.debug("Antes del execute!!!");

            if(queryReverse.length() > 0  && !(aggregatedObject instanceof EvaluatedCoverage)) {
                result = stmt.executeUpdate(queryReverse);
            }

            queryReverse = AggregatedManager.getReverseOpkQuery(aggregatedObject);
            stmt.executeUpdate(queryReverse);

            if(log.isDebugEnabled()) log.debug("result = " + result);
            if (result > 0) {
                OperationPK operationToRestore =
                        AggregatedManager.getOperationToRestore(aggregatedObject, conParam);
                if(log.isDebugEnabled())log.debug("operationToRestore = " + operationToRestore);
                if (operationToRestore != null) {
                    aggregatedObject.setOperationPK(operationToRestore);
                    AggregatedManager
                            .applyAggregated(aggregatedObject, operationToRestore.getTimeStamp(),
                                             conn);
                    if(log.isDebugEnabled())log.debug("...TerminoEl Otro!!!");
                }
            }

        } catch (Exception e) {
            if(log.isDebugEnabled())log.error("Query: " + queryReverse, e);
            JDBCUtil.rollBackConnection(conn, e.getMessage());
            throw new TechnicalException(Exceptions.PPErrorInReverseVersion, Severity.FATAL, e);
        } finally {
            JDBCUtil.closeQuietly(null, stmt, conParam == null ? conn : null);
        }
        return result > 0;
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param conParam Connection
     */
    private static void makeLastAppliedOperationNull(AggregateObject aggregatedObject,
                                                     Connection conParam) {
        DBTranslator dbtranslator = DBTranslatorFactory.getDBClass();
        aggregatedObject.setLastAppliedOperationId(0);
        aggregatedObject.setLastAppliedTimeStamp(null);

        Connection conn = null;
        Statement stmt = null;
        StringBuilder queryUpdate = null;
        try {
            conn = JDBCUtil.openUserDbConnectionIfNull(conParam);
            String field;
            field = aggregatedObject instanceof Participation ? "AGREGATEDOBJECTID" :
                    aggregatedObject.getTable() + "Id";

            queryUpdate = new StringBuilder();

            queryUpdate.append("UPDATE ").append(aggregatedObject.getTable())
                    .append(dbtranslator.addRowLock())
                    .append(" SET operationPk = NULL, time_stamp = NULL").append(" WHERE ")
                    .append(field).append(" = ").append(aggregatedObject.getPk());

            //NOTE: the value NULL is used to keep consistence with non applied objects
            if(log.isDebugEnabled())log.debug("[makeLastAppliedOperationNull] - queryUpdate = " + queryUpdate);
            stmt = conn.createStatement();
            stmt.executeUpdate(queryUpdate.toString());

        } catch (Exception e) {
            JDBCUtil.rollBackConnection(conn, e.getMessage());
            log.error("Error making null the applied operation in " +
                      aggregatedObject.getClass().getName() + " Id: " + aggregatedObject.getPk() +
                      " Query: " + queryUpdate, e);
            throw new TechnicalException(Exceptions.PPErrorMakingLastAppliedOperationNull,
                                         Severity.FATAL, e);
        } finally {
            JDBCUtil.closeQuietly(null, stmt, conParam == null ? conn : null);
        }

    }

    /**
     * Finds the last applied operation before the reversed version of the current. This method
     * asumes that the reversed operation has been processed.
     * <code>AggregateObject</code
     * @param aggregatedObject AggregateObject
     * @param connParam Connection to be used for transaccionality purposes
     * @return the <code>OperationPK</code> to be restored
     */
    private static OperationPK getOperationToRestore(AggregateObject aggregatedObject,
                                                     Connection connParam) {

        OperationPK operationPk = null;
        StringBuilder query = new StringBuilder();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {

            String tableVersion = aggregatedObject.getTableVersion();

            query.append("SELECT ctx.time_stamp AS time_stamp, vt0.operationPk AS operationPk FROM ").append(tableVersion)
                    .append(" vt0, ContextOperation ctx WHERE ctx.time_stamp = ( ").append(" SELECT MAX(ctx1.time_stamp) FROM ")
                    .append(tableVersion).append(" vt1, ContextOperation ctx1 WHERE vt1.agregatedObjectId = ")
                    .append(aggregatedObject.getPk()).append(" AND ctx1.status = ")
                    .append(Versionable.STATUS_APPLIED)
                    .append(" AND vt1.operationpk = ctx1.id ) AND vt0.agregatedObjectId = ")
                    .append(aggregatedObject.getPk()).append(" AND ctx.status = ")
                    .append(Versionable.STATUS_APPLIED)
                    .append(" AND vt0.operationpk = ctx.id");

            conn = JDBCUtil.openUserDbConnectionIfNull(connParam);

            stmt = conn.createStatement();
            rs = stmt.executeQuery(query.toString());
            if(log.isDebugEnabled())log.debug("[getOperationToRestore] - query = " + query);
            if (rs.next()) {
                java.sql.Timestamp timestamp = rs.getTimestamp("time_stamp");
                operationPk = new OperationPK(rs.getString("operationPk"), null,
                                              new java.sql.Date(timestamp.getTime()), null);
            } else {
                String errorMessage = "There is not any applied operation for " +
                                      aggregatedObject.getClass().getName() + ". Id: " +
                                      aggregatedObject.getPk();
                log.warn(errorMessage);
            }

        } catch (Exception e) {
            JDBCUtil.rollBackConnection(conn, e.getMessage());
            log.error("Error trying to get the operation to restore for " +
                      aggregatedObject.getClass().getName() + ". Id: " + aggregatedObject.getPk() +
                      ". Query: " + query, e);
            throw new TechnicalException(Exceptions.PPErrorRestoringLastAppliedOperation,
                                         Severity.FATAL, e);
        } finally {
            JDBCUtil.closeQuietly(rs, stmt, connParam == null ? conn : null);
        }

        return operationPk;
    }

    /**
     *
     * @param aggregatedObject AggregateObject
     * @param setState boolean
     * @return String
     */
    private static String getReverseVersionQuery(AggregateObject aggregatedObject,
                                                 boolean setState, OperationPK restoredOperation) {
        DBTranslator dbtranslator = DBTranslatorFactory.getDBClass();
        StringBuilder query = new StringBuilder();

        int newStatus = Versionable.STATUS_REVERSED;
        if(restoredOperation == null){
            newStatus = Versionable.STATUS_TEMPORARY;
        }
        query.append("UPDATE ").append(aggregatedObject.getTableVersion())
                .append(dbtranslator.addRowLock());
        query.append(" SET status = ").append(newStatus);
        if (setState) {
            LifeCycleState state = aggregatedObject.getState();
            String idState = (state == null) ? "-1" : state.getClave();
            query.append(", stateId = ").append(idState);
        }
        query.append(" WHERE agregatedObjectId = ").append(aggregatedObject.getPk());
        query.append(" AND operationPk = ").append(aggregatedObject.getAgregatedPolicy().getOperationPK().getPK());

        return String.valueOf(query);
    }

    private static String getReverseOpkQuery(AggregateObject aggregatedObject) {
        StringBuilder query = new StringBuilder();
            query.append("UPDATE ContextOperation ");
            query.append("SET status = ").append(Versionable.STATUS_REVERSED);
            query.append(" WHERE item = ").append(aggregatedObject.getAgregatedPolicy().getPk());
            query.append(" AND id = ").append(aggregatedObject.getAgregatedPolicy().getOperationPK().getPK());

        return String.valueOf(query);
    }

    /**
     * Gets the first initial date
     *    select initialdate from policydco where agregatedobjectid = 6657 and time_stamp in (
     *     select min(time_stamp) from policydco where status = 2 and agregatedobjectid = 6657 )
     * @param aggregatedObject AggregateObject
     * @return Date containing the applied finish date
     */
    public static Date getFirstInitialDate(AggregateObject aggregatedObject) {

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        StringBuilder query = new StringBuilder(200);
        query.append("select vt0.initialdate AS initialdate from ").append(aggregatedObject.getTableVersion())
                .append(" vt0, ContextOperation ctx0")
                .append(" where vt0.agregatedobjectid = ").append(aggregatedObject.getPk())
                .append(" and ctx0.id = vt0.operationpk")
                .append(" and ctx0.status = ").append(Versionable.STATUS_APPLIED)
                .append(" and ctx0.time_stamp in ( ").append("select min(ctx1.time_stamp) from ")
                .append(aggregatedObject.getTableVersion())
                .append(" vt1, ContextOperation ctx1 where ctx1.status = ")
                .append(Versionable.STATUS_APPLIED)
                .append(" AND vt1.operationpk = ctx1.id and vt1.agregatedobjectid = ")
                .append(aggregatedObject.getPk()).append(" )");

        try {
            conn = JDBCUtil.openUserDbConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query.toString());

            if (rs.next()) {
                return rs.getDate("initialdate");
            } else {
                return aggregatedObject.getInitialDate();
            }
        } catch (SQLException e) {
            log.error("Error getting the first initial date");
            log.error("The policy may not be applied.  Using actual initial date");
            log.error("Query = " + query, e);
            return aggregatedObject.getInitialDate();
        } finally {
            JDBCUtil.closeQuietly(rs, stmt, conn);
        }
    }

    /**
     * Gets the last state
     * @param aggregatedObject AggregateObject
     * @return Date containing the applied finish date
     */
    public static String getLastStateName(AggregateObject aggregatedObject) {

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        StringBuilder query = new StringBuilder(200);
        query.append("select st.description AS description from ").append(aggregatedObject.getTableVersion())
                .append(" vt0, ContextOperation ctx0, state st")
                .append(" where vt0.agregatedobjectid = ").append(aggregatedObject.getPk())
                .append(" and ctx0.id = vt0.operationpk")
                .append(" and st.stateid = vt0.stateid")
                .append(" and ctx0.status = ").append(Versionable.STATUS_APPLIED)
                .append(" and ctx0.time_stamp = ( ").append("select max(ctx1.time_stamp) from ")
                .append(aggregatedObject.getTableVersion())
                .append(" vt1, ContextOperation ctx1 where ctx1.status = ")
                .append(Versionable.STATUS_APPLIED)
                .append(" AND vt1.operationpk = ctx1.id and vt1.agregatedobjectid = ")
                .append(aggregatedObject.getPk()).append(" )");

        try {
            conn = JDBCUtil.openUserDbConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query.toString());

            if (rs.next()) {
                return rs.getString("description");
            } else {
                return aggregatedObject.getState().getDesc();
            }
        } catch (SQLException e) {
            log.error("Error getting the first initial date");
            log.error("The policy may not be applied.  Using actual initial date");
            log.error("Query = " + query, e);
            return aggregatedObject.getState().getDesc();
        } finally {
            JDBCUtil.closeQuietly(rs, stmt, conn);
        }
    }


    /**
     * Gets the applied finish date
     *    select finishdate from policydco where agregatedobjectid = 6657 and time_stamp in (
     *     select max(time_stamp) from policydco where status = 2 and agregatedobjectid = 6657 )
     * @param aggregatedObject AggregateObject
     * @return Date containing the applied finish date
     */
    public static Date getOldFinishDate(AggregateObject aggregatedObject) {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder query = new StringBuilder(200);
        query.append("select vt0.finishdate AS finishdate from ").append(aggregatedObject.getTableVersion())
                .append(" vt0, ContextOperation ctx0 where vt0.agregatedobjectid = ? ")
                .append(" and ctx0.status = ? ")
                .append(" and ctx0.time_stamp = (select max(ctx1.time_stamp) from ")
                .append(aggregatedObject.getTableVersion())
                .append(" vt1, ContextOperation ctx1 where ctx1.status = ? ")
                .append(" AND vt1.operationpk = ctx1.id and vt1.agregatedobjectid = vt0.agregatedobjectid ")
                .append(" ) AND ctx0.id = vt0.operationpk");

        try {
            conn = JDBCUtil.openUserDbConnection();
            stmt = conn.prepareStatement(query.toString());
            long aggregatedObjectPK = Long.valueOf(aggregatedObject.getPk());
            stmt.setLong(1, aggregatedObjectPK);
            stmt.setInt(2, Versionable.STATUS_APPLIED);
            stmt.setInt(3, Versionable.STATUS_APPLIED);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDate("finishdate");
            } else {
                return aggregatedObject.getFinishDate();
            }
        } catch (SQLException e) {
            log.error("Error getting applied finish date");
            log.error("The policy may not be applied.  Using actual finish date");
            log.error("Query = " + query, e);
            return aggregatedObject.getFinishDate();
        } finally {
            JDBCUtil.closeQuietly(rs, stmt, conn);
        }
    }
    /**
     * @param pk
     * @param ocurrenceDate
     * @return OperationPK
     */
    public OperationPK getOperationPkByOccurrenceDateOfClaim(String pk,java.util.Date ocurrenceDate) {
         //todo: por ahora lo obtengo asi por cuestiones de tiempo
        return ContextOperationManager.getInValidity(pk,ocurrenceDate);
    }

    /**
     * Return the states ids given a state description
     * @param description State description
     * @return int[] Ids of states by description
     */
    public static long[] getStatesIdsByDescription(String description){

        if( null == description )
        {
            return null;
        }

        List<Long> listIds = new ArrayList<Long>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        StringBuilder query = new StringBuilder(200);
        query.append("select stateid as id from state where description = '")
                .append(description).append("'");

        try {
            conn = JDBCUtil.openUserDbConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query.toString());

            while (rs.next()) {
                listIds.add(rs.getLong("id"));
            }
            
            int size = listIds.size();
            long [] stateIds = new long[size];
            for (int i = 0; i < listIds.size(); i++) {
                stateIds[i] =  listIds.get(i);
            }
            return  stateIds;
        } catch (SQLException e) {
            log.error("Error getting stateids from State");
            log.error("Query = " + query, e);
            return null;
        } finally {
            JDBCUtil.closeQuietly(rs, stmt, conn);
        }
    }

    /* Implementacion de Filtro de Casos CRM de Interseguro */
    private static void recreateQuery(StringBuilder query, String queryFields, String nroCaso) {
        query.replace(query.indexOf("SELECT * FROM( "), "SELECT * FROM( ".length()-1, "SELECT a.* FROM( ");
        query.append(" GROUP BY ").append(queryFields).append(" order by pdcoctx.auditdate desc ) a")
                .append(" INNER JOIN EXT_CRMCASE b on a.agregatedobjectid = b.policyId AND b.crmNumber = ")
                .append("'").append(nroCaso).append("'");
    }
}
