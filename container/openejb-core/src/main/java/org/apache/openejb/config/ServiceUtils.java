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
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.config.sys.ServicesJar;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.util.LogCategory;
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
import java.util.ArrayList;

public class ServiceUtils {
    public static final String ANY = ServiceUtils.class.getName() + "@ANY";
    public static final String NONE = ServiceUtils.class.getName() + "@NONE";

    /**
     * Default service provider package.  This value is choosen as follows:
     * </p>
     * 1. System property "openejb.provider.default" </br>
     * 2. If in a full server containing a "conf" directory "org.apache.openejb" </br>
     * 3. Embedded mode "org.apache.openejb.embedded" </br>
     */
    public static final String defaultProviderURL;
    static {
        String defaultValue = "org.apache.openejb";
        try {
            SystemInstance system = SystemInstance.get();
            FileUtils base = system.getBase();
            File confDir = base.getDirectory("conf");
            if (!confDir.exists()) {
                defaultValue = "org.apache.openejb.embedded";
            }
        } catch (Exception ignored) {
        }
        defaultProviderURL = System.getProperty("openejb.provider.default", defaultValue);
    }


    private static Map<String, List<ServiceProvider>> loadedServiceJars = new HashMap<String, List<ServiceProvider>>();
    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

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

            List<ServiceProvider> services = getServiceProviders(info.getPackageName());

            for (ServiceProvider service : services) {
                if (service.getId().equals(id)) {
                    return true;
                }
            }
        } catch (OpenEJBException ignored) {
            // someone else will load the file and get the exception
        }
        return false;
    }

    public static ServiceProvider getServiceProvider(String id) throws OpenEJBException {
        ProviderInfo info = getProviderInfo(id);

        List<ServiceProvider> services = getServiceProviders(info.getPackageName());

        for (ServiceProvider service : services) {
            if (service.getId().equals(info.getServiceName())) {
                return service;
            }
        }
        throw new NoSuchProviderException(messages.format("conf.4901", info.getServiceName(), info.getPackageName()));
    }

    public static String getServiceProviderId(String type) throws OpenEJBException {
        return getServiceProviderId(type, null);
    }

    public static String getServiceProviderId(String type, Properties required) throws OpenEJBException {
        ServiceProvider provider = getServiceProviderByType(type, required);

        return provider != null? provider.getId(): null;
    }


    public static List<ServiceProvider> getServiceProvidersByServiceType(String type) throws OpenEJBException {
        ArrayList<ServiceProvider> providers = new ArrayList<ServiceProvider>();
        if (type == null) return providers;

        List<ServiceProvider> services = getServiceProviders(defaultProviderURL);

        for (ServiceProvider service : services) {
            if (service.getService().equals(type)) {
                providers.add(service);
            }
        }

        return providers;
    }


    public static ServiceProvider getServiceProviderByType(String type) throws OpenEJBException {
        return getServiceProviderByType(type, (Properties) null);
    }

    public static ServiceProvider getServiceProviderByType(String type, Properties required) throws OpenEJBException {
        if (type == null) return null;
        if (required == null) required = new Properties();

        List<ServiceProvider> services = getServiceProviders(defaultProviderURL);

        for (ServiceProvider service : services) {
            if (service.getTypes().contains(type) && implies(required, service.getProperties())) {
                return service;
            }
        }

        return null;
    }

    public static boolean implies(Properties a, Properties b){
        for (Map.Entry<Object, Object> entry : a.entrySet()) {
            Object value = b.get(entry.getKey());

            Object expected = entry.getValue();

            if (expected.equals(NONE)){
                if (value != null) return false;
            } else if (expected.equals(ANY)){
                if (value == null) return false;
            } else if (!expected.equals(value)) return false;
        }
        return true;
    }


    public static ServiceProvider getServiceProviderByType(String providerType, String serviceType) throws OpenEJBException {
        if (serviceType == null) return null;

        List<ServiceProvider> services = getServiceProvidersByServiceType(providerType);

        for (ServiceProvider service : services) {
            if (service.getTypes().contains(serviceType)) {
                return service;
            }
        }

        return null;
    }

    public static List<ServiceProvider> getServiceProviders() throws OpenEJBException {
        return getServiceProviders(defaultProviderURL);
    }

    public static List<ServiceProvider> getServiceProviders(String packageName) throws OpenEJBException {
        List<ServiceProvider> services = loadedServiceJars.get(packageName);
        if (services == null) {
            ServicesJar servicesJar = JaxbOpenejb.readServicesJar(packageName);

            // index services by provider id
            List<ServiceProvider> serviceProviders = servicesJar.getServiceProvider();
            services = new ArrayList<ServiceProvider>(serviceProviders);

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
