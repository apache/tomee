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
package org.apache.openjpa.conf;

import java.io.Serializable;

/**
 * Configurable options for callbacks.
 *
 * @author Pinaki Poddar
 */
public class CallbackOptions implements Serializable {
    private boolean _isPostPersistCallbackImmediate = false;
    private boolean _allowsMultipleMethodsForSameCallback = false;
    private boolean _allowsDuplicateListener = true;
    
    /**
     * Affirms if the post-persist callback is invoked as soon as a new instance
     * is managed. The default is false, implies that the post-persist callback
     * is invoked after the instance been committed or flushed to the datastore.
     * Defaults to false.
     */
    public boolean getPostPersistCallbackImmediate() {
        return _isPostPersistCallbackImmediate;
    }
    
    /**
     * Sets if the post-persist callback is invoked as soon as a new instance
     * is managed. The default is false, implies that the post-persist callback
     * is invoked after the instance been committed or flushed to the datastore.
     */
    public void setPostPersistCallbackImmediate(boolean flag) {
        _isPostPersistCallbackImmediate = flag;
    }
    
    /** 
     * Flags if multiple methods of the same class can handle the same 
     * callback event.
     * Defaults to false.
     */
    public boolean getAllowsMultipleMethodsForSameCallback() {
        return _allowsMultipleMethodsForSameCallback;
    }
    
    /** 
     * Flags if multiple methods of the same class can handle the same 
     * callback event.
     */
    public void setAllowsMultipleMethodsForSameCallback(boolean flag) {
        _allowsMultipleMethodsForSameCallback = flag;
    }

    /** 
     * Flags if duplicate listeners are allowed to handle the same 
     * callback event.
     * Defaults to true.
     */
    public boolean getAllowsDuplicateListener() {
        return _allowsDuplicateListener;
    }
    
    /** 
     * Flags if duplicate listeners are allowed to handle the same 
     * callback event.
     */
    public void setAllowsDuplicateListener(boolean flag) {
        _allowsDuplicateListener = flag;
    }

}
