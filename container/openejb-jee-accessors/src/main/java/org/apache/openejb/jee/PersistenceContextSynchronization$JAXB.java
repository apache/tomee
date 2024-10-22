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

public class PersistenceContextSynchronization$JAXB
    extends JAXBEnum<PersistenceContextSynchronization>
{


    public PersistenceContextSynchronization$JAXB() {
        super(PersistenceContextSynchronization.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "persistenceContextSynchronization".intern()));
    }

    public PersistenceContextSynchronization parse(XoXMLStreamReader reader, RuntimeContext context, String value)
        throws Exception
    {
        return parsePersistenceContextSynchronization(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, PersistenceContextSynchronization persistenceContextSynchronization)
        throws Exception
    {
        return toStringPersistenceContextSynchronization(bean, parameterName, context, persistenceContextSynchronization);
    }

    public static PersistenceContextSynchronization parsePersistenceContextSynchronization(XoXMLStreamReader reader, RuntimeContext context, String value)
        throws Exception
    {
        if ("Synchronized".equals(value)) {
            return PersistenceContextSynchronization.SYNCHRONIZED;
        } else if ("Unsynchronized".equals(value)) {
            return PersistenceContextSynchronization.UNSYNCHRONIZED;
        } else {
            context.unexpectedEnumValue(reader, PersistenceContextSynchronization.class, value, "Synchronized", "Unsynchronized");
            return null;
        }
    }

    public static String toStringPersistenceContextSynchronization(Object bean, String parameterName, RuntimeContext context, PersistenceContextSynchronization persistenceContextSynchronization)
        throws Exception
    {
        if (PersistenceContextSynchronization.SYNCHRONIZED == persistenceContextSynchronization) {
            return "Synchronized";
        } else if (PersistenceContextSynchronization.UNSYNCHRONIZED == persistenceContextSynchronization) {
            return "Unsynchronized";
        } else {
            context.unexpectedEnumConst(bean, parameterName, persistenceContextSynchronization, PersistenceContextSynchronization.SYNCHRONIZED, PersistenceContextSynchronization.UNSYNCHRONIZED);
            return null;
        }
    }

}
