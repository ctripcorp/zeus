package com.ctrip.zeus.auth.entity;

import java.util.ArrayList;
import java.util.List;

public class UserList {
   private Integer m_total;

   private List<User> m_users = new ArrayList<User>();

   public UserList() {
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
   public UserList addUser(User user) {
      m_users.add(user);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof UserList) {
         UserList _o = (UserList) obj;

         if (!equals(m_total, _o.getTotal())) {
            return false;
         }

         if (!equals(m_users, _o.getUsers())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Integer getTotal() {
      return m_total;
   }

   public List<User> getUsers() {
      return m_users;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
      hash = hash * 31 + (m_users == null ? 0 : m_users.hashCode());

      return hash;
   }



   public UserList setTotal(Integer total) {
      m_total = total;
      return this;
   }

}
