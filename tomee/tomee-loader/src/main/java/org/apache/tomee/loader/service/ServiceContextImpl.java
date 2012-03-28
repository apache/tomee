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

package org.apache.tomee.loader.service;

import org.apache.tomee.loader.service.helper.JndiHelper;
import org.apache.tomee.loader.service.helper.JndiHelperImpl;
import org.apache.tomee.loader.service.helper.TestHelper;
import org.apache.tomee.loader.service.helper.TestHelperImpl;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ServiceContextImpl implements ServiceContext {
    private final Context ctx;
    private final JndiHelper jndiHelper;
    private final TestHelper testHelper;

    public void close() {
        if (this.ctx == null) {
            return; //do nothing
        }

        try {
            this.ctx.close();
        } catch (NamingException e) {
            throw new ServiceException(e);
        }
    }

    public ServiceContextImpl() {
        Context ctx = null;
        {
            final Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
            properties.put("openejb.loader", "embed");
            try {
                ctx = new InitialContext(properties);
            } catch (NamingException e) {
                throw new ServiceException(e);
            }
        }
        this.ctx = ctx;
        this.jndiHelper = new JndiHelperImpl(this);
        this.testHelper = new TestHelperImpl(this);
    }


    public List<Map<String, Object>> getJndi(String path) {
        return this.jndiHelper.getJndi(path);
    }

    public List<Map<String, Object>> getTest() {
        return this.testHelper.getTestResults();
    }

    public Context getContext() {
        return this.ctx;
    }
}
