package org.psicover.altimeter.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;

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
		ZipOutputStream zout = null;
		PrintStream ps = null;
		try {
			DecimalFormat fmtTime = new DecimalFormat("0.0##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			DecimalFormat fmtInt = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			DecimalFormat fmtAlt = new DecimalFormat("0.0#############", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			
			zout = new ZipOutputStream(new FileOutputStream(outputFile));
			ps = new PrintStream(zout, true, "UTF-8");
			
			zout.putNextEntry(new ZipEntry("META-INF/manifest.xml"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ps.println("<manifest:manifest xmlns:manifest=\"urn:oasis:names:tc:opendocument:xmlns:manifest:1.0\"><manifest:file-entry manifest:media-type=\"application/vnd.oasis.opendocument.spreadsheet\" manifest:full-path=\"/\" /><manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"meta.xml\" /><manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"content.xml\" /><manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"styles.xml\" /></manifest:manifest>");
			ps.flush();
			
			zout.putNextEntry(new ZipEntry("meta.xml"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ps.println("<office:document-meta xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" xmlns:svg=\"urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0\" xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\" xmlns:draw=\"urn:oasis:names:tc:opendocument:xmlns:drawing:1.0\" xmlns:script=\"urn:oasis:names:tc:opendocument:xmlns:script:1.0\" xmlns:ooo=\"http://openoffice.org/2004/office\" xmlns:number=\"urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0\" xmlns:form=\"urn:oasis:names:tc:opendocument:xmlns:form:1.0\" xmlns:meta=\"urn:oasis:names:tc:opendocument:xmlns:meta:1.0\" xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\" xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\" xmlns:config=\"urn:oasis:names:tc:opendocument:xmlns:config:1.0\" xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" office:version=\"1.2\"><office:meta><meta:generator>Altimeter Visualization Utility</meta:generator></office:meta></office:document-meta>");
			ps.flush();
			
			zout.putNextEntry(new ZipEntry("mimetype"));
			ps.print("application/vnd.oasis.opendocument.spreadsheet");
			ps.flush();
			
			zout.putNextEntry(new ZipEntry("styles.xml"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ps.println("<office:document-styles xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" xmlns:svg=\"urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0\" xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\" xmlns:draw=\"urn:oasis:names:tc:opendocument:xmlns:drawing:1.0\" xmlns:script=\"urn:oasis:names:tc:opendocument:xmlns:script:1.0\" xmlns:ooo=\"http://openoffice.org/2004/office\" xmlns:number=\"urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0\" xmlns:form=\"urn:oasis:names:tc:opendocument:xmlns:form:1.0\" xmlns:meta=\"urn:oasis:names:tc:opendocument:xmlns:meta:1.0\" xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\" xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\" xmlns:config=\"urn:oasis:names:tc:opendocument:xmlns:config:1.0\" xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" office:version=\"1.2\"><office:styles /><office:automatic-styles /><office:master-styles /></office:document-styles>");
			ps.flush();

			zout.putNextEntry(new ZipEntry("content.xml"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ps.print("<office:document-content xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" xmlns:svg=\"urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0\" xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\" xmlns:draw=\"urn:oasis:names:tc:opendocument:xmlns:drawing:1.0\" xmlns:script=\"urn:oasis:names:tc:opendocument:xmlns:script:1.0\" xmlns:ooo=\"http://openoffice.org/2004/office\" xmlns:number=\"urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0\" xmlns:form=\"urn:oasis:names:tc:opendocument:xmlns:form:1.0\" xmlns:meta=\"urn:oasis:names:tc:opendocument:xmlns:meta:1.0\" xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\" xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\" xmlns:config=\"urn:oasis:names:tc:opendocument:xmlns:config:1.0\" xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" office:version=\"1.2\">");
			ps.print("<office:automatic-styles><style:style style:family=\"table\" style:name=\"ta0\"><style:table-properties table:display=\"true\"/></style:style></office:automatic-styles>");
			ps.print("<office:body><office:spreadsheet>");
			final int numSheets = data.getSessions().length;
			// loop
			for(int i = 0; i < numSheets; i++) {
				AltimeterSession session = data.getSessions()[i];
				ps.print("<table:table table:style-name=\"ta0\" table:name=\"Session "+(i+1)+"\">");
				ps.print("<table:table-column/><table:table-column/><table:table-column/><table:table-column/>");
				// header
				ps.print("<table:table-row>");
				ps.print("<table:table-cell office:value-type=\"string\"><text:p>TIME</text:p></table:table-cell>");
				ps.print("<table:table-cell office:value-type=\"string\"><text:p>PRESSURE</text:p></table:table-cell>");
				ps.print("<table:table-cell office:value-type=\"string\"><text:p>TEMPERATURE</text:p></table:table-cell>");
				ps.print("<table:table-cell office:value-type=\"string\"><text:p>ALTITUDE</text:p></table:table-cell>");
				ps.print("</table:table-row>");
				
				for(AltimeterSample sample : session.getData()) {
					ps.print("<table:table-row>");
					ps.print("<table:table-cell office:value-type=\"float\" office:value=\""+fmtTime.format(sample.getTime())+"\"><text:p>"+fmtTime.format(sample.getTime())+"</text:p></table:table-cell>");
					ps.print("<table:table-cell office:value-type=\"float\" office:value=\""+fmtInt.format(sample.getPressure())+"\"><text:p>"+fmtInt.format(sample.getPressure())+"</text:p></table:table-cell>");
					ps.print("<table:table-cell office:value-type=\"float\" office:value=\""+fmtInt.format(sample.getTemperature())+"\"><text:p>"+fmtInt.format(sample.getTemperature())+"</text:p></table:table-cell>");
					ps.print("<table:table-cell office:value-type=\"float\" office:value=\""+fmtTime.format(sample.getAltitude())+"\"><text:p>"+fmtAlt.format(sample.getAltitude())+"</text:p></table:table-cell>");
					ps.print("</table:table-row>");
				}
				ps.print("</table:table>");
			}
			ps.println("</office:spreadsheet></office:body></office:document-content>");
			ps.flush();
		} catch(Throwable t) {
			throw new AltimeterIOException(t);
		} finally {
			if(ps != null)
				ps.close();
		}
	}

}
