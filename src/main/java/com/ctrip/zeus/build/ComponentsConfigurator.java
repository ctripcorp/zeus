package com.ctrip.zeus.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      // move following line to top-level project if necessary
		all.add(C(JdbcDataSourceDescriptorManager.class) //
				.config(E("datasourceFile").value("datasources.xml"), //
						E("baseDirRef").value("CONF_DIR")));

      all.addAll(new ZeusDatabaseConfigurator().defineComponents());

      return all;
   }

   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }
}
