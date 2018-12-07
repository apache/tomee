/*
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
import org.apache.openejb.config.provider.ID;
import org.apache.openejb.config.provider.ProviderManager;
import org.apache.openejb.config.provider.ServiceJarXmlLoader;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ServiceUtils {
    public static final String ANY = ServiceUtils.class.getName() + "@ANY";
    public static final String NONE = ServiceUtils.class.getName() + "@NONE";

    /**
     * Default service provider package.  This value is choosen as follows:
     * </p>
     * 1. System property "openejb.provider.default" </br>
     * 2. If in a full server containing a "conf" directory "org.apache.tomee" </br>
     * 3. Embedded mode "org.apache.openejb.embedded" </br>
     */
    public static final String DEFAULT_PROVIDER_URL;

    static {
        String defaultValue = "org.apache.openejb";
        try {
            final SystemInstance system = SystemInstance.get();
            if (system.getProperty("openejb.embedded") != null) {
                defaultValue = "org.apache.openejb.embedded";
            }
        } catch (final Exception ignored) {
            // no-op
        }
        DEFAULT_PROVIDER_URL = defaultValue;
    }

    private static String currentDefaultProviderUrl(final String defaultValue) {
        return SystemInstance.get().getProperty("openejb.provider.default", defaultValue);
    }

    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    public static ProviderManager getManager() {
        final ProviderManager manager = SystemInstance.get().getComponent(ProviderManager.class);
        if (manager != null) {
            return manager;
        }

        SystemInstance.get().setComponent(ProviderManager.class, new ProviderManager(new ServiceJarXmlLoader()));
        return getManager();
    }

    public static class ProviderInfo {
        private final String packageName;
        private final String serviceName;

        public ProviderInfo(final String providerName, final String serviceName) {
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

    public static boolean hasServiceProvider(final String id) {
        try {
            final ProviderInfo info = getProviderInfo(id);

            final List<ServiceProvider> services = getServiceProviders(info.getPackageName());

            for (final ServiceProvider service : services) {
                if (service.getId().equals(id)) {
                    return true;
                }
            }
        } catch (final OpenEJBException | IllegalStateException ignored) {
            // someone else will load the file and get the exception
        }
        return false;
    }

    public static ServiceProvider getServiceProvider(final String idString) throws OpenEJBException {
        final ID id = ID.parse(idString, currentDefaultProviderUrl(DEFAULT_PROVIDER_URL));

        {
            final ServiceProvider provider = getManager().get(id.getNamespace(), id.getName());
            if (provider != null) {
                return provider;
            }
        }

        throw new NoSuchProviderException(new Messages("org.apache.openejb.util.resources").format("conf.4901", id.getName(), id.getNamespace()));
    }

    public static String getServiceProviderId(final String type) throws OpenEJBException {
        return getServiceProviderId(type, null);
    }

    public static String getServiceProviderId(final String type, final Properties required) throws OpenEJBException {
        final ServiceProvider provider = getServiceProviderByType(type, required);

        return provider != null ? provider.getId() : null;
    }


    public static List<ServiceProvider> getServiceProvidersByServiceType(final String type) throws OpenEJBException {
        final ArrayList<ServiceProvider> providers = new ArrayList<>();
        if (type == null) {
            return providers;
        }

        final List<ServiceProvider> services = getServiceProviders(currentDefaultProviderUrl(DEFAULT_PROVIDER_URL));

        for (final ServiceProvider service : services) {
            if (service.getService().equals(type)) {
                providers.add(service);
            }
        }

        return providers;
    }

    public static ServiceProvider getServiceProviderByType(final String type, Properties required) throws OpenEJBException {
        if (type == null) {
            return null;
        }
        if (required == null) {
            required = new Properties();
        }

        final List<ServiceProvider> services = getServiceProviders(currentDefaultProviderUrl(DEFAULT_PROVIDER_URL));

        for (final ServiceProvider service : services) {
            if (service.getTypes().contains(type) && implies(required, service.getProperties())) {
                return service;
            }
        }

        return null;
    }

    public static boolean implies(final Properties required, final Properties available) {
        if (available.containsKey("openejb.connector")) { // created from a connector so our JtaManaged etc can't be used
            return true;
        }

        for (final Map.Entry<Object, Object> entry : required.entrySet()) {
            Object value = available.get(entry.getKey());

            Object expected = entry.getValue();

            if (expected.equals(NONE)) {
                if (value != null) {
                    return false;
                }
            } else if (expected.equals(ANY)) {
                if (value == null) {
                    return false;
                }
            } else {
                if (value instanceof String) {
                    value = ((String) value).toLowerCase();
                }

                if (expected instanceof String) {
                    expected = ((String) expected).toLowerCase();
                }

                if (!expected.equals(value)) {
                    return false;
                }
            }
        }
        return true;
    }


    public static ServiceProvider getServiceProviderByType(final String providerType, final String serviceType) throws OpenEJBException {
        if (serviceType == null) {
            return null;
        }

        final List<ServiceProvider> services = getServiceProvidersByServiceType(providerType);

        for (final ServiceProvider service : services) {
            if (service.getTypes().contains(serviceType)) {
                return service;
            }
        }

        return null;
    }

    public static List<ServiceProvider> getServiceProviders() throws OpenEJBException {
        return getServiceProviders(currentDefaultProviderUrl(DEFAULT_PROVIDER_URL));
    }

    public static List<ServiceProvider> getServiceProviders(final String packageName) throws OpenEJBException {
        return getManager().load(packageName);
    }

    public static void registerServiceProvider(final String packageName, final ServiceProvider provider) {
        getManager().register(packageName, provider);
    }

    private static ProviderInfo getProviderInfo(final String id) {
        String providerName = null;
        String serviceName = null;

        if (id.indexOf('#') != -1) {
            providerName = id.substring(0, id.indexOf('#'));
            serviceName = id.substring(id.indexOf('#') + 1);
        } else if (id.indexOf(':') != -1) {
            providerName = id.substring(0, id.indexOf(':'));
            serviceName = id.substring(id.indexOf(':') + 1);
        } else {
            providerName = currentDefaultProviderUrl(DEFAULT_PROVIDER_URL);
            serviceName = id;
        }

        return new ProviderInfo(providerName, serviceName);
    }
}
