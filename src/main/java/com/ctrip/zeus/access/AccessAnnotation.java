package com.ctrip.zeus.access;

import java.lang.annotation.*;

/**
 * Created by fanqq on 2015/7/15.
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessAnnotation {

}
