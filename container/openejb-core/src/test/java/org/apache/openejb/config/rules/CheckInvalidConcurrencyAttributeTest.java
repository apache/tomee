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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.rules;

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.junit.runner.RunWith;

import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Lock;
import jakarta.interceptor.AroundInvoke;

@RunWith(ValidationRunner.class)
public class CheckInvalidConcurrencyAttributeTest extends TestCase {
    @Keys({@Key(value = "ann.invalidConcurrencyAttribute", type = KeyType.WARNING), @Key(value = "aroundInvoke.invalidArguments")})
    public EjbJar test() throws Exception {
        System.setProperty("openejb.validation.output.level", "VERBOSE");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(TestBean.class));
        return ejbJar;
    }

    @ConcurrencyManagement(ConcurrencyManagementType.BEAN)
    @Lock
    public static class TestBean {
        @AroundInvoke
        public Object foo() {
            return null;
        }
    }
}
