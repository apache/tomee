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

package org.apache.openejb.config;

import org.apache.openejb.jee.JndiConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseConvertDefinitions implements DynamicDeployer {
    protected String cleanUpName(final String factory) {
        String name = factory;
        name = name.replaceFirst("java:comp/env/", "");
        name = name.replaceFirst("java:/", "");
        name = name.replaceFirst("java:", "");
        return name;
    }

    protected List<JndiConsumer> collectConsumers(final AppModule appModule) {

        final List<JndiConsumer> jndiConsumers = new ArrayList<>();

        for (final ClientModule module : appModule.getClientModules()) {
            final JndiConsumer consumer = module.getApplicationClient();
            if (consumer == null) {
                continue;
            }
            jndiConsumers.add(consumer);
        }

        for (final WebModule webModule : appModule.getWebModules()) {
            final JndiConsumer consumer = webModule.getWebApp();
            if (consumer == null) {
                continue;
            }
            jndiConsumers.add(consumer);
        }

        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            Collections.addAll(jndiConsumers, ejbModule.getEjbJar().getEnterpriseBeans());
        }

        if (appModule.getApplication() != null) {
            jndiConsumers.add(appModule.getApplication());
        }

        return jndiConsumers;
    }

}
