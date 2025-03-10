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
package org.apache.tomee.tck.jaxrs;

import org.apache.openejb.loader.SystemInstance;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.shrinkwrap.api.container.ClassContainer;

// inject NotFoundServlet into the deployment to make our global web.xml override work
public class JaxRsTckExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(getClass());
    }

    public void observeDeployment(@Observes BeforeDeploy bd) {
        if (bd.getDeployment().getArchive() instanceof ClassContainer<?> classContainer) {
            classContainer.addClass(NotFoundServlet.class);
            // hack to ensure, that default providers are registered for the TCK (JAX-RS 3.1 mandates them ...)
            SystemInstance.get().setProperty("openejb.jaxrs.skip.jakarta.json.providers.registration", "false");
        }
    }
}
