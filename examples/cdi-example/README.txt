** Below is taken directly from an email, please prettify it if you have time, specially replace the classes A and B with real classes**

you'll need 2 classes A and B, where b is a member of A, and A I made a
stateless.
The member b is annotated with @Injected.
B has a callback method annotated with @PostConstruct, does some
initialization.

I think the only tricky part is the sauce that binds it all,
you'll also need a resources/META-INF/beans.xml to activate the CDI,
otherwise the injection won't happen, and during runtime,
you'll get an error about not being able to locate some of the resources.

Then a simple test class. I had an @EJB reference to A that the container
injects, and I just retrieved A.b.getX() to make sure that X was initialized
properly in the callback method.
