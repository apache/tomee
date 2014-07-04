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

import org.metatype.sxc.jaxb.JAXBEnum;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.XoXMLStreamReader;

import javax.xml.namespace.QName;

public class SessionType$JAXB
    extends JAXBEnum<SessionType> {


    public SessionType$JAXB() {
        super(SessionType.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "sessionType".intern()));
    }

    public SessionType parse(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        return parseSessionType(reader, context, value);
    }

    public String toString(final Object bean, final String parameterName, final RuntimeContext context, final SessionType sessionType)
        throws Exception {
        return toStringSessionType(bean, parameterName, context, sessionType);
    }

    public static SessionType parseSessionType(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        if ("Stateful".equals(value)) {
            return SessionType.STATEFUL;
        } else if ("Stateless".equals(value)) {
            return SessionType.STATELESS;
        } else if ("Singleton".equals(value)) {
            return SessionType.SINGLETON;
        } else if ("Managed".equals(value)) {
            return SessionType.MANAGED;
        } else {
            context.unexpectedEnumValue(reader, SessionType.class, value, "Stateful", "Stateless", "Singleton", "Managed");
            return null;
        }
    }

    public static String toStringSessionType(final Object bean, final String parameterName, final RuntimeContext context, final SessionType sessionType)
        throws Exception {
        if (SessionType.STATEFUL == sessionType) {
            return "Stateful";
        } else if (SessionType.STATELESS == sessionType) {
            return "Stateless";
        } else if (SessionType.SINGLETON == sessionType) {
            return "Singleton";
        } else if (SessionType.MANAGED == sessionType) {
            return "Managed";
        } else {
            context.unexpectedEnumConst(bean, parameterName, sessionType, SessionType.STATEFUL, SessionType.STATELESS, SessionType.SINGLETON, SessionType.MANAGED);
            return null;
        }
    }

}
