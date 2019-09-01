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
package org.apache.openejb.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.openejb.monitoring.Managed;
import org.apache.openejb.spi.Service;

/**
 * @version $Rev$ $Date$
 */
@Managed
public interface ServerService extends Service {

    void start() throws ServiceException;

    void stop() throws ServiceException;

    void service(InputStream in, OutputStream out) throws ServiceException, IOException;

    void service(Socket socket) throws ServiceException, IOException;

    String getName();

    String getIP();

    int getPort();

}
