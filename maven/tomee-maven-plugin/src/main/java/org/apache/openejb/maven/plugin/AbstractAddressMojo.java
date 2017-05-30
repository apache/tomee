/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.openejb.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractAddressMojo extends AbstractMojo {
    @Parameter(property = "tomee-plugin.http")
    protected String tomeeHttpPort;

    @Parameter(property = "tomee-plugin.https")
    protected String tomeeHttpsPort;

    @Parameter(property = "tomee-plugin.host")
    protected String tomeeHost;

    @Parameter(property = "tomee-plugin.user")
    protected String user;

    @Parameter(property = "tomee-plugin.pwd")
    protected String password;

    @Parameter(property = "tomee-plugin.realm")
    protected String realm;
}
