This example shows how to create a session stateless EJB using annotations.

As stated in the "JSR 220: Enterprise JavaBeansTM,Version 3.0 - EJB Core Contracts and Requirements", 

"Stateless session beans are session beans whose instances have no conversational state. This means that
all bean instances are equivalent when they are not involved in servicing a client-invoked method.
The term 'stateless' signiﬁes that an instance has no state for a speciﬁc client."

With EJB 3.0, it's now possible to write stateless session bean without specifying a deployment descriptor; you basically have to write just

  * a remote or local business interface, which is a plain-old-java-interface, annotated with the @Remote or @Local annotation
  * the stateless session bean implementation, a plain-old-java-object which implements the remote or the local business interface and is annotated with the @Stateless annotation
  
To run the example simply type:

 $ mvn clean install


