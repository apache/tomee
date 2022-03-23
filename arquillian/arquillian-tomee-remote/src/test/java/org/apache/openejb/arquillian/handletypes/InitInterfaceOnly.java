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
package org.apache.openejb.arquillian.handletypes;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.HandlesTypes;
import java.io.IOException;
import java.util.Set;

@HandlesTypes(API.class)
public class InitInterfaceOnly implements ServletContainerInitializer {
    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext servletContext) throws ServletException {
        servletContext.addServlet("list", new ListServlet(classes)).addMapping("/list");
    }

    private static class ListServlet implements Servlet {
        private final Set<Class<?>> list;

        public ListServlet(final Set<Class<?>> classes) {
            this.list = classes;
        }

        @Override
        public void init(final ServletConfig servletConfig) throws ServletException {
            // no-op
        }

        @Override
        public ServletConfig getServletConfig() {
            return null;
        }

        @Override
        public void service(final ServletRequest servletRequest, final ServletResponse servletResponse) throws ServletException, IOException {
            if (list == null) {
                servletResponse.getWriter().write("oops");
            } else {
                servletResponse.getWriter().write(list.toString());
            }
        }

        @Override
        public String getServletInfo() {
            return null;
        }

        @Override
        public void destroy() {

        }
    }
}
