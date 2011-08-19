This example shows how to use the concept of CDI in a simple POJO context

This functionality is often referred as dependency injection (see
http://www.martinfowler.com/articles/injection.html), and has been recently introduced in
Java EE 5.

This example shows how the @Produces and @Disposes annotations work. A LogFactory creates an instance of the LogHandler
depending on a "type" attribute. For the purposes of this example, the type is hard-coded to a specific value.
A Logger implementation shall contain a list of LogHandlers. We shall have three implementations of the LogHandler interface.

  * A DatabaseHandler
  * A FileHandler
  * A ConsoleHandler

The DatabaseHandler would seemingly write the logs to a database. The FileHandler would write the same logs to a file.
The ConsoleHandler would just print the logs to a console (Standard out). This example is just an illustration of how
the concepts within CDI work and is not intended to provide a logging framework design/implementation.

