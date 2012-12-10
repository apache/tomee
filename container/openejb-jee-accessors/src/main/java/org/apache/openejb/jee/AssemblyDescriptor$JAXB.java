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

import static org.apache.openejb.jee.ApplicationException$JAXB.readApplicationException;
import static org.apache.openejb.jee.ApplicationException$JAXB.writeApplicationException;
import static org.apache.openejb.jee.ContainerConcurrency$JAXB.readContainerConcurrency;
import static org.apache.openejb.jee.ContainerConcurrency$JAXB.writeContainerConcurrency;
import static org.apache.openejb.jee.ContainerTransaction$JAXB.readContainerTransaction;
import static org.apache.openejb.jee.ContainerTransaction$JAXB.writeContainerTransaction;
import static org.apache.openejb.jee.ExcludeList$JAXB.readExcludeList;
import static org.apache.openejb.jee.ExcludeList$JAXB.writeExcludeList;
import static org.apache.openejb.jee.InterceptorBinding$JAXB.readInterceptorBinding;
import static org.apache.openejb.jee.InterceptorBinding$JAXB.writeInterceptorBinding;
import static org.apache.openejb.jee.MessageDestination$JAXB.readMessageDestination;
import static org.apache.openejb.jee.MessageDestination$JAXB.writeMessageDestination;
import static org.apache.openejb.jee.MethodPermission$JAXB.readMethodPermission;
import static org.apache.openejb.jee.MethodPermission$JAXB.writeMethodPermission;
import static org.apache.openejb.jee.SecurityRole$JAXB.readSecurityRole;
import static org.apache.openejb.jee.SecurityRole$JAXB.writeSecurityRole;

