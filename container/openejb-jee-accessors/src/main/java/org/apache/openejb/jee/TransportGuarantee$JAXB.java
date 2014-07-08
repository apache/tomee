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

public class TransportGuarantee$JAXB
    extends JAXBEnum<TransportGuarantee> {


    public TransportGuarantee$JAXB() {
        super(TransportGuarantee.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "transport-guaranteeType".intern()));
    }

    public TransportGuarantee parse(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        return parseTransportGuarantee(reader, context, value);
    }

    public String toString(final Object bean, final String parameterName, final RuntimeContext context, final TransportGuarantee transportGuarantee)
        throws Exception {
        return toStringTransportGuarantee(bean, parameterName, context, transportGuarantee);
    }

    public static TransportGuarantee parseTransportGuarantee(final XoXMLStreamReader reader, final RuntimeContext context, final String value)
        throws Exception {
        if ("NONE".equals(value)) {
            return TransportGuarantee.NONE;
        } else if ("INTEGRAL".equals(value)) {
            return TransportGuarantee.INTEGRAL;
        } else if ("CONFIDENTIAL".equals(value)) {
            return TransportGuarantee.CONFIDENTIAL;
        } else {
            context.unexpectedEnumValue(reader, TransportGuarantee.class, value, "NONE", "INTEGRAL", "CONFIDENTIAL");
            return null;
        }
    }

    public static String toStringTransportGuarantee(final Object bean, final String parameterName, final RuntimeContext context, final TransportGuarantee transportGuarantee)
        throws Exception {
        if (TransportGuarantee.NONE == transportGuarantee) {
            return "NONE";
        } else if (TransportGuarantee.INTEGRAL == transportGuarantee) {
            return "INTEGRAL";
        } else if (TransportGuarantee.CONFIDENTIAL == transportGuarantee) {
            return "CONFIDENTIAL";
        } else {
            context.unexpectedEnumConst(bean, parameterName, transportGuarantee, TransportGuarantee.NONE, TransportGuarantee.INTEGRAL, TransportGuarantee.CONFIDENTIAL);
            return null;
        }
    }

}
