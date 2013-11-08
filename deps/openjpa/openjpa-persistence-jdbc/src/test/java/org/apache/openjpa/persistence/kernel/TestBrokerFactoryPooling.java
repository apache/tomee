/*
 * TestBrokerFactoryPooling.java
 *
 * Created on October 9, 2006, 6:10 PM
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
package org.apache.openjpa.persistence.kernel;




import org.apache.openjpa.kernel.Bootstrap;
import org.apache.openjpa.kernel.BrokerFactory;

public class TestBrokerFactoryPooling extends BaseKernelTest {

    /**
     * Creates a new instance of TestBrokerFactoryPooling
     */
    public TestBrokerFactoryPooling(String name) {
        super(name);
    }

    public void testPooling() {
        BrokerFactory bf0 = Bootstrap.getBrokerFactory();
        BrokerFactory bf1 = Bootstrap.getBrokerFactory();
        assertSame(bf0, bf1);
    }
}
