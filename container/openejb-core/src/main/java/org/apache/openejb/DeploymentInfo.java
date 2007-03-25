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

import org.apache.openejb.core.timer.EjbTimerService;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import javax.naming.Context;
import javax.security.auth.Subject;

public interface DeploymentInfo {

    final public static byte TX_NEVER = (byte) 0;

    final public static byte TX_NOT_SUPPORTED = (byte) 1;

    final public static byte TX_SUPPORTS = (byte) 2;

    final public static byte TX_MANDITORY = (byte) 3;

    final public static byte TX_REQUIRED = (byte) 4;

    final public static byte TX_REQUIRES_NEW = (byte) 5;

    final public static byte TX_MAX = (byte) 5;

    final public static String AC_CREATE_EJBHOME = "create.ejbhome";

    public BeanType getComponentType();

    public InterfaceType getInterfaceType(Class clazz);
    
    public byte getTransactionAttribute(Method method);

    public Collection<String> getAuthorizedRoles(Method method);

    public String [] getAuthorizedRoles(String action);

    public Container getContainer();

    public Object getDeploymentID();

    public String getEjbName();

    public String getModuleID();

    public String getRunAs();

    public String getSecurityRole(String securityRoleReference);

    public boolean isBeanManagedTransaction();

    public Class getHomeInterface();

    public Class getLocalHomeInterface();

    public Class getLocalInterface();

    public Class getRemoteInterface();

    public Class getBeanClass();

    public Class getPrimaryKeyClass();

    public Class getBusinessLocalInterface();

    public Class getBusinessRemoteInterface();

    public Class getServiceEndpointInterface();

    public String getPrimaryKeyField();

    public Context getJndiEnc();

    public boolean isReentrant();

    public Class getInterface(InterfaceType interfaceType);

    public Class getMdbInterface();

    public Map<String, String> getActivationProperties();

    public ClassLoader getClassLoader();

    public List<Method> getAroundInvoke();

    public List<Method> getPostConstruct();

    public List<Method> getPreDestroy();

    public List<Method> getPostActivate();

    public List<Method> getPrePassivate();

    public List<Injection> getInjections();

    public void setContainer(Container container);

    public Method getEjbTimeout();

    public EjbTimerService getEjbTimerService();

    public interface BusinessLocalHome extends javax.ejb.EJBLocalHome {
        Object create();
    }

    public interface BusinessRemoteHome extends javax.ejb.EJBHome {
        Object create();
    }

    public <T> T get(Class<T> type);

    public <T> T set(Class<T> type, T value);

}
