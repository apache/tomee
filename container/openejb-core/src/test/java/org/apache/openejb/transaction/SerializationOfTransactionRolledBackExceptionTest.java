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
package org.apache.openejb.transaction;

import org.apache.openejb.core.ObjectInputStreamFiltered;
import org.junit.Test;

import jakarta.transaction.TransactionRolledbackException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class SerializationOfTransactionRolledBackExceptionTest {
    @Test
    public void test() throws Exception {
        final Exception exception = Exception.class.cast(deserialize(serialize(new org.apache.openejb.core.transaction.TransactionRolledbackException("foo", new NullPointerException()))));
        assertThat(exception, instanceOf(TransactionRolledbackException.class));
        assertThat(exception, not(instanceOf(org.apache.openejb.core.transaction.TransactionRolledbackException.class)));
        assertThat(exception.getMessage(), containsString("foo"));
        assertThat(exception.getMessage(), containsString("NullPointerException"));
        exception.printStackTrace();
    }

    private static Object deserialize(final byte[] serial) throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(serial);
        final ObjectInputStream ois = new ObjectInputStreamFiltered(bais);
        return ois.readObject();
    }

    private static byte[] serialize(final Object data) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(data);
        return baos.toByteArray();
    }
}
