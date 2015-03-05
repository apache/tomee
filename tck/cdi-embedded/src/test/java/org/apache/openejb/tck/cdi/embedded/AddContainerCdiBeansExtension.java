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
package org.apache.openejb.tck.cdi.embedded;

import org.apache.openejb.cdi.CompositeBeans;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.event.BeforeAppInfoBuilderEvent;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.observer.Observes;
import org.jboss.cdi.tck.extlib.Strict;
import org.jboss.cdi.tck.extlib.Translator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class AddContainerCdiBeansExtension {
    private static final URL EXT_LIB = AddContainerCdiBeansExtension.class.getClassLoader().getResource(Translator.class.getName().replace(".", "/") + ".class");
    private static final List<String> BEANS = new ArrayList<>(asList(Strict.class.getName(), Translator.class.getName()));

    public void addCdiExtLib(@Observes final BeforeAppInfoBuilderEvent event) {
        for (final EjbModule ejbModule : event.getAppModule().getEjbModules()) {
            if (ejbModule.getModuleId().startsWith("ear-scoped-cdi-beans")) {
                final Beans beans = ejbModule.getBeans();
                if (CompositeBeans.class.isInstance(beans)) {
                    final CompositeBeans cb = CompositeBeans.class.cast(beans);
                    cb.getManagedClasses().put(EXT_LIB, new ArrayList<>(BEANS));
                }
                return;
            }
        }
        // else a war
        for (final WebModule webModule : event.getAppModule().getWebModules()) {
            for (final EjbModule ejbModule : event.getAppModule().getEjbModules()) {
                if (ejbModule.getModuleId().equals(webModule.getModuleId())) {
                    final Beans beans = ejbModule.getBeans();
                    if (CompositeBeans.class.isInstance(beans)) {
                        final CompositeBeans cb = CompositeBeans.class.cast(beans);
                        cb.getManagedClasses().put(EXT_LIB, new ArrayList<>(BEANS));
                    }
                    return;
                }
            }
        }
    }
}
