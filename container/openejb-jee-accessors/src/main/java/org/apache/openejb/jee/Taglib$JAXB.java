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
public class Taglib$JAXB
    extends JAXBObject<Taglib>
{


    public Taglib$JAXB() {
        super(Taglib.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "taglibType".intern()));
    }

    public static Taglib readTaglib(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeTaglib(XoXMLStreamWriter writer, Taglib taglib, RuntimeContext context)
        throws Exception
    {
        _write(writer, taglib, context);
    }

    public void write(XoXMLStreamWriter writer, Taglib taglib, RuntimeContext context)
        throws Exception
    {
        _write(writer, taglib, context);
    }

    public static final Taglib _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Taglib taglib = new Taglib();
        context.beforeUnmarshal(taglib, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("taglibType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Taglib.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, taglib);
                taglib.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("taglib-uri" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: taglibUri
                String taglibUriRaw = elementReader.getElementText();

                String taglibUri;
                try {
                    taglibUri = Adapters.collapsedStringAdapterAdapter.unmarshal(taglibUriRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                taglib.taglibUri = taglibUri;
            } else if (("taglib-location" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: taglibLocation
                String taglibLocationRaw = elementReader.getElementText();

                String taglibLocation;
                try {
                    taglibLocation = Adapters.collapsedStringAdapterAdapter.unmarshal(taglibLocationRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                taglib.taglibLocation = taglibLocation;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "taglib-uri"), new QName("http://java.sun.com/xml/ns/javaee", "taglib-location"));
            }
        }

        context.afterUnmarshal(taglib, LifecycleCallback.NONE);

        return taglib;
    }

    public final Taglib read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Taglib taglib, RuntimeContext context)
        throws Exception
    {
        if (taglib == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Taglib.class!= taglib.getClass()) {
            context.unexpectedSubclass(writer, taglib, Taglib.class);
            return ;
        }

        context.beforeMarshal(taglib, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = taglib.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(taglib, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: taglibUri
        String taglibUriRaw = taglib.taglibUri;
        String taglibUri = null;
        try {
            taglibUri = Adapters.collapsedStringAdapterAdapter.marshal(taglibUriRaw);
        } catch (Exception e) {
            context.xmlAdapterError(taglib, "taglibUri", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (taglibUri!= null) {
            writer.writeStartElement(prefix, "taglib-uri", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(taglibUri);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(taglib, "taglibUri");
        }

        // ELEMENT: taglibLocation
        String taglibLocationRaw = taglib.taglibLocation;
        String taglibLocation = null;
        try {
            taglibLocation = Adapters.collapsedStringAdapterAdapter.marshal(taglibLocationRaw);
        } catch (Exception e) {
            context.xmlAdapterError(taglib, "taglibLocation", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (taglibLocation!= null) {
            writer.writeStartElement(prefix, "taglib-location", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(taglibLocation);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(taglib, "taglibLocation");
        }

        context.afterMarshal(taglib, LifecycleCallback.NONE);
    }

}
