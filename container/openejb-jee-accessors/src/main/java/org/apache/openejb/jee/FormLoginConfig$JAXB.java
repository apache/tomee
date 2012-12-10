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

@SuppressWarnings({
        "StringEquality"
})
public class FormLoginConfig$JAXB
        extends JAXBObject<FormLoginConfig> {


    public FormLoginConfig$JAXB() {
        super(FormLoginConfig.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "form-login-configType".intern()));
    }

    public static FormLoginConfig readFormLoginConfig(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFormLoginConfig(XoXMLStreamWriter writer, FormLoginConfig formLoginConfig, RuntimeContext context)
            throws Exception {
        _write(writer, formLoginConfig, context);
    }

    public void write(XoXMLStreamWriter writer, FormLoginConfig formLoginConfig, RuntimeContext context)
            throws Exception {
        _write(writer, formLoginConfig, context);
    }

    public final static FormLoginConfig _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FormLoginConfig formLoginConfig = new FormLoginConfig();
        context.beforeUnmarshal(formLoginConfig, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("form-login-configType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FormLoginConfig.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, formLoginConfig);
                formLoginConfig.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("form-login-page" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: formLoginPage
                String formLoginPageRaw = elementReader.getElementAsString();

                String formLoginPage;
                try {
                    formLoginPage = Adapters.collapsedStringAdapterAdapter.unmarshal(formLoginPageRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                formLoginConfig.formLoginPage = formLoginPage;
            } else if (("form-error-page" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: formErrorPage
                String formErrorPageRaw = elementReader.getElementAsString();

                String formErrorPage;
                try {
                    formErrorPage = Adapters.collapsedStringAdapterAdapter.unmarshal(formErrorPageRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                formLoginConfig.formErrorPage = formErrorPage;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "form-login-page"), new QName("http://java.sun.com/xml/ns/javaee", "form-error-page"));
            }
        }

        context.afterUnmarshal(formLoginConfig, LifecycleCallback.NONE);

        return formLoginConfig;
    }

    public final FormLoginConfig read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FormLoginConfig formLoginConfig, RuntimeContext context)
            throws Exception {
        if (formLoginConfig == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FormLoginConfig.class != formLoginConfig.getClass()) {
            context.unexpectedSubclass(writer, formLoginConfig, FormLoginConfig.class);
            return;
        }

        context.beforeMarshal(formLoginConfig, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = formLoginConfig.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(formLoginConfig, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: formLoginPage
        String formLoginPageRaw = formLoginConfig.formLoginPage;
        String formLoginPage = null;
        try {
            formLoginPage = Adapters.collapsedStringAdapterAdapter.marshal(formLoginPageRaw);
        } catch (Exception e) {
            context.xmlAdapterError(formLoginConfig, "formLoginPage", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (formLoginPage != null) {
            writer.writeStartElement(prefix, "form-login-page", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(formLoginPage);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(formLoginConfig, "formLoginPage");
        }

        // ELEMENT: formErrorPage
        String formErrorPageRaw = formLoginConfig.formErrorPage;
        String formErrorPage = null;
        try {
            formErrorPage = Adapters.collapsedStringAdapterAdapter.marshal(formErrorPageRaw);
        } catch (Exception e) {
            context.xmlAdapterError(formLoginConfig, "formErrorPage", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (formErrorPage != null) {
            writer.writeStartElement(prefix, "form-error-page", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(formErrorPage);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(formLoginConfig, "formErrorPage");
        }

        context.afterMarshal(formLoginConfig, LifecycleCallback.NONE);
    }

}
