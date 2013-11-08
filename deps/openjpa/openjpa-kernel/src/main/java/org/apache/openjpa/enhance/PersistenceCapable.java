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
 * The interface that persistent instances will implement.
 *
 * @author Marc Prud'hommeaux
 */
public interface PersistenceCapable {
    // DO NOT ADD ADDITIONAL DEPENDENCIES TO THIS CLASS

    public static final byte READ_WRITE_OK = 0;
    public static final byte LOAD_REQUIRED = 1;
    public static final byte READ_OK = -1;
    public static final byte CHECK_READ = 1;
    public static final byte MEDIATE_READ = 2;
    public static final byte CHECK_WRITE = 4;
    public static final byte MEDIATE_WRITE = 8;
    public static final byte SERIALIZABLE = 16;

    public static final Object DESERIALIZED = new Object();

    int pcGetEnhancementContractVersion();

    Object pcGetGenericContext();

    StateManager pcGetStateManager();

    void pcReplaceStateManager(StateManager sm);

    void pcProvideField(int fieldIndex);

    void pcProvideFields(int[] fieldIndices);

    void pcReplaceField(int fieldIndex);

    void pcReplaceFields(int[] fieldIndex);

    void pcCopyFields(Object fromObject, int[] fields);

    void pcDirty(String fieldName);

    Object pcFetchObjectId();

    Object pcGetVersion();

    boolean pcIsDirty();

    boolean pcIsTransactional();

    boolean pcIsPersistent();

    boolean pcIsNew();

    boolean pcIsDeleted();

    Boolean pcIsDetached(); // null == unknown

    PersistenceCapable pcNewInstance(StateManager sm, boolean clear);

    PersistenceCapable pcNewInstance(StateManager sm, Object obj,
        boolean clear);

    Object pcNewObjectIdInstance();

    Object pcNewObjectIdInstance(Object obj);

    void pcCopyKeyFieldsToObjectId(Object obj);

    void pcCopyKeyFieldsToObjectId(FieldSupplier supplier, Object obj);

    void pcCopyKeyFieldsFromObjectId(FieldConsumer consumer, Object obj);

    Object pcGetDetachedState();

    void pcSetDetachedState(Object state);
}
