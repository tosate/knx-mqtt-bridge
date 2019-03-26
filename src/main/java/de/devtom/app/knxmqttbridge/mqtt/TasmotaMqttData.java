package de.devtom.app.knxmqttbridge.mqtt;

public class TasmotaMqttData {
	private String mqttTopicPrefix;
	private String mqttTopic;
	private String mqttTopicSuffix;
	private String mqttPayload;
	
	public String getMqttTopicPrefix() {
		return mqttTopicPrefix;
	}
	public void setMqttPrefix(String mqttTopicPrefix) {
		this.mqttTopicPrefix = mqttTopicPrefix;
	}
	public String getMqttTopic() {
		return mqttTopic;
	}
	public void setMqttTopic(String mqttTopic) {
		this.mqttTopic = mqttTopic;
	}
	public String getMqttTopicSuffix() {
		return mqttTopicSuffix;
	}
	public void setMqttTopicSuffix(String mqttTopicSuffix) {
		this.mqttTopicSuffix = mqttTopicSuffix;
	}
	public String getMqttPayload() {
		return mqttPayload;
	}
	public void setMqttPayload(String mqttPayload) {
		this.mqttPayload = mqttPayload;
	}
	
}
