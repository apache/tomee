index-group=Misc
type=page
status=published
~~~~~~
# Sample #

This sample implements a simple polling application.

You can create polls and then vote (+1 or -1) for each poll (called Subject).

The front is a JAX-RS front and the backend uses EJBs and JPA.

# Module #

The application contains several modules:

* polling-domain: entities used by the client side too
* polling-core: the middle/dao layer
* polling-web: front layer (REST services)

# What is noticeable #

The front layer contains a MBean managed by CDI (VoteCounter) which is used by REST services to update information you
can retrieve through JMX protocol (JConsole client is fine to see it ;)).

It manages a dynamic datasource too. It manages in the example configuration 2 clients.

It is a simple round robin by request. That's why from the client if you simply create a poll then find it
you'll not find the persisted poll, you need to do it once again.

# Client #

It lets you create poll, retrieve them, find the best poll and vote for any poll.

Please type help for more information.
