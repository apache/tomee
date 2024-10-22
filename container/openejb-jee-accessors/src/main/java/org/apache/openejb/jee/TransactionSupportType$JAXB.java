/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import javax.xml.namespace.QName;
import org.metatype.sxc.jaxb.JAXBEnum;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.XoXMLStreamReader;

public class TransactionSupportType$JAXB
    extends JAXBEnum<TransactionSupportType>
{


    public TransactionSupportType$JAXB() {
        super(TransactionSupportType.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "transactionSupportType".intern()));
    }

    public TransactionSupportType parse(XoXMLStreamReader reader, RuntimeContext context, String value)
        throws Exception
    {
        return parseTransactionSupportType(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, TransactionSupportType transactionSupportType)
        throws Exception
    {
        return toStringTransactionSupportType(bean, parameterName, context, transactionSupportType);
    }

    public static TransactionSupportType parseTransactionSupportType(XoXMLStreamReader reader, RuntimeContext context, String value)
        throws Exception
    {
        if ("NoTransaction".equals(value)) {
            return TransactionSupportType.NO_TRANSACTION;
        } else if ("LocalTransaction".equals(value)) {
            return TransactionSupportType.LOCAL_TRANSACTION;
        } else if ("XATransaction".equals(value)) {
            return TransactionSupportType.XA_TRANSACTION;
        } else {
            context.unexpectedEnumValue(reader, TransactionSupportType.class, value, "NoTransaction", "LocalTransaction", "XATransaction");
            return null;
        }
    }

    public static String toStringTransactionSupportType(Object bean, String parameterName, RuntimeContext context, TransactionSupportType transactionSupportType)
        throws Exception
    {
        if (TransactionSupportType.NO_TRANSACTION == transactionSupportType) {
            return "NoTransaction";
        } else if (TransactionSupportType.LOCAL_TRANSACTION == transactionSupportType) {
            return "LocalTransaction";
        } else if (TransactionSupportType.XA_TRANSACTION == transactionSupportType) {
            return "XATransaction";
        } else {
            context.unexpectedEnumConst(bean, parameterName, transactionSupportType, TransactionSupportType.NO_TRANSACTION, TransactionSupportType.LOCAL_TRANSACTION, TransactionSupportType.XA_TRANSACTION);
            return null;
        }
    }

}
