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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.entity.ejbql;

import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;
import java.util.Collection;

public interface QueryLocalHome extends EJBLocalHome {
    public QueryLocal findByPrimaryKey(Integer primaryKey) throws FinderException;

    /**
     * Select a single string field
     */
    public abstract String ejbSelectSingleStringField(String value) throws FinderException;

    /**
     * Select a single boolean field
     */
    public abstract boolean ejbSelectSingleBooleanField(boolean value) throws FinderException;

    /**
     * Select a single char field
     */
    public abstract char ejbSelectSingleCharField(char value) throws FinderException;

    /**
     * Select a single byte field
     */
    public abstract byte ejbSelectSingleByteField(byte value) throws FinderException;

    /**
     * Select a single short field
     */
    public abstract short ejbSelectSingleShortField(short value) throws FinderException;

    /**
     * Select a single int field
     */
    public abstract int ejbSelectSingleIntField(int value) throws FinderException;

    /**
     * Select a single long field
     */
    public abstract long ejbSelectSingleLongField(long value) throws FinderException;

    /**
     * Select a single float field
     */
    public abstract float ejbSelectSingleFloatField(float value) throws FinderException;

    /**
     * Select a single double field
     */
    public abstract double ejbSelectSingleDoubleField(double value) throws FinderException;

    /**
     * Select a collection string field
     */
    public abstract Collection ejbSelectCollectionStringField(String value) throws FinderException;

    /**
     * Select a collection boolean field
     */
    public abstract Collection ejbSelectCollectionBooleanField(boolean test) throws FinderException;

    /**
     * Select a collection char field
     */
    public abstract Collection ejbSelectCollectionCharField(char test) throws FinderException;

    /**
     * Select a collection byte field
     */
    public abstract Collection ejbSelectCollectionByteField(byte test) throws FinderException;

    /**
     * Select a collection short field
     */
    public abstract Collection ejbSelectCollectionShortField(short test) throws FinderException;

    /**
     * Select a collection int field
     */
    public abstract Collection ejbSelectCollectionIntField(int test) throws FinderException;

    /**
     * Select a collection long field
     */
    public abstract Collection ejbSelectCollectionLongField(long test) throws FinderException;

    /**
     * Select a collection float field
     */
    public abstract Collection ejbSelectCollectionFloatField(float test) throws FinderException;

    /**
     * Select a collection double field
     */
    public abstract Collection ejbSelectCollectionDoubleField(double test) throws FinderException;

    /**
     * Select a single local ejb
     */
    public abstract Object ejbSelectSingleLocalEjb(String test) throws FinderException;

    /**
     * Select a single remote ejb
     */
    public abstract Object ejbSelectSingleRemoteEjb(String test) throws FinderException;

    /**
     * Select a collection local ejb
     */
    public abstract Collection ejbSelectCollectionLocalEjb(String test) throws FinderException;

    /**
     * Select a collection remote ejb
     */
    public abstract Collection ejbSelectCollectionRemoteEjb(String test) throws FinderException;}
