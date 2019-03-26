package de.devtom.app.knxmqttbridge.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TasmotaStateData {
	@JsonProperty("Time")
	private String time;
	@JsonProperty("Uptime")
	private String uptime;
	@JsonProperty("Vcc")
	private float vcc;
	@JsonProperty("SleepMode")
	private String sleepMode;
	@JsonProperty("Sleep")
	private int sleep;
	@JsonProperty("LoadAvg")
	private int loadAvg;
	@JsonProperty("POWER")
	private String power;
	@JsonProperty("Wifi")
	private TasmotaWifiData wifi;
	public String getTime() {
		return time;
	}
	public String getUptime() {
		return uptime;
	}
	public float getVcc() {
		return vcc;
	}
	public String getSleepMode() {
		return sleepMode;
	}
	public int getSleep() {
		return sleep;
	}
	public int getLoadAvg() {
		return loadAvg;
	}
	public String getPower() {
		return power;
	}
	public TasmotaWifiData getWifi() {
		return wifi;
	}
}
