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


import static org.apache.openejb.jee.Description$JAXB.readDescription;
import static org.apache.openejb.jee.Description$JAXB.writeDescription;
import static org.apache.openejb.jee.DisplayName$JAXB.readDisplayName;
import static org.apache.openejb.jee.DisplayName$JAXB.writeDisplayName;
import static org.apache.openejb.jee.FacesConfigIf$JAXB.readFacesConfigIf;
import static org.apache.openejb.jee.FacesConfigIf$JAXB.writeFacesConfigIf;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.XmlString$JAXB.readXmlString;
import static org.apache.openejb.jee.XmlString$JAXB.writeXmlString;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinitionSwitchCase$JAXB
    extends JAXBObject<FacesConfigFlowDefinitionSwitchCase>
{


    public FacesConfigFlowDefinitionSwitchCase$JAXB() {
        super(FacesConfigFlowDefinitionSwitchCase.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definition-switch-caseType".intern()), Description$JAXB.class, DisplayName$JAXB.class, Icon$JAXB.class, FacesConfigIf$JAXB.class, XmlString$JAXB.class);
    }

    public static FacesConfigFlowDefinitionSwitchCase readFacesConfigFlowDefinitionSwitchCase(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinitionSwitchCase(XoXMLStreamWriter writer, FacesConfigFlowDefinitionSwitchCase facesConfigFlowDefinitionSwitchCase, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionSwitchCase, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionSwitchCase facesConfigFlowDefinitionSwitchCase, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionSwitchCase, context);
    }

    public static final FacesConfigFlowDefinitionSwitchCase _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinitionSwitchCase facesConfigFlowDefinitionSwitchCase = new FacesConfigFlowDefinitionSwitchCase();
        context.beforeUnmarshal(facesConfigFlowDefinitionSwitchCase, LifecycleCallback.NONE);

        List<Description> description = null;
        List<DisplayName> displayName = null;
        List<Icon> icon = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definition-switch-caseType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinitionSwitchCase.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesConfigFlowDefinitionSwitchCase);
                facesConfigFlowDefinitionSwitchCase.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: description
                Description descriptionItem = readDescription(elementReader, context);
                if (description == null) {
                    description = facesConfigFlowDefinitionSwitchCase.description;
                    if (description!= null) {
                        description.clear();
                    } else {
                        description = new ArrayList<>();
                    }
                }
                description.add(descriptionItem);
            } else if (("display-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayName
                DisplayName displayNameItem = readDisplayName(elementReader, context);
                if (displayName == null) {
                    displayName = facesConfigFlowDefinitionSwitchCase.displayName;
                    if (displayName!= null) {
                        displayName.clear();
                    } else {
                        displayName = new ArrayList<>();
                    }
                }
                displayName.add(displayNameItem);
            } else if (("icon" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = facesConfigFlowDefinitionSwitchCase.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new ArrayList<>();
                    }
                }
                icon.add(iconItem);
            } else if (("if" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: _if
                FacesConfigIf _if = readFacesConfigIf(elementReader, context);
                facesConfigFlowDefinitionSwitchCase._if = _if;
            } else if (("from-outcome" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: fromOutcome
                XmlString fromOutcome = readXmlString(elementReader, context);
                facesConfigFlowDefinitionSwitchCase.fromOutcome = fromOutcome;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "if"), new QName("http://java.sun.com/xml/ns/javaee", "from-outcome"));
            }
        }
        if (description!= null) {
            facesConfigFlowDefinitionSwitchCase.description = description;
        }
        if (displayName!= null) {
            facesConfigFlowDefinitionSwitchCase.displayName = displayName;
        }
        if (icon!= null) {
            facesConfigFlowDefinitionSwitchCase.icon = icon;
        }

        context.afterUnmarshal(facesConfigFlowDefinitionSwitchCase, LifecycleCallback.NONE);

        return facesConfigFlowDefinitionSwitchCase;
    }

    public final FacesConfigFlowDefinitionSwitchCase read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionSwitchCase facesConfigFlowDefinitionSwitchCase, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinitionSwitchCase == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesConfigFlowDefinitionSwitchCase.class!= facesConfigFlowDefinitionSwitchCase.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinitionSwitchCase, FacesConfigFlowDefinitionSwitchCase.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinitionSwitchCase, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesConfigFlowDefinitionSwitchCase.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConfigFlowDefinitionSwitchCase, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: description
        List<Description> description = facesConfigFlowDefinitionSwitchCase.description;
        if (description!= null) {
            for (Description descriptionItem: description) {
                writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                if (descriptionItem!= null) {
                    writeDescription(writer, descriptionItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: displayName
        List<DisplayName> displayName = facesConfigFlowDefinitionSwitchCase.displayName;
        if (displayName!= null) {
            for (DisplayName displayNameItem: displayName) {
                if (displayNameItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeDisplayName(writer, displayNameItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: icon
        List<Icon> icon = facesConfigFlowDefinitionSwitchCase.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                if (iconItem!= null) {
                    writeIcon(writer, iconItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: _if
        FacesConfigIf _if = facesConfigFlowDefinitionSwitchCase._if;
        if (_if!= null) {
            writer.writeStartElement(prefix, "if", "http://java.sun.com/xml/ns/javaee");
            writeFacesConfigIf(writer, _if, context);
            writer.writeEndElement();
        }

        // ELEMENT: fromOutcome
        XmlString fromOutcome = facesConfigFlowDefinitionSwitchCase.fromOutcome;
        if (fromOutcome!= null) {
            writer.writeStartElement(prefix, "from-outcome", "http://java.sun.com/xml/ns/javaee");
            writeXmlString(writer, fromOutcome, context);
            writer.writeEndElement();
        }

        context.afterMarshal(facesConfigFlowDefinitionSwitchCase, LifecycleCallback.NONE);
    }

}
