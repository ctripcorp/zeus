package com.ctrip.zeus.model.tools;

import java.util.ArrayList;
import java.util.List;

public class CertUpgradeList{
   private Integer m_total;

   private List<CertUpgrade> m_upgrades = new ArrayList<CertUpgrade>();

   public CertUpgradeList() {
   }

   public CertUpgradeList addCertUpgrade(CertUpgrade certUpgrade) {
      m_upgrades.add(certUpgrade);
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
      if (obj instanceof CertUpgradeList) {
         CertUpgradeList _o = (CertUpgradeList) obj;

         if (!equals(m_total, _o.getTotal())) {
            return false;
         }

         if (!equals(m_upgrades, _o.getUpgrades())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Integer getTotal() {
      return m_total;
   }

   public List<CertUpgrade> getUpgrades() {
      return m_upgrades;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
      hash = hash * 31 + (m_upgrades == null ? 0 : m_upgrades.hashCode());

      return hash;
   }
   public CertUpgradeList setTotal(Integer total) {
      m_total = total;
      return this;
   }

}
