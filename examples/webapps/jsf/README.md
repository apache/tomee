Title: JSF

*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/jsf) or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/jsf). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*

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

## Calculator

    package org.superbiz.jsf;
    
    import javax.ejb.Remote;
    
    @Remote
    public interface Calculator {
        public double add(double x, double y);
    }

## CalculatorBean

    package org.superbiz.jsf;
    
    import javax.ejb.EJB;
    
    public class CalculatorBean {
        @EJB
        Calculator calculator;
        private double x;
        private double y;
        private double result;
    
        public double getX() {
            return x;
        }
    
        public void setX(double x) {
            this.x = x;
        }
    
        public double getY() {
            return y;
        }
    
        public void setY(double y) {
            this.y = y;
        }
    
        public double getResult() {
            return result;
        }
    
        public void setResult(double result) {
            this.result = result;
        }
    
        public String add() {
            result = calculator.add(x, y);
            return "success";
        }
    }

## CalculatorImpl

    package org.superbiz.jsf;
    
    import javax.ejb.Stateless;
    
    @Stateless
    public class CalculatorImpl implements Calculator {
    
        public double add(double x, double y) {
            return x + y;
        }
    }

## faces-config.xml

    <faces-config version="1.2"
                  xmlns="http://java.sun.com/xml/ns/javaee"
                  xmlns:xi="http://www.w3.org/2001/XInclude"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-facesconfig_1_2.xsd">
    
    
      <managed-bean>
        <managed-bean-name>calculator</managed-bean-name>
        <managed-bean-class>org.superbiz.jsf.CalculatorBean</managed-bean-class>
        <managed-bean-scope>request</managed-bean-scope>
      </managed-bean>
    
      <navigation-rule>
        <from-view-id>/calculator.jsp</from-view-id>
        <navigation-case>
          <from-outcome>success</from-outcome>
          <to-view-id>/result.jsp</to-view-id>
        </navigation-case>
      </navigation-rule>
    
      <navigation-rule>
        <from-view-id>/result.jsp</from-view-id>
        <navigation-case>
          <from-outcome>back</from-outcome>
          <to-view-id>/calculator.jsp</to-view-id>
        </navigation-case>
      </navigation-rule>
    </faces-config>

