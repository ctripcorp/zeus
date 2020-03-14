package com.ctrip.zeus.util;

import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fanqq on 2015/4/7.
 */
public class AssertUtils {

    private static Logger logger = LoggerFactory.getLogger(AssertUtils.class);

    public static boolean assertNotNull(Object obj, String exceptionMsg) throws Exception {
        if (obj == null) {
            Exception e = new NotFoundException(exceptionMsg);
            logger.warn("Assert Utils assertNotNull: ", e);
            throw e;
        }
        return true;
    }

    public static <T> T assertNotEquals(T except, T target, String exceptionMsg) throws Exception {
        if (target == null && except == null) {
            return null;
        } else if ((except != null && except.equals(target)) || (target != null && target.equals(except))) {
            Exception e = new ValidationException(exceptionMsg);
            logger.warn("Assert Utils assertNotEquals: ", e);
            throw e;
        }
        return target;
    }
}
