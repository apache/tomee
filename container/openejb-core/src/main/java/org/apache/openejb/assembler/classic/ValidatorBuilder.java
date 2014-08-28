/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.assembler.classic;

import org.apache.openejb.jee.bval.DefaultValidatedExecutableTypesType;
import org.apache.openejb.jee.bval.ExecutableValidationType;
import org.apache.openejb.jee.bval.PropertyType;
import org.apache.openejb.jee.bval.ValidationConfigType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.validation.BootstrapConfiguration;
import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableType;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public final class ValidatorBuilder {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ValidatorBuilder.class);
    public static final String VALIDATION_PROVIDER_KEY = "openejb.bean-validation.provider";

    private ValidatorBuilder() {
        // no-op
    }

    public static ValidatorFactory buildFactory(final ClassLoader classLoader, final ValidationInfo info) {
        // now we will not be polluted by log build
        return buildFactory(info, classLoader);
    }

    public static ValidationInfo getInfo(final ValidationConfigType config) {
        final ValidationInfo info = new ValidationInfo();
        if (config != null) {
            info.version = config.getVersion();
            info.providerClassName = config.getDefaultProvider();
            info.constraintFactoryClass = config.getConstraintValidatorFactory();
            info.traversableResolverClass = config.getTraversableResolver();
            info.messageInterpolatorClass = config.getMessageInterpolator();
            info.parameterNameProviderClass = config.getParameterNameProvider();

            final ExecutableValidationType executableValidation = config.getExecutableValidation();
            if (executableValidation != null) {
                info.executableValidationEnabled = executableValidation.getEnabled();
                final DefaultValidatedExecutableTypesType executableTypes = executableValidation.getDefaultValidatedExecutableTypes();
                if (executableTypes != null) {
                    for (final ExecutableType type : executableTypes.getExecutableType()) {
                        info.validatedTypes.add(type.name());
                    }
                }
            }
            for (final PropertyType p : config.getProperty()) {
                info.propertyTypes.put(p.getName(), p.getValue());
            }
            for (final String element : config.getConstraintMapping()) {
                info.constraintMappings.add(element);
            }
        }
        return info;
    }

    public static ValidatorFactory buildFactory(final ValidationInfo config, final ClassLoader classLoader) {
        ValidatorFactory factory = null;
        final Thread thread = Thread.currentThread();
        final ClassLoader oldContextLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(classLoader);
            if (config == null) {
                factory = Validation.buildDefaultValidatorFactory();
            } else {
                final Configuration<?> configuration = getConfig(config);
                try {
                    factory = configuration.buildValidatorFactory();
                } catch (final ValidationException ve) {
                    thread.setContextClassLoader(ValidatorBuilder.class.getClassLoader());
                    factory = Validation.buildDefaultValidatorFactory();
                    thread.setContextClassLoader(classLoader);

                    logger.warning("Unable create validator factory with config " + config
                        + " (" + ve.getMessage() + ")."
                        + " Default factory will be used.");
                }
            }
        } finally {
            thread.setContextClassLoader(oldContextLoader);
        }
        return factory;
    }

    @SuppressWarnings("unchecked")
    private static Configuration<?> getConfig(final ValidationInfo info) {
        Configuration<?> target = null;
        final Thread thread = Thread.currentThread();
        final ClassLoader classLoader = thread.getContextClassLoader();

        String providerClassName = info.providerClassName;
        if (providerClassName == null) {
            providerClassName = SystemInstance.get().getOptions().get(VALIDATION_PROVIDER_KEY, (String) null);
        }

        if (providerClassName != null) {
            try {
                @SuppressWarnings({"unchecked", "rawtypes"}) final
                Class clazz = classLoader.loadClass(providerClassName);
                target = Validation.byProvider(clazz).configure();
                logger.info("Using " + providerClassName + " as validation provider.");
            } catch (final ClassNotFoundException e) {
                logger.warning("Unable to load provider class " + providerClassName, e);
            } catch (final ValidationException ve) {
                logger.warning("Unable create validator factory with provider " + providerClassName
                    + " (" + ve.getMessage() + ")."
                    + " Default one will be used.");
            }
        }
        if (target == null) {
            // force to use container provider to ignore any conflicting configuration
            thread.setContextClassLoader(ValidatorBuilder.class.getClassLoader());
            target = Validation.byDefaultProvider().configure();
            thread.setContextClassLoader(classLoader);
        }

        final Set<ExecutableType> types = new HashSet<>();
        for (final String type : info.validatedTypes) {
            types.add(ExecutableType.valueOf(type));
        }

        final Map<String, String> props = new HashMap<>();
        for (final Map.Entry<Object, Object> entry : info.propertyTypes.entrySet()) {
            final PropertyType property = new PropertyType();
            property.setName((String) entry.getKey());
            property.setValue((String) entry.getValue());

            props.put(property.getName(), property.getValue());
            if (logger.isDebugEnabled()) {
                logger.debug("Found property '" + property.getName() + "' with value '" + property.getValue());
            }
            target.addProperty(property.getName(), property.getValue());
        }

        final OpenEjbBootstrapConfig bootstrapConfig = new OpenEjbBootstrapConfig(
            providerClassName, info.constraintFactoryClass, info.messageInterpolatorClass, info.traversableResolverClass,
            info.parameterNameProviderClass, new HashSet<>(info.constraintMappings), info.executableValidationEnabled,
            types, props);
        final OpenEjbConfig config = new OpenEjbConfig(bootstrapConfig, target);

        target.ignoreXmlConfiguration();

        final String messageInterpolatorClass = info.messageInterpolatorClass;
        if (messageInterpolatorClass != null) {
            try {
                @SuppressWarnings("unchecked") final
                Class<MessageInterpolator> clazz = (Class<MessageInterpolator>) classLoader.loadClass(messageInterpolatorClass);
                target.messageInterpolator(newInstance(config, clazz));
            } catch (final Exception e) {
                logger.warning("Unable to set " + messageInterpolatorClass + " as message interpolator.", e);
            }
            logger.info("Using " + messageInterpolatorClass + " as message interpolator.");
        }
        final String traversableResolverClass = info.traversableResolverClass;
        if (traversableResolverClass != null) {
            try {
                @SuppressWarnings("unchecked") final
                Class<TraversableResolver> clazz = (Class<TraversableResolver>) classLoader.loadClass(traversableResolverClass);
                target.traversableResolver(newInstance(config, clazz));
            } catch (final Exception e) {
                logger.warning("Unable to set " + traversableResolverClass + " as traversable resolver.", e);
            }
            logger.info("Using " + traversableResolverClass + " as traversable resolver.");
        }
        final String constraintFactoryClass = info.constraintFactoryClass;
        if (constraintFactoryClass != null) {
            try {
                @SuppressWarnings("unchecked") final
                Class<ConstraintValidatorFactory> clazz = (Class<ConstraintValidatorFactory>) classLoader.loadClass(constraintFactoryClass);
                target.constraintValidatorFactory(newInstance(config, clazz));
            } catch (final Exception e) {
                logger.warning("Unable to set " + constraintFactoryClass + " as constraint factory.", e);
            }
            logger.info("Using " + constraintFactoryClass + " as constraint factory.");
        }
        for (final String mappingFileName : info.constraintMappings) {
            if (logger.isDebugEnabled()) {
                logger.debug("Opening input stream for " + mappingFileName);
            }
            final InputStream in = classLoader.getResourceAsStream(mappingFileName);
            if (in == null) {
                logger.warning("Unable to open input stream for mapping file " + mappingFileName + ". It will be ignored");
            } else {
                target.addMapping(in);
            }
        }
        if (info.parameterNameProviderClass != null) {
            try {
                final Class<ParameterNameProvider> clazz = (Class<ParameterNameProvider>) classLoader.loadClass(info.parameterNameProviderClass);
                target.parameterNameProvider(newInstance(config, clazz));
            } catch (final Exception e) {
                logger.warning("Unable to set " + info.parameterNameProviderClass + " as parameter name provider.", e);
            }
            logger.info("Using " + info.parameterNameProviderClass + " as parameter name provider.");
        }

        return config;
    }

    private static <T> T newInstance(final OpenEjbConfig config, final Class<T> clazz) throws Exception {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        if (webBeansContext == null) {
            return clazz.newInstance();
        }

        final BeanManagerImpl beanManager = webBeansContext.getBeanManagerImpl();
        if (!beanManager.isInUse()) {
            return clazz.newInstance();
        }

        final AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(clazz);
        final InjectionTarget<T> it = beanManager.createInjectionTarget(annotatedType);
        final CreationalContext<T> context = beanManager.createCreationalContext(null);
        final T instance = it.produce(context);
        it.inject(instance, context);
        it.postConstruct(instance);

        config.releasables.add(new Releasable<T>(context, it, instance));

        return instance;
    }

    private static final class OpenEjbBootstrapConfig implements BootstrapConfiguration, Serializable {
        private final String providerClassName;
        private final String constraintFactoryClass;
        private final String messageInterpolatorClass;
        private final String traversableResolverClass;
        private final String parameterNameProviderClass;
        private final Set<String> constraintMappings;
        private final boolean executableValidationEnabled;
        private final Set<ExecutableType> validatedTypes;
        private final Map<String, String> props;

        public OpenEjbBootstrapConfig(final String providerClassName,
                                      final String constraintFactoryClass,
                                      final String messageInterpolatorClass,
                                      final String traversableResolverClass,
                                      final String parameterNameProviderClass,
                                      final Set<String> constraintMappings,
                                      final boolean executableValidationEnabled,
                                      final Set<ExecutableType> validatedTypes,
                                      final Map<String, String> props) {
            this.providerClassName = providerClassName;
            this.constraintFactoryClass = constraintFactoryClass;
            this.messageInterpolatorClass = messageInterpolatorClass;
            this.traversableResolverClass = traversableResolverClass;
            this.parameterNameProviderClass = parameterNameProviderClass;
            this.constraintMappings = constraintMappings;
            this.executableValidationEnabled = executableValidationEnabled;
            this.validatedTypes = validatedTypes;
            this.props = props;
        }

        @Override
        public String getDefaultProviderClassName() {
            return providerClassName;
        }

        @Override
        public String getConstraintValidatorFactoryClassName() {
            return constraintFactoryClass;
        }

        @Override
        public String getMessageInterpolatorClassName() {
            return messageInterpolatorClass;
        }

        @Override
        public String getTraversableResolverClassName() {
            return traversableResolverClass;
        }

        @Override
        public String getParameterNameProviderClassName() {
            return parameterNameProviderClass;
        }

        @Override
        public Set<String> getConstraintMappingResourcePaths() {
            return constraintMappings;
        }

        @Override
        public boolean isExecutableValidationEnabled() {
            return executableValidationEnabled;
        }

        @Override
        public Set<ExecutableType> getDefaultValidatedExecutableTypes() {
            return validatedTypes;
        }

        @Override
        public Map<String, String> getProperties() {
            return props;
        }
    }

    private static final class OpenEjbConfig<T  extends Configuration<T>> implements Configuration<T> {
        private final Collection<Releasable<?>> releasables = new LinkedList<>();
        private final Configuration<T> delegate;
        private final BootstrapConfiguration bootstrap;

        public OpenEjbConfig(final BootstrapConfiguration bootstrapConfig, final Configuration<T> target) {
            bootstrap = bootstrapConfig;
            delegate = target;
        }

        @Override
        public T ignoreXmlConfiguration() {
            return delegate.ignoreXmlConfiguration();
        }

        @Override
        public T messageInterpolator(final MessageInterpolator interpolator) {
            return delegate.messageInterpolator(interpolator);
        }

        @Override
        public T traversableResolver(final TraversableResolver resolver) {
            return delegate.traversableResolver(resolver);
        }

        @Override
        public T constraintValidatorFactory(final ConstraintValidatorFactory constraintValidatorFactory) {
            return delegate.constraintValidatorFactory(constraintValidatorFactory);
        }

        @Override
        public T addMapping(final InputStream stream) {
            return delegate.addMapping(stream);
        }

        @Override
        public T addProperty(final String name, final String value) {
            return delegate.addProperty(name, value);
        }

        @Override
        public MessageInterpolator getDefaultMessageInterpolator() {
            return delegate.getDefaultMessageInterpolator();
        }

        @Override
        public TraversableResolver getDefaultTraversableResolver() {
            return delegate.getDefaultTraversableResolver();
        }

        @Override
        public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
            return delegate.getDefaultConstraintValidatorFactory();
        }

        @Override
        public ValidatorFactory buildValidatorFactory() {
            return new OpenEJBValidatorFactory(delegate.buildValidatorFactory(), releasables);
        }

        @Override
        public T parameterNameProvider(ParameterNameProvider parameterNameProvider) {
            return delegate.parameterNameProvider(parameterNameProvider);
        }

        @Override
        public ParameterNameProvider getDefaultParameterNameProvider() {
            return delegate.getDefaultParameterNameProvider();
        }

        @Override
        public BootstrapConfiguration getBootstrapConfiguration() {
            return bootstrap;
        }
    }

    private static final class OpenEJBValidatorFactory implements ValidatorFactory {
        private final ValidatorFactory delegate;
        private final Collection<Releasable<?>> toRelease;

        public OpenEJBValidatorFactory(final ValidatorFactory validatorFactory, final Collection<Releasable<?>> releasables) {
            delegate = validatorFactory;
            toRelease = releasables;
        }

        @Override
        public Validator getValidator() {
            return delegate.getValidator();
        }

        @Override
        public ValidatorContext usingContext() {
            return delegate.usingContext();
        }

        @Override
        public MessageInterpolator getMessageInterpolator() {
            return delegate.getMessageInterpolator();
        }

        @Override
        public TraversableResolver getTraversableResolver() {
            return delegate.getTraversableResolver();
        }

        @Override
        public ConstraintValidatorFactory getConstraintValidatorFactory() {
            return delegate.getConstraintValidatorFactory();
        }

        @Override
        public <T> T unwrap(final Class<T> type) {
            return delegate.unwrap(type);
        }

        @Override
        public ParameterNameProvider getParameterNameProvider() {
            return delegate.getParameterNameProvider();
        }

        @Override
        public void close() {
            delegate.close();
            for (final Releasable<?> r : toRelease) {
                r.release();
            }
        }
    }

    private static final class Releasable<T> {
        private final CreationalContext<T> context;
        private final InjectionTarget<T> injectionTarget;
        private final T instance;

        private Releasable(final CreationalContext<T> context, final InjectionTarget<T> injectionTarget, final T instance) {
            this.context = context;
            this.injectionTarget = injectionTarget;
            this.instance = instance;
        }

        private void release() {
            try {
                injectionTarget.preDestroy(instance);
                injectionTarget.dispose(instance);
                context.release();
            } catch (final Exception | NoClassDefFoundError e) {
                // no-op
            }
        }
    }
}
