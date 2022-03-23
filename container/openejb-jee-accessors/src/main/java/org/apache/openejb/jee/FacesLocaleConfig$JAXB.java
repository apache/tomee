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
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.FacesDefaultLocale$JAXB.readFacesDefaultLocale;
import static org.apache.openejb.jee.FacesDefaultLocale$JAXB.writeFacesDefaultLocale;
import static org.apache.openejb.jee.FacesSupportedLocale$JAXB.readFacesSupportedLocale;
import static org.apache.openejb.jee.FacesSupportedLocale$JAXB.writeFacesSupportedLocale;

@SuppressWarnings({
    "StringEquality"
})
public class FacesLocaleConfig$JAXB
    extends JAXBObject<FacesLocaleConfig> {


    public FacesLocaleConfig$JAXB() {
        super(FacesLocaleConfig.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-locale-configType".intern()), FacesDefaultLocale$JAXB.class, FacesSupportedLocale$JAXB.class);
    }

    public static FacesLocaleConfig readFacesLocaleConfig(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesLocaleConfig(final XoXMLStreamWriter writer, final FacesLocaleConfig facesLocaleConfig, final RuntimeContext context)
        throws Exception {
        _write(writer, facesLocaleConfig, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesLocaleConfig facesLocaleConfig, final RuntimeContext context)
        throws Exception {
        _write(writer, facesLocaleConfig, context);
    }

    public final static FacesLocaleConfig _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesLocaleConfig facesLocaleConfig = new FacesLocaleConfig();
        context.beforeUnmarshal(facesLocaleConfig, LifecycleCallback.NONE);

        List<FacesSupportedLocale> supportedLocale = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-locale-configType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesLocaleConfig.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesLocaleConfig);
                facesLocaleConfig.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("default-locale" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultLocale
                final FacesDefaultLocale defaultLocale = readFacesDefaultLocale(elementReader, context);
                facesLocaleConfig.defaultLocale = defaultLocale;
            } else if (("supported-locale" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: supportedLocale
                final FacesSupportedLocale supportedLocaleItem = readFacesSupportedLocale(elementReader, context);
                if (supportedLocale == null) {
                    supportedLocale = facesLocaleConfig.supportedLocale;
                    if (supportedLocale != null) {
                        supportedLocale.clear();
                    } else {
                        supportedLocale = new ArrayList<FacesSupportedLocale>();
                    }
                }
                supportedLocale.add(supportedLocaleItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "default-locale"), new QName("http://java.sun.com/xml/ns/javaee", "supported-locale"));
            }
        }
        if (supportedLocale != null) {
            facesLocaleConfig.supportedLocale = supportedLocale;
        }

        context.afterUnmarshal(facesLocaleConfig, LifecycleCallback.NONE);

        return facesLocaleConfig;
    }

    public final FacesLocaleConfig read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesLocaleConfig facesLocaleConfig, RuntimeContext context)
        throws Exception {
        if (facesLocaleConfig == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesLocaleConfig.class != facesLocaleConfig.getClass()) {
            context.unexpectedSubclass(writer, facesLocaleConfig, FacesLocaleConfig.class);
            return;
        }

        context.beforeMarshal(facesLocaleConfig, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesLocaleConfig.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesLocaleConfig, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: defaultLocale
        final FacesDefaultLocale defaultLocale = facesLocaleConfig.defaultLocale;
        if (defaultLocale != null) {
            writer.writeStartElement(prefix, "default-locale", "http://java.sun.com/xml/ns/javaee");
            writeFacesDefaultLocale(writer, defaultLocale, context);
            writer.writeEndElement();
        }

        // ELEMENT: supportedLocale
        final List<FacesSupportedLocale> supportedLocale = facesLocaleConfig.supportedLocale;
        if (supportedLocale != null) {
            for (final FacesSupportedLocale supportedLocaleItem : supportedLocale) {
                if (supportedLocaleItem != null) {
                    writer.writeStartElement(prefix, "supported-locale", "http://java.sun.com/xml/ns/javaee");
                    writeFacesSupportedLocale(writer, supportedLocaleItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesLocaleConfig, LifecycleCallback.NONE);
    }

}
