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
import java.util.LinkedHashSet;
import java.util.Set;

import static org.apache.openejb.jee.InjectionTarget$JAXB.readInjectionTarget;
import static org.apache.openejb.jee.InjectionTarget$JAXB.writeInjectionTarget;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class ResourceEnvRef$JAXB
        extends JAXBObject<ResourceEnvRef> {


    public ResourceEnvRef$JAXB() {
        super(ResourceEnvRef.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "resource-env-refType".intern()), Text$JAXB.class, InjectionTarget$JAXB.class);
    }

    public static ResourceEnvRef readResourceEnvRef(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeResourceEnvRef(XoXMLStreamWriter writer, ResourceEnvRef resourceEnvRef, RuntimeContext context)
            throws Exception {
        _write(writer, resourceEnvRef, context);
    }

    public void write(XoXMLStreamWriter writer, ResourceEnvRef resourceEnvRef, RuntimeContext context)
            throws Exception {
        _write(writer, resourceEnvRef, context);
    }

    public final static ResourceEnvRef _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ResourceEnvRef resourceEnvRef = new ResourceEnvRef();
        context.beforeUnmarshal(resourceEnvRef, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        Set<InjectionTarget> injectionTarget = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("resource-env-refType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ResourceEnvRef.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, resourceEnvRef);
                resourceEnvRef.id = id;
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
            } else if (("resource-env-ref-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceEnvRefName
                String resourceEnvRefNameRaw = elementReader.getElementAsString();

                String resourceEnvRefName;
                try {
                    resourceEnvRefName = Adapters.collapsedStringAdapterAdapter.unmarshal(resourceEnvRefNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                resourceEnvRef.resourceEnvRefName = resourceEnvRefName;
            } else if (("resource-env-ref-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceEnvRefType
                String resourceEnvRefTypeRaw = elementReader.getElementAsString();

                String resourceEnvRefType;
                try {
                    resourceEnvRefType = Adapters.collapsedStringAdapterAdapter.unmarshal(resourceEnvRefTypeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                resourceEnvRef.resourceEnvRefType = resourceEnvRefType;
            } else if (("mapped-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mappedName
                String mappedNameRaw = elementReader.getElementAsString();

                String mappedName;
                try {
                    mappedName = Adapters.collapsedStringAdapterAdapter.unmarshal(mappedNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                resourceEnvRef.mappedName = mappedName;
            } else if (("injection-target" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: injectionTarget
                InjectionTarget injectionTargetItem = readInjectionTarget(elementReader, context);
                if (injectionTarget == null) {
                    injectionTarget = resourceEnvRef.injectionTarget;
                    if (injectionTarget != null) {
                        injectionTarget.clear();
                    } else {
                        injectionTarget = new LinkedHashSet<InjectionTarget>();
                    }
                }
                injectionTarget.add(injectionTargetItem);
            } else if (("lookup-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lookupName
                String lookupNameRaw = elementReader.getElementAsString();

                String lookupName;
                try {
                    lookupName = Adapters.collapsedStringAdapterAdapter.unmarshal(lookupNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                resourceEnvRef.lookupName = lookupName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "resource-env-ref-name"), new QName("http://java.sun.com/xml/ns/javaee", "resource-env-ref-type"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "injection-target"), new QName("http://java.sun.com/xml/ns/javaee", "lookup-name"));
            }
        }
        if (descriptions != null) {
            try {
                resourceEnvRef.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, ResourceEnvRef.class, "setDescriptions", Text[].class, e);
            }
        }
        if (injectionTarget != null) {
            resourceEnvRef.injectionTarget = injectionTarget;
        }

        context.afterUnmarshal(resourceEnvRef, LifecycleCallback.NONE);

        return resourceEnvRef;
    }

    public final ResourceEnvRef read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, ResourceEnvRef resourceEnvRef, RuntimeContext context)
            throws Exception {
        if (resourceEnvRef == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ResourceEnvRef.class != resourceEnvRef.getClass()) {
            context.unexpectedSubclass(writer, resourceEnvRef, ResourceEnvRef.class);
            return;
        }

        context.beforeMarshal(resourceEnvRef, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = resourceEnvRef.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(resourceEnvRef, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = resourceEnvRef.getDescriptions();
        } catch (Exception e) {
            context.getterError(resourceEnvRef, "descriptions", ResourceEnvRef.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(resourceEnvRef, "descriptions");
                }
            }
        }

        // ELEMENT: resourceEnvRefName
        String resourceEnvRefNameRaw = resourceEnvRef.resourceEnvRefName;
        String resourceEnvRefName = null;
        try {
            resourceEnvRefName = Adapters.collapsedStringAdapterAdapter.marshal(resourceEnvRefNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(resourceEnvRef, "resourceEnvRefName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (resourceEnvRefName != null) {
            writer.writeStartElement(prefix, "resource-env-ref-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(resourceEnvRefName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(resourceEnvRef, "resourceEnvRefName");
        }

        // ELEMENT: resourceEnvRefType
        String resourceEnvRefTypeRaw = resourceEnvRef.resourceEnvRefType;
        String resourceEnvRefType = null;
        try {
            resourceEnvRefType = Adapters.collapsedStringAdapterAdapter.marshal(resourceEnvRefTypeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(resourceEnvRef, "resourceEnvRefType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (resourceEnvRefType != null) {
            writer.writeStartElement(prefix, "resource-env-ref-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(resourceEnvRefType);
            writer.writeEndElement();
        }

        // ELEMENT: mappedName
        String mappedNameRaw = resourceEnvRef.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(resourceEnvRef, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: injectionTarget
        Set<InjectionTarget> injectionTarget = resourceEnvRef.injectionTarget;
        if (injectionTarget != null) {
            for (InjectionTarget injectionTargetItem : injectionTarget) {
                if (injectionTargetItem != null) {
                    writer.writeStartElement(prefix, "injection-target", "http://java.sun.com/xml/ns/javaee");
                    writeInjectionTarget(writer, injectionTargetItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(resourceEnvRef, "injectionTarget");
                }
            }
        }

        // ELEMENT: lookupName
        String lookupNameRaw = resourceEnvRef.lookupName;
        String lookupName = null;
        try {
            lookupName = Adapters.collapsedStringAdapterAdapter.marshal(lookupNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(resourceEnvRef, "lookupName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lookupName != null) {
            writer.writeStartElement(prefix, "lookup-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lookupName);
            writer.writeEndElement();
        }

        context.afterMarshal(resourceEnvRef, LifecycleCallback.NONE);
    }

}
