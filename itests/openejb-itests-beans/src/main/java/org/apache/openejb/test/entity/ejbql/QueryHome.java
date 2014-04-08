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

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface QueryHome extends EJBHome {
    public QueryRemote findByPrimaryKey(Integer primaryKey) throws FinderException, RemoteException;

    /**
     * Select a single string field
     */
    public abstract String selectSingleStringField(String value) throws FinderException, RemoteException;

    /**
     * Select a single boolean field
     */
    public abstract boolean selectSingleBooleanField(boolean value) throws FinderException, RemoteException;

    /**
     * Select a single char field
     */
    public abstract char selectSingleCharField(char value) throws FinderException, RemoteException;

    /**
     * Select a single byte field
     */
    public abstract byte selectSingleByteField(byte value) throws FinderException, RemoteException;

    /**
     * Select a single short field
     */
    public abstract short selectSingleShortField(short value) throws FinderException, RemoteException;

    /**
     * Select a single int field
     */
    public abstract int selectSingleIntField(int value) throws FinderException, RemoteException;

    /**
     * Select a single long field
     */
    public abstract long selectSingleLongField(long value) throws FinderException, RemoteException;

    /**
     * Select a single float field
     */
    public abstract float selectSingleFloatField(float value) throws FinderException, RemoteException;

    /**
     * Select a single double field
     */
    public abstract double selectSingleDoubleField(double value) throws FinderException, RemoteException;

    /**
     * Select a collection string field
     */
    public abstract Collection selectCollectionStringField() throws FinderException, RemoteException;

    /**
     * Select a collection boolean field
     */
    public abstract Collection selectCollectionBooleanField() throws FinderException, RemoteException;

    /**
     * Select a collection char field
     */
    public abstract Collection selectCollectionCharField() throws FinderException, RemoteException;

    /**
     * Select a collection byte field
     */
    public abstract Collection selectCollectionByteField() throws FinderException, RemoteException;

    /**
     * Select a collection short field
     */
    public abstract Collection selectCollectionShortField() throws FinderException, RemoteException;

    /**
     * Select a collection int field
     */
    public abstract Collection selectCollectionIntField() throws FinderException, RemoteException;

    /**
     * Select a collection long field
     */
    public abstract Collection selectCollectionLongField() throws FinderException, RemoteException;

    /**
     * Select a collection float field
     */
    public abstract Collection selectCollectionFloatField() throws FinderException, RemoteException;

    /**
     * Select a collection double field
     */
    public abstract Collection selectCollectionDoubleField() throws FinderException, RemoteException;

    /**
     * Select a single local ejb
     */
    public abstract Object selectSingleLocalEjb(int value) throws FinderException, RemoteException;

    /**
     * Select a single remote ejb
     */
    public abstract Object selectSingleRemoteEjb(int test) throws FinderException, RemoteException;

    /**
     * Select a collection local ejb
     */
    public abstract Collection selectCollectionLocalEjb() throws FinderException, RemoteException;

    /**
     * Select a collection remote ejb
     */
    public abstract Collection selectCollectionRemoteEjb() throws FinderException, RemoteException;
}
