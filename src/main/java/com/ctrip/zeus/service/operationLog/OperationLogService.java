package com.ctrip.zeus.service.operationLog;

import com.ctrip.zeus.operationlog.entity.OperationLogDataList;
import java.util.Date;

/**
 * Created by fanqq on 2015/7/20.
 */
public interface OperationLogService {
    public void insert(String type,String targetId,String operation , String data,
                       String user , String clientIp,boolean success,String errMsg , Date dateTime );
    public OperationLogDataList find(String type,String targetId,String operation ,
                       String user , String clientIp,Boolean success, Date fromDateTime ,Date toDateTime , Long count) throws Exception;
}
