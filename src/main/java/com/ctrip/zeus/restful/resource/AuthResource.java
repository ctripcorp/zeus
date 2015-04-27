package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.auth.entity.Role;
import com.ctrip.zeus.auth.entity.RoleList;
import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.auth.entity.UserList;
import com.ctrip.zeus.auth.transform.DefaultJsonParser;
import com.ctrip.zeus.auth.transform.DefaultSaxParser;
import com.ctrip.zeus.service.auth.AuthorizationService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author:mag
 * @date: 4/16/2015.
 */
@Component
@Path("/auth")
public class AuthResource {

    @Resource
    private AuthorizationService authService;

    @GET
    @Path("/role")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name="getAuth")
    public Response allRoles(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        List<Role> roles = authService.getAllRoles();
        RoleList roleList = new RoleList();
        for (Role role : roles) {
            roleList.addRole(role);
        }
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(RoleList.XML, roleList)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(RoleList.JSON, roleList)).type(MediaType.APPLICATION_JSON).build();
        }

    }

    @GET
    @Path("/user")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response allUsers(@Context HttpHeaders hh) throws Exception {
        List<User> users = authService.getAllUsers();
        UserList userList = new UserList();
        for (User user : users) {
            userList.addUser(user);
        }
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(UserList.XML, userList)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(UserList.JSON, userList)).type(MediaType.APPLICATION_JSON).build();
        }
    }


    @GET
    @Path("/user/{userName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response userInfo(@Context HttpHeaders hh, @PathParam("userName") String userName) throws Exception {
        User user = authService.getUser(userName);
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(User.XML, user)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(User.JSON, user)).type(MediaType.APPLICATION_JSON).build();
        }

    }

    @POST
    @Path("/user")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response addUser(@Context HttpHeaders hh, String user) throws Exception {
        User newUser;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            newUser = DefaultSaxParser.parseEntity(User.class, user);
        } else if (hh.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
            newUser = DefaultJsonParser.parse(User.class, user);
        } else {
            throw new Exception("Unacceptable type.");
        }
        authService.addUser(newUser);
        return Response.ok().build();
    }

    @POST
    @Path("/user/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response updateUser(@Context HttpHeaders hh, String userStr) throws Exception {
        User user;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            user = DefaultSaxParser.parseEntity(User.class, userStr);
        } else if (hh.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
            user = DefaultJsonParser.parse(User.class, userStr);
        } else {
            throw new Exception("Unacceptable type.");
        }
        authService.updateUser(user);
        return Response.ok().build();
    }

    @POST
    @Path("/resource")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response addResource(@Context HttpHeaders hh, String resourceStr) throws Exception {
        com.ctrip.zeus.auth.entity.Resource resource;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            resource = DefaultSaxParser.parseEntity(com.ctrip.zeus.auth.entity.Resource.class, resourceStr);
        } else if (hh.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
            resource = DefaultJsonParser.parse(com.ctrip.zeus.auth.entity.Resource.class, resourceStr);
        } else {
            throw new Exception("Unacceptable type.");
        }
        return Response.ok().build();
    }

    @POST
    @Path("/role")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response addRole(@Context HttpHeaders hh, String roleStr) throws Exception {
        Role role;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            role = DefaultSaxParser.parseEntity(Role.class, roleStr);
        } else if (hh.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
            role = DefaultJsonParser.parse(Role.class, roleStr);
        } else {
            throw new Exception("Unacceptable type.");
        }
        authService.addRole(role);
        return Response.ok().build();
    }

}
