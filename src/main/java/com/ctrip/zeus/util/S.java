package com.ctrip.zeus.util;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class S {

    public static void setPropertyDefaultValue(String propertyName, String defaultValue) {
        String val = System.getProperty(propertyName);
        if(val==null || val.trim().isEmpty()){
            System.setProperty(propertyName, defaultValue);
        }
    }

    final static public String getIp() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> er = NetworkInterface.getNetworkInterfaces();
            while (er.hasMoreElements()) {
                NetworkInterface ni = er.nextElement();
                if (ni.getName().startsWith("eth") || ni.getName().startsWith("bond") ) {
                    List<InterfaceAddress> list = ni.getInterfaceAddresses();
                    for (InterfaceAddress interfaceAddress : list) {
                        InetAddress address = interfaceAddress.getAddress();
                        if (address instanceof Inet4Address) {
                            ip = address.getHostAddress();
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (ip == null) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return ip;
    }
}
