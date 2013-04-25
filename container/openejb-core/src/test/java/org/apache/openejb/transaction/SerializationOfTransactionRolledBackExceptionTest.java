/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package org.apache.openejb.transaction;

import org.junit.Test;

import javax.transaction.TransactionRolledbackException;
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
        final ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }

    private static byte[] serialize(final Object data) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(data);
        return baos.toByteArray();
    }
}
