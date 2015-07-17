package com.ctrip.zeus.service.aop.OperationLog;

import com.ctrip.zeus.access.AccessType;

import java.util.HashMap;

/**
 * Created by fanqq on 2015/7/16.
 */
public class OperationLogConfig {

    private static HashMap<String,OpConf> config = new HashMap<>();
    private static OperationLogConfig logConfig = new OperationLogConfig();

    private OperationLogConfig() {
       loadConfig();
    }

    private void loadConfig(){
        config.put("ActivateResource.activateSlb",new OpConf(AccessType.SLB,new int[]{2,3}));
        config.put("ActivateResource.activateGroup",new OpConf(AccessType.GROUP,new int[]{2,3}));

        config.put("DeactivateResource.deactivateGroup",new OpConf(AccessType.GROUP,new int[]{2,3}));

        config.put("GroupResource.list",new OpConf(AccessType.GROUP,null,true));
        config.put("GroupResource.get",new OpConf(AccessType.GROUP,new int[]{2,3}));
        config.put("GroupResource.add",new OpConf(AccessType.GROUP,new int[]{-1,2}));
        config.put("GroupResource.update",new OpConf(AccessType.GROUP,new int[]{2}));
        config.put("GroupResource.delete",new OpConf(AccessType.GROUP,new int[]{2}));

        config.put("SlbResource.list",new OpConf(AccessType.SLB,null,true));
        config.put("SlbResource.get",new OpConf(AccessType.SLB,new int[]{2,3}));
        config.put("SlbResource.add",new OpConf(AccessType.SLB,new int[]{-1,2}));
        config.put("SlbResource.update",new OpConf(AccessType.SLB,new int[]{2}));
        config.put("SlbResource.delete",new OpConf(AccessType.SLB,new int[]{2}));

        config.put("ServerResource.upServer",new OpConf(AccessType.SERVER,new int[]{2}));
        config.put("ServerResource.downServer",new OpConf(AccessType.SERVER,new int[]{2}));
        config.put("ServerResource.upMember",new OpConf(AccessType.SERVER,new int[]{2,3}));
        config.put("ServerResource.downMember",new OpConf(AccessType.SERVER,new int[]{2,3}));

        config.put("StatusResource.allGroupStatusInSlb",new OpConf(AccessType.SLB,new int[]{2,3}));
        config.put("StatusResource.groupStatus",new OpConf(AccessType.GROUP,new int[]{2,3}));
    }

    public static OperationLogConfig getInstance(){return logConfig;}
    public AccessType getType(String key){
        OpConf tmp = config.get(key);
        if (tmp != null){
            return tmp.getType();
        }
        return null;
    }
    public int[] getIds(String key){
        OpConf tmp = config.get(key);
        if (tmp != null){
            return tmp.getId();
        }
        return null;
    }
    public boolean getBatch(String key){
        OpConf tmp = config.get(key);
        if (tmp != null){
            return tmp.getBatch();
        }
        return false;
    }

    class OpConf{
        private AccessType type;
        private int[] id;
        private boolean batch;
        OpConf(AccessType _type , int[]_id , boolean _batch){
            type = _type;
            id = _id;
            batch = _batch;
        }
        OpConf(AccessType _type , int[]_id){
            type = _type;
            id = _id;
            batch = false;
        }
        protected  int[] getId(){return id;}
        protected AccessType getType(){return type;}
        protected boolean getBatch(){return batch;}
    }
}
