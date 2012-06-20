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
package org.apache.openejb.arquillian.common;

public final class ArquillianUtil {
    private static final String OPENEJB_ADAPTER_SYSTEM_PROP = "openejb.arquillian.adapter";
    private static final String TOMEE_ADAPTER_SYSTEM_PROP = "tomee.arquillian.adapter";

    private ArquillianUtil() {
        // no-op
    }

    public static boolean isCurrentAdapter(final String name) {
        String adapter = System.getProperty(OPENEJB_ADAPTER_SYSTEM_PROP);
        if (adapter == null) {
            adapter = System.getProperty(TOMEE_ADAPTER_SYSTEM_PROP);
        }
        return adapter == null || name.equals(adapter);
    }
}
