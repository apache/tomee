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

public class TransactionType$JAXB
        extends JAXBEnum<TransactionType> {


    public TransactionType$JAXB() {
        super(TransactionType.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "transactionType".intern()));
    }

    public TransactionType parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parseTransactionType(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, TransactionType transactionType)
            throws Exception {
        return toStringTransactionType(bean, parameterName, context, transactionType);
    }

    public static TransactionType parseTransactionType(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("Bean".equals(value)) {
            return TransactionType.BEAN;
        } else if ("Container".equals(value)) {
            return TransactionType.CONTAINER;
        } else {
            context.unexpectedEnumValue(reader, TransactionType.class, value, "Bean", "Container");
            return null;
        }
    }

    public static String toStringTransactionType(Object bean, String parameterName, RuntimeContext context, TransactionType transactionType)
            throws Exception {
        if (TransactionType.BEAN == transactionType) {
            return "Bean";
        } else if (TransactionType.CONTAINER == transactionType) {
            return "Container";
        } else {
            context.unexpectedEnumConst(bean, parameterName, transactionType, TransactionType.BEAN, TransactionType.CONTAINER);
            return null;
        }
    }

}
