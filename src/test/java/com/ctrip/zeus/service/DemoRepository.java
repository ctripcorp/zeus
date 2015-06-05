package com.ctrip.zeus.service;

import com.ctrip.zeus.dal.core.GroupDao;
import com.ctrip.zeus.dal.core.GroupDo;
import com.ctrip.zeus.dal.core.GroupEntity;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
@Repository
public class DemoRepository {

    @Resource
    private GroupDao groupDao;

    public void addGroup(String name) {
        try {
            groupDao.insert(new GroupDo().setAppId("test").setName(name));
        } catch (DalException e) {
            e.printStackTrace();
        }
    }

    public void addGroupError(String name) throws Exception {
        try {
            groupDao.insert(new GroupDo().setAppId("test").setName(name));
        } catch (DalException e) {
            e.printStackTrace();
        }

        throw new Exception("rollback");
    }

    public GroupDo getGroup(String name) {
        try {
            return groupDao.findByName(name, GroupEntity.READSET_FULL);
        } catch (DalException e) {
            e.printStackTrace();
        }
        return new GroupDo();
    }

    public void deleteGroup(GroupDo groupDo) {
        try {
            groupDao.deleteByPK(groupDo);
        } catch (DalException e) {
            e.printStackTrace();
        }
    }
}
