package org.psicover.altimeter.io;

import java.io.File;
import java.io.IOException;

import javax.swing.table.TableModel;

import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.bean.AltimeterTableModel;

public class OdsFileWriter implements IExportDataAdapter {

	private static IExportDataAdapter instance;
	public static IExportDataAdapter getInstance() {
		if(instance == null)
			instance = new OdsFileWriter();
		return instance;
	}
	private OdsFileWriter() {
	}
	
	// FIXME it has a lot of performance issues. Maybe a streaming solution like SXSSF

	public void write(AltimeterFile data, AltimeterSession ignore, File outputFile) throws AltimeterIOException {
		try {
			// Save the data to an ODS file and open it.
			AltimeterSession[] sessions = data.getSessions();
			SpreadSheet spreadsheet = SpreadSheet.create(sessions.length, 1, 1);
			
			for(int i = 0; i < sessions.length; i++) {
				String title = String.format("Session %d", i+1);
				TableModel tableModel = new AltimeterTableModel(sessions[i]);
				Sheet sheet = spreadsheet.getSheet(i);
				sheet.setName(title);
				sheet.merge(tableModel, 0, 0, true);
			}
			
			spreadsheet.saveAs(outputFile);
		} catch (IOException e) {
			throw new AltimeterIOException(e);
		}
	}

}
