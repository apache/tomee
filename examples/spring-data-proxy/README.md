index-group=Frameworks
type=page
status=published
~~~~~~
# Spring Data sample #

This example uses OpenEJB hooks to replace an EJB implementation by a proxy
to uses Spring Data in your preferred container.

It is pretty simple: simply provide to OpenEJB an InvocationHandler using delegating to spring data
and that's it!

It is what is done in org.superbiz.dynamic.SpringDataProxy.

It contains a little trick: even if it is not annotated "implementingInterfaceClass" attribute
is injected by OpenEJB to get the interface.

Then we simply create the Spring Data repository and delegate to it.
