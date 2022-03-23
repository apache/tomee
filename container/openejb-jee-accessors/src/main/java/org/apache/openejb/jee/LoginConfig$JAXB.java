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

import static org.apache.openejb.jee.FormLoginConfig$JAXB.readFormLoginConfig;
import static org.apache.openejb.jee.FormLoginConfig$JAXB.writeFormLoginConfig;

@SuppressWarnings({
    "StringEquality"
})
public class LoginConfig$JAXB
    extends JAXBObject<LoginConfig> {


    public LoginConfig$JAXB() {
        super(LoginConfig.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "login-configType".intern()), FormLoginConfig$JAXB.class);
    }

    public static LoginConfig readLoginConfig(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeLoginConfig(final XoXMLStreamWriter writer, final LoginConfig loginConfig, final RuntimeContext context)
        throws Exception {
        _write(writer, loginConfig, context);
    }

    public void write(final XoXMLStreamWriter writer, final LoginConfig loginConfig, final RuntimeContext context)
        throws Exception {
        _write(writer, loginConfig, context);
    }

    public final static LoginConfig _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final LoginConfig loginConfig = new LoginConfig();
        context.beforeUnmarshal(loginConfig, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("login-configType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, LoginConfig.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, loginConfig);
                loginConfig.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("auth-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: authMethod
                final String authMethodRaw = elementReader.getElementAsString();

                final String authMethod;
                try {
                    authMethod = Adapters.collapsedStringAdapterAdapter.unmarshal(authMethodRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                loginConfig.authMethod = authMethod;
            } else if (("realm-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: realmName
                final String realmNameRaw = elementReader.getElementAsString();

                final String realmName;
                try {
                    realmName = Adapters.collapsedStringAdapterAdapter.unmarshal(realmNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                loginConfig.realmName = realmName;
            } else if (("form-login-config" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: formLoginConfig
                final FormLoginConfig formLoginConfig = readFormLoginConfig(elementReader, context);
                loginConfig.formLoginConfig = formLoginConfig;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "auth-method"), new QName("http://java.sun.com/xml/ns/javaee", "realm-name"), new QName("http://java.sun.com/xml/ns/javaee", "form-login-config"));
            }
        }

        context.afterUnmarshal(loginConfig, LifecycleCallback.NONE);

        return loginConfig;
    }

    public final LoginConfig read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final LoginConfig loginConfig, RuntimeContext context)
        throws Exception {
        if (loginConfig == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (LoginConfig.class != loginConfig.getClass()) {
            context.unexpectedSubclass(writer, loginConfig, LoginConfig.class);
            return;
        }

        context.beforeMarshal(loginConfig, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = loginConfig.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(loginConfig, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: authMethod
        final String authMethodRaw = loginConfig.authMethod;
        String authMethod = null;
        try {
            authMethod = Adapters.collapsedStringAdapterAdapter.marshal(authMethodRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(loginConfig, "authMethod", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (authMethod != null) {
            writer.writeStartElement(prefix, "auth-method", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(authMethod);
            writer.writeEndElement();
        }

        // ELEMENT: realmName
        final String realmNameRaw = loginConfig.realmName;
        String realmName = null;
        try {
            realmName = Adapters.collapsedStringAdapterAdapter.marshal(realmNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(loginConfig, "realmName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (realmName != null) {
            writer.writeStartElement(prefix, "realm-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(realmName);
            writer.writeEndElement();
        }

        // ELEMENT: formLoginConfig
        final FormLoginConfig formLoginConfig = loginConfig.formLoginConfig;
        if (formLoginConfig != null) {
            writer.writeStartElement(prefix, "form-login-config", "http://java.sun.com/xml/ns/javaee");
            writeFormLoginConfig(writer, formLoginConfig, context);
            writer.writeEndElement();
        }

        context.afterMarshal(loginConfig, LifecycleCallback.NONE);
    }

}
