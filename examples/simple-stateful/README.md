This example shows how to create a Stateful session EJB using annotations.

A Stateful session bean is a session bean whose instances can maintain the conversational state with the client. The conversational state of the stateful session bean, which describes the conversation between a specific client and a session bean, is contained in the fields of the stateful session bean.

Simply put, when you create a stateful bean an actual instance is created by the container and dedicated to you and only you. Every call you make will go to your instance. Further, your instance will not be shared with anyone unless you give them a reference to your stateful bean. The instance will last until you remove it or until it times-out and is removed by the container.

With EJB 3.0, it's now possible to write stateful session bean without specifying a deployment descriptor; you basically have to write just a remote or local business interface, which is a plain-old-java-interface, annotated with the @Remote or @Local annotation the stateful session bean implementation, a plain-old-java-object which implements the remote or the local business interface and is annotated with the @Stateful annotation

This example is the "simple-stateful" example located in the openejb-examples.zip available on the download page.