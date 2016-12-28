package org.psicover.altimeter.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.ui.AltimeterIOException;

public class DlmFileWriter implements IExportDataAdapter {
	private static IExportDataAdapter tsvInstance;
	private static IExportDataAdapter csvInstance;
	private static IExportDataAdapter ssvInstance;
	public static IExportDataAdapter getTsvInstance() {
		if(tsvInstance == null)
			tsvInstance = new DlmFileWriter('\t');
		return tsvInstance;
	}
	public static IExportDataAdapter getCsvInstance() {
		if(csvInstance == null)
			csvInstance = new DlmFileWriter(',');
		return csvInstance;
	}
	public static IExportDataAdapter getSsvInstance() {
		if(ssvInstance == null)
			ssvInstance = new DlmFileWriter(';');
		return ssvInstance;
	}
	
	private final char delimiter;
	private DlmFileWriter(char delimiter) {
		this.delimiter = delimiter;
	}
	
	public void write(AltimeterFile ignore, AltimeterSession session, File selectedFile) throws AltimeterIOException {
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
