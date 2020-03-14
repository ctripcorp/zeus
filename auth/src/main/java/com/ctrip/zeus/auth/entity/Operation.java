package com.ctrip.zeus.auth.entity;

public class Operation {
   private String m_type;

   public Operation() {
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
      if (obj instanceof Operation) {
         Operation _o = (Operation) obj;

         if (!equals(m_type, _o.getType())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public String getType() {
      return m_type;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());

      return hash;
   }


   public Operation setType(String type) {
      m_type = type;
      return this;
   }

}
