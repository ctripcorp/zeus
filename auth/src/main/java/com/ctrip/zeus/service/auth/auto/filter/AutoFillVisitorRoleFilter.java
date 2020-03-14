package com.ctrip.zeus.service.auth.auto.filter;

import com.ctrip.zeus.auth.entity.Role;
import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.RoleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/8/25.
 */
@Service("autoFillVisitorRoleFilter")
public class AutoFillVisitorRoleFilter extends AutoFillAbstractFilter {
    @Resource
    RoleService roleService;

    @Override
    public int order() {
        return -100;
    }

    @Override
    public void runFilter(User user) throws Exception {
        Role role = roleService.getRole(AuthDefaultValues.SLB_VISITOR_USER);
        if (role == null) {
            throw new ValidationException("Not found role [slbVisitor].Please add slbVisitor role first.");
        }
        user.addRole(role);
    }

    @Override
    public boolean shouldFilter(User user) {
        for (Role role : user.getRoles()){
            if (role.getRoleName().equals(AuthDefaultValues.SLB_VISITOR_USER)){
                return false;
            }
        }
        return true;
    }
}
