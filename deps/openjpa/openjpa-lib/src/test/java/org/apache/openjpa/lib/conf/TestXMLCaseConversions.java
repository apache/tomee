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
package org.apache.openjpa.lib.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;


public class TestXMLCaseConversions extends TestCase {

    public void testToXMLName() {
        assertEquals("easy-xml-conversion", 
            ConfigurationImpl.toXMLName("easyXmlConversion"));
        assertEquals("initial-caps", 
            ConfigurationImpl.toXMLName("InitialCaps"));
        assertEquals("nodash", 
            ConfigurationImpl.toXMLName("nodash"));
        assertEquals("anothernodash", 
            ConfigurationImpl.toXMLName("Anothernodash"));
        assertEquals("multiple-caps", 
            ConfigurationImpl.toXMLName("MUltipleCaps"));
        assertEquals("trailing-multi-caps", 
            ConfigurationImpl.toXMLName("TrailingMultiCAPS"));
        assertEquals("two-i-nner-caps", 
            ConfigurationImpl.toXMLName("TwoINnerCaps"));
        assertEquals("four-inn-er-caps", 
            ConfigurationImpl.toXMLName("FourINNErCaps"));
        assertEquals("inner-3-number", 
            ConfigurationImpl.toXMLName("Inner3Number"));
        assertEquals("inner-03-number", 
            ConfigurationImpl.toXMLName("Inner03Number"));
    }
    
    public static void main(String[] args) throws IOException {
        BufferedReader r = new BufferedReader (new FileReader(new File(
                args[0])));
        while (true) {
            String s = r.readLine();
            if (s == null)
                break;
            System.out.println(s + ": " + ConfigurationImpl.toXMLName(s));
        }
    }
}
