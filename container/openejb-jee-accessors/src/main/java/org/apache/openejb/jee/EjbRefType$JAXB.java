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

public class EjbRefType$JAXB
    extends JAXBEnum<EjbRefType> {


    public EjbRefType$JAXB() {
        super(EjbRefType.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "ejbRefType".intern()));
    }

    public EjbRefType parse(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        return parseEjbRefType(reader, context, value);
    }

    public String toString(final Object bean, final String parameterName, final RuntimeContext context, final EjbRefType ejbRefType)
        throws Exception {
        return toStringEjbRefType(bean, parameterName, context, ejbRefType);
    }

    public static EjbRefType parseEjbRefType(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        if ("Entity".equals(value)) {
            return EjbRefType.ENTITY;
        } else if ("Session".equals(value)) {
            return EjbRefType.SESSION;
        } else {
            context.unexpectedEnumValue(reader, EjbRefType.class, value, "Entity", "Session");
            return null;
        }
    }

    public static String toStringEjbRefType(final Object bean, final String parameterName, final RuntimeContext context, final EjbRefType ejbRefType)
        throws Exception {
        if (EjbRefType.ENTITY == ejbRefType) {
            return "Entity";
        } else if (EjbRefType.SESSION == ejbRefType) {
            return "Session";
        } else {
            context.unexpectedEnumConst(bean, parameterName, ejbRefType, EjbRefType.ENTITY, EjbRefType.SESSION);
            return null;
        }
    }

}
