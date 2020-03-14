package com.ctrip.zeus.restful.message;

import java.io.Serializable;

/**
 * Created by zhoumy on 2015/4/1.
 */
public interface Message {

    /**
     * Get http response status code
     * reference: http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Response.Status.html
     * @return
     */
    int getStatus();

    /**
     * Get serialized object
     * @return
     */
    Serializable getResponse();
}
