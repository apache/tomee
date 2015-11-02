/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs.logging.webappmodule;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.server.cxf.rs.logging.LoggingJAXRSCommons;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;


@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class LoggingJAXRSWebAppModuleTest extends LoggingJAXRSCommons {

    @BeforeClass
    public static void beforeClass() throws Exception {
        configurePort();
    }

    @Module
    public AppModule service() throws Exception {
        final WebModule war = new WebModule(getWebApp(), "/test", Thread.currentThread().getContextClassLoader(), "", "test");
        war.getRestApplications().add(LogginTestApplication.class.getName());
        final AppModule appModule = new AppModule(getEjbModule("jaxrs-application", "test"), war);

        configureLoggin();
        return appModule;
    }

    @Test
    public void checkLogger() throws Exception {
        assertTrue(assertJAXRSConfiguration());
    }
}
