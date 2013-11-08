/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.openjpa.persistence.jest;

import static org.apache.openjpa.persistence.jest.Constants.ATTR_ID;
import static org.apache.openjpa.persistence.jest.Constants.ATTR_KEY_TYPE;
import static org.apache.openjpa.persistence.jest.Constants.ATTR_MEMBER_TYPE;
import static org.apache.openjpa.persistence.jest.Constants.ATTR_NAME;
import static org.apache.openjpa.persistence.jest.Constants.ATTR_NULL;
import static org.apache.openjpa.persistence.jest.Constants.ATTR_TYPE;
import static org.apache.openjpa.persistence.jest.Constants.ATTR_VALUE_TYPE;
import static org.apache.openjpa.persistence.jest.Constants.ATTR_VERSION;
import static org.apache.openjpa.persistence.jest.Constants.ELEMENT_DESCRIPTION;
import static org.apache.openjpa.persistence.jest.Constants.ELEMENT_ENTRY;
import static org.apache.openjpa.persistence.jest.Constants.ELEMENT_ENTRY_KEY;
import static org.apache.openjpa.persistence.jest.Constants.ELEMENT_ENTRY_VALUE;
import static org.apache.openjpa.persistence.jest.Constants.ELEMENT_INSTANCE;
import static org.apache.openjpa.persistence.jest.Constants.ELEMENT_MEMBER;
import static org.apache.openjpa.persistence.jest.Constants.ELEMENT_NULL_REF;
import static org.apache.openjpa.persistence.jest.Constants.ELEMENT_REF;
import static org.apache.openjpa.persistence.jest.Constants.ELEMENT_URI;
import static org.apache.openjpa.persistence.jest.Constants.JEST_INSTANCE_XSD;
import static org.apache.openjpa.persistence.jest.Constants.MIME_TYPE_XML;
import static org.apache.openjpa.persistence.jest.Constants.NULL_VALUE;
import static org.apache.openjpa.persistence.jest.Constants.ROOT_ELEMENT_INSTANCE;
import static org.apache.openjpa.persistence.jest.Constants.ROOT_ELEMENT_MODEL;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.persistence.meta.Members;
import org.apache.openjpa.util.InternalException;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Marshals a root instance and its persistent closure as an XML element.
 * The closure is resolved against the persistence context that contains the root instance.
 * The XML document adheres to the <code>jest-instance.xsd</code> schema. 
 * 
 * @author Pinaki Poddar
 *
 */
public class XMLFormatter implements ObjectFormatter<Document> {
    public static final  Schema          _xsd;
    private static final DocumentBuilder _builder;
    private static final Transformer     _transformer;
    private static final String EMPTY_TEXT = " ";
    protected static Localizer _loc = Localizer.forPackage(XMLFormatter.class);
    
