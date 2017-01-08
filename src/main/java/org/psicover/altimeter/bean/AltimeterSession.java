package org.psicover.altimeter.bean;

import java.io.Serializable;

public class AltimeterSession implements Serializable {
	private static final long serialVersionUID = 7568228676336430042L;
	
	private AltimeterSample[] data;
	private SampleRate rate;
	private int minPa = Integer.MAX_VALUE;
	private int maxPa = Integer.MIN_VALUE;
	private int minTemp = Integer.MAX_VALUE;
	private int maxTemp = Integer.MIN_VALUE;
	private double minAlt = Double.MAX_VALUE;
	private double maxAlt = Double.MIN_VALUE;

	public AltimeterSession() {
	}

	public AltimeterSample[] getData() {
		return data;
	}

	public void setData(AltimeterSample[] data) {
		this.data = data;
	}

	public SampleRate getRate() {
		return rate;
	}

	public void setRate(SampleRate rate) {
		this.rate = rate;
	}
	
	public String getSessionDuration() {
		int duration = (int) rate.duration(data.length);
		int hours = duration/3600;
		int durm = duration%3600;
		int minutes = durm/60;
		int seconds = durm%60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	public void setLimits(int minPa, int maxPa, int minTemp, int maxTemp, double minAlt, double maxAlt) {
		this.minPa = minPa;
		this.maxPa = maxPa;
		this.minTemp = minTemp;
		this.maxTemp = maxTemp;
		this.minAlt = minAlt;
		this.maxAlt = maxAlt;
	}
	
	public int getMinPa() {
		return minPa;
	}

	public void setMinPa(int minPa) {
		this.minPa = minPa;
	}

	public int getMaxPa() {
		return maxPa;
	}

	public void setMaxPa(int maxPa) {
		this.maxPa = maxPa;
	}

	public int getMinTemp() {
		return minTemp;
	}

	public void setMinTemp(int minTemp) {
		this.minTemp = minTemp;
	}

	public int getMaxTemp() {
		return maxTemp;
	}

	public void setMaxTemp(int maxTemp) {
		this.maxTemp = maxTemp;
	}

	public double getMinAlt() {
		return minAlt;
	}

	public void setMinAlt(double minAlt) {
		this.minAlt = minAlt;
	}

	public double getMaxAlt() {
		return maxAlt;
	}

	public void setMaxAlt(double maxAlt) {
		this.maxAlt = maxAlt;
	}

	public double getDuration() {
		return rate.duration(data.length);
	}
	
}
