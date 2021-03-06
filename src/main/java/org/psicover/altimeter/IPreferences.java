package org.psicover.altimeter;

public interface IPreferences {

	double getSeaLevelPressure();

	String getAltitudeFormula();

	int getSmoothWindowSize();

	String getAltRawColor();

	String getAltSmoothColor();

	String getFlightColor();

	String getTempRawColor();

	String getTempSmootColor();

	double getLaunchDelta();

	double getLandingDelta();

	int getFlightWindowSize();

	String getFlightDetectionDataset();
	
	double getSeaLevelTemperature();

	int getCalibrationTime();
}