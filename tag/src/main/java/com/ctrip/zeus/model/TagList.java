package com.ctrip.zeus.model;

import java.util.ArrayList;
import java.util.List;

public class TagList  {
   private Integer m_total;

   private List<String> m_tags = new ArrayList<String>();

   public TagList() {
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
   public TagList addTag(String tag) {
      m_tags.add(tag);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof TagList) {
         TagList _o = (TagList) obj;

         if (!equals(m_total, _o.getTotal())) {
            return false;
         }

         if (!equals(m_tags, _o.getTags())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public List<String> getTags() {
      return m_tags;
   }

   public Integer getTotal() {
      return m_total;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
      hash = hash * 31 + (m_tags == null ? 0 : m_tags.hashCode());

      return hash;
   }


   public TagList setTotal(Integer total) {
      m_total = total;
      return this;
   }

}
