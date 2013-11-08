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

import org.apache.openjpa.kernel.DetachState;

/**
 * Detach options.
 *
 * @author Abe White
 * @nojavadoc
 */
public abstract class DetachOptions
    implements DetachState {

    private boolean _field = true;
    private boolean _transient = true;
    private boolean _manager = true;
    private boolean _access = true;
    
    private boolean _liteAutoDetach = false;
    private boolean _detachProxyFields = true;

    /**
     * The {@link DetachState} constant.
     */
    public abstract int getDetachState();

    /**
     * Whether to add a detached state field to enhanced classes.
     */
    public boolean getDetachedStateField() {
        return _field;
    }

    /**
     * Whether to add a detached state field to enhanced classes.
     */
    public void setDetachedStateField(boolean val) {
        _field = val;
        if (!val)
            _manager = false;
    }

    /**
     * For auto-configuration of the detached state field. Accepts values
     * "true", "false", or "transient".
     */
    public void setDetachedStateField(String val) {
        if (val == null)
            return;
        if ("transient".equals(val)) {
            setDetachedStateField(true);
            _transient = true;
        } else if ("true".equals(val)) {
            setDetachedStateField(true);
            _transient = false;
        } else if ("false".equals(val)) {
            setDetachedStateField(false);
            _transient = false;
        } else
            throw new IllegalArgumentException("DetachedStateField=" + val);
    }

    /**
     * Whether to use transient detached state.
     */
    public boolean isDetachedStateTransient() {
        return _transient;
    }

    /**
     * Whether to use transient detached state.
     */
    public void setDetachedStateTransient(boolean val) {
        _transient = val;
    }

    /**
     * Whether to use a detached state manager on types that allow it.
     * Types that do not use detached state or that declare a custom detached
     * state field to maintain serialization compatibility will never use
     * a detached state manager. Defaults to true.
     */
    public boolean getDetachedStateManager() {
        return _manager;
    }

    /**
     * Whether to use a detached state manager on types that allow it.
     * Types that do not use detached state or that declare a custom detached
     * state field to maintain serialization compatibility will never use
     * a detached state manager. Defaults to true.
     */
    public void setDetachedStateManager(boolean val) {
        _manager = val;
    }

    /**
     * Whether to allow access to unloaded detached fields. This setting only
     * applies to instances with detached state managers.
     */
    public boolean getAccessUnloaded() {
        return _access;
    }

    /**
     * Whether to allow access to unloaded detached fields. This setting only
     * applies to instances with detached state managers.
     */
    public void setAccessUnloaded(boolean val) {
        _access = val;
    }
    
    /**
     * Whether to use lite detachment when auto detaching. This setting only applies when
     * DetachState is set to loaded.
     */
    public void setLiteAutoDetach(boolean b) {
        _liteAutoDetach = b;
    }

    /**
     * Whether to use lite detachment when auto detaching. This setting only applies when
     * DetachState is set to loaded.
     */
    public boolean getLiteAutoDetach() {
        return (getDetachState() & DETACH_LOADED) == 1 && _liteAutoDetach;
    }
    
    /**
     * Whether to detach proxy fields.
     */
    public void setDetachProxyFields(boolean b) {
        _detachProxyFields = b;
    }
    
    /**
     * Whether to detach proxy fields.
     */
    public boolean getDetachProxyFields() {
        // This property can only be set to false when using lite auto detach.
        if(!_liteAutoDetach){
            return true;
        }
        return _detachProxyFields;
    }

    /**
     * Detach loaded state.
     */
    public static class Loaded
        extends DetachOptions {

        public int getDetachState() {
            return DETACH_LOADED;
        }
    }

    /**
     * Public to allow reflection.
     */
    public static class FetchGroups
        extends DetachOptions {

        public int getDetachState() {
            return DETACH_FGS;
        }
    }

    /**
     * Public to allow reflection.
     */
    public static class All
        extends DetachOptions {

        public int getDetachState() {
            return DETACH_ALL;
		}		
	}
}
