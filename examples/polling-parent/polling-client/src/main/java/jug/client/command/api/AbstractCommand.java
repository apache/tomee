package jug.client.command.api;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jettison.util.StringIndenter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

