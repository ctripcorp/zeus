package com.ctrip.zeus.service.aop.OperationLog;


import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fanqq on 2015/7/16.
 */
public class OperationLogConfig {

    private static HashMap<String,OpConf> config = new HashMap<>();
    private static OperationLogConfig logConfig = new OperationLogConfig();
    private DynamicStringProperty disableList = DynamicPropertyFactory.getInstance().getStringProperty("operation.log.disable.list", "");
    private volatile List<String> disable = new ArrayList<>();


    private OperationLogConfig() {
       loadConfig();
    }

    private void loadConfig(){
        config.put("ActivateResource.activateSlb",new OpConf(OperationLogType.SLB,new int[]{2,3}));
        config.put("ActivateResource.activateGroup",new OpConf(OperationLogType.GROUP,new int[]{2,3}));

        config.put("DeactivateResource.deactivateGroup",new OpConf(OperationLogType.GROUP,new int[]{2,3}));

//        config.put("GroupResource.list",new OpConf(OperationLogType.GROUP,null,true));
//        config.put("GroupResource.get",new OpConf(OperationLogType.GROUP,new int[]{2,3}));
        config.put("GroupResource.add",new OpConf(OperationLogType.GROUP,new int[]{-1,2}));
        config.put("GroupResource.update",new OpConf(OperationLogType.GROUP,new int[]{2}));
        config.put("GroupResource.delete",new OpConf(OperationLogType.GROUP,new int[]{2}));

//        config.put("SlbResource.list",new OpConf(OperationLogType.SLB,null,true));
//        config.put("SlbResource.get",new OpConf(OperationLogType.SLB,new int[]{2,3}));
        config.put("SlbResource.add",new OpConf(OperationLogType.SLB,new int[]{-1,2}));
        config.put("SlbResource.update",new OpConf(OperationLogType.SLB,new int[]{2}));
        config.put("SlbResource.delete",new OpConf(OperationLogType.SLB,new int[]{2}));

        config.put("ServerResource.upServer",new OpConf(OperationLogType.SERVER,new int[]{2}));
        config.put("ServerResource.downServer",new OpConf(OperationLogType.SERVER,new int[]{2}));
        config.put("ServerResource.upMember",new OpConf(OperationLogType.GROUP,new int[]{2,3}));
        config.put("ServerResource.downMember",new OpConf(OperationLogType.GROUP,new int[]{2,3}));

//        config.put("StatusResource.allGroupStatusInSlb",new OpConf(OperationLogType.SLB,new int[]{2,3}));
//        config.put("StatusResource.groupStatus",new OpConf(OperationLogType.GROUP,new int[]{2,3}));

        disableList.addCallback(new Runnable() {
            @Override
            public void run() {
                List<String> tmp = new ArrayList<>();
                String[] list = disableList.get().split(";");
                if (list.length>0){
                    tmp = Arrays.asList(list);
                }
                disable = tmp;
            }
        });
    }

    public static OperationLogConfig getInstance(){return logConfig;}
    public OperationLogType getType(String key){
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
    public boolean contain(String key){
        if (disable.contains(key))
        {
            return false;
        }
        if (config.get(key)==null){
            return false;
        }
        return true;
    }
    class OpConf{
        private OperationLogType type;
        private int[] id;
        private boolean batch;
        OpConf(OperationLogType _type , int[]_id , boolean _batch){
            type = _type;
            id = _id;
            batch = _batch;
        }
        OpConf(OperationLogType _type , int[]_id){
            type = _type;
            id = _id;
            batch = false;
        }
        protected  int[] getId(){return id;}
        protected OperationLogType getType(){return type;}
        protected boolean getBatch(){return batch;}
    }
}
