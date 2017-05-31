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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class ConstructorParameterOrder$JAXB
    extends JAXBObject<ConstructorParameterOrder> {


    public ConstructorParameterOrder$JAXB() {
        super(ConstructorParameterOrder.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "constructor-parameter-orderType".intern()));
    }

    public static ConstructorParameterOrder readConstructorParameterOrder(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeConstructorParameterOrder(final XoXMLStreamWriter writer, final ConstructorParameterOrder constructorParameterOrder, final RuntimeContext context)
        throws Exception {
        _write(writer, constructorParameterOrder, context);
    }

    public void write(final XoXMLStreamWriter writer, final ConstructorParameterOrder constructorParameterOrder, final RuntimeContext context)
        throws Exception {
        _write(writer, constructorParameterOrder, context);
    }

    public final static ConstructorParameterOrder _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final ConstructorParameterOrder constructorParameterOrder = new ConstructorParameterOrder();
        context.beforeUnmarshal(constructorParameterOrder, LifecycleCallback.NONE);

        List<String> elementName = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if ((!"constructor-parameter-orderType".equals(xsiType.getLocalPart())) || (!"http://java.sun.com/xml/ns/javaee".equals(xsiType.getNamespaceURI()))) {
                return context.unexpectedXsiType(reader, ConstructorParameterOrder.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id".equals(attribute.getLocalName())) && (("".equals(attribute.getNamespace())) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, constructorParameterOrder);
                constructorParameterOrder.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("element-name".equals(elementReader.getLocalName())) && ("http://java.sun.com/xml/ns/javaee".equals(elementReader.getNamespaceURI()))) {
                // ELEMENT: elementName
                final String elementNameItemRaw = elementReader.getElementAsString();

                final String elementNameItem;
                try {
                    elementNameItem = Adapters.collapsedStringAdapterAdapter.unmarshal(elementNameItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (elementName == null) {
                    elementName = constructorParameterOrder.elementName;
                    if (elementName != null) {
                        elementName.clear();
                    } else {
                        elementName = new ArrayList<String>();
                    }
                }
                elementName.add(elementNameItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "element-name"));
            }
        }
        if (elementName != null) {
            constructorParameterOrder.elementName = elementName;
        }

        context.afterUnmarshal(constructorParameterOrder, LifecycleCallback.NONE);

        return constructorParameterOrder;
    }

    public final ConstructorParameterOrder read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final ConstructorParameterOrder constructorParameterOrder, RuntimeContext context)
        throws Exception {
        if (constructorParameterOrder == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (ConstructorParameterOrder.class != constructorParameterOrder.getClass()) {
            context.unexpectedSubclass(writer, constructorParameterOrder, ConstructorParameterOrder.class);
            return;
        }

        context.beforeMarshal(constructorParameterOrder, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = constructorParameterOrder.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(constructorParameterOrder, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: elementName
        final List<String> elementNameRaw = constructorParameterOrder.elementName;
        if (elementNameRaw != null) {
            for (final String elementNameItem : elementNameRaw) {
                String elementName = null;
                try {
                    elementName = Adapters.collapsedStringAdapterAdapter.marshal(elementNameItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(constructorParameterOrder, "elementName", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (elementName != null) {
                    writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "element-name");
                    writer.writeCharacters(elementName);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(constructorParameterOrder, "elementName");
                }
            }
        }

        context.afterMarshal(constructorParameterOrder, LifecycleCallback.NONE);
    }

}
