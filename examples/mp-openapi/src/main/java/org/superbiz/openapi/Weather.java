package org.superbiz.openapi;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="Weather", description="POJO that represents weather.")
public class Weather {
	
	@Schema(required = true, example = "27")
	private String temperatureInCelsius;
	
	@Schema(required = true, example = "4")
	private String humidityPercentage;
	public String getTemperature() {
		return temperatureInCelsius;
	}
	public void setTemperature(String temperature) {
		this.temperatureInCelsius = temperature;
	}
	public String getHumidity() {
		return humidityPercentage;
	}
	public void setHumidity(String humidity) {
		this.humidityPercentage = humidity;
	}
}
