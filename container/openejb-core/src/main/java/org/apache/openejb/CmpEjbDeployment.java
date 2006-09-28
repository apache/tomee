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

import org.apache.openejb.cache.InstanceFactory;
import org.apache.openejb.cache.InstancePool;
import org.apache.openejb.dispatch.EJBTimeoutOperation;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.MethodHelper;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.entity.BusinessMethod;
import org.apache.openejb.entity.EntityInstanceFactory;
import org.apache.openejb.entity.HomeMethod;
import org.apache.openejb.entity.cmp.Cmp1Bridge;
import org.apache.openejb.entity.cmp.CmpCreateMethod;
import org.apache.openejb.entity.cmp.CmpField;
import org.apache.openejb.entity.cmp.CmpFieldGetter;
import org.apache.openejb.entity.cmp.CmpFieldSetter;
import org.apache.openejb.entity.cmp.CmpFinder;
import org.apache.openejb.entity.cmp.CmpInstanceContextFactory;
import org.apache.openejb.entity.cmp.CmpRemoveMethod;
import org.apache.openejb.entity.cmp.EjbCmpEngine;
import org.apache.openejb.entity.cmp.ModuleCmpEngine;
import org.apache.openejb.entity.cmp.SelectMethod;
import org.apache.openejb.entity.cmp.SelectQuery;
import org.apache.openejb.proxy.ProxyInfo;
import org.apache.openejb.util.SoftLimitedInstancePool;
import org.apache.openejb.util.Index;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.security.auth.Subject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * @version $Revision$ $Date$
 */
public class CmpEjbDeployment extends AbstractRpcDeployment implements EntityEjbDeployment {
    private final InstancePool instancePool;
    private final boolean reentrant;
    private final EjbCmpEngine ejbCmpEngine;
    private final Cmp1Bridge cmp1Bridge;
    private final Index dispatchIndex;

    public CmpEjbDeployment(String containerId,
                            String ejbName,

                            String homeInterfaceName,
                            String remoteInterfaceName,
                            String localHomeInterfaceName,
                            String localInterfaceName,
                            String primaryKeyClassName,
                            String beanClassName,
                            ClassLoader classLoader,

                            CmpEjbContainer ejbContainer,

                            String[] jndiNames,
                            String[] localJndiNames,

                            boolean securityEnabled,
                            String policyContextId,
                            Subject defaultSubject,
                            Subject runAs,

                            SortedMap transactionPolicies,

                            Map componentContext,

                            // connector stuff
                            Set unshareableResources,
                            Set applicationManagedSecurityResources,

                            ModuleCmpEngine moduleCmpEngine,
                            boolean cmp2,
                            boolean reentrant) throws Exception {

        this(containerId,
                ejbName,
                loadClass(homeInterfaceName, classLoader, "home interface"),
                loadClass(remoteInterfaceName, classLoader, "remote interface"),
                loadClass(localHomeInterfaceName, classLoader, "local home interface"),
                loadClass(localInterfaceName, classLoader, "local interface"),
                loadClass(primaryKeyClassName, classLoader, "primary key class"),
                loadClass(beanClassName, classLoader, "bean class"),
                classLoader,
                ejbContainer,
                jndiNames,
                localJndiNames,
                securityEnabled,
                policyContextId,
                defaultSubject,
                runAs,
                transactionPolicies,
                componentContext,
                unshareableResources,
                applicationManagedSecurityResources,
                moduleCmpEngine,
                cmp2,
                reentrant);
    }

    public CmpEjbDeployment(String containerId,
                            String ejbName,

                            Class homeInterface,
                            Class remoteInterface,
                            Class localHomeInterface,
                            Class localInterface,
                            Class primaryKeyClass,
                            Class beanClass,
                            ClassLoader classLoader,

                            CmpEjbContainer ejbContainer,

                            String[] jndiNames,
                            String[] localJndiNames,

                            boolean securityEnabled,
                            String policyContextId,
                            Subject defaultSubject,
                            Subject runAs,

                            SortedMap transactionPolicies,

                            Map componentContext,

                            // connector stuff
                            Set unshareableResources,
                            Set applicationManagedSecurityResources,

                            ModuleCmpEngine moduleCmpEngine,
                            boolean cmp2,
                            boolean reentrant) throws Exception {

        super(containerId,
                ejbName,
                new ProxyInfo(EJBComponentType.CMP_ENTITY,
                        containerId,
                        homeInterface,
                        remoteInterface,
                        localHomeInterface,
                        localInterface,
                        null,
                        primaryKeyClass),
                beanClass,
                classLoader,
                new RpcSignatureIndexBuilder(beanClass, homeInterface, remoteInterface, localHomeInterface, localInterface, null),
                ejbContainer,
                jndiNames,
                localJndiNames,
                securityEnabled,
                policyContextId,
                defaultSubject,
                runAs,
                false,
                transactionPolicies,
                componentContext,
                unshareableResources,
                applicationManagedSecurityResources);

        this.reentrant = reentrant;

        ejbCmpEngine = moduleCmpEngine.getEjbCmpEngine(ejbName, getBeanClass(), getProxyInfo());
        if (ejbCmpEngine == null) {
            throw new Exception("Module cmp engine does not contain an engine for ejb: " + ejbName);
        }

        dispatchIndex = buildDispatchMethodMap();

        Map instanceMap = null;
        if (cmp2) {
            instanceMap = buildInstanceMap(getBeanClass());
            cmp1Bridge = null;
        } else {
            cmp1Bridge = new Cmp1Bridge(getBeanClass(), ejbCmpEngine.getCmpFields());
        }

        InstanceContextFactory contextFactory = new CmpInstanceContextFactory(this,
                ejbContainer,
                proxyFactory,
                cmp2,
                instanceMap);

        InstanceFactory instanceFactory = new EntityInstanceFactory(contextFactory);

        // todo the pools should be created by an InstancePoolFactory in the interceptor stack
        instancePool = new SoftLimitedInstancePool(instanceFactory, 1);

        // todo we should have an instance cache for cmp beans
    }

