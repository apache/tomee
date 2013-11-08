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
package org.apache.openjpa.persistence;

/**
 * Represents a store sequence.
 *
 * @author Abe White
 * @since 0.4.1
 * @published
 */
public interface Generator {

    public static final String UUID_HEX = "uuid-hex";
    public static final String UUID_STRING = "uuid-string";
    public static final String UUID_TYPE4_STRING = "uuid-type4-string";
    public static final String UUID_TYPE4_HEX = "uuid-type4-hex";

    /**
     * The sequence name.
     */
    public String getName();

    /**
     * The next sequence value.
     */
    public Object next();

    /**
     * The current sequence value, or null if the sequence does not
     * support current values.
     */
    public Object current();

    /**
     * Hint to the sequence to allocate additional values up-front for
     * efficiency.
     */
    public void allocate(int additional);

    /**
     * @deprecated cast to {@link GeneratorImpl} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public org.apache.openjpa.kernel.Seq getDelegate();
}
