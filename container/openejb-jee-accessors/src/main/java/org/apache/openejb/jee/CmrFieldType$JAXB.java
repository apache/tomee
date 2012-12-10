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

public class CmrFieldType$JAXB
        extends JAXBEnum<CmrFieldType> {


    public CmrFieldType$JAXB() {
        super(CmrFieldType.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "cmrFieldType".intern()));
    }

    public CmrFieldType parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parseCmrFieldType(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, CmrFieldType cmrFieldType)
            throws Exception {
        return toStringCmrFieldType(bean, parameterName, context, cmrFieldType);
    }

    public static CmrFieldType parseCmrFieldType(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("java.util.Collection".equals(value)) {
            return CmrFieldType.COLLECTION;
        } else if ("java.util.Set".equals(value)) {
            return CmrFieldType.SET;
        } else {
            context.unexpectedEnumValue(reader, CmrFieldType.class, value, "java.util.Collection", "java.util.Set");
            return null;
        }
    }

    public static String toStringCmrFieldType(Object bean, String parameterName, RuntimeContext context, CmrFieldType cmrFieldType)
            throws Exception {
        if (CmrFieldType.COLLECTION == cmrFieldType) {
            return "java.util.Collection";
        } else if (CmrFieldType.SET == cmrFieldType) {
            return "java.util.Set";
        } else {
            context.unexpectedEnumConst(bean, parameterName, cmrFieldType, CmrFieldType.COLLECTION, CmrFieldType.SET);
            return null;
        }
    }

}
