package com.ctrip.zeus.auth.entity;

import java.util.ArrayList;
import java.util.List;

public class User {
   private Long m_id;

   private String m_userName;

   private String m_chineseName;

   private String m_email;

   private String m_bu;

   private List<Role> m_roles = new ArrayList<Role>();

   private List<DataResource> m_dataResources = new ArrayList<DataResource>();

   public User() {
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

   public User addDataResource(DataResource dataResource) {
      m_dataResources.add(dataResource);
      return this;
   }

   public User addRole(Role role) {
      m_roles.add(role);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof User) {
         User _o = (User) obj;

         if (!equals(m_id, _o.getId())) {
            return false;
         }

         if (!equals(m_userName, _o.getUserName())) {
            return false;
         }

         if (!equals(m_chineseName, _o.getChineseName())) {
            return false;
         }

         if (!equals(m_email, _o.getEmail())) {
            return false;
         }

         if (!equals(m_bu, _o.getBu())) {
            return false;
         }

         if (!equals(m_roles, _o.getRoles())) {
            return false;
         }

         if (!equals(m_dataResources, _o.getDataResources())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public String getBu() {
      return m_bu;
   }

   public String getChineseName() {
      return m_chineseName;
   }

   public List<DataResource> getDataResources() {
      return m_dataResources;
   }

   public String getEmail() {
      return m_email;
   }

   public Long getId() {
      return m_id;
   }

   public List<Role> getRoles() {
      return m_roles;
   }

   public String getUserName() {
      return m_userName;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
      hash = hash * 31 + (m_userName == null ? 0 : m_userName.hashCode());
      hash = hash * 31 + (m_chineseName == null ? 0 : m_chineseName.hashCode());
      hash = hash * 31 + (m_email == null ? 0 : m_email.hashCode());
      hash = hash * 31 + (m_bu == null ? 0 : m_bu.hashCode());
      hash = hash * 31 + (m_roles == null ? 0 : m_roles.hashCode());
      hash = hash * 31 + (m_dataResources == null ? 0 : m_dataResources.hashCode());

      return hash;
   }


   public User setBu(String bu) {
      m_bu = bu;
      return this;
   }

   public User setChineseName(String chineseName) {
      m_chineseName = chineseName;
      return this;
   }

   public User setEmail(String email) {
      m_email = email;
      return this;
   }

   public User setId(Long id) {
      m_id = id;
      return this;
   }

   public User setUserName(String userName) {
      m_userName = userName;
      return this;
   }

}
