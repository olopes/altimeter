package org.psicover.altimeter.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;

public class XlsxFileWriter implements IExportDataAdapter {
	private static IExportDataAdapter instance;
	public static IExportDataAdapter getInstance() {
		if(instance == null)
			instance = new XlsxFileWriter();
		return instance;
	}
	private XlsxFileWriter() {
	}

	public void write(AltimeterFile data, AltimeterSession ignore, File selectedFile) throws AltimeterIOException {
		ZipOutputStream zout = null;
		PrintStream ps = null;
		try {
			DecimalFormat fmtTime = new DecimalFormat("0.0##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			DecimalFormat fmtInt = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			DecimalFormat fmtAlt = new DecimalFormat("0.0##############", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			DateFormat tsFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			
			final int numSessions = data.getSessions().length;
			zout = new ZipOutputStream(new FileOutputStream(selectedFile));
			ps = new PrintStream(zout, true, "UTF-8");
			
			zout.putNextEntry(new ZipEntry("_rels/.rels"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			ps.println("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
			ps.println("<Relationship Id=\"rId1\" Target=\"xl/workbook.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\"/>");
			ps.println("<Relationship Id=\"rId2\" Target=\"docProps/app.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\"/>");
			ps.println("<Relationship Id=\"rId3\" Target=\"docProps/core.xml\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\"/>");
			ps.println("</Relationships>");
			ps.flush();

			zout.putNextEntry(new ZipEntry("[Content_Types].xml"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			ps.println("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
			ps.println("<Default ContentType=\"application/vnd.openxmlformats-package.relationships+xml\" Extension=\"rels\"/>");
			ps.println("<Default ContentType=\"application/xml\" Extension=\"xml\"/>");
			ps.println("<Override ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\" PartName=\"/docProps/app.xml\"/>");
			ps.println("<Override ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\" PartName=\"/docProps/core.xml\"/>");
			ps.println("<Override ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\" PartName=\"/xl/sharedStrings.xml\"/>");
			ps.println("<Override ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\" PartName=\"/xl/styles.xml\"/>");
			ps.println("<Override ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\" PartName=\"/xl/workbook.xml\"/>");
			// loop
			for(int i = 0; i < numSessions; i++)
				ps.println("<Override ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\" PartName=\"/xl/worksheets/sheet"+(i+1)+".xml\"/>");
			ps.println("</Types>");
			ps.flush();
			
			
			zout.putNextEntry(new ZipEntry("docProps/app.xml"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ps.print("<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\"><Application>Altimeter Visualization Tool</Application></Properties>");
			ps.flush();
			
			zout.putNextEntry(new ZipEntry("docProps/core.xml"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			ps.println("<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
			ps.println("<dcterms:created xsi:type=\"dcterms:W3CDTF\">"+tsFmt.format(new Date())+"</dcterms:created>");
			ps.println("<dc:creator>Altimeter Visualization Tool</dc:creator>");
			ps.println("</cp:coreProperties>");
			ps.flush();
			
			zout.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ps.print("<sst count=\"0\" uniqueCount=\"0\" xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"/>");
			ps.flush();
			
			zout.putNextEntry(new ZipEntry("xl/styles.xml"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ps.print("<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"#,##0.000\"/></numFmts><fonts count=\"1\"><font><sz val=\"11.0\"/><color indexed=\"8\"/><name val=\"Calibri\"/><family val=\"2\"/><scheme val=\"minor\"/></font></fonts><fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"darkGray\"/></fill></fills><borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"4\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/><xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"true\"/><xf numFmtId=\"3\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"true\"/><xf numFmtId=\"4\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"true\"/></cellXfs></styleSheet>");
			ps.flush();
			
			zout.putNextEntry(new ZipEntry("xl/workbook.xml"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ps.print("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">");
			ps.print("<workbookPr date1904=\"false\"/>");
			ps.print("<bookViews>");
			ps.print("<workbookView activeTab=\"0\"/>");
			ps.print("</bookViews>");
			ps.print("<sheets>");
			for(int i = 0; i < numSessions; i++)
				ps.print("<sheet name=\"Session "+(i+1)+"\" r:id=\"rId"+(i+3)+"\" sheetId=\""+(i+1)+"\"/>");
			ps.print("</sheets></workbook>");
			ps.flush();
			
			zout.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			ps.println("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
			ps.println("<Relationship Id=\"rId1\" Target=\"sharedStrings.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\"/>");
			ps.println("<Relationship Id=\"rId2\" Target=\"styles.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\"/>");
			for(int i = 0; i < numSessions; i++)
				ps.println("<Relationship Id=\"rId"+(i+3)+"\" Target=\"worksheets/sheet"+(i+1)+".xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\"/>");
			ps.println("</Relationships>");
			ps.flush();

			AltimeterSession [] sessions = data.getSessions();
			for(int i = 0; i < numSessions; i++){
				AltimeterSession session = sessions[i];
				zout.putNextEntry(new ZipEntry("xl/worksheets/sheet"+(i+1)+".xml"));
				
				// write header
				ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				ps.print("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
				ps.print("<dimension ref=\"A1\"/><sheetViews><sheetView workbookViewId=\"0\""+(i==0?" tabSelected=\"true\"":"")+"/></sheetViews><sheetFormatPr defaultRowHeight=\"15.0\"/>");
				ps.print("<cols><col min=\"1\" max=\"1\" style=\"1\" width=\"8.0\" customWidth=\"false\"/><col min=\"2\" max=\"2\" style=\"2\" width=\"8.0\" customWidth=\"false\"/><col min=\"3\" max=\"3\" style=\"2\" width=\"8.0\" customWidth=\"false\"/><col min=\"4\" max=\"4\" style=\"3\" width=\"8.0\" customWidth=\"false\"/></cols>");
				ps.print("<sheetData>\n");
				ps.print("<row r=\"1\">\n");
				ps.print("<c r=\"A1\" t=\"inlineStr\"><is><t>TIME</t></is></c><c r=\"B1\" t=\"inlineStr\"><is><t>PRESSURE</t></is></c><c r=\"C1\" t=\"inlineStr\"><is><t>TEMPERATURE</t></is></c><c r=\"D1\" t=\"inlineStr\"><is><t>ALTITUDE</t></is></c></row>\n");

				int r = 2;
				for(AltimeterSample sample : session.getData()) {
					ps.print("<row r=\""+r+"\">\n");
					ps.print("<c r=\"A"+r+"\" t=\"n\"><v>"+fmtTime.format(sample.getTime())+"</v></c>");
					ps.print("<c r=\"B"+r+"\" t=\"n\"><v>"+fmtInt.format(sample.getPressure())+"</v></c>");
					ps.print("<c r=\"C"+r+"\" t=\"n\"><v>"+fmtInt.format(sample.getTemperature())+"</v></c>");
					ps.print("<c r=\"D"+r+"\" t=\"n\"><v>"+fmtAlt.format(sample.getAltitude())+"</v></c>");
					ps.print("</row>\n");
					r++;
				}
				ps.print("</sheetData><pageMargins bottom=\"0.75\" footer=\"0.3\" header=\"0.3\" left=\"0.7\" right=\"0.7\" top=\"0.75\"/></worksheet>");
				ps.flush();
			}
			
		} catch (Throwable t) {
			throw new AltimeterIOException(t);
		} finally {
			if(ps != null)
				ps.close();
		}
	}
	
}
