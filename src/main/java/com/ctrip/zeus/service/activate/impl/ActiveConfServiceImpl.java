//package com.ctrip.zeus.service.activate.impl;
//
//import com.ctrip.zeus.dal.core.*;
//import com.ctrip.zeus.service.activate.ActiveConfService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
///**
// * Created by fanqq on 2015/3/30.
// */
//@Component("activeConfService")
//public class ActiveConfServiceImpl implements ActiveConfService {
//    @Resource
//    private ConfGroupActiveDao confGroupActiveDao;
//    @Resource
//    private ConfSlbActiveDao confSlbActiveDao;
//    @Resource
//    private ConfSlbVirtualServerActiveDao confSlbVirtualServerActiveDao;
//
//    private Logger logger = LoggerFactory.getLogger(ActiveConfServiceImpl.class);
//
//
//    @Override
//    public Set<Long> getSlbIdsByGroupId(Long groupId) throws Exception {
//        Set<Long> slbIds = new HashSet<>();
//
//        List<ConfGroupActiveDo> groupActiveDos = confGroupActiveDao.findAllByGroupIds(new Long[]{groupId},ConfGroupActiveEntity.READSET_FULL);
//        List<Long> vsIds = new ArrayList<>();
//        for (ConfGroupActiveDo c : groupActiveDos){
//            vsIds.add(c.getSlbVirtualServerId());
//        }
//        List<ConfSlbVirtualServerActiveDo> vsActiveDos = confSlbVirtualServerActiveDao.findBySlbVirtualServerIds(vsIds.toArray(new Long[]{}),ConfSlbVirtualServerActiveEntity.READSET_FULL);
//        for (ConfSlbVirtualServerActiveDo c : vsActiveDos){
//            slbIds.add(c.getSlbId());
//        }
//        return slbIds;
//    }
//    @Override
//    public Set<Long> getSlbIdsByGroupIds(Long[] groupId) throws Exception {
//        Set<Long> slbIds = new HashSet<>();
//
//        List<ConfGroupActiveDo> groupActiveDos = confGroupActiveDao.findAllByGroupIds(groupId,ConfGroupActiveEntity.READSET_FULL);
//        List<Long> vsIds = new ArrayList<>();
//        for (ConfGroupActiveDo c : groupActiveDos){
//            vsIds.add(c.getSlbVirtualServerId());
//        }
//        List<ConfSlbVirtualServerActiveDo> vsActiveDos = confSlbVirtualServerActiveDao.findBySlbVirtualServerIds(vsIds.toArray(new Long[]{}),ConfSlbVirtualServerActiveEntity.READSET_FULL);
//        for (ConfSlbVirtualServerActiveDo c : vsActiveDos){
//            slbIds.add(c.getSlbId());
//        }
//        return slbIds;
//    }
//    @Override
//    public Set<Long> getGroupIdsBySlbId(Long slbId) throws Exception {
//        Set<Long> groupIds = new HashSet<>();
//        List<ConfSlbVirtualServerActiveDo> vsActiveDos = confSlbVirtualServerActiveDao.findBySlbId(slbId,ConfSlbVirtualServerActiveEntity.READSET_FULL);
//        List<Long> vsIds = new ArrayList<>();
//        for (ConfSlbVirtualServerActiveDo c : vsActiveDos){
//            vsIds.add(c.getSlbVirtualServerId());
//        }
//        List<ConfGroupActiveDo> groupActiveDos = confGroupActiveDao.findAllByslbVirtualServerIds(vsIds.toArray(new Long[]{})
//                ,ConfGroupActiveEntity.READSET_FULL);
//
//        for (ConfGroupActiveDo c : groupActiveDos){
//            groupIds.add(c.getGroupId());
//        }
//        return groupIds;
//    }
//
//    @Override
//    public Set<Long> getVsIdsBySlbId(Long slbId) throws Exception {
//        List<ConfSlbVirtualServerActiveDo> vsActiveDos = confSlbVirtualServerActiveDao.findBySlbId(slbId,ConfSlbVirtualServerActiveEntity.READSET_FULL);
//        Set<Long> res = new HashSet<>();
//        for (ConfSlbVirtualServerActiveDo vs : vsActiveDos){
//            res.add(vs.getSlbVirtualServerId());
//        }
//        return res;
//    }
//}
