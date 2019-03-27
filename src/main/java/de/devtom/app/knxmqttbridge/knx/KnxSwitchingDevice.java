package de.devtom.app.knxmqttbridge.knx;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.device.BaseKnxDevice;
import tuwien.auto.calimero.device.ios.KNXPropertyException;
import tuwien.auto.calimero.link.KNXLinkClosedException;
import tuwien.auto.calimero.link.KNXNetworkLink;

public class KnxSwitchingDevice {
	private String deviceName;
	private IndividualAddress individualAddress;
	private KnxSwitchingService knxSwitchingService;
	
	public KnxSwitchingDevice(String deviceName, IndividualAddress individualAddress, KnxSwitchingService knxSwitchingService) {
		this.deviceName = deviceName;
		this.individualAddress = individualAddress;
		this.knxSwitchingService = knxSwitchingService;
	}
	
	public BaseKnxDevice connect(KNXNetworkLink knxLink) throws KNXLinkClosedException, KNXPropertyException, InterruptedException {
		final BaseKnxDevice device = new BaseKnxDevice(this.deviceName, this.individualAddress, knxLink, this.knxSwitchingService);
		
		return device;
	}
}
