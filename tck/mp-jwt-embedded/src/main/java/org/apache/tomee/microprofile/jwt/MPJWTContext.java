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
package org.apache.tomee.microprofile.jwt;

import org.eclipse.microprofile.auth.LoginConfig;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

/**
 * Responsible for holding the runtime model
 */
@ApplicationScoped
public class MPJWTContext {

    private final ConcurrentMap<MPJWTConfigKey, MPJWTConfigValue> configuration = new ConcurrentHashMap<>();

    public MPJWTConfigValue addMapping(final MPJWTConfigKey key, final MPJWTConfigValue value) {
        Objects.requireNonNull(key, "MP JWT Key is required");
        Objects.requireNonNull(value, "MP JWT Value is required");

        final MPJWTConfigValue oldValue = configuration.putIfAbsent(key, value);
        if (oldValue != null) {
            throw new IllegalStateException("A mapping has already been defined for the key " + key);
        }

        return value;
    }

    public Optional<MPJWTConfigValue> get(final MPJWTConfigKey key) {
        Objects.requireNonNull(key, "MP JWT Key is required to retrieve the configuration");
        return Optional.ofNullable(configuration.get(key));
    }

    public Optional<Map.Entry<MPJWTConfigKey, MPJWTConfigValue>> findFirst(final String path) {
        return configuration.entrySet()
                .stream()
                .filter(new Predicate<ConcurrentMap.Entry<MPJWTConfigKey, MPJWTConfigValue>>() {
                    @Override
                    public boolean test(final ConcurrentMap.Entry<MPJWTConfigKey, MPJWTConfigValue> e) {
                        return path.startsWith(e.getKey().toURI());
                    }
                })
                .findFirst();
    }


    public static class MPJWTConfigValue {
        private final String authMethod;
        private final String realm;

        public MPJWTConfigValue(final String authMethod, final String realm) {
            this.authMethod = authMethod;
            this.realm = realm;
        }

        public String getAuthMethod() {
            return authMethod;
        }

        public String getRealm() {
            return realm;
        }
    }

    public static class MPJWTConfigKey {
        private final String contextPath;
        private final String applicationPath;

        public MPJWTConfigKey(final String contextPath, final String applicationPath) {
            this.contextPath = contextPath;
            this.applicationPath = applicationPath;
        }

        public String getApplicationPath() {
            return applicationPath;
        }

        public String getContextPath() {
            return contextPath;
        }

        public String toURI() {
            return ("/" + contextPath + "/" + applicationPath).replaceAll("//", "/");
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final MPJWTConfigKey that = (MPJWTConfigKey) o;

            if (contextPath != null ? !contextPath.equals(that.contextPath) : that.contextPath != null) return false;
            return !(applicationPath != null ? !applicationPath.equals(that.applicationPath) : that.applicationPath != null);

        }

        @Override
        public int hashCode() {
            int result = contextPath != null ? contextPath.hashCode() : 0;
            result = 31 * result + (applicationPath != null ? applicationPath.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "MPJWTConfigKey{" +
                    "applicationPath='" + applicationPath + '\'' +
                    ", contextPath='" + contextPath + '\'' +
                    '}';
        }
    }
}