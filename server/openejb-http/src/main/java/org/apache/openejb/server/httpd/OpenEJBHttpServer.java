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
package org.apache.openejb.server.httpd;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.context.RequestInfos;
import org.apache.openejb.server.stream.CountingInputStream;
import org.apache.openejb.server.stream.CountingOutputStream;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OptionsLog;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This is the main class for the web administration.  It takes care of the
 * processing from the browser, sockets and threading.
 *
 * @since 11/25/2001
 */
public class OpenEJBHttpServer implements HttpServer {

    private static final Logger log = Logger.getInstance(LogCategory.HTTPSERVER, "org.apache.openejb.util.resources");

    private HttpListener listener;
    private Set<Output> print;
    private boolean indent;

    public OpenEJBHttpServer() {
        this(getHttpListenerRegistry());
    }

    public static HttpListenerRegistry getHttpListenerRegistry() {
        final SystemInstance systemInstance = SystemInstance.get();
        HttpListenerRegistry registry = systemInstance.getComponent(HttpListenerRegistry.class);
        if (registry == null) {
            registry = new HttpListenerRegistry();
            systemInstance.setComponent(HttpListenerRegistry.class, registry);
        }
        return registry;
    }

    public OpenEJBHttpServer(final HttpListener listener) {
        this.listener = listener;
    }

    public static boolean isTextXml(final Map<String, String> headers) {
        final String contentType = headers.get("Content-Type");
        return contentType != null && contentType.contains("text/xml");
    }

    @Override
    public HttpListener getListener() {
        return listener;
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        /**
         * The InputStream used to receive incoming messages from the client.
         */
        InputStream in = null;
        /**
         * The OutputStream used to send outgoing response messages to the client.
         */
        OutputStream out = null;

        try {
            RequestInfos.initRequestInfo(socket);

            in = new CountingInputStream(socket.getInputStream());
            out = new CountingOutputStream(socket.getOutputStream());

            //TODO: if ssl change to https
            final URI socketURI = new URI("http://" + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
            processRequest(socketURI, in, out);

        } catch (Throwable e) {
            log.error("Unexpected error", e);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                } catch (Throwable e) {
                    //Ignore
                }
                try {
                    out.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }

            try {
                socket.close();
            } catch (Throwable e) {
                log.error("Encountered problem while closing connection with client: " + e.getMessage());
            }
        }
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(InputStream in, OutputStream out)");
    }

    @Override
    public void init(final Properties props) throws Exception {
        final Options options = new Options(props);
        options.setLogger(new OptionsLog(log));
        print = options.getAll("print", OpenEJBHttpServer.Output.class);
        indent = print.size() > 0 && options.get("indent.xml", false);

    }

    public static enum Output {
        REQUEST,
        RESPONSE
    }

    @Override
    public void start() throws ServiceException {
    }

    @Override
    public void stop() throws ServiceException {
    }

    @Override
    public String getName() {
        return "httpd";
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public String getIP() {
        return "";
    }

    /**
     * takes care of processing requests and creating the webadmin ejb's
     *
     * @param in  the input stream from the browser
     * @param out the output stream to the browser
     */
    private void processRequest(final URI socketURI, final InputStream in, final OutputStream out) {
        HttpResponseImpl response = null;
        try {
            response = process(socketURI, in);

        } catch (Throwable t) {
            response = HttpResponseImpl.createError(t.getMessage(), t);
        } finally {
            try {
                if (response != null) {
                    response.writeMessage(out, false);

                    if (print.size() > 0 && print.contains(Output.RESPONSE)) {
                        response.writeMessage(new LoggerOutputStream(log, "debug"), indent);
                    }
                }
            } catch (Throwable t2) {
                log.error("Could not write response", t2);
            }
        }
    }

    private HttpResponseImpl process(final URI socketURI, final InputStream in) throws OpenEJBException {
        final HttpRequestImpl req = new HttpRequestImpl(socketURI);
        final HttpResponseImpl res = new HttpResponseImpl();

        try {
            req.readMessage(in);

            if (print.size() > 0 && print.contains(Output.REQUEST)) {
                req.print(log, indent);
            }

            res.setRequest(req);
        } catch (Throwable t) {
            res.setCode(400);
            res.setResponseString("Could not read the request");
            try {
                res.getWriter().println(t.getMessage());
                t.printStackTrace(res.getWriter());
            } catch (IOException e) {
                // no-op
            }
            log.error("BAD REQUEST", t);
            throw new OpenEJBException("Could not read the request.\n" + t.getClass().getName() + ":\n" + t.getMessage(), t);
        }

        final URI uri;
        String location = null;
        try {
            uri = req.getURI();
            location = uri.getPath();
            final int querry = location.indexOf("?");
            if (querry != -1) {
                location = location.substring(0, querry);
            }
        } catch (Throwable t) {
            throw new OpenEJBException("Could not determine the module " + location + "\n" + t.getClass().getName() + ":\n" + t.getMessage());
        }

        try {
            listener.onMessage(req, res);
        } catch (Throwable t) {
            throw new OpenEJBException("Error occurred while executing the module " + location + "\n" + t.getClass().getName() + ":\n" + t.getMessage(), t);
        }

        return res;
    }

    public static String reformat(final String raw) {
        if (raw.length() == 0)
            return raw;

        try {
            final TransformerFactory factory = TransformerFactory.newInstance();
            // bugged in some XML implementation
            // should we use another implementation?
            //factory.setAttribute("indent-number", 2);

            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            final StreamResult result = new StreamResult(new StringWriter());

            transformer.transform(new StreamSource(new StringReader(raw)), result);

            return result.getWriter().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
            return raw;
        }
    }

    private static class LoggerOutputStream extends OutputStream {

        private final Logger logger;
        private final String level;

        public LoggerOutputStream(final Logger log, final String lvl) {
            logger = log;
            level = lvl;
        }

        @Override
        public void write(final int b) throws IOException {
            logger.log(level, Character.toString((char) b));
        }

        @Override // shortcut for String - because we know what we have ;)
        public void write(final byte[] b) throws IOException {
            logger.log(level, new String(b));
        }
    }
}
