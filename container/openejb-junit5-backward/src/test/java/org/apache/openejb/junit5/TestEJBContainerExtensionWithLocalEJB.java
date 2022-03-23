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

import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.junit.jee.config.Properties;
import org.apache.openejb.junit.jee.config.Property;
import org.apache.openejb.junit5.ejbs.BasicEjbLocal;
import org.junit.jupiter.api.Test;

import jakarta.ejb.embeddable.EJBContainer;
import jakarta.inject.Inject;
import javax.naming.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Properties({
    @Property(key = DeploymentFilterable.CLASSPATH_EXCLUDE, value = "jar:.*"),
    @Property(key = DeploymentFilterable.CLASSPATH_INCLUDE, value = ".*openejb-junit5-backward.*")
})
@RunWithEjbContainer
public class TestEJBContainerExtensionWithLocalEJB {

    @org.apache.openejb.junit.jee.resources.TestResource
    private Context ctx;

    @org.apache.openejb.junit.jee.resources.TestResource
    private java.util.Properties props;

    @org.apache.openejb.junit.jee.resources.TestResource
    private EJBContainer container;

    @Inject
    private BasicEjbLocal ejb;

    private void doChecks() {
        assertNotNull(ctx);
        assertNotNull(props);
        assertNotNull(container);
        assertNotNull(ejb);
        assertEquals("a b", ejb.concat("a", "b"));
    }

    @Test
    public void checkAllIsFine() {
        doChecks();
    }

    @Test
    public void checkAllIsStillFine() {
        doChecks();
    }
}
