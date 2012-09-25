/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.spi.plugins;

/**
 * In fully Java EE environments, it will be implemented
 * by the application servers.
 * <p>
 * There is no default plugin implementation for any
 * Java EE application server.
 * </p>
 * @version $Rev$ $Date$
 *
 */
public interface OpenWebBeansJavaEEPlugin extends OpenWebBeansPlugin
{        
    //Any future operations for java ee containers
}
