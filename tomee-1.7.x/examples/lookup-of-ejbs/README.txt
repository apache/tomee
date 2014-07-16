This example shows how to use the @EJB annotation on a bean class to refer to other beans.

This functionality is often referred as dependency injection (see
http://www.martinfowler.com/articles/injection.html), and has been recently introduced in
Java EE 5.

In this particular example, we will create two session stateless beans

  * a DataStore session bean
  * a DataReader session bean
  
The DataReader bean uses the DataStore to retrieve some informations, and
we will see how we can, inside the DataReader bean, get a reference to the
DataStore bean using the @EJB annotation, thus avoiding the use of the
JNDI API.

