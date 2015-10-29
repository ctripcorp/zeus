package com.ctrip.zeus.server.config;

import static com.ctrip.zeus.restful.message.TrimmedQueryParamValueFactoryProvider.TrimmedQueryParamInjectionResolver;

import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.restful.message.TrimmedQueryParamValueFactoryProvider;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Singleton;
import javax.servlet.annotation.MultipartConfig;

/**
 * Created by zhoumy on 2015/8/7.
 */
public class SlbAdminResourceConfig extends ResourceConfig {
    public SlbAdminResourceConfig() {
        super(MultipartConfig.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(TrimmedQueryParamValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
                bind(TrimmedQueryParamInjectionResolver.class).to(new TypeLiteral<TrimmedQueryParam>() {
                }).in(Singleton.class);
            }
        });
        register(MultiPartFeature.class);
    }
}