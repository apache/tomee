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
    extends JAXBObject<FacesApplication> {


    public FacesApplication$JAXB() {
        super(FacesApplication.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-applicationType".intern()), FacesSystemEventListener$JAXB.class, FacesLocaleConfig$JAXB.class, FacesApplicationResourceBundle$JAXB.class, FacesApplicationExtension$JAXB.class, FacesValidator$JAXB.class);
    }

    public static FacesApplication readFacesApplication(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesApplication(final XoXMLStreamWriter writer, final FacesApplication facesApplication, final RuntimeContext context)
        throws Exception {
        _write(writer, facesApplication, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesApplication facesApplication, final RuntimeContext context)
        throws Exception {
        _write(writer, facesApplication, context);
    }

    public final static FacesApplication _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesApplication facesApplication = new FacesApplication();
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

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-applicationType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesApplication.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesApplication);
                facesApplication.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("action-listener" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: actionListener
                final String actionListenerItemRaw = elementReader.getElementAsString();

                final String actionListenerItem;
                try {
                    actionListenerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(actionListenerItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (actionListener == null) {
                    actionListener = facesApplication.actionListener;
                    if (actionListener != null) {
                        actionListener.clear();
                    } else {
                        actionListener = new ArrayList<String>();
                    }
                }
                actionListener.add(actionListenerItem);
            } else if (("default-render-kit-id" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultRenderKitId
                final String defaultRenderKitIdItemRaw = elementReader.getElementAsString();

                final String defaultRenderKitIdItem;
                try {
                    defaultRenderKitIdItem = Adapters.collapsedStringAdapterAdapter.unmarshal(defaultRenderKitIdItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (defaultRenderKitId == null) {
                    defaultRenderKitId = facesApplication.defaultRenderKitId;
                    if (defaultRenderKitId != null) {
                        defaultRenderKitId.clear();
                    } else {
                        defaultRenderKitId = new ArrayList<String>();
                    }
                }
                defaultRenderKitId.add(defaultRenderKitIdItem);
            } else if (("message-bundle" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageBundle
                final String messageBundleItemRaw = elementReader.getElementAsString();

                final String messageBundleItem;
                try {
                    messageBundleItem = Adapters.collapsedStringAdapterAdapter.unmarshal(messageBundleItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (messageBundle == null) {
                    messageBundle = facesApplication.messageBundle;
                    if (messageBundle != null) {
                        messageBundle.clear();
                    } else {
                        messageBundle = new ArrayList<String>();
                    }
                }
                messageBundle.add(messageBundleItem);
            } else if (("navigation-handler" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: navigationHandler
                final String navigationHandlerItemRaw = elementReader.getElementAsString();

                final String navigationHandlerItem;
                try {
                    navigationHandlerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(navigationHandlerItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (navigationHandler == null) {
                    navigationHandler = facesApplication.navigationHandler;
                    if (navigationHandler != null) {
                        navigationHandler.clear();
                    } else {
                        navigationHandler = new ArrayList<String>();
                    }
                }
                navigationHandler.add(navigationHandlerItem);
            } else if (("view-handler" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: viewHandler
                final String viewHandlerItemRaw = elementReader.getElementAsString();

                final String viewHandlerItem;
                try {
                    viewHandlerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(viewHandlerItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (viewHandler == null) {
                    viewHandler = facesApplication.viewHandler;
                    if (viewHandler != null) {
                        viewHandler.clear();
                    } else {
                        viewHandler = new ArrayList<String>();
                    }
                }
                viewHandler.add(viewHandlerItem);
            } else if (("state-manager" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: stateManager
                final String stateManagerItemRaw = elementReader.getElementAsString();

                final String stateManagerItem;
                try {
                    stateManagerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(stateManagerItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (stateManager == null) {
                    stateManager = facesApplication.stateManager;
                    if (stateManager != null) {
                        stateManager.clear();
                    } else {
                        stateManager = new ArrayList<String>();
                    }
                }
                stateManager.add(stateManagerItem);
            } else if (("el-resolver" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: elResolver
                final String elResolverItemRaw = elementReader.getElementAsString();

                final String elResolverItem;
                try {
                    elResolverItem = Adapters.collapsedStringAdapterAdapter.unmarshal(elResolverItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (elResolver == null) {
                    elResolver = facesApplication.elResolver;
                    if (elResolver != null) {
                        elResolver.clear();
                    } else {
                        elResolver = new ArrayList<String>();
                    }
                }
                elResolver.add(elResolverItem);
            } else if (("property-resolver" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: propertyResolver
                final String propertyResolverItemRaw = elementReader.getElementAsString();

                final String propertyResolverItem;
                try {
                    propertyResolverItem = Adapters.collapsedStringAdapterAdapter.unmarshal(propertyResolverItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (propertyResolver == null) {
                    propertyResolver = facesApplication.propertyResolver;
                    if (propertyResolver != null) {
                        propertyResolver.clear();
                    } else {
                        propertyResolver = new ArrayList<String>();
                    }
                }
                propertyResolver.add(propertyResolverItem);
            } else if (("variable-resolver" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: variableResolver
                final String variableResolverItemRaw = elementReader.getElementAsString();

                final String variableResolverItem;
                try {
                    variableResolverItem = Adapters.collapsedStringAdapterAdapter.unmarshal(variableResolverItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (variableResolver == null) {
                    variableResolver = facesApplication.variableResolver;
                    if (variableResolver != null) {
                        variableResolver.clear();
                    } else {
                        variableResolver = new ArrayList<String>();
                    }
                }
                variableResolver.add(variableResolverItem);
            } else if (("resource-handler" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceHandler
                final String resourceHandlerItemRaw = elementReader.getElementAsString();

                final String resourceHandlerItem;
                try {
                    resourceHandlerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(resourceHandlerItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (resourceHandler == null) {
                    resourceHandler = facesApplication.resourceHandler;
                    if (resourceHandler != null) {
                        resourceHandler.clear();
                    } else {
                        resourceHandler = new ArrayList<String>();
                    }
                }
                resourceHandler.add(resourceHandlerItem);
            } else if (("system-event-listener" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: systemEventListener
                final FacesSystemEventListener systemEventListenerItem = readFacesSystemEventListener(elementReader, context);
                if (systemEventListener == null) {
                    systemEventListener = facesApplication.systemEventListener;
                    if (systemEventListener != null) {
                        systemEventListener.clear();
                    } else {
                        systemEventListener = new ArrayList<FacesSystemEventListener>();
                    }
                }
                systemEventListener.add(systemEventListenerItem);
            } else if (("locale-config" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: localeConfig
                final FacesLocaleConfig localeConfigItem = readFacesLocaleConfig(elementReader, context);
                if (localeConfig == null) {
                    localeConfig = facesApplication.localeConfig;
                    if (localeConfig != null) {
                        localeConfig.clear();
                    } else {
                        localeConfig = new ArrayList<FacesLocaleConfig>();
                    }
                }
                localeConfig.add(localeConfigItem);
            } else if (("resource-bundle" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceBundle
                final FacesApplicationResourceBundle resourceBundle = readFacesApplicationResourceBundle(elementReader, context);
                facesApplication.resourceBundle = resourceBundle;
            } else if (("application-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: applicationExtension
                final FacesApplicationExtension applicationExtensionItem = readFacesApplicationExtension(elementReader, context);
                if (applicationExtension == null) {
                    applicationExtension = facesApplication.applicationExtension;
                    if (applicationExtension != null) {
                        applicationExtension.clear();
                    } else {
                        applicationExtension = new ArrayList<FacesApplicationExtension>();
                    }
                }
                applicationExtension.add(applicationExtensionItem);
            } else if (("default-validators" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultValidators
                final FacesValidator defaultValidatorsItem = readFacesValidator(elementReader, context);
                if (defaultValidators == null) {
                    defaultValidators = facesApplication.defaultValidators;
                    if (defaultValidators != null) {
                        defaultValidators.clear();
                    } else {
                        defaultValidators = new ArrayList<FacesValidator>();
                    }
                }
                defaultValidators.add(defaultValidatorsItem);
            } else {
                // just here ATM to not prevent users to get JSF 2.2 feature because we can't read it
                // TODO: handle it properly
                // context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "action-listener"), new QName("http://java.sun.com/xml/ns/javaee", "default-render-kit-id"), new QName("http://java.sun.com/xml/ns/javaee", "message-bundle"), new QName("http://java.sun.com/xml/ns/javaee", "navigation-handler"), new QName("http://java.sun.com/xml/ns/javaee", "view-handler"), new QName("http://java.sun.com/xml/ns/javaee", "state-manager"), new QName("http://java.sun.com/xml/ns/javaee", "el-resolver"), new QName("http://java.sun.com/xml/ns/javaee", "property-resolver"), new QName("http://java.sun.com/xml/ns/javaee", "variable-resolver"), new QName("http://java.sun.com/xml/ns/javaee", "resource-handler"), new QName("http://java.sun.com/xml/ns/javaee", "system-event-listener"), new QName("http://java.sun.com/xml/ns/javaee", "locale-config"), new QName("http://java.sun.com/xml/ns/javaee", "resource-bundle"), new QName("http://java.sun.com/xml/ns/javaee", "application-extension"), new QName("http://java.sun.com/xml/ns/javaee", "default-validators"));
            }
        }
        if (actionListener != null) {
            facesApplication.actionListener = actionListener;
        }
        if (defaultRenderKitId != null) {
            facesApplication.defaultRenderKitId = defaultRenderKitId;
        }
        if (messageBundle != null) {
            facesApplication.messageBundle = messageBundle;
        }
        if (navigationHandler != null) {
            facesApplication.navigationHandler = navigationHandler;
        }
        if (viewHandler != null) {
            facesApplication.viewHandler = viewHandler;
        }
        if (stateManager != null) {
            facesApplication.stateManager = stateManager;
        }
        if (elResolver != null) {
            facesApplication.elResolver = elResolver;
        }
        if (propertyResolver != null) {
            facesApplication.propertyResolver = propertyResolver;
        }
        if (variableResolver != null) {
            facesApplication.variableResolver = variableResolver;
        }
        if (resourceHandler != null) {
            facesApplication.resourceHandler = resourceHandler;
        }
        if (systemEventListener != null) {
            facesApplication.systemEventListener = systemEventListener;
        }
        if (localeConfig != null) {
            facesApplication.localeConfig = localeConfig;
        }
        if (applicationExtension != null) {
            facesApplication.applicationExtension = applicationExtension;
        }
        if (defaultValidators != null) {
            facesApplication.defaultValidators = defaultValidators;
        }

        context.afterUnmarshal(facesApplication, LifecycleCallback.NONE);

        return facesApplication;
    }

    public final FacesApplication read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesApplication facesApplication, RuntimeContext context)
        throws Exception {
        if (facesApplication == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesApplication.class != facesApplication.getClass()) {
            context.unexpectedSubclass(writer, facesApplication, FacesApplication.class);
            return;
        }

        context.beforeMarshal(facesApplication, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesApplication.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesApplication, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: actionListener
        final List<String> actionListenerRaw = facesApplication.actionListener;
        if (actionListenerRaw != null) {
            for (final String actionListenerItem : actionListenerRaw) {
                String actionListener = null;
                try {
                    actionListener = Adapters.collapsedStringAdapterAdapter.marshal(actionListenerItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(facesApplication, "actionListener", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (actionListener != null) {
                    writer.writeStartElement(prefix, "action-listener", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(actionListener);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: defaultRenderKitId
        final List<String> defaultRenderKitIdRaw = facesApplication.defaultRenderKitId;
        if (defaultRenderKitIdRaw != null) {
            for (final String defaultRenderKitIdItem : defaultRenderKitIdRaw) {
                String defaultRenderKitId = null;
                try {
                    defaultRenderKitId = Adapters.collapsedStringAdapterAdapter.marshal(defaultRenderKitIdItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(facesApplication, "defaultRenderKitId", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (defaultRenderKitId != null) {
                    writer.writeStartElement(prefix, "default-render-kit-id", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(defaultRenderKitId);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: messageBundle
        final List<String> messageBundleRaw = facesApplication.messageBundle;
        if (messageBundleRaw != null) {
            for (final String messageBundleItem : messageBundleRaw) {
                String messageBundle = null;
                try {
                    messageBundle = Adapters.collapsedStringAdapterAdapter.marshal(messageBundleItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(facesApplication, "messageBundle", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (messageBundle != null) {
                    writer.writeStartElement(prefix, "message-bundle", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(messageBundle);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: navigationHandler
        final List<String> navigationHandlerRaw = facesApplication.navigationHandler;
        if (navigationHandlerRaw != null) {
            for (final String navigationHandlerItem : navigationHandlerRaw) {
                String navigationHandler = null;
                try {
                    navigationHandler = Adapters.collapsedStringAdapterAdapter.marshal(navigationHandlerItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(facesApplication, "navigationHandler", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (navigationHandler != null) {
                    writer.writeStartElement(prefix, "navigation-handler", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(navigationHandler);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: viewHandler
        final List<String> viewHandlerRaw = facesApplication.viewHandler;
        if (viewHandlerRaw != null) {
            for (final String viewHandlerItem : viewHandlerRaw) {
                String viewHandler = null;
                try {
                    viewHandler = Adapters.collapsedStringAdapterAdapter.marshal(viewHandlerItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(facesApplication, "viewHandler", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (viewHandler != null) {
                    writer.writeStartElement(prefix, "view-handler", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(viewHandler);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: stateManager
        final List<String> stateManagerRaw = facesApplication.stateManager;
        if (stateManagerRaw != null) {
            for (final String stateManagerItem : stateManagerRaw) {
                String stateManager = null;
                try {
                    stateManager = Adapters.collapsedStringAdapterAdapter.marshal(stateManagerItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(facesApplication, "stateManager", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (stateManager != null) {
                    writer.writeStartElement(prefix, "state-manager", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(stateManager);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: elResolver
        final List<String> elResolverRaw = facesApplication.elResolver;
        if (elResolverRaw != null) {
            for (final String elResolverItem : elResolverRaw) {
                String elResolver = null;
                try {
                    elResolver = Adapters.collapsedStringAdapterAdapter.marshal(elResolverItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(facesApplication, "elResolver", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (elResolver != null) {
                    writer.writeStartElement(prefix, "el-resolver", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(elResolver);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: propertyResolver
        final List<String> propertyResolverRaw = facesApplication.propertyResolver;
        if (propertyResolverRaw != null) {
            for (final String propertyResolverItem : propertyResolverRaw) {
                String propertyResolver = null;
                try {
                    propertyResolver = Adapters.collapsedStringAdapterAdapter.marshal(propertyResolverItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(facesApplication, "propertyResolver", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (propertyResolver != null) {
                    writer.writeStartElement(prefix, "property-resolver", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(propertyResolver);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: variableResolver
        final List<String> variableResolverRaw = facesApplication.variableResolver;
        if (variableResolverRaw != null) {
            for (final String variableResolverItem : variableResolverRaw) {
                String variableResolver = null;
                try {
                    variableResolver = Adapters.collapsedStringAdapterAdapter.marshal(variableResolverItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(facesApplication, "variableResolver", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (variableResolver != null) {
                    writer.writeStartElement(prefix, "variable-resolver", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(variableResolver);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: resourceHandler
        final List<String> resourceHandlerRaw = facesApplication.resourceHandler;
        if (resourceHandlerRaw != null) {
            for (final String resourceHandlerItem : resourceHandlerRaw) {
                String resourceHandler = null;
                try {
                    resourceHandler = Adapters.collapsedStringAdapterAdapter.marshal(resourceHandlerItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(facesApplication, "resourceHandler", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (resourceHandler != null) {
                    writer.writeStartElement(prefix, "resource-handler", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(resourceHandler);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: systemEventListener
        final List<FacesSystemEventListener> systemEventListener = facesApplication.systemEventListener;
        if (systemEventListener != null) {
            for (final FacesSystemEventListener systemEventListenerItem : systemEventListener) {
                if (systemEventListenerItem != null) {
                    writer.writeStartElement(prefix, "system-event-listener", "http://java.sun.com/xml/ns/javaee");
                    writeFacesSystemEventListener(writer, systemEventListenerItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: localeConfig
        final List<FacesLocaleConfig> localeConfig = facesApplication.localeConfig;
        if (localeConfig != null) {
            for (final FacesLocaleConfig localeConfigItem : localeConfig) {
                if (localeConfigItem != null) {
                    writer.writeStartElement(prefix, "locale-config", "http://java.sun.com/xml/ns/javaee");
                    writeFacesLocaleConfig(writer, localeConfigItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: resourceBundle
        final FacesApplicationResourceBundle resourceBundle = facesApplication.resourceBundle;
        if (resourceBundle != null) {
            writer.writeStartElement(prefix, "resource-bundle", "http://java.sun.com/xml/ns/javaee");
            writeFacesApplicationResourceBundle(writer, resourceBundle, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesApplication, "resourceBundle");
        }

        // ELEMENT: applicationExtension
        final List<FacesApplicationExtension> applicationExtension = facesApplication.applicationExtension;
        if (applicationExtension != null) {
            for (final FacesApplicationExtension applicationExtensionItem : applicationExtension) {
                if (applicationExtensionItem != null) {
                    writer.writeStartElement(prefix, "application-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesApplicationExtension(writer, applicationExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: defaultValidators
        final List<FacesValidator> defaultValidators = facesApplication.defaultValidators;
        if (defaultValidators != null) {
            for (final FacesValidator defaultValidatorsItem : defaultValidators) {
                if (defaultValidatorsItem != null) {
                    writer.writeStartElement(prefix, "default-validators", "http://java.sun.com/xml/ns/javaee");
                    writeFacesValidator(writer, defaultValidatorsItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesApplication, LifecycleCallback.NONE);
    }

}
