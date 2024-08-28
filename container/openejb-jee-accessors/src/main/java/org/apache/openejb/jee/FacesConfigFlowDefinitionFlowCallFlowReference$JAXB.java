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


import static org.apache.openejb.jee.JavaIdentifier$JAXB.readJavaIdentifier;
import static org.apache.openejb.jee.JavaIdentifier$JAXB.writeJavaIdentifier;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinitionFlowCallFlowReference$JAXB
    extends JAXBObject<FacesConfigFlowDefinitionFlowCallFlowReference>
{


    public FacesConfigFlowDefinitionFlowCallFlowReference$JAXB() {
        super(FacesConfigFlowDefinitionFlowCallFlowReference.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definition-flow-call-flow-referenceType".intern()), JavaIdentifier$JAXB.class);
    }

    public static FacesConfigFlowDefinitionFlowCallFlowReference readFacesConfigFlowDefinitionFlowCallFlowReference(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinitionFlowCallFlowReference(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowCallFlowReference facesConfigFlowDefinitionFlowCallFlowReference, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionFlowCallFlowReference, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowCallFlowReference facesConfigFlowDefinitionFlowCallFlowReference, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionFlowCallFlowReference, context);
    }

    public static final FacesConfigFlowDefinitionFlowCallFlowReference _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinitionFlowCallFlowReference facesConfigFlowDefinitionFlowCallFlowReference = new FacesConfigFlowDefinitionFlowCallFlowReference();
        context.beforeUnmarshal(facesConfigFlowDefinitionFlowCallFlowReference, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definition-flow-call-flow-referenceType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinitionFlowCallFlowReference.class);
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
            if (("flow-document-id" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: flowDocumentId
                JavaIdentifier flowDocumentId = readJavaIdentifier(elementReader, context);
                facesConfigFlowDefinitionFlowCallFlowReference.flowDocumentId = flowDocumentId;
            } else if (("flow-id" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: flowId
                JavaIdentifier flowId = readJavaIdentifier(elementReader, context);
                facesConfigFlowDefinitionFlowCallFlowReference.flowId = flowId;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "flow-document-id"), new QName("http://java.sun.com/xml/ns/javaee", "flow-id"));
            }
        }

        context.afterUnmarshal(facesConfigFlowDefinitionFlowCallFlowReference, LifecycleCallback.NONE);

        return facesConfigFlowDefinitionFlowCallFlowReference;
    }

    public final FacesConfigFlowDefinitionFlowCallFlowReference read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFlowCallFlowReference facesConfigFlowDefinitionFlowCallFlowReference, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinitionFlowCallFlowReference == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesConfigFlowDefinitionFlowCallFlowReference.class!= facesConfigFlowDefinitionFlowCallFlowReference.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinitionFlowCallFlowReference, FacesConfigFlowDefinitionFlowCallFlowReference.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinitionFlowCallFlowReference, LifecycleCallback.NONE);


        // ELEMENT: flowDocumentId
        JavaIdentifier flowDocumentId = facesConfigFlowDefinitionFlowCallFlowReference.flowDocumentId;
        if (flowDocumentId!= null) {
            writer.writeStartElement(prefix, "flow-document-id", "http://java.sun.com/xml/ns/javaee");
            writeJavaIdentifier(writer, flowDocumentId, context);
            writer.writeEndElement();
        }

        // ELEMENT: flowId
        JavaIdentifier flowId = facesConfigFlowDefinitionFlowCallFlowReference.flowId;
        if (flowId!= null) {
            writer.writeStartElement(prefix, "flow-id", "http://java.sun.com/xml/ns/javaee");
            writeJavaIdentifier(writer, flowId, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesConfigFlowDefinitionFlowCallFlowReference, "flowId");
        }

        context.afterMarshal(facesConfigFlowDefinitionFlowCallFlowReference, LifecycleCallback.NONE);
    }

}
