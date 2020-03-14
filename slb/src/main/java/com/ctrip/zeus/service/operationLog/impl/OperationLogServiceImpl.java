package com.ctrip.zeus.service.operationLog.impl;

import com.ctrip.zeus.dao.entity.LogOperationLog;
import com.ctrip.zeus.dao.entity.LogOperationLogExample;
import com.ctrip.zeus.dao.mapper.LogOperationLogMapper;
import com.ctrip.zeus.model.operationlog.OperationLogData;
import com.ctrip.zeus.model.operationlog.OperationLogDataList;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.operationLog.OperationLogService;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by fanqq on 2015/7/20.
 */
@Component("operationLogService")
public class OperationLogServiceImpl implements OperationLogService {
    @Resource
    private LogOperationLogMapper logOperationLogMapper;
    @Resource
    public ConfigHandler configHandler;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void insert(String type, String targetId, String operation, String data,
                       String user, String clientIp, boolean success, String errMsg, Date dateTime) {
        insertMybatis(type, targetId, operation, data, user, clientIp, success, errMsg, dateTime);
    }

    private void insertMybatis(String type, String targetId, String operation, String data, String user, String clientIp, boolean success, String errMsg, Date dateTime) {
        LogOperationLog.Builder builder = LogOperationLog.builder();
        if (type != null) {
            builder.type(type);
        }
        if (targetId != null) {
            builder.targetId(targetId);
        }
        if (operation != null) {
            builder.operation(operation);
        }
        if (data != null) {
            builder.data(data);
        }
        if (user != null) {
            builder.userName(user);
        }
        if (clientIp != null) {
            builder.clientIp(clientIp);
        }
        if (errMsg != null) {
            builder.errMsg(errMsg);
        }
        if (dateTime != null) {
            builder.datetime(dateTime);
        }
        builder.success(success);
        logOperationLogMapper.insert(builder.build());
    }


    @Override
    public OperationLogDataList find(String type, String targetId, String operation,
                                     String user, String clientIp, Boolean success, Date fromDateTime, Date toDateTime, Long count) throws Exception {
        return findMybatis(type, targetId, operation, user, clientIp, success, fromDateTime, toDateTime, count);
    }

    private OperationLogDataList findMybatis(String type, String targetId, String operation,
                                             String user, String clientIp, Boolean success, Date fromDateTime, Date toDateTime, Long count) throws Exception {
        LogOperationLogExample.Criteria c = new LogOperationLogExample().createCriteria();
        int limitCount = count == null ? 1000 : count.intValue();

        if (type != null) {
            c.andTypeEqualTo(type);
        }
        if (targetId != null) {
            c.andTargetIdEqualTo(targetId);
        }
        if (operation != null) {
            c.andOperationEqualTo(operation);
        }
        if (user != null) {
            c.andUserNameEqualTo(user);
        }
        if (clientIp != null) {
            c.andClientIpEqualTo(clientIp);
        }
        if (fromDateTime != null) {
            c.andDatetimeGreaterThan(fromDateTime);
        }
        if (toDateTime != null) {
            c.andDatetimeLessThan(toDateTime);
        }
        if (success != null) {
            c.andSuccessEqualTo(success);
        }

        OperationLogDataList result = new OperationLogDataList();
        List<LogOperationLog> res = logOperationLogMapper.selectByExample(c.example().orderBy("datetime desc").limit(limitCount));
        if (res == null || res.isEmpty()) return result;
        OperationLogData tmp = null;
        for (LogOperationLog logDo : res) {
            tmp = new OperationLogData();
            tmp.setClientIp(logDo.getClientIp())
                    .setErrMsg(logDo.getErrMsg())
                    .setData(logDo.getData())
                    .setDateTime(logDo.getDatetime())
                    .setOperation(logDo.getOperation())
                    .setTargetId(logDo.getTargetId())
                    .setType(logDo.getType())
                    .setSuccess(logDo.getSuccess())
                    .setUserName(logDo.getUserName());
            result.addOperationLogData(tmp);
        }
        return result;
    }


    @Override
    public OperationLogDataList find(String operation, Date fromDateTime, Date toDateTime, Long count) throws Exception {
        return find(null, null, operation, null, null, null, fromDateTime, toDateTime, count);
    }

    @Override
    public OperationLogDataList find(String type, String[] targetIds, String[] operation, Boolean success, Date fromDateTime, Date toDateTime, Long count) throws Exception {
        return findMybatis(type, targetIds, operation, success, fromDateTime, toDateTime, count);
    }

