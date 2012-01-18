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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resolver.maven;

import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Connection extends URLConnection {
    private Parser m_parser;
    private AetherBasedResolver m_aetherBasedResolver;

    public Connection( final URL url, final MavenConfigurationImpl configuration ) throws MalformedURLException {
        super(url);
        m_parser = new Parser( url.getPath() );
        m_aetherBasedResolver = new AetherBasedResolver( configuration );
    }

    @Override
    public void connect() {
        // no-op
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        return m_aetherBasedResolver.resolve( m_parser.getGroup(), m_parser.getArtifact(), m_parser.getClassifier(), m_parser.getType(), m_parser.getVersion() );
    }
}
