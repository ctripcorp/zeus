package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.exceptions.ValidationException;

/**
 * Created by zhoumy on 2016/7/25.
 */
public class ViewConstraints {
    public static final String INFO = "INFO";
    public static final String NORMAL = "NORMAL";
    public static final String DETAIL = "DETAIL";
    public static final String EXTENDED = "EXTENDED";

    static public Class<?> getContraintType(String type) throws ValidationException {
        if (type == null || type.isEmpty()) return Detail.class;

        switch (type.toUpperCase()) {
            case INFO:
                return Info.class;
            case NORMAL:
                return Normal.class;
            case DETAIL:
                return Detail.class;
            case EXTENDED:
                return Extended.class;
            default:
                return Detail.class;
        }
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
