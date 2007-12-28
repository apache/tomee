#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

 OpenEJB 3.0 Configuration Directory Documentation

 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

     +--- Short attention span? --------------------------------
     |
     |  Read OVERVIEW and FIRST THINGS TO CHANGE then have fun.  
     |  Read the rest when you want more details.
     |
     +----------------------------------------------------------

OVERVIEW

This directory contains nothing but this readme file at the time
OpenEJB is unpacked.  The first time OpenEJB is started however, these
files will be created:

  conf/
    openejb.xml                (main config file)

    logging.properties         (log levels and files)

    login.config               (jaas config file)
    users.properties           (users that can log in)
    groups.properties          (groups in which users belong)

    admin.properties           (network socket for administration)
    ejbd.properties            (network socket for ejb invocations)
    hsql.properties            (network socket for hsql client access)
    httpejbd.properties        (network socket for ejb invocations over http)
    telnet.properties          (network socket for telnet "server") 

These files can be edited as desired.  If at any time you are unhappy
with your changes or simply wish to start over, you can delete or move
the file and a new one containing the default values will be
automatically created.


FIRST THINGS TO CHANGE

JDBC DataSources can be configured in the openejb.xml file.  There are
two definitions there by default that are setup to use the HSQL
database.  These can be edited without harm to OpenEJB, it does not
use them and simply provides them as a convenience.

The ip address in the ejbd.properties by default is 127.0.0.1 which
means that by default only clients on the localhost will be able to
connect.  Change it whichever IP address you expect clients to use
when contacting the server or to 0.0.0.0 if you wish to bind it to all
interfaces on the machine.

Users and groups can be added to the users.properties and
groups.properties files.  Note that the users and groups in these
files initially can be deleted without harm to OpenEJB, they are
simply there for example purposes and convenience.


DETAILS ABOUT EACH FILE

 ABOUT conf/openejb.xml
    
    WHAT YOU MUST KNOW (OVERVIEW)
    
    The openejb.xml is the main configuration file for the container
    system and it's services such as transaction, security, and data
    sources.
    
    The format is a mix of xml and properties inspired by the format of
    the httpd configuration file.  Basically:
    
    <tag id="">
        properties
    </tag>
    
    Such as:
    
    <Resource id="MyDataSource" type="DataSource">
      username foo
      password bar
    </Resource>
    
    Note that white space is a valid name/value pair separator in any java
    properties file (along with semi-colon).  So the above is equivalent
    to:
    
    <Resource id="MyDataSource" type="DataSource">
      username = foo
      password = bar
    </Resource>
    
    You may feel free to use any name/value value pair separator you like
    (white space, ":", or "=") with no affect on OpenEJB.
    
    PROPERTY DEFAULTS AND OVERRIDING
    
    This file itself functions as an override, default values are
    specified via other means (service-jar.xml files in the classpath),
    therefore you only need to specify property values here for 2 reasons:
      1. you wish to for documentation purposes 
      2. you need to change the default value
    
    The default openejb.xml file has most of the useful properties for
    each component explicitly listed with default values for documentation
    purposes.  It is safe to delete them and be assured that no behavior
    will change if a smaller config file is desired.
    
    CATALOG OF ALL PROPERTIES
    
    To know what properties can be overriden the './bin/openejb
    properties' command is very useful: see
    http://openejb.apache.org/3.0/properties-tool.html
    
    It's function is to connect to a running server and print a canonical
    list of all properties OpenEJB can see via the various means of
    configuration.  When sending requests for help to the users list or
    jira, it is highly encouraged to send the output of this tool with
    your message.
    
    OTHER MEANS OF OVERRIDING
    
    Overriding can also be done via the command line or via
     ./bin/openejb [command] -D<id>.<property>=<value>
    
    Such as:
     ./bin/openejb start -DMyDataSource.username=foo
    
    The -D properties can actually go before or after the command
    
    See http://openejb.apache.org/3.0/system-properties.html
    
    NOT CONFIGURABLE VIA THIS FILE
    
    The only thing not yet configurable via this file are ServerServices
    due to OpenEJB's embeddable nature and resulting long standing
    tradition of keeping the container system separate from the server
    layer.  This may change someday, but untill then ServerServices are
    configurable via conf/<service-id>.properties files such as
    conf/ejbd.properties to configure the main protocol that services EJB
    client requests.
    
    The format those properties files is greatly adapted from the xinet.d
    style of configuration and even shares similar functionality and
    properties such as host-based authorization (HBA) via the 'only_from'
    property.
    
    RESTORING THIS FILE
    
    To restore this file to its original default state, you can simply
    delete it or rename it and OpenEJB will see it's missing and unpack
    another openejb.xml into the conf/ directory when it starts.  
    
    This is not only handy for recovering from a non-functional config,
    but also for upgrading as OpenEJB will not overwrite your existing
    configuration file should you choose to unpack an new distro over the
    top of an old one -- this style of upgrade is safe provided you move
    your old lib/ directory first.
