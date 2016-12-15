package org.psicover.altimeter;

public class Physics {

	public static double altitute(AltimeterSample altimeterSample) {

		return 0;
	}
	
	// from https://forum.arduino.cc/index.php?topic=63726.0
	// and http://keisan.casio.com/exec/system/1224585971
	// this one takes in account mean temperature
	private static final double sea_press = 101325;
	public static double keisanAltitude(int pressure, int temp) {
		return ((Math.pow((sea_press / pressure), 1/5.257) - 1.0) * (temp + 273.15)) / 0.0065;
	}
	
	// this one works best in Hong Kong or maybe Shenzen
	private static double P0 = 44330.0D;
	public static final double Pa2M(int pressure)
	{
		return P0 * (1.0D - Math.pow(pressure / 101325.0D, 0.19029495718363465D));
	}
	
	// https://en.wikipedia.org/wiki/Hypsometric_equation
	private static final double R=287.058;
	private static final double g=9.80665;
	public static final double wikipediaAltitude(int pressure, int temp) {
		return (R*(temp + 273.15)/g)*Math.log(sea_press / pressure);
	}

}