@SuppressWarnings({
        "StringEquality"
})
public class AssemblyDescriptor$JAXB
        extends JAXBObject<AssemblyDescriptor> {


    public AssemblyDescriptor$JAXB() {
        super(AssemblyDescriptor.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "assembly-descriptorType".intern()), SecurityRole$JAXB.class, MethodPermission$JAXB.class, ContainerTransaction$JAXB.class, ContainerConcurrency$JAXB.class, InterceptorBinding$JAXB.class, MessageDestination$JAXB.class, ExcludeList$JAXB.class, ApplicationException$JAXB.class);
    }

    public static AssemblyDescriptor readAssemblyDescriptor(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeAssemblyDescriptor(XoXMLStreamWriter writer, AssemblyDescriptor assemblyDescriptor, RuntimeContext context)
            throws Exception {
        _write(writer, assemblyDescriptor, context);
    }

    public void write(XoXMLStreamWriter writer, AssemblyDescriptor assemblyDescriptor, RuntimeContext context)
            throws Exception {
        _write(writer, assemblyDescriptor, context);
    }

    public final static AssemblyDescriptor _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        AssemblyDescriptor assemblyDescriptor = new AssemblyDescriptor();
        context.beforeUnmarshal(assemblyDescriptor, LifecycleCallback.NONE);

        List<SecurityRole> securityRole = null;
        List<MethodPermission> methodPermission = null;
        List<ContainerTransaction> containerTransaction = null;
        List<ContainerConcurrency> containerConcurrency = null;
        List<InterceptorBinding> interceptorBinding = null;
        List<MessageDestination> messageDestination = null;
        KeyedCollection<String, ApplicationException> applicationException = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("assembly-descriptorType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, AssemblyDescriptor.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, assemblyDescriptor);
                assemblyDescriptor.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("security-role" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityRole
                SecurityRole securityRoleItem = readSecurityRole(elementReader, context);
                if (securityRole == null) {
                    securityRole = assemblyDescriptor.securityRole;
                    if (securityRole != null) {
                        securityRole.clear();
                    } else {
                        securityRole = new ArrayList<SecurityRole>();
                    }
                }
                securityRole.add(securityRoleItem);
            } else if (("method-permission" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodPermission
                MethodPermission methodPermissionItem = readMethodPermission(elementReader, context);
                if (methodPermission == null) {
                    methodPermission = assemblyDescriptor.methodPermission;
                    if (methodPermission != null) {
                        methodPermission.clear();
                    } else {
                        methodPermission = new ArrayList<MethodPermission>();
                    }
                }
                methodPermission.add(methodPermissionItem);
            } else if (("container-transaction" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: containerTransaction
                ContainerTransaction containerTransactionItem = readContainerTransaction(elementReader, context);
                if (containerTransaction == null) {
                    containerTransaction = assemblyDescriptor.containerTransaction;
                    if (containerTransaction != null) {
                        containerTransaction.clear();
                    } else {
                        containerTransaction = new ArrayList<ContainerTransaction>();
                    }
                }
                containerTransaction.add(containerTransactionItem);
            } else if (("container-concurrency" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: containerConcurrency
                ContainerConcurrency containerConcurrencyItem = readContainerConcurrency(elementReader, context);
                if (containerConcurrency == null) {
                    containerConcurrency = assemblyDescriptor.containerConcurrency;
                    if (containerConcurrency != null) {
                        containerConcurrency.clear();
                    } else {
                        containerConcurrency = new ArrayList<ContainerConcurrency>();
                    }
                }
                containerConcurrency.add(containerConcurrencyItem);
            } else if (("interceptor-binding" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: interceptorBinding
                InterceptorBinding interceptorBindingItem = readInterceptorBinding(elementReader, context);
                if (interceptorBinding == null) {
                    interceptorBinding = assemblyDescriptor.interceptorBinding;
                    if (interceptorBinding != null) {
                        interceptorBinding.clear();
                    } else {
                        interceptorBinding = new ArrayList<InterceptorBinding>();
                    }
                }
                interceptorBinding.add(interceptorBindingItem);
            } else if (("message-destination" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestination
                MessageDestination messageDestinationItem = readMessageDestination(elementReader, context);
                if (messageDestination == null) {
                    messageDestination = assemblyDescriptor.messageDestination;
                    if (messageDestination != null) {
                        messageDestination.clear();
                    } else {
                        messageDestination = new ArrayList<MessageDestination>();
                    }
                }
                messageDestination.add(messageDestinationItem);
            } else if (("exclude-list" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: excludeList
                ExcludeList excludeList = readExcludeList(elementReader, context);
                assemblyDescriptor.excludeList = excludeList;
            } else if (("application-exception" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: applicationException
                ApplicationException applicationExceptionItem = readApplicationException(elementReader, context);
                if (applicationException == null) {
                    applicationException = assemblyDescriptor.applicationException;
                    if (applicationException != null) {
                        applicationException.clear();
                    } else {
                        applicationException = new KeyedCollection<String, ApplicationException>();
                    }
                }
                applicationException.add(applicationExceptionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "security-role"), new QName("http://java.sun.com/xml/ns/javaee", "method-permission"), new QName("http://java.sun.com/xml/ns/javaee", "container-transaction"), new QName("http://java.sun.com/xml/ns/javaee", "container-concurrency"), new QName("http://java.sun.com/xml/ns/javaee", "interceptor-binding"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination"), new QName("http://java.sun.com/xml/ns/javaee", "exclude-list"), new QName("http://java.sun.com/xml/ns/javaee", "application-exception"));
            }
        }
        if (securityRole != null) {
            assemblyDescriptor.securityRole = securityRole;
        }
        if (methodPermission != null) {
            assemblyDescriptor.methodPermission = methodPermission;
        }
        if (containerTransaction != null) {
            assemblyDescriptor.containerTransaction = containerTransaction;
        }
        if (containerConcurrency != null) {
            assemblyDescriptor.containerConcurrency = containerConcurrency;
        }
        if (interceptorBinding != null) {
            assemblyDescriptor.interceptorBinding = interceptorBinding;
        }
        if (messageDestination != null) {
            assemblyDescriptor.messageDestination = messageDestination;
        }
        if (applicationException != null) {
            assemblyDescriptor.applicationException = applicationException;
        }

        context.afterUnmarshal(assemblyDescriptor, LifecycleCallback.NONE);

        return assemblyDescriptor;
    }

    public final AssemblyDescriptor read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, AssemblyDescriptor assemblyDescriptor, RuntimeContext context)
            throws Exception {
        if (assemblyDescriptor == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (AssemblyDescriptor.class != assemblyDescriptor.getClass()) {
            context.unexpectedSubclass(writer, assemblyDescriptor, AssemblyDescriptor.class);
            return;
        }

        context.beforeMarshal(assemblyDescriptor, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = assemblyDescriptor.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(assemblyDescriptor, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: securityRole
        List<SecurityRole> securityRole = assemblyDescriptor.securityRole;
        if (securityRole != null) {
            for (SecurityRole securityRoleItem : securityRole) {
                if (securityRoleItem != null) {
                    writer.writeStartElement(prefix, "security-role", "http://java.sun.com/xml/ns/javaee");
                    writeSecurityRole(writer, securityRoleItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(assemblyDescriptor, "securityRole");
                }
            }
        }

        // ELEMENT: methodPermission
        List<MethodPermission> methodPermission = assemblyDescriptor.methodPermission;
        if (methodPermission != null) {
            for (MethodPermission methodPermissionItem : methodPermission) {
                if (methodPermissionItem != null) {
                    writer.writeStartElement(prefix, "method-permission", "http://java.sun.com/xml/ns/javaee");
                    writeMethodPermission(writer, methodPermissionItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(assemblyDescriptor, "methodPermission");
                }
            }
        }

        // ELEMENT: containerTransaction
        List<ContainerTransaction> containerTransaction = assemblyDescriptor.containerTransaction;
        if (containerTransaction != null) {
            for (ContainerTransaction containerTransactionItem : containerTransaction) {
                if (containerTransactionItem != null) {
                    writer.writeStartElement(prefix, "container-transaction", "http://java.sun.com/xml/ns/javaee");
                    writeContainerTransaction(writer, containerTransactionItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(assemblyDescriptor, "containerTransaction");
                }
            }
        }

        // ELEMENT: containerConcurrency
        List<ContainerConcurrency> containerConcurrency = assemblyDescriptor.containerConcurrency;
        if (containerConcurrency != null) {
            for (ContainerConcurrency containerConcurrencyItem : containerConcurrency) {
                if (containerConcurrencyItem != null) {
                    writer.writeStartElement(prefix, "container-concurrency", "http://java.sun.com/xml/ns/javaee");
                    writeContainerConcurrency(writer, containerConcurrencyItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(assemblyDescriptor, "containerConcurrency");
                }
            }
        }

        // ELEMENT: interceptorBinding
        List<InterceptorBinding> interceptorBinding = assemblyDescriptor.interceptorBinding;
        if (interceptorBinding != null) {
            for (InterceptorBinding interceptorBindingItem : interceptorBinding) {
                if (interceptorBindingItem != null) {
                    writer.writeStartElement(prefix, "interceptor-binding", "http://java.sun.com/xml/ns/javaee");
                    writeInterceptorBinding(writer, interceptorBindingItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(assemblyDescriptor, "interceptorBinding");
                }
            }
        }

        // ELEMENT: messageDestination
        List<MessageDestination> messageDestination = assemblyDescriptor.messageDestination;
        if (messageDestination != null) {
            for (MessageDestination messageDestinationItem : messageDestination) {
                if (messageDestinationItem != null) {
                    writer.writeStartElement(prefix, "message-destination", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestination(writer, messageDestinationItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(assemblyDescriptor, "messageDestination");
                }
            }
        }

        // ELEMENT: excludeList
        ExcludeList excludeList = assemblyDescriptor.excludeList;
        if (excludeList != null) {
            writer.writeStartElement(prefix, "exclude-list", "http://java.sun.com/xml/ns/javaee");
            writeExcludeList(writer, excludeList, context);
            writer.writeEndElement();
        }

        // ELEMENT: applicationException
        KeyedCollection<String, ApplicationException> applicationException = assemblyDescriptor.applicationException;
        if (applicationException != null) {
            for (ApplicationException applicationExceptionItem : applicationException) {
                if (applicationExceptionItem != null) {
                    writer.writeStartElement(prefix, "application-exception", "http://java.sun.com/xml/ns/javaee");
                    writeApplicationException(writer, applicationExceptionItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(assemblyDescriptor, "applicationException");
                }
            }
        }

        context.afterMarshal(assemblyDescriptor, LifecycleCallback.NONE);
    }

}
