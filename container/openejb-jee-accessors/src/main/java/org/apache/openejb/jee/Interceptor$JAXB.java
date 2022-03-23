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
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.AroundInvoke$JAXB.readAroundInvoke;
import static org.apache.openejb.jee.AroundInvoke$JAXB.writeAroundInvoke;
import static org.apache.openejb.jee.AroundTimeout$JAXB.readAroundTimeout;
import static org.apache.openejb.jee.AroundTimeout$JAXB.writeAroundTimeout;
import static org.apache.openejb.jee.DataSource$JAXB.readDataSource;
import static org.apache.openejb.jee.DataSource$JAXB.writeDataSource;
import static org.apache.openejb.jee.EjbLocalRef$JAXB.readEjbLocalRef;
import static org.apache.openejb.jee.EjbLocalRef$JAXB.writeEjbLocalRef;
import static org.apache.openejb.jee.EjbRef$JAXB.readEjbRef;
import static org.apache.openejb.jee.EjbRef$JAXB.writeEjbRef;
import static org.apache.openejb.jee.EnvEntry$JAXB.readEnvEntry;
import static org.apache.openejb.jee.EnvEntry$JAXB.writeEnvEntry;
import static org.apache.openejb.jee.LifecycleCallback$JAXB.readLifecycleCallback;
import static org.apache.openejb.jee.LifecycleCallback$JAXB.writeLifecycleCallback;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.readMessageDestinationRef;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.writeMessageDestinationRef;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.readPersistenceContextRef;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.writePersistenceContextRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.readPersistenceUnitRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.writePersistenceUnitRef;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.readResourceEnvRef;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.writeResourceEnvRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.readResourceRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.writeResourceRef;
import static org.apache.openejb.jee.ServiceRef$JAXB.readServiceRef;
import static org.apache.openejb.jee.ServiceRef$JAXB.writeServiceRef;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Interceptor$JAXB
    extends JAXBObject<Interceptor> {


    public Interceptor$JAXB() {
        super(Interceptor.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "interceptorType".intern()), Text$JAXB.class, AroundInvoke$JAXB.class, AroundTimeout$JAXB.class, EnvEntry$JAXB.class, EjbRef$JAXB.class, EjbLocalRef$JAXB.class, ServiceRef$JAXB.class, ResourceRef$JAXB.class, ResourceEnvRef$JAXB.class, MessageDestinationRef$JAXB.class, PersistenceContextRef$JAXB.class, PersistenceUnitRef$JAXB.class, LifecycleCallback$JAXB.class, DataSource$JAXB.class);
    }

    public static Interceptor readInterceptor(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeInterceptor(final XoXMLStreamWriter writer, final Interceptor interceptor, final RuntimeContext context)
        throws Exception {
        _write(writer, interceptor, context);
    }

    public void write(final XoXMLStreamWriter writer, final Interceptor interceptor, final RuntimeContext context)
        throws Exception {
        _write(writer, interceptor, context);
    }

    public final static Interceptor _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Interceptor interceptor = new Interceptor();
        context.beforeUnmarshal(interceptor, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<AroundInvoke> aroundInvoke = null;
        List<AroundTimeout> aroundTimeout = null;
        KeyedCollection<String, EnvEntry> envEntry = null;
        KeyedCollection<String, EjbRef> ejbRef = null;
        KeyedCollection<String, EjbLocalRef> ejbLocalRef = null;
        KeyedCollection<String, ServiceRef> serviceRef = null;
        KeyedCollection<String, ResourceRef> resourceRef = null;
        KeyedCollection<String, ResourceEnvRef> resourceEnvRef = null;
        KeyedCollection<String, MessageDestinationRef> messageDestinationRef = null;
        KeyedCollection<String, PersistenceContextRef> persistenceContextRef = null;
        KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = null;
        List<org.apache.openejb.jee.LifecycleCallback> postConstruct = null;
        List<org.apache.openejb.jee.LifecycleCallback> preDestroy = null;
        KeyedCollection<String, DataSource> dataSource = null;
        List<org.apache.openejb.jee.LifecycleCallback> postActivate = null;
        List<org.apache.openejb.jee.LifecycleCallback> prePassivate = null;
        List<org.apache.openejb.jee.LifecycleCallback> afterBegin = null;
        List<org.apache.openejb.jee.LifecycleCallback> beforeCompletion = null;
        List<org.apache.openejb.jee.LifecycleCallback> afterCompletion = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("interceptorType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Interceptor.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, interceptor);
                interceptor.id = id;
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
            } else if (("interceptor-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: interceptorClass
                final String interceptorClassRaw = elementReader.getElementAsString();

                final String interceptorClass;
                try {
                    interceptorClass = Adapters.collapsedStringAdapterAdapter.unmarshal(interceptorClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                interceptor.interceptorClass = interceptorClass;
            } else if (("around-invoke" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: aroundInvoke
                final AroundInvoke aroundInvokeItem = readAroundInvoke(elementReader, context);
                if (aroundInvoke == null) {
                    aroundInvoke = interceptor.aroundInvoke;
                    if (aroundInvoke != null) {
                        aroundInvoke.clear();
                    } else {
                        aroundInvoke = new ArrayList<AroundInvoke>();
                    }
                }
                aroundInvoke.add(aroundInvokeItem);
            } else if (("around-timeout" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: aroundTimeout
                final AroundTimeout aroundTimeoutItem = readAroundTimeout(elementReader, context);
                if (aroundTimeout == null) {
                    aroundTimeout = interceptor.aroundTimeout;
                    if (aroundTimeout != null) {
                        aroundTimeout.clear();
                    } else {
                        aroundTimeout = new ArrayList<AroundTimeout>();
                    }
                }
                aroundTimeout.add(aroundTimeoutItem);
            } else if (("env-entry" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: envEntry
                final EnvEntry envEntryItem = readEnvEntry(elementReader, context);
                if (envEntry == null) {
                    envEntry = interceptor.envEntry;
                    if (envEntry != null) {
                        envEntry.clear();
                    } else {
                        envEntry = new KeyedCollection<String, EnvEntry>();
                    }
                }
                envEntry.add(envEntryItem);
            } else if (("ejb-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbRef
                final EjbRef ejbRefItem = readEjbRef(elementReader, context);
                if (ejbRef == null) {
                    ejbRef = interceptor.ejbRef;
                    if (ejbRef != null) {
                        ejbRef.clear();
                    } else {
                        ejbRef = new KeyedCollection<String, EjbRef>();
                    }
                }
                ejbRef.add(ejbRefItem);
            } else if (("ejb-local-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbLocalRef
                final EjbLocalRef ejbLocalRefItem = readEjbLocalRef(elementReader, context);
                if (ejbLocalRef == null) {
                    ejbLocalRef = interceptor.ejbLocalRef;
                    if (ejbLocalRef != null) {
                        ejbLocalRef.clear();
                    } else {
                        ejbLocalRef = new KeyedCollection<String, EjbLocalRef>();
                    }
                }
                ejbLocalRef.add(ejbLocalRefItem);
            } else if (("service-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceRef
                final ServiceRef serviceRefItem = readServiceRef(elementReader, context);
                if (serviceRef == null) {
                    serviceRef = interceptor.serviceRef;
                    if (serviceRef != null) {
                        serviceRef.clear();
                    } else {
                        serviceRef = new KeyedCollection<String, ServiceRef>();
                    }
                }
                serviceRef.add(serviceRefItem);
            } else if (("resource-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceRef
                final ResourceRef resourceRefItem = readResourceRef(elementReader, context);
                if (resourceRef == null) {
                    resourceRef = interceptor.resourceRef;
                    if (resourceRef != null) {
                        resourceRef.clear();
                    } else {
                        resourceRef = new KeyedCollection<String, ResourceRef>();
                    }
                }
                resourceRef.add(resourceRefItem);
            } else if (("resource-env-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceEnvRef
                final ResourceEnvRef resourceEnvRefItem = readResourceEnvRef(elementReader, context);
                if (resourceEnvRef == null) {
                    resourceEnvRef = interceptor.resourceEnvRef;
                    if (resourceEnvRef != null) {
                        resourceEnvRef.clear();
                    } else {
                        resourceEnvRef = new KeyedCollection<String, ResourceEnvRef>();
                    }
                }
                resourceEnvRef.add(resourceEnvRefItem);
            } else if (("message-destination-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestinationRef
                final MessageDestinationRef messageDestinationRefItem = readMessageDestinationRef(elementReader, context);
                if (messageDestinationRef == null) {
                    messageDestinationRef = interceptor.messageDestinationRef;
                    if (messageDestinationRef != null) {
                        messageDestinationRef.clear();
                    } else {
                        messageDestinationRef = new KeyedCollection<String, MessageDestinationRef>();
                    }
                }
                messageDestinationRef.add(messageDestinationRefItem);
            } else if (("persistence-context-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceContextRef
                final PersistenceContextRef persistenceContextRefItem = readPersistenceContextRef(elementReader, context);
                if (persistenceContextRef == null) {
                    persistenceContextRef = interceptor.persistenceContextRef;
                    if (persistenceContextRef != null) {
                        persistenceContextRef.clear();
                    } else {
                        persistenceContextRef = new KeyedCollection<String, PersistenceContextRef>();
                    }
                }
                persistenceContextRef.add(persistenceContextRefItem);
            } else if (("persistence-unit-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceUnitRef
                final PersistenceUnitRef persistenceUnitRefItem = readPersistenceUnitRef(elementReader, context);
                if (persistenceUnitRef == null) {
                    persistenceUnitRef = interceptor.persistenceUnitRef;
                    if (persistenceUnitRef != null) {
                        persistenceUnitRef.clear();
                    } else {
                        persistenceUnitRef = new KeyedCollection<String, PersistenceUnitRef>();
                    }
                }
                persistenceUnitRef.add(persistenceUnitRefItem);
            } else if (("post-construct" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: postConstruct
                final org.apache.openejb.jee.LifecycleCallback postConstructItem = readLifecycleCallback(elementReader, context);
                if (postConstruct == null) {
                    postConstruct = interceptor.postConstruct;
                    if (postConstruct != null) {
                        postConstruct.clear();
                    } else {
                        postConstruct = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                postConstruct.add(postConstructItem);
            } else if (("pre-destroy" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: preDestroy
                final org.apache.openejb.jee.LifecycleCallback preDestroyItem = readLifecycleCallback(elementReader, context);
                if (preDestroy == null) {
                    preDestroy = interceptor.preDestroy;
                    if (preDestroy != null) {
                        preDestroy.clear();
                    } else {
                        preDestroy = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                preDestroy.add(preDestroyItem);
            } else if (("data-source" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dataSource
                final DataSource dataSourceItem = readDataSource(elementReader, context);
                if (dataSource == null) {
                    dataSource = interceptor.dataSource;
                    if (dataSource != null) {
                        dataSource.clear();
                    } else {
                        dataSource = new KeyedCollection<String, DataSource>();
                    }
                }
                dataSource.add(dataSourceItem);
            } else if (("post-activate" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: postActivate
                final org.apache.openejb.jee.LifecycleCallback postActivateItem = readLifecycleCallback(elementReader, context);
                if (postActivate == null) {
                    postActivate = interceptor.postActivate;
                    if (postActivate != null) {
                        postActivate.clear();
                    } else {
                        postActivate = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                postActivate.add(postActivateItem);
            } else if (("pre-passivate" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: prePassivate
                final org.apache.openejb.jee.LifecycleCallback prePassivateItem = readLifecycleCallback(elementReader, context);
                if (prePassivate == null) {
                    prePassivate = interceptor.prePassivate;
                    if (prePassivate != null) {
                        prePassivate.clear();
                    } else {
                        prePassivate = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                prePassivate.add(prePassivateItem);
            } else if (("after-begin" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: afterBegin
                final org.apache.openejb.jee.LifecycleCallback afterBeginItem = readLifecycleCallback(elementReader, context);
                if (afterBegin == null) {
                    afterBegin = interceptor.afterBegin;
                    if (afterBegin != null) {
                        afterBegin.clear();
                    } else {
                        afterBegin = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                afterBegin.add(afterBeginItem);
            } else if (("before-completion" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: beforeCompletion
                final org.apache.openejb.jee.LifecycleCallback beforeCompletionItem = readLifecycleCallback(elementReader, context);
                if (beforeCompletion == null) {
                    beforeCompletion = interceptor.beforeCompletion;
                    if (beforeCompletion != null) {
                        beforeCompletion.clear();
                    } else {
                        beforeCompletion = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                beforeCompletion.add(beforeCompletionItem);
            } else if (("after-completion" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: afterCompletion
                final org.apache.openejb.jee.LifecycleCallback afterCompletionItem = readLifecycleCallback(elementReader, context);
                if (afterCompletion == null) {
                    afterCompletion = interceptor.afterCompletion;
                    if (afterCompletion != null) {
                        afterCompletion.clear();
                    } else {
                        afterCompletion = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                afterCompletion.add(afterCompletionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "interceptor-class"), new QName("http://java.sun.com/xml/ns/javaee", "around-invoke"), new QName("http://java.sun.com/xml/ns/javaee", "around-timeout"), new QName("http://java.sun.com/xml/ns/javaee", "env-entry"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-ref"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-local-ref"), new QName("http://java.sun.com/xml/ns/javaee", "service-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-env-ref"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-context-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-unit-ref"), new QName("http://java.sun.com/xml/ns/javaee", "post-construct"), new QName("http://java.sun.com/xml/ns/javaee", "pre-destroy"), new QName("http://java.sun.com/xml/ns/javaee", "data-source"), new QName("http://java.sun.com/xml/ns/javaee", "post-activate"), new QName("http://java.sun.com/xml/ns/javaee", "pre-passivate"), new QName("http://java.sun.com/xml/ns/javaee", "after-begin"), new QName("http://java.sun.com/xml/ns/javaee", "before-completion"), new QName("http://java.sun.com/xml/ns/javaee", "after-completion"));
            }
        }
        if (descriptions != null) {
            try {
                interceptor.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Interceptor.class, "setDescriptions", Text[].class, e);
            }
        }
        if (aroundInvoke != null) {
            interceptor.aroundInvoke = aroundInvoke;
        }
        if (aroundTimeout != null) {
            interceptor.aroundTimeout = aroundTimeout;
        }
        if (envEntry != null) {
            interceptor.envEntry = envEntry;
        }
        if (ejbRef != null) {
            interceptor.ejbRef = ejbRef;
        }
        if (ejbLocalRef != null) {
            interceptor.ejbLocalRef = ejbLocalRef;
        }
        if (serviceRef != null) {
            interceptor.serviceRef = serviceRef;
        }
        if (resourceRef != null) {
            interceptor.resourceRef = resourceRef;
        }
        if (resourceEnvRef != null) {
            interceptor.resourceEnvRef = resourceEnvRef;
        }
        if (messageDestinationRef != null) {
            interceptor.messageDestinationRef = messageDestinationRef;
        }
        if (persistenceContextRef != null) {
            interceptor.persistenceContextRef = persistenceContextRef;
        }
        if (persistenceUnitRef != null) {
            interceptor.persistenceUnitRef = persistenceUnitRef;
        }
        if (postConstruct != null) {
            interceptor.postConstruct = postConstruct;
        }
        if (preDestroy != null) {
            interceptor.preDestroy = preDestroy;
        }
        if (dataSource != null) {
            interceptor.dataSource = dataSource;
        }
        if (postActivate != null) {
            interceptor.postActivate = postActivate;
        }
        if (prePassivate != null) {
            interceptor.prePassivate = prePassivate;
        }
        if (afterBegin != null) {
            interceptor.afterBegin = afterBegin;
        }
        if (beforeCompletion != null) {
            interceptor.beforeCompletion = beforeCompletion;
        }
        if (afterCompletion != null) {
            interceptor.afterCompletion = afterCompletion;
        }

        context.afterUnmarshal(interceptor, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        return interceptor;
    }

    public final Interceptor read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Interceptor interceptor, RuntimeContext context)
        throws Exception {
        if (interceptor == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Interceptor.class != interceptor.getClass()) {
            context.unexpectedSubclass(writer, interceptor, Interceptor.class);
            return;
        }

        context.beforeMarshal(interceptor, org.metatype.sxc.jaxb.LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = interceptor.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(interceptor, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = interceptor.getDescriptions();
        } catch (final Exception e) {
            context.getterError(interceptor, "descriptions", Interceptor.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "descriptions");
                }
            }
        }

        // ELEMENT: interceptorClass
        final String interceptorClassRaw = interceptor.interceptorClass;
        String interceptorClass = null;
        try {
            interceptorClass = Adapters.collapsedStringAdapterAdapter.marshal(interceptorClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(interceptor, "interceptorClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (interceptorClass != null) {
            writer.writeStartElement(prefix, "interceptor-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(interceptorClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(interceptor, "interceptorClass");
        }

        // ELEMENT: aroundInvoke
        final List<AroundInvoke> aroundInvoke = interceptor.aroundInvoke;
        if (aroundInvoke != null) {
            for (final AroundInvoke aroundInvokeItem : aroundInvoke) {
                if (aroundInvokeItem != null) {
                    writer.writeStartElement(prefix, "around-invoke", "http://java.sun.com/xml/ns/javaee");
                    writeAroundInvoke(writer, aroundInvokeItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "aroundInvoke");
                }
            }
        }

        // ELEMENT: aroundTimeout
        final List<AroundTimeout> aroundTimeout = interceptor.aroundTimeout;
        if (aroundTimeout != null) {
            for (final AroundTimeout aroundTimeoutItem : aroundTimeout) {
                if (aroundTimeoutItem != null) {
                    writer.writeStartElement(prefix, "around-timeout", "http://java.sun.com/xml/ns/javaee");
                    writeAroundTimeout(writer, aroundTimeoutItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: envEntry
        final KeyedCollection<String, EnvEntry> envEntry = interceptor.envEntry;
        if (envEntry != null) {
            for (final EnvEntry envEntryItem : envEntry) {
                if (envEntryItem != null) {
                    writer.writeStartElement(prefix, "env-entry", "http://java.sun.com/xml/ns/javaee");
                    writeEnvEntry(writer, envEntryItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "envEntry");
                }
            }
        }

        // ELEMENT: ejbRef
        final KeyedCollection<String, EjbRef> ejbRef = interceptor.ejbRef;
        if (ejbRef != null) {
            for (final EjbRef ejbRefItem : ejbRef) {
                if (ejbRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbRef(writer, ejbRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "ejbRef");
                }
            }
        }

        // ELEMENT: ejbLocalRef
        final KeyedCollection<String, EjbLocalRef> ejbLocalRef = interceptor.ejbLocalRef;
        if (ejbLocalRef != null) {
            for (final EjbLocalRef ejbLocalRefItem : ejbLocalRef) {
                if (ejbLocalRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-local-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbLocalRef(writer, ejbLocalRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "ejbLocalRef");
                }
            }
        }

        // ELEMENT: serviceRef
        final KeyedCollection<String, ServiceRef> serviceRef = interceptor.serviceRef;
        if (serviceRef != null) {
            for (final ServiceRef serviceRefItem : serviceRef) {
                if (serviceRefItem != null) {
                    writer.writeStartElement(prefix, "service-ref", "http://java.sun.com/xml/ns/javaee");
                    writeServiceRef(writer, serviceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "serviceRef");
                }
            }
        }

        // ELEMENT: resourceRef
        final KeyedCollection<String, ResourceRef> resourceRef = interceptor.resourceRef;
        if (resourceRef != null) {
            for (final ResourceRef resourceRefItem : resourceRef) {
                if (resourceRefItem != null) {
                    writer.writeStartElement(prefix, "resource-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceRef(writer, resourceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "resourceRef");
                }
            }
        }

        // ELEMENT: resourceEnvRef
        final KeyedCollection<String, ResourceEnvRef> resourceEnvRef = interceptor.resourceEnvRef;
        if (resourceEnvRef != null) {
            for (final ResourceEnvRef resourceEnvRefItem : resourceEnvRef) {
                if (resourceEnvRefItem != null) {
                    writer.writeStartElement(prefix, "resource-env-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceEnvRef(writer, resourceEnvRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "resourceEnvRef");
                }
            }
        }

        // ELEMENT: messageDestinationRef
        final KeyedCollection<String, MessageDestinationRef> messageDestinationRef = interceptor.messageDestinationRef;
        if (messageDestinationRef != null) {
            for (final MessageDestinationRef messageDestinationRefItem : messageDestinationRef) {
                if (messageDestinationRefItem != null) {
                    writer.writeStartElement(prefix, "message-destination-ref", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestinationRef(writer, messageDestinationRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "messageDestinationRef");
                }
            }
        }

        // ELEMENT: persistenceContextRef
        final KeyedCollection<String, PersistenceContextRef> persistenceContextRef = interceptor.persistenceContextRef;
        if (persistenceContextRef != null) {
            for (final PersistenceContextRef persistenceContextRefItem : persistenceContextRef) {
                if (persistenceContextRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-context-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceContextRef(writer, persistenceContextRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "persistenceContextRef");
                }
            }
        }

        // ELEMENT: persistenceUnitRef
        final KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = interceptor.persistenceUnitRef;
        if (persistenceUnitRef != null) {
            for (final PersistenceUnitRef persistenceUnitRefItem : persistenceUnitRef) {
                if (persistenceUnitRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-unit-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceUnitRef(writer, persistenceUnitRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "persistenceUnitRef");
                }
            }
        }

        // ELEMENT: postConstruct
        final List<org.apache.openejb.jee.LifecycleCallback> postConstruct = interceptor.postConstruct;
        if (postConstruct != null) {
            for (final org.apache.openejb.jee.LifecycleCallback postConstructItem : postConstruct) {
                if (postConstructItem != null) {
                    writer.writeStartElement(prefix, "post-construct", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, postConstructItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "postConstruct");
                }
            }
        }

        // ELEMENT: preDestroy
        final List<org.apache.openejb.jee.LifecycleCallback> preDestroy = interceptor.preDestroy;
        if (preDestroy != null) {
            for (final org.apache.openejb.jee.LifecycleCallback preDestroyItem : preDestroy) {
                if (preDestroyItem != null) {
                    writer.writeStartElement(prefix, "pre-destroy", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, preDestroyItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "preDestroy");
                }
            }
        }

        // ELEMENT: dataSource
        final KeyedCollection<String, DataSource> dataSource = interceptor.dataSource;
        if (dataSource != null) {
            for (final DataSource dataSourceItem : dataSource) {
                if (dataSourceItem != null) {
                    writer.writeStartElement(prefix, "data-source", "http://java.sun.com/xml/ns/javaee");
                    writeDataSource(writer, dataSourceItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "dataSource");
                }
            }
        }

        // ELEMENT: postActivate
        final List<org.apache.openejb.jee.LifecycleCallback> postActivate = interceptor.postActivate;
        if (postActivate != null) {
            for (final org.apache.openejb.jee.LifecycleCallback postActivateItem : postActivate) {
                if (postActivateItem != null) {
                    writer.writeStartElement(prefix, "post-activate", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, postActivateItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "postActivate");
                }
            }
        }

        // ELEMENT: prePassivate
        final List<org.apache.openejb.jee.LifecycleCallback> prePassivate = interceptor.prePassivate;
        if (prePassivate != null) {
            for (final org.apache.openejb.jee.LifecycleCallback prePassivateItem : prePassivate) {
                if (prePassivateItem != null) {
                    writer.writeStartElement(prefix, "pre-passivate", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, prePassivateItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "prePassivate");
                }
            }
        }

        // ELEMENT: afterBegin
        final List<org.apache.openejb.jee.LifecycleCallback> afterBegin = interceptor.afterBegin;
        if (afterBegin != null) {
            for (final org.apache.openejb.jee.LifecycleCallback afterBeginItem : afterBegin) {
                if (afterBeginItem != null) {
                    writer.writeStartElement(prefix, "after-begin", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, afterBeginItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "afterBegin");
                }
            }
        }

        // ELEMENT: beforeCompletion
        final List<org.apache.openejb.jee.LifecycleCallback> beforeCompletion = interceptor.beforeCompletion;
        if (beforeCompletion != null) {
            for (final org.apache.openejb.jee.LifecycleCallback beforeCompletionItem : beforeCompletion) {
                if (beforeCompletionItem != null) {
                    writer.writeStartElement(prefix, "before-completion", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, beforeCompletionItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "beforeCompletion");
                }
            }
        }

        // ELEMENT: afterCompletion
        final List<org.apache.openejb.jee.LifecycleCallback> afterCompletion = interceptor.afterCompletion;
        if (afterCompletion != null) {
            for (final org.apache.openejb.jee.LifecycleCallback afterCompletionItem : afterCompletion) {
                if (afterCompletionItem != null) {
                    writer.writeStartElement(prefix, "after-completion", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, afterCompletionItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptor, "afterCompletion");
                }
            }
        }

        context.afterMarshal(interceptor, org.metatype.sxc.jaxb.LifecycleCallback.NONE);
    }

}
