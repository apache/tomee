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
package org.apache.openjpa.persistence;

import javax.persistence.Parameter;

/**
 * A user-defined parameter of a query.
 * 
 * A parameter is uniquely identified within the scope of a query by either
 * its name or integral position. The integral position refers to the integer
 * key as specified by the user. The index of this parameter during execution
 * in a datastore query may be different.  
 * <br>
 * A value can be bound to this parameter. This behavior of a parameter carrying
 * its own value is a change from earlier versions (where no explicit abstraction
 * existed for a query parameter).   
 * 
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 * 
 * @param <T> type of value carried by this parameter.
 * 
 */
public class ParameterImpl<T> implements Parameter<T> {
    private final String _name;
    private final Integer _position;
    private final Class<T> _expectedValueType;
    
    /**
     * Construct a positional parameter with the given position as key and
     * given expected value type.
     */
    public ParameterImpl(Integer position, Class<T> expectedValueType) {
        _name = null;
        _position = position;
        _expectedValueType = expectedValueType;
    }
    
    /**
     * Construct a named parameter with the given name as key and
     * given expected value type.
     */
    public ParameterImpl(String name, Class<T> expectedValueType) {
        _name = name;
        _position = null;
        _expectedValueType = expectedValueType;
    }
    
    public final String getName() {
        return _name;
    }
        
    public final Integer getPosition() {
        return _position;
    }
    
    public Class<T> getParameterType() {
      return _expectedValueType;
    }
    
    /**
     * Equals if the other parameter has the same name or position.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Parameter))
            return false;
        Parameter<?> that = (Parameter<?>)other;
        if (_name != null)
            return _name.equals(that.getName());
        if (_position != null)
            return _position.equals(that.getPosition());
        return false;
    }

    @Override
    public int hashCode() {
        return _expectedValueType.hashCode() ^ ((_name != null) ? _name.hashCode() : 0)
            ^ ((_position != null) ? _position.hashCode() : 0); 
    }
    
    public String toString() {
        StringBuilder buf = new StringBuilder("Parameter");
        buf.append("<" + getParameterType().getSimpleName() + ">");
        if (_name != null) {
            buf.append("('" + _name + "')");
        } else if (_position != null) {
            buf.append("(" + _position + ")");
        } else {
            buf.append("(?)");
        }

        return buf.toString();
    }

}
