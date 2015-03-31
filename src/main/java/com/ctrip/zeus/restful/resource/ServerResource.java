package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxAgentService;
import com.ctrip.zeus.service.status.StatusService;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
@Path("/server")
public class ServerResource {

    @Resource
    StatusService statusService;
    @Resource
    private BuildService buildService;
    @Resource
    private BuildInfoService buildInfoService;
    @Resource
    private NginxAgentService nginxAgentService;
    @Resource
    private SlbRepository slbClusterRepository;

    @POST
    @Path("/upServer")
    public Response upServer(@Context HttpHeaders hh,String req) throws Exception {

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
                    nginxAgentService.reloadConf(slbname);
                }
            }

        }
        return Response.ok().build();
    }

    @POST
    @Path("/downServer")
    public Response downServer(@Context HttpHeaders hh,String req) throws Exception {
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
                    nginxAgentService.reloadConf(slbname);
                }
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path("/upMember")
    public Response upMember(@Context HttpHeaders hh,String req) throws Exception {
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
                    nginxAgentService.reloadConf(slbname);
                }
            }

        }

        return Response.ok().build();
    }

    @POST
    @Path("/downMember")
    public Response downMember(@Context HttpHeaders hh,String req) throws Exception {
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
                    nginxAgentService.reloadConf(slbname);
                }
            }
        }

        return Response.ok().build();
    }

    @GET
    @Path("/info")
    public Response info(@Context HttpHeaders hh) throws IOException, SAXException {
        MemberAction a = new MemberAction().setAppName("app001")
                .addIp("192.168.1.1")
                .addIp("192.168.1.2");

        ServerAction a2 = new ServerAction()
                .addIp("192.168.1.1")
                .addIp("192.168.1.2");

        return Response.ok(String.format(MemberAction.JSON, a) + "\n\n\n" + String.format(ServerAction.JSON, a2)).build();
    }

}

