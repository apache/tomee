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
import java.util.List;

import static org.apache.openejb.jee.LocaleEncodingMapping$JAXB.readLocaleEncodingMapping;
import static org.apache.openejb.jee.LocaleEncodingMapping$JAXB.writeLocaleEncodingMapping;

@SuppressWarnings({
        "StringEquality"
})
public class LocaleEncodingMappingList$JAXB
        extends JAXBObject<LocaleEncodingMappingList> {


    public LocaleEncodingMappingList$JAXB() {
        super(LocaleEncodingMappingList.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "locale-encoding-mapping-listType".intern()), LocaleEncodingMapping$JAXB.class);
    }

    public static LocaleEncodingMappingList readLocaleEncodingMappingList(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeLocaleEncodingMappingList(XoXMLStreamWriter writer, LocaleEncodingMappingList localeEncodingMappingList, RuntimeContext context)
            throws Exception {
        _write(writer, localeEncodingMappingList, context);
    }

    public void write(XoXMLStreamWriter writer, LocaleEncodingMappingList localeEncodingMappingList, RuntimeContext context)
            throws Exception {
        _write(writer, localeEncodingMappingList, context);
    }

    public final static LocaleEncodingMappingList _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        LocaleEncodingMappingList localeEncodingMappingList = new LocaleEncodingMappingList();
        context.beforeUnmarshal(localeEncodingMappingList, LifecycleCallback.NONE);

        List<LocaleEncodingMapping> localeEncodingMapping = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("locale-encoding-mapping-listType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, LocaleEncodingMappingList.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, localeEncodingMappingList);
                localeEncodingMappingList.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("locale-encoding-mapping" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: localeEncodingMapping
                LocaleEncodingMapping localeEncodingMappingItem = readLocaleEncodingMapping(elementReader, context);
                if (localeEncodingMapping == null) {
                    localeEncodingMapping = localeEncodingMappingList.localeEncodingMapping;
                    if (localeEncodingMapping != null) {
                        localeEncodingMapping.clear();
                    } else {
                        localeEncodingMapping = new ArrayList<LocaleEncodingMapping>();
                    }
                }
                localeEncodingMapping.add(localeEncodingMappingItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "locale-encoding-mapping"));
            }
        }
        if (localeEncodingMapping != null) {
            localeEncodingMappingList.localeEncodingMapping = localeEncodingMapping;
        }

        context.afterUnmarshal(localeEncodingMappingList, LifecycleCallback.NONE);

        return localeEncodingMappingList;
    }

    public final LocaleEncodingMappingList read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, LocaleEncodingMappingList localeEncodingMappingList, RuntimeContext context)
            throws Exception {
        if (localeEncodingMappingList == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (LocaleEncodingMappingList.class != localeEncodingMappingList.getClass()) {
            context.unexpectedSubclass(writer, localeEncodingMappingList, LocaleEncodingMappingList.class);
            return;
        }

        context.beforeMarshal(localeEncodingMappingList, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = localeEncodingMappingList.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(localeEncodingMappingList, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: localeEncodingMapping
        List<LocaleEncodingMapping> localeEncodingMapping = localeEncodingMappingList.localeEncodingMapping;
        if (localeEncodingMapping != null) {
            for (LocaleEncodingMapping localeEncodingMappingItem : localeEncodingMapping) {
                if (localeEncodingMappingItem != null) {
                    writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "locale-encoding-mapping");
                    writeLocaleEncodingMapping(writer, localeEncodingMappingItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(localeEncodingMappingList, "localeEncodingMapping");
                }
            }
        }

        context.afterMarshal(localeEncodingMappingList, LifecycleCallback.NONE);
    }

}
