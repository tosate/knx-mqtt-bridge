package de.devtom.app.knxmqttbridge.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TasmotaMqttResultData {
	private static final String ON_MESSAGE = "ON";
	
	@JsonProperty("POWER")
	private String power;
	
	public boolean isPowerOn() {
		return this.power.equalsIgnoreCase(ON_MESSAGE);
	}
}
