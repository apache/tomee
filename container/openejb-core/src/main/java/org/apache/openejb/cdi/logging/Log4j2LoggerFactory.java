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

import org.apache.openejb.log.logger.Log4j2Logger;
import org.apache.webbeans.logger.WebBeansLoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class Log4j2LoggerFactory implements WebBeansLoggerFactory {
    @Override
    public Logger getLogger(final Class<?> clazz, final Locale desiredLocale) {
        return new Log4j2Logger(clazz.getName(), ResourceBundle.getBundle("openwebbeans/Messages", desiredLocale).toString());
    }

    @Override
    public Logger getLogger(final Class<?> clazz) {
        return new Log4j2Logger(clazz.getName(), "openwebbeans/Messages");
    }
}
