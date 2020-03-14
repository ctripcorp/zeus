package com.ctrip.zeus.auth.entity;

import java.util.ArrayList;
import java.util.List;

public class RoleList {
   private Integer m_total;

   private List<Role> m_roles = new ArrayList<Role>();

   public RoleList() {
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
   public RoleList addRole(Role role) {
      m_roles.add(role);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof RoleList) {
         RoleList _o = (RoleList) obj;

         if (!equals(m_total, _o.getTotal())) {
            return false;
         }

         if (!equals(m_roles, _o.getRoles())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public List<Role> getRoles() {
      return m_roles;
   }

   public Integer getTotal() {
      return m_total;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
      hash = hash * 31 + (m_roles == null ? 0 : m_roles.hashCode());

      return hash;
   }


   public RoleList setTotal(Integer total) {
      m_total = total;
      return this;
   }

}
