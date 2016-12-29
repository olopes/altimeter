package org.psicover.altimeter;

import java.io.File;

import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.io.AltimeterFileReader;
import org.psicover.altimeter.io.DlmFileWriter;
import org.psicover.altimeter.io.IExportDataAdapter;
import org.psicover.altimeter.io.OdsFileWriter;
import org.psicover.altimeter.io.XlsxFileWriter;
import org.psicover.altimeter.ui.AltimeterIOException;

public class ExportData {
	
	public static enum OutputFormat {
		xlsx(XlsxFileWriter.getInstance(),"Excel spreadsheet (default)"),
		ods(OdsFileWriter.getInstance(),"ODF Spreadsheet"),
		tsv(DlmFileWriter.getTsvInstance(),"Tab Delimited Values - '\\t'"),
		tab(DlmFileWriter.getTsvInstance(),"Tab Delimited Values - '\\t'"),
		csv(DlmFileWriter.getCsvInstance(),"Comma Delimited Values - ','"),
		ssv(DlmFileWriter.getSsvInstance(),"Semicolon Delimited Values - ';'"),
;
		private final IExportDataAdapter adapter;
		private final String description;
		private OutputFormat(IExportDataAdapter adapter, String description) {
			this.description=description;
			this.adapter=adapter;
		}
		public IExportDataAdapter getAdapter() {
			return adapter;
		}
		
		public String getDescription() {
			return description;
		}
	}

	public static void printFormats() {
		System.out.println("Available formats:");
		for(OutputFormat f : OutputFormat.values())
			System.out.println("  "+f.name()+" - "+f.getDescription());
	}
	
	public static void export(String format, String inFile, String sessionNum, String outFile) {
		OutputFormat fmt = null;
		try {
			fmt = OutputFormat.valueOf(format);
		} catch(Throwable t) {
			System.out.println("Unknown format '"+fmt+"'");
			printFormats();
			return;
		}
		
		File in = new File(inFile);
		if(!(in.exists() && in.canRead() && in.isFile())) {
			System.out.println("Input file not found '"+inFile+"'");
			return;
		}
		
		AltimeterFile data = null;
		try {
			data = AltimeterFileReader.readFile(in);
		} catch (AltimeterIOException e) {
			System.out.println("Error reading input file: "+e.getMessage());
			return;
		}
		
		File ou = new File(outFile);

		try {
			fmt.getAdapter().write(data, data.getSessions()[Integer.parseInt(sessionNum)], ou);
		} catch (AltimeterIOException e) {
			System.out.println("Error writing to output file: "+e.getMessage());
		} catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {
			System.out.println("Invalid session number: "+sessionNum);
			System.out.println("List of available sessions found:");
			AltimeterSession [] sessions = data.getSessions();
			for(int i = 0; i < sessions.length; i++)
				System.out.println("  Session "+i+" - duration "+sessions[i].getSessionDuration());
		}
	}

}
