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


import static org.apache.openejb.jee.FacesConfigFlowDefinitionFlowCallFlowReference$JAXB.readFacesConfigFlowDefinitionFlowCallFlowReference;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFlowCallFlowReference$JAXB.writeFacesConfigFlowDefinitionFlowCallFlowReference;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFlowCallOutboundParameter$JAXB.readFacesConfigFlowDefinitionFlowCallOutboundParameter;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFlowCallOutboundParameter$JAXB.writeFacesConfigFlowDefinitionFlowCallOutboundParameter;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinitionFlowCall$JAXB
    extends JAXBObject<FacesConfigFlowDefinitionFlowCall>
{


    public FacesConfigFlowDefinitionFlowCall$JAXB() {
        super(FacesConfigFlowDefinitionFlowCall.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definition-flow-callType".intern()), FacesConfigFlowDefinitionFlowCallFlowReference$JAXB.class, FacesConfigFlowDefinitionFlowCallOutboundParameter$JAXB.class);
    }

    public static FacesConfigFlowDefinitionFlowCall readFacesConfigFlowDefinitionFlowCall(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinitionFlowCall(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowCall facesConfigFlowDefinitionFlowCall, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionFlowCall, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowCall facesConfigFlowDefinitionFlowCall, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionFlowCall, context);
    }

    public static final FacesConfigFlowDefinitionFlowCall _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinitionFlowCall facesConfigFlowDefinitionFlowCall = new FacesConfigFlowDefinitionFlowCall();
        context.beforeUnmarshal(facesConfigFlowDefinitionFlowCall, LifecycleCallback.NONE);

        List<FacesConfigFlowDefinitionFlowCallOutboundParameter> outboundParameter = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definition-flow-callType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinitionFlowCall.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesConfigFlowDefinitionFlowCall);
                facesConfigFlowDefinitionFlowCall.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("flow-reference" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: flowReference
                FacesConfigFlowDefinitionFlowCallFlowReference flowReference = readFacesConfigFlowDefinitionFlowCallFlowReference(elementReader, context);
                facesConfigFlowDefinitionFlowCall.flowReference = flowReference;
            } else if (("outbound-parameter" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: outboundParameter
                FacesConfigFlowDefinitionFlowCallOutboundParameter outboundParameterItem = readFacesConfigFlowDefinitionFlowCallOutboundParameter(elementReader, context);
                if (outboundParameter == null) {
                    outboundParameter = facesConfigFlowDefinitionFlowCall.outboundParameter;
                    if (outboundParameter!= null) {
                        outboundParameter.clear();
                    } else {
                        outboundParameter = new ArrayList<>();
                    }
                }
                outboundParameter.add(outboundParameterItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "flow-reference"), new QName("http://java.sun.com/xml/ns/javaee", "outbound-parameter"));
            }
        }
        if (outboundParameter!= null) {
            facesConfigFlowDefinitionFlowCall.outboundParameter = outboundParameter;
        }

        context.afterUnmarshal(facesConfigFlowDefinitionFlowCall, LifecycleCallback.NONE);

        return facesConfigFlowDefinitionFlowCall;
    }

    public final FacesConfigFlowDefinitionFlowCall read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowCall facesConfigFlowDefinitionFlowCall, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinitionFlowCall == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesConfigFlowDefinitionFlowCall.class!= facesConfigFlowDefinitionFlowCall.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinitionFlowCall, FacesConfigFlowDefinitionFlowCall.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinitionFlowCall, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesConfigFlowDefinitionFlowCall.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConfigFlowDefinitionFlowCall, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: flowReference
        FacesConfigFlowDefinitionFlowCallFlowReference flowReference = facesConfigFlowDefinitionFlowCall.flowReference;
        if (flowReference!= null) {
            writer.writeStartElement(prefix, "flow-reference", "http://java.sun.com/xml/ns/javaee");
            writeFacesConfigFlowDefinitionFlowCallFlowReference(writer, flowReference, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesConfigFlowDefinitionFlowCall, "flowReference");
        }

        // ELEMENT: outboundParameter
        List<FacesConfigFlowDefinitionFlowCallOutboundParameter> outboundParameter = facesConfigFlowDefinitionFlowCall.outboundParameter;
        if (outboundParameter!= null) {
            for (FacesConfigFlowDefinitionFlowCallOutboundParameter outboundParameterItem: outboundParameter) {
                if (outboundParameterItem!= null) {
                    writer.writeStartElement(prefix, "outbound-parameter", "http://java.sun.com/xml/ns/javaee");
                    writeFacesConfigFlowDefinitionFlowCallOutboundParameter(writer, outboundParameterItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesConfigFlowDefinitionFlowCall, LifecycleCallback.NONE);
    }

}
