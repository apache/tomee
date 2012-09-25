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
package org.apache.webbeans.web.context;

import javax.servlet.http.HttpServletRequest;

import org.apache.webbeans.context.RequestContext;

/**
 * RequestContext which additionally holds the current servletRequest
 * for being able to lazily initialise the SessionContext if needed.
 */
public class ServletRequestContext extends RequestContext
{

    private static final long serialVersionUID = -8375349845543590243L;

    // this can only be accessed when the context is active
    private transient HttpServletRequest servletRequest;


    public ServletRequestContext()
    {
        super();
    }

    public HttpServletRequest getServletRequest()
    {
        if (active)
        {
            return servletRequest;
        }
        else
        {
            return null;
        }
    }

    public void setServletRequest(HttpServletRequest servletRequest)
    {
        this.servletRequest = servletRequest;
    }

    public void destroy()
    {
        super.destroy();
        servletRequest = null;
    }

}
