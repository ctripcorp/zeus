package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.util.AuthTokenUtil;
import com.ctrip.zeus.auth.util.AuthUtil;
import com.ctrip.zeus.client.AbstractRestClient;
import com.ctrip.zeus.dao.entity.AuthUserPassword;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.*;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.util.AesEncryptionUtil;
import com.ctrip.zeus.util.ClientIpUtil;
import com.ctrip.zeus.util.UserUtils;
import com.google.common.base.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


/**
 * @Discription
 **/
@Component
@Path("/user")
public class UserResource {

    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private AuthService authService;
    @Resource
    private ConfigHandler configHandler;

    @Path("/list")
    @GET
    public Response list(@Context HttpHeaders hh, @Context HttpServletRequest request) throws Exception {
        return responseHandler.handle(userLoginService.list(), hh.getMediaType());
    }

    /*
     * Accept login request client(browser)
     * set token if login succeed
     * @Param userName: userName
     * @Param password: encoded password
     * @return: 200 response with cookie when login succeed or NotFoundException when login failed
     **/
    @Path("/login")
    @GET
    public Response login(@Context HttpHeaders hh, @Context HttpServletRequest request,
                          @QueryParam("userName") String userName,
                          @QueryParam("password") String password) throws Exception {
        if (Strings.isNullOrEmpty(userName) || Strings.isNullOrEmpty(password)) {
            throw new ValidationException("userName and password is needed to login");
        }


        Response apiResponse = sendLoginReqToApi(userName, password);
        if (apiResponse.getStatus() == 200) {

            String token = generateToken(userName, ClientIpUtil.getClientIP(request));
            String domain = configHandler.getStringValue("cookies.domain", request.getHeader("host").split(":")[0]);
            int cookieMaxAge = configHandler.getIntValue("token.cookies.max.age", 600);
            NewCookie cookie = new NewCookie(AuthUtil.AUTH_USER_TOKEN, token, "/", domain, null, cookieMaxAge, false);
            Map<String, String> result = new HashMap<>();
            result.put("message", "success");
            return Response.status(Response.Status.OK).entity(result).cookie(cookie).build();
        } else {
            throw new NotFoundException("Username or password is not matched");
        }
    }

    @Path("/logout")
    @GET
    public Response logout(@Context HttpHeaders hh, @Context HttpServletRequest request) {
        NewCookie cookie = new NewCookie(AuthUtil.AUTH_USER_TOKEN, null);
        Map<String, String> result = new HashMap<>();
        result.put("message", "success");
        return Response.status(Response.Status.OK).entity(result).cookie(cookie).build();
    }

    /*
     * @Description: called by portal server to check whether an record exists in db given username and password
     * @return
     **/
    @Path("/query")
    @GET
    public Response query(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("userName") String userName, @QueryParam("password") String password) throws Exception {
        AuthUserPassword record = null;
        if (userName != null && password != null) {
            record = userLoginService.login(userName, password);
        }

        return responseHandler.handle(record, hh.getMediaType());
    }

    private Response sendLoginReqToApi(String userName, String password) {
        UserApiClient client = new UserApiClient(configHandler.getStringValue("agent.api.host", "http://localhost"));
        return client.query(userName, password);
    }

    private String generateToken(String userName, String clientIp) {
        String src = userName + ";" + clientIp;
        return AesEncryptionUtil.getInstance().encrypt(src);
    }

    /*
     * @Description: sign up(register)   api
     * @return: 200 response when signup succeed, or exception when signup failed
     **/
    @Path("/signup")
    @GET
    public Response signUp(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("userName") String userName, @QueryParam("password") String password) throws Exception {
        if (Strings.isNullOrEmpty(userName) || Strings.isNullOrEmpty(password)) {
            throw new ValidationException("Username and password can not be null or empty");
        }
        if (userLoginService.userExists(userName)) {
            throw new RuntimeException("Username already exists. Try another one");
        }

        userLoginService.signUp(userName, password);

        return responseHandler.handle("succeed", hh.getMediaType());
    }

    /*
     * @Description: create user account
     * @return: random password
     **/
    @Path("/add")
    @GET
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("userName") String userName, @QueryParam("password") String password) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.User, AuthDefaultValues.ALL);// todo 是否是产生混淆？

        if (Strings.isNullOrEmpty(userName)) {
            throw new ValidationException("Username can not be null or empty");
        }
        if (userLoginService.userExists(userName)) {
            throw new ValidationException("Username existed already. Try another one");
        }

        if (password == null) {
            password = userLoginService.add(userName);
            return responseHandler.handle(password, hh.getMediaType());
        } else {
            userLoginService.signUp(userName, password);
            return responseHandler.handle("succeed", hh.getMediaType());
        }
    }

    /*
     * @Description: update user's password
     * @return
     **/
    @Path("/update")
    @GET
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("userName") String userName, @QueryParam("password") String password) throws Exception {
        if (Strings.isNullOrEmpty(userName) || Strings.isNullOrEmpty(password)) {
            throw new ValidationException("Username and password can not be null or empty");
        }
        Long id = userLoginService.queryByUserName(userName);
        if (id == null) {
            throw new RuntimeException("username " + userName + " not sign up yet. ");
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.User, id);

        userLoginService.update(userName, password);
        return responseHandler.handle("succeed", hh.getMediaType());
    }

    /*
     * @Description reset user's password
     * @return: reset not-encoded password
     **/
    @Path("/reset")
    @GET
    public Response resetPassword(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("userName") String userName) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.User, AuthDefaultValues.ALL);
        if (Strings.isNullOrEmpty(userName)) {
            throw new ValidationException("username must be provided");
        }

        if (!userLoginService.userExists(userName)) {
            throw new RuntimeException("user not sign up yet");
        }

        String newPassword = userLoginService.resetPassword(userName);

        return responseHandler.handle(newPassword, hh.getMediaType());
    }

    /*
     * @Description delete user
     * @return:
     **/
    @Path("/delete")
    @GET
    public Response deleteUser(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("userName") String userName) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DELETE, ResourceDataType.User, AuthDefaultValues.ALL);
        if (Strings.isNullOrEmpty(userName)) {
            throw new ValidationException("username must be provided");
        }

        if (!userLoginService.userExists(userName)) {
            throw new RuntimeException("user not sign up yet");
        }

        userLoginService.deleteUser(userName);

        return responseHandler.handle("succeed", hh.getMediaType());
    }

    private class UserApiClient extends AbstractRestClient {

        protected UserApiClient(String url) {
            super(url);
        }

        Response query(String userName, String password) {
            return getTarget().path("/api/user/query").
                    queryParam("userName", userName).
                    queryParam("password", password).
                    request().headers(AuthTokenUtil.getDefaultHeaders()).get();
        }
    }
}

