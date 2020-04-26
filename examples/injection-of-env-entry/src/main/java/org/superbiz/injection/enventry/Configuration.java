/**
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
package org.superbiz.injection.enventry;

import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import java.util.Date;

/**
 * This example demostrates the use of the injection of environment entries
 * using <b>Resource</b> annotation.
 *
 * "EJB Core Contracts and Requirements" specification section 16.4.1.1.
 *
 * @version $Rev$ $Date$
 */
//START SNIPPET: code
@Singleton
public class Configuration {

    @Resource
    private String color;

    @Resource
    private Shape shape;

    @Resource
    private Class strategy;

    @Resource(name = "date")
    private long date;

    public String getColor() {
        return color;
    }

    public Shape getShape() {
        return shape;
    }

    public Class getStrategy() {
        return strategy;
    }

    public Date getDate() {
        return new Date(date);
    }
}
//END SNIPPET: code
