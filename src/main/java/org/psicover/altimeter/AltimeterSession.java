package org.psicover.altimeter;

import java.util.List;

public class AltimeterSession {

	private List<AltimeterSample> data;
	private SampleRate rate;

	public AltimeterSession() {
	}

	public List<AltimeterSample> getData() {
		return data;
	}

	public void setData(List<AltimeterSample> data) {
		this.data = data;
	}

	public SampleRate getRate() {
		return rate;
	}

	public void setRate(SampleRate rate) {
		this.rate = rate;
	}

}
