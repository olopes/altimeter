package org.psicover.altimeter.bean;

import java.io.Serializable;

import org.psicover.altimeter.Physics;

public class AltimeterSample implements Serializable {
	
	private static final long serialVersionUID = 3486851249061761488L;
	
	private byte temperature;
	private int pressure;
	private float time;
	private transient boolean altitudeCalc;
	private transient double altitude;
	public final transient float x;
	public final transient double y;

	public AltimeterSample(byte [] data, float time) {
		this(data[0], data[1], data[2], data[3], time);
	}
	public AltimeterSample(byte temp, byte pressh, byte pressm, byte pressl, float time) {
		this.temperature = temp;
		this.pressure = (pressh&0xff);
		this.pressure = (this.pressure << 8)|(pressm&0xff);
		this.pressure = (this.pressure << 8)|(pressl&0xff);
		this.x = this.time = time;
		y = getAltitude();
	}

	public byte getTemperature() {
		return temperature;
	}

	public void setTemperature(byte temperature) {
		this.temperature = temperature;
	}

	public int getPressure() {
		return pressure;
	}

	public void setPressure(int pressure) {
		this.pressure = pressure;
	}
	
	public double getAltitude() {
		if(!altitudeCalc) {
			altitude = Physics.altitude(getPressure(), getTemperature());
		}
		return altitude;
	}

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append("T=").append(getTemperature()).append(" C; P=").append(getPressure()).append(" Pa")
				.toString();
	}
}
