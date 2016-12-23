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

}
