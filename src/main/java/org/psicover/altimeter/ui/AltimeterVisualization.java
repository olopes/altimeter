package org.psicover.altimeter.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.io.AltimeterFileReader;
import org.psicover.altimeter.io.AltimeterIOException;
import org.psicover.altimeter.io.DlmFileWriter;
import org.psicover.altimeter.io.IExportChartAdapter;
import org.psicover.altimeter.io.IExportDataAdapter;
import org.psicover.altimeter.io.JpegFileWriter;
import org.psicover.altimeter.io.OdsFileWriter;
import org.psicover.altimeter.io.PngFileWriter;
import org.psicover.altimeter.io.SvgFileWriter;
import org.psicover.altimeter.io.XlsxFileWriter;


public class AltimeterVisualization extends JFrame {
	private static final long serialVersionUID = -5356669458739782963L;
	
	private JTabbedPane pane = new JTabbedPane();
	private JMenu fileMenu = new JMenu("File");
	private JMenuItem open = new JMenuItem("Open");
	private JMenuItem exportData = new JMenuItem("Export Data");
	private JMenuItem exportChart = new JMenuItem("Export Chart");
	private JMenuItem exit = new JMenuItem("Exit");
	private File lastDirectory = new File(".");
	private AltimeterFile currentFile;
	
	private FileFilter fileFilterXls = new ExportDataFileFilter(XlsxFileWriter.getInstance(),"Excel spreadsheet (*.xlsx)", "xlsx");
	private FileFilter fileFilterOds = new ExportDataFileFilter(OdsFileWriter.getInstance(),"ODF Spreadsheet (*.ods)", "ods");
	private FileFilter fileFilterTsv = new ExportDataFileFilter(DlmFileWriter.getTsvInstance(),"Tab Delimited Values (*.tsv, *.tab)", "tsv", "tab");
	private FileFilter fileFilterCsv = new ExportDataFileFilter(DlmFileWriter.getCsvInstance(),"Comma Delimited Values (*.csv)", "csv");
	private FileFilter fileFilterSsv = new ExportDataFileFilter(DlmFileWriter.getSsvInstance(),"Semicolon Delimited Values (*.csv)", "csv");

	private FileFilter fileFilterPng = new ExportChartFileFilter(PngFileWriter.getInstance(),"PNG image (*.png)", "png");
	private FileFilter fileFilterJpg = new ExportChartFileFilter(JpegFileWriter.getInstance(),"Jpeg image (*.jpg, *.jpeg)", "jpg", "jpeg");
	private FileFilter fileFilterSvg = new ExportChartFileFilter(SvgFileWriter.getInstance(),"SVG image (*.svg)", "svg");
	
	public AltimeterVisualization() {
		setupUI();
	}
	
	private void setupUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Altimeter visualization");
		setExtendedState(getExtendedState()|JFrame.MAXIMIZED_BOTH );
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(pane, BorderLayout.CENTER);
		
