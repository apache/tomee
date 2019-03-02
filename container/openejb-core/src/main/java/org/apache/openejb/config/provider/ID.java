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

package org.apache.openejb.config.provider;

import java.util.Objects;

/**
 * IDs are not case-sensitive
 */
public class ID {

    private final String namespace;
    private final String name;

    public ID(final String namespace, final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.namespace = namespace != null ? namespace.toLowerCase() : null;
        this.name = name.toLowerCase();
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public void validate() {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
    }

    public static ID parse(final String raw) {
        return parse(raw, (String) null);
    }

    public static ID parse(final String raw, final ID id) {
        return parse(raw, id.getNamespace());
    }

    public static ID parse(final String raw, final String namespace) {
        if (raw == null) {
            throw new NullPointerException("provider id cannot be null");
        }

        final String[] parts = raw.split("[#:]");

        if (parts.length == 1) {

            return new ID(namespace, parts[0]);

        } else if (parts.length == 2) {

            return new ID(parts[0], parts[1]);
        }

        throw new MalformedProviderNameException(raw);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ID id = (ID) o;

        if (!name.equals(id.name)) {
            return false;
        }
        if (!Objects.equals(namespace, id.namespace)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = namespace != null ? namespace.hashCode() : 0;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ID{" +
            "namespace='" + namespace + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
