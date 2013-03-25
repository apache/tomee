/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.catalina.session;

import org.apache.catalina.session.StandardManager;
import org.apache.webbeans.proxy.javassist.OpenWebBeansClassLoaderProvider;

import java.io.IOException;

public class OWBStandardManager extends StandardManager {
    @Override
    public void load() throws ClassNotFoundException, IOException {
        initOWBJavassistLoaderprovider();
        try {
            super.load();
        } finally {
            resetOWBJavassistLoaderProvider();
        }
    }

    private void initOWBJavassistLoaderprovider() {
        try {
            if(javassist.util.proxy.ProxyFactory.classLoaderProvider instanceof OpenWebBeansClassLoaderProvider) {
                OpenWebBeansClassLoaderProvider.class.cast(javassist.util.proxy.ProxyFactory.classLoaderProvider).useCurrentClassLoader();
            }
        } catch (final Throwable t) {
            // no-op: using asm
        }
    }

    private void resetOWBJavassistLoaderProvider() {
        try {
            if(javassist.util.proxy.ProxyFactory.classLoaderProvider instanceof OpenWebBeansClassLoaderProvider) {
                OpenWebBeansClassLoaderProvider.class.cast(javassist.util.proxy.ProxyFactory.classLoaderProvider).reset();
            }
        } catch (final Throwable t) {
            // no-op
        }
    }
}
