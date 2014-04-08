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
package org.apache.openejb.core.mdb;

import javax.jms.MessageProducer;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Connection;
import java.lang.reflect.Method;

public class MdbUtil {
    public static String getSignature(Method method){
        StringBuilder builder = new StringBuilder();
        builder.append(method.getName()).append("(");
        boolean first = true;
        for (Class<?> type : method.getParameterTypes()) {
            if (!first) {
                builder.append(",");
            }
            builder.append(type);
            first = false;
        }
        builder.append(")");
        return builder.toString();
    }

    public static void close(MessageProducer closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (JMSException e) {
            }
        }
    }

    public static void close(MessageConsumer closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (JMSException e) {
            }
        }
    }

    public static void close(Session closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (JMSException e) {
            }
        }
    }

    public static void close(Connection closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (JMSException e) {
            }
        }
    }
}
