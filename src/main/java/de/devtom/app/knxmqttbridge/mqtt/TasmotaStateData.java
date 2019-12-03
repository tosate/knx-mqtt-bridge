package de.devtom.app.knxmqttbridge.mqtt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class TasmotaStateData {
	@JsonProperty("Time")
	private String time;
	@JsonProperty("Uptime")
	private String uptime;
	@JsonProperty("Vcc")
	private float vcc;
	@JsonProperty("Heap")
	private int heap;
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
	@JsonProperty("UptimeSec")
	private int upTimeSec;
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
	public int getHeap() {
		return heap;
	}
}
