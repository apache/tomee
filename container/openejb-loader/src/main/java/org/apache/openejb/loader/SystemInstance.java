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

import java.util.Properties;
import java.util.HashMap;
import java.lang.annotation.Annotation;

/**
 * This class aims to be the one and only static in the entire system
 * A static, singleton, instance of this class can be created with the {@link #init(Properties)} method
 *
 * It is assumed that only one singleton per classloader is possible in any given VM
 * Thus loading this instance in a classloader will mean there can only be one OpenEJB
 * instance for that classloader and all children classloaders.
 *
 * @version $Revision$ $Date$
 * 
 * @org.apache.xbean.XBean element="system"
 */
public class SystemInstance {

    private final long startTime = System.currentTimeMillis();

    /**
     * Properties that have to be away from System (i.e. {@link System#setProperty(String, String)} must not be called)
     */
    private final Properties internalProperties = new Properties();

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

    // FIXME: Why is Exception thrown at all? It's almost impossible that it'll happen.
    private SystemInstance(Properties properties) throws Exception {
        this.components = new HashMap<Class, Object>();

        this.internalProperties.putAll(System.getProperties());
        this.internalProperties.putAll(properties);

        this.home = new FileUtils("openejb.home", "user.dir", this.internalProperties);
        this.base = new FileUtils("openejb.base", "openejb.home", this.internalProperties);
        this.classPath = ClassPathFactory.createClassPath(this.internalProperties.getProperty("openejb.loader", "context"));
        this.classLoader = classPath.getClassLoader();

        this.internalProperties.setProperty("openejb.home", home.getDirectory().getCanonicalPath());
        this.internalProperties.setProperty("openejb.base", base.getDirectory().getCanonicalPath());
        System.setProperty("derby.system.home", base.getDirectory().getCanonicalPath());
        // set the magic system property that causes derby to use explicity
        // file sync instead of relying on vm support for file open rws
        System.setProperty("derby.storage.fileSyncTransactionLog", "true");
    }

    public long getStartTime() {
        return startTime;
    }

    public Properties getProperties() {
        return internalProperties;
    }

    public String getProperty(String key) {
        return internalProperties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return internalProperties.getProperty(key, defaultValue);
    }

    public Object setProperty(String key, String value) {
        return setProperty(key, value, false);
    }

    /**
     * @param key property name
     * @param value property value
     * @param isExternalProperty should the property be set to System by {@link System#setProperty(String, String)}
     * @return property value
     */
    public Object setProperty(String key, String value, boolean isExternalProperty) {
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
    public <T> T getComponent(Class<T> type) {
        return (T)components.get(type);
    }

    /**
     * @param type the class type of the component required
     */
    public <T> T setComponent(Class<T> type, T value) {
//    public Object setComponent(Class type, Object value) {
        return (T)components.put(type, value);
    }

    private static SystemInstance system;

    static {
        try {
            system = new SystemInstance(System.getProperties());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default instance of SystemInstance", e);
        }
    }

    private static boolean initialized;

    public static void init(Properties properties) throws Exception {
        if (initialized) return;
        system = new SystemInstance(properties);
        initialized = true;
    }

    public static SystemInstance get() {
        return system;
    }

    /**
     * @param propName property name
     * 
     * @return true when property is set; false otherwise
     */
    public boolean hasProperty(String propName) {
        return this.internalProperties.get(propName) != null;
    }
}
