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

import static org.apache.openejb.jee.AuthenticationMechanism$JAXB.readAuthenticationMechanism;
import static org.apache.openejb.jee.AuthenticationMechanism$JAXB.writeAuthenticationMechanism;
import static org.apache.openejb.jee.ConnectionDefinition$JAXB.readConnectionDefinition;
import static org.apache.openejb.jee.ConnectionDefinition$JAXB.writeConnectionDefinition;
import static org.apache.openejb.jee.TransactionSupportType$JAXB.parseTransactionSupportType;
import static org.apache.openejb.jee.TransactionSupportType$JAXB.toStringTransactionSupportType;

@SuppressWarnings({
    "StringEquality"
})
public class OutboundResourceAdapter$JAXB
    extends JAXBObject<OutboundResourceAdapter> {


    public OutboundResourceAdapter$JAXB() {
        super(OutboundResourceAdapter.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "outbound-resourceadapterType".intern()), ConnectionDefinition$JAXB.class, TransactionSupportType$JAXB.class, AuthenticationMechanism$JAXB.class);
    }

    public static OutboundResourceAdapter readOutboundResourceAdapter(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeOutboundResourceAdapter(final XoXMLStreamWriter writer, final OutboundResourceAdapter outboundResourceAdapter, final RuntimeContext context)
        throws Exception {
        _write(writer, outboundResourceAdapter, context);
    }

    public void write(final XoXMLStreamWriter writer, final OutboundResourceAdapter outboundResourceAdapter, final RuntimeContext context)
        throws Exception {
        _write(writer, outboundResourceAdapter, context);
    }

    public final static OutboundResourceAdapter _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final OutboundResourceAdapter outboundResourceAdapter = new OutboundResourceAdapter();
        context.beforeUnmarshal(outboundResourceAdapter, LifecycleCallback.NONE);

        List<ConnectionDefinition> connectionDefinition = null;
        List<AuthenticationMechanism> authenticationMechanism = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("outbound-resourceadapterType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, OutboundResourceAdapter.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, outboundResourceAdapter);
                outboundResourceAdapter.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("connection-definition" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: connectionDefinition
                final ConnectionDefinition connectionDefinitionItem = readConnectionDefinition(elementReader, context);
                if (connectionDefinition == null) {
                    connectionDefinition = outboundResourceAdapter.connectionDefinition;
                    if (connectionDefinition != null) {
                        connectionDefinition.clear();
                    } else {
                        connectionDefinition = new ArrayList<ConnectionDefinition>();
                    }
                }
                connectionDefinition.add(connectionDefinitionItem);
            } else if (("transaction-support" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: transactionSupport
                final TransactionSupportType transactionSupport = parseTransactionSupportType(elementReader, context, elementReader.getElementAsString());
                if (transactionSupport != null) {
                    outboundResourceAdapter.transactionSupport = transactionSupport;
                }
            } else if (("authentication-mechanism" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: authenticationMechanism
                final AuthenticationMechanism authenticationMechanismItem = readAuthenticationMechanism(elementReader, context);
                if (authenticationMechanism == null) {
                    authenticationMechanism = outboundResourceAdapter.authenticationMechanism;
                    if (authenticationMechanism != null) {
                        authenticationMechanism.clear();
                    } else {
                        authenticationMechanism = new ArrayList<AuthenticationMechanism>();
                    }
                }
                authenticationMechanism.add(authenticationMechanismItem);
            } else if (("reauthentication-support" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: reauthenticationSupport
                final Boolean reauthenticationSupport = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                outboundResourceAdapter.reauthenticationSupport = reauthenticationSupport;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "connection-definition"), new QName("http://java.sun.com/xml/ns/javaee", "transaction-support"), new QName("http://java.sun.com/xml/ns/javaee", "authentication-mechanism"), new QName("http://java.sun.com/xml/ns/javaee", "reauthentication-support"));
            }
        }
        if (connectionDefinition != null) {
            outboundResourceAdapter.connectionDefinition = connectionDefinition;
        }
        if (authenticationMechanism != null) {
            outboundResourceAdapter.authenticationMechanism = authenticationMechanism;
        }

        context.afterUnmarshal(outboundResourceAdapter, LifecycleCallback.NONE);

        return outboundResourceAdapter;
    }

    public final OutboundResourceAdapter read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final OutboundResourceAdapter outboundResourceAdapter, RuntimeContext context)
        throws Exception {
        if (outboundResourceAdapter == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (OutboundResourceAdapter.class != outboundResourceAdapter.getClass()) {
            context.unexpectedSubclass(writer, outboundResourceAdapter, OutboundResourceAdapter.class);
            return;
        }

        context.beforeMarshal(outboundResourceAdapter, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = outboundResourceAdapter.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(outboundResourceAdapter, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: connectionDefinition
        final List<ConnectionDefinition> connectionDefinition = outboundResourceAdapter.connectionDefinition;
        if (connectionDefinition != null) {
            for (final ConnectionDefinition connectionDefinitionItem : connectionDefinition) {
                if (connectionDefinitionItem != null) {
                    writer.writeStartElement(prefix, "connection-definition", "http://java.sun.com/xml/ns/javaee");
                    writeConnectionDefinition(writer, connectionDefinitionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: transactionSupport
        final TransactionSupportType transactionSupport = outboundResourceAdapter.transactionSupport;
        if (transactionSupport != null) {
            writer.writeStartElement(prefix, "transaction-support", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringTransactionSupportType(outboundResourceAdapter, null, context, transactionSupport));
            writer.writeEndElement();
        }

        // ELEMENT: authenticationMechanism
        final List<AuthenticationMechanism> authenticationMechanism = outboundResourceAdapter.authenticationMechanism;
        if (authenticationMechanism != null) {
            for (final AuthenticationMechanism authenticationMechanismItem : authenticationMechanism) {
                if (authenticationMechanismItem != null) {
                    writer.writeStartElement(prefix, "authentication-mechanism", "http://java.sun.com/xml/ns/javaee");
                    writeAuthenticationMechanism(writer, authenticationMechanismItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: reauthenticationSupport
        final Boolean reauthenticationSupport = outboundResourceAdapter.reauthenticationSupport;
        if (reauthenticationSupport != null) {
            writer.writeStartElement(prefix, "reauthentication-support", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(reauthenticationSupport));
            writer.writeEndElement();
        }

        context.afterMarshal(outboundResourceAdapter, LifecycleCallback.NONE);
    }

}
