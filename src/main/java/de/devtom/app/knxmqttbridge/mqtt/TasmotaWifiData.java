package de.devtom.app.knxmqttbridge.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TasmotaWifiData {
	@JsonProperty("AP")
	private int ap;
	@JsonProperty("SSId")
	private String ssid;
	@JsonProperty("BSSId")
	private String bssId;
	@JsonProperty("Channel")
	private int channel;
	@JsonProperty("RSSI")
	private int rssi;
	@JsonProperty("LinkCount")
	private int linkCount;
	@JsonProperty("Downtime")
	private String downtime;
	public int getAp() {
		return ap;
	}
	public String getSsid() {
		return ssid;
	}
	public String getBssId() {
		return bssId;
	}
	public int getChannel() {
		return channel;
	}
	public int getRssi() {
		return rssi;
	}
	public int getLinkCount() {
		return linkCount;
	}
	public String getDowntime() {
		return downtime;
	}
	
}
