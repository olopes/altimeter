package org.psicover.altimeter.bean;

import java.io.Serializable;

public enum SampleRate implements Serializable {
	RATE_1HZ,
	RATE_2HZ,
	RATE_4HZ,
	RATE_8HZ,
;
	public static SampleRate findRate(int n) {
		SampleRate[] values = values();
		if(n < 0 || n >= values.length)
			return null;
		return values[n];
	}
	
	public int samplesPerSecond() {
		return (1<<ordinal());
	}
	
	public double duration(double numSamples) {
		return (numSamples/samplesPerSecond());
	}
	
}
