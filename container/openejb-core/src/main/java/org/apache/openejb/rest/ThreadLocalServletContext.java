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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

public class ThreadLocalServletContext extends AbstractRestThreadLocalProxy<ServletContext> implements ServletContext {
    protected ThreadLocalServletContext() {
        super(ServletContext.class);
    }

    @Override
    public String getContextPath() {
        return get().getContextPath();
    }

    @Override
    public ServletContext getContext(final String uripath) {
        return get().getContext(uripath);
    }

    @Override
    public int getMajorVersion() {
        return get().getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return get().getMinorVersion();
    }

    @Override
    public String getMimeType(final String file) {
        return get().getMimeType(file);
    }

    @Override
    public Set<String> getResourcePaths(final String path) {
        return get().getResourcePaths(path);
    }

    @Override
    public URL getResource(final String path) throws MalformedURLException {
        return get().getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(final String path) {
        return get().getResourceAsStream(path);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
        return get().getRequestDispatcher(path);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(final String name) {
        return get().getNamedDispatcher(name);
    }

    @Override
    public Servlet getServlet(final String name) throws ServletException {
        return get().getServlet(name);
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        return get().getServlets();
    }

    @Override
    public Enumeration<String> getServletNames() {
        return get().getServletNames();
    }

    @Override
    public void log(final String msg) {
        get().log(msg);
    }

    @Override
    public void log(final Exception exception, final String msg) {
        get().log(exception, msg);
    }

    @Override
    public void log(final String message, final Throwable throwable) {
        get().log(message, throwable);
    }

    @Override
    public String getRealPath(final String path) {
        return get().getRealPath(path);
    }

    @Override
    public String getServerInfo() {
        return get().getServerInfo();
    }

    @Override
    public String getInitParameter(final String name) {
        return get().getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return get().getInitParameterNames();
    }

    @Override
    public boolean setInitParameter(final String name, final String value) {
        return get().setInitParameter(name, value);
    }

    @Override
    public Object getAttribute(final String name) {
        return get().getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return get().getAttributeNames();
    }

    @Override
    public void setAttribute(final String name, final Object object) {
        get().setAttribute(name, object);
    }

    @Override
    public void removeAttribute(final String name) {
        get().removeAttribute(name);
    }

    @Override
    public String getServletContextName() {
        return get().getServletContextName();
    }



    @Override
    public ServletRegistration.Dynamic addServlet(final String servletName, final String className) throws IllegalArgumentException, IllegalStateException {
        return get().addServlet(servletName, className);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(final String servletName, final Servlet servlet) throws IllegalArgumentException, IllegalStateException {
        return get().addServlet(servletName, servlet);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(final String servletName, final Class<? extends Servlet> clazz) throws IllegalArgumentException, IllegalStateException {
        return get().addServlet(servletName, clazz);
    }

    @Override
    public <T extends Servlet> T createServlet(final Class<T> clazz) throws ServletException {
        return get().createServlet(clazz);
    }

    @Override
    public ServletRegistration getServletRegistration(final String servletName) {
        return get().getServletRegistration(servletName);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return get().getServletRegistrations();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(final String filterName, final String className) throws IllegalArgumentException, IllegalStateException {
        return get().addFilter(filterName, className);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(final String filterName, final Filter filter) throws IllegalArgumentException, IllegalStateException {
        return get().addFilter(filterName, filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(final String filterName, final Class<? extends Filter> filterClass) throws IllegalArgumentException, IllegalStateException {
        return get().addFilter(filterName, filterClass);
    }

    @Override
    public <T extends Filter> T createFilter(final Class<T> clazz) throws ServletException {
        return get().createFilter(clazz);
    }

    @Override
    public FilterRegistration getFilterRegistration(final String filterName) {
        return get().getFilterRegistration(filterName);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return get().getFilterRegistrations();
    }

    @Override
    public void addListener(final Class<? extends EventListener> listenerClass) {
        get().addListener(listenerClass);
    }

    @Override
    public void addListener(final String className) {
        get().addListener(className);
    }

    @Override
    public <T extends EventListener> void addListener(final T t) {
        get().addListener(t);
    }

    @Override
    public <T extends EventListener> T createListener(final Class<T> clazz) throws ServletException {
        return get().createListener(clazz);
    }

    @Override
    public void declareRoles(final String... roleNames) {
        get().declareRoles(roleNames);
    }

    @Override
    public String getVirtualServerName() {
        return get().getVirtualServerName();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return get().getSessionCookieConfig();
    }

    @Override
    public void setSessionTrackingModes(final Set<SessionTrackingMode> sessionTrackingModes) {
        get().setSessionTrackingModes(sessionTrackingModes);
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return get().getDefaultSessionTrackingModes();
    }

    @Override
    public int getEffectiveMajorVersion() throws UnsupportedOperationException {
        return get().getEffectiveMajorVersion();
    }

    @Override
    public int getEffectiveMinorVersion() throws UnsupportedOperationException {
        return get().getEffectiveMinorVersion();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return get().getEffectiveSessionTrackingModes();
    }

    @Override
    public ClassLoader getClassLoader() {
        return get().getClassLoader();
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return get().getJspConfigDescriptor();
    }

    public void setResponseCharacterEncoding(String encoding) {
        get().setResponseCharacterEncoding(encoding);
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String jspName, String jspFile) {
        return get().addJspFile(jspName, jspFile);
    }

    @Override
    public int getSessionTimeout() {
        return get().getSessionTimeout();
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
        get().setSessionTimeout(sessionTimeout);
    }

    @Override
    public String getRequestCharacterEncoding() {
        return get().getRequestCharacterEncoding();
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {
        get().setRequestCharacterEncoding(encoding);
    }

    @Override
    public String getResponseCharacterEncoding() {
        return get().getResponseCharacterEncoding();
    }
}
