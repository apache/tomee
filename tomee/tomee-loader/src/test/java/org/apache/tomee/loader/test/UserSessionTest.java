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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.loader.test;

import org.apache.tomee.loader.service.ServiceContext;
import org.apache.tomee.loader.service.ServiceContextImpl;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;
import java.util.Properties;

public class UserSessionTest {

    @Test()
    public void test() throws Exception {
        {
            final Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
            properties.put("openejb.loader", "embed");
            try {
                new InitialContext(properties);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }

        final ServiceContext service = new ServiceContextImpl();
        final Map<String, Object> result = service.getJndiHelper().getJndi();
        org.junit.Assert.assertNotNull(result);
        org.junit.Assert.assertFalse(result.isEmpty());

        System.out.println("*******************************************");
        System.out.println(result);
        System.out.println("*******************************************");
    }

}
