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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.proxy.ProxyFactory;
import org.apache.openejb.util.proxy.ProxyManager;

import javax.naming.InitialContext;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Map;

public class AssemblerTool {

    public static final Class PROXY_FACTORY = org.apache.openejb.util.proxy.ProxyFactory.class;
    public static final Class SECURITY_SERVICE = org.apache.openejb.spi.SecurityService.class;
    public static final Class TRANSACTION_SERVICE = org.apache.openejb.spi.TransactionService.class;
    public static final Class CONNECTION_MANAGER = javax.resource.spi.ConnectionManager.class;
    public static final Class CONNECTOR = javax.resource.spi.ManagedConnectionFactory.class;

    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");
    protected static final SafeToolkit toolkit = SafeToolkit.getToolkit("AssemblerTool");
    protected static final Map<String, ClassLoader> codebases = new HashMap<String, ClassLoader>();

    protected Properties props;

    static {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        codebases.put("CLASSPATH", cl);

        System.setProperty("noBanner", "true");
    }

    /*
    TODO: The Exception Handling here isn't up-to-date and doesn't
    use a message number. Message numbers allow the message text to
    be internationalized.
    */
    public InitialContext assembleRemoteJndiContext(JndiContextInfo context)
            throws org.apache.openejb.OpenEJBException {
        try {
            InitialContext ic = new InitialContext(context.properties);
            return ic;
        } catch (javax.naming.NamingException ne) {

            throw new org.apache.openejb.OpenEJBException("The remote JNDI EJB references for remote-jndi-contexts = " + context.jndiContextId + "+ could not be resolved.", ne);
        }
    }

    public ConnectionManager assembleConnectionManager(ConnectionManagerInfo cmInfo)
            throws OpenEJBException, java.lang.Exception {
        /*TODO: Add better exception handling, this method throws java.lang.Exception,
         which is not very specific. Only a very specific OpenEJBException should be
         thrown.
         */
        Class managerClass = SafeToolkit.loadClass(cmInfo.className, cmInfo.codebase);

        checkImplementation(CONNECTION_MANAGER, managerClass, "ConnectionManager", cmInfo.connectionManagerId);

        ConnectionManager connectionManager = (ConnectionManager) toolkit.newInstance(managerClass);

        if (cmInfo.properties != null) {
            Properties clonedProps = (Properties) (this.props.clone());
            clonedProps.putAll(cmInfo.properties);
            applyProperties(connectionManager, clonedProps);
        }

        return connectionManager;
    }

    public ManagedConnectionFactory assembleManagedConnectionFactory(ManagedConnectionFactoryInfo mngedConFactInfo)
            throws org.apache.openejb.OpenEJBException, java.lang.Exception {

        ManagedConnectionFactory managedConnectionFactory = null;
        try {
            Class factoryClass = SafeToolkit.loadClass(mngedConFactInfo.className, mngedConFactInfo.codebase);
            checkImplementation(CONNECTOR, factoryClass, "Connector", mngedConFactInfo.id);

            managedConnectionFactory = (ManagedConnectionFactory) toolkit.newInstance(factoryClass);
        } catch (Exception e) {
            throw new OpenEJBException("Could not instantiate Connector '" + mngedConFactInfo.id + "'.", e);
        }

        try {

            if (mngedConFactInfo.properties != null) {
                Properties clonedProps = (Properties) (this.props.clone());
                clonedProps.putAll(mngedConFactInfo.properties);
                applyProperties(managedConnectionFactory, clonedProps);
            }
        } catch (java.lang.reflect.InvocationTargetException ite) {
            throw new OpenEJBException("Could not initialize Connector '" + mngedConFactInfo.id + "'.", ite.getTargetException());
        } catch (Exception e) {

            throw new OpenEJBException("Could not initialize Connector '" + mngedConFactInfo.id + "'.", e);
        }

        return managedConnectionFactory;
    }

    public void applyProxyFactory(IntraVmServerInfo ivmInfo) throws OpenEJBException {
        Class factoryClass = SafeToolkit.loadClass(ivmInfo.proxyFactoryClassName, ivmInfo.codebase);

        checkImplementation(PROXY_FACTORY, factoryClass, "ProxyFactory", ivmInfo.factoryName);

        ProxyFactory factory = (ProxyFactory) toolkit.newInstance(factoryClass);

        factory.init(ivmInfo.properties);
        ProxyManager.registerFactory("ivm_server", factory);
        ProxyManager.setDefaultFactory("ivm_server");

    }

    public void applyProperties(Object target, Properties props) throws java.lang.reflect.InvocationTargetException, java.lang.IllegalAccessException, java.lang.NoSuchMethodException {
        if (props != null /*&& props.size()>0*/) {
            Method method = target.getClass().getMethod("init", Properties.class);
            method.invoke(target, props);
        }
    }

