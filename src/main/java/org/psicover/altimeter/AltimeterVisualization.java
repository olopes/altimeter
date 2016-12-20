package org.psicover.altimeter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Table;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;


public class AltimeterVisualization extends JFrame {
	private static final long serialVersionUID = -5356669458739782963L;
	
	private JTabbedPane pane = new JTabbedPane();
	private JMenu fileMenu = new JMenu("File");
	private JMenuItem open = new JMenuItem("Open");
	private JMenuItem exportCSV = new JMenuItem("Export CSV");
	private JMenuItem exportXLS = new JMenuItem("Export XLSX");
	private JMenuItem exportPNG = new JMenuItem("Export PNG");
	private JMenuItem exportSVG = new JMenuItem("Export SVG");
	private JMenuItem exit = new JMenuItem("Exit");
	private File lastDirectory = new File(".");

	public AltimeterVisualization() {
		setupUI();
	}
	
	private void setupUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Altimeter visualization");
		setExtendedState(getExtendedState()|JFrame.MAXIMIZED_BOTH );
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(pane, BorderLayout.CENTER);
		
		exportCSV.setEnabled(false);
		exportXLS.setEnabled(false);
		exportPNG.setEnabled(false);
		exportSVG.setEnabled(false);
		fileMenu.add(open);
		fileMenu.addSeparator();
		fileMenu.add(exportCSV);
		fileMenu.add(exportXLS);
		fileMenu.addSeparator();
		fileMenu.add(exportPNG);
		fileMenu.add(exportSVG);
		fileMenu.addSeparator();
		fileMenu.add(exit);
		
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOpen();
			}
		});
		exportCSV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExportCSV();
			}
		});
		exportXLS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExportXLS();
			}
		});
		exportPNG.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExportPNG();
			}
		});
		exportSVG.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExportSVG();
			}
		});
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExit();
			}
		});

		
		JMenuBar mbar = new JMenuBar();
		mbar.add(fileMenu);
		setJMenuBar(mbar);
	}
	
	
	// XXX Refactor to extract methods and use SwingWorker 
	
	private void doOpen() {
		JFileChooser jfc = new JFileChooser(lastDirectory);
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Altimeter data (*.hka, *.fda)", "hka", "fda"));
		if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			lastDirectory = jfc.getCurrentDirectory();
			try {
				loadChart(jfc.getSelectedFile());
			} catch (IOException e) {
				// TODO display error message
				e.printStackTrace();
			}
		}
		
	}

	private void doExportCSV() {
		ChartPanel panel = (ChartPanel)pane.getSelectedComponent();
		if(panel == null) return;
		JFileChooser jfc = new JFileChooser(lastDirectory);
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Tab Delimited Values (*.tsv, *.tab)", "tsv", "tab"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Comma Delimited Values (*.csv)", "csv"));
		if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			lastDirectory = jfc.getCurrentDirectory();
			// TODO add extension if necessary
			try (PrintWriter out = new PrintWriter(new FileWriter(jfc.getSelectedFile()))){
				AltimeterSession session = (AltimeterSession) panel.getClientProperty("raw session");
				char delimiter = '\t';
				if(jfc.getSelectedFile().getName().toLowerCase().endsWith(".csv"))
					delimiter = ',';
				out.printf("TIME%1$cPRESSURE%1$cTEMPERATURE%1$cALTITUDE%n", delimiter);
				long tuIncr = 1000/session.getRate().samplesPerSecond(); // millis per sample

				long time = 0;
				for(AltimeterSample sample : session.getData()) {
					out.printf(Locale.ENGLISH,"%2$tH:%2$tM:%2$tS.%2$tL%1$c%3$d%1$c%4$d%1$c%5$.4f%n", delimiter, time, sample.getPressure(), sample.getTemperature(), sample.getAltitude());
					time+=tuIncr;
				}
			} catch (IOException e) {
				// TODO display error message
				e.printStackTrace();
			}
		}
	}
	private void doExportXLS() {
		JFileChooser jfc = new JFileChooser(lastDirectory);
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Excel spreadsheet (*.xlsx)", "xlsx"));
		if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			lastDirectory = jfc.getCurrentDirectory();
			// TODO add extension if necessary
	        SXSSFWorkbook wb = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
	        // wb.setCompressTempFiles(true); // temp files will be gzipped
			try (FileOutputStream fout = new FileOutputStream(jfc.getSelectedFile())){
				CellStyle csTime = wb.createCellStyle();
				DataFormat dfTime = wb.createDataFormat();
				csTime.setDataFormat(dfTime.getFormat("#,##0.000"));
				CellStyle csInt = wb.createCellStyle();
				csInt.setDataFormat((short)3); // "#,##0"
				CellStyle csAlt = wb.createCellStyle();
				csAlt.setDataFormat((short)4); // "#,##0.00"
				
				for(int tab = 0; tab < pane.getTabCount(); tab++) {
					ChartPanel panel = (ChartPanel)pane.getComponentAt(tab);//TabComponentAt(tab);
					if(panel == null) continue;
					// String title = pane.getTitleAt(tab); 
					String title = String.format("Session %d", tab+1);
					AltimeterSession session = (AltimeterSession) panel.getClientProperty("raw session");
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
				// TODO display error message
				e.printStackTrace();
			} finally {
				// wb.dispose();
				try {
					wb.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void doExportODS() {
		JFileChooser jfc = new JFileChooser(lastDirectory);
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("ODF Spreadsheet (*.ods)", "ods"));
		if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			lastDirectory = jfc.getCurrentDirectory();
			// TODO add extension if necessary
			SpreadsheetDocument outOds = null;
			try {
				outOds = SpreadsheetDocument.newSpreadsheetDocument();
				Table sheet = outOds.appendSheet("Sheet _i_");
				org.odftoolkit.simple.table.Cell cell = null;
				int colIndex = 0;
				int rowIndex = 0;
				cell = sheet.getCellByPosition(colIndex, rowIndex);
				cell.setDoubleValue(123.456);
				
				outOds.save(jfc.getSelectedFile());
			} catch(Exception e) {
				// TODO display error
				e.printStackTrace();
			}
		}
		
	}
	
	private void doExportPNG() {
		ChartPanel panel = (ChartPanel)pane.getSelectedComponent();
		if(panel == null) return;
		JFileChooser jfc = new JFileChooser(lastDirectory);
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("PNG image (*.png)", "png"));
		if(jfc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			lastDirectory = jfc.getCurrentDirectory();
			// TODO add extension if necessary
			try {
				ChartUtilities.saveChartAsPNG(jfc.getSelectedFile(), panel.getChart(),
						panel.getWidth(), panel.getHeight());
			} catch (IOException e) {
				// TODO display error message
				e.printStackTrace();
			}
		}
	}

	private void doExportSVG() {
		ChartPanel panel = (ChartPanel)pane.getSelectedComponent();
		if(null == panel) return;

		JFileChooser jfc = new JFileChooser(lastDirectory);
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("SVG image (*.svg)", "svg"));
		if(jfc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			// TODO add extension if necessary
			lastDirectory = jfc.getCurrentDirectory();
			JFreeChart chart = panel.getChart();
			int x = panel.getWidth();
			int y = panel.getHeight();

			DOMImplementation domImpl =
					SVGDOMImplementation.getDOMImplementation();
			Document document = domImpl.createDocument(null, "svg", null);
			SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
			chart.draw(svgGenerator,new Rectangle(x,y));

			boolean useCSS = true; // we want to use CSS style attribute
			try(Writer out = new OutputStreamWriter(new FileOutputStream(jfc.getSelectedFile()), "UTF-8")) {
				svgGenerator.stream(out, useCSS);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void doExit() {
		setVisible(false);
		dispose();
	}
	
	private void loadChart(File f) throws IOException {
		List<AltimeterSession> sessions = AltimeterFileIO.readFile(f);
		if(null == sessions) return;
		exportCSV.setEnabled(true);
		exportXLS.setEnabled(true);
		exportPNG.setEnabled(true);
		exportSVG.setEnabled(true);

		int i = 0;
		for(AltimeterSession session : sessions) {
			i++;
			AltimeterSample [] samples = session.getData();
			int nsamples = samples.length;
			SampleRate rate = session.getRate();
			int duration = rate.duration(nsamples);
			double tuIncr = 1.0/rate.samplesPerSecond();
			int hours = duration/3600;
			int durm = duration%3600;
			int minutes = durm/60;
			int seconds = durm%60;
			String title = String.format("Session %d - %02d:%02d:%02d", i, hours, minutes, seconds);
			
			// TODO adjust ticks, scales, zoom, etc...
			XYSeriesCollection dataset = new XYSeriesCollection();
			XYSeriesCollection tempDataSet = new XYSeriesCollection();
			XYSeries altSeries = new XYSeries("Altitude (m)");
			XYSeries altSSeries = new XYSeries("Smoothed Altitude (m)");
			XYSeries tempSeries = new XYSeries("Temperature (C)");
			XYSeries tempSSeries = new XYSeries("Smoothed Temperature (C)");
			
			double x = 0;
			double [] smoothAlt = new double[samples.length];
			double [] smoothTem = new double[samples.length];
			for(int j = 0; j < samples.length; j++) {
				AltimeterSample sample = samples[j];
				double alt = sample.getAltitude();
				double tem = sample.getTemperature();
				
				// smooth curve
				int cnt = 1;
				double sumAlt = alt;
				double sumTem = tem;
				if(j > 0) {
					cnt++;
					sumAlt += smoothAlt[j-1];
					sumTem += smoothTem[j-1];
				} 
				if (j+1 < samples.length) {
					cnt++;
					AltimeterSample next = samples[j+1];
					sumAlt += next.getAltitude();
					sumTem += next.getTemperature();
				}
				
				smoothAlt[j] = sumAlt/cnt;
				smoothTem[j] = sumTem/cnt;
				
				altSeries.add(x, alt);
				altSSeries.add(x, smoothAlt[j]);
				tempSeries.add(x, tem);
				tempSSeries.add(x, smoothTem[j]);
				x+=tuIncr;
			}
			
			dataset.addSeries(altSeries);
			dataset.addSeries(altSSeries);
			tempDataSet.addSeries(tempSeries);
			tempDataSet.addSeries(tempSSeries);
			
			JFreeChart chart = ChartFactory.createXYLineChart(title,
		            "Time", "Altitude", dataset);
			
	        final XYPlot plot = chart.getXYPlot();
	        final NumberAxis axis2 = new NumberAxis("Temperature");
	        axis2.setAutoRangeIncludesZero(true);
	        plot.setRangeAxis(1, axis2);
	        plot.setDataset(1, tempDataSet);
	        plot.mapDatasetToRangeAxis(1, 1);
	        ((NumberAxis)plot.getRangeAxis()).setAutoRangeIncludesZero(false);
	        
			
	        final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
	        renderer2.setSeriesPaint(0, Color.BLUE);
	        // renderer2.setPlotShapes(true);
	        // renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
	        plot.setRenderer(1, renderer2);
			
			
			ChartPanel chartPanel = new ChartPanel(chart);
			chartPanel.putClientProperty("raw session", session);
			pane.add(title, chartPanel);
		}
	}

	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {  //Note 1
            public void run() {
            	AltimeterVisualization window = new AltimeterVisualization();
                window.setVisible(true);
            }
        });

		

	}

}
