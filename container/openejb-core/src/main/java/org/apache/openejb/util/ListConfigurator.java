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

package org.apache.openejb.util;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.xbean.recipe.ObjectRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class ListConfigurator {
    private ListConfigurator() {
        // no-op
    }

    public static <T> List<T> getList(final Properties properties, final String key, final ClassLoader classloader, final Class<T> filter) {
        if (properties == null) {
            return null;
        }

        final String features = properties.getProperty(key);
        if (features == null) {
            return null;
        }

        final List<T> list = new ArrayList<>();
        final String[] split = features.trim().split(",");
        for (final String feature : split) {
            if (feature == null || feature.trim().isEmpty()) {
                continue;
            }

            final String prefix = key + "." + feature + ".";
            final ObjectRecipe recipe = new ObjectRecipe(feature);
            for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
                final String current = entry.getKey().toString();
                if (current.startsWith(prefix)) {
                    final String property = current.substring(prefix.length());
                    recipe.setProperty(property, entry.getValue());
                }
            }

            final Object instance = recipe.create(classloader);
            if (!filter.isInstance(instance)) {
                throw new OpenEJBRuntimeException(feature + " is not an abstract feature");
            }
            list.add(filter.cast(instance));
        }

        if (list.isEmpty()) {
            return null;
        }
        return list;
    }
}

