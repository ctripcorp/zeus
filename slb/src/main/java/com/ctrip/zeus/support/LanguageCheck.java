package com.ctrip.zeus.support;

/**
 * Created by fanqq on 2017/3/24.
 */
public class LanguageCheck {
    public static String getLanguage(String container) {
        if (container == null) return "未知";
        switch (container.toLowerCase()) {
            case "windows_web_iis":
            case "windows_clr":
                return ".net";
            case "linux_apache_php":
            case "linux_nginx_php":
                return "php";
            case "linux_tomcat":
            case "linux_java":
                return "java";
            case "linux_python":
            case "linux_apache_python":
                return "python";
            case "linux_nginx_nodejs":
                return "nodejs";
            case "linux_memcached":
                return "memcached";
            case "linux_redis":
                return "redis";
            case "windows_mssql":
            case "linux_mysql":
                return "mysql";
            case "linux_webresource":
                return "webresource";
            case "linux_nginx_go":
                return "go";
            case "linux_slb_java":
                return "slb.java";
            case "windows_common":
            case "linux_common":
                return "未知";
            default:
                return "未知";
        }
    }
}
