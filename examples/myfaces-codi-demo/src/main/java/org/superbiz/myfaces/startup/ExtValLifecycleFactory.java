/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.superbiz.myfaces.startup;

import org.apache.myfaces.extensions.validator.core.DefaultExtValCoreConfiguration;
import org.apache.myfaces.extensions.validator.core.ExtValCoreConfiguration;
import org.apache.myfaces.extensions.validator.core.proxy.DefaultProxyHelper;
import org.apache.myfaces.extensions.validator.core.proxy.ProxyHelper;
import org.apache.myfaces.extensions.validator.core.startup.AbstractStartupListener;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import java.util.Iterator;

//TODO remove it after upgrading to ExtVal r8+
public class ExtValLifecycleFactory extends LifecycleFactory {
    private final LifecycleFactory wrapped;

    public ExtValLifecycleFactory(LifecycleFactory wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void addLifecycle(String lifecycleId, Lifecycle lifecycle) {
        wrapped.addLifecycle(lifecycleId, lifecycle);
    }

    @Override
    public Lifecycle getLifecycle(String lifecycleId) {
        return new LifecycleWrapper(wrapped.getLifecycle(lifecycleId));
    }

    @Override
    public Iterator<String> getLifecycleIds() {
        return wrapped.getLifecycleIds();
    }

    @Override
    public LifecycleFactory getWrapped() {
        return wrapped;
    }

    private static class LifecycleWrapper extends Lifecycle {
        private final Lifecycle wrapped;
        private static boolean firstPhaseListener = true;

        private LifecycleWrapper(Lifecycle wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void addPhaseListener(PhaseListener listener) {
            if (firstPhaseListener) {
                //forced order independent of any other config
                firstPhaseListener = false;
                wrapped.addPhaseListener(new ExtValStartupListener());
            }
            wrapped.addPhaseListener(listener);
        }

        @Override
        public void execute(FacesContext context) throws FacesException {
            wrapped.execute(context);
        }

        @Override
        public PhaseListener[] getPhaseListeners() {
            return wrapped.getPhaseListeners();
        }

        @Override
        public void removePhaseListener(PhaseListener listener) {
            wrapped.removePhaseListener(listener);
        }

        @Override
        public void render(FacesContext context) throws FacesException {
            wrapped.render(context);
        }
    }

    public static class ExtValStartupListener extends AbstractStartupListener {
        @Override
        protected void init() {
            ExtValCoreConfiguration.use(new DefaultExtValCoreConfiguration() {
                @Override
                public ProxyHelper proxyHelper() {
                    return new DefaultProxyHelper() {
                        @Override
                        public boolean isProxiedClass(Class currentClass) {
                            if (currentClass == null || currentClass.getSuperclass() == null) {
                                return false;
                            }
                            return currentClass.getName().startsWith(currentClass.getSuperclass().getName()) &&
                                    currentClass.getName().contains("$$");
                        }
                    };
                }
            }, true);
        }
    }
}
