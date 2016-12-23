package org.psicover.altimeter.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.FieldPosition;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.bean.SampleRate;
import org.psicover.altimeter.io.AltimeterFileReader;
import org.psicover.altimeter.io.DlmFileWriter;
import org.psicover.altimeter.io.OdsFileWriter;
import org.psicover.altimeter.io.PngFileWriter;
import org.psicover.altimeter.io.SvgFileWriter;
import org.psicover.altimeter.io.XlsxFileWriter;


public class AltimeterVisualization extends JFrame {
	private static final long serialVersionUID = -5356669458739782963L;
	
	private JTabbedPane pane = new JTabbedPane();
	private JMenu fileMenu = new JMenu("File");
	private JMenuItem open = new JMenuItem("Open");
	private JMenuItem exportCSV = new JMenuItem("Export CSV");
	private JMenuItem exportXLS = new JMenuItem("Export XLSX");
	private JMenuItem exportODS = new JMenuItem("Export ODS");
	private JMenuItem exportPNG = new JMenuItem("Export PNG");
	private JMenuItem exportSVG = new JMenuItem("Export SVG");
	private JMenuItem exit = new JMenuItem("Exit");
	private File lastDirectory = new File(".");
	private AltimeterFile currentFile;

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
		exportODS.setEnabled(false);
		exportPNG.setEnabled(false);
		exportSVG.setEnabled(false);
		fileMenu.add(open);
		fileMenu.addSeparator();
		fileMenu.add(exportCSV);
		fileMenu.add(exportXLS);
		fileMenu.add(exportODS);
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
		exportODS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExportODS();
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
				this.currentFile = AltimeterFileReader.readFile(jfc.getSelectedFile());
				createCharts(this.currentFile);
			} catch (AltimeterIOException e) {
				displayError(e);
			}
		}
		
	}

	private void doExportCSV() {
		JPanel panel = (JPanel)pane.getSelectedComponent();
		if(panel == null) return;
		JFileChooser jfc = new JFileChooser(lastDirectory);
		FileNameExtensionFilter fileFilterTsv = new FileNameExtensionFilter("Tab Delimited Values (*.tsv, *.tab)", "tsv", "tab");
		FileNameExtensionFilter fileFilterCsv = new FileNameExtensionFilter("Comma Delimited Values (*.csv)", "csv");
		FileNameExtensionFilter fileFilterSsv = new FileNameExtensionFilter("Semicolon Delimited Values (*.csv)", "csv");
		jfc.addChoosableFileFilter(fileFilterTsv);
		jfc.addChoosableFileFilter(fileFilterCsv);
		jfc.addChoosableFileFilter(fileFilterSsv);
		jfc.setFileFilter(fileFilterTsv);

		if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			lastDirectory = jfc.getCurrentDirectory();
			// TODO add extension if necessary
			FileFilter selectedFilter = jfc.getFileFilter();
			try {
				AltimeterSession session = (AltimeterSession) panel.getClientProperty("raw session");
				if(selectedFilter == fileFilterCsv)
					DlmFileWriter.writeCsv(session, jfc.getSelectedFile());
				else if(selectedFilter == fileFilterSsv)
					DlmFileWriter.writeSsv(session, jfc.getSelectedFile());
				else
					DlmFileWriter.writeTsv(session, jfc.getSelectedFile());  // tab as default
			} catch (AltimeterIOException e) {
				displayError(e);
			}
		}
	}
	private void doExportXLS() {
		JFileChooser jfc = new JFileChooser(lastDirectory);
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Excel spreadsheet (*.xlsx)", "xlsx");
		jfc.setFileFilter(fileFilter);
		if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			lastDirectory = jfc.getCurrentDirectory();
			// TODO add extension if necessary

			try {
				XlsxFileWriter.write(this.currentFile, jfc.getSelectedFile());
			} catch (AltimeterIOException e) {
				displayError(e);
			}
		}
	}

	private void doExportODS() {
		JFileChooser jfc = new JFileChooser(lastDirectory);
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("ODF Spreadsheet (*.ods)", "ods");
		jfc.setFileFilter(fileFilter);
		if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			lastDirectory = jfc.getCurrentDirectory();
			// TODO add extension if necessary
			try {
				OdsFileWriter.write(this.currentFile, jfc.getSelectedFile());
			} catch (AltimeterIOException e) {
				displayError(e);
			}
			
		}
		
	}
	
	private void doExportPNG() {
		JPanel panel = (JPanel)pane.getSelectedComponent();
		if(panel == null) return;
		ChartPanel chartPanel = (ChartPanel) panel.getComponent(0);
		JFileChooser jfc = new JFileChooser(lastDirectory);
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("PNG image (*.png)", "png");
		jfc.setFileFilter(fileFilter);
		if(jfc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			lastDirectory = jfc.getCurrentDirectory();
			// TODO add extension if necessary
			JFreeChart chart = chartPanel.getChart();
			int x = panel.getWidth();
			int y = panel.getHeight();

			try {
				PngFileWriter.write(chart, jfc.getSelectedFile(), x, y);
			} catch (AltimeterIOException e) {
				displayError(e);
			}
		}
	}

	private void doExportSVG() {
		JPanel panel = (JPanel)pane.getSelectedComponent();
		if(null == panel) return;
		ChartPanel chartPanel = (ChartPanel) panel.getComponent(0);

		JFileChooser jfc = new JFileChooser(lastDirectory);
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("SVG image (*.svg)", "svg");
		jfc.setFileFilter(fileFilter);
		if(jfc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			// TODO add extension if necessary
			lastDirectory = jfc.getCurrentDirectory();
			JFreeChart chart = chartPanel.getChart();
			int x = panel.getWidth();
			int y = panel.getHeight();

			try {
				SvgFileWriter.write(chart, jfc.getSelectedFile(), x, y);
			} catch (AltimeterIOException e) {
				displayError(e);
			}
		}
	}

	private void doExit() {
		setVisible(false);
		dispose();
	}
	
	private void createCharts(AltimeterFile altimeterFile) {
		if(null == altimeterFile) return; // TODO display error
		exportCSV.setEnabled(true);
		exportXLS.setEnabled(true);
		exportODS.setEnabled(true);
		exportPNG.setEnabled(true);
		exportSVG.setEnabled(true);

		int i = 0;
		for(AltimeterSession session : altimeterFile.getSessions()) {
			// TODO extract to custom panel class.
			
			i++;
			AltimeterSample [] samples = session.getData();
			int nsamples = samples.length;
			SampleRate rate = session.getRate();
			int duration = rate.duration(nsamples);
			int hours = duration/3600;
			int durm = duration%3600;
			int minutes = durm/60;
			int seconds = durm%60;
			String title = String.format("Session %d - %02d:%02d:%02d", i, hours, minutes, seconds);
			
			
			// TODO adjust ticks, scales, zoom, etc...
			TimeSeriesCollection dataset = new TimeSeriesCollection();
			TimeSeriesCollection tempDataSet = new TimeSeriesCollection();
			TimeSeries altSeries = new TimeSeries("Altitude (m)");
			TimeSeries altSSeries = new TimeSeries("Smoothed Altitude (m)");
			TimeSeries tempSeries = new TimeSeries("Temperature (C)");
			TimeSeries tempSSeries = new TimeSeries("Smoothed Temperature (C)");
			RegularTimePeriod tp=new AltimeterTimePeriod(rate);
			
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
				
				altSeries.add(tp, alt);
				altSSeries.add(tp, smoothAlt[j]);
				tempSeries.add(tp, tem);
				tempSSeries.add(tp, smoothTem[j]);
				tp = tp.next();
			}
			
			dataset.addSeries(altSeries);
			dataset.addSeries(altSSeries);
			tempDataSet.addSeries(tempSeries);
			tempDataSet.addSeries(tempSSeries);
			
			JFreeChart chart = ChartFactory.createXYLineChart(title,
		            "Time (s)", "Altitude (m)", dataset);
			
	        final XYPlot plot = chart.getXYPlot();
	        final NumberAxis axis2 = new NumberAxis("Temperature (C)");
	        axis2.setAutoRangeIncludesZero(true);
	        plot.setRangeAxis(1, axis2);
	        plot.setDataset(1, tempDataSet);
	        plot.mapDatasetToRangeAxis(1, 1);
	        ((NumberAxis)plot.getRangeAxis()).setAutoRangeIncludesZero(false);
	        
	        final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
	        domainAxis.setNumberFormatOverride(new DecimalFormat("##0") {
				private static final long serialVersionUID = 9200880572231520816L;
				@Override
	        	public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
	        		// TODO Auto-generated method stub
	        		return super.format(number/1000, result, fieldPosition);
	        	}
	        	@Override
	        	public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
	        		// TODO Auto-generated method stub
	        		return super.format(number/1000L, result, fieldPosition);
	        	}
	        });
