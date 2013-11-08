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
package org.apache.openjpa.slice.jdbc;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfigurationImpl;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.slice.SlicePersistence;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.UserException;

/**
 * A fetch configuration that is aware of special hint to narrow its operation on 
 * subset of slices.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class TargetFetchConfiguration extends JDBCFetchConfigurationImpl implements FetchConfiguration {
    boolean _explicitTarget = false;
    
    public TargetFetchConfiguration() {
        super();
    }
    
    /**
     * Setting hints on this configuration is treated specially if the given key
     * is {@linkplain SlicePersistence#HINT_TARGET a target hint}.
     *  
     * @param value if the given key is target hint, then the value can be either
     * null, a String or a non-zero sized String array. It can not be a zero-sized
     * String array.
     */
    @Override
    public void setHint(String key, Object value) {
        super.setHint(key, value);
        _explicitTarget = SlicePersistence.HINT_TARGET.equals(key);
    }

    public void setHint(String key, Object value, Object original) {
        super.setHint(key, value, original);
        _explicitTarget = SlicePersistence.HINT_TARGET.equals(key);
    }
       
    public void setTargets(String[] targets) {
        super.setHint(SlicePersistence.HINT_TARGET, targets);
        _explicitTarget = false;
    }
    
    /**
     * Affirms if the target is set on this receiver explicitly (i.e. by the user).
     */
    public boolean isExplicitTarget() {
        return _explicitTarget;
    }
    
    String[] toSliceNames(Object o, boolean user) {
        if (o == null)
            return null;
        if (o instanceof String) {
            return new String[]{o.toString()};
        }
        if (o instanceof String[]) {
            if (((String[])o).length == 0) {
                throw new InternalException("Hint values " + o + " are wrong type " + o.getClass());
                
            }
            return (String[])o;
        }
        throw new InternalException("Hint values " + o + " are wrong type " + o.getClass());
    }
    
    void assertTargets(String[] targets, boolean user) {
        if (targets != null && targets.length == 0) {
            if (user) {
                throw new UserException("Hint values " + targets + " are empty");
            }
        }
    }
    
    protected TargetFetchConfiguration newInstance(ConfigurationState state) {
        JDBCConfigurationState jstate = (state == null) ? null : _state;
        return new TargetFetchConfiguration(state, jstate);
    }
    
    protected TargetFetchConfiguration(ConfigurationState state, 
            JDBCConfigurationState jstate) {
            super(state, jstate);
        }


}
