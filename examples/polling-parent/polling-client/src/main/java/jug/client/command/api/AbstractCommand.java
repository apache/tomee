/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jug.client.command.api;

import jug.client.util.ClientNameHolder;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jettison.util.StringIndenter;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public abstract class AbstractCommand {

    protected String command;
    protected String url;

    protected WebClient client;

    public void execute(final String cmd) {
        final Response response = invoke(cmd);
        if (response == null) {
            return;
        }

        System.out.println("Status: " + response.getStatus());
        try {
            String json = slurp((InputStream) response.getEntity());
            System.out.println(format(json));
        } catch (IOException e) {
            System.err.println("can't get output: " + e.getMessage());
        }
    }

    protected String format(final String json) throws IOException {
        final StringIndenter formatter = new StringIndenter(json);
        final Writer outWriter = new StringWriter();
        IOUtils.copy(new StringReader(formatter.result()), outWriter, 2048);
        outWriter.close();
        return outWriter.toString();
    }

    protected abstract Response invoke(final String cmd);

    public void setCommand(String command) {
        this.command = command;
    }

    public void setUrl(String url) {
        this.url = url;
        client = WebClient.create(url).accept(MediaType.APPLICATION_JSON);
        if (ClientNameHolder.getCurrent() != null) {
            client.query("client", ClientNameHolder.getCurrent());
        }
    }

    public static String slurp(final InputStream from) throws IOException {
        ByteArrayOutputStream to = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = from.read(buffer)) != -1) {
            to.write(buffer, 0, length);
        }
        to.flush();
        return new String(to.toByteArray());
    }
}

