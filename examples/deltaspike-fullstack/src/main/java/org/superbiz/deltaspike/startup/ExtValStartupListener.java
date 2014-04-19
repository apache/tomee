/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.superbiz.deltaspike.startup;

import org.apache.myfaces.extensions.validator.core.DefaultExtValCoreConfiguration;
import org.apache.myfaces.extensions.validator.core.ExtValCoreConfiguration;
import org.apache.myfaces.extensions.validator.core.proxy.DefaultProxyHelper;
import org.apache.myfaces.extensions.validator.core.proxy.ProxyHelper;
import org.apache.myfaces.extensions.validator.core.startup.AbstractStartupListener;

//TODO remove it after upgrading to ExtVal r8+
public class ExtValStartupListener extends AbstractStartupListener
{
    @Override
    protected void init()
    {
        ExtValCoreConfiguration.use(new DefaultExtValCoreConfiguration() {
            @Override
            public ProxyHelper proxyHelper() {
                return new DefaultProxyHelper() {
                    @Override
                    public boolean isProxiedClass(Class currentClass) {
                        if (currentClass == null || currentClass.getSuperclass() == null) {
                            return false;
                        }
                        return currentClass.getName().startsWith(currentClass.getSuperclass().getName()) &&
                                currentClass.getName().contains("$$");
                    }
                };
            }
        }, true);
    }
}
