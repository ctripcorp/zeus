package com.ctrip.zeus.util;

import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
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
            Exception e = new NotFoundException(exceptionMsg);
            logger.warn("Assert Utils isNull: ",e);
            throw e;
        }
        return true;
    }

    public static <T> T assertEquels(T except,T target,String exceptionMsg)throws Exception{
        if (target != except || ( target!=null && !target.equals(except)))
        {
            Exception e = new ValidationException(exceptionMsg);
            logger.warn("Assert Utils arrertEquels: ",e);
            throw e;
        }
        return target;
    }
    public static <T> T assertNotEquels(T except,T target,String exceptionMsg)throws Exception{
        if (target==except || (target!=null && target.equals(except)))
        {
            Exception e = new ValidationException(exceptionMsg);
            logger.warn("Assert Utils assertNotEquels: ",e);
            throw e;
        }
        return target;
    }
}
