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

public class TransAttribute$JAXB
    extends JAXBEnum<TransAttribute> {


    public TransAttribute$JAXB() {
        super(TransAttribute.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "transAttribute".intern()));
    }

    public TransAttribute parse(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        return parseTransAttribute(reader, context, value);
    }

    public String toString(final Object bean, final String parameterName, final RuntimeContext context, final TransAttribute transAttribute)
        throws Exception {
        return toStringTransAttribute(bean, parameterName, context, transAttribute);
    }

    public static TransAttribute parseTransAttribute(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        if ("NotSupported".equals(value)) {
            return TransAttribute.NOT_SUPPORTED;
        } else if ("Supports".equals(value)) {
            return TransAttribute.SUPPORTS;
        } else if ("Required".equals(value)) {
            return TransAttribute.REQUIRED;
        } else if ("RequiresNew".equals(value)) {
            return TransAttribute.REQUIRES_NEW;
        } else if ("Mandatory".equals(value)) {
            return TransAttribute.MANDATORY;
        } else if ("Never".equals(value)) {
            return TransAttribute.NEVER;
        } else {
            context.unexpectedEnumValue(reader, TransAttribute.class, value, "NotSupported", "Supports", "Required", "RequiresNew", "Mandatory", "Never");
            return null;
        }
    }

    public static String toStringTransAttribute(final Object bean, final String parameterName, final RuntimeContext context, final TransAttribute transAttribute)
        throws Exception {
        if (TransAttribute.NOT_SUPPORTED == transAttribute) {
            return "NotSupported";
        } else if (TransAttribute.SUPPORTS == transAttribute) {
            return "Supports";
        } else if (TransAttribute.REQUIRED == transAttribute) {
            return "Required";
        } else if (TransAttribute.REQUIRES_NEW == transAttribute) {
            return "RequiresNew";
        } else if (TransAttribute.MANDATORY == transAttribute) {
            return "Mandatory";
        } else if (TransAttribute.NEVER == transAttribute) {
            return "Never";
        } else {
            context.unexpectedEnumConst(bean, parameterName, transAttribute, TransAttribute.NOT_SUPPORTED, TransAttribute.SUPPORTS, TransAttribute.REQUIRED, TransAttribute.REQUIRES_NEW, TransAttribute.MANDATORY, TransAttribute.NEVER);
            return null;
        }
    }

}
