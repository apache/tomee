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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import java.util.Enumeration;

public class ThreadLocalServletConfig extends AbstractRestThreadLocalProxy<ServletConfig> implements ServletConfig {
    protected ThreadLocalServletConfig() {
        super(ServletConfig.class);
    }

    @Override
    public String getServletName() {
        return get().getServletName();
    }

    @Override
    public ServletContext getServletContext() {
        return get().getServletContext();
    }

    @Override
    public String getInitParameter(final String name) {
        return get().getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return get().getInitParameterNames();
    }
}
