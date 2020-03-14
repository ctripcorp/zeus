package com.ctrip.zeus.service.auth.impl;

import com.ctrip.zeus.auth.entity.Role;
import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.dao.entity.AuthUserPassword;
import com.ctrip.zeus.dao.entity.AuthUserPasswordExample;
import com.ctrip.zeus.dao.mapper.AuthUserPasswordMapper;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.UserLoginService;
import com.ctrip.zeus.service.auth.UserService;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

/**
 * @Discription
 **/
@Service("userLoginService")
public class UserLoginServiceImpl implements UserLoginService {

    private final DynamicIntProperty USER_PASSWORD_LENGTH = DynamicPropertyFactory.getInstance().getIntProperty("auth.user.password.length", 8);

    @Resource
    private AuthUserPasswordMapper authUserPasswordMapper;
    @Resource
    private UserService userService;

    @Override
    public List<AuthUserPassword> list() {
        return authUserPasswordMapper.selectByExample(new AuthUserPasswordExample());
    }

    @Override
    public AuthUserPassword login(String userName, String password) {
        return authUserPasswordMapper.selectOneByExample(new AuthUserPasswordExample().createCriteria().
                andUserNameEqualTo(userName).
                andPasswordEqualTo(password).
                example());
    }

    @Override
    public void signUp(String userName, String password) throws Exception {
        authUserPasswordMapper.insert(AuthUserPassword.builder().userName(userName).password(password).build());

        markSlbVisitor(userName);
    }

    @Override
    public String add(String userName) throws Exception {
        int passwordLen = USER_PASSWORD_LENGTH.get();
        String password = RandomStringUtils.random(passwordLen, true, true);

        String encodedPwd = encodePassword(password);
        assert encodedPwd != null;

        authUserPasswordMapper.insert(AuthUserPassword.builder().userName(userName).password(encodedPwd).build());

        markSlbVisitor(userName);

        return password;
    }

    @Override
    public boolean userExists(String userName) {
        AuthUserPassword record = authUserPasswordMapper.selectOneByExample(new AuthUserPasswordExample().createCriteria().andUserNameEqualTo(userName).example());
        return record != null;
    }

    @Override
    public void update(String userName, String password) {
        AuthUserPassword prototype = AuthUserPassword.builder().userName(userName).password(password).build();

        authUserPasswordMapper.updateByExampleSelective(prototype, new AuthUserPasswordExample().createCriteria().andUserNameEqualTo(userName).example());
    }

    @Override
    public String resetPassword(String userName) {
        String newPassword = RandomStringUtils.random(USER_PASSWORD_LENGTH.get(), true, true);

        String encoded = encodePassword(newPassword);
        assert encoded != null;

        AuthUserPassword prototype = AuthUserPassword.builder().userName(userName).password(encoded).build();
        authUserPasswordMapper.updateByExampleSelective(prototype, new AuthUserPasswordExample().createCriteria().andUserNameEqualTo(userName).example());
        return newPassword;
    }

    @Override
    public void deleteUser(String userName) {
        authUserPasswordMapper.deleteByExample(new AuthUserPasswordExample().createCriteria().andUserNameEqualTo(userName).example());
    }

    private String encodePassword(String password) {
        try {
            String temp1 = DigestUtils.md5Hex(password.getBytes());
            String temp2 = Base64.getEncoder().encodeToString(MessageDigest.getInstance("sha-256").digest(password.getBytes()));
            String temp = temp1 + temp2;
            return DigestUtils.md5Hex(temp.getBytes());
        } catch (NoSuchAlgorithmException e) {
            // ignore
        }
        return null;
    }

    @Override
    public Long queryByUserName(String userName) {
        AuthUserPassword record = authUserPasswordMapper.selectOneByExample(new AuthUserPasswordExample().createCriteria().andUserNameEqualTo(userName).example());
        return record == null ? null : record.getId();
    }

    private void markSlbVisitor(String userName) throws Exception {
        User user = new User();
        user.setUserName(userName);
        user.addRole(new Role().setRoleName(AuthDefaultValues.SLB_VISITOR_USER));
        userService.newUser(user);
    }
}
