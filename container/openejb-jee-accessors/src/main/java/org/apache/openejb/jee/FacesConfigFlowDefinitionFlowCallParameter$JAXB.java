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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.FacesConfigFlowDefinitionParameterValue$JAXB.readFacesConfigFlowDefinitionParameterValue;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionParameterValue$JAXB.writeFacesConfigFlowDefinitionParameterValue;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinitionFlowCallParameter$JAXB
    extends JAXBObject<FacesConfigFlowDefinitionFlowCallParameter>
{


    public FacesConfigFlowDefinitionFlowCallParameter$JAXB() {
        super(FacesConfigFlowDefinitionFlowCallParameter.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definition-flow-call-parameterType".intern()), FacesConfigFlowDefinitionParameterValue$JAXB.class);
    }

    public static FacesConfigFlowDefinitionFlowCallParameter readFacesConfigFlowDefinitionFlowCallParameter(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinitionFlowCallParameter(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowCallParameter facesConfigFlowDefinitionFlowCallParameter, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionFlowCallParameter, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowCallParameter facesConfigFlowDefinitionFlowCallParameter, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionFlowCallParameter, context);
    }

    public static final FacesConfigFlowDefinitionFlowCallParameter _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinitionFlowCallParameter facesConfigFlowDefinitionFlowCallParameter = new FacesConfigFlowDefinitionFlowCallParameter();
        context.beforeUnmarshal(facesConfigFlowDefinitionFlowCallParameter, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definition-flow-call-parameterType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinitionFlowCallParameter.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: clazz
                String clazzRaw = elementReader.getElementText();

                String clazz;
                try {
                    clazz = Adapters.collapsedStringAdapterAdapter.unmarshal(clazzRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesConfigFlowDefinitionFlowCallParameter.clazz = clazz;
            } else if (("value" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: value
                FacesConfigFlowDefinitionParameterValue value = readFacesConfigFlowDefinitionParameterValue(elementReader, context);
                facesConfigFlowDefinitionFlowCallParameter.value = value;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "class"), new QName("http://java.sun.com/xml/ns/javaee", "value"));
            }
        }

        context.afterUnmarshal(facesConfigFlowDefinitionFlowCallParameter, LifecycleCallback.NONE);

        return facesConfigFlowDefinitionFlowCallParameter;
    }

    public final FacesConfigFlowDefinitionFlowCallParameter read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowCallParameter facesConfigFlowDefinitionFlowCallParameter, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinitionFlowCallParameter == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesConfigFlowDefinitionFlowCallParameter.class!= facesConfigFlowDefinitionFlowCallParameter.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinitionFlowCallParameter, FacesConfigFlowDefinitionFlowCallParameter.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinitionFlowCallParameter, LifecycleCallback.NONE);


        // ELEMENT: clazz
        String clazzRaw = facesConfigFlowDefinitionFlowCallParameter.clazz;
        String clazz = null;
        try {
            clazz = Adapters.collapsedStringAdapterAdapter.marshal(clazzRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesConfigFlowDefinitionFlowCallParameter, "clazz", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (clazz!= null) {
            writer.writeStartElement(prefix, "class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(clazz);
            writer.writeEndElement();
        }

        // ELEMENT: value
        FacesConfigFlowDefinitionParameterValue value = facesConfigFlowDefinitionFlowCallParameter.value;
        if (value!= null) {
            writer.writeStartElement(prefix, "value", "http://java.sun.com/xml/ns/javaee");
            writeFacesConfigFlowDefinitionParameterValue(writer, value, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesConfigFlowDefinitionFlowCallParameter, "value");
        }

        context.afterMarshal(facesConfigFlowDefinitionFlowCallParameter, LifecycleCallback.NONE);
    }

}
