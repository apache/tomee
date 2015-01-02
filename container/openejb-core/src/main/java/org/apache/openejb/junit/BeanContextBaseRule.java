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
package org.apache.openejb.junit;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Injector;
import org.apache.openejb.core.ThreadContext;

public abstract class BeanContextBaseRule {
    protected final Object instance;

    protected BeanContextBaseRule(final Object instance) {
        this.instance = instance;
    }

    protected BeanContext getBeanContext() {
        BeanContext beanContext = null;
        if (instance != null) {
            beanContext = Injector.resolve(instance.getClass());
        }
        if (beanContext == null) {
            final ThreadContext threadContext = ThreadContext.getThreadContext();
            if (threadContext != null) {
                beanContext = threadContext.getBeanContext();
            }
        }

        if (beanContext == null) {
            throw new IllegalStateException("Can't find test BeanContext");
        }
        return beanContext;
    }
}
