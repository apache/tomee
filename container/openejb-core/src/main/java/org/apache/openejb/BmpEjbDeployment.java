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
import org.apache.openejb.entity.bmp.BmpCreateMethod;
import org.apache.openejb.entity.bmp.BmpFinderMethod;
import org.apache.openejb.entity.bmp.BmpInstanceContextFactory;
import org.apache.openejb.entity.bmp.BmpRemoveMethod;
import org.apache.openejb.proxy.ProxyInfo;
import org.apache.openejb.util.SoftLimitedInstancePool;
import org.apache.openejb.util.Index;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.security.auth.Subject;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;


/**
 * @version $Revision$ $Date$
 */
public class BmpEjbDeployment extends AbstractRpcDeployment implements EntityEjbDeployment {
    private final InstancePool instancePool;
    private final boolean reentrant;
    private final Index dispatchIndex;

    public BmpEjbDeployment(String containerId,
                            String ejbName,

                            String homeInterfaceName,
                            String remoteInterfaceName,
                            String localHomeInterfaceName,
                            String localInterfaceName,
                            String primaryKeyClassName,
                            String beanClassName,
                            ClassLoader classLoader,

                            BmpEjbContainer ejbContainer,

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
                reentrant);
    }

    public BmpEjbDeployment(String containerId,
                            String ejbName,

                            Class homeInterface,
                            Class remoteInterface,
                            Class localHomeInterface,
                            Class localInterface,
                            Class primaryKeyClass,
                            Class beanClass,
                            ClassLoader classLoader,

                            BmpEjbContainer ejbContainer,

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

                            boolean reentrant) throws Exception {

        super(containerId,
                ejbName,
                new ProxyInfo(EJBComponentType.BMP_ENTITY,
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

        dispatchIndex = buildDispatchMethodMap();

        InstanceContextFactory contextFactory = new BmpInstanceContextFactory(this, ejbContainer, proxyFactory);

        InstanceFactory instanceFactory = new EntityInstanceFactory(contextFactory);

        // todo the pools should be created by an InstancePoolFactory in the interceptor stack
        instancePool = new SoftLimitedInstancePool(instanceFactory, 1);

        // todo we should have an instance cache for bmp beans
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

        MethodSignature removeSignature = new MethodSignature("ejbRemove");
        BmpRemoveMethod removeVop = new BmpRemoveMethod(beanClass, removeSignature);
        for (Iterator iterator = dispatchIndex.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) entry.getKey();
            String methodName = methodSignature.getMethodName();
            if (methodSignature.isHomeMethod()) {
                if (methodName.startsWith("create")) {
                    String baseName = methodName.substring(6);
                    MethodSignature createSignature = new MethodSignature("ejbCreate" + baseName, methodSignature.getParameterTypes());
                    MethodSignature postCreateSignature = new MethodSignature("ejbPostCreate" + baseName, methodSignature.getParameterTypes());
                    entry.setValue(new BmpCreateMethod(beanClass, createSignature, postCreateSignature));
                } else if (methodName.startsWith("remove")) {
                    entry.setValue(removeVop);
                } else if (methodName.startsWith("find")) {
                    MethodSignature findSignature = new MethodSignature("ejb" + MethodHelper.capitalize(methodName), methodSignature.getParameterTypes());
                    entry.setValue(new BmpFinderMethod(beanClass, findSignature));
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

    public InstancePool getInstancePool() {
        return instancePool;
    }

    public boolean isReentrant() {
        return reentrant;
    }


}
