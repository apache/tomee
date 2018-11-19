# Microprofile Metrics
This is an example on how to use microprofile metrics in TomEE.

The command to run the application the application is:

    mvn clean install tomee:run 

Make a call to the greeting endpoint: 

    http://localhost:8080/rest-mp-metrics/greeting
    
Check the metrics in Prometheus format:
    
    http://localhost:8080/rest-mp-metrics/metrics
    
    

     

