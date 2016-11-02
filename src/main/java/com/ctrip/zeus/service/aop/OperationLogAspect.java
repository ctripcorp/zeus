package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.aop.OperationLog.OperationLogConfig;
import com.ctrip.zeus.service.aop.OperationLog.OperationLogType;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.operationLog.OperationLogService;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fanqq on 2015/7/15.
 */
@Aspect
@Component
public class OperationLogAspect implements Ordered {
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private OperationLogService operationLogService;


    private final static String QUERY_NAME_SLB_ID = "slbId";
    private final static String QUERY_NAME_SLB_NAME = "slbName";
    private final static String QUERY_NAME_GROUP_ID = "groupId";
    private final static String QUERY_NAME_VS_ID = "vsId";
    private final static String QUERY_NAME_GROUP_NAME = "groupName";
    private final static String ID_NEW = "New";
    private final static String ID_UNKNOW = "Unknow";
    private final static String ID_BATCH = "Batch";


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private DynamicBooleanProperty enableAccess = DynamicPropertyFactory.getInstance().getBooleanProperty("log.access.enable", true);
    private DynamicBooleanProperty operationLogNewVersion = DynamicPropertyFactory.getInstance().getBooleanProperty("new.operation.log", false);

    @Around("execution(* com.ctrip.zeus.restful.resource.*Resource.*(..))")
    public Object operationLog(ProceedingJoinPoint point) throws Throwable {
        if (!enableAccess.get()) {
            return point.proceed();
        }
        if(operationLogNewVersion.get()){
            return point.proceed();
        }

        String type = null;
        String id = null;
        String op = null;
        String userName = null;
        String remoteAddr = null;
        HashMap<String, String> data = new HashMap<>();
        Object response = null;
        String errMsg = null;
        boolean success = false;
        HttpServletRequest request = findRequestArg(point);
        if (request == null) {
            return point.proceed();
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String key = point.getTarget().getClass().getSimpleName() + "." + method.getName();
        if (!OperationLogConfig.getInstance().contain(key)) {
            return point.proceed();
        }
        try {
            Object[] args = point.getArgs();
            Annotation[][] annotations = method.getParameterAnnotations();
            HttpHeaders hh = findHttpHeaders(point);
            type = OperationLogConfig.getInstance().getType(key).value();
            id = findId(key, request, args, point, hh);
            op = method.getName();
            userName = request.getRemoteUser();
            remoteAddr = request.getRemoteAddr();
            data = findData(key, request, args, hh, method);
        } catch (Exception e) {
            logger.warn("Operation Log Aspect Exception!" + e.getMessage());
        }
        try {
            response = point.proceed();
        } catch (Throwable throwable) {
            errMsg = throwable.getMessage();
            throw throwable;
        } finally {
            if (response != null) {
                success = true;
            }
            if (userName == null) {
                userName = "Unknown";
            }
            operationLogService.insert(type, id, op, data.toString(), userName, remoteAddr, success, errMsg, new Date());
        }
        return response;
    }

    @Override
    public int getOrder() {
        return AspectOrder.Access;
    }

    private HttpServletRequest findRequestArg(JoinPoint point) {
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        return null;
    }

    private HttpHeaders findHttpHeaders(JoinPoint point) {
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpHeaders) {
                return (HttpHeaders) arg;
            }
        }
        return null;
    }

    private String findId(String key, HttpServletRequest request, Object[] args, JoinPoint point, HttpHeaders hh) throws Exception {
        //1. Batch is true, return "Batch"
        if (OperationLogConfig.getInstance().getBatch(key)) {
            return ID_BATCH;
        }
        // find by config ids
        int[] ids = OperationLogConfig.getInstance().getIds(key);
        for (int i : ids) {
            //2. if id is out of range , return New
            if (i < 0 || i >= args.length) {
                return ID_NEW;
            }
            //3. if request Method is Post , Parse post data to get id
            if (request.getMethod().equals("POST") && null != args[i]) {
                // 3.1 httpHeaders is null , Can not parse post data;
                if (null == hh) {
                    return ID_UNKNOW;
                }
                // 3.2 type is AccessType.SLB , parse data to Slb object.
                if (OperationLogConfig.getInstance().getType(key) == OperationLogType.SLB) {
                    Slb slb = parseSlb((String) args[i]);
                    if (slb == null) {
                        return ID_UNKNOW;//"Slb Parse Fail";
                    }
                    return slb.getId() != null ? String.valueOf(slb.getId()) : slb.getName();
                }
                // 3.3 type is AccessType.GROUP , parse data to Group object.
                if (OperationLogConfig.getInstance().getType(key) == OperationLogType.GROUP) {
                    Group group = parseGroup((String) args[i]);
                    if (group == null) {
                        return ID_UNKNOW;//"Group Parse Fail";
                    }
                    return group.getId() != null ? String.valueOf(group.getId()) : group.getName();
                }
                // 3.3 type is AccessType.VS , parse data to Group object.
                if (OperationLogConfig.getInstance().getType(key) == OperationLogType.VS) {
                    VirtualServer vs = parseVS((String) args[i]);
                    if (vs == null) {
                        return ID_UNKNOW;//"Group Parse Fail";
                    }
                    return vs.getId() != null ? String.valueOf(vs.getId()) : vs.getName();
                }
                return ID_UNKNOW;
            }
            // 4. if method is GET , find id from args. Type Long comes first.
            if (null != args[i]) {
                String name = null;
                if (args[i] instanceof String) {
                    name = (String) args[i];
                }
                if (args[i] instanceof Long) {
                    return String.valueOf((Long) args[i]);
                }
                if (args[i] instanceof List<?>) {
                    List tmp = (List) args[i];
                    if (tmp.size() > 1) {
                        return ID_BATCH;
                    } else if (tmp.size() == 1) {
                        if (tmp.get(0) instanceof String) {
                            name = (String) tmp.get(0);
                        } else {
                            return tmp.get(0).toString();
                        }
                    }
                }
                if (OperationLogConfig.getInstance().getType(key) == OperationLogType.GROUP && name != null) {
                    Long groupId = groupCriteriaQuery.queryByName(name);
                    return groupId == null ? name : String.valueOf(groupId);
                } else if (OperationLogConfig.getInstance().getType(key) == OperationLogType.SLB && name != null) {
                    Long slbId = slbCriteriaQuery.queryByName(name);
                    return slbId == null ? name : String.valueOf(slbId);
                } else if (name != null) {
                    return name;
                }
            }
        }
        return ID_UNKNOW;
    }

    private HashMap<String, String> findData(String key, HttpServletRequest request, Object[] args, HttpHeaders hh, Method method) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        int[] ids = OperationLogConfig.getInstance().getIds(key);

        Annotation[][] annotations = method.getParameterAnnotations();
        //1. request Method is Post, data should be Name and Version
        if (request.getMethod().equals("POST")) {
            if (ids == null || ids.length <= 0) {
                return data;
            }
            //1.1 in case of New
            if (ids.length > 0 && (ids[0] < 0 || ids[0] >= args.length)) {
                data.put("Version", "1");
                if (ids.length >= 2 && args[ids[1]] instanceof String) {
                    String postData = (String) args[ids[1]];
                    if (OperationLogConfig.getInstance().getType(key) == OperationLogType.SLB && hh != null) {
                        Slb slb = parseSlb(postData);
                        if (slb == null) {
                            data.put("Slb", "Slb Parse Fail!");
                        } else {
                            data.put("SlbName", String.valueOf(slb.getName()));
                        }

                    } else if (OperationLogConfig.getInstance().getType(key) == OperationLogType.GROUP && hh != null) {
                        Group group = parseGroup(postData);
                        if (group == null) {
                            data.put("Group", "Group Parse Fail!");
                        } else {
                            data.put("GroupName", String.valueOf(group.getName()));
                        }
                    }
                }
                return data;
            }
            //1.2 in case of update
            Object obj = args[ids[0]];
            String tmp;
            if (obj instanceof String) {
                tmp = (String) obj;
            } else {
                data.put("errMsg", ids[0] + "'st argument is not String");
                return data;
            }
            if (OperationLogConfig.getInstance().getType(key) == OperationLogType.SLB && hh != null) {
                Slb slb = parseSlb(tmp);
                if (slb == null) {
                    data.put("Slb", "Slb Parse Fail!");
                } else {
                    data.put("SlbName", String.valueOf(slb.getName()));
                    data.put("Version", String.valueOf(slb.getVersion()));
                }

            } else if (OperationLogConfig.getInstance().getType(key) == OperationLogType.GROUP && hh != null) {
                Group group = parseGroup(tmp);
                if (group == null) {
                    data.put("Group", "Group Parse Fail!");
                } else {
                    data.put("GroupName", String.valueOf(group.getName()));
                    data.put("Version", String.valueOf(group.getVersion()));
                }
            } else if (OperationLogConfig.getInstance().getType(key) == OperationLogType.VS && hh != null) {
                VirtualServer vs = parseVS(tmp);
                if (vs == null) {
                    data.put("VS", "Group Parse Fail!");
                } else {
                    data.put("VSName", String.valueOf(vs.getName()));
                    data.put("VsVersion", String.valueOf(vs.getVersion()));
                }
            }
            return data;
        } else {
            //2. request method is GET, get data from QueryString
            String tmpKey = null;
            for (int i = 0; i < args.length; i++) {
                if (annotations[i][0] instanceof QueryParam) {
                    //1. get queryParam Names
                    tmpKey = ((QueryParam) annotations[i][0]).value();
                    //2. queryParam Names equals QUERY_NAME_GROUP_ID , must be groupId.
                    if (tmpKey.equalsIgnoreCase(QUERY_NAME_GROUP_ID) && args[i] != null) {
                        if (args[i] instanceof Long) {
                            Group group = groupRepository.getById((Long) args[i]);
                            data.put(OperationLogType.GROUP.value(), group != null ? group.getName() : args[i].toString() + "[id not found]");
                        }
                        if (args[i] instanceof List) {
                            List list = (List) args[i];
                            List<String> groupNames = new ArrayList<>();
                            for (Object groupId : list) {
                                Group group = groupRepository.getById((Long) groupId);
                                groupNames.add(group != null ? group.getName() : groupId.toString() + "[id not found]");
                            }
                            if (groupNames.size() > 0) {
                                data.put(OperationLogType.GROUP.value(), groupNames.toString());
                            }
                        }
                    } else if (tmpKey.equalsIgnoreCase(QUERY_NAME_SLB_ID) && args[i] != null) {
                        if (args[i] instanceof Long) {
                            Slb slb = slbRepository.getById((Long) args[i]);
                            data.put(OperationLogType.SLB.value(), slb != null ? slb.getName() : args[i].toString() + "[id not found]");
                        }
                        if (args[i] instanceof List) {
                            List list = (List) args[i];
                            List<String> slbNames = new ArrayList<>();
                            for (Object slbId : list) {
                                Slb slb = slbRepository.getById((Long) slbId);
                                slbNames.add(slb != null ? slb.getName() : slbId.toString() + "[id not found]");
                            }
                            if (slbNames.size() > 0) {
                                data.put(OperationLogType.SLB.value(), slbNames.toString());
                            }
                        }
                    } else if (tmpKey.equalsIgnoreCase(QUERY_NAME_VS_ID) && args[i] != null) {
                        if (args[i] instanceof Long) {
                            VirtualServer vs = virtualServerRepository.getById((Long) args[i]);
                            data.put(OperationLogType.VS.value(), vs != null ? vs.getName() : args[i].toString() + "[id not found]");
                        }
                        if (args[i] instanceof List) {
                            List list = (List) args[i];
                            List<String> vsNames = new ArrayList<>();
                            for (Object slbId : list) {
                                VirtualServer vs = virtualServerRepository.getById((Long) slbId);
                                vsNames.add(vs != null ? vs.getName() : slbId.toString() + "[id not found]");
                            }
                            if (vsNames.size() > 0) {
                                data.put(OperationLogType.VS.value(), vsNames.toString());
                            }
                        }
                    } else if (args[i] != null) {
                        if (args[i] instanceof List && ((List) args[i]).size() > 0 || !(args[i] instanceof List)) {
                            data.put(tmpKey, args[i].toString());
                        }
                    }
                }
            }
            return data;
        }
    }

    private Group parseGroup(String group) {
        Group g = ObjectJsonParser.parse(group, Group.class);
        trim(g);
        return g;
    }

    private Slb parseSlb(String slb) {
        Slb s = ObjectJsonParser.parse(slb, Slb.class);
        return s;
    }

    private VirtualServer parseVS(String vs) {
        VirtualServer s = ObjectJsonParser.parse(vs, VirtualServer.class);
        return s;
    }

    private void trim(Group g) {
        g.setAppId(trimIfNotNull(g.getAppId()));
        g.setName(trimIfNotNull(g.getName()));
        if (g.getHealthCheck() != null)
            g.getHealthCheck().setUri(trimIfNotNull(g.getHealthCheck().getUri()));
        for (GroupServer groupServer : g.getGroupServers()) {
            groupServer.setIp(trimIfNotNull(groupServer.getIp()));
            groupServer.setHostName(trimIfNotNull(groupServer.getHostName()));
        }
        if (g.getLoadBalancingMethod() != null)
            g.getLoadBalancingMethod().setValue(trimIfNotNull(g.getLoadBalancingMethod().getValue()));
    }

    private String trimIfNotNull(String value) {
        return value != null ? value.trim() : value;
    }
}
