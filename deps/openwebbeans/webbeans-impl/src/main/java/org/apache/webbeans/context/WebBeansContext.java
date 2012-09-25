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
package org.apache.webbeans.context;


import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 * Defines spi for contexts.
 *
 * @version $Rev$ $Date$
 */
public interface WebBeansContext extends javax.enterprise.context.spi.Context
{   
    /**
     * Initializes this contextual bag with
     * given creational context.
     * <p>
     * Given creational context is used fot creating the
     * bean instance.
     * </p>
     * @param <T> type
     * @param contextual contextual bean
     * @param creationalContext creational context
     */
    public <T> void initContextualBag(Contextual<T> contextual, CreationalContext<T> creationalContext);
    
    /**
     * Destroys the context.
     */
    public void destroy();

    /**
     * Gets creational context of the given bean.
     * @param <T> type
     * @param contextual contextual bean
     * @return creational context of the given bean
     */
    public <T> CreationalContext<T> getCreationalContext(Contextual<T> contextual);
}
