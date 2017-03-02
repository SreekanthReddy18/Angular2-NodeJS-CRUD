package com.zoegeop.producto;

import com.zoegeop.producto.util.AcseleConf;
import com.zoegeop.producto.util.StringUtil;

/**
 * Created by ext.dpalma on 04/02/2017.
 */
public class ClientInfo {
    static final String clientsRunningName = AcseleConf.getProperty("clientsRunning");

    public static boolean isClientRunning(String clientName){
        return !StringUtil.isEmpty(clientsRunningName) && clientsRunningName.equals(clientName);
    }
    public static String getClientRunning(String clientName){
        return clientsRunningName;
    }
}
