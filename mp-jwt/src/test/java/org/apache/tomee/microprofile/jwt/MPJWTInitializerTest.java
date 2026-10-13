/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.microprofile.jwt;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MPJWTInitializerTest {

    @LoginConfig(authMethod = "MP-JWT")
    public static class MpJwtApplication extends Application {
    }

    @LoginConfig(authMethod = "BASIC")
    public static class BasicApplication extends Application {
    }

    @LoginConfig(authMethod = "FORM")
    public static class FormApplication extends Application {
    }

    @Test
    public void filterIsRegisteredForMpJwt() throws Exception {
        final List<String> filters = startup(MpJwtApplication.class);
        assertEquals(Collections.singletonList("mp-jwt-filter"), filters);
    }

    @Test
    public void filterIsNotRegisteredForBasic() throws Exception {
        assertTrue(startup(BasicApplication.class).isEmpty());
    }

    @Test
    public void filterIsNotRegisteredForForm() throws Exception {
        assertTrue(startup(FormApplication.class).isEmpty());
    }

    private static List<String> startup(final Class<?> clazz) throws Exception {
        final List<String> filters = new ArrayList<>();
        final ClassLoader loader = MPJWTInitializerTest.class.getClassLoader();

        final FilterRegistration.Dynamic registration = (FilterRegistration.Dynamic) Proxy.newProxyInstance(
                loader, new Class<?>[]{FilterRegistration.Dynamic.class}, (proxy, method, args) -> null);

        final ServletContext context = (ServletContext) Proxy.newProxyInstance(
                loader, new Class<?>[]{ServletContext.class}, (proxy, method, args) -> {
                    if ("addFilter".equals(method.getName())) {
                        filters.add((String) args[0]);
                        return registration;
                    }
                    return null;
                });

        final Set<Class<?>> classes = Collections.singleton(clazz);
        new MPJWTInitializer().onStartup(classes, context);
        return filters;
    }
}
