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

package org.apache.openejb.jee.oejb3;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.oejb3.QueryMethod$JAXB.readQueryMethod;
import static org.apache.openejb.jee.oejb3.QueryMethod$JAXB.writeQueryMethod;

@SuppressWarnings({
    "StringEquality"
})
public class Query$JAXB
    extends JAXBObject<Query>
{


    public Query$JAXB() {
        super(Query.class, new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "query".intern()), null, QueryMethod$JAXB.class);
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
            return context.unexpectedXsiType(reader, Query.class);
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: description
                String descriptionRaw = elementReader.getElementText();

                String description;
                try {
                    description = Adapters.collapsedStringAdapterAdapter.unmarshal(descriptionRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                query.description = description;
            } else if (("query-method" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: queryMethod
                QueryMethod queryMethod = readQueryMethod(elementReader, context);
                query.queryMethod = queryMethod;
            } else if (("object-ql" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: objectQl
                String objectQlRaw = elementReader.getElementText();

                String objectQl;
                try {
                    objectQl = Adapters.collapsedStringAdapterAdapter.unmarshal(objectQlRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                query.objectQl = objectQl;
            } else {
                context.unexpectedElement(elementReader, new QName("http://www.openejb.org/openejb-jar/1.1", "description"), new QName("http://www.openejb.org/openejb-jar/1.1", "query-method"), new QName("http://www.openejb.org/openejb-jar/1.1", "object-ql"));
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

        String prefix = writer.getUniquePrefix("http://www.openejb.org/openejb-jar/1.1");
        if (Query.class!= query.getClass()) {
            context.unexpectedSubclass(writer, query, Query.class);
            return ;
        }

        context.beforeMarshal(query, LifecycleCallback.NONE);


        // ELEMENT: description
        String descriptionRaw = query.description;
        String description = null;
        try {
            description = Adapters.collapsedStringAdapterAdapter.marshal(descriptionRaw);
        } catch (Exception e) {
            context.xmlAdapterError(query, "description", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (description!= null) {
            writer.writeStartElement(prefix, "description", "http://www.openejb.org/openejb-jar/1.1");
            writer.writeCharacters(description);
            writer.writeEndElement();
        }

        // ELEMENT: queryMethod
        QueryMethod queryMethod = query.queryMethod;
        if (queryMethod!= null) {
            writer.writeStartElement(prefix, "query-method", "http://www.openejb.org/openejb-jar/1.1");
            writeQueryMethod(writer, queryMethod, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(query, "queryMethod");
        }

        // ELEMENT: objectQl
        String objectQlRaw = query.objectQl;
        String objectQl = null;
        try {
            objectQl = Adapters.collapsedStringAdapterAdapter.marshal(objectQlRaw);
        } catch (Exception e) {
            context.xmlAdapterError(query, "objectQl", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (objectQl!= null) {
            writer.writeStartElement(prefix, "object-ql", "http://www.openejb.org/openejb-jar/1.1");
            writer.writeCharacters(objectQl);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(query, "objectQl");
        }

        context.afterMarshal(query, LifecycleCallback.NONE);
    }

}
