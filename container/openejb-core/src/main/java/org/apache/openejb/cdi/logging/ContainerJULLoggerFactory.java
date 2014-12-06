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
package org.apache.openejb.cdi.logging;

import org.apache.webbeans.logger.WebBeansLoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ContainerJULLoggerFactory implements WebBeansLoggerFactory {
    @Override
    public Logger getLogger(final Class<?> clazz, final Locale desiredLocale) {
        final Thread th = Thread.currentThread();
        final ClassLoader l = th.getContextClassLoader();
        th.setContextClassLoader(WebBeansLoggerFactory.class.getClassLoader());
        try {
            return Logger.getLogger(clazz.getName(), ResourceBundle.getBundle("openwebbeans/Messages", desiredLocale).toString());
        } finally {
            th.setContextClassLoader(l);
        }
    }

    @Override
    public Logger getLogger(final Class<?> clazz) {
        final Thread th = Thread.currentThread();
        final ClassLoader l = th.getContextClassLoader();
        th.setContextClassLoader(WebBeansLoggerFactory.class.getClassLoader());
        try {
            return Logger.getLogger(clazz.getName(), "openwebbeans/Messages");
        } finally {
            th.setContextClassLoader(l);
        }
    }
}

