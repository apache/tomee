/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.conf;

import org.apache.openjpa.conf.CacheMarshaller.ValidationPolicy;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;

/**
 * Compute validity based on whether or not the cached data is from the same
 * version of Kodo as the current install. This also checks OpenJPA version
 * information in case the OpenJPA jars were independently updated.
 *
 * @since 1.1.0
 */
public class OpenJPAVersionAndConfigurationTypeValidationPolicy
    implements ValidationPolicy, Configurable {

    private String confClassName;

    public Object getCacheableData(Object o) {
        return new Object[] {
            OpenJPAVersion.VERSION_ID,
            confClassName,
            o,
        };
    }

    public Object getValidData(Object o) {
        Object[] array = (Object[]) o;
        if (array.length != 3)
            return null;

        if (OpenJPAVersion.VERSION_ID.equals(array[0])
            && confClassName.equals(array[1]))
            return array[2];
        else
            return null;
    }

    public void setConfiguration(Configuration conf) {
        confClassName = conf.getClass().getName();
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
    }
}
