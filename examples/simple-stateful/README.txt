This example shows how to create a session stateful EJB using annotations.

A stateful session bean is a session bean whose instances can maintain the conversational state with the client.
The conversational state of the stateful session bean, which describes the conversation between a speciﬁc 
client and a session bean, is contained in the fields of the stateful session bean.

With EJB 3.0, it's now possible to write stateful session bean without specifying a deployment descriptor; you basically have to write just

  * a remote or local business interface, which is a plain-old-java-interface, annotated with the @Remote or @Local annotation
  * the stateful session bean implementation, a plain-old-java-object which implements the remote or the local business interface and is annotated with the @Stateful annotation
  
To run the example simply type:

 $ mvn clean install


