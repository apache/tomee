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

package org.apache.openejb.cdi;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import java.beans.FeatureDescriptor;
import java.util.Iterator;

public class WebAppElResolver extends ELResolver {
    private final ELResolver parent;
    private final ELResolver resolver;

    public WebAppElResolver(final ELResolver elResolver, final ELResolver elResolver1) {
        resolver = elResolver;
        parent = elResolver1;
    }

    @Override
    public Object getValue(final ELContext context, final Object base, final Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        Object value = resolver.getValue(context, base, property);
        if (value == null) {
            value = parent.getValue(context, base, property);
        }
        return value;
    }

    @Override
    public Class<?> getType(final ELContext context, final Object base, final Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        Class<?> value = resolver.getType(context, base, property);
        if (value == null) {
            value = parent.getType(context, base, property);
        }
        return value;
    }

    @Override
    public void setValue(final ELContext context, final Object base, final Object property, final Object value) throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
        // no-op
    }

    @Override
    public boolean isReadOnly(final ELContext context, final Object base, final Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext context, final Object base) {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(final ELContext context, final Object base) {
        return null;
    }
}
