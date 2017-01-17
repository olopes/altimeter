package org.psicover.altimeter;

public class Physics {

	
	private static final double sea_press;
	private static double P0;
	private static final double R;
	private static final double g=9.80665;

	public static interface AltitudeAlgorithm {
		double convert(double pressure, int temp);
	}
	
	private static final AltitudeAlgorithm selectedAlgorithm;
	
	public static final AltitudeAlgorithm keisan = new AltitudeAlgorithm() {
		// from https://forum.arduino.cc/index.php?topic=63726.0
		// and http://keisan.casio.com/exec/system/1224585971
		// this one takes in account mean temperature
		public double convert(double pressure, int temp) {
			return ((Math.pow((sea_press / pressure), 1/5.257) - 1.0) * (temp + 273.15)) / 0.0065;
		}
	};
	public static final AltitudeAlgorithm pa2m = new AltitudeAlgorithm() {
		// this one works best in Hong Kong or maybe Shenzen
		public double convert(double pressure, int temp) {
			return P0 * (1.0D - Math.pow(pressure / sea_press, 0.19029495718363465D));
		}
	};
	public static final AltitudeAlgorithm wiki = new AltitudeAlgorithm() {
		// https://en.wikipedia.org/wiki/Hypsometric_equation
		public double convert(double pressure, int temp) {
			return (R*(temp + 273.15)/g)*Math.log(sea_press / pressure);
		}
	};
	
	public static double altitude(double pressure, int temperature) {
		return selectedAlgorithm.convert(pressure, temperature);
	}
	
	static {
		IPreferences pref = Preferences.getInstance();
		String algo = pref.getAltitudeFormula();
		// defaults wiki
		AltitudeAlgorithm selAlgorithm = wiki;
		if("pa2m".equals(algo))
			selAlgorithm=pa2m;
		else if("keisan".equals(algo))
			selAlgorithm=keisan;
		selectedAlgorithm=selAlgorithm;
		
		// other adjustable constants
		sea_press = pref.getSeaLevelPressure();
		P0 = pref.getP0();
		R=pref.getR();
	}

}
