package org.psicover.altimeter.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.psicover.altimeter.Messages;
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
	private JMenu fileMenu = new JMenu(Messages.getString("AltimeterVisualization.0")); //$NON-NLS-1$
	private JMenuItem open = new JMenuItem(Messages.getString("AltimeterVisualization.1")); //$NON-NLS-1$
	private JMenuItem exportData = new JMenuItem(Messages.getString("AltimeterVisualization.2")); //$NON-NLS-1$
	private JMenuItem exportChart = new JMenuItem(Messages.getString("AltimeterVisualization.3")); //$NON-NLS-1$
	private JMenuItem preferences = new JMenuItem(Messages.getString("AltimeterVisualization.4")); //$NON-NLS-1$
	private JMenuItem exit = new JMenuItem(Messages.getString("AltimeterVisualization.5")); //$NON-NLS-1$
	private File lastDirectory = new File("."); //$NON-NLS-1$
	private AltimeterFile currentFile;
	
	private FileFilter fileFilterXls = new ExportDataFileFilter(XlsxFileWriter.getInstance(),Messages.getString("AltimeterVisualization.7"), "xlsx"); //$NON-NLS-1$ //$NON-NLS-2$
	private FileFilter fileFilterOds = new ExportDataFileFilter(OdsFileWriter.getInstance(),Messages.getString("AltimeterVisualization.9"), "ods"); //$NON-NLS-1$ //$NON-NLS-2$
	private FileFilter fileFilterTsv = new ExportDataFileFilter(DlmFileWriter.getTsvInstance(),Messages.getString("AltimeterVisualization.11"), "tsv", "tab"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private FileFilter fileFilterCsv = new ExportDataFileFilter(DlmFileWriter.getCsvInstance(),Messages.getString("AltimeterVisualization.14"), "csv"); //$NON-NLS-1$ //$NON-NLS-2$
	private FileFilter fileFilterSsv = new ExportDataFileFilter(DlmFileWriter.getSsvInstance(),Messages.getString("AltimeterVisualization.16"), "csv"); //$NON-NLS-1$ //$NON-NLS-2$

	private FileFilter fileFilterPng = new ExportChartFileFilter(PngFileWriter.getInstance(),Messages.getString("AltimeterVisualization.18"), "png"); //$NON-NLS-1$ //$NON-NLS-2$
	private FileFilter fileFilterJpg = new ExportChartFileFilter(JpegFileWriter.getInstance(),Messages.getString("AltimeterVisualization.20"), "jpg", "jpeg"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private FileFilter fileFilterSvg = new ExportChartFileFilter(SvgFileWriter.getInstance(),Messages.getString("AltimeterVisualization.23"), "svg"); //$NON-NLS-1$ //$NON-NLS-2$
	
	public AltimeterVisualization() {
		setupUI();
	}
	
	private void setupUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(Messages.getString("AltimeterVisualization.25")); //$NON-NLS-1$
		// setExtendedState(getExtendedState()|JFrame.MAXIMIZED_BOTH );
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(pane, BorderLayout.CENTER);
		
		exportData.setEnabled(false);
		exportChart.setEnabled(false);
		fileMenu.add(open);
		fileMenu.addSeparator();
		fileMenu.add(exportData);
		fileMenu.add(exportChart);
		fileMenu.addSeparator();
		fileMenu.add(preferences);
		fileMenu.addSeparator();
		fileMenu.add(exit);
		
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		exportData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
		exportChart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		preferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		
		open.addActionListener(l->doOpen());
		exportData.addActionListener(l->doExportData());
		exportChart.addActionListener(l->doExportChart());
		preferences.addActionListener(l->openPreferences());
		exit.addActionListener(l->doExit());

		
		JMenuBar mbar = new JMenuBar();
		mbar.add(fileMenu);
		setJMenuBar(mbar);
		setMinimumSize(new Dimension(640, 480));
		pack();
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
				JOptionPane.showMessageDialog(AltimeterVisualization.this, Messages.getString("AltimeterVisualization.26")); //$NON-NLS-1$
			else
				displayError(Messages.getString("AltimeterVisualization.27", outputFile.getName()), error); //$NON-NLS-1$
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
				JOptionPane.showMessageDialog(AltimeterVisualization.this, Messages.getString("AltimeterVisualization.28")); //$NON-NLS-1$
			else
				displayError(Messages.getString("AltimeterVisualization.29", outputFile.getName()), error); //$NON-NLS-1$
			exportChart.setEnabled(true);
		}
	}
	
	
	private void doOpen() {
		JFileChooser jfc = new JFileChooser(lastDirectory);
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(Messages.getString("AltimeterVisualization.30"), "hka", "fda"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
			displayError(Messages.getString("AltimeterVisualization.33", f.getName()), e); //$NON-NLS-1$
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
	
	private void openPreferences() {
		new PreferencesDialog(this).setVisible(true);
	}
	
	private void createCharts(AltimeterFile altimeterFile) {
		if(null == altimeterFile) return; // TODO display error ??
		exportData.setEnabled(true);
		exportChart.setEnabled(true);

		AltimeterSession[] sessions = altimeterFile.getSessions();
		for(int i = 0; i < sessions.length; i++) {
			AltimeterChartPanel panel = new AltimeterChartPanel(sessions[i], i+1);
			pane.add(panel.getTitle(), panel);
		}
	}
	
	private void displayError(String msg, Throwable t) {
		if(t == null) return;
		// adapted from http://code.makery.ch/blog/javafx-dialogs-official/		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setPreferredSize(new Dimension(400, 300));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.fill=GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel(msg), c);
		c.gridy=1;
		c.fill=GridBagConstraints.BOTH;
		c.weightx = c.weighty = 1;
		StringWriter sw = new StringWriter(4096);
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		JTextArea ta = new JTextArea(sw.toString());
		ta.setEditable(false);
		// ta.setLineWrap(true);
		panel.add(new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), c);
		
		// parent set to null to display the box centered on screen.
		JOptionPane.showMessageDialog(null, panel, Messages.getString("AltimeterVisualization.34"), JOptionPane.ERROR_MESSAGE);		 //$NON-NLS-1$
	}

	public static void main(final String[] args) {

		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		SwingUtilities.invokeLater(new Runnable() { // Note 1
			public void run() {
				AltimeterVisualization window = new AltimeterVisualization();
				if (args != null && args.length == 1)
					window.readFile(new File(args[0]));
				window.setVisible(true);
			}
		});

	}

}
