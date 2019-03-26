package de.devtom.app.knxmqttbridge.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TasmotaEnergyData {
	@JsonProperty("TotalStartTime")
	private String totalStartTime;
	@JsonProperty("Total")
	private float total;
	@JsonProperty("Yesterday")
	private float yesterday;
	@JsonProperty("Today")
	private float today;
	@JsonProperty("Period")
	private int period;
	@JsonProperty("Power")
	private int power;
	@JsonProperty("ApparentPower")
	private int apparentPower;
	@JsonProperty("ReactivePower")
	private int reactivePower;
	@JsonProperty("Factor")
	private float factor;
	@JsonProperty("Voltage")
	private int voltage;
	@JsonProperty("Current")
	private float current;
	public String getTotalStartTime() {
		return totalStartTime;
	}
	public float getTotal() {
		return total;
	}
	public float getYesterday() {
		return yesterday;
	}
	public float getToday() {
		return today;
	}
	public int getPeriod() {
		return period;
	}
	public int getPower() {
		return power;
	}
	public int getApparentPower() {
		return apparentPower;
	}
	public int getReactivePower() {
		return reactivePower;
	}
	public float getFactor() {
		return factor;
	}
	public int getVoltage() {
		return voltage;
	}
	public float getCurrent() {
		return current;
	}
}
