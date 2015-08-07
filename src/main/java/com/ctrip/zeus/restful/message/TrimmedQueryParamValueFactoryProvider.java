package com.ctrip.zeus.restful.message;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

/**
 * Created by zhoumy on 2015/8/7.
 */
public class TrimmedQueryParamValueFactoryProvider extends AbstractValueFactoryProvider {
    @Context
    private HttpServletRequest request;

    @Inject
    public TrimmedQueryParamValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator injector) {
        super(mpep, injector, Parameter.Source.UNKNOWN);
    }

    protected TrimmedQueryParamValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator, Parameter.Source... compatibleSources) {
        super(mpep, locator, compatibleSources);
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        Class<?> classType = parameter.getRawType();
        if (classType == null || (!classType.equals(String.class))) {
            return null;
        }
        return new TrimmedValueFactory(parameter);
    }

    private class TrimmedValueFactory extends AbstractContainerRequestValueFactory<String> {
        private final Parameter parameter;

        public TrimmedValueFactory(Parameter parameter) {
            this.parameter = parameter;
        }

        @Override
        public String provide() {
            TrimmedQueryParam annotation = parameter.getAnnotation(TrimmedQueryParam.class);
            if (annotation == null) {
                throw new IllegalStateException("parameter has no TrimmedQueryParam annotation");
            }
            String parameterValue = request.getParameter(annotation.value());
            if (parameterValue == null) {
                return parameterValue;
            }
            return parameterValue.trim();
        }
    }

    public static class TrimmedQueryParamInjectionResolver extends ParamInjectionResolver<TrimmedQueryParam> {

        public TrimmedQueryParamInjectionResolver(Class<? extends ValueFactoryProvider> valueFactoryProviderClass) {
            super(TrimmedQueryParamValueFactoryProvider.class);
        }
    }
}
