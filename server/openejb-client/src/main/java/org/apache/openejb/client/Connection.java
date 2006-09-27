package org.apache.openejb.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Connection {

    public void close() throws java.io.IOException;

    public InputStream getInputStream() throws IOException;

    public OutputStream getOuputStream() throws IOException;

}
