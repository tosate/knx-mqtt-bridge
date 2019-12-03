package de.devtom.app.knxmqttbridge.mqtt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class TasmotaUptimeData {
	@JsonProperty("Time")
	private String time;
	@JsonProperty("Uptime")
	private String uptime;
	
	public String getTime() {
		return time;
	}
	public String getUptime() {
		return uptime;
	}
}
