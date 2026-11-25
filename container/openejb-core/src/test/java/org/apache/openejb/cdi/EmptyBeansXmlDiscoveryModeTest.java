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
package org.apache.openejb.cdi;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

public class EmptyBeansXmlDiscoveryModeTest {
    @After
    public void reset() {
        SystemInstance.reset();
    }

    @Test
    public void defaultsToAll() throws OpenEJBException {
        assertEquals("ALL", ReadDescriptors.readBeans(new ByteArrayInputStream(new byte[0])).getBeanDiscoveryMode());
    }

    @Test
    public void configurable() throws OpenEJBException {
        SystemInstance.get().setProperty(ReadDescriptors.DEFAULT_BEAN_DISCOVERY_MODE, "annotated");
        assertEquals("ANNOTATED", ReadDescriptors.readBeans(new ByteArrayInputStream(new byte[0])).getBeanDiscoveryMode());
    }
}
