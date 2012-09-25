/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.cditest.owb;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Mock ServletContext needed to startup the container.
 *
 */
public class MockServletContext implements ServletContext
{

    @SuppressWarnings("unchecked")
    private Hashtable attributes = new Hashtable();

    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames()
    {
        return attributes.keys();
    }

    public ServletContext getContext(String uripath)
    {
        return this;
    }

    public String getContextPath()
    {
        return "mockContextpath";
    }

    public String getInitParameter(String name)
    {
        return null;
    }

    @SuppressWarnings("unchecked")
    public Enumeration getInitParameterNames()
    {
        return new StringTokenizer(""); // 'standard' empty Enumeration
    }

    public int getMajorVersion()
    {
        return 2;
    }

    public String getMimeType(String file)
    {
        return null;
    }

    public int getMinorVersion()
    {
        return 0;
    }

    public RequestDispatcher getNamedDispatcher(String name)
    {
        return null;
    }

    public String getRealPath(String path)
    {
        return "mockRealPath";
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        return null;
    }

    public URL getResource(String path) throws MalformedURLException
    {
        return null;
    }

    public InputStream getResourceAsStream(String path)
    {
        return null;
    }

    @SuppressWarnings("unchecked")
    public Set getResourcePaths(String path)
    {
        return null;
    }

    public String getServerInfo()
    {
        return "mockServer";
    }

    public Servlet getServlet(String name) throws ServletException
    {
        return null;
    }

    public String getServletContextName()
    {
        return null;
    }

    @SuppressWarnings("unchecked")
    public Enumeration getServletNames()
    {
        return null;
    }

    @SuppressWarnings("unchecked")
    public Enumeration getServlets()
    {
        return null;
    }

    public void log(String msg)
    {
        // TODO
    }

    public void log(Exception exception, String msg)
    {
        // TODO

    }

    public void log(String message, Throwable throwable)
    {
        // TODO

    }

    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    @SuppressWarnings("unchecked")
    public void setAttribute(String name, Object object)
    {
        attributes.put(name, object);
    }

}
