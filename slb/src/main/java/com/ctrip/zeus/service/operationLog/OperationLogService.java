package com.ctrip.zeus.service.operationLog;

import com.ctrip.zeus.model.operationlog.OperationLogDataList;

import java.util.Date;
import java.util.Map;

/**
 * Created by fanqq on 2015/7/20.
 */
public interface OperationLogService {
    void insert(String type, String targetId, String operation, String data,
                String user, String clientIp, boolean success, String errMsg, Date dateTime);

    OperationLogDataList find(String type, String targetId, String operation,
                              String user, String clientIp, Boolean success, Date fromDateTime, Date toDateTime, Long count) throws Exception;

    OperationLogDataList find(String operation, Date fromDateTime, Date toDateTime, Long count) throws Exception;

    OperationLogDataList find(String type, String[] targetIds, String[] operation, Boolean success, Date fromDateTime, Date toDateTime, Long count) throws Exception;

    OperationLogDataList find(String type, String[] targetIds, String[] operation, Boolean success, Long count) throws Exception;


    long count(String operation, String type, Date fromDateTime, Date toDateTime) throws Exception;

    long count(String[] operations, String type, Date fromDateTime, Date toDateTime) throws Exception;

    long count(String[] operations, String type, boolean success, Date fromDateTime, Date toDateTime) throws Exception;

    Map<String,Long> count(String[] operations, String type, String[] targetIds, boolean success, Date fromDateTime, Date toDateTime) throws Exception;
}
