package org.apache.openejb.bval.util;

import org.apache.bval.jsr303.ApacheValidationProvider;
import org.apache.bval.jsr303.ApacheValidatorConfiguration;
import org.apache.bval.jsr303.ConfigurationImpl;

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

// simply a provider which is by default apache validation provider
// but it can be changed for test purpose
public class CustomValidatorProvider implements ValidationProvider<ApacheValidatorConfiguration> {
    public static ValidationProvider provider = new ApacheValidationProvider();

    @Override
    public ApacheValidatorConfiguration createSpecializedConfiguration(BootstrapState state) {
        return (ApacheValidatorConfiguration) provider.createSpecializedConfiguration(state);
    }

    @Override
    public javax.validation.Configuration<?> createGenericConfiguration(BootstrapState state) {
        return provider.createGenericConfiguration(state);
    }

    @Override
    public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
        return provider.buildValidatorFactory(configurationState);
    }

    public static class NullValidatorProvider implements ValidationProvider<ApacheValidatorConfiguration> {
        @Override
        public ApacheValidatorConfiguration createSpecializedConfiguration(BootstrapState state) {
            return new NullConfig(state, provider);
        }

        @Override
        public javax.validation.Configuration<?> createGenericConfiguration(BootstrapState state) {
            return new NullConfig(state, provider);
        }

        @Override
        public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
            return null;
        }
    }


    public static class NullConfig extends ConfigurationImpl {
        public NullConfig(BootstrapState aState, ValidationProvider<?> aProvider) {
            super(aState, aProvider);
        }

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

    public static class CustomValidatorFactory implements ValidatorFactory {
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

    public static class CustomValidatorContext implements ValidatorContext {
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

    public static class CustomValidator implements Validator {
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
