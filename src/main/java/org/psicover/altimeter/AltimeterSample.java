package org.psicover.altimeter;

public class AltimeterSample {
	private byte temperature;
	private int pressure;
	private boolean altitudeCalc=false;
	private double altitude;

	public AltimeterSample() {
	}

	public AltimeterSample(byte [] data) {
		this(data[0], data[1], data[2], data[3]);
	}
	public AltimeterSample(byte temp, byte pressh, byte pressm, byte pressl) {
		this.temperature = temp;
		this.pressure = (pressh&0xff);
		this.pressure = (this.pressure << 8)|(pressm&0xff);
		this.pressure = (this.pressure << 8)|(pressl&0xff);
		getAltitude();
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
			altitude = Physics.wikipediaAltitude(getPressure(), getTemperature());
		}
		return altitude;
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append("T=").append(getTemperature()).append(" C; P=").append(getPressure()).append(" Pa")
				.toString();
	}
}
