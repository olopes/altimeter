package org.psicover.altimeter.io;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

public class PngFileWriter implements IExportChartAdapter {

	private static IExportChartAdapter instance;
	public static IExportChartAdapter getInstance() {
		if(instance == null)
			instance = new PngFileWriter();
		return instance;
	}
	private PngFileWriter() {
	}


	public void write(JFreeChart chart, File selectedFile, int x, int y) throws AltimeterIOException {
		try {
			ChartUtilities.saveChartAsPNG(selectedFile, chart, x, y);
		} catch (IOException e) {
			throw new AltimeterIOException(e);
		}
	}
}
