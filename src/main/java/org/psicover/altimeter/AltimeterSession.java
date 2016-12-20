package org.psicover.altimeter;

public class AltimeterSession {

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

}
