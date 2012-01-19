* copy jar included in openejb-ssh zip in openejb libs (<tomee>/webapps/openejb/lib for instance)
* for TomEE add a file system.properties in conf containing

    openejb.servicemanager.enabled = true

* start the server
* ssh can be configured in conf/ssh.properties

Note: it uses JAAS for authentication, further details on http://openejb.apache.org/tomee-jaas.html
