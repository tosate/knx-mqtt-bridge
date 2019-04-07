package de.devtom.app.knxmqttbridge.device;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.devtom.app.knxmqttbridge.knx.KnxDeviceManager;
import de.devtom.app.knxmqttbridge.mqtt.MqttDeviceManager;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttMessageHandler;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

@Component
public class ServiceRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
	@Autowired
	private BridgeConfiguration config;
	@Autowired
	private TasmotaMqttMessageHandler mqttMessageHandler;
	@Autowired
	private ApplicationContext appContext;
	
	private MqttDeviceManager mqttDeviceManager;
	private KnxDeviceManager knxDeviceManager;
	private Map<String, String> individualAddressToTopic = new HashMap<>();
	private Map<String, String> topicToIndividualAddress = new HashMap<>();
	
	public MqttDeviceManager getMqttDeviceManager() {
		return mqttDeviceManager;
	}

	public void switchService(String serviceIdentifier, boolean switchOn) {
		try {
			IndividualAddress individualAddress = new IndividualAddress(serviceIdentifier);
			if(individualAddressToTopic.containsKey(individualAddress.toString())) {
				String topic = individualAddressToTopic.get(individualAddress.toString());
				mqttDeviceManager.switchService(topic, switchOn);
			} else {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("No topic found for {}", individualAddress.toString());
				}
			}
		} catch (KNXFormatException e) {
			LOGGER.error("KNX format exception: ", e);
		}
	}
	
	public void updateServiceState(String serviceIdentifier, boolean switchedOn) {
		// send status to KNX
		String individualAddresses = this.topicToIndividualAddress.get(serviceIdentifier);
		this.knxDeviceManager.updateServiceState(individualAddresses, switchedOn);
	}

	@PostConstruct
	private void init() {
		this.mqttDeviceManager = new MqttDeviceManager(this, mqttMessageHandler);
		this.knxDeviceManager = new KnxDeviceManager(this, config.getKnxRemoteHost());
		knxDeviceManager.connect();
		knxDeviceManager.initDevices(config.getDeviceConfigurations());
		for(DeviceConfiguration deviceConfig : config.getDeviceConfigurations()) {
			individualAddressToTopic.put(deviceConfig.getKnxIndividualAddress(), deviceConfig.getMqttTopic());
			
			topicToIndividualAddress.put(deviceConfig.getMqttTopic(), deviceConfig.getKnxIndividualAddress());
		}
	}
	
	public void terminateApplication() {
		int exitCode = SpringApplication.exit(appContext, new ExitCodeGenerator() {
			
			@Override
			public int getExitCode() {
				return -1;
			}
		});
		
		System.exit(exitCode);
	}
}