    private OperationLogDataList findMybatis(String type, String[] targetIds, String[] operation, Boolean success, Date fromDateTime, Date toDateTime, Long count) {
        LogOperationLogExample.Criteria c = new LogOperationLogExample().createCriteria();
        int limitCount = count == null ? 1000 : count.intValue();

        if (type != null) {
            c.andTypeEqualTo(type);
        }
        if (targetIds != null) {
            c.andTargetIdIn(Arrays.asList(targetIds));
        }
        if (operation != null) {
            c.andOperationIn(Arrays.asList(operation));
        }
        if (fromDateTime != null) {
            c.andDatetimeGreaterThan(fromDateTime);
        }
        if (toDateTime != null) {
            c.andDatetimeLessThan(toDateTime);
        }

        if (success != null) {
            c.andSuccessEqualTo(success);
        }

        OperationLogDataList result = new OperationLogDataList();

        List<LogOperationLog> res = logOperationLogMapper.selectByExample(c.example().orderBy("datetime desc").limit(limitCount));
        if (res == null || res.isEmpty()) return result;
        OperationLogData tmp = null;
        for (LogOperationLog logDo : res) {
            tmp = new OperationLogData();
            tmp.setClientIp(logDo.getClientIp())
                    .setErrMsg(logDo.getErrMsg())
                    .setData(logDo.getData())
                    .setDateTime(logDo.getDatetime())
                    .setOperation(logDo.getOperation())
                    .setTargetId(logDo.getTargetId())
                    .setType(logDo.getType())
                    .setSuccess(logDo.getSuccess())
                    .setUserName(logDo.getUserName());
            result.addOperationLogData(tmp);
        }
        return result;
    }


    @Override
    public OperationLogDataList find(String type, String[] targetIds, String[] operation, Boolean success, Long count) throws Exception {
        return find(type, targetIds, operation, success, null, null, count);
    }


    @Override
    public long count(String operation, String type, Date fromDateTime, Date toDateTime) throws Exception {
        return count(new String[]{operation}, type, fromDateTime, toDateTime);
    }


    @Override
    public long count(String[] operations, String type, Date fromDateTime, Date toDateTime) throws Exception {
        return countMybatis(operations, type, fromDateTime, toDateTime, null);
    }


    private long countMybatis(String[] operations, String type, Date fromDateTime, Date toDateTime, Boolean success) {
        LogOperationLogExample.Criteria c = new LogOperationLogExample().createCriteria();
        if (success != null) {
            c.andSuccessEqualTo(success);
        }
        if (type != null) {
            c.andTypeEqualTo(type);
        }
        if (operations != null) {
            c.andOperationIn(Arrays.asList(operations));
        }
        if (fromDateTime != null) {
            c.andDatetimeGreaterThan(fromDateTime);
        }
        if (toDateTime != null) {
            c.andDatetimeLessThan(toDateTime);
        }
        return logOperationLogMapper.countByExample(c.example());
    }

    @Override
    public long count(String[] operations, String type, boolean success, Date fromDateTime, Date toDateTime) throws Exception {
        return countMybatis(operations, type, fromDateTime, toDateTime, success);
    }

    @Override
    public Map<String, Long> count(String[] operations, String type, String[] targetIds, boolean success, Date fromDateTime, Date toDateTime) throws Exception {
        return countMybatis(operations, type, targetIds, success, fromDateTime, toDateTime);
    }

    private Map<String, Long> countMybatis(String[] operations, String type, String[] targetIds, boolean success, Date fromDateTime, Date toDateTime) {

        LogOperationLogExample.Criteria c = new LogOperationLogExample().createCriteria();
        c.andSuccessEqualTo(success);
        if (type != null) {
            c.andTypeEqualTo(type);
        }
        if (targetIds != null) {
            c.andTargetIdIn(Arrays.asList(targetIds));
        }
        if (operations != null) {
            c.andOperationIn(Arrays.asList(operations));
        }
        if (fromDateTime != null) {
            c.andDatetimeGreaterThan(fromDateTime);
        }
        if (toDateTime != null) {
            c.andDatetimeLessThan(toDateTime);
        }
        Map<String, Long> result = new HashMap<>();
        List<Map<String, Object>> logOperationLogs = null;
        logOperationLogs = logOperationLogMapper.countByExampleGroupByTargetId(c.example());

        Set<String> ids = null;
        if (targetIds != null) {
            ids = new HashSet<>();
            Collections.addAll(ids, targetIds);
        }
        for (Map<String, Object> logDo : logOperationLogs) {
            if (ids != null && !ids.contains(logDo.get("targetId").toString())) {
                continue;
            }
            String key = logDo.get("targetId") + "_" + logDo.get("operation");
            Long count = 0L;
            if (logDo.get("count") != null && logDo.get("count") instanceof Long) {
                count = (Long) logDo.get("count");
            }
            result.put(key, count);
        }
        return result;
    }

    @VisibleForTesting
    public OperationLogService setConfigHandler(ConfigHandler configHandler) {
        this.configHandler = configHandler;
        return this;
    }
}
