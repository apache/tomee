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

public class AddressingResponses$JAXB
    extends JAXBEnum<AddressingResponses> {


    public AddressingResponses$JAXB() {
        super(AddressingResponses.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "addressing-responsesType".intern()));
    }

    public AddressingResponses parse(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        return parseAddressingResponses(reader, context, value);
    }

    public String toString(final Object bean, final String parameterName, final RuntimeContext context, final AddressingResponses addressingResponses)
        throws Exception {
        return toStringAddressingResponses(bean, parameterName, context, addressingResponses);
    }

    public static AddressingResponses parseAddressingResponses(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        if ("ANONYMOUS".equals(value)) {
            return AddressingResponses.ANONYMOUS;
        } else if ("NON_ANONYMOUS".equals(value)) {
            return AddressingResponses.NON_ANONYMOUS;
        } else if ("ALL".equals(value)) {
            return AddressingResponses.ALL;
        } else {
            context.unexpectedEnumValue(reader, AddressingResponses.class, value, "ANONYMOUS", "NON_ANONYMOUS", "ALL");
            return null;
        }
    }

    public static String toStringAddressingResponses(final Object bean, final String parameterName, final RuntimeContext context, final AddressingResponses addressingResponses)
        throws Exception {
        if (AddressingResponses.ANONYMOUS == addressingResponses) {
            return "ANONYMOUS";
        } else if (AddressingResponses.NON_ANONYMOUS == addressingResponses) {
            return "NON_ANONYMOUS";
        } else if (AddressingResponses.ALL == addressingResponses) {
            return "ALL";
        } else {
            context.unexpectedEnumConst(bean, parameterName, addressingResponses, AddressingResponses.ANONYMOUS, AddressingResponses.NON_ANONYMOUS, AddressingResponses.ALL);
            return null;
        }
    }

}
