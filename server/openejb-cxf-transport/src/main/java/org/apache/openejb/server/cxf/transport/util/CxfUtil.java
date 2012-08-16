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
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.AbstractEndpointFactory;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.ServiceInfo;
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

    public static void configureEndpoint(final AbstractEndpointFactory svrFactory, final Collection<ServiceInfo> availableServices, final String prefix, final String beanId) {
        final ServiceInfo beanInfo = ServiceInfos.findByClass(availableServices, beanId);
        if (beanInfo != null) {
            final Properties beanConfig = beanInfo.properties;

            // endpoint properties
            final Properties properties = ServiceInfos.serviceProperties(availableServices, beanConfig.getProperty(prefix + ENDPOINT_PROPERTIES));
            if (properties != null) {
                svrFactory.setProperties(PropertiesHelper.map(properties));
            }
            if (SystemInstance.get().getOptions().get(prefix + DEBUG, false)) {
                svrFactory.getProperties(true).put("faultStackTraceEnabled", "true");
            }

            // endpoint features
            final String featuresIds = beanConfig.getProperty(prefix + FEATURES);
            if (featuresIds != null) {
                final List<?> features = ServiceInfos.resolve(availableServices, featuresIds.split(","));
                for (Object instance : features) {
                    if (!AbstractFeature.class.isInstance(instance)) {
                        throw new OpenEJBRuntimeException("feature should inherit from " + AbstractFeature.class.getName());
                    }
                }
                svrFactory.setFeatures((List<AbstractFeature>) features);
            }

            // interceptors
            final String inInterceptorsIds = beanConfig.getProperty(prefix + IN_INTERCEPTORS);
            if (inInterceptorsIds != null && !inInterceptorsIds.trim().isEmpty()) {
                svrFactory.setInInterceptors(createInterceptors(availableServices, inInterceptorsIds));
            }

            final String inFaultInterceptorsIds = beanConfig.getProperty(prefix + IN_FAULT_INTERCEPTORS);
            if (inFaultInterceptorsIds != null && !inFaultInterceptorsIds.trim().isEmpty()) {
                svrFactory.setInFaultInterceptors(createInterceptors(availableServices, inFaultInterceptorsIds));
            }

            final String outInterceptorsIds = beanConfig.getProperty(prefix + OUT_INTERCEPTORS);
            if (outInterceptorsIds != null && !outInterceptorsIds.trim().isEmpty()) {
                svrFactory.setOutInterceptors(createInterceptors(availableServices, outInterceptorsIds));
            }

            final String outFaultInterceptorsIds = beanConfig.getProperty(prefix + OUT_FAULT_INTERCEPTORS);
            if (outFaultInterceptorsIds != null && !outFaultInterceptorsIds.trim().isEmpty()) {
                svrFactory.setOutFaultInterceptors(createInterceptors(availableServices, outFaultInterceptorsIds));
            }

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
}
