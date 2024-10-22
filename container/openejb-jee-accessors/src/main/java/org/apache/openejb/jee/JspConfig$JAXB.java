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


import static org.apache.openejb.jee.JspPropertyGroup$JAXB.readJspPropertyGroup;
import static org.apache.openejb.jee.JspPropertyGroup$JAXB.writeJspPropertyGroup;
import static org.apache.openejb.jee.Taglib$JAXB.readTaglib;
import static org.apache.openejb.jee.Taglib$JAXB.writeTaglib;

@SuppressWarnings({
    "StringEquality"
})
public class JspConfig$JAXB
    extends JAXBObject<JspConfig>
{


    public JspConfig$JAXB() {
        super(JspConfig.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "jsp-configType".intern()), Taglib$JAXB.class, JspPropertyGroup$JAXB.class);
    }

    public static JspConfig readJspConfig(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeJspConfig(XoXMLStreamWriter writer, JspConfig jspConfig, RuntimeContext context)
        throws Exception
    {
        _write(writer, jspConfig, context);
    }

    public void write(XoXMLStreamWriter writer, JspConfig jspConfig, RuntimeContext context)
        throws Exception
    {
        _write(writer, jspConfig, context);
    }

    public static final JspConfig _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        JspConfig jspConfig = new JspConfig();
        context.beforeUnmarshal(jspConfig, LifecycleCallback.NONE);

        List<Taglib> taglib = null;
        List<JspPropertyGroup> jspPropertyGroup = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("jsp-configType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, JspConfig.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, jspConfig);
                jspConfig.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("taglib" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: taglib
                Taglib taglibItem = readTaglib(elementReader, context);
                if (taglib == null) {
                    taglib = jspConfig.taglib;
                    if (taglib!= null) {
                        taglib.clear();
                    } else {
                        taglib = new ArrayList<>();
                    }
                }
                taglib.add(taglibItem);
            } else if (("jsp-property-group" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: jspPropertyGroup
                JspPropertyGroup jspPropertyGroupItem = readJspPropertyGroup(elementReader, context);
                if (jspPropertyGroup == null) {
                    jspPropertyGroup = jspConfig.jspPropertyGroup;
                    if (jspPropertyGroup!= null) {
                        jspPropertyGroup.clear();
                    } else {
                        jspPropertyGroup = new ArrayList<>();
                    }
                }
                jspPropertyGroup.add(jspPropertyGroupItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "taglib"), new QName("http://java.sun.com/xml/ns/javaee", "jsp-property-group"));
            }
        }
        if (taglib!= null) {
            jspConfig.taglib = taglib;
        }
        if (jspPropertyGroup!= null) {
            jspConfig.jspPropertyGroup = jspPropertyGroup;
        }

        context.afterUnmarshal(jspConfig, LifecycleCallback.NONE);

        return jspConfig;
    }

    public final JspConfig read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, JspConfig jspConfig, RuntimeContext context)
        throws Exception
    {
        if (jspConfig == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (JspConfig.class!= jspConfig.getClass()) {
            context.unexpectedSubclass(writer, jspConfig, JspConfig.class);
            return ;
        }

        context.beforeMarshal(jspConfig, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = jspConfig.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(jspConfig, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: taglib
        List<Taglib> taglib = jspConfig.taglib;
        if (taglib!= null) {
            for (Taglib taglibItem: taglib) {
                writer.writeStartElement(prefix, "taglib", "http://java.sun.com/xml/ns/javaee");
                if (taglibItem!= null) {
                    writeTaglib(writer, taglibItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: jspPropertyGroup
        List<JspPropertyGroup> jspPropertyGroup = jspConfig.jspPropertyGroup;
        if (jspPropertyGroup!= null) {
            for (JspPropertyGroup jspPropertyGroupItem: jspPropertyGroup) {
                if (jspPropertyGroupItem!= null) {
                    writer.writeStartElement(prefix, "jsp-property-group", "http://java.sun.com/xml/ns/javaee");
                    writeJspPropertyGroup(writer, jspPropertyGroupItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(jspConfig, LifecycleCallback.NONE);
    }

}
