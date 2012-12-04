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
package org.apache.webbeans.inject;

/**
 * Marker interface for all injectable elements of the web beans components.
 * <p>
 * There are several injectable elements in the web beans container;
 * <ul>
 * <li>Constructor,</li>
 * <li>Methods,</li>
 * <li>Fields</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @since 1.0
 * @see InjectableConstructor
 * @see InjectableField
 * @see InjectableMethods
 * @see AbstractInjectable
 */
public interface Injectable
{
    /**
     * Responsible for injecting the owner required injected component
     * instances. Maybe returning an component instance, for example,
     * {@link InjectableConstructor#doInjection()} returns a new web bean
     * component instance.
     * <p>
     * Each injetable elements parameters, web beans component instances, are
     * resolved using the resolution type algorithm.
     * </p>
     * 
     * @return if the return instance if available
     */
    public Object doInjection();
}
