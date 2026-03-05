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
package org.apache.openejb.data.test;

import org.apache.openejb.data.extension.DataRepositoryExtension;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DataRepositoryExtensionUnitTest {

    @Test
    void hasHibernateReturnsFalseWhenHibernateNotOnClasspath() throws Exception {
        // In our test environment, Hibernate is not on the classpath
        // so hasHibernate() should return false
        final Method hasHibernate = DataRepositoryExtension.class.getDeclaredMethod("hasHibernate");
        hasHibernate.setAccessible(true);
        final boolean result = (boolean) hasHibernate.invoke(null);
        assertFalse(result, "hasHibernate() should return false when Hibernate is not on the classpath");
    }
}
