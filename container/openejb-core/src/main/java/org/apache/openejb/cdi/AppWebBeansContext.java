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

package org.apache.openejb.cdi;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectableBeanManager;

import java.util.Map;
import java.util.Properties;

public class AppWebBeansContext extends WebBeansContext {
    private final InjectableBeanManager ibm;
    private BeanManagerImpl bm;

    public AppWebBeansContext(final Map<Class<?>, Object> services, final Properties properties) {
        super(services, properties);
        ibm = new InjectableBeanManager(getBeanManagerImpl());
    }

    @Override
    public InjectableBeanManager getInjectableBeanManager() {
        return ibm;
    }

    @SuppressWarnings("PMD.DoubleCheckedLocking")
    @Override
    public BeanManagerImpl getBeanManagerImpl() {
        if (bm == null) { // should be done in the constructor
            synchronized (this) {
                if (bm == null) {
                    bm = new AppBeanManager(this);
                }
            }
        }
        return bm;
    }
}
