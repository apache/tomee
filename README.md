Title: Buildling Instructions

# Basic Usage

Apache TomEE is built with Apache Maven.

Simply use

`$> mvn clean install`

on your commandline to kick off the compile process of TomEE


If you intend building in environments where multicast is not allowed
then build with:

`$> mvn clean install -DskipMulticastTests=true`

Full build can be executed with (will execute arquillian test on all TomEE distributions)

`$> mvn clean install -Pall-adapters`

 
# Quick Build
 
If you only like to compile all classes and package up TomEE *without* running tests
then you can use the following build options

`mvn -Pquick -Dsurefire.useFile=false -DdisableXmlReport=true -DuniqueVersion=false -ff -Dassemble -DskipTests -DfailIfNoTests=false clean install`

# Direct builds

To build TomEE just execute:

`$> mvn clean install -pl tomee/apache-tomee -am -Dmaven.test.skip=true`

TomEE zip/tar.gz will be in tomee/apache-tomee/target

To build TomEE Embedded to be able to develop with its maven plugin execute:

`$> mvn clean install -pl maven/tomee-embedded-maven-plugin -am -Dmaven.test.skip=true`

 
