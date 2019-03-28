package de.devtom.app.knxmqttbridge.device;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttData;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttDevice;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttPrefix;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.device.BaseKnxDevice;
import tuwien.auto.calimero.exception.KNXFormatException;

@Component
public class DeviceManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceManager.class);
	
	@Autowired
	private BridgeConfiguration config;
	
	private Map<String, IndividualAddress> mqttToKnxMap;
	private Map<IndividualAddress, String> knxToMqttMap;
	
	private Map<String, TasmotaMqttDevice> mqttDevices;
	private Map<IndividualAddress, BaseKnxDevice> knxDevices;
	
	public DeviceManager() {
		this.mqttToKnxMap = new HashMap<>();
		this.knxToMqttMap = new HashMap<>();
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
		if(knxDevices.containsKey(knxDevice.getAddress())) {
			LOGGER.error("Failed to add KNX device to DeviceManager! Device {} is already registered.", knxDevice.getAddress().toString());
		} else {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding KNX device {} to DeviceManager.", knxDevice.getAddress().toString());
			}
			knxDevices.put(knxDevice.getAddress(), knxDevice);
		}
	}
	
	private BaseKnxDevice getByMqttTopic(String mqttTopic) {
		IndividualAddress individualAddress = this.mqttToKnxMap.get(mqttTopic);
		return this.knxDevices.get(individualAddress);
	}
	
	@PostConstruct
	private void initMaps() {
		for(DeviceConfiguration deviceConfiguration : config.getDeviceConfigurations()) {
			IndividualAddress individualAddress = null;
			try {
				individualAddress = new IndividualAddress(deviceConfiguration.getKnxIndividualAddress());
			} catch(KNXFormatException e) {
				LOGGER.debug("Invalid KNX device address: ", e);
				continue;
			}
			
			this.mqttToKnxMap.put(deviceConfiguration.getMqttTopic(), individualAddress);
			this.knxToMqttMap.put(individualAddress, deviceConfiguration.getMqttTopic());
		}
	}
}
