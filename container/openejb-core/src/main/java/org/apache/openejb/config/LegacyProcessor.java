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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.SessionBean;

import jakarta.ejb.MessageDrivenContext;
import jakarta.ejb.SessionContext;
import jakarta.ejb.SessionSynchronization;

import static org.apache.openejb.jee.SessionType.STATEFUL;

/**
 * @version $Rev$ $Date$
 */
public class LegacyProcessor implements DynamicDeployer {

    @Override
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final ClassLoader classLoader = ejbModule.getClassLoader();
            for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                if (bean.getEjbClass() == null) {
                    continue;
                }

                try {
                    final Class<?> clazz = classLoader.loadClass(bean.getEjbClass());

                    process(clazz, bean);
                } catch (final ClassNotFoundException e) {
                    // skip, we'll get this in validation
                }
            }

        }
        return appModule;
    }

    public static void process(final Class<?> clazz, final EnterpriseBean bean) {

        if (bean instanceof SessionBean) {
            final SessionBean sessionBean = (SessionBean) bean;

            if (sessionBean.getSessionType() == STATEFUL && SessionSynchronization.class.isAssignableFrom(clazz)) {
                try {
                    sessionBean.getAfterBegin().add(new LifecycleCallback(clazz.getMethod("afterBegin")));
                    sessionBean.getBeforeCompletion().add(new LifecycleCallback(clazz.getMethod("beforeCompletion")));
                    sessionBean.getAfterCompletion().add(new LifecycleCallback(clazz.getMethod("afterCompletion", boolean.class)));
                } catch (final NoSuchMethodException e) {
                    //Ignore, should never happen
                }
            }

            if (jakarta.ejb.SessionBean.class.isAssignableFrom(clazz)) {
                final ResourceEnvRef ref = new ResourceEnvRef("jakarta.ejb.SessionBean/sessionContext", SessionContext.class);
                final InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(clazz);
                target.setInjectionTargetName("sessionContext");
                ref.getInjectionTarget().add(target);

                sessionBean.getResourceEnvRef().add(ref);
            }
        }

        if (bean instanceof MessageDrivenBean) {
            final MessageDrivenBean messageDrivenBean = (MessageDrivenBean) bean;

            if (jakarta.ejb.MessageDrivenBean.class.isAssignableFrom(clazz)) {
                final ResourceEnvRef ref = new ResourceEnvRef("jakarta.ejb.MessageDrivenBean/messageDrivenContext", MessageDrivenContext.class);
                final InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(clazz);
                target.setInjectionTargetName("messageDrivenContext");
                ref.getInjectionTarget().add(target);

                messageDrivenBean.getResourceEnvRef().add(ref);
            }
        }
    }

}
