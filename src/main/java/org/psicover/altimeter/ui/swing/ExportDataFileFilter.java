package org.psicover.altimeter.ui;

import org.psicover.altimeter.io.IExportDataAdapter;

// I'm sad, FileNameExtensionFilter is final...
public class ExportDataFileFilter extends ExportActionFileFilter {
	private final IExportDataAdapter action;

	public ExportDataFileFilter(IExportDataAdapter action, String description, String ... extensions) {
		super(description, extensions);
		this.action = action;
	}
	
	public IExportDataAdapter getAdapter() {
		return action;
	}
}
