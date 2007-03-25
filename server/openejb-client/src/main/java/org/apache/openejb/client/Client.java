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
package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private static final Logger logger = Logger.getLogger("OpenEJB.client");

    private static Client client = new Client();

    // This lame hook point if only of testing
    public static void setClient(Client client) {
        Client.client = client;
    }

    public static Response request(Request req, Response res, ServerMetaData server) throws RemoteException {
        return client.processRequest(req, res, server);
    }

    protected Response processRequest(Request req, Response res, ServerMetaData server) throws RemoteException {
        if (server == null)
            throw new IllegalArgumentException("Server instance cannot be null");

        OutputStream out = null;
        ObjectOutput objectOut = null;
        ObjectInput objectIn = null;
        Connection conn = null;

        try {
            /*----------------------------*/
            /* Get a connection to server */
            /*----------------------------*/
            URI[] uris = server.getLocations();
            for (int i = 0; i < uris.length; i++) {
                URI uri = uris[i];
                try {
                    conn = ConnectionManager.getConnection(uri);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Cannot access server(s): " + uri.getHost() + ":" + uri.getPort() + " Exception: ", e);
                } catch (Throwable e) {
                    throw new RemoteException("Cannot access server: " + uri.getHost() + ":" + uri.getPort() + " due to an unkown exception in the OpenEJB client: ", e);
                }
            }
            
            // If no servers responded, throw an error
            if (conn == null) {
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < uris.length; i++) {
                    URI uri = uris[i];
                    buffer.append((i != 0 ? ", " : "") + "Server #" + i + ": " + uri);
                }
                throw new RemoteException("Cannot access servers: " + buffer.toString());
            }

            /*----------------------------------*/
            /* Get output streams */
            /*----------------------------------*/
            try {

                out = conn.getOuputStream();

            } catch (IOException e) {
                throw new RemoteException("Cannot open output stream to server: ", e);

            } catch (Throwable e) {
                throw new RemoteException("Cannot open output stream to server: ", e);
            }

            /*----------------------------------*/
            /* Write request type */
            /*----------------------------------*/
            try {

                out.write(req.getRequestType());

            } catch (IOException e) {
                throw new RemoteException("Cannot write the request type to the server: ", e);

            } catch (Throwable e) {
                throw new RemoteException("Cannot write the request type to the server: ", e);
            }

            /*----------------------------------*/
            /* Get output streams */
            /*----------------------------------*/
            try {

                objectOut = new ObjectOutputStream(out);

            } catch (IOException e) {
                throw new RemoteException("Cannot open object output stream to server: ", e);

            } catch (Throwable e) {
                throw new RemoteException("Cannot open object output stream to server: ", e);
            }

            /*----------------------------------*/
            /* Write request */
            /*----------------------------------*/
            try {

                req.writeExternal(objectOut);
                objectOut.flush();

            } catch (java.io.NotSerializableException e) {

                throw new IllegalArgumentException("Object is not serializable: " + e.getMessage());

            } catch (IOException e) {
                throw new RemoteException("Cannot write the request to the server: ", e);

            } catch (Throwable e) {
                throw new RemoteException("Cannot write the request to the server: ", e);
            }

            /*----------------------------------*/
            /* Get input streams */
            /*----------------------------------*/
            try {

                objectIn = new EjbObjectInputStream(conn.getInputStream());
            } catch (IOException e) {
                throw new RemoteException("Cannot open object input stream to server: ", e);

            } catch (Throwable e) {
                throw new RemoteException("Cannot open object input stream to server: ", e);
            }

            /*----------------------------------*/
            /* Read response */
            /*----------------------------------*/
            try {

                res.readExternal(objectIn);
            } catch (ClassNotFoundException e) {
                throw new RemoteException("Cannot read the response from the server.  The class for an object being returned is not located in this system:", e);

            } catch (IOException e) {
                throw new RemoteException("Cannot read the response from the server.", e);

            } catch (Throwable e) {
                throw new RemoteException("Error reading response from server: ", e);
            }

        } catch (Throwable error) {
            throw new RemoteException("Error while communicating with server: ", error);

        } finally {
            try {
                conn.close();
            } catch (Throwable t) {

                System.out.println("Error closing connection with server: " + t.getMessage());
            }
        }
        return res;
    }
}
