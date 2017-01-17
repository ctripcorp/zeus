package com.ctrip.zeus.service.model;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.TrafficPolicyValidator;
import com.ctrip.zeus.util.AssertUtils;
import org.junit.Assert;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2017/1/16.
 */
public class TrafficPolicyTest extends AbstractServerTest {
    @Resource
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;

    @Resource
    private TrafficPolicyValidator trafficPolicyValidator;
    @Resource
    private GroupValidator groupModelValidator;
    @Resource
    private TrafficPolicyDao trafficPolicyDao;
    @Resource
    private RTrafficPolicyVsDao rTrafficPolicyVsDao;
    @Resource
    private RTrafficPolicyGroupDao rTrafficPolicyGroupDao;

    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;

    @Test
    public void testTrafficPolicyValidation() throws DalException {
        TrafficPolicy object = new TrafficPolicy().setVersion(1)
                .addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(new VirtualServer().setId(1L)).setPath("~* ^/v1($|/|\\?)").setPriority(1200))
                .addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(new VirtualServer().setId(2L)).setPath("~* ^/v2($|/|\\?)").setPriority(1200))
                .addTrafficControl(new TrafficControl().setGroup(new Group().setId(1L)).setWeight(50))
                .addTrafficControl(new TrafficControl().setGroup(new Group().setId(2L)).setWeight(50));

        rGroupStatusDao.insert(new RelGroupStatusDo().setGroupId(1).setOfflineVersion(1).setOnlineVersion(1));
        rGroupStatusDao.insert(new RelGroupStatusDo().setGroupId(2).setOfflineVersion(2).setOnlineVersion(1));
        rGroupStatusDao.insert(new RelGroupStatusDo().setGroupId(3).setOfflineVersion(1));

        rGroupVsDao.insert(new RelGroupVsDo().setGroupId(1).setVsId(1).setGroupVersion(1).setPath("~* ^/v1($|/|\\?)").setPriority(1000));
        rGroupVsDao.insert(new RelGroupVsDo().setGroupId(1).setVsId(2).setGroupVersion(1).setPath("~* ^/v2($|/|\\?)").setPriority(1000));

        rGroupVsDao.insert(new RelGroupVsDo().setGroupId(2).setVsId(1).setGroupVersion(1).setPath("~* ^/v1($|/|\\?)").setPriority(1100));
        rGroupVsDao.insert(new RelGroupVsDo().setGroupId(2).setVsId(2).setGroupVersion(1).setPath("~* ^/v2($|/|\\?)").setPriority(1100));
        rGroupVsDao.insert(new RelGroupVsDo().setGroupId(2).setVsId(1).setGroupVersion(2).setPath("~* ^/v1($|/|\\?)").setPriority(1100));

        /********* case 1 *********/
        assertValidationFailed(object, "vs-traffic-control(2-2) combination cannot be found");

        /********* case 2 *********/
        RelGroupVsDo d0 = new RelGroupVsDo().setGroupId(2).setVsId(2).setGroupVersion(2).setPath("~* ^/v22($|/|\\?)").setPriority(1100);
        rGroupVsDao.insert(d0);
        assertValidationFailed(object, "traffic-control path is not equivalent");

        /********* case 3 *********/
        d0.setPath("~* ^/v2($|/|\\?)").setPriority(1500);
        rGroupVsDao.update(d0, RGroupVsEntity.UPDATESET_FULL);
        assertValidationFailed(object, "priority of traffic-control is higher");

        /********* reset *********/
        d0.setPriority(1100);
        rGroupVsDao.update(d0, RGroupVsEntity.UPDATESET_FULL);

        /********* case 4 *********/
        TrafficPolicyDo d1 = new TrafficPolicyDo().setVersion(1).setNxActiveVersion(1);
        trafficPolicyDao.insert(d1);
        RTrafficPolicyGroupDo d2 = new RTrafficPolicyGroupDo().setGroupId(1L).setPolicyId(d1.getId()).setPolicyVersion(1).setWeight(50);
        rTrafficPolicyGroupDao.insert(d2);
        RTrafficPolicyVsDo d3 = new RTrafficPolicyVsDo().setVsId(1).setPolicyId(d1.getId()).setPolicyVersion(1).setPath("/").setPriority(1000);
        rTrafficPolicyVsDao.insert(d3);
        assertValidationFailed(object, "vs-traffic-control combination is not unique");

        /********* reset *********/
        trafficPolicyDao.deleteById(d1);
        rTrafficPolicyGroupDao.deleteByPK(d2);
        rTrafficPolicyVsDao.deleteByPK(d3);

        /********* case 5 *********/
        RelGroupVsDo d4 = new RelGroupVsDo().setGroupId(3).setVsId(1).setGroupVersion(1).setPath("~* ^/v1/v2($|/|\\?)").setPriority(1000);
        rGroupVsDao.insert(d4);
        assertValidationFailed(object, "path overlap is found");

        /********* reset *********/
        rGroupVsDao.delete(d4);

        /********* case 6 *********/
        try {
            trafficPolicyValidator.validate(object, false);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGroupValidationAfterUsedAsTrafficControl() throws DalException {
        SlbVirtualServerDo v1 = new SlbVirtualServerDo().setSlbId(1).setName("v1").setPort("8080");
        slbVirtualServerDao.insert(v1);
        SlbVirtualServerDo v2 = new SlbVirtualServerDo().setSlbId(1).setName("v2").setPort("8080");
        slbVirtualServerDao.insert(v2);

        rGroupStatusDao.insert(new RelGroupStatusDo().setGroupId(11).setOfflineVersion(1).setOnlineVersion(1));
        rGroupStatusDao.insert(new RelGroupStatusDo().setGroupId(12).setOfflineVersion(1).setOnlineVersion(1));
        rGroupVsDao.insert(new RelGroupVsDo().setGroupId(12).setGroupVersion(1).setVsId(v1.getId()).setPath("~* ^/v11($|/|\\?)").setPriority(1000));
        rGroupVsDao.insert(new RelGroupVsDo().setGroupId(12).setGroupVersion(1).setVsId(v2.getId()).setPath("~* ^/v12($|/|\\?)").setPriority(1000));

        TrafficPolicyDo d0 = new TrafficPolicyDo().setVersion(1).setNxActiveVersion(1);
        trafficPolicyDao.insert(d0);
        rTrafficPolicyVsDao.insert(new RTrafficPolicyVsDo().setPolicyId(d0.getId()).setPolicyVersion(1).setVsId(v1.getId()).setPath("~* ^/v11($|/|\\?)").setPriority(1100));
        rTrafficPolicyGroupDao.insert(new RTrafficPolicyGroupDo().setPolicyId(d0.getId()).setPolicyVersion(1).setGroupId(11).setWeight(50));
        rTrafficPolicyGroupDao.insert(new RTrafficPolicyGroupDo().setPolicyId(d0.getId()).setPolicyVersion(1).setGroupId(12).setWeight(50));

        Group object = new Group().setId(11L).setName("name").setAppId("999999")
                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(new VirtualServer().setId(v1.getId())).setPath("~* ^/v11($|/|\\?)").setPriority(1200));

        /********* case 1 *********/
        assertValidationFailed(object, "group priority is higher than its traffic policy");

        /********* case 2 *********/
        object.getGroupVirtualServers().get(0).setPath("~* ^/v12($|/|\\?)").setPriority(1000);
        assertValidationFailed(object, "path is different");

        /********* reset *********/
        object.getGroupVirtualServers().get(0).setPath("~* ^/v11($|/|\\?)").setPriority(1000);

        /********* case 3 *********/
        rTrafficPolicyVsDao.insert(new RTrafficPolicyVsDo().setPolicyId(d0.getId()).setPolicyVersion(1).setVsId(v2.getId()).setPath("~* ^/v12($|/|\\?)").setPriority(1100));
        assertValidationFailed(object, "vs-group-combination " + v2.getId() + " is missing");

        /********* reset *********/
        object.addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(new VirtualServer().setId(v2.getId())).setPath("~* ^/v12($|/|\\?)").setPriority(1000));

        /********* case 4 *********/
        assertValidationFailed(object, "path is totally equivalent to another group");

        /********* case 5 *********/
        TrafficPolicyDo d1 = new TrafficPolicyDo().setVersion(1).setNxActiveVersion(1);
        trafficPolicyDao.insert(d1);
        RTrafficPolicyVsDo d2 = new RTrafficPolicyVsDo().setPolicyId(d1.getId()).setPolicyVersion(1).setVsId(v1.getId()).setPath("~* ^/v13/v12($|/|\\?)").setPriority(1000);
        rTrafficPolicyVsDao.insert(d2);
        Group object1 = new Group().setName("name").setAppId("999999")
                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(new VirtualServer().setId(v1.getId())).setPath("~* ^/v13($|/|\\?)").setPriority(1000));
        assertValidationFailed(object1, "path overlap is found");

        /********* reset *********/
        trafficPolicyDao.deleteByPK(d1);
        rTrafficPolicyVsDao.deleteByPK(d2);

        /********* case 6 *********/
        try {
            groupModelValidator.validateGroupVirtualServers(object1.getId(), object1.getGroupVirtualServers(), false);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    private void assertValidationFailed(Group object, String message) {
        try {
            groupModelValidator.validateGroupVirtualServers(object.getId(), object.getGroupVirtualServers(), false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println("Expected: " + message + ", Actual: " + e.getMessage());
            if (!(e instanceof ValidationException)) {
                e.printStackTrace();
                Assert.assertTrue(false);
            }
        }
    }

    private void assertValidationFailed(TrafficPolicy object, String message) {
        try {
            trafficPolicyValidator.validate(object);
            Assert.assertTrue(message, false);
        } catch (Exception e) {
            System.out.println("Expected: " + message + ", Actual: " + e.getMessage());
            if (!(e instanceof ValidationException)) {
                e.printStackTrace();
                Assert.assertTrue(false);
            }
        }
    }

}
