package com.ctrip.zeus.build;

import org.unidal.dal.jdbc.AbstractDao;
import org.unidal.dal.jdbc.transaction.TransactionManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
public class GenerateDaoBeansConfig {

    public static void main(String[] args) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "       xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.1.xsd\">\n\n");
        builder.append("    <bean id=\"daoFactory\" class=\"com.ctrip.zeus.support.DaoFactory\"/>\n\n");

        Class c= TransactionManager.class;
        builder.append(String.format("" +
                        "    <bean id=\"%s\" factory-bean=\"daoFactory\" factory-method=\"getDao\">\n" +
                        "        <constructor-arg type=\"java.lang.Class\" value=\"%s\"/>\n" +
                        "    </bean>\n\n",
                uncapitalize(c.getSimpleName()), c.getName()));

        String daoPackage="com.ctrip.zeus.dal.core";
        List<Class<?>> list = ClassFinder.find(daoPackage);
        for (Class<?> clazz : list) {
            if(AbstractDao.class.isAssignableFrom(clazz)){
                builder.append(String.format("" +
                                "    <bean id=\"%s\" factory-bean=\"daoFactory\" factory-method=\"getDao\">\n" +
                                "        <constructor-arg type=\"java.lang.Class\" value=\"%s\"/>\n" +
                                "    </bean>\n\n",
                        uncapitalize(clazz.getSimpleName()), clazz.getName()));
                System.out.println(String.format("@Resource\nprivate %s %s;",clazz.getSimpleName(),uncapitalize(clazz.getSimpleName())));
            }
        }

        builder.append("</beans>");

        FileWriter writer = new FileWriter("src/main/resources/dao-beans.xml");
        writer.write(builder.toString());
        writer.close();
    }

    static String uncapitalize(String str) {
        String first = new String(str.substring(0,1));
        return str.replaceFirst(first,first.toLowerCase());
    }
}
