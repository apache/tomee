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
package org.apache.openejb.testing;

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.junit.RunAsRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import javax.naming.NamingException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RunAsRuleTest {
    @Rule
    public final TestRule container = RuleChain.outerRule(new ApplicationComposerRule(this)).around(new RunAsRule());

    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(MyBean.class).localBean();
    }

    @EJB
    private MyBean ejb;

    @Test
    public void no() throws NamingException {
        assertFalse(ejb.isTest());
    }

    @Test
    @RunAsRule.As("test")
    public void yes() throws NamingException {
        assertTrue(ejb.isTest());
    }

    public static class MyBean {
        @Resource
        private SessionContext sc;

        public boolean isTest() {
            return sc.isCallerInRole("test");
        }
    }
}
