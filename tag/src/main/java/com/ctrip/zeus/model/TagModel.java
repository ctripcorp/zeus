package com.ctrip.zeus.model;

public class TagModel {
   private TagList m_tagList;

   private PropertyList m_propertyList;

   private Property m_property;

   public TagModel() {
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
      if (obj instanceof TagModel) {
         TagModel _o = (TagModel) obj;

         if (!equals(m_tagList, _o.getTagList())) {
            return false;
         }

         if (!equals(m_propertyList, _o.getPropertyList())) {
            return false;
         }

         if (!equals(m_property, _o.getProperty())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Property getProperty() {
      return m_property;
   }

   public PropertyList getPropertyList() {
      return m_propertyList;
   }

   public TagList getTagList() {
      return m_tagList;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_tagList == null ? 0 : m_tagList.hashCode());
      hash = hash * 31 + (m_propertyList == null ? 0 : m_propertyList.hashCode());
      hash = hash * 31 + (m_property == null ? 0 : m_property.hashCode());

      return hash;
   }

   public TagModel setProperty(Property property) {
      m_property = property;
      return this;
   }

   public TagModel setPropertyList(PropertyList propertyList) {
      m_propertyList = propertyList;
      return this;
   }

   public TagModel setTagList(TagList tagList) {
      m_tagList = tagList;
      return this;
   }

}
