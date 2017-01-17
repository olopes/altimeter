package org.psicover.altimeter.bean;

import javax.swing.table.AbstractTableModel;

public class AltimeterTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private final AltimeterSession session;
	private final double tuIncr; 

	public AltimeterTableModel(AltimeterSession session) {
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
			return "TIME"; //$NON-NLS-1$
		else if (columnIndex == 1)
			return "PRESSURE"; //$NON-NLS-1$
		else if (columnIndex == 2)
			return "TEMPERATURE"; //$NON-NLS-1$
		else if (columnIndex == 3)
			return "ALTITUDE"; //$NON-NLS-1$
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
