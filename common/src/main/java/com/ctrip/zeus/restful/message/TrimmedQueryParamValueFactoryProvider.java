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
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

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

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        Class<?> classType = parameter.getRawType();
        if (classType == null) return null;
        if (classType.equals(String.class)) return new TrimmedValueFactory(parameter);

        if (Collection.class.isAssignableFrom(classType)) {
            if (parameter.getType() instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) parameter.getType();
                if (pt.getActualTypeArguments().length == 1) {
                    Class<?> argType = (Class<?>) pt.getActualTypeArguments()[0];
                    if (argType.equals(String.class)) {
                        if (classType.equals(java.util.List.class))
                            return new TrimmedValueCollectionFactory(parameter, ArrayList.class);
                        if (classType.equals(java.util.Set.class))
                            return new TrimmedValueCollectionFactory(parameter, HashSet.class);
                    }
                }
            }
        }
        return null;
    }

    private class TrimmedValueCollectionFactory extends AbstractContainerRequestValueFactory<Collection<String>> {
        private final Parameter parameter;
        private final Class<?> collectionType;

        public TrimmedValueCollectionFactory(Parameter parameter, Class collectionType) {
            this.parameter = parameter;
            this.collectionType = collectionType;
        }

        @Override
        public Collection<String> provide() {
            TrimmedQueryParam annotation = parameter.getAnnotation(TrimmedQueryParam.class);
            if (annotation == null) {
                throw new IllegalStateException("parameter has no TrimmedQueryParam annotation");
            }
            String[] pvs = request.getParameterValues(annotation.value());
            if (pvs == null) return null;

            Collection<String> col = null;
            try {
                col = (Collection<String>) collectionType.newInstance();

            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            }
            if (col == null) {
                col = new ArrayList<>();
            }
            for (String s : pvs) {
                col.add(s.trim());
            }
            return col;
        }
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
                return null;
            }
            return parameterValue.trim();
        }
    }

    public static class TrimmedQueryParamInjectionResolver extends ParamInjectionResolver<TrimmedQueryParam> {
        Class<? extends ValueFactoryProvider> valueFactoryProviderClass;

        public TrimmedQueryParamInjectionResolver(Class<? extends ValueFactoryProvider> valueFactoryProviderClass) {
            super(TrimmedQueryParamValueFactoryProvider.class);
            this.valueFactoryProviderClass = valueFactoryProviderClass;
        }
    }
}
