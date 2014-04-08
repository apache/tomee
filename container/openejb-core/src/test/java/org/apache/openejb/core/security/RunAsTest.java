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
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class RunAsTest {
    @Module
    public Class<?>[] beans() {
        return new Class<?>[] { MyRunAsBean.class };
    }

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
