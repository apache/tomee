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


import static org.apache.openejb.jee.FacesConfigFlowDefinitionSwitchCase$JAXB.readFacesConfigFlowDefinitionSwitchCase;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionSwitchCase$JAXB.writeFacesConfigFlowDefinitionSwitchCase;
import static org.apache.openejb.jee.XmlString$JAXB.readXmlString;
import static org.apache.openejb.jee.XmlString$JAXB.writeXmlString;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinitionSwitch$JAXB
    extends JAXBObject<FacesConfigFlowDefinitionSwitch>
{


    public FacesConfigFlowDefinitionSwitch$JAXB() {
        super(FacesConfigFlowDefinitionSwitch.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definition-switchType".intern()), FacesConfigFlowDefinitionSwitchCase$JAXB.class, XmlString$JAXB.class);
    }

    public static FacesConfigFlowDefinitionSwitch readFacesConfigFlowDefinitionSwitch(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinitionSwitch(XoXMLStreamWriter writer, FacesConfigFlowDefinitionSwitch facesConfigFlowDefinitionSwitch, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionSwitch, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionSwitch facesConfigFlowDefinitionSwitch, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionSwitch, context);
    }

    public static final FacesConfigFlowDefinitionSwitch _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinitionSwitch facesConfigFlowDefinitionSwitch = new FacesConfigFlowDefinitionSwitch();
        context.beforeUnmarshal(facesConfigFlowDefinitionSwitch, LifecycleCallback.NONE);

        List<FacesConfigFlowDefinitionSwitchCase> _case = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definition-switchType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinitionSwitch.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesConfigFlowDefinitionSwitch);
                facesConfigFlowDefinitionSwitch.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("case" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: _case
                FacesConfigFlowDefinitionSwitchCase _caseItem = readFacesConfigFlowDefinitionSwitchCase(elementReader, context);
                if (_case == null) {
                    _case = facesConfigFlowDefinitionSwitch._case;
                    if (_case!= null) {
                        _case.clear();
                    } else {
                        _case = new ArrayList<>();
                    }
                }
                _case.add(_caseItem);
            } else if (("default-outcome" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultOutcome
                XmlString defaultOutcome = readXmlString(elementReader, context);
                facesConfigFlowDefinitionSwitch.defaultOutcome = defaultOutcome;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "case"), new QName("http://java.sun.com/xml/ns/javaee", "default-outcome"));
            }
        }
        if (_case!= null) {
            facesConfigFlowDefinitionSwitch._case = _case;
        }

        context.afterUnmarshal(facesConfigFlowDefinitionSwitch, LifecycleCallback.NONE);

        return facesConfigFlowDefinitionSwitch;
    }

    public final FacesConfigFlowDefinitionSwitch read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionSwitch facesConfigFlowDefinitionSwitch, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinitionSwitch == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesConfigFlowDefinitionSwitch.class!= facesConfigFlowDefinitionSwitch.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinitionSwitch, FacesConfigFlowDefinitionSwitch.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinitionSwitch, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesConfigFlowDefinitionSwitch.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConfigFlowDefinitionSwitch, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: _case
        List<FacesConfigFlowDefinitionSwitchCase> _case = facesConfigFlowDefinitionSwitch._case;
        if (_case!= null) {
            for (FacesConfigFlowDefinitionSwitchCase _caseItem: _case) {
                if (_caseItem!= null) {
                    writer.writeStartElement(prefix, "case", "http://java.sun.com/xml/ns/javaee");
                    writeFacesConfigFlowDefinitionSwitchCase(writer, _caseItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: defaultOutcome
        XmlString defaultOutcome = facesConfigFlowDefinitionSwitch.defaultOutcome;
        if (defaultOutcome!= null) {
            writer.writeStartElement(prefix, "default-outcome", "http://java.sun.com/xml/ns/javaee");
            writeXmlString(writer, defaultOutcome, context);
            writer.writeEndElement();
        }

        context.afterMarshal(facesConfigFlowDefinitionSwitch, LifecycleCallback.NONE);
    }

}
