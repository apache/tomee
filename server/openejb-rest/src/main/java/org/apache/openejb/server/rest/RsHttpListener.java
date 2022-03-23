/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.openejb.server.rest;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.webbeans.config.WebBeansContext;

import javax.naming.Context;
import jakarta.ws.rs.core.Application;
import java.util.Collection;
import java.util.Map;

public interface RsHttpListener extends HttpListener {
    void deploySingleton(String webContext, String fullContext, Object o, Application appInstance,
                         Collection<Object> additionalProviders, ServiceConfiguration serviceInfos);

    void deployPojo(ClassLoader classloader, String webContext, String fullContext, Class<?> loadedClazz, Application app,
                    Collection<Injection> injections, Context context, WebBeansContext owbCtx,
                    Collection<Object> additionalProviders, ServiceConfiguration serviceInfos);

    void deployEJB(String webContext, String fullContext, BeanContext beanContext, Collection<Object> additionalProviders, ServiceConfiguration serviceInfos);

    void undeploy();

    void deployApplication(Application application, String prefix, String webContext, Collection<Object> additionalProviders, Map<String, EJBRestServiceInfo> restEjbs, ClassLoader classLoader, Collection<Injection> injections, Context context, WebBeansContext owbCtx, ServiceConfiguration serviceConfiguration);
}
