package com.ctrip.zeus.auth;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * User: mag
 * Date: 4/22/2015
 * Time: 2:47 PM
 */
public interface ResourceGroupProvider {
    /**
     * Provide the resource group name what the request belongs to.
     * @param
     * @param request
     * @return
     */
    String provideResourceGroup(Method method,HttpServletRequest request);
}
