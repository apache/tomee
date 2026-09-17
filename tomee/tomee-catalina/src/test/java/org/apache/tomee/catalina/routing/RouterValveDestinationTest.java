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

package org.apache.tomee.catalina.routing;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RouterValveDestinationTest {
    @Test
    public void keepsRegularDestinations() {
        assertEquals("/app/page", RouterValve.normalizeDestination(null, "/app/page"));
        assertEquals("/app/page", RouterValve.normalizeDestination(null, "/app/page?x=1"));
        assertEquals("/app/a b", RouterValve.normalizeDestination(null, "/app/a%20b"));
        assertEquals("/b", RouterValve.normalizeDestination(null, "/a/../b"));
    }

    @Test
    public void refusesWebInfAndMetaInf() {
        assertNull(RouterValve.normalizeDestination(null, "/WEB-INF/web.xml"));
        assertNull(RouterValve.normalizeDestination(null, "/WEB-INF"));
        assertNull(RouterValve.normalizeDestination(null, "/web-inf/classes/app/DBConfig.class"));
        assertNull(RouterValve.normalizeDestination(null, "/META-INF/context.xml"));
        // traversal into WEB-INF, raw and encoded
        assertNull(RouterValve.normalizeDestination(null, "/pub/../WEB-INF/web.xml"));
        assertNull(RouterValve.normalizeDestination(null, "/pub/%2e%2e/WEB-INF/web.xml"));
        assertNull(RouterValve.normalizeDestination(null, "/%57EB-INF/web.xml"));
        // path parameters must not hide a segment from the check
        assertNull(RouterValve.normalizeDestination(null, "/WEB-INF;x=y/web.xml"));
    }

    @Test
    public void refusesEscapesAndRelativePaths() {
        assertNull(RouterValve.normalizeDestination(null, "/.."));
        assertNull(RouterValve.normalizeDestination(null, "/../outside"));
        assertNull(RouterValve.normalizeDestination(null, "relative/path"));
        assertNull(RouterValve.normalizeDestination(null, "/bad%zzescape"));
    }

    @Test
    public void securityConstraintPatternMatching() {
        assertTrue(RouterValve.patternMatches("/admin/users", "/admin/*"));
        assertTrue(RouterValve.patternMatches("/admin", "/admin/*"));
        assertTrue(RouterValve.patternMatches("/admin/users", "/admin/users"));
        assertTrue(RouterValve.patternMatches("/anything", "/*"));
        assertTrue(RouterValve.patternMatches("/anything", "/"));
        assertTrue(RouterValve.patternMatches("/a/b.jsp", "*.jsp"));
        assertFalse(RouterValve.patternMatches("/admins", "/admin/*"));
        assertFalse(RouterValve.patternMatches("/public/x", "/admin/*"));
        assertFalse(RouterValve.patternMatches("/a/b.jspx", "*.jsp"));
    }
}
