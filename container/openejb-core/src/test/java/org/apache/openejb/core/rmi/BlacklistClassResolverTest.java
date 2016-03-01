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
package org.apache.openejb.core.rmi;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BlacklistClassResolverTest {
    @Test
    public void blacklistDefault() {
        assertTrue(BlacklistClassResolver.DEFAULT.isBlacklisted("org.codehaus.groovy.runtime.Foo"));
        assertTrue(BlacklistClassResolver.DEFAULT.isBlacklisted("org.apache.commons.collections.functors.Foo"));
        assertTrue(BlacklistClassResolver.DEFAULT.isBlacklisted("org.apache.xalan.Foo"));
    }

    @Test
    public void implicitWhiteList() {
        assertFalse(BlacklistClassResolver.DEFAULT.isBlacklisted("org.apache.tomee.Foo"));
    }

    @Test
    public void whiteList() {
        assertFalse(new BlacklistClassResolver(null, new String[] { "org.apache.xalan" }).isBlacklisted("org.apache.xalan.Foo"));
    }

    @Test
    public void wildcard() {
        final BlacklistClassResolver classResolver = new BlacklistClassResolver(new String[]{"*"}, new String[] {"white", "com.white"});
        assertTrue(classResolver.isBlacklisted("white.Foo"));
        assertTrue(classResolver.isBlacklisted("com.white.test"));
        assertTrue(classResolver.isBlacklisted("other.test"));
    }
}
