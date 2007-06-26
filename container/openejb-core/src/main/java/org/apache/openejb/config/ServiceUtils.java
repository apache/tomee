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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.config.sys.ServicesJar;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.List;

public class ServiceUtils {
    public static final String defaultProviderURL = "org.apache.openejb";
    private static Map<String, Map<String, ServiceProvider>> loadedServiceJars = new HashMap<String, Map<String, ServiceProvider>>();
    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    public static class ProviderInfo {
        private final String packageName;
        private final String serviceName;

        public ProviderInfo(String providerName, String serviceName) {
            this.packageName = providerName;
            this.serviceName = serviceName;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getServiceName() {
            return serviceName;
        }
    }

    public static boolean hasServiceProvider(String id) {
        try {
            ProviderInfo info = getProviderInfo(id);

            Map<String, ServiceProvider> services = getServices(info.getPackageName());

            return  services.containsKey(info.getServiceName());
        } catch (OpenEJBException e) {
            // someone else will load the file and get the exception
            return false;
        }
    }

    public static ServiceProvider getServiceProvider(String id) throws OpenEJBException {
        ProviderInfo info = getProviderInfo(id);

        Map<String, ServiceProvider> services = getServices(info.getPackageName());

        ServiceProvider service = services.get(info.getServiceName());
        if (service == null) {
            throw new NoSuchProviderException(messages.format("conf.4901", info.getServiceName(), info.getPackageName()));
        }

        return service;
    }

    private static Map<String, ServiceProvider> getServices(String packageName) throws OpenEJBException {
        Map<String, ServiceProvider> services = loadedServiceJars.get(packageName);
        if (services == null) {
            ServicesJar servicesJar = JaxbOpenejb.readServicesJar(packageName);

            // index services by provider id
            List<ServiceProvider> serviceProviders = servicesJar.getServiceProvider();
            services = new HashMap<String, ServiceProvider>(serviceProviders.size());
            for (ServiceProvider serviceProvider : serviceProviders) {
                services.put(serviceProvider.getId(), serviceProvider);

            }

            loadedServiceJars.put(packageName, services);
        }
        return services;
    }

    private static ProviderInfo getProviderInfo(String id) {
        String providerName = null;
        String serviceName = null;

        if (id.indexOf("#") != -1) {
            providerName = id.substring(0, id.indexOf("#"));
            serviceName = id.substring(id.indexOf("#") + 1);
        } else if (id.indexOf(":") != -1) {
            providerName = id.substring(0, id.indexOf(":"));
            serviceName = id.substring(id.indexOf(":") + 1);
        } else {
            providerName = defaultProviderURL;
            serviceName = id;
        }

        return new ProviderInfo(providerName, serviceName);
    }

    public static Properties loadProperties(String pFile) throws OpenEJBException {
        return loadProperties(pFile, new Properties());
    }

    public static Properties loadProperties(String propertiesFile, Properties defaults) throws OpenEJBException {
        try {
            File pfile = new File(propertiesFile);
            InputStream in = new FileInputStream(pfile);

            try {
                /*
                This may not work as expected.  The desired effect is that
                the load method will read in the properties and overwrite
                the values of any properties that may have previously been
                defined.
                */
                defaults.load(in);
            } catch (IOException ex) {
                throw new OpenEJBException(messages.format("conf.0012", ex.getLocalizedMessage()), ex);
            }

            return defaults;
        } catch (FileNotFoundException ex) {
            throw new OpenEJBException(messages.format("conf.0006", propertiesFile, ex.getLocalizedMessage()), ex);
        } catch (IOException ex) {
            throw new OpenEJBException(messages.format("conf.0007", propertiesFile, ex.getLocalizedMessage()), ex);
        } catch (SecurityException ex) {
            throw new OpenEJBException(messages.format("conf.0005", propertiesFile, ex.getLocalizedMessage()), ex);
        }
    }

}
