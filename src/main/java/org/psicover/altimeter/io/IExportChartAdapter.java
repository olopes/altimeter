package org.psicover.altimeter.io;

import java.io.File;

import org.jfree.chart.JFreeChart;
import org.psicover.altimeter.ui.AltimeterIOException;

public interface IExportChartAdapter {
	
	void write(JFreeChart chart, File selectedFile, int x, int y) throws AltimeterIOException;

}
