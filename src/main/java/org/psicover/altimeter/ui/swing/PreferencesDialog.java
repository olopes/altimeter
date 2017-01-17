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
import org.psicover.altimeter.Messages;
import org.psicover.altimeter.Preferences;

public class PreferencesDialog extends JDialog {

	private static final long serialVersionUID = 2037321946718054510L;
	private BeanPropertyModel beanModel = new BeanPropertyModel(Preferences.getInstance()); 

	public PreferencesDialog(JFrame parent) {
		super(parent, Messages.getString("PreferencesDialog.0")); //$NON-NLS-1$
		setupUI();
	}
	
	private void setupUI() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		IPreferences prefs = Preferences.getInstance();
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
	
		JPanel stuff = new JPanel();
		container.add(new JScrollPane(stuff), BorderLayout.CENTER);
		
		AbstractFormatter intFormatter = new NumberFormatter(new DecimalFormat("0")); //$NON-NLS-1$
		AbstractFormatterFactory intFactory = new DefaultFormatterFactory(intFormatter);

		AbstractFormatter doubleFormatter = new NumberFormatter(new DecimalFormat("0.000")); //$NON-NLS-1$
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
			PropPanel aec = new PropPanel(Messages.getString("PreferencesDialog.3")); //$NON-NLS-1$
			aec.addRow(Messages.getString("PreferencesDialog.4"), new JFormattedTextField(intFactory, prefs.getSeaLevelPressure()), "seaLevelPressure"); //$NON-NLS-1$ //$NON-NLS-2$
			//  (specific gas constant for dry air)
			aec.addRow(Messages.getString("PreferencesDialog.6"), new JFormattedTextField(doubleFactory, prefs.getR()), "R"); //$NON-NLS-1$ //$NON-NLS-2$
			
			JComboBox<String> altiForm = new JComboBox<>(new String[]{"keisan","wiki"}); //$NON-NLS-1$ //$NON-NLS-2$
			altiForm.setSelectedItem(prefs.getAltitudeFormula());
			aec.addRow(Messages.getString("PreferencesDialog.13"), altiForm, "altitudeFormula"); //$NON-NLS-1$ //$NON-NLS-2$
			gc.gridy=0;
			stuff.add(aec, gc);
		}
		
		{
			PropPanel aec = new PropPanel(Messages.getString("PreferencesDialog.15")); //$NON-NLS-1$
			aec.addRow(Messages.getString("PreferencesDialog.16"), new JFormattedTextField(intFactory, prefs.getSmoothWindowSize()), "smoothWindowSize"); //$NON-NLS-1$ //$NON-NLS-2$
			gc.gridy=1;
			stuff.add(aec, gc);
		}
		
		{
			PropPanel aec = new PropPanel(Messages.getString("PreferencesDialog.18")); //$NON-NLS-1$
			JComboBox<String> fdds = new JComboBox<>(new String[]{Messages.getString("PreferencesDialog.19"),Messages.getString("PreferencesDialog.20")}); //$NON-NLS-1$ //$NON-NLS-2$
			fdds.setSelectedItem(prefs.getFlightDetectionDataset());
			aec.addRow(Messages.getString("PreferencesDialog.21"), fdds, "flightDetectionDataset"); //$NON-NLS-1$ //$NON-NLS-2$
			aec.addRow(Messages.getString("PreferencesDialog.23"), new JFormattedTextField(intFactory, prefs.getFlightWindowSize()), "flightWindowSize"); //$NON-NLS-1$ //$NON-NLS-2$
			aec.addRow(Messages.getString("PreferencesDialog.25"), new JFormattedTextField(doubleFactory, prefs.getLaunchDelta()), "launchDelta"); //$NON-NLS-1$ //$NON-NLS-2$
			aec.addRow(Messages.getString("PreferencesDialog.27"), new JFormattedTextField(doubleFactory, prefs.getLandingDelta()), "landingDelta"); //$NON-NLS-1$ //$NON-NLS-2$
			gc.gridy=2;
			stuff.add(aec, gc);
		}
		JButton okButton = new JButton(Messages.getString("PreferencesDialog.29")); //$NON-NLS-1$
		JButton cancelButton = new JButton(Messages.getString("PreferencesDialog.30")); //$NON-NLS-1$
		
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
				component.addPropertyChangeListener("value", beanModel.bindListener(propName)); //$NON-NLS-1$
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
			JLabel lbl = new JLabel(label==null?" ":label, SwingConstants.TRAILING); //$NON-NLS-1$
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