    public static void applyTransactionAttributes(CoreDeploymentInfo deploymentInfo, List<MethodTransactionInfo> mtis) throws OpenEJBException {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         */
        for (MethodTransactionInfo transInfo : mtis) {
            for (MethodInfo methodInfo : transInfo.methods) {

                if (methodInfo.ejbDeploymentId == null || methodInfo.ejbDeploymentId.equals(deploymentInfo.getDeploymentID())) {
                    if (!deploymentInfo.isBeanManagedTransaction()) {

                        List<Method> methods = new ArrayList<Method>();

                        if (methodInfo.methodIntf == null) {
                            if (deploymentInfo.getRemoteInterface() != null) {
                                resolveMethods(methods, deploymentInfo.getRemoteInterface(), methodInfo);
                            }
                            if (deploymentInfo.getHomeInterface() != null) {
                                resolveMethods(methods, deploymentInfo.getHomeInterface(), methodInfo);
                            }
                            if(deploymentInfo.getMdbInterface() != null) {
                            	resolveMethods(methods, deploymentInfo.getMdbInterface(), methodInfo);
                            }

                        } else if (methodInfo.methodIntf.equals("Home")) {
                            resolveMethods(methods, deploymentInfo.getHomeInterface(), methodInfo);
                        } else if (methodInfo.methodIntf.equals("Remote")) {
                            resolveMethods(methods, deploymentInfo.getRemoteInterface(), methodInfo);
                        } else {

                        }

                        for (Method method : methods) {
                            if ((method.getDeclaringClass() == javax.ejb.EJBObject.class ||
                                    method.getDeclaringClass() == javax.ejb.EJBHome.class) &&
                                    !method.getName().equals("remove")) {
                                continue;
                            }
                            deploymentInfo.setMethodTransactionAttribute(method, transInfo.transAttribute);
                        }
                    }
                }
            }
        }

    }

    public void applySecurityRoleReference(CoreDeploymentInfo deployment, EnterpriseBeanInfo beanInfo, AssemblerTool.RoleMapping roleMapping) {
        for (SecurityRoleReferenceInfo roleRef : beanInfo.securityRoleReferences) {
            List<String> physicalRoles = roleMapping.getPhysicalRoles(roleRef.roleLink);
            deployment.addSecurityRoleReference(roleRef.roleName, physicalRoles);
        }
    }

