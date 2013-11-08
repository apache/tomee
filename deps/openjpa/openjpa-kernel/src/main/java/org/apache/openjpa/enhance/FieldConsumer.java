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
package org.apache.openjpa.enhance;

/**
 * Consumes persistent field values.
 */
public interface FieldConsumer {

    /**
     * Set the value of the given field.
     */
    void storeBooleanField(int fieldIndex, boolean value);

    /**
     * Set the value of the given field.
     */
    void storeCharField(int fieldIndex, char value);

    /**
     * Set the value of the given field.
     */
    void storeByteField(int fieldIndex, byte value);

    /**
     * Set the value of the given field.
     */
    void storeShortField(int fieldIndex, short value);

    /**
     * Set the value of the given field.
     */
    void storeIntField(int fieldIndex, int value);

    /**
     * Set the value of the given field.
     */
    void storeLongField(int fieldIndex, long value);

    /**
     * Set the value of the given field.
     */
    void storeFloatField(int fieldIndex, float value);

    /**
     * Set the value of the given field.
     */
    void storeDoubleField(int fieldIndex, double value);

    /**
     * Set the value of the given field.
     */
    void storeStringField(int fieldIndex, String value);

    /**
     * Set the value of the given field.
     */
    void storeObjectField(int fieldIndex, Object value);
}
