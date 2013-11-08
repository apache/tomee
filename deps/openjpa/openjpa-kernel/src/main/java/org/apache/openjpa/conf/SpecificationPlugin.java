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

import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.ObjectValue;
import org.apache.openjpa.lib.conf.ProductDerivations;
import org.apache.openjpa.lib.conf.Value;
import org.apache.openjpa.lib.conf.ValueListener;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * A plug-in for Specification that enforces certain overwriting rules.
 * 
 * @author Pinaki Poddar
 *
 */
public class SpecificationPlugin extends ObjectValue implements ValueListener {
    private Configuration _conf;
    protected static final Localizer _loc = Localizer.forPackage
        (SpecificationPlugin.class);
    
    public SpecificationPlugin(Configuration conf, String prop) {
        super(prop);
        _conf = conf;
        addListener(this);
    }
    
    @Override
    public Class<?> getValueType() {
        return Specification.class;
    }
    
    /**
     * Set a value from the given String after validating.
     * 
     * @param str can be null to set the Specification to null.
     * If non-null, then the String must be in Specification format
     * @see Specification#create(String)  
     */
    @Override
    public void setString(String str) {
        this.set(str == null ? null : new Specification(str));
    }
    
    /**
     * Set a value from the given object after validating.
     * 
     * @param obj can be null to set the Specification to null.
     */
    @Override
    public void set(Object obj) {
        if (obj == null) {
            super.set(null);
            return;
        }
        if (obj instanceof Specification == false) {
            throw new UserException(_loc.get("spec-wrong-obj", obj, 
                obj.getClass())).setFatal(true);
        }
        validateOverwrite((Specification)obj);
        super.set(obj);
    }
    
    /**
     * Validates if the given Specification can overwrite the current 
     * Specification. If the given Specification is not same as the
     * current one, then it is valid to overwrite.
     * If the given Specification is same as the current Specification then
     * it must have a major version number equal or less than the current one.
     * 
     * @exception fatal UserException if the given Specification is same as
     * the current Specification but has a higher major version.
     * 
     * @see Specification#equals(Object)
     */
    protected void validateOverwrite(Specification newSpec) {
        Specification current = (Specification)get();
        if (current != null) {
            Log log = _conf.getConfigurationLog(); 
            if (!current.isSame(newSpec)) {
                log.warn(_loc.get("spec-different", newSpec, current));
                return;
            }
            if (current.compareVersion(newSpec) < 0) {
                throw new UserException(_loc.get("spec-version-higher", 
                    newSpec, current)).setFatal(true);
            }
            if (current.compareVersion(newSpec) > 0) {
                log.warn(_loc.get("spec-version-lower", newSpec, current));
            }
        }
    }
    
    public void valueChanged(Value value) {
        if (this.getClass().isInstance(value))
            ProductDerivations.afterSpecificationSet(_conf);
    }
}
