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

public class MethodIntf$JAXB
    extends JAXBEnum<MethodIntf> {


    public MethodIntf$JAXB() {
        super(MethodIntf.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "methodIntf".intern()));
    }

    public MethodIntf parse(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        return parseMethodIntf(reader, context, value);
    }

    public String toString(final Object bean, final String parameterName, final RuntimeContext context, final MethodIntf methodIntf)
        throws Exception {
        return toStringMethodIntf(bean, parameterName, context, methodIntf);
    }

    public static MethodIntf parseMethodIntf(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        if ("Home".equals(value)) {
            return MethodIntf.HOME;
        } else if ("Remote".equals(value)) {
            return MethodIntf.REMOTE;
        } else if ("LocalHome".equals(value)) {
            return MethodIntf.LOCALHOME;
        } else if ("Local".equals(value)) {
            return MethodIntf.LOCAL;
        } else if ("ServiceEndpoint".equals(value)) {
            return MethodIntf.SERVICEENDPOINT;
        } else if ("Timer".equals(value)) {
            return MethodIntf.TIMER;
        } else if ("MessageEndpoint".equals(value)) {
            return MethodIntf.MESSAGEENDPOINT;
        } else {
            context.unexpectedEnumValue(reader, MethodIntf.class, value, "Home", "Remote", "LocalHome", "Local", "ServiceEndpoint", "Timer", "MessageEndpoint");
            return null;
        }
    }

    public static String toStringMethodIntf(final Object bean, final String parameterName, final RuntimeContext context, final MethodIntf methodIntf)
        throws Exception {
        if (MethodIntf.HOME == methodIntf) {
            return "Home";
        } else if (MethodIntf.REMOTE == methodIntf) {
            return "Remote";
        } else if (MethodIntf.LOCALHOME == methodIntf) {
            return "LocalHome";
        } else if (MethodIntf.LOCAL == methodIntf) {
            return "Local";
        } else if (MethodIntf.SERVICEENDPOINT == methodIntf) {
            return "ServiceEndpoint";
        } else if (MethodIntf.TIMER == methodIntf) {
            return "Timer";
        } else if (MethodIntf.MESSAGEENDPOINT == methodIntf) {
            return "MessageEndpoint";
        } else {
            context.unexpectedEnumConst(bean, parameterName, methodIntf, MethodIntf.HOME, MethodIntf.REMOTE, MethodIntf.LOCALHOME, MethodIntf.LOCAL, MethodIntf.SERVICEENDPOINT, MethodIntf.TIMER, MethodIntf.MESSAGEENDPOINT);
            return null;
        }
    }

}
