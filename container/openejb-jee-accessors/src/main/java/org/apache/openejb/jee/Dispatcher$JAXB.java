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
package org.apache.openejb.jee;

import com.envoisolutions.sxc.jaxb.JAXBEnum;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.XoXMLStreamReader;

import javax.xml.namespace.QName;

public class Dispatcher$JAXB
        extends JAXBEnum<Dispatcher> {


    public Dispatcher$JAXB() {
        super(Dispatcher.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "dispatcherType".intern()));
    }

    public Dispatcher parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parseDispatcher(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, Dispatcher dispatcher)
            throws Exception {
        return toStringDispatcher(bean, parameterName, context, dispatcher);
    }

    public static Dispatcher parseDispatcher(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("FORWARD".equals(value)) {
            return Dispatcher.FORWARD;
        } else if ("REQUEST".equals(value)) {
            return Dispatcher.REQUEST;
        } else if ("INCLUDE".equals(value)) {
            return Dispatcher.INCLUDE;
        } else if ("ASYNC".equals(value)) {
            return Dispatcher.ASYNC;
        } else if ("ERROR".equals(value)) {
            return Dispatcher.ERROR;
        } else {
            context.unexpectedEnumValue(reader, Dispatcher.class, value, "FORWARD", "REQUEST", "INCLUDE", "ASYNC", "ERROR");
            return null;
        }
    }

    public static String toStringDispatcher(Object bean, String parameterName, RuntimeContext context, Dispatcher dispatcher)
            throws Exception {
        if (Dispatcher.FORWARD == dispatcher) {
            return "FORWARD";
        } else if (Dispatcher.REQUEST == dispatcher) {
            return "REQUEST";
        } else if (Dispatcher.INCLUDE == dispatcher) {
            return "INCLUDE";
        } else if (Dispatcher.ASYNC == dispatcher) {
            return "ASYNC";
        } else if (Dispatcher.ERROR == dispatcher) {
            return "ERROR";
        } else {
            context.unexpectedEnumConst(bean, parameterName, dispatcher, Dispatcher.FORWARD, Dispatcher.REQUEST, Dispatcher.INCLUDE, Dispatcher.ASYNC, Dispatcher.ERROR);
            return null;
        }
    }

}
