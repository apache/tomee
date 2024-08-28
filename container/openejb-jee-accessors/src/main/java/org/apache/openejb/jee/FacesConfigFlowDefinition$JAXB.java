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
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFacesMethodCall$JAXB.readFacesConfigFlowDefinitionFacesMethodCall;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFacesMethodCall$JAXB.writeFacesConfigFlowDefinitionFacesMethodCall;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFinalizer$JAXB.readFacesConfigFlowDefinitionFinalizer;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFinalizer$JAXB.writeFacesConfigFlowDefinitionFinalizer;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFlowCall$JAXB.readFacesConfigFlowDefinitionFlowCall;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFlowCall$JAXB.writeFacesConfigFlowDefinitionFlowCall;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFlowReturn$JAXB.readFacesConfigFlowDefinitionFlowReturn;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFlowReturn$JAXB.writeFacesConfigFlowDefinitionFlowReturn;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionInboundParameter$JAXB.readFacesConfigFlowDefinitionInboundParameter;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionInboundParameter$JAXB.writeFacesConfigFlowDefinitionInboundParameter;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionInitializer$JAXB.readFacesConfigFlowDefinitionInitializer;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionInitializer$JAXB.writeFacesConfigFlowDefinitionInitializer;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionSwitch$JAXB.readFacesConfigFlowDefinitionSwitch;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionSwitch$JAXB.writeFacesConfigFlowDefinitionSwitch;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionView$JAXB.readFacesConfigFlowDefinitionView;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionView$JAXB.writeFacesConfigFlowDefinitionView;
import static org.apache.openejb.jee.FacesNavigationRule$JAXB.readFacesNavigationRule;
import static org.apache.openejb.jee.FacesNavigationRule$JAXB.writeFacesNavigationRule;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.JavaIdentifier$JAXB.readJavaIdentifier;
import static org.apache.openejb.jee.JavaIdentifier$JAXB.writeJavaIdentifier;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinition$JAXB
    extends JAXBObject<FacesConfigFlowDefinition>
{


    public FacesConfigFlowDefinition$JAXB() {
        super(FacesConfigFlowDefinition.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definitionType".intern()), Description$JAXB.class, DisplayName$JAXB.class, Icon$JAXB.class, JavaIdentifier$JAXB.class, FacesConfigFlowDefinitionView$JAXB.class, FacesConfigFlowDefinitionSwitch$JAXB.class, FacesConfigFlowDefinitionFlowReturn$JAXB.class, FacesNavigationRule$JAXB.class, FacesConfigFlowDefinitionFlowCall$JAXB.class, FacesConfigFlowDefinitionFacesMethodCall$JAXB.class, FacesConfigFlowDefinitionInitializer$JAXB.class, FacesConfigFlowDefinitionFinalizer$JAXB.class, FacesConfigFlowDefinitionInboundParameter$JAXB.class);
    }

    public static FacesConfigFlowDefinition readFacesConfigFlowDefinition(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinition(XoXMLStreamWriter writer, FacesConfigFlowDefinition facesConfigFlowDefinition, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinition, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinition facesConfigFlowDefinition, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinition, context);
    }

    public static final FacesConfigFlowDefinition _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinition facesConfigFlowDefinition = new FacesConfigFlowDefinition();
        context.beforeUnmarshal(facesConfigFlowDefinition, LifecycleCallback.NONE);

        List<Description> description = null;
        List<DisplayName> displayName = null;
        List<Icon> icon = null;
        List<FacesConfigFlowDefinitionView> view = null;
        List<FacesConfigFlowDefinitionSwitch> _switch = null;
        List<FacesConfigFlowDefinitionFlowReturn> flowReturn = null;
        List<FacesNavigationRule> navigationRule = null;
        List<FacesConfigFlowDefinitionFlowCall> flowCall = null;
        List<FacesConfigFlowDefinitionFacesMethodCall> methodCall = null;
        List<FacesConfigFlowDefinitionInboundParameter> inboundParameter = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definitionType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinition.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesConfigFlowDefinition);
                facesConfigFlowDefinition.id = id;
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
                    description = facesConfigFlowDefinition.description;
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
                    displayName = facesConfigFlowDefinition.displayName;
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
                    icon = facesConfigFlowDefinition.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new ArrayList<>();
                    }
                }
                icon.add(iconItem);
            } else if (("start-node" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: startNode
                JavaIdentifier startNode = readJavaIdentifier(elementReader, context);
                facesConfigFlowDefinition.startNode = startNode;
            } else if (("view" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: view
                FacesConfigFlowDefinitionView viewItem = readFacesConfigFlowDefinitionView(elementReader, context);
                if (view == null) {
                    view = facesConfigFlowDefinition.view;
                    if (view!= null) {
                        view.clear();
                    } else {
                        view = new ArrayList<>();
                    }
                }
                view.add(viewItem);
            } else if (("switch" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: _switch
                FacesConfigFlowDefinitionSwitch _switchItem = readFacesConfigFlowDefinitionSwitch(elementReader, context);
                if (_switch == null) {
                    _switch = facesConfigFlowDefinition._switch;
                    if (_switch!= null) {
                        _switch.clear();
                    } else {
                        _switch = new ArrayList<>();
                    }
                }
                _switch.add(_switchItem);
            } else if (("flow-return" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: flowReturn
                FacesConfigFlowDefinitionFlowReturn flowReturnItem = readFacesConfigFlowDefinitionFlowReturn(elementReader, context);
                if (flowReturn == null) {
                    flowReturn = facesConfigFlowDefinition.flowReturn;
                    if (flowReturn!= null) {
                        flowReturn.clear();
                    } else {
                        flowReturn = new ArrayList<>();
                    }
                }
                flowReturn.add(flowReturnItem);
            } else if (("navigation-rule" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: navigationRule
                FacesNavigationRule navigationRuleItem = readFacesNavigationRule(elementReader, context);
                if (navigationRule == null) {
                    navigationRule = facesConfigFlowDefinition.navigationRule;
                    if (navigationRule!= null) {
                        navigationRule.clear();
                    } else {
                        navigationRule = new ArrayList<>();
                    }
                }
                navigationRule.add(navigationRuleItem);
            } else if (("flow-call" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: flowCall
                FacesConfigFlowDefinitionFlowCall flowCallItem = readFacesConfigFlowDefinitionFlowCall(elementReader, context);
                if (flowCall == null) {
                    flowCall = facesConfigFlowDefinition.flowCall;
                    if (flowCall!= null) {
                        flowCall.clear();
                    } else {
                        flowCall = new ArrayList<>();
                    }
                }
                flowCall.add(flowCallItem);
            } else if (("method-call" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodCall
                FacesConfigFlowDefinitionFacesMethodCall methodCallItem = readFacesConfigFlowDefinitionFacesMethodCall(elementReader, context);
                if (methodCall == null) {
                    methodCall = facesConfigFlowDefinition.methodCall;
                    if (methodCall!= null) {
                        methodCall.clear();
                    } else {
                        methodCall = new ArrayList<>();
                    }
                }
                methodCall.add(methodCallItem);
            } else if (("initializer" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initializer
                FacesConfigFlowDefinitionInitializer initializer = readFacesConfigFlowDefinitionInitializer(elementReader, context);
                facesConfigFlowDefinition.initializer = initializer;
            } else if (("finalizer" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: finalizer
                FacesConfigFlowDefinitionFinalizer finalizer = readFacesConfigFlowDefinitionFinalizer(elementReader, context);
                facesConfigFlowDefinition.finalizer = finalizer;
            } else if (("inbound-parameter" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: inboundParameter
                FacesConfigFlowDefinitionInboundParameter inboundParameterItem = readFacesConfigFlowDefinitionInboundParameter(elementReader, context);
                if (inboundParameter == null) {
                    inboundParameter = facesConfigFlowDefinition.inboundParameter;
                    if (inboundParameter!= null) {
                        inboundParameter.clear();
                    } else {
                        inboundParameter = new ArrayList<>();
                    }
                }
                inboundParameter.add(inboundParameterItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "start-node"), new QName("http://java.sun.com/xml/ns/javaee", "view"), new QName("http://java.sun.com/xml/ns/javaee", "switch"), new QName("http://java.sun.com/xml/ns/javaee", "flow-return"), new QName("http://java.sun.com/xml/ns/javaee", "navigation-rule"), new QName("http://java.sun.com/xml/ns/javaee", "flow-call"), new QName("http://java.sun.com/xml/ns/javaee", "method-call"), new QName("http://java.sun.com/xml/ns/javaee", "initializer"), new QName("http://java.sun.com/xml/ns/javaee", "finalizer"), new QName("http://java.sun.com/xml/ns/javaee", "inbound-parameter"));
            }
        }
        if (description!= null) {
            facesConfigFlowDefinition.description = description;
        }
        if (displayName!= null) {
            facesConfigFlowDefinition.displayName = displayName;
        }
        if (icon!= null) {
            facesConfigFlowDefinition.icon = icon;
        }
        if (view!= null) {
            facesConfigFlowDefinition.view = view;
        }
        if (_switch!= null) {
            facesConfigFlowDefinition._switch = _switch;
        }
        if (flowReturn!= null) {
            facesConfigFlowDefinition.flowReturn = flowReturn;
        }
        if (navigationRule!= null) {
            facesConfigFlowDefinition.navigationRule = navigationRule;
        }
        if (flowCall!= null) {
            facesConfigFlowDefinition.flowCall = flowCall;
        }
        if (methodCall!= null) {
            facesConfigFlowDefinition.methodCall = methodCall;
        }
        if (inboundParameter!= null) {
            facesConfigFlowDefinition.inboundParameter = inboundParameter;
        }

        context.afterUnmarshal(facesConfigFlowDefinition, LifecycleCallback.NONE);

        return facesConfigFlowDefinition;
    }

    public final FacesConfigFlowDefinition read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinition facesConfigFlowDefinition, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinition == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesConfigFlowDefinition.class!= facesConfigFlowDefinition.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinition, FacesConfigFlowDefinition.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinition, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesConfigFlowDefinition.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConfigFlowDefinition, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: description
        List<Description> description = facesConfigFlowDefinition.description;
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
        List<DisplayName> displayName = facesConfigFlowDefinition.displayName;
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
        List<Icon> icon = facesConfigFlowDefinition.icon;
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

        // ELEMENT: startNode
        JavaIdentifier startNode = facesConfigFlowDefinition.startNode;
        if (startNode!= null) {
            writer.writeStartElement(prefix, "start-node", "http://java.sun.com/xml/ns/javaee");
            writeJavaIdentifier(writer, startNode, context);
            writer.writeEndElement();
        }

        // ELEMENT: view
        List<FacesConfigFlowDefinitionView> view = facesConfigFlowDefinition.view;
        if (view!= null) {
            for (FacesConfigFlowDefinitionView viewItem: view) {
                writer.writeStartElement(prefix, "view", "http://java.sun.com/xml/ns/javaee");
                if (viewItem!= null) {
                    writeFacesConfigFlowDefinitionView(writer, viewItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: _switch
        List<FacesConfigFlowDefinitionSwitch> _switch = facesConfigFlowDefinition._switch;
        if (_switch!= null) {
            for (FacesConfigFlowDefinitionSwitch _switchItem: _switch) {
                if (_switchItem!= null) {
                    writer.writeStartElement(prefix, "switch", "http://java.sun.com/xml/ns/javaee");
                    writeFacesConfigFlowDefinitionSwitch(writer, _switchItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: flowReturn
        List<FacesConfigFlowDefinitionFlowReturn> flowReturn = facesConfigFlowDefinition.flowReturn;
        if (flowReturn!= null) {
            for (FacesConfigFlowDefinitionFlowReturn flowReturnItem: flowReturn) {
                if (flowReturnItem!= null) {
                    writer.writeStartElement(prefix, "flow-return", "http://java.sun.com/xml/ns/javaee");
                    writeFacesConfigFlowDefinitionFlowReturn(writer, flowReturnItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: navigationRule
        List<FacesNavigationRule> navigationRule = facesConfigFlowDefinition.navigationRule;
        if (navigationRule!= null) {
            for (FacesNavigationRule navigationRuleItem: navigationRule) {
                if (navigationRuleItem!= null) {
                    writer.writeStartElement(prefix, "navigation-rule", "http://java.sun.com/xml/ns/javaee");
                    writeFacesNavigationRule(writer, navigationRuleItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: flowCall
        List<FacesConfigFlowDefinitionFlowCall> flowCall = facesConfigFlowDefinition.flowCall;
        if (flowCall!= null) {
            for (FacesConfigFlowDefinitionFlowCall flowCallItem: flowCall) {
                if (flowCallItem!= null) {
                    writer.writeStartElement(prefix, "flow-call", "http://java.sun.com/xml/ns/javaee");
                    writeFacesConfigFlowDefinitionFlowCall(writer, flowCallItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: methodCall
        List<FacesConfigFlowDefinitionFacesMethodCall> methodCall = facesConfigFlowDefinition.methodCall;
        if (methodCall!= null) {
            for (FacesConfigFlowDefinitionFacesMethodCall methodCallItem: methodCall) {
                if (methodCallItem!= null) {
                    writer.writeStartElement(prefix, "method-call", "http://java.sun.com/xml/ns/javaee");
                    writeFacesConfigFlowDefinitionFacesMethodCall(writer, methodCallItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: initializer
        FacesConfigFlowDefinitionInitializer initializer = facesConfigFlowDefinition.initializer;
        if (initializer!= null) {
            writer.writeStartElement(prefix, "initializer", "http://java.sun.com/xml/ns/javaee");
            writeFacesConfigFlowDefinitionInitializer(writer, initializer, context);
            writer.writeEndElement();
        }

        // ELEMENT: finalizer
        FacesConfigFlowDefinitionFinalizer finalizer = facesConfigFlowDefinition.finalizer;
        if (finalizer!= null) {
            writer.writeStartElement(prefix, "finalizer", "http://java.sun.com/xml/ns/javaee");
            writeFacesConfigFlowDefinitionFinalizer(writer, finalizer, context);
            writer.writeEndElement();
        }

        // ELEMENT: inboundParameter
        List<FacesConfigFlowDefinitionInboundParameter> inboundParameter = facesConfigFlowDefinition.inboundParameter;
        if (inboundParameter!= null) {
            for (FacesConfigFlowDefinitionInboundParameter inboundParameterItem: inboundParameter) {
                if (inboundParameterItem!= null) {
                    writer.writeStartElement(prefix, "inbound-parameter", "http://java.sun.com/xml/ns/javaee");
                    writeFacesConfigFlowDefinitionInboundParameter(writer, inboundParameterItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesConfigFlowDefinition, LifecycleCallback.NONE);
    }

}
