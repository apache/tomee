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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import java.io.Externalizable;
import java.io.ObjectStreamException;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ArrayList;

/**
 * Works with objects that have a public no-arg constructor
 */
public class PojoExternalization extends PojoSerialization implements Externalizable {

    /** Constructor for externalization */
    public PojoExternalization() {
        super();
    }

    public PojoExternalization(Object object) {
        super(object);
    }

    protected Object readResolve() throws ObjectStreamException {
        return super.readResolve();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        read(in);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        write(out);
    }
}
