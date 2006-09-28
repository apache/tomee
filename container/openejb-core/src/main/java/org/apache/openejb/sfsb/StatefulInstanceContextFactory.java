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
package org.apache.openejb.sfsb;

import java.io.Serializable;
import javax.ejb.SessionBean;

import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBInstanceFactory;
import org.apache.openejb.EJBInstanceFactoryImpl;
import org.apache.openejb.InstanceContextFactory;
import org.apache.openejb.StatefulEjbContainer;
import org.apache.openejb.StatefulEjbDeployment;
import org.apache.openejb.proxy.EJBProxyFactory;

/**
 * @version $Revision$ $Date$
 */
public class StatefulInstanceContextFactory implements InstanceContextFactory, Serializable {
    private static final long serialVersionUID = 1363872823647038549L;
    protected final StatefulEjbContainer statefulEjbContainer;
    protected final StatefulEjbDeployment statefulEjbDeployment;
    private final EJBInstanceFactory instanceFactory;
    protected final transient EJBProxyFactory proxyFactory;

    public StatefulInstanceContextFactory(StatefulEjbDeployment statefulEjbDeployment,
            StatefulEjbContainer statefulEjbContainer,
            EJBProxyFactory proxyFactory) {
        this.statefulEjbContainer = statefulEjbContainer;
        this.instanceFactory = new EJBInstanceFactoryImpl(statefulEjbDeployment.getBeanClass());
        this.proxyFactory = proxyFactory;
        this.statefulEjbDeployment = statefulEjbDeployment;
    }

    public EJBInstanceContext newInstance() throws Exception {
        return new StatefulInstanceContext(
                statefulEjbDeployment,
                statefulEjbContainer,
                createInstance(),
                createInstanceId(),
                proxyFactory
        );
    }

    protected SessionBean createInstance() throws Exception {
        return (SessionBean) instanceFactory.newInstance();
    }

    private static int nextId;

    private Object createInstanceId() {
        synchronized (this) {
            return new StatefulInstanceId(nextId++);
        }
    }

    private static class StatefulInstanceId implements Serializable {
        private static final long serialVersionUID = 6798822247641308803L;
        private final int id;

        public StatefulInstanceId(int id) {
            this.id = id;
        }

        public int hashCode() {
            return id;
        }

        public boolean equals(Object object) {
            if (object instanceof StatefulInstanceId) {
                return id == ((StatefulInstanceId) object).id;
            }
            return false;
        }

        public String toString() {
            return "StatefulInstanceId: " + id;
        }
    }
}
