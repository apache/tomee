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
import org.apache.openejb.jee.jba.JndiName;
import org.apache.openejb.jee.jba.JndiName$JAXB;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.Description$JAXB.readDescription;
import static org.apache.openejb.jee.Description$JAXB.writeDescription;
import static org.apache.openejb.jee.Property$JAXB.readProperty;
import static org.apache.openejb.jee.Property$JAXB.writeProperty;
import static org.apache.openejb.jee.jba.JndiName$JAXB.readJndiName;
import static org.apache.openejb.jee.jba.JndiName$JAXB.writeJndiName;

@SuppressWarnings({
    "StringEquality"
})
public class ContextService$JAXB
    extends JAXBObject<ContextService>
{


    public ContextService$JAXB() {
        super(ContextService.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "context-serviceType".intern()), Description$JAXB.class, JndiName$JAXB.class, Property$JAXB.class);
    }

    public static ContextService readContextService(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeContextService(XoXMLStreamWriter writer, ContextService contextService, RuntimeContext context)
        throws Exception
    {
        _write(writer, contextService, context);
    }

    public void write(XoXMLStreamWriter writer, ContextService contextService, RuntimeContext context)
        throws Exception
    {
        _write(writer, contextService, context);
    }

    public static final ContextService _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ContextService contextService = new ContextService();
        context.beforeUnmarshal(contextService, LifecycleCallback.NONE);

        List<String> cleared = null;
        List<String> propagated = null;
        List<String> unchanged = null;
        List<Property> property = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("context-serviceType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ContextService.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, contextService);
                contextService.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: description
                Description description = readDescription(elementReader, context);
                contextService.description = description;
            } else if (("name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                JndiName name = readJndiName(elementReader, context);
                contextService.name = name;
            } else if (("cleared" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: cleared
                String clearedItemRaw = elementReader.getElementText();

                String clearedItem;
                try {
                    clearedItem = Adapters.collapsedStringAdapterAdapter.unmarshal(clearedItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (cleared == null) {
                    cleared = contextService.cleared;
                    if (cleared!= null) {
                        cleared.clear();
                    } else {
                        cleared = new ArrayList<>();
                    }
                }
                cleared.add(clearedItem);
            } else if (("propagated" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: propagated
                String propagatedItemRaw = elementReader.getElementText();

                String propagatedItem;
                try {
                    propagatedItem = Adapters.collapsedStringAdapterAdapter.unmarshal(propagatedItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (propagated == null) {
                    propagated = contextService.propagated;
                    if (propagated!= null) {
                        propagated.clear();
                    } else {
                        propagated = new ArrayList<>();
                    }
                }
                propagated.add(propagatedItem);
            } else if (("unchanged" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: unchanged
                String unchangedItemRaw = elementReader.getElementText();

                String unchangedItem;
                try {
                    unchangedItem = Adapters.collapsedStringAdapterAdapter.unmarshal(unchangedItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (unchanged == null) {
                    unchanged = contextService.unchanged;
                    if (unchanged!= null) {
                        unchanged.clear();
                    } else {
                        unchanged = new ArrayList<>();
                    }
                }
                unchanged.add(unchangedItem);
            } else if (("property" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: property
                Property propertyItem = readProperty(elementReader, context);
                if (property == null) {
                    property = contextService.property;
                    if (property!= null) {
                        property.clear();
                    } else {
                        property = new ArrayList<>();
                    }
                }
                property.add(propertyItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "cleared"), new QName("http://java.sun.com/xml/ns/javaee", "propagated"), new QName("http://java.sun.com/xml/ns/javaee", "unchanged"), new QName("http://java.sun.com/xml/ns/javaee", "property"));
            }
        }
        if (cleared!= null) {
            contextService.cleared = cleared;
        }
        if (propagated!= null) {
            contextService.propagated = propagated;
        }
        if (unchanged!= null) {
            contextService.unchanged = unchanged;
        }
        if (property!= null) {
            contextService.property = property;
        }

        context.afterUnmarshal(contextService, LifecycleCallback.NONE);

        return contextService;
    }

    public final ContextService read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ContextService contextService, RuntimeContext context)
        throws Exception
    {
        if (contextService == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ContextService.class!= contextService.getClass()) {
            context.unexpectedSubclass(writer, contextService, ContextService.class);
            return ;
        }

        context.beforeMarshal(contextService, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = contextService.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(contextService, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: description
        Description description = contextService.description;
        if (description!= null) {
            writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
            writeDescription(writer, description, context);
            writer.writeEndElement();
        }

        // ELEMENT: name
        JndiName name = contextService.name;
        if (name!= null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writeJndiName(writer, name, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(contextService, "name");
        }

        // ELEMENT: cleared
        List<String> clearedRaw = contextService.cleared;
        if (clearedRaw!= null) {
            for (String clearedItem: clearedRaw) {
                String cleared = null;
                try {
                    cleared = Adapters.collapsedStringAdapterAdapter.marshal(clearedItem);
                } catch (Exception e) {
                    context.xmlAdapterError(contextService, "cleared", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (cleared!= null) {
                    writer.writeStartElement(prefix, "cleared", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(cleared);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: propagated
        List<String> propagatedRaw = contextService.propagated;
        if (propagatedRaw!= null) {
            for (String propagatedItem: propagatedRaw) {
                String propagated = null;
                try {
                    propagated = Adapters.collapsedStringAdapterAdapter.marshal(propagatedItem);
                } catch (Exception e) {
                    context.xmlAdapterError(contextService, "propagated", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (propagated!= null) {
                    writer.writeStartElement(prefix, "propagated", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(propagated);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: unchanged
        List<String> unchangedRaw = contextService.unchanged;
        if (unchangedRaw!= null) {
            for (String unchangedItem: unchangedRaw) {
                String unchanged = null;
                try {
                    unchanged = Adapters.collapsedStringAdapterAdapter.marshal(unchangedItem);
                } catch (Exception e) {
                    context.xmlAdapterError(contextService, "unchanged", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (unchanged!= null) {
                    writer.writeStartElement(prefix, "unchanged", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(unchanged);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: property
        List<Property> property = contextService.property;
        if (property!= null) {
            for (Property propertyItem: property) {
                if (propertyItem!= null) {
                    writer.writeStartElement(prefix, "property", "http://java.sun.com/xml/ns/javaee");
                    writeProperty(writer, propertyItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(contextService, LifecycleCallback.NONE);
    }

}