## web.xml

    <web-app xmlns="http://java.sun.com/xml/ns/j2ee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"
             version="2.4">
    
      <description>MyProject web.xml</description>
    
      <!--optional: context-param>
          <description>Comma separated list of URIs of (additional) faces config files.
              (e.g. /WEB-INF/my-config.xml)
              See JSF 1.0 PRD2, 10.3.2
              Attention: You do not need to put /WEB-INF/faces-config.xml in here.
          </description>
          <param-name>javax.faces.CONFIG_FILES</param-name>
          <param-value>/WEB-INF/examples-config.xml</param-value>
      </context-param-->
      <context-param>
        <description>State saving method: "client" or "server" (= default)
          See JSF Specification 2.5.3
        </description>
        <param-name>javax.faces.STATE_SAVING_METHOD</param-name>
        <param-value>client</param-value>
      </context-param>
      <context-param>
        <description>Only applicable if state saving method is "server" (= default).
          Defines the amount (default = 20) of the latest views are stored in session.
        </description>
        <param-name>org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION</param-name>
        <param-value>20</param-value>
      </context-param>
      <context-param>
        <description>Only applicable if state saving method is "server" (= default).
          If true (default) the state will be serialized to a byte stream before it
          is written to the session.
          If false the state will not be serialized to a byte stream.
        </description>
        <param-name>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</param-name>
        <param-value>true</param-value>
      </context-param>
      <context-param>
        <description>Only applicable if state saving method is "server" (= default) and if
          org.apache.myfaces.SERIALIZE_STATE_IN_SESSION is true (= default)
          If true (default) the serialized state will be compressed before it
          is written to the session. If false the state will not be compressed.
        </description>
        <param-name>org.apache.myfaces.COMPRESS_STATE_IN_SESSION</param-name>
        <param-value>true</param-value>
      </context-param>
      <context-param>
        <description>This parameter tells MyFaces if javascript code should be allowed in the
          rendered HTML output.
          If javascript is allowed, command_link anchors will have javascript code
          that submits the corresponding form.
          If javascript is not allowed, the state saving info and nested parameters
          will be added as url parameters.
          Default: "true"
        </description>
        <param-name>org.apache.myfaces.ALLOW_JAVASCRIPT</param-name>
        <param-value>true</param-value>
      </context-param>
      <context-param>
        <param-name>org.apache.myfaces.DETECT_JAVASCRIPT</param-name>
        <param-value>false</param-value>
      </context-param>
      <context-param>
        <description>If true, rendered HTML code will be formatted, so that it is "human readable".
          i.e. additional line separators and whitespace will be written, that do not
          influence the HTML code.
          Default: "true"
        </description>
        <param-name>org.apache.myfaces.PRETTY_HTML</param-name>
        <param-value>true</param-value>
      </context-param>
      <context-param>
        <description>If true, a javascript function will be rendered that is able to restore the
          former vertical scroll on every request. Convenient feature if you have pages
          with long lists and you do not want the browser page to always jump to the top
          if you trigger a link or button action that stays on the same page.
          Default: "false"
        </description>
        <param-name>org.apache.myfaces.AUTO_SCROLL</param-name>
        <param-value>true</param-value>
      </context-param>
    
      <context-param>
        <description>Used for encrypting view state. Only relevant for client side
          state saving. See MyFaces wiki/web site documentation for instructions
          on how to configure an application for diffenent encryption strengths.
        </description>
        <param-name>org.apache.myfaces.SECRET</param-name>
        <param-value>NzY1NDMyMTA=</param-value>
      </context-param>
    
      <context-param>
        <description>
          Validate managed beans, navigation rules and ensure that forms are not nested.
        </description>
        <param-name>org.apache.myfaces.VALIDATE</param-name>
        <param-value>true</param-value>
      </context-param>
    
      <context-param>
        <description>
          Treat readonly same as if disabled attribute was set for select elements.
        </description>
        <param-name>org.apache.myfaces.READONLY_AS_DISABLED_FOR_SELECTS</param-name>
        <param-value>true</param-value>
      </context-param>
    
      <context-param>
        <description>
          Use the defined class as the class which will be called when a resource is added to the
          ExtensionFilter handling. Using StreamingAddResource here helps with performance. If you want to add
          custom components and want to use the ExtensionFilter, you need to provide your custom implementation here.
        </description>
        <param-name>org.apache.myfaces.ADD_RESOURCE_CLASS</param-name>
        <param-value>org.apache.myfaces.renderkit.html.util.DefaultAddResource</param-value>
      </context-param>
    
      <context-param>
        <description>
          Virtual path in the URL which triggers loading of resources for the MyFaces extended components
          in the ExtensionFilter.
        </description>
        <param-name>org.apache.myfaces.RESOURCE_VIRTUAL_PATH</param-name>
        <param-value>/faces/myFacesExtensionResource</param-value>
      </context-param>
    
      <context-param>
        <description>
          Check if the extensions-filter has been properly configured.
        </description>
        <param-name>org.apache.myfaces.CHECK_EXTENSIONS_FILTER</param-name>
        <param-value>true</param-value>
      </context-param>
    
      <context-param>
        <description>
          Define partial state saving as true/false.
        </description>
        <param-name>javax.faces.PARTIAL_STATE_SAVING_METHOD</param-name>
        <param-value>false</param-value>
      </context-param>
    
      <!-- Extensions Filter -->
      <filter>
        <filter-name>extensionsFilter</filter-name>
        <filter-class>org.apache.myfaces.webapp.filter.ExtensionsFilter</filter-class>
        <init-param>
          <description>Set the size limit for uploaded files.
            Format: 10 - 10 bytes
            10k - 10 KB
            10m - 10 MB
            1g - 1 GB
          </description>
          <param-name>uploadMaxFileSize</param-name>
          <param-value>100m</param-value>
        </init-param>
        <init-param>
          <description>Set the threshold size - files
            below this limit are stored in memory, files above
            this limit are stored on disk.
    
            Format: 10 - 10 bytes
            10k - 10 KB
            10m - 10 MB
            1g - 1 GB
          </description>
          <param-name>uploadThresholdSize</param-name>
          <param-value>100k</param-value>
        </init-param>
      </filter>
    
      <filter-mapping>
        <filter-name>extensionsFilter</filter-name>
        <url-pattern>*.jsf</url-pattern>
      </filter-mapping>
      <filter-mapping>
        <filter-name>extensionsFilter</filter-name>
        <url-pattern>/faces/*</url-pattern>
      </filter-mapping>
    
      <!-- Listener, to allow Jetty serving MyFaces apps -->
      <listener>
        <listener-class>org.apache.myfaces.webapp.StartupServletContextListener</listener-class>
      </listener>
    
      <!-- Faces Servlet -->
      <servlet>
        <servlet-name>Faces Servlet</servlet-name>
        <servlet-class>javax.faces.webapp.FacesServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
      </servlet>
    
      <!-- Faces Servlet Mapping -->
      <servlet-mapping>
        <servlet-name>Faces Servlet</servlet-name>
        <url-pattern>*.jsf</url-pattern>
      </servlet-mapping>
    
      <!-- Welcome files -->
      <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
        <welcome-file>index.html</welcome-file>
      </welcome-file-list>
    
    </web-app>
    
