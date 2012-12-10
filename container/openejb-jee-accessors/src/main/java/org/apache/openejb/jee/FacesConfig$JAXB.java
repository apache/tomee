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

import static org.apache.openejb.jee.FacesAbsoluteOrdering$JAXB.readFacesAbsoluteOrdering;
import static org.apache.openejb.jee.FacesAbsoluteOrdering$JAXB.writeFacesAbsoluteOrdering;
import static org.apache.openejb.jee.FacesApplication$JAXB.readFacesApplication;
import static org.apache.openejb.jee.FacesApplication$JAXB.writeFacesApplication;
import static org.apache.openejb.jee.FacesBehavior$JAXB.readFacesBehavior;
import static org.apache.openejb.jee.FacesBehavior$JAXB.writeFacesBehavior;
import static org.apache.openejb.jee.FacesComponent$JAXB.readFacesComponent;
import static org.apache.openejb.jee.FacesComponent$JAXB.writeFacesComponent;
import static org.apache.openejb.jee.FacesConverter$JAXB.readFacesConverter;
import static org.apache.openejb.jee.FacesConverter$JAXB.writeFacesConverter;
import static org.apache.openejb.jee.FacesExtension$JAXB.readFacesExtension;
import static org.apache.openejb.jee.FacesExtension$JAXB.writeFacesExtension;
import static org.apache.openejb.jee.FacesFactory$JAXB.readFacesFactory;
import static org.apache.openejb.jee.FacesFactory$JAXB.writeFacesFactory;
import static org.apache.openejb.jee.FacesLifecycle$JAXB.readFacesLifecycle;
import static org.apache.openejb.jee.FacesLifecycle$JAXB.writeFacesLifecycle;
import static org.apache.openejb.jee.FacesManagedBean$JAXB.readFacesManagedBean;
import static org.apache.openejb.jee.FacesManagedBean$JAXB.writeFacesManagedBean;
import static org.apache.openejb.jee.FacesNavigationRule$JAXB.readFacesNavigationRule;
import static org.apache.openejb.jee.FacesNavigationRule$JAXB.writeFacesNavigationRule;
import static org.apache.openejb.jee.FacesOrdering$JAXB.readFacesOrdering;
import static org.apache.openejb.jee.FacesOrdering$JAXB.writeFacesOrdering;
import static org.apache.openejb.jee.FacesReferencedBean$JAXB.readFacesReferencedBean;
import static org.apache.openejb.jee.FacesReferencedBean$JAXB.writeFacesReferencedBean;
import static org.apache.openejb.jee.FacesRenderKit$JAXB.readFacesRenderKit;
import static org.apache.openejb.jee.FacesRenderKit$JAXB.writeFacesRenderKit;
import static org.apache.openejb.jee.FacesValidator$JAXB.readFacesValidator;
import static org.apache.openejb.jee.FacesValidator$JAXB.writeFacesValidator;

