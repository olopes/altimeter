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

	// FIXME it has a lot of performance issues. Maybe a streaming solution like SXSSF

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
	}

}
