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
package org.apache.openejb.client;

import java.net.URI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This factory supports the following URI format
 *
 *  failover:[strategy:]urlList
 *
 * Where strategy and urlList are variables
 *
 * strategy = the ConnectionStrategy name, such as "sticky", "round-robin",
 * or "random".  This parameter is optional.
 *
 * urlList = a comma separated list connection URIs.  There must be a
 * ConnectionFactory installed for the associated URI.
 *
 * Some examples might be:
 *
 *  - failover:ejbd://foo:4201,ejbd://bar:4201
 *  - failover:random:ejbd://foo:4201,ejbd://bar:4201
 *  - failover:round-robin:ejbd://foo:4201,ejbds://bar:4201,multicast://239.255.2.3:6142
 *
 * The final URI being the most clever in that it will sequentially go
 * through the list, first attempting a couple hard-coded addresses before
 * finally resorting to multicast in an attempt to discover a server.
 *
 * @version $Rev$ $Date$
 */
public class FailoverConnectionFactory implements ConnectionFactory {

    public Connection getConnection(URI failoverUri) throws IOException {

        // URI can be in the following formats:
        //
        // failover:ejbd://foo:4201,ejbd://bar:4202
        // failover:sticky:ejbd://foo:4201,ejbd://bar:4202

        // trim off the "failover:"
        String remainder = failoverUri.getSchemeSpecificPart();

        URI uri = URI.create(remainder);

        String strategy = uri.getScheme();
        String servers = uri.getSchemeSpecificPart();

        if (servers.startsWith("//")) {
            strategy = "default";
            servers = remainder;
        }

        List<URI> list = new ArrayList<URI>();
        
        for (String server : servers.split(",")) {
            list.add(URI.create(server));
        }

        URI[] uris = list.toArray(new URI[0]);

        ClusterMetaData data = new ClusterMetaData(0, uris);
        data.setConnectionStrategy(strategy);

        return ConnectionManager.getConnection(data, new ServerMetaData(), null);
    }


    public static void main(String[] args) throws IOException {
        FailoverConnectionFactory factory = new FailoverConnectionFactory();

        URI uri = URI.create("failover:ejbd://foo:4201,ejbd://bar:4202");
        Connection connection = factory.getConnection(uri);
    }
}
