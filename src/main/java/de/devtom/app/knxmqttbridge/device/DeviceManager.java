package de.devtom.app.knxmqttbridge.device;

public interface DeviceManager {
	void switchService(String serviceIdentifier, boolean switchOn);
	void updateServiceState(String serviceIdentifier, boolean switchedOn);
}
