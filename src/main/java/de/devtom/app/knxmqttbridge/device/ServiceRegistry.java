package de.devtom.app.knxmqttbridge.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.devtom.app.knxmqttbridge.knx.KnxDeviceManager;
import de.devtom.app.knxmqttbridge.mqtt.MqttDeviceManager;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttMessageHandler;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

@Component
public class ServiceRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
	@Autowired
	private BridgeConfiguration config;
	@Autowired
	private TasmotaMqttMessageHandler mqttMessageHandler;
	
	private MqttDeviceManager mqttDeviceManager;
	private KnxDeviceManager knxDeviceManager;
	private Map<String, String> groupAddressToTopic = new HashMap<>();
	private Map<String, List<String>> topicToListeningGa = new HashMap<>();
	
	public MqttDeviceManager getMqttDeviceManager() {
		return mqttDeviceManager;
	}

	public void switchService(String serviceIdentifier, boolean switchOn) {
		try {
			GroupAddress ga = new GroupAddress(serviceIdentifier);
			if(groupAddressToTopic.containsKey(ga.toString())) {
				String topic = groupAddressToTopic.get(ga.toString());
				mqttDeviceManager.switchService(topic, switchOn);
			} else {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("No topic found for {}", ga.toString());
				}
			}
		} catch (KNXFormatException e) {
			LOGGER.error("KNX format exception: ", e);
		}
	}
	
	public void updateServiceState(String serviceIdentifier, boolean switchedOn) {
		// send status to KNX
		List<String> groupdAddresses = this.topicToListeningGa.get(serviceIdentifier);
		for(String ga : groupdAddresses) {
			this.knxDeviceManager.updateServiceState(ga, switchedOn);
		}
	}

	@PostConstruct
	private void init() {
		this.mqttDeviceManager = new MqttDeviceManager(this, mqttMessageHandler);
		this.knxDeviceManager = new KnxDeviceManager(this, config.getKnxRemoteHost());
		knxDeviceManager.connect();
		knxDeviceManager.initDevices(config.getDeviceConfigurations());
		for(DeviceConfiguration deviceConfig : config.getDeviceConfigurations()) {
			for(String groupAddress : deviceConfig.getKnxSwitchingGroupAddresses()) {
				groupAddressToTopic.put(groupAddress, deviceConfig.getMqttTopic());
			}
			
			topicToListeningGa.put(deviceConfig.getMqttTopic(), deviceConfig.getKnxListeningGroupAddresses());
		}
	}
}
