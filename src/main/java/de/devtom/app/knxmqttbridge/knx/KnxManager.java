package de.devtom.app.knxmqttbridge.knx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.devtom.app.knxmqttbridge.device.BridgeConfiguration;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;

@Component
public class KnxManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(KnxManager.class);

	@Autowired
	private BridgeConfiguration config;

	private KNXNetworkLink knxLink = null;
	private ProcessCommunicator pc = null;

	public boolean connect() {
		try {
			knxLink = new KNXNetworkLinkIP(config.getKnxRemoteHost(), TPSettings.TP1);
			
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
	
	public void writeToKnx(GroupAddress dp, String value) {
		try {
			pc.write(dp, value);
		} catch (KNXException e) {
			LOGGER.error("KNX exception: ", e);
			disconnect();
		}
	}
	
	public void disconnect() {
		if (pc != null) {
			pc.detach();
		}

		if (knxLink != null) {
			knxLink.close();
		}
	}
}
