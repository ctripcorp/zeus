package com.ctrip.zeus.auth.entity;

import java.util.ArrayList;
import java.util.List;

public class UserResource  {
   private String m_type;

   private List<DataResource> m_dataResources = new ArrayList<DataResource>();

   public UserResource() {
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


   public UserResource addDataResource(DataResource dataResource) {
      m_dataResources.add(dataResource);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof UserResource) {
         UserResource _o = (UserResource) obj;

         if (!equals(m_type, _o.getType())) {
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

   public String getType() {
      return m_type;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
      hash = hash * 31 + (m_dataResources == null ? 0 : m_dataResources.hashCode());

      return hash;
   }


   public UserResource setType(String type) {
      m_type = type;
      return this;
   }

}
