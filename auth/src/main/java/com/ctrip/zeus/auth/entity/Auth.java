package com.ctrip.zeus.auth.entity;

import java.util.ArrayList;
import java.util.List;

public class Auth {
   private List<DataResource> m_dataResources = new ArrayList<DataResource>();

   private Role m_role;

   private User m_user;

   private List<ResourceList> m_resourceLists = new ArrayList<ResourceList>();

   private UserList m_userList;

   private RoleList m_roleList;

   private AuthServerKey m_authServerKey;

   public Auth() {
   }

   protected boolean equals(Object o1, Object o2) {
      if (o1 == null) {
         return o2 == null;
      } else if (o2 == null) {
         return false;
      } else {
         return o1.equals(o2);
      }
   }
   public Auth addDataResource(DataResource dataResource) {
      m_dataResources.add(dataResource);
      return this;
   }

   public Auth addResourceList(ResourceList resourceList) {
      m_resourceLists.add(resourceList);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Auth) {
         Auth _o = (Auth) obj;

         if (!equals(m_dataResources, _o.getDataResources())) {
            return false;
         }

         if (!equals(m_role, _o.getRole())) {
            return false;
         }

         if (!equals(m_user, _o.getUser())) {
            return false;
         }

         if (!equals(m_resourceLists, _o.getResourceLists())) {
            return false;
         }

         if (!equals(m_userList, _o.getUserList())) {
            return false;
         }

         if (!equals(m_roleList, _o.getRoleList())) {
            return false;
         }

         if (!equals(m_authServerKey, _o.getAuthServerKey())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public AuthServerKey getAuthServerKey() {
      return m_authServerKey;
   }

   public List<DataResource> getDataResources() {
      return m_dataResources;
   }

   public List<ResourceList> getResourceLists() {
      return m_resourceLists;
   }

   public Role getRole() {
      return m_role;
   }

   public RoleList getRoleList() {
      return m_roleList;
   }

   public User getUser() {
      return m_user;
   }

   public UserList getUserList() {
      return m_userList;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_dataResources == null ? 0 : m_dataResources.hashCode());
      hash = hash * 31 + (m_role == null ? 0 : m_role.hashCode());
      hash = hash * 31 + (m_user == null ? 0 : m_user.hashCode());
      hash = hash * 31 + (m_resourceLists == null ? 0 : m_resourceLists.hashCode());
      hash = hash * 31 + (m_userList == null ? 0 : m_userList.hashCode());
      hash = hash * 31 + (m_roleList == null ? 0 : m_roleList.hashCode());
      hash = hash * 31 + (m_authServerKey == null ? 0 : m_authServerKey.hashCode());

      return hash;
   }



   public Auth setAuthServerKey(AuthServerKey authServerKey) {
      m_authServerKey = authServerKey;
      return this;
   }

   public Auth setRole(Role role) {
      m_role = role;
      return this;
   }

   public Auth setRoleList(RoleList roleList) {
      m_roleList = roleList;
      return this;
   }

   public Auth setUser(User user) {
      m_user = user;
      return this;
   }

   public Auth setUserList(UserList userList) {
      m_userList = userList;
      return this;
   }

}
