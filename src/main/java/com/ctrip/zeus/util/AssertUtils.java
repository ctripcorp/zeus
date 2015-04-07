package com.ctrip.zeus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fanqq on 2015/4/7.
 */
public class AssertUtils {

    private static Logger logger= LoggerFactory.getLogger(AssertUtils.class);

    public static boolean isNull(Object obj,String exceptionMsg)throws Exception
    {
        if (obj==null){
            Exception e = new Exception(exceptionMsg);
            logger.warn("Assert Utils isNull: ",e);
            throw e;
        }
        return true;
    }

    public static <T> T arrertEquels(T except,T target,String exceptionMsg)throws Exception{
        if (target!=except)
        {
            Exception e = new Exception(exceptionMsg);
            logger.warn("Assert Utils arrertEquels: ",e);
            throw e;
        }
        return target;
    }
    public static <T> T arrertNotEquels(T except,T target,String exceptionMsg)throws Exception{
        if (target==except)
        {
            Exception e = new Exception(exceptionMsg);
            logger.warn("Assert Utils arrertNotEquels: ",e);
            throw e;
        }
        return target;
    }
}
