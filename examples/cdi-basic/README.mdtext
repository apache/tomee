To use `@Inject`, the first thing you need is a `META-INF/beans.xml` file in the module
or jar.  This effectively turns on CDI and allows the `@Inject` references to work.
No `META-INF/beans.xml` no injection, period.  This may seem overly strict,
but it is not without reason.  The CDI API is a bit greedy and does consume a fair
about of resources by design.

When the container constructs a bean with an `@Inject` reference,
it will first find or create the object that will be injected.  For the sake of
simplicity, the is example has a basic `Faculty` pojo with a no-arg constructor.  Anyone
referencing `@Inject Faculty` will get their own instance of `Faculty`.  If the desire
is to share the same instance of `Faculty`, see the concept of `scopes` -- this is
exactly what scopes are for.

In this example we have an `@Stateless` bean `Course` with an `@Inject` reference to an
object of type `Faculty`.  When `Course` is created, the container will also create an
instance of `Faculty`.  The `@PostConstruct` will be called on the `Faculty`,
then the `Faculty` instance will be injected into the `Course` bean.  Finally, the
`@PostConstruct` will be invoked on `Course` and then we're done.  All instances will
have been created.

The `CourseTest` test case drives this creation process by having `Course` injected
into it in its `@Setup` method.  By the time our `@Test` method is invoked,
all the real work should be done and we should be ready to go.  In the test case we do
some basic asserts to ensure everything was constructed, all `@PostConstruct` methods
called and everyting injected.
