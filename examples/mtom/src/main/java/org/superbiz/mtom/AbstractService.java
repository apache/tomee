package org.superbiz.mtom;

import javax.activation.DataHandler;

public class AbstractService {

    public Response convertToBytes(final Request request) {
        return new Response(new DataHandler(request.getMessage().getBytes(), "application/octet-stream"));
    }
}
