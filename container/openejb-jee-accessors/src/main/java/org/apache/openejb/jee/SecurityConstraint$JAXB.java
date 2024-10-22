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
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.AuthConstraint$JAXB.readAuthConstraint;
import static org.apache.openejb.jee.AuthConstraint$JAXB.writeAuthConstraint;
import static org.apache.openejb.jee.UserDataConstraint$JAXB.readUserDataConstraint;
import static org.apache.openejb.jee.UserDataConstraint$JAXB.writeUserDataConstraint;
import static org.apache.openejb.jee.WebResourceCollection$JAXB.readWebResourceCollection;
import static org.apache.openejb.jee.WebResourceCollection$JAXB.writeWebResourceCollection;

@SuppressWarnings({
    "StringEquality"
})
public class SecurityConstraint$JAXB
    extends JAXBObject<SecurityConstraint>
{


    public SecurityConstraint$JAXB() {
        super(SecurityConstraint.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "security-constraintType".intern()), WebResourceCollection$JAXB.class, AuthConstraint$JAXB.class, UserDataConstraint$JAXB.class);
    }

    public static SecurityConstraint readSecurityConstraint(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeSecurityConstraint(XoXMLStreamWriter writer, SecurityConstraint securityConstraint, RuntimeContext context)
        throws Exception
    {
        _write(writer, securityConstraint, context);
    }

    public void write(XoXMLStreamWriter writer, SecurityConstraint securityConstraint, RuntimeContext context)
        throws Exception
    {
        _write(writer, securityConstraint, context);
    }

    public static final SecurityConstraint _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        SecurityConstraint securityConstraint = new SecurityConstraint();
        context.beforeUnmarshal(securityConstraint, LifecycleCallback.NONE);

        List<String> displayName = null;
        List<WebResourceCollection> webResourceCollection = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("security-constraintType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, SecurityConstraint.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, securityConstraint);
                securityConstraint.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("display-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayName
                String displayNameItemRaw = elementReader.getElementText();

                String displayNameItem;
                try {
                    displayNameItem = Adapters.collapsedStringAdapterAdapter.unmarshal(displayNameItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (displayName == null) {
                    displayName = securityConstraint.displayName;
                    if (displayName!= null) {
                        displayName.clear();
                    } else {
                        displayName = new ArrayList<>();
                    }
                }
                displayName.add(displayNameItem);
            } else if (("web-resource-collection" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: webResourceCollection
                WebResourceCollection webResourceCollectionItem = readWebResourceCollection(elementReader, context);
                if (webResourceCollection == null) {
                    webResourceCollection = securityConstraint.webResourceCollection;
                    if (webResourceCollection!= null) {
                        webResourceCollection.clear();
                    } else {
                        webResourceCollection = new ArrayList<>();
                    }
                }
                webResourceCollection.add(webResourceCollectionItem);
            } else if (("auth-constraint" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: authConstraint
                AuthConstraint authConstraint = readAuthConstraint(elementReader, context);
                securityConstraint.authConstraint = authConstraint;
            } else if (("user-data-constraint" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: userDataConstraint
                UserDataConstraint userDataConstraint = readUserDataConstraint(elementReader, context);
                securityConstraint.userDataConstraint = userDataConstraint;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "web-resource-collection"), new QName("http://java.sun.com/xml/ns/javaee", "auth-constraint"), new QName("http://java.sun.com/xml/ns/javaee", "user-data-constraint"));
            }
        }
        if (displayName!= null) {
            securityConstraint.displayName = displayName;
        }
        if (webResourceCollection!= null) {
            securityConstraint.webResourceCollection = webResourceCollection;
        }

        context.afterUnmarshal(securityConstraint, LifecycleCallback.NONE);

        return securityConstraint;
    }

    public final SecurityConstraint read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, SecurityConstraint securityConstraint, RuntimeContext context)
        throws Exception
    {
        if (securityConstraint == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (SecurityConstraint.class!= securityConstraint.getClass()) {
            context.unexpectedSubclass(writer, securityConstraint, SecurityConstraint.class);
            return ;
        }

        context.beforeMarshal(securityConstraint, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = securityConstraint.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(securityConstraint, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: displayName
        List<String> displayNameRaw = securityConstraint.displayName;
        if (displayNameRaw!= null) {
            for (String displayNameItem: displayNameRaw) {
                String displayName = null;
                try {
                    displayName = Adapters.collapsedStringAdapterAdapter.marshal(displayNameItem);
                } catch (Exception e) {
                    context.xmlAdapterError(securityConstraint, "displayName", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (displayName!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(displayName);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: webResourceCollection
        List<WebResourceCollection> webResourceCollection = securityConstraint.webResourceCollection;
        if (webResourceCollection!= null) {
            for (WebResourceCollection webResourceCollectionItem: webResourceCollection) {
                if (webResourceCollectionItem!= null) {
                    writer.writeStartElement(prefix, "web-resource-collection", "http://java.sun.com/xml/ns/javaee");
                    writeWebResourceCollection(writer, webResourceCollectionItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(securityConstraint, "webResourceCollection");
                }
            }
        }

        // ELEMENT: authConstraint
        AuthConstraint authConstraint = securityConstraint.authConstraint;
        if (authConstraint!= null) {
            writer.writeStartElement(prefix, "auth-constraint", "http://java.sun.com/xml/ns/javaee");
            writeAuthConstraint(writer, authConstraint, context);
            writer.writeEndElement();
        }

        // ELEMENT: userDataConstraint
        UserDataConstraint userDataConstraint = securityConstraint.userDataConstraint;
        if (userDataConstraint!= null) {
            writer.writeStartElement(prefix, "user-data-constraint", "http://java.sun.com/xml/ns/javaee");
            writeUserDataConstraint(writer, userDataConstraint, context);
            writer.writeEndElement();
        }

        context.afterMarshal(securityConstraint, LifecycleCallback.NONE);
    }

}
