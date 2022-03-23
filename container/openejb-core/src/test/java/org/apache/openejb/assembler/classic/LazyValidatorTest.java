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

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.io.Serializable;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertNotNull;

public class LazyValidatorTest {
    @Test
    public void serialize() {
        final Serializable obj = Serializable.class.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{ValidatorFactory.class}, new LazyValidator(Validation.buildDefaultValidatorFactory())));
        final LazyValidator deserialized = LazyValidator.class.cast(Proxy.getInvocationHandler(SerializationUtils.deserialize(SerializationUtils.serialize(obj))));
        final Validator validator = deserialized.getValidator();
        assertNotNull(validator);
    }
}
