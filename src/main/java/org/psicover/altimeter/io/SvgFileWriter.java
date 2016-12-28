package org.psicover.altimeter.io;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.psicover.altimeter.ui.AltimeterIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class SvgFileWriter implements IExportChartAdapter {

	private static IExportChartAdapter instance;
	public static IExportChartAdapter getInstance() {
		if(instance == null)
			instance = new SvgFileWriter();
		return instance;
	}
	private SvgFileWriter() {
	}

	public void write(JFreeChart chart, File selectedFile, int x, int y) throws AltimeterIOException {
		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
		Document document = domImpl.createDocument(null, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		chart.draw(svgGenerator, new Rectangle(x, y));

		boolean useCSS = true; // we want to use CSS style attribute
		try (Writer out = new OutputStreamWriter(new FileOutputStream(selectedFile), "UTF-8")) {
			svgGenerator.stream(out, useCSS);
		} catch (IOException e) {
			throw new AltimeterIOException(e);
		}

	}
}
