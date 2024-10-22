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


import static org.apache.openejb.jee.QueryMethod$JAXB.readQueryMethod;
import static org.apache.openejb.jee.QueryMethod$JAXB.writeQueryMethod;
import static org.apache.openejb.jee.ResultTypeMapping$JAXB.parseResultTypeMapping;
import static org.apache.openejb.jee.ResultTypeMapping$JAXB.toStringResultTypeMapping;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Query$JAXB
    extends JAXBObject<Query>
{


    public Query$JAXB() {
        super(Query.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "queryType".intern()), Text$JAXB.class, QueryMethod$JAXB.class, ResultTypeMapping$JAXB.class);
    }

    public static Query readQuery(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeQuery(XoXMLStreamWriter writer, Query query, RuntimeContext context)
        throws Exception
    {
        _write(writer, query, context);
    }

    public void write(XoXMLStreamWriter writer, Query query, RuntimeContext context)
        throws Exception
    {
        _write(writer, query, context);
    }

    public static final Query _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Query query = new Query();
        context.beforeUnmarshal(query, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("queryType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Query.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, query);
                query.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: description
                Text description = readText(elementReader, context);
                query.description = description;
            } else if (("query-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: queryMethod
                QueryMethod queryMethod = readQueryMethod(elementReader, context);
                query.queryMethod = queryMethod;
            } else if (("result-type-mapping" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resultTypeMapping
                ResultTypeMapping resultTypeMapping = parseResultTypeMapping(elementReader, context, elementReader.getElementText());
                if (resultTypeMapping!= null) {
                    query.resultTypeMapping = resultTypeMapping;
                }
            } else if (("ejb-ql" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbQl
                String ejbQlRaw = elementReader.getElementText();

                String ejbQl;
                try {
                    ejbQl = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbQlRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                query.ejbQl = ejbQl;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "query-method"), new QName("http://java.sun.com/xml/ns/javaee", "result-type-mapping"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-ql"));
            }
        }

        context.afterUnmarshal(query, LifecycleCallback.NONE);

        return query;
    }

    public final Query read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Query query, RuntimeContext context)
        throws Exception
    {
        if (query == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Query.class!= query.getClass()) {
            context.unexpectedSubclass(writer, query, Query.class);
            return ;
        }

        context.beforeMarshal(query, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = query.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(query, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: description
        Text description = query.description;
        if (description!= null) {
            writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
            writeText(writer, description, context);
            writer.writeEndElement();
        }

        // ELEMENT: queryMethod
        QueryMethod queryMethod = query.queryMethod;
        if (queryMethod!= null) {
            writer.writeStartElement(prefix, "query-method", "http://java.sun.com/xml/ns/javaee");
            writeQueryMethod(writer, queryMethod, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(query, "queryMethod");
        }

        // ELEMENT: resultTypeMapping
        ResultTypeMapping resultTypeMapping = query.resultTypeMapping;
        if (resultTypeMapping!= null) {
            writer.writeStartElement(prefix, "result-type-mapping", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringResultTypeMapping(query, null, context, resultTypeMapping));
            writer.writeEndElement();
        }

        // ELEMENT: ejbQl
        String ejbQlRaw = query.ejbQl;
        String ejbQl = null;
        try {
            ejbQl = Adapters.collapsedStringAdapterAdapter.marshal(ejbQlRaw);
        } catch (Exception e) {
            context.xmlAdapterError(query, "ejbQl", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbQl!= null) {
            writer.writeStartElement(prefix, "ejb-ql", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbQl);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(query, "ejbQl");
        }

        context.afterMarshal(query, LifecycleCallback.NONE);
    }

}
