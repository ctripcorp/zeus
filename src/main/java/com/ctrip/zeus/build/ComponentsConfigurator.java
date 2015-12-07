package com.ctrip.zeus.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.DataSourceProvider;
import org.unidal.dal.jdbc.datasource.DefaultDataSourceProvider;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;
import org.unidal.lookup.configuration.Configuration;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
    @Override
    public List<Component> defineComponents() {
        List<Component> all = new ArrayList<Component>();
        all.add(C(DataSourceProvider.class, DefaultDataSourceProvider.class)
                .config(new Configuration[]{
                        E("datasourceFile", new String[0]).value("datasources.xml"),
                        E("baseDirRef", new String[0]).value("CONF_DIR")
                }));
        all.addAll(new ZeusDatabaseConfigurator().defineComponents());
        return all;
    }

    public static void main(String[] args) {
        generatePlexusComponentsXmlFile(new ComponentsConfigurator());
    }
}
