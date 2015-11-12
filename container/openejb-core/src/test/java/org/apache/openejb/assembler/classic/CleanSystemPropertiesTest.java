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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class CleanSystemPropertiesTest {

    @Module
    public WebApp hook() {
        final WebApp webApp = new WebApp();
        return webApp;
    }

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
                .p("test-property", "true")
                .build();
    }

    @Test
    public void test() {

    }

    @AfterClass
    public static void shouldClearSystemProperties() {
        if (System.getProperty("test-property") != null) {
            fail("ApplicationComposers not clear the System properties");
        }
    }
}