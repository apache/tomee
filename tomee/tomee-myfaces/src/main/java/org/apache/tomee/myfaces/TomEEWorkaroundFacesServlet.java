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

import jakarta.faces.context.FacesContext;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;

@MultipartConfig // only there to be able to restore a FacesContext if lost cause of a forward in a JSF request
public class TomEEWorkaroundFacesServlet implements Servlet {
    private final FacesServlet delegate = new FacesServlet();

    @Override
    public void init(final ServletConfig config) throws ServletException {
        delegate.init(config);
    }

    @Override
    public ServletConfig getServletConfig() {
        return delegate.getServletConfig();
    }

    @Override
    public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {
        final FacesContext originalContext = FacesContext.getCurrentInstance(); // should be null in 80% of cases
        try {
            delegate.service(req, res);
        } finally {
            if (originalContext != null) {
                SetFacesContext.exec(originalContext);
            } // else delegate already released the context, we are fine
        }
    }

    @Override
    public String getServletInfo() {
        return delegate.getServletInfo();
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    private abstract static class SetFacesContext extends FacesContext {
        private static void exec(final FacesContext context) {
            setCurrentInstance(context);
        }
    }
}
