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

import org.apache.openejb.jee.bval.PropertyType;
import org.apache.openejb.jee.bval.ValidationConfigType;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;
import javax.xml.bind.JAXBElement;
import java.io.InputStream;
import java.util.Map;

public final class ValidatorBuilder {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ValidatorBuilder.class);
    public static final String VALIDATION_PROVIDER_KEY = "openejb.bean-validation.provider";

    private ValidatorBuilder() {
        // no-op
    }

    public static ValidatorFactory buildFactory(ClassLoader classLoader, ValidationInfo info) {
        return buildFactory(info, classLoader);
    }

    public static ValidationInfo getInfo(ValidationConfigType config) {
        ValidationInfo info = new ValidationInfo();
        if (config != null) {
            info.providerClassName = config.getDefaultProvider();
            info.constraintFactoryClass = config.getConstraintValidatorFactory();
            info.traversableResolverClass = config.getTraversableResolver();
            info.messageInterpolatorClass = config.getMessageInterpolator();
            for (PropertyType p : config.getProperty()) {
                info.propertyTypes.put(p.getName(), p.getValue());
            }
            for (JAXBElement<String> element : config.getConstraintMapping()) {
                info.constraintMappings.add(element.getValue());
            }
        }
        return info;
    }

    public static ValidatorFactory buildFactory(ValidationInfo config, ClassLoader classLoader) {
        ValidatorFactory factory = null;
        ClassLoader oldContextLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            if (config == null) {
                factory = Validation.buildDefaultValidatorFactory();
            } else {
                Configuration<?> configuration = getConfig(config);
                try {
                    factory = configuration.buildValidatorFactory();
                } catch (ValidationException ve) {
                    Thread.currentThread().setContextClassLoader(ValidatorBuilder.class.getClassLoader());
                    factory = Validation.buildDefaultValidatorFactory();
                    Thread.currentThread().setContextClassLoader(classLoader);

                    logger.warning("Unable create validator factory with config " + config
                        + " (" + ve.getMessage() + ")."
                        + " Default factory will be used.");
                }
                configuration.ignoreXmlConfiguration();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextLoader);
        }
        return factory;
    }

    @SuppressWarnings("unchecked")
	private static Configuration<?> getConfig(ValidationInfo info) {
        Configuration<?> target = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String providerClassName = info.providerClassName;
        if (providerClassName == null) {
            providerClassName = System.getProperty(VALIDATION_PROVIDER_KEY);
        }

        if (providerClassName != null) {
            try {
                @SuppressWarnings({"unchecked","rawtypes"})
                Class clazz = classLoader.loadClass(providerClassName);
                target = Validation.byProvider(clazz).configure();
                logger.info("Using " + providerClassName + " as validation provider.");
            } catch (ClassNotFoundException e) {
                logger.warning("Unable to load provider class " + providerClassName, e);
            } catch (ValidationException ve) {
                logger.warning("Unable create validator factory with provider " + providerClassName
                        + " (" + ve.getMessage() + ")."
                        + " Default one will be used.");
            }
        }
        if (target == null) {
            // force to use container provider to ignore any conflicting configuration
            Thread.currentThread().setContextClassLoader(ValidatorBuilder.class.getClassLoader());
            target = Validation.byDefaultProvider().configure();
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        // config is manage here so ignore provider parsing
        target.ignoreXmlConfiguration();

        String messageInterpolatorClass = info.messageInterpolatorClass;
        if (messageInterpolatorClass != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<MessageInterpolator> clazz = (Class<MessageInterpolator>) classLoader.loadClass(messageInterpolatorClass);
                target.messageInterpolator(clazz.newInstance());
            } catch (Exception e) {
                logger.warning("Unable to set "+messageInterpolatorClass+ " as message interpolator.", e);
            }
            logger.info("Using " + messageInterpolatorClass + " as message interpolator.");
        }
        String traversableResolverClass = info.traversableResolverClass;
        if (traversableResolverClass != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<TraversableResolver> clazz = (Class<TraversableResolver>) classLoader.loadClass(traversableResolverClass);
                target.traversableResolver(clazz.newInstance());
            } catch (Exception e) {
                logger.warning("Unable to set "+traversableResolverClass+ " as traversable resolver.", e);
            }
            logger.info("Using " + traversableResolverClass + " as traversable resolver.");
        }
        String constraintFactoryClass = info.constraintFactoryClass;
        if (constraintFactoryClass != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<ConstraintValidatorFactory> clazz = (Class<ConstraintValidatorFactory>) classLoader.loadClass(constraintFactoryClass);
                target.constraintValidatorFactory(clazz.newInstance());
            } catch (Exception e) {
                logger.warning("Unable to set "+constraintFactoryClass+ " as constraint factory.", e);
            }
            logger.info("Using " + constraintFactoryClass + " as constraint factory.");
        }
        for (Map.Entry<Object, Object> entry : info.propertyTypes.entrySet()) {
            PropertyType property = new PropertyType();
            property.setName((String) entry.getKey());
            property.setValue((String) entry.getValue());

            if (logger.isDebugEnabled()) {
                logger.debug("Found property '" + property.getName() + "' with value '" + property.getValue());
            }
            target.addProperty(property.getName(), property.getValue());
        }
        for (String mappingFileName : info.constraintMappings) {
            if (logger.isDebugEnabled()) {
                logger.debug("Opening input stream for " + mappingFileName);
            }
            InputStream in = classLoader.getResourceAsStream(mappingFileName);
            if (in == null) {
                logger.warning("Unable to open input stream for mapping file " + mappingFileName + ". It will be ignored");
            } else {
                target.addMapping(in);
            }
        }

        return target;
    }
}
