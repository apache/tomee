index-group=Unrevised
type=page
status=published
~~~~~~
# CDI events: loose coupling and extensibility

CDI allows you to extend business code by the Notifier/Observer pattern.

To do it you simply inject a field `Event` in the notifier class. This class is a template
and the parameter type is the object type to fire. Then when you want to notify observers
you simply call the method fire of the event object passing as a parameter the event itself
(your own class!).

On the other side you annotated a method parameter `@Observes` and the parameter type is the sent type
by the notifier.

Note: of course you can add qualifiers to be more precise on your events.

# The example

The example is pretty simple: an ejb uses the `@Schedule` annotation to get a notification each second.
The each second this EJB will send the current date through CDI events.

This is our "business" code. It is a simple behavior (nothing).

In our test (which is considered as an extension) we created an observer (`Observer` class)
which simply store and log each received date.

The test itself (`EventTest`) simply verifies the dates were received.

# Conclusion

CDI let's you implement very easily plugins through this event mecanism.

If you go further and look at CDI plugin API you'll realize it is simply the same kind
of events. CDI events is really the basis of CDI.
