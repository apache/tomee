/**
 *
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
package org.apache.openejb.spring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.transaction.TransactionManager;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.dynamic.PassthroughFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.spi.SecurityService;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

public class SpringAssembler extends Assembler {
    private final OpenEJB openEJB;
    private final Set<String> importedResourceIds = new TreeSet<String>();

    public SpringAssembler(OpenEJB openEJB) {
        this.openEJB = openEJB;
    }

    protected OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {
        OpenEjbConfiguration configuration = super.getOpenEjbConfiguration();
        ApplicationContext applicationContext = openEJB.getApplicationContext();

        // todo this is cheating
        ConfigurationFactory configurationFactory = (ConfigurationFactory) this.configFactory;

        //
        // Transaction Manager
        //
        TransactionManager transactionManager = openEJB.getTransactionManager();
        if (transactionManager == null) {
            transactionManager = getBeanForType(applicationContext, TransactionManager.class);
        }
        if (transactionManager != null) {
            TransactionServiceInfo info = initPassthrough(new TransactionServiceInfo(), "TransactionManager", transactionManager);
            configuration.facilities.transactionService = info;
        }

        //
        // Security Service
        //
        SecurityService securityService = openEJB.getSecurityService();
        if (securityService == null) {
            securityService = getBeanForType(applicationContext, SecurityService.class);
        }
        if (securityService != null) {
            SecurityServiceInfo info = initPassthrough(new SecurityServiceInfo(), "SecurityService", securityService);
            configuration.facilities.securityService = info;
        }

        //
        // Resources
        //
        for (Resource resource : openEJB.getResources()) {
            ResourceInfo info = configurationFactory.configureService(resource.getResourceDefinition(), ResourceInfo.class);
            importedResourceIds.add(info.id);
            configuration.facilities.resources.add(info);
        }
        if (openEJB.isImportContext() && applicationContext != null) {
            for (String beanName : applicationContext.getBeanDefinitionNames()) {
                if (!importedResourceIds.contains(beanName)) {
                    Class beanType = applicationContext.getType(beanName);
                    Class factoryType = applicationContext.getType("&" + beanName);
                    if (isImportableType(beanType, factoryType)) {
                        SpringReference factory = new SpringReference(applicationContext, beanName, beanType);

                        ResourceInfo info = initPassthrough(beanName, new ResourceInfo(), "Resource", factory);
                        info.types = getTypes(beanType);
                        configuration.facilities.resources.add(info);
                    }
                }
            }
        }

        //
        // Containers
        //
        for (ContainerProvider containerProvider: openEJB.getContainers()) {
            ContainerInfo info = configurationFactory.createContainerInfo(containerProvider.getContainerDefinition());
            configuration.containerSystem.containers.add(info);
        }

        return configuration;
    }

    private <T> T getBeanForType(ApplicationContext applicationContext, Class<T> type) throws OpenEJBException {
        String[] names = applicationContext.getBeanNamesForType(type);
        if (names.length == 0) {
            return null;
        }
        if (names.length > 1) {
            throw new OpenEJBException("Multiple " + type.getSimpleName() + " beans in application context: " + Arrays.toString(names));
        }

        String name = names[0];
        importedResourceIds.add(name);
        return (T) applicationContext.getBean(name);
    }

    protected boolean isImportableType(Class type, Class factoryType) {
        return !type.isAnnotationPresent(Exported.class) &&
                !BeanPostProcessor.class.isAssignableFrom(type) &&
                !BeanFactoryPostProcessor.class.isAssignableFrom(type) &&
                (factoryType == null || !factoryType.isAnnotationPresent(Exported.class));
    }

    private <T extends ServiceInfo> T initPassthrough(T info, String serviceType, Object instance) {
        return initPassthrough("Spring Supplied " + serviceType, info, serviceType, instance);
    }

    private <T extends ServiceInfo> T initPassthrough(String id, T info, String serviceType, Object instance) {
        info.id = id;
        info.service = serviceType;
        info.types = getTypes(instance);
        PassthroughFactory.add(info, instance);
        return info;
    }

    private List<String> getTypes(Object instance) {
        LinkedHashSet<String> types = new LinkedHashSet<String>();
        Class clazz = instance.getClass();
        addTypes(clazz, types);
        return new ArrayList<String>(types);
    }

    private void addTypes(Class clazz, LinkedHashSet<String> types) {
        if (clazz == null || Object.class.equals(clazz) || Serializable.class.equals(clazz)) {
            return;
        }
        if (types.add(clazz.getName())) {
            addTypes(clazz.getSuperclass(), types);
            for (Class intf : clazz.getInterfaces()) {
                addTypes(intf, types);
            }
        }
    }

}
