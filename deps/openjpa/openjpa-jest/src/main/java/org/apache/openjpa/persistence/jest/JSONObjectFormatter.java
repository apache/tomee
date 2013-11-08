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

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Metamodel;

import org.apache.openjpa.json.JSON;
import org.apache.openjpa.json.JSONObject;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.persistence.meta.Members;
import static org.apache.openjpa.persistence.jest.Constants.MIME_TYPE_JSON;

/**
 * Marshals a root instance and its persistent closure as JSON object.
 * The closure is resolved against the persistence context that contains the root instance.
 * The JSON format introduces a $id and $ref to address reference that pure JSON does not. 
 * 
 * @author Pinaki Poddar
 *
 */
public class JSONObjectFormatter implements ObjectFormatter<JSON> {
    
    public String getMimeType() {
        return MIME_TYPE_JSON;
    }

    public void encode(Object obj, JPAServletContext ctx) {
        if (obj instanceof OpenJPAStateManager) {
            try {
                JSON result = encodeManagedInstance((OpenJPAStateManager)obj, 
                    ctx.getPersistenceContext().getMetamodel());
                PrintWriter writer = ctx.getResponse().getWriter();
                writer.println(result.toString());
            } catch (Exception e) {
                throw new ProcessingException(ctx, e);
            }
        } else {
            throw new RuntimeException(this + " does not know how to encode " + obj);
        }
        return;
    }
    
    public JSON writeOut(Collection<OpenJPAStateManager> sms, Metamodel model, String title, String desc, 
        String uri, OutputStream out) throws IOException {
        JSON json = encode(sms,model);
        out.write(json.toString().getBytes());
        return json;
    }
    
    public JSON encode(Collection<OpenJPAStateManager> sms, Metamodel model) {
        return encodeManagedInstances(sms, model);
    }
    
    /**
     * Encodes the given managed instance into a new XML element as a child of the given parent node.
     * 
     * @param sm a managed instance, can be null.
     * @param parent the parent node to which the new node be attached.
     */
    private JSON encodeManagedInstance(final OpenJPAStateManager sm, Metamodel model) {
        return encodeManagedInstance(sm, new HashSet<OpenJPAStateManager>(), 0, false, model);
    }
    
    private JSON encodeManagedInstances(final Collection<OpenJPAStateManager> sms, Metamodel model) {
        JSONObject.Array result = new JSONObject.Array();
        for (OpenJPAStateManager sm : sms) {
            result.add(encodeManagedInstance(sm, new HashSet<OpenJPAStateManager>(), 0, false, model));
        }
        return result;
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
    private JSONObject encodeManagedInstance(final OpenJPAStateManager sm, final Set<OpenJPAStateManager> visited, 
        int indent, boolean indentPara, Metamodel model) {
        if (visited == null) {
            throw new IllegalArgumentException("null closure for encoder");
        }
        if (sm == null) {
            return null;
        }
        
        boolean ref = !visited.add(sm);
        JSONObject root =  new JSONObject(typeOf(sm), sm.getObjectId(), ref);;
        if (ref) {
            return root;
        } 
        
        BitSet loaded = sm.getLoaded();
        StoreContext ctx = (StoreContext)sm.getGenericContext();
        List<Attribute<?, ?>> attrs = MetamodelHelper.getAttributesInOrder(sm.getMetaData(), model);
            
        for (int i = 0; i < attrs.size(); i++) {
            FieldMetaData fmd = ((Members.Member<?, ?>) attrs.get(i)).fmd;
            if (!loaded.get(fmd.getIndex())) 
                continue;
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
                         root.set(fmd.getName(),value);
                break;
                
                case JavaTypes.PC:
                    if (value == null) {
                        root.set(fmd.getName(), null);
                    } else {
                        root.set(fmd.getName(),encodeManagedInstance(ctx.getStateManager(value), visited, 
                            indent+1, false, model)); 
                    }
                    break;
                    
                case JavaTypes.ARRAY:
                    Object[] values = (Object[])value;
                    value = Arrays.asList(values);
                // no break;
                case JavaTypes.COLLECTION:
                    if (value == null) {
                        root.set(fmd.getName(), null);
                        break;
                    }
                    Collection<?> members = (Collection<?>)value;
                    JSONObject.Array array = new JSONObject.Array();
                    root.set(fmd.getName(), array);
                    if (members.isEmpty()) {
                        break;
                    }
                    boolean basic = fmd.getElement().getTypeMetaData() == null;
                    for (Object o : members) {
                        if (o == null) {
                            array.add(null);
                        } else {
                            if (basic) {
                                array.add(o);
                            } else {
                                array.add(encodeManagedInstance(ctx.getStateManager(o), visited, indent+1, true, 
                                    model));
                            }
                        }
                    }
                    break;
                case JavaTypes.MAP:
                    if (value == null) {
                        root.set(fmd.getName(), null);
                        break;
                    }
                    Set<Map.Entry> entries = ((Map)value).entrySet();
                    JSONObject.KVMap map = new JSONObject.KVMap();
                    root.set(fmd.getName(), map);
                    if (entries.isEmpty()) {
                        break;
                    }
                    
                    boolean basicKey   = fmd.getElement().getTypeMetaData() == null;
                    boolean basicValue = fmd.getValue().getTypeMetaData() == null;
                    for (Map.Entry<?,?> e : entries) {
                        Object k = e.getKey();
                        Object v = e.getValue();
                        if (!basicKey) {
                            k = encodeManagedInstance(ctx.getStateManager(k), visited, indent+1, true, model);
                        }
                        if (!basicValue) {
                            v = encodeManagedInstance(ctx.getStateManager(e.getValue()), visited, 
                                indent+1, false, model);
                        }
                        map.put(k,v);
                    }
                    break;
                    
                case JavaTypes.INPUT_STREAM:
                case JavaTypes.INPUT_READER:
                    root.set(fmd.getName(), streamToString(value));
                    break;
                    
                case JavaTypes.PC_UNTYPED:
                case JavaTypes.OBJECT:
                case JavaTypes.OID:
                    root.set(fmd.getName(), "***UNSUPPORTED***");
            }
        }
        return root;
    }
    
    
    String typeOf(OpenJPAStateManager sm) {
        return sm.getMetaData().getDescribedType().getSimpleName();
    }
    
    
    /**
     * Convert the given stream (either an InutStream or a Reader) to a String
     * to be included in CDATA section of a XML document.
     * 
     * @param value the field value to be converted. Can not be null 
     * @return
     */
    String streamToString(Object value) {
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

    @Override
    public JSON encode(Metamodel model) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSON writeOut(Metamodel model, String title, String desc, String uri, OutputStream out) throws IOException {
        throw new UnsupportedOperationException();
    }
}
