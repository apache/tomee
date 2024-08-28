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
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.FacesConfigFlowDefinitionParameterValue$JAXB.readFacesConfigFlowDefinitionParameterValue;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionParameterValue$JAXB.writeFacesConfigFlowDefinitionParameterValue;
import static org.apache.openejb.jee.JavaIdentifier$JAXB.readJavaIdentifier;
import static org.apache.openejb.jee.JavaIdentifier$JAXB.writeJavaIdentifier;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinitionInboundParameter$JAXB
    extends JAXBObject<FacesConfigFlowDefinitionInboundParameter>
{


    public FacesConfigFlowDefinitionInboundParameter$JAXB() {
        super(FacesConfigFlowDefinitionInboundParameter.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definition-inbound-parameterType".intern()), JavaIdentifier$JAXB.class, FacesConfigFlowDefinitionParameterValue$JAXB.class);
    }

    public static FacesConfigFlowDefinitionInboundParameter readFacesConfigFlowDefinitionInboundParameter(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinitionInboundParameter(XoXMLStreamWriter writer, FacesConfigFlowDefinitionInboundParameter facesConfigFlowDefinitionInboundParameter, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionInboundParameter, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionInboundParameter facesConfigFlowDefinitionInboundParameter, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionInboundParameter, context);
    }

    public static final FacesConfigFlowDefinitionInboundParameter _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinitionInboundParameter facesConfigFlowDefinitionInboundParameter = new FacesConfigFlowDefinitionInboundParameter();
        context.beforeUnmarshal(facesConfigFlowDefinitionInboundParameter, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definition-inbound-parameterType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinitionInboundParameter.class);
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
            if (("name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                JavaIdentifier name = readJavaIdentifier(elementReader, context);
                facesConfigFlowDefinitionInboundParameter.name = name;
            } else if (("value" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: value
                FacesConfigFlowDefinitionParameterValue value = readFacesConfigFlowDefinitionParameterValue(elementReader, context);
                facesConfigFlowDefinitionInboundParameter.value = value;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "value"));
            }
        }

        context.afterUnmarshal(facesConfigFlowDefinitionInboundParameter, LifecycleCallback.NONE);

        return facesConfigFlowDefinitionInboundParameter;
    }

    public final FacesConfigFlowDefinitionInboundParameter read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionInboundParameter facesConfigFlowDefinitionInboundParameter, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinitionInboundParameter == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesConfigFlowDefinitionInboundParameter.class!= facesConfigFlowDefinitionInboundParameter.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinitionInboundParameter, FacesConfigFlowDefinitionInboundParameter.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinitionInboundParameter, LifecycleCallback.NONE);


        // ELEMENT: name
        JavaIdentifier name = facesConfigFlowDefinitionInboundParameter.name;
        if (name!= null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writeJavaIdentifier(writer, name, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesConfigFlowDefinitionInboundParameter, "name");
        }

        // ELEMENT: value
        FacesConfigFlowDefinitionParameterValue value = facesConfigFlowDefinitionInboundParameter.value;
        if (value!= null) {
            writer.writeStartElement(prefix, "value", "http://java.sun.com/xml/ns/javaee");
            writeFacesConfigFlowDefinitionParameterValue(writer, value, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesConfigFlowDefinitionInboundParameter, "value");
        }

        context.afterMarshal(facesConfigFlowDefinitionInboundParameter, LifecycleCallback.NONE);
    }

}
