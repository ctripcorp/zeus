package com.ctrip.zeus.service.compare.impl;


import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.compare.ModelComparer;
import com.ctrip.zeus.service.message.queue.entity.ChangeDataEntity;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by fanqq on 2017/2/14.
 */
@Service("modelComparer")
public class ModelComparerImpl implements ModelComparer {

    @Override
    public List<ChangeDataEntity> compareGroup(Group from, Group to) {
        List<ChangeDataEntity> res = new ArrayList<>();
        ChangeDataEntity tmp;
        if (hasChange(from, to)) {
            tmp = assertEntityAddOrDelete(from, to, "Group");
            if (tmp != null) {
                res.add(tmp);
                return res;
            }
        }
        tmp = assertObjectEquals(from.getName(), to.getName(), "Name");
        if (tmp != null) {
            res.add(tmp);
        }
        tmp = assertObjectEquals(from.getSsl(), to.getSsl(), "GroupSsl");
        if (tmp != null) {
            res.add(tmp);
        }
        tmp = assertObjectEquals(from.getAppId(), to.getAppId(), "AppId");
        if (tmp != null) {
            res.add(tmp);
        }
        tmp = assertObjectEquals(from.getHealthCheck(), to.getHealthCheck(), "HealthCheck");
        if (tmp != null) {
            res.add(tmp);
        }
        tmp = assertObjectEquals(from.getLoadBalancingMethod(), to.getLoadBalancingMethod(), "LoadBalance");
        if (tmp != null) {
            res.add(tmp);
        }
        tmp = assertListEquals(from.getRuleSet(), to.getRuleSet(), "Rule");
        if (tmp != null) {
            res.add(tmp);
        }
        tmp = assertListEquals(from.getGroupServers(), to.getGroupServers(), "GroupServer");
        if (tmp != null) {
            res.add(tmp);
        }
        Map<Long, GroupVirtualServer> fromGvs = new HashMap<>();
        Map<Long, GroupVirtualServer> toGvs = new HashMap<>();
        for (GroupVirtualServer gvs : from.getGroupVirtualServers()) {
            fromGvs.put(gvs.getVirtualServer().getId(), gvs);
        }
        for (GroupVirtualServer gvs : to.getGroupVirtualServers()) {
            toGvs.put(gvs.getVirtualServer().getId(), gvs);
        }
        tmp = assertListEquals(new ArrayList<>(fromGvs.keySet()), new ArrayList<>(toGvs.keySet()), "GroupVirtualServer");
        if (tmp != null) {
            res.add(tmp);
        }
        Set<Long> keys = new HashSet<>();
        keys.addAll(fromGvs.keySet());
        keys.addAll(toGvs.keySet());
        for (Long key : keys) {
            GroupVirtualServer f = fromGvs.get(key);
            GroupVirtualServer t = toGvs.get(key);
            String fPath = null, fRewrite = null, fRedirect = null, tPath = null, tRewrite = null, tRedirect = null;
            Integer tPriority = null, fPriority = null;
            if (f != null) {
                fPath = f.getPath();
                fPriority = f.getPriority();
                fRewrite = f.getRewrite();
                fRedirect = f.getRedirect();
            }
            if (t != null) {
                tPath = t.getPath();
                tPriority = t.getPriority();
                tRewrite = t.getRewrite();
                tRedirect = t.getRedirect();
            }
            tmp = assertObjectEquals(fPath, tPath, "Path");
            if (tmp != null) {
                tmp.setFrom(tmp.getFrom() + "[VsId:" + key + "]");
                tmp.setTo(tmp.getTo() + "[VsId:" + key + "]");
                res.add(tmp);
            }
            tmp = assertObjectEquals(fPriority, tPriority, "Priority");
            if (tmp != null) {
                tmp.setFrom(tmp.getFrom() + "[VsId:" + key + "]");
                tmp.setTo(tmp.getTo() + "[VsId:" + key + "]");
                res.add(tmp);
            }
            tmp = assertObjectEquals(fRewrite, tRewrite, "Rewrite");
            if (tmp != null) {
                tmp.setFrom(tmp.getFrom() + "[VsId:" + key + "]");
                tmp.setTo(tmp.getTo() + "[VsId:" + key + "]");
                res.add(tmp);
            }
            tmp = assertObjectEquals(fRedirect, tRedirect, "Redirect");
            if (tmp != null) {
                tmp.setFrom(tmp.getFrom() + "[VsId:" + key + "]");
                tmp.setTo(tmp.getTo() + "[VsId:" + key + "]");
                res.add(tmp);
            }
        }
        return res;
    }

