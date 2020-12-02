package org.superbiz.rest.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.jms.Message;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

@Path("jms")
@ApplicationScoped
public class JmsResource {

    private SseBroadcaster broadcaster;
    private OutboundSseEvent.Builder builder;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void stats(final @Context SseEventSink sink) {
        broadcaster.register(sink);
    }

    @Context
    public void setSse(final Sse sse) {
        this.broadcaster = sse.newBroadcaster();
        this.builder = sse.newEventBuilder();
    }

    public void onMessage(final @Observes Message message) {
        if (broadcaster == null) {
            return;
        }

        broadcaster.broadcast(builder.data(Message.class, message).mediaType(MediaType.APPLICATION_JSON_TYPE).build());
    }
}
