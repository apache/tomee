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

package org.apache.openejb.jee.jba;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

@SuppressWarnings({
    "StringEquality"
})
public class JndiName$JAXB
    extends JAXBObject<JndiName>
{


    public JndiName$JAXB() {
        super(JndiName.class, new QName("".intern(), "jndi-name".intern()), null);
    }

    public static JndiName readJndiName(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeJndiName(XoXMLStreamWriter writer, JndiName jndiName, RuntimeContext context)
        throws Exception
    {
        _write(writer, jndiName, context);
    }

    public void write(XoXMLStreamWriter writer, JndiName jndiName, RuntimeContext context)
        throws Exception
    {
        _write(writer, jndiName, context);
    }

    public static final JndiName _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        JndiName jndiName = new JndiName();
        context.beforeUnmarshal(jndiName, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            return context.unexpectedXsiType(reader, JndiName.class);
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // VALUE: value
        jndiName.value = reader.getElementText();

        context.afterUnmarshal(jndiName, LifecycleCallback.NONE);

        return jndiName;
    }

    public final JndiName read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, JndiName jndiName, RuntimeContext context)
        throws Exception
    {
        if (jndiName == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (JndiName.class!= jndiName.getClass()) {
            context.unexpectedSubclass(writer, jndiName, JndiName.class);
            return ;
        }

        context.beforeMarshal(jndiName, LifecycleCallback.NONE);


        // VALUE: value
        String value = jndiName.value;
        writer.writeCharacters(value);

        context.afterMarshal(jndiName, LifecycleCallback.NONE);
    }

}
