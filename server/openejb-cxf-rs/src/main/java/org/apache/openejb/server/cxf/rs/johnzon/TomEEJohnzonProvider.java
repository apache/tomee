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

import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.johnzon.mapper.MapperBuilder;

import jakarta.json.JsonStructure;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.ext.Provider;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces({"application/json", "application/*+json"})
@Consumes({"application/json", "application/*+json"})
public class TomEEJohnzonProvider<T> extends JohnzonProvider<T> {
    public TomEEJohnzonProvider() {
        super(new MapperBuilder().setAccessModeName("both").build(), null);
    }

    @Override
    public boolean isWriteable(final Class<?> rawType, final Type genericType,
                               final Annotation[] annotations, final MediaType mediaType) {
        return super.isWriteable(rawType, genericType, annotations, mediaType)
                && !OutputStream.class.isAssignableFrom(rawType)
                && !StreamingOutput.class.isAssignableFrom(rawType)
                && !Writer.class.isAssignableFrom(rawType)
                && !Response.class.isAssignableFrom(rawType)
                && !JsonStructure.class.isAssignableFrom(rawType);
    }
}
