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
package javax.security.enterprise.credential;

import java.util.Arrays;

import static java.util.Arrays.copyOf;
import static java.util.Objects.requireNonNull;

public class Password {
    private static final char[] EMPTY_VALUE = new char[0];
    private volatile char[] value;

    public Password(char[] value) {
        requireNonNull(value, "Password value may not be null");

        this.value = copyOf(value, value.length);
    }

    public Password(String value) {
        this(null == value ? null : value.toCharArray());
    }

    public char[] getValue() {
        return value;
    }

    public void clear() {
        if (EMPTY_VALUE == value) { return; }

        char[] tempValue = value;
        value = EMPTY_VALUE;

        for (int i = 0; i < tempValue.length; i++) {
            tempValue[i] = 0x00;
        }
    }
    
    public boolean compareTo(String password) {
        return password != null && Arrays.equals(password.toCharArray(), value);
    }
}
