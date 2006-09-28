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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.slsb;

import javax.ejb.SessionBean;

import org.apache.openejb.EJBInstanceFactory;
import org.apache.openejb.EJBInstanceFactoryImpl;
import org.apache.openejb.InstanceContextFactory;
import org.apache.openejb.StatelessEjbDeployment;
import org.apache.openejb.StatelessEjbContainer;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.proxy.EJBProxyFactory;

/**
 * @version $Revision$ $Date$
 */
public class StatelessInstanceContextFactory implements InstanceContextFactory {
    private final EJBInstanceFactory instanceFactory;
    private final EJBProxyFactory proxyFactory;
    private final StatelessEjbDeployment statelessEjbDeployment;
    private final StatelessEjbContainer statelessEjbContainer;

    public StatelessInstanceContextFactory(StatelessEjbDeployment statelessEjbDeployment,
            StatelessEjbContainer statelessEjbContainer,
            EJBProxyFactory proxyFactory) {
        this.instanceFactory = new EJBInstanceFactoryImpl(statelessEjbDeployment.getBeanClass());
        this.proxyFactory = proxyFactory;
        this.statelessEjbDeployment = statelessEjbDeployment;
        this.statelessEjbContainer = statelessEjbContainer;
    }

    public EJBInstanceContext newInstance() throws Exception {
        return new StatelessInstanceContext(statelessEjbDeployment,
                statelessEjbContainer,
                (SessionBean) instanceFactory.newInstance(),
                proxyFactory
        );
    }
}
