package com.ctrip.zeus.support;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.lookup.ContainerLoader;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
public class DaoFactory {
    public <T> T getDao(Class<T> clazz) throws ComponentLookupException {
        T lookup = ContainerLoader.getDefaultContainer().lookup(clazz);
        return lookup;
    }
}
