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

public class MessageDestinationUsage$JAXB
    extends JAXBEnum<MessageDestinationUsage> {


    public MessageDestinationUsage$JAXB() {
        super(MessageDestinationUsage.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "messageDestinationUsage".intern()));
    }

    public MessageDestinationUsage parse(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        return parseMessageDestinationUsage(reader, context, value);
    }

    public String toString(final Object bean, final String parameterName, final RuntimeContext context, final MessageDestinationUsage messageDestinationUsage)
        throws Exception {
        return toStringMessageDestinationUsage(bean, parameterName, context, messageDestinationUsage);
    }

    public static MessageDestinationUsage parseMessageDestinationUsage(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        if ("Consumes".equals(value)) {
            return MessageDestinationUsage.CONSUMES;
        } else if ("Produces".equals(value)) {
            return MessageDestinationUsage.PRODUCES;
        } else if ("ConsumesProduces".equals(value)) {
            return MessageDestinationUsage.CONSUMES_PRODUCES;
        } else {
            context.unexpectedEnumValue(reader, MessageDestinationUsage.class, value, "Consumes", "Produces", "ConsumesProduces");
            return null;
        }
    }

    public static String toStringMessageDestinationUsage(final Object bean, final String parameterName, final RuntimeContext context, final MessageDestinationUsage messageDestinationUsage)
        throws Exception {
        if (MessageDestinationUsage.CONSUMES == messageDestinationUsage) {
            return "Consumes";
        } else if (MessageDestinationUsage.PRODUCES == messageDestinationUsage) {
            return "Produces";
        } else if (MessageDestinationUsage.CONSUMES_PRODUCES == messageDestinationUsage) {
            return "ConsumesProduces";
        } else {
            context.unexpectedEnumConst(bean, parameterName, messageDestinationUsage, MessageDestinationUsage.CONSUMES, MessageDestinationUsage.PRODUCES, MessageDestinationUsage.CONSUMES_PRODUCES);
            return null;
        }
    }

}
