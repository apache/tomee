# Async Servlet  
  
The Original project is [async-servlet](https://github.com/apache/tomee/tree/main/examples/async-servlet).   
It is a very nice modern coding style for testing, but hard for beginners like us to understand how it works.  
Therefore, we have converted it into a Web application.


## OverView  
  
function: Sending two numbers and a operation to the servlet via a query strings with the GET method, this app returns the result of the four arithmetic operations.
![usage](https://user-images.githubusercontent.com/20388463/185592836-85beeed4-06af-434e-86f4-ff4b32bbbaf7.png)
You can also specify asynchronous processing. 
  
## How to Build, deploy and run  
  
1. Import project into IDE (NetBeans, eclipse...etc) or type `mvn clean install` from command line, you can get `async-servlet2.war` file.  
2. Deploy the war file via TomEE GUI manager.  
3. Access http://localhost:8080/async-servlet2/ from your browser.  
  
Enjoy TomEE!
  


  written by Akiba Taro and air-h-128k-il  

  