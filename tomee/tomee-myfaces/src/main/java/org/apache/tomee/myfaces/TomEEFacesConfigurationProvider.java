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
package org.apache.tomee.myfaces;

import org.apache.myfaces.config.DefaultFacesConfigurationProvider;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.openejb.loader.IO;

import javax.faces.context.ExternalContext;
import java.io.InputStream;

public class TomEEFacesConfigurationProvider extends DefaultFacesConfigurationProvider {
    @Override
    public FacesConfig getWebAppFacesConfig(final ExternalContext ectx) {
        final InputStream stream = ectx.getResourceAsStream("/WEB-INF/faces-config.xml");
        if (stream != null && isEmpty(stream)) {
            return new org.apache.myfaces.config.impl.digester.elements.FacesConfig();
        }
        // we can't just check the emptyness after the exception
        // because otherwise an exception is logged because of the parser error handler
        return super.getWebAppFacesConfig(ectx);
    }

    private static boolean isEmpty(final InputStream stream) {
        try {
            final String content = IO.slurp(stream);
            return content.trim().length() == 0;
        } catch (final Exception e) {
            return false;
        }
    }
}
