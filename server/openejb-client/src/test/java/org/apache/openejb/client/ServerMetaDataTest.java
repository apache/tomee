/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client;

import java.net.URI;

import junit.framework.TestCase;


public class ServerMetaDataTest extends TestCase {

    public void testHashIsTheSameWhateverTheOrderOfTheLocations() throws Exception {
        URI uri1 = new URI("ejbd://localhost:4201");
        URI uri2 = new URI("ejbd://localhost:4202");
        ServerMetaData server1 = new ServerMetaData(uri1, uri2);
        ServerMetaData server2 = new ServerMetaData(uri2, uri1);
        assertEquals(server1.buildHash(), server2.buildHash());
    }
    
}
