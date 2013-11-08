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
package org.apache.openjpa.validation;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.LifecycleEventManager;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * An extension of LifecycleEventManager which adds validation capabilities for
 * specific lifecycle events.  Validation occurs after firing all lifecycle 
 * events and callbacks.
 * 
 */
@SuppressWarnings("serial")
public class ValidatingLifecycleEventManager extends LifecycleEventManager
    implements Configurable {

    private OpenJPAConfiguration _conf = null;
    private Validator _validator = null;
    protected boolean _validationEnabled = true;

    /**
     * Constructor which accepts a reference to the validator to use.  If null,
     * no validation will occur.
     * @param validator
     */
    public ValidatingLifecycleEventManager() {
        super();
    }

    /* (non-Javadoc)
     * @see org.apache.openjpa.lib.conf.Configurable#endConfiguration()
     */
    public void endConfiguration() {
        _validator = (Validator)_conf.getValidatorInstance();
    }
   
    /* (non-Javadoc)
     * @see org.apache.openjpa.lib.conf.Configurable#setConfiguration(
     *      org.apache.openjpa.lib.conf.Configuration)
     */
    public void setConfiguration(Configuration conf) {
        if (conf instanceof OpenJPAConfiguration) {
            _conf = (OpenJPAConfiguration)conf;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.openjpa.lib.conf.Configurable#startConfiguration()
     */
    public void startConfiguration() {
    }

    @Override
    public boolean hasUpdateListeners(Object source, ClassMetaData meta) {
        if (_validator == null) {            
            return super.hasUpdateListeners(source, meta);
        }
        return _validator.validating(source, LifecycleEvent.BEFORE_UPDATE) ||
            super.hasUpdateListeners(source, meta);
    }

    @Override
    public boolean hasPersistListeners(Object source, ClassMetaData meta) {
        if (_validator == null) {            
            return super.hasPersistListeners(source, meta);
        }
        return _validator.validating(source, LifecycleEvent.BEFORE_PERSIST) ||
            super.hasPersistListeners(source, meta);        
    }

    @Override
    public boolean hasDeleteListeners(Object source, ClassMetaData meta) {
        if (_validator == null) {            
            return super.hasDeleteListeners(source, meta);
        }
        return _validator.validating(source, LifecycleEvent.BEFORE_DELETE) ||
            super.hasDeleteListeners(source, meta);
    }
    
    @Override
    public Exception[] fireEvent(Object source,
        ClassMetaData meta, int type) {
        
        return fireEvent(source, null, meta, type);
    }
    
    @Override
    public Exception[] fireEvent(Object source, Object related,
        ClassMetaData meta, int type) {

        // Fire all pre-validation events and handlers.
        Exception[] evx = super.fireEvent(source, related, meta, type);

        // If there are exceptions and the event manager is marked fail fast
        // do not validate
        if (evx != null && evx.length > 0 && isFailFast())
            return evx;
        
        // If a validator is provided and the source object should be validated,
        // validate it and return any exceptions
        if (_validationEnabled && _validator != null && _validator.validating(source, type)) {
            ValidationException vex = _validator.validate(source, type);
            if (vex != null) {
                if (evx == null || evx.length == 0) {
                    evx = new Exception[1];
                    evx[0] = vex;
                }
                else {
                    // resize the exception array and add the validation
                    // exception
                    Exception[] vevx = new Exception[evx.length +1];
                    System.arraycopy(vevx, 0, evx, 0, evx.length);
                    vevx[evx.length+1] = vex;
                    evx = vevx;
                }
            }
        }
        return evx;
    }

    /**
     * Whether this LifeCycleEventManager has had at least one listener or callback
     * registered.  Used for a quick test when firing events.
     * @return boolean
     */
    @Override
    public boolean isActive(ClassMetaData meta) {
        return isValidationEnabled() || super.isActive(meta);
    }

    public boolean isValidationEnabled() {
        return _validationEnabled;
    }

    public boolean setValidationEnabled(boolean enabled) {
        boolean val = _validationEnabled;
        _validationEnabled = enabled;
        return val;
    }
}
