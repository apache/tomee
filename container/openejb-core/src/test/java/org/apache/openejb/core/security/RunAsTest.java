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
package org.apache.openejb.core.security;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class RunAsTest {
    @EJB
    private MyRunAsBean bean;

    @Test
    public void runAs() {
        assertTrue(bean.isInRole());
        assertEquals("foo", bean.principal());
    }

    @RunAs("foo")
    @Singleton
    public static class MyRunAsBean {
        @EJB
        private Delegate delegate;

        public String principal() {
            return delegate.principal();
        }

        public boolean isInRole() {
            return delegate.isInRole();
        }
    }

    @RunAs("foo")
    @Singleton
    public static class Delegate {
        @Resource
        private SessionContext ctx;

        public String principal() {
            return ctx.getCallerPrincipal().getName();
        }

        public boolean isInRole() {
            return ctx.isCallerInRole("foo");
        }
    }
}
