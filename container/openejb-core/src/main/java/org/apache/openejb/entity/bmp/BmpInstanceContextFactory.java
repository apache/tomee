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
package org.apache.openejb.entity.bmp;

import java.io.Serializable;
import javax.ejb.EntityBean;

import org.apache.openejb.BmpEjbContainer;
import org.apache.openejb.BmpEjbDeployment;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBInstanceFactory;
import org.apache.openejb.EJBInstanceFactoryImpl;
import org.apache.openejb.InstanceContextFactory;
import org.apache.openejb.proxy.EJBProxyFactory;

/**
 * @version $Revision$ $Date$
 */
public class BmpInstanceContextFactory implements InstanceContextFactory, Serializable {
    private static final long serialVersionUID = 1309544723932462002L;
    private final BmpEjbDeployment bmpEjbDeployment;
    private final BmpEjbContainer bmpEjbContainer;
    private final EJBInstanceFactory instanceFactory;
    private final transient EJBProxyFactory proxyFactory;

    public BmpInstanceContextFactory(BmpEjbDeployment bmpEjbDeployment,
            BmpEjbContainer bmpEjbContainer,
            EJBProxyFactory proxyFactory) {
        this.bmpEjbDeployment = bmpEjbDeployment;
        this.bmpEjbContainer = bmpEjbContainer;
        this.instanceFactory = new EJBInstanceFactoryImpl(bmpEjbDeployment.getBeanClass());
        this.proxyFactory = proxyFactory;
    }

    public EJBInstanceContext newInstance() throws Exception {
        return new BmpInstanceContext(bmpEjbDeployment,
                bmpEjbContainer,
                (EntityBean) instanceFactory.newInstance(),
                proxyFactory
        );
    }
}
