/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.ParamValue$JAXB.readParamValue;
import static org.apache.openejb.jee.ParamValue$JAXB.writeParamValue;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Validator$JAXB
    extends JAXBObject<Validator>
{


    public Validator$JAXB() {
        super(Validator.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "validatorType".intern()), Text$JAXB.class, ParamValue$JAXB.class);
    }

    public static Validator readValidator(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeValidator(XoXMLStreamWriter writer, Validator validator, RuntimeContext context)
        throws Exception
    {
        _write(writer, validator, context);
    }

    public void write(XoXMLStreamWriter writer, Validator validator, RuntimeContext context)
        throws Exception
    {
        _write(writer, validator, context);
    }

    public static final Validator _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Validator validator = new Validator();
        context.beforeUnmarshal(validator, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<ParamValue> initParam = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("validatorType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Validator.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, validator);
                validator.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<>();
                }
                descriptions.add(descriptionsItem);
            } else if (("validator-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: validatorClass
                String validatorClassRaw = elementReader.getElementText();

                String validatorClass;
                try {
                    validatorClass = Adapters.collapsedStringAdapterAdapter.unmarshal(validatorClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                validator.validatorClass = validatorClass;
            } else if (("init-param" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initParam
                ParamValue initParamItem = readParamValue(elementReader, context);
                if (initParam == null) {
                    initParam = validator.initParam;
                    if (initParam!= null) {
                        initParam.clear();
                    } else {
                        initParam = new ArrayList<>();
                    }
                }
                initParam.add(initParamItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "validator-class"), new QName("http://java.sun.com/xml/ns/javaee", "init-param"));
            }
        }
        if (descriptions!= null) {
            try {
                validator.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Validator.class, "setDescriptions", Text[].class, e);
            }
        }
        if (initParam!= null) {
            validator.initParam = initParam;
        }

        context.afterUnmarshal(validator, LifecycleCallback.NONE);

        return validator;
    }

    public final Validator read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Validator validator, RuntimeContext context)
        throws Exception
    {
        if (validator == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Validator.class!= validator.getClass()) {
            context.unexpectedSubclass(writer, validator, Validator.class);
            return ;
        }

        context.beforeMarshal(validator, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = validator.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(validator, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = validator.getDescriptions();
        } catch (Exception e) {
            context.getterError(validator, "descriptions", Validator.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(validator, "descriptions");
                }
            }
        }

        // ELEMENT: validatorClass
        String validatorClassRaw = validator.validatorClass;
        String validatorClass = null;
        try {
            validatorClass = Adapters.collapsedStringAdapterAdapter.marshal(validatorClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(validator, "validatorClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (validatorClass!= null) {
            writer.writeStartElement(prefix, "validator-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(validatorClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(validator, "validatorClass");
        }

        // ELEMENT: initParam
        List<ParamValue> initParam = validator.initParam;
        if (initParam!= null) {
            for (ParamValue initParamItem: initParam) {
                if (initParamItem!= null) {
                    writer.writeStartElement(prefix, "init-param", "http://java.sun.com/xml/ns/javaee");
                    writeParamValue(writer, initParamItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(validator, LifecycleCallback.NONE);
    }

}
