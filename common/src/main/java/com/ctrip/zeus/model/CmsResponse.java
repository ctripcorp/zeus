package com.ctrip.zeus.model;

public class CmsResponse {
   private Boolean m_success;

   private String m_message;

   private Result m_result;

   public CmsResponse() {
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof CmsResponse) {
         CmsResponse _o = (CmsResponse) obj;

         if (!equals(m_success, _o.getSuccess())) {
            return false;
         }

         if (!equals(m_message, _o.getMessage())) {
            return false;
         }

         if (!equals(m_result, _o.getResult())) {
            return false;
         }


         return true;
      }

      return false;
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

   public String getMessage() {
      return m_message;
   }


   public Boolean getSuccess() {
      return m_success;
   }


   public Result getResult() {
      return m_result;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_success == null ? 0 : m_success.hashCode());
      hash = hash * 31 + (m_message == null ? 0 : m_message.hashCode());
      hash = hash * 31 + (m_result == null ? 0 : m_result.hashCode());

      return hash;
   }

   public boolean isSuccess() {
      return m_success != null && m_success.booleanValue();
   }

   public CmsResponse setMessage(String message) {
      m_message = message;
      return this;
   }

   public CmsResponse setResult(Result result) {
      m_result = result;
      return this;
   }

   public CmsResponse setSuccess(Boolean success) {
      m_success = success;
      return this;
   }

}
