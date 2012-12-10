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

import static org.apache.openejb.jee.AssemblyDescriptor$JAXB.readAssemblyDescriptor;
import static org.apache.openejb.jee.AssemblyDescriptor$JAXB.writeAssemblyDescriptor;
import static org.apache.openejb.jee.EntityBean$JAXB.readEntityBean;
import static org.apache.openejb.jee.EntityBean$JAXB.writeEntityBean;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Interceptors$JAXB.readInterceptors;
import static org.apache.openejb.jee.Interceptors$JAXB.writeInterceptors;
import static org.apache.openejb.jee.MessageDrivenBean$JAXB.readMessageDrivenBean;
import static org.apache.openejb.jee.MessageDrivenBean$JAXB.writeMessageDrivenBean;
import static org.apache.openejb.jee.Relationships$JAXB.readRelationships;
import static org.apache.openejb.jee.Relationships$JAXB.writeRelationships;
import static org.apache.openejb.jee.SessionBean$JAXB.readSessionBean;
import static org.apache.openejb.jee.SessionBean$JAXB.writeSessionBean;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class EjbJar$JAXB
        extends JAXBObject<EjbJar> {


    public EjbJar$JAXB() {
        super(EjbJar.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "ejb-jar".intern()), new QName("http://java.sun.com/xml/ns/javaee".intern(), "ejb-jarType".intern()), Text$JAXB.class, Icon$JAXB.class, MessageDrivenBean$JAXB.class, SessionBean$JAXB.class, EntityBean$JAXB.class, Interceptors$JAXB.class, Relationships$JAXB.class, AssemblyDescriptor$JAXB.class);
    }

    public static EjbJar readEjbJar(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeEjbJar(XoXMLStreamWriter writer, EjbJar ejbJar, RuntimeContext context)
            throws Exception {
        _write(writer, ejbJar, context);
    }

    public void write(XoXMLStreamWriter writer, EjbJar ejbJar, RuntimeContext context)
            throws Exception {
        _write(writer, ejbJar, context);
    }

    public final static EjbJar _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        EjbJar ejbJar = new EjbJar();
        context.beforeUnmarshal(ejbJar, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("ejb-jarType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, EjbJar.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, ejbJar);
                ejbJar.id = id;
            } else if (("metadata-complete" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: metadataComplete
                Boolean metadataComplete = ("1".equals(attribute.getValue()) || "true".equals(attribute.getValue()));
                ejbJar.metadataComplete = metadataComplete;
            } else if (("version" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: version
                ejbJar.version = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("", "metadata-complete"), new QName("", "version"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("module-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: moduleName
                String moduleNameRaw = elementReader.getElementAsString();

                String moduleName;
                try {
                    moduleName = Adapters.collapsedStringAdapterAdapter.unmarshal(moduleNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                ejbJar.moduleName = moduleName;
            } else if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = ejbJar.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("enterprise-beans" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT WRAPPER: enterpriseBeans
                _readEnterpriseBeans(elementReader, context, ejbJar);
            } else if (("interceptors" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: interceptors
                Interceptors interceptors = readInterceptors(elementReader, context);
                ejbJar.interceptors = interceptors;
            } else if (("relationships" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: relationships
                Relationships relationships = readRelationships(elementReader, context);
                ejbJar.relationships = relationships;
            } else if (("assembly-descriptor" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: assemblyDescriptor
                AssemblyDescriptor assemblyDescriptor = readAssemblyDescriptor(elementReader, context);
                ejbJar.assemblyDescriptor = assemblyDescriptor;
            } else if (("ejb-client-jar" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbClientJar
                String ejbClientJarRaw = elementReader.getElementAsString();

                String ejbClientJar;
                try {
                    ejbClientJar = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbClientJarRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                ejbJar.ejbClientJar = ejbClientJar;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "module-name"), new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "enterprise-beans"), new QName("http://java.sun.com/xml/ns/javaee", "interceptors"), new QName("http://java.sun.com/xml/ns/javaee", "relationships"), new QName("http://java.sun.com/xml/ns/javaee", "assembly-descriptor"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-client-jar"));
            }
        }
        if (descriptions != null) {
            try {
                ejbJar.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, EjbJar.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                ejbJar.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (Exception e) {
                context.setterError(reader, EjbJar.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            ejbJar.icon = icon;
        }

        context.afterUnmarshal(ejbJar, LifecycleCallback.NONE);

        return ejbJar;
    }

    public final EjbJar read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _readEnterpriseBeans(XoXMLStreamReader reader, RuntimeContext context, EjbJar ejbJar)
            throws Exception {
        ArrayList<EnterpriseBean> enterpriseBeans = null;

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("message-driven" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: enterpriseBeans
                org.apache.openejb.jee.MessageDrivenBean enterpriseBeansItem = readMessageDrivenBean(elementReader, context);
                if (enterpriseBeans == null) {
                    enterpriseBeans = new ArrayList<EnterpriseBean>();
                }
                enterpriseBeans.add(enterpriseBeansItem);
            } else if (("session" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: enterpriseBeans
                org.apache.openejb.jee.SessionBean enterpriseBeansItem1 = readSessionBean(elementReader, context);
                if (enterpriseBeans == null) {
                    enterpriseBeans = new ArrayList<EnterpriseBean>();
                }
                enterpriseBeans.add(enterpriseBeansItem1);
            } else if (("entity" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: enterpriseBeans
                org.apache.openejb.jee.EntityBean enterpriseBeansItem2 = readEntityBean(elementReader, context);
                if (enterpriseBeans == null) {
                    enterpriseBeans = new ArrayList<EnterpriseBean>();
                }
                enterpriseBeans.add(enterpriseBeansItem2);
            }
        }
        if (enterpriseBeans != null) {
            try {
                ejbJar.setEnterpriseBeans(enterpriseBeans.toArray(new EnterpriseBean[enterpriseBeans.size()]));
            } catch (Exception e) {
                context.setterError(reader, EjbJar.class, "setEnterpriseBeans", EnterpriseBean[].class, e);
            }
        }
    }

    public final static void _write(XoXMLStreamWriter writer, EjbJar ejbJar, RuntimeContext context)
            throws Exception {
        if (ejbJar == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (EjbJar.class != ejbJar.getClass()) {
            context.unexpectedSubclass(writer, ejbJar, EjbJar.class);
            return;
        }

        context.beforeMarshal(ejbJar, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = ejbJar.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(ejbJar, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: metadataComplete
        Boolean metadataComplete = ejbJar.metadataComplete;
        if (metadataComplete != null) {
            writer.writeAttribute("", "", "metadata-complete", Boolean.toString(metadataComplete));
        }

        // ATTRIBUTE: version
        String versionRaw = ejbJar.version;
        if (versionRaw != null) {
            String version = null;
            try {
                version = Adapters.collapsedStringAdapterAdapter.marshal(versionRaw);
            } catch (Exception e) {
                context.xmlAdapterError(ejbJar, "version", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "version", version);
        }

        // ELEMENT: moduleName
        String moduleNameRaw = ejbJar.moduleName;
        String moduleName = null;
        try {
            moduleName = Adapters.collapsedStringAdapterAdapter.marshal(moduleNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(ejbJar, "moduleName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (moduleName != null) {
            writer.writeStartElement(prefix, "module-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(moduleName);
            writer.writeEndElement();
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = ejbJar.getDescriptions();
        } catch (Exception e) {
            context.getterError(ejbJar, "descriptions", EjbJar.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbJar, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = ejbJar.getDisplayNames();
        } catch (Exception e) {
            context.getterError(ejbJar, "displayNames", EjbJar.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbJar, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = ejbJar.icon;
        if (icon != null) {
            for (Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbJar, "icon");
                }
            }
        }

        // ELEMENT: enterpriseBeans
        EnterpriseBean[] enterpriseBeans = null;
        try {
            enterpriseBeans = ejbJar.getEnterpriseBeans();
        } catch (Exception e) {
            context.getterError(ejbJar, "enterpriseBeans", EjbJar.class, "getEnterpriseBeans", e);
        }
        writer.writeStartElement(prefix, "enterprise-beans", "http://java.sun.com/xml/ns/javaee");
        if (enterpriseBeans != null) {
            for (EnterpriseBean enterpriseBeansItem : enterpriseBeans) {
                if (enterpriseBeansItem instanceof org.apache.openejb.jee.EntityBean) {
                    org.apache.openejb.jee.EntityBean EntityBean = ((org.apache.openejb.jee.EntityBean) enterpriseBeansItem);
                    writer.writeStartElement(prefix, "entity", "http://java.sun.com/xml/ns/javaee");
                    writeEntityBean(writer, EntityBean, context);
                    writer.writeEndElement();
                } else if (enterpriseBeansItem instanceof org.apache.openejb.jee.SessionBean) {
                    org.apache.openejb.jee.SessionBean SessionBean = ((org.apache.openejb.jee.SessionBean) enterpriseBeansItem);
                    writer.writeStartElement(prefix, "session", "http://java.sun.com/xml/ns/javaee");
                    writeSessionBean(writer, SessionBean, context);
                    writer.writeEndElement();
                } else if (enterpriseBeansItem instanceof org.apache.openejb.jee.MessageDrivenBean) {
                    org.apache.openejb.jee.MessageDrivenBean MessageDrivenBean = ((org.apache.openejb.jee.MessageDrivenBean) enterpriseBeansItem);
                    writer.writeStartElement(prefix, "message-driven", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDrivenBean(writer, MessageDrivenBean, context);
                    writer.writeEndElement();
                } else if (enterpriseBeansItem == null) {
                    context.unexpectedNullValue(ejbJar, "enterpriseBeans");
                } else {
                    context.unexpectedElementType(writer, ejbJar, "enterpriseBeans", enterpriseBeansItem, org.apache.openejb.jee.EntityBean.class, org.apache.openejb.jee.SessionBean.class, org.apache.openejb.jee.MessageDrivenBean.class);
                }
            }
        }
        writer.writeEndElement();

        // ELEMENT: interceptors
        Interceptors interceptors = ejbJar.interceptors;
        if (interceptors != null) {
            writer.writeStartElement(prefix, "interceptors", "http://java.sun.com/xml/ns/javaee");
            writeInterceptors(writer, interceptors, context);
            writer.writeEndElement();
        }

        // ELEMENT: relationships
        Relationships relationships = ejbJar.relationships;
        if (relationships != null) {
            writer.writeStartElement(prefix, "relationships", "http://java.sun.com/xml/ns/javaee");
            writeRelationships(writer, relationships, context);
            writer.writeEndElement();
        }

        // ELEMENT: assemblyDescriptor
        AssemblyDescriptor assemblyDescriptor = ejbJar.assemblyDescriptor;
        if (assemblyDescriptor != null) {
            writer.writeStartElement(prefix, "assembly-descriptor", "http://java.sun.com/xml/ns/javaee");
            writeAssemblyDescriptor(writer, assemblyDescriptor, context);
            writer.writeEndElement();
        }

        // ELEMENT: ejbClientJar
        String ejbClientJarRaw = ejbJar.ejbClientJar;
        String ejbClientJar = null;
        try {
            ejbClientJar = Adapters.collapsedStringAdapterAdapter.marshal(ejbClientJarRaw);
        } catch (Exception e) {
            context.xmlAdapterError(ejbJar, "ejbClientJar", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbClientJar != null) {
            writer.writeStartElement(prefix, "ejb-client-jar", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbClientJar);
            writer.writeEndElement();
        }

        context.afterMarshal(ejbJar, LifecycleCallback.NONE);
    }

}