    public static void applyMethodPermissions(CoreDeploymentInfo deployment, List<MethodPermissionInfo> permissions) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */
        for (MethodPermissionInfo methodPermission : permissions) {
            for (MethodInfo methodInfo : methodPermission.methods) {

                if (methodInfo.ejbDeploymentId == null || methodInfo.ejbDeploymentId.equals(deployment.getDeploymentID())) {

                    List<Method> methods = resolveMethodInfo(methodInfo, deployment);
                    for (Method method : methods) {
                        deployment.appendMethodPermissions(method, methodPermission.roleNames);
                    }
                }

            }
        }
    }

    public static void applyMethodPermissions(CoreDeploymentInfo deployment, List<MethodPermissionInfo> permissions, AssemblerTool.RoleMapping roleMapping) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */
        for (MethodPermissionInfo permission : permissions) {
            applyRoleMappings(permission, roleMapping);
        }
        applyMethodPermissions(deployment, permissions);
    }

    /*
    * Makes a copy of the MethodPermissionObject and then replaces the logical roles of the MethodPermissionInfo copy
    * with the physical roles in the roleMapping object.
    * If the RoleMapping object doesn't have a set of physical roles for a particular logical role in the
    * MethodPermissionInfo, then the logical role is used.
    *
    * @param methodPermission the permission object to be copies and updated.
    * @param roleMapping encapsulates the mapping of many logical roles to their equivalent physical roles.
    * @see org.apache.openejb.assembler.classic.MethodPermissionInfo
    * @see org.apache.openejb.assembler.classic.AssemblerTool.RoleMapping
    */
    public static void applyRoleMappings(MethodPermissionInfo methodPermission,
                                  AssemblerTool.RoleMapping roleMapping) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */

        // Map the logical roles to physical roles
        Set<String> physicalRoles = new LinkedHashSet<String>();
        for (String roleName : methodPermission.roleNames) {
            List<String> physicals = roleMapping.getPhysicalRoles(roleName);
            if (physicals != null) {
                physicalRoles.addAll(physicals);
            } else {
                // if no physical roles are mapped use logical role
                physicalRoles.add(roleName);
            }
        }

        // replace the existing role for this permission with the new roles
        methodPermission.roleNames.clear();
        methodPermission.roleNames.addAll(physicalRoles);
    }

    public static class RoleMapping {
        private Map<String, List<String>> map = new HashMap<String, List<String>>();

        public RoleMapping(List<RoleMappingInfo> roleMappingInfos) {
            for (RoleMappingInfo mapping : roleMappingInfos) {
                for (String logincalRoleName : mapping.logicalRoleNames) {
                    map.put(logincalRoleName, mapping.physicalRoleNames);
                }
            }
        }

        public String[] logicalRoles() {
            return (String[]) map.keySet().toArray();
        }

        public ArrayList<String> getPhysicalRoles(String logicalRole) {
            List<String> roles = map.get(logicalRole);
            return roles != null ? new ArrayList<String>(roles) : null;
        }

    }

    protected static List<Method> resolveMethodInfo(MethodInfo methodInfo, org.apache.openejb.core.CoreDeploymentInfo di) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */

        List<Method> methods = new ArrayList<Method>();

        Class remote = di.getRemoteInterface();
        Class home = di.getHomeInterface();
        if (methodInfo.methodIntf == null) {
            resolveMethods(methods, remote, methodInfo);
            resolveMethods(methods, home, methodInfo);
        } else if (methodInfo.methodIntf.equals("Remote")) {
            resolveMethods(methods, remote, methodInfo);
        } else {
            resolveMethods(methods, home, methodInfo);
        }
        return methods;
    }

    protected static void resolveMethods(List<Method> methods, Class intrface, MethodInfo mi)
            throws SecurityException {
        /*TODO: Add better exception handling. There is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */

        if (mi.methodName.equals("*")) {
            Method[] mthds = intrface.getMethods();
            for (int i = 0; i < mthds.length; i++)
                methods.add(mthds[i]);
        } else if (mi.methodParams != null) {// there are paramters specified
            try {
                List<Class> params = new ArrayList<Class>();
                ClassLoader cl = intrface.getClassLoader();
                for (String methodParam : mi.methodParams) {
                    try {
                        params.add(getClassForParam(methodParam, cl));
                    } catch (ClassNotFoundException cnfe) {

                    }
                }
                Method m = intrface.getMethod(mi.methodName, params.toArray(new Class[params.size()]));
                methods.add(m);
            } catch (NoSuchMethodException nsme) {
                /*
                Do nothing.  Exceptions are not only possible they are expected to be a part of normall processing.
                Normally exception handling should not be a part of business logic, but server start up doesn't need to be
                as peformant as server runtime, so its allowed.
                */
            }
        } else {// no paramters specified so may be several methods
            Method[] ms = intrface.getMethods();
            for (int i = 0; i < ms.length; i++) {
                if (ms[i].getName().equals(mi.methodName))
                    methods.add(ms[i]);
            }
        }

    }

    protected void checkImplementation(Class intrfce, Class factory, String serviceType, String serviceName) throws OpenEJBException {
        if (!intrfce.isAssignableFrom(factory)) {
            handleException("init.0100", serviceType, serviceName, factory.getName(), intrfce.getName());
        }
    }

    private static java.lang.Class getClassForParam(java.lang.String className, ClassLoader cl) throws ClassNotFoundException {
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        if (className.equals("int")) {
            return java.lang.Integer.TYPE;
        } else if (className.equals("double")) {
            return java.lang.Double.TYPE;
        } else if (className.equals("long")) {
            return java.lang.Long.TYPE;
        } else if (className.equals("boolean")) {
            return java.lang.Boolean.TYPE;
        } else if (className.equals("float")) {
            return java.lang.Float.TYPE;
        } else if (className.equals("char")) {
            return java.lang.Character.TYPE;
        } else if (className.equals("short")) {
            return java.lang.Short.TYPE;
        } else if (className.equals("byte")) {
            return java.lang.Byte.TYPE;
        } else
            return cl.loadClass(className);

    }

    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public void handleException(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public void handleException(String errorCode, Object arg0, Object arg1, Object arg2) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2));
    }

    public void handleException(String errorCode, Object arg0, Object arg1) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1));
    }

    public void handleException(String errorCode, Object arg0) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0));
    }

    public void handleException(String errorCode) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode));
    }

    /*------------------------------------------------------*/
    /*  Methods for logging exceptions that are noteworthy  */
    /*  but not bad enough to stop the container system.    */
    /*------------------------------------------------------*/
    public void logWarning(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) {
        System.out.println("Warning: " + messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public void logWarning(String errorCode, Object arg0, Object arg1, Object arg2) {
        System.out.println("Warning: " + messages.format(errorCode, arg0, arg1, arg2));
    }

    public void logWarning(String errorCode, Object arg0, Object arg1) {
        System.out.println("Warning: " + messages.format(errorCode, arg0, arg1));
    }

    public void logWarning(String errorCode, Object arg0) {
        System.out.println("Warning: " + messages.format(errorCode, arg0));
    }

    public void logWarning(String errorCode) {
        System.out.println("Warning: " + messages.format(errorCode));
    }
}