    static {
        try {
            _builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            _transformer = TransformerFactory.newInstance().newTransformer();
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream xsd = XMLFormatter.class.getResourceAsStream(JEST_INSTANCE_XSD);
            _xsd = factory.newSchema(new StreamSource(xsd));

            _transformer.setOutputProperty(OutputKeys.METHOD,     "xml");
            _transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            _transformer.setOutputProperty(OutputKeys.INDENT,     "yes");
            _transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            _transformer.setOutputProperty(OutputKeys.ENCODING,   "UTF-8");
            _transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getMimeType() {
        return MIME_TYPE_XML;
    }    
    
    /**
     * Encodes the closure of given collection of  managed instance into a new XML document 
     * according to JEST Instance XML Schema.
     * 
     * @param sm a collection of managed instances. 
     * @param parent the parent node to which the new node be attached.
     */
    public Document encode(final Collection<OpenJPAStateManager> sms, Metamodel model) {
        Element root = newDocument(ROOT_ELEMENT_INSTANCE);
        Closure closure = new Closure(sms);
        for (OpenJPAStateManager sm : closure) {
            encodeManagedInstance(sm, root, false, model);
        }
        return root.getOwnerDocument();
    }
    
    /**
     * Encodes the given meta-model into a new XML document according to JEST Domain XML Schema.
     * 
     * @param model a persistent domain model. Must not be null.
     */
    public Document encode(Metamodel model) {
        Element root = newDocument(ROOT_ELEMENT_MODEL);
        for (ManagedType<?> t : model.getManagedTypes()) {
            encodeManagedType(t, root);
        }
        return root.getOwnerDocument();
    }
    
    /**
     * Create a new document with the given tag as the root element. 
     * 
     * @param rootTag the tag of the root element
     * 
     * @return the document element of a new document
     */
    public Element newDocument(String rootTag) {
        Document doc = _builder.newDocument();
        Element root = doc.createElement(rootTag);
        doc.appendChild(root);
        String[] nvpairs = new String[] {
                "xmlns:xsi",                     XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
//                "xsi:noNamespaceSchemaLocation", JEST_INSTANCE_XSD,
                ATTR_VERSION,                       "1.0",
        };
        for (int i = 0; i < nvpairs.length; i += 2) {
            root.setAttribute(nvpairs[i], nvpairs[i+1]);
        }
        return root;
    }

    
    @Override
    public Document writeOut(Collection<OpenJPAStateManager> objs, Metamodel model, String title, String desc, 
        String uri, OutputStream out) throws IOException {
        Document doc = encode(objs, model);
        decorate(doc, title, desc, uri);
        write(doc, out);
        return doc;
    }
    
    @Override
    public Document writeOut(Metamodel model, String title, String desc, String uri, OutputStream out) 
        throws IOException {
        Document doc = encode(model);
        decorate(doc, title, desc, uri);
        write(doc, out);
        return doc;
    }
    
    Document decorate(Document doc, String title, String desc, String uri) {
        Element root = doc.getDocumentElement();
        Element instance = (Element)root.getElementsByTagName(ELEMENT_INSTANCE).item(0);
        Element uriElement = doc.createElement(ELEMENT_URI);
        uriElement.setTextContent(uri == null ? NULL_VALUE : uri);
        Element descElement = doc.createElement(ELEMENT_DESCRIPTION);
        descElement.setTextContent(desc == null ? NULL_VALUE : desc);
        root.insertBefore(uriElement, instance);
        root.insertBefore(descElement, instance);
        return doc;
    }
    
    public void write(Document doc, OutputStream out) throws IOException {
        try {
            _transformer.transform(new DOMSource(doc), new StreamResult(out));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    public void write(Document doc, Writer writer) throws IOException {
        try {
            _transformer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Encodes the closure of a persistent instance into a XML element.
     * 
     * @param sm the managed instance to be encoded. Can be null.
     * @param parent the parent XML element to which the new XML element be added. Must not be null. Must be
     * owned by a document. 
     * @param visited the persistent instances that had been encoded already. Must not be null or immutable.
     * 
     * @return the new element. The element has been appended as a child to the given parent in this method.  
     */
    private Element encodeManagedInstance(final OpenJPAStateManager sm, final Element parent, 
         boolean isRef, Metamodel model) {
        if (parent == null)
            throw new InternalException(_loc.get("format-xml-null-parent"));
        Document doc = parent.getOwnerDocument();
        if (doc == null) 
            throw new InternalException(_loc.get("format-xml-null-doc"));
        
        if (sm == null || isRef) {
            return encodeRef(parent, sm);
        }
        Element root = doc.createElement(ELEMENT_INSTANCE);
        parent.appendChild(root);
        root.setAttribute(ATTR_ID, ior(sm));
        Element child = null;
        BitSet loaded = sm.getLoaded();
        StoreContext ctx = (StoreContext)sm.getGenericContext();
        List<Attribute<?, ?>> attrs = MetamodelHelper.getAttributesInOrder(sm.getMetaData(), model);
        for (int i = 0; i < attrs.size(); child = null, i++) {
            Members.Member<?, ?> attr = (Members.Member<?, ?>) attrs.get(i);
            FieldMetaData fmd = attr.fmd;
            if (!loaded.get(fmd.getIndex())) 
                continue;
            String tag = MetamodelHelper.getTagByAttributeType(attr);
            Object value = sm.fetch(fmd.getIndex());
            switch (fmd.getDeclaredTypeCode()) {
                case JavaTypes.BOOLEAN:
                case JavaTypes.BYTE:
                case JavaTypes.CHAR:
                case JavaTypes.DOUBLE:
                case JavaTypes.FLOAT:
                case JavaTypes.INT:
                case JavaTypes.LONG:
                case JavaTypes.SHORT:

                case JavaTypes.BOOLEAN_OBJ:
                case JavaTypes.BYTE_OBJ:
                case JavaTypes.CHAR_OBJ:
                case JavaTypes.DOUBLE_OBJ:
                case JavaTypes.FLOAT_OBJ:
                case JavaTypes.INT_OBJ:
                case JavaTypes.LONG_OBJ:
                case JavaTypes.SHORT_OBJ:

                case JavaTypes.BIGDECIMAL:
                case JavaTypes.BIGINTEGER:
                case JavaTypes.DATE:
                case JavaTypes.NUMBER:
                case JavaTypes.CALENDAR:
                case JavaTypes.LOCALE:
                case JavaTypes.STRING:
                case JavaTypes.ENUM:
                child = doc.createElement(tag);
                child.setAttribute(ATTR_NAME, fmd.getName());
                if (value == null) {
                    encodeNull(child);
                } else { 
                    encodeBasic(child, value, fmd.getDeclaredType());
                }
                break;
                
                case JavaTypes.OID:
                    child = doc.createElement(ELEMENT_REF);
                    child.setAttribute(ATTR_NAME, fmd.getName());
                    if (value == null) {
                        encodeNull(child);
                    } else { 
                        encodeBasic(child, value, fmd.getDeclaredType());
                    }
                    break;
                    
                case JavaTypes.PC:
                    child = doc.createElement(tag);
                    child.setAttribute(ATTR_NAME, fmd.getName());
                    child.setAttribute(ATTR_TYPE, typeOf(fmd));
                    OpenJPAStateManager other = ctx.getStateManager(value);
                    encodeManagedInstance(other, child, true, model);
                    break;
                    
                case JavaTypes.ARRAY:
                    Object[] values = (Object[])value;
                    value = Arrays.asList(values);
                // no break;
                case JavaTypes.COLLECTION:
                    child = doc.createElement(tag);
                    child.setAttribute(ATTR_NAME, fmd.getName());
                    child.setAttribute(ATTR_TYPE, typeOf(fmd));
                    child.setAttribute(ATTR_MEMBER_TYPE, typeOf(fmd.getElement().getDeclaredType()));
                    if (value == null) {
                        encodeNull(child);
                        break;
                    }
                    Collection<?> members = (Collection<?>)value;
                    boolean basic = fmd.getElement().getTypeMetaData() == null;
                    for (Object o : members) {
                        Element member = doc.createElement(ELEMENT_MEMBER);
                        child.appendChild(member);
                        if (o == null) {
                            encodeNull(member);
                        } else {
                            if (basic) {
                                encodeBasic(member, o, o.getClass());
                            } else {
                                encodeManagedInstance(ctx.getStateManager(o), member, true, model);
                            }
                        }
                    }
                    break;
                case JavaTypes.MAP:
                    child = doc.createElement(tag);
                    child.setAttribute(ATTR_NAME, fmd.getName());
                    child.setAttribute(ATTR_TYPE, typeOf(fmd));
                    child.setAttribute(ATTR_KEY_TYPE, typeOf(fmd.getElement().getDeclaredType()));
                    child.setAttribute(ATTR_VALUE_TYPE, typeOf(fmd.getValue().getDeclaredType()));
                    if (value == null) {
                        encodeNull(child);
                        break;
                    }
                    Set<Map.Entry<?,?>> entries = ((Map)value).entrySet();
                    boolean basicKey   = fmd.getElement().getTypeMetaData() == null;
                    boolean basicValue = fmd.getValue().getTypeMetaData() == null;
                    for (Map.Entry<?,?> e : entries) {
                        Element entry = doc.createElement(ELEMENT_ENTRY);
                        Element entryKey = doc.createElement(ELEMENT_ENTRY_KEY);
                        Element entryValue = doc.createElement(ELEMENT_ENTRY_VALUE);
                        entry.appendChild(entryKey);
                        entry.appendChild(entryValue);
                        child.appendChild(entry);
                        if (e.getKey() == null) {
                            encodeNull(entryKey);
                        } else {
                            if (basicKey) {
                                encodeBasic(entryKey, e.getKey(), e.getKey().getClass());
                            } else {
                                encodeManagedInstance(ctx.getStateManager(e.getKey()), entryKey, true, model);
                            }
                        }
                        if (e.getValue() == null) {
                            encodeNull(entryValue);
                        } else {
                            if (basicValue) {
                                encodeBasic(entryValue, e.getValue(), e.getValue().getClass());
                            } else {
                                encodeManagedInstance(ctx.getStateManager(e.getValue()), entryValue, true, model);
                            }
                        }
                    }
                    break;
                    
                case JavaTypes.INPUT_STREAM:
                case JavaTypes.INPUT_READER:
                    child = doc.createElement(tag);
                    child.setAttribute(ATTR_NAME, fmd.getName());
                    child.setAttribute(ATTR_TYPE, typeOf(fmd));
                    if (value == null) {
                        encodeNull(child);
                    } else { 
                        CDATASection data = doc.createCDATASection(streamToString(value));
                        child.appendChild(data);
                    }
                    break;
                    
                case JavaTypes.PC_UNTYPED:
                case JavaTypes.OBJECT:
                    // START - ALLOW PRINT STATEMENTS
                    System.err.println("Not handled " + fmd.getName() + " of type " + fmd.getDeclaredType());
                    // STOP - ALLOW PRINT STATEMENTS
            }
            
            if (child != null) {
                root.appendChild(child);
            }
        }
        return root;
    }
    
    /**
     * Sets the given value element as null. The <code>null</code> attribute is set to true.
     * 
     * @param element the XML element to be set
     */
    private void encodeNull(Element element) {
        element.setAttribute(ATTR_NULL, "true");
    }
    
    private Element encodeRef(Element parent, OpenJPAStateManager sm) {
        Element ref = parent.getOwnerDocument().createElement(sm == null ? ELEMENT_NULL_REF : ELEMENT_REF);
        if (sm != null)
            ref.setAttribute(ATTR_ID, ior(sm));
     // IMPORTANT: for xml transformer not to omit the closing tag, otherwise dojo is confused
        ref.setTextContent(EMPTY_TEXT); 
        parent.appendChild(ref);
        return ref;
    }
    
    
    /**
     * Sets the given value element. The <code>type</code> is set to the given runtime type.
     * String form of the given object is set as the text content. 
     * 
     * @param element the XML element to be set
     * @param obj value of the element. Never null.
     */
    private void encodeBasic(Element element, Object obj, Class<?> runtimeType) {
        element.setAttribute(ATTR_TYPE, typeOf(runtimeType));
        if (obj instanceof Date)
            element.setTextContent(dateFormat.format(obj));
        else 
            element.setTextContent(obj == null ? NULL_VALUE : obj.toString());
    }
    
    
    
    /**
     * Convert the given stream (either an InutStream or a Reader) to a String
     * to be included in CDATA section of a XML document.
     * 
     * @param value the field value to be converted. Can not be null 
     * @return
     */
    private String streamToString(Object value) {
        Reader reader = null;
        if (value instanceof InputStream) {
            reader = new BufferedReader(new InputStreamReader((InputStream)value));
        } else if (value instanceof Reader) {
            reader = (Reader)value;
        } else {
            throw new RuntimeException();
        }
        CharArrayWriter writer = new CharArrayWriter();
        try {
            for (int c; (c = reader.read()) != -1;) {
                writer.write(c);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return writer.toString();
    }
    
    
    private void encodeManagedType(ManagedType<?> type, Element parent) {
        Document doc = parent.getOwnerDocument();
        Element root = doc.createElement(type.getPersistenceType().toString().toLowerCase());
        parent.appendChild(root);
        root.setAttribute(ATTR_NAME, type.getJavaType().getSimpleName());
        List<Attribute<?,?>> attributes = MetamodelHelper.getAttributesInOrder(type);
        for (Attribute<?,?> a : attributes) {
            String tag = MetamodelHelper.getTagByAttributeType(a);
            
            Element child = doc.createElement(tag);
            root.appendChild(child);
            child.setAttribute(ATTR_TYPE, typeOf(a.getJavaType()));
            if (a instanceof PluralAttribute) {
                if (a instanceof MapAttribute) {
                    child.setAttribute(ATTR_KEY_TYPE,   typeOf(((MapAttribute)a).getKeyJavaType()));
                    child.setAttribute(ATTR_VALUE_TYPE, typeOf(((MapAttribute)a).getBindableJavaType()));
                } else {
                    child.setAttribute(ATTR_MEMBER_TYPE, typeOf(((PluralAttribute)a).getBindableJavaType()));
                }
            }
            child.setTextContent(a.getName());
        }
    }

    void validate(Document doc) throws Exception {
        Validator validator = _xsd.newValidator();
        validator.validate(new DOMSource(doc));
    }
    
    String ior(OpenJPAStateManager sm) {
        return typeOf(sm) + "-" + sm.getObjectId();
    }
    
    String typeOf(OpenJPAStateManager sm) {
        return sm.getMetaData().getDescribedType().getSimpleName();
    }
    
    String typeOf(Class<?> cls) {
        return cls.getSimpleName();
    }
    
    String typeOf(ClassMetaData meta) {
        return meta.getDescribedType().getSimpleName();
    }
    
    String typeOf(ValueMetaData vm) {
        if (vm.getTypeMetaData() == null)
            return typeOf(vm.getType()); 
        return typeOf(vm.getTypeMetaData());
    }
    
    String typeOf(FieldMetaData fmd) {
        return fmd.getType().getSimpleName();
    }
}
