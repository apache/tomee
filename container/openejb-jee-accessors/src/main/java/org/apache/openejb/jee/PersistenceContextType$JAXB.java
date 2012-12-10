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

public class PersistenceContextType$JAXB
        extends JAXBEnum<PersistenceContextType> {


    public PersistenceContextType$JAXB() {
        super(PersistenceContextType.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "persistenceContextType".intern()));
    }

    public PersistenceContextType parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parsePersistenceContextType(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, PersistenceContextType persistenceContextType)
            throws Exception {
        return toStringPersistenceContextType(bean, parameterName, context, persistenceContextType);
    }

    public static PersistenceContextType parsePersistenceContextType(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("Transaction".equals(value)) {
            return PersistenceContextType.TRANSACTION;
        } else if ("Extended".equals(value)) {
            return PersistenceContextType.EXTENDED;
        } else {
            context.unexpectedEnumValue(reader, PersistenceContextType.class, value, "Transaction", "Extended");
            return null;
        }
    }

    public static String toStringPersistenceContextType(Object bean, String parameterName, RuntimeContext context, PersistenceContextType persistenceContextType)
            throws Exception {
        if (PersistenceContextType.TRANSACTION == persistenceContextType) {
            return "Transaction";
        } else if (PersistenceContextType.EXTENDED == persistenceContextType) {
            return "Extended";
        } else {
            context.unexpectedEnumConst(bean, parameterName, persistenceContextType, PersistenceContextType.TRANSACTION, PersistenceContextType.EXTENDED);
            return null;
        }
    }

}
