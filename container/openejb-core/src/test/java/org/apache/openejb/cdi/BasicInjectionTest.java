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
package org.apache.openejb.cdi;

import junit.framework.TestCase;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;

/**
 * @version $Rev$ $Date$
 */

@RunWith(ApplicationComposer.class)
public class BasicInjectionTest extends TestCase {

    @Module
    @Classes(cdi = true, value = {Configuration.class})
    public StatelessBean app() throws Exception {

        final StatelessBean bean = new StatelessBean(WidgetBean.class);
        bean.setLocalBean(new Empty());

        return bean;
    }

    @Inject
    private WidgetBean bean;

    @Test
    public void test() {
        assertNotNull(bean.getJmsLocation());
        assertNotNull(bean.getWebLocation());
    }


    public static class WidgetBean {

        @Inject
        @Web
        private URI webLocation;

        @Inject
        @Jms
        private URI jmsLocation;

        @PostConstruct
        private void init() {
            if (webLocation == null) throw new IllegalStateException("webLocation");
            if (jmsLocation == null) throw new IllegalStateException("jmsLocation");
        }

        public URI getWebLocation() {
            return webLocation;
        }

        public URI getJmsLocation() {
            return jmsLocation;
        }
    }

    public static class Configuration {

        @Produces
        @Jms
        public URI getWebURI() {
            return URI.create("jms://foo");
        }

        @Produces
        @Web
        public URI getJmsURI() {
            return URI.create("web://foo");
        }
    }


    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.FIELD, ElementType.METHOD})
    public static @interface Jms {

    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.FIELD, ElementType.METHOD})
    public static @interface Web {

    }

}
