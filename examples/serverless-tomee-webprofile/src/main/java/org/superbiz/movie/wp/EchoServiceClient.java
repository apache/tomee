package org.superbiz.movie.wp;

import java.util.Random;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class EchoServiceClient {

    private static final Logger LOG = Logger.getLogger(EchoServiceClient.class.getName());

    private static final Random R = new Random();

    private WebTarget target;

    @PostConstruct
    public void init() {
        target = ClientBuilder.newClient().target("http://localhost:8080/echo");
    }

    public void getEcho(Movie m) {
        try (Response r = target.path("" + R.nextInt()).request().post(
            Entity.entity(m, MediaType.APPLICATION_JSON_TYPE))) {
            LOG.info("status=" + r.getStatus());
        }
    }
}
