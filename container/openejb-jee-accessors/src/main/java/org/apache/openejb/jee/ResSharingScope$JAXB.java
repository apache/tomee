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

public class ResSharingScope$JAXB
        extends JAXBEnum<ResSharingScope> {


    public ResSharingScope$JAXB() {
        super(ResSharingScope.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "resSharingScope".intern()));
    }

    public ResSharingScope parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parseResSharingScope(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, ResSharingScope resSharingScope)
            throws Exception {
        return toStringResSharingScope(bean, parameterName, context, resSharingScope);
    }

    public static ResSharingScope parseResSharingScope(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("Shareable".equals(value)) {
            return ResSharingScope.SHAREABLE;
        } else if ("Unshareable".equals(value)) {
            return ResSharingScope.UNSHAREABLE;
        } else {
            context.unexpectedEnumValue(reader, ResSharingScope.class, value, "Shareable", "Unshareable");
            return null;
        }
    }

    public static String toStringResSharingScope(Object bean, String parameterName, RuntimeContext context, ResSharingScope resSharingScope)
            throws Exception {
        if (ResSharingScope.SHAREABLE == resSharingScope) {
            return "Shareable";
        } else if (ResSharingScope.UNSHAREABLE == resSharingScope) {
            return "Unshareable";
        } else {
            context.unexpectedEnumConst(bean, parameterName, resSharingScope, ResSharingScope.SHAREABLE, ResSharingScope.UNSHAREABLE);
            return null;
        }
    }

}
