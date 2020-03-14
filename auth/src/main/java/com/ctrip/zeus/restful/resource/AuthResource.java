package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.entity.*;
import com.ctrip.zeus.auth.entity.UserResource;
import com.ctrip.zeus.auth.util.AuthUtil;
import com.ctrip.zeus.exceptions.BadRequestException;
import com.ctrip.zeus.exceptions.ForbiddenException;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.*;
import com.ctrip.zeus.util.ObjectJsonParser;
import com.ctrip.zeus.util.UserUtils;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author:fanqq
 * @date: 4/16/2015.
 */
@Component
@Path("/")
public class AuthResource {

    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private UserAuthCache userAuthCache;
    @Resource
    private AuthService authService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    DynamicStringProperty casServerUrlPrefix = DynamicPropertyFactory.getInstance().getStringProperty("server.sso.casServer.url.prefix", "");
    DynamicStringProperty serverName = DynamicPropertyFactory.getInstance().getStringProperty("server.sso.server.name", "");

    @GET
    @Path("/auth/roles")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response allRoles(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);

        List<Role> roles = roleService.getRoles();
        RoleList roleList = new RoleList();
        roleList.getRoles().addAll(roles);
        roleList.setTotal(roles.size());
        return responseHandler.handle(roleList, hh.getMediaType());
    }

    @GET
    @Path("/auth/role")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRole(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("roleName") String name, @QueryParam("roleId") Long id) throws Exception {
        Role role = null;
        if (id == null && name == null) {
            throw new BadRequestException("Bad Request Query Param.");
        }
        if (id != null) {
            role = roleService.getRole(id);
        }
        if (role == null && name != null) {
            role = roleService.getRole(name);
        }
        if (role == null) {
            throw new NotFoundException("Role Not Found.Id: " + id + " name: " + name);
        }
        return responseHandler.handle(role, hh.getMediaType());
    }

    @POST
    @Path("/auth/role/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response newRole(@Context HttpServletRequest request, @Context HttpHeaders hh, String roleStr) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);
        Role role;
        role = ObjectJsonParser.parse(roleStr, Role.class);
        if (role == null) {
            throw new BadRequestException("Invalidate role data.");
        }
        dataResourcesValidate(role.getDataResources());
        Role result = roleService.newRole(role);
        return responseHandler.handle(result, hh.getMediaType());
    }

    @POST
    @Path("/auth/role/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response updateRole(@Context HttpServletRequest request, @Context HttpHeaders hh, String roleStr) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);
        Role role;
        role = ObjectJsonParser.parse(roleStr, Role.class);
        if (role == null) {
            throw new BadRequestException("Invalidate role data.");
        }
        dataResourcesValidate(role.getDataResources());
        Role result = roleService.updateRole(role);
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/auth/role/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response deleteRole(@Context HttpServletRequest request, @Context HttpHeaders hh,
                               @QueryParam("roleName") String roleName, @QueryParam("roleId") Long rid) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);
        if (rid == null && roleName == null) {
            throw new BadRequestException("dBad Request Query Param.At least one param is needed.");
        }
        if (rid != null) {
            roleService.deleteRole(rid);
        } else {
            roleService.deleteRole(roleName);
        }
        return responseHandler.handle("{\"status\":\"success\"}", hh.getMediaType());
    }


    @GET
    @Path("/auth/users")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response allUsers(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("type") String type) throws Exception {
        List<User> users;
        if (type != null && type.equals("detail")) {
            users = userService.getUsers();
        } else {
            users = userService.getUsersSimpleInfo();
        }
        UserList userList = new UserList();
        userList.getUsers().addAll(users);
        userList.setTotal(users.size());
        return responseHandler.handle(userList, hh.getMediaType());
    }


    @GET
    @Path("/auth/user")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response userInfo(@Context HttpServletRequest request, @Context HttpHeaders hh,
                             @QueryParam("userName") String userName, @QueryParam("userId") Long userId) throws Exception {
        if (userId == null && userName == null) {
            throw new BadRequestException("dBad Request Query Param.At least one param is needed.");
        }
        User user = null;
        if (userId != null) {
            user = userService.getUser(userId);
        }
        if (user == null && userName != null) {
            user = userService.getUser(userName);
        }
        if (user == null) {
            throw new NotFoundException("User Not Found.Id: " + userId + " name: " + userName);
        }
        return responseHandler.handle(user, hh.getMediaType());
    }

    // This api shouldn't be used anymore
    // Call /api/user/add instead.
    // See UserResource.add method for detail.
    @POST
    @Path("/auth/user/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response addUser(@Context HttpServletRequest request, @Context HttpHeaders hh, String user) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);
        User newUser;
        newUser = ObjectJsonParser.parse(user, User.class);

        if (newUser == null) {
            throw new BadRequestException("Invalidate User data.");
        }
        newUser = userService.newUser(newUser);
        return responseHandler.handle(newUser, hh.getMediaType());
    }

    @POST
    @Path("/auth/user/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response updateUser(@Context HttpServletRequest request, @Context HttpHeaders hh, String userStr) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);
        User user;
        user = ObjectJsonParser.parse(userStr, User.class);
        if (user == null) {
            throw new BadRequestException("Invalidate User data.");
        }
        dataResourcesValidate(user.getDataResources());
        user = userService.updateUser(user);
        return responseHandler.handle(user, hh.getMediaType());
    }

    @GET
    @Path("/auth/user/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response deleteUser(@Context HttpServletRequest request, @Context HttpHeaders hh,
                               @QueryParam("userName") String userName, @QueryParam("userId") Long userId) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);
        if (userId == null && userName == null) {
            throw new BadRequestException("dBad Request Query Param.At least one param is needed.");
        }
        if (userId != null) {
            userService.deleteUser(userId);
        } else {
            userService.deleteUser(userName);
        }
        return responseHandler.handle("{\"status\":\"success\"}", hh.getMediaType());
    }

    @GET
    @Path("/auth/user/resources")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response getUserResources(@Context HttpServletRequest request, @Context HttpHeaders hh,
                               @QueryParam("fromCache") Boolean cache) throws Exception {
        String userName = UserUtils.getUserName(request);
        if (userName == null) {
            logger.warn("Fail to get user name from assertion value");
            return null;
        }
        Map<String, Map<String, DataResource>> map = null;
        if (cache != null && cache == true) {
            map = userAuthCache.getAuthResource(userName);
        } else {
            map = userService.getAuthResourcesByUserName(userName);
        }
        if (map == null) {
            throw new NotFoundException("Not found any resource for user. userName: " + userName);
        }
        ResourceList resourceList = new ResourceList();
        int total = 0;
        for (String type : map.keySet()) {
            com.ctrip.zeus.auth.entity.UserResource u = new UserResource();
            u.setType(type);
            u.getDataResources().addAll(map.get(type).values());
            resourceList.addUserResource(u);
            total += u.getDataResources().size();
        }
        resourceList.setTotal(total);
        return responseHandler.handle(resourceList, hh.getMediaType());
    }

    @GET
    @Path("/auth/current/user")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response deleteUser(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        CasUserInfo userInfo = new CasUserInfo();

        final HttpSession session = request.getSession(false);
        Assertion assertion = session != null ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : null;
        String userName = null;
        if (assertion != null && assertion.getPrincipal().getAttributes().size() > 0) {
            userInfo.setChineseName(assertion.getPrincipal().getAttributes().get("sn").toString());
            userInfo.setCity(assertion.getPrincipal().getAttributes().get("city").toString());
            userInfo.setMail(assertion.getPrincipal().getAttributes().get("mail").toString());
            userInfo.setName(assertion.getPrincipal().getAttributes().get("name").toString());
            userInfo.setCompany(assertion.getPrincipal().getAttributes().get("company").toString());
            userInfo.setEmployee(assertion.getPrincipal().getAttributes().get("employee").toString());
            userInfo.setMemberOf(assertion.getPrincipal().getAttributes().get("memberOf").toString());
            userInfo.setDisplayName(assertion.getPrincipal().getAttributes().get("displayName").toString());
            userInfo.setDistinguishedName(assertion.getPrincipal().getAttributes().get("distinguishedName").toString());
            userInfo.setDepartment(assertion.getPrincipal().getAttributes().get("department").toString());
        } else {
            userName = request.getAttribute(AuthUtil.AUTH_USER) == null ? null : request.getAttribute(AuthUtil.AUTH_USER).toString();
            userInfo.setName(userName);
            userInfo.setChineseName(userName);
            userInfo.setDisplayName(userName);
            userInfo.setDistinguishedName(userName);
        }
        if (userInfo.getName() == null) {
            throw new ForbiddenException("Unknown User.");
        }
        return responseHandler.handle(userInfo, hh.getMediaType());
    }

    @GET
    @Path("/auth/user/logout")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response logout(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        URI uri = new URI(String.format("%s/logout?service=%s", casServerUrlPrefix.get(),
                serverName.get()));
        NewCookie cookie = new NewCookie("memCacheAssertionID", null, request.getContextPath() + "/", null, null, 0, false);
        NewCookie signOffCookie = new NewCookie(AuthUtil.AUTH_USER_TOKEN, null);

        return Response.status(Response.Status.FOUND).location(uri).cookie(cookie).cookie(signOffCookie).build();
    }

    private void dataResourcesValidate(List<DataResource> resources) throws ValidationException {
        for (DataResource d : resources) {
            for (Operation ops : d.getOperations()) {
                if (!ResourceOperationType.contain(ops.getType())) {
                    throw new ValidationException("Ops Type is undefined. Ops:" + ops.getType() + " \nAll Ops Types: " + ResourceOperationType.getNames());
                }
            }
        }
    }

    @GET
    @Path("/auth/toggle/ops")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response toggleOpsRole(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                  @QueryParam("name") String name, @QueryParam("ops") Boolean ops) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);
        if (name == null || ops == null) {
            throw new ValidationException("Name and ops param is needed.");
        }
        User user;
        user = userService.getUser(name);
        boolean newUser = false;
        if (user == null) {
            user = new User();
            user.setUserName(name);
            newUser = true;
        }
        if (ops == true) {
            user.getRoles().clear();
            Role role = roleService.getRole("ops");
            user.addRole(role);
        } else {
            user.getRoles().clear();
            Role role = roleService.getRole("slbVisitor");
            user.addRole(role);
        }
        if (newUser) {
            userService.newUser(user);
        } else {
            userService.updateUser(user);
        }
        return responseHandler.handle("success", hh.getMediaType());
    }


}
