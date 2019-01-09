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
package org.apache.tomee.security;

import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.util.NetworkUtil;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractTomEESecurityTest {
    protected static Container container;

    @BeforeClass
    public static void setUp() throws Exception {
        container = new Container(
                new Configuration()
                        .conf("conf")
                        .http(NetworkUtil.getNextAvailablePort())
                        .property("openejb.container.additional.exclude", "org.apache.tomee.security.")
                        .property("openejb.additional.include", "tomee-"))
                .deployPathsAsWebapp(JarLocation.jarLocation(AbstractTomEESecurityTest.class));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        container.close();
    }

    protected String getAppUrl() {
        return "http://localhost:" + container.getConfiguration().getHttpPort();
    }
}
