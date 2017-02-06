package com.ctrip.zeus.service.model;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.common.ValidationContext;
import org.junit.Assert;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private ValidationFacade validationFacade;
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
        TrafficPolicy object = new TrafficPolicy().setName("name").setVersion(1)
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
        TrafficPolicyDo d1 = new TrafficPolicyDo().setName("d1").setVersion(1).setNxActiveVersion(1);
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
        ValidationContext context = new ValidationContext();
        validationFacade.validatePolicy(object, context);
        if (context.getErrors().size() > 0) {
            for (Map.Entry<String, String> r : context.getErrors().entrySet()) {
                System.out.printf("%-10s : %s\n", r.getKey(), r.getValue());
            }
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

        TrafficPolicyDo d0 = new TrafficPolicyDo().setName("d0").setVersion(1).setNxActiveVersion(1);
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
        TrafficPolicyDo d1 = new TrafficPolicyDo().setName("d1").setVersion(1).setNxActiveVersion(1);
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
        ValidationContext context = new ValidationContext();
        validationFacade.validateGroup(object1, context);
        if (context.getErrors().size() > 0) {
            for (Map.Entry<String, String> r : context.getErrors().entrySet()) {
                System.out.printf("%-10s : %s\n", r.getKey(), r.getValue());
            }
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testValidationForMergedData() {
        Map<Long, Group> groupRef = new HashMap<>();
        Map<Long, TrafficPolicy> policyRef = new HashMap<>();
        groupRef.put(1L, new Group().setId(1L).addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(new VirtualServer().setId(1L)).setPath("~* ^/path").setPriority(1000)));
        groupRef.put(2L, new Group().setId(2L).addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(new VirtualServer().setId(1L)).setPath("~* ^/path").setPriority(1000)));
        groupRef.put(3L, new Group().setId(3L).addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(new VirtualServer().setId(1L)).setPath("~* ^/path/overlap").setPriority(1100)));

        policyRef.put(1L, new TrafficPolicy().setId(1L).addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(new VirtualServer().setId(1L)).setPath("~* ^/path").setPriority(1050))
                .addTrafficControl(new TrafficControl().setGroup(new Group().setId(1L)))
                .addTrafficControl(new TrafficControl().setGroup(new Group().setId(2L))));
        policyRef.put(2L, new TrafficPolicy().setId(2L).addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(new VirtualServer().setId(1L)).setPath("~* ^/path").setPriority(1050))
                .addTrafficControl(new TrafficControl().setGroup(new Group().setId(1L)))
                .addTrafficControl(new TrafficControl().setGroup(new Group().setId(2L))));

        assertValidationFailed(1L, new HashSet<>(groupRef.values()), new HashSet<>(policyRef.values()), "vs-traffic-control combination is not unique");
        policyRef.remove(2L);

        ValidationContext context = new ValidationContext();
        validationFacade.validateEntriesOnVs(1L, new HashSet<>(groupRef.values()), new HashSet<>(policyRef.values()), context);
        if (context.getErrors().size() > 0) {
            if (context.getErrors().size() > 0) {
                for (Map.Entry<String, String> r : context.getErrors().entrySet()) {
                    System.out.printf("%s-%s\n", r.getKey(), r.getValue());
                }
            }
            System.out.print("\n");
            Assert.assertTrue(false);
        }

        groupRef.remove(2L);
        assertValidationFailed(1L, new HashSet<>(groupRef.values()), new HashSet<>(policyRef.values()), "missing group on vs");
    }

    @Test
    public void testRepository() throws DalException {
        rGroupStatusDao.insert(new RelGroupStatusDo().setGroupId(21).setOfflineVersion(1).setOnlineVersion(1));
        rGroupStatusDao.insert(new RelGroupStatusDo().setGroupId(22).setOfflineVersion(2).setOnlineVersion(1));

        rGroupVsDao.insert(new RelGroupVsDo().setGroupId(21).setVsId(1).setGroupVersion(1).setPath("~* ^/v21($|/|\\?)").setPriority(1000));
        rGroupVsDao.insert(new RelGroupVsDo().setGroupId(22).setVsId(2).setGroupVersion(1).setPath("~* ^/v21($|/|\\?)").setPriority(1000));
        rGroupVsDao.insert(new RelGroupVsDo().setGroupId(22).setVsId(2).setGroupVersion(2).setPath("~* ^/v21($|/|\\?)").setPriority(1000));

        TrafficPolicy object = new TrafficPolicy().setName("name").setVersion(1)
                .addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(new VirtualServer().setId(1L)).setPath("~* ^/v21($|/|\\?)").setPriority(1200))
                .addTrafficControl(new TrafficControl().setGroup(new Group().setId(11L)).setWeight(50))
                .addTrafficControl(new TrafficControl().setGroup(new Group().setId(22L)).setWeight(50));
        try {
            object = trafficPolicyRepository.add(object);
            TrafficPolicy copy = trafficPolicyRepository.getById(object.getId());
            assertEquals(object, copy);

            object = trafficPolicyRepository.update(object);
            Assert.assertEquals(1, trafficPolicyRepository.list().size());
            assertEquals(object, trafficPolicyRepository.getById(object.getId()));
            assertEquals(copy, trafficPolicyRepository.getByKey(new IdVersion(object.getId(), copy.getVersion())));

            trafficPolicyRepository.delete(object.getId());
            Assert.assertNull(trafficPolicyRepository.getById(object.getId()));
            Assert.assertNull(trafficPolicyDao.findById(object.getId(), TrafficPolicyEntity.READSET_FULL));
            Assert.assertEquals(0, rTrafficPolicyVsDao.findByPolicy(object.getId(), object.getVersion(), RTrafficPolicyVsEntity.READSET_FULL).size());
            Assert.assertEquals(0, rTrafficPolicyGroupDao.findByPolicy(object.getId(), object.getVersion(), RTrafficPolicyGroupEntity.READSET_FULL).size());
        } catch (Exception e) {
        }

    }

    private void assertValidationFailed(Long vsId, Set<Group> groups, Set<TrafficPolicy> policies, String message) {
        ValidationContext context = new ValidationContext();
        validationFacade.validateEntriesOnVs(vsId, groups, policies, context);
        if (context.getErrors().size() == 0) {
            Assert.assertTrue(false);
        }
        System.out.print("Expected: " + message + ", Actual: ");
        if (context.getErrors().size() > 0) {
            for (Map.Entry<String, String> r : context.getErrors().entrySet()) {
                System.out.printf("%s-%s\n", r.getKey(), r.getValue());
            }
        }
        System.out.print("\n");
    }

    private void assertValidationFailed(Group object, String message) {
        ValidationContext context = new ValidationContext();
        validationFacade.validateGroup(object, context);
        if (context.getErrors().size() == 0) {
            Assert.assertTrue(false);
        }
        System.out.print("Expected: " + message + ", Actual: ");
        if (context.getErrors().size() > 0) {
            for (Map.Entry<String, String> r : context.getErrors().entrySet()) {
                System.out.printf("%s-%s\n", r.getKey(), r.getValue());
            }
        }
        System.out.print("\n");
    }

    private void assertValidationFailed(TrafficPolicy object, String message) {
        ValidationContext context = new ValidationContext();
        validationFacade.validatePolicy(object, context);
        if (context.getErrors().size() == 0) {
            Assert.assertTrue(false);
        }
        System.out.print("Expected: " + message + ", Actual: ");
        if (context.getErrors().size() > 0) {
            for (Map.Entry<String, String> r : context.getErrors().entrySet()) {
                System.out.printf("%s-%s\n", r.getKey(), r.getValue());
            }
        }
        System.out.print("\n");
    }

    private void assertEquals(TrafficPolicy o1, TrafficPolicy o2) {
        Assert.assertEquals(o1.getId(), o2.getId());
        Assert.assertEquals(o1.getPolicyVirtualServers().size(), o2.getPolicyVirtualServers().size());
        Assert.assertEquals(o1.getControls().size(), o2.getControls().size());
        Assert.assertEquals(o1.getVersion(), o2.getVersion());

        boolean result = true;
        for (int i = 0; i < o1.getPolicyVirtualServers().size(); i++) {
            PolicyVirtualServer v1 = o1.getPolicyVirtualServers().get(i);
            PolicyVirtualServer v2 = o2.getPolicyVirtualServers().get(i);
            result &= (v1.getPath().equals(v2.getPath()));
            result &= (v1.getPriority().equals(v2.getPriority()));
            result &= (v1.getVirtualServer().getId().equals(v2.getVirtualServer().getId()));
        }

        for (int i = 0; i < o1.getControls().size(); i++) {
            TrafficControl c1 = o1.getControls().get(i);
            TrafficControl c2 = o2.getControls().get(i);
            result &= (c1.getGroup().getId().equals(c2.getGroup().getId()));
            result &= (c1.getWeight().equals(c2.getWeight()));
        }
        Assert.assertTrue(result);
    }
}
