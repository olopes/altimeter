package org.psicover.altimeter.ui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import org.psicover.altimeter.IPreferences;
import org.psicover.altimeter.Preferences;

public class PreferencesDialog extends JDialog {

	private static final long serialVersionUID = 2037321946718054510L;
	private BeanPropertyModel beanModel = new BeanPropertyModel(Preferences.getInstance()); 

	public PreferencesDialog(JFrame parent) {
		super(parent, "Preferences");
		setupUI();
	}
	
	private void setupUI() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		IPreferences prefs = Preferences.getInstance();
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
	
		JPanel stuff = new JPanel();
		container.add(new JScrollPane(stuff), BorderLayout.CENTER);
		
		AbstractFormatter intFormatter = new NumberFormatter(new DecimalFormat("0"));
		AbstractFormatterFactory intFactory = new DefaultFormatterFactory(intFormatter);

		AbstractFormatter doubleFormatter = new NumberFormatter(new DecimalFormat("0.000"));
		AbstractFormatterFactory doubleFactory = new DefaultFormatterFactory(doubleFormatter);
		
		stuff.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx=1.0;
		gc.weighty=1.0;
		gc.gridx=0;
		gc.gridy=0;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.insets=new Insets(1,1,1,1);
	
		{
			PropPanel aec = new PropPanel("Algorithms and Constants");
			aec.addRow("Sea Level Pressure", new JFormattedTextField(intFactory, prefs.getSeaLevelPressure()), "seaLevelPressure");
			//  (specific gas constant for dry air)
			aec.addRow("R", new JFormattedTextField(doubleFactory, prefs.getR()), "R");
			// Unknown? precomputed?
			aec.addRow("P0", new JFormattedTextField(doubleFactory, prefs.getR()), "P0");
			JComboBox<String> altiForm = new JComboBox<>(new String[]{"keisan","p2a","wiki"});
			altiForm.setSelectedItem(prefs.getAltitudeFormula());
			aec.addRow("Altitude Formula", altiForm, "altitudeFormula");
			gc.gridy=0;
			stuff.add(aec, gc);
		}
		
		{
			PropPanel aec = new PropPanel("Curve Smoothing");
			aec.addRow("Window Size", new JFormattedTextField(intFactory, prefs.getSmoothWindowSize()), "smoothWindowSize");
			gc.gridy=1;
			stuff.add(aec, gc);
		}
		
		{
			PropPanel aec = new PropPanel("Flight Detection");
			JComboBox<String> fdds = new JComboBox<>(new String[]{"raw","smooth"});
			fdds.setSelectedItem(prefs.getFlightDetectionDataset());
			aec.addRow("Dataset", fdds, "flightDetectionDataset");
			aec.addRow("Window Size", new JFormattedTextField(intFactory, prefs.getFlightWindowSize()), "flightWindowSize");
			aec.addRow("Launch Delta", new JFormattedTextField(doubleFactory, prefs.getLaunchDelta()), "launchDelta");
			aec.addRow("Landing Delta", new JFormattedTextField(doubleFactory, prefs.getLandingDelta()), "landingDelta");
			gc.gridy=2;
			stuff.add(aec, gc);
		}
		JButton okButton = new JButton("Ok");
		JButton cancelButton = new JButton("Cancel");
		
		okButton.addActionListener(l->saveAndClose());
		cancelButton.addActionListener(l->closeWindow());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		container.add(buttonPanel, BorderLayout.SOUTH);
		setMinimumSize(new Dimension(320, 240));
		pack();
	}
	
	private class PropPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3080656400580154872L;
		GridBagConstraints gc = new GridBagConstraints();
		
		public PropPanel(String title) {
			initUI();
			setBorder(BorderFactory.createTitledBorder(title));
		}
		
		private void initUI() {
			setLayout(new GridBagLayout());
			gc.gridx=0;
			gc.gridy=0;
			gc.weightx=1.0;
			gc.insets=new Insets(2, 2, 2, 2);
			gc.fill=GridBagConstraints.HORIZONTAL;
			
		}
		public void addRow(String label, JFormattedTextField component, String propName) {
			addRow(label, component);
			if(propName != null) {
				component.addPropertyChangeListener("value", beanModel.bindListener(propName));
			}
		}
		
		public void addRow(String label, JTextField component, String propName) {
			addRow(label, component);
			if(propName != null) {
				component.getDocument().addDocumentListener(beanModel.bindListener(propName));
			}
		}
		
		public void addRow(String label, JComboBox component, String propName) {
			addRow(label, component);
			if(propName != null) {
				component.addItemListener(beanModel.bindListener(propName));
			}
		}
		
		private void addRow(String label, JComponent component) {
			JLabel lbl = new JLabel(label==null?" ":label, SwingConstants.TRAILING);
			gc.gridx=0;
			gc.anchor=GridBagConstraints.LINE_END;
			add(lbl, gc);

			if(component != null) {
				lbl.setLabelFor(component);
				gc.gridx=1;
				gc.anchor=GridBagConstraints.LINE_START;
				add(component, gc);
			}
			gc.gridy++;
			
		}
		
	}
	
	private void saveAndClose() {
		Preferences.savePreferences();
		closeWindow();
	}
	
	private void closeWindow() {
		this.setVisible(false);
		this.dispose();
	}

}
