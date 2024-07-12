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
package org.apache.tomee.mojarra;

import com.sun.faces.cdi.CdiExtension;
import org.apache.openejb.cdi.OptimizedLoaderService;
import org.apache.openejb.spi.Service;
import org.apache.tomee.mojarra.owb.OwbCompatibleCdiExtension;

import java.util.Properties;

// This is just a workaround until we have a permanent solution
// Remove this optional service in TomcatLoader again when deleting this service
public class TomEEMojarraService implements Service {
    @Override
    public void init(Properties props) throws Exception {
        // Replace Mojarra's CDI extension because it registers beans OWB can't proxy since mojarra 4.0.1
        // See https://github.com/eclipse-ee4j/mojarra/issues/5457
        OptimizedLoaderService.EXTENSION_REPLACEMENTS.put(CdiExtension.class.getName(), OwbCompatibleCdiExtension.class.getName());
    }
}
