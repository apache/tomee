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
package org.apache.openejb.loader;

import org.apache.openejb.loader.event.ComponentAdded;
import org.apache.openejb.loader.event.ComponentRemoved;
import org.apache.openejb.loader.provisining.ProvisioningResolver;
import org.apache.openejb.observer.ObserverManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class aims to be the one and only static in the entire SYSTEM
 A static, singleton, instance of this class can be created with the {@link #init(Properties)} method
 *
 * It is assumed that only one singleton per classloader is possible in any given VM
 * Thus loading this instance in a classloader will mean there can only be one OpenEJB
 * instance for that classloader and all children classloaders.
 *
 * @version $Revision$ $Date$
 */
public final class SystemInstance {
    private static final String PROFILE_PROP = "openejb.profile";
    private static final String DEFAULT_PROFILE = "development";
    public static final String ACTIVEMQ_CREATE_JMX_CONNECTOR = "org.apache.activemq.broker.jmx.createConnector";

    private final long startTime = System.currentTimeMillis();

    /**
     * Properties that have to be away from System (i.e. {@link System#setProperty(String, String)} must not be called)
     */
    private final Properties internalProperties = new Properties(System.getProperties());

    private final Options options;

    private final FileUtils home;
    private final FileUtils base;
    private final ClassLoader classLoader;
    private final Map<Class, Object> components;
    private final ClassPath classPath;
    private final ObserverManager observerManager = new ObserverManager();

    private SystemInstance(final Properties properties) {
        this.components = new HashMap<>();

        // import JVM SYSTEM property config (if a resource/container/... is set through this way)
        for (final String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith("sun.")) {
                continue;
            }
            if (key.startsWith("os.")) {
                continue;
            }
            if (key.startsWith("user.")) {
                continue;
            }
            if (key.startsWith("awt.")) {
                continue;
            }
            if (key.startsWith("java.")) {
                final String pkg = key.substring("java.".length());
                if (pkg.startsWith("vm.")) {
                    continue;
                }
                if (pkg.startsWith("runtime.")) {
                    continue;
                }
                if (pkg.startsWith("awt.")) {
                    continue;
                }
                if (pkg.startsWith("specification.")) {
                    continue;
                }
                if (pkg.startsWith("class.")) {
                    continue;
                }
                if (pkg.startsWith("library.")) {
                    continue;
                }
                if (pkg.startsWith("ext.")) {
                    continue;
                }
                if (pkg.startsWith("vendor.")) {
                    continue;
                }
                if (pkg.startsWith("endorsed.")) {
                    continue;
                }
            }
            final String value = System.getProperty(key);
            if (value != null) {
                this.internalProperties.put(key, value);
            }
        }
        this.internalProperties.putAll(properties);

        setDefaults();

        this.options = new Options(internalProperties, new Options(System.getProperties()));
        this.home = new FileUtils("openejb.home", "user.dir", this.internalProperties);
        this.base = new FileUtils("openejb.base", "openejb.home", this.internalProperties);
        this.classPath = ClassPathFactory.createClassPath(this.internalProperties.getProperty("openejb.loader", "context"));
        this.classLoader = classPath.getClassLoader();
        final String homeDirCanonicalPath;
        final String baseDirCanonicalPath;
        try {
            homeDirCanonicalPath = home.getDirectory().getCanonicalPath();
            baseDirCanonicalPath = base.getDirectory().getCanonicalPath();
        } catch (final IOException e) {
            throw new LoaderRuntimeException("Failed to create default instance of SystemInstance", e);
        }
        this.internalProperties.setProperty("openejb.home", homeDirCanonicalPath);
        this.internalProperties.setProperty("openejb.base", baseDirCanonicalPath);
        System.setProperty("derby.system.home", System.getProperty("derby.system.home", baseDirCanonicalPath));
    }

    /**
     * This method sets some specific defaults. We shouldn't add to this unless there's a really good reason -
     * e.g. we need to override an upstream component's defaults.
     */
    private void setDefaults() {
        if (! this.internalProperties.containsKey(ACTIVEMQ_CREATE_JMX_CONNECTOR)) {
            this.internalProperties.setProperty(ACTIVEMQ_CREATE_JMX_CONNECTOR, Boolean.FALSE.toString());
        }

        if (getProperty("hsqldb.reconfig_logging") == null) {
            setProperty("hsqldb.reconfig_logging", "false", true);
        }
    }

    public <E> E fireEvent(final E event) {
        return observerManager.fireEvent(event);
    }

    public boolean addObserver(final Object observer) {
        return observerManager.addObserver(observer);
    }

    public boolean removeObserver(final Object observer) {
        return observerManager.removeObserver(observer);
    }

    @SuppressWarnings("unused")
    public long getStartTime() {
        return startTime;
    }

    public Options getOptions() {
        return options;
    }

    public Properties getProperties() {
        return internalProperties;
    }

    public String getProperty(final String key) {
        return internalProperties.getProperty(key);
    }

    public String getProperty(final String key, final String defaultValue) {
        return internalProperties.getProperty(key, defaultValue);
    }

    public Object setProperty(final String key, final String value) {
        return setProperty(key, value, false);
    }

    /**
     * @param key                property name
     * @param value              property value
     * @param isExternalProperty should the property be set to System by {@link System#setProperty(String, String)}
     * @return property value
     */
    public Object setProperty(final String key, final String value, final boolean isExternalProperty) {
        if (isExternalProperty) {
            System.setProperty(key, value);
        }
        return internalProperties.setProperty(key, value);
    }

    public FileUtils getHome() {
        if (!isInitialized()) {
            return new FileUtils("openejb.home", "user.dir", System.getProperties());
        }
        return home;
    }

    public FileUtils getBase() {
        if (!isInitialized()) {
            return new FileUtils("openejb.base", "openejb.home", System.getProperties());
        }
        return base;
    }

    public ClassPath getClassPath() {
        return classPath;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * I'm not sure how this will play out, but I've used class instances instead of strings
     * for lookups as class instances are classloader scoped and there is an implicit "namespace"
     * associated with that.  Theoretically, you can't lookup things that you can't already see
     * in your classloader.
     *
     * @param type Class
     * @return the object associated with the class type or null
     * @throws IllegalStateException of the component isn't found
     */
    @SuppressWarnings("unchecked")
    public <T> T getComponent(final Class<T> type) {
        final T component = (T) components.get(type);
        if (component != null) {
            return component;
        }

        final String classname = getProperty(type.getName());
        if (classname != null) {
            try {
                final T instance = type.cast(Thread.currentThread().getContextClassLoader()
                    .loadClass(classname).newInstance());
                components.put(type, instance);
                return instance;
            } catch (final Throwable e) {
                // no-op
                System.err.println("Failed to load class: " + classname);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T removeComponent(final Class<T> type) {
        final T component = (T) components.remove(type);

        if (component != null) {
            fireEvent(new ComponentRemoved(type, component));
        }

        return component;
    }

    /**
     * @param type the class type of the component required
     */
    @SuppressWarnings("unchecked")
    public <T> T setComponent(final Class<T> type, final T value) {
        final T removed = (T) components.put(type, value);

        if (removed != null) {
            fireEvent(new ComponentRemoved(type, value));
        }

        if (value != null) {
            fireEvent(new ComponentAdded(type, value));
        }

        return removed;
    }

    private static final AtomicReference<SystemInstance> SYSTEM = new AtomicReference<>();

    static {
        reset();
    }

    private static boolean initialized;

    public static boolean isInitialized() {
        return initialized;
    }

    public static synchronized void reset() {
        try {
            System.clearProperty("openejb.loader");
            SYSTEM.set(new SystemInstance(new Properties())); // don't put SYSTEM properties here, it is already done
            initialized = false;
        } catch (final Exception e) {
            throw new LoaderRuntimeException("Failed to create default instance of SystemInstance", e);
        }
    }

    public static synchronized void init(final Properties properties) throws Exception {
        if (initialized) {
            return;
        }
        SYSTEM.set(new SystemInstance(properties));
        // WARNING: reverse order since we don't overwrite existing entries
        readSystemProperties(get().currentProfile());
        readSystemProperties();
        readUserSystemProperties();


        // if the user read System.getProperties() instead of our properties, used in bval-tomee tck for instance
        System.getProperties().putAll(SYSTEM.get().getProperties());

        initialized = true;
        get().setProperty("openejb.profile.custom", Boolean.toString(!get().isDefaultProfile()));

        initDefaultComponents();
    }

    private static void initDefaultComponents() {
        SYSTEM.get().components.put(ProvisioningResolver.class, new ProvisioningResolver());
    }

    private static void readUserSystemProperties() {
        final File file = new File(System.getProperty("user.home"), ".openejb/system.properties");
        addSystemProperties(file);
    }

    public File getConf(final String subPath) {

        File conf = null;
        final FileUtils base = SYSTEM.get().getBase();

        try {
            conf = base.getDirectory("conf");
        } catch (final IOException e) {
            // no-op
        }

        if (conf == null || !conf.exists()) {
            try {
                conf = base.getDirectory("etc");
            } catch (final IOException e) {
                // no-op
            }
        }

        if (conf == null || !conf.exists()) {
            return new File(base.getDirectory(), "conf");
        }
        if (subPath == null) {
            return conf;
        }
        return new File(conf, subPath);
    }

    private static void readSystemProperties(final String prefix) {
        final String completePrefix;
        if (prefix != null && !prefix.isEmpty()) {
            completePrefix = prefix + ".";
        } else {
            completePrefix = "";
        }

        // Read in and apply the conf/system.properties
        final File conf = SYSTEM.get().getConf(completePrefix + "system.properties");
        if (conf != null && conf.exists()) {
            addSystemProperties(conf);
        }
    }

    private static void readSystemProperties() {
        readSystemProperties(null);
    }

    private static void addSystemProperties(final File file) {
        if (!file.exists()) {
            return;
        }

        final Properties systemProperties;
        try {
            systemProperties = IO.readProperties(file);
        } catch (final IOException e) {
            return;
        }

        for (final String key : systemProperties.stringPropertyNames()) {
            final SystemInstance systemInstance = SYSTEM.get();
            if (systemInstance.getProperty(key) == null) {
                systemInstance.setProperty(key, systemProperties.getProperty(key));
            }
        }
        // don't override SYSTEM props
        // SYSTEM.getProperties().putAll(systemProperties);
    }

    public static SystemInstance get() {
        return SYSTEM.get();
    }

    public String currentProfile() {
        return getProperty(PROFILE_PROP, DEFAULT_PROFILE);
    }

    public boolean isDefaultProfile() {
        return DEFAULT_PROFILE.equals(currentProfile());
    }

    /**
     * @param propName property name
     * @return true when property is set; false otherwise
     */
    public boolean hasProperty(final String propName) {
        return this.internalProperties.get(propName) != null;
    }

    public void removeObservers() {
        observerManager.destroy();
    }
}
