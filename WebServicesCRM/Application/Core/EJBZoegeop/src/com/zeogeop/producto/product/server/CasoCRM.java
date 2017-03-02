package com.consisint.acsele.product.server;


import com.consisint.acsele.UserInfo;
import com.consisint.acsele.template.server.ConfigurableObjectType;
import com.consisint.acsele.util.JDBCUtil;
import com.consisint.acsele.util.logging.AcseleLogger;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * The product wrapper collecion class.
 * Title: CasoCRM.java <br>
 * This class represent a assembly of CasoCRM
 * Copyright: (c) 2017 Consis International<br>
 * Company: Consis International<br>
 * @author Consis International (CON)
 * @author Darwin Palma (DP)
 * <br>
 * Changes:<br>
 * <ul>
 *
 * </ul>
 */

public class CasoCRM implements Serializable {

    private static final AcseleLogger log = AcseleLogger.getLogger(CasoCRM.class);
    private final static Lock lock = new ReentrantLock();

    private static Map<String, CasoCRM> allProducts = new ConcurrentHashMap<String, CasoCRM>();

    //    private ProxyVector productos;
    private boolean isDirty = true;
    private Map<String, String> mapping;
    protected ConfigurableObjectType objectType;

    private static final long serialVersionUID = -6311518460774194702L;

    public static CasoCRM getInstance() {
        return checkAndReturnProducts(UserInfo.getCountry());
    }


    private static CasoCRM checkAndReturnProducts(String country_code) {
        CasoCRM productosLocal = null;
        lock.lock();
        try {
            productosLocal = CasoCRM.allProducts.get(country_code);
            if ((productosLocal == null) || (productosLocal.isDirty)) {
                log.info("Productos dirty or not iniciality for country " + country_code);
                productosLocal = new CasoCRM();
                CasoCRM.allProducts.put(country_code, productosLocal);
                productosLocal.isDirty = false;
            }
        } finally {
            lock.unlock();
        }
        return productosLocal;
    }
    /**
     * Return a boolean indicating if the CasoCRM contains the key
     * @param key key
     * @return boolean indicating if the CasoCRM contains the key
     */
    public boolean containsKey(String key) {
        //  The objects of mapping are description of products
        return mapping.containsValue(key);
    }

    /**
     * Return a boolean indicating if the CasoCRM contains the object
     * @param object object
     * @return boolean indicating if the CasoCRM contains the object
     */
    public boolean contains(Object object) {
        return false;
    }

    /**
     *
     * @return Hashtable containing a pairs CRMCASE,CRMCASE from all CasoCRM
     */
    public Hashtable<String, String> getAllCRMCase()
    {
        log.info( " *** METHOD NAME  " + "getAllCRMCase" );
        //--------------------------- ACSELE - 5588 --------------------------------------
        String query = "SELECT DISTINCT ( CRMNUMBER ) AS CRMCASE FROM EXT_CRMCASE ORDER BY CRMNUMBER";
        Connection conn = JDBCUtil.openUserDbConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        Hashtable<String, String> crmNumberCaseList = new Hashtable<String, String>();
        //search of all CRMCASE

        try
        {
            ArrayList<String> values = new ArrayList<String>();
            prepStmt = conn.prepareStatement( query );
            rs       = prepStmt.executeQuery();
            if( null != rs && rs.isBeforeFirst() )
            {
                while( rs.next() )
                {
                    crmNumberCaseList.put( ( rs.getString( "CRMCASE" ) ),  rs.getString( "CRMCASE" ) );
                }
    
                rs.close();
            }
        }
        catch( SQLException E )
        {
            log.error( E.getMessage() );
        }
        return crmNumberCaseList;
    }

}
