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
package org.apache.openjpa.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.openjpa.lib.util.Localizer;

/**
 * Exception type reserved for violations of integrity constraints.
 *
 * @author Patrick Linskey
 */
public class ReferentialIntegrityException
    extends StoreException {

    public static final int IV_UNKNOWN = 0;
    public static final int IV_DUPLICATE_OID = 1;
    public static final int IV_UNIQUE = 2;
    public static final int IV_REFERENCE = 3;
    public static final int IV_MIXED = 4;

    private static final transient Localizer _loc = Localizer.forPackage
        (ReferentialIntegrityException.class);

    private int _iv = IV_UNKNOWN;

    public ReferentialIntegrityException(String msg) {
        super(msg);
    }

    public ReferentialIntegrityException(int iv) {
        this(getMessage(iv));
        setIntegrityViolation(iv);
    }

    private static String getMessage(int iv) {
        switch (iv) {
            case IV_DUPLICATE_OID:
                return _loc.get("dup-oid").getMessage();
            case IV_UNIQUE:
                return _loc.get("unique").getMessage();
            default:
                return _loc.get("ref-integrity").getMessage();
        }
    }

    public int getSubtype() {
        return REFERENTIAL_INTEGRITY;
    }

    /**
     * The type of integrity violation that occurred.
     */
    public int getIntegrityViolation() {
        return _iv;
    }

    /**
     * The type of integrity violation that occurred.
     */
    public ReferentialIntegrityException setIntegrityViolation(int iv) {
        _iv = iv;
        return this;
    }

    private void writeObject(ObjectOutputStream out)
        throws IOException {
        out.writeInt(_iv);
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        _iv = in.readInt();
	}
}
