Take the moviefun.war and put it into Tomcat's webapps directory.

When the archive expands, update the openejb.home init-parameter in
the web.xml to point to the directory where you installed OpenEJB.

For example, if you have unpacked this distribution of OpenEJB in the
directory /home/jsmith/openejb-1.0-beta1 then you would set the
openejb.home init parameter as follows:

    <init-param>
      <param-name>openejb.home</param-name>
      <param-value>/home/jsmith/openejb-1.0-beta1</param-value>
    </init-param>

This example has been tested on Tomcat 4.x.  The status of Tomcat
5.5.x is unknown at this time, but will be fully tested before
OpenEJB 1.0 final is released.
