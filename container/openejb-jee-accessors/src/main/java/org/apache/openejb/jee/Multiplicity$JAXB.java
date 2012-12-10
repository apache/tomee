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

public class Multiplicity$JAXB
        extends JAXBEnum<Multiplicity> {


    public Multiplicity$JAXB() {
        super(Multiplicity.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "multiplicity".intern()));
    }

    public Multiplicity parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parseMultiplicity(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, Multiplicity multiplicity)
            throws Exception {
        return toStringMultiplicity(bean, parameterName, context, multiplicity);
    }

    public static Multiplicity parseMultiplicity(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("One".equals(value)) {
            return Multiplicity.ONE;
        } else if ("Many".equals(value)) {
            return Multiplicity.MANY;
        } else {
            context.unexpectedEnumValue(reader, Multiplicity.class, value, "One", "Many");
            return null;
        }
    }

    public static String toStringMultiplicity(Object bean, String parameterName, RuntimeContext context, Multiplicity multiplicity)
            throws Exception {
        if (Multiplicity.ONE == multiplicity) {
            return "One";
        } else if (Multiplicity.MANY == multiplicity) {
            return "Many";
        } else {
            context.unexpectedEnumConst(bean, parameterName, multiplicity, Multiplicity.ONE, Multiplicity.MANY);
            return null;
        }
    }

}
