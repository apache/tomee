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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;

/**
 * @version $Rev$ $Date$
 */
public class AppModulePreProcessor implements DynamicDeployer {

    private SessionSynchronizationProcessor sessionSynchronizationProcessor = new SessionSynchronizationProcessor();

    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            ClassLoader classLoader = ejbModule.getClassLoader();
            for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                if (bean.getEjbClass() == null) {
                    continue;
                }
                Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(bean.getEjbClass());
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load bean class: " + bean.getEjbClass(), e);
                }
                sessionSynchronizationProcessor.process(clazz, bean);
            }

        }
        return appModule;
    }

    public static class SessionSynchronizationProcessor {

        public void process(Class<?> clazz, EnterpriseBean enterpriseBean) {
            if (!(enterpriseBean instanceof SessionBean)) {
                return;
            }
            SessionBean sessionBean = (SessionBean) enterpriseBean;
            if (sessionBean.getSessionType() != SessionType.STATEFUL) {
                return;
            }
            if (SessionSynchronization.class.isAssignableFrom(clazz)) {
                sessionBean.getAfterBegin().add(new LifecycleCallback(clazz.getName(), "afterBegin"));
                sessionBean.getBeforeCompletion().add(new LifecycleCallback(clazz.getName(), "beforeCompletion"));
                sessionBean.getAfterCompletion().add(new LifecycleCallback(clazz.getName(), "afterCompletion"));
            } else {
                if (sessionBean.getAfterBeginMethod() != null) {
                    sessionBean.getAfterBegin().add(new LifecycleCallback(clazz.getName(), sessionBean.getAfterBeginMethod().getMethodName()));
                }
                if (sessionBean.getBeforeCompletionMethod() != null) {
                    sessionBean.getBeforeCompletion().add(new LifecycleCallback(clazz.getName(), sessionBean.getBeforeCompletionMethod().getMethodName()));
                }
                if (sessionBean.getAfterCompletionMethod() != null) {
                    sessionBean.getAfterCompletion().add(new LifecycleCallback(clazz.getName(), sessionBean.getAfterCompletionMethod().getMethodName()));
                }
            }
        }
    }
}
