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
import org.apache.tomee.loader.service.helper.OpenEJBHelper;
import org.apache.tomee.loader.service.helper.OpenEJBHelperImpl;
import org.apache.tomee.loader.service.helper.TestHelper;
import org.apache.tomee.loader.service.helper.TestHelperImpl;

import java.util.List;
import java.util.Map;

public class ServiceContextImpl implements ServiceContext {
    private final OpenEJBHelper openEJBHelper;
    private final JndiHelper jndiHelper;
    private final TestHelper testHelper;

    public ServiceContextImpl() {
        this.openEJBHelper = new OpenEJBHelperImpl();
        this.jndiHelper = new JndiHelperImpl(this);
        this.testHelper = new TestHelperImpl(this);
    }

    @Override
    public OpenEJBHelper getOpenEJBHelper() {
        return openEJBHelper;
    }

    @Override
    public JndiHelper getJndiHelper() {
        return jndiHelper;
    }

    @Override
    public TestHelper getTestHelper() {
        return testHelper;
    }
}
