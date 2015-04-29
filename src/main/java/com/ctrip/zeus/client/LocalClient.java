package com.ctrip.zeus.client;

import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by fanqq on 2015/4/28.
 */
public class LocalClient extends AbstractRestClient {
    private static DynamicIntProperty dyupsPort = DynamicPropertyFactory.getInstance().getIntProperty("dyups.port", 8081);
    private static LocalClient localClient = null;

    public LocalClient(String url) {
        super(url);
    }

    public LocalClient(){
        super("http://127.0.0.1:"+dyupsPort.get());
    }

    public static LocalClient getLocalClient(){
        if (localClient==null)
        {
            localClient=new LocalClient();
        }
        return localClient;
    }

    public synchronized NginxResponse dyups(String upsName ,String upsCommands)throws IOException {
        Response responseStr = getTarget().path("/upstream/"+upsName).request().post(Entity.entity(upsCommands,
                MediaType.APPLICATION_JSON
        ));
        if (responseStr.getStatus()==200)
        {
            return new NginxResponse().setSucceed(true).setOutMsg(responseStr.getEntity().toString());
        }else {
            return new NginxResponse().setSucceed(false).setErrMsg(responseStr.getEntity().toString());
        }
    }
}
