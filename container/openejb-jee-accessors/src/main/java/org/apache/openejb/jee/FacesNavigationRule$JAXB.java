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

import static org.apache.openejb.jee.FacesNavigationCase$JAXB.readFacesNavigationCase;
import static org.apache.openejb.jee.FacesNavigationCase$JAXB.writeFacesNavigationCase;
import static org.apache.openejb.jee.FacesNavigationRuleExtension$JAXB.readFacesNavigationRuleExtension;
import static org.apache.openejb.jee.FacesNavigationRuleExtension$JAXB.writeFacesNavigationRuleExtension;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class FacesNavigationRule$JAXB
        extends JAXBObject<FacesNavigationRule> {


    public FacesNavigationRule$JAXB() {
        super(FacesNavigationRule.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-navigation-ruleType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesNavigationCase$JAXB.class, FacesNavigationRuleExtension$JAXB.class);
    }

    public static FacesNavigationRule readFacesNavigationRule(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesNavigationRule(XoXMLStreamWriter writer, FacesNavigationRule facesNavigationRule, RuntimeContext context)
            throws Exception {
        _write(writer, facesNavigationRule, context);
    }

    public void write(XoXMLStreamWriter writer, FacesNavigationRule facesNavigationRule, RuntimeContext context)
            throws Exception {
        _write(writer, facesNavigationRule, context);
    }

    public final static FacesNavigationRule _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesNavigationRule facesNavigationRule = new FacesNavigationRule();
        context.beforeUnmarshal(facesNavigationRule, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesNavigationCase> navigationCase = null;
        List<FacesNavigationRuleExtension> navigationRuleExtension = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-navigation-ruleType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesNavigationRule.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesNavigationRule);
                facesNavigationRule.id = id;
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
                    icon = facesNavigationRule.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("from-view-id" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: fromViewId
                String fromViewIdRaw = elementReader.getElementAsString();

                String fromViewId;
                try {
                    fromViewId = Adapters.collapsedStringAdapterAdapter.unmarshal(fromViewIdRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesNavigationRule.fromViewId = fromViewId;
            } else if (("navigation-case" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: navigationCase
                FacesNavigationCase navigationCaseItem = readFacesNavigationCase(elementReader, context);
                if (navigationCase == null) {
                    navigationCase = facesNavigationRule.navigationCase;
                    if (navigationCase != null) {
                        navigationCase.clear();
                    } else {
                        navigationCase = new ArrayList<FacesNavigationCase>();
                    }
                }
                navigationCase.add(navigationCaseItem);
            } else if (("navigation-rule-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: navigationRuleExtension
                FacesNavigationRuleExtension navigationRuleExtensionItem = readFacesNavigationRuleExtension(elementReader, context);
                if (navigationRuleExtension == null) {
                    navigationRuleExtension = facesNavigationRule.navigationRuleExtension;
                    if (navigationRuleExtension != null) {
                        navigationRuleExtension.clear();
                    } else {
                        navigationRuleExtension = new ArrayList<FacesNavigationRuleExtension>();
                    }
                }
                navigationRuleExtension.add(navigationRuleExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "from-view-id"), new QName("http://java.sun.com/xml/ns/javaee", "navigation-case"), new QName("http://java.sun.com/xml/ns/javaee", "navigation-rule-extension"));
            }
        }
        if (descriptions != null) {
            try {
                facesNavigationRule.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesNavigationRule.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesNavigationRule.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesNavigationRule.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesNavigationRule.icon = icon;
        }
        if (navigationCase != null) {
            facesNavigationRule.navigationCase = navigationCase;
        }
        if (navigationRuleExtension != null) {
            facesNavigationRule.navigationRuleExtension = navigationRuleExtension;
        }

        context.afterUnmarshal(facesNavigationRule, LifecycleCallback.NONE);

        return facesNavigationRule;
    }

    public final FacesNavigationRule read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesNavigationRule facesNavigationRule, RuntimeContext context)
            throws Exception {
        if (facesNavigationRule == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesNavigationRule.class != facesNavigationRule.getClass()) {
            context.unexpectedSubclass(writer, facesNavigationRule, FacesNavigationRule.class);
            return;
        }

        context.beforeMarshal(facesNavigationRule, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesNavigationRule.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesNavigationRule, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesNavigationRule.getDescriptions();
        } catch (Exception e) {
            context.getterError(facesNavigationRule, "descriptions", FacesNavigationRule.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesNavigationRule, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesNavigationRule.getDisplayNames();
        } catch (Exception e) {
            context.getterError(facesNavigationRule, "displayNames", FacesNavigationRule.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesNavigationRule, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = facesNavigationRule.icon;
        if (icon != null) {
            for (Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesNavigationRule, "icon");
                }
            }
        }

        // ELEMENT: fromViewId
        String fromViewIdRaw = facesNavigationRule.fromViewId;
        String fromViewId = null;
        try {
            fromViewId = Adapters.collapsedStringAdapterAdapter.marshal(fromViewIdRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesNavigationRule, "fromViewId", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (fromViewId != null) {
            writer.writeStartElement(prefix, "from-view-id", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(fromViewId);
            writer.writeEndElement();
        }

        // ELEMENT: navigationCase
        List<FacesNavigationCase> navigationCase = facesNavigationRule.navigationCase;
        if (navigationCase != null) {
            for (FacesNavigationCase navigationCaseItem : navigationCase) {
                if (navigationCaseItem != null) {
                    writer.writeStartElement(prefix, "navigation-case", "http://java.sun.com/xml/ns/javaee");
                    writeFacesNavigationCase(writer, navigationCaseItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: navigationRuleExtension
        List<FacesNavigationRuleExtension> navigationRuleExtension = facesNavigationRule.navigationRuleExtension;
        if (navigationRuleExtension != null) {
            for (FacesNavigationRuleExtension navigationRuleExtensionItem : navigationRuleExtension) {
                if (navigationRuleExtensionItem != null) {
                    writer.writeStartElement(prefix, "navigation-rule-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesNavigationRuleExtension(writer, navigationRuleExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesNavigationRule, LifecycleCallback.NONE);
    }

}
