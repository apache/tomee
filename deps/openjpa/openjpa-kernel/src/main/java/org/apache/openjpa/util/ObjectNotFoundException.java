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
package org.apache.openjpa.util;

import java.util.Collection;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Localizer.Message;

/**
 * Exception type reserved for failed object lookups.
 *
 * @author Abe White
 */
public class ObjectNotFoundException
    extends StoreException {

    private static final transient Localizer _loc = Localizer.forPackage
        (ObjectNotFoundException.class);

    public ObjectNotFoundException(Message msg) {
        super(msg);
    }

    public ObjectNotFoundException(Object failed) {
        super(_loc.get("not-found", Exceptions.toString(failed)));
        setFailedObject(failed);
    }

    public ObjectNotFoundException(Collection failed, Throwable[] nested) {
        super(_loc.get("not-found-multi", Exceptions.toString(failed)));
        setNestedThrowables(nested);
    }

    public int getSubtype() {
        return OBJECT_NOT_FOUND;
    }
}

