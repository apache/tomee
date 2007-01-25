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
package org.apache.openejb.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.persistence.LockModeType;
import javax.persistence.spi.PersistenceUnitInfo;

public class FakeEntityManager implements EntityManager {

    private PersistenceUnitInfo pu = null;
    
    public FakeEntityManager(PersistenceUnitInfo pu){
        this.pu = pu;
    }

    public Object getDelegate() {
        return null;
    }

    public void joinTransaction() {
    }

    public void lock(Object object, LockModeType lockModeType) {
    }

    public PersistenceUnitInfo getPersistenceUnitInfo(){
        return pu;
    }

    public void persist(Object arg0) {
    }
    
    public <T> T merge(T arg0) {
        return null;
    }

    public void remove(Object arg0) {
    }

    public <T> T find(Class<T> arg0, Object arg1) {
        return null;
    }

    public <T> T getReference(Class<T> arg0, Object arg1) {
        return null;
    }

    public void flush() {
    }

    public void setFlushMode(FlushModeType arg0) {
    }

    public FlushModeType getFlushMode() {
        return null;
    }

    public void refresh(Object arg0) {
    }

    public void clear() {
    }

    public boolean contains(Object arg0) {
        return false;
    }

    public Query createQuery(String arg0) {
        return null;
    }

    public Query createNamedQuery(String arg0) {
        return null;
    }

    public Query createNativeQuery(String arg0) {
        return null;
    }

    public Query createNativeQuery(String arg0, Class arg1) {
        return null;
    }

    public Query createNativeQuery(String arg0, String arg1) {
        return null;
    }

    public void close() {
    }

    public boolean isOpen() {
        return false;
    }

    public EntityTransaction getTransaction() {
        return null;
    }

}
