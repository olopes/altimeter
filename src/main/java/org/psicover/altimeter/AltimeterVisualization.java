package org.psicover.altimeter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class AltimeterVisualization extends JFrame {
	private static final long serialVersionUID = -5356669458739782963L;
	
	private JTabbedPane pane = new JTabbedPane();

	public AltimeterVisualization() {
		setupUI();
	}
	
	private void setupUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Altimeter visualization");
		setExtendedState(getExtendedState()|JFrame.MAXIMIZED_BOTH );
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(pane, BorderLayout.CENTER);
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem open = new JMenuItem("Open");
		JMenuItem exportPNG = new JMenuItem("Export PNG");
		JMenuItem exportSVG = new JMenuItem("Export SVG");
		JMenuItem exit = new JMenuItem("Exit");
		fileMenu.add(open);
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
	
	private void doOpen() {
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File("."));
		if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			loadChart(jfc.getSelectedFile());
		}
		
	}

	private void doExportPNG() {
		
	}

	private void doExportSVG() {
		ChartPanel panel = (ChartPanel)pane.getSelectedComponent();
		if(null == panel) return;

		JFileChooser jfc = new JFileChooser(new File("."));
		if(jfc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {

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
	}
	
	private void loadChart(File f) {
		List<AltimeterSession> sessions = AltimeterFileIO.readFile(f);
		int i = 0;
		for(AltimeterSession session : sessions) {
			i++;
			int nsamples = session.getData().size();
			SampleRate rate = session.getRate();
			int duration = rate.duration(nsamples);
			double tuIncr = 1.0/rate.samplesPerSecond();
			int hours = duration/60;
			int seconds = duration%60;
			String title = String.format("Session %d: %02d:%02d", i, hours, seconds);
			XYSeriesCollection dataset = new XYSeriesCollection();
			XYSeriesCollection tempDataSet = new XYSeriesCollection();
			XYSeries altSeries = new XYSeries("Altitude (m)");
			XYSeries tempSeries = new XYSeries("Temperature (C)");
			
			double x = 0;
			for(AltimeterSample sample : session.getData()) {
				altSeries.add(x, Physics.wikipediaAltitude(sample.getPressure(), sample.getTemperature()));
				tempSeries.add(x, sample.getTemperature());
				x+=tuIncr;
			}
			
			dataset.addSeries(altSeries);
			tempDataSet.addSeries(tempSeries);
			
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
