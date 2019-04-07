package de.devtom.app.knxmqttbridge.knx;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KnxSceneConfiguration {
	@JsonProperty("scene-number")
	private int sceneNumber;
	@JsonProperty("switched-on")
	private boolean switchedOn;
	
	public int getSceneNumber() {
		return sceneNumber;
	}
	public boolean isSwitchedOn() {
		return switchedOn;
	}
}
