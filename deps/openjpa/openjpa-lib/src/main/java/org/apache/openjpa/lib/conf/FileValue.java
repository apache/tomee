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
package org.apache.openjpa.lib.conf;

import java.io.File;
import java.security.AccessController;

import org.apache.commons.lang.ObjectUtils;
import org.apache.openjpa.lib.util.J2DoPrivHelper;

/**
 * A {@link File} {@link Value}.
 *
 * @author Marc Prud'hommeaux
 */
public class FileValue extends Value {

    private File value;

    public FileValue(String prop) {
        super(prop);
    }

    public Class<File> getValueType() {
        return File.class;
    }

    /**
     * The internal value.
     */
    public void set(File value) {
        assertChangeable();
        File oldValue = this.value;
        this.value = value;
        if (!ObjectUtils.equals(oldValue, value))
            valueChanged();
    }

    /**
     * The internal value.
     */
    public File get() {
        return value;
    }

    protected String getInternalString() {
        return (value == null) ? null : AccessController.doPrivileged(
            J2DoPrivHelper.getAbsolutePathAction(value));
    }

    protected void setInternalString(String val) {
        set(new File(val));
    }

    protected void setInternalObject(Object obj) {
        set((File) obj);
    }
}

