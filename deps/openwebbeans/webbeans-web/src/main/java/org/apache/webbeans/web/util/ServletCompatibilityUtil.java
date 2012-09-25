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
package org.apache.webbeans.web.util;

import javax.servlet.ServletContext;

/**
 * This utility helps to be compatible with Servlet API 2.4
 */
public class ServletCompatibilityUtil
{

    // avoid instantiation

    private ServletCompatibilityUtil()
    {
    }

    /**
     * Returns an information about the given servlet context.
     * In case of Servlet API 2.5 or higher the context name will be returned.
     *
     * @param servletContext A given servlet context or null.
     * @return The info, or the string "null"
     */
    public static String getServletInfo(ServletContext servletContext)
    {
        if (servletContext != null)
        {
            if (servletContext.getMajorVersion() >= 3 ||
                    servletContext.getMajorVersion() == 2 && servletContext.getMinorVersion() >= 5)
            {
                return servletContext.getContextPath();
            }
            else
            {
                return servletContext.getServletContextName();
            }
        }
        else
        {
            return "null";
        }
    }
}
