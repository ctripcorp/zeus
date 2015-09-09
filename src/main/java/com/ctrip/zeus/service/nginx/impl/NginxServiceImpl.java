package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.dal.core.NginxServerDao;
import com.ctrip.zeus.dal.core.NginxServerDo;
import com.ctrip.zeus.dal.core.NginxServerEntity;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.NginxOperator;
import com.ctrip.zeus.nginx.TrafficStatusHelper;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import com.ctrip.zeus.nginx.entity.ReqStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.nginx.RollingTrafficStatus;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Service("nginxService")
public class NginxServiceImpl implements NginxService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NginxServiceImpl.class);
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);
    private static DynamicIntProperty dyupsPort = DynamicPropertyFactory.getInstance().getIntProperty("dyups.port", 8081);

    private final DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private NginxConfService nginxConfService;
    @Resource
    private NginxServerDao nginxServerDao;
    @Resource
    private RollingTrafficStatus rollingTrafficStatus;
    @Resource
    private ActivateService activateService;


    private Logger logger = LoggerFactory.getLogger(NginxServiceImpl.class);

    @Override
    public NginxResponse writeToDisk(List<Long> vsIds , Long slbId ,Integer slbVersion) throws Exception {
        String ip = S.getIp();
        Slb slb ;
        if (slbVersion!=null&&slbVersion!=0){
            slb = activateService.getActivatingSlb(slbId,slbVersion);
        }else {
            slb = activateService.getActivatedSlb(slbId);
        }
        AssertUtils.assertNotNull(slb,"Can't found slbId when writing config to disk!");
        int version = nginxConfService.getCurrentBuildingVersion(slbId);

        NginxServerDo nginxServerDo = nginxServerDao.findByIp(ip, NginxServerEntity.READSET_FULL);
        if (nginxServerDo != null && nginxServerDo.getVersion() == version) {
            NginxResponse res = new NginxResponse();
            res.setServerIp(ip).setSucceed(true).setOutMsg("current version is lower then or equal the version used!current version ["
                    + version + "],used version [" + nginxServerDo.getVersion() + "]");
            return res;
        }

        NginxOperator nginxOperator = new NginxOperator(slb.getNginxConf(), slb.getNginxBin());

        cleanConfOnDisk(slbId, version, nginxOperator);
        writeConfToDisk(slbId, version, vsIds, nginxOperator);

        NginxResponse response = nginxOperator.reloadConfTest();
        response.setServerIp(ip);
        if(response.getSucceed()){
            NginxServerDo nginxServer = nginxServerDao.findByIp(ip, NginxServerEntity.READSET_FULL);
            if (nginxServer == null)
            {
                nginxServer = new NginxServerDo().setIp(ip).setVersion(version);
            }
            nginxServerDao.updateByPK(nginxServer.setVersion(version), NginxServerEntity.UPDATESET_FULL);
        }
        return response;
    }

    @Override
    public boolean writeALLToDisk(Long slbId, Integer slbVersion ,List<Long> vsIds) throws Exception {
        List<NginxResponse> responses = new ArrayList<>();
        if (!writeALLToDisk(slbId,slbVersion,vsIds, responses)){
            for (NginxResponse response : responses){
                if (!response.getSucceed()){
                    throw new Exception("Write To Disk Failed! Detail: "+String.format(NginxResponse.JSON,response));
                }
            }
            throw new Exception("Write To Disk Failed! Detail: None.");
        }else{
            return true;
        }
    }

    @Override
    public List<NginxResponse> writeALLToDiskListResult(Long slbId,Integer slbVersion ,List<Long> vsIds ) throws Exception {
        List<NginxResponse> result = new ArrayList<>();
        writeALLToDisk(slbId, slbVersion ,vsIds, result);
        return result;
    }

    public boolean writeALLToDisk(Long slbId,Integer slbVersion , List<Long> vsIds , List<NginxResponse> responses) throws Exception {
        List<NginxResponse> result = null;
        boolean sucess = true;
        if (responses != null) {
            result = responses;
        } else {
            result = new ArrayList<>();
        }

        Slb slb ;
        if (slbVersion!=null&&slbVersion!=0){
            slb = activateService.getActivatingSlb(slbId,slbVersion);
        }else {
            slb =activateService.getActivatedSlb(slbId);
        }
        AssertUtils.assertNotNull(slb,"Can't found slbId when writing config to disk!");
        List<SlbServer> slbServers = slb.getSlbServers();
        for (SlbServer slbServer : slbServers) {
            logger.info("[ writeAllToDisk ]: start write to server : " + slbServer.getIp());
            NginxClient nginxClient = NginxClient.getClient(buildRemoteUrl(slbServer.getIp()));
            NginxResponse response = nginxClient.write(vsIds,slbId,slbVersion);
            result.add(response);

            logger.info("[ writeAllToDisk ]: write to server finished : " + slbServer.getIp());
        }

        if (result.size() == 0) {
            sucess = false;
        }

        for (NginxResponse res : result) {
            sucess = sucess && res.getSucceed();
        }

        return sucess;
    }

    @Override
    public NginxResponse load(Long slbId , Integer version) throws Exception {
        String ip = S.getIp();
        Slb slb ;
        if (version!=null&&version!=0){
            slb = activateService.getActivatingSlb(slbId,version);
        }else {
            slb = activateService.getActivatedSlb(slbId);
        }

        NginxOperator nginxOperator = new NginxOperator(slb.getNginxConf(), slb.getNginxBin());

        // reload configuration
        NginxResponse response = nginxOperator.reloadConf();
        response.setServerIp(ip);
        return response;
    }


    @Override
    public List<NginxResponse> loadAll(Long slbId , Integer version) throws Exception {
        List<NginxResponse> result = new ArrayList<>();
        Slb slb;
        if (version!=null&&version!=0){
            slb = activateService.getActivatingSlb(slbId,version);
        }else {
            slb = activateService.getActivatedSlb(slbId);
        }

        List<SlbServer> slbServers = slb.getSlbServers();
        for (SlbServer slbServer : slbServers) {
            NginxClient nginxClient = NginxClient.getClient(buildRemoteUrl(slbServer.getIp()));
            NginxResponse response = nginxClient.load(slbId,version);
            result.add(response);
        }
        return result;

    }

    @Override
    public List<NginxResponse> writeAllAndLoadAll(Long slbId,Integer slbVersion,List<Long> vsIds) throws Exception {
        List<NginxResponse> result = new ArrayList<>();
        if (!writeALLToDisk(slbId,slbVersion,vsIds, result)) {
            LOGGER.error("Write All To Disk Failed!");
            StringBuilder sb = new StringBuilder(128);
            sb.append("[");
            for (NginxResponse res : result) {
                sb.append(String.format(NginxResponse.JSON, res)).append(",\n");
            }
            sb.append("]");
            throw new Exception("Write All To Disk Failed!\nDetail:\n" + sb.toString());
        }
        result = loadAll(slbId , slbVersion);
        return result;
    }

    @Override
    public NginxResponse dyopsLocal(String upsName, String upsCommands) throws Exception {
        return new NginxOperator().dyupsLocal(upsName, upsCommands);
    }

    @Override
    public List<NginxResponse> dyops(Long slbId, List<DyUpstreamOpsData> dyups) throws Exception {
        List<NginxResponse> result = new ArrayList<>();
        Slb slb = activateService.getActivatedSlb(slbId);
        boolean flag = false;

        List<SlbServer> slbServers = slb.getSlbServers();
        for (SlbServer slbServer : slbServers) {
            flag = true;
            NginxClient nginxClient = NginxClient.getClient("http://" + slbServer.getIp() + ":" + adminServerPort.get());
            for (DyUpstreamOpsData dyup : dyups) {
                NginxResponse response = nginxClient.dyups(dyup.getUpstreamName(), dyup.getUpstreamCommands());
                response.setServerIp(slbServer.getIp());
                result.add(response);
                flag = flag && response.getSucceed();
            }
        }
        return result;
    }


    @Override
    public NginxServerStatus getStatus() throws Exception {
        String ip = S.getIp();
        Slb slb = slbRepository.getBySlbServer(ip);
        NginxOperator nginxOperator = new NginxOperator(slb.getNginxConf(), slb.getNginxBin());
        return nginxOperator.getRuntimeStatus();
    }

    @Override
    public List<NginxServerStatus> getStatusAll(Long slbId) throws Exception {
        List<NginxServerStatus> result = new ArrayList<>();
        String ip = S.getIp();

        Slb slb = slbRepository.getById(slbId);
        for (SlbServer slbServer : slb.getSlbServers()) {
            if (ip.equals(slbServer.getIp())) {
                result.add(getStatus());
                continue;
            }
            NginxClient nginxClient = NginxClient.getClient(buildRemoteUrl(slbServer.getIp()));
            NginxServerStatus response = nginxClient.getNginxServerStatus();
            result.add(response);
        }
        return result;
    }

    @Override
    public List<ReqStatus> getTrafficStatusBySlb(Long slbId, int count, boolean aggregatedByGroup, boolean aggregatedBySlbServer) throws Exception {
        List<ReqStatus> result = getTrafficStatusBySlb(slbId, count);
        if (!(aggregatedByGroup && aggregatedBySlbServer)) {
            result = aggregateByKey(result, aggregatedByGroup, aggregatedBySlbServer, slbId);
        }
        if (aggregatedByGroup) {
            for (ReqStatus reqStatus : result) {
                if (reqStatus.getGroupId()!= null && reqStatus.getGroupId() == -1L) {
                    reqStatus.setSlbId(slbId);
                    reqStatus.setGroupName("Not exist");
                    continue;
                }
                Group g = groupRepository.getById(reqStatus.getGroupId());
                if (g == null)
                    reqStatus.setGroupName("Not Found");
                else
                    reqStatus.setGroupName(g.getName());
                reqStatus.setSlbId(slbId);
            }
        } else {
            for (ReqStatus reqStatus : result) {
                reqStatus.setSlbId(slbId);
            }
        }
        return result;
    }

    private List<ReqStatus> getTrafficStatusBySlb(Long slbId, int count) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        List<ReqStatus> list = new ArrayList<>();
        for (SlbServer slbServer : slb.getSlbServers()) {
            NginxClient nginxClient = NginxClient.getClient(buildRemoteUrl(slbServer.getIp()));
            try {
                list.addAll(nginxClient.getTrafficStatus(System.currentTimeMillis() - 60 * 1000L, count).getStatuses());
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        return list;
    }

    private List<ReqStatus> aggregateByKey(List<ReqStatus> raw, boolean group, boolean slbServer, Long slbId) {
        Map<String, ReqStatus> result = new ConcurrentHashMap<>();
        for (ReqStatus reqStatus : raw) {
            String key = genKey(reqStatus, group, slbServer);
            ReqStatus value = result.get(key);
            if (group) {
                result.put(key, TrafficStatusHelper.add(value, reqStatus, "", slbId, reqStatus.getGroupId(), null));
                continue;
            }
            if (slbServer) {
                result.put(key, TrafficStatusHelper.add(value, reqStatus, reqStatus.getHostName(), slbId, -1L, ""));
                continue;
            }
            result.put(key, TrafficStatusHelper.add(value, reqStatus, "", slbId, -1L, ""));
        }
        return new LinkedList<>(result.values());
    }

    private String genKey(ReqStatus reqStatus, boolean group, boolean slbServer) {
        String time = formatter.format(reqStatus.getTime());
        if (group)
            return time + "-" + reqStatus.getGroupId();
        if (slbServer)
            return time + "-" + reqStatus.getHostName();
        return time + "";
    }

    @Override
    public List<ReqStatus> getTrafficStatusBySlb(String groupName, Long slbId, int count) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        List<ReqStatus> list = new ArrayList<>();
        for (SlbServer slbServer : slb.getSlbServers()) {
            NginxClient nginxClient = NginxClient.getClient(buildRemoteUrl(slbServer.getIp()));
            try {
                list.addAll(nginxClient.getTrafficStatusByGroup(System.currentTimeMillis() - 60 * 1000L, groupName, count).getStatuses());
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        return list;
    }

    @Override
    public List<ReqStatus> getLocalTrafficStatus(Date time, int count) {
        LinkedList<TrafficStatus> l = (LinkedList<TrafficStatus>) rollingTrafficStatus.getResult();
        List<ReqStatus> result = new LinkedList<>();
        int size = l.size();
        TrafficStatus head = l.peekLast();
        // In case of time diff from server fetchers
        for (int i = 0; i < count + 1 && i < size; i++) {
            TrafficStatus ts = l.pollLast();
            if (formatter.format(time).equals(formatter.format(ts.getTime()))) {
                result.addAll(ts.getReqStatuses());
            }
        }
        if (result.size() == 0) {
            for (ReqStatus reqStatus : head.getReqStatuses()) {
                result.add(new ReqStatus().setGroupId(reqStatus.getGroupId())
                        .setGroupName(reqStatus.getGroupName())
                        .setSlbId(reqStatus.getSlbId()).setTime(time));
            }
        }
        return result;
    }

    @Override
    public List<ReqStatus> getLocalTrafficStatus(Date time, String groupName, int count) {
        LinkedList<TrafficStatus> l = (LinkedList<TrafficStatus>) rollingTrafficStatus.getResult();
        List<ReqStatus> result = new LinkedList<>();
        int size = l.size();
        for (int i = 0; i < count && i < size; i++) {
            for (ReqStatus reqStatus : l.pollLast().getReqStatuses()) {
                if (reqStatus.getGroupName().equalsIgnoreCase(groupName)) {
                    result.add(reqStatus);
                }
            }
        }
        return result;
    }

    private void writeConfToDisk(Long slbId, int version, List<Long> vsIds ,NginxOperator nginxOperator) throws Exception {
        LOGGER.info("Start writing nginx configuration.");
        // write nginx conf
        writeNginxConf(slbId, version, nginxOperator);
        // write server conf
        writeServerConf(slbId, version,vsIds, nginxOperator);
        // write upstream conf
        writeUpstreamConf(slbId, version,vsIds, nginxOperator);
    }

    private static String buildRemoteUrl(String ip) {
        return "http://" + ip + ":" + adminServerPort.get();
    }

    private void writeNginxConf(Long slbId, int version, NginxOperator nginxOperator) throws Exception {
        String nginxConf = nginxConfService.getNginxConf(slbId, version);
        if (nginxConf == null || nginxConf.isEmpty()) {
            throw new IllegalStateException("the nginx conf must not be empty!");
        }
        nginxOperator.writeNginxConf(nginxConf);
    }

    private void writeServerConf(Long slbId, int version,List<Long> vsIds , NginxOperator nginxOperator) throws Exception {
        List<NginxConfServerData> nginxConfServerDataList = nginxConfService.getNginxConfServer(slbId, version);
        for (NginxConfServerData d : nginxConfServerDataList) {
            if (vsIds.contains(d.getVsId()))
            {
                nginxOperator.writeServerConf(d.getVsId(), d.getContent());
            }
        }
    }

    private void writeUpstreamConf(Long slbId, int version,List<Long> vsIds , NginxOperator nginxOperator) throws Exception {
        List<NginxConfUpstreamData> nginxConfUpstreamList = nginxConfService.getNginxConfUpstream(slbId, version);
        for (NginxConfUpstreamData d : nginxConfUpstreamList) {
            if (vsIds.contains(d.getVsId())){
                nginxOperator.writeUpstreamsConf(d.getVsId(), d.getContent());
            }
        }
    }

    private void cleanConfOnDisk(Long slbId, int version, NginxOperator nginxOperator) throws Exception {
        List<NginxConfServerData> nginxConfServerDataList = nginxConfService.getNginxConfServer(slbId, version);
        List<Long> vslist = new ArrayList<>();
        for (NginxConfServerData d : nginxConfServerDataList) {
            vslist.add(d.getVsId());
        }
        nginxOperator.cleanConf(vslist);
    }
}
