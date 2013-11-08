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
package org.apache.openjpa.meta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import junit.framework.TestCase;
import org.apache.openjpa.meta.FieldMetaData.MemberProvider;

public class TestMemberProvider
    extends TestCase {

    private String field;

    public void testField()
        throws NoSuchFieldException, IOException, ClassNotFoundException {
        MemberProvider b = new MemberProvider(
            getClass().getDeclaredField("field"));
        MemberProvider b2 = roundtrip(b);
        assertEquals(b.getMember(), b2.getMember());
    }

    public void testMethod()
        throws NoSuchMethodException, IOException, ClassNotFoundException {
        MemberProvider b = new MemberProvider(
            getClass().getDeclaredMethod("testMethod", null));
        MemberProvider b2 = roundtrip(b);
        assertEquals(b.getMember(), b2.getMember());
    }

    private MemberProvider roundtrip(MemberProvider other)
        throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(other);
        out.flush();
        byte[] bytes = out.toByteArray();
        out.close();

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        return (MemberProvider) new ObjectInputStream(in).readObject();
    }
}
