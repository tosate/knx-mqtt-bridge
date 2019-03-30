package de.devtom.app.knxmqttbridge.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.stream.CharacterStreamReadingMessageSource;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.devtom.app.knxmqttbridge.device.BridgeConfiguration;
import de.devtom.app.knxmqttbridge.device.DeviceConfiguration;
import de.devtom.app.knxmqttbridge.device.ServiceRegistry;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttDevice;
import de.devtom.app.knxmqttbridge.mqtt.TasmotaMqttMessageHandler;

@Configuration
public class KnxMqttBridgeConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(KnxMqttBridgeConfiguration.class);
	
	@Value("${mqtt.defaultTopic}")
	private String mqttDefaultTopic;
	@Value("${mqtt.publisher.clientId}")
	private String mqttPublisherClientId;
	@Value("${mqtt.consumer.clientId}")
	private String mqttConsumerClientId;
	@Value("${config.file}")
	private String configfile;
	
	@Autowired
	private TasmotaMqttMessageHandler tasmotaMqttMessageHandler;
	@Autowired
	private ServiceRegistry serviceRegistry;
	
	@Bean
	public BridgeConfiguration getBridgeConfiguration() {
		try {
			FileInputStream fis = new FileInputStream(this.configfile);
			ObjectMapper objectMapper = new ObjectMapper();
			BridgeConfiguration config = objectMapper.readValue(fis, BridgeConfiguration.class);
			
			return config;
		} catch(JsonParseException e) {
			
		} catch (FileNotFoundException e) {
			LOGGER.error("File not found exception :", e);
		} catch (JsonMappingException e) {
			LOGGER.error("JSON mapping exception :", e);
		} catch (IOException e) {
			LOGGER.error("IO exception :", e);
		}
		
		return null;
	}
	
	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions options = new MqttConnectOptions();
		options.setServerURIs(new String[] { getBridgeConfiguration().getMqttServerUri() });
		options.setUserName(getBridgeConfiguration().getMqttUser());
		options.setPassword(getBridgeConfiguration().getMqttPassword().toCharArray());
		factory.setConnectionOptions(options);
		return factory;
	}
	
	@Bean
	public IntegrationFlow mqttOutFlow() {
		return IntegrationFlows.from(CharacterStreamReadingMessageSource.stdin(),
				e -> e.poller(Pollers.fixedDelay(1000)))
				.transform(p -> p + " sent to MQTT")
				.handle(mqttOutbound())
				.get();
	}
	
	@Bean
	@ServiceActivator(inputChannel = "mqttOutboundChannel")
	public MessageHandler mqttOutbound() {
		MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(mqttPublisherClientId, mqttClientFactory());
		messageHandler.setAsync(true);
		messageHandler.setDefaultTopic(mqttDefaultTopic);
		return messageHandler;
	}
	
	@Bean
	public IntegrationFlow mqttInFlow() {
		return IntegrationFlows.from(mqttInbound())
				.transform(p -> p + ", received from MQTT")
				.handle(tasmotaMqttMessageHandler)
				.get();
	}

	@Bean
	public MessageProducerSupport mqttInbound() {
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(mqttConsumerClientId, mqttClientFactory(), mqttDefaultTopic);
		adapter.setCompletionTimeout(5000);
		adapter.setConverter(new DefaultPahoMessageConverter());
		adapter.setQos(1);
		
		for(DeviceConfiguration device : getBridgeConfiguration().getDeviceConfigurations()) {
			TasmotaMqttDevice mqttDevice = new TasmotaMqttDevice(device.getMqttTopic());
			adapter.addTopic(mqttDevice.getMqttCommandTopics(), mqttDevice.getMqttTelemetryTopics(), mqttDevice.getMqttStatusTopics());
			this.serviceRegistry.getMqttDeviceManager().addMqttDevice(mqttDevice);
		}
		return adapter;
	}
	
	@Bean
	public MessageChannel mqttOutboundChannel() {
		return new DirectChannel();
	}
	
	@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
	public interface MqttGateway {
		void send(@Header(MqttHeaders.TOPIC) String topic, String out);
	}
}
