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
import org.apache.openejb.junit.ApplicationComposer;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.Context;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class AppComposerContextInjectionTest {
    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(MyBean.class).localBean();
    }

    @AppResource
    private Context context;

    @Test
    public void lookupShouldWorkOnOpenEJBNames() throws NamingException {
        assertEquals("ok", MyBean.class.cast(context.lookup("MyBeanLocalBean")).ok());
    }

    @Test
    public void lookupShouldWorkOnGlobalNames() throws NamingException {
        assertEquals("ok", MyBean.class.cast(context.lookup("java:global/AppComposerContextInjectionTest/bean/MyBean")).ok());
    }

    public static class MyBean {
        public String ok() {
            return "ok";
        }
    }
}
