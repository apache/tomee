package org.apache.openejb.server.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Properties;

import org.apache.openejb.server.ServiceException;

public class TelnetServer implements org.apache.openejb.server.ServerService {


    public void init(Properties props) throws Exception {
    }

    public void service(Socket socket) throws ServiceException, IOException {
        InputStream telnetIn = null;
        PrintStream telnetOut = null;

        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            telnetIn = new TelnetInputStream(in, out);
            telnetOut = new TelnetPrintStream(out);

            telnetOut.println("OpenEJB Remote Server Console");
            telnetOut.println("type \'help\' for a list of commands");


            TextConsole shell = new TextConsole();
            shell.exec(telnetIn, telnetOut);

        } catch (Throwable t) {

        } finally {
            if (telnetIn != null)
                telnetIn.close();
            if (telnetOut != null)
                telnetOut.close();
            if (socket != null) socket.close();

        }
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(InputStream in, OutputStream out)");
    }
    
    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    public String getName() {
        return "telnet";
    }

    public int getPort() {
        return 0;
    }

    public String getIP() {
        return "";
    }

}
