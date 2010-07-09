/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.config;

import javax.ejb.SessionSynchronization;
import javax.ejb.SessionContext;
import javax.ejb.MessageDrivenContext;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.MessageDrivenBean;
import static org.apache.openejb.jee.SessionType.STATEFUL;

/**
 * @version $Rev$ $Date$
 */
public class LegacyProcessor implements DynamicDeployer {

    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            ClassLoader classLoader = ejbModule.getClassLoader();
            for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                if (bean.getEjbClass() == null) continue;

                try {
                    Class<?> clazz = classLoader.loadClass(bean.getEjbClass());

                    process(clazz, bean);
                } catch (ClassNotFoundException e) {
                    // skip, we'll get this in validation
                }
            }

        }
        return appModule;
    }

    public static void process(Class<?> clazz, EnterpriseBean bean) {

        if (bean instanceof SessionBean) {
            SessionBean sessionBean = (SessionBean) bean;

            if (sessionBean.getSessionType() == STATEFUL && SessionSynchronization.class.isAssignableFrom(clazz)) {
                sessionBean.getAfterBegin().add(new LifecycleCallback(clazz.getName(), "afterBegin"));
                sessionBean.getBeforeCompletion().add(new LifecycleCallback(clazz.getName(), "beforeCompletion"));
                sessionBean.getAfterCompletion().add(new LifecycleCallback(clazz.getName(), "afterCompletion"));
            }

            if (javax.ejb.SessionBean.class.isAssignableFrom(clazz)) {
                final ResourceEnvRef ref = new ResourceEnvRef("javax.ejb.SessionBean/sessionContext", SessionContext.class);
                InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(clazz);
                target.setInjectionTargetName("sessionContext");
                ref.getInjectionTarget().add(target);

                sessionBean.getResourceEnvRef().add(ref);
            }
        }

        if (bean instanceof MessageDrivenBean) {
            MessageDrivenBean messageDrivenBean = (MessageDrivenBean) bean;

            if (javax.ejb.MessageDrivenBean.class.isAssignableFrom(clazz)) {
                final ResourceEnvRef ref = new ResourceEnvRef("javax.ejb.MessageDrivenBean/messageDrivenContext", MessageDrivenContext.class);
                InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(clazz);
                target.setInjectionTargetName("messageDrivenContext");
                ref.getInjectionTarget().add(target);

                messageDrivenBean.getResourceEnvRef().add(ref);
            }
        }
    }
}
