package de.devtom.app.knxmqttbridge.mqtt;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.devtom.app.knxmqttbridge.device.DeviceManager;
import de.devtom.app.knxmqttbridge.device.ServiceRegistry;

public class MqttDeviceManager implements DeviceManager{
	private static final Logger LOGGER = LoggerFactory.getLogger(MqttDeviceManager.class);
	
	private ServiceRegistry serviceRegistry;
	private TasmotaMqttMessageHandler mqttMessageHandler;
	private Map<String, TasmotaMqttDevice> mqttDevices = new HashMap<>();
	
	public MqttDeviceManager(ServiceRegistry serviceRegistry, TasmotaMqttMessageHandler mqttMessageHandler) {
		this.mqttMessageHandler = mqttMessageHandler;
		this.serviceRegistry = serviceRegistry;
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
				 // send request to KNX
				this.serviceRegistry.updateServiceState(tasmotaMqttData.getMqttTopic(), mqttDevice.isSwitchedOn());
				break;
			}
		} else {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Could not find device {} for processing MQTT data.", tasmotaMqttData.getMqttTopic());
			}
		}
	}

	@Override
	public void switchService(String serviceIdentifier, boolean switchOn) {
		// send MQTT command
		TasmotaMqttDevice device = this.mqttDevices.get(serviceIdentifier);
		mqttMessageHandler.sendMqttMessage(device.getMqttFullTopic() + "/POWER", switchOn);
	}
	
	@Override
	public void updateServiceState(String serviceIdentifier, boolean switchedOn) {
		// do nothing
	}
}
