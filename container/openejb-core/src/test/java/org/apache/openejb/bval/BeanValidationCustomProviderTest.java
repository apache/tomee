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

import org.apache.bval.jsr.ApacheValidationProvider;
import org.apache.openejb.assembler.classic.ValidatorBuilder;
import org.apache.openejb.bval.util.CustomValidatorProvider;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.validation.Validator;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class BeanValidationCustomProviderTest {
    @EJB
    private ABean bean;

    @BeforeClass
    public static void initProvider() {
        CustomValidatorProvider.provider = new CustomValidatorProvider.NullValidatorProvider();
        System.setProperty(ValidatorBuilder.VALIDATION_PROVIDER_KEY, CustomValidatorProvider.class.getName());
    }

    @AfterClass
    public static void resetProvider() {
        CustomValidatorProvider.provider = new ApacheValidationProvider();
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
        assertTrue(bean.getValidator() instanceof CustomValidatorProvider.CustomValidator);
    }
}
