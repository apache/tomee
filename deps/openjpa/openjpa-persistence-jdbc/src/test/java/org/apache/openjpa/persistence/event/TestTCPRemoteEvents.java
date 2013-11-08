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
package org.apache.openjpa.persistence.event;


import org.apache.openjpa.persistence.event.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.test.AllowFailure;

import org.apache.openjpa.event.TCPRemoteCommitProvider;

@AllowFailure(message="surefire excluded")
public class TestTCPRemoteEvents
    extends RemoteEventBase {

    public TestTCPRemoteEvents(String s) {
        super(s);
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);
    }

    public void testEvents() {
        doTest(TCPRemoteCommitProvider.class,
            "Port=5636, Addresses=127.0.0.1:5636;127.0.0.1:6636",
            "Port=6636, Addresses=127.0.0.1:5636;127.0.0.1:6636");
    }
}
