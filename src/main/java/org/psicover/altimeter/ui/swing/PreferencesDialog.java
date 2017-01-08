package org.psicover.altimeter.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class PreferencesDialog extends JDialog {

	private static final long serialVersionUID = 2037321946718054510L;
	
	PropertyChangeSupport ss;
	
	public PreferencesDialog(JFrame parent) {
		super(parent, "Preferences");
		setupUI();
	}
	
	private void setupUI() {
		PropertyChangeListener pcl;
	}

}
