package org.psicover.altimeter;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

public class Preferences implements Serializable {

	private static final long serialVersionUID = -4165253709603991239L;
	private static final Preferences instance;
	static {

		Preferences prefs = new Preferences();
		try (XMLDecoder dec = new XMLDecoder(new FileInputStream("preferences.xml"))) {
			prefs = (Preferences) dec.readObject();
		} catch (Throwable t) {
		}

		instance = prefs;
	}

	public static Preferences getInstance() {
		return instance;
	}

	public static void savePreferences() {
		try (XMLEncoder enc = new XMLEncoder(new FileOutputStream("preferences.xml"))) {
			enc.writeObject(instance);
		} catch (Throwable t) {
		}
	}

	// constants used in physics module
	private double seaLevelPressure = 101325;
	private double P0 = 44330.0D;
	private double R = 287.058;
	private String altitudeFormula = "wiki";

	// smoothing
	private int smoothWindowSize = 4;

	// flight detection
	private double launchDelta = 5.0; // 5 meters is launch
	private double landingDelta = 0.2; // 0.2 meters from initial launch height
	private int flightWindowSize = 0; // 0 is rate.samplesPerSecond();
	
	private String altRawColor;
	private String altSmoothColor;
	private String flightColor;
	private String tempRawColor;
	private String tempSmootColor;

	private Preferences() {

	}

	public double getSeaLevelPressure() {
		return seaLevelPressure;
	}

	public void setSeaLevelPressure(double seaLevelPressure) {
		this.seaLevelPressure = seaLevelPressure;
	}

	public double getP0() {
		return P0;
	}

	public void setP0(double p0) {
		P0 = p0;
	}

	public double getR() {
		return R;
	}

	public void setR(double r) {
		R = r;
	}

	public String getAltitudeFormula() {
		return altitudeFormula;
	}

	public void setAltitudeFormula(String altitudeFormula) {
		this.altitudeFormula = altitudeFormula;
	}

	public int getSmoothWindowSize() {
		return smoothWindowSize;
	}

	public void setSmoothWindowSize(int smoothWindowSize) {
		this.smoothWindowSize = smoothWindowSize;
	}

	public String getAltRawColor() {
		return altRawColor;
	}

	public void setAltRawColor(String altRawColor) {
		this.altRawColor = altRawColor;
	}

	public String getAltSmoothColor() {
		return altSmoothColor;
	}

	public void setAltSmoothColor(String altSmoothColor) {
		this.altSmoothColor = altSmoothColor;
	}

	public String getFlightColor() {
		return flightColor;
	}

	public void setFlightColor(String flightColor) {
		this.flightColor = flightColor;
	}

	public String getTempRawColor() {
		return tempRawColor;
	}

	public void setTempRawColor(String tempRawColor) {
		this.tempRawColor = tempRawColor;
	}

	public String getTempSmootColor() {
		return tempSmootColor;
	}

	public void setTempSmootColor(String tempSmootColor) {
		this.tempSmootColor = tempSmootColor;
	}

	public double getLaunchDelta() {
		return launchDelta;
	}

	public void setLaunchDelta(double launchDelta) {
		this.launchDelta = launchDelta;
	}

	public double getLandingDelta() {
		return landingDelta;
	}

	public void setLandingDelta(double landingDelta) {
		this.landingDelta = landingDelta;
	}

	public int getFlightWindowSize() {
		return flightWindowSize;
	}

	public void setFlightWindowSize(int flightWindowSize) {
		this.flightWindowSize = flightWindowSize;
	}

}
