package de.devtom.app.knxmqttbridge.device;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceConfiguration {
	@JsonProperty("device-name")
	private String name;
	@JsonProperty("mqtt-topic")
	private String mqttTopic;
	@JsonProperty("knx-individual-address")
	private String knxIndividualAddress;
	@JsonProperty("knx-ga-switching")
	private List<String> knxSwitchingGroupAddresses;
	@JsonProperty("knx-ga-listening")
	private List<String> knxListeningGroupAddresses;
	
	public String getName() {
		return name;
	}
	public String getMqttTopic() {
		return mqttTopic;
	}
	public List<String> getKnxSwitchingGroupAddresses() {
		return knxSwitchingGroupAddresses;
	}
	public List<String> getKnxListeningGroupAddresses() {
		return knxListeningGroupAddresses;
	}
	public String getKnxIndividualAddress() {
		return knxIndividualAddress;
	}
}
