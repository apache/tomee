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

public class BodyContent$JAXB
        extends JAXBEnum<BodyContent> {


    public BodyContent$JAXB() {
        super(BodyContent.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "bodyContent".intern()));
    }

    public BodyContent parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parseBodyContent(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, BodyContent bodyContent)
            throws Exception {
        return toStringBodyContent(bean, parameterName, context, bodyContent);
    }

    public static BodyContent parseBodyContent(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("tagdependent".equals(value)) {
            return BodyContent.TAGDEPENDENT;
        } else if ("JSP".equals(value)) {
            return BodyContent.JSP;
        } else if ("empty".equals(value)) {
            return BodyContent.EMPTY;
        } else if ("scriptless".equals(value)) {
            return BodyContent.SCRIPTLESS;
        } else {
            context.unexpectedEnumValue(reader, BodyContent.class, value, "tagdependent", "JSP", "empty", "scriptless");
            return null;
        }
    }

    public static String toStringBodyContent(Object bean, String parameterName, RuntimeContext context, BodyContent bodyContent)
            throws Exception {
        if (BodyContent.TAGDEPENDENT == bodyContent) {
            return "tagdependent";
        } else if (BodyContent.JSP == bodyContent) {
            return "JSP";
        } else if (BodyContent.EMPTY == bodyContent) {
            return "empty";
        } else if (BodyContent.SCRIPTLESS == bodyContent) {
            return "scriptless";
        } else {
            context.unexpectedEnumConst(bean, parameterName, bodyContent, BodyContent.TAGDEPENDENT, BodyContent.JSP, BodyContent.EMPTY, BodyContent.SCRIPTLESS);
            return null;
        }
    }

}
