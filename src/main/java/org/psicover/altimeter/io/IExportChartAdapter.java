package org.psicover.altimeter.io;

import java.io.File;

import org.jfree.chart.JFreeChart;

public interface IExportChartAdapter {
	
	void write(JFreeChart chart, File selectedFile, int x, int y) throws AltimeterIOException;

}