//	        
//			DateAxis axis = (DateAxis) plot.getDomainAxis();
//			axis.setDateFormatOverride(new SimpleDateFormat(dateFormat));
	        
			
	        final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
	        renderer2.setSeriesPaint(0, Color.BLUE);
	        // renderer2.setPlotShapes(true);
	        // renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
	        plot.setRenderer(1, renderer2);
			
			ChartPanel chartPanel = new ChartPanel(chart);
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(chartPanel, BorderLayout.CENTER);
			panel.putClientProperty("raw session", session);
			
			// TODO input to set zoomLevel
			final int stepSize = 100;
			
			// setup dos botoes
			final JSlider slider = new JSlider(0, (int)(tp.getFirstMillisecond()/1000L), 0);
			panel.add(slider, BorderLayout.SOUTH);
			slider.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
		            int value = slider.getValue();
		            DateRange range = new DateRange(value*1000,(value+stepSize)*1000);
		            domainAxis.setRange(range);
				}
			});
            DateRange range = new DateRange(0,stepSize*1000);
            domainAxis.setRange(range);

            // XXX I will use checkboxes instead
            // http://stackoverflow.com/questions/24562775/setting-series-visiblity-to-false-also-hides-it-from-the-legend
            // chartPanel.addChartMouseListener(new ChartMouseListener() {
			// 	
			// 	@Override
			// 	public void chartMouseMoved(ChartMouseEvent event) {
			// 		// TODO Auto-generated method stub
			// 		
			// 	}
			// 	
            //     @Override
            //     public void chartMouseClicked(ChartMouseEvent event) {
            //         ChartEntity entity = event.getEntity();
            //         if (entity instanceof LegendItemEntity) {
            //             //*
            //             LegendItemEntity itemEntity = (LegendItemEntity) entity;
            //             XYDataset dataset = (XYDataset) itemEntity.getDataset();
            //             int index = dataset.indexOf(itemEntity.getSeriesKey());
            //             XYPlot plot = (XYPlot) event.getChart().getPlot();
            // 
            //             //set the renderer to hide the series
            //             XYItemRenderer renderer = plot.getRenderer();
            //             renderer.setSeriesVisible(index, !renderer.isSeriesVisible(index), false);
            //             renderer.setSeriesVisibleInLegend(index, true, false);
            //             //*/        
            //         }
            //     }
            // });            
            
			pane.add(title, panel);
		}
	}
	
	private void displayError(Throwable t) {
		if(t == null) return;
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setMaximumSize(new Dimension(400, 300));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.fill=GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Please note the error below"), c);
		c.gridy=1;
		c.fill=GridBagConstraints.BOTH;
		c.weightx = c.weighty = 1;
		StringWriter sw = new StringWriter(4096);
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		JTextArea ta = new JTextArea(sw.toString());
		panel.add(new JScrollPane(ta), c);
		
		// parent set to null to display the box centered on screen.
		JOptionPane.showMessageDialog(null, panel, "An error has occurred", JOptionPane.ERROR_MESSAGE);		
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
