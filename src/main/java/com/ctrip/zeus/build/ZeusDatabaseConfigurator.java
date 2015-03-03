package com.ctrip.zeus.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

final class ZeusDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();


      defineSimpleTableProviderComponents(all, "zeus", com.ctrip.zeus.dal.core._INDEX.getEntityClasses());
      defineDaoComponents(all, com.ctrip.zeus.dal.core._INDEX.getDaoClasses());

      return all;
   }
}
