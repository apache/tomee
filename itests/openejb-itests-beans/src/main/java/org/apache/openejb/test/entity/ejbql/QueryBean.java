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

import jakarta.ejb.EntityBean;
import jakarta.ejb.EntityContext;
import jakarta.ejb.FinderException;
import java.util.Collection;

public abstract class QueryBean implements EntityBean {
    public abstract Integer getId();

    public abstract void setId(Integer id);

    public void setEntityContext(final EntityContext ctx) {
    }

    public void unsetEntityContext() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    /**
     * Select a single string field
     */
    public String ejbHomeSelectSingleStringField(final String value) throws FinderException {
        return ejbSelectSingleStringField(value);
    }

    public abstract String ejbSelectSingleStringField(String value) throws FinderException;

    /**
     * Select a single boolean field
     */
    public boolean ejbHomeSelectSingleBooleanField(final boolean value) throws FinderException {
        return ejbSelectSingleBooleanField(value);
    }

    public abstract boolean ejbSelectSingleBooleanField(boolean value) throws FinderException;

    /**
     * Select a single char field
     */
    public char ejbHomeSelectSingleCharField(final char value) throws FinderException {
        return ejbSelectSingleCharField(value);
    }

    public abstract char ejbSelectSingleCharField(char value) throws FinderException;

    /**
     * Select a single byte field
     */
    public byte ejbHomeSelectSingleByteField(final byte value) throws FinderException {
        return ejbSelectSingleByteField(value);
    }

    public abstract byte ejbSelectSingleByteField(byte value) throws FinderException;

    /**
     * Select a single short field
     */
    public short ejbHomeSelectSingleShortField(final short value) throws FinderException {
        return ejbSelectSingleShortField(value);
    }

    public abstract short ejbSelectSingleShortField(short value) throws FinderException;

    /**
     * Select a single int field
     */
    public int ejbHomeSelectSingleIntField(final int value) throws FinderException {
        return ejbSelectSingleIntField(value);
    }

    public abstract int ejbSelectSingleIntField(int value) throws FinderException;

    /**
     * Select a single long field
     */
    public long ejbHomeSelectSingleLongField(final long value) throws FinderException {
        return ejbSelectSingleLongField(value);
    }

    public abstract long ejbSelectSingleLongField(long value) throws FinderException;

    /**
     * Select a single float field
     */
    public float ejbHomeSelectSingleFloatField(final float value) throws FinderException {
        return ejbSelectSingleFloatField(value);
    }

    public abstract float ejbSelectSingleFloatField(float value) throws FinderException;

    /**
     * Select a single double field
     */
    public double ejbHomeSelectSingleDoubleField(final double value) throws FinderException {
        return ejbSelectSingleDoubleField(value);
    }

    public abstract double ejbSelectSingleDoubleField(double value) throws FinderException;

    /**
     * Select a collection string field
     */
    public Collection ejbHomeSelectCollectionStringField() throws FinderException {
        return ejbSelectCollectionStringField();
    }

    public abstract Collection ejbSelectCollectionStringField() throws FinderException;

    /**
     * Select a collection boolean field
     */
    public Collection ejbHomeSelectCollectionBooleanField() throws FinderException {
        return ejbSelectCollectionBooleanField();
    }

    public abstract Collection ejbSelectCollectionBooleanField() throws FinderException;

    /**
     * Select a collection char field
     */
    public Collection ejbHomeSelectCollectionCharField() throws FinderException {
        return ejbSelectCollectionCharField();
    }

    public abstract Collection ejbSelectCollectionCharField() throws FinderException;

    /**
     * Select a collection byte field
     */
    public Collection ejbHomeSelectCollectionByteField() throws FinderException {
        return ejbSelectCollectionByteField();
    }

    public abstract Collection ejbSelectCollectionByteField() throws FinderException;

    /**
     * Select a collection short field
     */
    public Collection ejbHomeSelectCollectionShortField() throws FinderException {
        return ejbSelectCollectionShortField();
    }

    public abstract Collection ejbSelectCollectionShortField() throws FinderException;

    /**
     * Select a collection int field
     */
    public Collection ejbHomeSelectCollectionIntField() throws FinderException {
        return ejbSelectCollectionIntField();
    }

    public abstract Collection ejbSelectCollectionIntField() throws FinderException;

    /**
     * Select a collection long field
     */
    public Collection ejbHomeSelectCollectionLongField() throws FinderException {
        return ejbSelectCollectionLongField();
    }

    public abstract Collection ejbSelectCollectionLongField() throws FinderException;

    /**
     * Select a collection float field
     */
    public Collection ejbHomeSelectCollectionFloatField() throws FinderException {
        return ejbSelectCollectionFloatField();
    }

    public abstract Collection ejbSelectCollectionFloatField() throws FinderException;

    /**
     * Select a collection double field
     */
    public Collection ejbHomeSelectCollectionDoubleField() throws FinderException {
        return ejbSelectCollectionDoubleField();
    }

    public abstract Collection ejbSelectCollectionDoubleField() throws FinderException;

    /**
     * Select a single local ejb
     */
    public Object ejbHomeSelectSingleLocalEjb(final int value) throws FinderException {
        return ejbSelectSingleLocalEjb(value);
    }

    public abstract Object ejbSelectSingleLocalEjb(int value) throws FinderException;

    /**
     * Select a single remote ejb
     */
    public Object ejbHomeSelectSingleRemoteEjb(final int value) throws FinderException {
        return ejbSelectSingleRemoteEjb(value);
    }

    public abstract Object ejbSelectSingleRemoteEjb(int value) throws FinderException;

    /**
     * Select a collection local ejb
     */
    public Collection ejbHomeSelectCollectionLocalEjb() throws FinderException {
        return ejbSelectCollectionLocalEjb();
    }

    public abstract Collection ejbSelectCollectionLocalEjb() throws FinderException;

    /**
     * Select a collection remote ejb
     */
    public Collection ejbHomeSelectCollectionRemoteEjb() throws FinderException {
        return ejbSelectCollectionRemoteEjb();
    }

    public abstract Collection ejbSelectCollectionRemoteEjb() throws FinderException;
}
