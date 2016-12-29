package org.psicover.altimeter.bean;

import java.io.Serializable;

public class AltimeterSession implements Serializable {
	private static final long serialVersionUID = 7568228676336430042L;
	
	private AltimeterSample[] data;
	private SampleRate rate;

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
		int duration = rate.duration(data.length);
		int hours = duration/3600;
		int durm = duration%3600;
		int minutes = durm/60;
		int seconds = durm%60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}
