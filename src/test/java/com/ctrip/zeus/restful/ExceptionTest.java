package com.ctrip.zeus.restful;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.client.GroupClient;
import com.ctrip.zeus.client.SlbClient;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.restful.response.entity.ErrorMessage;
import com.ctrip.zeus.restful.response.transform.DefaultJsonParser;
import com.ctrip.zeus.util.IOUtils;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.io.*;

/**
 * Created by zhoumy on 2015/4/2.
 */
public class ExceptionTest extends AbstractServerTest {
    @Resource
    private GroupDao appDao;
    @Resource
    private SlbDao slbDao;

    @Test
    public void testDalNotFoundException() throws DalException {
        Assert.assertNull(appDao.findByName("notExistGroup", GroupEntity.READSET_FULL));
        Assert.assertNull(slbDao.findByName("notExistSlb", SlbEntity.READSET_FULL));
    }

    @Test
    public void testExceptionInterceptor() throws Exception {
        GroupClient ac = new GroupClient("http://127.0.0.1:8099");
        Response appResponse = ac.add(new Group());
        Assert.assertEquals(400, appResponse.getStatus());

        String appString = IOUtils.inputStreamStringify((InputStream) appResponse.getEntity());
        ErrorMessage aem = DefaultJsonParser.parse(ErrorMessage.class, appString);
        printErrorMessage(aem);
        Assert.assertEquals(ValidationException.class.getSimpleName(), aem.getCode());

        SlbClient sc = new SlbClient("http://127.0.0.1:8099");
        Response slbResponse = sc.add(new Slb());
        Assert.assertEquals(400, slbResponse.getStatus());

        String slbString = IOUtils.inputStreamStringify((InputStream) slbResponse.getEntity());
        ErrorMessage sem = DefaultJsonParser.parse(ErrorMessage.class, slbString);
        printErrorMessage(sem);
        Assert.assertEquals(ValidationException.class.getSimpleName(), sem.getCode());
    }

    private static void printErrorMessage(ErrorMessage em) {
        System.out.println("*************** Error message ***************");
        System.out.println("code:\n" + em.getCode());
        System.out.println("message:\n" + em.getMessage());
        System.out.println("*********************************************");
    }
}
