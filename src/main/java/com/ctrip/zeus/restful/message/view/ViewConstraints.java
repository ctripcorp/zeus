package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.exceptions.ValidationException;

/**
 * Created by zhoumy on 2016/7/25.
 */
public class ViewConstraints {
    static public Class<?> getContraintType(String type) throws ValidationException {
        switch (type.toUpperCase()) {
            case "INFO":
                return Info.class;
            case "NORMAL":
                return Normal.class;
            case "DETAIL":
                return Detail.class;
            case "EXTENDED":
                return Extended.class;
        }
        throw new ValidationException("Unknown view type - " + type + ".");
    }

    static public class Info {
    }

    static public class Normal extends Info {
    }

    static public class Detail extends Normal {
    }

    static public class Extended extends Detail {
    }
}
