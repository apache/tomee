index-group=Misc
type=page
status=published
title=MVC-CXF
~~~~~~


Simple example of using [JSR-371](http://mvc-spec.org) together with Deltaspike-Data to do CRUD operations.

<h2>Steps to run the example</h2>

Build and start the demo:

    mvn clean package

Open:

    http://localhost:8080/mvc-cxf

<h2>Intro of Eclipse Krazo</h2>
    
[Eclipse Krazo](https://projects.eclipse.org/proposals/eclipse-krazo) is an implementation of action-based MVC specifiec by MVC 1.0 (JSR-371). 
It builds on top of JAX-RS and currently contains support for RESTEasy, Jersey and CXF with a well-defined SPI for other imlementations.