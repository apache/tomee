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
package org.apache.webbeans.web.tests;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class MockServletRequest implements HttpServletRequest
{

    public Object getAttribute(String name)
    {
        return null;
    }

    public Enumeration getAttributeNames()
    {
        return null;
    }

    public String getCharacterEncoding()
    {
        return null;
    }

    public void setCharacterEncoding(String env) throws UnsupportedEncodingException
    {
    }

    public int getContentLength()
    {
        return 0;
    }

    public String getContentType()
    {
        return null;
    }

    public ServletInputStream getInputStream() throws IOException
    {
        return null;
    }

    public String getParameter(String name)
    {
        return null;
    }

    public Enumeration getParameterNames()
    {
        return null;
    }

    public String[] getParameterValues(String name)
    {
        return new String[0];
    }

    public Map getParameterMap()
    {
        return null;
    }

    public String getProtocol()
    {
        return null;
    }

    public String getScheme()
    {
        return null;
    }

    public String getServerName()
    {
        return null;
    }

    public int getServerPort()
    {
        return 0;
    }

    public BufferedReader getReader() throws IOException
    {
        return null;
    }

    public String getRemoteAddr()
    {
        return null;
    }

    public String getRemoteHost()
    {
        return null;
    }

    public void setAttribute(String name, Object o)
    {
    }

    public void removeAttribute(String name)
    {
    }

    public Locale getLocale()
    {
        return null;
    }

    public Enumeration getLocales()
    {
        return null;
    }

    public boolean isSecure()
    {
        return false;
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        return null;
    }

    public String getRealPath(String path)
    {
        return null;
    }

    public int getRemotePort()
    {
        return 0;
    }

    public String getLocalName()
    {
        return null;
    }

    public String getLocalAddr()
    {
        return null;
    }

    public int getLocalPort()
    {
        return 0;
    }

    public String getAuthType()
    {
        return null;
    }

    public Cookie[] getCookies()
    {
        return new Cookie[0];
    }

    public long getDateHeader(String name)
    {
        return 0;
    }

    public String getHeader(String name)
    {
        return null;
    }

    public Enumeration getHeaders(String name)
    {
        return null;
    }

    public Enumeration getHeaderNames()
    {
        return null;
    }

    public int getIntHeader(String name)
    {
        return 0;
    }

    public String getMethod()
    {
        return null;
    }

    public String getPathInfo()
    {
        return null;
    }

    public String getPathTranslated()
    {
        return null; 
    }

    public String getContextPath()
    {
        return null;
    }

    public String getQueryString()
    {
        return null;
    }

    public String getRemoteUser()
    {
        return null;
    }

    public boolean isUserInRole(String role)
    {
        return false;
    }

    public Principal getUserPrincipal()
    {
        return null;
    }

    public String getRequestedSessionId()
    {
        return null;
    }

    public String getRequestURI()
    {
        return null;
    }

    public StringBuffer getRequestURL()
    {
        return null;
    }

    public String getServletPath()
    {
        return null;
    }

    public HttpSession getSession(boolean create)
    {
        return null;
    }

    public HttpSession getSession()
    {
        return null;
    }

    public boolean isRequestedSessionIdValid()
    {
        return false;
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        return false;
    }

    public boolean isRequestedSessionIdFromURL()
    {
        return false;
    }

    public boolean isRequestedSessionIdFromUrl()
    {
        return false;
    }
}
