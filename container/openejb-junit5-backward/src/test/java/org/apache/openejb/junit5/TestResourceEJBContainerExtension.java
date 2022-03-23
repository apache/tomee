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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.junit5;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.junit.jee.config.Properties;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;
import javax.naming.NamingException;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;


@Properties({
    @org.apache.openejb.junit.jee.config.Property(key = DeploymentFilterable.CLASSPATH_EXCLUDE, value = "jar:.*"),
    @org.apache.openejb.junit.jee.config.Property(key = DeploymentFilterable.CLASSPATH_INCLUDE, value = ".*openejb-junit5-backward.*"),
    @org.apache.openejb.junit.jee.config.Property(key = "r", value = "new://Resource?type=DataSource")
})
@RunWithEjbContainer
public class TestResourceEJBContainerExtension {

    @Resource(name = "r")
    private DataSource ds;

    @Test
    public void checkResource() throws NamingException {
        assertEquals(ds,   SystemInstance.get().getComponent(ContainerSystem.class)
                .getJNDIContext().lookup("java:" + Assembler.OPENEJB_RESOURCE_JNDI_PREFIX + "r"));
    }
}
