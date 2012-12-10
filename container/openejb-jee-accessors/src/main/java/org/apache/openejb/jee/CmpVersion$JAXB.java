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

public class CmpVersion$JAXB
        extends JAXBEnum<CmpVersion> {


    public CmpVersion$JAXB() {
        super(CmpVersion.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "cmpVersion".intern()));
    }

    public CmpVersion parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parseCmpVersion(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, CmpVersion cmpVersion)
            throws Exception {
        return toStringCmpVersion(bean, parameterName, context, cmpVersion);
    }

    public static CmpVersion parseCmpVersion(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("1.x".equals(value)) {
            return CmpVersion.CMP1;
        } else if ("2.x".equals(value)) {
            return CmpVersion.CMP2;
        } else {
            context.unexpectedEnumValue(reader, CmpVersion.class, value, "1.x", "2.x");
            return null;
        }
    }

    public static String toStringCmpVersion(Object bean, String parameterName, RuntimeContext context, CmpVersion cmpVersion)
            throws Exception {
        if (CmpVersion.CMP1 == cmpVersion) {
            return "1.x";
        } else if (CmpVersion.CMP2 == cmpVersion) {
            return "2.x";
        } else {
            context.unexpectedEnumConst(bean, parameterName, cmpVersion, CmpVersion.CMP1, CmpVersion.CMP2);
            return null;
        }
    }

}
