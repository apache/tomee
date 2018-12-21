<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

<!--- badget section missing:
  Jiras opened ? I don't know if this information can be pulled from JIRA
  Bug issues ? I don't know if this information can be pulled from JIRA
  Gitter ? We currently have IRC, do we want to add Gitter to the channels?
-->


 

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub forks](https://img.shields.io/github/forks/apache/tomee.svg?style=social&label=Fork)](https://github.com/apache/tomee/tomee/fork) 
[![GitHub stars](https://img.shields.io/github/stars/apache/tomee.svg?style=social&label=Star)](https://github.com/apache/tomee) 

# Apache TomEE


<img src="http://tomee.apache.org/img/logo-active.png">  Apache TomEE 
is a lightweight, yet powerful, JavaEE Application server with feature rich tooling.

Is the Java Enterprise Edition of Apache Tomcat (Tomcat + Java EE = TomEE) and currently is a JakartaEE (formerly JavaEE) and Microprofile compliant application server.

The application server is totally open source and created with the community efforts for the public good. 
Collaborative consensus and and peer review according to the Apache Foundation guidelines are used to guarantee the highest 
quality standards. 


Official website: [https://tomee.apache.org](https://tomee.apache.org)

---

- [To start using TomEE](#to-starti-using-tomee)
- [To start developing TomEE](#to-start-developing-tomEE)
- [Contributing](#contributing)
- [Changelog](#changelog)
- [License](#license)


---

## To start using TomEE

You can use TomEE to deploy Jakarta EE, Microprofile and Java EE compliant applications. 

* Apache TomEE is distributed 5 different flavors, you can check the [comparison](http://tomee.apache.org/comparison.html) 
table and then proceed to [donwload](http://tomee.apache.org/download-ng.html) the distribution for your use case. 
    
* [Documentation and examples](http://tomee.apache.org/docs.html) are available in the official website.

* The dozens of application included in the examples/ folder of the project.
   


## To start developing TomEE

TomEE is open source and you can help in its creation.

Apache TomEE is built with Apache Maven >= 3.3.9 and Java 8. The [Contribute to TomEE](http://tomee.apache.org/community/sources.html) section from the official website for a complete 
Git, Github, Build, Test, and Continuous Integration details.


    
### Quick build

If you only like to compile all classes and package up TomEE *without* running tests 
then you can use the following build options
    
    mvn -Pquick -Dsurefire.useFile=false -DdisableXmlReport=true -DuniqueVersion=false -ff -Dassemble -DskipTests -DfailIfNoTests=false clean install
    
### Full build

    mvn clean install
    

If you intend building in environments where multicast is not allowed
then build with:
    
    mvn clean install -DskipMulticastTests=true
    
Full build that execute arquillian test on all TomEE distributions:
    
    mvn clean install -Pall-adapters
    
### Partial build    

To build just TomEE distribution execute:

    mvn clean install -pl tomee/apache-tomee -am -Dmaven.test.skip=true

TomEE zip/tar.gz will be in tomee/apache-tomee/target

To build TomEE Embedded to be able to develop with its maven plugin execute:

    mvn clean install -pl maven/tomee-embedded-maven-plugin -am -Dmaven.test.skip=true


## Contributing

The [community](http://tomee.apache.org/community/index.html) section from the official website offers details on how you
can join the mailing lists, file tickets, fix bugs and start to contribute to the project. 


## Changelog

List of [Jira Releases](https://issues.apache.org/jira/projects/TOMEE?selectedItem=com.atlassian.jira.jira-projects-plugin:release-page&status=released) 
(*)

(*) You need to login or sign up with a free Apache Jira Account

 

## License

[Apache License 2.0](LICENSE)





