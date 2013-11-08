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

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * An immutable representation of a Specification supported by OpenJPA.
 * 
 * Available via {@linkplain OpenJPAConfiguration#getSpecificationInstance()()} 
 * for configuration that may depend on Specification version.
 * 
 * @author Pinaki Poddar
 *
 */
public class Specification {
    private String _name = "";
    private int    _major = 1;
    private String _minor = "0";
    private String _description = "";
    private Compatibility _compatibility;

    private static Localizer _loc = Localizer.forPackage(Specification.class);
    
    /**
     * Construct from a String that encodes name and version fields.
     * 
     * @param fullName a encoded string in the following prescribed format.
     * <code>name major.minor</code> e.g. <code>JPA 2.0-draft</code>
     * Only the 'name' field is mandatory. 
     * 'major' version defaults to 1 and must be an integer. 
     * 'minor' version defaults to 0 and can be a String. 
     */
    public Specification(String fullName) {
        try {
            Object[] tokens = parse(fullName);
            _name = tokens[0].toString();
            _major = tokens.length > 1 ? 
                Integer.parseInt(tokens[1].toString()) : 1;
            _minor = tokens.length > 2 ? tokens[2].toString() : "0";
        } catch (Exception e) {
            throw new UserException(_loc.get("spec-wrong-format", fullName));
        }
    }
    
    public String getName() {
        return _name;
    }
    
    public int getVersion() {
        return _major;
    }
    
    public String getMinorVersion() {
        return _minor;
    }
    
    public String getDescription() {
        return _description;
    }
    
    public Specification setDescription(String description) {
        this._description = description;
        return this;
    }
    
    /**
     * Affirms if the given argument is equal to this receiver.
     */
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || !this.getClass().isInstance(other))
            return false;
        Specification that = (Specification)other;
        return StringUtils.equals(_name, this._name) && _major == that._major 
            && StringUtils.equals(_minor, this._minor);
    }    
    
    /**
     * Affirms if the given specification has the same name of this receiver,
     * ignoring the case.
     */
    public boolean isSame(Specification other) {
        return this == other 
            || (other != null && _name.equalsIgnoreCase(other._name));
    }
    
    /**
     * Affirms if the given string equals name of this receiver, ignoring the 
     * case.
     */
    public boolean isSame(String other) {
        return _name.equalsIgnoreCase(other);
    }
    
    /**
     * Compares major version number of the given Specification with this 
     * receiver.

     * @return 0 if they are equal.
     *       > 0 if this receiver is higher version.
     *       < 0 if this receiver is lower version.
     */
    public int compareVersion(Specification other) {
        return _major > other._major ? 1 : _major == other._major ? 0 : -1;
    }
    
    public String toString() {
        return _name.toUpperCase() + " " + _major + "." + _minor;
    }
    
    private Object[] parse(String str) {
        int space = str.indexOf(' ');
        
        if (space == -1)
            return new Object[]{str};
        String name = str.substring(0,space);
        String version = str.substring(space+1);
        int dot = version.indexOf('.');
        if (dot == -1)
            return new Object[] {name, version};
        return new Object[] {name, 
            version.substring(0,dot), version.substring(dot+1)};
    }
    
    /**
     * Associate a compatibility object with this Specification instance
     * @param compatibility a Compatibility object with flags set in compliance
     * with this Specification instance.
     */
    public void setCompatibility(Compatibility compatibility) {
        _compatibility = compatibility;
    }
    
    /**
     * Return the compatibility object associated with this Specification instance.
     * @return
     */
    public Compatibility getCompatibility() {
        return _compatibility;
    }
}
