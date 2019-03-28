package de.devtom.app.knxmqttbridge.device;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BridgeConfiguration {
	@JsonProperty("mqtt-server-host")
	private String mqttServerHost;
	@JsonProperty("mqtt-server-port")
	private int mqttServerPort;
	@JsonProperty("mqtt-user")
	private String mqttUser;
	@JsonProperty("mqtt-password")
	private String mqttPassword;
	@JsonProperty("knx-remote-host")
	private String knxRemoteHost;
	@JsonProperty("devices")
	private List<DeviceConfiguration> devices;

	public List<DeviceConfiguration> getDevices() {
		return devices;
	}
	
	public String getMqttServerUri() {
		StringBuilder sb = new StringBuilder();
		sb.append("tcp://");
		sb.append(mqttServerHost);
		sb.append(":");
		sb.append(mqttServerPort);
		
		return sb.toString();
	}

	public String getMqttUser() {
		return mqttUser;
	}

	public String getMqttPassword() {
		return mqttPassword;
	}

	public String getKnxRemoteHost() {
		return knxRemoteHost;
	}
}
