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

import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Variable$JAXB
    extends JAXBObject<Variable> {


    public Variable$JAXB() {
        super(Variable.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "variableType".intern()), Text$JAXB.class);
    }

    public static Variable readVariable(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeVariable(final XoXMLStreamWriter writer, final Variable variable, final RuntimeContext context)
        throws Exception {
        _write(writer, variable, context);
    }

    public void write(final XoXMLStreamWriter writer, final Variable variable, final RuntimeContext context)
        throws Exception {
        _write(writer, variable, context);
    }

    public final static Variable _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Variable variable = new Variable();
        context.beforeUnmarshal(variable, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("variableType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Variable.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, variable);
                variable.id = id;
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
            } else if (("name-given" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nameGiven
                final String nameGivenRaw = elementReader.getElementAsString();

                final String nameGiven;
                try {
                    nameGiven = Adapters.collapsedStringAdapterAdapter.unmarshal(nameGivenRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variable.nameGiven = nameGiven;
            } else if (("name-from-attribute" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nameFromAttribute
                final String nameFromAttributeRaw = elementReader.getElementAsString();

                final String nameFromAttribute;
                try {
                    nameFromAttribute = Adapters.collapsedStringAdapterAdapter.unmarshal(nameFromAttributeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variable.nameFromAttribute = nameFromAttribute;
            } else if (("variable-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: variableClass
                final String variableClassRaw = elementReader.getElementAsString();

                final String variableClass;
                try {
                    variableClass = Adapters.collapsedStringAdapterAdapter.unmarshal(variableClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variable.variableClass = variableClass;
            } else if (("declare" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: declare
                final String declareRaw = elementReader.getElementAsString();

                final String declare;
                try {
                    declare = Adapters.collapsedStringAdapterAdapter.unmarshal(declareRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variable.declare = declare;
            } else if (("scope" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: scope
                final String scopeRaw = elementReader.getElementAsString();

                final String scope;
                try {
                    scope = Adapters.collapsedStringAdapterAdapter.unmarshal(scopeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variable.scope = scope;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "name-given"), new QName("http://java.sun.com/xml/ns/javaee", "name-from-attribute"), new QName("http://java.sun.com/xml/ns/javaee", "variable-class"), new QName("http://java.sun.com/xml/ns/javaee", "declare"), new QName("http://java.sun.com/xml/ns/javaee", "scope"));
            }
        }
        if (descriptions != null) {
            try {
                variable.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Variable.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(variable, LifecycleCallback.NONE);

        return variable;
    }

    public final Variable read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Variable variable, RuntimeContext context)
        throws Exception {
        if (variable == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Variable.class != variable.getClass()) {
            context.unexpectedSubclass(writer, variable, Variable.class);
            return;
        }

        context.beforeMarshal(variable, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = variable.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(variable, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = variable.getDescriptions();
        } catch (final Exception e) {
            context.getterError(variable, "descriptions", Variable.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(variable, "descriptions");
                }
            }
        }

        // ELEMENT: nameGiven
        final String nameGivenRaw = variable.nameGiven;
        String nameGiven = null;
        try {
            nameGiven = Adapters.collapsedStringAdapterAdapter.marshal(nameGivenRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(variable, "nameGiven", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (nameGiven != null) {
            writer.writeStartElement(prefix, "name-given", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(nameGiven);
            writer.writeEndElement();
        }

        // ELEMENT: nameFromAttribute
        final String nameFromAttributeRaw = variable.nameFromAttribute;
        String nameFromAttribute = null;
        try {
            nameFromAttribute = Adapters.collapsedStringAdapterAdapter.marshal(nameFromAttributeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(variable, "nameFromAttribute", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (nameFromAttribute != null) {
            writer.writeStartElement(prefix, "name-from-attribute", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(nameFromAttribute);
            writer.writeEndElement();
        }

        // ELEMENT: variableClass
        final String variableClassRaw = variable.variableClass;
        String variableClass = null;
        try {
            variableClass = Adapters.collapsedStringAdapterAdapter.marshal(variableClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(variable, "variableClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (variableClass != null) {
            writer.writeStartElement(prefix, "variable-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(variableClass);
            writer.writeEndElement();
        }

        // ELEMENT: declare
        final String declareRaw = variable.declare;
        String declare = null;
        try {
            declare = Adapters.collapsedStringAdapterAdapter.marshal(declareRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(variable, "declare", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (declare != null) {
            writer.writeStartElement(prefix, "declare", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(declare);
            writer.writeEndElement();
        }

        // ELEMENT: scope
        final String scopeRaw = variable.scope;
        String scope = null;
        try {
            scope = Adapters.collapsedStringAdapterAdapter.marshal(scopeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(variable, "scope", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (scope != null) {
            writer.writeStartElement(prefix, "scope", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(scope);
            writer.writeEndElement();
        }

        context.afterMarshal(variable, LifecycleCallback.NONE);
    }

}
