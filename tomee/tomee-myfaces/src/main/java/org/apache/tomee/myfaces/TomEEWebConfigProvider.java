/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.myfaces;

import org.apache.myfaces.spi.ServletMapping;
import org.apache.myfaces.spi.impl.DefaultWebConfigProvider;
import org.apache.myfaces.spi.impl.ServletMappingImpl;
import org.apache.openejb.util.reflection.Reflections;

import jakarta.faces.context.ExternalContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import java.util.List;
import java.util.Map;

// support web-fragment.xml as well
public class TomEEWebConfigProvider extends DefaultWebConfigProvider {
    @Override
    public List<ServletMapping> getFacesServletMappings(final ExternalContext externalContext) {
        final List<ServletMapping> facesServletMappings = super.getFacesServletMappings(externalContext);
        try { // getContext() is a runtime object where getServletRegistrations() is forbidden so unwrap
            final ServletContext sc = ServletContext.class.cast(Reflections.get(externalContext.getContext(), "sc"));
            if (sc != null && sc.getServletRegistrations() != null) {
                for (final Map.Entry<String, ? extends ServletRegistration> reg : sc.getServletRegistrations().entrySet()) {
                    final ServletRegistration value = reg.getValue();
                    if ("jakarta.faces.webapp.FacesServlet".equals(value.getClassName())) {
                        for (final String mapping : value.getMappings()) {
                            final Class<?> clazz = sc.getClassLoader().loadClass(value.getClassName());
                            final org.apache.myfaces.shared_impl.webapp.webxml.ServletMapping mappingImpl =
                                    new org.apache.myfaces.shared_impl.webapp.webxml.ServletMapping(
                                        value.getName(), clazz, mapping);
                            facesServletMappings.add(new ServletMappingImpl(mappingImpl));
                        }
                    }
                }
            } else {
                facesServletMappings.addAll(super.getFacesServletMappings(externalContext));
            }
        } catch (final Exception e) { // don't fail cause our cast failed
            facesServletMappings.clear();
            facesServletMappings.addAll(super.getFacesServletMappings(externalContext));
        }
        return facesServletMappings;
    }
}
