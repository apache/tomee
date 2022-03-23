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
package org.apache.openejb.resource.jdbc;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import javax.sql.DataSource;

import java.io.Serializable;

import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
@ContainerProperties(@ContainerProperties.Property(name = "ds", value = "new://Resource?type=DataSource"))
@Classes(cdi = true, innerClassesAsBean = true)
public class DataSourceSerializationTest {
    @Inject
    private DataSource ds;

    @Test
    public void run() {
        final DataSource d = SerializationUtils.deserialize(SerializationUtils.serialize(Serializable.class.cast(ds)));
        assertNotNull(d);
    }

    public static class Producer {
        @Resource(name = "ds")
        @Produces
        private DataSource produce;
    }
}
