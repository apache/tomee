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
package org.apache.openejb.testing;

import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class WebAppEnvEntryTest {
    @Module
    @Classes(cdi = true, value = {CdiBean.class})
    public WebApp war() {
        final WebApp webApp = new WebApp().contextRoot("/myapp");
        webApp.getEnvEntry().add(new EnvEntry("foo", String.class.getName(), "bar"));
        return webApp;
    }

    @Inject
    private CdiBean bean;

    @Test
    public void test() {
        assertEquals("bar", bean.lookup());
    }

    public static class CdiBean {
        public String lookup() {
            try {
                return String.class.cast(new InitialContext().lookup("java:comp/env/foo"));
            } catch (final NamingException e) {
                return "-";
            }
        }
    }
}

