package org.psicover.altimeter.io;

import java.io.File;
import java.io.IOException;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.ui.AltimeterIOException;

public class OdsFileWriter {

	// FIXME it has a lot of performance issues. Maybe a streaming solution like
	// Xlsx?

	private static class AltimeterTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private final AltimeterSession session;
		private final double tuIncr; 

		private AltimeterTableModel(AltimeterSession session) {
			this.session = session;
			tuIncr = 1.0/session.getRate().samplesPerSecond();
		}

		@Override
		public int getRowCount() {
			return this.session.getData().length;
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			AltimeterSample sample = this.session.getData()[rowIndex];
			if (columnIndex == 0)
				return tuIncr * rowIndex;
			else if (columnIndex == 1)
				return sample.getPressure();
			else if (columnIndex == 2)
				return sample.getTemperature();
			else if (columnIndex == 3)
				return sample.getAltitude();
			return null;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "TIME";
			else if (columnIndex == 1)
				return "PRESSURE";
			else if (columnIndex == 2)
				return "TEMPERATURE";
			else if (columnIndex == 3)
				return "ALTITUDE";
			return null;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0)
				return double.class;
			else if (columnIndex == 1)
				return int.class;
			else if (columnIndex == 2)
				return int.class;
			else if (columnIndex == 3)
				return double.class;
			return null;
		}

	}

	public static void write(AltimeterFile data, File outputFile) throws AltimeterIOException {
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

		
//		SpreadsheetDocument outOds = null;
//		try {
//			outOds = SpreadsheetDocument.newSpreadsheetDocument();
//			AltimeterSession[] sessions = data.getSessions();
//
//			for(int tab = 0; tab < sessions.length; tab++) {
//				// String title = pane.getTitleAt(tab); 
//				String title = String.format("Session %d", tab+1);
//				AltimeterSession session = sessions[tab];
//				Table sheet = outOds.appendSheet(title);
//				sheet.appendColumns(4);
//				sheet.appendRows(session.getData().length+1);
//				Cell c = null;
//				c = sheet.getCellByPosition(0, 0); c.setStringValue("TIME");
//				c = sheet.getCellByPosition(1, 0); c.setStringValue("PRESSURE");
//				c = sheet.getCellByPosition(2, 0); c.setStringValue("TEMPERATURE");
//				c = sheet.getCellByPosition(3, 0); c.setStringValue("ALTITUDE");
//				double tuIncr = 1.0/session.getRate().samplesPerSecond(); // millis per sample
//
//				double time = 0;
//				int ri = 1;
//				for(AltimeterSample sample : session.getData()) {
//					c = sheet.getCellByPosition(0, ri); c.setDoubleValue(Double.valueOf(time));
//					c = sheet.getCellByPosition(1, ri); c.setDoubleValue(Double.valueOf(sample.getPressure()));
//					c = sheet.getCellByPosition(2, ri); c.setDoubleValue(Double.valueOf(sample.getTemperature()));
//					c = sheet.getCellByPosition(3, ri); c.setDoubleValue(Double.valueOf(sample.getAltitude()));
//					ri++;
//					time += tuIncr;
//				}
//			}
//			
//			outOds.removeSheet(0); // remove the default (first) sheet
//			outOds.save(outputFile);
//			System.out.println("ods done");
//		} catch (Exception e) {
//			throw new AltimeterIOException(e);
//		}

	}

}
