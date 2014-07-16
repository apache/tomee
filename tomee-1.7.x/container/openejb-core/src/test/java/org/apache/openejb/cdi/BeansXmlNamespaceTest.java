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
import org.apache.openejb.jee.Beans;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BeansXmlNamespaceTest {
    @Test
    public void read() throws OpenEJBException {
        final Beans beans = ReadDescriptors.readBeans(getClass().getResourceAsStream("/beans-namespace.xml"));
        assertEquals(1, beans.getInterceptors().size());
        assertEquals("foo", beans.getInterceptors().iterator().next());
        assertEquals(1, beans.getDecorators().size());
        assertEquals("bar", beans.getDecorators().iterator().next());
    }
}
