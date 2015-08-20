package com.ctrip.zeus.ao;

import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fanqq on 2015/3/30.
 */
public class ReqClient {

    private String host = null;
    private Client client = null;
    public static ReqClient reqClient = new ReqClient("");
    private static HashMap<String,String> requestNotes = new HashMap<>();

    public ReqClient(String host) {
        ClientConfig config = new ClientConfig();
        client = ClientBuilder.newClient(config);
        this.host = host;
    }

    public void setHost(String host){
        this.host = host;
    }
    public String getHost(){
        return  host;
    }

    private WebTarget getTarget(){
        return client.target(host);
    }
    private WebTarget getTarget(String path)
    {
        String api = path.split("\\?")[0];

        String tmp = requestNotes.get(api);
        if (tmp == null)
        {
            requestNotes.put(api,"test unmarked");
        }

        return client.target(host+path);
    }

    public boolean markPass(String apipath)
    {
        String api = apipath.split("\\?")[0];
        if (requestNotes.get(api)!=null)
        {
            requestNotes.put(api,"Pass");
            return true;
        }else {
            System.out.println("[Error] you can not mark an api which never be requested!");
            return false;
        }
    }
    public boolean markFail(String apipath)
    {
        String api = apipath.split("\\?")[0];
        if (requestNotes.get(api)!=null)
        {
            requestNotes.put(api,"Fail");
            return true;
        }else {
            System.out.println("[Error] you can not mark an api which never be requested!");
            return false;
        }
    }

    public String buildReport()
    {
        StringBuilder sb = new StringBuilder(10240);

        int size = requestNotes.size();
        int pass = 0 ;
        int fail = 0 ;
        int unmark = 0 ;

        String api = null;
        String value = null;

        sb.append("Api Test Report:(Total Test:").append(size).append(")\n");

        for (Map.Entry<String, String> a :requestNotes.entrySet())
        {
            api = a.getKey();
            value = a.getValue();
            sb.append("[TestCase] ").append(api).append("\t\t[").append(value).append("]\n");
            if (value.equals("Fail"))
            {
                fail++;
            }else if (value.equals("Pass"))
            {
                pass++;
            }else {
                unmark++;
            }
        }

        sb.append("\n[Total Tested] ").append(size).append("\t[Pass] ").append(pass)
                .append("\t[Fail] ").append(fail).append("\t[Unmark] ").append(unmark);

        System.out.println(sb.toString());
        return sb.toString();
    }

    public Response post(String path, String data) {
        Response res = getTarget(path).request()
                .post(Entity.entity(data,
                        MediaType.APPLICATION_JSON
                ));
        return res;
    }

    public Response get() {
        Response res = getTarget().request()
                .get();

        return res;
    }
    public Response get(String path )
    {
        return getTarget(path).request().get();
    }

    public String getstr() {
        String res = getTarget().request()
                .get(String.class);
        return res;
    }

    public String getstr(String path) {
        return getTarget(path).request().get(String.class);
    }

}
