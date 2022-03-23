/**
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

package org.apache.openejb.arquillian.tests.sharedenv;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;


public class Green implements Environment {

    @Resource(name = "returnEmail")
    private String returnEmail;

    @Resource(name = "connectionPool")
    private Integer connectionPool;

    @Resource(name = "startCount")
    private Long startCount;

    @Resource(name = "initSize")
    private Short initSize;

    @Resource(name = "totalQuantity")
    private Byte totalQuantity;

    @Resource(name = "enableEmail")
    private Boolean enableEmail;

    @Resource(name = "optionDefault")
    private Character optionDefault;

    @Inject
    public Green() {
    }

    public Green(String returnEmail, Integer connectionPool, Long startCount, Short initSize, Byte totalQuantity, Boolean enableEmail, Character optionDefault) {
        this.returnEmail = returnEmail;
        this.connectionPool = connectionPool;
        this.startCount = startCount;
        this.initSize = initSize;
        this.totalQuantity = totalQuantity;
        this.enableEmail = enableEmail;
        this.optionDefault = optionDefault;
    }

    @Override
    public String getReturnEmail() {
        return returnEmail;
    }

    @Override
    public Integer getConnectionPool() {
        return connectionPool;
    }

    @Override
    public Long getStartCount() {
        return startCount;
    }

    @Override
    public Short getInitSize() {
        return initSize;
    }

    @Override
    public Byte getTotalQuantity() {
        return totalQuantity;
    }

    @Override
    public Boolean getEnableEmail() {
        return enableEmail;
    }

    @Override
    public Character getOptionDefault() {
        return optionDefault;
    }
}