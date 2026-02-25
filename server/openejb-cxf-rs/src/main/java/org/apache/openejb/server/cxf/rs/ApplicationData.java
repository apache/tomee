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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.openejb.server.rest.InternalApplication;

import jakarta.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationData {

    private final String path;
    private final Application application;
    private final Class<?> applicationClass;
    final List<Resource> resources = new ArrayList<>();
    final List<Provider> providers = new ArrayList<>();
    final List<Invalid> invalids = new ArrayList<>();

    public ApplicationData(final String path, final Application application) {
        this.path = path;
        this.application = application;

        if (application instanceof InternalApplication internalApplication && internalApplication.getOriginal() != null) {
            this.applicationClass = internalApplication.getOriginal().getClass();
        } else {
            this.applicationClass = application.getClass();
        }
    }

    public String getPath() {
        return path;
    }

    public Application getApplication() {
        return application;
    }

    public Class<?> getApplicationClass() {
        return applicationClass;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public List<Class<?>> getResourceClasses() {
        return resources.stream()
                .map(Resource::getClazz)
                .collect(Collectors.toList());
    }

    public List<Provider> getProviders() {
        return providers;
    }

    public List<Invalid> getInvalids() {
        return invalids;
    }

    public void addProvider(final boolean discovered, final Class<?> clazz, final Object singleton) {
        providers.add(new Provider(discovered, clazz, singleton));
    }

    public void addResource(final boolean discovered, final Class<?> clazz, final Object singleton){
        resources.add(new Resource(discovered, clazz, singleton));
    }

    public void addInvalid(final Class<?> clazz, final String reason) {
        invalids.add(new Invalid(clazz, reason));
    }
    
    @Override
    public String toString() {
        return "Application{" +
                "path='" + path + '\'' +
                ", class=" + applicationClass.getName() +
                ", resources=" + resources.size() +
                ", providers=" + providers.size() +
                ", invalids=" + invalids.size() +
                '}';
    }

    public static class Resource {
        private final Class<?> clazz;
        private final boolean discovered;
        private final Object singleton;

        public Resource(final boolean discovered, final Class<?> clazz, final Object singleton) {
            this.discovered = discovered;
            this.clazz = clazz;
            this.singleton = singleton;
        }

        public boolean isDiscovered() {
            return discovered;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public Object getSingleton() {
            return singleton;
        }

        @Override
        public String toString() {
            return "Resource{" +
                    "clazz=" + clazz.getName() +
                    ", discovered=" + discovered +
                    ", singleton=" + (singleton != null) +
                    '}';
        }
    }

    public static class Provider {
        private final Class<?> clazz;
        private final boolean discovered;
        private final Object singleton;

        public Provider(final boolean discovered, final Class<?> clazz, final Object singleton) {
            this.discovered = discovered;
            this.clazz = clazz;
            this.singleton = singleton;
        }

        @Override
        public String toString() {
            return "Provider{" +
                    "clazz=" + clazz.getName() +
                    ", discovered=" + discovered +
                    ", singleton=" + (singleton != null) +
                    '}';
        }
    }

    public static class Invalid {
        private final Class<?> clazz;
        private final String reason;

        public Invalid(final Class<?> clazz, final String reason) {
            this.clazz = clazz;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return "Invalid{" +
                    "clazz=" + clazz.getName() +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }


}
