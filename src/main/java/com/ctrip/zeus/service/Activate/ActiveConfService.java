package com.ctrip.zeus.service.Activate;

import java.util.List;

/**
 * Created by fanqq on 2015/3/30.
 */
public interface ActiveConfService {

    /**
     * get config content of App Active by app name
     * @param appnames the app names
     * @return content list
     * @throws Exception
     */
    public List<String> getConfAppActiveContentByAppNames(String []appnames)throws Exception;
    /**
     * get config content of Slb Active by slb name
     * @param slbname the slb name
     * @return content string
     * @throws Exception
     */
    public String getConfSlbActiveContentBySlbNames(String slbname)throws Exception;
}
