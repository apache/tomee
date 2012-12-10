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
import static org.apache.openejb.jee.ResAuth$JAXB.parseResAuth;
import static org.apache.openejb.jee.ResAuth$JAXB.toStringResAuth;
import static org.apache.openejb.jee.ResSharingScope$JAXB.parseResSharingScope;
import static org.apache.openejb.jee.ResSharingScope$JAXB.toStringResSharingScope;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class ResourceRef$JAXB
        extends JAXBObject<ResourceRef> {


    public ResourceRef$JAXB() {
        super(ResourceRef.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "resource-refType".intern()), Text$JAXB.class, ResAuth$JAXB.class, ResSharingScope$JAXB.class, InjectionTarget$JAXB.class);
    }

    public static ResourceRef readResourceRef(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeResourceRef(XoXMLStreamWriter writer, ResourceRef resourceRef, RuntimeContext context)
            throws Exception {
        _write(writer, resourceRef, context);
    }

    public void write(XoXMLStreamWriter writer, ResourceRef resourceRef, RuntimeContext context)
            throws Exception {
        _write(writer, resourceRef, context);
    }

    public final static ResourceRef _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ResourceRef resourceRef = new ResourceRef();
        context.beforeUnmarshal(resourceRef, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        Set<InjectionTarget> injectionTarget = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("resource-refType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ResourceRef.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, resourceRef);
                resourceRef.id = id;
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
            } else if (("res-ref-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resRefName
                String resRefNameRaw = elementReader.getElementAsString();

                String resRefName;
                try {
                    resRefName = Adapters.collapsedStringAdapterAdapter.unmarshal(resRefNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                resourceRef.resRefName = resRefName;
            } else if (("res-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resType
                String resTypeRaw = elementReader.getElementAsString();

                String resType;
                try {
                    resType = Adapters.collapsedStringAdapterAdapter.unmarshal(resTypeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                resourceRef.resType = resType;
            } else if (("res-auth" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resAuth
                ResAuth resAuth = parseResAuth(elementReader, context, elementReader.getElementAsString());
                if (resAuth != null) {
                    resourceRef.resAuth = resAuth;
                }
            } else if (("res-sharing-scope" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resSharingScope
                ResSharingScope resSharingScope = parseResSharingScope(elementReader, context, elementReader.getElementAsString());
                if (resSharingScope != null) {
                    resourceRef.resSharingScope = resSharingScope;
                }
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

                resourceRef.mappedName = mappedName;
            } else if (("injection-target" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: injectionTarget
                InjectionTarget injectionTargetItem = readInjectionTarget(elementReader, context);
                if (injectionTarget == null) {
                    injectionTarget = resourceRef.injectionTarget;
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

                resourceRef.lookupName = lookupName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "res-ref-name"), new QName("http://java.sun.com/xml/ns/javaee", "res-type"), new QName("http://java.sun.com/xml/ns/javaee", "res-auth"), new QName("http://java.sun.com/xml/ns/javaee", "res-sharing-scope"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "injection-target"), new QName("http://java.sun.com/xml/ns/javaee", "lookup-name"));
            }
        }
        if (descriptions != null) {
            try {
                resourceRef.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, ResourceRef.class, "setDescriptions", Text[].class, e);
            }
        }
        if (injectionTarget != null) {
            resourceRef.injectionTarget = injectionTarget;
        }

        context.afterUnmarshal(resourceRef, LifecycleCallback.NONE);

        return resourceRef;
    }

    public final ResourceRef read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, ResourceRef resourceRef, RuntimeContext context)
            throws Exception {
        if (resourceRef == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ResourceRef.class != resourceRef.getClass()) {
            context.unexpectedSubclass(writer, resourceRef, ResourceRef.class);
            return;
        }

        context.beforeMarshal(resourceRef, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = resourceRef.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(resourceRef, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = resourceRef.getDescriptions();
        } catch (Exception e) {
            context.getterError(resourceRef, "descriptions", ResourceRef.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(resourceRef, "descriptions");
                }
            }
        }

        // ELEMENT: resRefName
        String resRefNameRaw = resourceRef.resRefName;
        String resRefName = null;
        try {
            resRefName = Adapters.collapsedStringAdapterAdapter.marshal(resRefNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(resourceRef, "resRefName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (resRefName != null) {
            writer.writeStartElement(prefix, "res-ref-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(resRefName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(resourceRef, "resRefName");
        }

        // ELEMENT: resType
        String resTypeRaw = resourceRef.resType;
        String resType = null;
        try {
            resType = Adapters.collapsedStringAdapterAdapter.marshal(resTypeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(resourceRef, "resType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (resType != null) {
            writer.writeStartElement(prefix, "res-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(resType);
            writer.writeEndElement();
        }

        // ELEMENT: resAuth
        ResAuth resAuth = resourceRef.resAuth;
        if (resAuth != null) {
            writer.writeStartElement(prefix, "res-auth", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringResAuth(resourceRef, null, context, resAuth));
            writer.writeEndElement();
        }

        // ELEMENT: resSharingScope
        ResSharingScope resSharingScope = resourceRef.resSharingScope;
        if (resSharingScope != null) {
            writer.writeStartElement(prefix, "res-sharing-scope", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringResSharingScope(resourceRef, null, context, resSharingScope));
            writer.writeEndElement();
        }

        // ELEMENT: mappedName
        String mappedNameRaw = resourceRef.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(resourceRef, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: injectionTarget
        Set<InjectionTarget> injectionTarget = resourceRef.injectionTarget;
        if (injectionTarget != null) {
            for (InjectionTarget injectionTargetItem : injectionTarget) {
                if (injectionTargetItem != null) {
                    writer.writeStartElement(prefix, "injection-target", "http://java.sun.com/xml/ns/javaee");
                    writeInjectionTarget(writer, injectionTargetItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(resourceRef, "injectionTarget");
                }
            }
        }

        // ELEMENT: lookupName
        String lookupNameRaw = resourceRef.lookupName;
        String lookupName = null;
        try {
            lookupName = Adapters.collapsedStringAdapterAdapter.marshal(lookupNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(resourceRef, "lookupName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lookupName != null) {
            writer.writeStartElement(prefix, "lookup-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lookupName);
            writer.writeEndElement();
        }

        context.afterMarshal(resourceRef, LifecycleCallback.NONE);
    }

}
