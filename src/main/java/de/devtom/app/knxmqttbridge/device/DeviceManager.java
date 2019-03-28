package de.devtom.app.knxmqttbridge.device;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttData;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttDevice;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttPrefix;
import tuwien.auto.calimero.device.BaseKnxDevice;

@Component
public class DeviceManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceManager.class);
	private Map<String, TasmotaMqttDevice> mqttDevices;
	private Map<String, BaseKnxDevice> knxDevices;
	
	public DeviceManager() {
		this.mqttDevices = new HashMap<>();
		this.knxDevices = new HashMap<>();
	}

	public void addMqttDevice(TasmotaMqttDevice device) {
		if(mqttDevices.containsKey(device.getTopic())) {
			LOGGER.error("Failed to add MQTT device to DeviceManager! Device {} is already registerd.", device.getTopic());
		} else {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding MQTT device {} to DeviceManager.", device.getTopic());
			}
			mqttDevices.put(device.getTopic(), device);
		}
	}

	public void processMqttData(TasmotaMqttData tasmotaMqttData) {
		if(mqttDevices.containsKey(tasmotaMqttData.getMqttTopic())) {
			TasmotaMqttDevice mqttDevice = mqttDevices.get(tasmotaMqttData.getMqttTopic());
			
			TasmotaMqttPrefix tasmotaMqttPrefix = TasmotaMqttPrefix.fromValue(tasmotaMqttData.getMqttTopicPrefix());
			
			switch(tasmotaMqttPrefix) {
			case COMMAND:
				break;
			case TELEMETRY:
				mqttDevice.processTelemetryData(tasmotaMqttData);
				break;
			case STATUS:
				mqttDevice.processStatusData(tasmotaMqttData);
				break;
			}
		} else {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Could not find device {} for processing MQTT data.", tasmotaMqttData.getMqttTopic());
			}
		}
	}

	public void addKnxDevice(BaseKnxDevice knxDevice) {
		if(knxDevices.containsKey(knxDevice.getAddress().toString())) {
			LOGGER.error("Failed to add KNX device to DeviceManager! Device {} is already registered.", knxDevice.getAddress().toString());
		} else {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding KNX device {} to DeviceManager.", knxDevice.getAddress().toString());
			}
			knxDevices.put(knxDevice.getAddress().toString(), knxDevice);
		}
	}
}
