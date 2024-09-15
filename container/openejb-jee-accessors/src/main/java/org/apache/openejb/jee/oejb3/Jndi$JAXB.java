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

package org.apache.openejb.jee.oejb3;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

@SuppressWarnings({
    "StringEquality"
})
public class Jndi$JAXB
    extends JAXBObject<Jndi>
{


    public Jndi$JAXB() {
        super(Jndi.class, new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "jndi".intern()), null);
    }

    public static Jndi readJndi(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeJndi(XoXMLStreamWriter writer, Jndi jndi, RuntimeContext context)
        throws Exception
    {
        _write(writer, jndi, context);
    }

    public void write(XoXMLStreamWriter writer, Jndi jndi, RuntimeContext context)
        throws Exception
    {
        _write(writer, jndi, context);
    }

    public static final Jndi _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Jndi jndi = new Jndi();
        context.beforeUnmarshal(jndi, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            return context.unexpectedXsiType(reader, Jndi.class);
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("name" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: name
                jndi.name = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (("interface" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: intrface
                jndi.intrface = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "name"), new QName("", "interface"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            context.unexpectedElement(elementReader);
        }

        context.afterUnmarshal(jndi, LifecycleCallback.NONE);

        return jndi;
    }

    public final Jndi read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Jndi jndi, RuntimeContext context)
        throws Exception
    {
        if (jndi == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (Jndi.class!= jndi.getClass()) {
            context.unexpectedSubclass(writer, jndi, Jndi.class);
            return ;
        }

        context.beforeMarshal(jndi, LifecycleCallback.NONE);


        // ATTRIBUTE: name
        String nameRaw = jndi.name;
        if (nameRaw!= null) {
            String name = null;
            try {
                name = Adapters.collapsedStringAdapterAdapter.marshal(nameRaw);
            } catch (Exception e) {
                context.xmlAdapterError(jndi, "name", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "name", name);
        }

        // ATTRIBUTE: intrface
        String intrfaceRaw = jndi.intrface;
        if (intrfaceRaw!= null) {
            String intrface = null;
            try {
                intrface = Adapters.collapsedStringAdapterAdapter.marshal(intrfaceRaw);
            } catch (Exception e) {
                context.xmlAdapterError(jndi, "intrface", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "interface", intrface);
        }

        context.afterMarshal(jndi, LifecycleCallback.NONE);
    }

}
