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

public class ConcurrencyManagementType$JAXB
    extends JAXBEnum<ConcurrencyManagementType>
{


    public ConcurrencyManagementType$JAXB() {
        super(ConcurrencyManagementType.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "concurrency-management-typeType".intern()));
    }

    public ConcurrencyManagementType parse(XoXMLStreamReader reader, RuntimeContext context, String value)
        throws Exception
    {
        return parseConcurrencyManagementType(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, ConcurrencyManagementType concurrencyManagementType)
        throws Exception
    {
        return toStringConcurrencyManagementType(bean, parameterName, context, concurrencyManagementType);
    }

    public static ConcurrencyManagementType parseConcurrencyManagementType(XoXMLStreamReader reader, RuntimeContext context, String value)
        throws Exception
    {
        if ("Bean".equals(value)) {
            return ConcurrencyManagementType.BEAN;
        } else if ("Container".equals(value)) {
            return ConcurrencyManagementType.CONTAINER;
        } else {
            context.unexpectedEnumValue(reader, ConcurrencyManagementType.class, value, "Bean", "Container");
            return null;
        }
    }

    public static String toStringConcurrencyManagementType(Object bean, String parameterName, RuntimeContext context, ConcurrencyManagementType concurrencyManagementType)
        throws Exception
    {
        if (ConcurrencyManagementType.BEAN == concurrencyManagementType) {
            return "Bean";
        } else if (ConcurrencyManagementType.CONTAINER == concurrencyManagementType) {
            return "Container";
        } else {
            context.unexpectedEnumConst(bean, parameterName, concurrencyManagementType, ConcurrencyManagementType.BEAN, ConcurrencyManagementType.CONTAINER);
            return null;
        }
    }

}
