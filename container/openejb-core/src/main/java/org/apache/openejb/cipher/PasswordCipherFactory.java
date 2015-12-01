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

package org.apache.openejb.cipher;

import org.apache.xbean.finder.ResourceFinder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

@SuppressWarnings("deprecation")
public class PasswordCipherFactory {
    /**
     * Create a {@link org.apache.openejb.cipher.PasswordCipher} instance from the
     * passwordCipher class name.
     *
     * @param passwordCipherClass the password cipher to look for
     * @return the password cipher from the passwordCipher class name
     * optionally set.
     */
    public static PasswordCipher getPasswordCipher(final String passwordCipherClass) {
        final PasswordCipher cipher;
        try {
            cipher = doInternalPasswordCipher(PasswordCipher.class, passwordCipherClass);

        } catch (final PasswordCipherException initial) {
            try {
                return doInternalPasswordCipher(org.apache.openejb.resource.jdbc.cipher.PasswordCipher.class, passwordCipherClass);

            } catch (final PasswordCipherException ignore) {
                throw initial;
            }
        }
        return cipher;
    }

    private static <T extends PasswordCipher> T doInternalPasswordCipher(final Class<T> intf, final String passwordCipherClass) {
        // Load the password cipher class
        Class<? extends T> pwdCipher;

        // try looking for implementation in /META-INF/org.apache.openejb.cipher.PasswordCipher
        final ResourceFinder finder = new ResourceFinder("META-INF/");
        final Map<String, Class<? extends T>> impls;
        try {
            impls = finder.mapAllImplementations(intf);

        } catch (final Throwable t) {
            final String message =
                "Password cipher '" + passwordCipherClass +
                    "' not found in META-INF/org.apache.openejb.cipher.PasswordCipher.";
            throw new PasswordCipherException(message, t);
        }
        pwdCipher = impls.get(passwordCipherClass);

        //
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        final URL url = tccl.getResource("META-INF/" + intf.getName() + "/" + passwordCipherClass);
        if (url != null) {
            try {
                final String clazz = new BufferedReader(new InputStreamReader(url.openStream())).readLine().trim();
                pwdCipher = tccl.loadClass(clazz).asSubclass(intf);
            } catch (final Exception e) {
                // ignored
            }
        }

        // if not found in META-INF/org.apache.openejb.cipher.PasswordCipher
        // we can try to load the class.
        if (null == pwdCipher) {
            try {
                try {
                    pwdCipher = Class.forName(passwordCipherClass).asSubclass(intf);

                } catch (final ClassNotFoundException cnfe) {
                    pwdCipher = tccl.loadClass(passwordCipherClass).asSubclass(intf);
                }
            } catch (final Throwable t) {
                final String message = "Cannot load password cipher class '" + passwordCipherClass + "'";
                throw new PasswordCipherException(message, t);
            }
        }

        // Create an instance
        try {
            return pwdCipher.newInstance();
        } catch (final Throwable t) {
            final String message = "Cannot create password cipher instance";
            throw new PasswordCipherException(message, t);
        }

    }
}
