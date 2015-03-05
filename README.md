Title: Buildling Instructions

# Basic Usage

Apache TomEE is built with Apache Maven.

Simply use

`$> mvn clean install`

on your commandline to kick off the compile process of TomEE


If you intend building in environments where multicast is not allowed
then build with:

`$> mvn clean install -DskipMulticastTests=true`

 
# Quick Build
 
If you only like to compile all classes and package up TomEE *without* running tests
then you can use the following build options

`mvn -Pquick -Dsurefire.useFile=false -DdisableXmlReport=true -DuniqueVersion=false -ff -Dassemble -DskipTests -DfailIfNoTests=false clean install`


 
