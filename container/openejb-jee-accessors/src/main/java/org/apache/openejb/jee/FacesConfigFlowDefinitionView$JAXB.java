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


import static org.apache.openejb.jee.JavaIdentifier$JAXB.readJavaIdentifier;
import static org.apache.openejb.jee.JavaIdentifier$JAXB.writeJavaIdentifier;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinitionView$JAXB
    extends JAXBObject<FacesConfigFlowDefinitionView>
{


    public FacesConfigFlowDefinitionView$JAXB() {
        super(FacesConfigFlowDefinitionView.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definition-viewType".intern()), JavaIdentifier$JAXB.class);
    }

    public static FacesConfigFlowDefinitionView readFacesConfigFlowDefinitionView(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinitionView(XoXMLStreamWriter writer, FacesConfigFlowDefinitionView facesConfigFlowDefinitionView, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionView, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionView facesConfigFlowDefinitionView, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionView, context);
    }

    public static final FacesConfigFlowDefinitionView _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinitionView facesConfigFlowDefinitionView = new FacesConfigFlowDefinitionView();
        context.beforeUnmarshal(facesConfigFlowDefinitionView, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definition-viewType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinitionView.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesConfigFlowDefinitionView);
                facesConfigFlowDefinitionView.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("vdl-document" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: vdlDocument
                JavaIdentifier vdlDocument = readJavaIdentifier(elementReader, context);
                facesConfigFlowDefinitionView.vdlDocument = vdlDocument;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "vdl-document"));
            }
        }

        context.afterUnmarshal(facesConfigFlowDefinitionView, LifecycleCallback.NONE);

        return facesConfigFlowDefinitionView;
    }

    public final FacesConfigFlowDefinitionView read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionView facesConfigFlowDefinitionView, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinitionView == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesConfigFlowDefinitionView.class!= facesConfigFlowDefinitionView.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinitionView, FacesConfigFlowDefinitionView.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinitionView, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesConfigFlowDefinitionView.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConfigFlowDefinitionView, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: vdlDocument
        JavaIdentifier vdlDocument = facesConfigFlowDefinitionView.vdlDocument;
        if (vdlDocument!= null) {
            writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "vdl-document");
            writeJavaIdentifier(writer, vdlDocument, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesConfigFlowDefinitionView, "vdlDocument");
        }

        context.afterMarshal(facesConfigFlowDefinitionView, LifecycleCallback.NONE);
    }

}