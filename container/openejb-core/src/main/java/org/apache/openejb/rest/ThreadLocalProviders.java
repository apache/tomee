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

package org.apache.openejb.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class ThreadLocalProviders extends AbstractRestThreadLocalProxy<Providers> implements Providers {
    protected ThreadLocalProviders() {
        super(Providers.class);
    }

    @Override
    public <T> ContextResolver<T> getContextResolver(final Class<T> rawType, final MediaType mediaType) {
        return get().getContextResolver(rawType, mediaType);
    }

    @Override
    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(final Class<T> rawType) {
        return get().getExceptionMapper(rawType);
    }

    @Override
    public <T> MessageBodyReader<T> getMessageBodyReader(final Class<T> rawType, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return get().getMessageBodyReader(rawType, genericType, annotations, mediaType);
    }

    @Override
    public <T> MessageBodyWriter<T> getMessageBodyWriter(final Class<T> rawType, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return get().getMessageBodyWriter(rawType, genericType, annotations, mediaType);
    }
}
