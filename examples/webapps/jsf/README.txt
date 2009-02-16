A simple web-app showing how to use dependency injection in JSF managed beans using openejb with tomcat
It contains a Local Stateless session bean (CalculatorImpl) which adds two numbers and returns the result.
The application also contains a JSF managed bean (CalculatorBean), which uses the EJB to add two numbers
and display the results to the user. The EJB is injected in the managed bean using @EJB annotation.

To run this example, perform the following steps:-
1. Install latest Tomcat
2. Deploy OpenEJB WAR in tomcat
3. Open <<tomcat-install>>/conf/tomcat-users.xml and add the following user
   <user username="admin" password="" roles="manager"/>
4. Run the following command while in the jsf directory
   mvn clean install war:exploded tomcat:deploy
5. The above will deploy this web application to tomcat.
6. To test the application, open a web browser and navigate to 
   http://localhost:8080/jsf
7. Enter two numbers and click on the add button. You should be able to see the result. Use the Home link to go back to main page.


Once you make the change, you would need to redeploy the application. Run the following command
  mvn clean install war:exploded tomcat:redeploy
