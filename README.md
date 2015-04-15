Title: Building Instructions

Apache TomEE is built with Apache Maven.

Simply use
`$> mvn clean install`
on your commandline to kick off the compile process of TomEE


If you intend building in environments where multicast is not allowed
then build with:
 `$> mvn clean install -DskipMulticastTests=true`
 
