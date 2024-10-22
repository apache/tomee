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

@SuppressWarnings({
    "StringEquality"
})
public class WelcomeFileList$JAXB
    extends JAXBObject<WelcomeFileList>
{


    public WelcomeFileList$JAXB() {
        super(WelcomeFileList.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "welcome-file-listType".intern()));
    }

    public static WelcomeFileList readWelcomeFileList(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeWelcomeFileList(XoXMLStreamWriter writer, WelcomeFileList welcomeFileList, RuntimeContext context)
        throws Exception
    {
        _write(writer, welcomeFileList, context);
    }

    public void write(XoXMLStreamWriter writer, WelcomeFileList welcomeFileList, RuntimeContext context)
        throws Exception
    {
        _write(writer, welcomeFileList, context);
    }

    public static final WelcomeFileList _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        WelcomeFileList welcomeFileList = new WelcomeFileList();
        context.beforeUnmarshal(welcomeFileList, LifecycleCallback.NONE);

        List<String> welcomeFile = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("welcome-file-listType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, WelcomeFileList.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, welcomeFileList);
                welcomeFileList.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("welcome-file" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: welcomeFile
                String welcomeFileItemRaw = elementReader.getElementText();

                String welcomeFileItem;
                try {
                    welcomeFileItem = Adapters.collapsedStringAdapterAdapter.unmarshal(welcomeFileItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (welcomeFile == null) {
                    welcomeFile = welcomeFileList.welcomeFile;
                    if (welcomeFile!= null) {
                        welcomeFile.clear();
                    } else {
                        welcomeFile = new ArrayList<>();
                    }
                }
                welcomeFile.add(welcomeFileItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "welcome-file"));
            }
        }
        if (welcomeFile!= null) {
            welcomeFileList.welcomeFile = welcomeFile;
        }

        context.afterUnmarshal(welcomeFileList, LifecycleCallback.NONE);

        return welcomeFileList;
    }

    public final WelcomeFileList read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, WelcomeFileList welcomeFileList, RuntimeContext context)
        throws Exception
    {
        if (welcomeFileList == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (WelcomeFileList.class!= welcomeFileList.getClass()) {
            context.unexpectedSubclass(writer, welcomeFileList, WelcomeFileList.class);
            return ;
        }

        context.beforeMarshal(welcomeFileList, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = welcomeFileList.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(welcomeFileList, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: welcomeFile
        List<String> welcomeFileRaw = welcomeFileList.welcomeFile;
        if (welcomeFileRaw!= null) {
            for (String welcomeFileItem: welcomeFileRaw) {
                String welcomeFile = null;
                try {
                    welcomeFile = Adapters.collapsedStringAdapterAdapter.marshal(welcomeFileItem);
                } catch (Exception e) {
                    context.xmlAdapterError(welcomeFileList, "welcomeFile", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (welcomeFile!= null) {
                    writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "welcome-file");
                    writer.writeCharacters(welcomeFile);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(welcomeFileList, "welcomeFile");
                }
            }
        }

        context.afterMarshal(welcomeFileList, LifecycleCallback.NONE);
    }

}
