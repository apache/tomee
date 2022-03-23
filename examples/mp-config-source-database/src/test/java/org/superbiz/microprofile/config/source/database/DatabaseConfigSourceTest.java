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
package org.superbiz.microprofile.config.source.database;

import org.apache.ziplock.maven.Mvn;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class DatabaseConfigSourceTest {
    @Deployment
    public static WebArchive createDeployment() {
        return (WebArchive) Mvn.war();
    }

    @Inject
    private Config config;
    @Inject
    private ApplicationBean applicationBean;

    @Test
    public void testConfigSourceStatic() throws Exception {
        assertEquals(config.getValue("application.currency", String.class), "Euro");
        assertEquals(config.getValue("application.country", String.class), "PT");

        assertEquals(applicationBean.getApplicationCurrrency(), "Euro");
        assertEquals(applicationBean.getApplicationCountry(), "PT");

        assertEquals(ConfigProvider.getConfig().getValue("application.currency", String.class), "Euro");
        assertEquals(ConfigProvider.getConfig().getValue("application.country", String.class), "PT");
    }
}
