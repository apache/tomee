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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class MockHttpSession implements HttpSession
{
    public static final String SESSION_ID = "testSessionId";

    private Map<String, Object> attributes = new HashMap<String, Object>();

    public Object getAttribute(String key)
    {
        return attributes.get(key);
    }

    public Enumeration getAttributeNames()
    {
        return null;
    }

    public long getCreationTime()
    {
        return 0;
    }

    public String getId()
    {
        return SESSION_ID;
    }

    public long getLastAccessedTime()
    {
        return 0;
    }

    public int getMaxInactiveInterval()
    {
        return 0;
    }

    public ServletContext getServletContext()
    {
        return null;
    }

    public HttpSessionContext getSessionContext()
    {
        return null;
    }

    public Object getValue(String arg0)
    {
        return null;
    }

    public String[] getValueNames()
    {
        return null;
    }

    public void invalidate()
    {

    }

    public boolean isNew()
    {
        return false;
    }

    public void putValue(String arg0, Object arg1)
    {

    }

    public void removeAttribute(String key)
    {
        attributes.remove(key);
    }

    public void removeValue(String arg0)
    {

    }

    public void setAttribute(String key, Object value)
    {
        attributes.put(key, value);
    }

    public void setMaxInactiveInterval(int arg0)
    {

    }
}