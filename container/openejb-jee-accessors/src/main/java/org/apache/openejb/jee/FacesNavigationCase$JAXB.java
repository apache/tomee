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
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.FacesFromAction$JAXB.readFacesFromAction;
import static org.apache.openejb.jee.FacesFromAction$JAXB.writeFacesFromAction;
import static org.apache.openejb.jee.FacesRedirect$JAXB.readFacesRedirect;
import static org.apache.openejb.jee.FacesRedirect$JAXB.writeFacesRedirect;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class FacesNavigationCase$JAXB
    extends JAXBObject<FacesNavigationCase>
{


    public FacesNavigationCase$JAXB() {
        super(FacesNavigationCase.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-navigation-caseType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesFromAction$JAXB.class, FacesRedirect$JAXB.class);
    }

    public static FacesNavigationCase readFacesNavigationCase(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesNavigationCase(XoXMLStreamWriter writer, FacesNavigationCase facesNavigationCase, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesNavigationCase, context);
    }

    public void write(XoXMLStreamWriter writer, FacesNavigationCase facesNavigationCase, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesNavigationCase, context);
    }

    public static final FacesNavigationCase _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesNavigationCase facesNavigationCase = new FacesNavigationCase();
        context.beforeUnmarshal(facesNavigationCase, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-navigation-caseType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesNavigationCase.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesNavigationCase);
                facesNavigationCase.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = facesNavigationCase.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<>();
                    }
                }
                icon.add(iconItem);
            } else if (("from-action" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: fromAction
                FacesFromAction fromAction = readFacesFromAction(elementReader, context);
                facesNavigationCase.fromAction = fromAction;
            } else if (("from-outcome" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: fromOutcome
                String fromOutcomeRaw = elementReader.getElementText();

                String fromOutcome;
                try {
                    fromOutcome = Adapters.collapsedStringAdapterAdapter.unmarshal(fromOutcomeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesNavigationCase.fromOutcome = fromOutcome;
            } else if (("if" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: _if
                String _ifRaw = elementReader.getElementText();

                String _if;
                try {
                    _if = Adapters.collapsedStringAdapterAdapter.unmarshal(_ifRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesNavigationCase._if = _if;
            } else if (("to-view-id" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: toViewId
                String toViewIdRaw = elementReader.getElementText();

                String toViewId;
                try {
                    toViewId = Adapters.collapsedStringAdapterAdapter.unmarshal(toViewIdRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesNavigationCase.toViewId = toViewId;
            } else if (("redirect" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: redirect
                FacesRedirect redirect = readFacesRedirect(elementReader, context);
                facesNavigationCase.redirect = redirect;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "from-action"), new QName("http://java.sun.com/xml/ns/javaee", "from-outcome"), new QName("http://java.sun.com/xml/ns/javaee", "if"), new QName("http://java.sun.com/xml/ns/javaee", "to-view-id"), new QName("http://java.sun.com/xml/ns/javaee", "redirect"));
            }
        }
        if (descriptions!= null) {
            try {
                facesNavigationCase.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, FacesNavigationCase.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames!= null) {
            try {
                facesNavigationCase.setDisplayNames(displayNames.toArray(new Text[displayNames.size()] ));
            } catch (Exception e) {
                context.setterError(reader, FacesNavigationCase.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon!= null) {
            facesNavigationCase.icon = icon;
        }

        context.afterUnmarshal(facesNavigationCase, LifecycleCallback.NONE);

        return facesNavigationCase;
    }

    public final FacesNavigationCase read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesNavigationCase facesNavigationCase, RuntimeContext context)
        throws Exception
    {
        if (facesNavigationCase == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesNavigationCase.class!= facesNavigationCase.getClass()) {
            context.unexpectedSubclass(writer, facesNavigationCase, FacesNavigationCase.class);
            return ;
        }

        context.beforeMarshal(facesNavigationCase, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesNavigationCase.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesNavigationCase, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesNavigationCase.getDescriptions();
        } catch (Exception e) {
            context.getterError(facesNavigationCase, "descriptions", FacesNavigationCase.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesNavigationCase, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesNavigationCase.getDisplayNames();
        } catch (Exception e) {
            context.getterError(facesNavigationCase, "displayNames", FacesNavigationCase.class, "getDisplayNames", e);
        }
        if (displayNames!= null) {
            for (Text displayNamesItem: displayNames) {
                if (displayNamesItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesNavigationCase, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = facesNavigationCase.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                if (iconItem!= null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesNavigationCase, "icon");
                }
            }
        }

        // ELEMENT: fromAction
        FacesFromAction fromAction = facesNavigationCase.fromAction;
        if (fromAction!= null) {
            writer.writeStartElement(prefix, "from-action", "http://java.sun.com/xml/ns/javaee");
            writeFacesFromAction(writer, fromAction, context);
            writer.writeEndElement();
        }

        // ELEMENT: fromOutcome
        String fromOutcomeRaw = facesNavigationCase.fromOutcome;
        String fromOutcome = null;
        try {
            fromOutcome = Adapters.collapsedStringAdapterAdapter.marshal(fromOutcomeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesNavigationCase, "fromOutcome", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (fromOutcome!= null) {
            writer.writeStartElement(prefix, "from-outcome", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(fromOutcome);
            writer.writeEndElement();
        }

        // ELEMENT: _if
        String _ifRaw = facesNavigationCase._if;
        String _if = null;
        try {
            _if = Adapters.collapsedStringAdapterAdapter.marshal(_ifRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesNavigationCase, "_if", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (_if!= null) {
            writer.writeStartElement(prefix, "if", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(_if);
            writer.writeEndElement();
        }

        // ELEMENT: toViewId
        String toViewIdRaw = facesNavigationCase.toViewId;
        String toViewId = null;
        try {
            toViewId = Adapters.collapsedStringAdapterAdapter.marshal(toViewIdRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesNavigationCase, "toViewId", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (toViewId!= null) {
            writer.writeStartElement(prefix, "to-view-id", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toViewId);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesNavigationCase, "toViewId");
        }

        // ELEMENT: redirect
        FacesRedirect redirect = facesNavigationCase.redirect;
        if (redirect!= null) {
            writer.writeStartElement(prefix, "redirect", "http://java.sun.com/xml/ns/javaee");
            writeFacesRedirect(writer, redirect, context);
            writer.writeEndElement();
        }

        context.afterMarshal(facesNavigationCase, LifecycleCallback.NONE);
    }

}
