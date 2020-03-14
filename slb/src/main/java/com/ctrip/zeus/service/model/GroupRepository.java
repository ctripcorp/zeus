package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.service.Repository;
import com.ctrip.zeus.service.model.impl.RepositoryContext;

import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface GroupRepository extends Repository {

    List<Group> list(Long[] ids) throws Exception;

    List<Group> list(IdVersion[] keys) throws Exception;

    List<Group> list(IdVersion[] keys, RepositoryContext repositoryContext) throws Exception;

    Group getById(Long id) throws Exception;

    Group getByKey(IdVersion key) throws Exception;

    Group getByKey(IdVersion key, RepositoryContext repositoryContext) throws Exception;

    Group add(Group group, boolean escapedPathValidation) throws Exception;

    /**
     * group: group to be added
     * gids: group ids which need escape path validate with added group
     * **/
    Group add(Group group, Set<Long> gids) throws Exception;

    Group add(Group group) throws Exception;

    Group addVGroup(Group group) throws Exception;

    Group addVGroup(Group group, boolean escapedPathValidation) throws Exception;

    /**
     * group: group to be updated
     * purpose: update group called by rule repository
     * **/
    Group updateGroupRules(Group group) throws Exception;

    Group update(Group group) throws Exception;

    Group update(Group group, boolean escapedPathValidation) throws Exception;

    Group update(Group group, boolean escapedPathValidation, boolean escapedDependencyValidation) throws Exception;

    Group updateVGroup(Group group) throws Exception;

    Group updateVGroup(Group group, boolean escapedPathValidation) throws Exception;

    int delete(Long groupId) throws Exception;

    int deleteVGroup(Long groupId) throws Exception;

    void updateStatus(IdVersion[] groups, SelectionMode state) throws Exception;

    void updateStatus(IdVersion[] groups) throws Exception;

    void updateCanaryStatus(IdVersion[] groups) throws Exception;

    @Deprecated
    Group get(String groupName) throws Exception;
}