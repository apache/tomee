/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.tomitribe.connector.starter.adapter;

import java.util.logging.Logger;

import javax.resource.ResourceException;

import javax.resource.spi.ManagedConnectionMetaData;

public class SampleManagedConnectionMetaData implements ManagedConnectionMetaData {

    private static Logger log = Logger.getLogger(SampleManagedConnectionMetaData.class.getName());

    public SampleManagedConnectionMetaData() {

    }

    @Override
    public String getEISProductName() throws ResourceException {
        log.finest("getEISProductName()");
        return null; //TODO
    }

    @Override
    public String getEISProductVersion() throws ResourceException {
        log.finest("getEISProductVersion()");
        return null; //TODO
    }

    @Override
    public int getMaxConnections() throws ResourceException {
        log.finest("getMaxConnections()");
        return 0; //TODO
    }

    @Override
    public String getUserName() throws ResourceException {
        log.finest("getUserName()");
        return null; //TODO
    }
}
