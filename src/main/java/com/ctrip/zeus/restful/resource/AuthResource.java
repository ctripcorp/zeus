package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.auth.entity.*;
import com.ctrip.zeus.auth.transform.DefaultJsonParser;
import com.ctrip.zeus.auth.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthorizationService;
import com.ctrip.zeus.util.AssertUtils;
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
    @Resource
    private ResponseHandler responseHandler;

    @GET
    @Path("/roles")
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
    @Path("/resources")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name="getAuth")
    public Response allResources(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        List<com.ctrip.zeus.auth.entity.Resource> resources = authService.getAllResources();
        ResourceList roleList = new ResourceList();
        for (com.ctrip.zeus.auth.entity.Resource resource : resources) {
            roleList.addResource(resource);
        }
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(ResourceList.XML, roleList)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(ResourceList.JSON, roleList)).type(MediaType.APPLICATION_JSON).build();
        }

    }

    @GET
    @Path("/users")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name="getAuth")
    public Response allUsers(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
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
    @Path("/user")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name="getAuth")
    public Response userInfo(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("userName") String userName) throws Exception {
        User user = authService.getUser(userName);
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(User.XML, user)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(User.JSON, user)).type(MediaType.APPLICATION_JSON).build();
        }

    }

    @POST
    @Path("/user/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name="modifyAuth")
    public Response addUser(@Context HttpServletRequest request, @Context HttpHeaders hh, String user) throws Exception {
        User newUser;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            newUser = DefaultSaxParser.parseEntity(User.class, user);
        } else {
            newUser = DefaultJsonParser.parse(User.class, user);
        }
        authService.addUser(newUser);
        return Response.ok().build();
    }

    @POST
    @Path("/user/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name="modifyAuth")
    public Response updateUser(@Context HttpServletRequest request, @Context HttpHeaders hh, String userStr) throws Exception {
        User user;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            user = DefaultSaxParser.parseEntity(User.class, userStr);
        } else {
            user = DefaultJsonParser.parse(User.class, userStr);
        }
        authService.updateUser(user);
        return Response.ok().build();
    }
    @GET
    @Path("/user/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name="modifyAuth")
    public Response deleteUser(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("userName")String userName) throws Exception {
        authService.deleteUser(userName);
        return Response.ok().build();
    }



    @POST
    @Path("/resource/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name="modifyAuth")
    public Response addResource(@Context HttpServletRequest request, @Context HttpHeaders hh, String resourceStr) throws Exception {
        com.ctrip.zeus.auth.entity.Resource resource;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            resource = DefaultSaxParser.parseEntity(com.ctrip.zeus.auth.entity.Resource.class, resourceStr);
        } else {
            resource = DefaultJsonParser.parse(com.ctrip.zeus.auth.entity.Resource.class, resourceStr);
        }
        authService.addResource(resource);
        return Response.ok().build();
    }
    @POST
    @Path("/resource/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name="modifyAuth")
    public Response updateResource(@Context HttpServletRequest request, @Context HttpHeaders hh, String resourceStr) throws Exception {
        com.ctrip.zeus.auth.entity.Resource resource;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            resource = DefaultSaxParser.parseEntity(com.ctrip.zeus.auth.entity.Resource.class, resourceStr);
        } else {
            resource = DefaultJsonParser.parse(com.ctrip.zeus.auth.entity.Resource.class, resourceStr);
        }
        authService.updateResource(resource);
        return Response.ok().build();
    }
    @GET
    @Path("/resource/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name="modifyAuth")
    public Response deleteResource(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("resourceName")String resourceName) throws Exception {
        authService.deleteResource(resourceName);
        return Response.ok().build();
    }

    @POST
    @Path("/role/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name="modifyAuth")
    public Response addRole(@Context HttpServletRequest request, @Context HttpHeaders hh, String roleStr) throws Exception {
        Role role;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            role = DefaultSaxParser.parseEntity(Role.class, roleStr);
        } else {
            role = DefaultJsonParser.parse(Role.class, roleStr);
        }
        authService.addRole(role);
        return Response.ok().build();
    }

    @POST
    @Path("/role/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name="modifyAuth")
    public Response updateRole(@Context HttpServletRequest request, @Context HttpHeaders hh, String roleStr) throws Exception {
        Role role;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            role = DefaultSaxParser.parseEntity(Role.class, roleStr);
        } else {
            role = DefaultJsonParser.parse(Role.class, roleStr);
        }
        authService.updateRole(role);
        return Response.ok().build();
    }
    @GET
    @Path("/role/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name="modifyAuth")
    public Response deleteRole(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("roleName")String roleName) throws Exception {
        authService.deleteRole(roleName);
        return Response.ok().build();
    }

}
