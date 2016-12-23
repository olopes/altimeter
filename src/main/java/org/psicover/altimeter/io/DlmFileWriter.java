package org.psicover.altimeter.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.ui.AltimeterIOException;

public class DlmFileWriter {

	public static void writeCsv(AltimeterSession session, File selectedFile) throws AltimeterIOException {
		writeDlm(session, selectedFile, ',');
	}
	
	public static void writeSsv(AltimeterSession session, File selectedFile) throws AltimeterIOException {
		writeDlm(session, selectedFile, ';');
	}
	
	public static void writeTsv(AltimeterSession session, File selectedFile) throws AltimeterIOException {
		writeDlm(session, selectedFile, '\t');
	}
	
	public static void write(AltimeterSession session, File selectedFile) throws AltimeterIOException {
		writeDlm(session, selectedFile, '\t');
	}
	
	public static void writeDlm(AltimeterSession session, File selectedFile, char delimiter) throws AltimeterIOException {
		try (PrintWriter out = new PrintWriter(new FileWriter(selectedFile))){
			out.printf("TIME%1$cPRESSURE%1$cTEMPERATURE%1$cALTITUDE%n", delimiter);
			long tuIncr = 1000/session.getRate().samplesPerSecond(); // millis per sample

			long time = 0;
			for(AltimeterSample sample : session.getData()) {
				out.printf(Locale.ENGLISH,"%2$tH:%2$tM:%2$tS.%2$tL%1$c%3$d%1$c%4$d%1$c%5$.4f%n", delimiter, time, sample.getPressure(), sample.getTemperature(), sample.getAltitude());
				time+=tuIncr;
			}
		} catch (IOException e) {
			throw new AltimeterIOException(e);
		}

	}
	
}
