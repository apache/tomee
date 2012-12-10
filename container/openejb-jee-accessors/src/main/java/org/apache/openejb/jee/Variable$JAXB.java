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

    public static Variable readVariable(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeVariable(XoXMLStreamWriter writer, Variable variable, RuntimeContext context)
            throws Exception {
        _write(writer, variable, context);
    }

    public void write(XoXMLStreamWriter writer, Variable variable, RuntimeContext context)
            throws Exception {
        _write(writer, variable, context);
    }

    public final static Variable _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Variable variable = new Variable();
        context.beforeUnmarshal(variable, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("variableType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Variable.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, variable);
                variable.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("name-given" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nameGiven
                String nameGivenRaw = elementReader.getElementAsString();

                String nameGiven;
                try {
                    nameGiven = Adapters.collapsedStringAdapterAdapter.unmarshal(nameGivenRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variable.nameGiven = nameGiven;
            } else if (("name-from-attribute" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nameFromAttribute
                String nameFromAttributeRaw = elementReader.getElementAsString();

                String nameFromAttribute;
                try {
                    nameFromAttribute = Adapters.collapsedStringAdapterAdapter.unmarshal(nameFromAttributeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variable.nameFromAttribute = nameFromAttribute;
            } else if (("variable-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: variableClass
                String variableClassRaw = elementReader.getElementAsString();

                String variableClass;
                try {
                    variableClass = Adapters.collapsedStringAdapterAdapter.unmarshal(variableClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variable.variableClass = variableClass;
            } else if (("declare" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: declare
                String declareRaw = elementReader.getElementAsString();

                String declare;
                try {
                    declare = Adapters.collapsedStringAdapterAdapter.unmarshal(declareRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variable.declare = declare;
            } else if (("scope" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: scope
                String scopeRaw = elementReader.getElementAsString();

                String scope;
                try {
                    scope = Adapters.collapsedStringAdapterAdapter.unmarshal(scopeRaw);
                } catch (Exception e) {
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
            } catch (Exception e) {
                context.setterError(reader, Variable.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(variable, LifecycleCallback.NONE);

        return variable;
    }

    public final Variable read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, Variable variable, RuntimeContext context)
            throws Exception {
        if (variable == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Variable.class != variable.getClass()) {
            context.unexpectedSubclass(writer, variable, Variable.class);
            return;
        }

        context.beforeMarshal(variable, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = variable.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(variable, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = variable.getDescriptions();
        } catch (Exception e) {
            context.getterError(variable, "descriptions", Variable.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
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
        String nameGivenRaw = variable.nameGiven;
        String nameGiven = null;
        try {
            nameGiven = Adapters.collapsedStringAdapterAdapter.marshal(nameGivenRaw);
        } catch (Exception e) {
            context.xmlAdapterError(variable, "nameGiven", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (nameGiven != null) {
            writer.writeStartElement(prefix, "name-given", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(nameGiven);
            writer.writeEndElement();
        }

        // ELEMENT: nameFromAttribute
        String nameFromAttributeRaw = variable.nameFromAttribute;
        String nameFromAttribute = null;
        try {
            nameFromAttribute = Adapters.collapsedStringAdapterAdapter.marshal(nameFromAttributeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(variable, "nameFromAttribute", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (nameFromAttribute != null) {
            writer.writeStartElement(prefix, "name-from-attribute", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(nameFromAttribute);
            writer.writeEndElement();
        }

        // ELEMENT: variableClass
        String variableClassRaw = variable.variableClass;
        String variableClass = null;
        try {
            variableClass = Adapters.collapsedStringAdapterAdapter.marshal(variableClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(variable, "variableClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (variableClass != null) {
            writer.writeStartElement(prefix, "variable-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(variableClass);
            writer.writeEndElement();
        }

        // ELEMENT: declare
        String declareRaw = variable.declare;
        String declare = null;
        try {
            declare = Adapters.collapsedStringAdapterAdapter.marshal(declareRaw);
        } catch (Exception e) {
            context.xmlAdapterError(variable, "declare", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (declare != null) {
            writer.writeStartElement(prefix, "declare", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(declare);
            writer.writeEndElement();
        }

        // ELEMENT: scope
        String scopeRaw = variable.scope;
        String scope = null;
        try {
            scope = Adapters.collapsedStringAdapterAdapter.marshal(scopeRaw);
        } catch (Exception e) {
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
