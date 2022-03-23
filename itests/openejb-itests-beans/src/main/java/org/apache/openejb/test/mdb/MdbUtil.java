/**
 *
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
package org.apache.openejb.test.mdb;

import jakarta.jms.Connection;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import java.lang.reflect.Method;

public class MdbUtil {

    public static String getSignature(final Method method) {
        final StringBuilder builder = new StringBuilder();
        builder.append(method.getName()).append("(");
        boolean first = true;
        for (final Class<?> type : method.getParameterTypes()) {
            if (!first) {
                builder.append(",");
            }
            builder.append(type.getName());
            first = false;
        }
        builder.append(")");
        return builder.toString();
    }

    public static void close(final MessageProducer closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Throwable e) {
                //Ignore
            }
        }
    }

    public static void close(final MessageConsumer closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Throwable e) {
                //Ignore
            }
        }
    }

    public static void close(final Session closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Throwable e) {
                //Ignore
            }
        }
    }

    public static void close(final Connection closeable) {
        if (closeable != null) {

            try {
                closeable.stop();
            } catch (final Throwable e) {
                //Ignore
            }

            try {
                closeable.close();
            } catch (final Throwable e) {
                //Ignore
            }
        }
    }
}
