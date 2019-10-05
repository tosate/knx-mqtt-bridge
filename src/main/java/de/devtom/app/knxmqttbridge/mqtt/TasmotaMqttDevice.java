package de.devtom.app.knxmqttbridge.mqtt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TasmotaMqttDevice {
	private static final Logger LOGGER = LoggerFactory.getLogger(TasmotaMqttDevice.class);
	private static final String MQTT_DELIMITER = "/";
	private static final String FALLBACK_TOPIC_SUFFIX = "_fb";
	private static final String MQTT_SUBSCRIPTION_WILDCARD = "+";

	private String topic;
	private String clientId;
	private TasmotaStateData stateData;
	private TasmotaSensorData sensorData;
	private TasmotaMqttResultData resultData;
	private TasmotaUptimeData uptimeData;
	private String state;

	public TasmotaMqttDevice(String topic) {
		this.topic = topic;
		this.state = "Offline";
	}

	public String getMqttFullTopic() {
		StringBuilder sb = new StringBuilder();
		sb.append(TasmotaMqttPrefix.COMMAND.getValue());
		sb.append(MQTT_DELIMITER);
		sb.append(topic);

		return sb.toString();
	}

	public String getMqttFallbackTopic() {
		StringBuilder sb = new StringBuilder();
		sb.append(TasmotaMqttPrefix.COMMAND.getValue());
		sb.append(MQTT_DELIMITER);
		if (this.clientId != null) {
			sb.append(this.clientId);
		}
		sb.append(FALLBACK_TOPIC_SUFFIX);

		return sb.toString();
	}

	public String getMqttCommandTopics() {
		StringBuilder sb = new StringBuilder();
		sb.append(TasmotaMqttPrefix.COMMAND.getValue());
		sb.append(MQTT_DELIMITER);
		sb.append(topic);
		sb.append(MQTT_DELIMITER);
		sb.append(MQTT_SUBSCRIPTION_WILDCARD);

		return sb.toString();
	}

	public String getMqttTelemetryTopics() {
		StringBuilder sb = new StringBuilder();
		sb.append(TasmotaMqttPrefix.TELEMETRY.getValue());
		sb.append(MQTT_DELIMITER);
		sb.append(topic);
		sb.append(MQTT_DELIMITER);
		sb.append(MQTT_SUBSCRIPTION_WILDCARD);

		return sb.toString();
	}

	public String getMqttStatusTopics() {
		StringBuilder sb = new StringBuilder();
		sb.append(TasmotaMqttPrefix.STATUS.getValue());
		sb.append(MQTT_DELIMITER);
		sb.append(topic);
		sb.append(MQTT_DELIMITER);
		sb.append(MQTT_SUBSCRIPTION_WILDCARD);

		return sb.toString();
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getTopic() {
		return topic;
	}

	public TasmotaStateData getStateData() {
		return stateData;
	}

	public TasmotaSensorData getSensorData() {
		return sensorData;
	}

	public boolean isSwitchedOn() {
		return this.resultData.isPowerOn();
	}

	public String getState() {
		return state;
	}

	public void processTelemetryData(TasmotaMqttData tasmotaMqttData) {
		try {
			switch (tasmotaMqttData.getMqttTopicSuffix()) {
			case "STATE":
				this.processStateData(tasmotaMqttData);
				break;
			case "SENSOR":
				this.processSensorData(tasmotaMqttData);
				break;
			case "LWT":
				this.state = tasmotaMqttData.getMqttPayload();
				break;
			case "UPTIME":
				this.processUptimeData(tasmotaMqttData);
				break;
			default:
				LOGGER.error("Unknown telemetry topic suffix: " + tasmotaMqttData.getMqttTopicSuffix());
			}
		} catch (Exception e) {
			LOGGER.error("Error processing Tasmota MQTT data", e);
		}
	}

	private void processUptimeData(TasmotaMqttData tasmotaMqttData) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		this.uptimeData = objectMapper.readValue(tasmotaMqttData.getMqttPayload(), TasmotaUptimeData.class);
	}

	private void processStateData(TasmotaMqttData tasmotaMqttData)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		this.stateData = objectMapper.readValue(tasmotaMqttData.getMqttPayload(), TasmotaStateData.class);
	}
	
	private void processSensorData(TasmotaMqttData tasmotaMqttData) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		this.sensorData = objectMapper.readValue(tasmotaMqttData.getMqttPayload(), TasmotaSensorData.class);
	}
	
	private void processResultData(TasmotaMqttData tasmotaMqttData) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		this.resultData = objectMapper.readValue(tasmotaMqttData.getMqttPayload(), TasmotaMqttResultData.class);
	}

	public void processStatusData(TasmotaMqttData tasmotaMqttData) {
		try {
			switch(tasmotaMqttData.getMqttTopicSuffix()) {
			case "RESULT":
				this.processResultData(tasmotaMqttData);
				break;
			case "POWER":
				break;
			default:
				LOGGER.error("Unknown status topic suffix: " + tasmotaMqttData.getMqttTopicSuffix());
			}
		} catch (Exception e) {
			LOGGER.error("Error processing Tasmota MQTT data", e);
		}
	}

	public TasmotaUptimeData getUptimeData() {
		return this.uptimeData;
	}
}
