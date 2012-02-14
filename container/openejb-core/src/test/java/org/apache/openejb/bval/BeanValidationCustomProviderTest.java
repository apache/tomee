/**
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
package org.apache.openejb.bval;

import org.apache.openejb.assembler.classic.ValidatorBuilder;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;
import java.io.InputStream;
import java.util.Set;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class BeanValidationCustomProviderTest {
    @EJB
    private ABean bean;

    @BeforeClass
    public static void initProvider() {
        System.setProperty(ValidatorBuilder.VALIDATION_PROVIDER_KEY, CustomValidatorProvider.class.getName());
    }

    @AfterClass
    public static void resetProvider() {
        System.clearProperty(ValidatorBuilder.VALIDATION_PROVIDER_KEY);
    }

    @Module
    public StatelessBean app() throws Exception {
        final StatelessBean bean = new StatelessBean(ABean.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Stateless
    public static class ABean {
        @Resource
        private Validator validator;

        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public Validator getValidator() {
            return validator;
        }
    }

    @Test
    public void valid() {
        assertTrue(bean.getValidator() instanceof CustomValidator);
    }

    public static class CustomValidatorProvider implements ValidationProvider<NullConfig> {
        @Override
        public NullConfig createSpecializedConfiguration(BootstrapState state) {
            return new NullConfig();
        }

        @Override
        public javax.validation.Configuration<?> createGenericConfiguration(BootstrapState state) {
            return new NullConfig();
        }

        @Override
        public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
            return null;
        }
    }

    private static class NullConfig implements javax.validation.Configuration<NullConfig> {
        @Override
        public NullConfig ignoreXmlConfiguration() {
            return null;
        }

        @Override
        public NullConfig messageInterpolator(MessageInterpolator interpolator) {
            return null;
        }

        @Override
        public NullConfig traversableResolver(TraversableResolver resolver) {
            return null;
        }

        @Override
        public NullConfig constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
            return null;
        }

        @Override
        public NullConfig addMapping(InputStream stream) {
            return null;
        }

        @Override
        public NullConfig addProperty(String name, String value) {
            return null;
        }

        @Override
        public MessageInterpolator getDefaultMessageInterpolator() {
            return null;
        }

        @Override
        public TraversableResolver getDefaultTraversableResolver() {
            return null;
        }

        @Override
        public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
            return null;
        }

        @Override
        public ValidatorFactory buildValidatorFactory() {
            return new CustomValidatorFactory();
        }
    }

    private static class CustomValidatorFactory implements ValidatorFactory {
        @Override
        public Validator getValidator() {
            return new CustomValidator();
        }

        @Override
        public ValidatorContext usingContext() {
            return new CustomValidatorContext();
        }

        @Override
        public MessageInterpolator getMessageInterpolator() {
            return null;
        }

        @Override
        public TraversableResolver getTraversableResolver() {
            return null;
        }

        @Override
        public ConstraintValidatorFactory getConstraintValidatorFactory() {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            return null;
        }
    }

    private static class CustomValidatorContext implements ValidatorContext {
        @Override
        public ValidatorContext messageInterpolator(MessageInterpolator messageInterpolator) {
            return null;
        }

        @Override
        public ValidatorContext traversableResolver(TraversableResolver traversableResolver) {
            return null;
        }

        @Override
        public ValidatorContext constraintValidatorFactory(ConstraintValidatorFactory factory) {
            return null;
        }

        @Override
        public Validator getValidator() {
            return new CustomValidator();
        }
    }

    private static class CustomValidator implements Validator {
        @Override
        public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
            return null;
        }

        @Override
        public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
            return null;
        }

        @Override
        public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
            return null;
        }

        @Override
        public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            return null;
        }
    }
}
