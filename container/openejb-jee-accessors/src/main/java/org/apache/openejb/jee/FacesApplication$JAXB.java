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


import static org.apache.openejb.jee.FacesApplicationExtension$JAXB.readFacesApplicationExtension;
import static org.apache.openejb.jee.FacesApplicationExtension$JAXB.writeFacesApplicationExtension;
import static org.apache.openejb.jee.FacesApplicationResourceBundle$JAXB.readFacesApplicationResourceBundle;
import static org.apache.openejb.jee.FacesApplicationResourceBundle$JAXB.writeFacesApplicationResourceBundle;
import static org.apache.openejb.jee.FacesLocaleConfig$JAXB.readFacesLocaleConfig;
import static org.apache.openejb.jee.FacesLocaleConfig$JAXB.writeFacesLocaleConfig;
import static org.apache.openejb.jee.FacesSystemEventListener$JAXB.readFacesSystemEventListener;
import static org.apache.openejb.jee.FacesSystemEventListener$JAXB.writeFacesSystemEventListener;
import static org.apache.openejb.jee.FacesValidator$JAXB.readFacesValidator;
import static org.apache.openejb.jee.FacesValidator$JAXB.writeFacesValidator;

@SuppressWarnings({
    "StringEquality"
})
public class FacesApplication$JAXB
    extends JAXBObject<FacesApplication>
{


    public FacesApplication$JAXB() {
        super(FacesApplication.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-applicationType".intern()), FacesSystemEventListener$JAXB.class, FacesLocaleConfig$JAXB.class, FacesApplicationResourceBundle$JAXB.class, FacesApplicationExtension$JAXB.class, FacesValidator$JAXB.class);
    }

    public static FacesApplication readFacesApplication(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesApplication(XoXMLStreamWriter writer, FacesApplication facesApplication, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesApplication, context);
    }

    public void write(XoXMLStreamWriter writer, FacesApplication facesApplication, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesApplication, context);
    }

    public static final FacesApplication _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesApplication facesApplication = new FacesApplication();
        context.beforeUnmarshal(facesApplication, LifecycleCallback.NONE);

        List<String> actionListener = null;
        List<String> defaultRenderKitId = null;
        List<String> messageBundle = null;
        List<String> navigationHandler = null;
        List<String> viewHandler = null;
        List<String> stateManager = null;
        List<String> elResolver = null;
        List<String> propertyResolver = null;
        List<String> variableResolver = null;
        List<String> resourceHandler = null;
        List<FacesSystemEventListener> systemEventListener = null;
        List<FacesLocaleConfig> localeConfig = null;
        List<FacesApplicationExtension> applicationExtension = null;
        List<FacesValidator> defaultValidators = null;
        List<Object> others = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-applicationType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesApplication.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesApplication);
                facesApplication.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("action-listener" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: actionListener
                String actionListenerItemRaw = elementReader.getElementText();

                String actionListenerItem;
                try {
                    actionListenerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(actionListenerItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (actionListener == null) {
                    actionListener = facesApplication.actionListener;
                    if (actionListener!= null) {
                        actionListener.clear();
                    } else {
                        actionListener = new ArrayList<>();
                    }
                }
                actionListener.add(actionListenerItem);
            } else if (("default-render-kit-id" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultRenderKitId
                String defaultRenderKitIdItemRaw = elementReader.getElementText();

                String defaultRenderKitIdItem;
                try {
                    defaultRenderKitIdItem = Adapters.collapsedStringAdapterAdapter.unmarshal(defaultRenderKitIdItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (defaultRenderKitId == null) {
                    defaultRenderKitId = facesApplication.defaultRenderKitId;
                    if (defaultRenderKitId!= null) {
                        defaultRenderKitId.clear();
                    } else {
                        defaultRenderKitId = new ArrayList<>();
                    }
                }
                defaultRenderKitId.add(defaultRenderKitIdItem);
            } else if (("message-bundle" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageBundle
                String messageBundleItemRaw = elementReader.getElementText();

                String messageBundleItem;
                try {
                    messageBundleItem = Adapters.collapsedStringAdapterAdapter.unmarshal(messageBundleItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (messageBundle == null) {
                    messageBundle = facesApplication.messageBundle;
                    if (messageBundle!= null) {
                        messageBundle.clear();
                    } else {
                        messageBundle = new ArrayList<>();
                    }
                }
                messageBundle.add(messageBundleItem);
            } else if (("navigation-handler" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: navigationHandler
                String navigationHandlerItemRaw = elementReader.getElementText();

                String navigationHandlerItem;
                try {
                    navigationHandlerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(navigationHandlerItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (navigationHandler == null) {
                    navigationHandler = facesApplication.navigationHandler;
                    if (navigationHandler!= null) {
                        navigationHandler.clear();
                    } else {
                        navigationHandler = new ArrayList<>();
                    }
                }
                navigationHandler.add(navigationHandlerItem);
            } else if (("view-handler" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: viewHandler
                String viewHandlerItemRaw = elementReader.getElementText();

                String viewHandlerItem;
                try {
                    viewHandlerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(viewHandlerItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (viewHandler == null) {
                    viewHandler = facesApplication.viewHandler;
                    if (viewHandler!= null) {
                        viewHandler.clear();
                    } else {
                        viewHandler = new ArrayList<>();
                    }
                }
                viewHandler.add(viewHandlerItem);
            } else if (("state-manager" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: stateManager
                String stateManagerItemRaw = elementReader.getElementText();

                String stateManagerItem;
                try {
                    stateManagerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(stateManagerItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (stateManager == null) {
                    stateManager = facesApplication.stateManager;
                    if (stateManager!= null) {
                        stateManager.clear();
                    } else {
                        stateManager = new ArrayList<>();
                    }
                }
                stateManager.add(stateManagerItem);
            } else if (("el-resolver" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: elResolver
                String elResolverItemRaw = elementReader.getElementText();

                String elResolverItem;
                try {
                    elResolverItem = Adapters.collapsedStringAdapterAdapter.unmarshal(elResolverItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (elResolver == null) {
                    elResolver = facesApplication.elResolver;
                    if (elResolver!= null) {
                        elResolver.clear();
                    } else {
                        elResolver = new ArrayList<>();
                    }
                }
                elResolver.add(elResolverItem);
            } else if (("property-resolver" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: propertyResolver
                String propertyResolverItemRaw = elementReader.getElementText();

                String propertyResolverItem;
                try {
                    propertyResolverItem = Adapters.collapsedStringAdapterAdapter.unmarshal(propertyResolverItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (propertyResolver == null) {
                    propertyResolver = facesApplication.propertyResolver;
                    if (propertyResolver!= null) {
                        propertyResolver.clear();
                    } else {
                        propertyResolver = new ArrayList<>();
                    }
                }
                propertyResolver.add(propertyResolverItem);
            } else if (("variable-resolver" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: variableResolver
                String variableResolverItemRaw = elementReader.getElementText();

                String variableResolverItem;
                try {
                    variableResolverItem = Adapters.collapsedStringAdapterAdapter.unmarshal(variableResolverItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (variableResolver == null) {
                    variableResolver = facesApplication.variableResolver;
                    if (variableResolver!= null) {
                        variableResolver.clear();
                    } else {
                        variableResolver = new ArrayList<>();
                    }
                }
                variableResolver.add(variableResolverItem);
            } else if (("resource-handler" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceHandler
                String resourceHandlerItemRaw = elementReader.getElementText();

                String resourceHandlerItem;
                try {
                    resourceHandlerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(resourceHandlerItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (resourceHandler == null) {
                    resourceHandler = facesApplication.resourceHandler;
                    if (resourceHandler!= null) {
                        resourceHandler.clear();
                    } else {
                        resourceHandler = new ArrayList<>();
                    }
                }
                resourceHandler.add(resourceHandlerItem);
            } else if (("system-event-listener" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: systemEventListener
                FacesSystemEventListener systemEventListenerItem = readFacesSystemEventListener(elementReader, context);
                if (systemEventListener == null) {
                    systemEventListener = facesApplication.systemEventListener;
                    if (systemEventListener!= null) {
                        systemEventListener.clear();
                    } else {
                        systemEventListener = new ArrayList<>();
                    }
                }
                systemEventListener.add(systemEventListenerItem);
            } else if (("locale-config" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: localeConfig
                FacesLocaleConfig localeConfigItem = readFacesLocaleConfig(elementReader, context);
                if (localeConfig == null) {
                    localeConfig = facesApplication.localeConfig;
                    if (localeConfig!= null) {
                        localeConfig.clear();
                    } else {
                        localeConfig = new ArrayList<>();
                    }
                }
                localeConfig.add(localeConfigItem);
            } else if (("resource-bundle" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceBundle
                FacesApplicationResourceBundle resourceBundle = readFacesApplicationResourceBundle(elementReader, context);
                facesApplication.resourceBundle = resourceBundle;
            } else if (("application-extension" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: applicationExtension
                FacesApplicationExtension applicationExtensionItem = readFacesApplicationExtension(elementReader, context);
                if (applicationExtension == null) {
                    applicationExtension = facesApplication.applicationExtension;
                    if (applicationExtension!= null) {
                        applicationExtension.clear();
                    } else {
                        applicationExtension = new ArrayList<>();
                    }
                }
                applicationExtension.add(applicationExtensionItem);
            } else if (("default-validators" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultValidators
                FacesValidator defaultValidatorsItem = readFacesValidator(elementReader, context);
                if (defaultValidators == null) {
                    defaultValidators = facesApplication.defaultValidators;
                    if (defaultValidators!= null) {
                        defaultValidators.clear();
                    } else {
                        defaultValidators = new ArrayList<>();
                    }
                }
                defaultValidators.add(defaultValidatorsItem);
            } else {
                // ELEMENT_REF: others
                if (others == null) {
                    others = facesApplication.others;
                    if (others!= null) {
                        others.clear();
                    } else {
                        others = new ArrayList<>();
                    }
                }
                others.add(context.readXmlAny(elementReader, Object.class, false));
            }
        }
        if (actionListener!= null) {
            facesApplication.actionListener = actionListener;
        }
        if (defaultRenderKitId!= null) {
            facesApplication.defaultRenderKitId = defaultRenderKitId;
        }
        if (messageBundle!= null) {
            facesApplication.messageBundle = messageBundle;
        }
        if (navigationHandler!= null) {
            facesApplication.navigationHandler = navigationHandler;
        }
        if (viewHandler!= null) {
            facesApplication.viewHandler = viewHandler;
        }
        if (stateManager!= null) {
            facesApplication.stateManager = stateManager;
        }
        if (elResolver!= null) {
            facesApplication.elResolver = elResolver;
        }
        if (propertyResolver!= null) {
            facesApplication.propertyResolver = propertyResolver;
        }
        if (variableResolver!= null) {
            facesApplication.variableResolver = variableResolver;
        }
        if (resourceHandler!= null) {
            facesApplication.resourceHandler = resourceHandler;
        }
        if (systemEventListener!= null) {
            facesApplication.systemEventListener = systemEventListener;
        }
        if (localeConfig!= null) {
            facesApplication.localeConfig = localeConfig;
        }
        if (applicationExtension!= null) {
            facesApplication.applicationExtension = applicationExtension;
        }
        if (defaultValidators!= null) {
            facesApplication.defaultValidators = defaultValidators;
        }
        if (others!= null) {
            facesApplication.others = others;
        }

        context.afterUnmarshal(facesApplication, LifecycleCallback.NONE);

        return facesApplication;
    }

    public final FacesApplication read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesApplication facesApplication, RuntimeContext context)
        throws Exception
    {
        if (facesApplication == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesApplication.class!= facesApplication.getClass()) {
            context.unexpectedSubclass(writer, facesApplication, FacesApplication.class);
            return ;
        }

        context.beforeMarshal(facesApplication, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesApplication.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesApplication, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: actionListener
        List<String> actionListenerRaw = facesApplication.actionListener;
        if (actionListenerRaw!= null) {
            for (String actionListenerItem: actionListenerRaw) {
                String actionListener = null;
                try {
                    actionListener = Adapters.collapsedStringAdapterAdapter.marshal(actionListenerItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesApplication, "actionListener", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (actionListener!= null) {
                    writer.writeStartElement(prefix, "action-listener", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(actionListener);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: defaultRenderKitId
        List<String> defaultRenderKitIdRaw = facesApplication.defaultRenderKitId;
        if (defaultRenderKitIdRaw!= null) {
            for (String defaultRenderKitIdItem: defaultRenderKitIdRaw) {
                String defaultRenderKitId = null;
                try {
                    defaultRenderKitId = Adapters.collapsedStringAdapterAdapter.marshal(defaultRenderKitIdItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesApplication, "defaultRenderKitId", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (defaultRenderKitId!= null) {
                    writer.writeStartElement(prefix, "default-render-kit-id", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(defaultRenderKitId);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: messageBundle
        List<String> messageBundleRaw = facesApplication.messageBundle;
        if (messageBundleRaw!= null) {
            for (String messageBundleItem: messageBundleRaw) {
                String messageBundle = null;
                try {
                    messageBundle = Adapters.collapsedStringAdapterAdapter.marshal(messageBundleItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesApplication, "messageBundle", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (messageBundle!= null) {
                    writer.writeStartElement(prefix, "message-bundle", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(messageBundle);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: navigationHandler
        List<String> navigationHandlerRaw = facesApplication.navigationHandler;
        if (navigationHandlerRaw!= null) {
            for (String navigationHandlerItem: navigationHandlerRaw) {
                String navigationHandler = null;
                try {
                    navigationHandler = Adapters.collapsedStringAdapterAdapter.marshal(navigationHandlerItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesApplication, "navigationHandler", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (navigationHandler!= null) {
                    writer.writeStartElement(prefix, "navigation-handler", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(navigationHandler);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: viewHandler
        List<String> viewHandlerRaw = facesApplication.viewHandler;
        if (viewHandlerRaw!= null) {
            for (String viewHandlerItem: viewHandlerRaw) {
                String viewHandler = null;
                try {
                    viewHandler = Adapters.collapsedStringAdapterAdapter.marshal(viewHandlerItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesApplication, "viewHandler", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (viewHandler!= null) {
                    writer.writeStartElement(prefix, "view-handler", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(viewHandler);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: stateManager
        List<String> stateManagerRaw = facesApplication.stateManager;
        if (stateManagerRaw!= null) {
            for (String stateManagerItem: stateManagerRaw) {
                String stateManager = null;
                try {
                    stateManager = Adapters.collapsedStringAdapterAdapter.marshal(stateManagerItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesApplication, "stateManager", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (stateManager!= null) {
                    writer.writeStartElement(prefix, "state-manager", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(stateManager);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: elResolver
        List<String> elResolverRaw = facesApplication.elResolver;
        if (elResolverRaw!= null) {
            for (String elResolverItem: elResolverRaw) {
                String elResolver = null;
                try {
                    elResolver = Adapters.collapsedStringAdapterAdapter.marshal(elResolverItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesApplication, "elResolver", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (elResolver!= null) {
                    writer.writeStartElement(prefix, "el-resolver", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(elResolver);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: propertyResolver
        List<String> propertyResolverRaw = facesApplication.propertyResolver;
        if (propertyResolverRaw!= null) {
            for (String propertyResolverItem: propertyResolverRaw) {
                String propertyResolver = null;
                try {
                    propertyResolver = Adapters.collapsedStringAdapterAdapter.marshal(propertyResolverItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesApplication, "propertyResolver", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (propertyResolver!= null) {
                    writer.writeStartElement(prefix, "property-resolver", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(propertyResolver);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: variableResolver
        List<String> variableResolverRaw = facesApplication.variableResolver;
        if (variableResolverRaw!= null) {
            for (String variableResolverItem: variableResolverRaw) {
                String variableResolver = null;
                try {
                    variableResolver = Adapters.collapsedStringAdapterAdapter.marshal(variableResolverItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesApplication, "variableResolver", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (variableResolver!= null) {
                    writer.writeStartElement(prefix, "variable-resolver", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(variableResolver);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: resourceHandler
        List<String> resourceHandlerRaw = facesApplication.resourceHandler;
        if (resourceHandlerRaw!= null) {
            for (String resourceHandlerItem: resourceHandlerRaw) {
                String resourceHandler = null;
                try {
                    resourceHandler = Adapters.collapsedStringAdapterAdapter.marshal(resourceHandlerItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesApplication, "resourceHandler", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (resourceHandler!= null) {
                    writer.writeStartElement(prefix, "resource-handler", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(resourceHandler);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: systemEventListener
        List<FacesSystemEventListener> systemEventListener = facesApplication.systemEventListener;
        if (systemEventListener!= null) {
            for (FacesSystemEventListener systemEventListenerItem: systemEventListener) {
                if (systemEventListenerItem!= null) {
                    writer.writeStartElement(prefix, "system-event-listener", "http://java.sun.com/xml/ns/javaee");
                    writeFacesSystemEventListener(writer, systemEventListenerItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: localeConfig
        List<FacesLocaleConfig> localeConfig = facesApplication.localeConfig;
        if (localeConfig!= null) {
            for (FacesLocaleConfig localeConfigItem: localeConfig) {
                if (localeConfigItem!= null) {
                    writer.writeStartElement(prefix, "locale-config", "http://java.sun.com/xml/ns/javaee");
                    writeFacesLocaleConfig(writer, localeConfigItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: resourceBundle
        FacesApplicationResourceBundle resourceBundle = facesApplication.resourceBundle;
        if (resourceBundle!= null) {
            writer.writeStartElement(prefix, "resource-bundle", "http://java.sun.com/xml/ns/javaee");
            writeFacesApplicationResourceBundle(writer, resourceBundle, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesApplication, "resourceBundle");
        }

        // ELEMENT: applicationExtension
        List<FacesApplicationExtension> applicationExtension = facesApplication.applicationExtension;
        if (applicationExtension!= null) {
            for (FacesApplicationExtension applicationExtensionItem: applicationExtension) {
                if (applicationExtensionItem!= null) {
                    writer.writeStartElement(prefix, "application-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesApplicationExtension(writer, applicationExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: defaultValidators
        List<FacesValidator> defaultValidators = facesApplication.defaultValidators;
        if (defaultValidators!= null) {
            for (FacesValidator defaultValidatorsItem: defaultValidators) {
                if (defaultValidatorsItem!= null) {
                    writer.writeStartElement(prefix, "default-validators", "http://java.sun.com/xml/ns/javaee");
                    writeFacesValidator(writer, defaultValidatorsItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT_REF: others
        List<Object> others = facesApplication.others;
        if (others!= null) {
            for (Object othersItem: others) {
                context.writeXmlAny(writer, facesApplication, "others", othersItem);
            }
        }

        context.afterMarshal(facesApplication, LifecycleCallback.NONE);
    }

}
