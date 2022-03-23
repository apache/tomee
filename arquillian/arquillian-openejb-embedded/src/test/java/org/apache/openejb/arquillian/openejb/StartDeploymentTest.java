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
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.arquillian.openejb.archive.SimpleArchive;
import org.apache.openejb.arquillian.openejb.archive.SimpleArchive2;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class StartDeploymentTest {
    @Inject
    private SimpleArchive2.AnotherSingleton bean;

    @Test
    public void deployment() {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        assertNotNull(containerSystem.getAppContext("start"));
        assertNotNull(containerSystem.getAppContext("start2"));
    }

    @Test
    public void checkItIsStarted() {
        assertTrue(SimpleArchive.ASingleton.ok);
    }

    @Test
    public void injections() {
        assertNotNull(bean);
    }
}
