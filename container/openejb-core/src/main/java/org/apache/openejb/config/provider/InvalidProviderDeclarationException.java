/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.provider;

import org.apache.openejb.config.sys.ServiceProvider;

/**
 * @version $Rev$ $Date$
 */
public class InvalidProviderDeclarationException extends IllegalArgumentException {

    private final ID id;
    private final ServiceProvider provider;

    public InvalidProviderDeclarationException(String s, ID id, ServiceProvider provider) {
        super(String.format("%s - %s", id, s));
        this.id = id;
        this.provider = provider;
    }

    public ServiceProvider getProvider() {
        return provider;
    }

    public ID getId() {
        return id;
    }
}
