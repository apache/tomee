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


import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class AuthConstraint$JAXB
    extends JAXBObject<AuthConstraint>
{


    public AuthConstraint$JAXB() {
        super(AuthConstraint.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "auth-constraintType".intern()), Text$JAXB.class);
    }

    public static AuthConstraint readAuthConstraint(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeAuthConstraint(XoXMLStreamWriter writer, AuthConstraint authConstraint, RuntimeContext context)
        throws Exception
    {
        _write(writer, authConstraint, context);
    }

    public void write(XoXMLStreamWriter writer, AuthConstraint authConstraint, RuntimeContext context)
        throws Exception
    {
        _write(writer, authConstraint, context);
    }

    public static final AuthConstraint _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        AuthConstraint authConstraint = new AuthConstraint();
        context.beforeUnmarshal(authConstraint, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<String> roleName = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("auth-constraintType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, AuthConstraint.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, authConstraint);
                authConstraint.id = id;
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
            } else if (("role-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: roleName
                String roleNameItemRaw = elementReader.getElementText();

                String roleNameItem;
                try {
                    roleNameItem = Adapters.collapsedStringAdapterAdapter.unmarshal(roleNameItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (roleName == null) {
                    roleName = authConstraint.roleName;
                    if (roleName!= null) {
                        roleName.clear();
                    } else {
                        roleName = new ArrayList<>();
                    }
                }
                roleName.add(roleNameItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "role-name"));
            }
        }
        if (descriptions!= null) {
            try {
                authConstraint.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, AuthConstraint.class, "setDescriptions", Text[].class, e);
            }
        }
        if (roleName!= null) {
            authConstraint.roleName = roleName;
        }

        context.afterUnmarshal(authConstraint, LifecycleCallback.NONE);

        return authConstraint;
    }

    public final AuthConstraint read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, AuthConstraint authConstraint, RuntimeContext context)
        throws Exception
    {
        if (authConstraint == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (AuthConstraint.class!= authConstraint.getClass()) {
            context.unexpectedSubclass(writer, authConstraint, AuthConstraint.class);
            return ;
        }

        context.beforeMarshal(authConstraint, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = authConstraint.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(authConstraint, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = authConstraint.getDescriptions();
        } catch (Exception e) {
            context.getterError(authConstraint, "descriptions", AuthConstraint.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(authConstraint, "descriptions");
                }
            }
        }

        // ELEMENT: roleName
        List<String> roleNameRaw = authConstraint.roleName;
        if (roleNameRaw!= null) {
            for (String roleNameItem: roleNameRaw) {
                String roleName = null;
                try {
                    roleName = Adapters.collapsedStringAdapterAdapter.marshal(roleNameItem);
                } catch (Exception e) {
                    context.xmlAdapterError(authConstraint, "roleName", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (roleName!= null) {
                    writer.writeStartElement(prefix, "role-name", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(roleName);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(authConstraint, LifecycleCallback.NONE);
    }

}
