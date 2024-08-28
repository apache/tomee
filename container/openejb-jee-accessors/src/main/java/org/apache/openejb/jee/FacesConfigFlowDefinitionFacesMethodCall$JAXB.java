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


import static org.apache.openejb.jee.FacesConfigFlowDefinitionFacesMethodCallMethod$JAXB.readFacesConfigFlowDefinitionFacesMethodCallMethod;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFacesMethodCallMethod$JAXB.writeFacesConfigFlowDefinitionFacesMethodCallMethod;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFlowCallParameter$JAXB.readFacesConfigFlowDefinitionFlowCallParameter;
import static org.apache.openejb.jee.FacesConfigFlowDefinitionFlowCallParameter$JAXB.writeFacesConfigFlowDefinitionFlowCallParameter;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigFlowDefinitionFacesMethodCall$JAXB
    extends JAXBObject<FacesConfigFlowDefinitionFacesMethodCall>
{


    public FacesConfigFlowDefinitionFacesMethodCall$JAXB() {
        super(FacesConfigFlowDefinitionFacesMethodCall.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-flow-definition-faces-method-callType".intern()), FacesConfigFlowDefinitionFacesMethodCallMethod$JAXB.class, FacesConfigFlowDefinitionFlowCallParameter$JAXB.class);
    }

    public static FacesConfigFlowDefinitionFacesMethodCall readFacesConfigFlowDefinitionFacesMethodCall(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigFlowDefinitionFacesMethodCall(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFacesMethodCall facesConfigFlowDefinitionFacesMethodCall, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionFacesMethodCall, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFacesMethodCall facesConfigFlowDefinitionFacesMethodCall, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigFlowDefinitionFacesMethodCall, context);
    }

    public static final FacesConfigFlowDefinitionFacesMethodCall _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigFlowDefinitionFacesMethodCall facesConfigFlowDefinitionFacesMethodCall = new FacesConfigFlowDefinitionFacesMethodCall();
        context.beforeUnmarshal(facesConfigFlowDefinitionFacesMethodCall, LifecycleCallback.NONE);

        List<FacesConfigFlowDefinitionFlowCallParameter> parameter = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-flow-definition-faces-method-callType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigFlowDefinitionFacesMethodCall.class);
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
            if (("method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: method
                FacesConfigFlowDefinitionFacesMethodCallMethod method = readFacesConfigFlowDefinitionFacesMethodCallMethod(elementReader, context);
                facesConfigFlowDefinitionFacesMethodCall.method = method;
            } else if (("default-outcome" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultOutcome
                String defaultOutcomeRaw = elementReader.getElementText();

                String defaultOutcome;
                try {
                    defaultOutcome = Adapters.collapsedStringAdapterAdapter.unmarshal(defaultOutcomeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesConfigFlowDefinitionFacesMethodCall.defaultOutcome = defaultOutcome;
            } else if (("parameter" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: parameter
                FacesConfigFlowDefinitionFlowCallParameter parameterItem = readFacesConfigFlowDefinitionFlowCallParameter(elementReader, context);
                if (parameter == null) {
                    parameter = facesConfigFlowDefinitionFacesMethodCall.parameter;
                    if (parameter!= null) {
                        parameter.clear();
                    } else {
                        parameter = new ArrayList<>();
                    }
                }
                parameter.add(parameterItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "method"), new QName("http://java.sun.com/xml/ns/javaee", "default-outcome"), new QName("http://java.sun.com/xml/ns/javaee", "parameter"));
            }
        }
        if (parameter!= null) {
            facesConfigFlowDefinitionFacesMethodCall.parameter = parameter;
        }

        context.afterUnmarshal(facesConfigFlowDefinitionFacesMethodCall, LifecycleCallback.NONE);

        return facesConfigFlowDefinitionFacesMethodCall;
    }

    public final FacesConfigFlowDefinitionFacesMethodCall read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigFlowDefinitionFacesMethodCall facesConfigFlowDefinitionFacesMethodCall, RuntimeContext context)
        throws Exception
    {
        if (facesConfigFlowDefinitionFacesMethodCall == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesConfigFlowDefinitionFacesMethodCall.class!= facesConfigFlowDefinitionFacesMethodCall.getClass()) {
            context.unexpectedSubclass(writer, facesConfigFlowDefinitionFacesMethodCall, FacesConfigFlowDefinitionFacesMethodCall.class);
            return ;
        }

        context.beforeMarshal(facesConfigFlowDefinitionFacesMethodCall, LifecycleCallback.NONE);


        // ELEMENT: method
        FacesConfigFlowDefinitionFacesMethodCallMethod method = facesConfigFlowDefinitionFacesMethodCall.method;
        if (method!= null) {
            writer.writeStartElement(prefix, "method", "http://java.sun.com/xml/ns/javaee");
            writeFacesConfigFlowDefinitionFacesMethodCallMethod(writer, method, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesConfigFlowDefinitionFacesMethodCall, "method");
        }

        // ELEMENT: defaultOutcome
        String defaultOutcomeRaw = facesConfigFlowDefinitionFacesMethodCall.defaultOutcome;
        String defaultOutcome = null;
        try {
            defaultOutcome = Adapters.collapsedStringAdapterAdapter.marshal(defaultOutcomeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesConfigFlowDefinitionFacesMethodCall, "defaultOutcome", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (defaultOutcome!= null) {
            writer.writeStartElement(prefix, "default-outcome", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(defaultOutcome);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesConfigFlowDefinitionFacesMethodCall, "defaultOutcome");
        }

        // ELEMENT: parameter
        List<FacesConfigFlowDefinitionFlowCallParameter> parameter = facesConfigFlowDefinitionFacesMethodCall.parameter;
        if (parameter!= null) {
            for (FacesConfigFlowDefinitionFlowCallParameter parameterItem: parameter) {
                writer.writeStartElement(prefix, "parameter", "http://java.sun.com/xml/ns/javaee");
                if (parameterItem!= null) {
                    writeFacesConfigFlowDefinitionFlowCallParameter(writer, parameterItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        context.afterMarshal(facesConfigFlowDefinitionFacesMethodCall, LifecycleCallback.NONE);
    }

}
