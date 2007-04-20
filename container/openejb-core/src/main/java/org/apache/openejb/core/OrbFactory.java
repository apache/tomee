/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core;

import org.omg.CORBA.ORB;
import org.apache.openejb.loader.SystemInstance;

public class OrbFactory {
    public ORB create() {
        ORB orb = SystemInstance.get().getComponent(ORB.class);
        if (orb == null) {
           // todo add support for args and properties
           orb = ORB.init();
           SystemInstance.get().setComponent(ORB.class, orb);
        }
        return orb;
    }
}
