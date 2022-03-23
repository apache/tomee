/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.interceptors;

import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

import static org.testng.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class StatelessWithAroundInvokeOnlyTest {
    @EJB
    private StatelessTestBean stateless;

    @Test
    public void testAroundInvoke() {
        final int param = 1;
        final int result = stateless.execute(param);
        assertEquals(result, param);
    }

    @Module
    @Classes(cdi = true)
    public StatelessBean bean() {
        final StatelessBean bean = new StatelessBean(StatelessTestBean.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Stateless
    public static class StatelessTestBean {

        public int execute(int x) {
            return -x;
        }

        @AroundInvoke
        private Object aroundInvoke(InvocationContext ctx) throws Exception {
            final Integer result = (Integer) ctx.proceed();
            return -result;
        }
    }
}
