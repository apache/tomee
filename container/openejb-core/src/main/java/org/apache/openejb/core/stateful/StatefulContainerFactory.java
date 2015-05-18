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

package org.apache.openejb.core.stateful;

import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.SuperProperties;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class StatefulContainerFactory {
    private Object id;
    private SecurityService securityService;
    private Cache<Object, Instance> cache;
    private final Properties properties = new SuperProperties().caseInsensitive(false);
    private Duration accessTimeout = new Duration(0, TimeUnit.MILLISECONDS);

    public Object getId() {
        return id;
    }

    public void setId(final Object id) {
        this.id = id;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(final SecurityService securityService) {
        this.securityService = securityService;
    }

    public Duration getAccessTimeout() {
        return accessTimeout;
    }

    public void setAccessTimeout(final Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public Cache<Object, Instance> getCache() {
        return cache;
    }

    public void setCache(final Cache<Object, Instance> cache) {
        this.cache = cache;
    }

    public void setCache(final String s) {
        properties.put("Cache", s);
    }

    public void setPassivator(final String s) {
        properties.put("Passivator", s);
    }

    public void setTimeOut(final String s) {
        properties.put("TimeOut", s);
    }

    public void setCapacity(final String s) {
        properties.put("Capacity", s);
    }

    public void setBulkPassivate(final String s) {
        properties.put("BulkPassivate", s);
    }

    public void setFrequency(final String s) {
        properties.put("Frequency", s);
    }

    public void setPreventExtendedEntityManagerSerialization(final boolean preventExtendedEntityManagerSerialization) {
        properties.put("PreventExtendedEntityManagerSerialization", Boolean.toString(preventExtendedEntityManagerSerialization));
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties.putAll(properties);
    }

    public StatefulContainer create() throws Exception {
        // if a live cache was not assigned, build one
        if (cache == null) {
            buildCache();
        }
        cache.init();
        return new StatefulContainer(
            id, securityService,
            cache, accessTimeout,
            "true".equalsIgnoreCase(properties.getProperty("PreventExtendedEntityManagerSerialization", "false").trim()),
            createLockFactory());
    }

    private LockFactory createLockFactory() {
        final Object lockFactory = properties.remove("LockFactory");
        if (lockFactory != null) {
            try {
                return LockFactory.class.cast(StatefulContainerFactory.class.getClassLoader().loadClass(lockFactory.toString().trim()).newInstance());
            } catch (final Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        return new DefaultLockFactory();
    }

    private void buildCache() throws Exception {
        if (properties == null) {
            throw new IllegalArgumentException("No cache defined for StatefulContainer " + id);
        }

        // get the cache property
        Object cache = getProperty("Cache");
        if (cache == null) {
            throw new IllegalArgumentException("No cache defined for StatefulContainer " + id);
        }

        // if property contains a live cache instance, just use it
        if (cache instanceof Cache) {
            this.cache = (Cache<Object, Instance>) cache;
            return;
        }

        // build the object recipe
        final ObjectRecipe serviceRecipe = new ObjectRecipe((String) cache);
        serviceRecipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        serviceRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        serviceRecipe.allow(Option.NAMED_PARAMETERS);
        serviceRecipe.setAllProperties(properties);

        // invoke recipe
        /* the cache should be created with container loader to avoid memory leaks
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) getClass().getClassLoader();
        */
        ClassLoader classLoader = StatefulContainerFactory.class.getClassLoader();
        if (!((String) cache).startsWith("org.apache.tomee")) { // user impl?
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        cache = serviceRecipe.create(classLoader);

        // assign value
        this.cache = (Cache<Object, Instance>) cache;
    }

    private Object getProperty(final String name) {
        for (final Entry<Object, Object> entry : properties.entrySet()) {
            final Object key = entry.getKey();
            if (key instanceof String && name.equalsIgnoreCase((String) key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void setLockFactory(final String lockFactory) {
        properties.setProperty("LockFactory", lockFactory);
    }
}
