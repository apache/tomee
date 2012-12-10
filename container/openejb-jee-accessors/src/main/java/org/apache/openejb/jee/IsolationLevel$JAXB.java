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

public class IsolationLevel$JAXB
        extends JAXBEnum<IsolationLevel> {


    public IsolationLevel$JAXB() {
        super(IsolationLevel.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "isolation-levelType".intern()));
    }

    public IsolationLevel parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parseIsolationLevel(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, IsolationLevel isolationLevel)
            throws Exception {
        return toStringIsolationLevel(bean, parameterName, context, isolationLevel);
    }

    public static IsolationLevel parseIsolationLevel(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("TRANSACTION_READ_UNCOMMITTED".equals(value)) {
            return IsolationLevel.TRANSACTION_READ_UNCOMMITTED;
        } else if ("TRANSACTION_READ_COMMITTED".equals(value)) {
            return IsolationLevel.TRANSACTION_READ_COMMITTED;
        } else if ("TRANSACTION_REPEATABLE_READ".equals(value)) {
            return IsolationLevel.TRANSACTION_REPEATABLE_READ;
        } else if ("TRANSACTION_SERIALIZABLE".equals(value)) {
            return IsolationLevel.TRANSACTION_SERIALIZABLE;
        } else {
            context.unexpectedEnumValue(reader, IsolationLevel.class, value, "TRANSACTION_READ_UNCOMMITTED", "TRANSACTION_READ_COMMITTED", "TRANSACTION_REPEATABLE_READ", "TRANSACTION_SERIALIZABLE");
            return null;
        }
    }

    public static String toStringIsolationLevel(Object bean, String parameterName, RuntimeContext context, IsolationLevel isolationLevel)
            throws Exception {
        if (IsolationLevel.TRANSACTION_READ_UNCOMMITTED == isolationLevel) {
            return "TRANSACTION_READ_UNCOMMITTED";
        } else if (IsolationLevel.TRANSACTION_READ_COMMITTED == isolationLevel) {
            return "TRANSACTION_READ_COMMITTED";
        } else if (IsolationLevel.TRANSACTION_REPEATABLE_READ == isolationLevel) {
            return "TRANSACTION_REPEATABLE_READ";
        } else if (IsolationLevel.TRANSACTION_SERIALIZABLE == isolationLevel) {
            return "TRANSACTION_SERIALIZABLE";
        } else {
            context.unexpectedEnumConst(bean, parameterName, isolationLevel, IsolationLevel.TRANSACTION_READ_UNCOMMITTED, IsolationLevel.TRANSACTION_READ_COMMITTED, IsolationLevel.TRANSACTION_REPEATABLE_READ, IsolationLevel.TRANSACTION_SERIALIZABLE);
            return null;
        }
    }

}
