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

import javax.json.bind.JsonbConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import java.util.Locale;

@Provider
// This will sort the Provider to be after CXF defaults. Check org.apache.cxf.jaxrs.provider.ProviderFactory.sortReaders()
@Produces({"application/json", "application/*+json"})
@Consumes({"application/json", "application/*+json"})
public class TomEEJsonbProvider<T> extends JsonbJaxrsProvider<T> {
    public TomEEJsonbProvider() {
        config.withPropertyVisibilityStrategy(new TomEEJsonbPropertyVisibilityStrategy());
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
