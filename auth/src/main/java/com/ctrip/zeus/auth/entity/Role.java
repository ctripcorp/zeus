package com.ctrip.zeus.auth.entity;

import java.util.ArrayList;
import java.util.List;

public class Role {
   private Long m_id;

   private String m_roleName;

   private String m_discription;

   private List<DataResource> m_dataResources = new ArrayList<DataResource>();

   public Role() {
   }

   public Role addDataResource(DataResource dataResource) {
      m_dataResources.add(dataResource);
      return this;
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

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Role) {
         Role _o = (Role) obj;

         if (!equals(m_id, _o.getId())) {
            return false;
         }

         if (!equals(m_roleName, _o.getRoleName())) {
            return false;
         }

         if (!equals(m_discription, _o.getDiscription())) {
            return false;
         }

         if (!equals(m_dataResources, _o.getDataResources())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public List<DataResource> getDataResources() {
      return m_dataResources;
   }

   public String getDiscription() {
      return m_discription;
   }

   public Long getId() {
      return m_id;
   }

   public String getRoleName() {
      return m_roleName;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
      hash = hash * 31 + (m_roleName == null ? 0 : m_roleName.hashCode());
      hash = hash * 31 + (m_discription == null ? 0 : m_discription.hashCode());
      hash = hash * 31 + (m_dataResources == null ? 0 : m_dataResources.hashCode());

      return hash;
   }



   public Role setDiscription(String discription) {
      m_discription = discription;
      return this;
   }

   public Role setId(Long id) {
      m_id = id;
      return this;
   }

   public Role setRoleName(String roleName) {
      m_roleName = roleName;
      return this;
   }

}
