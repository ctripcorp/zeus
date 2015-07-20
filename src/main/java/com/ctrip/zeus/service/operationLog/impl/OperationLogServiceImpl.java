package com.ctrip.zeus.service.operationLog.impl;

import com.ctrip.zeus.dal.core.OperationLogDao;
import com.ctrip.zeus.dal.core.OperationLogDo;
import com.ctrip.zeus.dal.core.OperationLogEntity;
import com.ctrip.zeus.operationlog.entity.OperationLogData;
import com.ctrip.zeus.operationlog.entity.OperationLogDataList;
import com.ctrip.zeus.service.operationLog.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by fanqq on 2015/7/20.
 */
@Component("operationLogService")
public class OperationLogServiceImpl implements OperationLogService {
    @Resource
    private OperationLogDao operationLogDao;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void insert(String type,String targetId,String operation , String data,
                       String user , String clientIp,boolean success,String errMsg , Date dateTime) {
        OperationLogDo operationLogDo = new OperationLogDo();
        if (type!=null){
            operationLogDo.setType(type);
        }
        if (targetId !=null){
            operationLogDo.setTargetId(targetId);
        }
        if (operation !=null){
            operationLogDo.setOperation(operation);
        }
        if (data !=null){
            operationLogDo.setData(data);
        }
        if (user !=null){
            operationLogDo.setUserName(user);
        }
        if (clientIp !=null){
            operationLogDo.setClientIp(clientIp);
        }
        if (errMsg !=null){
            operationLogDo.setErrMsg(errMsg);
        }
        if (dateTime !=null){
            operationLogDo.setDatetime(dateTime);
        }
        operationLogDo.setSuccess(success);
        try {
            operationLogDao.insert(operationLogDo);
        } catch (DalException e) {
            logger.warn("OperationLog DalException! "+e.getMessage());
        }
    }

    @Override
    public OperationLogDataList find(String type,String targetId,String operation ,
                                     String user , String clientIp,Boolean success, Date fromDateTime ,Date toDateTime , Long count) throws Exception {
        List<OperationLogDo> res = null;
        Long limitCount = count == null?1000L:count;
        if (success==null){
            res = operationLogDao.findByOptions(type,targetId,operation,user,clientIp,fromDateTime,toDateTime,limitCount, OperationLogEntity.READSET_FULL);
        }else {
            res = operationLogDao.findByOptionsWithSuccess(type,targetId,operation,user,clientIp,success,fromDateTime,toDateTime,limitCount, OperationLogEntity.READSET_FULL);
        }
        OperationLogDataList result = new OperationLogDataList();
        if (res == null)return result;
        OperationLogData tmp = null;
        for (OperationLogDo logDo : res){
            tmp = new OperationLogData();
            tmp.setClientIp(logDo.getClientIp())
                    .setErrMsg(logDo.getErrMsg())
                    .setData(logDo.getData())
                    .setDateTime(logDo.getDatetime())
                    .setOperation(logDo.getOperation())
                    .setTargetId(logDo.getTargetId())
                    .setType(logDo.getType())
                    .setSuccess(logDo.isSuccess())
                    .setUserName(logDo.getUserName());
            result.addOperationLogData(tmp);
        }
        return result;
    }
}
