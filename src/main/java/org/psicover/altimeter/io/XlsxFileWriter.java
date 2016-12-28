package org.psicover.altimeter.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.ui.AltimeterIOException;

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
		try(SXSSFWorkbook wb = new SXSSFWorkbook(100)) { // keep 100 rows in memory, exceeding rows will be flushed to disk
			// wb.setCompressTempFiles(true); // temp files will be gzipped
			try (FileOutputStream fout = new FileOutputStream(selectedFile)){
				CellStyle csTime = wb.createCellStyle();
				DataFormat dfTime = wb.createDataFormat();
				csTime.setDataFormat(dfTime.getFormat("#,##0.000"));
				CellStyle csInt = wb.createCellStyle();
				csInt.setDataFormat((short)3); // "#,##0"
				CellStyle csAlt = wb.createCellStyle();
				csAlt.setDataFormat((short)4); // "#,##0.00"

				AltimeterSession[] sessions = data.getSessions();

				for(int tab = 0; tab < sessions.length; tab++) {
					// String title = pane.getTitleAt(tab); 
					String title = String.format("Session %d", tab+1);
					AltimeterSession session = sessions[tab];
					Row r;
					Cell c;
					SXSSFSheet sheet = wb.createSheet(title);
					sheet.setDefaultColumnStyle(0, csTime);
					sheet.setDefaultColumnStyle(1, csInt);
					sheet.setDefaultColumnStyle(2, csInt);
					sheet.setDefaultColumnStyle(3, csAlt);
					r = sheet.createRow(0);
					c = r.createCell(0); c.setCellValue("TIME");
					c = r.createCell(1); c.setCellValue("PRESSURE");
					c = r.createCell(2); c.setCellValue("TEMPERATURE");
					c = r.createCell(3); c.setCellValue("ALTITUDE");
					double tuIncr = 1.0/session.getRate().samplesPerSecond(); // millis per sample

					double time = 0;
					int ri = 1;
					for(AltimeterSample sample : session.getData()) {
						r = sheet.createRow(ri);
						c = r.createCell(0); c.setCellValue(time);
						c = r.createCell(1); c.setCellValue(sample.getPressure());
						c = r.createCell(2); c.setCellValue(sample.getTemperature());
						c = r.createCell(3); c.setCellValue(sample.getAltitude());
						ri++;
						time += tuIncr;
					}
				}

				wb.write(fout);
			} catch (IOException e) {
				throw new AltimeterIOException(e);
			} finally {
				wb.dispose();
				// try {
				// 	wb.close();
				// } catch (IOException e) {
				// }
			}
		} catch (AltimeterIOException e) {
			throw e;
		} catch (IOException e) {
			throw new AltimeterIOException(e);
		}

	}

}