    public VirtualOperation getVirtualOperation(int methodIndex) {
        VirtualOperation vop = (VirtualOperation) dispatchIndex.get(methodIndex);
        return vop;
    }

    private Index buildDispatchMethodMap() throws Exception {
        Class beanClass = getBeanClass();

        Index dispatchIndex = new Index(signatures);

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            InterfaceMethodSignature timeoutSignature = new InterfaceMethodSignature("ejbTimeout", new Class[]{Timer.class}, false);
            dispatchIndex.put(timeoutSignature, EJBTimeoutOperation.INSTANCE);
        }

        // build an index from the finder method signatures to the select query
        Map finderIndex = new TreeMap();
        Set selectQueries = ejbCmpEngine.getSelectQueries();
        for (Iterator iterator = selectQueries.iterator(); iterator.hasNext();) {
            SelectQuery selectQuery = (SelectQuery) iterator.next();
            if (!selectQuery.getMethodName().startsWith("ejbSelect")) {
                InterfaceMethodSignature signature = new InterfaceMethodSignature(selectQuery.getMethodName(), selectQuery.getParameterTypes(), true);

                finderIndex.put(signature, selectQuery);
            }
        }

        MethodSignature removeSignature = new MethodSignature("ejbRemove");
        CmpRemoveMethod removeVop = new CmpRemoveMethod(beanClass, removeSignature, ejbCmpEngine);
        for (Iterator iterator = dispatchIndex.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) entry.getKey();
            String methodName = methodSignature.getMethodName();
            if (methodSignature.isHomeMethod()) {
                if (methodName.startsWith("create")) {
                    String baseName = methodName.substring(6);
                    MethodSignature createSignature = new MethodSignature("ejbCreate" + baseName, methodSignature.getParameterTypes());
                    MethodSignature postCreateSignature = new MethodSignature("ejbPostCreate" + baseName, methodSignature.getParameterTypes());
                    entry.setValue(new CmpCreateMethod(beanClass, cmp1Bridge, createSignature, postCreateSignature, ejbCmpEngine));
                } else if (methodName.startsWith("remove")) {
                    entry.setValue(removeVop);
                } else if (methodName.startsWith("find")) {
                    SelectQuery selectQuery = (SelectQuery) finderIndex.get(methodSignature);
                    if (selectQuery == null) {
                        throw new IllegalStateException("No ejbql specified for finder method " + methodSignature + " on CMP entity " + getEjbName());
                    }
                    entry.setValue(new CmpFinder(selectQuery));
                } else {
                    MethodSignature homeSignature = new MethodSignature("ejbHome" + MethodHelper.capitalize(methodName), methodSignature.getParameterTypes());
                    entry.setValue(new HomeMethod(beanClass, homeSignature));
                }
            } else {
                if (methodName.startsWith("remove") && methodSignature.getParameterTypes().length == 0) {
                    entry.setValue(removeVop);
                } else if (!methodName.startsWith("ejb") &&
                        !methodName.equals("setEntityContext") &&
                        !methodName.equals("unsetEntityContext")) {
                    MethodSignature signature = new MethodSignature(methodName, methodSignature.getParameterTypes());
                    entry.setValue(new BusinessMethod(beanClass, signature));
                }
            }
        }

        return dispatchIndex;
    }


    private Map buildInstanceMap(Class beanClass) {
        Map instanceMap;
        instanceMap = new HashMap();

        Set cmpFields = ejbCmpEngine.getCmpFields();
        for (Iterator iterator = cmpFields.iterator(); iterator.hasNext();) {
            CmpField cmpField = (CmpField) iterator.next();
            String fieldName = cmpField.getName();
            String baseName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            try {
                Method getter = beanClass.getMethod("get" + baseName, null);
                Method setter = beanClass.getMethod("set" + baseName, new Class[]{getter.getReturnType()});

                instanceMap.put(new MethodSignature(getter), new CmpFieldGetter(cmpField));
                instanceMap.put(new MethodSignature(setter), new CmpFieldSetter(cmpField));
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing accessor for field " + fieldName);
            }
        }

        Set selectQueries = ejbCmpEngine.getSelectQueries();
        for (Iterator iterator = selectQueries.iterator(); iterator.hasNext();) {
            SelectQuery selectQuery = (SelectQuery) iterator.next();
            if (selectQuery.getMethodName().startsWith("ejbSelect")) {
                InterfaceMethodSignature signature = new InterfaceMethodSignature(selectQuery.getMethodName(), selectQuery.getParameterTypes(), true);

                Method method = signature.getMethod(beanClass);
                if (method == null) {
                    throw new IllegalArgumentException("Could not find select for signature: " + signature);
                }
                Method selectMethod = null;
                try {
                    selectMethod = beanClass.getMethod(selectQuery.getMethodName(), selectQuery.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    MethodSignature methodSignature = new MethodSignature(selectQuery.getMethodName(), selectQuery.getParameterTypes());
                    throw new IllegalArgumentException("Missing select method for query " + methodSignature);
                }

                instanceMap.put(new MethodSignature(selectMethod), new SelectMethod(selectQuery));
            }
        }

        return instanceMap;
    }

    public InstancePool getInstancePool() {
        return instancePool;
    }

    public boolean isReentrant() {
        return reentrant;
    }

    public EjbCmpEngine getEjbCmpEngine() {
        return ejbCmpEngine;
    }


}
