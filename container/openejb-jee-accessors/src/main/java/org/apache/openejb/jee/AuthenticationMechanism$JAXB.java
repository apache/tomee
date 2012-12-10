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

import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.LifecycleCallback;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.Attribute;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;

import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class AuthenticationMechanism$JAXB
        extends JAXBObject<AuthenticationMechanism> {


    public AuthenticationMechanism$JAXB() {
        super(AuthenticationMechanism.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "authentication-mechanismType".intern()), Text$JAXB.class);
    }

    public static AuthenticationMechanism readAuthenticationMechanism(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeAuthenticationMechanism(XoXMLStreamWriter writer, AuthenticationMechanism authenticationMechanism, RuntimeContext context)
            throws Exception {
        _write(writer, authenticationMechanism, context);
    }

    public void write(XoXMLStreamWriter writer, AuthenticationMechanism authenticationMechanism, RuntimeContext context)
            throws Exception {
        _write(writer, authenticationMechanism, context);
    }

    public final static AuthenticationMechanism _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        AuthenticationMechanism authenticationMechanism = new AuthenticationMechanism();
        context.beforeUnmarshal(authenticationMechanism, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("authentication-mechanismType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, AuthenticationMechanism.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, authenticationMechanism);
                authenticationMechanism.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("authentication-mechanism-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: authenticationMechanismType
                String authenticationMechanismTypeRaw = elementReader.getElementAsString();

                String authenticationMechanismType;
                try {
                    authenticationMechanismType = Adapters.collapsedStringAdapterAdapter.unmarshal(authenticationMechanismTypeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                authenticationMechanism.authenticationMechanismType = authenticationMechanismType;
            } else if (("credential-interface" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: credentialInterface
                String credentialInterfaceRaw = elementReader.getElementAsString();

                String credentialInterface;
                try {
                    credentialInterface = Adapters.collapsedStringAdapterAdapter.unmarshal(credentialInterfaceRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                authenticationMechanism.credentialInterface = credentialInterface;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "authentication-mechanism-type"), new QName("http://java.sun.com/xml/ns/javaee", "credential-interface"));
            }
        }
        if (descriptions != null) {
            try {
                authenticationMechanism.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, AuthenticationMechanism.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(authenticationMechanism, LifecycleCallback.NONE);

        return authenticationMechanism;
    }

    public final AuthenticationMechanism read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, AuthenticationMechanism authenticationMechanism, RuntimeContext context)
            throws Exception {
        if (authenticationMechanism == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (AuthenticationMechanism.class != authenticationMechanism.getClass()) {
            context.unexpectedSubclass(writer, authenticationMechanism, AuthenticationMechanism.class);
            return;
        }

        context.beforeMarshal(authenticationMechanism, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = authenticationMechanism.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(authenticationMechanism, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = authenticationMechanism.getDescriptions();
        } catch (Exception e) {
            context.getterError(authenticationMechanism, "descriptions", AuthenticationMechanism.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(authenticationMechanism, "descriptions");
                }
            }
        }

        // ELEMENT: authenticationMechanismType
        String authenticationMechanismTypeRaw = authenticationMechanism.authenticationMechanismType;
        String authenticationMechanismType = null;
        try {
            authenticationMechanismType = Adapters.collapsedStringAdapterAdapter.marshal(authenticationMechanismTypeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(authenticationMechanism, "authenticationMechanismType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (authenticationMechanismType != null) {
            writer.writeStartElement(prefix, "authentication-mechanism-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(authenticationMechanismType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(authenticationMechanism, "authenticationMechanismType");
        }

        // ELEMENT: credentialInterface
        String credentialInterfaceRaw = authenticationMechanism.credentialInterface;
        String credentialInterface = null;
        try {
            credentialInterface = Adapters.collapsedStringAdapterAdapter.marshal(credentialInterfaceRaw);
        } catch (Exception e) {
            context.xmlAdapterError(authenticationMechanism, "credentialInterface", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (credentialInterface != null) {
            writer.writeStartElement(prefix, "credential-interface", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(credentialInterface);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(authenticationMechanism, "credentialInterface");
        }

        context.afterMarshal(authenticationMechanism, LifecycleCallback.NONE);
    }

}
