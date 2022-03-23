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

import org.apache.openejb.bval.ValidatorUtil;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.NamingException;
import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;
import java.io.Serializable;
import java.util.Map;

public class ValidatorFactoryWrapper implements ValidatorFactory, Serializable {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, ValidatorFactoryWrapper.class);

    private transient Map<ComparableValidationConfig, ValidatorFactory> fallbackValidators;

    private ValidatorFactory factory() {
        try {
            return ValidatorUtil.lookupFactory();
        } catch (final NamingException e) { // in absolute we should sort them to get the closest one of the persistence-unit?
            if (fallbackValidators != null && !fallbackValidators.isEmpty()) {
                return fallbackValidators.values().iterator().next();
            }
            return ValidatorUtil.tryJndiLaterFactory();
        }
    }

    public ValidatorFactoryWrapper(final Map<ComparableValidationConfig, ValidatorFactory> validators) {
        fallbackValidators = validators;
    }

    @Override
    public Validator getValidator() {
        return factory().getValidator();
    }

    @Override
    public ValidatorContext usingContext() {
        return factory().usingContext();
    }

    @Override
    public MessageInterpolator getMessageInterpolator() {
        return factory().getMessageInterpolator();
    }

    @Override
    public TraversableResolver getTraversableResolver() {
        return factory().getTraversableResolver();
    }

    @Override
    public ConstraintValidatorFactory getConstraintValidatorFactory() {
        return factory().getConstraintValidatorFactory();
    }

    @Override
    public <T> T unwrap(final Class<T> tClass) {
        return factory().unwrap(tClass);
    }

    @Override
    public ParameterNameProvider getParameterNameProvider() {
        return factory().getParameterNameProvider();
    }

    @Override
    public ClockProvider getClockProvider()
    {
        return factory().getClockProvider();
    }

    @Override
    public void close() {
        factory().close();
    }
}
