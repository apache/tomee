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
package org.apache.openejb.config;

import org.apache.openejb.util.ContainerClassesFilter;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContainerClassesFilterTest {
    @Test
    public void excludes() {
        final ContainerClassesFilter filter = new ContainerClassesFilter(new Properties());
        assertTrue(filter.accept("com.business.AClass"));
        assertTrue(filter.accept("org.apache.AClass"));
        assertTrue(filter.accept("org.apache.myfaces.cdi.AClass"));
        assertFalse(filter.accept("org.apache.openejb.Bar"));
        assertFalse(filter.accept("org.apache.openejb.foo.Bar"));
        assertFalse(filter.accept("org.jasypt.dfzg.Bar"));
        assertFalse(filter.accept("org.codehaus.swizzle.Grep"));
        assertTrue(filter.accept("org.codehaus.swizzle.Gre"));
    }
}
