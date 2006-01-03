package org.openejb.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class RemoteTestServer implements org.openejb.test.TestServer {

    static {
        System.setProperty("noBanner", "true");
    }

    /**
     * Has the remote server's instance been already running ?
     */
    private boolean serverHasAlreadyBeenStarted = true;

    private Properties properties;

    public void init(Properties props) {
        properties = props;

        props.put("test.server.class", "org.openejb.test.RemoteTestServer");
        props.put("java.naming.factory.initial", "org.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "127.0.0.1:4201");
        props.put("java.naming.security.principal", "testuser");
        props.put("java.naming.security.credentials", "testpassword");


    }

    public void destroy() {
    }

    public void start() {
        if (!connect()) {
            try {
                System.out.println("[] START SERVER");
                serverHasAlreadyBeenStarted = false;
                String version = null;

                URL resource = this.getClass().getResource("openejb-version.properties");

                Properties versionInfo = new Properties();
                versionInfo.load(resource.openConnection().getInputStream());
                version = (String) versionInfo.get("version");

                Process server = Runtime.getRuntime().exec("java -jar lib" + File.separator + "openejb-core-" + version + ".jar start -nowait");

                // Pipe the processes STDOUT to ours
                InputStream out = server.getInputStream();
                Thread serverOut = new Thread(new Pipe(out, System.out));

                serverOut.setDaemon(true);
                serverOut.start();

                // Pipe the processes STDERR to ours
                InputStream err = server.getErrorStream();
                Thread serverErr = new Thread(new Pipe(err, System.err));

                serverErr.setDaemon(true);
                serverErr.start();
            } catch (Exception e) {
                throw new RuntimeException("Cannot start the server.");
            }
            connect(10);
        } else {
            //System.out.println("[] SERVER STARTED");
        }
    }

    private void oldStart() throws IOException, FileNotFoundException {
        String s = java.io.File.separator;
        String java = System.getProperty("java.home") + s + "bin" + s + "java";
        String classpath = System.getProperty("java.class.path");
        String openejbHome = System.getProperty("openejb.home");


        String[] cmd = new String[ 5 ];
        cmd[0] = java;
        cmd[1] = "-classpath";
        cmd[2] = classpath;
        cmd[3] = "-Dopenejb.home=" + openejbHome;
        cmd[4] = "org.openejb.server.Main";
        for (int i = 0; i < cmd.length; i++) {
            //System.out.println("[] "+cmd[i]);
        }

        Process remoteServerProcess = Runtime.getRuntime().exec(cmd);

        // it seems as if OpenEJB wouldn't start up till the output stream was read
        final java.io.InputStream is = remoteServerProcess.getInputStream();
        final java.io.OutputStream out = new FileOutputStream("logs/testsuite.out");
        Thread serverOut = new Thread(new Runnable() {
            public void run() {
                try {
                    //while ( is.read() != -1 );
                    int i = is.read();
                    out.write(i);
                    while (i != -1) {
                        //System.out.write( i );
                        i = is.read();
                        out.write(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        serverOut.setDaemon(true);
        serverOut.start();

        final java.io.InputStream is2 = remoteServerProcess.getErrorStream();
        Thread serverErr = new Thread(new Runnable() {
            public void run() {
                try {
                    //while ( is.read() != -1 );
                    int i = is2.read();
                    out.write(i);
                    while (i != -1) {
                        //System.out.write( i );
                        i = is2.read();
                        out.write(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        serverErr.setDaemon(true);
        serverErr.start();
    }

    public void stop() {
        if (!serverHasAlreadyBeenStarted) {
            try {
                System.out.println("[] STOP SERVER");

                Socket socket = new Socket("localhost", 4200);
                OutputStream out = socket.getOutputStream();

                out.write("Stop".getBytes());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Properties getContextEnvironment() {
        return (Properties) properties.clone();
    }

    private boolean connect() {
        return connect(1);
    }

    private boolean connect(int tries) {
        //System.out.println("CONNECT "+ tries);
        try {
            Socket socket = new Socket("localhost", 4200);
            OutputStream out = socket.getOutputStream();
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            if (tries < 2) {
                return false;
            } else {
                try {
                    Thread.sleep(2000);
                } catch (Exception e2) {
                    e.printStackTrace();
                }
                return connect(--tries);
            }
        }

        return true;
    }

    private static final class Pipe implements Runnable {


        private final InputStream is;

        private final OutputStream out;

        private Pipe(InputStream is, OutputStream out) {

            super();

            this.is = is;

            this.out = out;

        }

        public void run() {

            try {

                int i = is.read();

                out.write(i);

                while (i != -1) {

                    i = is.read();

                    out.write(i);

                }

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

    }
}
