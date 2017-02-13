package com.ctrip.zeus.util;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.entity.VirtualServerConfResponse;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.GroupView;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

/**
 * Created by zhoumy on 2017/2/13.
 */
public class ObjectJsonWriterTest {
    @Test
    public void testEmptyString() throws JsonProcessingException {
        VirtualServerConfResponse r = new VirtualServerConfResponse();
        r.setServerConf("").setUpstreamConf("").setVersion(1).setVirtualServerId(1L);
        System.out.println(ObjectJsonWriter.write(r));
    }

    @Test
    public void testNonEmptyArrayString() throws JsonProcessingException, ValidationException {
        VirtualServer vs = new VirtualServer().setSlbId(1L).addDomain(new Domain().setName("test.domain.com"));
        vs.getSlbIds().add(1L);
        vs.getSlbIds().add(2L);
        GroupView view = new ExtendedView.ExtendedGroup(new Group().addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(vs)));
        System.out.println(ObjectJsonWriter.write(view, "detail"));
    }
}
