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

package org.apache.openejb.arquillian.common;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.arquillian.common.enrichment.OpenEJBEnricher;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.TestEnricher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TomEEInjectionEnricher implements TestEnricher {

    @Inject
    private Instance<TestClass> testClass;

    @Override
    public void enrich(final Object o) {
        if (!SystemInstance.isInitialized()) {
            return;
        }
        OpenEJBEnricher.enrich(o, getAppContext(o.getClass().getName()));
    }

    private AppContext getAppContext(final String className) {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final List<AppContext> appContexts = containerSystem.getAppContexts();

        final int size = appContexts.size();
        if (size == 1) {
            return appContexts.get(0);
        }

        final List<AppContext> found = new ArrayList<AppContext>(size);

        for (final AppContext app : appContexts) {
            final BeanContext context = containerSystem.getBeanContext(app.getId() + "_" + className);
            if (context != null) {
                found.add(app);
            }
        }

        if (found.size() > 0) {

            Collections.sort(found, new Comparator<AppContext>() {

                /**
                 * If multiple apps are found that contain the test class then a best guess effort needs to be made
                 * to find the context that best matches the test class application.
                 *
                 * @param ac1 AppContext
                 * @param ac2 AppContext
                 * @return int
                 */
                @Override
                public int compare(final AppContext ac1, final AppContext ac2) {
                    int c = 0;

                    if (isBeanManagerInUse(ac1) && !isBeanManagerInUse(ac2)) {
                        c--;
                    } else if (!isBeanManagerInUse(ac1) && isBeanManagerInUse(ac2)) {
                        c++;
                    }

                    if (ac1.isCdiEnabled() && !ac2.isCdiEnabled()) {
                        c--;
                    } else if (!ac1.isCdiEnabled() && ac2.isCdiEnabled()) {
                        c++;
                    }

                    int size1 = ac1.getBeanContexts().size();
                    int size2 = ac2.getBeanContexts().size();
                    if (size1 > size2) {
                        c--;
                    } else if (size2 > size1) {
                        c++;
                    }

                    size1 = ac1.getBindings().size();
                    size2 = ac2.getBindings().size();
                    if (size1 > size2) {
                        c--;
                    } else if (size2 > size1) {
                        c++;
                    }

                    size1 = ac1.getWebContexts().size();
                    size2 = ac2.getWebContexts().size();
                    if (size1 > size2) {
                        c--;
                    } else if (size2 > size1) {
                        c++;
                    }

                    return c;
                }

                private boolean isBeanManagerInUse(final AppContext ac) {
                    try {
                        return ac.getWebBeansContext().getBeanManagerImpl().isInUse();
                    } catch (final Exception e) {
                        return false;
                    }
                }
            });

            //Return the most likely candidate
            return found.get(0);
        }

        Logger.getLogger(TomEEInjectionEnricher.class.getName()).log(Level.WARNING, "Failed to find AppContext for: " + className);

        return null;
    }

    @Override
    public Object[] resolve(final Method method) {
        return OpenEJBEnricher.resolve(getAppContext(method.getDeclaringClass().getName()), testClass.get(), method);
    }
}
