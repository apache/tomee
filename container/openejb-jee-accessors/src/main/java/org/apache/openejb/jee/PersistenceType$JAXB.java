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

public class PersistenceType$JAXB
        extends JAXBEnum<PersistenceType> {


    public PersistenceType$JAXB() {
        super(PersistenceType.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "persistenceType".intern()));
    }

    public PersistenceType parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parsePersistenceType(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, PersistenceType persistenceType)
            throws Exception {
        return toStringPersistenceType(bean, parameterName, context, persistenceType);
    }

    public static PersistenceType parsePersistenceType(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("Bean".equals(value)) {
            return PersistenceType.BEAN;
        } else if ("Container".equals(value)) {
            return PersistenceType.CONTAINER;
        } else {
            context.unexpectedEnumValue(reader, PersistenceType.class, value, "Bean", "Container");
            return null;
        }
    }

    public static String toStringPersistenceType(Object bean, String parameterName, RuntimeContext context, PersistenceType persistenceType)
            throws Exception {
        if (PersistenceType.BEAN == persistenceType) {
            return "Bean";
        } else if (PersistenceType.CONTAINER == persistenceType) {
            return "Container";
        } else {
            context.unexpectedEnumConst(bean, parameterName, persistenceType, PersistenceType.BEAN, PersistenceType.CONTAINER);
            return null;
        }
    }

}
