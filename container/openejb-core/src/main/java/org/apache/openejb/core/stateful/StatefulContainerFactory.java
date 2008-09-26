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
package org.apache.openejb.core.stateful;

import java.util.Properties;
import java.util.Map.Entry;

import org.apache.openejb.spi.SecurityService;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

public class StatefulContainerFactory {
    private Object id;
    private SecurityService securityService;
    private Cache<Object, Instance> cache;
    private Properties properties = new Properties();

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public Cache<Object, Instance> getCache() {
        return cache;
    }

    public void setCache(Cache<Object, Instance> cache) {
        this.cache = cache;
    }

    public void setCache(String s) {
        properties.put("Cache", s);
    }

    public void setPassivator(String s) {
        properties.put("Passivator", s);
    }

    public void setTimeOut(String s) {
        properties.put("TimeOut", s);
    }

    public void setCapacity(String s) {
        properties.put("Capacity", s);
    }

    public void setBulkPassivate(String s) {
        properties.put("BulkPassivate", s);
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties.putAll(properties);
    }

    public StatefulContainer create() throws Exception {
        // if a live cache was not assigned, build one
        if (cache == null) {
            buildCache();
        }
        return new StatefulContainer(id, securityService, cache);
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
        ObjectRecipe serviceRecipe = new ObjectRecipe((String) cache);
        serviceRecipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        serviceRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        serviceRecipe.allow(Option.NAMED_PARAMETERS);
        serviceRecipe.setAllProperties(properties);

        // invoke recipe
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) getClass().getClassLoader();
        cache = serviceRecipe.create(classLoader);

        // assign value
        this.cache = (Cache<Object, Instance>) cache;
    }

    private Object getProperty(String name) {
        for (Entry<Object, Object> entry : properties.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof String && name.equalsIgnoreCase((String) key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
