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
 * Interface for dynamically generated classes. Certain getters/setters
 * may either return null or throw an exception depending on the
 * {@link DynamicStorageGenerator}'s field policy.
 *
 * @author Steve Kim
 * @nojavadoc
 * @since 0.3.2.0
 */
public interface DynamicStorage {

    /**
     * Return the number of fields, regardless of type
     */
    public int getFieldCount();

    /**
     * Return the number of object fields
     */
    public int getObjectCount();

    /**
     * Factory method for getting new instances of the same definition.
     */
    public DynamicStorage newInstance();

    /**
     * Get the boolean at the given index.
     */
    public boolean getBoolean(int field);

    /**
     * Set the boolean at the given index.
     */
    public void setBoolean(int field, boolean val);

    /**
     * Get the byte at the given index.
     */
    public byte getByte(int field);

    /**
     * Set the byte at the given index.
     */
    public void setByte(int field, byte val);

    /**
     * Get the char at the given index.
     */
    public char getChar(int field);

    /**
     * Set the char at the given index.
     */
    public void setChar(int field, char val);

    /**
     * Get the double at the given index.
     */
    public double getDouble(int field);

    /**
     * Set the double at the given index.
     */
    public void setDouble(int field, double val);

    /**
     * Get the float at the given index.
     */
    public float getFloat(int field);

    /**
     * Set the float at the given index.
     */
    public void setFloat(int field, float val);

    /**
     * Get the int at the given index.
     */
    public int getInt(int field);

    /**
     * Set the int at the given index.
     */
    public void setInt(int field, int val);

    /**
     * Get the long at the given index.
     */
    public long getLong(int field);

    /**
     * Set the long at the given index.
     */
    public void setLong(int field, long val);

    /**
     * Get the short at the given index.
     */
    public short getShort(int field);

    /**
     * Set the short at the given index.
     */
    public void setShort(int field, short val);

    /**
     * Get the object at the given index.
     */
    public Object getObject(int field);

    /**
     * Set the object at the given index.
     */
    public void setObject(int field, Object val);

    /**
     * Ensure object capacity
     */
    public void initialize ();
}
