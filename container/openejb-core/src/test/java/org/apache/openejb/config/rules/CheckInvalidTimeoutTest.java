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
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.StatelessBean;

import jakarta.ejb.Timeout;

//@RunWith(ValidationRunner.class)
public class CheckInvalidTimeoutTest extends TestCase {

    public void testNothing() {
    }

    @Keys({@Key(value = "timeout.badReturnType"), @Key("timeout.invalidArguments"), @Key("timeout.tooManyMethods"), @Key("timeout.missing.possibleTypo")})
    public EjbJar _test() throws Exception {
        System.setProperty("openejb.validation.output.level", "VERBOSE");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(TestBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        final StatelessBean barBean = new StatelessBean(BarBean.class);
        barBean.setTimeoutMethod(new NamedMethod("foo", "java.lang.String"));
        ejbJar.addEnterpriseBean(barBean);
        return ejbJar;
    }

    // A case where the class has the method with wrong signature annotated with @Timeout
    // timeout.badReturnType
    // timeout.invalidArguments
    public static class TestBean {
        @Timeout
        public Object bar(final Object m) {
            return null;
        }
    }

    // A case where the class has more than one method annotated with @Timeout
    // timeout.tooManyMethods
    public static class FooBean {
        @Timeout
        public void foo(final jakarta.ejb.Timer timer) {
        }

        @Timeout
        public void bar(final jakarta.ejb.Timer timer) {
        }
    }

    //  A case where incorrect timeout method is configured in the deployment descriptor
    // timeout.missing.possibleTypo
    public static class BarBean {

        public void foo(final jakarta.ejb.Timer timer) {
        }
    }

}
