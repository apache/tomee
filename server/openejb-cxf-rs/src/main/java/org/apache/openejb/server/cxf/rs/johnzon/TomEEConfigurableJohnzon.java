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
package org.apache.openejb.server.cxf.rs.johnzon;

import org.apache.johnzon.jaxrs.ConfigurableJohnzonProvider;
import org.apache.johnzon.mapper.Adapter;
import org.apache.johnzon.mapper.Converter;
import org.apache.johnzon.mapper.MapperBuilder;
import org.apache.johnzon.mapper.converter.DateConverter;
import org.apache.johnzon.mapper.internal.ConverterAdapter;
import org.apache.openejb.util.reflection.Reflections;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static java.util.Arrays.asList;

@Provider
@Produces("application/json")
@Consumes("application/json")
public class TomEEConfigurableJohnzon<T> extends ConfigurableJohnzonProvider<T> {
    private MapperBuilder localBuilder;

    public void setConverters(final Collection<Converter<?>> converters) {
        for (final Converter<?> converter : converters) {
            final Type type = findType(converter, Converter.class);
            builder().addAdapter(ParameterizedType.class.cast(type).getActualTypeArguments()[0], String.class, new ConverterAdapter(converter, type));
        }
    }

    public void setConverter(final Converter<?> converter) {
        setConverters(Collections.<Converter<?>>singletonList(converter));
    }

    public void setAdapter(final Adapter<?, ?> converter) {
        setAdapters(Collections.<Adapter<?, ?>>singletonList(converter));
    }

    // @Experimental
    public void setAdapters(final Collection<Adapter<?, ?>> adapters) {
        for (final Adapter<?, ?> adapter : adapters) {
            final Type type = findType(adapter, Adapter.class);
            final ParameterizedType pt = ParameterizedType.class.cast(type);
            final Type[] actualTypeArguments = pt.getActualTypeArguments();
            builder().addAdapter(actualTypeArguments[0], actualTypeArguments[1], adapter);
        }
    }

    public void setDatePattern(final String datePattern) {
        builder().addAdapter(Date.class, String.class, new ConverterAdapter<>(new DateConverter(datePattern), Date.class));
    }

    private Type findType(final Object ref, final Class<?> api) { // need to impl adapters directly
        for (final Type type : ref.getClass().getGenericInterfaces()) {
            if (ParameterizedType.class.isInstance(type) && ParameterizedType.class.cast(type).getRawType() == api) {
                return type;
            }
        }
        throw new IllegalArgumentException("Didn't find " + ref + " in interfaces: " + asList(ref.getClass().getGenericInterfaces()));
    }

    private MapperBuilder builder() {
        return localBuilder == null ? (localBuilder = MapperBuilder.class.cast(Reflections.get(this, "builder"))) : localBuilder;
    }
}
