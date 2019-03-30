package de.devtom.app.knxmqttbridge.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import de.devtom.app.knxmqttbridge.config.KnxMqttBridgeConfiguration.MqttGateway;
import de.devtom.app.knxmqttbridge.device.ServiceRegistry;

@Component
public class TasmotaMqttMessageHandler extends AbstractMessageHandler {
	private static final String MQTT_DELIMITER = "/";
	private static final Logger LOGGER = LoggerFactory.getLogger(TasmotaMqttMessageHandler.class);
	private static final String MQTT_RECEIVED_TOPIC_KEY = "mqtt_receivedTopic";
	
	@Autowired
	private ServiceRegistry serviceRegistry;
	@Autowired
	private MqttGateway mqttGateway;
	
	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		if(message instanceof Throwable) {
			Throwable throwable = (Throwable)message;
			throw new Exception(throwable);
		}
		
		TasmotaMqttData tasmotaMqttData = new TasmotaMqttData();
		this.processHeaders(message.getHeaders(), tasmotaMqttData);
		this.processPayload(message.getPayload(), tasmotaMqttData);
		
		this.serviceRegistry.getMqttDeviceManager().processMqttData(tasmotaMqttData);
	}
	
	private void processPayload(Object payload, TasmotaMqttData tasmotaMqttData) {
		if(payload instanceof String) {
			String payloadStr = (String)payload;
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Payload {} received", payloadStr);
			}
			tasmotaMqttData.setMqttPayload(payloadStr);
		} else {
			LOGGER.error("Unknown payload type");
		}
		
	}

	private void processHeaders(MessageHeaders headers, TasmotaMqttData tasmotaMqttData) {
		if(headers.containsKey(MQTT_RECEIVED_TOPIC_KEY)) {
			String fullTopic = (String) headers.get(MQTT_RECEIVED_TOPIC_KEY);
			if(LOGGER.isDebugEnabled()) {
				LOGGER.info("MQTT topic {} received", fullTopic);
			}
			
			String[] topicElements = fullTopic.split(MQTT_DELIMITER);
			if(topicElements.length == 3) {
				tasmotaMqttData.setMqttPrefix(topicElements[0]);
				tasmotaMqttData.setMqttTopic(topicElements[1]);
				tasmotaMqttData.setMqttTopicSuffix(topicElements[2]);
			} else {
				LOGGER.error("Unknown topic structure: {}", fullTopic);
			}
		}
	}

	public void sendMqttMessage(String topic, boolean switchOn) {
		String message = switchOn ? "ON" : "OFF";
		this.mqttGateway.send(topic, message);
	}
}
