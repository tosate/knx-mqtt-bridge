package de.devtom.app.knxmqttbridge.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TasmotaSensorData {
	@JsonProperty("Time")
	private String time;
	@JsonProperty("ENERGY")
	private TasmotaEnergyData energy;
	
	public String getTime() {
		return time;
	}
	public TasmotaEnergyData getEnergy() {
		return energy;
	}
}
