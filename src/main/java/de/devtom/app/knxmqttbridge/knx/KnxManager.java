package de.devtom.app.knxmqttbridge.knx;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.devtom.app.knxmqttbridge.device.BridgeConfiguration;
import de.devtom.app.knxmqttbridge.device.DeviceConfiguration;
import de.devtom.app.knxmqttbridge.device.DeviceManager;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.device.BaseKnxDevice;
import tuwien.auto.calimero.device.ios.KNXPropertyException;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.link.KNXLinkClosedException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

@Component
public class KnxManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(KnxManager.class);

	@Autowired
	private BridgeConfiguration config;
	@Autowired
	private DeviceManager deviceManager;

	private KNXNetworkLink knxLink = null;

	@PostConstruct
	public boolean connect() {
		InetSocketAddress local = new InetSocketAddress("192.168.178.22", 0);
		InetSocketAddress server = new InetSocketAddress(config.getKnxRemoteHost(), KNXnetIPConnection.DEFAULT_PORT);;
		
		try {
			knxLink = new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNELING, local, server, false, TPSettings.TP1);
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Connected to KNX: {}", knxLink.getName());
			}
			
			this.initDevices();
			
			return true;
		} catch (KNXException e) {
			LOGGER.error("KNX exception: ", e);
			disconnect();
		} catch (InterruptedException e) {
			LOGGER.error("Interrupted: ", e);
			disconnect();
		}
		
		return false;
	}
	
	private void initDevices() {
		for(DeviceConfiguration device : config.getDeviceConfigurations()) {
			try {
				GroupAddress switchingGa = new GroupAddress(device.getKnxSwitchingGroupAddresses().get(0));
				GroupAddress listeningGa = new GroupAddress(device.getKnxListeningGroupAddresses().get(0));
				KnxSwitchingService knxSwitchingService = new KnxSwitchingService(switchingGa , listeningGa);
				
				IndividualAddress individualAddress = new IndividualAddress(device.getKnxIndividualAddress());
				final BaseKnxDevice knxDevice = new BaseKnxDevice(device.getName(), individualAddress, knxLink, knxSwitchingService);
				this.deviceManager.addKnxDevice(knxDevice);
			} catch(KNXFormatException e) {
				LOGGER.error("Could not add KNX group address", e);
			} catch (KNXLinkClosedException e) {
				LOGGER.error("KNX link closed exception: ", e);
			} catch (KNXPropertyException e) {
				LOGGER.error("KNX property exception: ", e);
			}
		}
	}
	
	public void disconnect() {
		if (knxLink != null) {
			knxLink.close();
		}
	}
}
