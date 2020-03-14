package com.ctrip.zeus.service.verify;

import com.ctrip.zeus.model.model.AppList;
import com.ctrip.zeus.restful.message.view.GroupListView;
import com.ctrip.zeus.restful.message.view.SlbListView;
import com.ctrip.zeus.restful.message.view.TrafficPolicyListView;
import com.ctrip.zeus.restful.message.view.VsListView;

/**
 * @Discription
 **/
public interface VerifyContext {
    AppList getApps();

    com.ctrip.zeus.auth.entity.UserList getUsers();

    TrafficPolicyListView getPolicies();

    SlbListView getSlbs();

    VsListView getVses();

    GroupListView getGroups();
}
