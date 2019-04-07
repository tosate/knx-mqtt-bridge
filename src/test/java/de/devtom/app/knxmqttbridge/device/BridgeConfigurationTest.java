package de.devtom.app.knxmqttbridge.device;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BridgeConfigurationTest {
	private static final String LOCALHOST = "localhost";
	private static final int MQTT_SERVER_PORT = 1883;
	private static final String MQTT_USER = "user";
	private static final String MQTT_PASSWORD = "secret";
	private static final String DEVICE_NAME = "Lamp 1";
	private static final String MQTT_TOPIC = "topic1";
	private static final String KNX_INDIVIDUAL_ADDRESS = "1.1.1";
	private static final String KNX_SWITCHING_GA1 = "1/1/1";
	private static final String KNX_SWITCHING_GA2 = "1/1/2";
	private static final String KNX_LISTENING_GA1 = "1/4/1";
	private static final String KNX_SCENE_GA = "4/1/1";

	@Test
	public void testConfigNoScenes() {
		BridgeConfiguration config = this.loadConfig("config_no-scenes.json");
		this.checkGenericConfigProperties(config);
		Assert.assertEquals(1, config.getDeviceConfigurations().size());
		this.checkDeviceConfiguration(config.getDeviceConfigurations().get(0), false);
	}
	
	@Test
	public void testConfigWithScenes() {
		BridgeConfiguration config = this.loadConfig("config_with-scenes.json");
		this.checkGenericConfigProperties(config);
		Assert.assertEquals(1, config.getDeviceConfigurations().size());
		this.checkDeviceConfiguration(config.getDeviceConfigurations().get(0), true);
	}
	
	private void checkGenericConfigProperties(BridgeConfiguration config) {
		Assert.assertEquals(getMqttServerUri(), config.getMqttServerUri());
		Assert.assertEquals(LOCALHOST, config.getKnxRemoteHost());
		Assert.assertEquals(MQTT_USER, config.getMqttUser());
		Assert.assertEquals(MQTT_PASSWORD, config.getMqttPassword());
		Assert.assertEquals(LOCALHOST, config.getKnxRemoteHost());
	}
	
	private void checkDeviceConfiguration(DeviceConfiguration deviceConfiguration, boolean withScene) {
		Assert.assertEquals(DEVICE_NAME, deviceConfiguration.getName());
		Assert.assertEquals(MQTT_TOPIC, deviceConfiguration.getMqttTopic());
		Assert.assertEquals(KNX_INDIVIDUAL_ADDRESS, deviceConfiguration.getKnxIndividualAddress());
		Assert.assertEquals(2, deviceConfiguration.getKnxSwitchingGroupAddresses().size());
		Assert.assertEquals(KNX_SWITCHING_GA1, deviceConfiguration.getKnxSwitchingGroupAddresses().get(0));
		Assert.assertEquals(KNX_SWITCHING_GA2, deviceConfiguration.getKnxSwitchingGroupAddresses().get(1));
		Assert.assertEquals(1, deviceConfiguration.getKnxListeningGroupAddresses().size());
		Assert.assertEquals(KNX_LISTENING_GA1, deviceConfiguration.getKnxListeningGroupAddresses().get(0));
		if(withScene) {
			Assert.assertEquals(KNX_SCENE_GA, deviceConfiguration.getKnxSceneGroupAddress());
			Assert.assertEquals(1, deviceConfiguration.getKnxScenes().size());
			Assert.assertEquals(1, deviceConfiguration.getKnxScenes().get(0).getSceneNumber());
			Assert.assertFalse(deviceConfiguration.getKnxScenes().get(0).isSwitchedOn());
		} else {
			Assert.assertNull(deviceConfiguration.getKnxSceneGroupAddress());
			Assert.assertNull(deviceConfiguration.getKnxScenes());
		}
	}
	
	private BridgeConfiguration loadConfig(String resource) {
		try {
			URI uri = BridgeConfigurationTest.class.getClassLoader().getResource(resource).toURI();
			String configString = new String(Files.readAllBytes(Paths.get(uri)), StandardCharsets.UTF_8);
			ObjectMapper objectMapper = new ObjectMapper();
			BridgeConfiguration config =  objectMapper.readValue(configString, BridgeConfiguration.class);
			return config;
		} catch (Exception e) {
			Assert.fail("Exception: +" + e.getMessage());
			return null;
		}
	}
	
	private String getMqttServerUri() {
		StringBuilder sb = new StringBuilder();
		sb.append("tcp://");
		sb.append(LOCALHOST);
		sb.append(":");
		sb.append(MQTT_SERVER_PORT);
		
		return sb.toString();
	}
}
