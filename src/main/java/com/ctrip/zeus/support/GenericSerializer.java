package com.ctrip.zeus.support;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/4/3.
 */
public class GenericSerializer {
    private static Logger logger = LoggerFactory.getLogger(GenericSerializer.class);

    public static final String JSON = "%#.3s";
    public static final String JSON_COMPACT = "%#s";
    public static final String XML = "%.3s";
    public static final String XML_COMPACT = "%s";

    private static Map<String, Class> Builders = new HashMap<>();
    private static Map<Class, Method> Methods = new HashMap<>();
    private static LoadingCache<Class, Constructor<?>> Ctors = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class, Constructor<?>>() {
                @Override
                public Constructor<?> load(Class aClass) throws Exception {
                    return aClass.getConstructor(boolean.class);
                }
            });

    public static String writeJson(Object object) {
        return writeJson(object, true);
    }

    public static String inject(String jsonValue, String key, Object object) {
        StringBuilder sb = new StringBuilder(jsonValue);
        sb.setLength(jsonValue.lastIndexOf('}'));
        sb.append(',');
        sb.append("\"" + key + "\":");
        sb.append(writeJson(object));
        sb.append('}');
        return sb.toString();
    }

    public static String writeJson(Object object, boolean pretty) {
        String pkg = object.getClass().getPackage().getName();
        pkg = pkg.substring(0, pkg.lastIndexOf(".entity"));
        Class clazz = Builders.get(pkg + "#Json");
        if (clazz == null) {
            try {
                clazz = Class.forName(pkg + ".transform.DefaultJsonBuilder");
                Builders.put(pkg + "#Json", clazz);
            } catch (ClassNotFoundException e) {
                logger.error("Cannot find " + pkg + ".transform.DefaultJsonBuilder", e);
                return "";
            }
        }
        try {
            Object builder = Ctors.get(clazz).newInstance(!pretty);
            Method m = Methods.get(clazz);
            if (m == null) {
                try {
                    m = clazz.getMethod("build", Class.forName(pkg + ".IEntity"));
                    Methods.put(clazz, m);
                } catch (Exception e) {
                    logger.error("Cannot find method build(IEntity)", e);
                }
            }
            return (String) m.invoke(builder, object);
        } catch (Exception e) {
            logger.error("Fail to build json data.", e);
            return "";
        }
    }

    public static String writeXml(Object object) {
        return writeXml(object, true);
    }

    public static String writeXml(Object object, boolean pretty) {
        String pkg = object.getClass().getPackage().getName();
        pkg = pkg.substring(0, pkg.lastIndexOf(".entity"));
        Class clazz = Builders.get(pkg + "#Xml");
        if (clazz == null) {
            try {
                clazz = Class.forName(pkg + ".transform.DefaultXmlBuilder");
                Builders.put(pkg + "#Xml", clazz);
            } catch (ClassNotFoundException e) {
                logger.error("Cannot find " + pkg + ".transform.DefaultXmlBuilder", e);
                return "";
            }
        }
        try {
            Object builder = Ctors.get(clazz).newInstance(!pretty);
            Method m = Methods.get(clazz);
            if (m == null) {
                try {
                    m = clazz.getMethod("buildXml", Class.forName(pkg + ".IEntity"));
                    Methods.put(clazz, m);
                } catch (Exception e) {
                    logger.error("Cannot find method buildXml(IEntity)", e);
                }
            }
            return (String) m.invoke(builder, object);
        } catch (Exception e) {
            logger.error("Fail to build xml data.", e);
            return "";
        }
    }
}
