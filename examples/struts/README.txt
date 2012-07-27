A simple web-app showing how to use maven, struts2, site-mesh, mysql/hsqldb,openejb with tomcat
This application allows you to insert, find and findAll users. It uses one @Entity named User
and uses a @Local @Stateless session bean to insert, find and find all User's.

To run this example, perform the following steps:-
1. Install latest Tomcat
2. Deploy OpenEJB WAR in tomcat
3. Open <<tomcat-install>>/conf/tomcat-users.xml and add the following user
   <user username="admin" password="" roles="manager"/>
4. Run the following command while in the struts directory
   mvn clean install war:exploded tomcat:deploy
5. The above will deploy this web application to tomcat.
6. To test the application, open a web browser and navigate to 
   http://localhost:8080/struts
7. Use the links on the homepage to add, find and list users

By default this example uses hsqldb database whose data should be stored under
 <<tomcat-install>>/data

If you want to use mysql, then open persistence.xml (its under src/main/resources/META-INF) and
comment out the <persistence-unit> section for hsqldb and uncomment the one for mysql. Read the 
comments in persistence.xml for further instructions 

Once you make the change, you would need to redeploy the application. Run the following command
  mvn clean install war:exploded tomcat:redeploy