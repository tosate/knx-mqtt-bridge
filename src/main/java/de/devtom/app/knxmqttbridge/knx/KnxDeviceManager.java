package de.devtom.app.knxmqttbridge.knx;
 
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.devtom.app.knxmqttbridge.device.DeviceConfiguration;
import de.devtom.app.knxmqttbridge.device.DeviceManager;
import de.devtom.app.knxmqttbridge.device.ServiceRegistry;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.device.BaseKnxDevice;
import tuwien.auto.calimero.device.ios.KNXPropertyException;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXTimeoutException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.link.KNXLinkClosedException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;

public class KnxDeviceManager implements DeviceManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(KnxDeviceManager.class);
	
	private ServiceRegistry serviceRegistry;
	private String knxRemoteHost;
	private List<BaseKnxDevice> knxDevices = new ArrayList<>();

	private KNXNetworkLink knxLink = null;
	private ProcessCommunicator processCommunicator = null;
	private Map<GroupAddress, KnxSwitchingService> gaToKnxSwitchingService = new HashMap<>();

	public KnxDeviceManager(ServiceRegistry serviceRegistry, String knxRemoteHost) {
		this.serviceRegistry = serviceRegistry;
		this.knxRemoteHost = knxRemoteHost;
	}
	public boolean connect() {
		// TODO remove hard coded IP
		InetSocketAddress local = new InetSocketAddress("192.168.178.22", 0);
		InetSocketAddress server = new InetSocketAddress(knxRemoteHost, KNXnetIPConnection.DEFAULT_PORT);;
		
		try {
			knxLink = new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNELING, local, server, false, TPSettings.TP1);
			processCommunicator = new ProcessCommunicatorImpl(knxLink);
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Connected to KNX: {}", knxLink.getName());
			}
			
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
	
	public void initDevices(List<DeviceConfiguration> deviceConfigurations) {
		for(DeviceConfiguration device : deviceConfigurations) {
			try {
				List<GroupAddress> switchingGaList = new ArrayList<>();
				List<GroupAddress> listeningGaList = new ArrayList<>();
				
				for(String ga : device.getKnxSwitchingGroupAddresses()) {
					switchingGaList.add(new GroupAddress(ga));
				}
				
				for(String ga : device.getKnxListeningGroupAddresses()) {
					listeningGaList.add(new GroupAddress(ga));
				}
				
				KnxSwitchingService knxSwitchingService = new KnxSwitchingService(switchingGaList , listeningGaList, this);
				for(GroupAddress listeningGa : listeningGaList) {
					this.gaToKnxSwitchingService.put(listeningGa, knxSwitchingService);
				}
				
				IndividualAddress individualAddress = new IndividualAddress(device.getKnxIndividualAddress());
				final BaseKnxDevice knxDevice = new BaseKnxDevice(device.getName(), individualAddress, knxLink, knxSwitchingService);
				this.knxDevices.add(knxDevice);
			} catch(KNXFormatException e) {
				LOGGER.error("Could not add KNX group address", e);
			} catch (KNXLinkClosedException e) {
				LOGGER.error("KNX link closed exception: ", e);
			} catch (KNXPropertyException e) {
				LOGGER.error("KNX property exception: ", e);
			}
		}
	}
	
	@Override
	public void switchService(String serviceIdentifier, boolean switchOn) {
		// Send message to service registry
		serviceRegistry.switchService(serviceIdentifier, switchOn);
	}
	

	@Override
	public void updateServiceState(String serviceIdentifier, boolean switchedOn) {
		// send status to KNX
		try {
			GroupAddress ga = new GroupAddress(serviceIdentifier);
			if(!this.gaToKnxSwitchingService.containsKey(ga)) {
				LOGGER.error("No KNX service for group address {}!", ga.toString());
				return;
			}
			// publish KNX message to group Address
			this.processCommunicator.write(ga, switchedOn);
		} catch (KNXFormatException e) {
			LOGGER.error("KNX format exception: ", e);
		} catch (KNXTimeoutException e) {
			LOGGER.error("KNX timeout exception: ", e);
		} catch (KNXLinkClosedException e) {
			LOGGER.error("KNX link closed exception: ", e);
		}
		
	}

	public void disconnect() {
		if(processCommunicator != null) {
			processCommunicator.detach();
		}
		
		if (knxLink != null) {
			knxLink.close();
		}
	}
}
