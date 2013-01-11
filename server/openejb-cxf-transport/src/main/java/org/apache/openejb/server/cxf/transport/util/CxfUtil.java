/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.transport.util;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.CXFBusImpl;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.AbstractEndpointFactory;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.assembler.classic.util.ServiceInfos;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.PropertiesHelper;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

public final class CxfUtil {
    public static final String ENDPOINT_PROPERTIES = "properties";
    public static final String FEATURES = "features";
    public static final String IN_INTERCEPTORS = "in-interceptors";
    public static final String IN_FAULT_INTERCEPTORS = "in-fault-interceptors";
    public static final String OUT_INTERCEPTORS = "out-interceptors";
    public static final String OUT_FAULT_INTERCEPTORS = "out-fault-interceptors";
    public static final String DATABINDING = "databinding";
    public static final String ADDRESS = "address";
    public static final String DEBUG = "debug";
    public static final String BUS_PREFIX = "org.apache.openejb.cxf.bus.";
    public static final String BUS_CONFIGURED_FLAG = "openejb.cxf.bus.configured";

    private CxfUtil() {
        // no-op
    }

    /*
     * Ensure the bus created is unqiue and non-shared.
     * The very first bus created is set as a default bus which then can
     * be (re)used in other places.
     */
    public static Bus getBus() {
        getDefaultBus();
        return new ExtensionManagerBus();
    }

    /*
     * Ensure the Spring bus is initialized with the CXF module classloader
     * instead of the application classloader.
     */
    public static Bus getDefaultBus() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.class.getClassLoader());
        try {
            return BusFactory.getDefaultBus();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public static void configureEndpoint(final AbstractEndpointFactory svrFactory, final ServiceConfiguration configuration, final String prefix) {
        final Properties beanConfig = configuration.getProperties();
        if (beanConfig == null || beanConfig.isEmpty()) {
            return;
        }

        final Collection<ServiceInfo> availableServices = configuration.getAvailableServices();

        // endpoint properties
        final Properties properties = ServiceInfos.serviceProperties(availableServices, beanConfig.getProperty(prefix + ENDPOINT_PROPERTIES));
        if (properties != null) {
            svrFactory.setProperties(PropertiesHelper.map(properties));
        }

        final String debugKey = prefix + DEBUG;
        if ("true".equalsIgnoreCase(beanConfig.getProperty(debugKey, SystemInstance.get().getOptions().get(debugKey, "false")))) {
            svrFactory.getProperties(true).put("faultStackTraceEnabled", "true");
        }

        // endpoint features
        final String featuresIds = beanConfig.getProperty(prefix + FEATURES);
        if (featuresIds != null) {
            final List<?> features = createFeatures(availableServices, featuresIds);
            svrFactory.setFeatures((List<AbstractFeature>) features);
        }

        configureInterceptors(svrFactory, prefix, availableServices, beanConfig);

        // databinding
        final String databinding = beanConfig.getProperty(prefix + DATABINDING);
        if (databinding != null && !databinding.trim().isEmpty()) {
            final Object instance = ServiceInfos.resolve(availableServices, databinding);
            if (!DataBinding.class.isInstance(instance)) {
                throw new OpenEJBRuntimeException(instance + " is not a " + DataBinding.class.getName()
                        + ", please check configuration of service [id=" + databinding + "]");
            }
            svrFactory.setDataBinding((DataBinding) instance);
        }

        // address: easier than using openejb-jar.xml
        final String changedAddress = beanConfig.getProperty(prefix + ADDRESS);
        if (changedAddress != null && !changedAddress.trim().isEmpty()) {
            svrFactory.setAddress(changedAddress);
        }
    }

    private static void configureInterceptors(final AbstractBasicInterceptorProvider abip, final String prefix, final Collection<ServiceInfo> availableServices, final Properties beanConfig) {
        // interceptors
        final String inInterceptorsIds = beanConfig.getProperty(prefix + IN_INTERCEPTORS);
        if (inInterceptorsIds != null && !inInterceptorsIds.trim().isEmpty()) {
            abip.getInInterceptors().addAll(createInterceptors(availableServices, inInterceptorsIds));
        }

        final String inFaultInterceptorsIds = beanConfig.getProperty(prefix + IN_FAULT_INTERCEPTORS);
        if (inFaultInterceptorsIds != null && !inFaultInterceptorsIds.trim().isEmpty()) {
            abip.getInFaultInterceptors().addAll(createInterceptors(availableServices, inFaultInterceptorsIds));
        }

        final String outInterceptorsIds = beanConfig.getProperty(prefix + OUT_INTERCEPTORS);
        if (outInterceptorsIds != null && !outInterceptorsIds.trim().isEmpty()) {
            abip.getOutInterceptors().addAll(createInterceptors(availableServices, outInterceptorsIds));
        }

        final String outFaultInterceptorsIds = beanConfig.getProperty(prefix + OUT_FAULT_INTERCEPTORS);
        if (outFaultInterceptorsIds != null && !outFaultInterceptorsIds.trim().isEmpty()) {
            abip.getOutFaultInterceptors().addAll(createInterceptors(availableServices, outFaultInterceptorsIds));
        }
    }

    private static List<AbstractFeature> createFeatures(final Collection<ServiceInfo> availableServices, final String featuresIds) {
        final List<?> features = ServiceInfos.resolve(availableServices, featuresIds.split(","));
        for (Object instance : features) {
            if (!AbstractFeature.class.isInstance(instance)) {
                throw new OpenEJBRuntimeException("feature should inherit from " + AbstractFeature.class.getName());
            }
        }
        return (List<AbstractFeature>) features;
    }

    private static List<Interceptor<? extends Message>> createInterceptors(final Collection<ServiceInfo> availableServices, final String ids) {
        final List<?> instances = ServiceInfos.resolve(availableServices, ids.split(","));
        for (Object instance : instances) {
            if (!Interceptor.class.isInstance(instance)) {
                throw new OpenEJBRuntimeException("interceptors should implement " + Interceptor.class.getName());
            }
        }
        return (List<Interceptor<? extends Message>>) instances;
    }

    public static void configureBus() {
        if (SystemInstance.get().getProperties().containsKey(BUS_CONFIGURED_FLAG)) { // jaxws and jaxrs for instance
            return;
        }

        final Bus bus = getDefaultBus();
        if (bus instanceof CXFBusImpl) {
            final ServiceConfiguration configuration = new ServiceConfiguration(SystemInstance.get().getProperties(),
                    SystemInstance.get().getComponent(OpenEjbConfiguration.class).facilities.services);

            final CXFBusImpl busImpl = (CXFBusImpl) bus;
            final Collection<ServiceInfo> serviceInfos = configuration.getAvailableServices();
            final Properties properties = configuration.getProperties();
            if (properties == null || properties.isEmpty()) {
                return;
            }

            final String featuresIds = properties.getProperty(BUS_PREFIX + FEATURES);
            if (featuresIds != null) {
                final List<AbstractFeature> features = createFeatures(serviceInfos, featuresIds);
                if (features != null) {
                    features.addAll(busImpl.getFeatures());
                    busImpl.setFeatures(features);
                }
            }

            final Properties busProperties = ServiceInfos.serviceProperties(serviceInfos, properties.getProperty(BUS_PREFIX + ENDPOINT_PROPERTIES));
            if (busProperties != null) {
                busImpl.getProperties().putAll(PropertiesHelper.map(busProperties));
            }

            configureInterceptors(busImpl, BUS_PREFIX, serviceInfos, configuration.getProperties());

            SystemInstance.get().getProperties().setProperty(BUS_CONFIGURED_FLAG, "true");
        }
    }
}
