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
package org.apache.openejb.junit;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.junit.jee.EJBContainerRunner;
import org.apache.openejb.junit.jee.config.Properties;
import org.apache.openejb.junit.jee.config.Property;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@Properties({ // just a small conf to go faster
    @Property(key = DeploymentFilterable.CLASSPATH_EXCLUDE, value = "jar:.*"),
    @Property(key = DeploymentFilterable.CLASSPATH_INCLUDE, value = ".*openejb-junit.*")
})
@RunWith(EJBContainerRunner.class)
public class TestEJBContainerRunnerResources {
    @org.apache.openejb.junit.jee.resources.TestResource
    private Context ctx;

    @org.apache.openejb.junit.jee.resources.TestResource
    private java.util.Properties props;

    @org.apache.openejb.junit.jee.resources.TestResource
    private EJBContainer container;

    @Test
    public void checkCtx() {
        assertNotNull(ctx);
    }

    @Test
    public void checkProps() {
        assertNotNull(props);
    }

    @Test
    public void checkContainer() {
        assertNotNull(container);
    }
}
