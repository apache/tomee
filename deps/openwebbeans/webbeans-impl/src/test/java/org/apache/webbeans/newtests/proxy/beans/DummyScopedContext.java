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
package org.apache.webbeans.newtests.proxy.beans;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Context for the DummyScope
 */
public class DummyScopedContext implements Context
{
    private Map<Contextual<?>, Object> map = new HashMap<Contextual<?>, Object>();

    @Override
    public <T> T get(Contextual<T> component)
    {
        return (T) map.get(component);
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return DummyScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> component, CreationalContext<T> creationalContext)
    {
        Object obj = map.get(component);
        if (obj == null)
        {
            obj = component.create(creationalContext);
            map.put(component, obj);
        }
        return (T) obj;
    }

    @Override
    public boolean isActive()
    {
        return true;
    }
}
