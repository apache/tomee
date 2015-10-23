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

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.server.cxf.rs.logging.LoggingJAXRSCommons;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;


@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class LoggingJAXRSWebAppModuleWithoutConfigurationTest extends LoggingJAXRSCommons {

    @BeforeClass
    public static void beforeClass() throws Exception {
        configurePort();
    }

    @Module
    @Classes(value = {LogginTestApplication.class, LogginTestBean.class})
    public WebApp service() throws Exception {
        configureLoggin();
        return getWebApp();
    }

    @Test
    public void checkLogger() throws Exception {
        assertFalse(assertJAXRSConfiguration());
    }
}
