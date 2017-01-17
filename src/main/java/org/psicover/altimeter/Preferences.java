package org.psicover.altimeter;

import java.io.File;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public final class Preferences implements Serializable, IPreferences {
	
	private static final Logger logger = LogFactory.getLogger(Preferences.class);
	private static final long serialVersionUID = -4165253709603991239L;
	private static final String PREFERENCES_FILE ="preferences.xml";
	private static final IPreferences instance;
	static {
		IPreferences prefs = new Preferences();
		File f = new File(PREFERENCES_FILE);

		if(f.exists()) {
			try {
				JAXBContext jc = JAXBContext.newInstance( Preferences.class );
				Unmarshaller u = jc.createUnmarshaller();
				prefs = (IPreferences) u.unmarshal(new File(PREFERENCES_FILE));
			} catch (Throwable t) {
				logger.log(Level.SEVERE, "Could not read preferences file", t);
			}
		}

		instance = prefs;
	}

	public static IPreferences getInstance() {
		return instance;
	}

	public static void savePreferences() {
		try {
			JAXBContext jc = JAXBContext.newInstance( Preferences.class );
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(instance, new File(PREFERENCES_FILE));
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Could not write preferences file", t);
		}
	}

	// constants used in physics module
	private double seaLevelPressure = 101325;
	private double R = 287.058;
	private String altitudeFormula = "wiki";

	// smoothing
	private int smoothWindowSize = 4;

	// flight detection
	private double launchDelta = 5.0; // 5 meters is launch
	private double landingDelta = 0.2; // 0.2 meters from initial launch height
	private int flightWindowSize = 0; // 0 is rate.samplesPerSecond();
	private String flightDetectionDataset = "raw"; // or smooth

	private String altRawColor;
	private String altSmoothColor;
	private String flightColor;
	private String tempRawColor;
	private String tempSmootColor;

	private Preferences() {

	}

	@Override
	public double getSeaLevelPressure() {
		return seaLevelPressure;
	}

	public void setSeaLevelPressure(double seaLevelPressure) {
		this.seaLevelPressure = seaLevelPressure;
	}

	@Override
	public double getR() {
		return R;
	}

	public void setR(double r) {
		R = r;
	}

	@Override
	public String getAltitudeFormula() {
		return altitudeFormula;
	}

	public void setAltitudeFormula(String altitudeFormula) {
		this.altitudeFormula = altitudeFormula;
	}

	@Override
	public int getSmoothWindowSize() {
		return smoothWindowSize;
	}

	public void setSmoothWindowSize(int smoothWindowSize) {
		this.smoothWindowSize = smoothWindowSize;
	}

	@Override
	public String getAltRawColor() {
		return altRawColor;
	}

	public void setAltRawColor(String altRawColor) {
		this.altRawColor = altRawColor;
	}

	@Override
	public String getAltSmoothColor() {
		return altSmoothColor;
	}

	public void setAltSmoothColor(String altSmoothColor) {
		this.altSmoothColor = altSmoothColor;
	}

	@Override
	public String getFlightColor() {
		return flightColor;
	}

	public void setFlightColor(String flightColor) {
		this.flightColor = flightColor;
	}

	@Override
	public String getTempRawColor() {
		return tempRawColor;
	}

	public void setTempRawColor(String tempRawColor) {
		this.tempRawColor = tempRawColor;
	}

	@Override
	public String getTempSmootColor() {
		return tempSmootColor;
	}

	public void setTempSmootColor(String tempSmootColor) {
		this.tempSmootColor = tempSmootColor;
	}

	@Override
	public double getLaunchDelta() {
		return launchDelta;
	}

	public void setLaunchDelta(double launchDelta) {
		this.launchDelta = launchDelta;
	}

	@Override
	public double getLandingDelta() {
		return landingDelta;
	}

	public void setLandingDelta(double landingDelta) {
		this.landingDelta = landingDelta;
	}

	@Override
	public int getFlightWindowSize() {
		return flightWindowSize;
	}

	public void setFlightWindowSize(int flightWindowSize) {
		this.flightWindowSize = flightWindowSize;
	}

	@Override
	public String getFlightDetectionDataset() {
		return flightDetectionDataset;
	}

	public void setFlightDetectionDataset(String flightDetectionDataset) {
		this.flightDetectionDataset = flightDetectionDataset;
	}

}
