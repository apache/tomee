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

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import java.io.Serializable;
import java.util.Map;

public class ValidatorFactoryWrapper implements ValidatorFactory, Serializable {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, ValidatorFactoryWrapper.class);

    private static ValidatorFactory factory() {
        try {
            return (ValidatorFactory) new InitialContext().lookup("java:comp/ValidatorFactory");
        } catch (NamingException e) {
            logger.warning("validator factory not found for current module ");
            return null;
        }
    }

    @Override public Validator getValidator() {
        return factory().getValidator();
    }

    @Override public ValidatorContext usingContext() {
        return factory().usingContext();
    }

    @Override public MessageInterpolator getMessageInterpolator() {
        return factory().getMessageInterpolator();
    }

    @Override public TraversableResolver getTraversableResolver() {
        return factory().getTraversableResolver();
    }

    @Override public ConstraintValidatorFactory getConstraintValidatorFactory() {
        return factory().getConstraintValidatorFactory();
    }

    @Override public <T> T unwrap(Class<T> tClass) {
        return factory().unwrap(tClass);
    }
}
