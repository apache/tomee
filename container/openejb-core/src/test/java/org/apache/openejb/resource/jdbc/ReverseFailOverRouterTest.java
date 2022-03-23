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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.router.FailOverRouter;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static org.apache.openejb.resource.jdbc.FailOverRouters.datasource;
import static org.apache.openejb.resource.jdbc.FailOverRouters.url;
import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class ReverseFailOverRouterTest {
    @Resource(name = "routedDs")
    private DataSource failover;

    @Test
    public void test() throws SQLException {
        int i = 2;
        for (int it = 0; it < 6; it++) {
            assertEquals("Iteration #" + i, "jdbc:hsqldb:mem:fo" + i, url(failover.getConnection()));
            i = 1 + (i % 2);
        }
    }

    @Configuration
    public Properties configuration() {
        return datasource(datasource(new PropertiesBuilder(), "fo1"), "fo2")

            .property("router", "new://Resource?class-name=" + FailOverRouter.class.getName())
            .property("router.datasourceNames", "fo1,fo2")
            .property("router.strategy", "reverse")

            .property("routedDs", "new://Resource?provider=RoutedDataSource&type=DataSource")
            .property("routedDs.router", "router")

            .build();
    }

    @Module
    public IAnnotationFinder finder() { // needed to run the test
        return new AnnotationFinder(new ClassesArchive());
    }
}
