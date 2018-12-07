# Microprofile Fault Tolerance - Retry policy
This is an example of how to use Microprofile @Retry in TomEE.

#### Retry Feature
Microprofile Fault Tolerance has a feature called Retry that can be used to recover an operation from failure, invoking the same operation again 
until it reaches its stopping criteria.

The Retry policy allows to configure :

* **maxRetries**: the maximum retries 
* **delay**: delays between each retry
* **delayUnit**: the delay unit
* **maxDuration**: maximum duration to perform the retry for.
* **durationUnit**: duration unit
* **jitter:** the random vary of retry delays
* **jitterDelayUnit:** the jitter unit
* **retryOn:** specify the failures to retry on
* **abortOn:** specify the failures to abort on

To use this feature you can annotate a class and/or a method with the @Retry annotation. 
Check the [specification](http://download.eclipse.org/microprofile/microprofile-fault-tolerance-1.1/microprofile-fault-tolerance-spec.html) for more details.

### Examples

##### Run the application

    mvn clean install tomee:run   
    
##### Example 1

The method statusOfDay will fail three times, each time, throwing a `WeatherGatewayTimeoutException` and as the
@Retry annotation is configured to `retryOn` in case of failure, the FailSafe library will take the `maxRetry` value and
retry the same operation until it reaches the number maximum of attempts, which is 3 (default value).  

```java
@RequestScoped
public class WeatherGateway{ 
   ...
   @Retry(maxRetry=3, retryOn = WeatherGatewayTimeoutException.class)
   public String statusOfDay(){
       if(counterStatusOfDay.addAndGet(1) <= DEFAULT_MAX_RETRY){
           LOGGER.warning(String.format(FORECAST_TIMEOUT_MESSAGE, DEFAULT_MAX_RETRY, counterStatusOfDay.get()));
           throw new WeatherGatewayTimeoutException();
       }
       return "Today is a sunny day!";
   }
   ...
 }
```

Day status call

    GET http://localhost:8080/mp-faulttolerance-retry/weather/day/status
    
Server log
```
WARNING - Timeout when accessing AccuWeather Forecast Service. Max of Attempts: (3), Attempts: (1)
WARNING - Timeout when accessing AccuWeather Forecast Service. Max of Attempts: (3), Attempts: (2)
WARNING - Timeout when accessing AccuWeather Forecast Service. Max of Attempts: (3), Attempts: (3)
```

Response
``` 
Today is a sunny day!
```

##### Example 2

The method weekStatus will fail two times, each time, throwing a `WeatherGatewayTimeoutException` because `retryOn` is configured and instead of 
returning a response to the caller, the logic states that at the third attempt, a `WeatherGatewayBusyServiceException` will be thrown.
 As the `@Retry` annotation is configured to `abortOn` in case of `WeatherGatewayTimeoutException` happens, the remaining attempt won't be 
 executed and the caller must handle the exception.

```java
@Retry(maxRetries = 3, retryOn = WeatherGatewayTimeoutException.class, abortOn = WeatherGatewayBusyServiceException.class)
public String statusOfWeek(){
    if(counterStatusOfWeek.addAndGet(1) <= DEFAULT_MAX_RETRY){
        LOGGER.warning(String.format(FORECAST_TIMEOUT_MESSAGE_ATTEMPTS, DEFAULT_MAX_RETRY, counterStatusOfWeek.get()));
        throw new WeatherGatewayTimeoutException();
    }
    LOGGER.log(Level.SEVERE, String.format(FORECAST_BUSY_MESSAGE, counterStatusOfWeek.get()));
    throw new WeatherGatewayBusyServiceException();
}
```

Week status call

    GET http://localhost:8080/mp-faulttolerance-retry/weather/week/status

Server log

```
WARNING - Timeout when accessing AccuWeather Forecast Service. Max of Attempts: (3), Attempts: (1)
WARNING - Timeout when accessing AccuWeather Forecast Service. Max of Attempts: (3), Attempts: (2)
WARNING - Timeout when accessing AccuWeather Forecast Service. Max of Attempts: (3), Attempts: (3)
SEVERE  - Error AccuWeather Forecast Service is busy. Number of Attempts: (4) 
```

Response
``` 
WeatherGateway Service is Busy. Retry later
```

##### Example 3

The `@Retry` annotation allows to configure a delay for each new attempt be executed giving a chance to service
requested to recover itself and answerer the request properly. For each new retry follow the delay configure,
is needed to set `jitter` to zero (0). Otherwise the delay of each new attempt will be randomized.

Analysing the logged messages, is possible to see that all attempts took the pretty much the same time to execute.

```java 
@Retry(retryOn = WeatherGatewayTimeoutException.class, maxRetries = 5, delay = 500, jitter = 0)
public String statusOfWeekend() {
    if (counterStatusOfWeekend.addAndGet(1) <= 5) {
        logTimeoutMessage(statusOfWeekendInstant);
        statusOfWeekendInstant = Instant.now();
        throw new WeatherGatewayTimeoutException();
    }
    return "The Forecast for the Weekend is Scattered Showers.";
}
```

Weekend status call

    GET http://localhost:8080/mp-faulttolerance-retry/weather/weekend/status
    
Server log

```
WARNING - Timeout when accessing AccuWeather Forecast Service.
WARNING - Timeout when accessing AccuWeather Forecast Service. Delay before this attempt: (501) millis
WARNING - Timeout when accessing AccuWeather Forecast Service. Delay before this attempt: (501) millis
WARNING - Timeout when accessing AccuWeather Forecast Service. Delay before this attempt: (501) millis
WARNING - Timeout when accessing AccuWeather Forecast Service. Delay before this attempt: (500) millis
```

##### Example 4

Basically with the same behaviour of the `Example 3`, this example sets the `delay` and `jitter` with 500 millis to randomly
create a new delay for each new attempt after the first failure. [AbstractExecution#randomDelay(delay,jitter,random)](https://github.com/jhalterman/failsafe/blob/master/src/main/java/net/jodah/failsafe/AbstractExecution.java) 
can give a hit of how the new delay is calculated.

Analysing the logged messages, is possible to see how long each attempt had to wait until its execution.

```java 
@Retry(retryOn = WeatherGatewayTimeoutException.class, delay = 500, jitter = 500)
public String statusOfMonth() {
    if (counterStatusOfWeekend.addAndGet(1) <= DEFAULT_MAX_RETRY) {
        logTimeoutMessage(statusOfMonthInstant);
        statusOfMonthInstant = Instant.now();
        throw new WeatherGatewayTimeoutException();
    }
    return "The Forecast for the Weekend is Scattered Showers.";
}
```

Month status call

    GET http://localhost:8080/mp-faulttolerance-retry/weather/month/status
    
Server log

```
WARNING - Timeout when accessing AccuWeather Forecast Service.
WARNING - Timeout when accessing AccuWeather Forecast Service. Delay before this attempt: (417) millis
WARNING - Timeout when accessing AccuWeather Forecast Service. Delay before this attempt: (90) millis
```

##### Example 5

If a condition for an operation be re-executed is not set as in the previous examples using the parameter `retryOn`, 
the operation is executed again for _any_ exception that is thrown.

```java 
@Retry(maxDuration = 1000)
public String statusOfYear(){
    if (counterStatusOfWeekend.addAndGet(1) <= 5) {
        logTimeoutMessage(statusOfYearInstant);
        statusOfYearInstant = Instant.now();
        throw new RuntimeException();
    }
    return "WeatherGateway Service Error";
}
```

Year status call

    GET http://localhost:8080/mp-faulttolerance-retry/weather/year/statusk

Server log

```
WARNING - Timeout when accessing AccuWeather Forecast Service.
WARNING - Timeout when accessing AccuWeather Forecast Service. Delay before this attempt: (666) millis
WARNING - Timeout when accessing AccuWeather Forecast Service. Delay before this attempt: (266) millis
WARNING - Timeout when accessing AccuWeather Forecast Service. Delay before this attempt: (66) millis
```

##### Run the tests

You can also try it out using the [WeatherServiceTest.java](src/test/java/org/superbiz/rest/WeatherServiceTest.java) available in the project.

    mvn clean test
    
```
[INFO] Results:
[INFO] 
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