    @Override
    public List<ChangeDataEntity> compareVs(VirtualServer from, VirtualServer to) {
        List<ChangeDataEntity> res = new ArrayList<>();
        ChangeDataEntity tmp;

        if (hasChange(from, to)) {
            tmp = assertEntityAddOrDelete(from, to, "Vs");
            if (tmp != null) {
                res.add(tmp);
                return res;
            }
        }


        tmp = assertObjectEquals(from.getName(), to.getName(), "Name");
        if (tmp != null) {
            res.add(tmp);
        }

        tmp = assertObjectEquals(from.getPort(), to.getPort(), "Port");
        if (tmp != null) {
            res.add(tmp);
        }

        tmp = assertObjectEquals(from.getSsl(), to.getSsl(), "VsSsl");
        if (tmp != null) {
            res.add(tmp);
        }

        tmp = assertObjectEquals(from.getSlbId(), to.getSlbId(), "VsSlbId");
        if (tmp != null) {
            res.add(tmp);
        }

        tmp = assertListEquals(from.getRuleSet(), to.getRuleSet(), "Rule");
        if (tmp != null) {
            res.add(tmp);
        }

        tmp = assertListEquals(from.getDomains(), to.getDomains(), "Domains");
        if (tmp != null) {
            res.add(tmp);
        }

        tmp = assertListEquals(from.getSlbIds(), to.getSlbIds(), "VsSlbIds");
        if (tmp != null) {
            res.add(tmp);
        }
        return res;
    }

    @Override
    public List<ChangeDataEntity> compareSlb(Slb from, Slb to) {
        List<ChangeDataEntity> res = new ArrayList<>();
        ChangeDataEntity tmp;

        if (hasChange(from, to)) {
            tmp = assertEntityAddOrDelete(from, to, "Slb");
            if (tmp != null) {
                res.add(tmp);
                return res;
            }
        }

        tmp = assertObjectEquals(from.getName(), to.getName(), "Name");
        if (tmp != null) {
            res.add(tmp);
        }

        tmp = assertListEquals(from.getVips(), to.getVips(), "Vip");
        if (tmp != null) {
            res.add(tmp);
        }

        tmp = assertListEquals(from.getSlbServers(), to.getSlbServers(), "SlbServer");
        if (tmp != null) {
            res.add(tmp);
        }

        tmp = assertListEquals(from.getRuleSet(), to.getRuleSet(), "Rule");
        if (tmp != null) {
            res.add(tmp);
        }
        return res;
    }

    private ChangeDataEntity assertObjectEquals(Object o1, Object o2, String desc) {
        ChangeDataEntity change = null;
        if (o1 == null && o2 == null) return null;
        if (o1 == null) {
            change = new ChangeDataEntity();
            change.setEntity(desc);
            change.setFrom(null);
            change.setTo(toString(o2));
            change.setDescription("添加" + desc);
        } else if (o2 == null) {
            change = new ChangeDataEntity();
            change.setEntity(desc);
            change.setFrom(toString(o1));
            change.setTo(null);
            change.setDescription("删除" + desc);
        } else if (!o1.equals(o2)) {
            change = new ChangeDataEntity();
            change.setEntity(desc);
            change.setFrom(toString(o1));
            change.setTo(toString(o2));
            change.setDescription("修改" + desc);
        }
        return change;
    }

    private boolean hasChange(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return false;
        } else {
            return o1 == null || !o1.equals(o2);
        }
    }

    private ChangeDataEntity assertEntityAddOrDelete(Object o1, Object o2, String desc) {
        ChangeDataEntity change = null;
        if (o1 == null && o2 == null) return null;
        if (o1 == null) {
            change = new ChangeDataEntity();
            change.setEntity(desc);
            change.setFrom(null);
            change.setTo(toString(o2));
            change.setDescription("上线" + desc);
        } else if (o2 == null) {
            change = new ChangeDataEntity();
            change.setEntity(desc);
            change.setFrom(toString(o1));
            change.setTo(null);
            change.setDescription("下线" + desc);
        }
        return change;
    }

    private <T> ChangeDataEntity assertListEquals(List<T> l1, List<T> l2, String desc) {
        ChangeDataEntity change = null;
        if ((l1 == null || l1.size() == 0) && (l2 == null || l2.size() == 0)) return null;
        if (l1 == null || l1.size() == 0) {
            change = new ChangeDataEntity();
            change.setEntity(desc);
            change.setFrom(null);
            change.setTo(toString(l2));
            change.setDescription("添加" + desc);
        } else if (l2 == null || l2.size() == 0) {
            change = new ChangeDataEntity();
            change.setEntity(desc);
            change.setFrom(toString(l1));
            change.setTo(null);
            change.setDescription("删除所有" + desc);
        } else if (!l1.containsAll(l2) || !l2.containsAll(l1)) {
            change = new ChangeDataEntity();
            change.setEntity(desc);
            change.setFrom(toString(l1));
            change.setTo(toString(l2));
            if (l2.containsAll(l1)) {
                change.setDescription("增加" + desc);
            } else if (l1.containsAll(l2)) {
                change.setDescription("减少" + desc);
            } else {
                change.setDescription("修改" + desc);
            }
        }
        return change;
    }

    private String toString(Object obj) {
        try {
            return ObjectJsonWriter.write(obj).replaceAll("[\n|\r|\\s]", "");
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
