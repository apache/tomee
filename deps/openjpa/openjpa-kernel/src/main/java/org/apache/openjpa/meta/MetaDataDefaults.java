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
package org.apache.openjpa.meta;

import java.lang.reflect.Member;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.event.CallbackModes;

/**
 * Populates new metadata with default values.
 *
 * @author Abe White
 */
public interface MetaDataDefaults
    extends CallbackModes {

    /**
     * Return the default access type for a base persistent class with
     * {@link ClassMetaData#ACCESS_UNKNOWN} access type.
     */
    public int getDefaultAccessType();

    /**
     * Return the default identity type for unmapped classes without primary
     * key fields.
     */
    public int getDefaultIdentityType();

    /**
     * What to do on lifecycle callback exceptions.
     */
    public int getCallbackMode();

    /**
     * If callbacks are fired before listeners for the given
     * event type. Defaults to false.
     */
    public boolean getCallbacksBeforeListeners(int type);
   
    /**
     * Whether declared interfaces of a class are treated as persistent
     * types. Defaults to true.
     */
    public boolean isDeclaredInterfacePersistent();

    /**
     * Whether the field in the object id class corresponding to a 
     * datastore id persistence-capable primary key field is the simple 
     * datastore id value of the related instance.  Defaults to false.
     */
    public boolean isDataStoreObjectIdFieldUnwrapped();
 
    /**
     * Whether to ignore members which are not persistent by default
     * during metadata population. Defaults to true.
     */
    public void setIgnoreNonPersistent(boolean ignore);

    /**
     * Populate the given metadata with default settings.
     *
     * @param access access type constant from {@link ClassMetaData}
     */
    public void populate(ClassMetaData meta, int access);

    /**
     * Populate the given metadata with default settings.
     *
     * @param access access type constant from {@link ClassMetaData}
     */
    public void populate(ClassMetaData meta, int access, boolean ignoreTransient);

    /**
     * Return the backing member for the given field metadata.
     */
    public Member getBackingMember(FieldMetaData field);
    
    /**
     * Get the field or getter for the given attribute of the given class.
     * 
     * @param meta is the declaring class 
     * @param attribute name of the logical attribute
     * @param access whether to look for the field of getter method. 
     * If unknown, then field or property is chosen based on the access type 
     * used by the given class.
     * @param scanAnnotation if true and access is unknown then scans the
     * annotation on the member to determine access.
     * 
     * @since 2.0.0
     */
    public Member getMemberByProperty(ClassMetaData meta, String attribute, 
    	int access, boolean scanAnnotation);

    /**
     * Return a runtime exception class to throw for un-implemented
     * managed interface methods.
     */
    public Class getUnimplementedExceptionType();
    
    /**
     * Whether the relationship in MappedSuper class must be
     * uni-directional.  
     * @since 2.0.0
     */
    public boolean isAbstractMappingUniDirectional(OpenJPAConfiguration conf);
    
    /**
     * Whether non-default mapping is allowed.
     * @since 2.0.0
     */
    public boolean isNonDefaultMappingAllowed(OpenJPAConfiguration conf);
    
}
