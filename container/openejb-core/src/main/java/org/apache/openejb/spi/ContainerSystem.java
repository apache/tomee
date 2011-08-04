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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.spi;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.core.WebContext;

import javax.naming.Context;
import java.util.List;

public interface ContainerSystem {

    public BeanContext getBeanContext(Object id);

    public BeanContext[] deployments();

    public Container getContainer(Object id);

    public Container [] containers();

    WebContext getWebContext(String id);

    Context getJNDIContext();

    List<AppContext> getAppContexts();
    
    AppContext getAppContext(Object id);
}