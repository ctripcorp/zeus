package com.ctrip.zeus.auth;

import com.ctrip.zeus.auth.impl.UriResGroupProvider;

import java.lang.annotation.*;

/**
 * User: mag
 * Date: 4/22/2015
 * Time: 2:36 PM
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorize {
    String name();
    Class<? extends ResourceGroupProvider> groupProvider() default UriResGroupProvider.class;

    /**
     * indicate the resource group is in which part of the uri.
     * for example, when uri is: /api/app/get/app1 and the group hint is set to 3,
     * then the resource group will be "app1". By default the value is 0, which means
     * the group will be parsed from the path annotation from the Resource api,
     * for example, if the Path is set to "/get/{appName}", then the {appName} part in
     * the uri will be used as resource group. When it is set to -1, it means no group.
     * @return
     */
    int uriGroupHint() default 0;
}
