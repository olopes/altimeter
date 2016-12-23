package org.psicover.altimeter.io;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.psicover.altimeter.ui.AltimeterIOException;

public class PngFileWriter {

	public static void write(JFreeChart chart, File selectedFile, int x, int y) throws AltimeterIOException {
		try {
			ChartUtilities.saveChartAsPNG(selectedFile, chart, x, y);
		} catch (IOException e) {
			throw new AltimeterIOException(e);
		}
	}
}
