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
		try(XMLDecoder dec = new XMLDecoder(new FileInputStream("preferences.xml"))) {
			prefs = (Preferences)dec.readObject();
		} catch(Throwable t) {}
		
		instance = prefs;
	}
	
	public static Preferences getInstance() {
		return instance;
	}
	
	public static void savePreferences() {
		try(XMLEncoder enc = new XMLEncoder(new FileOutputStream("preferences.xml"))) {
			enc.writeObject(instance);
		} catch(Throwable t) {}
	}
	
	// constants used in physics module
	private double sea_press = 101325;
	private double P0 = 44330.0D;
	private double R=287.058;
	private double g=9.80665;
	private String altitudeFormula="wiki";

	private String series1Color;
	private String series2Color;
	private String series3Color;
	private String series4Color;
	
	private Preferences() {
		
	}
	

}
