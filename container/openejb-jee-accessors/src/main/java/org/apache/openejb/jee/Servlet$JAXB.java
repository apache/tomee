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

import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.MultipartConfig$JAXB.readMultipartConfig;
import static org.apache.openejb.jee.MultipartConfig$JAXB.writeMultipartConfig;
import static org.apache.openejb.jee.ParamValue$JAXB.readParamValue;
import static org.apache.openejb.jee.ParamValue$JAXB.writeParamValue;
import static org.apache.openejb.jee.RunAs$JAXB.readRunAs;
import static org.apache.openejb.jee.RunAs$JAXB.writeRunAs;
import static org.apache.openejb.jee.SecurityRoleRef$JAXB.readSecurityRoleRef;
import static org.apache.openejb.jee.SecurityRoleRef$JAXB.writeSecurityRoleRef;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Servlet$JAXB
    extends JAXBObject<Servlet> {


    public Servlet$JAXB() {
        super(Servlet.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "servletType".intern()), Text$JAXB.class, Icon$JAXB.class, ParamValue$JAXB.class, RunAs$JAXB.class, SecurityRoleRef$JAXB.class, MultipartConfig$JAXB.class);
    }

    public static Servlet readServlet(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeServlet(final XoXMLStreamWriter writer, final Servlet servlet, final RuntimeContext context)
        throws Exception {
        _write(writer, servlet, context);
    }

    public void write(final XoXMLStreamWriter writer, final Servlet servlet, final RuntimeContext context)
        throws Exception {
        _write(writer, servlet, context);
    }

    public final static Servlet _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Servlet servlet = new Servlet();
        context.beforeUnmarshal(servlet, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<ParamValue> initParam = null;
        List<SecurityRoleRef> securityRoleRef = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("servletType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Servlet.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, servlet);
                servlet.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                final Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                final Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                final Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = servlet.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("servlet-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: servletName
                final String servletNameRaw = elementReader.getElementAsString();

                final String servletName;
                try {
                    servletName = Adapters.collapsedStringAdapterAdapter.unmarshal(servletNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                servlet.servletName = servletName;
            } else if (("servlet-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: servletClass
                final String servletClassRaw = elementReader.getElementAsString();

                final String servletClass;
                try {
                    servletClass = Adapters.collapsedStringAdapterAdapter.unmarshal(servletClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                servlet.servletClass = servletClass;
            } else if (("jsp-file" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: jspFile
                final String jspFileRaw = elementReader.getElementAsString();

                final String jspFile;
                try {
                    jspFile = Adapters.collapsedStringAdapterAdapter.unmarshal(jspFileRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                servlet.jspFile = jspFile;
            } else if (("init-param" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initParam
                final ParamValue initParamItem = readParamValue(elementReader, context);
                if (initParam == null) {
                    initParam = servlet.initParam;
                    if (initParam != null) {
                        initParam.clear();
                    } else {
                        initParam = new ArrayList<ParamValue>();
                    }
                }
                initParam.add(initParamItem);
            } else if (("load-on-startup" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: loadOnStartup
                final String loadOnStartupRaw = elementReader.getElementAsString();

                final Integer loadOnStartup;
                try {
                    loadOnStartup = Adapters.loadOnStartupAdapterAdapter.unmarshal(loadOnStartupRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, LoadOnStartupAdapter.class, Integer.class, Integer.class, e);
                    continue;
                }

                servlet.loadOnStartup = loadOnStartup;
            } else if (("enabled" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: enabled
                final Boolean enabled = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                servlet.enabled = enabled;
            } else if (("async-supported" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: asyncSupported
                final Boolean asyncSupported = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                servlet.asyncSupported = asyncSupported;
            } else if (("run-as" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: runAs
                final RunAs runAs = readRunAs(elementReader, context);
                servlet.runAs = runAs;
            } else if (("security-role-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityRoleRef
                final SecurityRoleRef securityRoleRefItem = readSecurityRoleRef(elementReader, context);
                if (securityRoleRef == null) {
                    securityRoleRef = servlet.securityRoleRef;
                    if (securityRoleRef != null) {
                        securityRoleRef.clear();
                    } else {
                        securityRoleRef = new ArrayList<SecurityRoleRef>();
                    }
                }
                securityRoleRef.add(securityRoleRefItem);
            } else if (("multipart-config" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: multipartConfig
                final MultipartConfig multipartConfig = readMultipartConfig(elementReader, context);
                servlet.multipartConfig = multipartConfig;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "servlet-name"), new QName("http://java.sun.com/xml/ns/javaee", "servlet-class"), new QName("http://java.sun.com/xml/ns/javaee", "jsp-file"), new QName("http://java.sun.com/xml/ns/javaee", "init-param"), new QName("http://java.sun.com/xml/ns/javaee", "load-on-startup"), new QName("http://java.sun.com/xml/ns/javaee", "enabled"), new QName("http://java.sun.com/xml/ns/javaee", "async-supported"), new QName("http://java.sun.com/xml/ns/javaee", "run-as"), new QName("http://java.sun.com/xml/ns/javaee", "security-role-ref"), new QName("http://java.sun.com/xml/ns/javaee", "multipart-config"));
            }
        }
        if (descriptions != null) {
            try {
                servlet.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Servlet.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                servlet.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Servlet.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            servlet.icon = icon;
        }
        if (initParam != null) {
            servlet.initParam = initParam;
        }
        if (securityRoleRef != null) {
            servlet.securityRoleRef = securityRoleRef;
        }

        context.afterUnmarshal(servlet, LifecycleCallback.NONE);

        return servlet;
    }

    public final Servlet read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Servlet servlet, RuntimeContext context)
        throws Exception {
        if (servlet == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Servlet.class != servlet.getClass()) {
            context.unexpectedSubclass(writer, servlet, Servlet.class);
            return;
        }

        context.beforeMarshal(servlet, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = servlet.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(servlet, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = servlet.getDescriptions();
        } catch (final Exception e) {
            context.getterError(servlet, "descriptions", Servlet.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(servlet, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = servlet.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(servlet, "displayNames", Servlet.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(servlet, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = servlet.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(servlet, "icon");
                }
            }
        }

        // ELEMENT: servletName
        final String servletNameRaw = servlet.servletName;
        String servletName = null;
        try {
            servletName = Adapters.collapsedStringAdapterAdapter.marshal(servletNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(servlet, "servletName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (servletName != null) {
            writer.writeStartElement(prefix, "servlet-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(servletName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(servlet, "servletName");
        }

        // ELEMENT: servletClass
        final String servletClassRaw = servlet.servletClass;
        String servletClass = null;
        try {
            servletClass = Adapters.collapsedStringAdapterAdapter.marshal(servletClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(servlet, "servletClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (servletClass != null) {
            writer.writeStartElement(prefix, "servlet-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(servletClass);
            writer.writeEndElement();
        }

        // ELEMENT: jspFile
        final String jspFileRaw = servlet.jspFile;
        String jspFile = null;
        try {
            jspFile = Adapters.collapsedStringAdapterAdapter.marshal(jspFileRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(servlet, "jspFile", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (jspFile != null) {
            writer.writeStartElement(prefix, "jsp-file", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(jspFile);
            writer.writeEndElement();
        }

        // ELEMENT: initParam
        final List<ParamValue> initParam = servlet.initParam;
        if (initParam != null) {
            for (final ParamValue initParamItem : initParam) {
                if (initParamItem != null) {
                    writer.writeStartElement(prefix, "init-param", "http://java.sun.com/xml/ns/javaee");
                    writeParamValue(writer, initParamItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: loadOnStartup
        final Integer loadOnStartupRaw = servlet.loadOnStartup;
        String loadOnStartup = null;
        try {
            loadOnStartup = Adapters.loadOnStartupAdapterAdapter.marshal(loadOnStartupRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(servlet, "loadOnStartup", LoadOnStartupAdapter.class, Integer.class, Integer.class, e);
        }
        if (loadOnStartup != null) {
            writer.writeStartElement(prefix, "load-on-startup", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(loadOnStartup);
            writer.writeEndElement();
        }

        // ELEMENT: enabled
        final Boolean enabled = servlet.enabled;
        if (enabled != null) {
            writer.writeStartElement(prefix, "enabled", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(enabled));
            writer.writeEndElement();
        }

        // ELEMENT: asyncSupported
        final Boolean asyncSupported = servlet.asyncSupported;
        if (asyncSupported != null) {
            writer.writeStartElement(prefix, "async-supported", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(asyncSupported));
            writer.writeEndElement();
        }

        // ELEMENT: runAs
        final RunAs runAs = servlet.runAs;
        if (runAs != null) {
            writer.writeStartElement(prefix, "run-as", "http://java.sun.com/xml/ns/javaee");
            writeRunAs(writer, runAs, context);
            writer.writeEndElement();
        }

        // ELEMENT: securityRoleRef
        final List<SecurityRoleRef> securityRoleRef = servlet.securityRoleRef;
        if (securityRoleRef != null) {
            for (final SecurityRoleRef securityRoleRefItem : securityRoleRef) {
                if (securityRoleRefItem != null) {
                    writer.writeStartElement(prefix, "security-role-ref", "http://java.sun.com/xml/ns/javaee");
                    writeSecurityRoleRef(writer, securityRoleRefItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: multipartConfig
        final MultipartConfig multipartConfig = servlet.multipartConfig;
        if (multipartConfig != null) {
            writer.writeStartElement(prefix, "multipart-config", "http://java.sun.com/xml/ns/javaee");
            writeMultipartConfig(writer, multipartConfig, context);
            writer.writeEndElement();
        }

        context.afterMarshal(servlet, LifecycleCallback.NONE);
    }

}
