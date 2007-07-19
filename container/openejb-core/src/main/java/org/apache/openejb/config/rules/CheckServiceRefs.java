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
package org.apache.openejb.config.rules;

import org.apache.openejb.config.EjbSet;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ServiceRef;

/**
 * @version $Rev$ $Date$
 */
public class CheckServiceRefs extends ValidationBase {
    public void validate(EjbSet set) {
        // Warn that we do not support service-refs yet.

        // Skip if in geronimo
        if (System.getProperty("duct tape") != null) return;

        this.set = set;

        for (EnterpriseBean bean : set.getEjbJar().getEnterpriseBeans()) {
            for (ServiceRef ref : bean.getServiceRef()) {
                warn(bean, "serviceRef.unsupported", ref.getName());
            }
        }
    }
}
