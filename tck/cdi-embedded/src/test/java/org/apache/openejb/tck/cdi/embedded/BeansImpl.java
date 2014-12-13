/*
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
package org.apache.openejb.tck.cdi.embedded;

import org.apache.openejb.core.ivm.IntraVmCopyMonitor;
import org.apache.openejb.core.ivm.IntraVmProxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
* @version $Rev$ $Date$
*/
public class BeansImpl extends org.apache.webbeans.test.tck.BeansImpl {
    public boolean isProxy(final Object instance) {
        return instance instanceof IntraVmProxy || super.isProxy(instance);
    }

    @Override
    public byte[] passivate(final Object instance) throws IOException {
        IntraVmCopyMonitor.prePassivationOperation();
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream os = new ObjectOutputStream(baos);
            os.writeObject(instance);
            os.flush();
            return baos.toByteArray();
        } finally {
            IntraVmCopyMonitor.postPassivationOperation();
        }
    }
}
