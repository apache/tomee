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
public class PackageMapping$JAXB
    extends JAXBObject<PackageMapping>
{


    public PackageMapping$JAXB() {
        super(PackageMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "package-mappingType".intern()));
    }

    public static PackageMapping readPackageMapping(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writePackageMapping(XoXMLStreamWriter writer, PackageMapping packageMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, packageMapping, context);
    }

    public void write(XoXMLStreamWriter writer, PackageMapping packageMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, packageMapping, context);
    }

    public static final PackageMapping _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        PackageMapping packageMapping = new PackageMapping();
        context.beforeUnmarshal(packageMapping, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("package-mappingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, PackageMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, packageMapping);
                packageMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("package-type" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: packageType
                String packageTypeRaw = elementReader.getElementText();

                String packageType;
                try {
                    packageType = Adapters.collapsedStringAdapterAdapter.unmarshal(packageTypeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                packageMapping.packageType = packageType;
            } else if (("namespaceURI" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: namespaceURI
                String namespaceURIRaw = elementReader.getElementText();

                String namespaceURI;
                try {
                    namespaceURI = Adapters.collapsedStringAdapterAdapter.unmarshal(namespaceURIRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                packageMapping.namespaceURI = namespaceURI;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "package-type"), new QName("http://java.sun.com/xml/ns/javaee", "namespaceURI"));
            }
        }

        context.afterUnmarshal(packageMapping, LifecycleCallback.NONE);

        return packageMapping;
    }

    public final PackageMapping read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, PackageMapping packageMapping, RuntimeContext context)
        throws Exception
    {
        if (packageMapping == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (PackageMapping.class!= packageMapping.getClass()) {
            context.unexpectedSubclass(writer, packageMapping, PackageMapping.class);
            return ;
        }

        context.beforeMarshal(packageMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = packageMapping.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(packageMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: packageType
        String packageTypeRaw = packageMapping.packageType;
        String packageType = null;
        try {
            packageType = Adapters.collapsedStringAdapterAdapter.marshal(packageTypeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(packageMapping, "packageType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (packageType!= null) {
            writer.writeStartElement(prefix, "package-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(packageType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(packageMapping, "packageType");
        }

        // ELEMENT: namespaceURI
        String namespaceURIRaw = packageMapping.namespaceURI;
        String namespaceURI = null;
        try {
            namespaceURI = Adapters.collapsedStringAdapterAdapter.marshal(namespaceURIRaw);
        } catch (Exception e) {
            context.xmlAdapterError(packageMapping, "namespaceURI", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (namespaceURI!= null) {
            writer.writeStartElement(prefix, "namespaceURI", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(namespaceURI);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(packageMapping, "namespaceURI");
        }

        context.afterMarshal(packageMapping, LifecycleCallback.NONE);
    }

}
