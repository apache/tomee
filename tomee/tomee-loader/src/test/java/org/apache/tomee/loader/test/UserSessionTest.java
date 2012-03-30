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
import org.apache.tomee.loader.service.helper.TypeAndValueEntry;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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
        final Context context = service.getJndiHelper().getContext(null, null);

        org.junit.Assert.assertNotNull(result);
        org.junit.Assert.assertFalse(result.isEmpty());

        final List<String> names = new ArrayList<String>();
        mountPathsList(names, result);

        System.out.println("*******************************************");
        System.out.println(result);
        System.out.println("*******************************************");
        for (String name : names) {
            Object srv = null;
            try {
                srv = context.lookup(name);

            } catch (NamingException e) {
                //not found
            }

            if (DummyEjb.class.isInstance(srv)) {
                System.out.println(name + " -> " + service.getJndiHelper().invokeJndiMethod(
                        context,
                        name,
                        "sayHi",
                        new TypeAndValueEntry(String.class, "buddy")
                ));
                System.out.println(name + " -> " + service.getJndiHelper().invokeJndiMethod(
                        context,
                        name,
                        "sayWhassup"
                ));
                showMethods(service, name);
            } else {
                if (srv == null) {
                    System.out.println(name + " (NOT FOUND) ");
                } else {
                    System.out.println(name);
                    showMethods(service, name);
                }
            }
        }
        System.out.println("*******************************************");
    }

    private void showMethods(final ServiceContext service, String name) {
        //show methods
        final List<Method> methods = service.getJndiHelper().getJndiMethods(name);
        for (Method method : methods) {
            System.out.println("    METHOD -> " + method);
        }
    }

    private void mountPathsList(final List<String> names, final Map<String, Object> jndiEntry) {
        names.add((String) jndiEntry.get("path"));

        List<Map<String, Object>> jndiEntries = (List<Map<String, Object>>) jndiEntry.get("children");
        if (jndiEntries != null && !jndiEntries.isEmpty()) {

            for (Map<String, Object> child : jndiEntries) {
                mountPathsList(names, child);
            }
        }

    }

}
