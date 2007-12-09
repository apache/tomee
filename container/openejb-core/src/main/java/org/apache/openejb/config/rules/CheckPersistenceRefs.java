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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.rules;

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.PersistenceContextType;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.BeanType;

/**
 * @version $Rev$ $Date$
 */
public class CheckPersistenceRefs extends ValidationBase {
    public void validate(EjbModule ejbModule) {

        for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {

            String beanType = getType(bean);
            if (beanType.equals("Stateful")) continue; // skip statefuls

            for (PersistenceContextRef ref : bean.getPersistenceContextRef()) {
                if (isExtented(ref)) {
                    String refName = ref.getName();
                    String prefix = bean.getEjbClass() + "/";
                    if (refName.startsWith(prefix)) {
                        refName = refName.substring(prefix.length());
                    }
                    fail(bean, "persistenceContextExtented.nonStateful", refName, beanType);
                }

            }
        }
    }

    private boolean isExtented(PersistenceContextRef ref) {
        PersistenceContextType type = ref.getPersistenceContextType();
        return type != null && type.equals(PersistenceContextType.EXTENDED);
    }

    private String getType(EnterpriseBean bean) {
        if (bean instanceof SessionBean) {
            SessionBean sessionBean = (SessionBean) bean;
            switch(sessionBean.getSessionType()){
                case STATEFUL: return "Stateful";
                case STATELESS: return "Stateless";
                default: throw new IllegalArgumentException("Uknown SessionBean type "+bean.getClass());
            }
        } else if (bean instanceof MessageDrivenBean) return "MessageDriven";
        else if (bean instanceof EntityBean) return "EJB 2.1 Entity";
        else throw new IllegalArgumentException("Uknown bean type "+bean.getClass());
    }
}
