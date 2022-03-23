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
package org.apache.openejb.server.cxf.rs.johnzon;

import org.apache.johnzon.jaxrs.jsonb.jaxrs.JsonbJaxrsProvider;
import org.apache.johnzon.mapper.access.AccessMode;

import jakarta.activation.DataSource;
import jakarta.annotation.Priority;
import jakarta.json.bind.JsonbConfig;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import java.io.File;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Locale;

@Provider
// This will sort the Provider to be after CXF defaults. Check org.apache.cxf.jaxrs.provider.ProviderFactory.sortReaders()
@Produces({"application/json", "application/*+json"})
@Consumes({"application/json", "application/*+json"})
@Priority(value = 5000)
public class TomEEJsonbProvider<T> extends JsonbJaxrsProvider<T> {
    public TomEEJsonbProvider() {
        config.withPropertyVisibilityStrategy(new TomEEJsonbPropertyVisibilityStrategy());
        setThrowNoContentExceptionOnEmptyStreams(true); // this is to make TCK tests happy
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // let the CXF built-in writer handle this one
        // TODO: add a setting?
        if (DataSource.class.isAssignableFrom(type)) return false;
        if (byte[].class.isAssignableFrom(type)) return false;
        if (File.class.isAssignableFrom(type)) return false;
        if (Reader.class.isAssignableFrom(type)) return false;

        return super.isWriteable(type, genericType, annotations, mediaType);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // let the CXF built-in writer handle this one
        // TODO: add a setting?
        if (DataSource.class.isAssignableFrom(type)) return false;
        if (byte[].class.isAssignableFrom(type)) return false;
        if (File.class.isAssignableFrom(type)) return false;
        if (Reader.class.isAssignableFrom(type)) return false;

        return super.isReadable(type, genericType, annotations, mediaType);
    }

    public void setDateFormat(String dateFormat) {
        config.setProperty(JsonbConfig.DATE_FORMAT, dateFormat);
    }

    public void setLocale(Locale locale) {
        config.setProperty(JsonbConfig.LOCALE, locale);
    }

    public void setAccessMode(AccessMode accessMode) {
        config.setProperty("johnzon.accessMode", accessMode);
    }

}
