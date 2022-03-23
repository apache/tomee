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

import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;

import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class SecurityPermission$JAXB
    extends JAXBObject<SecurityPermission> {


    public SecurityPermission$JAXB() {
        super(SecurityPermission.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "security-permissionType".intern()), Text$JAXB.class);
    }

    public static SecurityPermission readSecurityPermission(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeSecurityPermission(final XoXMLStreamWriter writer, final SecurityPermission securityPermission, final RuntimeContext context)
        throws Exception {
        _write(writer, securityPermission, context);
    }

    public void write(final XoXMLStreamWriter writer, final SecurityPermission securityPermission, final RuntimeContext context)
        throws Exception {
        _write(writer, securityPermission, context);
    }

    public final static SecurityPermission _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final SecurityPermission securityPermission = new SecurityPermission();
        context.beforeUnmarshal(securityPermission, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("security-permissionType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, SecurityPermission.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, securityPermission);
                securityPermission.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                final Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("security-permission-spec" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityPermissionSpec
                final String securityPermissionSpecRaw = elementReader.getElementAsString();

                final String securityPermissionSpec;
                try {
                    securityPermissionSpec = Adapters.collapsedStringAdapterAdapter.unmarshal(securityPermissionSpecRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                securityPermission.securityPermissionSpec = securityPermissionSpec;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "security-permission-spec"));
            }
        }
        if (descriptions != null) {
            try {
                securityPermission.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, SecurityPermission.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(securityPermission, LifecycleCallback.NONE);

        return securityPermission;
    }

    public final SecurityPermission read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final SecurityPermission securityPermission, RuntimeContext context)
        throws Exception {
        if (securityPermission == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (SecurityPermission.class != securityPermission.getClass()) {
            context.unexpectedSubclass(writer, securityPermission, SecurityPermission.class);
            return;
        }

        context.beforeMarshal(securityPermission, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = securityPermission.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(securityPermission, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = securityPermission.getDescriptions();
        } catch (final Exception e) {
            context.getterError(securityPermission, "descriptions", SecurityPermission.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(securityPermission, "descriptions");
                }
            }
        }

        // ELEMENT: securityPermissionSpec
        final String securityPermissionSpecRaw = securityPermission.securityPermissionSpec;
        String securityPermissionSpec = null;
        try {
            securityPermissionSpec = Adapters.collapsedStringAdapterAdapter.marshal(securityPermissionSpecRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(securityPermission, "securityPermissionSpec", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (securityPermissionSpec != null) {
            writer.writeStartElement(prefix, "security-permission-spec", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(securityPermissionSpec);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(securityPermission, "securityPermissionSpec");
        }

        context.afterMarshal(securityPermission, LifecycleCallback.NONE);
    }

}
