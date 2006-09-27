package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

public class Client {

    public static Response request(Request req, Response res, ServerMetaData server) throws RemoteException {
        if (server == null) throw new IllegalArgumentException("Server instance cannot be null");

        OutputStream out = null;
        ObjectOutput objectOut = null;
        ObjectInput objectIn = null;
        Connection conn = null;

        try {
            /*----------------------------*/
            /* Get a connection to server */
            /*----------------------------*/
            try {
                conn = ConnectionManager.getConnection(server);
            } catch (IOException e) {
                throw new RemoteException("Cannot access server: " + server.getHost() + ":" + server.getPort() + " Exception: ", e);
            } catch (Throwable e) {
                throw new RemoteException("Cannot access server: " + server.getHost() + ":" + server.getPort() + " due to an unkown exception in the OpenEJB client: ", e);
            }

            /*----------------------------------*/
            /* Get output streams               */
            /*----------------------------------*/
            try {

                out = conn.getOuputStream();

            } catch (IOException e) {
                throw new RemoteException("Cannot open output stream to server: ", e);

            } catch (Throwable e) {
                throw new RemoteException("Cannot open output stream to server: ", e);
            }

            /*----------------------------------*/
            /* Write request type               */
            /*----------------------------------*/
            try {

                out.write(req.getRequestType());

            } catch (IOException e) {
                throw new RemoteException("Cannot write the request type to the server: ", e);

            } catch (Throwable e) {
                throw new RemoteException("Cannot write the request type to the server: ", e);
            }

            /*----------------------------------*/
            /* Get output streams               */
            /*----------------------------------*/
            try {

                objectOut = new ObjectOutputStream(out);

            } catch (IOException e) {
                throw new RemoteException("Cannot open object output stream to server: ", e);

            } catch (Throwable e) {
                throw new RemoteException("Cannot open object output stream to server: ", e);
            }

            /*----------------------------------*/
            /* Write request                    */
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
            /* Get input streams               */
            /*----------------------------------*/
            try {

                objectIn = new ObjectInputStream(conn.getInputStream());
            } catch (IOException e) {
                throw new RemoteException("Cannot open object input stream to server: ", e);

            } catch (Throwable e) {
                throw new RemoteException("Cannot open object input stream to server: ", e);
            }

            /*----------------------------------*/
            /* Read response                    */
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

