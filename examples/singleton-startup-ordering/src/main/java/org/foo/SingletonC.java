/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.foo;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import java.util.logging.Logger;


/**
 * Singlenton C only is annotated with @Singleton and will be lazy initialized.
 */
@Singleton
public class SingletonC {

    @Inject
    Supervisor supervisor;

    private final static Logger LOGGER = Logger.getLogger(SingletonC.class.getName());

    @PostConstruct
    public void init() {
        LOGGER.info("Hi from init in class: " + this.getClass().getName());
        supervisor.addRecord(this.getClass().getSimpleName());

    }

    public String hello() {
        return "Hello from SingletonC.class";
    }
}
