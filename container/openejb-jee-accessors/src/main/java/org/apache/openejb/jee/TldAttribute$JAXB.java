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
import static org.apache.openejb.jee.TldDeferredMethod$JAXB.readTldDeferredMethod;
import static org.apache.openejb.jee.TldDeferredMethod$JAXB.writeTldDeferredMethod;
import static org.apache.openejb.jee.TldDeferredValue$JAXB.readTldDeferredValue;
import static org.apache.openejb.jee.TldDeferredValue$JAXB.writeTldDeferredValue;

@SuppressWarnings({
        "StringEquality"
})
public class TldAttribute$JAXB
        extends JAXBObject<TldAttribute> {


    public TldAttribute$JAXB() {
        super(TldAttribute.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "tld-attributeType".intern()), Text$JAXB.class, TldDeferredValue$JAXB.class, TldDeferredMethod$JAXB.class);
    }

    public static TldAttribute readTldAttribute(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeTldAttribute(XoXMLStreamWriter writer, TldAttribute tldAttribute, RuntimeContext context)
            throws Exception {
        _write(writer, tldAttribute, context);
    }

    public void write(XoXMLStreamWriter writer, TldAttribute tldAttribute, RuntimeContext context)
            throws Exception {
        _write(writer, tldAttribute, context);
    }

    public final static TldAttribute _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        TldAttribute tldAttribute = new TldAttribute();
        context.beforeUnmarshal(tldAttribute, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("tld-attributeType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, TldAttribute.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, tldAttribute);
                tldAttribute.id = id;
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
            } else if (("name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                String nameRaw = elementReader.getElementAsString();

                String name;
                try {
                    name = Adapters.collapsedStringAdapterAdapter.unmarshal(nameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldAttribute.name = name;
            } else if (("required" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: required
                String requiredRaw = elementReader.getElementAsString();

                String required;
                try {
                    required = Adapters.collapsedStringAdapterAdapter.unmarshal(requiredRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldAttribute.required = required;
            } else if (("rtexprvalue" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: rtexprvalue
                String rtexprvalueRaw = elementReader.getElementAsString();

                String rtexprvalue;
                try {
                    rtexprvalue = Adapters.collapsedStringAdapterAdapter.unmarshal(rtexprvalueRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldAttribute.rtexprvalue = rtexprvalue;
            } else if (("type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: type
                String typeRaw = elementReader.getElementAsString();

                String type;
                try {
                    type = Adapters.collapsedStringAdapterAdapter.unmarshal(typeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldAttribute.type = type;
            } else if (("deferred-value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: deferredValue
                TldDeferredValue deferredValue = readTldDeferredValue(elementReader, context);
                tldAttribute.deferredValue = deferredValue;
            } else if (("deferred-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: deferredMethod
                TldDeferredMethod deferredMethod = readTldDeferredMethod(elementReader, context);
                tldAttribute.deferredMethod = deferredMethod;
            } else if (("fragment" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: fragment
                String fragmentRaw = elementReader.getElementAsString();

                String fragment;
                try {
                    fragment = Adapters.collapsedStringAdapterAdapter.unmarshal(fragmentRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldAttribute.fragment = fragment;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "required"), new QName("http://java.sun.com/xml/ns/javaee", "rtexprvalue"), new QName("http://java.sun.com/xml/ns/javaee", "type"), new QName("http://java.sun.com/xml/ns/javaee", "deferred-value"), new QName("http://java.sun.com/xml/ns/javaee", "deferred-method"), new QName("http://java.sun.com/xml/ns/javaee", "fragment"));
            }
        }
        if (descriptions != null) {
            try {
                tldAttribute.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, TldAttribute.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(tldAttribute, LifecycleCallback.NONE);

        return tldAttribute;
    }

    public final TldAttribute read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, TldAttribute tldAttribute, RuntimeContext context)
            throws Exception {
        if (tldAttribute == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (TldAttribute.class != tldAttribute.getClass()) {
            context.unexpectedSubclass(writer, tldAttribute, TldAttribute.class);
            return;
        }

        context.beforeMarshal(tldAttribute, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = tldAttribute.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(tldAttribute, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = tldAttribute.getDescriptions();
        } catch (Exception e) {
            context.getterError(tldAttribute, "descriptions", TldAttribute.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tldAttribute, "descriptions");
                }
            }
        }

        // ELEMENT: name
        String nameRaw = tldAttribute.name;
        String name = null;
        try {
            name = Adapters.collapsedStringAdapterAdapter.marshal(nameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(tldAttribute, "name", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (name != null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(name);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(tldAttribute, "name");
        }

        // ELEMENT: required
        String requiredRaw = tldAttribute.required;
        String required = null;
        try {
            required = Adapters.collapsedStringAdapterAdapter.marshal(requiredRaw);
        } catch (Exception e) {
            context.xmlAdapterError(tldAttribute, "required", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (required != null) {
            writer.writeStartElement(prefix, "required", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(required);
            writer.writeEndElement();
        }

        // ELEMENT: rtexprvalue
        String rtexprvalueRaw = tldAttribute.rtexprvalue;
        String rtexprvalue = null;
        try {
            rtexprvalue = Adapters.collapsedStringAdapterAdapter.marshal(rtexprvalueRaw);
        } catch (Exception e) {
            context.xmlAdapterError(tldAttribute, "rtexprvalue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (rtexprvalue != null) {
            writer.writeStartElement(prefix, "rtexprvalue", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(rtexprvalue);
            writer.writeEndElement();
        }

        // ELEMENT: type
        String typeRaw = tldAttribute.type;
        String type = null;
        try {
            type = Adapters.collapsedStringAdapterAdapter.marshal(typeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(tldAttribute, "type", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (type != null) {
            writer.writeStartElement(prefix, "type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(type);
            writer.writeEndElement();
        }

        // ELEMENT: deferredValue
        TldDeferredValue deferredValue = tldAttribute.deferredValue;
        if (deferredValue != null) {
            writer.writeStartElement(prefix, "deferred-value", "http://java.sun.com/xml/ns/javaee");
            writeTldDeferredValue(writer, deferredValue, context);
            writer.writeEndElement();
        }

        // ELEMENT: deferredMethod
        TldDeferredMethod deferredMethod = tldAttribute.deferredMethod;
        if (deferredMethod != null) {
            writer.writeStartElement(prefix, "deferred-method", "http://java.sun.com/xml/ns/javaee");
            writeTldDeferredMethod(writer, deferredMethod, context);
            writer.writeEndElement();
        }

        // ELEMENT: fragment
        String fragmentRaw = tldAttribute.fragment;
        String fragment = null;
        try {
            fragment = Adapters.collapsedStringAdapterAdapter.marshal(fragmentRaw);
        } catch (Exception e) {
            context.xmlAdapterError(tldAttribute, "fragment", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (fragment != null) {
            writer.writeStartElement(prefix, "fragment", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(fragment);
            writer.writeEndElement();
        }

        context.afterMarshal(tldAttribute, LifecycleCallback.NONE);
    }

}
