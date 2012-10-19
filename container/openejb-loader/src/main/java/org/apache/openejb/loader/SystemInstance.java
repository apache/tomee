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
package org.apache.openejb.loader;

import org.apache.openejb.loader.event.ComponentAdded;
import org.apache.openejb.loader.event.ComponentRemoved;
import org.apache.openejb.observer.ObserverManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class aims to be the one and only static in the entire system
 * A static, singleton, instance of this class can be created with the {@link #init(Properties)} method
 * <p/>
 * It is assumed that only one singleton per classloader is possible in any given VM
 * Thus loading this instance in a classloader will mean there can only be one OpenEJB
 * instance for that classloader and all children classloaders.
 *
 * @version $Revision$ $Date$
 * @org.apache.xbean.XBean element="system"
 */
public class SystemInstance {
    private static final String PROFILE_PROP = "openejb.profile";
    private static final String DEFAULT_PROFILE = "development";

    private final long startTime = System.currentTimeMillis();

    /**
     * Properties that have to be away from System (i.e. {@link System#setProperty(String, String)} must not be called)
     */
    private final Properties internalProperties = new Properties();

    private final Options options;

    /**
     * Properties that need to be set to System via {@link System#setProperty(String, String)}
     * FIXME: Some properties are doubled in internal and external prop sets, but it simplifies get's
     */
    private final Properties externalProperties = new Properties();

    private final FileUtils home;
    private final FileUtils base;
    private final ClassLoader classLoader;
    private final HashMap<Class, Object> components;
    private final ClassPath classPath;
    private final ObserverManager observerManager = new ObserverManager();

    // FIXME: Why is Exception thrown at all? It's almost impossible that it'll happen.
    private SystemInstance(final Properties properties) throws Exception {
        this.components = new HashMap<Class, Object>();

        for (Map.Entry<? extends Object, ? extends Object> e : System.getProperties().entrySet()){
            final String key = e.getKey().toString();
            if (key.startsWith("sun.")) continue;
            if (key.startsWith("os.")) continue;
            if (key.startsWith("user.")) continue;
            if (key.startsWith("awt.")) continue;
            if (key.startsWith("java.")) {
                final String pkg = key.substring("java.".length());
                if (pkg.startsWith("vm.")) continue;
                if (pkg.startsWith("runtime.")) continue;
                if (pkg.startsWith("awt.")) continue;
                if (pkg.startsWith("specification.")) continue;
                if (pkg.startsWith("class.")) continue;
                if (pkg.startsWith("library.")) continue;
                if (pkg.startsWith("ext.")) continue;
                if (pkg.startsWith("vendor.")) continue;
                if (pkg.startsWith("endorsed.")) continue;
            }
            this.internalProperties.put(e.getKey(), e.getValue());
        }

        this.internalProperties.putAll(properties);

        this.options = new Options(internalProperties, new Options(System.getProperties()));
        this.home = new FileUtils("openejb.home", "user.dir", this.internalProperties);
        this.base = new FileUtils("openejb.base", "openejb.home", this.internalProperties);
        this.classPath = ClassPathFactory.createClassPath(this.internalProperties.getProperty("openejb.loader", "context"));
        this.classLoader = classPath.getClassLoader();

        this.internalProperties.setProperty("openejb.home", home.getDirectory().getCanonicalPath());
        this.internalProperties.setProperty("openejb.base", base.getDirectory().getCanonicalPath());
        System.setProperty("derby.system.home", base.getDirectory().getCanonicalPath());


    }

    public void fireEvent(Object event) {
        observerManager.fireEvent(event);
    }

    public boolean addObserver(Object observer) {
        return observerManager.addObserver(observer);
    }

    public boolean removeObserver(Object observer) {
        return observerManager.removeObserver(observer);
    }

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
            this.externalProperties.setProperty(key, value);
            System.setProperty(key, value);
        }
        return internalProperties.setProperty(key, value);
    }

    public FileUtils getHome() {
        return home;
    }

    public FileUtils getBase() {
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
     * @param type
     * @return the object associated with the class type or null
     * @throws IllegalStateException of the component isn't found
     */
    public <T> T getComponent(final Class<T> type) {
        return (T) components.get(type);
    }

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
    public <T> T setComponent(final Class<T> type, final T value) {
        final T removed = (T) components.put(type, value);

        if (removed !=null) {
            fireEvent(new ComponentRemoved(type, value));
        }

        if (value != null) {
            fireEvent(new ComponentAdded(type, value));
        }

        return removed;
    }

    private static SystemInstance system;

    static {
        reset();
    }

    private static boolean initialized;

    public static boolean isInitialized() {
        return initialized;
    }

    public static synchronized void reset() {
        try {
            system = new SystemInstance(new Properties()); // don't put system properties here, it is already done
            initialized = false;
        } catch (Exception e) {
            throw new LoaderRuntimeException("Failed to create default instance of SystemInstance", e);
        }
    }

    public static synchronized void init(final Properties properties) throws Exception {
        if (initialized) return;
        system = new SystemInstance(properties);
        readUserSystemProperties();
        readSystemProperties();
        readSystemProperties(get().currentProfile());
        initialized = true;
        get().setProperty("openejb.profile.custom", Boolean.toString(!get().isDefaultProfile()));
    }

    private static void readUserSystemProperties() {
        final File file = new File(System.getProperty("user.home"), ".openejb/system.properties");
        addSystemProperties(file);
    }

    public File getConf(final String subPath) {
        File conf = null;
        try {
            conf = system.getBase().getDirectory("conf");
        } catch (IOException e) {
            // no-op
        }

        if (conf == null || !conf.exists()) {
            try {
                conf = system.getBase().getDirectory("etc");
            } catch (IOException e) {
                // no-op
            }
        }

        if (conf == null || !conf.exists()) {
            return new File(system.getBase().getDirectory(), "conf");
        }
        if (subPath == null) {
            return conf;
        }
        return new File(conf, subPath);
    }

    private static void readSystemProperties(final String prefix) {
        final String completePrefix;
        if (prefix != null && !prefix.isEmpty()) {
            completePrefix =  prefix + ".";
        } else {
            completePrefix = "";
        }

        // Read in and apply the conf/system.properties
        final File conf = system.getConf(completePrefix + "system.properties");
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
        } catch (IOException e) {
            return;
        }

        System.getProperties().putAll(systemProperties);
        system.getProperties().putAll(systemProperties);
    }

    public static SystemInstance get() {
        return system;
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
}
