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

import static org.apache.openejb.jee.FacesFactoryExtension$JAXB.readFacesFactoryExtension;
import static org.apache.openejb.jee.FacesFactoryExtension$JAXB.writeFacesFactoryExtension;

@SuppressWarnings({
        "StringEquality"
})
public class FacesFactory$JAXB
        extends JAXBObject<FacesFactory> {


    public FacesFactory$JAXB() {
        super(FacesFactory.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-factoryType".intern()), FacesFactoryExtension$JAXB.class);
    }

    public static FacesFactory readFacesFactory(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesFactory(XoXMLStreamWriter writer, FacesFactory facesFactory, RuntimeContext context)
            throws Exception {
        _write(writer, facesFactory, context);
    }

    public void write(XoXMLStreamWriter writer, FacesFactory facesFactory, RuntimeContext context)
            throws Exception {
        _write(writer, facesFactory, context);
    }

    public final static FacesFactory _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesFactory facesFactory = new FacesFactory();
        context.beforeUnmarshal(facesFactory, LifecycleCallback.NONE);

        List<String> applicationFactory = null;
        List<String> exceptionHandlerFactory = null;
        List<String> externalContextFactory = null;
        List<String> facesContextFactory = null;
        List<String> partialViewContextFactory = null;
        List<String> lifecycleFactory = null;
        List<String> viewDeclarationLanguageFactory = null;
        List<String> tagHandlerDelegateFactory = null;
        List<String> renderKitFactory = null;
        List<String> visitContextFactory = null;
        List<FacesFactoryExtension> factoryExtension = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-factoryType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesFactory.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesFactory);
                facesFactory.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("application-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: applicationFactory
                String applicationFactoryItemRaw = elementReader.getElementAsString();

                String applicationFactoryItem;
                try {
                    applicationFactoryItem = Adapters.collapsedStringAdapterAdapter.unmarshal(applicationFactoryItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (applicationFactory == null) {
                    applicationFactory = facesFactory.applicationFactory;
                    if (applicationFactory != null) {
                        applicationFactory.clear();
                    } else {
                        applicationFactory = new ArrayList<String>();
                    }
                }
                applicationFactory.add(applicationFactoryItem);
            } else if (("exception-handler-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: exceptionHandlerFactory
                String exceptionHandlerFactoryItemRaw = elementReader.getElementAsString();

                String exceptionHandlerFactoryItem;
                try {
                    exceptionHandlerFactoryItem = Adapters.collapsedStringAdapterAdapter.unmarshal(exceptionHandlerFactoryItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (exceptionHandlerFactory == null) {
                    exceptionHandlerFactory = facesFactory.exceptionHandlerFactory;
                    if (exceptionHandlerFactory != null) {
                        exceptionHandlerFactory.clear();
                    } else {
                        exceptionHandlerFactory = new ArrayList<String>();
                    }
                }
                exceptionHandlerFactory.add(exceptionHandlerFactoryItem);
            } else if (("external-context-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: externalContextFactory
                String externalContextFactoryItemRaw = elementReader.getElementAsString();

                String externalContextFactoryItem;
                try {
                    externalContextFactoryItem = Adapters.collapsedStringAdapterAdapter.unmarshal(externalContextFactoryItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (externalContextFactory == null) {
                    externalContextFactory = facesFactory.externalContextFactory;
                    if (externalContextFactory != null) {
                        externalContextFactory.clear();
                    } else {
                        externalContextFactory = new ArrayList<String>();
                    }
                }
                externalContextFactory.add(externalContextFactoryItem);
            } else if (("faces-context-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: facesContextFactory
                String facesContextFactoryItemRaw = elementReader.getElementAsString();

                String facesContextFactoryItem;
                try {
                    facesContextFactoryItem = Adapters.collapsedStringAdapterAdapter.unmarshal(facesContextFactoryItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (facesContextFactory == null) {
                    facesContextFactory = facesFactory.facesContextFactory;
                    if (facesContextFactory != null) {
                        facesContextFactory.clear();
                    } else {
                        facesContextFactory = new ArrayList<String>();
                    }
                }
                facesContextFactory.add(facesContextFactoryItem);
            } else if (("partial-view-context-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: partialViewContextFactory
                String partialViewContextFactoryItemRaw = elementReader.getElementAsString();

                String partialViewContextFactoryItem;
                try {
                    partialViewContextFactoryItem = Adapters.collapsedStringAdapterAdapter.unmarshal(partialViewContextFactoryItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (partialViewContextFactory == null) {
                    partialViewContextFactory = facesFactory.partialViewContextFactory;
                    if (partialViewContextFactory != null) {
                        partialViewContextFactory.clear();
                    } else {
                        partialViewContextFactory = new ArrayList<String>();
                    }
                }
                partialViewContextFactory.add(partialViewContextFactoryItem);
            } else if (("lifecycle-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lifecycleFactory
                String lifecycleFactoryItemRaw = elementReader.getElementAsString();

                String lifecycleFactoryItem;
                try {
                    lifecycleFactoryItem = Adapters.collapsedStringAdapterAdapter.unmarshal(lifecycleFactoryItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (lifecycleFactory == null) {
                    lifecycleFactory = facesFactory.lifecycleFactory;
                    if (lifecycleFactory != null) {
                        lifecycleFactory.clear();
                    } else {
                        lifecycleFactory = new ArrayList<String>();
                    }
                }
                lifecycleFactory.add(lifecycleFactoryItem);
            } else if (("view-declaration-language-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: viewDeclarationLanguageFactory
                String viewDeclarationLanguageFactoryItemRaw = elementReader.getElementAsString();

                String viewDeclarationLanguageFactoryItem;
                try {
                    viewDeclarationLanguageFactoryItem = Adapters.collapsedStringAdapterAdapter.unmarshal(viewDeclarationLanguageFactoryItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (viewDeclarationLanguageFactory == null) {
                    viewDeclarationLanguageFactory = facesFactory.viewDeclarationLanguageFactory;
                    if (viewDeclarationLanguageFactory != null) {
                        viewDeclarationLanguageFactory.clear();
                    } else {
                        viewDeclarationLanguageFactory = new ArrayList<String>();
                    }
                }
                viewDeclarationLanguageFactory.add(viewDeclarationLanguageFactoryItem);
            } else if (("tag-handler-delegate-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: tagHandlerDelegateFactory
                String tagHandlerDelegateFactoryItemRaw = elementReader.getElementAsString();

                String tagHandlerDelegateFactoryItem;
                try {
                    tagHandlerDelegateFactoryItem = Adapters.collapsedStringAdapterAdapter.unmarshal(tagHandlerDelegateFactoryItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (tagHandlerDelegateFactory == null) {
                    tagHandlerDelegateFactory = facesFactory.tagHandlerDelegateFactory;
                    if (tagHandlerDelegateFactory != null) {
                        tagHandlerDelegateFactory.clear();
                    } else {
                        tagHandlerDelegateFactory = new ArrayList<String>();
                    }
                }
                tagHandlerDelegateFactory.add(tagHandlerDelegateFactoryItem);
            } else if (("render-kit-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: renderKitFactory
                String renderKitFactoryItemRaw = elementReader.getElementAsString();

                String renderKitFactoryItem;
                try {
                    renderKitFactoryItem = Adapters.collapsedStringAdapterAdapter.unmarshal(renderKitFactoryItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (renderKitFactory == null) {
                    renderKitFactory = facesFactory.renderKitFactory;
                    if (renderKitFactory != null) {
                        renderKitFactory.clear();
                    } else {
                        renderKitFactory = new ArrayList<String>();
                    }
                }
                renderKitFactory.add(renderKitFactoryItem);
            } else if (("visit-context-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: visitContextFactory
                String visitContextFactoryItemRaw = elementReader.getElementAsString();

                String visitContextFactoryItem;
                try {
                    visitContextFactoryItem = Adapters.collapsedStringAdapterAdapter.unmarshal(visitContextFactoryItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (visitContextFactory == null) {
                    visitContextFactory = facesFactory.visitContextFactory;
                    if (visitContextFactory != null) {
                        visitContextFactory.clear();
                    } else {
                        visitContextFactory = new ArrayList<String>();
                    }
                }
                visitContextFactory.add(visitContextFactoryItem);
            } else if (("factory-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: factoryExtension
                FacesFactoryExtension factoryExtensionItem = readFacesFactoryExtension(elementReader, context);
                if (factoryExtension == null) {
                    factoryExtension = facesFactory.factoryExtension;
                    if (factoryExtension != null) {
                        factoryExtension.clear();
                    } else {
                        factoryExtension = new ArrayList<FacesFactoryExtension>();
                    }
                }
                factoryExtension.add(factoryExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "application-factory"), new QName("http://java.sun.com/xml/ns/javaee", "exception-handler-factory"), new QName("http://java.sun.com/xml/ns/javaee", "external-context-factory"), new QName("http://java.sun.com/xml/ns/javaee", "faces-context-factory"), new QName("http://java.sun.com/xml/ns/javaee", "partial-view-context-factory"), new QName("http://java.sun.com/xml/ns/javaee", "lifecycle-factory"), new QName("http://java.sun.com/xml/ns/javaee", "view-declaration-language-factory"), new QName("http://java.sun.com/xml/ns/javaee", "tag-handler-delegate-factory"), new QName("http://java.sun.com/xml/ns/javaee", "render-kit-factory"), new QName("http://java.sun.com/xml/ns/javaee", "visit-context-factory"), new QName("http://java.sun.com/xml/ns/javaee", "factory-extension"));
            }
        }
        if (applicationFactory != null) {
            facesFactory.applicationFactory = applicationFactory;
        }
        if (exceptionHandlerFactory != null) {
            facesFactory.exceptionHandlerFactory = exceptionHandlerFactory;
        }
        if (externalContextFactory != null) {
            facesFactory.externalContextFactory = externalContextFactory;
        }
        if (facesContextFactory != null) {
            facesFactory.facesContextFactory = facesContextFactory;
        }
        if (partialViewContextFactory != null) {
            facesFactory.partialViewContextFactory = partialViewContextFactory;
        }
        if (lifecycleFactory != null) {
            facesFactory.lifecycleFactory = lifecycleFactory;
        }
        if (viewDeclarationLanguageFactory != null) {
            facesFactory.viewDeclarationLanguageFactory = viewDeclarationLanguageFactory;
        }
        if (tagHandlerDelegateFactory != null) {
            facesFactory.tagHandlerDelegateFactory = tagHandlerDelegateFactory;
        }
        if (renderKitFactory != null) {
            facesFactory.renderKitFactory = renderKitFactory;
        }
        if (visitContextFactory != null) {
            facesFactory.visitContextFactory = visitContextFactory;
        }
        if (factoryExtension != null) {
            facesFactory.factoryExtension = factoryExtension;
        }

        context.afterUnmarshal(facesFactory, LifecycleCallback.NONE);

        return facesFactory;
    }

    public final FacesFactory read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesFactory facesFactory, RuntimeContext context)
            throws Exception {
        if (facesFactory == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesFactory.class != facesFactory.getClass()) {
            context.unexpectedSubclass(writer, facesFactory, FacesFactory.class);
            return;
        }

        context.beforeMarshal(facesFactory, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesFactory.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesFactory, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: applicationFactory
        List<String> applicationFactoryRaw = facesFactory.applicationFactory;
        if (applicationFactoryRaw != null) {
            for (String applicationFactoryItem : applicationFactoryRaw) {
                String applicationFactory = null;
                try {
                    applicationFactory = Adapters.collapsedStringAdapterAdapter.marshal(applicationFactoryItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesFactory, "applicationFactory", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (applicationFactory != null) {
                    writer.writeStartElement(prefix, "application-factory", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(applicationFactory);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: exceptionHandlerFactory
        List<String> exceptionHandlerFactoryRaw = facesFactory.exceptionHandlerFactory;
        if (exceptionHandlerFactoryRaw != null) {
            for (String exceptionHandlerFactoryItem : exceptionHandlerFactoryRaw) {
                String exceptionHandlerFactory = null;
                try {
                    exceptionHandlerFactory = Adapters.collapsedStringAdapterAdapter.marshal(exceptionHandlerFactoryItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesFactory, "exceptionHandlerFactory", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (exceptionHandlerFactory != null) {
                    writer.writeStartElement(prefix, "exception-handler-factory", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(exceptionHandlerFactory);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: externalContextFactory
        List<String> externalContextFactoryRaw = facesFactory.externalContextFactory;
        if (externalContextFactoryRaw != null) {
            for (String externalContextFactoryItem : externalContextFactoryRaw) {
                String externalContextFactory = null;
                try {
                    externalContextFactory = Adapters.collapsedStringAdapterAdapter.marshal(externalContextFactoryItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesFactory, "externalContextFactory", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (externalContextFactory != null) {
                    writer.writeStartElement(prefix, "external-context-factory", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(externalContextFactory);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: facesContextFactory
        List<String> facesContextFactoryRaw = facesFactory.facesContextFactory;
        if (facesContextFactoryRaw != null) {
            for (String facesContextFactoryItem : facesContextFactoryRaw) {
                String facesContextFactory = null;
                try {
                    facesContextFactory = Adapters.collapsedStringAdapterAdapter.marshal(facesContextFactoryItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesFactory, "facesContextFactory", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (facesContextFactory != null) {
                    writer.writeStartElement(prefix, "faces-context-factory", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(facesContextFactory);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: partialViewContextFactory
        List<String> partialViewContextFactoryRaw = facesFactory.partialViewContextFactory;
        if (partialViewContextFactoryRaw != null) {
            for (String partialViewContextFactoryItem : partialViewContextFactoryRaw) {
                String partialViewContextFactory = null;
                try {
                    partialViewContextFactory = Adapters.collapsedStringAdapterAdapter.marshal(partialViewContextFactoryItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesFactory, "partialViewContextFactory", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (partialViewContextFactory != null) {
                    writer.writeStartElement(prefix, "partial-view-context-factory", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(partialViewContextFactory);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: lifecycleFactory
        List<String> lifecycleFactoryRaw = facesFactory.lifecycleFactory;
        if (lifecycleFactoryRaw != null) {
            for (String lifecycleFactoryItem : lifecycleFactoryRaw) {
                String lifecycleFactory = null;
                try {
                    lifecycleFactory = Adapters.collapsedStringAdapterAdapter.marshal(lifecycleFactoryItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesFactory, "lifecycleFactory", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (lifecycleFactory != null) {
                    writer.writeStartElement(prefix, "lifecycle-factory", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(lifecycleFactory);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: viewDeclarationLanguageFactory
        List<String> viewDeclarationLanguageFactoryRaw = facesFactory.viewDeclarationLanguageFactory;
        if (viewDeclarationLanguageFactoryRaw != null) {
            for (String viewDeclarationLanguageFactoryItem : viewDeclarationLanguageFactoryRaw) {
                String viewDeclarationLanguageFactory = null;
                try {
                    viewDeclarationLanguageFactory = Adapters.collapsedStringAdapterAdapter.marshal(viewDeclarationLanguageFactoryItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesFactory, "viewDeclarationLanguageFactory", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (viewDeclarationLanguageFactory != null) {
                    writer.writeStartElement(prefix, "view-declaration-language-factory", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(viewDeclarationLanguageFactory);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: tagHandlerDelegateFactory
        List<String> tagHandlerDelegateFactoryRaw = facesFactory.tagHandlerDelegateFactory;
        if (tagHandlerDelegateFactoryRaw != null) {
            for (String tagHandlerDelegateFactoryItem : tagHandlerDelegateFactoryRaw) {
                String tagHandlerDelegateFactory = null;
                try {
                    tagHandlerDelegateFactory = Adapters.collapsedStringAdapterAdapter.marshal(tagHandlerDelegateFactoryItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesFactory, "tagHandlerDelegateFactory", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (tagHandlerDelegateFactory != null) {
                    writer.writeStartElement(prefix, "tag-handler-delegate-factory", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(tagHandlerDelegateFactory);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: renderKitFactory
        List<String> renderKitFactoryRaw = facesFactory.renderKitFactory;
        if (renderKitFactoryRaw != null) {
            for (String renderKitFactoryItem : renderKitFactoryRaw) {
                String renderKitFactory = null;
                try {
                    renderKitFactory = Adapters.collapsedStringAdapterAdapter.marshal(renderKitFactoryItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesFactory, "renderKitFactory", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (renderKitFactory != null) {
                    writer.writeStartElement(prefix, "render-kit-factory", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(renderKitFactory);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: visitContextFactory
        List<String> visitContextFactoryRaw = facesFactory.visitContextFactory;
        if (visitContextFactoryRaw != null) {
            for (String visitContextFactoryItem : visitContextFactoryRaw) {
                String visitContextFactory = null;
                try {
                    visitContextFactory = Adapters.collapsedStringAdapterAdapter.marshal(visitContextFactoryItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesFactory, "visitContextFactory", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (visitContextFactory != null) {
                    writer.writeStartElement(prefix, "visit-context-factory", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(visitContextFactory);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: factoryExtension
        List<FacesFactoryExtension> factoryExtension = facesFactory.factoryExtension;
        if (factoryExtension != null) {
            for (FacesFactoryExtension factoryExtensionItem : factoryExtension) {
                if (factoryExtensionItem != null) {
                    writer.writeStartElement(prefix, "factory-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesFactoryExtension(writer, factoryExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesFactory, LifecycleCallback.NONE);
    }

}
