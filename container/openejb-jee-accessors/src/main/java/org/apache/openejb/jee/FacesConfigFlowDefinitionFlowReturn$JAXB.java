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


import static org.apache.openejb.jee.XmlString$JAXB.readXmlString;
import static org.apache.openejb.jee.XmlString$JAXB.writeXmlString;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinitionFlowReturn$JAXB
    extends JAXBObject<FacesConfigFlowDefinitionFlowReturn>
{


    public FacesConfigFlowDefinitionFlowReturn$JAXB() {
        super(FacesConfigFlowDefinitionFlowReturn.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definition-flow-returnType".intern()), XmlString$JAXB.class);
    }

    public static FacesConfigFlowDefinitionFlowReturn readFacesConfigFlowDefinitionFlowReturn(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinitionFlowReturn(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowReturn facesConfigFlowDefinitionFlowReturn, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionFlowReturn, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowReturn facesConfigFlowDefinitionFlowReturn, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionFlowReturn, context);
    }

    public static final FacesConfigFlowDefinitionFlowReturn _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinitionFlowReturn facesConfigFlowDefinitionFlowReturn = new FacesConfigFlowDefinitionFlowReturn();
        context.beforeUnmarshal(facesConfigFlowDefinitionFlowReturn, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definition-flow-returnType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinitionFlowReturn.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesConfigFlowDefinitionFlowReturn);
                facesConfigFlowDefinitionFlowReturn.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("from-outcome" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: fromOutcome
                XmlString fromOutcome = readXmlString(elementReader, context);
                facesConfigFlowDefinitionFlowReturn.fromOutcome = fromOutcome;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "from-outcome"));
            }
        }

        context.afterUnmarshal(facesConfigFlowDefinitionFlowReturn, LifecycleCallback.NONE);

        return facesConfigFlowDefinitionFlowReturn;
    }

    public final FacesConfigFlowDefinitionFlowReturn read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowReturn facesConfigFlowDefinitionFlowReturn, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinitionFlowReturn == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesConfigFlowDefinitionFlowReturn.class!= facesConfigFlowDefinitionFlowReturn.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinitionFlowReturn, FacesConfigFlowDefinitionFlowReturn.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinitionFlowReturn, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesConfigFlowDefinitionFlowReturn.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConfigFlowDefinitionFlowReturn, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: fromOutcome
        XmlString fromOutcome = facesConfigFlowDefinitionFlowReturn.fromOutcome;
        if (fromOutcome!= null) {
            writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "from-outcome");
            writeXmlString(writer, fromOutcome, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesConfigFlowDefinitionFlowReturn, "fromOutcome");
        }

        context.afterMarshal(facesConfigFlowDefinitionFlowReturn, LifecycleCallback.NONE);
    }

}
