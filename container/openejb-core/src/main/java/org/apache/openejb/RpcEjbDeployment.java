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
package org.apache.openejb;

import java.lang.reflect.Method;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.apache.openejb.proxy.ProxyInfo;

/**
 * Interface exposed by server side EJB deployment to allow the interceptor
 * stack to interact with them.
 *
 * @version $Revision$ $Date$
 */
public interface RpcEjbDeployment extends EjbDeployment {

    /**
     * Return a proxy for the EJB's home interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to EJBContext.getEJBHome() )
     * @return the proxy for this EJB's home interface
     */
    EJBHome getEjbHome();

    /**
     * Return a proxy for the EJB's remote interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to SessionContext.getEJBObject() )
     * @return the proxy for this EJB's home interface
     */
    EJBObject getEjbObject(Object primaryKey);

    /**
     * Return a proxy for the EJB's local home interface. This can be
     * passed back to any client that wishes to access the EJB
     * (e.g. in response to a call to EJBContext.getEJBLocalHome() )
     * @return the proxy for this EJB's local home interface
     */
    EJBLocalHome getEjbLocalHome();

    /**
     * Return a proxy for the EJB's local interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to SessionContext.getEJBLocalObject() )
     * @return the proxy for this EJB's local interface
     */
    EJBLocalObject getEjbLocalObject(Object primaryKey);

    String[] getJndiNames();

    String[] getLocalJndiNames();

    ProxyInfo getProxyInfo();

    /**
     * Legacy invoke method for openejb 1.0 code
     * @param callMethod the method object for the method called on the interface
     * @param args arguemnts to the method
     * @param primKey primary key of the instance to invoke
     * @return the return value
     * @throws Throwable if a problem occurs while calling the bean
     */
    Object invoke(Method callMethod, Object[] args, Object primKey) throws Throwable;
}
