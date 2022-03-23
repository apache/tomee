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
package org.apache.openejb.resource;

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class URLAsResourceTest {
    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
            .p("url", "new://Resource?class-name=java.net.URL&constructor=value")
            .p("url.value", "http://tomee.apache.org")
            .build();
    }

    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(WithUrl.class).localBean();
    }

    @EJB
    private WithUrl withUrl;

    @Test
    public void url() {
        final URL url = withUrl.getUrl();
        assertNotNull(url);
        assertEquals("http://tomee.apache.org", url.toExternalForm());
    }

    @Singleton
    public static class WithUrl {
        @Resource(name = "url")
        private URL url;

        public URL getUrl() {
            return url;
        }
    }
}
