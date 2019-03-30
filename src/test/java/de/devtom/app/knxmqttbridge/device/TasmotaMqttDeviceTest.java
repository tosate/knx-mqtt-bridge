package de.devtom.app.knxmqttbridge.device;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttData;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttDevice;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaSensorData;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaStateData;

public class TasmotaMqttDeviceTest {
	private static final String MQTT_DELIMITER = "/";
	private static final String MQTT_SUBSCRIPTION_WILDCARD = "+";
	private static final String TOPIC = "sonoff";
	private static final String CMND_PREFIX = "cmnd";
	private static final String TEL_PREFIX = "tele";
	private static final String STAT_PREFIX = "stat";
	private static final String CLIENT_ID = "DVES_12A3456";
	private static final String STATE_SUFFIX = "STATE";
	private static final String SENSOR_SUFFIX = "SENSOR";
	private static final String RESULT_SUFFIX = "RESULT";
	
	private TasmotaMqttDevice device;
	
	@Before
	public void init() {
		this.device = new TasmotaMqttDevice(TOPIC);
		device.setClientId(CLIENT_ID);
	}
	
	@Test
	public void testGetMqttFullTopic() {
		Assert.assertEquals(CMND_PREFIX + MQTT_DELIMITER + TOPIC, device.getMqttFullTopic());
	}
	
	@Test
	public void testGetMqttFallbackTopic() {
		Assert.assertEquals(CMND_PREFIX + MQTT_DELIMITER + CLIENT_ID + "_fb", device.getMqttFallbackTopic());
	}
	
	@Test
	public void testGetMqttCommandTopics() {
		Assert.assertEquals(CMND_PREFIX + MQTT_DELIMITER + TOPIC + MQTT_DELIMITER + MQTT_SUBSCRIPTION_WILDCARD, device.getMqttCommandTopics());
	}
	
	@Test
	public void testGetMqttTelemetryTopics() {
		Assert.assertEquals(TEL_PREFIX + MQTT_DELIMITER + TOPIC + MQTT_DELIMITER + MQTT_SUBSCRIPTION_WILDCARD, device.getMqttTelemetryTopics());
	}
	
	@Test
	public void testGetMqttStatusTopics() {
		Assert.assertEquals(STAT_PREFIX + MQTT_DELIMITER + TOPIC + MQTT_DELIMITER + MQTT_SUBSCRIPTION_WILDCARD, device.getMqttStatusTopics());
	}
	
	@Test
	public void testProcessTelemetryDataState() {
		TasmotaMqttData mqttData = new TasmotaMqttData();
		mqttData.setMqttPrefix(TEL_PREFIX);
		mqttData.setMqttTopic(TOPIC);
		mqttData.setMqttTopicSuffix(STATE_SUFFIX);
		mqttData.setMqttPayload("{\"Time\":\"2019-03-24T13:22:32\",\"Uptime\":\"4T00:25:22\",\"Vcc\":3.532,\"SleepMode\":\"Dynamic\",\"Sleep\":0,\"LoadAvg\":999,\"POWER\":\"OFF\",\"Wifi\":{\"AP\":1,\"SSId\":\"My-Wifi\",\"BSSId\":\"AA:AA:AA:AA:AA:AA\",\"Channel\":6,\"RSSI\":78,\"LinkCount\":1,\"Downtime\":\"0T00:00:04\"}}");
		
		this.device.processTelemetryData(mqttData);
		TasmotaStateData stateData = device.getStateData();
		Assert.assertEquals("2019-03-24T13:22:32", stateData.getTime());
		Assert.assertEquals("4T00:25:22", stateData.getUptime());
		Assert.assertEquals(3.532, stateData.getVcc(), 0.0001);
		Assert.assertEquals("Dynamic", stateData.getSleepMode());
		Assert.assertEquals(0, stateData.getSleep());
		Assert.assertEquals(999, stateData.getLoadAvg());
		Assert.assertEquals("OFF", stateData.getPower());
		Assert.assertEquals(1, stateData.getWifi().getAp());
		Assert.assertEquals("My-Wifi", stateData.getWifi().getSsid());
		Assert.assertEquals("AA:AA:AA:AA:AA:AA", stateData.getWifi().getBssId());
		Assert.assertEquals(6, stateData.getWifi().getChannel());
		Assert.assertEquals(78, stateData.getWifi().getRssi());
		Assert.assertEquals(1, stateData.getWifi().getLinkCount());
		Assert.assertEquals("0T00:00:04", stateData.getWifi().getDowntime());
	}
	
	@Test
	public void testProcessTelemetryDataSensor() {
		TasmotaMqttData mqttData = new TasmotaMqttData();
		mqttData.setMqttPrefix(TEL_PREFIX);
		mqttData.setMqttTopic(TOPIC);
		mqttData.setMqttTopicSuffix(SENSOR_SUFFIX);
		mqttData.setMqttPayload("{\"Time\":\"2019-03-24T13:42:32\",\"ENERGY\":{\"TotalStartTime\":\"2019-03-17T21:49:56\",\"Total\":0.027,\"Yesterday\":0.000,\"Today\":0.000,\"Period\":0,\"Power\":0,\"ApparentPower\":0,\"ReactivePower\":0,\"Factor\":0.00,\"Voltage\":0,\"Current\":0.000}}");
		
		this.device.processTelemetryData(mqttData);
		TasmotaSensorData sensorData = device.getSensorData();
		Assert.assertEquals("2019-03-24T13:42:32", sensorData.getTime());
		Assert.assertEquals("2019-03-17T21:49:56", sensorData.getEnergy().getTotalStartTime());
		Assert.assertEquals(0.027, sensorData.getEnergy().getTotal(), 0.0001);
		Assert.assertEquals(0.000, sensorData.getEnergy().getYesterday(), 0.0001);
		Assert.assertEquals(0.000, sensorData.getEnergy().getToday(), 0.0001);
		Assert.assertEquals(0, sensorData.getEnergy().getPeriod());
		Assert.assertEquals(0, sensorData.getEnergy().getPower());
		Assert.assertEquals(0, sensorData.getEnergy().getApparentPower());
		Assert.assertEquals(0, sensorData.getEnergy().getReactivePower());
		Assert.assertEquals(0.000, sensorData.getEnergy().getFactor(), 0.0001);
		Assert.assertEquals(0, sensorData.getEnergy().getVoltage());
		Assert.assertEquals(0.000, sensorData.getEnergy().getCurrent(), 0.0001);
	}
	
	@Test
	public void testProcessStatusDataResult() {
		TasmotaMqttData mqttData = new TasmotaMqttData();
		mqttData.setMqttPrefix(STAT_PREFIX);
		mqttData.setMqttTopic(TOPIC);
		mqttData.setMqttTopicSuffix(RESULT_SUFFIX);
		mqttData.setMqttPayload("{\"POWER\":\"OFF\"}");
		
		this.device.processStatusData(mqttData);
		Assert.assertEquals("OFF", device.isSwitchedOn());
	}
}
