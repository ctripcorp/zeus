package com.ctrip.zeus.service.status;

import com.ctrip.zeus.model.entity.AppServerStatus;
import com.ctrip.zeus.model.entity.AppStatus;

import java.util.List;

/**
 * User: mag
 * Date: 4/1/2015
 * Time: 10:54 AM
 */
public interface AppStatusService {
     /**
      * Find all app status
      * @return
      * @throws Exception
      */
     List<AppStatus> getAllAppStatus() throws Exception;

     /**
      * Find all app status in the specific slb cluster
      * @param slbName
      * @return
      * @throws Exception
      */
     List<AppStatus> getAllAppStatus(String slbName) throws Exception;

     List<AppStatus> getAppStatus(String appName) throws Exception;

     AppStatus getAppStatus(String appName,String slbName) throws Exception;

     AppServerStatus getAppServerStatus(String appName, String slbName, String ip, Integer port) throws Exception;
}
