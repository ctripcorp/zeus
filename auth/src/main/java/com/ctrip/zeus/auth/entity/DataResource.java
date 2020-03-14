package com.ctrip.zeus.auth.entity;

import java.util.ArrayList;
import java.util.List;

public class DataResource {
   private String m_resourceType;

   private String m_data;

   private List<Operation> m_operations = new ArrayList<Operation>();

   public DataResource() {
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
   public DataResource addOperation(Operation operation) {
      m_operations.add(operation);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof DataResource) {
         DataResource _o = (DataResource) obj;

         if (!equals(m_resourceType, _o.getResourceType())) {
            return false;
         }

         if (!equals(m_data, _o.getData())) {
            return false;
         }

         if (!equals(m_operations, _o.getOperations())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public String getData() {
      return m_data;
   }

   public List<Operation> getOperations() {
      return m_operations;
   }

   public String getResourceType() {
      return m_resourceType;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_resourceType == null ? 0 : m_resourceType.hashCode());
      hash = hash * 31 + (m_data == null ? 0 : m_data.hashCode());
      hash = hash * 31 + (m_operations == null ? 0 : m_operations.hashCode());

      return hash;
   }

   public DataResource setData(String data) {
      m_data = data;
      return this;
   }

   public DataResource setResourceType(String resourceType) {
      m_resourceType = resourceType;
      return this;
   }

}
