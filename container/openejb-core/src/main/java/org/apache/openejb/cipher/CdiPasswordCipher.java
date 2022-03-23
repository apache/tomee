/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.cipher;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;

// use: cipher:cdi:<your bean class>:<encrypted pwd>
public final class CdiPasswordCipher implements PasswordCipher {
    @Override
    public char[] encrypt(final String plainPassword) {
        throw new UnsupportedOperationException("cdi password cipher only supports decryption");
    }

    @Override
    public String decrypt(final char[] encryptedPassword) {
        final String string = new String(encryptedPassword);
        final BeanManagerImpl mgr;
        try {
            final WebBeansContext wbc = WebBeansContext.currentInstance();
            mgr = wbc.getBeanManagerImpl();
            if (!mgr.isInUse()) { // not yet the time to use CDI, container is not started
                // would be cool to log a warning here but would pollute the logs with false positives
                return "cipher:cdi:" + string;
            }
        } catch (final IllegalStateException ise) { // no cdi
            return "cipher:cdi:" + string;
        }

        final int split = string.indexOf(':');
        final String delegate = string.substring(0, split);
        final String pwdStr = string.substring(split + 1, string.length());
        final char[] pwd = pwdStr.toCharArray();

        try {
            final Class<?> beanType = Thread.currentThread().getContextClassLoader().loadClass(delegate);
            final Bean<?> bean = mgr.resolve(mgr.getBeans(beanType));
            if (bean == null) {
                throw new IllegalArgumentException("No bean for " + delegate);
            }

            final CreationalContext<?> cc = mgr.createCreationalContext(null);
            try {
                return PasswordCipher.class.cast(mgr.getReference(bean, PasswordCipher.class, cc)).decrypt(pwd);
            } finally {
                if (!mgr.isNormalScope(bean.getScope())) {
                    cc.release();
                }
            }
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Can't find " + delegate, e);
        }
    }
}
