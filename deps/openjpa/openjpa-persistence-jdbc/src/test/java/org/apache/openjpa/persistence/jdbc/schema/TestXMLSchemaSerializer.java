/*
 * TestXMLSchemaSerializer.java
 *
 * Created on October 6, 2006, 4:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.jdbc.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.SchemaParser;
import org.apache.openjpa.jdbc.schema.SchemaSerializer;
import org.apache.openjpa.jdbc.schema.XMLSchemaParser;
import org.apache.openjpa.jdbc.schema.XMLSchemaSerializer;


public class TestXMLSchemaSerializer extends TestXMLSchemaParser{
    
    
    /** Creates a new instance of TestXMLSchemaSerializer */
    public TestXMLSchemaSerializer() {
    }
    
    
    public TestXMLSchemaSerializer(String test) {
        super(test);
    }
    
    protected SchemaGroup getSchemaGroup()
    throws Exception {
        // parse in the schema group, then serialize it to a buffer, then
        // recreate it and test againt that to make sure it's the same as the
        // original
        SchemaGroup group = parseSchemaGroup();
        SchemaSerializer ser = new XMLSchemaSerializer(this.conf);
        ser.addAll(group);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ser.serialize(new OutputStreamWriter(out), ser.PRETTY);
        byte[] bytes = out.toByteArray();
        
        SchemaParser parser = new XMLSchemaParser(this.conf);
        parser.parse(new InputStreamReader
                (new ByteArrayInputStream(bytes)), "bytes");
        return parser.getSchemaGroup();
    }
    
    public static void main(String[] args) {
        //   main(TestXMLSchemaSerializer.class);
    }
}
