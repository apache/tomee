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
package org.apache.openejb.assembler.classic.util;

import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.PojoDeployment;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.sql.DataSource;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class ServiceInfosTest {
    @Configuration
    public Properties config() {
        return new PropertiesBuilder().p("ds", "new://Resource?type=DataSource").build();
    }

    @Module
    public EjbJar module() {
        return new EjbJar();
    }

    @Test
    public void createServiceWithResourceAttribute() {
        final ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.id = "the-id";
        serviceInfo.className = TheService.class.getName();
        serviceInfo.properties = new Properties();
        serviceInfo.properties.setProperty("dataSource", "@ds");
        final TheService instance = TheService.class.cast(ServiceInfos.resolve(asList(serviceInfo), serviceInfo.id));
        assertNotNull(instance.dataSource);
    }

    public static class TheService {
        private DataSource dataSource;

        public void setDataSource(final DataSource ds) {
            this.dataSource = ds;
        }
    }
}
