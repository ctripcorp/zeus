package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.model.AppRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.status.AppStatusService;
import com.ctrip.zeus.service.status.StatusService;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Component
@Path("/op")
public class ServerResource {

    @Resource
    StatusService statusService;
    @Resource
    private BuildService buildService;
    @Resource
    private BuildInfoService buildInfoService;
    @Resource
    private NginxService nginxAgentService;
    @Resource
    private SlbRepository slbClusterRepository;
    @Resource
    private NginxService nginxService;
    @Resource
    private AppStatusService appStatusService;
    @Resource
    private AppRepository appRepository;





    @GET
    @Path("/upServer")
    public Response upServer(@Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception{
        String serverip = ip;

        //update status
        statusService.upServer(serverip);

        //get slb by serverip
        List<Slb> slblist = slbClusterRepository.listByAppServerAndAppName(serverip,null);
        for (Slb slb : slblist)
        {
            String slbname = slb.getName();
            int ticket = buildInfoService.getTicket(slbname);
            if(buildService.build(slbname,ticket))
            {
                nginxAgentService.loadAll(slbname);
            }
        }

        ServerStatus ss = new ServerStatus().setIp(serverip).setUp(statusService.getServerStatus(serverip));
        for (String name : appRepository.listAppsByAppServer(serverip))
        {
            ss.addAppName(name);
        }

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(ServerStatus.XML, ss)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(ServerStatus.JSON, ss)).type(MediaType.APPLICATION_JSON).build();
        }

    }

    @GET
    @Path("/downServer")
    public Response downServer(@Context HttpHeaders hh, @QueryParam("ip") String ip) throws Exception{
        String serverip = ip;
        //update status
        statusService.downServer(serverip);
        //get slb by serverip
        List<Slb> slblist = slbClusterRepository.listByAppServerAndAppName(serverip,null);
        for (Slb slb : slblist)
        {
            String slbname = slb.getName();
            int ticket = buildInfoService.getTicket(slbname);
            if(buildService.build(slbname,ticket))
            {
                nginxAgentService.loadAll(slbname);
            }
        }

        ServerStatus ss = new ServerStatus().setIp(serverip).setUp(statusService.getServerStatus(serverip));
        for (String name : appRepository.listAppsByAppServer(serverip))
        {
            ss.addAppName(name);
        }

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(ServerStatus.XML, ss)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(ServerStatus.JSON, ss)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/upMember")
    public Response upMember(@Context HttpHeaders hh, @QueryParam("appName") String appName, @QueryParam("ip") String ip)throws Exception
    {
        statusService.upMember(appName,ip);

        //get slb by appname and ip
        List<Slb> slblist = slbClusterRepository.listByAppServerAndAppName(ip,appName);

        for (Slb slb : slblist) {
            String slbname = slb.getName();
            //get ticket
            int ticket = buildInfoService.getTicket(slbname);
            //build config
            if(buildService.build(slbname,ticket))
            {
                //push
                nginxAgentService.loadAll(slbname);
            }
        }

        List<AppStatus> statuses = appStatusService.getAppStatus(appName);
        AppStatus appStatusList = new AppStatus().setAppName(appName).setSlbName("");
        for (AppStatus a : statuses)
        {
            appStatusList.setSlbName(appStatusList.getSlbName()+" "+a.getSlbName());
            for(AppServerStatus b : a.getAppServerStatuses())
            {
                appStatusList.addAppServerStatus(b);
            }
        }

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(AppStatus.XML, appStatusList)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(AppStatus.JSON, appStatusList)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/downMember")
    public Response downMember(@Context HttpHeaders hh, @QueryParam("appName") String appName, @QueryParam("ip") String ip)throws Exception
    {
        statusService.downMember(appName, ip);

        //get slb by appname and ip
        List<Slb> slblist = slbClusterRepository.listByAppServerAndAppName(appName,ip);

        for (Slb slb : slblist) {
            String slbname = slb.getName();
            //get ticket
            int ticket = buildInfoService.getTicket(slbname);
            //build config
            if(buildService.build(slbname,ticket))
            {
                //push
                nginxAgentService.loadAll(slbname);
            }
        }

        List<AppStatus> statuses = appStatusService.getAppStatus(appName);
        AppStatus appStatusList = new AppStatus().setAppName(appName).setSlbName("");
        for (AppStatus a : statuses)
        {
            appStatusList.setSlbName(appStatusList.getSlbName() + " " + a.getSlbName());
            for(AppServerStatus b : a.getAppServerStatuses())
            {
                appStatusList.addAppServerStatus(b);
            }
        }

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(AppStatus.XML, appStatusList)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(AppStatus.JSON, appStatusList)).type(MediaType.APPLICATION_JSON).build();
        }
    }



    /*
     *  for Batch operation
     *  not used
     */

    @POST
    @Path("/upServers")
    public Response upServers(@Context HttpHeaders hh,String req) throws Exception {

        OpServerStatusReq upserverIps ;

        //parser req
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            upserverIps = DefaultSaxParser.parseEntity(OpServerStatusReq.class, req);
        } else {
            upserverIps = DefaultJsonParser.parse(OpServerStatusReq.class, req);
        }

        if (upserverIps == null) return Response.status(500).type(hh.getMediaType()).build();


        for (IpAddresses ipAddresses : upserverIps.getIpAddresseses())
        {
            String serverip = ipAddresses.getIpAddr();
            //update status
            statusService.upServer(serverip);
            //get slb by serverip
            List<Slb> slblist = slbClusterRepository.listByAppServerAndAppName(serverip,null);
            for (Slb slb : slblist)
            {
                String slbname = slb.getName();
                int ticket = buildInfoService.getTicket(slbname);
                if(buildService.build(slbname,ticket))
                {
                    nginxAgentService.loadAll(slbname);
                }
            }

        }
        return Response.ok().build();
    }

    @POST
    @Path("/downServers")
    public Response downServers(@Context HttpHeaders hh,String req) throws Exception {
        OpServerStatusReq downserverIps ;

        //parser req
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            downserverIps = DefaultSaxParser.parseEntity(OpServerStatusReq.class, req);
        } else {
            downserverIps = DefaultJsonParser.parse(OpServerStatusReq.class, req);
        }

        if (downserverIps == null) return Response.status(500).type(hh.getMediaType()).build();

        for (IpAddresses ipAddresses : downserverIps.getIpAddresseses())
        {
            String serverip = ipAddresses.getIpAddr();
            //update status
            statusService.downServer(serverip);

            //get slb by serverip
            List<Slb> slblist = slbClusterRepository.listByAppServerAndAppName(serverip,null);

            for (Slb slb : slblist) {
                String slbname = slb.getName();
                //get ticket
                int ticket = buildInfoService.getTicket(slbname);
                //build config
                if (buildService.build(slbname, ticket)) {
                    //push
                    nginxAgentService.loadAll(slbname);
                }
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path("/upMembers")
    public Response upMembers(@Context HttpHeaders hh,String req) throws Exception {
        OpMemberStatusReq upMemberInfo ;

        //parser req
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            upMemberInfo = DefaultSaxParser.parseEntity(OpMemberStatusReq.class, req);
        } else {
            upMemberInfo = DefaultJsonParser.parse(OpMemberStatusReq.class, req);
        }

        if (upMemberInfo == null) return Response.status(500).type(hh.getMediaType()).build();

        for (IpAppname tmp : upMemberInfo.getIpAppnames())
        {
            //update status
            statusService.upMember(tmp.getMemberAppname(),tmp.getMemberIp());

            //get slb by appname and ip
            List<Slb> slblist = slbClusterRepository.listByAppServerAndAppName(tmp.getMemberIp(),tmp.getMemberAppname());

            for (Slb slb : slblist) {
                String slbname = slb.getName();
                //get ticket
                int ticket = buildInfoService.getTicket(slbname);
                //build config
                if(buildService.build(slbname,ticket))
                {
                    //push
                    nginxAgentService.loadAll(slbname);
                }
            }

        }

        return Response.ok().build();
    }

    @POST
    @Path("/downMembers")
    public Response downMembers(@Context HttpHeaders hh,String req) throws Exception {
        OpMemberStatusReq downMemberInfo ;


        //parser req
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            downMemberInfo = DefaultSaxParser.parseEntity(OpMemberStatusReq.class, req);
        } else {
            downMemberInfo = DefaultJsonParser.parse(OpMemberStatusReq.class, req);
        }


        if (downMemberInfo == null) return Response.status(500).type(hh.getMediaType()).build();


        for (IpAppname tmp : downMemberInfo.getIpAppnames())
        {
            //update status
            statusService.downMember(tmp.getMemberAppname(), tmp.getMemberIp());

            //get slb by appname and ip
            List<Slb> slblist = slbClusterRepository.listByAppServerAndAppName(tmp.getMemberIp(),tmp.getMemberAppname());
            for (Slb slb : slblist) {
                String slbname = slb.getName();
                //get ticket
                int ticket = buildInfoService.getTicket(slbname);
                //build config
                if (buildService.build(slbname, ticket)) {
                    //push
                    nginxAgentService.loadAll(slbname);
                }
            }
        }

        return Response.ok().build();
    }



}

