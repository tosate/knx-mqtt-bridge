package de.devtom.app.knxmqttbridge.mqtt;

public enum TasmotaMqttPrefix {
	COMMAND("cmnd"),
	STATUS("stat"),
	TELEMETRY("tele");
	
	private final String value;
	
	private TasmotaMqttPrefix(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static TasmotaMqttPrefix fromValue(String value) throws IllegalArgumentException {
		if(value == null) {
			throw new IllegalArgumentException("Null value argument provided!");
		}
		
		for(TasmotaMqttPrefix prefix : TasmotaMqttPrefix.values()) {
			if(prefix.getValue().equalsIgnoreCase(value)) {
				return prefix;
			}
		}
		
		throw new IllegalArgumentException("No prefix found for: " + value);
	}
}
