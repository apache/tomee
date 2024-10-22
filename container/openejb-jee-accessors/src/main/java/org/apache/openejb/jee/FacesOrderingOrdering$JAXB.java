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


import static org.apache.openejb.jee.FacesOrderingOthers$JAXB.readFacesOrderingOthers;
import static org.apache.openejb.jee.FacesOrderingOthers$JAXB.writeFacesOrderingOthers;

@SuppressWarnings({
    "StringEquality"
})
public class FacesOrderingOrdering$JAXB
    extends JAXBObject<FacesOrderingOrdering>
{


    public FacesOrderingOrdering$JAXB() {
        super(FacesOrderingOrdering.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-ordering-orderingType".intern()), FacesOrderingOthers$JAXB.class);
    }

    public static FacesOrderingOrdering readFacesOrderingOrdering(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesOrderingOrdering(XoXMLStreamWriter writer, FacesOrderingOrdering facesOrderingOrdering, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesOrderingOrdering, context);
    }

    public void write(XoXMLStreamWriter writer, FacesOrderingOrdering facesOrderingOrdering, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesOrderingOrdering, context);
    }

    public static final FacesOrderingOrdering _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesOrderingOrdering facesOrderingOrdering = new FacesOrderingOrdering();
        context.beforeUnmarshal(facesOrderingOrdering, LifecycleCallback.NONE);

        List<String> name = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-ordering-orderingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesOrderingOrdering.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                String nameItemRaw = null;
                if (!elementReader.isXsiNil()) {
                    nameItemRaw = elementReader.getElementText();
                }

                String nameItem;
                try {
                    nameItem = Adapters.collapsedStringAdapterAdapter.unmarshal(nameItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (name == null) {
                    name = facesOrderingOrdering.name;
                    if (name!= null) {
                        name.clear();
                    } else {
                        name = new ArrayList<>();
                    }
                }
                name.add(nameItem);
            } else if (("others" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: others
                FacesOrderingOthers others = readFacesOrderingOthers(elementReader, context);
                facesOrderingOrdering.others = others;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "others"));
            }
        }
        if (name!= null) {
            facesOrderingOrdering.name = name;
        }

        context.afterUnmarshal(facesOrderingOrdering, LifecycleCallback.NONE);

        return facesOrderingOrdering;
    }

    public final FacesOrderingOrdering read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesOrderingOrdering facesOrderingOrdering, RuntimeContext context)
        throws Exception
    {
        if (facesOrderingOrdering == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesOrderingOrdering.class!= facesOrderingOrdering.getClass()) {
            context.unexpectedSubclass(writer, facesOrderingOrdering, FacesOrderingOrdering.class);
            return ;
        }

        context.beforeMarshal(facesOrderingOrdering, LifecycleCallback.NONE);


        // ELEMENT: name
        List<String> nameRaw = facesOrderingOrdering.name;
        if (nameRaw!= null) {
            for (String nameItem: nameRaw) {
                String name = null;
                try {
                    name = Adapters.collapsedStringAdapterAdapter.marshal(nameItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesOrderingOrdering, "name", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
                if (name!= null) {
                    writer.writeCharacters(name);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: others
        FacesOrderingOthers others = facesOrderingOrdering.others;
        if (others!= null) {
            writer.writeStartElement(prefix, "others", "http://java.sun.com/xml/ns/javaee");
            writeFacesOrderingOthers(writer, others, context);
            writer.writeEndElement();
        }

        context.afterMarshal(facesOrderingOrdering, LifecycleCallback.NONE);
    }

}
