/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import javax.xml.namespace.QName;
import org.metatype.sxc.jaxb.JAXBEnum;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.XoXMLStreamReader;

public class ConcurrentLockType$JAXB
    extends JAXBEnum<ConcurrentLockType>
{


    public ConcurrentLockType$JAXB() {
        super(ConcurrentLockType.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "concurrent-lock-typeType".intern()));
    }

    public ConcurrentLockType parse(XoXMLStreamReader reader, RuntimeContext context, String value)
        throws Exception
    {
        return parseConcurrentLockType(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, ConcurrentLockType concurrentLockType)
        throws Exception
    {
        return toStringConcurrentLockType(bean, parameterName, context, concurrentLockType);
    }

    public static ConcurrentLockType parseConcurrentLockType(XoXMLStreamReader reader, RuntimeContext context, String value)
        throws Exception
    {
        if ("Read".equals(value)) {
            return ConcurrentLockType.READ;
        } else if ("Write".equals(value)) {
            return ConcurrentLockType.WRITE;
        } else {
            context.unexpectedEnumValue(reader, ConcurrentLockType.class, value, "Read", "Write");
            return null;
        }
    }

    public static String toStringConcurrentLockType(Object bean, String parameterName, RuntimeContext context, ConcurrentLockType concurrentLockType)
        throws Exception
    {
        if (ConcurrentLockType.READ == concurrentLockType) {
            return "Read";
        } else if (ConcurrentLockType.WRITE == concurrentLockType) {
            return "Write";
        } else {
            context.unexpectedEnumConst(bean, parameterName, concurrentLockType, ConcurrentLockType.READ, ConcurrentLockType.WRITE);
            return null;
        }
    }

}
