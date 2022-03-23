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

package org.apache.openejb.config.rules;

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ResourceEnvRef;

import java.util.Collection;

import static org.apache.openejb.jee.TransactionType.CONTAINER;

/**
 * Excerpt from EJB 3.0 Core spec, chapter <b>16.12. UserTransaction Interface</b>:
 * <p>
 * <i>The container must make the UserTransaction interface available to the enterprise beans that are
 * allowed to use this interface (only session and message-driven beans with bean-managed transaction
 * demarcation are allowed to use this interface)</i>
 * </p>
 *
 * @version $Rev$ $Date$
 */
public class CheckUserTransactionRefs extends ValidationBase {

    public void validate(final EjbModule ejbModule) {
        for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            if (bean.getTransactionType() == CONTAINER) {
                final Collection<ResourceEnvRef> resRefs = bean.getResourceEnvRef();
                for (final ResourceEnvRef resRef : resRefs) {
                    if ("jakarta.transaction.UserTransaction".equals(resRef.getResourceEnvRefType())) {
                        fail(bean, "userTransactionRef.forbiddenForCmtdBeans", resRef.getName());
                    }
                }
            }
        }
    }
}
