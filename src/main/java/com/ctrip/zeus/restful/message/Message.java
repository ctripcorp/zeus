package com.ctrip.zeus.restful.message;

import java.io.Serializable;

/**
 * Created by zhoumy on 2015/4/1.
 */
public interface Message {

    int getStatus();

    Serializable getResponse();
}
