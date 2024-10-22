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


import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;
import static org.apache.openejb.jee.TransportGuarantee$JAXB.parseTransportGuarantee;
import static org.apache.openejb.jee.TransportGuarantee$JAXB.toStringTransportGuarantee;

@SuppressWarnings({
    "StringEquality"
})
public class UserDataConstraint$JAXB
    extends JAXBObject<UserDataConstraint>
{


    public UserDataConstraint$JAXB() {
        super(UserDataConstraint.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "user-data-constraintType".intern()), Text$JAXB.class, TransportGuarantee$JAXB.class);
    }

    public static UserDataConstraint readUserDataConstraint(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeUserDataConstraint(XoXMLStreamWriter writer, UserDataConstraint userDataConstraint, RuntimeContext context)
        throws Exception
    {
        _write(writer, userDataConstraint, context);
    }

    public void write(XoXMLStreamWriter writer, UserDataConstraint userDataConstraint, RuntimeContext context)
        throws Exception
    {
        _write(writer, userDataConstraint, context);
    }

    public static final UserDataConstraint _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        UserDataConstraint userDataConstraint = new UserDataConstraint();
        context.beforeUnmarshal(userDataConstraint, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("user-data-constraintType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, UserDataConstraint.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, userDataConstraint);
                userDataConstraint.id = id;
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
            } else if (("transport-guarantee" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: transportGuarantee
                TransportGuarantee transportGuarantee = parseTransportGuarantee(elementReader, context, elementReader.getElementText());
                if (transportGuarantee!= null) {
                    userDataConstraint.transportGuarantee = transportGuarantee;
                }
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "transport-guarantee"));
            }
        }
        if (descriptions!= null) {
            try {
                userDataConstraint.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, UserDataConstraint.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(userDataConstraint, LifecycleCallback.NONE);

        return userDataConstraint;
    }

    public final UserDataConstraint read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, UserDataConstraint userDataConstraint, RuntimeContext context)
        throws Exception
    {
        if (userDataConstraint == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (UserDataConstraint.class!= userDataConstraint.getClass()) {
            context.unexpectedSubclass(writer, userDataConstraint, UserDataConstraint.class);
            return ;
        }

        context.beforeMarshal(userDataConstraint, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = userDataConstraint.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(userDataConstraint, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = userDataConstraint.getDescriptions();
        } catch (Exception e) {
            context.getterError(userDataConstraint, "descriptions", UserDataConstraint.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(userDataConstraint, "descriptions");
                }
            }
        }

        // ELEMENT: transportGuarantee
        TransportGuarantee transportGuarantee = userDataConstraint.transportGuarantee;
        if (transportGuarantee!= null) {
            writer.writeStartElement(prefix, "transport-guarantee", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringTransportGuarantee(userDataConstraint, null, context, transportGuarantee));
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(userDataConstraint, "transportGuarantee");
        }

        context.afterMarshal(userDataConstraint, LifecycleCallback.NONE);
    }

}
