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

import java.util.ArrayList;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.Empty$JAXB.readEmpty;
import static org.apache.openejb.jee.Empty$JAXB.writeEmpty;
import static org.apache.openejb.jee.RunAs$JAXB.readRunAs;
import static org.apache.openejb.jee.RunAs$JAXB.writeRunAs;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class SecurityIdentity$JAXB
    extends JAXBObject<SecurityIdentity>
{


    public SecurityIdentity$JAXB() {
        super(SecurityIdentity.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "security-identityType".intern()), Text$JAXB.class, Empty$JAXB.class, RunAs$JAXB.class);
    }

    public static SecurityIdentity readSecurityIdentity(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeSecurityIdentity(XoXMLStreamWriter writer, SecurityIdentity securityIdentity, RuntimeContext context)
        throws Exception
    {
        _write(writer, securityIdentity, context);
    }

    public void write(XoXMLStreamWriter writer, SecurityIdentity securityIdentity, RuntimeContext context)
        throws Exception
    {
        _write(writer, securityIdentity, context);
    }

    public static final SecurityIdentity _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        SecurityIdentity securityIdentity = new SecurityIdentity();
        context.beforeUnmarshal(securityIdentity, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("security-identityType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, SecurityIdentity.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, securityIdentity);
                securityIdentity.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<>();
                }
                descriptions.add(descriptionsItem);
            } else if (("use-caller-identity" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: useCallerIdentity
                Empty useCallerIdentity = readEmpty(elementReader, context);
                securityIdentity.useCallerIdentity = useCallerIdentity;
            } else if (("run-as" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: runAs
                RunAs runAs = readRunAs(elementReader, context);
                securityIdentity.runAs = runAs;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "use-caller-identity"), new QName("http://java.sun.com/xml/ns/javaee", "run-as"));
            }
        }
        if (descriptions!= null) {
            try {
                securityIdentity.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, SecurityIdentity.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(securityIdentity, LifecycleCallback.NONE);

        return securityIdentity;
    }

    public final SecurityIdentity read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, SecurityIdentity securityIdentity, RuntimeContext context)
        throws Exception
    {
        if (securityIdentity == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (SecurityIdentity.class!= securityIdentity.getClass()) {
            context.unexpectedSubclass(writer, securityIdentity, SecurityIdentity.class);
            return ;
        }

        context.beforeMarshal(securityIdentity, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = securityIdentity.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(securityIdentity, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = securityIdentity.getDescriptions();
        } catch (Exception e) {
            context.getterError(securityIdentity, "descriptions", SecurityIdentity.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(securityIdentity, "descriptions");
                }
            }
        }

        // ELEMENT: useCallerIdentity
        Empty useCallerIdentity = securityIdentity.useCallerIdentity;
        if (useCallerIdentity!= null) {
            writer.writeStartElement(prefix, "use-caller-identity", "http://java.sun.com/xml/ns/javaee");
            writeEmpty(writer, useCallerIdentity, context);
            writer.writeEndElement();
        }

        // ELEMENT: runAs
        RunAs runAs = securityIdentity.runAs;
        if (runAs!= null) {
            writer.writeStartElement(prefix, "run-as", "http://java.sun.com/xml/ns/javaee");
            writeRunAs(writer, runAs, context);
            writer.writeEndElement();
        }

        context.afterMarshal(securityIdentity, LifecycleCallback.NONE);
    }

}
