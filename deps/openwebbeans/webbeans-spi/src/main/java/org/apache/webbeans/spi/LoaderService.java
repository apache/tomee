/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.spi;

import java.util.List;

/**
 * Decouples OpenWebBeans from the Service-Loader approach.
 * It allows to provide custom loaders with custom mechanisms as well as support for Java 1.5
 */
public interface LoaderService
{
    /**
     * Loads all active implementations for the given service-type
     * @param serviceType base type of the services which should be loaded
     * @param <T> current type
     * @return all active implementations for the given service-type
     */
    <T> List<T> load(Class<T> serviceType);

    /**
     * Loads all active implementations for the given service-type
     * @param serviceType base type of the services which should be loaded
     * @param classLoader
     * @param <T> current type
     * @return all active implementations for the given service-type
     */
    <T> List<T> load(Class<T> serviceType, ClassLoader classLoader);
}