@SuppressWarnings({
        "StringEquality"
})
public class FacesConfig$JAXB
        extends JAXBObject<FacesConfig> {


    public FacesConfig$JAXB() {
        super(FacesConfig.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config".intern()), new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-configType".intern()), FacesApplication$JAXB.class, FacesOrdering$JAXB.class, FacesAbsoluteOrdering$JAXB.class, FacesFactory$JAXB.class, FacesComponent$JAXB.class, FacesConverter$JAXB.class, FacesManagedBean$JAXB.class, FacesNavigationRule$JAXB.class, FacesReferencedBean$JAXB.class, FacesRenderKit$JAXB.class, FacesLifecycle$JAXB.class, FacesValidator$JAXB.class, FacesBehavior$JAXB.class, FacesExtension$JAXB.class);
    }

    public static FacesConfig readFacesConfig(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesConfig(XoXMLStreamWriter writer, FacesConfig facesConfig, RuntimeContext context)
            throws Exception {
        _write(writer, facesConfig, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfig facesConfig, RuntimeContext context)
            throws Exception {
        _write(writer, facesConfig, context);
    }

    public final static FacesConfig _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfig facesConfig = new FacesConfig();
        context.beforeUnmarshal(facesConfig, LifecycleCallback.NONE);

        List<FacesApplication> application = null;
        List<FacesOrdering> ordering = null;
        List<FacesAbsoluteOrdering> absoluteOrdering = null;
        List<FacesFactory> factory = null;
        List<FacesComponent> component = null;
        List<FacesConverter> converter = null;
        List<FacesManagedBean> managedBean = null;
        List<String> name = null;
        List<FacesNavigationRule> navigationRule = null;
        List<FacesReferencedBean> referencedBean = null;
        List<FacesRenderKit> renderKit = null;
        List<FacesLifecycle> lifecycle = null;
        List<FacesValidator> validator = null;
        List<FacesBehavior> behavior = null;
        List<FacesExtension> facesConfigExtension = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-configType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfig.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesConfig);
                facesConfig.id = id;
            } else if (("version" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: version
                facesConfig.version = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (("metadata-complete" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: metadataComplete
                Boolean metadataComplete = ("1".equals(attribute.getValue()) || "true".equals(attribute.getValue()));
                facesConfig.metadataComplete = metadataComplete;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("", "version"), new QName("", "metadata-complete"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("application" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: application
                FacesApplication applicationItem = readFacesApplication(elementReader, context);
                if (application == null) {
                    application = facesConfig.application;
                    if (application != null) {
                        application.clear();
                    } else {
                        application = new ArrayList<FacesApplication>();
                    }
                }
                application.add(applicationItem);
            } else if (("ordering" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ordering
                FacesOrdering orderingItem = readFacesOrdering(elementReader, context);
                if (ordering == null) {
                    ordering = facesConfig.ordering;
                    if (ordering != null) {
                        ordering.clear();
                    } else {
                        ordering = new ArrayList<FacesOrdering>();
                    }
                }
                ordering.add(orderingItem);
            } else if (("absolute-ordering" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: absoluteOrdering
                FacesAbsoluteOrdering absoluteOrderingItem = readFacesAbsoluteOrdering(elementReader, context);
                if (absoluteOrdering == null) {
                    absoluteOrdering = facesConfig.absoluteOrdering;
                    if (absoluteOrdering != null) {
                        absoluteOrdering.clear();
                    } else {
                        absoluteOrdering = new ArrayList<FacesAbsoluteOrdering>();
                    }
                }
                absoluteOrdering.add(absoluteOrderingItem);
            } else if (("factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: factory
                FacesFactory factoryItem = readFacesFactory(elementReader, context);
                if (factory == null) {
                    factory = facesConfig.factory;
                    if (factory != null) {
                        factory.clear();
                    } else {
                        factory = new ArrayList<FacesFactory>();
                    }
                }
                factory.add(factoryItem);
            } else if (("component" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: component
                FacesComponent componentItem = readFacesComponent(elementReader, context);
                if (component == null) {
                    component = facesConfig.component;
                    if (component != null) {
                        component.clear();
                    } else {
                        component = new ArrayList<FacesComponent>();
                    }
                }
                component.add(componentItem);
            } else if (("converter" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: converter
                FacesConverter converterItem = readFacesConverter(elementReader, context);
                if (converter == null) {
                    converter = facesConfig.converter;
                    if (converter != null) {
                        converter.clear();
                    } else {
                        converter = new ArrayList<FacesConverter>();
                    }
                }
                converter.add(converterItem);
            } else if (("managed-bean" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: managedBean
                FacesManagedBean managedBeanItem = readFacesManagedBean(elementReader, context);
                if (managedBean == null) {
                    managedBean = facesConfig.managedBean;
                    if (managedBean != null) {
                        managedBean.clear();
                    } else {
                        managedBean = new ArrayList<FacesManagedBean>();
                    }
                }
                managedBean.add(managedBeanItem);
            } else if (("name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                String nameItemRaw = null;
                if (!elementReader.isXsiNil()) {
                    nameItemRaw = elementReader.getElementAsString();
                }

                String nameItem;
                try {
                    nameItem = Adapters.collapsedStringAdapterAdapter.unmarshal(nameItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (name == null) {
                    name = facesConfig.name;
                    if (name != null) {
                        name.clear();
                    } else {
                        name = new ArrayList<String>();
                    }
                }
                name.add(nameItem);
            } else if (("navigation-rule" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: navigationRule
                FacesNavigationRule navigationRuleItem = readFacesNavigationRule(elementReader, context);
                if (navigationRule == null) {
                    navigationRule = facesConfig.navigationRule;
                    if (navigationRule != null) {
                        navigationRule.clear();
                    } else {
                        navigationRule = new ArrayList<FacesNavigationRule>();
                    }
                }
                navigationRule.add(navigationRuleItem);
            } else if (("referenced-bean" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: referencedBean
                FacesReferencedBean referencedBeanItem = readFacesReferencedBean(elementReader, context);
                if (referencedBean == null) {
                    referencedBean = facesConfig.referencedBean;
                    if (referencedBean != null) {
                        referencedBean.clear();
                    } else {
                        referencedBean = new ArrayList<FacesReferencedBean>();
                    }
                }
                referencedBean.add(referencedBeanItem);
            } else if (("render-kit" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: renderKit
                FacesRenderKit renderKitItem = readFacesRenderKit(elementReader, context);
                if (renderKit == null) {
                    renderKit = facesConfig.renderKit;
                    if (renderKit != null) {
                        renderKit.clear();
                    } else {
                        renderKit = new ArrayList<FacesRenderKit>();
                    }
                }
                renderKit.add(renderKitItem);
            } else if (("lifecycle" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lifecycle
                FacesLifecycle lifecycleItem = readFacesLifecycle(elementReader, context);
                if (lifecycle == null) {
                    lifecycle = facesConfig.lifecycle;
                    if (lifecycle != null) {
                        lifecycle.clear();
                    } else {
                        lifecycle = new ArrayList<FacesLifecycle>();
                    }
                }
                lifecycle.add(lifecycleItem);
            } else if (("validator" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: validator
                FacesValidator validatorItem = readFacesValidator(elementReader, context);
                if (validator == null) {
                    validator = facesConfig.validator;
                    if (validator != null) {
                        validator.clear();
                    } else {
                        validator = new ArrayList<FacesValidator>();
                    }
                }
                validator.add(validatorItem);
            } else if (("behavior" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: behavior
                FacesBehavior behaviorItem = readFacesBehavior(elementReader, context);
                if (behavior == null) {
                    behavior = facesConfig.behavior;
                    if (behavior != null) {
                        behavior.clear();
                    } else {
                        behavior = new ArrayList<FacesBehavior>();
                    }
                }
                behavior.add(behaviorItem);
            } else if (("faces-config-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: facesConfigExtension
                FacesExtension facesConfigExtensionItem = readFacesExtension(elementReader, context);
                if (facesConfigExtension == null) {
                    facesConfigExtension = facesConfig.facesConfigExtension;
                    if (facesConfigExtension != null) {
                        facesConfigExtension.clear();
                    } else {
                        facesConfigExtension = new ArrayList<FacesExtension>();
                    }
                }
                facesConfigExtension.add(facesConfigExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "application"), new QName("http://java.sun.com/xml/ns/javaee", "ordering"), new QName("http://java.sun.com/xml/ns/javaee", "absolute-ordering"), new QName("http://java.sun.com/xml/ns/javaee", "factory"), new QName("http://java.sun.com/xml/ns/javaee", "component"), new QName("http://java.sun.com/xml/ns/javaee", "converter"), new QName("http://java.sun.com/xml/ns/javaee", "managed-bean"), new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "navigation-rule"), new QName("http://java.sun.com/xml/ns/javaee", "referenced-bean"), new QName("http://java.sun.com/xml/ns/javaee", "render-kit"), new QName("http://java.sun.com/xml/ns/javaee", "lifecycle"), new QName("http://java.sun.com/xml/ns/javaee", "validator"), new QName("http://java.sun.com/xml/ns/javaee", "behavior"), new QName("http://java.sun.com/xml/ns/javaee", "faces-config-extension"));
            }
        }
        if (application != null) {
            facesConfig.application = application;
        }
        if (ordering != null) {
            facesConfig.ordering = ordering;
        }
        if (absoluteOrdering != null) {
            facesConfig.absoluteOrdering = absoluteOrdering;
        }
        if (factory != null) {
            facesConfig.factory = factory;
        }
        if (component != null) {
            facesConfig.component = component;
        }
        if (converter != null) {
            facesConfig.converter = converter;
        }
        if (managedBean != null) {
            facesConfig.managedBean = managedBean;
        }
        if (name != null) {
            facesConfig.name = name;
        }
        if (navigationRule != null) {
            facesConfig.navigationRule = navigationRule;
        }
        if (referencedBean != null) {
            facesConfig.referencedBean = referencedBean;
        }
        if (renderKit != null) {
            facesConfig.renderKit = renderKit;
        }
        if (lifecycle != null) {
            facesConfig.lifecycle = lifecycle;
        }
        if (validator != null) {
            facesConfig.validator = validator;
        }
        if (behavior != null) {
            facesConfig.behavior = behavior;
        }
        if (facesConfigExtension != null) {
            facesConfig.facesConfigExtension = facesConfigExtension;
        }

        context.afterUnmarshal(facesConfig, LifecycleCallback.NONE);

        return facesConfig;
    }

    public final FacesConfig read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesConfig facesConfig, RuntimeContext context)
            throws Exception {
        if (facesConfig == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesConfig.class != facesConfig.getClass()) {
            context.unexpectedSubclass(writer, facesConfig, FacesConfig.class);
            return;
        }

        context.beforeMarshal(facesConfig, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesConfig.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConfig, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: version
        String versionRaw = facesConfig.version;
        if (versionRaw != null) {
            String version = null;
            try {
                version = Adapters.collapsedStringAdapterAdapter.marshal(versionRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConfig, "version", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "version", version);
        }

        // ATTRIBUTE: metadataComplete
        Boolean metadataComplete = facesConfig.metadataComplete;
        if (metadataComplete != null) {
            writer.writeAttribute("", "", "metadata-complete", Boolean.toString(metadataComplete));
        }

        // ELEMENT: application
        List<FacesApplication> application = facesConfig.application;
        if (application != null) {
            for (FacesApplication applicationItem : application) {
                writer.writeStartElement(prefix, "application", "http://java.sun.com/xml/ns/javaee");
                if (applicationItem != null) {
                    writeFacesApplication(writer, applicationItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: ordering
        List<FacesOrdering> ordering = facesConfig.ordering;
        if (ordering != null) {
            for (FacesOrdering orderingItem : ordering) {
                writer.writeStartElement(prefix, "ordering", "http://java.sun.com/xml/ns/javaee");
                if (orderingItem != null) {
                    writeFacesOrdering(writer, orderingItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: absoluteOrdering
        List<FacesAbsoluteOrdering> absoluteOrdering = facesConfig.absoluteOrdering;
        if (absoluteOrdering != null) {
            for (FacesAbsoluteOrdering absoluteOrderingItem : absoluteOrdering) {
                if (absoluteOrderingItem != null) {
                    writer.writeStartElement(prefix, "absolute-ordering", "http://java.sun.com/xml/ns/javaee");
                    writeFacesAbsoluteOrdering(writer, absoluteOrderingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: factory
        List<FacesFactory> factory = facesConfig.factory;
        if (factory != null) {
            for (FacesFactory factoryItem : factory) {
                writer.writeStartElement(prefix, "factory", "http://java.sun.com/xml/ns/javaee");
                if (factoryItem != null) {
                    writeFacesFactory(writer, factoryItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: component
        List<FacesComponent> component = facesConfig.component;
        if (component != null) {
            for (FacesComponent componentItem : component) {
                writer.writeStartElement(prefix, "component", "http://java.sun.com/xml/ns/javaee");
                if (componentItem != null) {
                    writeFacesComponent(writer, componentItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: converter
        List<FacesConverter> converter = facesConfig.converter;
        if (converter != null) {
            for (FacesConverter converterItem : converter) {
                writer.writeStartElement(prefix, "converter", "http://java.sun.com/xml/ns/javaee");
                if (converterItem != null) {
                    writeFacesConverter(writer, converterItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: managedBean
        List<FacesManagedBean> managedBean = facesConfig.managedBean;
        if (managedBean != null) {
            for (FacesManagedBean managedBeanItem : managedBean) {
                if (managedBeanItem != null) {
                    writer.writeStartElement(prefix, "managed-bean", "http://java.sun.com/xml/ns/javaee");
                    writeFacesManagedBean(writer, managedBeanItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: name
        List<String> nameRaw = facesConfig.name;
        if (nameRaw != null) {
            for (String nameItem : nameRaw) {
                String name = null;
                try {
                    name = Adapters.collapsedStringAdapterAdapter.marshal(nameItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesConfig, "name", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
                if (name != null) {
                    writer.writeCharacters(name);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: navigationRule
        List<FacesNavigationRule> navigationRule = facesConfig.navigationRule;
        if (navigationRule != null) {
            for (FacesNavigationRule navigationRuleItem : navigationRule) {
                if (navigationRuleItem != null) {
                    writer.writeStartElement(prefix, "navigation-rule", "http://java.sun.com/xml/ns/javaee");
                    writeFacesNavigationRule(writer, navigationRuleItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: referencedBean
        List<FacesReferencedBean> referencedBean = facesConfig.referencedBean;
        if (referencedBean != null) {
            for (FacesReferencedBean referencedBeanItem : referencedBean) {
                if (referencedBeanItem != null) {
                    writer.writeStartElement(prefix, "referenced-bean", "http://java.sun.com/xml/ns/javaee");
                    writeFacesReferencedBean(writer, referencedBeanItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: renderKit
        List<FacesRenderKit> renderKit = facesConfig.renderKit;
        if (renderKit != null) {
            for (FacesRenderKit renderKitItem : renderKit) {
                if (renderKitItem != null) {
                    writer.writeStartElement(prefix, "render-kit", "http://java.sun.com/xml/ns/javaee");
                    writeFacesRenderKit(writer, renderKitItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: lifecycle
        List<FacesLifecycle> lifecycle = facesConfig.lifecycle;
        if (lifecycle != null) {
            for (FacesLifecycle lifecycleItem : lifecycle) {
                writer.writeStartElement(prefix, "lifecycle", "http://java.sun.com/xml/ns/javaee");
                if (lifecycleItem != null) {
                    writeFacesLifecycle(writer, lifecycleItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: validator
        List<FacesValidator> validator = facesConfig.validator;
        if (validator != null) {
            for (FacesValidator validatorItem : validator) {
                writer.writeStartElement(prefix, "validator", "http://java.sun.com/xml/ns/javaee");
                if (validatorItem != null) {
                    writeFacesValidator(writer, validatorItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: behavior
        List<FacesBehavior> behavior = facesConfig.behavior;
        if (behavior != null) {
            for (FacesBehavior behaviorItem : behavior) {
                writer.writeStartElement(prefix, "behavior", "http://java.sun.com/xml/ns/javaee");
                if (behaviorItem != null) {
                    writeFacesBehavior(writer, behaviorItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: facesConfigExtension
        List<FacesExtension> facesConfigExtension = facesConfig.facesConfigExtension;
        if (facesConfigExtension != null) {
            for (FacesExtension facesConfigExtensionItem : facesConfigExtension) {
                if (facesConfigExtensionItem != null) {
                    writer.writeStartElement(prefix, "faces-config-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesExtension(writer, facesConfigExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesConfig, LifecycleCallback.NONE);
    }

}
