package org.superbiz.openapi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.Json;
import javax.json.JsonObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.StringReader;

import java.net.URL;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class WeatherServiceTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(WeatherService.class)
                .addClass(Weather.class)
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");
        return webArchive;
    }

    @ArquillianResource
    private URL base;

    private Client client;

    @Before
    public void before() {
        this.client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        this.client.close();
    }

    
    @Test
    public void dayStatus() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final Response message =  webTarget.path("/openapi")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        final String metaData = message.readEntity(String.class);
        JsonObject jsonObject = Json.createReader(new StringReader(metaData)).readObject();
        JsonObject response=jsonObject.get("paths").asJsonObject().get("/weather/status/{day}").asJsonObject();
        final String expected = "{\r\n" + 
        		"            \"get\": {\r\n" + 
        		"                \"deprecated\": false,\r\n" + 
        		"                \"description\": \"Describes how the day will be.\",\r\n" + 
        		"                \"operationId\": \"dayStatus\",\r\n" + 
        		"                \"parameters\": [\r\n" + 
        		"                    {\r\n" + 
        		"                        \"allowEmptyValue\": false,\r\n" + 
        		"                        \"allowReserved\": false,\r\n" + 
        		"                        \"description\": \"The day for which the weather needs to be fetched.\",\r\n" + 
        		"                        \"in\": \"path\",\r\n" + 
        		"                        \"name\": \"\",\r\n" + 
        		"                        \"required\": true,\r\n" + 
        		"                        \"schema\": {\r\n" + 
        		"                            \"type\": \"string\"\r\n" + 
        		"                        }\r\n" + 
        		"                    }\r\n" + 
        		"                ],\r\n" + 
        		"                \"responses\": {\r\n" + 
        		"                    \"400\": {\r\n" + 
        		"                        \"content\": {\r\n" + 
        		"                            \"200\": {}\r\n" + 
        		"                        },\r\n" + 
        		"                        \"description\": \"Weather for this day not found\"\r\n" + 
        		"                    }\r\n" + 
        		"                },\r\n" + 
        		"                \"summary\": \"Finds weather for day specified in the URL \"\r\n" + 
        		"            }\r\n" + 
        		"        }";
        JsonObject expectedJson = Json.createReader(new StringReader(expected)).readObject();
        assertEquals(expectedJson.keySet().size(), response.keySet().size());
        String[] expectedKeys = new String[]{"deprecated", "description", "operationId", "parameters", "responses", "summary"};
        Stream.of(expectedKeys).forEach((text) -> {
          assertTrue("Expected: " + text
                  + " to be present in " + expected,
                  expectedJson.getJsonObject("get").get(text) != null);
        });
        

    }
    
    @Test
    public void detailedDayStatus() {
        WebTarget webTarget = this.client.target(this.base.toExternalForm());
        final Response message =  webTarget.path("/openapi")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        final String metaData = message.readEntity(String.class);
        JsonObject jsonObject = Json.createReader(new StringReader(metaData)).readObject();
        JsonObject response=jsonObject.get("paths").asJsonObject().get("/weather/detailedWeather/{day}").asJsonObject();
        final String expected = "{\r\n" + 
        		"            \"get\": {\r\n" + 
        		"                \"operationId\": \"getDetailedWeather\",\r\n" + 
        		"                \"parameters\": [\r\n" + 
        		"                    {\r\n" + 
        		"                        \"allowEmptyValue\": false,\r\n" + 
        		"                        \"allowReserved\": false,\r\n" + 
        		"                        \"description\": \"The day for which the detailed weather needs to be fetched.\",\r\n" + 
        		"                        \"in\": \"path\",\r\n" + 
        		"                        \"name\": \"\",\r\n" + 
        		"                        \"required\": true,\r\n" + 
        		"                        \"schema\": {\r\n" + 
        		"                            \"type\": \"string\"\r\n" + 
        		"                        }\r\n" + 
        		"                    }\r\n" + 
        		"                ],\r\n" + 
        		"                \"responses\": {\r\n" + 
        		"                    \"default\": {\r\n" + 
        		"                        \"content\": {\r\n" + 
        		"                            \"application/json\": {\r\n" + 
        		"                                \"schema\": {\r\n" + 
        		"                                    \"deprecated\": false,\r\n" + 
        		"                                    \"description\": \"POJO that represents weather.\",\r\n" + 
        		"                                    \"exclusiveMaximum\": false,\r\n" + 
        		"                                    \"exclusiveMinimum\": false,\r\n" + 
        		"                                    \"items\": {},\r\n" + 
        		"                                    \"maxLength\": 2147483647,\r\n" + 
        		"                                    \"minLength\": 0,\r\n" + 
        		"                                    \"nullable\": false,\r\n" + 
        		"                                    \"properties\": {\r\n" + 
        		"                                        \"humidityPercentage\": {\r\n" + 
        		"                                            \"deprecated\": false,\r\n" + 
        		"                                            \"example\": \"4\",\r\n" + 
        		"                                            \"exclusiveMaximum\": false,\r\n" + 
        		"                                            \"exclusiveMinimum\": false,\r\n" + 
        		"                                            \"maxLength\": 2147483647,\r\n" + 
        		"                                            \"minLength\": 0,\r\n" + 
        		"                                            \"nullable\": false,\r\n" + 
        		"                                            \"readOnly\": false,\r\n" + 
        		"                                            \"type\": \"string\",\r\n" + 
        		"                                            \"uniqueItems\": false,\r\n" + 
        		"                                            \"writeOnly\": false\r\n" + 
        		"                                        },\r\n" + 
        		"                                        \"temperatureInCelsius\": {\r\n" + 
        		"                                            \"deprecated\": false,\r\n" + 
        		"                                            \"example\": \"27\",\r\n" + 
        		"                                            \"exclusiveMaximum\": false,\r\n" + 
        		"                                            \"exclusiveMinimum\": false,\r\n" + 
        		"                                            \"maxLength\": 2147483647,\r\n" + 
        		"                                            \"minLength\": 0,\r\n" + 
        		"                                            \"nullable\": false,\r\n" + 
        		"                                            \"readOnly\": false,\r\n" + 
        		"                                            \"type\": \"string\",\r\n" + 
        		"                                            \"uniqueItems\": false,\r\n" + 
        		"                                            \"writeOnly\": false\r\n" + 
        		"                                        }\r\n" + 
        		"                                    },\r\n" + 
        		"                                    \"readOnly\": false,\r\n" + 
        		"                                    \"required\": [\r\n" + 
        		"                                        \"temperatureInCelsius\",\r\n" + 
        		"                                        \"humidityPercentage\"\r\n" + 
        		"                                    ],\r\n" + 
        		"                                    \"uniqueItems\": false,\r\n" + 
        		"                                    \"writeOnly\": false\r\n" + 
        		"                                }\r\n" + 
        		"                            }\r\n" + 
        		"                        },\r\n" + 
        		"                        \"description\": \"Detailed Weather\"\r\n" + 
        		"                    }\r\n" + 
        		"                }\r\n" + 
        		"            }\r\n" + 
        		"        }";
        JsonObject expectedJson = Json.createReader(new StringReader(expected)).readObject();
        assertEquals(expectedJson.keySet().size(), response.keySet().size());
        String[] expectedKeys = new String[]{ "operationId", "parameters", "responses"};
        Stream.of(expectedKeys).forEach((text) -> {
          assertTrue("Expected: " + text
                  + " to be present in " + expected,
                  expectedJson.getJsonObject("get").get(text) != null);
        });
        
        
        

    }


}