		exportData.setEnabled(false);
		exportChart.setEnabled(false);
		fileMenu.add(open);
		fileMenu.addSeparator();
		fileMenu.add(exportData);
		fileMenu.add(exportChart);
		fileMenu.addSeparator();
		fileMenu.add(exit);
		
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOpen();
			}
		});
		exportData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExportData();
			}
		});
		exportChart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExportChart();
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
	
	
	private class ExportDataWorker extends SwingWorker<Void, Void> {
		IExportDataAdapter adapter;
		AltimeterFile data;
		AltimeterSession currentSession;
		File outputFile;
		AltimeterIOException error;
		
		public ExportDataWorker(IExportDataAdapter adapter, AltimeterFile data, AltimeterSession currentSession, File outputFile) {
			this.adapter=adapter;
			this.data=data;
			this.currentSession= currentSession;
			this.outputFile=outputFile;
			this.error=null;
			exportData.setEnabled(false);
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			try {
				adapter.write(data, currentSession, outputFile);
			} catch(AltimeterIOException e) {
				error = e;
			}
			return null;
		}

		@Override
		protected void done() {
			if(error == null)
				JOptionPane.showMessageDialog(AltimeterVisualization.this, "Done!");
			else
				displayError(error);
			exportData.setEnabled(true);
		}
		
		
		
	}
	
	private class ExportChartWorker extends SwingWorker<Void, Void> {
		IExportChartAdapter adapter;
		JFreeChart chart;
		File outputFile;
		int x,y;
		AltimeterIOException error;
		
		public ExportChartWorker(IExportChartAdapter adapter, JFreeChart chart, File outputFile, int x, int y) {
			this.adapter=adapter;
			this.chart=chart;
			this.outputFile=outputFile;
			this.x = x;
			this.y = y;
			this.error=null;
			exportChart.setEnabled(false);
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			try {
				adapter.write(chart, outputFile, x, y);
			} catch(AltimeterIOException e) {
				error = e;
			}
			return null;
		}

		@Override
		protected void done() {
			if(error == null)
				JOptionPane.showMessageDialog(AltimeterVisualization.this, "Done!");
			else
				displayError(error);
			exportChart.setEnabled(true);
		}
	}
	
	
	private void doOpen() {
		JFileChooser jfc = new JFileChooser(lastDirectory);
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Altimeter data (*.hka, *.fda)", "hka", "fda");
		jfc.addChoosableFileFilter(fileFilter);
		jfc.setFileFilter(fileFilter);
		if(jfc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
		
		lastDirectory = jfc.getCurrentDirectory();
		readFile(jfc.getSelectedFile());
	}
	
	private void readFile(File f) {
		try {
			this.currentFile = AltimeterFileReader.readFile(f);
			createCharts(this.currentFile);
		} catch (AltimeterIOException e) {
			displayError(e);
		}
	}

	private void doExportData() {
		AltimeterChartPanel panel = (AltimeterChartPanel)pane.getSelectedComponent();
		if(panel == null) return;
		JFileChooser jfc = new JFileChooser(lastDirectory);
		jfc.addChoosableFileFilter(fileFilterXls);
		jfc.addChoosableFileFilter(fileFilterOds);
		jfc.addChoosableFileFilter(fileFilterTsv);
		jfc.addChoosableFileFilter(fileFilterCsv);
		jfc.addChoosableFileFilter(fileFilterSsv);
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setFileFilter(fileFilterXls);

		if(jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

		lastDirectory = jfc.getCurrentDirectory();
		ExportDataFileFilter selectedFilter = (ExportDataFileFilter) jfc.getFileFilter();
		File selectedFile = selectedFilter.ensureFileExtension(jfc.getSelectedFile());
		IExportDataAdapter export = selectedFilter.getAdapter();
		AltimeterSession currentSession = panel.getSession();
		new ExportDataWorker(export, currentFile, currentSession, selectedFile).execute();
	}
	
	private void doExportChart() {
		JPanel panel = (JPanel)pane.getSelectedComponent();
		if(panel == null) return;
		ChartPanel chartPanel = (ChartPanel) panel.getComponent(0);
		JFileChooser jfc = new JFileChooser(lastDirectory);
		jfc.addChoosableFileFilter(fileFilterPng);
		jfc.addChoosableFileFilter(fileFilterJpg);
		jfc.addChoosableFileFilter(fileFilterSvg);
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setFileFilter(fileFilterPng);
		if(jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
		
		lastDirectory = jfc.getCurrentDirectory();
		JFreeChart chart = chartPanel.getChart();
		int x = panel.getWidth();
		int y = panel.getHeight();
		ExportChartFileFilter selectedFilter = (ExportChartFileFilter) jfc.getFileFilter();
		File selectedFile = selectedFilter.ensureFileExtension(jfc.getSelectedFile());
		IExportChartAdapter export = selectedFilter.getAdapter();

		new ExportChartWorker(export, chart, selectedFile, x, y).execute();
	}

	private void doExit() {
		setVisible(false);
		dispose();
	}
	
	private void createCharts(AltimeterFile altimeterFile) {
		if(null == altimeterFile) return; // TODO display error ??
		exportData.setEnabled(true);
		exportChart.setEnabled(true);

		int i = 0;
		for(AltimeterSession session : altimeterFile.getSessions()) {
			i++;
			AltimeterChartPanel panel = new AltimeterChartPanel(session, i);
            
			pane.add(panel.getTitle(), panel);
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

	public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {  //Note 1
            public void run() {
            	AltimeterVisualization window = new AltimeterVisualization();
        		if(args != null && args.length == 1)
        			window.readFile(new File(args[0]));
                window.setVisible(true);
            }
        });

		

	}

}
